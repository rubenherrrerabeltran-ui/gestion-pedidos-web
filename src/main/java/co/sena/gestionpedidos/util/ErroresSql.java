package co.sena.gestionpedidos.util;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Utilidades para interpretar errores de la base de datos.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class ErroresSql {

    /** Clase SQLSTATE estándar de las violaciones de integridad (llave única, foránea, CHECK). */
    private static final String CLASE_VIOLACION_INTEGRIDAD = "23";

    private ErroresSql() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * @param error excepción lanzada por el controlador JDBC
     * @return {@code true} si el error corresponde a una restricción de integridad
     */
    public static boolean esViolacionDeIntegridad(SQLException error) {
        if (error instanceof SQLIntegrityConstraintViolationException) {
            return true;
        }
        String estadoSql = error.getSQLState();
        return estadoSql != null && estadoSql.startsWith(CLASE_VIOLACION_INTEGRIDAD);
    }
}
