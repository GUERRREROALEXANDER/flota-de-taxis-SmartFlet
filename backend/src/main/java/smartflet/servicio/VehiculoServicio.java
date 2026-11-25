package smartflet.servicio;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import smartflet.dto.VehiculoRespuesta;
import smartflet.dto.VehiculoSolicitud;
import smartflet.excepcion.RecursoDuplicadoException;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.excepcion.ReglaNegocioException;
import smartflet.modelo.EstadoViaje;
import smartflet.modelo.Vehiculo;
import smartflet.repositorio.TurnoRepositorio;
import smartflet.repositorio.VehiculoRepositorio;
import smartflet.repositorio.ViajeRepositorio;

/**
 * Logica de negocio de los vehiculos de la flota.
 */
@Service
public class VehiculoServicio {

    private static final Set<EstadoViaje> VIAJES_ACTIVOS = Set.of(EstadoViaje.ASIGNADO, EstadoViaje.EN_CURSO);

    private final VehiculoRepositorio vehiculoRepositorio;
    private final TurnoRepositorio turnoRepositorio;
    private final ViajeRepositorio viajeRepositorio;

    public VehiculoServicio(VehiculoRepositorio vehiculoRepositorio, TurnoRepositorio turnoRepositorio,
                            ViajeRepositorio viajeRepositorio) {
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.turnoRepositorio = turnoRepositorio;
        this.viajeRepositorio = viajeRepositorio;
    }

    public List<VehiculoRespuesta> listarTodos() {
        return vehiculoRepositorio.findAll().stream()
                .map(VehiculoRespuesta::desde)
                .toList();
    }

    public List<VehiculoRespuesta> listarPorEstado(Boolean estadoOperativo) {
        return vehiculoRepositorio.findByEstadoOperativo(estadoOperativo).stream()
                .map(VehiculoRespuesta::desde)
                .toList();
    }

    public VehiculoRespuesta buscarPorId(Long id) {
        return VehiculoRespuesta.desde(obtenerVehiculo(id));
    }

    public VehiculoRespuesta crear(VehiculoSolicitud solicitud) {
        String placa = normalizarPlaca(solicitud.placa());
        if (vehiculoRepositorio.existsByPlaca(placa)) {
            throw new RecursoDuplicadoException("Ya existe un vehiculo con la placa " + placa);
        }

        Vehiculo vehiculo = new Vehiculo(
                placa,
                solicitud.modelo().trim(),
                solicitud.capacidad(),
                solicitud.tipo().trim(),
                solicitud.estadoOperativo());

        return VehiculoRespuesta.desde(vehiculoRepositorio.save(vehiculo));
    }

    public VehiculoRespuesta actualizar(Long id, VehiculoSolicitud solicitud) {
        Vehiculo vehiculo = obtenerVehiculo(id);

        String placa = normalizarPlaca(solicitud.placa());
        if (vehiculoRepositorio.existsByPlacaAndIdNot(placa, id)) {
            throw new RecursoDuplicadoException("Ya existe otro vehiculo con la placa " + placa);
        }

        vehiculo.setPlaca(placa);
        vehiculo.setModelo(solicitud.modelo().trim());
        vehiculo.setCapacidad(solicitud.capacidad());
        vehiculo.setTipo(solicitud.tipo().trim());
        validarPuedeSalirDeServicio(vehiculo, solicitud.estadoOperativo());
        vehiculo.setEstadoOperativo(solicitud.estadoOperativo());

        return VehiculoRespuesta.desde(vehiculoRepositorio.save(vehiculo));
    }

    public VehiculoRespuesta cambiarEstado(Long id, Boolean estadoOperativo) {
        Vehiculo vehiculo = obtenerVehiculo(id);
        validarPuedeSalirDeServicio(vehiculo, estadoOperativo);
        vehiculo.setEstadoOperativo(estadoOperativo);
        return VehiculoRespuesta.desde(vehiculoRepositorio.save(vehiculo));
    }

    public void eliminar(Long id) {
        Vehiculo vehiculo = obtenerVehiculo(id);
        if (turnoRepositorio.existsByVehiculoId(id) || viajeRepositorio.existsByVehiculoId(id)) {
            throw new ReglaNegocioException("El vehiculo " + vehiculo.getPlaca()
                    + " tiene turnos o viajes registrados: sacalo de servicio en vez de eliminarlo");
        }
        vehiculoRepositorio.delete(vehiculo);
    }

    /** Un vehiculo que atiende un viaje (asignado o en curso) no puede salir de servicio. */
    private void validarPuedeSalirDeServicio(Vehiculo vehiculo, Boolean nuevoEstado) {
        if (Boolean.FALSE.equals(nuevoEstado)
                && viajeRepositorio.existsByVehiculoIdAndEstadoViajeIn(vehiculo.getId(), VIAJES_ACTIVOS)) {
            throw new ReglaNegocioException("El vehiculo " + vehiculo.getPlaca()
                    + " esta atendiendo un viaje: no puede salir de servicio");
        }
    }

    private Vehiculo obtenerVehiculo(Long id) {
        return vehiculoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", id));
    }

    /** Guarda todas las placas igual: "abc-123" y "ABC123" quedan como "ABC123". */
    private String normalizarPlaca(String placa) {
        return placa.trim().replace("-", "").toUpperCase();
    }
}
