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
import smartflet.dto.EstadoRegistroSolicitud;
import smartflet.dto.PasajeroRespuesta;
import smartflet.dto.PasajeroSolicitud;
import smartflet.servicio.PasajeroServicio;

/**
 * Endpoints REST de pasajeros. No hay DELETE: el pasajero se desactiva para conservar su historial.
 * El historial de viajes se consulta en GET /api/viajes?pasajeroId={id}.
 */
@RestController
@RequestMapping("/api/pasajeros")
public class PasajeroControlador {

    private final PasajeroServicio pasajeroServicio;

    public PasajeroControlador(PasajeroServicio pasajeroServicio) {
        this.pasajeroServicio = pasajeroServicio;
    }

    @GetMapping
    public ResponseEntity<List<PasajeroRespuesta>> listar() {
        return ResponseEntity.ok(pasajeroServicio.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasajeroRespuesta> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(pasajeroServicio.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PasajeroRespuesta> registrar(@Valid @RequestBody PasajeroSolicitud solicitud) {
        PasajeroRespuesta creado = pasajeroServicio.registrar(solicitud);
        return ResponseEntity.created(URI.create("/api/pasajeros/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PasajeroRespuesta> modificarDatos(@PathVariable String id,
                                                            @Valid @RequestBody PasajeroSolicitud solicitud) {
        return ResponseEntity.ok(pasajeroServicio.modificarDatos(id, solicitud));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PasajeroRespuesta> cambiarEstado(@PathVariable String id,
                                                           @Valid @RequestBody EstadoRegistroSolicitud solicitud) {
        return ResponseEntity.ok(pasajeroServicio.cambiarEstado(id, solicitud.estado()));
    }
}
