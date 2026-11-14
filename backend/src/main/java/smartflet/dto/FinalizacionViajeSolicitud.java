package smartflet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import smartflet.modelo.MetodoPago;

/**
 * Cuerpo para finalizar un viaje: distancia recorrida y forma de pago.
 */
public record FinalizacionViajeSolicitud(

        @NotNull(message = "La distancia es obligatoria")
        @Positive(message = "La distancia debe ser mayor que 0")
        Double distanciaKm,

        @NotNull(message = "El metodo de pago es obligatorio (EFECTIVO, TARJETA o TRANSFERENCIA)")
        MetodoPago metodoPago
) {
}
