/** Cuentas de acceso → UsuarioControlador (/api/usuarios). Solo la ve el ADMINISTRADOR. */

import { usuariosApi } from '../api/recursos.js';
import { esc, insignia, formato, avisoExito, manejarError } from '../componentes/ui.js';
import { estadoRegistro } from '../componentes/estados.js';
import { abrirFormulario } from '../componentes/formulario.js';
import { crearCatalogo } from '../componentes/catalogo.js';
import { sesion, iniciales, nombreRol } from '../app/sesion.js';

const ROLES = [
    { valor: 'OPERADOR', texto: 'Operador de central' },
    { valor: 'ADMINISTRADOR', texto: 'Administrador' },
];

function formulario(recargar) {
    return abrirFormulario({
        titulo: 'Nuevo usuario',
        descripcion: 'La contrasena se guarda cifrada (BCrypt); nadie puede verla despues, ni el administrador.',
        campos: [
            { nombre: 'nombre', etiqueta: 'Nombre completo', requerido: true, placeholder: 'Ana Rodriguez', ancho: 'completo' },
            { nombre: 'nombreUsuario', etiqueta: 'Usuario', requerido: true, dato: true, placeholder: 'ana.rodriguez',
              ayuda: '3 a 30 caracteres: letras, numeros, punto o guion. Sin espacios.' },
            { nombre: 'rol', etiqueta: 'Rol', requerido: true, tipo: 'select', opciones: ROLES, valor: 'OPERADOR', vacio: false,
              ayuda: 'El operador consulta todo y opera viajes, turnos y pasajeros.' },
            { nombre: 'clave', etiqueta: 'Contrasena inicial', requerido: true, tipo: 'password', ancho: 'completo',
              ayuda: 'Minimo 8 caracteres. Compartela por un canal privado.' },
        ],
        guardar: async (datos) => {
            await usuariosApi.crear(datos);
            avisoExito('Usuario creado', `${datos.nombre} ya puede iniciar sesion`);
            await recargar();
        },
    });
}

export const { render } = crearCatalogo({
    id: 'usuarios',
    cargar: () => usuariosApi.listar(),
    textoNuevo: 'Nuevo usuario',
    alCrear: (recargar) => formulario(recargar),
    busqueda: { placeholder: 'Buscar por nombre o usuario…', texto: (u) => `${u.nombre} ${u.nombreUsuario}` },
    filtros: [
        { valor: '', etiqueta: 'Todos' },
        { valor: 'ADMINISTRADOR', etiqueta: 'Administradores', prueba: (u) => u.rol === 'ADMINISTRADOR' },
        { valor: 'OPERADOR', etiqueta: 'Operadores', prueba: (u) => u.rol === 'OPERADOR' },
    ],
    columnas: [
        { titulo: 'Usuario', celda: (u) => `
            <div style="display:flex;align-items:center;gap:.75rem">
                <span class="usuario__avatar">${esc(iniciales(u.nombre))}</span>
                <div><div class="principal-celda">${esc(u.nombre)}${u.nombreUsuario === sesion.usuario?.usuario ? ' <span class="insignia insignia--informativo">Tu</span>' : ''}</div>
                <div class="secundaria dato">${esc(u.nombreUsuario)}</div></div>
            </div>` },
        { titulo: 'Rol', celda: (u) => insignia({ etiqueta: nombreRol(u.rol), tono: u.rol === 'ADMINISTRADOR' ? 'advertencia' : 'informativo' }) },
        { titulo: 'Alta', celda: (u) => formato.fecha(u.fechaRegistro + 'T00:00') },
        { titulo: 'Estado', celda: (u) => insignia(estadoRegistro(u.estado)) },
    ],
    acciones: () => [{ accion: 'estado', icono: 'intercambio', titulo: 'Activar o desactivar' }],
    alAccion: {
        estado: async (u, recargar) => {
            const nuevo = u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
            try {
                await usuariosApi.cambiarEstado(u.id, nuevo);
                avisoExito('Cuenta actualizada', `${u.nombre}: ${estadoRegistro(nuevo).etiqueta}`);
                await recargar();
            } catch (error) {
                manejarError(error);
            }
        },
    },
    vacio: { icono: 'usuarios', titulo: 'Sin usuarios', texto: 'Crea cuentas para los operadores de la central.' },
});
