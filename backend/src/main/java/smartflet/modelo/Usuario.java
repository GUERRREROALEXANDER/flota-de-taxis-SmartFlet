package smartflet.modelo;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Cuenta para entrar al sistema. La contrasena NUNCA se guarda en texto plano:
 * se guarda el hash BCrypt (claveCifrada). Se desactiva en vez de borrarse.
 */
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    /** Siempre en minusculas, asi "Admin" y "admin" son el mismo usuario. */
    @Indexed(unique = true)
    private String nombreUsuario;

    private String nombre;

    private String claveCifrada;

    private Rol rol;

    private EstadoRegistro estado;

    private LocalDate fechaRegistro;

    protected Usuario() {
    }

    public Usuario(String nombreUsuario, String nombre, String claveCifrada, Rol rol) {
        this.nombreUsuario = normalizar(nombreUsuario);
        this.nombre = nombre;
        this.claveCifrada = claveCifrada;
        this.rol = rol;
        this.estado = EstadoRegistro.ACTIVO;
        this.fechaRegistro = LocalDate.now();
    }

    public static String normalizar(String nombreUsuario) {
        return nombreUsuario == null ? null : nombreUsuario.trim().toLowerCase();
    }

    public boolean estaActivo() {
        return estado == EstadoRegistro.ACTIVO;
    }

    public String getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClaveCifrada() {
        return claveCifrada;
    }

    public void setClaveCifrada(String claveCifrada) {
        this.claveCifrada = claveCifrada;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public EstadoRegistro getEstado() {
        return estado;
    }

    public void setEstado(EstadoRegistro estado) {
        this.estado = estado;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
}
