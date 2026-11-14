# Prompts para el Claude conectado a Visual Paradigm (MCP)

Ejecutar **en orden**. Al terminar cada prompt: guardar el proyecto, exportar el PNG y reportar antes de seguir.
Proyecto: `D:\Escritorio\flota de taxis SmartFlet` · VP: `docs\diagramas\smartflet-diagramas.vpp`.
Código de referencia: rama `migracion-mongodb` (backend en **Spring Data MongoDB**).

Entrega final (7 diagramas):

| # | Diagrama | Estado |
|---|---|---|
| 01 | Clases (vista A: dominio · vista B: capas) | rehacer desde el código |
| 02 | Componentes | rehacer |
| 03 | Casos de uso | nuevo |
| 04 | Secuencia: Finalizar viaje y registrar pago | nuevo |
| 05 | Actividades: Asignar vehículo a un viaje | nuevo |
| 06 | Máquina de estados: Viaje | nuevo |
| 07 | Despliegue | nuevo |

El antiguo "03 Modelo conceptual" **sale de la entrega**: duplicaba el diagrama de clases.

---

## PROMPT 0 — Contexto, reglas y auditoría (no dibuja)

```
Actúa como arquitecto de software senior especialista en UML y Visual Paradigm.
Proyecto: D:\Escritorio\flota de taxis SmartFlet (SmartFlet, gestión de flota de taxis), rama migracion-mongodb.
Proyecto VP a usar (NO crees otro): docs\diagramas\smartflet-diagramas.vpp.

REGLAS (para todos los prompts):
1. No inventes clases, métodos, atributos, relaciones, nodos ni tecnologías. Todo sale del código actual
   (backend\src\main\java\smartflet\**, frontend\src\**). Lo que sea mejora va con estereotipo «propuesta».
2. No modifiques código.
3. Todo como elementos UML editables en VP (nada de imágenes, Mermaid o texto). PlantUML/XMI solo como importación
   intermedia, verificada luego en VP. Si no puedes guardar el .vpp, dilo; no afirmes lo que no hiciste.
4. Español. Título en cada diagrama: "NN - Nombre — SmartFlet".
5. Usa los PDF del profesor ("De la Web a una API REST con Spring Boot y MongoDB" y "Diagramas de despliegue UML")
   como referencia de notación; el ejemplo Editorial/Libro/Disco NO se copia.

STACK REAL: Java (target 21), Spring Boot 3.3.5 (Web, Validation, Spring Data MongoDB), Maven (mvnw).
Base de datos: MongoDB, URI por variable de entorno MONGODB_URI (leída de .env); por defecto
mongodb://localhost:27017/smartflet. Pruebas con MongoDB embebido (flapdoodle). Ya NO hay JPA, H2 ni PostgreSQL.
Frontend independiente: JavaScript puro MPA (index.html, vehiculos.html, rutas.html), Vite :5173 con proxy
/api -> :8080, Three.js, GSAP. Sin autenticación.

CLASES DEL BACKEND (paquete smartflet):
- modelo, documentos @Document: Vehiculo, Ruta, Pasajero, Conductor, Turno, Tarifa, Viaje
- modelo, clase embebida (NO es documento): Pago (vive dentro de Viaje)
- modelo, enumeraciones: EstadoRegistro{ACTIVO, INACTIVO}, EstadoTurno{PROGRAMADO, EN_CURSO, FINALIZADO},
  EstadoViaje{SOLICITADO, ASIGNADO, EN_CURSO, FINALIZADO, CANCELADO}, MetodoPago{EFECTIVO, TARJETA, TRANSFERENCIA},
  EstadoPago{PAGADO, ANULADO}
- repositorio (interfaces que extienden MongoRepository<T, String>): Vehiculo-, Ruta-, Pasajero-, Conductor-,
  Turno-, Tarifa-, ViajeRepositorio. NO existe PagoRepositorio.
- servicio (clases concretas): Vehiculo-, Ruta-, Pasajero-, Conductor-, Turno-, Tarifa-, Viaje-, PagoServicio
- controlador (@RestController): Vehiculo-, Ruta-, Pasajero-, Conductor-, Turno-, Tarifa-, Viaje-, PagoControlador
- dto (records): *Solicitud, *Respuesta, EstadoVehiculoSolicitud, EstadoRegistroSolicitud,
  AsignacionVehiculoSolicitud, FinalizacionViajeSolicitud, ErrorRespuesta
- excepcion: RecursoNoEncontradoException (404), ReglaNegocioException (400), RecursoDuplicadoException (409),
  ManejadorGlobalExcepciones (@RestControllerAdvice)

TAREA: lee los PDF y el código, abre los diagramas existentes en VP y entrégame un informe de auditoría:
qué exigen los PDF, qué está mal en los diagramas actuales frente al código, y si la lista de 7 diagramas cumple
lo pedido. Si los PDF piden otros diagramas, detente y repórtalo. No dibujes todavía.
```

