# Prompts para el Claude conectado a Visual Paradigm (MCP)

Ejecutar **en orden**. Al terminar cada prompt: guardar el proyecto, exportar el PNG y reportar antes de seguir.
Proyecto: `D:\Escritorio\flota de taxis SmartFlet` · VP: `docs\diagramas\smartflet-diagramas.vpp`.

Entrega final (7 diagramas):

| # | Diagrama | Estado |
|---|---|---|
| 01 | Clases (vista A: dominio · vista B: capas) | rehacer desde el código |
| 02 | Componentes | rehacer |
| 03 | Casos de uso | nuevo |
| 04 | Secuencia: Finalizar viaje y registrar pago | nuevo |
| 05 | Actividades: Asignar vehículo a un viaje | nuevo |
| 06 | Máquina de estados: Viaje | nuevo |
| 07 | Despliegue (local actual) | nuevo |

El antiguo "03 Modelo conceptual" **sale de la entrega**: duplicaba el diagrama de clases.

---

## PROMPT 0 — Contexto, reglas y auditoría (no dibuja)

```
Actúa como arquitecto de software senior especialista en UML y Visual Paradigm.
Proyecto: D:\Escritorio\flota de taxis SmartFlet (SmartFlet, gestión de flota de taxis).
Proyecto VP a usar (NO crees otro): docs\diagramas\smartflet-diagramas.vpp. Si existe el .lck, el proyecto está
abierto: trabaja sobre esa instancia.

REGLAS (para todos los prompts):
1. No inventes clases, métodos, atributos, relaciones, nodos ni tecnologías. Todo sale del código actual
   (backend\src\main\java\smartflet\**, frontend\src\**). Lo que sea mejora va con estereotipo «propuesta».
2. No modifiques código. No implementes MongoDB.
3. Todo como elementos UML editables en VP (nada de imágenes, Mermaid o texto). PlantUML/XMI solo como importación
   intermedia, verificada luego en VP. Si no puedes guardar el .vpp, dilo; no afirmes lo que no hiciste.
4. Español. Título en cada diagrama: "NN - Nombre — SmartFlet".
5. Si los PDF del profesor ("De la Web a una API REST con Spring Boot y MongoDB" y "Diagramas de despliegue UML")
   no están a tu alcance, pídeselos a Diego antes del Prompt 5. El ejemplo Editorial/Libro/Disco NO se copia.

STACK REAL: Java (target 21), Spring Boot 3.3.5 (Web, Data JPA/Hibernate, Validation), Maven (mvnw),
H2 EN MEMORIA embebida en la JVM (jdbc:h2:mem:smartflet, consola /h2-console). NO hay PostgreSQL ni MongoDB.
Frontend independiente: JavaScript puro MPA (index.html, vehiculos.html, rutas.html), Vite :5173 con proxy
/api -> :8080, Three.js, GSAP. Sin autenticación ni servicios externos.

CLASES DEL BACKEND (paquete smartflet):
- modelo (entidades JPA): Vehiculo, Ruta, Pasajero, Conductor, Turno, Tarifa, Viaje, Pago
- modelo (enumeraciones): EstadoRegistro{ACTIVO, INACTIVO}, EstadoTurno{PROGRAMADO, EN_CURSO, FINALIZADO},
  EstadoViaje{SOLICITADO, ASIGNADO, EN_CURSO, FINALIZADO, CANCELADO}, MetodoPago{EFECTIVO, TARJETA, TRANSFERENCIA},
  EstadoPago{PAGADO, ANULADO}
- repositorio (interfaces que extienden JpaRepository<T, Long>): Vehiculo-, Ruta-, Pasajero-, Conductor-, Turno-,
  Tarifa-, Viaje-, PagoRepositorio
- servicio (clases concretas, sin interfaces): Vehiculo-, Ruta-, Pasajero-, Conductor-, Turno-, Tarifa-, Viaje-,
  PagoServicio
- controlador (@RestController): Vehiculo-, Ruta-, Pasajero-, Conductor-, Turno-, Tarifa-, Viaje-, PagoControlador
- dto (records): *Solicitud, *Respuesta, EstadoVehiculoSolicitud, EstadoRegistroSolicitud,
  AsignacionVehiculoSolicitud, FinalizacionViajeSolicitud, ErrorRespuesta
- excepcion: RecursoNoEncontradoException, ReglaNegocioException (extienden RuntimeException),
  ManejadorGlobalExcepciones (@RestControllerAdvice)

TAREA: lee los PDF y el código, abre los diagramas existentes en VP y entrégame un informe de auditoría:
qué exigen los PDF, qué está mal en los diagramas actuales frente al código, y si la lista de 7 diagramas
(01 Clases, 02 Componentes, 03 Casos de uso, 04 Secuencia, 05 Actividades, 06 Estados, 07 Despliegue) cumple
lo pedido. Si los PDF piden otros diagramas, detente y repórtalo. No dibujes todavía.
```

