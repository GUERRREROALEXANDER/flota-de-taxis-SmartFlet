package smartflet.excepcion;

/**
 * Se lanza cuando se busca un registro por id y no existe. Se responde con 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        // Ej: "Conductor con ID 3 no encontrado"
        super(recurso + " con ID " + id + " no encontrado");
    }

    /** Para busquedas que no son por id, ej. la tarifa vigente. */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