---

## PROMPT 1 — 01 Diagrama de clases (prioridad máxima)

```
PASO PREVIO: crea en el modelo VP un paquete "Archivo - versión anterior" y mueve allí los diagramas viejos
01 (Pasajero/Turno/Tarifa con atributos públicos), 02 (Auth/Tiquetes) y 03 (Modelo conceptual), renombrados con
prefijo "OBSOLETO -". No reutilices sus clases: crea elementos nuevos en paquetes UML que repliquen los paquetes
Java (smartflet::modelo, ::repositorio, ::servicio, ::controlador, ::dto, ::excepcion).

Construye el diagrama leyendo CADA archivo .java. Dos vistas del mismo modelo (mismos elementos):

=== 01-A - Diagrama de Clases: Dominio — SmartFlet ===  (A3 horizontal)
Contenido: 7 documentos + Pago + 5 enumeraciones + ReglaNegocioException.
- Estereotipo «Document» con la colección como tagged value (vehiculos, rutas, pasajeros, conductores, tarifas,
  turnos, viajes). Pago lleva «embedded» (no es documento).
- Atributos privados "-" con tipo exacto: id: String (ObjectId), String, Integer, Double, Boolean, BigDecimal
  {Decimal128}, LocalDate, LocalDateTime y los enums. Marca {unique} donde hay @Indexed(unique = true):
  Vehiculo.placa, Pasajero.identificacion, Conductor.identificacion, Conductor.numeroLicencia.
- Constructor protegido "#" sin parámetros y constructor público con parámetros exactos, p. ej.
  + Turno(conductorId: String, vehiculoId: String, fechaInicio: LocalDateTime, fechaFin: LocalDateTime)
  + Viaje(pasajeroId: String, tarifa: Tarifa, origen: String, destino: String)
  ~ Pago(monto: BigDecimal, metodoPago: MetodoPago)   <- visibilidad de paquete
- Getters y setters EXACTAMENTE como en el código:
  · Ningún getId tiene setId.
  · Viaje, Turno y Pago NO tienen setters (su estado cambia solo con métodos de negocio). Nota explicándolo.
  · Pasajero no tiene setFechaRegistro; Tarifa no tiene setEstado.
- Métodos de negocio con firma completa:
  Viaje: + asignarVehiculo(vehiculo: Vehiculo): void, + iniciarViaje(): void,
         + finalizarViaje(distanciaKm: Double, metodoPago: MetodoPago): Pago, + cancelarViaje(): void,
         - exigirEstado(esperado: EstadoViaje, accion: String): void
  Turno: + iniciarTurno(): void, + finalizarTurno(): void
  Tarifa: + calcularPrecio(distanciaKm: Double): BigDecimal, + desactivarTarifa(): void
  Pago: + anularPago(): void
  Conductor: + licenciaVigenteEn(fecha: LocalDate): boolean, + estaActivo(): boolean
  Pasajero: + estaActivo(): boolean
RELACIONES (refleja el modelo de MongoDB, exactamente así):
- Viaje "1" ◆—— "0..1" Pago         COMPOSICIÓN, navegable solo Viaje -> Pago (rol pago). Pago no conoce a Viaje.
                                    Justificación: subdocumento embebido; finalizarViaje lo crea.
- Viaje "0..*" ———> "1" Tarifa      asociación dirigida, rol tarifa, estereotipo «copia embebida»:
                                    el viaje guarda una copia de la tarifa vigente al solicitar.
- Viaje ..> Pasajero, Viaje ..> Vehiculo, Turno ..> Conductor, Turno ..> Vehiculo:
  DEPENDENCIAS con estereotipo «referencia por id» (en el código son atributos String: pasajeroId, vehiculoId,
  conductorId). No las dibujes como asociaciones con objetos, porque el código no guarda el objeto.
  Viaje.asignarVehiculo(vehiculo: Vehiculo) usa el Vehiculo como parámetro (dependencia «use»).
- Ruta: SIN relaciones. Nota: "Ruta es un catálogo de trayectos; Viaje guarda origen y destino como texto."
- Enums: atributo tipado + dependencia hacia el enum (misma forma en todo el diagrama).
- Viaje, Turno, Pago ..> ReglaNegocioException «throw»; Viaje ..> Pago «create».
Nota general: "Modelo documental (MongoDB): composición = subdocumento embebido; dependencias «referencia por id»
= campo String con el _id de otro documento."
Distribución: Viaje al centro con Pago debajo y Tarifa arriba; Pasajero a la izquierda; Vehiculo a la derecha con
Turno y Conductor debajo; enums en franja inferior; Ruta aparte. Sin cruces de líneas.

=== 01-B - Diagrama de Clases: Arquitectura por capas — SmartFlet ===  (A3 horizontal)
Contenido: controladores, servicios, repositorios, ManejadorGlobalExcepciones, ErrorRespuesta, las 3 excepciones,
MongoRepository<T, ID> (externo, gris) y los documentos como cajas SIN compartimentos (detallados en 01-A).
- Controladores y servicios: atributo privado {readOnly} por cada dependencia, constructor y TODOS los métodos
  públicos y privados con parámetros y retorno exactos (ids son String). En controladores, verbo HTTP y ruta como
  nota o tagged value.
- Repositorios «interface»: métodos derivados, @Query (TarifaRepositorio: buscarVigentes, existeSolapamiento) y
  métodos default (TurnoRepositorio: conductorTieneTurnoSolapado, vehiculoTieneTurnoSolapado); generalización
  hacia MongoRepository<Documento, String> con binding de plantilla.
- DTOs: si no caben legibles, haz una tercera vista "01-C - Clases: DTOs" («record», componentes, desde(...) {static},
  sin setters, accesores sin "get").
RELACIONES:
- Controlador ---> Servicio (asociación dirigida, 1).
- Servicio ---> Repositorio(s) (asociación dirigida, 1), según los constructores reales:
  VehiculoServicio -> VehiculoRepositorio, TurnoRepositorio, ViajeRepositorio
  TurnoServicio    -> TurnoRepositorio, ConductorRepositorio, VehiculoRepositorio
  ViajeServicio    -> ViajeRepositorio, PasajeroRepositorio, VehiculoRepositorio, TarifaRepositorio, TurnoRepositorio
  PagoServicio     -> ViajeRepositorio   (el pago se lee y guarda dentro del viaje)
  los demás        -> solo su repositorio.
- Sin agregación/composición entre capas (las inyecta Spring).
- Servicios ..> excepciones «throw»; ManejadorGlobalExcepciones ..> ErrorRespuesta «create» y ..> cada excepción «handle».
Distribución: columnas controlador | servicio | repositorio, una fila por módulo.

VERIFICA 1 a 1 contra el código antes de terminar. Exporta PNG a docs\diagramas\01-clases\.
```

