package co.sena.gestionpedidos.dao.impl;

import co.sena.gestionpedidos.config.ConexionBD;
import co.sena.gestionpedidos.dao.UsuarioDao;
import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.util.ErroresSql;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Implementación JDBC del {@link UsuarioDao}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class UsuarioDaoJdbc implements UsuarioDao {

    private static final String COLUMNAS = "id_usuario, nombre, correo, password_hash, rol, fecha_registro";
    private static final String SQL_INSERTAR =
            "INSERT INTO usuario (nombre, correo, password_hash, rol, fecha_registro) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_BUSCAR_POR_ID =
            "SELECT " + COLUMNAS + " FROM usuario WHERE id_usuario = ?";
    private static final String SQL_BUSCAR_POR_CORREO =
            "SELECT " + COLUMNAS + " FROM usuario WHERE LOWER(correo) = ?";
    private static final String SQL_LISTAR_TODOS =
            "SELECT " + COLUMNAS + " FROM usuario ORDER BY id_usuario";
    private static final String SQL_ACTUALIZAR =
            "UPDATE usuario SET nombre = ?, correo = ?, password_hash = ?, rol = ? WHERE id_usuario = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM usuario WHERE id_usuario = ?";

    private final ConexionBD conexionBD;

    /**
     * @param conexionBD proveedor de conexiones JDBC
     */
    public UsuarioDaoJdbc(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    @Override
    public Usuario insertar(Usuario usuario) {
        LocalDateTime fechaRegistro = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, usuario.getNombre());
            sentencia.setString(2, usuario.getCorreo());
            sentencia.setString(3, usuario.getPasswordHash());
            sentencia.setString(4, usuario.getRol().name());
            sentencia.setTimestamp(5, Timestamp.valueOf(fechaRegistro));
            sentencia.executeUpdate();
            try (ResultSet llaves = sentencia.getGeneratedKeys()) {
                if (llaves.next()) {
                    usuario.setIdUsuario(llaves.getInt(1));
                }
            }
            usuario.setFechaRegistro(fechaRegistro);
            return usuario;
        } catch (SQLException e) {
            if (ErroresSql.esViolacionDeIntegridad(e)) {
                throw new DatosException("Ya existe un usuario con el correo " + usuario.getCorreo(), e);
            }
            throw new DatosException("No fue posible registrar el usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer idUsuario) {
        return consultarUno(SQL_BUSCAR_POR_ID, idUsuario);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return consultarUno(SQL_BUSCAR_POR_CORREO, correo.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public List<Usuario> listarTodos() {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR_TODOS);
             ResultSet resultado = sentencia.executeQuery()) {
            List<Usuario> usuarios = new ArrayList<>();
            while (resultado.next()) {
                usuarios.add(mapearUsuario(resultado));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new DatosException("No fue posible listar los usuarios", e);
        }
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            sentencia.setString(1, usuario.getNombre());
            sentencia.setString(2, usuario.getCorreo());
            sentencia.setString(3, usuario.getPasswordHash());
            sentencia.setString(4, usuario.getRol().name());
            sentencia.setInt(5, usuario.getIdUsuario());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            if (ErroresSql.esViolacionDeIntegridad(e)) {
                throw new DatosException("El correo " + usuario.getCorreo() + " ya pertenece a otro usuario", e);
            }
            throw new DatosException("No fue posible actualizar el usuario " + usuario.getIdUsuario(), e);
        }
    }

    @Override
    public boolean eliminar(Integer idUsuario) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, idUsuario);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            if (ErroresSql.esViolacionDeIntegridad(e)) {
                throw new DatosException("El usuario " + idUsuario
                        + " tiene pedidos registrados y no se puede eliminar", e);
            }
            throw new DatosException("No fue posible eliminar el usuario " + idUsuario, e);
        }
    }

    private Optional<Usuario> consultarUno(String sql, Object parametro) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setObject(1, parametro);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapearUsuario(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatosException("No fue posible consultar el usuario", e);
        }
    }

    private Usuario mapearUsuario(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultado.getInt("id_usuario"));
        usuario.setNombre(resultado.getString("nombre"));
        usuario.setCorreo(resultado.getString("correo"));
        usuario.setPasswordHash(resultado.getString("password_hash"));
        usuario.setRol(Rol.valueOf(resultado.getString("rol")));
        Timestamp fechaRegistro = resultado.getTimestamp("fecha_registro");
        usuario.setFechaRegistro(fechaRegistro == null ? null : fechaRegistro.toLocalDateTime());
        return usuario;
    }
}
