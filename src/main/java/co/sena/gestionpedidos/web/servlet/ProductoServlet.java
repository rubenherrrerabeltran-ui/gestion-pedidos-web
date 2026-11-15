package co.sena.gestionpedidos.web.servlet;

import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.modelo.Rol;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Gestión de productos (inventario).
 * <ul>
 *   <li>GET /productos?texto=...: lista y busca (consulta, no modifica datos).</li>
 *   <li>GET /productos?accion=nuevo | accion=editar&amp;id=N: muestra el formulario.</li>
 *   <li>POST /productos con accion=guardar: inserta o actualiza.</li>
 *   <li>POST /productos con accion=eliminar&amp;id=N: elimina.</li>
 * </ul>
 *
 * @author Rubén Darío Herrera Beltrán
 */
@WebServlet(name = "productoServlet", urlPatterns = "/productos")
public class ProductoServlet extends ServletBase {

    private static final long serialVersionUID = 1L;
    private static final String RUTA = "/productos";

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String accion = leerTexto(solicitud, "accion");
        try {
            switch (accion) {
                case "nuevo" -> {
                    solicitud.setAttribute("producto", new Producto());
                    mostrarVista(solicitud, respuesta, "productos/formulario");
                }
                case "editar" -> {
                    int idProducto = leerEntero(solicitud, "id");
                    solicitud.setAttribute("producto", getProductoServicio().consultarProducto(idProducto));
                    mostrarVista(solicitud, respuesta, "productos/formulario");
                }
                default -> {
                    String texto = leerTexto(solicitud, "texto");
                    solicitud.setAttribute("productos", texto.isEmpty()
                            ? getProductoServicio().listarProductos()
                            : getProductoServicio().buscarProductos(texto));
                    mostrarVista(solicitud, respuesta, "productos/lista");
                }
            }
        } catch (DatosException | IllegalArgumentException e) {
            redirigirConMensaje(solicitud, respuesta, RUTA, "error", e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        if (!tieneRol(solicitud, Rol.ADMINISTRADOR, Rol.VENDEDOR)) {
            redirigirConMensaje(solicitud, respuesta, RUTA, "error", "Su rol no puede modificar productos");
            return;
        }
        String accion = leerTexto(solicitud, "accion");
        if ("eliminar".equals(accion)) {
            eliminar(solicitud, respuesta);
        } else {
            guardar(solicitud, respuesta);
        }
    }

    private void guardar(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String textoId = leerTexto(solicitud, "id");
        Producto producto = new Producto();
        producto.setNombreProducto(leerTexto(solicitud, "nombreProducto"));
        producto.setActivo(solicitud.getParameter("activo") != null);
        try {
            producto.setPrecio(leerDecimal(solicitud, "precio"));
            producto.setStock(leerEntero(solicitud, "stock"));
            if (textoId.isEmpty()) {
                Producto registrado = getProductoServicio().registrarProducto(
                        producto.getNombreProducto(), producto.getPrecio(), producto.getStock());
                redirigirConMensaje(solicitud, respuesta, RUTA, "exito",
                        "Producto registrado con id " + registrado.getIdProducto());
            } else {
                producto.setIdProducto(Integer.parseInt(textoId));
                getProductoServicio().actualizarProducto(producto);
                redirigirConMensaje(solicitud, respuesta, RUTA, "exito",
                        "Producto " + producto.getIdProducto() + " actualizado");
            }
        } catch (DatosException | IllegalArgumentException e) {
            // Se vuelve a mostrar el formulario con los datos escritos y el error.
            solicitud.setAttribute("producto", producto);
            solicitud.setAttribute("error", e.getMessage());
            mostrarVista(solicitud, respuesta, "productos/formulario");
        }
    }

    private void eliminar(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        try {
            int idProducto = leerEntero(solicitud, "id");
            getProductoServicio().eliminarProducto(idProducto);
            redirigirConMensaje(solicitud, respuesta, RUTA, "exito", "Producto " + idProducto + " eliminado");
        } catch (DatosException | IllegalArgumentException e) {
            redirigirConMensaje(solicitud, respuesta, RUTA, "error", e.getMessage());
        }
    }
}
