package smartflet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import smartflet.modelo.EstadoViaje;
import smartflet.modelo.Viaje;

/**
 * Datos de un viaje que se devuelven al cliente. vehiculoId y pago son null mientras no existan.
 * tarifaValorBase y tarifaValorPorKm son los valores fijados al solicitar el viaje.
 */
public record ViajeRespuesta(
        String id,
        String pasajeroId,
        String vehiculoId,
        String tarifaId,
        BigDecimal tarifaValorBase,
        BigDecimal tarifaValorPorKm,
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
        return new ViajeRespuesta(
                viaje.getId(),
                viaje.getPasajeroId(),
                viaje.getVehiculoId(),
                viaje.getTarifa().getId(),
                viaje.getTarifa().getValorBase(),
                viaje.getTarifa().getValorPorKm(),
                viaje.getOrigen(),
                viaje.getDestino(),
                viaje.getFechaSolicitud(),
                viaje.getFechaInicio(),
                viaje.getFechaFin(),
                viaje.getDistanciaKm(),
                viaje.getEstadoViaje(),
                viaje.getPago() != null ? PagoRespuesta.desde(viaje.getPago(), viaje.getId()) : null);
    }
}
