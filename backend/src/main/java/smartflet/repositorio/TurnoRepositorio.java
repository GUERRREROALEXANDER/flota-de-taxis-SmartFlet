package smartflet.repositorio;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.EstadoTurno;
import smartflet.modelo.Turno;

@Repository
public interface TurnoRepositorio extends MongoRepository<Turno, String> {

    List<Turno> findByConductorIdOrderByFechaInicioDesc(String conductorId);

    boolean existsByVehiculoIdAndEstado(String vehiculoId, EstadoTurno estado);

    boolean existsByVehiculoId(String vehiculoId);

    /** Consulta derivada: otro turno del conductor, no finalizado, con inicio < fin y fin > inicio. */
    boolean existsByConductorIdAndEstadoNotAndFechaInicioLessThanAndFechaFinGreaterThan(
            String conductorId, EstadoTurno estado, LocalDateTime fin, LocalDateTime inicio);

    /** Consulta derivada: otro turno del vehiculo, no finalizado, con inicio < fin y fin > inicio. */
    boolean existsByVehiculoIdAndEstadoNotAndFechaInicioLessThanAndFechaFinGreaterThan(
            String vehiculoId, EstadoTurno estado, LocalDateTime fin, LocalDateTime inicio);

    /** True si el conductor tiene otro turno no finalizado que se cruza con [inicio, fin). */
    default boolean conductorTieneTurnoSolapado(String conductorId, LocalDateTime inicio, LocalDateTime fin) {
        return existsByConductorIdAndEstadoNotAndFechaInicioLessThanAndFechaFinGreaterThan(
                conductorId, EstadoTurno.FINALIZADO, fin, inicio);
    }

    /** True si el vehiculo tiene otro turno no finalizado que se cruza con [inicio, fin). */
    default boolean vehiculoTieneTurnoSolapado(String vehiculoId, LocalDateTime inicio, LocalDateTime fin) {
        return existsByVehiculoIdAndEstadoNotAndFechaInicioLessThanAndFechaFinGreaterThan(
                vehiculoId, EstadoTurno.FINALIZADO, fin, inicio);
    }
}
