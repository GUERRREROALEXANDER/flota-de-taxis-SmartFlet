package smartflet.repositorio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import smartflet.modelo.EstadoRegistro;
import smartflet.modelo.Tarifa;

@Repository
public interface TarifaRepositorio extends JpaRepository<Tarifa, Long> {

    /** Tarifas con el estado dado cuya vigencia incluye la fecha (fin null = sin fecha de fin). */
    @Query("""
            select t from Tarifa t
            where t.estado = :estado
              and t.fechaInicioVigencia <= :fecha
              and (t.fechaFinVigencia is null or t.fechaFinVigencia >= :fecha)
            """)
    List<Tarifa> buscarVigentes(@Param("estado") EstadoRegistro estado, @Param("fecha") LocalDate fecha);

    /** True si otra tarifa con el estado dado se cruza con el periodo [inicio, fin]. */
    @Query("""
            select count(t) > 0 from Tarifa t
            where t.estado = :estado
              and t.id <> :idExcluido
              and t.fechaInicioVigencia <= :fin
              and (t.fechaFinVigencia is null or t.fechaFinVigencia >= :inicio)
            """)
    boolean existeSolapamiento(@Param("estado") EstadoRegistro estado, @Param("inicio") LocalDate inicio,
                               @Param("fin") LocalDate fin, @Param("idExcluido") Long idExcluido);
}
