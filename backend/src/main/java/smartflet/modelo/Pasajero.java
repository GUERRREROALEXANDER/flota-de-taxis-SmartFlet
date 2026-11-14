package smartflet.modelo;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Persona que solicita viajes y los paga. Se desactiva en vez de borrarse para conservar su historial.
 */
@Entity
@Table(name = "pasajero")
public class Pasajero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String identificacion;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(name = "correo_electronico", nullable = false, length = 120)
    private String correoElectronico;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cuenta", nullable = false, length = 10)
    private EstadoRegistro estadoCuenta;

    /** Constructor vacio que exige JPA. */
    protected Pasajero() {
    }

    /** Un pasajero nuevo queda ACTIVO con fecha de registro de hoy. */
    public Pasajero(String identificacion, String nombre, String telefono, String correoElectronico) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.fechaRegistro = LocalDate.now();
        this.estadoCuenta = EstadoRegistro.ACTIVO;
    }

    public boolean estaActivo() {
        return estadoCuenta == EstadoRegistro.ACTIVO;
    }

    public Long getId() {
        return id;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public EstadoRegistro getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(EstadoRegistro estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }
}
