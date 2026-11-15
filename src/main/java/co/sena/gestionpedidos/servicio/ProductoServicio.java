package co.sena.gestionpedidos.servicio;

import co.sena.gestionpedidos.dao.ProductoDao;
import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.util.Validador;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Reglas de negocio de productos (RF de gestión de inventario): valida los datos y
 * delega la persistencia en el {@link ProductoDao}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ProductoServicio {

    private static final int LONGITUD_MAXIMA_NOMBRE = 120;
    private static final int DECIMALES_PRECIO = 2;

    private final ProductoDao productoDao;

    /**
     * @param productoDao DAO de productos
     */
    public ProductoServicio(ProductoDao productoDao) {
        this.productoDao = productoDao;
    }

    /**
     * @param nombreProducto nombre del producto
     * @param precio         precio de venta
     * @param stock          unidades iniciales
     * @return producto registrado con su identificador
     */
    public Producto registrarProducto(String nombreProducto, BigDecimal precio, int stock) {
        String nombreValidado = Validador.validarTextoObligatorio(nombreProducto, "nombre", LONGITUD_MAXIMA_NOMBRE);
        Validador.validarPrecio(precio);
        Validador.validarMinimo(stock, "stock", 0);
        BigDecimal precioNormalizado = precio.setScale(DECIMALES_PRECIO, RoundingMode.HALF_UP);
        return productoDao.insertar(new Producto(nombreValidado, precioNormalizado, stock));
    }

    /**
     * @param idProducto identificador
     * @return el producto
     * @throws DatosException si no existe
     */
    public Producto consultarProducto(int idProducto) {
        return productoDao.buscarPorId(idProducto)
                .orElseThrow(() -> new DatosException("No existe el producto con id " + idProducto));
    }

    /**
     * @return todos los productos
     */
    public List<Producto> listarProductos() {
        return productoDao.listarTodos();
    }

    /**
     * @param textoBuscado parte del nombre
     * @return productos que coinciden
     */
    public List<Producto> buscarProductos(String textoBuscado) {
        return productoDao.buscarPorNombre(textoBuscado == null ? "" : textoBuscado.trim());
    }

    /**
     * @param producto producto con los datos modificados
     * @return producto actualizado
     */
    public Producto actualizarProducto(Producto producto) {
        producto.setNombreProducto(Validador.validarTextoObligatorio(
                producto.getNombreProducto(), "nombre", LONGITUD_MAXIMA_NOMBRE));
        Validador.validarPrecio(producto.getPrecio());
        producto.setPrecio(producto.getPrecio().setScale(DECIMALES_PRECIO, RoundingMode.HALF_UP));
        Validador.validarMinimo(producto.getStock(), "stock", 0);
        if (!productoDao.actualizar(producto)) {
            throw new DatosException("No existe el producto con id " + producto.getIdProducto());
        }
        return producto;
    }

    /**
     * @param idProducto identificador del producto a eliminar
     */
    public void eliminarProducto(int idProducto) {
        if (!productoDao.eliminar(idProducto)) {
            throw new DatosException("No existe el producto con id " + idProducto);
        }
    }
}
