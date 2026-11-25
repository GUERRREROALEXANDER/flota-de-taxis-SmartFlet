package smartflet.servicio;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import smartflet.dto.FinalizacionViajeSolicitud;
import smartflet.dto.ViajeRespuesta;
import smartflet.dto.ViajeSolicitud;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.excepcion.ReglaNegocioException;
import smartflet.modelo.EstadoRegistro;
import smartflet.modelo.EstadoTurno;
import smartflet.modelo.EstadoViaje;
import smartflet.modelo.Pasajero;
import smartflet.modelo.Tarifa;
import smartflet.modelo.Vehiculo;
import smartflet.modelo.Viaje;
import smartflet.repositorio.PasajeroRepositorio;
import smartflet.repositorio.TarifaRepositorio;
import smartflet.repositorio.TurnoRepositorio;
import smartflet.repositorio.VehiculoRepositorio;
import smartflet.repositorio.ViajeRepositorio;

/**
 * Logica de negocio del despacho de viajes: solicitar, asignar vehiculo, iniciar, finalizar (con pago) y cancelar.
 * Las transiciones de estado las valida la propia entidad Viaje; aqui van las reglas que necesitan consultar datos.
 */
@Service
public class ViajeServicio {

    private static final Set<EstadoViaje> ACTIVOS = Set.of(EstadoViaje.ASIGNADO, EstadoViaje.EN_CURSO);

    private final ViajeRepositorio viajeRepositorio;
    private final PasajeroRepositorio pasajeroRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;
    private final TarifaRepositorio tarifaRepositorio;
    private final TurnoRepositorio turnoRepositorio;

    public ViajeServicio(ViajeRepositorio viajeRepositorio, PasajeroRepositorio pasajeroRepositorio,
                         VehiculoRepositorio vehiculoRepositorio, TarifaRepositorio tarifaRepositorio,
                         TurnoRepositorio turnoRepositorio) {
        this.viajeRepositorio = viajeRepositorio;
        this.pasajeroRepositorio = pasajeroRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.tarifaRepositorio = tarifaRepositorio;
        this.turnoRepositorio = turnoRepositorio;
    }

    public List<ViajeRespuesta> listarTodos() {
        return viajeRepositorio.findAll().stream()
                .map(ViajeRespuesta::desde)
                .toList();
    }

    public List<ViajeRespuesta> listarPorEstado(EstadoViaje estado) {
        return viajeRepositorio.findByEstadoViaje(estado).stream()
                .map(ViajeRespuesta::desde)
                .toList();
    }

    /** consultarHistorial() del pasajero. */
    public List<ViajeRespuesta> listarPorPasajero(Long pasajeroId) {
        return viajeRepositorio.findByPasajeroIdOrderByFechaSolicitudDesc(pasajeroId).stream()
                .map(ViajeRespuesta::desde)
                .toList();
    }

    public ViajeRespuesta buscarPorId(Long id) {
        return ViajeRespuesta.desde(obtenerViaje(id));
    }

    public ViajeRespuesta solicitar(ViajeSolicitud solicitud) {
        Pasajero pasajero = pasajeroRepositorio.findById(solicitud.pasajeroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pasajero", solicitud.pasajeroId()));
        if (!pasajero.estaActivo()) {
            throw new ReglaNegocioException("El pasajero " + pasajero.getNombre() + " tiene la cuenta inactiva");
        }
        String origen = solicitud.origen().trim();
        String destino = solicitud.destino().trim();
        if (origen.equalsIgnoreCase(destino)) {
            throw new ReglaNegocioException("El origen y el destino no pueden ser el mismo lugar");
        }
        Tarifa tarifa = tarifaRepositorio.buscarVigentes(EstadoRegistro.ACTIVO, LocalDate.now()).stream()
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException("No hay una tarifa vigente: no se pueden solicitar viajes"));

        Viaje viaje = new Viaje(pasajero.getId(), tarifa, origen, destino);
        return ViajeRespuesta.desde(viajeRepositorio.save(viaje));
    }

    /** Solo vehiculos operativos, con un turno EN_CURSO y sin otro viaje activo. */
    public ViajeRespuesta asignarVehiculo(Long id, Long vehiculoId) {
        Viaje viaje = obtenerViaje(id);
        Vehiculo vehiculo = vehiculoRepositorio.findById(vehiculoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", vehiculoId));

        if (!Boolean.TRUE.equals(vehiculo.getEstadoOperativo())) {
            throw new ReglaNegocioException("El vehiculo " + vehiculo.getPlaca() + " esta fuera de servicio");
        }
        if (!turnoRepositorio.existsByVehiculoIdAndEstado(vehiculoId, EstadoTurno.EN_CURSO)) {
            throw new ReglaNegocioException("El vehiculo " + vehiculo.getPlaca() + " no tiene un turno en curso");
        }
        if (viajeRepositorio.existsByVehiculoIdAndEstadoViajeInAndIdNot(vehiculoId, ACTIVOS, id)) {
            throw new ReglaNegocioException("El vehiculo " + vehiculo.getPlaca() + " ya esta atendiendo otro viaje");
        }

        viaje.asignarVehiculo(vehiculo);
        return ViajeRespuesta.desde(viajeRepositorio.save(viaje));
    }

    public ViajeRespuesta iniciar(Long id) {
        Viaje viaje = obtenerViaje(id);
        viaje.iniciarViaje();
        return ViajeRespuesta.desde(viajeRepositorio.save(viaje));
    }

    /** Finaliza el viaje; el Pago queda embebido en el mismo documento (una sola escritura atomica). */
    public ViajeRespuesta finalizar(Long id, FinalizacionViajeSolicitud solicitud) {
        Viaje viaje = obtenerViaje(id);
        viaje.finalizarViaje(solicitud.distanciaKm(), solicitud.metodoPago());
        return ViajeRespuesta.desde(viajeRepositorio.save(viaje));
    }

    public ViajeRespuesta cancelar(Long id) {
        Viaje viaje = obtenerViaje(id);
        viaje.cancelarViaje();
        return ViajeRespuesta.desde(viajeRepositorio.save(viaje));
    }

    private Viaje obtenerViaje(Long id) {
        return viajeRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Viaje", id));
    }
}
