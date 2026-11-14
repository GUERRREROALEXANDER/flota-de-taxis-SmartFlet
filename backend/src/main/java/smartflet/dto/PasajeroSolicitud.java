package smartflet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que envia el cliente para registrar o modificar un pasajero.
 */
public record PasajeroSolicitud(

        @NotBlank(message = "La identificacion es obligatoria")
        @Size(max = 20, message = "La identificacion no puede superar 20 caracteres")
        String identificacion,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @NotBlank(message = "El telefono es obligatorio")
        @Pattern(regexp = "^[0-9+ ]{7,20}$", message = "El telefono solo admite numeros, espacios y +, entre 7 y 20")
        String telefono,

        @NotBlank(message = "El correo electronico es obligatorio")
        @Email(message = "El correo electronico no es valido")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres")
        String correoElectronico
) {
}
