package co.sena.gestionpedidos.modelo;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados posibles de un pedido y transiciones permitidas entre ellos.
 * El {@link #getCodigoBd() código} coincide con la restricción {@code chk_pedido_estado}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public enum EstadoPedido {
    PENDIENTE("pendiente"),
    PAGADO("pagado"),
    ENVIADO("enviado"),
    CANCELADO("cancelado");

    private final String codigoBd;

    EstadoPedido(String codigoBd) {
        this.codigoBd = codigoBd;
    }

    /**
     * @return valor almacenado en la columna {@code pedido.estado}
     */
    public String getCodigoBd() {
        return codigoBd;
    }

    /**
     * Convierte el valor de la base de datos al estado correspondiente.
     *
     * @param codigo valor de la columna {@code estado}
     * @return estado del pedido
     * @throws IllegalArgumentException si el código no existe
     */
    public static EstadoPedido desdeCodigo(String codigo) {
        for (EstadoPedido estado : values()) {
            if (estado.codigoBd.equalsIgnoreCase(codigo)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pedido desconocido: " + codigo);
    }

    /**
     * Indica si el pedido puede pasar de este estado al estado indicado.
     * PENDIENTE → PAGADO | CANCELADO; PAGADO → ENVIADO | CANCELADO; ENVIADO y CANCELADO son finales.
     *
     * @param nuevoEstado estado al que se quiere cambiar
     * @return {@code true} si la transición está permitida
     */
    public boolean puedeCambiarA(EstadoPedido nuevoEstado) {
        return transicionesPermitidas().contains(nuevoEstado);
    }

    private Set<EstadoPedido> transicionesPermitidas() {
        return switch (this) {
            case PENDIENTE -> EnumSet.of(PAGADO, CANCELADO);
            case PAGADO -> EnumSet.of(ENVIADO, CANCELADO);
            case ENVIADO, CANCELADO -> EnumSet.noneOf(EstadoPedido.class);
        };
    }
}
