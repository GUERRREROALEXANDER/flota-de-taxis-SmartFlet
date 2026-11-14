package smartflet.dto;

import java.time.LocalDate;

import smartflet.modelo.Conductor;
import smartflet.modelo.EstadoRegistro;

/**
 * Datos de un conductor que se devuelven al cliente.
 */
public record ConductorRespuesta(
        Long id,
        String identificacion,
        String nombre,
        String numeroLicencia,
        LocalDate fechaVencimientoLicencia,
        EstadoRegistro estadoConductor
) {

    public static ConductorRespuesta desde(Conductor conductor) {
        return new ConductorRespuesta(
                conductor.getId(),
                conductor.getIdentificacion(),
                conductor.getNombre(),
                conductor.getNumeroLicencia(),
                conductor.getFechaVencimientoLicencia(),
                conductor.getEstadoConductor());
    }
}
