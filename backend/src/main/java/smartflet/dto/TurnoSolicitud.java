package smartflet.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos para programar un turno: que conductor opera que vehiculo y entre que horas.
 */
public record TurnoSolicitud(

        @NotBlank(message = "El conductor es obligatorio")
        String conductorId,

        @NotBlank(message = "El vehiculo es obligatorio")
        String vehiculoId,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDateTime fechaFin
) {
}
