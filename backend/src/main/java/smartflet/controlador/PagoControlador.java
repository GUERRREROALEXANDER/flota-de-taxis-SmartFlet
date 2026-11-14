package smartflet.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smartflet.dto.PagoRespuesta;
import smartflet.servicio.PagoServicio;

/**
 * Endpoints REST de pagos. No hay POST: el pago lo crea PATCH /api/viajes/{id}/finalizar.
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoControlador {

    private final PagoServicio pagoServicio;

    public PagoControlador(PagoServicio pagoServicio) {
        this.pagoServicio = pagoServicio;
    }

    @GetMapping
    public ResponseEntity<List<PagoRespuesta>> listar() {
        return ResponseEntity.ok(pagoServicio.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoRespuesta> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(pagoServicio.buscarPorId(id));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<PagoRespuesta> anular(@PathVariable String id) {
        return ResponseEntity.ok(pagoServicio.anular(id));
    }
}
