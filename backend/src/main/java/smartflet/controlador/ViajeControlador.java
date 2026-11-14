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
import smartflet.dto.AsignacionVehiculoSolicitud;
import smartflet.dto.FinalizacionViajeSolicitud;
import smartflet.dto.ViajeRespuesta;
import smartflet.dto.ViajeSolicitud;
import smartflet.modelo.EstadoViaje;
import smartflet.servicio.ViajeServicio;

/**
 * Endpoints REST del despacho de viajes.
 */
@RestController
@RequestMapping("/api/viajes")
public class ViajeControlador {

    private final ViajeServicio viajeServicio;

    public ViajeControlador(ViajeServicio viajeServicio) {
        this.viajeServicio = viajeServicio;
    }

    /** GET /api/viajes, ?estado=SOLICITADO o ?pasajeroId=1 (historial del pasajero). */
    @GetMapping
    public ResponseEntity<List<ViajeRespuesta>> listar(@RequestParam(required = false) EstadoViaje estado,
                                                       @RequestParam(required = false) Long pasajeroId) {
        if (pasajeroId != null) {
            return ResponseEntity.ok(viajeServicio.listarPorPasajero(pasajeroId));
        }
        return ResponseEntity.ok(estado == null
                ? viajeServicio.listarTodos()
                : viajeServicio.listarPorEstado(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViajeRespuesta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(viajeServicio.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ViajeRespuesta> solicitar(@Valid @RequestBody ViajeSolicitud solicitud) {
        ViajeRespuesta creado = viajeServicio.solicitar(solicitud);
        return ResponseEntity.created(URI.create("/api/viajes/" + creado.id())).body(creado);
    }

    @PatchMapping("/{id}/asignar")
    public ResponseEntity<ViajeRespuesta> asignarVehiculo(@PathVariable Long id,
                                                          @Valid @RequestBody AsignacionVehiculoSolicitud solicitud) {
        return ResponseEntity.ok(viajeServicio.asignarVehiculo(id, solicitud.vehiculoId()));
    }

    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<ViajeRespuesta> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(viajeServicio.iniciar(id));
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<ViajeRespuesta> finalizar(@PathVariable Long id,
                                                    @Valid @RequestBody FinalizacionViajeSolicitud solicitud) {
        return ResponseEntity.ok(viajeServicio.finalizar(id, solicitud));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ViajeRespuesta> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(viajeServicio.cancelar(id));
    }
}
