package smartflet.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import smartflet.excepcion.ReglaNegocioException;

/**
 * Viaje de un pasajero: desde que se solicita hasta que finaliza o se cancela.
 * No tiene setters: el estado solo cambia con asignarVehiculo, iniciarViaje, finalizarViaje y cancelarViaje,
 * que validan las transiciones. Al finalizar crea su Pago (composicion: el pago no existe sin el viaje).
 */
@Entity
@Table(name = "viaje")
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pasajero_id", nullable = false)
    private Pasajero pasajero;

    /** Null mientras el viaje esta SOLICITADO. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    /** Tarifa vigente al solicitar; queda fija aunque la tarifa cambie despues. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_id", nullable = false)
    private Tarifa tarifa;

    @Column(nullable = false, length = 150)
    private String origen;

    @Column(nullable = false, length = 150)
    private String destino;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_viaje", nullable = false, length = 12)
    private EstadoViaje estadoViaje;

    @OneToOne(mappedBy = "viaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pago pago;

    /** Constructor vacio que exige JPA. */
    protected Viaje() {
    }

    /** Solicitar un viaje: queda SOLICITADO, sin vehiculo. */
    public Viaje(Pasajero pasajero, Tarifa tarifa, String origen, String destino) {
        this.pasajero = pasajero;
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
        this.vehiculo = vehiculo;
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
        this.pago = new Pago(this, tarifa.calcularPrecio(distanciaKm), metodoPago);
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

    public Long getId() {
        return id;
    }

    public Pasajero getPasajero() {
        return pasajero;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
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
