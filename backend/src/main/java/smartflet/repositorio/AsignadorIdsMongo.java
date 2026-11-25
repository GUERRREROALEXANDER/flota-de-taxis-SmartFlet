package smartflet.repositorio;

import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import smartflet.modelo.EntidadConId;
import smartflet.modelo.Viaje;

/**
 * Pone el ID numerico automaticamente antes de guardar en MongoDB.
 *
 * Hace lo mismo que @GeneratedValue en JPA: el servicio llama repositorio.save(conductor)
 * con id = null, Spring Data llama a este metodo justo antes de guardar, y aqui se le
 * asigna el siguiente numero. Si la entidad ya tiene ID (una actualizacion), no se toca.
 */
@Component
public class AsignadorIdsMongo implements BeforeConvertCallback<Object> {

    private final GeneradorIds generadorIds;

    public AsignadorIdsMongo(GeneradorIds generadorIds) {
        this.generadorIds = generadorIds;
    }

    @Override
    public Object onBeforeConvert(Object entidad, String coleccion) {
        if (entidad instanceof EntidadConId conId && conId.getId() == null) {
            conId.setId(generadorIds.siguienteId(coleccion));
        }
        // El pago no es un documento propio (vive dentro del viaje), pero tambien
        // necesita su propio numero para consultarlo en /api/pagos/{id}.
        if (entidad instanceof Viaje viaje && viaje.getPago() != null && viaje.getPago().getId() == null) {
            viaje.getPago().setId(generadorIds.siguienteId("pagos"));
        }
        return entidad;
    }
}
