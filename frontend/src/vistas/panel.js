/**
 * Panel de control: lo primero que ve el operador.
 * Indicadores → avisos → actividad de la semana → viajes que esperan atencion.
 */

import { vehiculosApi, viajesApi, turnosApi, conductoresApi, tarifasApi, pasajerosApi } from '../api/recursos.js';
import { esc, icono, insignia, tarjetaKpi, esqueletoKpi, formato, vacio } from '../componentes/ui.js';
import { estadoViaje } from '../componentes/estados.js';
import { anillo, areaTemporal } from '../componentes/graficas.js';
import { serieDiaria, distribucionViajes, sumaPagados, dia, haceDias } from '../componentes/estadisticas.js';
import { irA, fijarContador } from '../app/enrutador.js';

export async function render(contenedor) {
    contenedor.innerHTML = `
        ${esqueletoKpi(5)}
        <div class="rejilla-panel">
            <div class="esqueleto" style="height:320px;border-radius:var(--radio-l)"></div>
            <div class="esqueleto" style="height:320px;border-radius:var(--radio-l)"></div>
        </div>`;

    const [vehiculos, viajes, turnos, conductores, pasajeros, vigente] = await Promise.all([
        vehiculosApi.listar(), viajesApi.listar(), turnosApi.listar(), conductoresApi.listar(),
        pasajerosApi.listar(), tarifasApi.vigente().catch(() => null),
    ]);

    const hoy = formato.paraInput();
    const operativos = vehiculos.filter((v) => v.estadoOperativo).length;
    const enTurno = new Set(turnos.filter((t) => t.estado === 'EN_CURSO').map((t) => t.vehiculoId));
    const pendientes = viajes.filter((v) => v.estadoViaje === 'SOLICITADO');
    const activos = viajes.filter((v) => ['SOLICITADO', 'ASIGNADO', 'EN_CURSO'].includes(v.estadoViaje));
    const finalizadosHoy = viajes.filter((v) => v.estadoViaje === 'FINALIZADO' && dia(v.fechaFin) === hoy);

    fijarContador('viajes', pendientes.length);

    const nombres = new Map(pasajeros.map((p) => [p.id, p.nombre]));
    const placas = new Map(vehiculos.map((v) => [v.id, v.placa]));

    contenedor.innerHTML = `
        <div class="rejilla-kpi animar-entrada">
            ${tarjetaKpi({ etiqueta: 'Flota', valor: vehiculos.length,
                detalle: `${operativos} operativos`, tono: 'informativo', icono: 'taxi' })}
            ${tarjetaKpi({ etiqueta: 'En turno ahora', valor: enTurno.size,
                detalle: 'Pueden recibir viajes', tono: 'positivo', icono: 'reloj' })}
            ${tarjetaKpi({ etiqueta: 'Por asignar', valor: pendientes.length,
                detalle: 'Viajes solicitados', tono: pendientes.length ? 'advertencia' : 'neutro', icono: 'servicio' })}
            ${tarjetaKpi({ etiqueta: 'En curso', valor: viajes.filter((v) => v.estadoViaje === 'EN_CURSO').length,
                detalle: 'Pasajeros a bordo', tono: 'informativo', icono: 'reproducir' })}
            ${tarjetaKpi({ etiqueta: 'Ingresos de hoy', valor: formato.dinero(sumaPagados(finalizadosHoy)),
                detalle: `${finalizadosHoy.length} viaje${finalizadosHoy.length === 1 ? '' : 's'} cerrado${finalizadosHoy.length === 1 ? '' : 's'}`,
                tono: 'positivo', icono: 'dinero' })}
        </div>
        ${avisos({ vigente, pendientes, enTurno, conductores })}
        <div class="rejilla-panel">
            ${tarjeta('Actividad de los ultimos 7 dias', `${formato.dinero(sumaPagados(viajes.filter((v) => dia(v.fechaSolicitud) >= haceDias(6))))} cobrados`,
                areaTemporal(serieDiaria(viajes, haceDias(6), hoy), { campo: 'total', etiqueta: 'viajes', altura: 230 }))}
            ${tarjeta('Viajes por estado', `${viajes.length} viajes registrados`,
                anillo(distribucionViajes(viajes), { titulo: 'Viajes por estado', textoCentro: 'Viajes' }))}
        </div>
        ${tarjeta('Viajes activos', 'Solicitados, asignados y en curso', tablaActivos(activos, nombres, placas), true)}`;

    contenedor.querySelectorAll('[data-ir-a]').forEach((el) => {
        el.addEventListener('click', () => irA(el.dataset.irA));
        el.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); irA(el.dataset.irA); }
        });
    });
}

