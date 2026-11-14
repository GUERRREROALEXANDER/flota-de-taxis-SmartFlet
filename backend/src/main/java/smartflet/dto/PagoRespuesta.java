package smartflet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import smartflet.modelo.EstadoPago;
import smartflet.modelo.MetodoPago;
import smartflet.modelo.Pago;

/**
 * Datos de un pago que se devuelven al cliente.
 */
public record PagoRespuesta(
        Long id,
        Long viajeId,
        BigDecimal monto,
        LocalDateTime fechaPago,
        MetodoPago metodoPago,
        EstadoPago estadoPago
) {

    public static PagoRespuesta desde(Pago pago) {
        return new PagoRespuesta(
                pago.getId(),
                pago.getViaje().getId(),
                pago.getMonto(),
                pago.getFechaPago(),
                pago.getMetodoPago(),
                pago.getEstadoPago());
    }
}
