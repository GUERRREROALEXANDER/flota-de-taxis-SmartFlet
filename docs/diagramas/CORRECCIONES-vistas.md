# Corrección de las 5 vistas de arquitectura (SmartFlet)

Todo está en `visual-paradigm/smartflet-diagramas.vpp` (Visual Paradigm 18.1). Los PNG de `imagenes/` se exportaron desde ese archivo.
La vista lógica y la de desarrollo se verificaron de forma automática contra el código: las 18 clases dibujadas tienen los mismos atributos, tipos, métodos y constructores que los `.java`.

## b) Pasos que faltan hacer a mano en Visual Paradigm

El conector MCP de VP no aplica colores ni oculta miembros, así que estos pasos se hacen a mano:

1. Abre `docs/diagramas/visual-paradigm/smartflet-diagramas.vpp`.
2. **Colores.** Selecciona las figuras de cada capa → clic derecho → *Styles and Formatting → Formats… → Fill* → color sólido:
   - Controlador: azul claro `#D6E9FF`
   - Servicio: verde claro `#DDF5DD`
   - Repositorio y persistencia (incluye GeneradorIds y AsignadorIdsMongo): amarillo claro `#FFF6CC`
   - Modelo (entidades, EntidadConId, Contador): naranja claro `#FFE2C2`
   - Enumeraciones: gris claro `#EEEEEE`
   - Frontend (vistas funcional y despliegue): morado claro `#EADCF8`
   - MongoDB (clase `MongoDB`, nodo Atlas, colecciones): gris `#D0D0D0`
   - Módulos y nodo del backend (funcional y despliegue): azul claro `#D6E9FF`
3. **Leyenda.** En las vistas lógica, de desarrollo, funcional y de despliegue hay una pila de notas "Leyenda". Pinta cada nota con el color que nombra su texto.
4. **Vista lógica, ViajeServicio.** Clic derecho → *Presentation Options → Attributes → Hide all*. Después, en *Operations → Customized…*, oculta `obtenerViaje(id : Long) : Viaje`, que es privado. Así queda solo con sus métodos públicos, como pide la consigna.
5. Si alguna clase de la vista de desarrollo vuelve a mostrar sus miembros: selecciónala → *Presentation Options → Attributes / Operations → Hide all*.
6. Guarda (Ctrl+S) y exporta cada diagrama: *Project → Export → Active Diagram as Image*, con PNG en la misma ruta de `imagenes/`.

## c) Qué explicar de cada vista (para el profesor)

**1. Contexto (DFD nivel 0).**
- SmartFlet es un solo proceso con 4 actores externos: Pasajero, Conductor, Operador de central y Administrador.
- Cada flujo lleva el endpoint REST que lo implementa.
- La nota deja claro que la autenticación por roles (HU-14) todavía no existe.

**2. Funcional (componentes).**
- Hay 8 módulos del backend, cada uno con su interfaz `/api/...`, y el frontend consume las 8 por HTTP/JSON.
- Las flechas `<<use>>` son solo las dependencias reales entre servicios, por ejemplo cuando Despacho de viajes valida pasajero, tarifa, vehículo y turno.
- Debajo de cada módulo está su colección MongoDB. Pagos no tiene colección propia, porque el pago va embebido en el viaje.

**3. Lógica (clases).**
- Muestra el dominio: 8 entidades con sus miembros reales, las enumeraciones y las multiplicidades.
- `Viaje` es Creador de `Pago` (composición), porque `finalizarViaje` lo crea con un constructor de paquete.
- `ViajeServicio` es el Controlador GRASP del caso de uso principal.
- `ViajeRepositorio` y `GeneradorIds` son Fabricación pura: separan la persistencia del dominio.

**4. Desarrollo (paquetes).**
- Muestra cómo está organizado el código: controlador → servicio → repositorio → MongoDB.
- Servicio y repositorio dependen de modelo.
- Las clases aparecen solo con su nombre; `dto` y `excepcion` van aparte porque son de apoyo.

