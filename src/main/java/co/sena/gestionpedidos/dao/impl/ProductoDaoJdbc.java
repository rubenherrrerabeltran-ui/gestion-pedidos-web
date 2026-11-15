package co.sena.gestionpedidos.dao.impl;

import co.sena.gestionpedidos.config.ConexionBD;
import co.sena.gestionpedidos.dao.ProductoDao;
import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.util.ErroresSql;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Implementación JDBC del {@link ProductoDao}. Todas las sentencias usan
 * {@link PreparedStatement} (evita inyección SQL) y <em>try-with-resources</em>
 * (cierra conexión, sentencia y resultado aunque ocurra un error).
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ProductoDaoJdbc implements ProductoDao {

    private static final String COLUMNAS = "id_producto, nombre_producto, precio, stock, activo";
    private static final String SQL_INSERTAR =
            "INSERT INTO producto (nombre_producto, precio, stock, activo) VALUES (?, ?, ?, ?)";
    private static final String SQL_BUSCAR_POR_ID =
            "SELECT " + COLUMNAS + " FROM producto WHERE id_producto = ?";
    private static final String SQL_LISTAR_TODOS =
            "SELECT " + COLUMNAS + " FROM producto ORDER BY id_producto";
    private static final String SQL_BUSCAR_POR_NOMBRE =
            "SELECT " + COLUMNAS + " FROM producto WHERE LOWER(nombre_producto) LIKE ? ORDER BY nombre_producto";
    private static final String SQL_ACTUALIZAR =
            "UPDATE producto SET nombre_producto = ?, precio = ?, stock = ?, activo = ? WHERE id_producto = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM producto WHERE id_producto = ?";

    private final ConexionBD conexionBD;

    /**
     * @param conexionBD proveedor de conexiones JDBC
     */
    public ProductoDaoJdbc(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    @Override
    public Producto insertar(Producto producto) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, producto.getNombreProducto());
            sentencia.setBigDecimal(2, producto.getPrecio());
            sentencia.setInt(3, producto.getStock());
            sentencia.setBoolean(4, producto.isActivo());
            sentencia.executeUpdate();
            try (ResultSet llaves = sentencia.getGeneratedKeys()) {
                if (llaves.next()) {
                    producto.setIdProducto(llaves.getInt(1));
                }
            }
            return producto;
        } catch (SQLException e) {
            throw new DatosException("No fue posible registrar el producto: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(Integer idProducto) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            sentencia.setInt(1, idProducto);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapearProducto(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatosException("No fue posible consultar el producto " + idProducto, e);
        }
    }

    @Override
    public List<Producto> listarTodos() {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR_TODOS);
             ResultSet resultado = sentencia.executeQuery()) {
            List<Producto> productos = new ArrayList<>();
            while (resultado.next()) {
                productos.add(mapearProducto(resultado));
            }
            return productos;
        } catch (SQLException e) {
            throw new DatosException("No fue posible listar los productos", e);
        }
    }

    @Override
    public List<Producto> buscarPorNombre(String textoBuscado) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {
            sentencia.setString(1, "%" + textoBuscado.toLowerCase(Locale.ROOT) + "%");
            try (ResultSet resultado = sentencia.executeQuery()) {
                List<Producto> productos = new ArrayList<>();
                while (resultado.next()) {
                    productos.add(mapearProducto(resultado));
                }
                return productos;
            }
        } catch (SQLException e) {
            throw new DatosException("No fue posible buscar productos por nombre", e);
        }
    }

    @Override
    public boolean actualizar(Producto producto) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            sentencia.setString(1, producto.getNombreProducto());
            sentencia.setBigDecimal(2, producto.getPrecio());
            sentencia.setInt(3, producto.getStock());
            sentencia.setBoolean(4, producto.isActivo());
            sentencia.setInt(5, producto.getIdProducto());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatosException("No fue posible actualizar el producto " + producto.getIdProducto(), e);
        }
    }

    @Override
    public boolean eliminar(Integer idProducto) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, idProducto);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            if (ErroresSql.esViolacionDeIntegridad(e)) {
                throw new DatosException("El producto " + idProducto
                        + " tiene pedidos asociados; desactívelo en lugar de eliminarlo", e);
            }
            throw new DatosException("No fue posible eliminar el producto " + idProducto, e);
        }
    }

    private Producto mapearProducto(ResultSet resultado) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(resultado.getInt("id_producto"));
        producto.setNombreProducto(resultado.getString("nombre_producto"));
        producto.setPrecio(resultado.getBigDecimal("precio"));
        producto.setStock(resultado.getInt("stock"));
        producto.setActivo(resultado.getBoolean("activo"));
        return producto;
    }
}