---

## PROMPT 1 — 01 Diagrama de clases (prioridad máxima)

```
PASO PREVIO: crea en el modelo VP un paquete "Archivo - versión anterior" y mueve allí los diagramas viejos
01 (Pasajero/Turno/Tarifa con atributos públicos), 02 (Auth/Tiquetes) y 03 (Modelo conceptual), renombrados con
prefijo "OBSOLETO -". No reutilices sus clases: crea elementos nuevos dentro de paquetes UML que repliquen los
paquetes Java (smartflet::modelo, ::repositorio, ::servicio, ::controlador, ::dto, ::excepcion).

Construye el diagrama de clases leyendo CADA archivo .java. Dos vistas del mismo modelo (mismos elementos):

=== 01-A - Diagrama de Clases: Dominio — SmartFlet ===  (A3 horizontal)
Contenido: las 8 entidades + 5 enumeraciones + ReglaNegocioException.
Para cada entidad, con detalle completo:
- Atributos privados "-" con tipo exacto (Long, String, Integer, Double, Boolean, BigDecimal, LocalDate,
  LocalDateTime, y el enum correspondiente). Restricciones JPA como {unique}, {id}, {length=n} si aportan.
- Constructor protegido "#" sin parámetros (exigido por JPA) y constructor público con sus parámetros exactos.
  Excepción: Pago tiene constructor de paquete "~ Pago(viaje: Viaje, monto: BigDecimal, metodoPago: MetodoPago)".
- Getters y setters EXACTAMENTE como están en el código. No agregues los que no existen:
  · Ningún getId tiene setId.
  · Viaje, Turno y Pago NO tienen setters: su estado cambia solo con métodos de negocio (decisión de diseño:
    encapsulamiento de las transiciones). Anótalo en una nota.
  · Pasajero no tiene setFechaRegistro; Tarifa no tiene setEstado.
- Métodos de negocio con firma completa, p. ej.:
  Viaje: + asignarVehiculo(vehiculo: Vehiculo): void, + iniciarViaje(): void,
         + finalizarViaje(distanciaKm: Double, metodoPago: MetodoPago): Pago, + cancelarViaje(): void,
         - exigirEstado(esperado: EstadoViaje, accion: String): void
  Turno: + iniciarTurno(): void, + finalizarTurno(): void
  Tarifa: + calcularPrecio(distanciaKm: Double): BigDecimal, + desactivarTarifa(): void
  Pago: + anularPago(): void
  Conductor: + licenciaVigenteEn(fecha: LocalDate): boolean, + estaActivo(): boolean
  Pasajero: + estaActivo(): boolean
RELACIONES (exactamente estas, con multiplicidad y nombre de rol = nombre del atributo):
- Viaje "0..*" ---> "1" Pasajero          (asociación dirigida, rol pasajero)
- Viaje "0..*" ---> "0..1" Vehiculo       (rol vehiculo; null mientras está SOLICITADO)
- Viaje "0..*" ---> "1" Tarifa            (rol tarifa; tarifa fijada al solicitar)
- Viaje "1" ◆—— "0..1" Pago               (COMPOSICIÓN, bidireccional: roles pago / viaje. Justificación:
                                           el Pago solo lo crea Viaje.finalizarViaje, cascade ALL + orphanRemoval)
- Turno "0..*" ---> "1" Conductor ; Turno "0..*" ---> "1" Vehiculo
- Ruta: SIN relaciones (así está en el código). Nota: "Ruta es un catálogo de trayectos; Viaje guarda origen y
  destino como texto y no referencia a Ruta."
- Atributos de tipo enum: muéstralos como atributo tipado Y con asociación/dependencia hacia el enum
  (elige una forma y úsala igual en todo el diagrama).
- Viaje, Turno, Pago ..> ReglaNegocioException «throw»; Viaje ..> Pago «create».
Distribución: Viaje al centro; Pasajero arriba-izquierda, Tarifa arriba-derecha, Pago abajo, Vehiculo a la derecha
con Turno y Conductor debajo; enums en una franja inferior; Ruta aparte con su nota. Sin cruces de líneas.

=== 01-B - Diagrama de Clases: Arquitectura por capas — SmartFlet ===  (A3 horizontal)
Contenido: controladores, servicios, repositorios, ManejadorGlobalExcepciones, ErrorRespuesta,
JpaRepository<T, ID> (externo, gris) y las entidades como cajas SIN compartimentos (ya detalladas en 01-A).
- Controladores y servicios: atributo privado {readOnly} de la dependencia, constructor y TODOS los métodos
  públicos y privados con parámetros y retorno exactos (lee cada clase). En controladores anota verbo HTTP y ruta
  como nota o tagged value (p. ej. PATCH /api/viajes/{id}/finalizar).
- Repositorios: «interface», métodos derivados y @Query exactos; generalización hacia
  JpaRepository<Entidad, Long> con binding de plantilla.
- DTOs: muéstralos en un paquete dto como «record» con sus componentes y desde(...) {static}; sin setters,
  accesores sin "get". Si no caben legibles, haz una tercera vista "01-C - Clases: DTOs" en vez de amontonarlos.
RELACIONES:
- Controlador ---> Servicio (asociación dirigida, multiplicidad 1).
- Servicio ---> Repositorio(s) (asociación dirigida, 1). OJO, dependencias reales por constructor:
  VehiculoServicio -> VehiculoRepositorio, TurnoRepositorio, ViajeRepositorio
  TurnoServicio    -> TurnoRepositorio, ConductorRepositorio, VehiculoRepositorio
  ViajeServicio    -> ViajeRepositorio, PasajeroRepositorio, VehiculoRepositorio, TarifaRepositorio, TurnoRepositorio
  los demás servicios -> solo su repositorio.
- NO uses agregación/composición entre capas (las inyecta Spring).
- Servicios ..> excepciones «throw»; ManejadorGlobalExcepciones ..> ErrorRespuesta «create».
Distribución: columnas controlador | servicio | repositorio de izquierda a derecha, una fila por módulo
(Vehículo, Ruta, Pasajero, Conductor, Turno, Tarifa, Viaje, Pago), para minimizar cruces.

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
   (lollipop en puerto) y contiene internamente Controlador -> Servicio -> Repositorio:
   - Flota: Vehículos (/api/vehiculos) y Rutas (/api/rutas)
   - Pasajeros (/api/pasajeros) · Conductores (/api/conductores)
   - Turnos (/api/turnos) · Tarifas (/api/tarifas)
   - Despacho de viajes: Viajes (/api/viajes) y Pagos (/api/pagos)
   - Manejo de errores (ManejadorGlobalExcepciones): transversal, produce ErrorRespuesta (JSON)
   Interfaces de persistencia proporcionadas por cada módulo ("Persistencia de X", repositorios Spring Data JPA)
   y las REQUERIDAS reales entre módulos (ball-and-socket):
   - Despacho requiere Persistencia de pasajeros, vehículos, tarifas y turnos (ViajeServicio usa esos repositorios)
   - Turnos requiere Persistencia de conductores y vehículos
   - Flota (VehiculoServicio) requiere Persistencia de turnos y viajes (no borra vehículos con historial)
   Conectores internos: "llamada Java en proceso (inyección por constructor)".
   Nota: "Las interfaces de servicio son lógicas: en el código los servicios son clases concretas; los
   repositorios sí son interfaces (JpaRepository)."
3) Hibernate/JPA requiere "JDBC"; «database» H2 en memoria lo proporciona (más la Consola H2 por HTTP).
4) Conector frontend–backend: "HTTP/JSON REST (en desarrollo vía proxy Vite /api -> :8080)".
NO incluyas autenticación, tiquetes, MongoDB, PostgreSQL ni servicios externos.
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
  Finalizar viaje «include» Registrar pago (el pago se crea siempre al finalizar)
  Consultar viajes «extend» Filtrar viajes por estado (punto de extensión: filtro)
  Ver tablero de la flota «include» Consultar vehículos ; «include» Consultar rutas
  Subir/Bajar pasajero «extend» Ver tablero (solo en navegador, localStorage)
  Generalización de actores solo si la justificas; si no, no la pongas.
Notas: "Pasajeros, conductores, turnos, tarifas, viajes y pagos se usan hoy por la API REST; la interfaz web cubre
vehículos, rutas y tablero." No modeles pasos internos (validar placa, buscar tarifa vigente) como casos de uso.
Exporta PNG a docs\diagramas\03-casos-de-uso\.

=== 04 - Diagrama de Secuencia: Finalizar viaje y registrar pago — SmartFlet ===
Realiza "Finalizar viaje" + «include» "Registrar pago". Líneas de vida:
  Conductor (actor) · Cliente HTTP · :ViajeControlador · :ViajeServicio · :ViajeRepositorio · :Viaje ·
  :Tarifa · :Pago · :BD H2 · :ManejadorGlobalExcepciones
Mensajes:
  1. Conductor -> Cliente: finaliza viaje (distanciaKm, metodoPago)
  2. Cliente -> ViajeControlador: PATCH /api/viajes/{id}/finalizar  -> finalizar(id, solicitud) [@Valid]
  alt [cuerpo inválido] -> MethodArgumentNotValidException -> manejarValidacion -> 400 ErrorRespuesta{errores}
  else
   3. ViajeControlador -> ViajeServicio: finalizar(id, solicitud)
   4. ViajeServicio -> ViajeRepositorio: findById(id): Optional<Viaje>  -> BD H2: SELECT
   alt [no existe] -> RecursoNoEncontradoException -> manejarNoEncontrado -> 404
   else
    5. ViajeServicio -> Viaje: finalizarViaje(distanciaKm, metodoPago): Pago
    6. Viaje -> Viaje: exigirEstado(EN_CURSO, "finalizar")
    alt [estadoViaje != EN_CURSO] -> ReglaNegocioException -> manejarReglaNegocio -> 400
    else
     7. Viaje -> Tarifa: calcularPrecio(distanciaKm): BigDecimal
     8. Viaje -> «create» :Pago(viaje, monto, metodoPago)   (estado PAGADO)
     9. ViajeServicio -> ViajeRepositorio: save(viaje)  -> BD H2: UPDATE viaje + INSERT pago (cascade)
    10. ViajeServicio: ViajeRespuesta.desde(viaje)  -> 200 OK con pago{monto, estadoPago}
Fragmentos alt con guardas, activaciones, retornos punteados, mensajes numerados.
Verifica nombres contra ViajeControlador, ViajeServicio, Viaje, Tarifa, Pago. Exporta PNG a docs\diagramas\04-secuencia\.
```

