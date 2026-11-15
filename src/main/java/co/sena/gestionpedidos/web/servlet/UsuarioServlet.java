package co.sena.gestionpedidos.web.servlet;

import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Gestión de usuarios, disponible solo para el rol ADMINISTRADOR.
 * <ul>
 *   <li>GET /usuarios: lista; GET /usuarios?accion=nuevo | accion=editar&amp;id=N: formulario.</li>
 *   <li>POST /usuarios con accion=guardar: registra o actualiza (la contraseña se cifra).</li>
 *   <li>POST /usuarios con accion=eliminar&amp;id=N: elimina.</li>
 * </ul>
 *
 * @author Rubén Darío Herrera Beltrán
 */
@WebServlet(name = "usuarioServlet", urlPatterns = "/usuarios")
public class UsuarioServlet extends ServletBase {

    private static final long serialVersionUID = 1L;
    private static final String RUTA = "/usuarios";

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        if (!verificarAdministrador(solicitud, respuesta)) {
            return;
        }
        String accion = leerTexto(solicitud, "accion");
        try {
            switch (accion) {
                case "nuevo" -> {
                    solicitud.setAttribute("usuario", new Usuario());
                    mostrarVista(solicitud, respuesta, "usuarios/formulario");
                }
                case "editar" -> {
                    int idUsuario = leerEntero(solicitud, "id");
                    solicitud.setAttribute("usuario", getUsuarioServicio().consultarUsuario(idUsuario));
                    mostrarVista(solicitud, respuesta, "usuarios/formulario");
                }
                default -> {
                    solicitud.setAttribute("usuarios", getUsuarioServicio().listarUsuarios());
                    mostrarVista(solicitud, respuesta, "usuarios/lista");
                }
            }
        } catch (DatosException | IllegalArgumentException e) {
            redirigirConMensaje(solicitud, respuesta, RUTA, "error", e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        if (!verificarAdministrador(solicitud, respuesta)) {
            return;
        }
        if ("eliminar".equals(leerTexto(solicitud, "accion"))) {
            eliminar(solicitud, respuesta);
        } else {
            guardar(solicitud, respuesta);
        }
    }

    private void guardar(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String textoId = leerTexto(solicitud, "id");
        String clave = solicitud.getParameter("clave");
        Usuario usuario = new Usuario();
        usuario.setNombre(leerTexto(solicitud, "nombre"));
        usuario.setCorreo(leerTexto(solicitud, "correo"));
        try {
            usuario.setRol(Rol.valueOf(leerTexto(solicitud, "rol").toUpperCase(Locale.ROOT)));
            if (textoId.isEmpty()) {
                Usuario registrado = getUsuarioServicio().registrarUsuario(
                        usuario.getNombre(), usuario.getCorreo(), clave, usuario.getRol());
                redirigirConMensaje(solicitud, respuesta, RUTA, "exito",
                        "Usuario registrado con id " + registrado.getIdUsuario());
            } else {
                Usuario existente = getUsuarioServicio().consultarUsuario(Integer.parseInt(textoId));
                existente.setNombre(usuario.getNombre());
                existente.setCorreo(usuario.getCorreo());
                existente.setRol(usuario.getRol());
                getUsuarioServicio().actualizarUsuario(existente, clave);
                redirigirConMensaje(solicitud, respuesta, RUTA, "exito",
                        "Usuario " + existente.getIdUsuario() + " actualizado");
            }
        } catch (DatosException | IllegalArgumentException e) {
            if (!textoId.isEmpty()) {
                usuario.setIdUsuario(Integer.valueOf(textoId));
            }
            solicitud.setAttribute("usuario", usuario);
            solicitud.setAttribute("error", e.getMessage());
            mostrarVista(solicitud, respuesta, "usuarios/formulario");
        }
    }

    private void eliminar(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        try {
            int idUsuario = leerEntero(solicitud, "id");
            if (usuarioActual(solicitud).getIdUsuario() == idUsuario) {
                throw new DatosException("No puede eliminar su propio usuario mientras tiene la sesión abierta");
            }
            getUsuarioServicio().eliminarUsuario(idUsuario);
            redirigirConMensaje(solicitud, respuesta, RUTA, "exito", "Usuario " + idUsuario + " eliminado");
        } catch (DatosException | IllegalArgumentException e) {
            redirigirConMensaje(solicitud, respuesta, RUTA, "error", e.getMessage());
        }
    }

    private boolean verificarAdministrador(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws IOException {
        if (tieneRol(solicitud, Rol.ADMINISTRADOR)) {
            return true;
        }
        redirigirConMensaje(solicitud, respuesta, "/inicio", "error",
                "Solo el rol ADMINISTRADOR puede gestionar usuarios");
        return false;
    }
}
