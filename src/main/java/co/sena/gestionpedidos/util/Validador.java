package co.sena.gestionpedidos.util;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validaciones de datos de entrada antes de enviarlos a la base de datos.
 * Cada método lanza {@link IllegalArgumentException} con un mensaje claro.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Validador {

    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final int LONGITUD_MINIMA_CLAVE = 8;

    private Validador() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * @param valor          texto a validar
     * @param nombreCampo    nombre que se muestra en el mensaje de error
     * @param longitudMaxima tamaño máximo permitido por la columna
     * @return el texto sin espacios al inicio ni al final
     */
    public static String validarTextoObligatorio(String valor, String nombreCampo, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " es obligatorio");
        }
        String valorLimpio = valor.trim();
        if (valorLimpio.length() > longitudMaxima) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " admite máximo "
                    + longitudMaxima + " caracteres");
        }
        return valorLimpio;
    }

    /**
     * @param correo correo electrónico
     * @return el correo en minúsculas y sin espacios
     */
    public static String validarCorreo(String correo) {
        String correoLimpio = validarTextoObligatorio(correo, "correo", 150).toLowerCase(Locale.ROOT);
        if (!PATRON_CORREO.matcher(correoLimpio).matches()) {
            throw new IllegalArgumentException("El correo " + correoLimpio + " no tiene un formato válido");
        }
        return correoLimpio;
    }

    /**
     * @param clave contraseña en texto plano
     */
    public static void validarClave(String clave) {
        if (clave == null || clave.length() < LONGITUD_MINIMA_CLAVE) {
            throw new IllegalArgumentException("La contraseña debe tener al menos "
                    + LONGITUD_MINIMA_CLAVE + " caracteres");
        }
    }

    /**
     * @param precio precio del producto
     */
    public static void validarPrecio(BigDecimal precio) {
        if (precio == null || precio.signum() < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual a cero");
        }
    }

    /**
     * @param cantidad    valor numérico
     * @param nombreCampo nombre que se muestra en el mensaje de error
     * @param minimo      valor mínimo permitido
     */
    public static void validarMinimo(int cantidad, String nombreCampo, int minimo) {
        if (cantidad < minimo) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser mayor o igual a " + minimo);
        }
    }
}
