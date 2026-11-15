package co.sena.gestionpedidos.web.servlet;

import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.servicio.PedidoServicio;
import co.sena.gestionpedidos.servicio.ProductoServicio;
import co.sena.gestionpedidos.servicio.UsuarioServicio;
import co.sena.gestionpedidos.web.listener.ContextoAplicacionListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Clase base de los servlets del sistema. Reúne las operaciones comunes: obtener los
 * servicios del ámbito de aplicación, enviar la petición a una vista JSP, redirigir con
 * un mensaje (patrón Post/Redirect/Get) y leer parámetros con validación.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public abstract class ServletBase extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Carpeta protegida donde están las vistas JSP (no accesibles directamente desde el navegador). */
    private static final String CARPETA_VISTAS = "/WEB-INF/vistas/";
    /** Atributo de sesión con el texto del mensaje que se muestra una sola vez. */
    public static final String ATRIBUTO_MENSAJE = "mensajeFlash";
    /** Atributo de sesión con el tipo de mensaje: exito o error. */
    public static final String ATRIBUTO_TIPO_MENSAJE = "tipoMensajeFlash";

    protected ProductoServicio getProductoServicio() {
        return (ProductoServicio) getServletContext()
                .getAttribute(ContextoAplicacionListener.ATRIBUTO_PRODUCTO_SERVICIO);
    }

    protected UsuarioServicio getUsuarioServicio() {
        return (UsuarioServicio) getServletContext()
                .getAttribute(ContextoAplicacionListener.ATRIBUTO_USUARIO_SERVICIO);
    }

    protected PedidoServicio getPedidoServicio() {
        return (PedidoServicio) getServletContext()
                .getAttribute(ContextoAplicacionListener.ATRIBUTO_PEDIDO_SERVICIO);
    }

    /**
     * Reenvía (forward) la petición a una vista JSP dentro de /WEB-INF/vistas.
     *
     * @param vista ruta de la vista sin extensión, por ejemplo {@code productos/lista}
     */
    protected void mostrarVista(HttpServletRequest solicitud, HttpServletResponse respuesta, String vista)
            throws ServletException, IOException {
        solicitud.getRequestDispatcher(CARPETA_VISTAS + vista + ".jsp").forward(solicitud, respuesta);
    }

    /**
     * Guarda un mensaje en la sesión y redirige con GET a la ruta indicada (Post/Redirect/Get),
     * para que al recargar la página no se reenvíe el formulario.
     */
    protected void redirigirConMensaje(HttpServletRequest solicitud, HttpServletResponse respuesta,
                                       String ruta, String tipo, String mensaje) throws IOException {
        HttpSession sesion = solicitud.getSession();
        sesion.setAttribute(ATRIBUTO_MENSAJE, mensaje);
        sesion.setAttribute(ATRIBUTO_TIPO_MENSAJE, tipo);
        respuesta.sendRedirect(solicitud.getContextPath() + ruta);
    }

    /**
     * @return valor del parámetro sin espacios, o cadena vacía si no viene
     */
    protected String leerTexto(HttpServletRequest solicitud, String nombre) {
        String valor = solicitud.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }

    /**
     * @return parámetro convertido a entero
     * @throws IllegalArgumentException si no es un número entero
     */
    protected int leerEntero(HttpServletRequest solicitud, String nombre) {
        String valor = leerTexto(solicitud, nombre);
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + nombre + " debe ser un número entero");
        }
    }

    /**
     * @return parámetro convertido a decimal (acepta coma o punto)
     * @throws IllegalArgumentException si no es numérico
     */
    protected BigDecimal leerDecimal(HttpServletRequest solicitud, String nombre) {
        String valor = leerTexto(solicitud, nombre).replace(',', '.');
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + nombre + " debe ser un valor numérico");
        }
    }

    /**
     * @return usuario que inició sesión (el filtro garantiza que existe)
     */
    protected Usuario usuarioActual(HttpServletRequest solicitud) {
        HttpSession sesion = solicitud.getSession(false);
        return sesion == null ? null : (Usuario) sesion.getAttribute(LoginServlet.ATRIBUTO_USUARIO_SESION);
    }

    /**
     * @return {@code true} si el usuario en sesión tiene alguno de los roles indicados
     */
    protected boolean tieneRol(HttpServletRequest solicitud, Rol... roles) {
        Usuario usuario = usuarioActual(solicitud);
        if (usuario == null) {
            return false;
        }
        for (Rol rol : roles) {
            if (usuario.getRol() == rol) {
                return true;
            }
        }
        return false;
    }
}
