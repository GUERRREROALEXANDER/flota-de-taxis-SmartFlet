package smartflet.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo para cambiar solo el estado operativo de un vehiculo.
 */
public record EstadoVehiculoSolicitud(

        @NotNull(message = "El estado operativo es obligatorio")
        Boolean estadoOperativo
) {
}
