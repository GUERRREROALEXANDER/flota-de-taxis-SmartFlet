/**
 * Pantalla de inicio de sesion.
 *
 * Ayudas para el usuario ("pistas"):
 *   - aviso en vivo si Bloq Mayus esta activado (la causa mas comun de "clave incorrecta");
 *   - boton para ver la contrasena escrita;
 *   - el error del backend se muestra junto con su "pista" (que revisar, intentos que quedan);
 *   - los campos vacios se marcan antes de llamar al backend.
 */

import { $, esc, icono } from '../componentes/ui.js';
import { ErrorApi } from '../api/clienteHttp.js';
import { iniciarSesion } from '../app/sesion.js';

const LOGO = `
    <svg viewBox="0 0 48 48" fill="none" aria-hidden="true">
        <rect width="48" height="48" rx="12" fill="#F5B93B"/>
        <path d="M12 30h24v5a2 2 0 0 1-2 2h-3a2 2 0 0 1-2-2v-1H19v1a2 2 0 0 1-2 2h-3a2 2 0 0 1-2-2z" fill="#0B1120"/>
        <path d="M15 29l3-9a3 3 0 0 1 3-2h6a3 3 0 0 1 3 2l3 9z" fill="#0B1120"/>
        <circle cx="17" cy="33" r="1.6" fill="#F5B93B"/>
        <circle cx="31" cy="33" r="1.6" fill="#F5B93B"/>
    </svg>`;

/** Ilustracion animada: una ciudad nocturna con taxis recorriendo rutas (SVG + CSS). */
const ESCENA = `
    <svg class="escena" viewBox="0 0 480 300" aria-hidden="true">
        <defs>
            <linearGradient id="escenaRuta" x1="0" x2="1">
                <stop offset="0" stop-color="var(--primario)" stop-opacity="0"/>
                <stop offset=".5" stop-color="var(--primario)" stop-opacity=".9"/>
                <stop offset="1" stop-color="var(--marca)" stop-opacity="0"/>
            </linearGradient>
        </defs>
        <g class="escena__edificios">
            <rect x="20"  y="120" width="46" height="150" rx="4"/>
            <rect x="74"  y="80"  width="56" height="190" rx="4"/>
            <rect x="140" y="140" width="40" height="130" rx="4"/>
            <rect x="300" y="100" width="52" height="170" rx="4"/>
            <rect x="360" y="60"  width="44" height="210" rx="4"/>
            <rect x="412" y="130" width="50" height="140" rx="4"/>
        </g>
        <g class="escena__ventanas">
            ${Array.from({ length: 22 }, (_, i) => {
                const xs = [30, 46, 86, 104, 150, 312, 330, 372, 388, 424, 442];
                const x = xs[i % xs.length];
                const y = 100 + ((i * 37) % 150);
                return `<rect x="${x}" y="${y}" width="8" height="6" rx="1" style="--d:${(i * 0.37) % 4}s"/>`;
            }).join('')}
        </g>
        <path class="escena__calle" d="M0 262 H480"/>
        <path class="escena__ruta" d="M10 230 C 120 170, 200 250, 260 200 S 400 120, 470 160"/>
        <path class="escena__ruta escena__ruta--2" d="M10 180 C 90 220, 180 120, 270 150 S 390 230, 470 200"/>
        <g class="escena__taxi escena__taxi--1"><rect x="-14" y="-7" width="28" height="12" rx="4"/><circle cx="-7" cy="6" r="3"/><circle cx="7" cy="6" r="3"/></g>
        <g class="escena__taxi escena__taxi--2"><rect x="-14" y="-7" width="28" height="12" rx="4"/><circle cx="-7" cy="6" r="3"/><circle cx="7" cy="6" r="3"/></g>
        <circle class="escena__pulso" cx="260" cy="200" r="6"/>
        <circle class="escena__pulso escena__pulso--2" cx="400" cy="140" r="6"/>
    </svg>`;

/**
 * Pinta la pantalla de acceso dentro de `contenedor` y resuelve cuando el login es correcto.
 * @param {string} [motivo] texto opcional arriba del formulario (ej. "Tu sesion expiro")
 */
