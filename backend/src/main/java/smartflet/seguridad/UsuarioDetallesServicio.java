package smartflet.seguridad;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import smartflet.modelo.Usuario;
import smartflet.repositorio.UsuarioRepositorio;

/**
 * Puente entre nuestra coleccion "usuarios" y Spring Security:
 * convierte un Usuario de MongoDB en el UserDetails que Spring sabe verificar.
 */
@Service
public class UsuarioDetallesServicio implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioDetallesServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) {
        Usuario usuario = usuarioRepositorio.findByNombreUsuario(Usuario.normalizar(nombreUsuario))
                .orElseThrow(() -> new UsernameNotFoundException(nombreUsuario));

        return User.withUsername(usuario.getNombreUsuario())
                .password(usuario.getClaveCifrada())
                .roles(usuario.getRol().name())   // queda como ROLE_ADMINISTRADOR / ROLE_OPERADOR
                .disabled(!usuario.estaActivo())   // un usuario INACTIVO no puede entrar
                .build();
    }
}
