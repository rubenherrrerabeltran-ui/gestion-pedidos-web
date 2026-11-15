package co.sena.gestionpedidos.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa la tabla {@code pedido} junto con sus detalles.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class Pedido {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Integer idPedido;
    private Integer idUsuario;
    private String nombreCliente;
    private LocalDateTime fechaPedido;
    private EstadoPedido estado;
    private final List<DetallePedido> detalles = new ArrayList<>();

    /** Constructor vacío requerido para el mapeo desde {@code ResultSet}. */
    public Pedido() {
        this.estado = EstadoPedido.PENDIENTE;
    }

    /**
     * @param idUsuario usuario (cliente) que realiza el pedido
     */
    public Pedido(Integer idUsuario) {
        this();
        this.idUsuario = idUsuario;
    }

    /**
     * @param detalle producto y cantidad que se agregan al pedido
     */
    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
    }

    /**
     * @return suma de los subtotales de todos los detalles
     */
    public BigDecimal calcularTotal() {
        return detalles.stream()
                .map(DetallePedido::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    /**
     * @return lista modificable de detalles del pedido
     */
    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    @Override
    public String toString() {
        String fechaTexto = fechaPedido == null ? "-" : fechaPedido.format(FORMATO_FECHA);
        return String.format("Pedido #%d | cliente: %s | fecha: %s | estado: %s | total: %s",
                idPedido, nombreCliente, fechaTexto, estado, calcularTotal());
    }
}