---

## PROMPT 2 — 02 Diagrama de componentes

```
"02 - Diagrama de Componentes — SmartFlet". Izquierda -> derecha: cliente, backend, datos.

1) «subsystem» Frontend SmartFlet (JS + Vite MPA). Componentes: Páginas (tablero.js, vehiculos.js, rutas.js),
   Componentes UI, Escena 3D (Three.js + GSAP), Servicios frontend (flotaServicio, ocupacionServicio, formatos),
   Cliente API (clienteHttp, vehiculosApi, rutasApi). Dependencias solo hacia abajo.
   Requiere: "API Vehículos" y "API Rutas" (únicas que usa hoy) y «external» Web Storage (localStorage).
   Nota: "El frontend aún no consume las APIs de pasajeros, conductores, turnos, tarifas, viajes ni pagos."

2) «subsystem» Backend SmartFlet (Spring Boot 3.3.5). Un componente por módulo; cada uno proporciona su API REST
   (lollipop en puerto) y contiene Controlador -> Servicio -> Repositorio:
   - Flota: Vehículos (/api/vehiculos) y Rutas (/api/rutas)
   - Pasajeros (/api/pasajeros) · Conductores (/api/conductores)
   - Turnos (/api/turnos) · Tarifas (/api/tarifas)
   - Despacho de viajes: Viajes (/api/viajes) y Pagos (/api/pagos)
   - Manejo de errores (ManejadorGlobalExcepciones): transversal, produce ErrorRespuesta (JSON; 400/404/405/409/500)
   Interfaces de persistencia proporcionadas ("Persistencia de X", repositorios Spring Data MongoDB) y las
   REQUERIDAS reales entre módulos (ball-and-socket):
   - Despacho requiere Persistencia de pasajeros, vehículos, tarifas y turnos (ViajeServicio)
   - Pagos usa la Persistencia de viajes (el pago está embebido en el viaje)
   - Turnos requiere Persistencia de conductores y vehículos
   - Flota (VehiculoServicio) requiere Persistencia de turnos y viajes (no borra vehículos con historial)
   Conectores internos: "llamada Java en proceso (inyección por constructor)".
   Nota: "Las interfaces de servicio son lógicas (servicios = clases concretas); los repositorios sí son
   interfaces (MongoRepository)."
3) Componente Spring Data MongoDB + MongoDB Java Driver: requiere "Protocolo MongoDB".
   «database» MongoDB (colecciones vehiculos, rutas, pasajeros, conductores, tarifas, turnos, viajes) lo proporciona.
4) Conector frontend–backend: "HTTP/JSON REST (en desarrollo vía proxy Vite /api -> :8080)".
NO incluyas autenticación, tiquetes, H2, PostgreSQL ni servicios que no existan.
Fondos suaves distintos por subsistema. Exporta PNG a docs\diagramas\02-componentes\.
```

