package smartflet.seguridad;

import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import smartflet.modelo.Rol;
import smartflet.modelo.Usuario;
import smartflet.repositorio.UsuarioRepositorio;

/**
 * Resuelve el problema del huevo y la gallina: para crear usuarios hay que ser administrador,
 * pero al principio no hay ninguno. Al arrancar, si NO existe ningun ADMINISTRADOR, crea uno
 * con ADMIN_USUARIO / ADMIN_CLAVE. Si ya existe, no hace nada (no pisa la clave).
 */
@Component
public class CreadorAdministradorInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CreadorAdministradorInicial.class);

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder cifrador;
    private final String usuario;
    private final String nombre;
    private final String clave;

    public CreadorAdministradorInicial(UsuarioRepositorio usuarioRepositorio, PasswordEncoder cifrador,
                                       @Value("${smartflet.admin.usuario}") String usuario,
                                       @Value("${smartflet.admin.nombre}") String nombre,
                                       @Value("${smartflet.admin.clave}") String clave) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.cifrador = cifrador;
        this.usuario = usuario;
        this.nombre = nombre;
        this.clave = clave;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepositorio.existsByRol(Rol.ADMINISTRADOR)) {
            return;
        }
        String claveFinal = clave;
        if (claveFinal == null || claveFinal.isBlank()) {
            claveFinal = claveAleatoria();
            log.warn("""

                    ============================================================
                     No se definio ADMIN_CLAVE. Se creo el administrador con:
                       usuario: {}
                       clave:   {}
                     Cambiala definiendo ADMIN_CLAVE antes del primer arranque.
                    ============================================================""", usuario, claveFinal);
        }
        usuarioRepositorio.save(new Usuario(usuario, nombre, cifrador.encode(claveFinal), Rol.ADMINISTRADOR));
        log.info("Administrador inicial '{}' creado", Usuario.normalizar(usuario));
    }

    private static String claveAleatoria() {
        byte[] bytes = new byte[12];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
