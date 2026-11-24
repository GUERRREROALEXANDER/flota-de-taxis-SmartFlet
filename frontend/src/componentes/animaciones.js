/**
 * Animaciones que necesitan JavaScript. Las demas (entradas, barras, linea de la grafica)
 * son CSS puro en estilos/movimiento.css.
 *
 * Se llama a `animarContenido(contenedor)` despues de pintar una vista (lo hace el enrutador),
 * asi ninguna vista tiene que acordarse de animar nada.
 *
 * Si el usuario pidio "reducir movimiento" en su sistema operativo, todo se muestra
 * directamente con su valor final.
 */

import { formato } from './ui.js';

const reducirMovimiento = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches;

/** Curva de salida suave: arranca rapido y frena al llegar. */
const suavizar = (t) => 1 - Math.pow(1 - t, 3);

/**
 * Cifras que cuentan desde 0 hasta su valor: `<p data-contar="1250000" data-formato="dinero">`.
 */
export function contarCifras(raiz = document) {
    const elementos = raiz.querySelectorAll('[data-contar]:not([data-contado])');
    elementos.forEach((el, i) => {
        el.dataset.contado = '1';
        const destino = Number(el.dataset.contar);
        const escribir = (v) => {
            el.textContent = el.dataset.formato === 'dinero' ? formato.dinero(Math.round(v)) : formato.numero(Math.round(v));
        };
        if (reducirMovimiento() || !destino) {
            escribir(destino);
            return;
        }
        const duracion = 900 + i * 90;
        const inicio = performance.now();
        escribir(0);
        const paso = (ahora) => {
            const t = Math.min(1, (ahora - inicio) / duracion);
            escribir(destino * suavizar(t));
            if (t < 1) requestAnimationFrame(paso);
        };
        requestAnimationFrame(paso);
    });
}

/**
 * Segmentos del anillo: cada uno "crece" alrededor del circulo, uno tras otro.
 * Se guarda el trazo final, se arranca en 0 y la transicion CSS lo lleva a su largo real.
 */
export function animarAnillos(raiz = document) {
    if (reducirMovimiento()) return;
    raiz.querySelectorAll('.anillo__segmento:not([data-animado])').forEach((seg, i) => {
        seg.dataset.animado = '1';
        const final = seg.getAttribute('stroke-dasharray');
        const [largo, resto] = final.split(' ').map(Number);
        seg.style.transition = 'none';
        seg.setAttribute('stroke-dasharray', `0 ${largo + resto}`);
        // Dos cuadros: el primero aplica el 0, el segundo dispara la transicion.
        requestAnimationFrame(() => requestAnimationFrame(() => {
            seg.style.transition = `stroke-dasharray 900ms cubic-bezier(0.22, 1, 0.36, 1) ${150 + i * 120}ms`;
            seg.setAttribute('stroke-dasharray', final);
        }));
    });
}

/**
 * Efecto de luz que sigue al cursor sobre las tarjetas (usa las variables --x y --y en CSS).
 * Se registra una sola vez para todo el documento.
 */
let brilloConectado = false;
export function conectarBrilloTarjetas() {
    if (brilloConectado || reducirMovimiento()) return;
    brilloConectado = true;
    document.addEventListener('pointermove', (e) => {
        const tarjeta = e.target.closest?.('.tarjeta-kpi, .tarjeta');
        if (!tarjeta) return;
        const caja = tarjeta.getBoundingClientRect();
        tarjeta.style.setProperty('--x', `${e.clientX - caja.left}px`);
        tarjeta.style.setProperty('--y', `${e.clientY - caja.top}px`);
    }, { passive: true });
}

/** Todo lo anterior sobre una vista recien pintada. */
export function animarContenido(raiz) {
    contarCifras(raiz);
    animarAnillos(raiz);
}
