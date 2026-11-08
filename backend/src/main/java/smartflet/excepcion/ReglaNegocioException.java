package smartflet.excepcion;

/**
 * Se lanza cuando los datos son validos en formato pero rompen una regla del negocio
 * (placa repetida, origen igual al destino, etc.). Se responde con 400.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
