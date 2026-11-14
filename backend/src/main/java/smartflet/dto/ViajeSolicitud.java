package smartflet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para solicitar un viaje.
 */
public record ViajeSolicitud(

        @NotNull(message = "El pasajero es obligatorio")
        Long pasajeroId,

        @NotBlank(message = "El origen es obligatorio")
        @Size(max = 150, message = "El origen no puede superar 150 caracteres")
        String origen,

        @NotBlank(message = "El destino es obligatorio")
        @Size(max = 150, message = "El destino no puede superar 150 caracteres")
        String destino
) {
}
