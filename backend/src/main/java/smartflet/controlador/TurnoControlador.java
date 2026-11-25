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
import smartflet.dto.RespuestaApi;
import smartflet.dto.TurnoRespuesta;
import smartflet.dto.TurnoSolicitud;
import smartflet.servicio.TurnoServicio;

/**
 * Endpoints REST de turnos: /api/turnos
 * Un turno dice que conductor maneja que vehiculo y en que horario.
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
    public ResponseEntity<RespuestaApi<List<TurnoRespuesta>>> listar(@RequestParam(required = false) Long conductorId) {
        List<TurnoRespuesta> turnos = (conductorId == null)
                ? turnoServicio.listarTodos()
                : turnoServicio.listarPorConductor(conductorId);
        return ResponseEntity.ok(RespuestaApi.de("Turnos encontrados", turnos));
    }

    /** GET /api/turnos/1 → el turno con ID 1, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<TurnoRespuesta>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Turno encontrado", turnoServicio.buscarPorId(id)));
    }

    /** POST /api/turnos → 201 con el turno programado. */
    @PostMapping
    public ResponseEntity<RespuestaApi<TurnoRespuesta>> programar(@Valid @RequestBody TurnoSolicitud solicitud) {
        TurnoRespuesta creado = turnoServicio.programar(solicitud);
        return ResponseEntity.created(URI.create("/api/turnos/" + creado.id()))
                .body(RespuestaApi.de("Turno programado con exito", creado));
    }

    /** PATCH /api/turnos/1/iniciar → PROGRAMADO pasa a EN_CURSO. */
    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<RespuestaApi<TurnoRespuesta>> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Turno iniciado", turnoServicio.iniciar(id)));
    }

    /** PATCH /api/turnos/1/finalizar → EN_CURSO pasa a FINALIZADO. */
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<RespuestaApi<TurnoRespuesta>> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Turno finalizado", turnoServicio.finalizar(id)));
    }
}
