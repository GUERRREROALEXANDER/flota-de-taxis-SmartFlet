package smartflet.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * Precio del servicio: valor base + valor por kilometro, vigente entre dos fechas.
 * Si fechaFinVigencia es null, la tarifa no tiene fecha de fin.
 * Cada Viaje guarda una COPIA embebida de la tarifa vigente al solicitarlo: si la tarifa cambia despues,
 * el precio de ese viaje no cambia.
 */
@Document(collection = "tarifas")
public class Tarifa {

    @Id
    private String id;

    private String nombre;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal valorBase;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal valorPorKm;

    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;

    private EstadoRegistro estado;

    /** Constructor vacio que usa Spring Data para leer el documento. */
    protected Tarifa() {
    }

    /** Una tarifa nueva queda ACTIVA. */
    public Tarifa(String nombre, BigDecimal valorBase, BigDecimal valorPorKm,
                  LocalDate fechaInicioVigencia, LocalDate fechaFinVigencia) {
        this.nombre = nombre;
        this.valorBase = valorBase;
        this.valorPorKm = valorPorKm;
        this.fechaInicioVigencia = fechaInicioVigencia;
        this.fechaFinVigencia = fechaFinVigencia;
        this.estado = EstadoRegistro.ACTIVO;
    }

    /** Precio de un viaje: valorBase + valorPorKm * distanciaKm, redondeado a 2 decimales. */
    public BigDecimal calcularPrecio(Double distanciaKm) {
        return valorBase.add(valorPorKm.multiply(BigDecimal.valueOf(distanciaKm)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void desactivarTarifa() {
        this.estado = EstadoRegistro.INACTIVO;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getValorBase() {
        return valorBase;
    }

    public void setValorBase(BigDecimal valorBase) {
        this.valorBase = valorBase;
    }

    public BigDecimal getValorPorKm() {
        return valorPorKm;
    }

    public void setValorPorKm(BigDecimal valorPorKm) {
        this.valorPorKm = valorPorKm;
    }

    public LocalDate getFechaInicioVigencia() {
        return fechaInicioVigencia;
    }

    public void setFechaInicioVigencia(LocalDate fechaInicioVigencia) {
        this.fechaInicioVigencia = fechaInicioVigencia;
    }

    public LocalDate getFechaFinVigencia() {
        return fechaFinVigencia;
    }

    public void setFechaFinVigencia(LocalDate fechaFinVigencia) {
        this.fechaFinVigencia = fechaFinVigencia;
    }

    public EstadoRegistro getEstado() {
        return estado;
    }
}
