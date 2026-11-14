package smartflet.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import smartflet.dto.TarifaRespuesta;
import smartflet.dto.TarifaSolicitud;
import smartflet.servicio.TarifaServicio;

/**
 * Endpoints REST de tarifas. No hay DELETE: una tarifa se desactiva.
 */
@RestController
@RequestMapping("/api/tarifas")
public class TarifaControlador {

    private final TarifaServicio tarifaServicio;

    public TarifaControlador(TarifaServicio tarifaServicio) {
        this.tarifaServicio = tarifaServicio;
    }

    @GetMapping
    public ResponseEntity<List<TarifaRespuesta>> listar() {
        return ResponseEntity.ok(tarifaServicio.listarTodas());
    }

    /** Tarifa activa cuya vigencia incluye hoy. 404 si no hay. */
    @GetMapping("/vigente")
    public ResponseEntity<TarifaRespuesta> buscarVigente() {
        return ResponseEntity.ok(tarifaServicio.buscarVigente());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifaRespuesta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tarifaServicio.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TarifaRespuesta> crear(@Valid @RequestBody TarifaSolicitud solicitud) {
        TarifaRespuesta creada = tarifaServicio.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/tarifas/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarifaRespuesta> modificar(@PathVariable Long id,
                                                     @Valid @RequestBody TarifaSolicitud solicitud) {
        return ResponseEntity.ok(tarifaServicio.modificar(id, solicitud));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<TarifaRespuesta> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(tarifaServicio.desactivar(id));
    }
}
