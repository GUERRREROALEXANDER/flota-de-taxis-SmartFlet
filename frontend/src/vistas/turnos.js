/** Turnos → TurnoControlador (/api/turnos). Un turno une a un conductor con un vehiculo. */

import { turnosApi, vehiculosApi, conductoresApi } from '../api/recursos.js';
import { insignia, formato, confirmar, avisoExito, manejarError } from '../componentes/ui.js';
import { estadoTurno } from '../componentes/estados.js';
import { abrirFormulario } from '../componentes/formulario.js';
import { crearCatalogo } from '../componentes/catalogo.js';
import {
    cargarReferencias, textoVehiculo, textoPersona, opcionesVehiculos, opcionesPersonas,
} from '../componentes/referencias.js';

let refs = { vehiculos: new Map(), conductores: new Map() };

function programar(recargar) {
    const manana = new Date(Date.now() + 86400000);
    return abrirFormulario({
        titulo: 'Programar turno',
        descripcion: 'El conductor debe estar activo y con la licencia vigente; el vehiculo, operativo.',
        textoGuardar: 'Programar',
        campos: [
            { nombre: 'conductorId', etiqueta: 'Conductor', requerido: true, tipo: 'select',
              opciones: async () => opcionesPersonas(await conductoresApi.listar(), 'estadoConductor') },
            { nombre: 'vehiculoId', etiqueta: 'Vehiculo', requerido: true, tipo: 'select',
              opciones: async () => opcionesVehiculos(await vehiculosApi.listar()) },
            { nombre: 'fechaInicio', etiqueta: 'Inicio', requerido: true, tipo: 'datetime-local', valor: `${formato.paraInput()}T06:00` },
            { nombre: 'fechaFin', etiqueta: 'Fin', requerido: true, tipo: 'datetime-local', valor: `${formato.paraInput(manana)}T06:00` },
        ],
        guardar: async (datos) => {
            await turnosApi.programar(datos);
            avisoExito('Turno programado');
            await recargar();
        },
    });
}

async function cambiar(turno, recargar, accion) {
    const textos = {
        iniciar: ['Iniciar turno', 'El vehiculo quedara disponible para recibir viajes.', 'Turno iniciado'],
        finalizar: ['Finalizar turno', 'El vehiculo ya no podra recibir viajes nuevos.', 'Turno finalizado'],
    }[accion];
    const ok = await confirmar({ titulo: textos[0], mensaje: textos[1], textoConfirmar: textos[0] });
    if (!ok) return;
    try {
        await turnosApi[accion](turno.id);
        avisoExito(textos[2]);
        await recargar();
    } catch (error) {
        manejarError(error);
    }
}

export const { render } = crearCatalogo({
    id: 'turnos',
    cargar: async () => {
        [refs] = await Promise.all([cargarReferencias({ vehiculos: true, conductores: true })]);
        const turnos = await turnosApi.listar();
        return turnos.sort((a, b) => String(b.fechaInicio).localeCompare(String(a.fechaInicio)));
    },
    textoNuevo: 'Programar turno',
    alCrear: programar,
    busqueda: {
        placeholder: 'Buscar por conductor o placa…',
        texto: (t) => `${refs.conductores.get(t.conductorId)?.nombre ?? ''} ${refs.vehiculos.get(t.vehiculoId)?.placa ?? ''}`,
    },
    filtros: [
        { valor: '', etiqueta: 'Todos' },
        { valor: 'PROGRAMADO', etiqueta: 'Programados', prueba: (t) => t.estado === 'PROGRAMADO' },
        { valor: 'EN_CURSO', etiqueta: 'En curso', prueba: (t) => t.estado === 'EN_CURSO' },
        { valor: 'FINALIZADO', etiqueta: 'Finalizados', prueba: (t) => t.estado === 'FINALIZADO' },
    ],
    columnas: [
        { titulo: 'Conductor', celda: (t) => `<div class="principal-celda">${textoPersona(refs.conductores, t.conductorId)}</div>` },
        { titulo: 'Vehiculo', celda: (t) => textoVehiculo(refs.vehiculos, t.vehiculoId) },
        { titulo: 'Horario', celda: (t) => `${formato.fechaHora(t.fechaInicio)}<div class="secundaria">hasta ${formato.fechaHora(t.fechaFin)}</div>` },
        { titulo: 'Estado', celda: (t) => insignia(estadoTurno(t.estado)) },
    ],
    acciones: (t) => ({
        PROGRAMADO: [{ accion: 'iniciar', icono: 'reproducir', titulo: 'Iniciar turno' }],
        EN_CURSO: [{ accion: 'finalizar', icono: 'detener', titulo: 'Finalizar turno' }],
    }[t.estado] || []),
    alAccion: {
        iniciar: (t, recargar) => cambiar(t, recargar, 'iniciar'),
        finalizar: (t, recargar) => cambiar(t, recargar, 'finalizar'),
    },
    vacio: { icono: 'reloj', titulo: 'Aun no hay turnos', texto: 'Programe un turno para que un vehiculo pueda atender viajes.' },
});
