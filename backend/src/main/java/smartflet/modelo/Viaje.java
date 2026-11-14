package smartflet.modelo;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import smartflet.excepcion.ReglaNegocioException;

/**
 * Viaje de un pasajero: desde que se solicita hasta que finaliza o se cancela.
 * No tiene setters: el estado solo cambia con asignarVehiculo, iniciarViaje, finalizarViaje y cancelarViaje,
 * que validan las transiciones.
 *
 * Modelo en MongoDB (un solo documento por viaje):
 * - pasajeroId y vehiculoId: referencias por id.
 * - tarifa: COPIA embebida de la tarifa vigente al solicitar, para que el precio no cambie si la tarifa se modifica.
 * - pago: subdocumento embebido (composicion: el pago no existe sin el viaje). Finalizar escribe viaje y pago
 *   en una sola operacion atomica sobre el mismo documento.
 */
@Document(collection = "viajes")
public class Viaje {

    @Id
    private String id;

    @Indexed
    private String pasajeroId;

    /** Null mientras el viaje esta SOLICITADO. */
    @Indexed
    private String vehiculoId;

    private Tarifa tarifa;

    private String origen;

    private String destino;

    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private Double distanciaKm;

    private EstadoViaje estadoViaje;

    private Pago pago;

    /** Constructor vacio que usa Spring Data para leer el documento. */
    protected Viaje() {
    }

    /** Solicitar un viaje: queda SOLICITADO, sin vehiculo, con la tarifa fijada. */
    public Viaje(String pasajeroId, Tarifa tarifa, String origen, String destino) {
        this.pasajeroId = pasajeroId;
        this.tarifa = tarifa;
        this.origen = origen;
        this.destino = destino;
        this.fechaSolicitud = LocalDateTime.now();
        this.estadoViaje = EstadoViaje.SOLICITADO;
    }

    /** Asigna o reasigna el vehiculo mientras el viaje no haya iniciado. */
    public void asignarVehiculo(Vehiculo vehiculo) {
        if (estadoViaje != EstadoViaje.SOLICITADO && estadoViaje != EstadoViaje.ASIGNADO) {
            throw new ReglaNegocioException(
                    "Solo se puede asignar un vehiculo a un viaje SOLICITADO o ASIGNADO (estado actual: " + estadoViaje + ")");
        }
        this.vehiculoId = vehiculo.getId();
        this.estadoViaje = EstadoViaje.ASIGNADO;
    }

    public void iniciarViaje() {
        exigirEstado(EstadoViaje.ASIGNADO, "iniciar");
        this.fechaInicio = LocalDateTime.now();
        this.estadoViaje = EstadoViaje.EN_CURSO;
    }

    /** Cierra el viaje, cobra con la tarifa fijada y devuelve el pago creado. */
    public Pago finalizarViaje(Double distanciaKm, MetodoPago metodoPago) {
        exigirEstado(EstadoViaje.EN_CURSO, "finalizar");
        this.distanciaKm = distanciaKm;
        this.fechaFin = LocalDateTime.now();
        this.estadoViaje = EstadoViaje.FINALIZADO;
        this.pago = new Pago(tarifa.calcularPrecio(distanciaKm), metodoPago);
        return pago;
    }

    public void cancelarViaje() {
        if (estadoViaje != EstadoViaje.SOLICITADO && estadoViaje != EstadoViaje.ASIGNADO) {
            throw new ReglaNegocioException(
                    "Solo se puede cancelar un viaje antes de iniciar (estado actual: " + estadoViaje + ")");
        }
        this.fechaFin = LocalDateTime.now();
        this.estadoViaje = EstadoViaje.CANCELADO;
    }

    private void exigirEstado(EstadoViaje esperado, String accion) {
        if (estadoViaje != esperado) {
            throw new ReglaNegocioException("Para " + accion + " el viaje debe estar " + esperado
                    + " (estado actual: " + estadoViaje + ")");
        }
    }

    public String getId() {
        return id;
    }

    public String getPasajeroId() {
        return pasajeroId;
    }

    public String getVehiculoId() {
        return vehiculoId;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public EstadoViaje getEstadoViaje() {
        return estadoViaje;
    }

    public Pago getPago() {
        return pago;
    }
}
