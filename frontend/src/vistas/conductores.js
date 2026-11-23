/** Conductores → ConductorControlador (/api/conductores). */

import { conductoresApi } from '../api/recursos.js';
import { esc, insignia, formato, avisoExito, manejarError } from '../componentes/ui.js';
import { estadoRegistro } from '../componentes/estados.js';
import { abrirFormulario } from '../componentes/formulario.js';
import { crearCatalogo } from '../componentes/catalogo.js';

const DIAS_AVISO = 30;

/** Dias que faltan para que venza la licencia (negativo si ya vencio). */
function diasParaVencer(fecha) {
    const hoy = new Date(formato.paraInput());
    return Math.round((new Date(fecha) - hoy) / 86400000);
}

function licencia(c) {
    const dias = diasParaVencer(c.fechaVencimientoLicencia);
    const estado = dias < 0
        ? { etiqueta: 'Vencida', tono: 'negativo' }
        : dias <= DIAS_AVISO ? { etiqueta: `Vence en ${dias} dias`, tono: 'advertencia' } : null;
    return `<div class="dato">${esc(c.numeroLicencia)}</div>
            <div class="secundaria">Vence ${formato.fecha(c.fechaVencimientoLicencia + 'T00:00')}</div>
            ${estado ? insignia(estado) : ''}`;
}

function formulario(c, recargar) {
    return abrirFormulario({
        titulo: c ? `Editar ${c.nombre}` : 'Registrar conductor',
        descripcion: 'Un conductor con la licencia vencida no puede iniciar turnos.',
        campos: [
            { nombre: 'identificacion', etiqueta: 'Identificacion', requerido: true, dato: true, valor: c?.identificacion },
            { nombre: 'nombre', etiqueta: 'Nombre completo', requerido: true, valor: c?.nombre },
            { nombre: 'numeroLicencia', etiqueta: 'Numero de licencia', requerido: true, dato: true, valor: c?.numeroLicencia },
            { nombre: 'fechaVencimientoLicencia', etiqueta: 'Vencimiento de la licencia', requerido: true, tipo: 'date',
              valor: c?.fechaVencimientoLicencia },
        ],
        guardar: async (datos) => {
            if (c) await conductoresApi.actualizar(c.id, datos);
            else await conductoresApi.crear(datos);
            avisoExito(c ? 'Conductor actualizado' : 'Conductor registrado', datos.nombre);
            await recargar();
        },
    });
}

export const { render } = crearCatalogo({
    id: 'conductores',
    cargar: () => conductoresApi.listar(),
    textoNuevo: 'Nuevo conductor',
    alCrear: (recargar) => formulario(null, recargar),
    busqueda: { placeholder: 'Buscar por nombre, identificacion o licencia…',
                texto: (c) => `${c.nombre} ${c.identificacion} ${c.numeroLicencia}` },
    filtros: [
        { valor: '', etiqueta: 'Todos' },
        { valor: 'ACTIVO', etiqueta: 'Activos', prueba: (c) => c.estadoConductor === 'ACTIVO' },
        { valor: 'INACTIVO', etiqueta: 'Inactivos', prueba: (c) => c.estadoConductor === 'INACTIVO' },
        { valor: 'licencia', etiqueta: 'Licencia por vencer',
          prueba: (c) => diasParaVencer(c.fechaVencimientoLicencia) <= DIAS_AVISO },
    ],
    columnas: [
        { titulo: 'Conductor', celda: (c) => `<div class="principal-celda">${esc(c.nombre)}</div><div class="secundaria dato">${esc(c.identificacion)}</div>` },
        { titulo: 'Licencia', celda: licencia },
        { titulo: 'Estado', celda: (c) => insignia(estadoRegistro(c.estadoConductor)) },
    ],
    acciones: () => [
        { accion: 'estado', icono: 'intercambio', titulo: 'Activar o inactivar' },
        { accion: 'editar', icono: 'editar', titulo: 'Editar' },
    ],
    alAccion: {
        editar: (c, recargar) => formulario(c, recargar),
        estado: async (c, recargar) => {
            const nuevo = c.estadoConductor === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
            try {
                await conductoresApi.cambiarEstado(c.id, nuevo);
                avisoExito('Estado actualizado', `${c.nombre}: ${estadoRegistro(nuevo).etiqueta}`);
                await recargar();
            } catch (error) {
                manejarError(error);
            }
        },
    },
    vacio: { icono: 'conductor', titulo: 'Aun no hay conductores', texto: 'Registre conductores para poder programar turnos.' },
});
