/**
 * Graficas en SVG generado a mano, sin ninguna libreria.
 *
 * La razon no es purismo: el proyecto se ejecuta sin gestor de dependencias y una politica
 * de origen cruzado podria bloquear un CDN en el equipo donde se evalue. Un SVG construido
 * en el navegador siempre funciona, pesa unos pocos kilobytes, hereda los colores del tema
 * automaticamente (porque usa `currentColor` y las variables CSS) y escala sin perder
 * definicion.
 *
 * Cada grafica lleva `role="img"` y una descripcion textual: un lector de pantalla anuncia
 * el resumen de los datos en vez de saltarse el dibujo.
 */

import { esc, formato } from './ui.js';

/** Traduce el tono semantico del dominio a la variable CSS correspondiente. */
const COLOR_TONO = {
    positivo: 'var(--positivo)',
    informativo: 'var(--informativo)',
    advertencia: 'var(--advertencia)',
    negativo: 'var(--negativo)',
    neutro: 'var(--neutro)',
};

export const colorDeTono = (tono) => COLOR_TONO[tono] || COLOR_TONO.neutro;

/* ==========================================================================
   Grafica de anillo
   ========================================================================== */

/**
 * Anillo de distribucion con leyenda.
 *
 * Se elige un anillo y no un pastel porque el hueco central permite alojar el total, que
 * es el dato que primero se busca. Los segmentos se dibujan con `stroke-dasharray` sobre un
 * unico circulo: es mas corto y preciso que calcular arcos con trigonometria.
 *
 * @param {Array} datos elementos {etiqueta, cantidad, tono, porcentaje}
 */
export function anillo(datos, { titulo = 'Distribucion', textoCentro = 'Total' } = {}) {
    const total = datos.reduce((suma, item) => suma + Number(item.cantidad || 0), 0);
    const radio = 70;
    const circunferencia = 2 * Math.PI * radio;

    if (total === 0) {
        return `<p class="vacio__texto" style="margin:2rem 0">Todavia no hay datos para graficar.</p>`;
    }

    let acumulado = 0;
    const segmentos = datos
        .filter((item) => Number(item.cantidad) > 0)
        .map((item) => {
            const proporcion = Number(item.cantidad) / total;
            const largo = proporcion * circunferencia;
            // El desfase negativo hace que el primer segmento arranque arriba, no a la
            // derecha, que es donde el ojo espera que empiece la lectura.
            const desfase = -acumulado;
            acumulado += largo;
            return `<circle class="anillo__segmento" cx="90" cy="90" r="${radio}"
                        fill="none" stroke="${colorDeTono(item.tono)}" stroke-width="18"
                        stroke-dasharray="${largo.toFixed(2)} ${(circunferencia - largo).toFixed(2)}"
                        stroke-dashoffset="${desfase.toFixed(2)}"
                        transform="rotate(-90 90 90)" stroke-linecap="butt">
                        <title>${esc(item.etiqueta)}: ${item.cantidad} (${formato.decimal(item.porcentaje ?? proporcion * 100, 1)} %)</title>
                    </circle>`;
        })
        .join('');

    const resumen = datos
        .filter((item) => Number(item.cantidad) > 0)
        .map((item) => `${item.etiqueta}: ${item.cantidad}`)
        .join('; ');

    const leyenda = datos
        .map((item) => {
            const porcentaje = item.porcentaje ?? (total ? (item.cantidad / total) * 100 : 0);
            return `
                <div class="leyenda__item">
                    <span class="leyenda__muestra" style="background:${colorDeTono(item.tono)}"></span>
                    <span class="leyenda__etiqueta">${esc(item.etiqueta)}</span>
                    <span class="leyenda__valor dato">${esc(item.cantidad)}</span>
                    <span class="leyenda__porcentaje dato">${formato.decimal(porcentaje, 0)} %</span>
                </div>`;
        })
        .join('');

    return `
        <div class="anillo">
            <svg class="anillo__svg" viewBox="0 0 180 180" width="164" height="164"
                 role="img" aria-label="${esc(titulo)}. ${esc(resumen)}">
                <circle cx="90" cy="90" r="${radio}" fill="none"
                        stroke="var(--borde)" stroke-width="18" opacity="0.45"/>
                ${segmentos}
                <text x="90" y="86" text-anchor="middle" class="anillo__centro-valor">${total}</text>
                <text x="90" y="104" text-anchor="middle" class="anillo__centro-texto">${esc(textoCentro)}</text>
            </svg>
            <div class="leyenda">${leyenda}</div>
        </div>`;
}

/* ==========================================================================
   Serie temporal
   ========================================================================== */

