package smartflet.repositorio;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.EstadoViaje;
import smartflet.modelo.Viaje;

@Repository
public interface ViajeRepositorio extends MongoRepository<Viaje, String> {

    List<Viaje> findByEstadoViaje(EstadoViaje estadoViaje);

    List<Viaje> findByPasajeroIdOrderByFechaSolicitudDesc(String pasajeroId);

    boolean existsByVehiculoId(String vehiculoId);

    boolean existsByVehiculoIdAndEstadoViajeIn(String vehiculoId, Collection<EstadoViaje> estados);

    /** Para saber si el vehiculo ya atiende OTRO viaje activo. */
    boolean existsByVehiculoIdAndEstadoViajeInAndIdNot(String vehiculoId, Collection<EstadoViaje> estados, String id);

    /** Viajes que ya tienen pago (el pago vive embebido en el viaje). */
    List<Viaje> findByPagoIsNotNull();

    /** El viaje que contiene el pago con ese id. */
    Optional<Viaje> findByPagoId(String pagoId);
}
