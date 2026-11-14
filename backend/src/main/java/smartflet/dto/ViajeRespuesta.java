package smartflet.dto;

import java.time.LocalDateTime;

import smartflet.modelo.EstadoViaje;
import smartflet.modelo.Viaje;

/**
 * Datos de un viaje que se devuelven al cliente. vehiculo y pago son null mientras no existan.
 */
public record ViajeRespuesta(
        Long id,
        Long pasajeroId,
        String pasajeroNombre,
        Long vehiculoId,
        String vehiculoPlaca,
        Long tarifaId,
        String origen,
        String destino,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Double distanciaKm,
        EstadoViaje estadoViaje,
        PagoRespuesta pago
) {

    public static ViajeRespuesta desde(Viaje viaje) {
        boolean conVehiculo = viaje.getVehiculo() != null;
        return new ViajeRespuesta(
                viaje.getId(),
                viaje.getPasajero().getId(),
                viaje.getPasajero().getNombre(),
                conVehiculo ? viaje.getVehiculo().getId() : null,
                conVehiculo ? viaje.getVehiculo().getPlaca() : null,
                viaje.getTarifa().getId(),
                viaje.getOrigen(),
                viaje.getDestino(),
                viaje.getFechaSolicitud(),
                viaje.getFechaInicio(),
                viaje.getFechaFin(),
                viaje.getDistanciaKm(),
                viaje.getEstadoViaje(),
                viaje.getPago() != null ? PagoRespuesta.desde(viaje.getPago()) : null);
    }
}
