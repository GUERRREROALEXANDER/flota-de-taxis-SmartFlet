package smartflet.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smartflet.dto.PagoRespuesta;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.modelo.Pago;
import smartflet.repositorio.PagoRepositorio;

/**
 * Consulta y anulacion de pagos. Los pagos no se crean aqui: los crea el viaje al finalizar.
 */
@Service
public class PagoServicio {

    private final PagoRepositorio pagoRepositorio;

    public PagoServicio(PagoRepositorio pagoRepositorio) {
        this.pagoRepositorio = pagoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<PagoRespuesta> listarTodos() {
        return pagoRepositorio.findAll().stream()
                .map(PagoRespuesta::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagoRespuesta buscarPorId(Long id) {
        return PagoRespuesta.desde(obtenerPago(id));
    }

    @Transactional
    public PagoRespuesta anular(Long id) {
        Pago pago = obtenerPago(id);
        pago.anularPago();
        return PagoRespuesta.desde(pagoRepositorio.save(pago));
    }

    private Pago obtenerPago(Long id) {
        return pagoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }
}
