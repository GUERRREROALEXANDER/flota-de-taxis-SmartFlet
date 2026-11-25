package smartflet.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo para asignar (o reasignar) un vehiculo a un viaje.
 */
public record AsignacionVehiculoSolicitud(

        @NotNull(message = "El vehiculo es obligatorio")
        Long vehiculoId
) {
}
