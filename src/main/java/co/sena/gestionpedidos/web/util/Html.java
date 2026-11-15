package co.sena.gestionpedidos.web.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Funciones de apoyo para las vistas JSP. Se publican como funciones EL en
 * {@code /WEB-INF/tlds/gestionpedidos.tld} (prefijo {@code gp}).
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class Html {

    private static final Locale LOCALE_COLOMBIA = Locale.forLanguageTag("es-CO");

    private Html() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * Escapa los caracteres especiales de HTML para evitar inyección de código (XSS).
     *
     * @param texto texto a mostrar
     * @return texto seguro para insertar en la página
     */
    public static String escapar(Object texto) {
        if (texto == null) {
            return "";
        }
        String valor = String.valueOf(texto);
        StringBuilder resultado = new StringBuilder(valor.length() + 16);
        for (char caracter : valor.toCharArray()) {
            switch (caracter) {
                case '<' -> resultado.append("&lt;");
                case '>' -> resultado.append("&gt;");
                case '&' -> resultado.append("&amp;");
                case '"' -> resultado.append("&quot;");
                case '\'' -> resultado.append("&#39;");
                default -> resultado.append(caracter);
            }
        }
        return resultado.toString();
    }

    /**
     * @param valor valor monetario
     * @return valor con formato de pesos colombianos, por ejemplo {@code $ 28.000}
     */
    public static String moneda(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(LOCALE_COLOMBIA);
        formato.setMaximumFractionDigits(0);
        return formato.format(valor == null ? BigDecimal.ZERO : valor);
    }
}
