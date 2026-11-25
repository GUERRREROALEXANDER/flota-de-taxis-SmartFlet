package smartflet.repositorio;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.Conductor;
import smartflet.modelo.EstadoRegistro;

@Repository
public interface ConductorRepositorio extends MongoRepository<Conductor, Long> {

    boolean existsByIdentificacion(String identificacion);

    boolean existsByIdentificacionAndIdNot(String identificacion, Long id);

    boolean existsByNumeroLicencia(String numeroLicencia);

    boolean existsByNumeroLicenciaAndIdNot(String numeroLicencia, Long id);

    List<Conductor> findByEstadoConductor(EstadoRegistro estadoConductor);
}
