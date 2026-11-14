package smartflet.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos que envia el cliente para registrar o modificar un conductor.
 */
public record ConductorSolicitud(

        @NotBlank(message = "La identificacion es obligatoria")
        @Size(max = 20, message = "La identificacion no puede superar 20 caracteres")
        String identificacion,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @NotBlank(message = "El numero de licencia es obligatorio")
        @Size(max = 30, message = "El numero de licencia no puede superar 30 caracteres")
        String numeroLicencia,

        @NotNull(message = "La fecha de vencimiento de la licencia es obligatoria")
        LocalDate fechaVencimientoLicencia
) {
}
