package co.sena.gestionpedidos.web.servlet;

import co.sena.gestionpedidos.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * Inicio y cierre de sesión.
 * <ul>
 *   <li>GET /login: muestra el formulario de ingreso.</li>
 *   <li>POST /login: valida correo y contraseña (se usa POST para que la clave no viaje en la URL).</li>
 *   <li>GET /logout: invalida la sesión.</li>
 * </ul>
 *
 * @author Rubén Darío Herrera Beltrán
 */
@WebServlet(name = "loginServlet", urlPatterns = {"/login", "/logout"})
public class LoginServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    /** Atributo de sesión con el usuario autenticado. */
    public static final String ATRIBUTO_USUARIO_SESION = "usuarioSesion";
    private static final int MINUTOS_INACTIVIDAD = 30;

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        if ("/logout".equals(solicitud.getServletPath())) {
            HttpSession sesion = solicitud.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }
            redirigirConMensaje(solicitud, respuesta, "/login", "exito", "Sesión cerrada correctamente");
            return;
        }
        mostrarVista(solicitud, respuesta, "login");
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String correo = leerTexto(solicitud, "correo");
        String clave = solicitud.getParameter("clave");
        Optional<Usuario> usuario = correo.isEmpty() || clave == null
                ? Optional.empty()
                : getUsuarioServicio().autenticar(correo, clave);
        if (usuario.isEmpty()) {
            solicitud.setAttribute("error", "Correo o contraseña incorrectos");
            solicitud.setAttribute("correo", correo);
            mostrarVista(solicitud, respuesta, "login");
            return;
        }
        solicitud.changeSessionId();
        HttpSession sesion = solicitud.getSession();
        sesion.setAttribute(ATRIBUTO_USUARIO_SESION, usuario.get());
        sesion.setMaxInactiveInterval(MINUTOS_INACTIVIDAD * 60);
        redirigirConMensaje(solicitud, respuesta, "/inicio", "exito",
                "Bienvenido(a), " + usuario.get().getNombre());
    }
}
