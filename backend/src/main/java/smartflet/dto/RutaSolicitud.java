package smartflet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos que envia el cliente para crear o actualizar una ruta.
 */
public record RutaSolicitud(

        @NotBlank(message = "El origen es obligatorio")
        @Size(max = 100, message = "El origen no puede superar 100 caracteres")
        String origen,

        @NotBlank(message = "El destino es obligatorio")
        @Size(max = 100, message = "El destino no puede superar 100 caracteres")
        String destino,

        @NotNull(message = "La distancia es obligatoria")
        @Positive(message = "La distancia debe ser mayor que 0")
        Double distanciaKm,

        @NotNull(message = "La duracion estimada es obligatoria")
        @Min(value = 1, message = "La duracion estimada minima es 1 minuto")
        @Max(value = 1440, message = "La duracion estimada no puede superar 24 horas")
        Integer duracionEstimadaMinutos
) {
}
