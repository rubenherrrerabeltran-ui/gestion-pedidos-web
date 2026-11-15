package co.sena.gestionpedidos.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Locale;
import java.util.UUID;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de integración de los servlets y las páginas JSP. Arranca un Tomcat embebido con la
 * carpeta src/main/webapp y una base H2 en memoria (modo MySQL) cargada con los scripts del
 * proyecto, y envía peticiones HTTP reales con los métodos GET y POST.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("Servlets y JSP - peticiones HTTP GET y POST")
class ServletsIntegracionTest {

    private static final String CONTEXTO = "/gestion-pedidos";
    private static Tomcat tomcat;
    private static String urlBase;

    private HttpClient cliente;

    @BeforeAll
    static void iniciarServidor() throws Exception {
        String urlBd = "jdbc:h2:mem:web_" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        cargarBaseDeDatos(urlBd);
        System.setProperty("db.url", urlBd);
        System.setProperty("db.usuario", "sa");
        System.setProperty("db.clave", "");

        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.setBaseDir(Files.createTempDirectory("tomcat-prueba").toString());
        tomcat.getConnector();
        tomcat.setAddDefaultWebXmlToWebapp(false);
        Context contexto = tomcat.addWebapp(CONTEXTO, new File("src/main/webapp").getAbsolutePath());
        Tomcat.initWebappDefaults(contexto);
        String compiladorJsp = System.getProperty("jsp.compilador");
        if (compiladorJsp != null) {
            ((Wrapper) contexto.findChild("jsp")).addInitParameter("compilerClassName", compiladorJsp);
        }
        StandardRoot recursos = new StandardRoot(contexto);
        recursos.addPreResources(new DirResourceSet(recursos, "/WEB-INF/classes",
                new File(System.getProperty("ruta.clases", "target/classes")).getAbsolutePath(), "/"));
        contexto.setResources(recursos);
        tomcat.start();
        urlBase = "http://localhost:" + tomcat.getConnector().getLocalPort() + CONTEXTO;
    }

    @AfterAll
    static void detenerServidor() throws Exception {
        tomcat.stop();
        tomcat.destroy();
    }

