package smartflet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que envia el cliente para crear o actualizar un vehiculo.
 */
public record VehiculoSolicitud(

        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^[A-Za-z]{3}-?[0-9]{2}[0-9A-Za-z]$",
                 message = "La placa debe tener el formato ABC123 o ABC-123")
        String placa,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 60, message = "El modelo no puede superar 60 caracteres")
        String modelo,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad minima es 1 pasajero")
        @Max(value = 8, message = "La capacidad maxima es 8 pasajeros")
        Integer capacidad,

        @NotBlank(message = "El tipo es obligatorio")
        @Size(max = 30, message = "El tipo no puede superar 30 caracteres")
        String tipo,

        @NotNull(message = "El estado operativo es obligatorio")
        Boolean estadoOperativo
) {
}
