package smartflet.excepcion;

/** Demasiados intentos fallidos seguidos para el mismo usuario. Se responde con 429. */
public class DemasiadosIntentosException extends RuntimeException {

    private final long minutosRestantes;

    public DemasiadosIntentosException(long minutosRestantes) {
        super("Demasiados intentos fallidos. La cuenta quedo bloqueada temporalmente");
        this.minutosRestantes = minutosRestantes;
    }

    public long getMinutosRestantes() {
        return minutosRestantes;
    }
}
