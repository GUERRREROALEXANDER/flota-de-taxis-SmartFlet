package smartflet.dto;

/**
 * Formato unico de las respuestas exitosas de la API:
 *
 *   { "mensaje": "Conductor encontrado", "datos": { "id": 3, "nombre": "..." } }
 *
 * "mensaje" explica en palabras lo que paso y "datos" trae el registro o la lista.
 */
public record RespuestaApi<T>(
        String mensaje,
        T datos
) {
    public static <T> RespuestaApi<T> de(String mensaje, T datos) {
        return new RespuestaApi<>(mensaje, datos);
    }
}
