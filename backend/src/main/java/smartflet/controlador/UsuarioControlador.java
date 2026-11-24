package smartflet.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import smartflet.dto.EstadoRegistroSolicitud;
import smartflet.dto.UsuarioRespuesta;
import smartflet.dto.UsuarioSolicitud;
import smartflet.servicio.UsuarioServicio;

/** Cuentas de acceso. Todo /api/usuarios es solo para ADMINISTRADOR. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioRespuesta>> listar() {
        return ResponseEntity.ok(usuarioServicio.listarTodos());
    }

    @PostMapping
    public ResponseEntity<UsuarioRespuesta> registrar(@Valid @RequestBody UsuarioSolicitud solicitud) {
        UsuarioRespuesta creado = usuarioServicio.registrar(solicitud);
        return ResponseEntity.created(URI.create("/api/usuarios/" + creado.id())).body(creado);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioRespuesta> cambiarEstado(@PathVariable String id,
                                                          @Valid @RequestBody EstadoRegistroSolicitud solicitud,
                                                          Authentication quien) {
        return ResponseEntity.ok(usuarioServicio.cambiarEstado(id, solicitud.estado(), quien.getName()));
    }
}
