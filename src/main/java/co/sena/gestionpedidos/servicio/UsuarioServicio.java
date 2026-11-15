package co.sena.gestionpedidos.servicio;

import co.sena.gestionpedidos.dao.UsuarioDao;
import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.util.ClaveUtil;
import co.sena.gestionpedidos.util.Validador;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio de usuarios: registro con contraseña cifrada, consulta,
 * actualización, eliminación y autenticación.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class UsuarioServicio {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final UsuarioDao usuarioDao;

    /**
     * @param usuarioDao DAO de usuarios
     */
    public UsuarioServicio(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    /**
     * @param nombre     nombre completo
     * @param correo     correo electrónico único
     * @param clavePlana contraseña (mínimo 8 caracteres); se guarda solo su hash
     * @param rol        rol asignado
     * @return usuario registrado
     */
    public Usuario registrarUsuario(String nombre, String correo, String clavePlana, Rol rol) {
        String nombreValidado = Validador.validarTextoObligatorio(nombre, "nombre", LONGITUD_MAXIMA_NOMBRE);
        String correoValidado = Validador.validarCorreo(correo);
        Validador.validarClave(clavePlana);
        if (usuarioDao.buscarPorCorreo(correoValidado).isPresent()) {
            throw new DatosException("Ya existe un usuario con el correo " + correoValidado);
        }
        Usuario usuario = new Usuario(nombreValidado, correoValidado, ClaveUtil.generarHash(clavePlana), rol);
        return usuarioDao.insertar(usuario);
    }

    /**
     * @param idUsuario identificador
     * @return el usuario
     * @throws DatosException si no existe
     */
    public Usuario consultarUsuario(int idUsuario) {
        return usuarioDao.buscarPorId(idUsuario)
                .orElseThrow(() -> new DatosException("No existe el usuario con id " + idUsuario));
    }

    /**
     * @return todos los usuarios
     */
    public List<Usuario> listarUsuarios() {
        return usuarioDao.listarTodos();
    }

    /**
     * Actualiza nombre, correo y rol. Si {@code nuevaClave} no es vacía también cambia la contraseña.
     *
     * @param usuario    usuario con los datos modificados
     * @param nuevaClave nueva contraseña o {@code null} para conservar la actual
     * @return usuario actualizado
     */
    public Usuario actualizarUsuario(Usuario usuario, String nuevaClave) {
        usuario.setNombre(Validador.validarTextoObligatorio(usuario.getNombre(), "nombre", LONGITUD_MAXIMA_NOMBRE));
        usuario.setCorreo(Validador.validarCorreo(usuario.getCorreo()));
        if (nuevaClave != null && !nuevaClave.isBlank()) {
            Validador.validarClave(nuevaClave);
            usuario.setPasswordHash(ClaveUtil.generarHash(nuevaClave));
        }
        if (!usuarioDao.actualizar(usuario)) {
            throw new DatosException("No existe el usuario con id " + usuario.getIdUsuario());
        }
        return usuario;
    }

    /**
     * @param idUsuario identificador del usuario a eliminar
     */
    public void eliminarUsuario(int idUsuario) {
        if (!usuarioDao.eliminar(idUsuario)) {
            throw new DatosException("No existe el usuario con id " + idUsuario);
        }
    }

    /**
     * @param correo     correo del usuario
     * @param clavePlana contraseña escrita
     * @return el usuario si las credenciales son correctas
     */
    public Optional<Usuario> autenticar(String correo, String clavePlana) {
        return usuarioDao.buscarPorCorreo(correo)
                .filter(usuario -> ClaveUtil.verificar(clavePlana, usuario.getPasswordHash()));
    }
}
