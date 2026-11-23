/**
 * Viajes → ViajeControlador (/api/viajes).
 *
 * Ciclo: SOLICITADO → ASIGNADO → EN_CURSO → FINALIZADO, o CANCELADO antes de empezar.
 * Cada fila solo ofrece las acciones que el estado permite; si aun asi el backend rechaza
 * algo (por ejemplo, un vehiculo sin turno en curso), se muestra su mensaje.
 */

import { viajesApi, pasajerosApi, vehiculosApi, turnosApi } from '../api/recursos.js';
import { esc, insignia, formato, abrirModal, confirmar, avisoExito, manejarError } from '../componentes/ui.js';
import { estadoViaje, estadoPago, metodoPago, ESTADOS_VIAJE, METODOS_PAGO } from '../componentes/estados.js';
import { abrirFormulario } from '../componentes/formulario.js';
import { crearCatalogo } from '../componentes/catalogo.js';
import { cargarReferencias, textoVehiculo, textoPersona, opcionesPersonas } from '../componentes/referencias.js';

let refs = { vehiculos: new Map(), pasajeros: new Map() };

function solicitar(recargar) {
    return abrirFormulario({
        titulo: 'Solicitar viaje',
        descripcion: 'El viaje toma la tarifa vigente de hoy y queda en estado Solicitado.',
        textoGuardar: 'Solicitar',
        campos: [
            { nombre: 'pasajeroId', etiqueta: 'Pasajero', requerido: true, tipo: 'select', ancho: 'completo',
              opciones: async () => opcionesPersonas(await pasajerosApi.listar(), 'estadoCuenta') },
            { nombre: 'origen', etiqueta: 'Origen', requerido: true, placeholder: 'Cra 7 # 72-10' },
            { nombre: 'destino', etiqueta: 'Destino', requerido: true, placeholder: 'Aeropuerto El Dorado' },
        ],
        guardar: async (datos) => {
            await viajesApi.solicitar(datos);
            avisoExito('Viaje solicitado', `${datos.origen} → ${datos.destino}`);
            await recargar();
        },
    });
}

/** Solo se ofrecen vehiculos operativos con un turno en curso, que es lo que exige el backend. */
async function vehiculosDisponibles() {
    const [vehiculos, turnos] = await Promise.all([vehiculosApi.listar(true), turnosApi.listar()]);
    const enTurno = new Set(turnos.filter((t) => t.estado === 'EN_CURSO').map((t) => t.vehiculoId));
    return vehiculos
        .filter((v) => enTurno.has(v.id))
        .map((v) => ({ valor: v.id, texto: `${v.placa} · ${v.modelo} (${v.capacidad} pasajeros)` }));
}

function asignar(viaje, recargar) {
    return abrirFormulario({
        titulo: 'Asignar vehiculo',
        descripcion: `${viaje.origen} → ${viaje.destino}. Solo aparecen vehiculos operativos con turno en curso.`,
        textoGuardar: 'Asignar',
        campos: [
            { nombre: 'vehiculoId', etiqueta: 'Vehiculo', requerido: true, tipo: 'select', ancho: 'completo',
              vacio: 'Seleccione un vehiculo en turno…', opciones: vehiculosDisponibles },
        ],
        guardar: async ({ vehiculoId }) => {
            await viajesApi.asignar(viaje.id, vehiculoId);
            avisoExito('Vehiculo asignado');
            await recargar();
        },
    });
}

function finalizar(viaje, recargar) {
    const estimado = (km) => Number(viaje.tarifaValorBase) + Number(viaje.tarifaValorPorKm) * km;
    return abrirFormulario({
        titulo: 'Finalizar viaje',
        descripcion: `Tarifa del viaje: ${formato.dinero(viaje.tarifaValorBase)} + ${formato.dinero(viaje.tarifaValorPorKm)} por km. Al finalizar se crea el pago.`,
        textoGuardar: 'Finalizar y cobrar',
        campos: [
            { nombre: 'distanciaKm', etiqueta: 'Distancia recorrida (km)', requerido: true, tipo: 'number', step: '0.1', min: 0.1 },
            { nombre: 'metodoPago', etiqueta: 'Metodo de pago', requerido: true, tipo: 'select', opciones: METODOS_PAGO },
        ],
        guardar: async ({ distanciaKm, metodoPago: metodo }) => {
            const resultado = await viajesApi.finalizar(viaje.id, distanciaKm, metodo);
            avisoExito('Viaje finalizado', `Cobrado: ${formato.dinero(resultado?.pago?.monto ?? estimado(distanciaKm || 0))}`);
            await recargar();
        },
    });
}

async function cambiarEstado(viaje, recargar, accion) {
    const textos = {
        iniciar: ['Iniciar viaje', 'El conductor recogio al pasajero.', 'primario', 'Viaje en curso'],
        cancelar: ['Cancelar viaje', 'Solo se puede cancelar antes de que inicie.', 'peligro', 'Viaje cancelado'],
    }[accion];
    const ok = await confirmar({ titulo: textos[0], mensaje: textos[1], textoConfirmar: textos[0], tono: textos[2] });
    if (!ok) return;
    try {
        await viajesApi[accion](viaje.id);
        avisoExito(textos[3]);
        await recargar();
    } catch (error) {
        manejarError(error);
    }
}

