package smartflet.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo para asignar (o reasignar) un vehiculo a un viaje.
 */
public record AsignacionVehiculoSolicitud(

        @NotBlank(message = "El vehiculo es obligatorio")
        String vehiculoId
) {
}
