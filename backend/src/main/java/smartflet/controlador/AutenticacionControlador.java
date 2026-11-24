package smartflet.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import smartflet.dto.LoginSolicitud;
import smartflet.dto.SesionRespuesta;
import smartflet.excepcion.CredencialesInvalidasException;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.modelo.Usuario;
import smartflet.repositorio.UsuarioRepositorio;
import smartflet.seguridad.ControlIntentos;

/**
 * Inicio y cierre de sesion.
 *
 *   POST /api/auth/login   {usuario, clave}  → 200 + cookie de sesion | 401 | 429
 *   GET  /api/auth/sesion                    → quien soy (401 si no hay sesion)
 *   POST /api/auth/logout                    → 204, la sesion se destruye
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    private final AuthenticationManager gestorAutenticacion;
    private final SecurityContextRepository repositorioContexto;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ControlIntentos controlIntentos;

    public AutenticacionControlador(AuthenticationManager gestorAutenticacion,
                                    SecurityContextRepository repositorioContexto,
                                    UsuarioRepositorio usuarioRepositorio,
                                    ControlIntentos controlIntentos) {
        this.gestorAutenticacion = gestorAutenticacion;
        this.repositorioContexto = repositorioContexto;
        this.usuarioRepositorio = usuarioRepositorio;
        this.controlIntentos = controlIntentos;
    }

    @PostMapping("/login")
    public ResponseEntity<SesionRespuesta> iniciarSesion(@Valid @RequestBody LoginSolicitud solicitud,
                                                         HttpServletRequest peticion,
                                                         HttpServletResponse respuesta) {
        String usuario = Usuario.normalizar(solicitud.usuario());
        controlIntentos.verificarNoBloqueado(usuario);

        Authentication autenticacion;
        try {
            autenticacion = gestorAutenticacion.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(usuario, solicitud.clave()));
        } catch (AuthenticationException ex) {
            // Usuario inexistente, clave mala o cuenta inactiva: misma respuesta para no dar pistas a un atacante.
            throw new CredencialesInvalidasException(controlIntentos.registrarFallo(usuario));
        }
        controlIntentos.reiniciar(usuario);

        // Sesion nueva en cada login: evita que alguien "fije" un id de sesion de antemano.
        HttpSession anterior = peticion.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        peticion.getSession(true);

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);
        repositorioContexto.saveContext(contexto, peticion, respuesta);

        return ResponseEntity.ok(sesionDe(usuario));
    }

    @GetMapping("/sesion")
    public ResponseEntity<SesionRespuesta> sesionActual(Authentication autenticacion) {
        return ResponseEntity.ok(sesionDe(autenticacion.getName()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    private SesionRespuesta sesionDe(String nombreUsuario) {
        return usuarioRepositorio.findByNombreUsuario(nombreUsuario)
                .map(SesionRespuesta::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario " + nombreUsuario + " ya no existe"));
    }
}
