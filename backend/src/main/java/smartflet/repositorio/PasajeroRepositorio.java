package smartflet.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.Pasajero;

@Repository
public interface PasajeroRepositorio extends JpaRepository<Pasajero, Long> {

    boolean existsByIdentificacion(String identificacion);

    boolean existsByIdentificacionAndIdNot(String identificacion, Long id);
}
