/** Pagos → PagoControlador (/api/pagos). El pago vive dentro del viaje y se crea al finalizarlo. */

import { pagosApi } from '../api/recursos.js';
import { esc, insignia, formato, tarjetaKpi, confirmar, avisoExito, manejarError } from '../componentes/ui.js';
import { estadoPago, metodoPago, METODOS_PAGO } from '../componentes/estados.js';
import { crearCatalogo } from '../componentes/catalogo.js';

function resumen(pagos) {
    const validos = pagos.filter((p) => p.estadoPago === 'PAGADO');
    const total = validos.reduce((suma, p) => suma + Number(p.monto), 0);
    const hoy = formato.paraInput();
    const deHoy = validos.filter((p) => String(p.fechaPago).startsWith(hoy));
    return `
        <div class="rejilla-kpi">
            ${tarjetaKpi({ etiqueta: 'Recaudado', valor: formato.dinero(total), detalle: `${validos.length} pagos validos`, tono: 'positivo', icono: 'dinero' })}
            ${tarjetaKpi({ etiqueta: 'Hoy', valor: formato.dinero(deHoy.reduce((s, p) => s + Number(p.monto), 0)), detalle: `${deHoy.length} pagos`, tono: 'informativo', icono: 'calendario' })}
            ${tarjetaKpi({ etiqueta: 'Ticket promedio', valor: formato.dinero(validos.length ? total / validos.length : 0), tono: 'neutro', icono: 'reporte' })}
            ${tarjetaKpi({ etiqueta: 'Anulados', valor: formato.numero(pagos.length - validos.length), tono: 'negativo', icono: 'equis' })}
        </div>`;
}

export const { render } = crearCatalogo({
    id: 'pagos',
    cargar: async () => (await pagosApi.listar())
        .sort((a, b) => String(b.fechaPago).localeCompare(String(a.fechaPago))),
    resumen,
    filtros: [
        { valor: '', etiqueta: 'Todos' },
        { valor: 'PAGADO', etiqueta: 'Pagados', prueba: (p) => p.estadoPago === 'PAGADO' },
        { valor: 'ANULADO', etiqueta: 'Anulados', prueba: (p) => p.estadoPago === 'ANULADO' },
        ...METODOS_PAGO.map((m) => ({ valor: m.valor, etiqueta: m.texto, prueba: (p) => p.metodoPago === m.valor })),
    ],
    columnas: [
        { titulo: 'Fecha', celda: (p) => `${formato.fechaHora(p.fechaPago)}<div class="secundaria dato">Viaje ${esc(String(p.viajeId).slice(-6))}</div>` },
        { titulo: 'Metodo', celda: (p) => esc(metodoPago(p.metodoPago)) },
        { titulo: 'Estado', celda: (p) => insignia(estadoPago(p.estadoPago)) },
        { titulo: 'Monto', clase: 'numerico', celda: (p) => formato.dinero(p.monto) },
    ],
    acciones: (p) => (p.estadoPago === 'PAGADO' ? [{ accion: 'anular', icono: 'equis', titulo: 'Anular pago' }] : []),
    alAccion: {
        anular: async (p, recargar) => {
            const ok = await confirmar({
                titulo: 'Anular pago', mensaje: `${formato.dinero(p.monto)} · ${metodoPago(p.metodoPago)}`,
                detalle: 'El pago queda como Anulado y deja de sumar en los reportes.', textoConfirmar: 'Anular', tono: 'peligro',
            });
            if (!ok) return;
            try {
                await pagosApi.anular(p.id);
                avisoExito('Pago anulado');
                await recargar();
            } catch (error) {
                manejarError(error);
            }
        },
    },
    vacio: { icono: 'dinero', titulo: 'Aun no hay pagos', texto: 'Los pagos se crean al finalizar un viaje.' },
});
