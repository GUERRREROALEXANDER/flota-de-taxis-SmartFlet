package smartflet.excepcion;

/**
 * Se lanza cuando el dato choca con uno que ya existe (placa, identificacion o licencia repetida,
 * ruta repetida, vigencias o turnos que se cruzan). Se responde con 409 Conflict.
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}