package co.sena.gestionpedidos.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.sena.gestionpedidos.soporte.BaseDatosPrueba;
import co.sena.gestionpedidos.util.ClaveUtil;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la conexión JDBC, del esquema aplicado y del cifrado de claves.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("ConexionBD - conexión JDBC y esquema")
class ConexionBDTest {

    @Test
    @DisplayName("La conexión JDBC es válida y el esquema tiene las 4 tablas del modelo")
    void conexionYTablas() throws SQLException {
        ConexionBD conexionBD = BaseDatosPrueba.crearBaseEnMemoria();

        assertTrue(conexionBD.probarConexion());
        try (Connection conexion = conexionBD.obtenerConexion()) {
            DatabaseMetaData metadatos = conexion.getMetaData();
            int tablasEncontradas = 0;
            for (String tabla : new String[] {"usuario", "producto", "pedido", "detalle_pedido"}) {
                try (ResultSet resultado = metadatos.getTables(null, null, tabla, new String[] {"TABLE"})) {
                    tablasEncontradas += resultado.next() ? 1 : 0;
                }
            }
            assertEquals(4, tablasEncontradas);
        }
    }

    @Test
    @DisplayName("Con datos de conexión incorrectos probarConexion devuelve false")
    void conexionInvalida() {
        ConexionBD conexionInvalida = new ConexionBD("jdbc:motor-inexistente://localhost/bd", "x", "x");

        assertFalse(conexionInvalida.probarConexion());
    }

    @Test
    @DisplayName("ClaveUtil genera hashes distintos para la misma clave (sal aleatoria)")
    void hashConSalAleatoria() {
        String primerHash = ClaveUtil.generarHash("ClaveSegura2026");
        String segundoHash = ClaveUtil.generarHash("ClaveSegura2026");

        assertFalse(primerHash.equals(segundoHash));
        assertTrue(ClaveUtil.verificar("ClaveSegura2026", primerHash));
        assertTrue(ClaveUtil.verificar("ClaveSegura2026", segundoHash));
    }
}
