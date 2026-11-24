package smartflet.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato unico de error que devuelve la API.
 * "errores" solo trae contenido cuando falla la validacion de campos.
 * "pista" (opcional) le dice al usuario que revisar para corregir el problema;
 * si no hay pista, el campo no aparece en el JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRespuesta(
        int estado,
        String mensaje,
        Map<String, String> errores,
        LocalDateTime fecha,
        String pista
) {

    public static ErrorRespuesta de(int estado, String mensaje) {
        return new ErrorRespuesta(estado, mensaje, Map.of(), LocalDateTime.now(), null);
    }

    public static ErrorRespuesta conPista(int estado, String mensaje, String pista) {
        return new ErrorRespuesta(estado, mensaje, Map.of(), LocalDateTime.now(), pista);
    }

    public static ErrorRespuesta deValidacion(int estado, Map<String, String> errores) {
        return new ErrorRespuesta(estado, "Hay campos con datos invalidos", errores, LocalDateTime.now(), null);
    }
}
