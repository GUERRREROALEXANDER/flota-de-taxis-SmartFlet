/** Vehiculos de la flota → VehiculoControlador (/api/vehiculos). */

import { vehiculosApi } from '../api/recursos.js';
import { esc, insignia, confirmar, avisoExito, manejarError } from '../componentes/ui.js';
import { estadoVehiculo } from '../componentes/estados.js';
import { abrirFormulario } from '../componentes/formulario.js';
import { crearCatalogo } from '../componentes/catalogo.js';

function formulario(vehiculo, recargar) {
    return abrirFormulario({
        titulo: vehiculo ? `Editar ${vehiculo.placa}` : 'Registrar vehiculo',
        descripcion: 'La placa no se puede repetir en la flota.',
        campos: [
            { nombre: 'placa', etiqueta: 'Placa', requerido: true, dato: true, maxlength: 7, placeholder: 'ABC123',
              ayuda: 'Formato ABC123 o ABC-123.', valor: vehiculo?.placa, transformar: (v) => v.toUpperCase() },
            { nombre: 'modelo', etiqueta: 'Modelo', requerido: true, placeholder: 'Chevrolet Spark', valor: vehiculo?.modelo },
            { nombre: 'tipo', etiqueta: 'Tipo', requerido: true, tipo: 'select', valor: vehiculo?.tipo,
              opciones: [{ valor: 'Sedan', texto: 'Sedan' }, { valor: 'Hatchback', texto: 'Hatchback' },
                         { valor: 'Camioneta', texto: 'Camioneta' }, { valor: 'Van', texto: 'Van' }] },
            { nombre: 'capacidad', etiqueta: 'Capacidad (pasajeros)', requerido: true, tipo: 'number', min: 1, max: 8,
              valor: vehiculo?.capacidad ?? 4 },
            { nombre: 'estadoOperativo', etiqueta: 'Estado', tipo: 'checkbox', textoCasilla: 'Operativo',
              valor: vehiculo ? vehiculo.estadoOperativo : true, ancho: 'completo' },
        ],
        guardar: async (datos) => {
            if (vehiculo) await vehiculosApi.actualizar(vehiculo.id, datos);
            else await vehiculosApi.crear(datos);
            avisoExito(vehiculo ? 'Vehiculo actualizado' : 'Vehiculo registrado', datos.placa);
            await recargar();
        },
    });
}

export const { render } = crearCatalogo({
    id: 'vehiculos',
    cargar: () => vehiculosApi.listar(),
    textoNuevo: 'Nuevo vehiculo',
    alCrear: (recargar) => formulario(null, recargar),
    busqueda: { placeholder: 'Buscar por placa, modelo o tipo…', texto: (v) => `${v.placa} ${v.modelo} ${v.tipo}` },
    filtros: [
        { valor: '', etiqueta: 'Todos' },
        { valor: 'si', etiqueta: 'Operativos', prueba: (v) => v.estadoOperativo },
        { valor: 'no', etiqueta: 'Fuera de servicio', prueba: (v) => !v.estadoOperativo },
    ],
    columnas: [
        { titulo: 'Placa', celda: (v) => `<span class="dato" style="font-weight:600">${esc(v.placa)}</span>` },
        { titulo: 'Vehiculo', celda: (v) => `<div class="principal-celda">${esc(v.modelo)}</div><div class="secundaria">${esc(v.tipo)}</div>` },
        { titulo: 'Capacidad', clase: 'numerico', celda: (v) => `${esc(v.capacidad)} pasajeros` },
        { titulo: 'Estado', celda: (v) => insignia(estadoVehiculo(v.estadoOperativo)) },
    ],
    acciones: () => [
        { accion: 'estado', icono: 'intercambio', titulo: 'Cambiar estado operativo' },
        { accion: 'editar', icono: 'editar', titulo: 'Editar' },
        { accion: 'eliminar', icono: 'papelera', titulo: 'Eliminar' },
    ],
    alAccion: {
        editar: (v, recargar) => formulario(v, recargar),
        estado: async (v, recargar) => {
            try {
                await vehiculosApi.cambiarEstado(v.id, !v.estadoOperativo);
                avisoExito('Estado actualizado', `${v.placa}: ${estadoVehiculo(!v.estadoOperativo).etiqueta}`);
                await recargar();
            } catch (error) {
                manejarError(error);
            }
        },
        eliminar: async (v, recargar) => {
            const ok = await confirmar({
                titulo: `Eliminar ${v.placa}`,
                mensaje: 'El vehiculo se borra de la flota.',
                detalle: 'El backend no lo permite si tiene turnos o viajes registrados.',
                textoConfirmar: 'Eliminar', tono: 'peligro',
            });
            if (!ok) return;
            try {
                await vehiculosApi.eliminar(v.id);
                avisoExito('Vehiculo eliminado', v.placa);
                await recargar();
            } catch (error) {
                manejarError(error);
            }
        },
    },
    vacio: { icono: 'taxi', titulo: 'Aun no hay vehiculos', texto: 'Registre el primer vehiculo para empezar a operar la flota.' },
});
