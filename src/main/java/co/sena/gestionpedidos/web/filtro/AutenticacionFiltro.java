package co.sena.gestionpedidos.web.filtro;

import co.sena.gestionpedidos.web.servlet.LoginServlet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Permite el acceso a los módulos solo a usuarios que iniciaron sesión. Deja pasar
 * la página de ingreso y los recursos estáticos (hojas de estilo).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@WebFilter(filterName = "autenticacionFiltro", urlPatterns = "/*")
public class AutenticacionFiltro extends HttpFilter {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doFilter(HttpServletRequest solicitud, HttpServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        String ruta = solicitud.getServletPath();
        boolean rutaPublica = ruta.equals("/login") || ruta.startsWith("/css/");
        HttpSession sesion = solicitud.getSession(false);
        boolean autenticado = sesion != null && sesion.getAttribute(LoginServlet.ATRIBUTO_USUARIO_SESION) != null;
        if (rutaPublica || autenticado) {
            cadena.doFilter(solicitud, respuesta);
        } else {
            respuesta.sendRedirect(solicitud.getContextPath() + "/login");
        }
    }
}
