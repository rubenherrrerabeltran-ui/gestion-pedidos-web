package co.sena.gestionpedidos.dao.impl;

import co.sena.gestionpedidos.config.ConexionBD;
import co.sena.gestionpedidos.dao.PedidoDao;
import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.DetallePedido;
import co.sena.gestionpedidos.modelo.EstadoPedido;
import co.sena.gestionpedidos.modelo.Pedido;
import co.sena.gestionpedidos.util.ErroresSql;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación JDBC del {@link PedidoDao}.
 *
 * <p>Las operaciones que tocan varias tablas (registrar, cancelar y eliminar un pedido)
 * se ejecutan en una transacción: {@code setAutoCommit(false)}, {@code commit()} si todo
 * sale bien y {@code rollback()} ante cualquier error, para que el inventario nunca quede
 * descuadrado.</p>
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class PedidoDaoJdbc implements PedidoDao {

    private static final String SQL_INSERTAR_PEDIDO =
            "INSERT INTO pedido (id_usuario, fecha_pedido, estado) VALUES (?, ?, ?)";
    private static final String SQL_BLOQUEAR_PRODUCTO =
            "SELECT nombre_producto, precio, stock, activo FROM producto WHERE id_producto = ? FOR UPDATE";
    private static final String SQL_INSERTAR_DETALLE =
            "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
    private static final String SQL_DESCONTAR_STOCK =
            "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";
    private static final String SQL_DEVOLVER_STOCK =
            "UPDATE producto SET stock = stock + ? WHERE id_producto = ?";
    private static final String SQL_CONSULTA_BASE =
            "SELECT p.id_pedido, p.id_usuario, u.nombre AS nombre_cliente, p.fecha_pedido, p.estado, "
            + "d.id_detalle, d.id_producto, pr.nombre_producto, d.cantidad, d.precio_unitario "
            + "FROM pedido p "
            + "JOIN usuario u ON u.id_usuario = p.id_usuario "
            + "LEFT JOIN detalle_pedido d ON d.id_pedido = p.id_pedido "
            + "LEFT JOIN producto pr ON pr.id_producto = d.id_producto ";
    private static final String ORDEN = "ORDER BY p.id_pedido, d.id_detalle";
    private static final String SQL_BUSCAR_POR_ID = SQL_CONSULTA_BASE + "WHERE p.id_pedido = ? " + ORDEN;
    private static final String SQL_LISTAR_POR_USUARIO = SQL_CONSULTA_BASE + "WHERE p.id_usuario = ? " + ORDEN;
    private static final String SQL_LISTAR_TODOS = SQL_CONSULTA_BASE + ORDEN;
    private static final String SQL_ESTADO_ACTUAL =
            "SELECT estado FROM pedido WHERE id_pedido = ? FOR UPDATE";
    private static final String SQL_DETALLES_DEL_PEDIDO =
            "SELECT id_producto, cantidad FROM detalle_pedido WHERE id_pedido = ?";
    private static final String SQL_ACTUALIZAR_ESTADO = "UPDATE pedido SET estado = ? WHERE id_pedido = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM pedido WHERE id_pedido = ?";

    private final ConexionBD conexionBD;

    /**
     * @param conexionBD proveedor de conexiones JDBC
     */
    public PedidoDaoJdbc(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    /**
     * Registra el pedido con sus detalles y descuenta el stock en una sola transacción.
     * El precio unitario se toma del producto en el momento de la compra.
     *
     * @throws DatosException si un producto no existe, está inactivo o no tiene stock suficiente
     */
    @Override
    public Pedido insertar(Pedido pedido) {
        try (Connection conexion = conexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                LocalDateTime fechaPedido = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                int idPedido = insertarEncabezado(conexion, pedido, fechaPedido);
                for (DetallePedido detalle : pedido.getDetalles()) {
                    reservarProducto(conexion, detalle);
                    insertarDetalle(conexion, idPedido, detalle);
                }
                conexion.commit();
                pedido.setIdPedido(idPedido);
                pedido.setFechaPedido(fechaPedido);
                return pedido;
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatosException("No fue posible registrar el pedido: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Pedido> buscarPorId(Integer idPedido) {
        List<Pedido> pedidos = consultarPedidos(SQL_BUSCAR_POR_ID, idPedido);
        return pedidos.isEmpty() ? Optional.empty() : Optional.of(pedidos.get(0));
    }

    @Override
    public List<Pedido> listarTodos() {
        return consultarPedidos(SQL_LISTAR_TODOS, null);
    }

    @Override
    public List<Pedido> listarPorUsuario(int idUsuario) {
        return consultarPedidos(SQL_LISTAR_POR_USUARIO, idUsuario);
    }

    /**
     * Actualiza el estado del pedido (los detalles no se modifican después de registrados).
     */
    @Override
    public boolean actualizar(Pedido pedido) {
        return actualizarEstado(pedido.getIdPedido(), pedido.getEstado());
    }

    @Override
    public boolean actualizarEstado(int idPedido, EstadoPedido nuevoEstado) {
        try (Connection conexion = conexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Optional<EstadoPedido> estadoActual = consultarEstado(conexion, idPedido);
                if (estadoActual.isEmpty()) {
                    conexion.rollback();
                    return false;
                }
                if (nuevoEstado == EstadoPedido.CANCELADO && estadoActual.get() != EstadoPedido.CANCELADO) {
                    devolverStock(conexion, idPedido);
                }
                try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR_ESTADO)) {
                    sentencia.setString(1, nuevoEstado.getCodigoBd());
                    sentencia.setInt(2, idPedido);
                    sentencia.executeUpdate();
                }
                conexion.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatosException("No fue posible actualizar el pedido " + idPedido, e);
        }
    }

    /**
     * Elimina el pedido (los detalles se borran por {@code ON DELETE CASCADE}). Si el pedido
     * no estaba cancelado, primero devuelve sus unidades al inventario.
     */
    @Override
    public boolean eliminar(Integer idPedido) {
        try (Connection conexion = conexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Optional<EstadoPedido> estadoActual = consultarEstado(conexion, idPedido);
                if (estadoActual.isEmpty()) {
                    conexion.rollback();
                    return false;
                }
                if (estadoActual.get() != EstadoPedido.CANCELADO) {
                    devolverStock(conexion, idPedido);
                }
                try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
                    sentencia.setInt(1, idPedido);
                    sentencia.executeUpdate();
                }
                conexion.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatosException("No fue posible eliminar el pedido " + idPedido, e);
        }
    }

    private int insertarEncabezado(Connection conexion, Pedido pedido, LocalDateTime fechaPedido)
            throws SQLException {
        try (PreparedStatement sentencia =
                     conexion.prepareStatement(SQL_INSERTAR_PEDIDO, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, pedido.getIdUsuario());
            sentencia.setTimestamp(2, Timestamp.valueOf(fechaPedido));
            sentencia.setString(3, pedido.getEstado().getCodigoBd());
            sentencia.executeUpdate();
            try (ResultSet llaves = sentencia.getGeneratedKeys()) {
                if (!llaves.next()) {
                    throw new SQLException("La base de datos no devolvió el identificador del pedido");
                }
                return llaves.getInt(1);
            }
        } catch (SQLException e) {
            if (ErroresSql.esViolacionDeIntegridad(e)) {
                throw new DatosException("El usuario " + pedido.getIdUsuario() + " no existe", e);
            }
            throw e;
        }
    }

    /** Bloquea la fila del producto, valida existencia y stock, y descuenta las unidades. */
    private void reservarProducto(Connection conexion, DetallePedido detalle) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(SQL_BLOQUEAR_PRODUCTO)) {
            consulta.setInt(1, detalle.getIdProducto());
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new DatosException("El producto " + detalle.getIdProducto() + " no existe");
                }
                String nombreProducto = resultado.getString("nombre_producto");
                int stockDisponible = resultado.getInt("stock");
                if (!resultado.getBoolean("activo")) {
                    throw new DatosException("El producto " + nombreProducto + " está inactivo");
                }
                if (stockDisponible < detalle.getCantidad()) {
                    throw new DatosException("Stock insuficiente de " + nombreProducto + ": disponible "
                            + stockDisponible + ", solicitado " + detalle.getCantidad());
                }
                detalle.setNombreProducto(nombreProducto);
                detalle.setPrecioUnitario(resultado.getBigDecimal("precio"));
            }
        }
        try (PreparedStatement actualizacion = conexion.prepareStatement(SQL_DESCONTAR_STOCK)) {
            actualizacion.setInt(1, detalle.getCantidad());
            actualizacion.setInt(2, detalle.getIdProducto());
            actualizacion.executeUpdate();
        }
    }

    private void insertarDetalle(Connection conexion, int idPedido, DetallePedido detalle) throws SQLException {
        try (PreparedStatement sentencia =
                     conexion.prepareStatement(SQL_INSERTAR_DETALLE, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, idPedido);
            sentencia.setInt(2, detalle.getIdProducto());
            sentencia.setInt(3, detalle.getCantidad());
            sentencia.setBigDecimal(4, detalle.getPrecioUnitario());
            sentencia.executeUpdate();
            try (ResultSet llaves = sentencia.getGeneratedKeys()) {
                if (llaves.next()) {
                    detalle.setIdDetalle(llaves.getInt(1));
                }
            }
            detalle.setIdPedido(idPedido);
        }
    }

    private Optional<EstadoPedido> consultarEstado(Connection conexion, int idPedido) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ESTADO_ACTUAL)) {
            sentencia.setInt(1, idPedido);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next()
                        ? Optional.of(EstadoPedido.desdeCodigo(resultado.getString("estado")))
                        : Optional.empty();
            }
        }
    }

    private void devolverStock(Connection conexion, int idPedido) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(SQL_DETALLES_DEL_PEDIDO);
             PreparedStatement actualizacion = conexion.prepareStatement(SQL_DEVOLVER_STOCK)) {
            consulta.setInt(1, idPedido);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    actualizacion.setInt(1, resultado.getInt("cantidad"));
                    actualizacion.setInt(2, resultado.getInt("id_producto"));
                    actualizacion.addBatch();
                }
            }
            actualizacion.executeBatch();
        }
    }

    /** Ejecuta la consulta con JOIN y agrupa las filas por pedido. */
    private List<Pedido> consultarPedidos(String sql, Integer parametro) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (parametro != null) {
                sentencia.setInt(1, parametro);
            }
            Map<Integer, Pedido> pedidosPorId = new LinkedHashMap<>();
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    int idPedido = resultado.getInt("id_pedido");
                    Pedido pedido = pedidosPorId.get(idPedido);
                    if (pedido == null) {
                        pedido = mapearPedido(resultado);
                        pedidosPorId.put(idPedido, pedido);
                    }
                    if (resultado.getObject("id_detalle") != null) {
                        pedido.agregarDetalle(mapearDetalle(resultado));
                    }
                }
            }
            return new ArrayList<>(pedidosPorId.values());
        } catch (SQLException e) {
            throw new DatosException("No fue posible consultar los pedidos", e);
        }
    }

    private Pedido mapearPedido(ResultSet resultado) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(resultado.getInt("id_pedido"));
        pedido.setIdUsuario(resultado.getInt("id_usuario"));
        pedido.setNombreCliente(resultado.getString("nombre_cliente"));
        pedido.setFechaPedido(resultado.getTimestamp("fecha_pedido").toLocalDateTime());
        pedido.setEstado(EstadoPedido.desdeCodigo(resultado.getString("estado")));
        return pedido;
    }

    private DetallePedido mapearDetalle(ResultSet resultado) throws SQLException {
        DetallePedido detalle = new DetallePedido();
        detalle.setIdDetalle(resultado.getInt("id_detalle"));
        detalle.setIdPedido(resultado.getInt("id_pedido"));
        detalle.setIdProducto(resultado.getInt("id_producto"));
        detalle.setNombreProducto(resultado.getString("nombre_producto"));
        detalle.setCantidad(resultado.getInt("cantidad"));
        BigDecimal precioUnitario = resultado.getBigDecimal("precio_unitario");
        detalle.setPrecioUnitario(precioUnitario);
        return detalle;
    }
}
