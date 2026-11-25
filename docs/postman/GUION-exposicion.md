# Guion de la exposición con Postman: SmartFlet

**Duración:** unos 12 minutos · **Archivo:** `SmartFlet-exposicion.postman_collection.json`
**Servidor:** http://3.150.79.12 (AWS EC2, us-east-2)

---

## Antes de empezar (preparar la noche anterior o 10 min antes)

1. Abre http://3.150.79.12 en el navegador y comprueba que carga el login. Si no carga, revisa en la consola de AWS → EC2 que la instancia esté **Running**.
2. En Postman: **Import** → `SmartFlet-exposicion.postman_collection.json` (la copia del Escritorio ya trae la clave del admin).
3. Selecciona **No environment** arriba a la derecha: la colección trae sus propias variables.
4. **Ensayo**: clic derecho en la colección → **Run collection** → **Run**. Deben salir **26 peticiones en verde**. Cada ensayo agrega 1 vehículo, 1 conductor, 1 pasajero y 1 viaje a la demo; no pasa nada.
5. Ten abierta también la app web con la cuenta demo, para mostrar que lo que crea Postman aparece en el panel.
6. Si en Postman ves 401 donde no debería: ve a **Cookies** (debajo del botón Send), borra `3.150.79.12` y empieza desde la petición 01.

> Para cada petición: clic → **Send** → muestra el **código** (arriba a la derecha), el **JSON** y la pestaña **Test Results**.
> El ícono 📄 (Documentation) a la derecha tiene el texto de qué decir.

---

## Introducción (30 s)

> "SmartFlet es un sistema de gestión de flota de taxis. El backend es **Spring Boot** con **MongoDB**, organizado en capas
> **Controlador → Servicio → Repositorio**. Está desplegado en **AWS EC2** con **Docker Compose**: un contenedor con la app
> y otro con Mongo. Voy a probar la API REST con Postman siguiendo un caso real: registrar un taxi, ponerlo en turno y
> hacer un viaje completo hasta cobrarlo. Cada petición tiene **pruebas automáticas** que verifican la respuesta."

---

## Acto 1: Seguridad y login (2 min)

| # | Petición | Resultado esperado | Qué decir |
|---|---|---|---|
| 01 | `GET /vehiculos` sin sesión | **401** | "La API está protegida con Spring Security. Sin sesión responde 401, y en vez de un error feo devuelve JSON con un campo **pista** que dice qué hacer." |
| 02 | Login con clave equivocada | **401** | "El mensaje es genérico, *usuario o contraseña incorrectos*, para no revelar si el usuario existe. La pista dice cuántos intentos quedan: con 5 fallos se bloquea 5 minutos." |
| 03 | Login como admin | **200** | "Las contraseñas se guardan cifradas con **BCrypt**. Si coinciden, el servidor crea una sesión y manda la cookie **JSESSIONID**." → *muestra la pestaña **Cookies** de la respuesta*. "Es **HttpOnly**: JavaScript no la puede leer, así que un script malicioso no la puede robar." |
| 04 | `GET /auth/sesion` | **200** con rol ADMINISTRADOR | "Ya no mando usuario ni clave: Postman reenvía la cookie y el servidor sabe quién soy." |

## Acto 2: Consultar datos (1 min)

| # | Petición | Resultado | Qué decir |
|---|---|---|---|
| 05 | `GET /vehiculos` | **200**, lista | "La petición entra por **VehiculoControlador**, que delega en **VehiculoServicio**, y este usa **VehiculoRepositorio** (Spring Data MongoDB). La respuesta es un **DTO**, no la entidad: así no expongo campos internos." |
| 06 | `GET /tarifas/vigente` | **200** | "La tarifa vigente: valor base más valor por kilómetro. Con esto se cobra cada viaje." |

## Acto 3: Registrar y validar (2,5 min)

| # | Petición | Resultado | Qué decir |
|---|---|---|---|
| 07 | Registrar vehículo | **201 Created** | "Mando un JSON. La placa la genera Postman al azar para poder repetir la demo." |
| 08 | Misma placa otra vez | **409 Conflict** | "Regla de negocio: la placa es única. El servicio lanza una excepción y **ManejadorGlobalExcepciones** (`@RestControllerAdvice`) la convierte en un 409 con mensaje claro. Así los controladores no tienen try/catch." |
| 09 | Vehículo inválido | **400** | "**Bean Validation** (`@Valid`, `@NotBlank`, `@Pattern`…): el error dice **campo por campo** qué está mal: placa, modelo, capacidad. Ni siquiera llega al servicio." |
| 10 | Registrar conductor | **201** | "Con licencia vigente; sin ella no se le pueden programar turnos." |
| 11 | Registrar pasajero | **201** | |
| 12 | Pasajero con correo y teléfono malos | **400** | "Validación de formato con `@Email` y patrón de teléfono." |

## Acto 4: Turno (1,5 min)

| # | Petición | Resultado | Qué decir |
|---|---|---|---|
| 13 | Programar turno | **201**, PROGRAMADO | "El turno une un conductor con un vehículo en un horario." |
| 14 | Turno que se cruza | **409** | "No puede haber dos turnos del mismo conductor o vehículo a la misma hora." |
| 15 | Iniciar turno | **200**, EN_CURSO | "Es una **máquina de estados**: PROGRAMADO → EN_CURSO → FINALIZADO. Solo un vehículo con turno en curso puede recibir viajes." |

