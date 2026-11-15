package co.sena.gestionpedidos.soporte;

import co.sena.gestionpedidos.config.ConexionBD;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.UUID;

/**
 * Crea, para cada prueba, una base de datos H2 en memoria en modo de compatibilidad
 * MySQL y le aplica el mismo script {@code database/01_esquema.sql} del proyecto.
 * Así los DAO se prueban con JDBC real sin depender de un servidor instalado.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class BaseDatosPrueba {

    private static final Path SCRIPT_ESQUEMA = Path.of("database", "01_esquema.sql");
    private static final String OPCIONES_H2 = ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    private BaseDatosPrueba() {
        // Clase de soporte: no se instancia.
    }

    /**
     * @return configuración de conexión a una base nueva y vacía con el esquema creado
     */
    public static ConexionBD crearBaseEnMemoria() {
        String url = "jdbc:h2:mem:prueba_" + UUID.randomUUID() + OPCIONES_H2;
        ConexionBD conexionBD = new ConexionBD(url, "sa", "");
        aplicarEsquema(conexionBD);
        return conexionBD;
    }

    private static void aplicarEsquema(ConexionBD conexionBD) {
        String script;
        try {
            script = Files.readString(SCRIPT_ESQUEMA, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + SCRIPT_ESQUEMA, e);
        }
        try (Connection conexion = conexionBD.obtenerConexion();
             Statement sentencia = conexion.createStatement()) {
            for (String instruccion : script.replaceAll("(?m)^--.*$", "").split(";")) {
                String instruccionLimpia = instruccion.strip();
                if (!instruccionLimpia.isEmpty() && esInstruccionDeEstructura(instruccionLimpia)) {
                    sentencia.execute(instruccionLimpia);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear el esquema de prueba", e);
        }
    }

    /** Omite CREATE DATABASE y USE, que no aplican a una base en memoria. */
    private static boolean esInstruccionDeEstructura(String instruccion) {
        String mayusculas = instruccion.toUpperCase(Locale.ROOT);
        return mayusculas.startsWith("CREATE TABLE");
    }
}
