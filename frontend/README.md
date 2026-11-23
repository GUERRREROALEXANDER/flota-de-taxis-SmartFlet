# Frontend SmartFlet

Interfaz web del centro de operaciones. Es una aplicación de una sola página en JavaScript puro (módulos ES), sin frameworks. Vite solo sirve los archivos en desarrollo y arma la versión final.

## Cómo correrlo

```bash
# 1. Backend en otra terminal (puerto 8080)
cd backend
./mvnw spring-boot:run

# 2. Frontend
cd frontend
npm install   # solo la primera vez
npm run dev   # http://localhost:5173
```

Vite reenvía todo lo que empieza por `/api` al backend (proxy en `vite.config.js`), así que no hay problemas de CORS. Si el backend corre en otro puerto:

```bash
BACKEND_URL=http://localhost:9090 npm run dev            # Git Bash
$env:BACKEND_URL="http://localhost:9090"; npm run dev    # PowerShell
```

## Estructura

```
frontend/
├── index.html               # Esqueleto: menú lateral, barra superior y <main>
├── public/favicon.svg
├── vite.config.js           # Puerto 5173 y proxy /api → backend
└── src/
    ├── main.js              # Arranque
    ├── api/
    │   ├── clienteHttp.js   # Clase ClienteHttp (baseUrl "/api"; get, post, put, patch, delete)
    │   └── recursos.js      # vehiculosApi, viajesApi... uno por controlador del backend
    ├── app/
    │   ├── enrutador.js     # Lista de pantallas y navegación por #/ruta
    │   └── interfaz.js      # Tema claro/oscuro, menú en móvil, atajos de teclado
    ├── componentes/
    │   ├── ui.js            # Formato, iconos, avisos, ventana modal, manejo de errores
    │   ├── catalogo.js      # Pantalla tipo lista: búsqueda, filtros, tabla y acciones
    │   ├── formulario.js    # Formularios en ventana modal a partir de una lista de campos
    │   ├── estados.js       # Etiqueta y color de cada enum del backend
    │   ├── referencias.js   # Nombres y placas a partir de los ids que guarda MongoDB
    │   ├── estadisticas.js  # Cálculos del panel y los reportes
    │   └── graficas.js      # Gráficas en SVG (anillo, área, barras)
    ├── vistas/              # Una pantalla por módulo
    │   ├── panel.js  viajes.js  turnos.js  vehiculos.js  conductores.js
    │   └── rutas.js  pasajeros.js  tarifas.js  pagos.js  reportes.js
    └── estilos/app.css
```

Cómo encaja con el backend:

```
vistas/viajes.js → api/recursos.js (viajesApi) → api/clienteHttp.js → HTTP/JSON → ViajeControlador → ViajeServicio → ViajeRepositorio
```

Es la misma cadena que muestra la vista de desarrollo en `docs/diagramas`.

## Pantallas

| Pantalla | Endpoint | Qué se puede hacer |
|---|---|---|
| Panel de control | varios | Indicadores, avisos (tarifa vigente, viajes sin asignar, licencias vencidas), actividad de 7 días |
| Viajes | `/api/viajes` | Solicitar, asignar vehículo en turno, iniciar, finalizar y cobrar, cancelar, ver detalle |
| Turnos | `/api/turnos` | Programar, iniciar y finalizar |
| Vehículos | `/api/vehiculos` | Registrar, editar, cambiar estado operativo, eliminar |
| Conductores | `/api/conductores` | Registrar, editar, activar o inactivar; aviso de licencia por vencer |
| Rutas | `/api/rutas` | Registrar, editar, eliminar |
| Pasajeros | `/api/pasajeros` | Registrar, editar, activar o inactivar |
| Tarifas | `/api/tarifas` | Crear, editar, desactivar; marca la vigente |
| Pagos | `/api/pagos` | Consultar y anular |
| Reportes | `/api/viajes`, `/api/pagos` | Viajes e ingresos por periodo, por vehículo y por método de pago |

Los errores del backend (`ErrorRespuesta`) se muestran tal cual: los de validación debajo de cada campo y los de reglas de negocio como aviso.

Atajos: `/` enfoca la búsqueda y `R` recarga la pantalla.
