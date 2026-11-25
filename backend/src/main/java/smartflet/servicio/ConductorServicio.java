package smartflet.servicio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import smartflet.dto.ConductorRespuesta;
import smartflet.dto.ConductorSolicitud;
import smartflet.excepcion.RecursoDuplicadoException;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.excepcion.ReglaNegocioException;
import smartflet.modelo.Conductor;
import smartflet.modelo.EstadoRegistro;
import smartflet.repositorio.ConductorRepositorio;

/**
 * Logica de negocio de los conductores. No se registra un conductor con la licencia vencida.
 */
@Service
public class ConductorServicio {

    private final ConductorRepositorio conductorRepositorio;

    public ConductorServicio(ConductorRepositorio conductorRepositorio) {
        this.conductorRepositorio = conductorRepositorio;
    }

    public List<ConductorRespuesta> listarTodos() {
        return conductorRepositorio.findAll().stream()
                .map(ConductorRespuesta::desde)
                .toList();
    }

    public List<ConductorRespuesta> listarPorEstado(EstadoRegistro estado) {
        return conductorRepositorio.findByEstadoConductor(estado).stream()
                .map(ConductorRespuesta::desde)
                .toList();
    }

    public ConductorRespuesta buscarPorId(Long id) {
        return ConductorRespuesta.desde(obtenerConductor(id));
    }

    public ConductorRespuesta registrar(ConductorSolicitud solicitud) {
        String identificacion = solicitud.identificacion().trim();
        String licencia = normalizarLicencia(solicitud.numeroLicencia());

        validarLicenciaVigente(solicitud.fechaVencimientoLicencia());
        if (conductorRepositorio.existsByIdentificacion(identificacion)) {
            throw new RecursoDuplicadoException("Ya existe un conductor con la identificacion " + identificacion);
        }
        if (conductorRepositorio.existsByNumeroLicencia(licencia)) {
            throw new RecursoDuplicadoException("Ya existe un conductor con la licencia " + licencia);
        }

        Conductor conductor = new Conductor(identificacion, solicitud.nombre().trim(), licencia,
                solicitud.fechaVencimientoLicencia());
        return ConductorRespuesta.desde(conductorRepositorio.save(conductor));
    }

    public ConductorRespuesta actualizar(Long id, ConductorSolicitud solicitud) {
        Conductor conductor = obtenerConductor(id);
        String identificacion = solicitud.identificacion().trim();
        String licencia = normalizarLicencia(solicitud.numeroLicencia());

        validarLicenciaVigente(solicitud.fechaVencimientoLicencia());
        if (conductorRepositorio.existsByIdentificacionAndIdNot(identificacion, id)) {
            throw new RecursoDuplicadoException("Ya existe otro conductor con la identificacion " + identificacion);
        }
        if (conductorRepositorio.existsByNumeroLicenciaAndIdNot(licencia, id)) {
            throw new RecursoDuplicadoException("Ya existe otro conductor con la licencia " + licencia);
        }

        conductor.setIdentificacion(identificacion);
        conductor.setNombre(solicitud.nombre().trim());
        conductor.setNumeroLicencia(licencia);
        conductor.setFechaVencimientoLicencia(solicitud.fechaVencimientoLicencia());
        return ConductorRespuesta.desde(conductorRepositorio.save(conductor));
    }

    /** actualizarEstado() del diagrama de clases: activar o desactivar sin borrar el historial. */
    public ConductorRespuesta cambiarEstado(Long id, EstadoRegistro estado) {
        Conductor conductor = obtenerConductor(id);
        conductor.setEstadoConductor(estado);
        return ConductorRespuesta.desde(conductorRepositorio.save(conductor));
    }

    private Conductor obtenerConductor(Long id) {
        return conductorRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Conductor", id));
    }

    private void validarLicenciaVigente(LocalDate fechaVencimiento) {
        if (fechaVencimiento.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La licencia esta vencida (" + fechaVencimiento + ")");
        }
    }

    private String normalizarLicencia(String licencia) {
        return licencia.trim().toUpperCase();
    }
}
