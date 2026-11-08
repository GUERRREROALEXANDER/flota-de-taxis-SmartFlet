package smartflet.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smartflet.dto.VehiculoRespuesta;
import smartflet.dto.VehiculoSolicitud;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.excepcion.ReglaNegocioException;
import smartflet.modelo.Vehiculo;
import smartflet.repositorio.VehiculoRepositorio;

/**
 * Logica de negocio de los vehiculos de la flota.
 */
@Service
public class VehiculoServicio {

    private final VehiculoRepositorio vehiculoRepositorio;

    public VehiculoServicio(VehiculoRepositorio vehiculoRepositorio) {
        this.vehiculoRepositorio = vehiculoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<VehiculoRespuesta> listarTodos() {
        return vehiculoRepositorio.findAll().stream()
                .map(VehiculoRespuesta::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehiculoRespuesta> listarPorEstado(Boolean estadoOperativo) {
        return vehiculoRepositorio.findByEstadoOperativo(estadoOperativo).stream()
                .map(VehiculoRespuesta::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehiculoRespuesta buscarPorId(Long id) {
        return VehiculoRespuesta.desde(obtenerVehiculo(id));
    }

    @Transactional
    public VehiculoRespuesta crear(VehiculoSolicitud solicitud) {
        String placa = normalizarPlaca(solicitud.placa());
        if (vehiculoRepositorio.existsByPlaca(placa)) {
            throw new ReglaNegocioException("Ya existe un vehiculo con la placa " + placa);
        }

        Vehiculo vehiculo = new Vehiculo(
                placa,
                solicitud.modelo().trim(),
                solicitud.capacidad(),
                solicitud.tipo().trim(),
                solicitud.estadoOperativo());

        return VehiculoRespuesta.desde(vehiculoRepositorio.save(vehiculo));
    }

    @Transactional
    public VehiculoRespuesta actualizar(Long id, VehiculoSolicitud solicitud) {
        Vehiculo vehiculo = obtenerVehiculo(id);

        String placa = normalizarPlaca(solicitud.placa());
        if (vehiculoRepositorio.existsByPlacaAndIdNot(placa, id)) {
            throw new ReglaNegocioException("Ya existe otro vehiculo con la placa " + placa);
        }

        vehiculo.setPlaca(placa);
        vehiculo.setModelo(solicitud.modelo().trim());
        vehiculo.setCapacidad(solicitud.capacidad());
        vehiculo.setTipo(solicitud.tipo().trim());
        vehiculo.setEstadoOperativo(solicitud.estadoOperativo());

        return VehiculoRespuesta.desde(vehiculoRepositorio.save(vehiculo));
    }

    @Transactional
    public VehiculoRespuesta cambiarEstado(Long id, Boolean estadoOperativo) {
        Vehiculo vehiculo = obtenerVehiculo(id);
        vehiculo.setEstadoOperativo(estadoOperativo);
        return VehiculoRespuesta.desde(vehiculoRepositorio.save(vehiculo));
    }

    @Transactional
    public void eliminar(Long id) {
        Vehiculo vehiculo = obtenerVehiculo(id);
        vehiculoRepositorio.delete(vehiculo);
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
