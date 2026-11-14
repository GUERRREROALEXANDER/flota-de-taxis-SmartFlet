package smartflet.dto;

import java.time.LocalDate;

import smartflet.modelo.EstadoRegistro;
import smartflet.modelo.Pasajero;

/**
 * Datos de un pasajero que se devuelven al cliente.
 */
public record PasajeroRespuesta(
        Long id,
        String identificacion,
        String nombre,
        String telefono,
        String correoElectronico,
        LocalDate fechaRegistro,
        EstadoRegistro estadoCuenta
) {

    public static PasajeroRespuesta desde(Pasajero pasajero) {
        return new PasajeroRespuesta(
                pasajero.getId(),
                pasajero.getIdentificacion(),
                pasajero.getNombre(),
                pasajero.getTelefono(),
                pasajero.getCorreoElectronico(),
                pasajero.getFechaRegistro(),
                pasajero.getEstadoCuenta());
    }
}
