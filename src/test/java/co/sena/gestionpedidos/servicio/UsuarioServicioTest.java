package co.sena.gestionpedidos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.sena.gestionpedidos.dao.impl.UsuarioDaoJdbc;
import co.sena.gestionpedidos.excepcion.DatosException;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.modelo.Usuario;
import co.sena.gestionpedidos.soporte.BaseDatosPrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del CRUD de usuarios a través del servicio y del DAO JDBC.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@DisplayName("UsuarioServicio - CRUD de usuarios")
class UsuarioServicioTest {

    private UsuarioServicio usuarioServicio;

    @BeforeEach
    void prepararBaseDeDatos() {
        usuarioServicio = new UsuarioServicio(new UsuarioDaoJdbc(BaseDatosPrueba.crearBaseEnMemoria()));
    }

    @Test
    @DisplayName("INSERT: registra el usuario y guarda el hash, nunca la contraseña")
    void registrarUsuarioGuardaHash() {
        Usuario usuario = usuarioServicio.registrarUsuario("Laura Gómez", "Laura.Vendedora@Correo.com",
                "ClaveSegura2026", Rol.VENDEDOR);

        Usuario releido = usuarioServicio.consultarUsuario(usuario.getIdUsuario());

        assertEquals("laura.vendedora@correo.com", releido.getCorreo());
        assertEquals(Rol.VENDEDOR, releido.getRol());
        assertNotEquals("ClaveSegura2026", releido.getPasswordHash());
        assertTrue(releido.getPasswordHash().startsWith("pbkdf2$"));
    }

    @Test
    @DisplayName("INSERT: un correo repetido se rechaza")
    void correoDuplicadoLanzaExcepcion() {
        usuarioServicio.registrarUsuario("Carlos Ramírez", "carlos@correo.com", "ClaveSegura2026", Rol.CLIENTE);

        assertThrows(DatosException.class, () ->
                usuarioServicio.registrarUsuario("Otro Carlos", "CARLOS@correo.com", "OtraClave2026", Rol.CLIENTE));
    }

    @Test
    @DisplayName("SELECT: autenticar acepta la clave correcta y rechaza la incorrecta")
    void autenticarValidaLaClave() {
        usuarioServicio.registrarUsuario("Administrador", "admin@correo.com", "Admin*2026", Rol.ADMINISTRADOR);

        assertTrue(usuarioServicio.autenticar("admin@correo.com", "Admin*2026").isPresent());
        assertFalse(usuarioServicio.autenticar("admin@correo.com", "clave-incorrecta").isPresent());
    }

    @Test
    @DisplayName("UPDATE: actualiza nombre y rol del usuario")
    void actualizarCambiaNombreYRol() {
        Usuario usuario = usuarioServicio.registrarUsuario("Carlos Ramírez", "carlos@correo.com",
                "ClaveSegura2026", Rol.CLIENTE);
        usuario.setNombre("Carlos Andrés Ramírez");
        usuario.setRol(Rol.VENDEDOR);

        usuarioServicio.actualizarUsuario(usuario, null);
        Usuario releido = usuarioServicio.consultarUsuario(usuario.getIdUsuario());

        assertEquals("Carlos Andrés Ramírez", releido.getNombre());
        assertEquals(Rol.VENDEDOR, releido.getRol());
    }

    @Test
    @DisplayName("DELETE: elimina un usuario sin pedidos")
    void eliminarUsuario() {
        Usuario usuario = usuarioServicio.registrarUsuario("Usuario temporal", "temporal@correo.com",
                "ClaveSegura2026", Rol.CLIENTE);

        usuarioServicio.eliminarUsuario(usuario.getIdUsuario());

        assertTrue(usuarioServicio.listarUsuarios().isEmpty());
        assertThrows(DatosException.class, () -> usuarioServicio.consultarUsuario(usuario.getIdUsuario()));
    }

    @Test
    @DisplayName("Validación: rechaza correo con formato inválido y contraseña corta")
    void validacionesDeEntrada() {
        assertThrows(IllegalArgumentException.class, () ->
                usuarioServicio.registrarUsuario("Ana", "correo-invalido", "ClaveSegura2026", Rol.CLIENTE));
        assertThrows(IllegalArgumentException.class, () ->
                usuarioServicio.registrarUsuario("Ana", "ana@correo.com", "corta", Rol.CLIENTE));
    }
}
