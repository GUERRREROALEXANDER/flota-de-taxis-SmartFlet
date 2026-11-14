package smartflet.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import smartflet.modelo.Pago;

@Repository
public interface PagoRepositorio extends JpaRepository<Pago, Long> {
}
