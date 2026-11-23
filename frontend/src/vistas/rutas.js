/** Rutas frecuentes → RutaControlador (/api/rutas). */

import { rutasApi } from '../api/recursos.js';
import { esc, formato, confirmar, avisoExito, manejarError } from '../componentes/ui.js';
import { abrirFormulario } from '../componentes/formulario.js';
import { crearCatalogo } from '../componentes/catalogo.js';

function formulario(r, recargar) {
    return abrirFormulario({
        titulo: r ? 'Editar ruta' : 'Registrar ruta',
        campos: [
            { nombre: 'origen', etiqueta: 'Origen', requerido: true, placeholder: 'Terminal del Norte', valor: r?.origen },
            { nombre: 'destino', etiqueta: 'Destino', requerido: true, placeholder: 'Aeropuerto', valor: r?.destino },
            { nombre: 'distanciaKm', etiqueta: 'Distancia (km)', requerido: true, tipo: 'number', step: '0.1', min: 0.1, valor: r?.distanciaKm },
            { nombre: 'duracionEstimadaMinutos', etiqueta: 'Duracion estimada (min)', requerido: true, tipo: 'number',
              min: 1, max: 1440, valor: r?.duracionEstimadaMinutos },
        ],
        guardar: async (datos) => {
            if (r) await rutasApi.actualizar(r.id, datos);
            else await rutasApi.crear(datos);
            avisoExito(r ? 'Ruta actualizada' : 'Ruta registrada', `${datos.origen} → ${datos.destino}`);
            await recargar();
        },
    });
}

export const { render } = crearCatalogo({
    id: 'rutas',
    cargar: () => rutasApi.listar(),
    textoNuevo: 'Nueva ruta',
    alCrear: (recargar) => formulario(null, recargar),
    busqueda: { placeholder: 'Buscar por origen o destino…', texto: (r) => `${r.origen} ${r.destino}` },
    columnas: [
        { titulo: 'Recorrido', celda: (r) => `<div class="principal-celda">${esc(r.origen)} → ${esc(r.destino)}</div>` },
        { titulo: 'Distancia', clase: 'numerico', celda: (r) => formato.kilometros(r.distanciaKm) },
        { titulo: 'Duracion', clase: 'numerico', celda: (r) => formato.duracion(r.duracionEstimadaMinutos) },
    ],
    acciones: () => [
        { accion: 'editar', icono: 'editar', titulo: 'Editar' },
        { accion: 'eliminar', icono: 'papelera', titulo: 'Eliminar' },
    ],
    alAccion: {
        editar: (r, recargar) => formulario(r, recargar),
        eliminar: async (r, recargar) => {
            const ok = await confirmar({
                titulo: 'Eliminar ruta', mensaje: `${r.origen} → ${r.destino}`, textoConfirmar: 'Eliminar', tono: 'peligro',
            });
            if (!ok) return;
            try {
                await rutasApi.eliminar(r.id);
                avisoExito('Ruta eliminada');
                await recargar();
            } catch (error) {
                manejarError(error);
            }
        },
    },
    vacio: { icono: 'ruta', titulo: 'Aun no hay rutas', texto: 'Registre los trayectos frecuentes de la flota.' },
});
