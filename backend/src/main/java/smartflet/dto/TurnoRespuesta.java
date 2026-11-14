package smartflet.dto;

import java.time.LocalDateTime;

import smartflet.modelo.EstadoTurno;
import smartflet.modelo.Turno;

/**
 * Datos de un turno que se devuelven al cliente.
 */
public record TurnoRespuesta(
        Long id,
        Long conductorId,
        String conductorNombre,
        Long vehiculoId,
        String vehiculoPlaca,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        EstadoTurno estado
) {

    public static TurnoRespuesta desde(Turno turno) {
        return new TurnoRespuesta(
                turno.getId(),
                turno.getConductor().getId(),
                turno.getConductor().getNombre(),
                turno.getVehiculo().getId(),
                turno.getVehiculo().getPlaca(),
                turno.getFechaInicio(),
                turno.getFechaFin(),
                turno.getEstado());
    }
}
