package smartflet.excepcion;

/**
 * Se lanza cuando los datos son validos en formato pero rompen una regla del negocio
 * (origen igual al destino, licencia vencida, transicion de estado invalida, etc.). Se responde con 400.
 * Los datos repetidos usan RecursoDuplicadoException (409).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
