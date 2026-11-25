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
import smartflet.dto.RespuestaApi;
import smartflet.servicio.PasajeroServicio;

/**
 * Endpoints REST de pasajeros: /api/pasajeros
 * No hay DELETE: el pasajero se desactiva para conservar su historial.
 * Su historial de viajes se consulta en GET /api/viajes?pasajeroId={id}.
 */
@RestController
@RequestMapping("/api/pasajeros")
public class PasajeroControlador {

    private final PasajeroServicio pasajeroServicio;

    public PasajeroControlador(PasajeroServicio pasajeroServicio) {
        this.pasajeroServicio = pasajeroServicio;
    }

    /** GET /api/pasajeros */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<PasajeroRespuesta>>> listar() {
        return ResponseEntity.ok(RespuestaApi.de("Pasajeros encontrados", pasajeroServicio.listarTodos()));
    }

    /** GET /api/pasajeros/1 → el pasajero con ID 1, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<PasajeroRespuesta>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Pasajero encontrado", pasajeroServicio.buscarPorId(id)));
    }

    /** POST /api/pasajeros → 201 con el pasajero creado. */
    @PostMapping
    public ResponseEntity<RespuestaApi<PasajeroRespuesta>> registrar(@Valid @RequestBody PasajeroSolicitud solicitud) {
        PasajeroRespuesta creado = pasajeroServicio.registrar(solicitud);
        return ResponseEntity.created(URI.create("/api/pasajeros/" + creado.id()))
                .body(RespuestaApi.de("Pasajero creado con exito", creado));
    }

    /** PUT /api/pasajeros/1 → reemplaza los datos del pasajero 1. */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<PasajeroRespuesta>> modificarDatos(
            @PathVariable Long id, @Valid @RequestBody PasajeroSolicitud solicitud) {
        return ResponseEntity.ok(RespuestaApi.de("Pasajero actualizado con exito",
                pasajeroServicio.modificarDatos(id, solicitud)));
    }

    /** PATCH /api/pasajeros/1/estado  con  { "estado": "INACTIVO" } */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<RespuestaApi<PasajeroRespuesta>> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody EstadoRegistroSolicitud solicitud) {
        return ResponseEntity.ok(RespuestaApi.de("Estado del pasajero actualizado",
                pasajeroServicio.cambiarEstado(id, solicitud.estado())));
    }
}
