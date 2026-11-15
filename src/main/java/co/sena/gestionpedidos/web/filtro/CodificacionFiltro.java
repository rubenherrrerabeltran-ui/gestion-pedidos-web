package co.sena.gestionpedidos.web.filtro;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Garantiza que los parámetros de los formularios (tildes, eñes) se lean en UTF-8.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@WebFilter(filterName = "codificacionFiltro", urlPatterns = "/*")
public class CodificacionFiltro extends HttpFilter {

    private static final long serialVersionUID = 1L;

    @Override
    public void doFilter(ServletRequest solicitud, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        if (solicitud.getCharacterEncoding() == null) {
            solicitud.setCharacterEncoding(StandardCharsets.UTF_8.name());
        }
        cadena.doFilter(solicitud, respuesta);
    }
}
