package smartflet.dto;

import jakarta.validation.constraints.NotNull;
import smartflet.modelo.EstadoRegistro;

/**
 * Cuerpo para activar o desactivar un pasajero o un conductor.
 */
public record EstadoRegistroSolicitud(

        @NotNull(message = "El estado es obligatorio (ACTIVO o INACTIVO)")
        EstadoRegistro estado
) {
}
