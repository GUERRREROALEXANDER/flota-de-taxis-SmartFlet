package smartflet.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.Ruta;

@Repository
public interface RutaRepositorio extends JpaRepository<Ruta, Long> {

    boolean existsByOrigenIgnoreCaseAndDestinoIgnoreCase(String origen, String destino);

    /** Para validar ruta repetida al actualizar, sin contar la misma ruta. */
    boolean existsByOrigenIgnoreCaseAndDestinoIgnoreCaseAndIdNot(String origen, String destino, Long id);

    List<Ruta> findByOrigenContainingIgnoreCase(String origen);
}
