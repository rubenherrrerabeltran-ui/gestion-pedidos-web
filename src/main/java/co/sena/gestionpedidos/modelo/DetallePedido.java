package co.sena.gestionpedidos.modelo;

import java.math.BigDecimal;

/**
 * Entidad que representa la tabla {@code detalle_pedido}: un producto dentro de un pedido.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class DetallePedido {

    private Integer idDetalle;
    private Integer idPedido;
    private Integer idProducto;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;

    /** Constructor vacío requerido para el mapeo desde {@code ResultSet}. */
    public DetallePedido() {
    }

    /**
     * @param idProducto identificador del producto solicitado
     * @param cantidad   unidades solicitadas (mayor que cero)
     */
    public DetallePedido(Integer idProducto, int cantidad) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
    }

    /**
     * @return cantidad multiplicada por el precio unitario, o cero si aún no hay precio
     */
    public BigDecimal calcularSubtotal() {
        if (precioUnitario == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    @Override
    public String toString() {
        return String.format("     - %-30s x%-3d %12s = %12s",
                nombreProducto, cantidad, precioUnitario, calcularSubtotal());
    }
}
