package smartflet.servicio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import smartflet.dto.TarifaRespuesta;
import smartflet.dto.TarifaSolicitud;
import smartflet.excepcion.RecursoDuplicadoException;
import smartflet.excepcion.RecursoNoEncontradoException;
import smartflet.excepcion.ReglaNegocioException;
import smartflet.modelo.EstadoRegistro;
import smartflet.modelo.Tarifa;
import smartflet.repositorio.TarifaRepositorio;

/**
 * Logica de negocio de las tarifas. No pueden existir dos tarifas activas con vigencias que se crucen.
 */
@Service
public class TarifaServicio {

    /** Se usa como "sin fecha de fin" al comparar vigencias. */
    private static final LocalDate SIN_FIN = LocalDate.of(9999, 12, 31);
    /** Al crear no hay tarifa que excluir de la comparacion. */
    /** Al crear no hay tarifa que excluir: ningun registro tiene ID 0. */
    private static final Long NINGUNA = 0L;

    private final TarifaRepositorio tarifaRepositorio;

    public TarifaServicio(TarifaRepositorio tarifaRepositorio) {
        this.tarifaRepositorio = tarifaRepositorio;
    }

    public List<TarifaRespuesta> listarTodas() {
        return tarifaRepositorio.findAll().stream()
                .map(TarifaRespuesta::desde)
                .toList();
    }

    public TarifaRespuesta buscarPorId(Long id) {
        return TarifaRespuesta.desde(obtenerTarifa(id));
    }

    public TarifaRespuesta buscarVigente() {
        return tarifaRepositorio.buscarVigentes(EstadoRegistro.ACTIVO, LocalDate.now()).stream()
                .findFirst()
                .map(TarifaRespuesta::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay una tarifa vigente hoy"));
    }

    public TarifaRespuesta crear(TarifaSolicitud solicitud) {
        validarVigencia(solicitud, NINGUNA);
        Tarifa tarifa = new Tarifa(solicitud.nombre().trim(), solicitud.valorBase(), solicitud.valorPorKm(),
                solicitud.fechaInicioVigencia(), solicitud.fechaFinVigencia());
        return TarifaRespuesta.desde(tarifaRepositorio.save(tarifa));
    }

    /** Los viajes ya cobrados no cambian: el monto quedo guardado en su Pago. */
    public TarifaRespuesta modificar(Long id, TarifaSolicitud solicitud) {
        Tarifa tarifa = obtenerTarifa(id);
        if (tarifa.getEstado() == EstadoRegistro.INACTIVO) {
            throw new ReglaNegocioException("No se puede modificar una tarifa desactivada");
        }
        validarVigencia(solicitud, id);
        tarifa.setNombre(solicitud.nombre().trim());
        tarifa.setValorBase(solicitud.valorBase());
        tarifa.setValorPorKm(solicitud.valorPorKm());
        tarifa.setFechaInicioVigencia(solicitud.fechaInicioVigencia());
        tarifa.setFechaFinVigencia(solicitud.fechaFinVigencia());
        return TarifaRespuesta.desde(tarifaRepositorio.save(tarifa));
    }

    public TarifaRespuesta desactivar(Long id) {
        Tarifa tarifa = obtenerTarifa(id);
        tarifa.desactivarTarifa();
        return TarifaRespuesta.desde(tarifaRepositorio.save(tarifa));
    }

    private Tarifa obtenerTarifa(Long id) {
        return tarifaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tarifa", id));
    }

    private void validarVigencia(TarifaSolicitud solicitud, Long idExcluido) {
        LocalDate inicio = solicitud.fechaInicioVigencia();
        LocalDate fin = solicitud.fechaFinVigencia() == null ? SIN_FIN : solicitud.fechaFinVigencia();
        if (fin.isBefore(inicio)) {
            throw new ReglaNegocioException("La fecha de fin de vigencia no puede ser anterior a la de inicio");
        }
        if (tarifaRepositorio.existeSolapamiento(EstadoRegistro.ACTIVO, inicio, fin, idExcluido)) {
            throw new RecursoDuplicadoException("Ya existe una tarifa activa con una vigencia que se cruza con esas fechas");
        }
    }
}
