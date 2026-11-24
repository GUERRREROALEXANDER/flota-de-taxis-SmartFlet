package smartflet.dto;

import java.time.LocalDate;

import smartflet.modelo.EstadoRegistro;
import smartflet.modelo.Rol;
import smartflet.modelo.Usuario;

/** Usuario sin la contrasena: el hash nunca sale del backend. */
public record UsuarioRespuesta(
        String id,
        String nombreUsuario,
        String nombre,
        Rol rol,
        EstadoRegistro estado,
        LocalDate fechaRegistro
) {

    public static UsuarioRespuesta desde(Usuario u) {
        return new UsuarioRespuesta(u.getId(), u.getNombreUsuario(), u.getNombre(), u.getRol(),
                u.getEstado(), u.getFechaRegistro());
    }
}
