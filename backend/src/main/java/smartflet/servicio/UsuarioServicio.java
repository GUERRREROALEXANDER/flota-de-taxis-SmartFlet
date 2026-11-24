package smartflet.servicio;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import smartflet.dto.UsuarioRespuesta;
import smartflet.dto.UsuarioSolicitud;
import smartflet.excepcion.RecursoDuplicadoException;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.excepcion.ReglaNegocioException;
import smartflet.modelo.EstadoRegistro;
import smartflet.modelo.Rol;
import smartflet.modelo.Usuario;
import smartflet.repositorio.UsuarioRepositorio;

/** Alta y activacion de cuentas. Solo lo usa un ADMINISTRADOR (ver ConfiguracionSeguridad). */
@Service
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder cifrador;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, PasswordEncoder cifrador) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.cifrador = cifrador;
    }

    public List<UsuarioRespuesta> listarTodos() {
        return usuarioRepositorio.findAll().stream().map(UsuarioRespuesta::desde).toList();
    }

    public UsuarioRespuesta registrar(UsuarioSolicitud s) {
        String nombreUsuario = Usuario.normalizar(s.nombreUsuario());
        if (usuarioRepositorio.existsByNombreUsuario(nombreUsuario)) {
            throw new RecursoDuplicadoException("Ya existe el usuario " + nombreUsuario);
        }
        Usuario nuevo = new Usuario(nombreUsuario, s.nombre().trim(), cifrador.encode(s.clave()), s.rol());
        return UsuarioRespuesta.desde(usuarioRepositorio.save(nuevo));
    }

    /** Un administrador no puede desactivarse a si mismo: se quedaria el sistema sin quien lo administre. */
    public UsuarioRespuesta cambiarEstado(String id, EstadoRegistro estado, String quienLoPide) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario con id " + id + " no existe"));
        if (estado == EstadoRegistro.INACTIVO && usuario.getNombreUsuario().equals(quienLoPide)) {
            throw new ReglaNegocioException("No puedes desactivar tu propia cuenta");
        }
        if (estado == EstadoRegistro.INACTIVO && usuario.getRol() == Rol.ADMINISTRADOR
                && usuarioRepositorio.findAll().stream()
                    .filter(u -> u.getRol() == Rol.ADMINISTRADOR && u.estaActivo()).count() <= 1) {
            throw new ReglaNegocioException("Debe quedar al menos un administrador activo");
        }
        usuario.setEstado(estado);
        return UsuarioRespuesta.desde(usuarioRepositorio.save(usuario));
    }
}
