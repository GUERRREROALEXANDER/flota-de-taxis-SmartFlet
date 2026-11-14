package smartflet.repositorio;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.EstadoViaje;
import smartflet.modelo.Viaje;

@Repository
public interface ViajeRepositorio extends JpaRepository<Viaje, Long> {

    List<Viaje> findByEstadoViaje(EstadoViaje estadoViaje);

    List<Viaje> findByPasajeroIdOrderByFechaSolicitudDesc(Long pasajeroId);

    boolean existsByVehiculoId(Long vehiculoId);

    boolean existsByVehiculoIdAndEstadoViajeIn(Long vehiculoId, Collection<EstadoViaje> estados);

    /** Para saber si el vehiculo ya atiende OTRO viaje activo. */
    boolean existsByVehiculoIdAndEstadoViajeInAndIdNot(Long vehiculoId, Collection<EstadoViaje> estados, Long id);
}
