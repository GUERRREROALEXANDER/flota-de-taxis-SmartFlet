package smartflet.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.Vehiculo;

@Repository
public interface VehiculoRepositorio extends JpaRepository<Vehiculo, Long> {

    boolean existsByPlaca(String placa);

    /** Para validar placa repetida al actualizar, sin contar el mismo vehiculo. */
    boolean existsByPlacaAndIdNot(String placa, Long id);

    List<Vehiculo> findByEstadoOperativo(Boolean estadoOperativo);
}
