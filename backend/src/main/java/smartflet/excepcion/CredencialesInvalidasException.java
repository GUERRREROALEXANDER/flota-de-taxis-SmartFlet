package smartflet.excepcion;

/** Usuario o contrasena incorrectos. Se responde con 401 y una pista. */
public class CredencialesInvalidasException extends RuntimeException {

    private final int intentosRestantes;

    public CredencialesInvalidasException(int intentosRestantes) {
        super("Usuario o contrasena incorrectos");
        this.intentosRestantes = intentosRestantes;
    }

    public int getIntentosRestantes() {
        return intentosRestantes;
    }
}
