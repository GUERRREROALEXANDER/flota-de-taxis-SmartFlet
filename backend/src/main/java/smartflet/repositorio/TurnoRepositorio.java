package smartflet.repositorio;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import smartflet.modelo.EstadoTurno;
import smartflet.modelo.Turno;

@Repository
public interface TurnoRepositorio extends JpaRepository<Turno, Long> {

    List<Turno> findByConductorIdOrderByFechaInicioDesc(Long conductorId);

    boolean existsByVehiculoIdAndEstado(Long vehiculoId, EstadoTurno estado);

    boolean existsByVehiculoId(Long vehiculoId);

    /** True si el conductor tiene otro turno no finalizado que se cruza con [inicio, fin). */
    @Query("""
            select count(t) > 0 from Turno t
            where t.conductor.id = :conductorId
              and t.estado <> :finalizado
              and t.fechaInicio < :fin and t.fechaFin > :inicio
            """)
    boolean conductorTieneTurnoSolapado(@Param("conductorId") Long conductorId,
                                        @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
                                        @Param("finalizado") EstadoTurno finalizado);

    /** True si el vehiculo tiene otro turno no finalizado que se cruza con [inicio, fin). */
    @Query("""
            select count(t) > 0 from Turno t
            where t.vehiculo.id = :vehiculoId
              and t.estado <> :finalizado
              and t.fechaInicio < :fin and t.fechaFin > :inicio
            """)
    boolean vehiculoTieneTurnoSolapado(@Param("vehiculoId") Long vehiculoId,
                                       @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
                                       @Param("finalizado") EstadoTurno finalizado);
}
