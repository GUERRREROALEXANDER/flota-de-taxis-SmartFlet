package smartflet.modelo;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Persona habilitada (licencia vigente) para operar un vehiculo de la flota durante sus turnos.
 */
@Document(collection = "conductores")
public class Conductor implements EntidadConId {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String identificacion;

    private String nombre;

    @Indexed(unique = true)
    private String numeroLicencia;

    private LocalDate fechaVencimientoLicencia;

    private EstadoRegistro estadoConductor;

    /** Constructor vacio que usa Spring Data para leer el documento. */
    protected Conductor() {
    }

    /** Un conductor nuevo queda ACTIVO. */
    public Conductor(String identificacion, String nombre, String numeroLicencia, LocalDate fechaVencimientoLicencia) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.numeroLicencia = numeroLicencia;
        this.fechaVencimientoLicencia = fechaVencimientoLicencia;
        this.estadoConductor = EstadoRegistro.ACTIVO;
    }

    /** La licencia sirve hasta su fecha de vencimiento, inclusive. */
    public boolean licenciaVigenteEn(LocalDate fecha) {
        return !fechaVencimientoLicencia.isBefore(fecha);
    }

    public boolean estaActivo() {
        return estadoConductor == EstadoRegistro.ACTIVO;
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

    public String getNumeroLicencia() {
        return numeroLicencia;
    }

    public void setNumeroLicencia(String numeroLicencia) {
        this.numeroLicencia = numeroLicencia;
    }

    public LocalDate getFechaVencimientoLicencia() {
        return fechaVencimientoLicencia;
    }

    public void setFechaVencimientoLicencia(LocalDate fechaVencimientoLicencia) {
        this.fechaVencimientoLicencia = fechaVencimientoLicencia;
    }

    public EstadoRegistro getEstadoConductor() {
        return estadoConductor;
    }

    public void setEstadoConductor(EstadoRegistro estadoConductor) {
        this.estadoConductor = estadoConductor;
    }
}