---

## PROMPT 3 — 03 Casos de uso y 04 Secuencia

```
=== 03 - Diagrama de Casos de Uso — SmartFlet ===
Límite: "SmartFlet". Actores (no hay login: los roles son conceptuales, nota obligatoria):
Administrador de flota, Operador de central, Conductor, Pasajero.
Casos de uso (solo lo implementado):
  Administrador: Gestionar vehículos · Gestionar rutas · Gestionar conductores · Gestionar pasajeros ·
                 Administrar tarifas · Programar turnos · Anular pago · Ver tablero de la flota
  Operador:      Asignar vehículo a viaje · Cancelar viaje · Consultar viajes
  Conductor:     Iniciar turno · Finalizar turno · Iniciar viaje · Finalizar viaje
  Pasajero:      Solicitar viaje · Consultar historial de viajes · Cancelar viaje
Relaciones:
  Finalizar viaje «include» Registrar pago
  Consultar viajes «extend» Filtrar viajes por estado (punto de extensión: filtro)
  Ver tablero de la flota «include» Consultar vehículos ; «include» Consultar rutas
  Subir pasajero / Bajar pasajero «extend» Ver tablero (solo en navegador, localStorage)
Nota: "Pasajeros, conductores, turnos, tarifas, viajes y pagos se usan hoy por la API REST; la interfaz web cubre
vehículos, rutas y tablero." No modeles pasos internos (validar placa, buscar tarifa vigente) como casos de uso.
Exporta PNG a docs\diagramas\03-casos-de-uso\.

=== 04 - Diagrama de Secuencia: Finalizar viaje y registrar pago — SmartFlet ===
Líneas de vida: Conductor (actor) · Cliente HTTP · :ViajeControlador · :ViajeServicio · :ViajeRepositorio ·
  :Viaje · :Tarifa (copia embebida) · :Pago · :MongoDB · :ManejadorGlobalExcepciones
Mensajes:
  1. Conductor -> Cliente: finaliza viaje (distanciaKm, metodoPago)
  2. Cliente -> ViajeControlador: PATCH /api/viajes/{id}/finalizar -> finalizar(id: String, solicitud) [@Valid]
  alt [cuerpo inválido] MethodArgumentNotValidException -> manejarValidacion -> 400 ErrorRespuesta{errores}
  else
   3. ViajeControlador -> ViajeServicio: finalizar(id, solicitud)
   4. ViajeServicio -> ViajeRepositorio: findById(id): Optional<Viaje> -> MongoDB: find en "viajes"
   alt [no existe] RecursoNoEncontradoException -> manejarNoEncontrado -> 404
   else
    5. ViajeServicio -> Viaje: finalizarViaje(distanciaKm, metodoPago): Pago
    6. Viaje -> Viaje: exigirEstado(EN_CURSO, "finalizar")
    alt [estadoViaje != EN_CURSO] ReglaNegocioException -> manejarReglaNegocio -> 400
    else
     7. Viaje -> Tarifa: calcularPrecio(distanciaKm): BigDecimal   (tarifa copiada al solicitar)
     8. Viaje -> «create» :Pago(monto, metodoPago)   (estado PAGADO)
     9. ViajeServicio -> ViajeRepositorio: save(viaje) -> MongoDB: reemplaza UN documento "viajes" con el pago embebido
    10. ViajeRespuesta.desde(viaje) -> 200 OK con pago{id, monto, estadoPago}
Nota: "Una sola escritura atómica: no hace falta transacción."
Verifica nombres contra el código. Exporta PNG a docs\diagramas\04-secuencia\.
```

