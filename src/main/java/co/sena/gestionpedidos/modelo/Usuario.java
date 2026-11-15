package co.sena.gestionpedidos.modelo;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla {@code usuario}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class Usuario {

    private Integer idUsuario;
    private String nombre;
    private String correo;
    private String passwordHash;
    private Rol rol;
    private LocalDateTime fechaRegistro;

    /** Constructor vacío requerido para el mapeo desde {@code ResultSet}. */
    public Usuario() {
        this.rol = Rol.CLIENTE;
    }

    /**
     * @param nombre       nombre completo
     * @param correo       correo electrónico único
     * @param passwordHash hash de la contraseña (nunca la contraseña en texto plano)
     * @param rol          rol del usuario
     */
    public Usuario(String nombre, String correo, String passwordHash, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return String.format("%-4d %-28s %-36s %-13s", idUsuario, nombre, correo, rol);
    }
}
