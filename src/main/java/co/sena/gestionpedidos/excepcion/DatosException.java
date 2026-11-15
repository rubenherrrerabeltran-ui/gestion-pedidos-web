package co.sena.gestionpedidos.excepcion;

/**
 * Excepción no comprobada que representa un error de acceso a datos o una regla
 * de negocio incumplida. Envuelve la {@link java.sql.SQLException} original para
 * que las capas superiores no dependan de JDBC.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class DatosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción del error para el usuario
     */
    public DatosException(String mensaje) {
        super(mensaje);
    }

    /**
     * @param mensaje descripción del error para el usuario
     * @param causa   excepción original (normalmente una {@code SQLException})
     */
    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
