/** Reportes por periodo: viajes, ingresos, metodos de pago y rendimiento por vehiculo. */

import { viajesApi, vehiculosApi, pagosApi } from '../api/recursos.js';
import { $, esc, crear, icono, tarjetaKpi, formato } from '../componentes/ui.js';
import { anillo, areaTemporal, barrasHorizontales } from '../componentes/graficas.js';
import {
    serieDiaria, distribucionViajes, ingresosPorMetodo, sumaPagados, dia, haceDias,
} from '../componentes/estadisticas.js';

const periodo = { desde: haceDias(29), hasta: formato.paraInput() };

export async function render(contenedor, { acciones }) {
    const barra = crear(`
        <form id="form-periodo" style="display:flex;gap:.5rem;align-items:center;flex-wrap:wrap">
            <input class="campo__control" type="date" id="periodo-desde" name="desde" value="${periodo.desde}" aria-label="Desde" style="width:auto">
            <span class="secundaria">a</span>
            <input class="campo__control" type="date" id="periodo-hasta" name="hasta" value="${periodo.hasta}" aria-label="Hasta" style="width:auto">
            <button class="boton boton--secundario" type="submit">${icono('calendario')}<span>Aplicar</span></button>
        </form>`);
    barra.addEventListener('submit', (evento) => {
        evento.preventDefault();
        periodo.desde = $('#periodo-desde').value || periodo.desde;
        periodo.hasta = $('#periodo-hasta').value || periodo.hasta;
        pintar(contenedor);
    });
    acciones.appendChild(barra);
    await pintar(contenedor);
}

async function pintar(contenedor) {
    const [todos, vehiculos, pagos] = await Promise.all([viajesApi.listar(), vehiculosApi.listar(), pagosApi.listar()]);
    const enRango = (fecha) => dia(fecha) >= periodo.desde && dia(fecha) <= periodo.hasta;
    const viajes = todos.filter((v) => enRango(v.fechaSolicitud));
    const pagosRango = pagos.filter((p) => enRango(p.fechaPago));
    const finalizados = viajes.filter((v) => v.estadoViaje === 'FINALIZADO');
    const cancelados = viajes.filter((v) => v.estadoViaje === 'CANCELADO');
    const ingresos = sumaPagados(viajes);
    const km = finalizados.reduce((s, v) => s + Number(v.distanciaKm || 0), 0);

    const porVehiculo = vehiculos
        .map((ve) => {
            const suyos = finalizados.filter((v) => v.vehiculoId === ve.id);
            return { etiqueta: ve.placa, valor: sumaPagados(suyos), detalle: `${suyos.length} viajes`, tono: 'informativo' };
        })
        .filter((x) => x.valor > 0)
        .sort((a, b) => b.valor - a.valor)
        .slice(0, 8);

    const metodos = ingresosPorMetodo(pagosRango);

    contenedor.innerHTML = `
        <div class="rejilla-kpi">
            ${tarjetaKpi({ etiqueta: 'Viajes solicitados', valor: formato.numero(viajes.length), detalle: `${formato.fecha(periodo.desde + 'T00:00')} – ${formato.fecha(periodo.hasta + 'T00:00')}`, icono: 'servicio' })}
            ${tarjetaKpi({ etiqueta: 'Finalizados', valor: formato.numero(finalizados.length),
                detalle: viajes.length ? `${formato.decimal((finalizados.length / viajes.length) * 100, 0)} % del total` : '—', tono: 'positivo', icono: 'revisar' })}
            ${tarjetaKpi({ etiqueta: 'Cancelados', valor: formato.numero(cancelados.length), tono: cancelados.length ? 'negativo' : 'neutro', icono: 'equis' })}
            ${tarjetaKpi({ etiqueta: 'Ingresos', valor: formato.dinero(ingresos), detalle: `${formato.kilometros(km)} recorridos`, tono: 'positivo', icono: 'dinero' })}
        </div>
        <section class="tarjeta" style="margin-bottom:var(--e5)">
            <header class="tarjeta__cabecera"><div>
                <h2 class="tarjeta__titulo">Viajes por dia</h2>
                <p class="tarjeta__subtitulo">Solicitudes registradas en el periodo</p>
            </div></header>
            <div class="tarjeta__cuerpo">${areaTemporal(serieDiaria(viajes, periodo.desde, periodo.hasta), { campo: 'total', etiqueta: 'viajes', altura: 240 })}</div>
        </section>
        <div class="rejilla-panel">
            <section class="tarjeta">
                <header class="tarjeta__cabecera"><div>
                    <h2 class="tarjeta__titulo">Ingresos por vehiculo</h2>
                    <p class="tarjeta__subtitulo">Pagos validos de viajes finalizados</p>
                </div></header>
                <div class="tarjeta__cuerpo">${porVehiculo.length
                    ? barrasHorizontales(porVehiculo, { formatoValor: (v) => formato.dinero(v) })
                    : '<p class="vacio__texto">Ningun vehiculo cerro viajes en el periodo.</p>'}</div>
            </section>
            <section class="tarjeta">
                <header class="tarjeta__cabecera"><div>
                    <h2 class="tarjeta__titulo">Metodos de pago</h2>
                    <p class="tarjeta__subtitulo">${metodos.map((m) => `${esc(m.etiqueta)}: ${formato.dinero(m.monto)}`).join(' · ')}</p>
                </div></header>
                <div class="tarjeta__cuerpo">${anillo(metodos, { titulo: 'Pagos por metodo', textoCentro: 'Pagos' })}</div>
            </section>
        </div>
        <section class="tarjeta">
            <header class="tarjeta__cabecera"><div>
                <h2 class="tarjeta__titulo">Viajes por estado</h2>
                <p class="tarjeta__subtitulo">Como terminaron las solicitudes del periodo</p>
            </div></header>
            <div class="tarjeta__cuerpo">${anillo(distribucionViajes(viajes), { titulo: 'Viajes por estado', textoCentro: 'Viajes' })}</div>
        </section>`;
}
