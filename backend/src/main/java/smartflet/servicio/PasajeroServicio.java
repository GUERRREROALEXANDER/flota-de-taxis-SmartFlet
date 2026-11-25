package smartflet.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import smartflet.dto.PasajeroRespuesta;
import smartflet.dto.PasajeroSolicitud;
import smartflet.excepcion.RecursoDuplicadoException;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.modelo.EstadoRegistro;
import smartflet.modelo.Pasajero;
import smartflet.repositorio.PasajeroRepositorio;

/**
 * Logica de negocio de los pasajeros. No se borran: se desactivan para conservar su historial de viajes.
 */
@Service
public class PasajeroServicio {

    private final PasajeroRepositorio pasajeroRepositorio;

    public PasajeroServicio(PasajeroRepositorio pasajeroRepositorio) {
        this.pasajeroRepositorio = pasajeroRepositorio;
    }

    public List<PasajeroRespuesta> listarTodos() {
        return pasajeroRepositorio.findAll().stream()
                .map(PasajeroRespuesta::desde)
                .toList();
    }

    public PasajeroRespuesta buscarPorId(Long id) {
        return PasajeroRespuesta.desde(obtenerPasajero(id));
    }

    public PasajeroRespuesta registrar(PasajeroSolicitud solicitud) {
        String identificacion = solicitud.identificacion().trim();
        if (pasajeroRepositorio.existsByIdentificacion(identificacion)) {
            throw new RecursoDuplicadoException("Ya existe un pasajero con la identificacion " + identificacion);
        }
        Pasajero pasajero = new Pasajero(identificacion, solicitud.nombre().trim(),
                solicitud.telefono().trim(), solicitud.correoElectronico().trim().toLowerCase());
        return PasajeroRespuesta.desde(pasajeroRepositorio.save(pasajero));
    }

    public PasajeroRespuesta modificarDatos(Long id, PasajeroSolicitud solicitud) {
        Pasajero pasajero = obtenerPasajero(id);
        String identificacion = solicitud.identificacion().trim();
        if (pasajeroRepositorio.existsByIdentificacionAndIdNot(identificacion, id)) {
            throw new RecursoDuplicadoException("Ya existe otro pasajero con la identificacion " + identificacion);
        }
        pasajero.setIdentificacion(identificacion);
        pasajero.setNombre(solicitud.nombre().trim());
        pasajero.setTelefono(solicitud.telefono().trim());
        pasajero.setCorreoElectronico(solicitud.correoElectronico().trim().toLowerCase());
        return PasajeroRespuesta.desde(pasajeroRepositorio.save(pasajero));
    }

    public PasajeroRespuesta cambiarEstado(Long id, EstadoRegistro estado) {
        Pasajero pasajero = obtenerPasajero(id);
        pasajero.setEstadoCuenta(estado);
        return PasajeroRespuesta.desde(pasajeroRepositorio.save(pasajero));
    }

    private Pasajero obtenerPasajero(Long id) {
        return pasajeroRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pasajero", id));
    }
}
