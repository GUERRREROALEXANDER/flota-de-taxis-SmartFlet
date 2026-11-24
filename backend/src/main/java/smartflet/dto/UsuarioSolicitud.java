package smartflet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import smartflet.modelo.Rol;

/** Cuerpo de POST /api/usuarios (solo ADMINISTRADOR). */
public record UsuarioSolicitud(

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$",
                message = "Usa de 3 a 30 letras, numeros, punto, guion o guion bajo (sin espacios)")
        String nombreUsuario,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre admite maximo 80 caracteres")
        String nombre,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
        String clave,

        @NotNull(message = "El rol es obligatorio (ADMINISTRADOR u OPERADOR)")
        Rol rol
) {
}
