package smartflet.repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.Pasajero;

@Repository
public interface PasajeroRepositorio extends MongoRepository<Pasajero, Long> {

    boolean existsByIdentificacion(String identificacion);

    boolean existsByIdentificacionAndIdNot(String identificacion, Long id);
}
