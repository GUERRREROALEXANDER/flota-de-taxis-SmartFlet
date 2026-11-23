/**
 * Biblioteca de interfaz: formato, iconos, avisos, ventanas modales y bloques reutilizables.
 *
 * Todo el marcado se genera con plantillas de cadena, que resultan mucho mas legibles que
 * una cascada de `createElement`. La contrapartida es que cualquier dato interpolado debe
 * pasar por `esc()`: un nombre de cliente con `<` no puede convertirse en etiquetas. La
 * regla es simple y sin excepciones: los datos que vienen de la API van siempre escapados.
 */

import { ErrorApi } from '../api/clienteHttp.js';

/* ==========================================================================
   Utilidades basicas
   ========================================================================== */

/** Escapa texto para incrustarlo con seguridad en HTML. */
export function esc(valor) {
    if (valor === null || valor === undefined) return '';
    return String(valor)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/** Crea un elemento a partir de una cadena de HTML. */
export function crear(cadenaHtml) {
    const plantilla = document.createElement('template');
    plantilla.innerHTML = cadenaHtml.trim();
    return plantilla.content.firstElementChild;
}

export const $ = (selector, raiz = document) => raiz.querySelector(selector);
export const $$ = (selector, raiz = document) => Array.from(raiz.querySelectorAll(selector));

/** Retrasa la ejecucion hasta que pasen `espera` ms sin nuevas llamadas. */
export function retrasar(fn, espera = 260) {
    let temporizador;
    return (...args) => {
        clearTimeout(temporizador);
        temporizador = setTimeout(() => fn(...args), espera);
    };
}

/* ==========================================================================
   Formato de datos
   ========================================================================== */

const LOCAL = 'es-CO';

export const formato = {
    /** 15 mar 2026 */
    fecha(valor) {
        if (!valor) return '—';
        const fecha = new Date(valor);
        if (Number.isNaN(fecha.getTime())) return '—';
        return fecha.toLocaleDateString(LOCAL, { day: '2-digit', month: 'short', year: 'numeric' });
    },

    /** 15 mar 2026, 14:32 */
    fechaHora(valor) {
        if (!valor) return '—';
        const fecha = new Date(valor);
        if (Number.isNaN(fecha.getTime())) return '—';
        return fecha.toLocaleString(LOCAL, {
            day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
        });
    },

    /** 14:32 */
    hora(valor) {
        if (!valor) return '—';
        const fecha = new Date(valor);
        if (Number.isNaN(fecha.getTime())) return '—';
        return fecha.toLocaleTimeString(LOCAL, { hour: '2-digit', minute: '2-digit' });
    },

    /**
     * "hace 5 min", "ayer", "hace 3 dias".
     *
     * En una bitacora, saber que algo paso "hace dos minutos" comunica mucho mas rapido que
     * una marca de tiempo completa que hay que restar mentalmente.
     */
    relativo(valor) {
        if (!valor) return '—';
        const fecha = new Date(valor);
        if (Number.isNaN(fecha.getTime())) return '—';
        const segundos = Math.floor((Date.now() - fecha.getTime()) / 1000);
        if (segundos < 60) return 'hace unos segundos';
        const minutos = Math.floor(segundos / 60);
        if (minutos < 60) return `hace ${minutos} min`;
        const horas = Math.floor(minutos / 60);
        if (horas < 24) return `hace ${horas} h`;
        const dias = Math.floor(horas / 24);
        if (dias === 1) return 'ayer';
        if (dias < 30) return `hace ${dias} dias`;
        return formato.fecha(valor);
    },

    /** $ 35.200 — sin decimales, que es como se maneja el peso en la calle. */
    dinero(valor) {
        const numero = Number(valor) || 0;
        return numero.toLocaleString(LOCAL, {
            style: 'currency', currency: 'COP', minimumFractionDigits: 0, maximumFractionDigits: 0,
        });
    },

    numero(valor) {
        return (Number(valor) || 0).toLocaleString(LOCAL);
    },

    decimal(valor, decimales = 1) {
        return (Number(valor) || 0).toLocaleString(LOCAL, {
            minimumFractionDigits: decimales, maximumFractionDigits: decimales,
        });
    },

    porcentaje(valor) {
        return `${formato.decimal(valor, 1)} %`;
    },

    /** 1 h 25 min */
    duracion(minutos) {
        const total = Math.round(Number(minutos) || 0);
        if (total <= 0) return '—';
        if (total < 60) return `${total} min`;
        const horas = Math.floor(total / 60);
        const resto = total % 60;
        return resto === 0 ? `${horas} h` : `${horas} h ${resto} min`;
    },

    kilometros(valor) {
        return `${formato.decimal(valor, 1)} km`;
    },

    /** Fecha de hoy en el formato que espera un <input type="date">. */
    paraInput(fecha = new Date()) {
        const copia = new Date(fecha);
        copia.setMinutes(copia.getMinutes() - copia.getTimezoneOffset());
        return copia.toISOString().slice(0, 10);
    },

    iniciales(nombre) {
        if (!nombre) return '?';
        return nombre.trim().split(/\s+/).slice(0, 2).map((p) => p[0]).join('').toUpperCase();
    },
};

/* ==========================================================================
   Iconos
   ========================================================================== */

/**
 * Trazados SVG de los iconos del sistema.
 *
 * Se usan iconos de trazo dibujados en linea, nunca emojis: un emoji cambia de aspecto
 * segun el sistema operativo, no hereda el color del tema y los lectores de pantalla lo
 * anuncian con nombres desconcertantes.
 */
export const iconos = {
    panel: '<path d="M3 13h8V3H3zM13 21h8V11h-8zM13 3v6h8V3zM3 21h8v-6H3z"/>',
    taxi: '<path d="M5 17h14M6 17v2a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1v-2M21 19v-2M18 17v2a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1M3 17v-4l2-6a2 2 0 0 1 2-1.4h10A2 2 0 0 1 19 7l2 6v4zM3 13h18M9 3h6"/><circle cx="7" cy="14.5" r="1"/><circle cx="17" cy="14.5" r="1"/>',
    conductor: '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
    servicio: '<path d="M12 21s-7-5.2-7-10a7 7 0 1 1 14 0c0 4.8-7 10-7 10Z"/><circle cx="12" cy="11" r="2.5"/>',
    llave: '<circle cx="7.5" cy="15.5" r="5.5"/><path d="m21 2-9.6 9.6M15.5 7.5l3 3L22 7l-3-3"/>',
    intercambio: '<path d="m17 2 4 4-4 4M3 11V9a4 4 0 0 1 4-4h14M7 22l-4-4 4-4M21 13v2a4 4 0 0 1-4 4H3"/>',
    herramienta: '<path d="M14.7 6.3a4.5 4.5 0 0 0 6 6L21 12a9 9 0 1 1-9-9z"/><path d="m9 15 6-6"/>',
    reporte: '<path d="M3 3v18h18"/><path d="m7 15 4-5 3 3 5-7"/>',
    usuarios: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.9M16 3.1a4 4 0 0 1 0 7.8"/>',
    reglas: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6M9 13h6M9 17h4"/>',
    trazabilidad: '<path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><path d="m9 14 2 2 4-4"/>',
    bitacora: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    mas: '<path d="M12 5v14M5 12h14"/>',
    lupa: '<circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/>',
    editar: '<path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.1 2.1 0 0 1 3 3L12 15l-4 1 1-4z"/>',
    ojo: '<path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/>',
    reproducir: '<polygon points="6 4 20 12 6 20 6 4"/>',
    detener: '<rect x="5" y="5" width="14" height="14" rx="2"/>',
    revisar: '<path d="M20 6 9 17l-5-5"/>',
    equis: '<path d="M18 6 6 18M6 6l12 12"/>',
    alerta: '<path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z"/><path d="M12 9v4M12 17h.01"/>',
    informacion: '<circle cx="12" cy="12" r="9"/><path d="M12 16v-4M12 8h.01"/>',
    exito: '<circle cx="12" cy="12" r="9"/><path d="m9 12 2 2 4-4"/>',
    enlace: '<path d="M10 13a5 5 0 0 0 7.5.5l3-3a5 5 0 0 0-7-7l-1.7 1.7"/><path d="M14 11a5 5 0 0 0-7.5-.5l-3 3a5 5 0 0 0 7 7l1.7-1.7"/>',
    descargar: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>',
    calendario: '<rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>',
    inventario: '<path d="M21 8v12a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V8M1 3h22v5H1zM10 12h4"/>',
    reloj: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    dinero: '<circle cx="12" cy="12" r="9"/><path d="M14.5 9a2.5 2.5 0 0 0-2.5-1.5c-1.4 0-2.5.7-2.5 2s1.1 2 2.5 2 2.5.7 2.5 2-1.1 2-2.5 2A2.5 2.5 0 0 1 9.5 15M12 6v12"/>',
    liberar: '<path d="M14.7 6.3a5 5 0 1 0 0 7M21 12H9"/>',
    ruta: '<circle cx="6" cy="19" r="2.5"/><circle cx="18" cy="5" r="2.5"/><path d="M8.5 19H16a3.5 3.5 0 0 0 0-7H8a3.5 3.5 0 0 1 0-7h7.5"/>',
    pasajero: '<circle cx="12" cy="6" r="3"/><path d="M8 21v-6l-2-4h12l-2 4v6M10 21v-4h4v4"/>',
    tarifa: '<path d="M20.6 13.4 13.4 20.6a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8Z"/><circle cx="7.5" cy="7.5" r="1.5"/>',
    papelera: '<path d="M3 6h18M8 6V4h8v2M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6M10 11v6M14 11v6"/>',
    tema: '<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>',
};

/**
 * Devuelve el marcado SVG de un icono.
 *
 * Siempre incluye la clase `icono`, que aporta tamano, trazo y color heredado. Un SVG con
 * `viewBox` pero sin dimensiones se estira hasta llenar su contenedor, asi que dejar el
 * tamano en manos de quien llama es una trampa: basta olvidar una clase para que un icono de
 * dieciocho pixeles ocupe media pantalla. Con la clase base garantizada, las reglas de cada
 * componente solo tienen que ajustar el tamano cuando quieran otro.
 */
export function icono(nombre, clase = '') {
    const trazado = iconos[nombre] || iconos.informacion;
    const clases = clase ? `icono ${clase}` : 'icono';
    return `<svg viewBox="0 0 24 24" class="${esc(clases)}" aria-hidden="true">${trazado}</svg>`;
}

/* ==========================================================================
   Bloques reutilizables
   ========================================================================== */

/**
 * Insignia de estado. Recibe `{ etiqueta, tono }`; los tonos de cada enum del backend
 * estan en `componentes/estados.js`.
 */
export function insignia(estado) {
    if (!estado) return '<span class="insignia insignia--neutro">Sin estado</span>';
    return `<span class="insignia insignia--${esc(estado.tono || 'neutro')}">${esc(estado.etiqueta)}</span>`;
}

export function codigo(texto) {
    return `<span class="etiqueta-codigo dato">${esc(texto)}</span>`;
}

/** Estado vacio con explicacion y, si procede, una accion para salir de el. */
export function vacio({ icono: nombreIcono = 'inventario', titulo, texto, accion = '' }) {
    return `
        <div class="vacio">
            <svg viewBox="0 0 24 24" class="vacio__icono">${iconos[nombreIcono] || iconos.inventario}</svg>
            <p class="vacio__titulo">${esc(titulo)}</p>
            <p class="vacio__texto">${esc(texto)}</p>
            ${accion}
        </div>`;
}

/** Marcador de carga con la forma aproximada del contenido que va a aparecer. */
export function esqueletoTabla(filas = 5, columnas = 5) {
    const celdas = (ancho) => `<td style="padding:.75rem 1rem"><div class="esqueleto" style="width:${ancho}%"></div></td>`;
    const cuerpo = Array.from({ length: filas }, () => {
        const anchos = Array.from({ length: columnas }, () => 45 + Math.round(Math.random() * 45));
        return `<tr>${anchos.map(celdas).join('')}</tr>`;
    }).join('');
    return `<div class="tabla-envoltorio"><table class="tabla"><tbody>${cuerpo}</tbody></table></div>`;
}

export function esqueletoKpi(cantidad = 4) {
    return `<div class="rejilla-kpi">${
        Array.from({ length: cantidad }, () => '<div class="esqueleto esqueleto--kpi"></div>').join('')
    }</div>`;
}

/** Tarjeta de indicador del panel. */
export function tarjetaKpi({ etiqueta, valor, detalle = '', tono = 'informativo', icono: nombreIcono = 'panel' }) {
    return `
        <article class="tarjeta-kpi kpi--${esc(tono)}">
            <div class="tarjeta-kpi__fila">
                <div>
                    <p class="tarjeta-kpi__etiqueta">${esc(etiqueta)}</p>
                    <p class="tarjeta-kpi__valor">${esc(valor)}</p>
                    ${detalle ? `<p class="tarjeta-kpi__detalle">${esc(detalle)}</p>` : ''}
                </div>
                <div class="tarjeta-kpi__icono">${icono(nombreIcono)}</div>
            </div>
        </article>`;
}

/* ==========================================================================
   Avisos emergentes
   ========================================================================== */

const contenedorAvisos = () => document.getElementById('avisos');

/**
 * Muestra un aviso temporal.
 *
 * Los errores duran mas que las confirmaciones: leer "guardado" es inmediato, pero
 * entender por que una regla rechazo la operacion requiere unos segundos mas.
 */
export function aviso(tono, titulo, texto = '', duracion = null) {
    const iconoPorTono = {
        positivo: 'exito', negativo: 'alerta', advertencia: 'alerta', informativo: 'informacion',
    };
    const elemento = crear(`
        <div class="aviso aviso--${esc(tono)}" role="alert">
            <svg viewBox="0 0 24 24" class="aviso__icono">${iconos[iconoPorTono[tono] || 'informacion']}</svg>
            <div class="aviso__contenido">
                <p class="aviso__titulo">${esc(titulo)}</p>
                ${texto ? `<p class="aviso__texto">${esc(texto)}</p>` : ''}
            </div>
            <button class="aviso__cerrar" aria-label="Cerrar aviso">
                <svg viewBox="0 0 24 24">${iconos.equis}</svg>
            </button>
        </div>`);

    const quitar = () => {
        elemento.classList.add('saliendo');
        elemento.addEventListener('animationend', () => elemento.remove(), { once: true });
    };

    elemento.querySelector('.aviso__cerrar').addEventListener('click', quitar);
    contenedorAvisos().appendChild(elemento);

    const espera = duracion ?? (tono === 'negativo' ? 7000 : 3800);
    setTimeout(quitar, espera);
    return elemento;
}

export const avisoExito = (titulo, texto) => aviso('positivo', titulo, texto);
export const avisoError = (titulo, texto) => aviso('negativo', titulo, texto);

/* ==========================================================================
   Ventana modal
   ========================================================================== */

let cerrarModalActual = null;

/**
 * Abre una ventana modal.
 *
 * Se atiende lo que hace falta para que una modal sea usable con teclado: el foco entra
 * al abrir, queda atrapado dentro mientras esta abierta, Escape la cierra y al cerrarse el
 * foco vuelve al elemento que la abrio. Sin esto, quien navega con teclado se pierde
 * detras del dialogo y no puede volver.
 */
export function abrirModal({ titulo, descripcion = '', contenido, acciones = '', ancha = false, alAbrir = null }) {
    cerrarModal();

    const disparador = document.activeElement;
    const capa = document.getElementById('modal');

    capa.innerHTML = `
        <div class="modal__caja${ancha ? ' modal__caja--ancha' : ''}" role="dialog" aria-modal="true"
             aria-labelledby="titulo-modal">
            <header class="modal__cabecera">
                <div>
                    <h2 class="modal__titulo" id="titulo-modal">${esc(titulo)}</h2>
                    ${descripcion ? `<p class="modal__descripcion">${esc(descripcion)}</p>` : ''}
                </div>
                <button class="boton-icono" data-cerrar-modal aria-label="Cerrar">
                    <svg viewBox="0 0 24 24">${iconos.equis}</svg>
                </button>
            </header>
            <div class="modal__cuerpo">${contenido}</div>
            ${acciones ? `<footer class="modal__pie">${acciones}</footer>` : ''}
        </div>`;
    capa.hidden = false;
    document.body.style.overflow = 'hidden';

    const caja = capa.querySelector('.modal__caja');

    const enTeclado = (evento) => {
        if (evento.key === 'Escape') {
            evento.preventDefault();
            cerrarModal();
            return;
        }
        if (evento.key !== 'Tab') return;

        const enfocables = $$(
            'button, [href], input:not([type="hidden"]), select, textarea, [tabindex]:not([tabindex="-1"])',
            caja,
        ).filter((el) => !el.disabled && el.offsetParent !== null);
        if (!enfocables.length) return;

        const primero = enfocables[0];
        const ultimo = enfocables[enfocables.length - 1];
        if (evento.shiftKey && document.activeElement === primero) {
            evento.preventDefault();
            ultimo.focus();
        } else if (!evento.shiftKey && document.activeElement === ultimo) {
            evento.preventDefault();
            primero.focus();
        }
    };

    const enClic = (evento) => {
        if (evento.target === capa) cerrarModal();
        if (evento.target.closest('[data-cerrar-modal]')) cerrarModal();
    };

    document.addEventListener('keydown', enTeclado);
    capa.addEventListener('click', enClic);

    cerrarModalActual = () => {
        document.removeEventListener('keydown', enTeclado);
        capa.removeEventListener('click', enClic);
        capa.hidden = true;
        capa.innerHTML = '';
        document.body.style.overflow = '';
        cerrarModalActual = null;
        if (disparador && disparador.focus) disparador.focus();
    };

    // El foco va al primer campo, no al boton de cerrar: quien abre un formulario quiere
    // empezar a escribir.
    const primerCampo = caja.querySelector('input:not([type="hidden"]), select, textarea');
    (primerCampo || caja.querySelector('button')).focus();

    if (alAbrir) alAbrir(caja);
    return caja;
}

export function cerrarModal() {
    if (cerrarModalActual) cerrarModalActual();
}

/**
 * Dialogo de confirmacion.
 *
 * Devuelve una promesa que resuelve a verdadero o falso, lo que permite escribir
 * `if (await confirmar(...))` en lugar de encadenar retrollamadas.
 */
export function confirmar({ titulo, mensaje, textoConfirmar = 'Confirmar', tono = 'primario', detalle = '' }) {
    return new Promise((resolver) => {
        const caja = abrirModal({
            titulo,
            contenido: `
                <p style="font-size:.875rem;color:var(--texto-medio)">${esc(mensaje)}</p>
                ${detalle ? `<div class="alerta alerta--advertencia" style="margin-top:1rem;margin-bottom:0">
                    <svg viewBox="0 0 24 24" class="alerta__icono">${iconos.alerta}</svg>
                    <div class="alerta__contenido"><p class="alerta__texto">${esc(detalle)}</p></div>
                </div>` : ''}`,
            acciones: `
                <button class="boton boton--fantasma" data-cerrar-modal>Cancelar</button>
                <button class="boton boton--${esc(tono)}" data-confirmar>${esc(textoConfirmar)}</button>`,
        });

        let resuelto = false;
        caja.querySelector('[data-confirmar]').addEventListener('click', () => {
            resuelto = true;
            cerrarModal();
            resolver(true);
        });

        // Si la modal se cierra por Escape, por el velo o por la equis, la respuesta es no.
        const capa = document.getElementById('modal');
        const observador = new MutationObserver(() => {
            if (capa.hidden) {
                observador.disconnect();
                if (!resuelto) resolver(false);
            }
        });
        observador.observe(capa, { attributes: true, attributeFilter: ['hidden'] });
    });
}

/* ==========================================================================
   Formularios
   ========================================================================== */

/** Recoge los valores de un formulario como objeto plano. */
export function datosFormulario(formulario) {
    const datos = {};
    new FormData(formulario).forEach((valor, clave) => {
        datos[clave] = typeof valor === 'string' ? valor.trim() : valor;
    });
    return datos;
}

/** Limpia los mensajes de error de todos los campos. */
export function limpiarErrores(formulario) {
    $$('.campo--invalido', formulario).forEach((campo) => campo.classList.remove('campo--invalido'));
    $$('.campo__error', formulario).forEach((error) => {
        error.hidden = true;
        error.textContent = '';
    });
}

/**
 * Pinta los errores devueltos por la API junto a cada campo.
 *
 * El servidor envia `campos: { placa: "La placa debe tener..." }`, asi que el mensaje que
 * lee el usuario es exactamente el que produjo la validacion del dominio. No hay una
 * segunda copia de las reglas escrita en JavaScript que pueda quedar desincronizada.
 *
 * @returns {boolean} si se pudo situar algun error en un campo concreto
 */
export function mostrarErroresFormulario(formulario, error) {
    limpiarErrores(formulario);
    if (!(error instanceof ErrorApi) || !error.campos) return false;

    let primero = null;
    Object.entries(error.campos).forEach(([nombre, mensaje]) => {
        const control = formulario.elements[nombre];
        if (!control) return;
        const campo = control.closest('.campo');
        if (!campo) return;

        campo.classList.add('campo--invalido');
        let parrafo = campo.querySelector('.campo__error');
        if (!parrafo) {
            parrafo = crear('<p class="campo__error"></p>');
            campo.appendChild(parrafo);
        }
        parrafo.textContent = mensaje;
        parrafo.hidden = false;
        if (!primero) primero = control;
    });

    if (primero) {
        primero.focus();
        primero.scrollIntoView({ block: 'center', behavior: 'smooth' });
        return true;
    }
    return false;
}

/**
 * Traduce cualquier error en un aviso comprensible.
 *
 * Una violacion de regla de negocio se muestra con su codigo ("Regla RN-03") porque eso
 * convierte un rechazo opaco en una explicacion: el usuario entiende que no fallo el
 * sistema, sino que la operacion no estaba permitida y por que.
 */
export function manejarError(error, formulario = null) {
    if (formulario && mostrarErroresFormulario(formulario, error)) {
        avisoError('Revise los datos', 'Hay campos con errores; se senalaron abajo.');
        return;
    }
    if (error instanceof ErrorApi) {
        if (error.esReglaNegocio) {
            aviso('advertencia', 'Regla de negocio', error.message, 8000);
            return;
        }
        if (error.esDuplicado) {
            aviso('advertencia', 'Registro duplicado', error.message);
            return;
        }
        avisoError('No se pudo completar', error.message);
        return;
    }
    console.error(error);
    avisoError('Error inesperado', 'Revise la consola del navegador para mas detalle.');
}

/** Ejecuta una accion asincrona mostrando el boton en estado de carga. */
export async function conCarga(boton, accion) {
    if (!boton) return accion();
    boton.classList.add('cargando');
    boton.disabled = true;
    try {
        return await accion();
    } finally {
        boton.classList.remove('cargando');
        boton.disabled = false;
    }
}