function ficha(v) {
    const fila = (termino, valor) => `<div><dt class="definicion__termino">${esc(termino)}</dt><dd class="definicion__valor">${valor}</dd></div>`;
    abrirModal({
        titulo: `${v.origen} → ${v.destino}`,
        descripcion: `Solicitado ${formato.fechaHora(v.fechaSolicitud)}`,
        ancha: true,
        contenido: `
            <dl class="definiciones">
                ${fila('Estado', insignia(estadoViaje(v.estadoViaje)))}
                ${fila('Pasajero', textoPersona(refs.pasajeros, v.pasajeroId))}
                ${fila('Vehiculo', textoVehiculo(refs.vehiculos, v.vehiculoId))}
                ${fila('Inicio', formato.fechaHora(v.fechaInicio))}
                ${fila('Fin', formato.fechaHora(v.fechaFin))}
                ${fila('Distancia', v.distanciaKm ? formato.kilometros(v.distanciaKm) : '—')}
                ${fila('Tarifa (copia)', `${formato.dinero(v.tarifaValorBase)} + ${formato.dinero(v.tarifaValorPorKm)}/km`)}
                ${fila('Pago', v.pago
                    ? `${formato.dinero(v.pago.monto)} · ${esc(metodoPago(v.pago.metodoPago))} ${insignia(estadoPago(v.pago.estadoPago))}`
                    : 'Se genera al finalizar')}
            </dl>`,
        acciones: '<button class="boton boton--secundario" data-cerrar-modal>Cerrar</button>',
    });
}

export const { render } = crearCatalogo({
    id: 'viajes',
    cargar: async () => {
        [refs] = await Promise.all([cargarReferencias({ vehiculos: true, pasajeros: true })]);
        const viajes = await viajesApi.listar();
        return viajes.sort((a, b) => String(b.fechaSolicitud).localeCompare(String(a.fechaSolicitud)));
    },
    textoNuevo: 'Solicitar viaje',
    alCrear: solicitar,
    busqueda: {
        placeholder: 'Buscar por origen, destino, pasajero o placa…',
        texto: (v) => `${v.origen} ${v.destino} ${refs.pasajeros.get(v.pasajeroId)?.nombre ?? ''} ${refs.vehiculos.get(v.vehiculoId)?.placa ?? ''}`,
    },
    filtros: [
        { valor: '', etiqueta: 'Todos' },
        ...ESTADOS_VIAJE.map((e) => ({ valor: e, etiqueta: estadoViaje(e).etiqueta, prueba: (v) => v.estadoViaje === e })),
    ],
    columnas: [
        { titulo: 'Recorrido', celda: (v) => `<div class="principal-celda">${esc(v.origen)} → ${esc(v.destino)}</div>
            <div class="secundaria">${formato.relativo(v.fechaSolicitud)}</div>` },
        { titulo: 'Pasajero', celda: (v) => textoPersona(refs.pasajeros, v.pasajeroId) },
        { titulo: 'Vehiculo', celda: (v) => textoVehiculo(refs.vehiculos, v.vehiculoId) },
        { titulo: 'Estado', celda: (v) => insignia(estadoViaje(v.estadoViaje)) },
        { titulo: 'Valor', clase: 'numerico', celda: (v) => (v.pago ? formato.dinero(v.pago.monto) : '—') },
    ],
    acciones: (v) => [
        { accion: 'ver', icono: 'ojo', titulo: 'Ver detalle' },
        ...({
            SOLICITADO: [{ accion: 'asignar', icono: 'taxi', titulo: 'Asignar vehiculo' },
                         { accion: 'cancelar', icono: 'equis', titulo: 'Cancelar' }],
            ASIGNADO: [{ accion: 'iniciar', icono: 'reproducir', titulo: 'Iniciar viaje' },
                       { accion: 'asignar', icono: 'intercambio', titulo: 'Cambiar vehiculo' },
                       { accion: 'cancelar', icono: 'equis', titulo: 'Cancelar' }],
            EN_CURSO: [{ accion: 'finalizar', icono: 'revisar', titulo: 'Finalizar y cobrar' }],
        }[v.estadoViaje] || []),
    ],
    alAccion: {
        ver: (v) => ficha(v),
        asignar: (v, recargar) => asignar(v, recargar),
        iniciar: (v, recargar) => cambiarEstado(v, recargar, 'iniciar'),
        cancelar: (v, recargar) => cambiarEstado(v, recargar, 'cancelar'),
        finalizar: (v, recargar) => finalizar(v, recargar),
    },
    vacio: { icono: 'servicio', titulo: 'Aun no hay viajes', texto: 'Solicite el primer viaje para un pasajero registrado.' },
});
