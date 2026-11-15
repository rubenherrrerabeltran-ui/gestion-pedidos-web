package co.sena.gestionpedidos.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico con las cuatro operaciones CRUD que debe cumplir cada DAO.
 *
 * @param <T> tipo de la entidad
 * @param <K> tipo de la llave primaria
 * @author Rubén Darío Herrera Beltrán
 */
public interface CrudDao<T, K> {

    /**
     * Inserta la entidad y le asigna la llave generada por la base de datos.
     *
     * @param entidad datos a insertar
     * @return la misma entidad con su identificador asignado
     */
    T insertar(T entidad);

    /**
     * Consulta una entidad por su llave primaria.
     *
     * @param id llave primaria
     * @return la entidad, o vacío si no existe
     */
    Optional<T> buscarPorId(K id);

    /**
     * @return todas las entidades ordenadas por su identificador
     */
    List<T> listarTodos();

    /**
     * Actualiza la entidad identificada por su llave primaria.
     *
     * @param entidad datos actualizados (debe tener identificador)
     * @return {@code true} si se modificó un registro
     */
    boolean actualizar(T entidad);

    /**
     * Elimina la entidad con la llave indicada.
     *
     * @param id llave primaria
     * @return {@code true} si se eliminó un registro
     */
    boolean eliminar(K id);
}
