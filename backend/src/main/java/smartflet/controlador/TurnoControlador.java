package smartflet.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import smartflet.dto.TurnoRespuesta;
import smartflet.dto.TurnoSolicitud;
import smartflet.servicio.TurnoServicio;

/**
 * Endpoints REST de turnos.
 */
@RestController
@RequestMapping("/api/turnos")
public class TurnoControlador {

    private final TurnoServicio turnoServicio;

    public TurnoControlador(TurnoServicio turnoServicio) {
        this.turnoServicio = turnoServicio;
    }

    /** GET /api/turnos  o  GET /api/turnos?conductorId=1 */
    @GetMapping
    public ResponseEntity<List<TurnoRespuesta>> listar(@RequestParam(required = false) String conductorId) {
        return ResponseEntity.ok(conductorId == null
                ? turnoServicio.listarTodos()
                : turnoServicio.listarPorConductor(conductorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoRespuesta> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(turnoServicio.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TurnoRespuesta> programar(@Valid @RequestBody TurnoSolicitud solicitud) {
        TurnoRespuesta creado = turnoServicio.programar(solicitud);
        return ResponseEntity.created(URI.create("/api/turnos/" + creado.id())).body(creado);
    }

    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<TurnoRespuesta> iniciar(@PathVariable String id) {
        return ResponseEntity.ok(turnoServicio.iniciar(id));
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<TurnoRespuesta> finalizar(@PathVariable String id) {
        return ResponseEntity.ok(turnoServicio.finalizar(id));
    }
}
