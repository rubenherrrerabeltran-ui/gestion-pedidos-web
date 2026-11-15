package co.sena.gestionpedidos.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * Administra los datos de conexión a la base de datos y entrega conexiones JDBC.
 *
 * <p>Los valores se leen del archivo {@code db.properties} del classpath y pueden
 * reemplazarse con propiedades de sistema ({@code -Ddb.url}) o variables de entorno
 * ({@code DB_URL}, {@code DB_USUARIO}, {@code DB_CLAVE}), así la contraseña real no
 * queda escrita en el código ni en el repositorio.</p>
 *
 * <p>Cada llamada a {@link #obtenerConexion()} abre una conexión nueva que debe
 * cerrarse con <em>try-with-resources</em>.</p>
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class ConexionBD {

    private static final String ARCHIVO_PROPIEDADES = "db.properties";
    private static final String PROPIEDAD_URL = "db.url";
    private static final String PROPIEDAD_USUARIO = "db.usuario";
    private static final String PROPIEDAD_CLAVE = "db.clave";

    private final String url;
    private final String usuario;
    private final String clave;

    /**
     * Crea la configuración de conexión con valores explícitos.
     *
     * @param url     cadena JDBC, por ejemplo {@code jdbc:mysql://localhost:3306/bd}
     * @param usuario usuario de la base de datos
     * @param clave   contraseña del usuario (puede ser vacía en entornos locales)
     */
    public ConexionBD(String url, String usuario, String clave) {
        this.url = Objects.requireNonNull(url, "La URL de conexión es obligatoria");
        this.usuario = Objects.requireNonNullElse(usuario, "");
        this.clave = Objects.requireNonNullElse(clave, "");
    }

    /**
     * Construye la configuración a partir de {@code db.properties}, aplicando las
     * propiedades de sistema y variables de entorno cuando existen.
     *
     * @return configuración lista para abrir conexiones
     * @throws IllegalStateException si el archivo de propiedades no existe o no se puede leer
     */
    public static ConexionBD desdeConfiguracion() {
        Properties propiedades = new Properties();
        try (InputStream entrada = ConexionBD.class.getClassLoader()
                .getResourceAsStream(ARCHIVO_PROPIEDADES)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontró " + ARCHIVO_PROPIEDADES + " en el classpath");
            }
            propiedades.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible leer " + ARCHIVO_PROPIEDADES, e);
        }
        String urlConfigurada = leerValor(PROPIEDAD_URL, "DB_URL", propiedades);
        String usuarioConfigurado = leerValor(PROPIEDAD_USUARIO, "DB_USUARIO", propiedades);
        String claveConfigurada = leerValor(PROPIEDAD_CLAVE, "DB_CLAVE", propiedades);
        return new ConexionBD(urlConfigurada, usuarioConfigurado, claveConfigurada);
    }

    /**
     * Abre una nueva conexión JDBC con el {@link DriverManager}.
     *
     * @return conexión abierta; quien la recibe es responsable de cerrarla
     * @throws SQLException si el servidor no responde o las credenciales no son válidas
     */
    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    /**
     * Verifica que la base de datos responda abriendo y cerrando una conexión.
     *
     * @return {@code true} si la conexión es válida en menos de dos segundos
     */
    public boolean probarConexion() {
        try (Connection conexion = obtenerConexion()) {
            return conexion.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Devuelve la URL de conexión sin parámetros, útil para mostrarla en consola.
     *
     * @return URL sin la parte de parámetros ({@code ?...} o {@code ;...})
     */
    public String getUrlSinParametros() {
        int posicionParametros = url.indexOf('?');
        if (posicionParametros < 0) {
            posicionParametros = url.indexOf(';');
        }
        return posicionParametros < 0 ? url : url.substring(0, posicionParametros);
    }

    private static String leerValor(String propiedad, String variableEntorno, Properties propiedades) {
        String valorSistema = System.getProperty(propiedad);
        if (valorSistema != null && !valorSistema.isBlank()) {
            return valorSistema;
        }
        String valorEntorno = System.getenv(variableEntorno);
        if (valorEntorno != null && !valorEntorno.isBlank()) {
            return valorEntorno;
        }
        return propiedades.getProperty(propiedad, "");
    }
}