function tarjeta(titulo, subtitulo, cuerpo, sinRelleno = false) {
    return `
        <section class="tarjeta">
            <header class="tarjeta__cabecera">
                <div>
                    <h2 class="tarjeta__titulo">${esc(titulo)}</h2>
                    <p class="tarjeta__subtitulo">${esc(subtitulo)}</p>
                </div>
            </header>
            <div class="tarjeta__cuerpo${sinRelleno ? ' tarjeta__cuerpo--sin-relleno' : ''}">${cuerpo}</div>
        </section>`;
}

/** Avisos que llevan a la pantalla donde se resuelve el problema. */
function avisos({ vigente, pendientes, enTurno, conductores }) {
    const lista = [];
    if (!vigente) {
        lista.push({ tono: 'negativo', titulo: 'No hay tarifa vigente', detalle: 'Sin tarifa vigente no se pueden solicitar viajes.', vista: 'tarifas' });
    }
    if (pendientes.length && enTurno.size === 0) {
        lista.push({ tono: 'advertencia', titulo: `${pendientes.length} viaje(s) esperando y ningun vehiculo en turno`,
                     detalle: 'Inicie un turno para poder asignarlos.', vista: 'turnos' });
    } else if (pendientes.length) {
        lista.push({ tono: 'advertencia', titulo: `${pendientes.length} viaje(s) por asignar`, detalle: 'Asigne un vehiculo en turno.', vista: 'viajes' });
    }
    const hoy = formato.paraInput();
    const vencidas = conductores.filter((c) => c.estadoConductor === 'ACTIVO' && c.fechaVencimientoLicencia < hoy);
    if (vencidas.length) {
        lista.push({ tono: 'negativo', titulo: `${vencidas.length} conductor(es) con la licencia vencida`,
                     detalle: vencidas.map((c) => c.nombre).join(', '), vista: 'conductores' });
    }

    if (!lista.length) {
        return `
            <div class="alerta alerta--positivo">
                ${icono('exito', 'alerta__icono')}
                <div class="alerta__contenido">
                    <p class="alerta__titulo">Todo en orden</p>
                    <p class="alerta__texto">Hay tarifa vigente, ningun viaje espera asignacion y las licencias estan al dia.</p>
                </div>
            </div>`;
    }
    return `<div class="pila pila--compacta" style="margin-bottom:var(--e5)">
        ${lista.map((a) => `
            <div class="alerta alerta--${a.tono} alerta--accionable" style="margin-bottom:0"
                 data-ir-a="${a.vista}" role="button" tabindex="0">
                ${icono('alerta', 'alerta__icono')}
                <div class="alerta__contenido">
                    <p class="alerta__titulo">${esc(a.titulo)}</p>
                    <p class="alerta__texto">${esc(a.detalle)}</p>
                </div>
                ${icono('enlace', 'icono--tenue')}
            </div>`).join('')}
    </div>`;
}

function tablaActivos(activos, nombres, placas) {
    if (!activos.length) {
        return vacio({
            icono: 'servicio', titulo: 'No hay viajes activos',
            texto: 'Cuando un pasajero solicite un viaje aparecera aqui hasta que termine.',
            accion: '<button class="boton boton--primario" data-ir-a="viajes">Ir a viajes</button>',
        });
    }
    return `
        <div class="tabla-envoltorio">
            <table class="tabla">
                <thead><tr><th>Recorrido</th><th>Pasajero</th><th>Vehiculo</th><th>Estado</th><th class="numerico">Solicitado</th></tr></thead>
                <tbody>
                    ${activos.map((v) => `
                        <tr>
                            <td><div class="principal-celda">${esc(v.origen)}</div><div class="secundaria">→ ${esc(v.destino)}</div></td>
                            <td>${esc(nombres.get(v.pasajeroId) ?? '—')}</td>
                            <td>${v.vehiculoId ? `<span class="dato">${esc(placas.get(v.vehiculoId) ?? '—')}</span>`
                                               : '<span class="insignia insignia--advertencia">Sin vehiculo</span>'}</td>
                            <td>${insignia(estadoViaje(v.estadoViaje))}</td>
                            <td class="numerico">${formato.relativo(v.fechaSolicitud)}</td>
                        </tr>`).join('')}
                </tbody>
            </table>
        </div>`;
}