/**
 * Grafica de area con linea y puntos sobre una serie diaria.
 *
 * Detalles que la hacen legible y no solo bonita:
 *   - el eje vertical arranca en cero, de modo que la altura de la curva es proporcional
 *     al valor real y no exagera variaciones pequenas;
 *   - las etiquetas del eje horizontal se reparten para no solaparse cuando el rango es
 *     largo, en lugar de imprimirse todas y volverse ilegibles;
 *   - cada punto lleva un `<title>`, que el navegador muestra como informacion emergente
 *     nativa: no hace falta programar un tooltip.
 *
 * @param {Array} serie elementos {fecha, total, finalizados, ingresos}
 */
export function areaTemporal(serie, { campo = 'total', etiqueta = 'servicios', altura = 220 } = {}) {
    if (!serie || serie.length === 0) {
        return `<p class="vacio__texto" style="margin:2rem 0">No hay actividad en el periodo seleccionado.</p>`;
    }

    const ancho = 720;
    const margen = { arriba: 16, derecha: 16, abajo: 30, izquierda: 44 };
    const anchoUtil = ancho - margen.izquierda - margen.derecha;
    const altoUtil = altura - margen.arriba - margen.abajo;

    const valores = serie.map((punto) => Number(punto[campo]) || 0);
    const maximoReal = Math.max(...valores, 1);
    // Se redondea el techo hacia arriba para que las lineas de referencia caigan en
    // numeros redondos y el eje se lea comodo.
    const paso = Math.max(1, Math.ceil(maximoReal / 4));
    const maximo = paso * 4;

    const x = (indice) => margen.izquierda
        + (serie.length === 1 ? anchoUtil / 2 : (indice / (serie.length - 1)) * anchoUtil);
    const y = (valor) => margen.arriba + altoUtil - (valor / maximo) * altoUtil;

    const puntos = valores.map((valor, indice) => `${x(indice).toFixed(1)},${y(valor).toFixed(1)}`);
    const linea = `M ${puntos.join(' L ')}`;
    const area = `${linea} L ${x(serie.length - 1).toFixed(1)},${(margen.arriba + altoUtil).toFixed(1)} `
        + `L ${x(0).toFixed(1)},${(margen.arriba + altoUtil).toFixed(1)} Z`;

    const referencias = Array.from({ length: 5 }, (_, i) => {
        const valor = i * paso;
        const posicion = y(valor);
        return `<line class="grafica__rejilla" x1="${margen.izquierda}" y1="${posicion}"
                      x2="${ancho - margen.derecha}" y2="${posicion}" opacity="0.5"/>
                <text class="grafica__eje" x="${margen.izquierda - 8}" y="${posicion + 3}"
                      text-anchor="end">${valor}</text>`;
    }).join('');

    // Como maximo siete etiquetas en el eje horizontal: con mas, se pisan entre si.
    const salto = Math.max(1, Math.ceil(serie.length / 7));
    const etiquetasEjeX = serie.map((punto, indice) => {
        if (indice % salto !== 0 && indice !== serie.length - 1) return '';
        const fecha = new Date(`${punto.fecha}T00:00:00`);
        const texto = fecha.toLocaleDateString('es-CO', { day: '2-digit', month: 'short' });
        return `<text class="grafica__eje" x="${x(indice).toFixed(1)}" y="${altura - 8}"
                      text-anchor="middle">${esc(texto)}</text>`;
    }).join('');

    const marcadores = serie.map((punto, indice) => `
        <circle class="grafica__punto" cx="${x(indice).toFixed(1)}" cy="${y(valores[indice]).toFixed(1)}" r="3">
            <title>${esc(formato.fecha(`${punto.fecha}T00:00:00`))}: ${valores[indice]} ${esc(etiqueta)}</title>
        </circle>`).join('');

    const total = valores.reduce((suma, valor) => suma + valor, 0);

    return `
        <svg class="grafica" viewBox="0 0 ${ancho} ${altura}" preserveAspectRatio="xMidYMid meet"
             role="img" aria-label="Evolucion diaria de ${esc(etiqueta)}: ${total} en ${serie.length} dias, maximo ${maximoReal}.">
            <defs>
                <linearGradient id="degradadoArea" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="var(--primario)" stop-opacity="0.28"/>
                    <stop offset="100%" stop-color="var(--primario)" stop-opacity="0"/>
                </linearGradient>
            </defs>
            ${referencias}
            <path class="grafica__area" d="${area}"/>
            <path class="grafica__linea" d="${linea}"/>
            ${marcadores}
            ${etiquetasEjeX}
        </svg>`;
}

/* ==========================================================================
   Barras verticales
   ========================================================================== */

