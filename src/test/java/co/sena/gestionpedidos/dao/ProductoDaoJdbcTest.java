package co.sena.gestionpedidos.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.sena.gestionpedidos.dao.impl.ProductoDaoJdbc;
import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.soporte.BaseDatosPrueba;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las operaciones CRUD del {@link ProductoDaoJdbc} contra una base real (H2 modo MySQL).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("ProductoDaoJdbc - CRUD de productos")
class ProductoDaoJdbcTest {

    private ProductoDao productoDao;

    @BeforeEach
    void prepararBaseDeDatos() {
        productoDao = new ProductoDaoJdbc(BaseDatosPrueba.crearBaseEnMemoria());
    }

    @Test
    @DisplayName("INSERT: insertar asigna el id generado por la base de datos")
    void insertarAsignaIdentificador() {
        Producto producto = productoDao.insertar(
                new Producto("Café de origen Huila 500 g", new BigDecimal("28000.00"), 40));

        assertNotNull(producto.getIdProducto());
        assertEquals(1, producto.getIdProducto());
    }

    @Test
    @DisplayName("SELECT: buscarPorId devuelve los datos guardados")
    void buscarPorIdDevuelveLosDatosGuardados() {
        Producto guardado = productoDao.insertar(new Producto("Panela orgánica 1 kg", new BigDecimal("6500.00"), 60));

        Optional<Producto> encontrado = productoDao.buscarPorId(guardado.getIdProducto());

        assertTrue(encontrado.isPresent());
        assertEquals("Panela orgánica 1 kg", encontrado.get().getNombreProducto());
        assertEquals(0, new BigDecimal("6500.00").compareTo(encontrado.get().getPrecio()));
        assertEquals(60, encontrado.get().getStock());
        assertTrue(encontrado.get().isActivo());
    }

    @Test
    @DisplayName("SELECT: buscarPorId de un id inexistente devuelve vacío")
    void buscarPorIdInexistenteDevuelveVacio() {
        assertFalse(productoDao.buscarPorId(999).isPresent());
    }

    @Test
    @DisplayName("SELECT: listarTodos y buscarPorNombre filtran correctamente")
    void listarTodosYBuscarPorNombre() {
        productoDao.insertar(new Producto("Café de origen Huila 500 g", new BigDecimal("28000"), 40));
        productoDao.insertar(new Producto("Chocolate de mesa 250 g", new BigDecimal("9800"), 25));
        productoDao.insertar(new Producto("Café molido 250 g", new BigDecimal("15000"), 30));

        List<Producto> todos = productoDao.listarTodos();
        List<Producto> cafes = productoDao.buscarPorNombre("CAFÉ");

        assertEquals(3, todos.size());
        assertEquals(2, cafes.size());
    }

    @Test
    @DisplayName("UPDATE: actualizar modifica nombre, precio, stock y estado")
    void actualizarModificaElRegistro() {
        Producto producto = productoDao.insertar(new Producto("Chocolate de mesa 250 g", new BigDecimal("9800"), 25));
        producto.setPrecio(new BigDecimal("10500.00"));
        producto.setStock(18);
        producto.setActivo(false);

        boolean actualizado = productoDao.actualizar(producto);
        Producto releido = productoDao.buscarPorId(producto.getIdProducto()).orElseThrow();

        assertTrue(actualizado);
        assertEquals(0, new BigDecimal("10500.00").compareTo(releido.getPrecio()));
        assertEquals(18, releido.getStock());
        assertFalse(releido.isActivo());
    }

    @Test
    @DisplayName("DELETE: eliminar borra el registro de la tabla")
    void eliminarBorraElRegistro() {
        Producto producto = productoDao.insertar(new Producto("Producto temporal", BigDecimal.ONE, 1));

        boolean eliminado = productoDao.eliminar(producto.getIdProducto());

        assertTrue(eliminado);
        assertFalse(productoDao.buscarPorId(producto.getIdProducto()).isPresent());
        assertFalse(productoDao.eliminar(producto.getIdProducto()));
    }
}
