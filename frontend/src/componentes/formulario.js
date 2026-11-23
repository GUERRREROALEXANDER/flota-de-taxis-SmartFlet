/**
 * Formularios en ventana modal a partir de una lista de campos.
 *
 * Cada vista describe sus campos (nombre, etiqueta, tipo...) y este modulo arma el HTML,
 * convierte los valores al tipo que espera el DTO de Spring y pinta los errores de
 * validacion (@NotBlank, @Pattern...) debajo del campo que fallo.
 */

import { esc, abrirModal, cerrarModal, datosFormulario, manejarError, conCarga } from './ui.js';

let consecutivo = 0;

/**
 * @param {object} opciones
 * @param {string} opciones.titulo
 * @param {string} [opciones.descripcion]
 * @param {Array}  opciones.campos   {nombre, etiqueta, tipo, requerido, opciones, ayuda, valor, ...}
 * @param {string} [opciones.textoGuardar]
 * @param {(datos: object) => Promise} opciones.guardar  recibe los datos ya convertidos
 */
export async function abrirFormulario({ titulo, descripcion = '', campos, textoGuardar = 'Guardar', guardar }) {
    const id = `formulario-${++consecutivo}`;

    // Los <select> que dependen de la API (pasajeros, vehiculos...) se cargan antes de abrir.
    const opcionesCargadas = await Promise.all(
        campos.map((campo) => (typeof campo.opciones === 'function' ? campo.opciones() : campo.opciones)),
    );

    const caja = abrirModal({
        titulo,
        descripcion,
        contenido: `
            <form id="${id}" novalidate>
                <div class="rejilla-campos">
                    ${campos.map((campo, i) => campoHtml(campo, opcionesCargadas[i])).join('')}
                </div>
            </form>`,
        acciones: `
            <button class="boton boton--fantasma" data-cerrar-modal>Cancelar</button>
            <button class="boton boton--primario" data-guardar>${esc(textoGuardar)}</button>`,
    });

    const formulario = caja.querySelector(`#${id}`);
    const botonGuardar = caja.querySelector('[data-guardar]');

    const enviar = async () => {
        const datos = convertir(datosFormulario(formulario), campos, formulario);
        await conCarga(botonGuardar, async () => {
            try {
                await guardar(datos);
                cerrarModal();
            } catch (error) {
                manejarError(error, formulario);
            }
        });
    };

    botonGuardar.addEventListener('click', enviar);
    formulario.addEventListener('submit', (evento) => {
        evento.preventDefault();
        enviar();
    });
}

function campoHtml(campo, opciones) {
    const idCampo = `campo-${campo.nombre}-${consecutivo}`;
    const valor = campo.valor ?? '';
    const obligatorio = campo.requerido ? '<span class="campo__obligatorio">*</span>' : '';
    const estiloAncho = campo.ancho === 'completo' ? ' style="grid-column:1 / -1"' : '';

    let control;
    if (campo.tipo === 'select') {
        control = `
            <select class="campo__control" id="${idCampo}" name="${esc(campo.nombre)}" ${campo.requerido ? 'required' : ''}>
                ${campo.vacio !== false ? `<option value="">${esc(campo.vacio || 'Seleccione…')}</option>` : ''}
                ${(opciones || []).map((o) => `
                    <option value="${esc(o.valor)}" ${String(o.valor) === String(valor) ? 'selected' : ''}>${esc(o.texto)}</option>`).join('')}
            </select>`;
    } else if (campo.tipo === 'checkbox') {
        control = `
            <label style="display:flex;gap:.5rem;align-items:center;font-size:.875rem">
                <input type="checkbox" id="${idCampo}" name="${esc(campo.nombre)}" value="true" ${valor ? 'checked' : ''}>
                ${esc(campo.textoCasilla || campo.etiqueta)}
            </label>`;
    } else {
        const extras = ['min', 'max', 'step', 'maxlength', 'placeholder']
            .filter((atributo) => campo[atributo] !== undefined)
            .map((atributo) => `${atributo}="${esc(campo[atributo])}"`)
            .join(' ');
        control = `
            <input class="campo__control${campo.dato ? ' dato' : ''}" id="${idCampo}" name="${esc(campo.nombre)}"
                   type="${esc(campo.tipo || 'text')}" value="${esc(valor)}" ${extras}
                   ${campo.requerido ? 'required' : ''}>`;
    }

    return `
        <div class="campo"${estiloAncho}>
            <label class="campo__etiqueta" for="${idCampo}">${esc(campo.etiqueta)} ${obligatorio}</label>
            <div class="campo__envoltorio">${control}</div>
            ${campo.ayuda ? `<p class="campo__ayuda">${esc(campo.ayuda)}</p>` : ''}
            <p class="campo__error" hidden></p>
        </div>`;
}

/** Convierte cada valor al tipo que espera el DTO (Integer, BigDecimal, Boolean, null...). */
function convertir(datos, campos, formulario) {
    const resultado = {};
    for (const campo of campos) {
        let valor = datos[campo.nombre];
        if (campo.tipo === 'checkbox') {
            valor = formulario.elements[campo.nombre].checked;
        } else if (valor === '' || valor === undefined) {
            valor = null;
        } else if (campo.tipo === 'number') {
            valor = Number(valor);
        } else if (campo.tipo === 'datetime-local' && valor.length === 16) {
            valor = `${valor}:00`;
        }
        if (campo.transformar && valor !== null) valor = campo.transformar(valor);
        resultado[campo.nombre] = valor;
    }
    return resultado;
}

/** Valor de un LocalDateTime del backend en el formato de <input type="datetime-local">. */
export function paraFechaHora(valor) {
    return valor ? String(valor).slice(0, 16) : '';
}