    @BeforeEach
    void crearCliente() {
        cliente = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @Test
    @DisplayName("GET sin sesión: el filtro redirige al formulario de ingreso (302)")
    void sinSesionRedirigeAlLogin() throws Exception {
        HttpResponse<String> respuesta = get("/productos");

        assertEquals(302, respuesta.statusCode());
        assertTrue(respuesta.headers().firstValue("Location").orElse("").endsWith("/login"));
    }

    @Test
    @DisplayName("POST /login con clave incorrecta muestra el error en el JSP")
    void loginIncorrecto() throws Exception {
        HttpResponse<String> respuesta = post("/login", "correo=admin@gestionpedidos.local&clave=ClaveErronea1");

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Correo o contraseña incorrectos"));
    }

    @Test
    @DisplayName("POST /login correcto crea la sesión y redirige a /inicio con elementos JSP")
    void loginCorrecto() throws Exception {
        HttpResponse<String> respuesta = iniciarSesion("admin@gestionpedidos.local", "Admin2026*");
        HttpResponse<String> inicio = get("/inicio");

        assertEquals(302, respuesta.statusCode());
        assertTrue(respuesta.headers().firstValue("Location").orElse("").endsWith("/inicio"));
        assertTrue(inicio.body().contains("Hola, Administrador del sistema"), "EL con sessionScope");
        assertTrue(inicio.body().contains("class=\"tarjeta\""), "archivo de etiquetas tarjeta.tag");
        assertTrue(inicio.body().contains("Servidor: Apache Tomcat"), "objeto implícito application");
    }

    @Test
    @DisplayName("GET /productos?texto=café filtra con el parámetro de la URL")
    void buscarProductoPorGet() throws Exception {
        iniciarSesion("admin@gestionpedidos.local", "Admin2026*");

        HttpResponse<String> respuesta = get("/productos?texto=" + codificar("café"));

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Café de origen Huila 500 g"));
        assertFalse(respuesta.body().contains("Panela orgánica 1 kg"));
    }

    @Test
    @DisplayName("POST /productos inserta, actualiza y elimina un producto")
    void crudDeProductoPorPost() throws Exception {
        iniciarSesion("laura.vendedora@gestionpedidos.local", "Vendedor2026*");

        HttpResponse<String> insercion = post("/productos",
                "accion=guardar&id=&nombreProducto=" + codificar("Miel de abejas 500 g")
                        + "&precio=18500&stock=22&activo=on");
        String lista = get("/productos").body();
        String idNuevo = extraerIdProducto(lista, "Miel de abejas 500 g");
        HttpResponse<String> actualizacion = post("/productos",
                "accion=guardar&id=" + idNuevo + "&nombreProducto=" + codificar("Miel de abejas 1 kg")
                        + "&precio=32000&stock=10&activo=on");
        String listaActualizada = get("/productos").body();
        HttpResponse<String> eliminacion = post("/productos", "accion=eliminar&id=" + idNuevo);
        String listaFinal = get("/productos").body();

        assertEquals(302, insercion.statusCode());
        assertTrue(lista.contains("Miel de abejas 500 g"));
        assertEquals(302, actualizacion.statusCode());
        assertTrue(listaActualizada.contains("Miel de abejas 1 kg"));
        assertEquals(302, eliminacion.statusCode());
        assertFalse(listaFinal.contains("Miel de abejas"));
    }

    @Test
    @DisplayName("POST /pedidos registra un pedido con parámetros repetidos y descuenta el stock")
    void registrarPedidoPorPost() throws Exception {
        iniciarSesion("admin@gestionpedidos.local", "Admin2026*");

        HttpResponse<String> respuesta = post("/pedidos",
                "accion=registrar&idCliente=3&producto=1&cantidad=2&producto=2&cantidad=3&producto=&cantidad=");
        String ubicacion = respuesta.headers().firstValue("Location").orElse("");
        String detalle = get(ubicacion.substring(ubicacion.indexOf("/pedidos"))).body();

        assertEquals(302, respuesta.statusCode());
        assertTrue(ubicacion.contains("accion=ver"));
        assertTrue(detalle.contains("Café de origen Huila 500 g"));
        assertTrue(detalle.contains("75.500"), "total calculado: 2 x 28.000 + 3 x 6.500");
    }

    @Test
    @DisplayName("Un VENDEDOR no puede entrar al módulo de usuarios")
    void autorizacionPorRol() throws Exception {
        iniciarSesion("laura.vendedora@gestionpedidos.local", "Vendedor2026*");

        HttpResponse<String> respuesta = get("/usuarios");

        assertEquals(302, respuesta.statusCode());
        assertTrue(get("/inicio").body().contains("Solo el rol ADMINISTRADOR puede gestionar usuarios"));
    }

    @Test
    @DisplayName("Una ruta inexistente muestra la página de error JSP (404)")
    void paginaDeError() throws Exception {
        iniciarSesion("admin@gestionpedidos.local", "Admin2026*");

        HttpResponse<String> respuesta = get("/pagina-que-no-existe");

        assertEquals(404, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Error 404"));
    }

    // ---------------------------------------------------------------- utilidades

    private HttpResponse<String> iniciarSesion(String correo, String clave) throws Exception {
        return post("/login", "correo=" + codificar(correo) + "&clave=" + codificar(clave));
    }

    private HttpResponse<String> get(String ruta) throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlBase + ruta)).GET().build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> post(String ruta, String formulario) throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlBase + ruta))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(formulario, StandardCharsets.UTF_8))
                .build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private static String extraerIdProducto(String html, String nombre) {
        int posicionNombre = html.indexOf(nombre);
        int inicioFila = html.lastIndexOf("<tr>", posicionNombre);
        String fila = html.substring(inicioFila, posicionNombre);
        return fila.replaceAll("(?s).*<td>(\\d+)</td>.*", "$1").trim();
    }

    private static void cargarBaseDeDatos(String urlBd) throws Exception {
        try (Connection conexion = DriverManager.getConnection(urlBd, "sa", "");
             Statement sentencia = conexion.createStatement()) {
            for (String archivo : new String[] {"01_esquema.sql", "02_datos_prueba.sql"}) {
                String script = Files.readString(Path.of("database", archivo), StandardCharsets.UTF_8)
                        .replaceAll("(?m)^--.*$", "");
                for (String instruccion : script.split(";")) {
                    String limpia = instruccion.strip();
                    String mayusculas = limpia.toUpperCase(Locale.ROOT);
                    if (mayusculas.startsWith("CREATE TABLE") || mayusculas.startsWith("INSERT")) {
                        sentencia.execute(limpia);
                    }
                }
            }
        }
    }
}
