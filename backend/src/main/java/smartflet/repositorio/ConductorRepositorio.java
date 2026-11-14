package smartflet.repositorio;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.Conductor;
import smartflet.modelo.EstadoRegistro;

@Repository
public interface ConductorRepositorio extends MongoRepository<Conductor, String> {

    boolean existsByIdentificacion(String identificacion);

    boolean existsByIdentificacionAndIdNot(String identificacion, String id);

    boolean existsByNumeroLicencia(String numeroLicencia);

    boolean existsByNumeroLicenciaAndIdNot(String numeroLicencia, String id);

    List<Conductor> findByEstadoConductor(EstadoRegistro estadoConductor);
}