---

## PROMPT 4 — 05 Actividades y 06 Estados

```
=== 05 - Diagrama de Actividades: Asignar vehículo a un viaje — SmartFlet ===
Particiones: Operador de central | Backend (ViajeControlador/ViajeServicio) | Documento Viaje | MongoDB
Flujo (ViajeServicio.asignarVehiculo y Viaje.asignarVehiculo):
 inicio -> [Operador] Elegir viaje y vehículo -> PATCH /api/viajes/{id}/asignar
 -> ¿vehiculoId presente? no -> 400 errores de campo -> fin
 -> [MongoDB] buscar viaje -> ¿existe? no -> 404 -> fin
 -> [MongoDB] buscar vehículo -> ¿existe? no -> 404 -> fin
 -> ¿vehículo operativo? no -> 400 "fuera de servicio" -> fin
 -> [MongoDB] ¿tiene turno EN_CURSO? no -> 400 "no tiene un turno en curso" -> fin
 -> [MongoDB] ¿atiende otro viaje ASIGNADO o EN_CURSO? sí -> 400 "ya está atendiendo otro viaje" -> fin
 -> [Viaje] ¿estado SOLICITADO o ASIGNADO? no -> 400 -> fin
 -> [Viaje] guardar vehiculoId, estado = ASIGNADO -> [MongoDB] guardar documento -> 200 OK -> fin
Guardas en corchetes; nodos de fusión donde converjan los errores. Exporta PNG a docs\diagramas\05-actividades\.

=== 06 - Diagrama de Máquina de Estados: Viaje — SmartFlet ===
Estados = EstadoViaje: SOLICITADO, ASIGNADO, EN_CURSO, FINALIZADO, CANCELADO.
 inicial -> SOLICITADO : new Viaje(pasajeroId, tarifa, origen, destino) [pasajero activo y hay tarifa vigente]
 SOLICITADO -> ASIGNADO : asignarVehiculo(v) [vehículo operativo, con turno EN_CURSO y libre]
 ASIGNADO -> ASIGNADO : asignarVehiculo(v) / reasignación
 ASIGNADO -> EN_CURSO : iniciarViaje() / fechaInicio = ahora
 EN_CURSO -> FINALIZADO : finalizarViaje(distanciaKm, metodoPago) / fechaFin = ahora; crear Pago embebido
 SOLICITADO -> CANCELADO y ASIGNADO -> CANCELADO : cancelarViaje() / fechaFin = ahora
 FINALIZADO -> final ; CANCELADO -> final
Nota: "Cualquier otro evento lanza ReglaNegocioException (HTTP 400) y el estado no cambia."
Exporta PNG a docs\diagramas\06-estados-viaje\.
```

