package smartflet.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato unico de error que devuelve la API.
 * "errores" solo trae contenido cuando falla la validacion de campos.
 */
public record ErrorRespuesta(
        int estado,
        String mensaje,
        Map<String, String> errores,
        LocalDateTime fecha
) {

    public static ErrorRespuesta de(int estado, String mensaje) {
        return new ErrorRespuesta(estado, mensaje, Map.of(), LocalDateTime.now());
    }

    public static ErrorRespuesta deValidacion(int estado, Map<String, String> errores) {
        return new ErrorRespuesta(estado, "Hay campos con datos invalidos", errores, LocalDateTime.now());
    }
}
