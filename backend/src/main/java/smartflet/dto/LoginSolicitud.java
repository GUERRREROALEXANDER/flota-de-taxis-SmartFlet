package smartflet.dto;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de POST /api/auth/login. */
public record LoginSolicitud(

        @NotBlank(message = "Escribe tu nombre de usuario")
        String usuario,

        @NotBlank(message = "Escribe tu contrasena")
        String clave
) {
}
