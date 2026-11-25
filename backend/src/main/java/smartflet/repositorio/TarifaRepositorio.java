package smartflet.repositorio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import smartflet.modelo.EstadoRegistro;
import smartflet.modelo.Tarifa;

@Repository
public interface TarifaRepositorio extends MongoRepository<Tarifa, Long> {

    /** Tarifas con el estado dado cuya vigencia incluye la fecha (fin null = sin fecha de fin). */
    @Query("""
            { 'estado': ?0,
              'fechaInicioVigencia': { $lte: ?1 },
              $or: [ { 'fechaFinVigencia': null }, { 'fechaFinVigencia': { $gte: ?1 } } ] }
            """)
    List<Tarifa> buscarVigentes(EstadoRegistro estado, LocalDate fecha);

    /** True si otra tarifa con el estado dado se cruza con el periodo [inicio, fin]. */
    @Query(value = """
            { 'estado': ?0,
              '_id': { $ne: ?3 },
              'fechaInicioVigencia': { $lte: ?2 },
              $or: [ { 'fechaFinVigencia': null }, { 'fechaFinVigencia': { $gte: ?1 } } ] }
            """, exists = true)
    boolean existeSolapamiento(EstadoRegistro estado, LocalDate inicio, LocalDate fin, Long idExcluido);
}
