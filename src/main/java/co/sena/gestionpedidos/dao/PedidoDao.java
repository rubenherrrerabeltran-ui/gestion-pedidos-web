package co.sena.gestionpedidos.dao;

import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import java.util.List;

/**
 * Operaciones de persistencia de las tablas {@code pedido} y {@code detalle_pedido}.
 *
 * <p>{@link #insertar(Object)} registra el pedido, sus detalles y descuenta el stock en
 * una sola transacción; {@link #actualizar(Object)} cambia el estado del pedido.</p>
 *
 * @author Rubén Darío Herrera Beltrán
 */
public interface PedidoDao extends CrudDao<Pedido, Integer> {

    /**
     * @param idUsuario identificador del cliente
     * @return pedidos del cliente con sus detalles
     */
    List<Pedido> listarPorUsuario(int idUsuario);

    /**
     * Cambia el estado del pedido. Si el nuevo estado es CANCELADO devuelve al
     * inventario las unidades del pedido dentro de la misma transacción.
     *
     * @param idPedido    identificador del pedido
     * @param nuevoEstado estado destino
     * @return {@code true} si el pedido existía y se actualizó
     */
    boolean actualizarEstado(int idPedido, EstadoPedido nuevoEstado);
}
