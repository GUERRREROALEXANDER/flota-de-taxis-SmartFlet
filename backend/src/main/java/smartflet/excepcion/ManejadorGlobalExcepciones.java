package smartflet.excepcion;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import smartflet.dto.ErrorRespuesta;

/**
 * Convierte las excepciones de toda la API en respuestas HTTP claras.
 * Asi los controladores no necesitan try/catch.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    /** Login fallido: mismo mensaje si el usuario no existe o si la clave esta mal (no se revela cual). */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorRespuesta> manejarCredenciales(CredencialesInvalidasException ex) {
        String pista = "Revisa que Bloq Mayus este apagado y que el usuario este bien escrito. "
                + "Te quedan " + ex.getIntentosRestantes() + " intento(s) antes del bloqueo temporal.";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorRespuesta.conPista(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), pista));
    }

    @ExceptionHandler(DemasiadosIntentosException.class)
    public ResponseEntity<ErrorRespuesta> manejarDemasiadosIntentos(DemasiadosIntentosException ex) {
        String pista = "Espera " + ex.getMinutosRestantes() + " minuto(s) e intenta de nuevo, "
                + "o pide a un administrador que te asigne una contrasena nueva.";
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorRespuesta.conPista(HttpStatus.TOO_MANY_REQUESTS.value(), ex.getMessage(), pista));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> manejarReglaNegocio(ReglaNegocioException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorRespuesta> manejarDuplicado(RecursoDuplicadoException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Respaldo: el indice unico de MongoDB rechazo un dato repetido (ej. dos altas simultaneas). */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorRespuesta> manejarClaveDuplicada(DuplicateKeyException ex) {
        return responder(HttpStatus.CONFLICT, "Ya existe un registro con ese valor unico");
    }

    /** Falla de @Valid: devuelve cada campo con su mensaje. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ErrorRespuesta.deValidacion(HttpStatus.BAD_REQUEST.value(), errores));
    }

    /** JSON mal escrito o con un tipo incorrecto (ej. texto donde va un numero). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarCuerpoIlegible(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es un JSON valido");
    }

    /** Parametro con un valor del tipo equivocado, ej. /api/viajes?estado=VOLANDO */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST,
                "El parametro '" + ex.getName() + "' tiene un valor invalido: " + ex.getValue());
    }

    /** URL que no corresponde a ningun endpoint. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> manejarRutaInexistente(NoResourceFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, "No existe el endpoint /" + ex.getResourcePath());
    }

    /** Metodo HTTP no soportado, ej. DELETE sobre /api/vehiculos sin id. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "El metodo " + ex.getMethod() + " no esta permitido en esta URL");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarErrorInesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno en el servidor");
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(ErrorRespuesta.de(estado.value(), mensaje));
    }
}
