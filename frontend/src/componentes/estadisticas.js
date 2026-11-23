/**
 * Calculos para el panel y los reportes.
 *
 * El backend no tiene endpoints de reportes, asi que las cifras se calculan en el
 * navegador a partir de /api/viajes, /api/pagos y /api/vehiculos.
 */

import { formato } from './ui.js';
import { estadoViaje, ESTADOS_VIAJE, METODOS_PAGO } from './estados.js';

/** "2026-11-22" para la fecha local de un LocalDateTime del backend. */
export const dia = (valor) => String(valor ?? '').slice(0, 10);

/** Serie diaria {fecha, total, finalizados, ingresos} entre dos fechas (inclusive). */
export function serieDiaria(viajes, desde, hasta) {
    const puntos = new Map();
    for (let f = new Date(`${desde}T00:00:00`); dia(formato.paraInput(f)) <= hasta; f.setDate(f.getDate() + 1)) {
        puntos.set(formato.paraInput(f), { fecha: formato.paraInput(f), total: 0, finalizados: 0, ingresos: 0 });
    }
    for (const v of viajes) {
        const punto = puntos.get(dia(v.fechaSolicitud));
        if (!punto) continue;
        punto.total += 1;
        if (v.estadoViaje === 'FINALIZADO') punto.finalizados += 1;
        if (v.pago?.estadoPago === 'PAGADO') punto.ingresos += Number(v.pago.monto);
    }
    return [...puntos.values()];
}

/** Datos para el anillo de viajes por estado. */
export const distribucionViajes = (viajes) =>
    ESTADOS_VIAJE.map((codigo) => ({
        etiqueta: estadoViaje(codigo).etiqueta,
        tono: estadoViaje(codigo).tono,
        cantidad: viajes.filter((v) => v.estadoViaje === codigo).length,
    }));

/** Ingresos por metodo de pago (solo pagos validos). */
export const ingresosPorMetodo = (pagos) => {
    const tonos = { EFECTIVO: 'positivo', TARJETA: 'informativo', TRANSFERENCIA: 'advertencia' };
    return METODOS_PAGO.map((m) => ({
        etiqueta: m.texto,
        tono: tonos[m.valor],
        cantidad: pagos.filter((p) => p.metodoPago === m.valor && p.estadoPago === 'PAGADO').length,
        monto: pagos.filter((p) => p.metodoPago === m.valor && p.estadoPago === 'PAGADO')
            .reduce((s, p) => s + Number(p.monto), 0),
    }));
};

export const sumaPagados = (viajes) =>
    viajes.reduce((s, v) => s + (v.pago?.estadoPago === 'PAGADO' ? Number(v.pago.monto) : 0), 0);

export function haceDias(n) {
    const f = new Date();
    f.setDate(f.getDate() - n);
    return formato.paraInput(f);
}
