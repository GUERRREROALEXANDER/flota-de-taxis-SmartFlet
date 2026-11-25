package smartflet.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import smartflet.dto.RespuestaApi;
import smartflet.dto.RutaRespuesta;
import smartflet.dto.RutaSolicitud;
import smartflet.servicio.RutaServicio;

/**
 * Endpoints REST de rutas: /api/rutas
 * Responde siempre { mensaje, datos }. Los errores (404, 400, 409) los resuelve ManejadorGlobalExcepciones.
 */
@RestController
@RequestMapping("/api/rutas")
public class RutaControlador {

    private final RutaServicio rutaServicio;

    public RutaControlador(RutaServicio rutaServicio) {
        this.rutaServicio = rutaServicio;
    }

    /** GET /api/rutas  o  GET /api/rutas?origen=Centro */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<RutaRespuesta>>> listar(@RequestParam(required = false) String origen) {
        List<RutaRespuesta> rutas = (origen == null || origen.isBlank())
                ? rutaServicio.listarTodas()
                : rutaServicio.buscarPorOrigen(origen);
        return ResponseEntity.ok(RespuestaApi.de("Rutas encontradas", rutas));
    }

    /** GET /api/rutas/1 → la ruta con ID 1, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<RutaRespuesta>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Ruta encontrada", rutaServicio.buscarPorId(id)));
    }

    /** POST /api/rutas → 201 con la ruta creada. */
    @PostMapping
    public ResponseEntity<RespuestaApi<RutaRespuesta>> crear(@Valid @RequestBody RutaSolicitud solicitud) {
        RutaRespuesta creada = rutaServicio.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/rutas/" + creada.id()))
                .body(RespuestaApi.de("Ruta creada con exito", creada));
    }

    /** PUT /api/rutas/1 → reemplaza los datos de la ruta 1. */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<RutaRespuesta>> actualizar(
            @PathVariable Long id, @Valid @RequestBody RutaSolicitud solicitud) {
        return ResponseEntity.ok(RespuestaApi.de("Ruta actualizada con exito", rutaServicio.actualizar(id, solicitud)));
    }

    /** DELETE /api/rutas/1 */
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminar(@PathVariable Long id) {
        rutaServicio.eliminar(id);
        return ResponseEntity.ok(RespuestaApi.de("Ruta eliminada con exito", null));
    }
}
