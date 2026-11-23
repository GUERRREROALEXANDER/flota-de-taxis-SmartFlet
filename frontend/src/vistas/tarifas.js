/** Tarifas → TarifaControlador (/api/tarifas). */

import { tarifasApi } from '../api/recursos.js';
import { esc, insignia, formato, confirmar, avisoExito, manejarError } from '../componentes/ui.js';
import { estadoRegistro } from '../componentes/estados.js';
import { abrirFormulario } from '../componentes/formulario.js';
import { crearCatalogo } from '../componentes/catalogo.js';

const fecha = (valor) => (valor ? formato.fecha(valor + 'T00:00') : 'Sin fecha fin');

function formulario(t, recargar) {
    return abrirFormulario({
        titulo: t ? `Editar ${t.nombre}` : 'Nueva tarifa',
        descripcion: 'Precio del viaje = valor base + valor por km × distancia. Cada viaje guarda una copia de la tarifa vigente.',
        campos: [
            { nombre: 'nombre', etiqueta: 'Nombre', requerido: true, placeholder: 'Diurna', valor: t?.nombre, ancho: 'completo' },
            { nombre: 'valorBase', etiqueta: 'Valor base (COP)', requerido: true, tipo: 'number', min: 0, step: 100, valor: t?.valorBase },
            { nombre: 'valorPorKm', etiqueta: 'Valor por km (COP)', requerido: true, tipo: 'number', min: 1, step: 50, valor: t?.valorPorKm },
            { nombre: 'fechaInicioVigencia', etiqueta: 'Inicio de vigencia', requerido: true, tipo: 'date',
              valor: t?.fechaInicioVigencia ?? formato.paraInput() },
            { nombre: 'fechaFinVigencia', etiqueta: 'Fin de vigencia', tipo: 'date', valor: t?.fechaFinVigencia,
              ayuda: 'Opcional. Vacio = sin fecha de fin.' },
        ],
        guardar: async (datos) => {
            if (t) await tarifasApi.actualizar(t.id, datos);
            else await tarifasApi.crear(datos);
            avisoExito(t ? 'Tarifa actualizada' : 'Tarifa creada', datos.nombre);
            await recargar();
        },
    });
}

export const { render } = crearCatalogo({
    id: 'tarifas',
    cargar: async () => {
        const [tarifas, vigente] = await Promise.all([tarifasApi.listar(), tarifasApi.vigente().catch(() => null)]);
        return tarifas.map((t) => ({ ...t, esVigente: t.id === vigente?.id }));
    },
    textoNuevo: 'Nueva tarifa',
    alCrear: (recargar) => formulario(null, recargar),
    filtros: [
        { valor: '', etiqueta: 'Todas' },
        { valor: 'ACTIVO', etiqueta: 'Activas', prueba: (t) => t.estado === 'ACTIVO' },
        { valor: 'INACTIVO', etiqueta: 'Inactivas', prueba: (t) => t.estado === 'INACTIVO' },
    ],
    columnas: [
        { titulo: 'Tarifa', celda: (t) => `<div class="principal-celda">${esc(t.nombre)}</div>
            ${t.esVigente ? '<span class="insignia insignia--positivo">Vigente hoy</span>' : ''}` },
        { titulo: 'Valor base', clase: 'numerico', celda: (t) => formato.dinero(t.valorBase) },
        { titulo: 'Por km', clase: 'numerico', celda: (t) => formato.dinero(t.valorPorKm) },
        { titulo: 'Vigencia', celda: (t) => `Desde ${fecha(t.fechaInicioVigencia)}<div class="secundaria">${
            t.fechaFinVigencia ? `hasta ${fecha(t.fechaFinVigencia)}` : 'sin fecha de fin'}</div>` },
        { titulo: 'Estado', celda: (t) => insignia(estadoRegistro(t.estado)) },
    ],
    acciones: (t) => [
        { accion: 'editar', icono: 'editar', titulo: 'Editar' },
        ...(t.estado === 'ACTIVO' ? [{ accion: 'desactivar', icono: 'equis', titulo: 'Desactivar' }] : []),
    ],
    alAccion: {
        editar: (t, recargar) => formulario(t, recargar),
        desactivar: async (t, recargar) => {
            const ok = await confirmar({
                titulo: `Desactivar ${t.nombre}`, mensaje: 'Los viajes nuevos ya no podran usar esta tarifa.',
                textoConfirmar: 'Desactivar', tono: 'peligro',
            });
            if (!ok) return;
            try {
                await tarifasApi.desactivar(t.id);
                avisoExito('Tarifa desactivada', t.nombre);
                await recargar();
            } catch (error) {
                manejarError(error);
            }
        },
    },
    vacio: { icono: 'tarifa', titulo: 'Aun no hay tarifas', texto: 'Sin una tarifa vigente no se pueden solicitar viajes.' },
});
