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
 * Persona habilitada (licencia vigente) para operar un vehiculo de la flota durante sus turnos.
 */
@Entity
@Table(name = "conductor")
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String identificacion;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "numero_licencia", nullable = false, unique = true, length = 30)
    private String numeroLicencia;

    @Column(name = "fecha_vencimiento_licencia", nullable = false)
    private LocalDate fechaVencimientoLicencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conductor", nullable = false, length = 10)
    private EstadoRegistro estadoConductor;

    /** Constructor vacio que exige JPA. */
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
