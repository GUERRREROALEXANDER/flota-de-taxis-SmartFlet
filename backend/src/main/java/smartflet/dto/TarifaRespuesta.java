package smartflet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import smartflet.modelo.EstadoRegistro;
import smartflet.modelo.Tarifa;

/**
 * Datos de una tarifa que se devuelven al cliente.
 */
public record TarifaRespuesta(
        String id,
        String nombre,
        BigDecimal valorBase,
        BigDecimal valorPorKm,
        LocalDate fechaInicioVigencia,
        LocalDate fechaFinVigencia,
        EstadoRegistro estado
) {

    public static TarifaRespuesta desde(Tarifa tarifa) {
        return new TarifaRespuesta(
                tarifa.getId(),
                tarifa.getNombre(),
                tarifa.getValorBase(),
                tarifa.getValorPorKm(),
                tarifa.getFechaInicioVigencia(),
                tarifa.getFechaFinVigencia(),
                tarifa.getEstado());
    }
}
