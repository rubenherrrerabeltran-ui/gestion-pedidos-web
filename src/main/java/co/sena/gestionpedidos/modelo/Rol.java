package co.sena.gestionpedidos.modelo;

/**
 * Roles de los usuarios del sistema definidos en los casos de uso del proyecto.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public enum Rol {
    /** Gestiona usuarios, productos y la configuración del sistema. */
    ADMINISTRADOR,
    /** Registra y atiende pedidos y consulta el inventario. */
    VENDEDOR,
    /** Realiza pedidos y consulta su estado. */
    CLIENTE
}
