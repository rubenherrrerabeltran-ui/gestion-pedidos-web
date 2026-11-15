package co.sena.gestionpedidos.web.servlet;

import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.modelo.Producto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * GET /inicio: panel principal con los indicadores del sistema.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@WebServlet(name = "inicioServlet", urlPatterns = "/inicio")
public class InicioServlet extends ServletBase {

    private static final long serialVersionUID = 1L;
    private static final int STOCK_BAJO = 10;

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        List<Producto> productos = getProductoServicio().listarProductos();
        List<Pedido> pedidos = getPedidoServicio().listarPedidos();
        BigDecimal totalVendido = pedidos.stream()
                .filter(pedido -> pedido.getEstado() != EstadoPedido.CANCELADO)
                .map(Pedido::calcularTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        solicitud.setAttribute("totalProductos", productos.size());
        solicitud.setAttribute("productosStockBajo",
                productos.stream().filter(producto -> producto.getStock() < STOCK_BAJO).count());
        solicitud.setAttribute("totalUsuarios", getUsuarioServicio().listarUsuarios().size());
        solicitud.setAttribute("totalPedidos", pedidos.size());
        solicitud.setAttribute("pedidosPendientes",
                pedidos.stream().filter(pedido -> pedido.getEstado() == EstadoPedido.PENDIENTE).count());
        solicitud.setAttribute("totalVendido", totalVendido);
        mostrarVista(solicitud, respuesta, "inicio");
    }
}
