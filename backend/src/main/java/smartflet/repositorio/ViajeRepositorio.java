package smartflet.repositorio;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.EstadoViaje;
import smartflet.modelo.Viaje;

@Repository
public interface ViajeRepositorio extends MongoRepository<Viaje, Long> {

    List<Viaje> findByEstadoViaje(EstadoViaje estadoViaje);

    List<Viaje> findByPasajeroIdOrderByFechaSolicitudDesc(Long pasajeroId);

    boolean existsByVehiculoId(Long vehiculoId);

    boolean existsByVehiculoIdAndEstadoViajeIn(Long vehiculoId, Collection<EstadoViaje> estados);

    /** Para saber si el vehiculo ya atiende OTRO viaje activo. */
    boolean existsByVehiculoIdAndEstadoViajeInAndIdNot(Long vehiculoId, Collection<EstadoViaje> estados, Long id);

    /** Viajes que ya tienen pago (el pago vive embebido en el viaje). */
    List<Viaje> findByPagoIsNotNull();

    /** El viaje que contiene el pago con ese id. */
    Optional<Viaje> findByPagoId(Long pagoId);
}
