/**
 * Quien tiene la sesion abierta y que puede hacer.
 *
 * El backend es quien de verdad protege los datos (ConfiguracionSeguridad.java). Lo que hay
 * aqui solo sirve para no mostrar menus que el usuario no puede usar; si alguien los forzara,
 * el backend respondera 403 igualmente.
 */

import { autenticacionApi } from '../api/recursos.js';

let usuarioActual = null;

export const sesion = {
    /** { usuario, nombre, rol } o null */
    get usuario() {
        return usuarioActual;
    },
    get esAdministrador() {
        return usuarioActual?.rol === 'ADMINISTRADOR';
    },
};

/** Pregunta al backend si la cookie de sesion sigue valida. Devuelve el usuario o null. */
export async function recuperarSesion() {
    try {
        usuarioActual = await autenticacionApi.sesionActual();
    } catch {
        usuarioActual = null;
    }
    return usuarioActual;
}

export async function iniciarSesion(usuario, clave) {
    usuarioActual = await autenticacionApi.iniciarSesion(usuario, clave);
    return usuarioActual;
}

export async function cerrarSesion() {
    try {
        await autenticacionApi.cerrarSesion();
    } finally {
        usuarioActual = null;
    }
}

/** Iniciales para el avatar: "Diego Guerrero" → "DG". */
export function iniciales(nombre = '') {
    return nombre.trim().split(/\s+/).slice(0, 2).map((p) => p[0]?.toUpperCase() ?? '').join('') || '?';
}

export const nombreRol = (rol) => ({ ADMINISTRADOR: 'Administrador', OPERADOR: 'Operador de central' }[rol] ?? rol);
