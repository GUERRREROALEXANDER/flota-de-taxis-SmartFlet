/**
 * Los documentos de MongoDB guardan solo el id de otros documentos (un turno guarda
 * `conductorId` y `vehiculoId`). Para mostrar nombres y placas, las vistas cargan los
 * catalogos con estas funciones y buscan por id.
 */

import { vehiculosApi, conductoresApi, pasajerosApi } from '../api/recursos.js';
import { esc } from './ui.js';

const porId = (lista) => new Map(lista.map((item) => [item.id, item]));

export async function cargarReferencias({ vehiculos = false, conductores = false, pasajeros = false } = {}) {
    const [v, c, p] = await Promise.all([
        vehiculos ? vehiculosApi.listar() : [],
        conductores ? conductoresApi.listar() : [],
        pasajeros ? pasajerosApi.listar() : [],
    ]);
    return { vehiculos: porId(v), conductores: porId(c), pasajeros: porId(p) };
}

export const textoVehiculo = (mapa, id) => {
    if (!id) return '<span class="secundaria">Sin asignar</span>';
    const v = mapa.get(id);
    return v
        ? `<span class="dato" style="font-weight:600">${esc(v.placa)}</span><div class="secundaria">${esc(v.modelo)}</div>`
        : `<span class="secundaria dato">${esc(id)}</span>`;
};

export const textoPersona = (mapa, id) => {
    const persona = mapa.get(id);
    return persona ? esc(persona.nombre) : `<span class="secundaria dato">${esc(id)}</span>`;
};

/** Opciones de <select> para los formularios. */
export const opcionesVehiculos = (lista) =>
    lista.filter((v) => v.estadoOperativo).map((v) => ({ valor: v.id, texto: `${v.placa} · ${v.modelo}` }));

export const opcionesPersonas = (lista, campoEstado) =>
    lista.filter((p) => p[campoEstado] === 'ACTIVO').map((p) => ({ valor: p.id, texto: `${p.nombre} · ${p.identificacion}` }));
