/** Tema claro/oscuro, menu lateral en movil y atajos de teclado. */

import { $ } from '../componentes/ui.js';
import { recargarVista } from './enrutador.js';

const CLAVE_TEMA = 'smartflet.tema';

function leerTema() {
    try {
        return localStorage.getItem(CLAVE_TEMA);
    } catch {
        return null;
    }
}

export function aplicarTemaGuardado() {
    const prefiereClaro = window.matchMedia('(prefers-color-scheme: light)').matches;
    fijarTema(leerTema() || (prefiereClaro ? 'claro' : 'oscuro'));
}

function fijarTema(tema) {
    document.documentElement.dataset.tema = tema;
    $('#etiqueta-tema').textContent = tema === 'oscuro' ? 'Tema claro' : 'Tema oscuro';
}

function alternarTema() {
    const nuevo = document.documentElement.dataset.tema === 'oscuro' ? 'claro' : 'oscuro';
    fijarTema(nuevo);
    try {
        localStorage.setItem(CLAVE_TEMA, nuevo);
    } catch { /* sin almacenamiento el tema solo dura esta visita */ }
}

function alternarLateral(abrir) {
    $('#lateral').classList.toggle('abierto', abrir);
    $('#velo').hidden = !abrir;
}

export function conectarInterfaz() {
    $('#cambiar-tema').addEventListener('click', alternarTema);
    $('#abrir-lateral').addEventListener('click', () => alternarLateral(true));
    $('#cerrar-lateral').addEventListener('click', () => alternarLateral(false));
    $('#velo').addEventListener('click', () => alternarLateral(false));
    $('#navegacion').addEventListener('click', (e) => {
        if (e.target.closest('.enlace-navegacion')) alternarLateral(false);
    });
    $('#boton-actualizar').addEventListener('click', recargarVista);

    // "/" enfoca la busqueda y "r" recarga, fuera de los campos de texto.
    document.addEventListener('keydown', (evento) => {
        const enCampo = /^(INPUT|TEXTAREA|SELECT)$/.test(evento.target.tagName);
        if (enCampo || evento.ctrlKey || evento.metaKey || evento.altKey || !$('#modal').hidden) return;
        if (evento.key === '/') {
            const busqueda = $('.herramientas__busqueda .campo__control');
            if (busqueda) {
                evento.preventDefault();
                busqueda.focus();
            }
        } else if (evento.key === 'r' || evento.key === 'R') {
            evento.preventDefault();
            recargarVista();
        }
    });
}
