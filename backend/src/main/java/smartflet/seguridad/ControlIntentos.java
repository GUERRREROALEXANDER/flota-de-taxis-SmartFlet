package smartflet.seguridad;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import smartflet.excepcion.DemasiadosIntentosException;

/**
 * Frena los ataques de fuerza bruta: tras N claves incorrectas seguidas para un mismo
 * usuario, ese usuario queda bloqueado unos minutos.
 *
 * Se guarda en memoria (un mapa). Es suficiente para una sola instancia del backend;
 * si se reinicia, los contadores vuelven a cero.
 */
@Component
public class ControlIntentos {

    private record Registro(int fallos, Instant bloqueadoHasta) { }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final int intentosMaximos;
    private final Duration bloqueo;

    public ControlIntentos(@Value("${smartflet.login.intentos-maximos:5}") int intentosMaximos,
                           @Value("${smartflet.login.minutos-bloqueo:5}") int minutosBloqueo) {
        this.intentosMaximos = intentosMaximos;
        this.bloqueo = Duration.ofMinutes(minutosBloqueo);
    }

    /** Lanza DemasiadosIntentosException si el usuario esta bloqueado ahora mismo. */
    public void verificarNoBloqueado(String usuario) {
        Registro r = registros.get(usuario);
        if (r != null && r.bloqueadoHasta() != null) {
            Instant ahora = Instant.now();
            if (ahora.isBefore(r.bloqueadoHasta())) {
                long minutos = Math.max(1, Duration.between(ahora, r.bloqueadoHasta()).toMinutes() + 1);
                throw new DemasiadosIntentosException(minutos);
            }
            registros.remove(usuario); // el bloqueo ya paso: empieza de cero
        }
    }

    /** Suma un fallo y devuelve cuantos intentos le quedan (0 = acaba de quedar bloqueado). */
    public int registrarFallo(String usuario) {
        Registro nuevo = registros.merge(usuario, new Registro(1, null),
                (viejo, uno) -> new Registro(viejo.fallos() + 1, null));
        if (nuevo.fallos() >= intentosMaximos) {
            registros.put(usuario, new Registro(nuevo.fallos(), Instant.now().plus(bloqueo)));
            return 0;
        }
        return intentosMaximos - nuevo.fallos();
    }

    public void reiniciar(String usuario) {
        registros.remove(usuario);
    }
}
