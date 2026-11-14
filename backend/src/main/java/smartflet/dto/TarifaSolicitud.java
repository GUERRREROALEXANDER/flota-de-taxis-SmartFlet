package smartflet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Datos que envia el cliente para crear o modificar una tarifa. fechaFinVigencia es opcional.
 */
public record TarifaSolicitud(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
        String nombre,

        @NotNull(message = "El valor base es obligatorio")
        @PositiveOrZero(message = "El valor base no puede ser negativo")
        BigDecimal valorBase,

        @NotNull(message = "El valor por km es obligatorio")
        @Positive(message = "El valor por km debe ser mayor que 0")
        BigDecimal valorPorKm,

        @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
        LocalDate fechaInicioVigencia,

        LocalDate fechaFinVigencia
) {
}
