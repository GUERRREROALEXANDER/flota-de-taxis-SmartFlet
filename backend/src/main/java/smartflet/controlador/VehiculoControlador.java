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
import smartflet.dto.VehiculoRespuesta;
import smartflet.dto.VehiculoSolicitud;
import smartflet.servicio.VehiculoServicio;

/**
 * Endpoints REST de vehiculos. Los errores (404, 400) los resuelve ManejadorGlobalExcepciones.
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
    public ResponseEntity<List<VehiculoRespuesta>> listar(
            @RequestParam(required = false) Boolean estadoOperativo) {
        List<VehiculoRespuesta> vehiculos = (estadoOperativo == null)
                ? vehiculoServicio.listarTodos()
                : vehiculoServicio.listarPorEstado(estadoOperativo);
        return ResponseEntity.ok(vehiculos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoRespuesta> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(vehiculoServicio.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<VehiculoRespuesta> crear(@Valid @RequestBody VehiculoSolicitud solicitud) {
        VehiculoRespuesta creado = vehiculoServicio.crear(solicitud);
        URI ubicacion = URI.create("/api/vehiculos/" + creado.id());
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoRespuesta> actualizar(@PathVariable String id,
                                                        @Valid @RequestBody VehiculoSolicitud solicitud) {
        return ResponseEntity.ok(vehiculoServicio.actualizar(id, solicitud));
    }

    /** Cambia solo el estado operativo (actualizarEstado() del modelo conceptual). */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<VehiculoRespuesta> cambiarEstado(@PathVariable String id,
                                                           @Valid @RequestBody EstadoVehiculoSolicitud solicitud) {
        return ResponseEntity.ok(vehiculoServicio.cambiarEstado(id, solicitud.estadoOperativo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        vehiculoServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
