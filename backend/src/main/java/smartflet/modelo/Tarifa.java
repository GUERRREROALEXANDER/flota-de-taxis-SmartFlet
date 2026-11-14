package smartflet.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Precio del servicio: valor base + valor por kilometro, vigente entre dos fechas.
 * Si fechaFinVigencia es null, la tarifa no tiene fecha de fin.
 */
@Entity
@Table(name = "tarifa")
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(name = "valor_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorBase;

    @Column(name = "valor_por_km", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPorKm;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoRegistro estado;

    /** Constructor vacio que exige JPA. */
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

    public Long getId() {
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
