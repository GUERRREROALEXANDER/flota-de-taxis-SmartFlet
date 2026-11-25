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
import smartflet.dto.RespuestaApi;
import smartflet.dto.ViajeRespuesta;
import smartflet.dto.ViajeSolicitud;
import smartflet.modelo.EstadoViaje;
import smartflet.servicio.ViajeServicio;

/**
 * Endpoints REST del despacho de viajes: /api/viajes
 *
 * Ciclo de vida de un viaje:
 *   POST (SOLICITADO) → /asignar (ASIGNADO) → /iniciar (EN_CURSO) → /finalizar (FINALIZADO + pago)
 *   /cancelar se permite mientras no haya iniciado.
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
    public ResponseEntity<RespuestaApi<List<ViajeRespuesta>>> listar(
            @RequestParam(required = false) EstadoViaje estado,
            @RequestParam(required = false) Long pasajeroId) {
        List<ViajeRespuesta> viajes;
        if (pasajeroId != null) {
            viajes = viajeServicio.listarPorPasajero(pasajeroId);
        } else if (estado != null) {
            viajes = viajeServicio.listarPorEstado(estado);
        } else {
            viajes = viajeServicio.listarTodos();
        }
        return ResponseEntity.ok(RespuestaApi.de("Viajes encontrados", viajes));
    }

    /** GET /api/viajes/1 → el viaje con ID 1, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<ViajeRespuesta>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Viaje encontrado", viajeServicio.buscarPorId(id)));
    }

    /** POST /api/viajes  con  { "pasajeroId": 1, "origen": "...", "destino": "..." } */
    @PostMapping
    public ResponseEntity<RespuestaApi<ViajeRespuesta>> solicitar(@Valid @RequestBody ViajeSolicitud solicitud) {
        ViajeRespuesta creado = viajeServicio.solicitar(solicitud);
        return ResponseEntity.created(URI.create("/api/viajes/" + creado.id()))
                .body(RespuestaApi.de("Viaje solicitado con exito", creado));
    }

    /** PATCH /api/viajes/1/asignar  con  { "vehiculoId": 2 } */
    @PatchMapping("/{id}/asignar")
    public ResponseEntity<RespuestaApi<ViajeRespuesta>> asignarVehiculo(
            @PathVariable Long id, @Valid @RequestBody AsignacionVehiculoSolicitud solicitud) {
        return ResponseEntity.ok(RespuestaApi.de("Vehiculo asignado al viaje",
                viajeServicio.asignarVehiculo(id, solicitud.vehiculoId())));
    }

    /** PATCH /api/viajes/1/iniciar */
    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<RespuestaApi<ViajeRespuesta>> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Viaje iniciado", viajeServicio.iniciar(id)));
    }

    /** PATCH /api/viajes/1/finalizar  con  { "distanciaKm": 8.5, "metodoPago": "EFECTIVO" } */
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<RespuestaApi<ViajeRespuesta>> finalizar(
            @PathVariable Long id, @Valid @RequestBody FinalizacionViajeSolicitud solicitud) {
        return ResponseEntity.ok(RespuestaApi.de("Viaje finalizado y pago registrado",
                viajeServicio.finalizar(id, solicitud)));
    }

    /** PATCH /api/viajes/1/cancelar */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<RespuestaApi<ViajeRespuesta>> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Viaje cancelado", viajeServicio.cancelar(id)));
    }
}
