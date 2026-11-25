package smartflet.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Guarda el ultimo ID numerico que se uso en cada coleccion.
 *
 * MongoDB no tiene autoincremento como MySQL o PostgreSQL, asi que lo simulamos con una
 * coleccion "contadores" que tiene un documento por coleccion:
 *
 *   { "_id": "conductores", "secuencia": 3 }   → el proximo conductor sera el ID 4
 *   { "_id": "vehiculos",   "secuencia": 5 }   → el proximo vehiculo sera el ID 6
 */
@Document(collection = "contadores")
public class Contador {

    /** Nombre de la coleccion a la que pertenece este contador (ej. "conductores"). */
    @Id
    private String id;

    /** Ultimo numero entregado. */
    private long secuencia;

    public String getId() {
        return id;
    }

    public long getSecuencia() {
        return secuencia;
    }
}
