package smartflet.excepcion;

/**
 * Se lanza cuando se busca un registro por id y no existe. Se responde con 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " con id " + id + " no existe");
    }

    /** Para busquedas que no son por id, ej. la tarifa vigente. */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
