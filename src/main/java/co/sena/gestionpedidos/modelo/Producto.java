package co.sena.gestionpedidos.modelo;

import java.math.BigDecimal;

/**
 * Entidad que representa la tabla {@code producto}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class Producto {

    private Integer idProducto;
    private String nombreProducto;
    private BigDecimal precio;
    private int stock;
    private boolean activo;

    /** Constructor vacío requerido para el mapeo desde {@code ResultSet}. */
    public Producto() {
        this.activo = true;
    }

    /**
     * @param nombreProducto nombre comercial del producto
     * @param precio         precio de venta (mayor o igual a cero)
     * @param stock          unidades disponibles
     */
    public Producto(String nombreProducto, BigDecimal precio, int stock) {
        this.nombreProducto = nombreProducto;
        this.precio = precio;
        this.stock = stock;
        this.activo = true;
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return String.format("%-4d %-30s %12s %6d  %s",
                idProducto, nombreProducto, precio, stock, activo ? "Activo" : "Inactivo");
    }
}
