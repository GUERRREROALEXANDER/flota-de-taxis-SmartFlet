package smartflet.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import smartflet.excepcion.ReglaNegocioException;

/**
 * Periodo en el que un conductor opera un vehiculo. Su estado solo cambia con iniciarTurno() y finalizarTurno().
 */
@Entity
@Table(name = "turno")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conductor_id", nullable = false)
    private Conductor conductor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoTurno estado;

    /** Constructor vacio que exige JPA. */
    protected Turno() {
    }

    /** Un turno nuevo queda PROGRAMADO. */
    public Turno(Conductor conductor, Vehiculo vehiculo, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.conductor = conductor;
        this.vehiculo = vehiculo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = EstadoTurno.PROGRAMADO;
    }

    public void iniciarTurno() {
        if (estado != EstadoTurno.PROGRAMADO) {
            throw new ReglaNegocioException("Solo se puede iniciar un turno PROGRAMADO (estado actual: " + estado + ")");
        }
        this.estado = EstadoTurno.EN_CURSO;
    }

    public void finalizarTurno() {
        if (estado != EstadoTurno.EN_CURSO) {
            throw new ReglaNegocioException("Solo se puede finalizar un turno EN_CURSO (estado actual: " + estado + ")");
        }
        this.estado = EstadoTurno.FINALIZADO;
    }

    public Long getId() {
        return id;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public EstadoTurno getEstado() {
        return estado;
    }
}
