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
import smartflet.dto.RespuestaApi;
import smartflet.modelo.EstadoRegistro;
import smartflet.servicio.ConductorServicio;

/**
 * Endpoints REST de conductores: /api/conductores
 *
 * El controlador solo recibe la peticion, valida el cuerpo (@Valid) y responde con
 * { mensaje, datos }. Las reglas de negocio estan en ConductorServicio y los errores
 * (404, 400, 409) los convierte ManejadorGlobalExcepciones.
 * No hay DELETE: el conductor se desactiva para conservar su historial de turnos.
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
    public ResponseEntity<RespuestaApi<List<ConductorRespuesta>>> listar(
            @RequestParam(required = false) EstadoRegistro estado) {
        List<ConductorRespuesta> conductores = (estado == null)
                ? conductorServicio.listarTodos()
                : conductorServicio.listarPorEstado(estado);
        return ResponseEntity.ok(RespuestaApi.de("Conductores encontrados", conductores));
    }

    /** GET /api/conductores/3 → el conductor con ID 3, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<ConductorRespuesta>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Conductor encontrado", conductorServicio.buscarPorId(id)));
    }

    /** POST /api/conductores → 201 con el conductor creado y su ID nuevo. */
    @PostMapping
    public ResponseEntity<RespuestaApi<ConductorRespuesta>> registrar(
            @Valid @RequestBody ConductorSolicitud solicitud) {
        ConductorRespuesta creado = conductorServicio.registrar(solicitud);
        return ResponseEntity.created(URI.create("/api/conductores/" + creado.id()))
                .body(RespuestaApi.de("Conductor creado con exito", creado));
    }

    /** PUT /api/conductores/3 → reemplaza los datos del conductor 3. */
    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<ConductorRespuesta>> actualizar(
            @PathVariable Long id, @Valid @RequestBody ConductorSolicitud solicitud) {
        return ResponseEntity.ok(RespuestaApi.de("Conductor actualizado con exito",
                conductorServicio.actualizar(id, solicitud)));
    }

    /** PATCH /api/conductores/3/estado  con  { "estado": "INACTIVO" } */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<RespuestaApi<ConductorRespuesta>> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody EstadoRegistroSolicitud solicitud) {
        return ResponseEntity.ok(RespuestaApi.de("Estado del conductor actualizado",
                conductorServicio.cambiarEstado(id, solicitud.estado())));
    }
}
