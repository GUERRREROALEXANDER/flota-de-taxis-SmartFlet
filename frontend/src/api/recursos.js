/**
 * Un modulo por cada controlador del backend.
 *
 * Cada objeto corresponde a un `*Controlador` de Spring Boot y usa su misma `rutaBase`.
 * Si se agrega un endpoint en el backend, aqui se agrega su llamada y ninguna vista
 * tiene que saber como se arma la URL.
 */

import { clienteHttp as http } from './clienteHttp.js';

/** VehiculoControlador → /api/vehiculos */
export const vehiculosApi = {
    listar: (estadoOperativo) => http.get('/vehiculos', { estadoOperativo }),
    obtener: (id) => http.get(`/vehiculos/${id}`),
    crear: (datos) => http.post('/vehiculos', datos),
    actualizar: (id, datos) => http.put(`/vehiculos/${id}`, datos),
    cambiarEstado: (id, estadoOperativo) => http.patch(`/vehiculos/${id}/estado`, { estadoOperativo }),
    eliminar: (id) => http.delete(`/vehiculos/${id}`),
};

/** RutaControlador → /api/rutas */
export const rutasApi = {
    listar: (origen) => http.get('/rutas', { origen }),
    obtener: (id) => http.get(`/rutas/${id}`),
    crear: (datos) => http.post('/rutas', datos),
    actualizar: (id, datos) => http.put(`/rutas/${id}`, datos),
    eliminar: (id) => http.delete(`/rutas/${id}`),
};

/** PasajeroControlador → /api/pasajeros */
export const pasajerosApi = {
    listar: () => http.get('/pasajeros'),
    obtener: (id) => http.get(`/pasajeros/${id}`),
    crear: (datos) => http.post('/pasajeros', datos),
    actualizar: (id, datos) => http.put(`/pasajeros/${id}`, datos),
    cambiarEstado: (id, estado) => http.patch(`/pasajeros/${id}/estado`, { estado }),
};

/** ConductorControlador → /api/conductores */
export const conductoresApi = {
    listar: (estado) => http.get('/conductores', { estado }),
    obtener: (id) => http.get(`/conductores/${id}`),
    crear: (datos) => http.post('/conductores', datos),
    actualizar: (id, datos) => http.put(`/conductores/${id}`, datos),
    cambiarEstado: (id, estado) => http.patch(`/conductores/${id}/estado`, { estado }),
};

/** TarifaControlador → /api/tarifas */
export const tarifasApi = {
    listar: () => http.get('/tarifas'),
    vigente: () => http.get('/tarifas/vigente'),
    obtener: (id) => http.get(`/tarifas/${id}`),
    crear: (datos) => http.post('/tarifas', datos),
    actualizar: (id, datos) => http.put(`/tarifas/${id}`, datos),
    desactivar: (id) => http.patch(`/tarifas/${id}/desactivar`),
};

/** TurnoControlador → /api/turnos */
export const turnosApi = {
    listar: (conductorId) => http.get('/turnos', { conductorId }),
    obtener: (id) => http.get(`/turnos/${id}`),
    programar: (datos) => http.post('/turnos', datos),
    iniciar: (id) => http.patch(`/turnos/${id}/iniciar`),
    finalizar: (id) => http.patch(`/turnos/${id}/finalizar`),
};

/** ViajeControlador → /api/viajes */
export const viajesApi = {
    listar: (estado, pasajeroId) => http.get('/viajes', { estado, pasajeroId }),
    obtener: (id) => http.get(`/viajes/${id}`),
    solicitar: (datos) => http.post('/viajes', datos),
    asignar: (id, vehiculoId) => http.patch(`/viajes/${id}/asignar`, { vehiculoId }),
    iniciar: (id) => http.patch(`/viajes/${id}/iniciar`),
    finalizar: (id, distanciaKm, metodoPago) =>
        http.patch(`/viajes/${id}/finalizar`, { distanciaKm, metodoPago }),
    cancelar: (id) => http.patch(`/viajes/${id}/cancelar`),
};

/** PagoControlador → /api/pagos */
export const pagosApi = {
    listar: () => http.get('/pagos'),
    obtener: (id) => http.get(`/pagos/${id}`),
    anular: (id) => http.patch(`/pagos/${id}/anular`),
};