export function mostrarAcceso(contenedor, motivo = '') {
    return new Promise((resolver) => {
        contenedor.innerHTML = `
            <div class="acceso">
                <div class="fondo-aurora" aria-hidden="true"><span></span><span></span><span></span></div>

                <section class="acceso__marca">
                    <div class="acceso__marca-contenido">
                        <div class="acceso__logo">${LOGO}</div>
                        <h1 class="acceso__titulo">Tu flota, <span class="texto-degradado">en tiempo real</span>.</h1>
                        <p class="acceso__lema">Despacha viajes, controla turnos y mira los ingresos del dia desde un solo lugar.</p>
                        ${ESCENA}
                        <ul class="acceso__lista">
                            <li><span class="punto punto--positivo"></span>Viajes, turnos y pagos sincronizados con el backend</li>
                            <li><span class="punto punto--informativo"></span>Permisos por rol: administrador y operador</li>
                            <li><span class="punto punto--advertencia"></span>Sesion protegida que expira por inactividad</li>
                        </ul>
                    </div>
                </section>

                <section class="acceso__panel">
                    <form class="acceso__formulario tarjeta-vidrio" id="formulario-acceso" novalidate>
                        <div class="acceso__logo acceso__logo--movil">${LOGO}</div>
                        <h2 class="acceso__encabezado">Iniciar sesion</h2>
                        <p class="acceso__subencabezado">Entra con la cuenta que te dio el administrador.</p>

                        ${motivo ? `<div class="alerta alerta--informativo acceso__motivo">${icono('informacion', 'alerta__icono')}
                            <div class="alerta__contenido"><p class="alerta__texto">${esc(motivo)}</p></div></div>` : ''}

                        <div class="alerta alerta--negativo acceso__error" id="acceso-error" hidden role="alert">
                            ${icono('alerta', 'alerta__icono')}
                            <div class="alerta__contenido">
                                <p class="alerta__titulo" id="acceso-error-titulo"></p>
                                <p class="alerta__texto" id="acceso-error-pista"></p>
                            </div>
                        </div>

                        <div class="campo">
                            <label class="campo__etiqueta" for="acceso-usuario">Usuario</label>
                            <div class="campo__envoltorio">
                                <span class="campo__icono">${icono('conductor')}</span>
                                <input class="campo__control" id="acceso-usuario" name="usuario" autocomplete="username"
                                       autocapitalize="none" spellcheck="false" placeholder="ej. admin" required>
                            </div>
                        </div>

                        <div class="campo">
                            <label class="campo__etiqueta" for="acceso-clave">Contrasena</label>
                            <div class="campo__envoltorio">
                                <span class="campo__icono">${icono('candado')}</span>
                                <input class="campo__control" id="acceso-clave" name="clave" type="password"
                                       autocomplete="current-password" placeholder="••••••••" required>
                                <button type="button" class="campo__accion" id="ver-clave" aria-label="Mostrar contrasena"
                                        aria-pressed="false">${icono('ojo')}</button>
                            </div>
                            <p class="campo__ayuda acceso__mayus" id="aviso-mayus" hidden>
                                ${icono('alerta')} Bloq Mayus esta activado
                            </p>
                        </div>

                        <button class="boton boton--primario boton--bloque boton--brillo" type="submit" id="boton-entrar">
                            <span>Entrar</span>${icono('salir')}
                        </button>

                        <div class="acceso__pista">
                            ${icono('bombilla')}
                            <p><strong>¿Primer ingreso?</strong> Usa el administrador definido en el servidor
                               (<code>ADMIN_USUARIO</code> / <code>ADMIN_CLAVE</code>). Desde el menu <em>Usuarios</em>
                               puedes crear cuentas de operador.</p>
                        </div>
                    </form>
                </section>
            </div>`;

        const formulario = $('#formulario-acceso', contenedor);
        const usuario = $('#acceso-usuario', contenedor);
        const clave = $('#acceso-clave', contenedor);
        const boton = $('#boton-entrar', contenedor);
        const cajaError = $('#acceso-error', contenedor);

        usuario.focus();

        // Ver / ocultar contrasena
        const verClave = $('#ver-clave', contenedor);
        verClave.addEventListener('click', () => {
            const visible = clave.type === 'text';
            clave.type = visible ? 'password' : 'text';
            verClave.setAttribute('aria-pressed', String(!visible));
            verClave.setAttribute('aria-label', visible ? 'Mostrar contrasena' : 'Ocultar contrasena');
            verClave.innerHTML = icono(visible ? 'ojo' : 'ojoCerrado');
            clave.focus();
        });

        // Aviso de Bloq Mayus mientras escribe la clave
        const avisoMayus = $('#aviso-mayus', contenedor);
        const revisarMayus = (e) => { avisoMayus.hidden = !e.getModifierState?.('CapsLock'); };
        clave.addEventListener('keydown', revisarMayus);
        clave.addEventListener('keyup', revisarMayus);
        clave.addEventListener('blur', () => { avisoMayus.hidden = true; });

        const mostrarError = (titulo, pista = '') => {
            $('#acceso-error-titulo', contenedor).textContent = titulo;
            $('#acceso-error-pista', contenedor).textContent = pista;
            $('#acceso-error-pista', contenedor).hidden = !pista;
            cajaError.hidden = false;
            formulario.classList.remove('sacudir');
            void formulario.offsetWidth; // reinicia la animacion
            formulario.classList.add('sacudir');
        };

        const marcarCampo = (input, mensaje) => {
            const campo = input.closest('.campo');
            campo.classList.toggle('campo--invalido', Boolean(mensaje));
            let p = campo.querySelector('.campo__error');
            if (!p) {
                p = document.createElement('p');
                p.className = 'campo__error';
                campo.appendChild(p);
            }
            p.textContent = mensaje || '';
            p.hidden = !mensaje;
        };

        [usuario, clave].forEach((input) => input.addEventListener('input', () => marcarCampo(input, '')));

        formulario.addEventListener('submit', async (evento) => {
            evento.preventDefault();
            cajaError.hidden = true;
            const faltaUsuario = !usuario.value.trim();
            const faltaClave = !clave.value;
            marcarCampo(usuario, faltaUsuario ? 'Escribe tu nombre de usuario' : '');
            marcarCampo(clave, faltaClave ? 'Escribe tu contrasena' : '');
            if (faltaUsuario || faltaClave) {
                (faltaUsuario ? usuario : clave).focus();
                return;
            }

            boton.classList.add('cargando');
            boton.disabled = true;
            try {
                const datos = await iniciarSesion(usuario.value.trim(), clave.value);
                formulario.classList.add('saliendo-acceso');
                setTimeout(() => resolver(datos), 380);
            } catch (error) {
                if (error instanceof ErrorApi && error.campos) {
                    marcarCampo(usuario, error.campos.usuario);
                    marcarCampo(clave, error.campos.clave);
                } else if (error instanceof ErrorApi && error.estado) {
                    mostrarError(error.message, error.pista);
                } else {
                    mostrarError('No hay conexion con el servidor',
                        'Revisa que el backend (Spring Boot) este encendido y vuelve a intentar.');
                }
                clave.select();
            } finally {
                boton.classList.remove('cargando');
                boton.disabled = false;
            }
        });
    });
}
