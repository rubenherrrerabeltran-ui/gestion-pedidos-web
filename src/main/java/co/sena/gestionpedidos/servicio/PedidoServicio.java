package co.sena.gestionpedidos.servicio;

import co.sena.gestionpedidos.dao.PedidoDao;
import co.sena.gestionpedidos.dao.UsuarioDao;
import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.DetallePedido;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.util.Validador;
import java.util.List;
import java.util.Map;

/**
 * Reglas de negocio de pedidos: registro con validación de cliente y cantidades,
 * consulta, cambio de estado según las transiciones permitidas y eliminación.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class PedidoServicio {

    private final PedidoDao pedidoDao;
    private final UsuarioDao usuarioDao;

    /**
     * @param pedidoDao  DAO de pedidos
     * @param usuarioDao DAO de usuarios (para validar el cliente)
     */
    public PedidoServicio(PedidoDao pedidoDao, UsuarioDao usuarioDao) {
        this.pedidoDao = pedidoDao;
        this.usuarioDao = usuarioDao;
    }

    /**
     * Registra un pedido nuevo en estado PENDIENTE.
     *
     * @param idUsuario           cliente que hace el pedido
     * @param cantidadPorProducto mapa idProducto → cantidad (en el orden de captura)
     * @return pedido registrado con detalles, precios y total
     */
    public Pedido registrarPedido(int idUsuario, Map<Integer, Integer> cantidadPorProducto) {
        if (usuarioDao.buscarPorId(idUsuario).isEmpty()) {
            throw new DatosException("No existe el usuario con id " + idUsuario);
        }
        if (cantidadPorProducto == null || cantidadPorProducto.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        }
        Pedido pedido = new Pedido(idUsuario);
        cantidadPorProducto.forEach((idProducto, cantidad) -> {
            Validador.validarMinimo(cantidad, "cantidad", 1);
            pedido.agregarDetalle(new DetallePedido(idProducto, cantidad));
        });
        pedidoDao.insertar(pedido);
        return consultarPedido(pedido.getIdPedido());
    }

    /**
     * @param idPedido identificador
     * @return el pedido con sus detalles
     */
    public Pedido consultarPedido(int idPedido) {
        return pedidoDao.buscarPorId(idPedido)
                .orElseThrow(() -> new DatosException("No existe el pedido con id " + idPedido));
    }

    /**
     * @return todos los pedidos
     */
    public List<Pedido> listarPedidos() {
        return pedidoDao.listarTodos();
    }

    /**
     * @param estado estado por el que se filtra, o {@code null} para todos
     * @return pedidos en ese estado
     */
    public List<Pedido> listarPedidosPorEstado(EstadoPedido estado) {
        List<Pedido> pedidos = pedidoDao.listarTodos();
        if (estado == null) {
            return pedidos;
        }
        return pedidos.stream().filter(pedido -> pedido.getEstado() == estado).toList();
    }

    /**
     * @param idUsuario cliente
     * @return pedidos del cliente
     */
    public List<Pedido> listarPedidosDeUsuario(int idUsuario) {
        return pedidoDao.listarPorUsuario(idUsuario);
    }

    /**
     * @param idPedido    pedido a modificar
     * @param nuevoEstado estado destino
     * @return pedido actualizado
     * @throws DatosException si la transición no está permitida
     */
    public Pedido cambiarEstado(int idPedido, EstadoPedido nuevoEstado) {
        Pedido pedido = consultarPedido(idPedido);
        if (!pedido.getEstado().puedeCambiarA(nuevoEstado)) {
            throw new DatosException("No se puede pasar el pedido de " + pedido.getEstado()
                    + " a " + nuevoEstado);
        }
        pedidoDao.actualizarEstado(idPedido, nuevoEstado);
        return consultarPedido(idPedido);
    }

    /**
     * Elimina un pedido que aún no ha sido enviado (devuelve el stock si corresponde).
     *
     * @param idPedido pedido a eliminar
     */
    public void eliminarPedido(int idPedido) {
        Pedido pedido = consultarPedido(idPedido);
        if (pedido.getEstado() == EstadoPedido.ENVIADO) {
            throw new DatosException("Un pedido enviado no se puede eliminar");
        }
        pedidoDao.eliminar(idPedido);
    }
}
