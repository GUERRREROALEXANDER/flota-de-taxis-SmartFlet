package smartflet.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import smartflet.excepcion.ReglaNegocioException;

/**
 * Cobro de un viaje finalizado. No es un documento propio: vive embebido dentro de su Viaje (composicion).
 * Solo lo crea Viaje.finalizarViaje() (constructor de paquete). Tiene id propio para consultarlo en /api/pagos/{id}.
 */
public class Pago {

    private String id;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal monto;

    private LocalDateTime fechaPago;

    private MetodoPago metodoPago;

    private EstadoPago estadoPago;

    /** Constructor vacio que usa Spring Data para leer el subdocumento. */
    protected Pago() {
    }

    /** Registrar el pago: queda PAGADO con fecha de ahora. */
    Pago(BigDecimal monto, MetodoPago metodoPago) {
        this.id = new ObjectId().toHexString();
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

    public String getId() {
        return id;
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
