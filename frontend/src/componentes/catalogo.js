/**
 * Vista de catalogo reutilizable: busqueda, filtros con contador, tabla y acciones por fila.
 *
 * Todas las pantallas de gestion (vehiculos, conductores, viajes...) tienen la misma forma,
 * asi que la forma vive aqui y cada vista solo describe sus columnas, filtros y acciones.
 */

import { $, $$, esc, crear, icono, vacio, esqueletoTabla, retrasar } from './ui.js';

/**
 * @param {object} config
 * @param {string} config.id                 prefijo para los ids del DOM
 * @param {() => Promise<Array>} config.cargar  trae los registros (ya enriquecidos si hace falta)
 * @param {string} [config.textoNuevo]       texto del boton de alta; sin el no hay boton
 * @param {() => void} [config.alCrear]
 * @param {object} [config.busqueda]         { placeholder, texto: (item) => string }
 * @param {Array}  [config.filtros]          [{ valor, etiqueta, prueba: (item) => bool }]
 * @param {Array}  config.columnas           [{ titulo, clase, celda: (item) => html }]
 * @param {(item) => Array} [config.acciones] [{ accion, icono, titulo }]
 * @param {object} [config.alAccion]         { nombreAccion: async (item, recargar) => {} }
 * @param {object} config.vacio              { icono, titulo, texto }
 * @param {(items) => string} [config.resumen] HTML opcional encima de la tabla
 */
export function crearCatalogo(config) {
    const filtro = { buscar: '', valor: '' };

    async function render(contenedor, { acciones }) {
        if (config.textoNuevo) {
            const boton = crear(`<button class="boton boton--primario">${icono('mas')}<span>${esc(config.textoNuevo)}</span></button>`);
            boton.addEventListener('click', () => config.alCrear(recargar));
            acciones.appendChild(boton);
        }

        contenedor.innerHTML = `
            <div id="${config.id}-resumen"></div>
            <div id="${config.id}-herramientas"></div>
            <div class="tarjeta">
                <div class="tarjeta__cuerpo tarjeta__cuerpo--sin-relleno" id="${config.id}-tabla">
                    ${esqueletoTabla(6, config.columnas.length)}
                </div>
            </div>`;

        await recargar();
    }

    async function recargar() {
        const todos = await config.cargar();
        if (config.resumen) $(`#${config.id}-resumen`).innerHTML = config.resumen(todos);
        pintarHerramientas(todos);
        pintarTabla(todos);
    }

    function filtrados(todos) {
        const activo = (config.filtros || []).find((f) => f.valor === filtro.valor);
        const texto = filtro.buscar.toLowerCase();
        return todos.filter((item) =>
            (!activo?.prueba || activo.prueba(item))
            && (!texto || !config.busqueda || config.busqueda.texto(item).toLowerCase().includes(texto)));
    }

    function pintarHerramientas(todos) {
        const zona = $(`#${config.id}-herramientas`);
        const filtros = config.filtros || [];
        zona.innerHTML = `
            <div class="herramientas">
                ${config.busqueda ? `
                <div class="herramientas__busqueda campo" style="margin:0">
                    <div class="campo__envoltorio">
                        ${icono('lupa', 'campo__icono')}
                        <input class="campo__control" type="search" id="${config.id}-buscar"
                               placeholder="${esc(config.busqueda.placeholder)}"
                               aria-label="${esc(config.busqueda.placeholder)}" value="${esc(filtro.buscar)}">
                    </div>
                </div>` : ''}
                ${filtros.length ? `
                <div class="filtros" role="group" aria-label="Filtrar">
                    ${filtros.map((f) => `
                        <button class="filtro" data-valor="${esc(f.valor)}" aria-pressed="${filtro.valor === f.valor}">
                            ${esc(f.etiqueta)}
                            <span class="filtro__contador">${f.prueba ? todos.filter(f.prueba).length : todos.length}</span>
                        </button>`).join('')}
                </div>` : ''}
            </div>`;

        const buscador = $(`#${config.id}-buscar`);
        if (buscador) {
            buscador.addEventListener('input', retrasar((evento) => {
                filtro.buscar = evento.target.value;
                pintarTabla(todos);
            }, 200));
        }
        $$('.filtro', zona).forEach((boton) => {
            boton.addEventListener('click', () => {
                filtro.valor = boton.dataset.valor;
                $$('.filtro', zona).forEach((b) => b.setAttribute('aria-pressed', String(b === boton)));
                pintarTabla(todos);
            });
        });
    }

    function pintarTabla(todos) {
        const zona = $(`#${config.id}-tabla`);
        const items = filtrados(todos);

        if (items.length === 0) {
            const hayFiltro = filtro.buscar || filtro.valor;
            zona.innerHTML = vacio({
                icono: config.vacio.icono,
                titulo: hayFiltro ? 'Ningun registro coincide' : config.vacio.titulo,
                texto: hayFiltro ? 'Pruebe con otra busqueda o quite el filtro.' : config.vacio.texto,
            });
            return;
        }

        zona.innerHTML = `
            <div class="tabla-envoltorio">
                <table class="tabla">
                    <thead>
                        <tr>
                            ${config.columnas.map((c) => `<th class="${esc(c.clase || '')}">${esc(c.titulo)}</th>`).join('')}
                            ${config.acciones ? '<th class="acciones">Acciones</th>' : ''}
                        </tr>
                    </thead>
                    <tbody>
                        ${items.map((item) => `
                            <tr>
                                ${config.columnas.map((c) => `<td class="${esc(c.clase || '')}">${c.celda(item)}</td>`).join('')}
                                ${config.acciones ? `<td class="acciones"><div class="grupo-acciones">
                                    ${config.acciones(item).map((a) => `
                                        <button class="boton-icono" data-accion="${esc(a.accion)}" data-id="${esc(item.id)}"
                                                title="${esc(a.titulo)}" aria-label="${esc(a.titulo)}">${icono(a.icono)}</button>`).join('')}
                                </div></td>` : ''}
                            </tr>`).join('')}
                    </tbody>
                </table>
            </div>`;

        // onclick y no addEventListener: la tabla se repinta en cada filtro.
        zona.onclick = async (evento) => {
            const boton = evento.target.closest('[data-accion]');
            if (!boton) return;
            const item = todos.find((i) => String(i.id) === boton.dataset.id);
            const manejador = config.alAccion?.[boton.dataset.accion];
            if (item && manejador) await manejador(item, recargar);
        };
    }

    return { render };
}
