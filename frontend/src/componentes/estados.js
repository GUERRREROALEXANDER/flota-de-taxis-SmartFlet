/**
 * Etiqueta y color de cada enum del backend (paquete smartflet.modelo).
 *
 * El backend envia el nombre del enum (por ejemplo "EN_CURSO"); aqui se decide como se
 * muestra. Es el unico sitio del frontend que conoce esos valores.
 */

const tabla = (definicion) => (codigo) =>
    definicion[codigo] || { etiqueta: codigo ?? 'Sin estado', tono: 'neutro' };

/** EstadoViaje: SOLICITADO → ASIGNADO → EN_CURSO → FINALIZADO (o CANCELADO). */
export const estadoViaje = tabla({
    SOLICITADO: { etiqueta: 'Solicitado', tono: 'advertencia' },
    ASIGNADO: { etiqueta: 'Asignado', tono: 'informativo' },
    EN_CURSO: { etiqueta: 'En curso', tono: 'informativo' },
    FINALIZADO: { etiqueta: 'Finalizado', tono: 'positivo' },
    CANCELADO: { etiqueta: 'Cancelado', tono: 'negativo' },
});
export const ESTADOS_VIAJE = ['SOLICITADO', 'ASIGNADO', 'EN_CURSO', 'FINALIZADO', 'CANCELADO'];

/** EstadoTurno */
export const estadoTurno = tabla({
    PROGRAMADO: { etiqueta: 'Programado', tono: 'neutro' },
    EN_CURSO: { etiqueta: 'En curso', tono: 'positivo' },
    FINALIZADO: { etiqueta: 'Finalizado', tono: 'informativo' },
});
export const ESTADOS_TURNO = ['PROGRAMADO', 'EN_CURSO', 'FINALIZADO'];

/** EstadoRegistro: lo usan Pasajero, Conductor y Tarifa. */
export const estadoRegistro = tabla({
    ACTIVO: { etiqueta: 'Activo', tono: 'positivo' },
    INACTIVO: { etiqueta: 'Inactivo', tono: 'neutro' },
});

/** EstadoPago */
export const estadoPago = tabla({
    PAGADO: { etiqueta: 'Pagado', tono: 'positivo' },
    ANULADO: { etiqueta: 'Anulado', tono: 'negativo' },
});

/** MetodoPago */
export const METODOS_PAGO = [
    { valor: 'EFECTIVO', texto: 'Efectivo' },
    { valor: 'TARJETA', texto: 'Tarjeta' },
    { valor: 'TRANSFERENCIA', texto: 'Transferencia' },
];
export const metodoPago = (codigo) => METODOS_PAGO.find((m) => m.valor === codigo)?.texto ?? codigo;

/** Vehiculo.estadoOperativo es un booleano. */
export const estadoVehiculo = (operativo) =>
    operativo
        ? { etiqueta: 'Operativo', tono: 'positivo' }
        : { etiqueta: 'Fuera de servicio', tono: 'negativo' };