---

## PROMPT 4 — 05 Actividades y 06 Estados

```
=== 05 - Diagrama de Actividades: Asignar vehículo a un viaje — SmartFlet ===
Particiones: Operador de central | Backend (ViajeControlador/ViajeServicio) | Entidad Viaje | BD H2
Flujo según ViajeServicio.asignarVehiculo y Viaje.asignarVehiculo:
 inicio -> [Operador] Elegir viaje y vehículo -> PATCH /api/viajes/{id}/asignar
 -> ¿vehiculoId presente? no -> 400 errores de campo -> fin
 -> [BD] buscar viaje  -> ¿existe? no -> 404 -> fin
 -> [BD] buscar vehículo -> ¿existe? no -> 404 -> fin
 -> ¿vehículo operativo (estadoOperativo = true)? no -> 400 "fuera de servicio" -> fin
 -> [BD] ¿tiene turno EN_CURSO? no -> 400 "no tiene un turno en curso" -> fin
 -> [BD] ¿atiende otro viaje ASIGNADO o EN_CURSO? sí -> 400 "ya está atendiendo otro viaje" -> fin
 -> [Viaje] ¿estado SOLICITADO o ASIGNADO? no -> 400 -> fin
 -> [Viaje] asignar vehículo, estado = ASIGNADO -> [BD] guardar -> 200 OK -> fin
Guardas en corchetes en cada salida de decisión; nodos de fusión donde converjan los errores.
Exporta PNG a docs\diagramas\05-actividades\.

=== 06 - Diagrama de Máquina de Estados: Viaje — SmartFlet ===
Estados = EstadoViaje: SOLICITADO, ASIGNADO, EN_CURSO, FINALIZADO, CANCELADO.
 inicial -> SOLICITADO : new Viaje(pasajero, tarifa, origen, destino) [pasajero activo y hay tarifa vigente]
 SOLICITADO -> ASIGNADO : asignarVehiculo(v) [vehículo operativo, con turno EN_CURSO y libre]
 ASIGNADO -> ASIGNADO : asignarVehiculo(v) / reasignación
 ASIGNADO -> EN_CURSO : iniciarViaje() / fechaInicio = ahora
 EN_CURSO -> FINALIZADO : finalizarViaje(distanciaKm, metodoPago) / fechaFin = ahora; crear Pago(monto = tarifa.calcularPrecio)
 SOLICITADO -> CANCELADO y ASIGNADO -> CANCELADO : cancelarViaje() / fechaFin = ahora
 FINALIZADO -> final ; CANCELADO -> final
Nota: "Cualquier otro evento en un estado no permitido lanza ReglaNegocioException (HTTP 400) y el estado no cambia."
Exporta PNG a docs\diagramas\06-estados-viaje\.
```

