package smartflet.dto;

import java.time.LocalDateTime;

import smartflet.modelo.EstadoTurno;
import smartflet.modelo.Turno;

/**
 * Datos de un turno que se devuelven al cliente.
 */
public record TurnoRespuesta(
        String id,
        String conductorId,
        String vehiculoId,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        EstadoTurno estado
) {

    public static TurnoRespuesta desde(Turno turno) {
        return new TurnoRespuesta(
                turno.getId(),
                turno.getConductorId(),
                turno.getVehiculoId(),
                turno.getFechaInicio(),
                turno.getFechaFin(),
                turno.getEstado());
    }
}
