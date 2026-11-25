package smartflet.modelo;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Persona que solicita viajes y los paga. Se desactiva en vez de borrarse para conservar su historial.
 */
@Document(collection = "pasajeros")
public class Pasajero implements EntidadConId {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String identificacion;

    private String nombre;

    private String telefono;

    private String correoElectronico;

    private LocalDate fechaRegistro;

    private EstadoRegistro estadoCuenta;

    /** Constructor vacio que usa Spring Data para leer el documento. */
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

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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
