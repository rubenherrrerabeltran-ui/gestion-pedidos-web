package co.sena.gestionpedidos.web.servlet;

import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gestión de pedidos.
 * <ul>
 *   <li>GET /pedidos?estado=...: lista y filtra por estado.</li>
 *   <li>GET /pedidos?accion=nuevo: formulario; GET /pedidos?accion=ver&amp;id=N: detalle.</li>
 *   <li>POST /pedidos con accion=registrar: crea el pedido (los parámetros producto y cantidad
 *       llegan repetidos, uno por fila del formulario).</li>
 *   <li>POST /pedidos con accion=estado | accion=eliminar: cambia el estado o elimina.</li>
 * </ul>
 *
 * @author Rubén Darío Herrera Beltrán
 */
@WebServlet(name = "pedidoServlet", urlPatterns = "/pedidos")
public class PedidoServlet extends ServletBase {

    private static final long serialVersionUID = 1L;
    private static final String RUTA = "/pedidos";

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String accion = leerTexto(solicitud, "accion");
        try {
            switch (accion) {
                case "nuevo" -> mostrarFormulario(solicitud, respuesta);
                case "ver" -> {
                    solicitud.setAttribute("pedido", getPedidoServicio().consultarPedido(leerEntero(solicitud, "id")));
                    mostrarVista(solicitud, respuesta, "pedidos/detalle");
                }
                default -> listar(solicitud, respuesta);
            }
        } catch (DatosException | IllegalArgumentException e) {
            redirigirConMensaje(solicitud, respuesta, RUTA, "error", e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String accion = leerTexto(solicitud, "accion");
        try {
            switch (accion) {
                case "registrar" -> registrar(solicitud, respuesta);
                case "estado" -> {
                    exigirPersonalInterno(solicitud);
                    int idPedido = leerEntero(solicitud, "id");
                    EstadoPedido nuevoEstado = EstadoPedido.valueOf(
                            leerTexto(solicitud, "nuevoEstado").toUpperCase(Locale.ROOT));
                    getPedidoServicio().cambiarEstado(idPedido, nuevoEstado);
                    redirigirConMensaje(solicitud, respuesta, RUTA + "?accion=ver&id=" + idPedido, "exito",
                            "El pedido #" + idPedido + " pasó a " + nuevoEstado);
                }
                case "eliminar" -> {
                    exigirPersonalInterno(solicitud);
                    int idPedido = leerEntero(solicitud, "id");
                    getPedidoServicio().eliminarPedido(idPedido);
                    redirigirConMensaje(solicitud, respuesta, RUTA, "exito", "Pedido #" + idPedido + " eliminado");
                }
                default -> redirigirConMensaje(solicitud, respuesta, RUTA, "error", "Acción no válida");
            }
        } catch (DatosException | IllegalArgumentException e) {
            String destino = "registrar".equals(accion) ? RUTA + "?accion=nuevo" : RUTA;
            redirigirConMensaje(solicitud, respuesta, destino, "error", e.getMessage());
        }
    }

    private void listar(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String textoEstado = leerTexto(solicitud, "estado");
        EstadoPedido estado = textoEstado.isEmpty() ? null : EstadoPedido.valueOf(textoEstado.toUpperCase(Locale.ROOT));
        Usuario usuario = usuarioActual(solicitud);
        List<Pedido> pedidos = usuario.getRol() == Rol.CLIENTE
                ? getPedidoServicio().listarPedidosDeUsuario(usuario.getIdUsuario())
                : getPedidoServicio().listarPedidosPorEstado(estado);
        if (estado != null) {
            pedidos = pedidos.stream().filter(pedido -> pedido.getEstado() == estado).toList();
        }
        solicitud.setAttribute("pedidos", pedidos);
        solicitud.setAttribute("estados", EstadoPedido.values());
        mostrarVista(solicitud, respuesta, "pedidos/lista");
    }

    private void mostrarFormulario(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        solicitud.setAttribute("productos", getProductoServicio().listarProductos().stream()
                .filter(producto -> producto.isActivo() && producto.getStock() > 0).toList());
        solicitud.setAttribute("clientes", getUsuarioServicio().listarUsuarios().stream()
                .filter(usuario -> usuario.getRol() == Rol.CLIENTE).toList());
        mostrarVista(solicitud, respuesta, "pedidos/formulario");
    }

    private void registrar(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        Usuario usuario = usuarioActual(solicitud);
        int idCliente = usuario.getRol() == Rol.CLIENTE ? usuario.getIdUsuario() : leerEntero(solicitud, "idCliente");
        String[] productos = solicitud.getParameterValues("producto");
        String[] cantidades = solicitud.getParameterValues("cantidad");
        Map<Integer, Integer> cantidadPorProducto = new LinkedHashMap<>();
        if (productos != null && cantidades != null) {
            for (int fila = 0; fila < Math.min(productos.length, cantidades.length); fila++) {
                if (!productos[fila].isBlank() && !cantidades[fila].isBlank()) {
                    cantidadPorProducto.merge(Integer.valueOf(productos[fila]),
                            Integer.valueOf(cantidades[fila].trim()), Integer::sum);
                }
            }
        }
        Pedido pedido = getPedidoServicio().registrarPedido(idCliente, cantidadPorProducto);
        redirigirConMensaje(solicitud, respuesta, RUTA + "?accion=ver&id=" + pedido.getIdPedido(), "exito",
                "Pedido #" + pedido.getIdPedido() + " registrado; el stock fue descontado");
    }

    private void exigirPersonalInterno(HttpServletRequest solicitud) {
        if (!tieneRol(solicitud, Rol.ADMINISTRADOR, Rol.VENDEDOR)) {
            throw new DatosException("Solo administradores y vendedores pueden modificar pedidos");
        }
    }
}
