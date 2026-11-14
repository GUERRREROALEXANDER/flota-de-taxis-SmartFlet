package smartflet.repositorio;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.Ruta;

@Repository
public interface RutaRepositorio extends MongoRepository<Ruta, String> {

    boolean existsByOrigenIgnoreCaseAndDestinoIgnoreCase(String origen, String destino);

    /** Para validar ruta repetida al actualizar, sin contar la misma ruta. */
    boolean existsByOrigenIgnoreCaseAndDestinoIgnoreCaseAndIdNot(String origen, String destino, String id);

    List<Ruta> findByOrigenContainingIgnoreCase(String origen);
}
