package smartflet.repositorio;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import smartflet.modelo.Contador;

/**
 * Entrega el siguiente ID numerico de una coleccion: 1, 2, 3...
 *
 * Patron GRASP: Fabricacion pura. No es un concepto del negocio de taxis; es una clase
 * inventada para que el modelo no tenga que saber como se numeran los registros.
 */
@Component
public class GeneradorIds {

    private final MongoOperations mongo;

    /**
     * @Lazy rompe un ciclo: MongoTemplate necesita a AsignadorIdsMongo (que usa esta clase)
     * y esta clase necesita a MongoTemplate. Con @Lazy, Mongo se pide recien al primer uso.
     */
    public GeneradorIds(@Lazy MongoOperations mongo) {
        this.mongo = mongo;
    }

    /**
     * Suma 1 al contador de la coleccion y devuelve el nuevo valor.
     * findAndModify lo hace en UNA sola operacion atomica: si dos peticiones llegan al mismo
     * tiempo, cada una recibe un numero distinto. upsert(true) crea el contador la primera vez.
     */
    public long siguienteId(String coleccion) {
        Contador contador = mongo.findAndModify(
                query(where("_id").is(coleccion)),
                new Update().inc("secuencia", 1),
                options().returnNew(true).upsert(true),
                Contador.class);
        return contador.getSecuencia();
    }
}
