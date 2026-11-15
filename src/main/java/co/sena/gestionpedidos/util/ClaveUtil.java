package co.sena.gestionpedidos.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Genera y verifica hashes de contraseñas con PBKDF2-HMAC-SHA256 (incluido en el JDK).
 * El formato almacenado es {@code pbkdf2$iteraciones$salBase64$hashBase64}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public final class ClaveUtil {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_SAL_BYTES = 16;
    private static final int LONGITUD_HASH_BITS = 256;
    private static final SecureRandom GENERADOR_ALEATORIO = new SecureRandom();

    private ClaveUtil() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * @param clavePlana contraseña escrita por el usuario
     * @return hash con sal aleatoria listo para guardar en {@code usuario.password_hash}
     */
    public static String generarHash(String clavePlana) {
        byte[] sal = new byte[LONGITUD_SAL_BYTES];
        GENERADOR_ALEATORIO.nextBytes(sal);
        byte[] hash = calcular(clavePlana.toCharArray(), sal, ITERACIONES);
        Base64.Encoder codificador = Base64.getEncoder();
        return String.join("$", PREFIJO, String.valueOf(ITERACIONES),
                codificador.encodeToString(sal), codificador.encodeToString(hash));
    }

    /**
     * @param clavePlana   contraseña a comprobar
     * @param hashGuardado valor almacenado en la base de datos
     * @return {@code true} si la contraseña corresponde al hash
     */
    public static boolean verificar(String clavePlana, String hashGuardado) {
        String[] partes = hashGuardado == null ? new String[0] : hashGuardado.split("\\$");
        if (partes.length != 4 || !PREFIJO.equals(partes[0])) {
            return false;
        }
        Base64.Decoder decodificador = Base64.getDecoder();
        byte[] sal = decodificador.decode(partes[2]);
        byte[] hashEsperado = decodificador.decode(partes[3]);
        byte[] hashCalculado = calcular(clavePlana.toCharArray(), sal, Integer.parseInt(partes[1]));
        return MessageDigest.isEqual(hashEsperado, hashCalculado);
    }

    private static byte[] calcular(char[] clave, byte[] sal, int iteraciones) {
        try {
            KeySpec especificacion = new PBEKeySpec(clave, sal, iteraciones, LONGITUD_HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("El algoritmo " + ALGORITMO + " no está disponible", e);
        }
    }
}