**5. Despliegue.**
- El navegador carga el frontend desde Vite (:5173).
- Vite reenvía `/api` al jar de Spring Boot, que corre en la JVM 21 (:8080).
- El backend se conecta con el driver de MongoDB a la base `smartflet` en Atlas, que tiene 8 colecciones (7 de entidades más `contadores`, que guarda los IDs).

## d) Elemento del diagrama → código

| # | Elemento del diagrama | Archivo y método |
|---|---|---|
| 1 | Viaje «Creador» ◆ Pago | `modelo/Viaje.java:84` `finalizarViaje()` → `new Pago(...)` en la línea 89 |
| 2 | `~Pago(monto, metodoPago)`, constructor de paquete | `modelo/Pago.java:36` |
| 3 | Tarifa «Experto»: `calcularPrecio` | `modelo/Tarifa.java:54` |
| 4 | Estados de Viaje (`exigirEstado`) | `modelo/Viaje.java:102` |
| 5 | ViajeServicio «Controlador»: `solicitar` | `servicio/ViajeServicio.java:75` (busca la tarifa vigente en la línea 86) |
| 6 | Despacho `<<use>>` Turnos y Vehículos | `servicio/ViajeServicio.java:95` `asignarVehiculo()` (líneas 103 y 106) |
| 7 | Pagos `<<use>>` Despacho, sin colección | `servicio/PagoServicio.java:43` → `ViajeRepositorio.findByPagoId` (`repositorio/ViajeRepositorio.java:31`) |
| 8 | GeneradorIds «Fabricación pura» | `repositorio/GeneradorIds.java:38` `siguienteId()` |
| 9 | AsignadorIdsMongo (vista de desarrollo) | `repositorio/AsignadorIdsMongo.java:26` `onBeforeConvert()` |
| 10 | Vite proxy `/api` → :8080 (despliegue) | `frontend/vite.config.js:8-11` |
| 11 | Driver MongoDB → Atlas (despliegue) | `backend/src/main/resources/application.yml:17` (`MONGODB_URI`) |

## e) Qué se quitó o corrigió respecto a la versión anterior, y por qué

- **IDs:** pasaron de `String` a `Long` en todas las clases y parámetros, porque así están en el código (rama de IDs numéricos).
- **Constructores vacíos `#Clase()`:** se agregaron a las 8 entidades porque existen en el código (`protected`, los usa Spring Data).
- **Constructor `~Pago(...)`:** se marcó como de paquete, no público.
- **Vista lógica, lo que se quitó:** EntidadConId, Contador, AsignadorIdsMongo, excepciones, DTOs, controladores y DatosIniciales. No responden a "qué entidades y reglas tiene el dominio".
- **GRASP:**
  - Se quitó «Experto» de Vehiculo, Turno, Ruta y Pago, porque no tienen lógica propia que lo justifique.
  - Se quitó «Fabricación pura» de AsignadorIdsMongo; el único comentario GRASP del código está en GeneradorIds.
- **Ruta:** se agregó la nota que aclara que es un catálogo independiente; Viaje guarda origen y destino como texto.
- **Funcional:**
  - Se agregaron el módulo Rutas y las 8 interfaces.
  - Se dejaron solo los `<<use>>` reales.
  - Pagos quedó sin colección.
  - Se reordenaron los módulos para que ninguna línea se cruce.
- **Desarrollo:** las clases quedaron solo con su nombre y las flechas van entre paquetes, no entre clases. Así se entiende en 10 segundos.
- **Despliegue:** se agregaron Vite con el proxy, MongoDB Atlas con sus 8 colecciones (incluida `contadores`) y las etiquetas de protocolo.
- **Contexto:**
  - Los flujos ahora nombran sus endpoints.
  - Se agregó la nota de HU-14.
  - En el flujo del Administrador también aparecen pasajeros y "anular pago" (`PATCH /api/pagos/{id}/anular`), porque esos endpoints existen.
