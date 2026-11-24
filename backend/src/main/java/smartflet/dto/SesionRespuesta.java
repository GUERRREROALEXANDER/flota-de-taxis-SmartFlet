package smartflet.dto;

import smartflet.modelo.Rol;
import smartflet.modelo.Usuario;

/** Quien tiene la sesion abierta. Lo usa el frontend para saludar y decidir que menus mostrar. */
public record SesionRespuesta(String usuario, String nombre, Rol rol) {

    public static SesionRespuesta desde(Usuario u) {
        return new SesionRespuesta(u.getNombreUsuario(), u.getNombre(), u.getRol());
    }
}
