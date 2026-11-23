/** Punto de entrada del frontend. */

import './estilos/app.css';
import { aplicarTemaGuardado, conectarInterfaz } from './app/interfaz.js';
import { construirNavegacion, renderizarVista, escucharCambiosDeRuta } from './app/enrutador.js';

aplicarTemaGuardado();
construirNavegacion();
conectarInterfaz();
escucharCambiosDeRuta();
renderizarVista();
