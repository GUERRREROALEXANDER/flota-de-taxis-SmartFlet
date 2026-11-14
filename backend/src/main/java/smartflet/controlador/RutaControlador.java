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
import smartflet.dto.RutaRespuesta;
import smartflet.dto.RutaSolicitud;
import smartflet.servicio.RutaServicio;

/**
 * Endpoints REST de rutas. Los errores (404, 400) los resuelve ManejadorGlobalExcepciones.
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
    public ResponseEntity<List<RutaRespuesta>> listar(@RequestParam(required = false) String origen) {
        List<RutaRespuesta> rutas = (origen == null || origen.isBlank())
                ? rutaServicio.listarTodas()
                : rutaServicio.buscarPorOrigen(origen);
        return ResponseEntity.ok(rutas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutaRespuesta> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(rutaServicio.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<RutaRespuesta> crear(@Valid @RequestBody RutaSolicitud solicitud) {
        RutaRespuesta creada = rutaServicio.crear(solicitud);
        URI ubicacion = URI.create("/api/rutas/" + creada.id());
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RutaRespuesta> actualizar(@PathVariable String id,
                                                    @Valid @RequestBody RutaSolicitud solicitud) {
        return ResponseEntity.ok(rutaServicio.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        rutaServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
