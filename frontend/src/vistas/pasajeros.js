/** Pasajeros → PasajeroControlador (/api/pasajeros). */

import { pasajerosApi } from '../api/recursos.js';
import { esc, insignia, formato, avisoExito, manejarError } from '../componentes/ui.js';
import { estadoRegistro } from '../componentes/estados.js';
import { abrirFormulario } from '../componentes/formulario.js';
import { crearCatalogo } from '../componentes/catalogo.js';

function formulario(p, recargar) {
    return abrirFormulario({
        titulo: p ? `Editar ${p.nombre}` : 'Registrar pasajero',
        campos: [
            { nombre: 'identificacion', etiqueta: 'Identificacion', requerido: true, dato: true, valor: p?.identificacion },
            { nombre: 'nombre', etiqueta: 'Nombre completo', requerido: true, valor: p?.nombre },
            { nombre: 'telefono', etiqueta: 'Telefono', requerido: true, tipo: 'tel', placeholder: '+57 300 000 0000', valor: p?.telefono },
            { nombre: 'correoElectronico', etiqueta: 'Correo electronico', requerido: true, tipo: 'email', valor: p?.correoElectronico },
        ],
        guardar: async (datos) => {
            if (p) await pasajerosApi.actualizar(p.id, datos);
            else await pasajerosApi.crear(datos);
            avisoExito(p ? 'Pasajero actualizado' : 'Pasajero registrado', datos.nombre);
            await recargar();
        },
    });
}

export const { render } = crearCatalogo({
    id: 'pasajeros',
    cargar: () => pasajerosApi.listar(),
    textoNuevo: 'Nuevo pasajero',
    alCrear: (recargar) => formulario(null, recargar),
    busqueda: { placeholder: 'Buscar por nombre, identificacion o correo…',
                texto: (p) => `${p.nombre} ${p.identificacion} ${p.correoElectronico} ${p.telefono}` },
    filtros: [
        { valor: '', etiqueta: 'Todos' },
        { valor: 'ACTIVO', etiqueta: 'Activos', prueba: (p) => p.estadoCuenta === 'ACTIVO' },
        { valor: 'INACTIVO', etiqueta: 'Inactivos', prueba: (p) => p.estadoCuenta === 'INACTIVO' },
    ],
    columnas: [
        { titulo: 'Pasajero', celda: (p) => `<div class="principal-celda">${esc(p.nombre)}</div><div class="secundaria dato">${esc(p.identificacion)}</div>` },
        { titulo: 'Contacto', celda: (p) => `<div>${esc(p.telefono)}</div><div class="secundaria">${esc(p.correoElectronico)}</div>` },
        { titulo: 'Registro', celda: (p) => formato.fecha(p.fechaRegistro + 'T00:00') },
        { titulo: 'Cuenta', celda: (p) => insignia(estadoRegistro(p.estadoCuenta)) },
    ],
    acciones: () => [
        { accion: 'estado', icono: 'intercambio', titulo: 'Activar o inactivar' },
        { accion: 'editar', icono: 'editar', titulo: 'Editar' },
    ],
    alAccion: {
        editar: (p, recargar) => formulario(p, recargar),
        estado: async (p, recargar) => {
            const nuevo = p.estadoCuenta === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
            try {
                await pasajerosApi.cambiarEstado(p.id, nuevo);
                avisoExito('Cuenta actualizada', `${p.nombre}: ${estadoRegistro(nuevo).etiqueta}`);
                await recargar();
            } catch (error) {
                manejarError(error);
            }
        },
    },
    vacio: { icono: 'pasajero', titulo: 'Aun no hay pasajeros', texto: 'Los pasajeros registrados pueden solicitar viajes.' },
});
