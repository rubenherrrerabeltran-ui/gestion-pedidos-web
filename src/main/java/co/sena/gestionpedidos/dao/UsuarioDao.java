package co.sena.gestionpedidos.dao;

import co.sena.gestionpedidos.modelo.Usuario;
import java.util.Optional;

/**
 * Operaciones de persistencia de la tabla {@code usuario}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public interface UsuarioDao extends CrudDao<Usuario, Integer> {

    /**
     * @param correo correo electrónico (no distingue mayúsculas)
     * @return el usuario con ese correo, o vacío si no existe
     */
    Optional<Usuario> buscarPorCorreo(String correo);
}