---

## PROMPT 5 — 07 Despliegue (usa el PDF de despliegue)

```
"07 - Diagrama de Despliegue (estado actual: ejecución local) — SmartFlet", con la notación del PDF.
«device» Computador de desarrollo (Windows 11)
 ├─ «executionEnvironment» Navegador web (Chrome/Edge): páginas index/vehiculos/rutas + JS/CSS; «artifact» localStorage
 ├─ «executionEnvironment» Node.js 26 └─ «executionEnvironment» Vite dev server :5173 (proxy /api)
 │     «artifact» smartflet-frontend 0.1.0
 └─ «executionEnvironment» JVM (JDK 26, bytecode 21) └─ «executionEnvironment» Spring Boot 3.3.5 + Tomcat embebido :8080
       «artifact» backend-0.1.0-SNAPSHOT (com.smartflet, mvnw spring-boot:run) · «artifact» application.yml
       «database» H2 en memoria "smartflet" DENTRO de la JVM (se borra al detener la app)
Rutas: Navegador–Vite «HTTP :5173» · Vite–Spring «HTTP/JSON REST, proxy /api» · Spring–H2 «JDBC embebido» ·
Navegador–Spring «HTTP /h2-console» (opcional). Clientes de API (Postman/curl) «HTTP/JSON» directo a :8080 para los
módulos sin interfaz web.
«manifest»: artefacto backend -> subsistema Backend del 02; artefacto frontend -> subsistema Frontend.
PROHIBIDO: nube, balanceadores, Docker, PostgreSQL o MongoDB Atlas como existentes.
SOLO si el PDF lo exige: diagrama aparte "07b - Despliegue propuesto con MongoDB Atlas «propuesta»" con nodo
separado "Clúster MongoDB Atlas (nube)" comunicado por «TCP/TLS mongodb+srv»; Atlas es un nodo distinto de Spring Boot.
Exporta PNG a docs\diagramas\07-despliegue\.
```

