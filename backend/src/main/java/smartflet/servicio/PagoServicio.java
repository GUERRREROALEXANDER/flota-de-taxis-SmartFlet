package smartflet.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import smartflet.dto.PagoRespuesta;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.modelo.Viaje;
import smartflet.repositorio.ViajeRepositorio;

/**
 * Consulta y anulacion de pagos. El pago vive embebido en su Viaje, asi que se lee y se guarda
 * a traves de ViajeRepositorio. Los pagos no se crean aqui: los crea el viaje al finalizar.
 */
@Service
public class PagoServicio {

    private final ViajeRepositorio viajeRepositorio;

    public PagoServicio(ViajeRepositorio viajeRepositorio) {
        this.viajeRepositorio = viajeRepositorio;
    }

    public List<PagoRespuesta> listarTodos() {
        return viajeRepositorio.findByPagoIsNotNull().stream()
                .map(viaje -> PagoRespuesta.desde(viaje.getPago(), viaje.getId()))
                .toList();
    }

    public PagoRespuesta buscarPorId(Long id) {
        Viaje viaje = obtenerViajeDelPago(id);
        return PagoRespuesta.desde(viaje.getPago(), viaje.getId());
    }

    public PagoRespuesta anular(Long id) {
        Viaje viaje = obtenerViajeDelPago(id);
        viaje.getPago().anularPago();
        Viaje guardado = viajeRepositorio.save(viaje);
        return PagoRespuesta.desde(guardado.getPago(), guardado.getId());
    }

    private Viaje obtenerViajeDelPago(Long pagoId) {
        return viajeRepositorio.findByPagoId(pagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", pagoId));
    }
}