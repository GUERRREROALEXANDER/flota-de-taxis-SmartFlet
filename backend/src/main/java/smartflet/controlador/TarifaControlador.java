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
import smartflet.dto.RespuestaApi;
import smartflet.dto.TarifaRespuesta;
import smartflet.dto.TarifaSolicitud;
import smartflet.servicio.TarifaServicio;

/**
 * Endpoints REST de tarifas: /api/tarifas
 * No hay DELETE: una tarifa se desactiva para no perder el precio de los viajes viejos.
 */
@RestController
@RequestMapping("/api/tarifas")
public class TarifaControlador {

    private final TarifaServicio tarifaServicio;

    public TarifaControlador(TarifaServicio tarifaServicio) {
        this.tarifaServicio = tarifaServicio;
    }

    /** GET /api/tarifas */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<TarifaRespuesta>>> listar() {
        return ResponseEntity.ok(RespuestaApi.de("Tarifas encontradas", tarifaServicio.listarTodas()));
    }

    /** GET /api/tarifas/vigente → la tarifa activa que aplica hoy, o 404 si no hay. */
    @GetMapping("/vigente")
    public ResponseEntity<RespuestaApi<TarifaRespuesta>> buscarVigente() {
        return ResponseEntity.ok(RespuestaApi.de("Tarifa vigente encontrada", tarifaServicio.buscarVigente()));
    }

    /** GET /api/tarifas/1 → la tarifa con ID 1, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<TarifaRespuesta>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Tarifa encontrada", tarifaServicio.buscarPorId(id)));
    }

    /** POST /api/tarifas → 201 con la tarifa creada. */
    @PostMapping
    public ResponseEntity<RespuestaApi<TarifaRespuesta>> crear(@Valid @RequestBody TarifaSolicitud solicitud) {
        TarifaRespuesta creada = tarifaServicio.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/tarifas/" + creada.id()))
                .body(RespuestaApi.de("Tarifa creada con exito", creada));
    }

    /** PUT /api/tarifas/1 → los viajes ya cobrados no cambian. */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<TarifaRespuesta>> modificar(
            @PathVariable Long id, @Valid @RequestBody TarifaSolicitud solicitud) {
        return ResponseEntity.ok(RespuestaApi.de("Tarifa actualizada con exito", tarifaServicio.modificar(id, solicitud)));
    }

    /** PATCH /api/tarifas/1/desactivar */
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<RespuestaApi<TarifaRespuesta>> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Tarifa desactivada", tarifaServicio.desactivar(id)));
    }
}
