package smartflet.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import smartflet.excepcion.ReglaNegocioException;

/**
 * Cobro de un viaje finalizado. Solo lo crea Viaje.finalizarViaje() (constructor de paquete).
 */
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false, unique = true)
    private Viaje viaje;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 15)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 10)
    private EstadoPago estadoPago;

    /** Constructor vacio que exige JPA. */
    protected Pago() {
    }

    /** Registrar el pago: queda PAGADO con fecha de ahora. */
    Pago(Viaje viaje, BigDecimal monto, MetodoPago metodoPago) {
        this.viaje = viaje;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.fechaPago = LocalDateTime.now();
        this.estadoPago = EstadoPago.PAGADO;
    }

    public void anularPago() {
        if (estadoPago == EstadoPago.ANULADO) {
            throw new ReglaNegocioException("El pago ya esta anulado");
        }
        this.estadoPago = EstadoPago.ANULADO;
    }

    public Long getId() {
        return id;
    }

    public Viaje getViaje() {
        return viaje;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }
}
