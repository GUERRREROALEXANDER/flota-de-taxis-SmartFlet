/**
 * Punto de entrada del frontend.
 *
 * Flujo:
 *   1. Pregunta al backend si ya hay sesion (GET /api/auth/sesion).
 *   2. Si no hay, muestra la pantalla de acceso y espera el login.
 *   3. Con sesion, arma el menu segun el rol y pinta la vista de la URL.
 *   4. Si en cualquier momento el backend responde 401 (sesion expirada),
 *      vuelve al paso 2 sin perder la pantalla en la que estaba.
 */

import './estilos/app.css';
import './estilos/movimiento.css';
import { $ } from './componentes/ui.js';
import { aplicarTemaGuardado, conectarInterfaz } from './app/interfaz.js';
import { construirNavegacion, renderizarVista, escucharCambiosDeRuta } from './app/enrutador.js';
import { recuperarSesion, cerrarSesion, sesion, iniciales, nombreRol } from './app/sesion.js';
import { mostrarAcceso } from './vistas/acceso.js';
import { conectarBrilloTarjetas } from './componentes/animaciones.js';

const aplicacion = $('#aplicacion');
const pantallaAcceso = $('#pantalla-acceso');
let interfazConectada = false;
let pidiendoLogin = false;

async function pedirLogin(motivo = '') {
    if (pidiendoLogin) return;
    pidiendoLogin = true;
    aplicacion.hidden = true;
    pantallaAcceso.hidden = false;
    await mostrarAcceso(pantallaAcceso, motivo);
    pantallaAcceso.hidden = true;
    pantallaAcceso.innerHTML = '';
    pidiendoLogin = false;
    abrirAplicacion();
}

function abrirAplicacion() {
    const u = sesion.usuario;
    $('#usuario-avatar').textContent = iniciales(u.nombre);
    $('#usuario-nombre').textContent = u.nombre;
    $('#usuario-rol').textContent = nombreRol(u.rol);

    construirNavegacion();          // el menu depende del rol
    if (!interfazConectada) {
        conectarInterfaz();
        escucharCambiosDeRuta();
        conectarBrilloTarjetas();
        interfazConectada = true;
    }
    aplicacion.hidden = false;
    aplicacion.classList.add('aplicacion-entrando');
    renderizarVista();
}

async function arrancar() {
    aplicarTemaGuardado();

    $('#cerrar-sesion').addEventListener('click', async () => {
        await cerrarSesion();
        pedirLogin('Cerraste sesion correctamente.');
    });
    window.addEventListener('smartflet:sesion-expirada', () =>
        pedirLogin('Tu sesion expiro por inactividad. Vuelve a entrar para continuar donde ibas.'));

    const usuario = await recuperarSesion();
    $('#arranque').remove();
    if (usuario) abrirAplicacion();
    else pedirLogin();
}

arrancar();
