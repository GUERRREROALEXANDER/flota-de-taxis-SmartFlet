/**
 * ClienteHttp: la unica pieza del frontend que habla con el backend.
 *
 * Es la clase `ClienteHttp` de la vista de desarrollo: tiene `baseUrl = "/api"` y los
 * metodos get, post, put, patch y delete. Las vistas nunca usan `fetch` directamente;
 * llaman a los modulos de `src/api/` (vehiculosApi, viajesApi...) y estos a este cliente.
 *
 * Los errores del backend llegan siempre con la forma de `ErrorRespuesta`
 * `{ estado, mensaje, errores, fecha }`. Aqui se convierten en un `ErrorApi`, asi un
 * formulario puede pintar el mensaje debajo del campo exacto que fallo.
 */

/** Error devuelto por la API, con el codigo HTTP y los errores por campo. */
export class ErrorApi extends Error {
    constructor(mensaje, { estado = 0, campos = null, pista = null } = {}) {
        super(mensaje);
        this.name = 'ErrorApi';
        this.estado = estado;
        this.campos = campos && Object.keys(campos).length ? campos : null;
        /** Sugerencia del backend sobre que revisar (ErrorRespuesta.pista). */
        this.pista = pista;
    }

    get sinSesion() {
        return this.estado === 401;
    }

    get sinPermiso() {
        return this.estado === 403;
    }

    /** 400 sin errores de campo: el backend rechazo la operacion por una regla de negocio. */
    get esReglaNegocio() {
        return this.estado === 400 && !this.campos;
    }

    get esValidacion() {
        return this.estado === 400 && Boolean(this.campos);
    }

    get esDuplicado() {
        return this.estado === 409;
    }

    get noEncontrado() {
        return this.estado === 404;
    }
}

export class ClienteHttp {
    constructor(baseUrl = '/api') {
        this.baseUrl = baseUrl;
    }

    get(ruta, parametros) {
        return this.#peticion('GET', ruta + consulta(parametros));
    }

    post(ruta, cuerpo) {
        return this.#peticion('POST', ruta, cuerpo ?? {});
    }

    put(ruta, cuerpo) {
        return this.#peticion('PUT', ruta, cuerpo);
    }

    patch(ruta, cuerpo) {
        return this.#peticion('PATCH', ruta, cuerpo);
    }

    delete(ruta) {
        return this.#peticion('DELETE', ruta);
    }

    async #peticion(metodo, ruta, cuerpo) {
        const opciones = { method: metodo, headers: { Accept: 'application/json' } };
        if (cuerpo !== undefined) {
            opciones.headers['Content-Type'] = 'application/json';
            opciones.body = JSON.stringify(cuerpo);
        }

        let respuesta;
        try {
            respuesta = await fetch(this.baseUrl + ruta, opciones);
        } catch {
            throw new ErrorApi(
                'No se pudo contactar al backend. Revise que Spring Boot este corriendo en el puerto 8080.',
            );
        }

        if (respuesta.status === 204) return null;

        const tipo = respuesta.headers.get('Content-Type') || '';
        const datos = tipo.includes('json') ? await respuesta.json().catch(() => null) : null;

        if (!respuesta.ok) {
            // 401 fuera del login = la sesion expiro: app/sesion.js escucha este evento
            // y vuelve a mostrar la pantalla de acceso.
            if (respuesta.status === 401 && !ruta.startsWith('/auth/')) {
                window.dispatchEvent(new CustomEvent('smartflet:sesion-expirada'));
            }
            throw new ErrorApi(datos?.mensaje || `Error ${respuesta.status} al procesar la solicitud.`, {
                estado: respuesta.status,
                campos: datos?.errores,
                pista: datos?.pista,
            });
        }
        return datos;
    }
}

/** Arma "?clave=valor" descartando los parametros vacios. */
function consulta(parametros = {}) {
    const partes = Object.entries(parametros)
        .filter(([, valor]) => valor !== null && valor !== undefined && valor !== '')
        .map(([clave, valor]) => `${encodeURIComponent(clave)}=${encodeURIComponent(valor)}`);
    return partes.length ? `?${partes.join('&')}` : '';
}

/** Instancia unica que comparten todos los modulos de la API. */
export const clienteHttp = new ClienteHttp('/api');