## Acto 5: Viaje completo y pago (3 min). Lo más importante

| # | Petición | Resultado | Qué decir |
|---|---|---|---|
| 16 | Solicitar viaje | **201**, SOLICITADO | "El viaje guarda una **copia de la tarifa** del momento: si mañana sube la tarifa, este viaje se cobra con la de hoy." |
| 17 | Asignar vehículo | **200**, ASIGNADO | "**ViajeServicio.asignarVehiculo** valida tres cosas: que el vehículo esté operativo, que tenga turno EN_CURSO y que no esté en otro viaje." |
| 18 | Iniciar viaje | **200**, EN_CURSO | "Registra la hora de inicio." |
| 19 | Cancelar viaje en curso | **400** | "Un viaje solo se cancela antes de iniciar. Esa regla vive en **Viaje.cancelarViaje**. Es el patrón **Experto** de GRASP: el viaje conoce su propio estado, así que él decide." |
| 20 | Finalizar y cobrar | **200**, FINALIZADO + pago | "**Viaje.finalizarViaje** crea el Pago, patrón **Creador**: el viaje tiene los datos para crearlo. El precio lo calcula **Tarifa.calcularPrecio**, otra vez Experto: valor base + valor por km × 12,5 km." → *muestra Test Results*: "La prueba verifica que el monto sea exactamente esa cuenta." |
| 21 | Consultar el pago | **200** | "El pago queda enlazado a su viaje." |

> 💡 Aquí cambia a la app web (cuenta demo) → **Panel**: el viaje y el ingreso nuevos ya aparecen.

## Acto 6: Roles y cierre (1,5 min)

| # | Petición | Resultado | Qué decir |
|---|---|---|---|
| 22 | Finalizar turno | **200** | "Cierro el turno del conductor." |
| 23 | Login como `demo` | **200**, rol OPERADOR | "Hay dos roles: **ADMINISTRADOR** y **OPERADOR**. El operador opera viajes, turnos y pasajeros, y consulta todo." |
| 24 | Operador crea vehículo | **403** | "Ahora sí hay sesión, pero el rol no alcanza: **403**, que es distinto del 401. La pista dice el rol actual. Todas las reglas de quién puede hacer qué están en un solo método: **ConfiguracionSeguridad.reglasDeAcceso**." |
| 25 | Cerrar sesión | **204** | "Invalida la sesión en el servidor." |
| 26 | Consultar después | **401** | "La cookie vieja ya no sirve. Fin." |

**Cierre (20 s):** clic derecho en la colección → **Run collection** → **Run**:
> "Y todo esto se puede ejecutar de una vez: 26 peticiones y 63 pruebas automáticas en verde."

---

## Preguntas probables del profesor

| Pregunta | Respuesta corta |
|---|---|
| ¿Por qué 401 y 403 son distintos? | 401 = no sé quién eres (sin sesión). 403 = sé quién eres pero tu rol no tiene permiso. |
| ¿Dónde se guardan las contraseñas? | En Mongo, colección `usuarios`, solo el **hash BCrypt** con sal. Ni el admin puede verlas; la API nunca las devuelve. |
| ¿Por qué sesión con cookie y no JWT? | Es más simple, el servidor puede cerrar la sesión al instante (logout real) y la cookie HttpOnly no la lee JavaScript. JWT sirve más cuando hay muchos servidores o apps móviles. |
| ¿Por qué desactivaron CSRF? | La cookie es **SameSite=Strict**: el navegador no la manda desde otro sitio, y la API solo acepta JSON. |
| ¿Qué es un DTO y por qué usarlo? | Objeto solo para entrada/salida de la API. Valida la entrada y evita exponer la entidad (ej. nunca sale `claveCifrada`). |
| ¿Dónde están las reglas de negocio? | En el **modelo** cuando dependen del propio objeto (Viaje, Tarifa: Experto) y en los **servicios** cuando necesitan consultar otras colecciones (turno en curso, placa repetida). |
| ¿Cómo manejan los errores? | Excepciones propias (`ReglaNegocioException`, etc.) → `ManejadorGlobalExcepciones` las traduce a 400/404/409 con el mismo formato JSON. |
| ¿Cómo está desplegado? | EC2 t3.small en AWS, Docker Compose (app + mongo), puerto 80, IP elástica. Sin SSH: se administra con **Systems Manager**. La clave del admin está cifrada en **Parameter Store**. |
| ¿Qué pasa si se cae la instancia? | Los contenedores tienen `restart: unless-stopped` y los datos de Mongo están en un volumen: al reiniciar vuelve todo solo. |
| ¿Hay pruebas aparte de Postman? | Sí: 29 pruebas JUnit 5 con MockMvc y Mongo embebido (`mvnw test`). |

## Plan B

- **No hay internet o AWS no responde:** corre el backend local (`mvnw spring-boot:run`) y en la colección, pestaña **Variables**, cambia `baseUrl` a `http://localhost:8080/api`. La clave local es la de tu `ADMIN_CLAVE`.
- **Una petición falla a mitad:** no pasa nada, explica el error con la pista que da la API y ejecuta desde la **03** (login) en adelante.
- **429 (bloqueado por intentos):** espera 5 minutos o usa la cuenta `demo` para mostrar lo demás.