---

## PROMPT 5 — 07 Despliegue (usa el PDF de despliegue)

```
"07 - Diagrama de Despliegue — SmartFlet", con la notación del PDF. Distingue nodos, entornos de ejecución y
artefactos; MongoDB es un nodo/entorno DISTINTO de la aplicación Spring Boot.

«device» Computador de desarrollo (Windows 11)
 ├─ «executionEnvironment» Navegador web: páginas index/vehiculos/rutas + JS/CSS; «artifact» localStorage
 ├─ «executionEnvironment» Node.js 26 └─ «executionEnvironment» Vite dev server :5173 (proxy /api)
 │     «artifact» smartflet-frontend 0.1.0
 └─ «executionEnvironment» JVM (JDK 26, bytecode 21) └─ «executionEnvironment» Spring Boot 3.3.5 + Tomcat :8080
       «artifact» backend-0.1.0-SNAPSHOT.jar · «artifact» application.yml · «artifact» .env (MONGODB_URI, no versionado)

Nodo de base de datos — dibuja el que esté CONFIGURADO cuando hagas el diagrama (pregúntale a Diego):
 a) Si .env tiene una URI de Atlas: «device» "MongoDB Atlas (nube, cluster)" con «executionEnvironment» MongoDB
    y «database» smartflet (colecciones: vehiculos, rutas, pasajeros, conductores, tarifas, turnos, viajes).
    Ruta: Spring Boot — Atlas «TCP/TLS, protocolo MongoDB (mongodb+srv)».
 b) Si no: «executionEnvironment» "mongod 7.0 (local) :27017" en el mismo computador, con la misma «database».
    Ruta: «TCP 27017, protocolo MongoDB». Nota: "Destino previsto: MongoDB Atlas al configurar MONGODB_URI."
Rutas restantes: Navegador–Vite «HTTP :5173» · Vite–Spring «HTTP/JSON REST, proxy /api».
«manifest»: jar -> subsistema Backend del 02; artefacto frontend -> subsistema Frontend.
PROHIBIDO: balanceadores, Docker, servidores que no existan, H2 o PostgreSQL.
Exporta PNG a docs\diagramas\07-despliegue\.
```

---

## PROMPT 6 — Validación final y entrega

```
Audita todo el proyecto VP y responde con evidencia (archivo:línea):
1. ¿Cada clase, atributo, constructor, método, parámetro y tipo de 01 existe en el código (ids String)?
   ¿Getters/setters solo donde existen?
2. ¿Composición solo Viaje–Pago; Tarifa como «copia embebida»; referencias por id como dependencias; enums;
   generalizaciones hacia MongoRepository y RuntimeException?
3. ¿02 separa frontend/backend, interfaces proporcionadas/requeridas reales, MongoDB y protocolos?
4. ¿07 refleja la configuración real de MongoDB (Atlas o local) como nodo distinto de Spring Boot?
5. ¿La secuencia 04 coincide con "Finalizar viaje" del 03 y con los métodos del 01?
6. ¿Estados del 06 = enum EstadoViaje? ¿Decisiones del 05 = reglas de ViajeServicio?
7. ¿Nombres idénticos entre diagramas? ¿Ningún diagrama repite a otro?
8. ¿Todos titulados, en smartflet-diagramas.vpp, editables? ¿Obsoletos en "Archivo - versión anterior"?
Corrige lo que falle. Guarda el .vpp y confirma la fecha de modificación en disco. Exporta PNG de cada diagrama y un
PDF único docs\diagramas\smartflet-diagramas.pdf. Actualiza docs\diagramas\README.md (tabla de los 7) y un README por
carpeta. Entrega: informe de cambios, lista de inconsistencias del proyecto, y cómo abrir/editar en VP.
```
