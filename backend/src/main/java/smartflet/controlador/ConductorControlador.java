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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import smartflet.dto.ConductorRespuesta;
import smartflet.dto.ConductorSolicitud;
import smartflet.dto.EstadoRegistroSolicitud;
import smartflet.modelo.EstadoRegistro;
import smartflet.servicio.ConductorServicio;

/**
 * Endpoints REST de conductores. Sus turnos se consultan en GET /api/turnos?conductorId={id}.
 */
@RestController
@RequestMapping("/api/conductores")
public class ConductorControlador {

    private final ConductorServicio conductorServicio;

    public ConductorControlador(ConductorServicio conductorServicio) {
        this.conductorServicio = conductorServicio;
    }

    /** GET /api/conductores  o  GET /api/conductores?estado=ACTIVO */
    @GetMapping
    public ResponseEntity<List<ConductorRespuesta>> listar(@RequestParam(required = false) EstadoRegistro estado) {
        return ResponseEntity.ok(estado == null
                ? conductorServicio.listarTodos()
                : conductorServicio.listarPorEstado(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConductorRespuesta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(conductorServicio.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ConductorRespuesta> registrar(@Valid @RequestBody ConductorSolicitud solicitud) {
        ConductorRespuesta creado = conductorServicio.registrar(solicitud);
        return ResponseEntity.created(URI.create("/api/conductores/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConductorRespuesta> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody ConductorSolicitud solicitud) {
        return ResponseEntity.ok(conductorServicio.actualizar(id, solicitud));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ConductorRespuesta> cambiarEstado(@PathVariable Long id,
                                                            @Valid @RequestBody EstadoRegistroSolicitud solicitud) {
        return ResponseEntity.ok(conductorServicio.cambiarEstado(id, solicitud.estado()));
    }
}
