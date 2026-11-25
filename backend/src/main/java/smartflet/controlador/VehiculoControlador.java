package smartflet.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import smartflet.dto.EstadoVehiculoSolicitud;
import smartflet.dto.RespuestaApi;
import smartflet.dto.VehiculoRespuesta;
import smartflet.dto.VehiculoSolicitud;
import smartflet.servicio.VehiculoServicio;

/**
 * Endpoints REST de vehiculos: /api/vehiculos
 * Responde siempre { mensaje, datos }. Los errores (404, 400, 409) los resuelve ManejadorGlobalExcepciones.
 */
@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoControlador {

    private final VehiculoServicio vehiculoServicio;

    public VehiculoControlador(VehiculoServicio vehiculoServicio) {
        this.vehiculoServicio = vehiculoServicio;
    }

    /** GET /api/vehiculos  o  GET /api/vehiculos?estadoOperativo=true */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<VehiculoRespuesta>>> listar(
            @RequestParam(required = false) Boolean estadoOperativo) {
        List<VehiculoRespuesta> vehiculos = (estadoOperativo == null)
                ? vehiculoServicio.listarTodos()
                : vehiculoServicio.listarPorEstado(estadoOperativo);
        return ResponseEntity.ok(RespuestaApi.de("Vehiculos encontrados", vehiculos));
    }

    /** GET /api/vehiculos/2 → el vehiculo con ID 2, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<VehiculoRespuesta>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Vehiculo encontrado", vehiculoServicio.buscarPorId(id)));
    }

    /** POST /api/vehiculos → 201 con el vehiculo creado. */
    @PostMapping
    public ResponseEntity<RespuestaApi<VehiculoRespuesta>> crear(@Valid @RequestBody VehiculoSolicitud solicitud) {
        VehiculoRespuesta creado = vehiculoServicio.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/vehiculos/" + creado.id()))
                .body(RespuestaApi.de("Vehiculo creado con exito", creado));
    }

    /** PUT /api/vehiculos/2 → reemplaza los datos del vehiculo 2. */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<VehiculoRespuesta>> actualizar(
            @PathVariable Long id, @Valid @RequestBody VehiculoSolicitud solicitud) {
        return ResponseEntity.ok(RespuestaApi.de("Vehiculo actualizado con exito",
                vehiculoServicio.actualizar(id, solicitud)));
    }

    /** PATCH /api/vehiculos/2/estado  con  { "estadoOperativo": false } (mandarlo al taller). */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<RespuestaApi<VehiculoRespuesta>> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody EstadoVehiculoSolicitud solicitud) {
        return ResponseEntity.ok(RespuestaApi.de("Estado del vehiculo actualizado",
                vehiculoServicio.cambiarEstado(id, solicitud.estadoOperativo())));
    }

    /** DELETE /api/vehiculos/2 → solo si el vehiculo nunca tuvo turnos ni viajes. */
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminar(@PathVariable Long id) {
        vehiculoServicio.eliminar(id);
        return ResponseEntity.ok(RespuestaApi.de("Vehiculo eliminado con exito", null));
    }
}
