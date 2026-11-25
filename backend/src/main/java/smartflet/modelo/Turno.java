package smartflet.modelo;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import smartflet.excepcion.ReglaNegocioException;

/**
 * Periodo en el que un conductor opera un vehiculo. Su estado solo cambia con iniciarTurno() y finalizarTurno().
 * Conductor y vehiculo se guardan como referencias por id (documentos independientes en MongoDB).
 */
@Document(collection = "turnos")
public class Turno implements EntidadConId {

    @Id
    private Long id;

    @Indexed
    private Long conductorId;

    @Indexed
    private Long vehiculoId;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private EstadoTurno estado;

    /** Constructor vacio que usa Spring Data para leer el documento. */
    protected Turno() {
    }

    /** Un turno nuevo queda PROGRAMADO. */
    public Turno(Long conductorId, Long vehiculoId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.conductorId = conductorId;
        this.vehiculoId = vehiculoId;
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

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getConductorId() {
        return conductorId;
    }

    public Long getVehiculoId() {
        return vehiculoId;
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
