# SmartFlet — Sistema de gestión de flota de taxis

> **Estado:** En desarrollo. Backend Spring Boot con API REST sobre MongoDB (vehículos, rutas, pasajeros, conductores, turnos, tarifas, viajes y pagos) y frontend web (carpeta `frontend/`) con panel, viajes, turnos, vehículos, conductores, rutas, pasajeros, tarifas, pagos y reportes.
> Lo que aún no está implementado se indica como **(pendiente)**.

---

## Tabla de contenido

1. [Descripción general](#1-descripción-general)
2. [Alcance del proyecto](#2-alcance-del-proyecto)
3. [Tecnologías y arquitectura](#3-tecnologías-y-arquitectura)
4. [Instalación, configuración y ejecución](#4-instalación-configuración-y-ejecución)
5. [Estructura de directorios](#5-estructura-de-directorios)
6. [Historias de usuario](#6-historias-de-usuario)
7. [Requerimientos funcionales](#7-requerimientos-funcionales)
8. [Diagramas UML](#8-diagramas-uml)

---

## 1. Descripción general

**SmartFlet** es una plataforma para administrar una flota de taxis de forma centralizada. Permite a la empresa registrar sus vehículos y conductores, asignar viajes a los taxis disponibles, seguir su ubicación en tiempo real, controlar turnos y mantenimiento, y consultar indicadores de operación.

### Propósito

- Reducir los tiempos de espera de los pasajeros mediante la asignación automática o manual del taxi más cercano disponible.
- Dar a la central de operaciones visibilidad en tiempo real del estado de cada vehículo.
- Llevar un registro confiable de viajes, kilometraje, ingresos y mantenimiento para la toma de decisiones.

### Usuarios y roles

| Rol | Descripción |
|---|---|
| **Administrador** | Gestiona vehículos, conductores, tarifas y usuarios del sistema. Consulta reportes completos. |
| **Operador de central** | Recibe solicitudes de viaje, asigna taxis y supervisa la flota en tiempo real. |
| **Conductor** | Recibe asignaciones, reporta su estado (disponible, ocupado, fuera de servicio) y cierra viajes. |
| **Pasajero** | Solicita un taxi y consulta el estado y la ubicación de su viaje. |

---

## 2. Alcance del proyecto

### Incluido en la versión inicial (MVP)

- Registro y autenticación de usuarios con roles.
- Gestión de vehículos y conductores.
- Solicitud, asignación y seguimiento de viajes.
- Seguimiento de ubicación de la flota en un mapa.
- Historial de viajes y reportes básicos.

### Fuera del alcance del MVP

- Procesamiento de pagos con tarjeta (se registrará solo el monto cobrado).
- Aplicaciones nativas para iOS y Android.
- Integración con sistemas de facturación electrónica del gobierno.
- Optimización avanzada de rutas con tráfico predictivo.

---

## 3. Tecnologías y arquitectura

### Tecnologías

| Capa | Tecnología | Uso |
|---|---|---|
| Backend | Java (compila a 21; JDK 26 instalado) + Spring Boot 3.3.5 | API REST, validación (Bean Validation), inyección de dependencias. |
| Persistencia | Spring Data MongoDB | Documentos `@Document` y repositorios `MongoRepository`. |
| Base de datos | MongoDB (Atlas o local) | Conexión por la variable de entorno `MONGODB_URI`. |
| Pruebas | JUnit 5, MockMvc, MongoDB embebido (flapdoodle) | Las pruebas levantan su propio `mongod`; no tocan Atlas. |
| Build | Maven Wrapper (`mvnw`) | No hace falta instalar Maven. |
| Frontend | JavaScript puro (módulos ES) + Vite | Proyecto aparte en `frontend/` (ver su README). Sin frameworks. |

### Arquitectura

```
 Navegador ──HTTP──► Vite :5173 ──proxy /api──► Backend Spring Boot :8080 ──driver MongoDB──► MongoDB (Atlas / local)
                                                  Controlador → Servicio → Repositorio
```

- **Controladores** (`controlador/`): solo reciben la petición HTTP, validan el cuerpo con `@Valid` y devuelven el código de estado.
- **Servicios** (`servicio/`): reglas de negocio que necesitan consultar datos (placa única, turno en curso, tarifa vigente…).
- **Modelo** (`modelo/`): documentos de MongoDB. `Viaje`, `Turno` y `Pago` validan sus propias transiciones de estado.
- **Repositorios** (`repositorio/`): interfaces `MongoRepository`; Spring genera la implementación.
- **Errores** (`excepcion/ManejadorGlobalExcepciones`): convierte toda excepción en un JSON `ErrorRespuesta`.

### Modelo de datos en MongoDB

| Colección | Documento | Relaciones |
|---|---|---|
| `vehiculos` | Vehiculo (placa única) | — |
| `rutas` | Ruta | — |
| `pasajeros` | Pasajero (identificación única) | — |
| `conductores` | Conductor (identificación y licencia únicas) | — |
| `tarifas` | Tarifa (valores en `Decimal128`) | — |
| `turnos` | Turno | `conductorId`, `vehiculoId` (referencias) |
| `viajes` | Viaje | `pasajeroId`, `vehiculoId` (referencias); **copia embebida** de la tarifa; **pago embebido** |

Decisiones:
- El **pago vive dentro del viaje** (composición). Finalizar un viaje escribe viaje y pago en un solo documento, sin necesitar transacciones.
- El viaje guarda una **copia de la tarifa** vigente al solicitarlo, así el precio no cambia si después se modifica la tarifa.
- No se usa `@Transactional`: cada operación escribe un único documento, y las transacciones de MongoDB exigen un *replica set*.

### Códigos HTTP

| Código | Cuándo |
|---|---|
| 200 OK | Consultas y modificaciones correctas. |
| 201 Created | Creación (`POST`), con cabecera `Location`. |
| 204 No Content | Eliminación correcta. |
| 400 Bad Request | Campos inválidos (con detalle por campo), JSON mal escrito o regla de negocio incumplida (licencia vencida, viaje que no se puede finalizar, etc.). |
| 404 Not Found | El id o el endpoint no existe. |
| 405 Method Not Allowed | Método HTTP no soportado en esa URL. |
| 409 Conflict | Dato repetido: placa, identificación, licencia o ruta ya registradas, tarifas o turnos que se cruzan. |
| 500 Internal Server Error | Error no previsto (se registra en el log). |

### Endpoints

| Recurso | Endpoints |
|---|---|
| Vehículos | `GET/POST /api/vehiculos` (`?estadoOperativo=`), `GET/PUT/DELETE /api/vehiculos/{id}`, `PATCH /api/vehiculos/{id}/estado` |
| Rutas | `GET/POST /api/rutas` (`?origen=`), `GET/PUT/DELETE /api/rutas/{id}` |
| Pasajeros | `GET/POST /api/pasajeros`, `GET/PUT /api/pasajeros/{id}`, `PATCH /api/pasajeros/{id}/estado` |
| Conductores | `GET/POST /api/conductores` (`?estado=`), `GET/PUT /api/conductores/{id}`, `PATCH /api/conductores/{id}/estado` |
| Tarifas | `GET/POST /api/tarifas`, `GET /api/tarifas/vigente`, `GET/PUT /api/tarifas/{id}`, `PATCH /api/tarifas/{id}/desactivar` |
| Turnos | `GET/POST /api/turnos` (`?conductorId=`), `GET /api/turnos/{id}`, `PATCH /api/turnos/{id}/iniciar` y `/finalizar` |
| Viajes | `GET/POST /api/viajes` (`?estado=`, `?pasajeroId=`), `GET /api/viajes/{id}`, `PATCH /api/viajes/{id}/asignar`, `/iniciar`, `/finalizar`, `/cancelar` |
| Pagos | `GET /api/pagos`, `GET /api/pagos/{id}`, `PATCH /api/pagos/{id}/anular` |
| Sesión | `POST /api/auth/login`, `GET /api/auth/sesion`, `POST /api/auth/logout` |
| Usuarios (solo admin) | `GET/POST /api/usuarios`, `PATCH /api/usuarios/{id}/estado` |

**Permisos:** cualquier usuario con sesión consulta (GET) todo; `OPERADOR` además opera viajes, turnos y pasajeros; el resto es solo `ADMINISTRADOR`. Sin sesión → 401, sin permiso → 403; ambos traen un campo `pista` que dice qué hacer. Las reglas están en `seguridad/ConfiguracionSeguridad.java` (método `reglasDeAcceso`).

---

## 4. Instalación, configuración y ejecución

### 4.1 Requisitos

- **JDK 21 o superior** (el proyecto se probó con JDK 26).
- **Node.js** para el frontend.
- **MongoDB**: una base en **MongoDB Atlas** (recomendado) o un MongoDB local en el puerto 27017.
  No hace falta para correr las pruebas.

### 4.2 Configuración

1. Copia el archivo de ejemplo en la raíz del proyecto:
   ```powershell
   Copy-Item .env.example .env
   ```
2. En `.env`, pon tu cadena de conexión en `MONGODB_URI` (Atlas → *Database → Connect → Drivers*).
   Si no defines `MONGODB_URI`, el backend se conecta a `mongodb://localhost:27017/smartflet`.
3. `.env` está en `.gitignore`: las credenciales nunca se suben al repositorio.

| Variable | Descripción | Ejemplo |
|---|---|---|
| `MONGODB_URI` | Conexión a MongoDB | `mongodb+srv://<usuario>:<clave>@<cluster>.mongodb.net/smartflet` |
| `SERVER_PORT` | Puerto HTTP del backend | `8080` |

### 4.3 Ejecución

```powershell
# Backend (lee ../.env automáticamente)
cd backend
./mvnw spring-boot:run

# Pruebas (usan un MongoDB embebido; la primera vez descargan mongod, ~600 MB)
./mvnw test

# Frontend, en otra terminal
cd frontend
npm install   # solo la primera vez
npm run dev   # http://localhost:5173
```

### 4.4 Inicio de sesión

Al primer arranque se crea el administrador con `ADMIN_USUARIO` / `ADMIN_CLAVE` del `.env` (si `ADMIN_CLAVE` está vacía, se genera una y se imprime en el log). Desde **Usuarios** el administrador crea cuentas de operador. Las contraseñas se guardan con BCrypt y la sesión expira tras `SESION_MINUTOS` sin actividad; tras 5 intentos fallidos el usuario se bloquea 5 minutos.

### 4.5 Despliegue en AWS

Una instancia EC2 con Docker Compose (app + MongoDB). Scripts y explicación en [`despliegue/README.md`](despliegue/README.md).

---

## 5. Estructura de directorios

```
flota-de-taxis-SmartFlet/
├── README.md
├── .env.example                 # Variables de entorno de ejemplo (sin secretos)
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/smartflet/
│       │   ├── controlador/     # Controladores REST
│       │   ├── servicio/        # Reglas de negocio
│       │   ├── repositorio/     # Interfaces MongoRepository
│       │   ├── modelo/          # Documentos MongoDB y enumeraciones
│       │   ├── dto/             # Records de entrada (Solicitud) y salida (Respuesta)
│       │   └── excepcion/       # Excepciones de negocio y manejador global
│       ├── main/resources/application.yml
│       └── test/                # Pruebas de integración (MongoDB embebido)
├── frontend/                    # Todo el frontend; ver frontend/README.md
│   ├── index.html
│   └── src/
│       ├── api/                 # ClienteHttp y una llamada por endpoint del backend
│       ├── app/                 # Navegación entre pantallas, tema y menú
│       ├── componentes/         # Tablas, formularios, gráficas, avisos
│       ├── vistas/              # Una pantalla por módulo (viajes.js, turnos.js...)
│       └── estilos/             # app.css
└── docs/diagramas/              # 5 vistas de arquitectura (Visual Paradigm)
```

Pendiente: ubicación en tiempo real y mantenimiento de vehículos.

---

## 6. Historias de usuario

Formato: *Como [rol], quiero [funcionalidad], para [beneficio/valor].*

### Épica A — Gestión de flota

**HU-01 — Registrar vehículos**
Como **administrador**, quiero registrar los datos de cada taxi (placa, modelo, año, número interno), para tener un inventario actualizado de la flota.

- [ ] La placa es única; el sistema rechaza un registro con una placa existente.
- [ ] Todos los campos obligatorios se validan antes de guardar.
- [ ] El vehículo queda en estado "fuera de servicio" hasta que se active.

**HU-02 — Registrar conductores**
Como **administrador**, quiero registrar a los conductores con su licencia y datos de contacto, para controlar quién puede operar cada taxi.

- [ ] No se permite registrar un conductor con licencia vencida.
- [ ] Un conductor solo puede estar asignado a un vehículo activo a la vez.
- [ ] Se puede desactivar un conductor sin borrar su historial.

**HU-03 — Controlar turnos**
Como **administrador**, quiero asignar turnos a los conductores, para cubrir la operación en los horarios de mayor demanda.

- [ ] Un turno tiene fecha de inicio, fecha de fin y vehículo asociado.
- [ ] El sistema avisa si dos turnos se solapan para el mismo conductor o vehículo.
- [ ] El conductor puede consultar sus turnos programados.

### Épica B — Despacho de viajes

**HU-04 — Solicitar un taxi**
Como **pasajero**, quiero solicitar un taxi indicando mi punto de recogida, para llegar a mi destino sin tener que buscar un vehículo.

- [ ] La solicitud requiere punto de recogida válido dentro del área de servicio.
- [ ] Se muestra el tiempo estimado de llegada antes de confirmar.
- [ ] El pasajero recibe una confirmación con un identificador de viaje.

**HU-05 — Asignar automáticamente el taxi más cercano**
Como **operador de central**, quiero que el sistema asigne el taxi disponible más cercano a cada solicitud, para reducir el tiempo de espera del pasajero.

- [ ] Solo se consideran taxis en estado "disponible" con conductor activo.
- [ ] Si no hay taxis disponibles, la solicitud queda en cola y se notifica a la central.
- [ ] La asignación queda registrada con fecha, hora y vehículo.

**HU-06 — Asignar manualmente un taxi**
Como **operador de central**, quiero asignar manualmente un taxi a una solicitud, para atender casos especiales o emergencias.

- [ ] Solo se pueden asignar vehículos disponibles.
- [ ] La asignación manual queda marcada con el usuario que la realizó.
- [ ] Se puede reasignar un viaje mientras el taxi no haya recogido al pasajero.

**HU-07 — Responder a una asignación**
Como **conductor**, quiero recibir y aceptar o rechazar las asignaciones, para indicar si estoy en condiciones de atender el viaje.

- [ ] La asignación aparece en la app del conductor con datos de recogida.
- [ ] Si el conductor no responde en un tiempo límite, la asignación pasa al siguiente taxi.
- [ ] El estado del conductor cambia a "ocupado" al aceptar.

**HU-08 — Cerrar un viaje**
Como **conductor**, quiero marcar el inicio y el fin del viaje, para que el sistema registre la duración, distancia y tarifa correctas.

- [ ] Los estados del viaje siguen el orden: asignado → en curso → finalizado.
- [ ] Al finalizar, se calcula el precio según la tarifa vigente.
- [ ] El conductor vuelve a estado "disponible" al cerrar el viaje.

### Épica C — Seguimiento y control

**HU-09 — Ver la flota en tiempo real**
Como **operador de central**, quiero ver en un mapa la ubicación y el estado de cada taxi, para tomar decisiones de despacho rápidamente.

- [ ] El mapa se actualiza al menos cada 10 segundos.
- [ ] Cada taxi se muestra con un color según su estado.
- [ ] Al hacer clic en un taxi se muestra su conductor, viaje actual y última posición.

**HU-10 — Consultar el estado del viaje**
Como **pasajero**, quiero ver el estado y la ubicación de mi taxi, para saber cuánto falta para que llegue.

- [ ] El estado del viaje se actualiza sin recargar la página.
- [ ] Se muestra la hora estimada de llegada.
- [ ] Si el viaje se reasigna, el pasajero recibe una notificación.

**HU-11 — Registrar mantenimiento**
Como **administrador**, quiero registrar los mantenimientos de cada vehículo, para anticipar fallas y cumplir con la normativa.

- [ ] Cada mantenimiento registra fecha, tipo, costo y kilometraje.
- [ ] El sistema alerta cuando un vehículo supera el kilometraje límite de mantenimiento.
- [ ] Un vehículo en mantenimiento no puede ser asignado a viajes.

### Épica D — Reportes y administración

**HU-12 — Consultar reportes de operación**
Como **administrador**, quiero consultar reportes de viajes, kilometraje e ingresos por período, para evaluar el desempeño de la flota.

- [ ] Los reportes se pueden filtrar por fecha, vehículo y conductor.
- [ ] Los reportes se pueden exportar en formato CSV.
- [ ] Los totales del reporte coinciden con el detalle de viajes.

**HU-13 — Administrar tarifas**
Como **administrador**, quiero definir y modificar las tarifas, para ajustar los precios sin cambiar el código.

- [ ] Una tarifa tiene vigencia de fecha de inicio y fin.
- [ ] Los viajes ya finalizados conservan la tarifa con la que se cobraron.
- [ ] No pueden existir dos tarifas vigentes en el mismo horario.

**HU-14 — Iniciar sesión con roles**
Como **usuario del sistema**, quiero iniciar sesión con mi cuenta, para acceder solo a las funciones de mi rol.

- [ ] El acceso se valida con usuario y contraseña; las contraseñas se almacenan cifradas.
- [ ] Un usuario sin permiso recibe un error 403 al intentar acceder a una función restringida.
- [ ] La sesión expira tras un período de inactividad configurable.

---

## 7. Requerimientos funcionales

Prioridad según la escala **MoSCoW**: **Alta** (imprescindible para el MVP), **Media** (importante, puede postergarse), **Baja** (deseable).

### 7.1 Gestión de flota y conductores

| ID | Nombre | Descripción detallada | Prioridad |
|---|---|---|---|
| RF-01 | Registrar vehículo | El sistema debe permitir registrar un vehículo con placa, marca, modelo, año y número interno. La placa debe ser única. | Alta |
| RF-02 | Consultar y editar vehículo | El sistema debe permitir listar, buscar, editar y dar de baja lógica un vehículo. | Alta |
| RF-03 | Registrar conductor | El sistema debe permitir registrar un conductor con nombre, documento, número de licencia y fecha de vencimiento de la licencia. | Alta |
| RF-04 | Gestionar turnos | El sistema debe permitir programar turnos asociando conductor y vehículo, validando solapamientos. | Media |
| RF-05 | Registrar mantenimiento | El sistema debe registrar mantenimientos por vehículo y bloquear la asignación de vehículos en mantenimiento. | Media |

**Alcance:** Cubre el inventario de la flota y de los conductores. Es el punto de partida de los demás módulos, ya que sin vehículos y conductores activos no es posible despachar viajes. Los registros se desactivan de forma lógica para conservar el historial.

### 7.2 Solicitud y despacho de viajes

| ID | Nombre | Descripción detallada | Prioridad |
|---|---|---|---|
| RF-06 | Solicitar viaje | El sistema debe permitir crear una solicitud de viaje con punto de recogida y, opcionalmente, destino. | Alta |
| RF-07 | Asignar taxi automáticamente | El sistema debe asignar el taxi disponible más cercano al punto de recogida, considerando solo vehículos activos con conductor disponible. | Alta |
| RF-08 | Asignar taxi manualmente | El operador de central debe poder asignar o reasignar un taxi disponible a una solicitud. | Alta |
| RF-09 | Aceptar o rechazar asignación | El conductor debe poder aceptar o rechazar una asignación. Tras un tiempo límite sin respuesta, el sistema debe reasignar el viaje. | Alta |
| RF-10 | Gestionar estados del viaje | El sistema debe controlar los estados: solicitado, asignado, en curso, finalizado y cancelado, sin permitir transiciones inválidas. | Alta |
| RF-11 | Cancelar viaje | El sistema debe permitir cancelar un viaje antes de que inicie, registrando el motivo y quién lo canceló. | Media |

**Alcance:** Describe el flujo completo desde que el pasajero solicita un taxi hasta que el viaje termina. La asignación automática es el comportamiento principal; la manual sirve como respaldo para la central. Cada cambio de estado debe quedar registrado con fecha, hora y usuario responsable.

### 7.3 Ubicación y seguimiento

| ID | Nombre | Descripción detallada | Prioridad |
|---|---|---|---|
| RF-12 | Enviar ubicación del taxi | La app del conductor debe enviar su posición (latitud, longitud, velocidad) cada 10 segundos mientras esté en servicio. | Alta |
| RF-13 | Visualizar flota en mapa | El panel de central debe mostrar en un mapa la posición y el estado de cada taxi, con actualización automática. | Alta |
| RF-14 | Consultar estado del viaje | El pasajero debe poder consultar el estado de su viaje y la ubicación aproximada del taxi asignado. | Media |
| RF-15 | Notificar eventos | El sistema debe notificar a los usuarios afectados ante asignación, reasignación, llegada del taxi o cancelación. | Media |

**Alcance:** Garantiza la visibilidad de la operación en tiempo real. La frecuencia de envío de ubicación es un parámetro configurable; el valor por defecto equilibra precisión y consumo de datos del dispositivo.

### 7.4 Tarifas, reportes y seguridad

| ID | Nombre | Descripción detallada | Prioridad |
|---|---|---|---|
| RF-16 | Calcular tarifa del viaje | El sistema debe calcular el precio del viaje a partir de distancia, tiempo y la tarifa vigente en la fecha del viaje. | Alta |
| RF-17 | Administrar tarifas | El administrador debe poder crear, modificar y desactivar tarifas con vigencia definida. | Media |
| RF-18 | Generar reportes | El sistema debe generar reportes de viajes, kilometraje e ingresos filtrables por período, vehículo y conductor, con exportación a CSV. | Media |
| RF-19 | Autenticación y roles | El sistema debe autenticar usuarios con usuario y contraseña, y restringir las funciones según el rol (administrador, operador, conductor, pasajero). | Alta |
| RF-20 | Registro de auditoría | El sistema debe registrar las acciones sensibles (asignaciones manuales, cambios de tarifa, altas y bajas) con usuario, fecha y hora. | Baja |

**Alcance:** Agrupa las reglas económicas, la información para la toma de decisiones y los controles de acceso. La tarifa vigente debe quedar fijada en cada viaje para que los cambios futuros no alteren los cobros pasados.

---

## 8. Vistas de arquitectura

Las cinco vistas están modeladas en Visual Paradigm, en [`docs/diagramas/smartflet-diagramas.vpp`](docs/diagramas/smartflet-diagramas.vpp), a partir del código actual:

| # | Vista | Diagrama | Responde |
|---|---|---|---|
| 1 | Contexto | Diagrama de contexto (DFD nivel 0) | ¿Con quién intercambia información SmartFlet? |
| 2 | Funcional | Diagrama de componentes | ¿Qué módulos tiene y qué servicios se piden entre ellos? |
| 3 | Lógica | Diagrama de clases con estereotipos GRASP | ¿Qué clases del dominio hay y qué responsabilidad tiene cada una? |
| 4 | Desarrollo | Diagrama de clases por capas | ¿Cómo está organizado el código (controlador → servicio → repositorio)? |
| 5 | Despliegue | Diagrama de despliegue | ¿Dónde corre cada pieza y cómo se comunican? |

Los PNG exportados y la vista previa de cada una están en [`docs/diagramas/README.md`](docs/diagramas/README.md).

---
## Licencia

Por definir.
