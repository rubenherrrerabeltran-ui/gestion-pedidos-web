package co.sena.gestionpedidos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.sena.gestionpedidos.config.ConexionBD;
import co.sena.gestionpedidos.dao.ProductoDao;
import co.sena.gestionpedidos.dao.UsuarioDao;
import co.sena.gestionpedidos.dao.impl.PedidoDaoJdbc;
import co.sena.gestionpedidos.dao.impl.ProductoDaoJdbc;
import co.sena.gestionpedidos.dao.impl.UsuarioDaoJdbc;
import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.soporte.BaseDatosPrueba;
import co.sena.gestionpedidos.util.ClaveUtil;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del CRUD de pedidos, incluidas las transacciones (commit y rollback) que
 * mantienen el inventario consistente.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("PedidoServicio - CRUD de pedidos con transacciones")
class PedidoServicioTest {

    private PedidoServicio pedidoServicio;
    private ProductoDao productoDao;
    private UsuarioDao usuarioDao;
    private int idCliente;
    private int idCafe;
    private int idPanela;

    @BeforeEach
    void prepararDatos() {
        ConexionBD conexionBD = BaseDatosPrueba.crearBaseEnMemoria();
        productoDao = new ProductoDaoJdbc(conexionBD);
        usuarioDao = new UsuarioDaoJdbc(conexionBD);
        pedidoServicio = new PedidoServicio(new PedidoDaoJdbc(conexionBD), usuarioDao);

        idCliente = usuarioDao.insertar(new Usuario("Carlos Ramírez", "carlos@correo.com",
                ClaveUtil.generarHash("ClaveSegura2026"), Rol.CLIENTE)).getIdUsuario();
        idCafe = productoDao.insertar(new Producto("Café de origen Huila 500 g",
                new BigDecimal("28000.00"), 40)).getIdProducto();
        idPanela = productoDao.insertar(new Producto("Panela orgánica 1 kg",
                new BigDecimal("6500.00"), 60)).getIdProducto();
    }

    @Test
    @DisplayName("INSERT: registra el pedido, sus detalles, el total y descuenta el stock")
    void registrarPedidoDescuentaStock() {
        Pedido pedido = pedidoServicio.registrarPedido(idCliente, cantidades(idCafe, 2, idPanela, 3));

        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(2, pedido.getDetalles().size());
        assertEquals(0, new BigDecimal("75500.00").compareTo(pedido.calcularTotal()));
        assertEquals(38, stockDe(idCafe));
        assertEquals(57, stockDe(idPanela));
    }

    @Test
    @DisplayName("ROLLBACK: si falta stock no se guarda nada y el inventario no cambia")
    void stockInsuficienteRevierteLaTransaccion() {
        DatosException error = assertThrows(DatosException.class, () ->
                pedidoServicio.registrarPedido(idCliente, cantidades(idCafe, 5, idPanela, 999)));

        assertTrue(error.getMessage().contains("Stock insuficiente"));
        assertEquals(40, stockDe(idCafe));
        assertEquals(60, stockDe(idPanela));
        assertTrue(pedidoServicio.listarPedidos().isEmpty());
    }

    @Test
    @DisplayName("SELECT: consulta el pedido por id y por cliente")
    void consultarPedidos() {
        Pedido pedido = pedidoServicio.registrarPedido(idCliente, cantidades(idCafe, 1, idPanela, 1));

        Pedido consultado = pedidoServicio.consultarPedido(pedido.getIdPedido());

        assertEquals("Carlos Ramírez", consultado.getNombreCliente());
        assertEquals(1, pedidoServicio.listarPedidosDeUsuario(idCliente).size());
    }

    @Test
    @DisplayName("UPDATE: cancelar el pedido devuelve las unidades al inventario")
    void cancelarPedidoDevuelveStock() {
        Pedido pedido = pedidoServicio.registrarPedido(idCliente, cantidades(idCafe, 4, idPanela, 10));

        Pedido cancelado = pedidoServicio.cambiarEstado(pedido.getIdPedido(), EstadoPedido.CANCELADO);

        assertEquals(EstadoPedido.CANCELADO, cancelado.getEstado());
        assertEquals(40, stockDe(idCafe));
        assertEquals(60, stockDe(idPanela));
    }

    @Test
    @DisplayName("UPDATE: una transición no permitida (PENDIENTE a ENVIADO) se rechaza")
    void transicionNoPermitida() {
        Pedido pedido = pedidoServicio.registrarPedido(idCliente, cantidades(idCafe, 1, idPanela, 1));

        assertThrows(DatosException.class, () ->
                pedidoServicio.cambiarEstado(pedido.getIdPedido(), EstadoPedido.ENVIADO));
        assertEquals(EstadoPedido.PAGADO,
                pedidoServicio.cambiarEstado(pedido.getIdPedido(), EstadoPedido.PAGADO).getEstado());
    }

    @Test
    @DisplayName("DELETE: eliminar el pedido borra los detalles y devuelve el stock")
    void eliminarPedidoDevuelveStock() {
        Pedido pedido = pedidoServicio.registrarPedido(idCliente, cantidades(idCafe, 3, idPanela, 2));

        pedidoServicio.eliminarPedido(pedido.getIdPedido());

        assertTrue(pedidoServicio.listarPedidos().isEmpty());
        assertEquals(40, stockDe(idCafe));
        assertEquals(60, stockDe(idPanela));
    }

    @Test
    @DisplayName("Integridad: no se elimina un producto ni un usuario con pedidos")
    void integridadReferencial() {
        pedidoServicio.registrarPedido(idCliente, cantidades(idCafe, 1, idPanela, 1));

        assertThrows(DatosException.class, () -> productoDao.eliminar(idCafe));
        assertThrows(DatosException.class, () -> usuarioDao.eliminar(idCliente));
    }

    private Map<Integer, Integer> cantidades(int primerProducto, int primeraCantidad,
                                             int segundoProducto, int segundaCantidad) {
        Map<Integer, Integer> cantidadPorProducto = new LinkedHashMap<>();
        cantidadPorProducto.put(primerProducto, primeraCantidad);
        cantidadPorProducto.put(segundoProducto, segundaCantidad);
        return cantidadPorProducto;
    }

    private int stockDe(int idProducto) {
        return productoDao.buscarPorId(idProducto).orElseThrow().getStock();
    }
}
