package co.sena.gestionpedidos.dao;

import co.sena.gestionpedidos.modelo.Producto;
import java.util.List;

/**
 * Operaciones de persistencia de la tabla {@code producto}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public interface ProductoDao extends CrudDao<Producto, Integer> {

    /**
     * @param textoBuscado parte del nombre del producto (no distingue mayúsculas)
     * @return productos cuyo nombre contiene el texto
     */
    List<Producto> buscarPorNombre(String textoBuscado);
}
