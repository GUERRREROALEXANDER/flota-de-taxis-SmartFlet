package smartflet.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import smartflet.dto.TurnoRespuesta;
import smartflet.dto.TurnoSolicitud;
import smartflet.excepcion.RecursoDuplicadoException;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.excepcion.ReglaNegocioException;
import smartflet.modelo.Conductor;
import smartflet.modelo.Turno;
import smartflet.modelo.Vehiculo;
import smartflet.repositorio.ConductorRepositorio;
import smartflet.repositorio.TurnoRepositorio;
import smartflet.repositorio.VehiculoRepositorio;

/**
 * Logica de negocio de los turnos: que conductor opera que vehiculo y cuando.
 */
@Service
public class TurnoServicio {

    private final TurnoRepositorio turnoRepositorio;
    private final ConductorRepositorio conductorRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;

    public TurnoServicio(TurnoRepositorio turnoRepositorio, ConductorRepositorio conductorRepositorio,
                         VehiculoRepositorio vehiculoRepositorio) {
        this.turnoRepositorio = turnoRepositorio;
        this.conductorRepositorio = conductorRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
    }

    public List<TurnoRespuesta> listarTodos() {
        return turnoRepositorio.findAll().stream()
                .map(TurnoRespuesta::desde)
                .toList();
    }

    /** consultarTurnos() del conductor. */
    public List<TurnoRespuesta> listarPorConductor(String conductorId) {
        return turnoRepositorio.findByConductorIdOrderByFechaInicioDesc(conductorId).stream()
                .map(TurnoRespuesta::desde)
                .toList();
    }

    public TurnoRespuesta buscarPorId(String id) {
        return TurnoRespuesta.desde(obtenerTurno(id));
    }

    public TurnoRespuesta programar(TurnoSolicitud solicitud) {
        if (!solicitud.fechaFin().isAfter(solicitud.fechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin del turno debe ser posterior a la de inicio");
        }
        Conductor conductor = conductorRepositorio.findById(solicitud.conductorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Conductor", solicitud.conductorId()));
        Vehiculo vehiculo = vehiculoRepositorio.findById(solicitud.vehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", solicitud.vehiculoId()));

        if (!conductor.estaActivo()) {
            throw new ReglaNegocioException("El conductor " + conductor.getNombre() + " esta inactivo");
        }
        if (!conductor.licenciaVigenteEn(solicitud.fechaFin().toLocalDate())) {
            throw new ReglaNegocioException("La licencia del conductor vence antes de terminar el turno");
        }
        if (!Boolean.TRUE.equals(vehiculo.getEstadoOperativo())) {
            throw new ReglaNegocioException("El vehiculo " + vehiculo.getPlaca() + " esta fuera de servicio");
        }
        if (turnoRepositorio.conductorTieneTurnoSolapado(conductor.getId(), solicitud.fechaInicio(),
                solicitud.fechaFin())) {
            throw new RecursoDuplicadoException("El conductor ya tiene un turno que se cruza con ese horario");
        }
        if (turnoRepositorio.vehiculoTieneTurnoSolapado(vehiculo.getId(), solicitud.fechaInicio(),
                solicitud.fechaFin())) {
            throw new RecursoDuplicadoException("El vehiculo ya tiene un turno que se cruza con ese horario");
        }

        Turno turno = new Turno(conductor.getId(), vehiculo.getId(), solicitud.fechaInicio(), solicitud.fechaFin());
        return TurnoRespuesta.desde(turnoRepositorio.save(turno));
    }

    public TurnoRespuesta iniciar(String id) {
        Turno turno = obtenerTurno(id);
        Conductor conductor = conductorRepositorio.findById(turno.getConductorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Conductor", turno.getConductorId()));
        if (!conductor.estaActivo()) {
            throw new ReglaNegocioException("El conductor del turno esta inactivo");
        }
        turno.iniciarTurno();
        return TurnoRespuesta.desde(turnoRepositorio.save(turno));
    }

    public TurnoRespuesta finalizar(String id) {
        Turno turno = obtenerTurno(id);
        turno.finalizarTurno();
        return TurnoRespuesta.desde(turnoRepositorio.save(turno));
    }

    private Turno obtenerTurno(String id) {
        return turnoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno", id));
    }
}
