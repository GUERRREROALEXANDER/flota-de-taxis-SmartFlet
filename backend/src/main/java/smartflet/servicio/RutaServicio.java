package smartflet.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smartflet.dto.RutaRespuesta;
import smartflet.dto.RutaSolicitud;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.excepcion.ReglaNegocioException;
import smartflet.modelo.Ruta;
import smartflet.repositorio.RutaRepositorio;

/**
 * Logica de negocio de las rutas (trayectos origen - destino).
 */
@Service
public class RutaServicio {

    private final RutaRepositorio rutaRepositorio;

    public RutaServicio(RutaRepositorio rutaRepositorio) {
        this.rutaRepositorio = rutaRepositorio;
    }

    @Transactional(readOnly = true)
    public List<RutaRespuesta> listarTodas() {
        return rutaRepositorio.findAll().stream()
                .map(RutaRespuesta::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RutaRespuesta> buscarPorOrigen(String origen) {
        return rutaRepositorio.findByOrigenContainingIgnoreCase(origen.trim()).stream()
                .map(RutaRespuesta::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public RutaRespuesta buscarPorId(Long id) {
        return RutaRespuesta.desde(obtenerRuta(id));
    }

    @Transactional
    public RutaRespuesta crear(RutaSolicitud solicitud) {
        String origen = solicitud.origen().trim();
        String destino = solicitud.destino().trim();

        validarOrigenDistintoDeDestino(origen, destino);
        if (rutaRepositorio.existsByOrigenIgnoreCaseAndDestinoIgnoreCase(origen, destino)) {
            throw new ReglaNegocioException("Ya existe una ruta de " + origen + " a " + destino);
        }

        Ruta ruta = new Ruta(origen, destino, solicitud.distanciaKm(), solicitud.duracionEstimadaMinutos());
        return RutaRespuesta.desde(rutaRepositorio.save(ruta));
    }

    @Transactional
    public RutaRespuesta actualizar(Long id, RutaSolicitud solicitud) {
        Ruta ruta = obtenerRuta(id);

        String origen = solicitud.origen().trim();
        String destino = solicitud.destino().trim();

        validarOrigenDistintoDeDestino(origen, destino);
        if (rutaRepositorio.existsByOrigenIgnoreCaseAndDestinoIgnoreCaseAndIdNot(origen, destino, id)) {
            throw new ReglaNegocioException("Ya existe otra ruta de " + origen + " a " + destino);
        }

        ruta.setOrigen(origen);
        ruta.setDestino(destino);
        ruta.setDistanciaKm(solicitud.distanciaKm());
        ruta.setDuracionEstimadaMinutos(solicitud.duracionEstimadaMinutos());

        return RutaRespuesta.desde(rutaRepositorio.save(ruta));
    }

    @Transactional
    public void eliminar(Long id) {
        Ruta ruta = obtenerRuta(id);
        rutaRepositorio.delete(ruta);
    }

    private Ruta obtenerRuta(Long id) {
        return rutaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta", id));
    }

    private void validarOrigenDistintoDeDestino(String origen, String destino) {
        if (origen.equalsIgnoreCase(destino)) {
            throw new ReglaNegocioException("El origen y el destino no pueden ser el mismo lugar");
        }
    }
}
