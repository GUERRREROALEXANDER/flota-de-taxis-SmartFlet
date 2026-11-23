/**
 * Navegacion de la aplicacion de una sola pagina.
 *
 * Cada vista tiene su ruta en el fragmento de la URL (#/viajes), asi se puede recargar,
 * guardar en favoritos y usar el boton Atras del navegador.
 */

import { $, $$, esc, icono } from '../componentes/ui.js';
import { ErrorApi } from '../api/clienteHttp.js';

import * as vistaPanel from '../vistas/panel.js';
import * as vistaViajes from '../vistas/viajes.js';
import * as vistaTurnos from '../vistas/turnos.js';
import * as vistaVehiculos from '../vistas/vehiculos.js';
import * as vistaConductores from '../vistas/conductores.js';
import * as vistaPasajeros from '../vistas/pasajeros.js';
import * as vistaRutas from '../vistas/rutas.js';
import * as vistaTarifas from '../vistas/tarifas.js';
import * as vistaPagos from '../vistas/pagos.js';
import * as vistaReportes from '../vistas/reportes.js';

/** Una entrada por pantalla. `api` indica el controlador del backend que usa. */
const VISTAS = [
    { ruta: 'panel', titulo: 'Panel de control', grupo: 'Operacion', icono: 'panel', modulo: vistaPanel,
      descripcion: 'Situacion de la flota, avisos y viajes activos' },
    { ruta: 'viajes', titulo: 'Viajes', grupo: 'Operacion', icono: 'servicio', modulo: vistaViajes,
      descripcion: 'Solicitud, asignacion, inicio y cierre de los viajes · /api/viajes' },
    { ruta: 'turnos', titulo: 'Turnos', grupo: 'Operacion', icono: 'reloj', modulo: vistaTurnos,
      descripcion: 'Que conductor maneja que vehiculo y cuando · /api/turnos' },
    { ruta: 'vehiculos', titulo: 'Vehiculos', grupo: 'Flota', icono: 'taxi', modulo: vistaVehiculos,
      descripcion: 'Vehiculos de la flota y su estado operativo · /api/vehiculos' },
    { ruta: 'conductores', titulo: 'Conductores', grupo: 'Flota', icono: 'conductor', modulo: vistaConductores,
      descripcion: 'Personal de conduccion y vencimiento de licencias · /api/conductores' },
    { ruta: 'rutas', titulo: 'Rutas', grupo: 'Flota', icono: 'ruta', modulo: vistaRutas,
      descripcion: 'Trayectos frecuentes con distancia y duracion · /api/rutas' },
    { ruta: 'pasajeros', titulo: 'Pasajeros', grupo: 'Clientes', icono: 'pasajero', modulo: vistaPasajeros,
      descripcion: 'Pasajeros registrados que pueden solicitar viajes · /api/pasajeros' },
    { ruta: 'tarifas', titulo: 'Tarifas', grupo: 'Cobro', icono: 'tarifa', modulo: vistaTarifas,
      descripcion: 'Valor base y valor por km de cada tarifa · /api/tarifas' },
    { ruta: 'pagos', titulo: 'Pagos', grupo: 'Cobro', icono: 'dinero', modulo: vistaPagos,
      descripcion: 'Pagos generados al finalizar cada viaje · /api/pagos' },
    { ruta: 'reportes', titulo: 'Reportes', grupo: 'Cobro', icono: 'reporte', modulo: vistaReportes,
      descripcion: 'Viajes e ingresos por periodo, vehiculo y metodo de pago' },
];

const GRUPOS = ['Operacion', 'Flota', 'Clientes', 'Cobro'];

let vistaActual = null;

export function construirNavegacion() {
    const navegacion = $('#navegacion');
    navegacion.innerHTML = GRUPOS.map((grupo) => `
        <div class="grupo-navegacion">
            <p class="grupo-navegacion__titulo">${esc(grupo)}</p>
            <div class="pila pila--compacta" style="gap:2px">
                ${VISTAS.filter((v) => v.grupo === grupo).map((v) => `
                    <a class="enlace-navegacion" href="#/${v.ruta}" data-ruta="${v.ruta}">
                        ${icono(v.icono)}
                        <span>${esc(v.titulo)}</span>
                        <span class="enlace-navegacion__contador" data-contador="${v.ruta}" hidden></span>
                    </a>`).join('')}
            </div>
        </div>`).join('');
}

function vistaDesdeHash() {
    const ruta = location.hash.replace(/^#\/?/, '').split('?')[0];
    return VISTAS.find((v) => v.ruta === ruta) || VISTAS[0];
}

export async function renderizarVista(vista = vistaDesdeHash()) {
    vistaActual = vista;
    if (location.hash !== `#/${vista.ruta}`) history.replaceState(null, '', `#/${vista.ruta}`);

    $('#titulo-vista').textContent = vista.titulo;
    $('#descripcion-vista').textContent = vista.descripcion;
    document.title = `${vista.titulo} · SmartFlet`;
    $$('.enlace-navegacion').forEach((enlace) => {
        if (enlace.dataset.ruta === vista.ruta) enlace.setAttribute('aria-current', 'page');
        else enlace.removeAttribute('aria-current');
    });

    const acciones = $('#acciones-vista');
    acciones.innerHTML = '';
    const contenido = $('#contenido');
    contenido.innerHTML = '';
    window.scrollTo({ top: 0 });

    try {
        await vista.modulo.render(contenido, { acciones });
    } catch (error) {
        contenido.innerHTML = `
            <div class="tarjeta"><div class="tarjeta__cuerpo">
                <div class="alerta alerta--negativo" style="margin:0">
                    ${icono('alerta', 'alerta__icono')}
                    <div class="alerta__contenido">
                        <p class="alerta__titulo">No se pudo cargar la vista</p>
                        <p class="alerta__texto">${esc(error.message)}</p>
                    </div>
                </div>
                <button class="boton boton--secundario" style="margin-top:1rem" id="reintentar">Reintentar</button>
            </div></div>`;
        $('#reintentar').addEventListener('click', () => renderizarVista(vista));
        if (!(error instanceof ErrorApi)) console.error(error);
    }
}

export const recargarVista = () => renderizarVista(vistaActual ?? vistaDesdeHash());

export function irA(ruta) {
    location.hash = `#/${ruta}`;
}

/** Numero junto a una entrada del menu (por ejemplo, viajes por asignar). */
export function fijarContador(ruta, valor) {
    const contador = $(`[data-contador="${ruta}"]`);
    if (!contador) return;
    contador.hidden = !valor;
    contador.textContent = valor || '';
}

export function escucharCambiosDeRuta() {
    window.addEventListener('hashchange', () => renderizarVista(vistaDesdeHash()));
}
