package smartflet.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smartflet.dto.PagoRespuesta;
import smartflet.dto.RespuestaApi;
import smartflet.servicio.PagoServicio;

/**
 * Endpoints REST de pagos: /api/pagos
 * No hay POST: el pago lo crea PATCH /api/viajes/{id}/finalizar.
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoControlador {

    private final PagoServicio pagoServicio;

    public PagoControlador(PagoServicio pagoServicio) {
        this.pagoServicio = pagoServicio;
    }

    /** GET /api/pagos */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<PagoRespuesta>>> listar() {
        return ResponseEntity.ok(RespuestaApi.de("Pagos encontrados", pagoServicio.listarTodos()));
    }

    /** GET /api/pagos/1 → el pago con ID 1, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<PagoRespuesta>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Pago encontrado", pagoServicio.buscarPorId(id)));
    }

    /** PATCH /api/pagos/1/anular */
    @PatchMapping("/{id}/anular")
    public ResponseEntity<RespuestaApi<PagoRespuesta>> anular(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.de("Pago anulado", pagoServicio.anular(id)));
    }
}