---

## PROMPT 6 — Validación final y entrega

```
Audita todo el proyecto VP y responde con evidencia (archivo:línea):
1. ¿Cada clase, atributo, constructor, método, parámetro y tipo de 01 existe en el código? ¿Getters/setters solo
   donde existen (sin setId; Viaje/Turno/Pago sin setters; records sin setters)?
2. ¿Relaciones correctas: composición solo Viaje–Pago, asociaciones dirigidas con multiplicidad, generalizaciones
   de repositorios y excepciones, dependencias estereotipadas? ¿Enums presentes?
3. ¿02 separa frontend/backend, muestra interfaces proporcionadas/requeridas reales, H2 y protocolos?
4. ¿07 refleja el entorno local con H2 dentro de la JVM?
5. ¿La secuencia 04 coincide con el caso de uso "Finalizar viaje" del 03 y con los métodos del 01?
6. ¿Estados del 06 = enum EstadoViaje? ¿Decisiones del 05 = reglas de ViajeServicio?
7. ¿Nombres idénticos entre diagramas? ¿Ningún diagrama repite a otro?
8. ¿Todos titulados, en smartflet-diagramas.vpp, editables? ¿Obsoletos en "Archivo - versión anterior"?
Corrige lo que falle. Guarda el .vpp y confirma la fecha de modificación en disco. Exporta PNG de cada diagrama y un
PDF único docs\diagramas\smartflet-diagramas.pdf. Actualiza docs\diagramas\README.md (tabla de los 7) y un README por
carpeta. Entrega: informe de cambios, lista de inconsistencias del proyecto, y cómo abrir/editar en VP.
```