/** Barras verticales para comparar una magnitud a lo largo de una serie corta. */
export function barrasVerticales(serie, { campo = 'total', etiqueta = 'servicios', altura = 200 } = {}) {
    if (!serie || serie.length === 0) {
        return `<p class="vacio__texto" style="margin:2rem 0">No hay datos para graficar.</p>`;
    }

    const ancho = 720;
    const margen = { arriba: 16, derecha: 12, abajo: 28, izquierda: 44 };
    const anchoUtil = ancho - margen.izquierda - margen.derecha;
    const altoUtil = altura - margen.arriba - margen.abajo;

    const valores = serie.map((punto) => Number(punto[campo]) || 0);
    const maximo = Math.max(...valores, 1);
    const anchoRanura = anchoUtil / serie.length;
    const anchoBarra = Math.max(3, Math.min(28, anchoRanura * 0.62));

    const barras = serie.map((punto, indice) => {
        const alto = (valores[indice] / maximo) * altoUtil;
        const posX = margen.izquierda + indice * anchoRanura + (anchoRanura - anchoBarra) / 2;
        const posY = margen.arriba + altoUtil - alto;
        return `<rect class="grafica__barra" x="${posX.toFixed(1)}" y="${posY.toFixed(1)}"
                      width="${anchoBarra.toFixed(1)}" height="${Math.max(alto, 1).toFixed(1)}"
                      rx="3" fill="var(--primario)">
                    <title>${esc(formato.fecha(`${punto.fecha}T00:00:00`))}: ${valores[indice]} ${esc(etiqueta)}</title>
                </rect>`;
    }).join('');

    const paso = Math.max(1, Math.ceil(maximo / 4));
    const referencias = Array.from({ length: 5 }, (_, i) => {
        const valor = i * paso;
        const posicion = margen.arriba + altoUtil - (valor / (paso * 4)) * altoUtil;
        return `<line class="grafica__rejilla" x1="${margen.izquierda}" y1="${posicion}"
                      x2="${ancho - margen.derecha}" y2="${posicion}" opacity="0.5"/>
                <text class="grafica__eje" x="${margen.izquierda - 8}" y="${posicion + 3}"
                      text-anchor="end">${valor}</text>`;
    }).join('');

    return `
        <svg class="grafica" viewBox="0 0 ${ancho} ${altura}" preserveAspectRatio="xMidYMid meet"
             role="img" aria-label="Barras de ${esc(etiqueta)} por dia, maximo ${maximo}.">
            ${referencias}${barras}
        </svg>`;
}

/* ==========================================================================
   Barras horizontales
   ========================================================================== */

/**
 * Barras horizontales para rankings.
 *
 * Con etiquetas largas (nombres de personas) las barras horizontales ganan por goleada a
 * las verticales: el texto se lee de corrido en lugar de girarse cuarenta y cinco grados.
 *
 * @param {Array} items elementos {etiqueta, valor, detalle, tono}
 */
export function barrasHorizontales(items, { formatoValor = (v) => formato.numero(v), maximo = null } = {}) {
    if (!items || items.length === 0) {
        return `<p class="vacio__texto" style="margin:2rem 0">No hay datos para comparar.</p>`;
    }

    const tope = maximo ?? Math.max(...items.map((item) => Number(item.valor) || 0), 1);

    return `<div class="pila pila--compacta">${items.map((item) => {
        const valor = Number(item.valor) || 0;
        const ancho = tope === 0 ? 0 : (valor / tope) * 100;
        return `
            <div>
                <div style="display:flex;justify-content:space-between;gap:1rem;margin-bottom:4px">
                    <span style="font-size:.8125rem;font-weight:500;overflow:hidden;
                                 text-overflow:ellipsis;white-space:nowrap">${esc(item.etiqueta)}</span>
                    <span class="dato" style="font-size:.8125rem;font-weight:600;white-space:nowrap">
                        ${esc(formatoValor(valor))}
                    </span>
                </div>
                <div class="barra-progreso">
                    <div class="barra-progreso__relleno"
                         style="width:${ancho.toFixed(1)}%;background:${colorDeTono(item.tono || 'informativo')}"></div>
                </div>
                ${item.detalle ? `<p style="font-size:.6875rem;color:var(--texto-tenue);margin-top:3px">${esc(item.detalle)}</p>` : ''}
            </div>`;
    }).join('')}</div>`;
}

/* ==========================================================================
   Barra apilada
   ========================================================================== */

/** Barra unica apilada: util para mostrar una composicion en poco espacio vertical. */
export function barraApilada(datos) {
    const total = datos.reduce((suma, item) => suma + Number(item.cantidad || 0), 0);
    if (total === 0) return '';

    const segmentos = datos
        .filter((item) => Number(item.cantidad) > 0)
        .map((item) => {
            const porcentaje = (Number(item.cantidad) / total) * 100;
            return `<div style="width:${porcentaje.toFixed(2)}%;background:${colorDeTono(item.tono)};height:100%"
                         title="${esc(item.etiqueta)}: ${item.cantidad}"></div>`;
        })
        .join('');

    return `<div style="display:flex;height:8px;border-radius:99px;overflow:hidden;background:var(--borde)"
                 role="img" aria-label="Composicion: ${esc(datos.map((d) => `${d.etiqueta} ${d.cantidad}`).join(', '))}">
                ${segmentos}
            </div>`;
}
