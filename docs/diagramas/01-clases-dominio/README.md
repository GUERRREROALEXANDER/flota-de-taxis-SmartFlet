# 01 — Modelo de Clases de Dominio: SmartFleet

Diagrama de clases de dominio del sistema de gestión de flota de taxis **SmartFleet**, adaptado a partir de la estructura de referencia de un modelo de dominio de gimnasio (Socio/Clase/Instructor/Reserva/Pago), reemplazando cada entidad por su equivalente real del negocio de transporte.

## Archivos

| Archivo | Contenido |
|---|---|
| `smartflet-clases-dominio.puml` | Código fuente PlantUML (autocontenido, sin `!include`). |
| `smartflet-clases-dominio.png` | Render validado del diagrama (PlantUML). |
| `smartflet-clases-dominio-vp.png` | El mismo diagrama exportado desde Visual Paradigm. |

## Equivalencia con la referencia

| Referencia (gimnasio) | SmartFleet | Motivo del cambio |
|---|---|---|
| Socio | **Pasajero** | Quien solicita el servicio. |
| Instructor | **Conductor** | Quien presta el servicio. |
| Clase | *(se elimina)* | No hay una "sesión" recurrente en un taxi; cada viaje es un evento único, no una clase programada con cupo. |
| Reserva | **Viaje** | La reserva y la prestación del servicio son el mismo evento en este dominio (no se separan en "reserva" + "sesión"). |
| Pago / Factura | **Pago** | Se mantiene, pero ligado al Viaje en vez de directamente al Pasajero. |
| Membresía | **Tarifa** | La vigencia por fechas se mantiene, pero aplica al precio del servicio, no a una suscripción. |
| *(no existía)* | **Vehículo** | Entidad obligatoria en una flota: el servicio no lo presta solo el conductor. |
| *(no existía)* | **Turno** | Representa qué conductor opera qué vehículo en qué período; sustituye la relación fija 1..1 Instructor–Clase de la referencia, que no es realista en una flota (un conductor puede operar distintos vehículos en distintos turnos). |

## Clases del modelo

| Clase | Responsabilidad |
|---|---|
| **Pasajero** | Persona que solicita viajes y los paga. |
| **Conductor** | Persona habilitada (licencia vigente) para operar un vehículo de la flota. |
| **Vehículo** | Unidad de la flota; tiene estado operativo y kilometraje. |
| **Turno** | Período en el que un conductor queda asociado a un vehículo específico. |
| **Viaje** | Evento de transporte: desde que se solicita hasta que finaliza o se cancela. |
| **Pago** | Cobro generado al finalizar un viaje. |
| **Tarifa** | Precio vigente (base + valor por km) aplicado a los viajes. |

## Multiplicidades

| Relación | Lectura |
|---|---|
| Pasajero **1** — **0..\*** Viaje | Un pasajero solicita cero o muchos viajes; cada viaje pertenece a un único pasajero. |
| Vehículo **0..1** — **0..\*** Viaje | Un viaje tiene cero o un vehículo asignado (cero mientras está "solicitado"); un vehículo atiende muchos viajes a lo largo del tiempo. |
| Conductor **1** — **0..\*** Turno | Un conductor cumple cero o muchos turnos; cada turno tiene un único conductor. |
| Vehículo **1** — **0..\*** Turno | Un vehículo es operado en cero o muchos turnos; cada turno tiene un único vehículo. |
| Viaje **1** ◆— **0..1** Pago | *Composición*: el pago depende del viaje (no existe sin él) y se genera solo al finalizar (0..1). |
| Tarifa **1** — **0..\*** Viaje | Una tarifa se aplica a cero o muchos viajes; cada viaje usa una única tarifa vigente. |

## Decisiones y supuestos

- **Se eliminó la asociación redundante** que en la referencia conectaba directamente Socio↔Clase además de la clase Reserva: en UML correcto, si Reserva ya es la entidad que vincula a ambos, no hace falta una segunda línea de asociación directa entre ellos (es información duplicada).
- **Solo se usó composición (◆) en Viaje–Pago**, porque es la única relación todo-parte real del modelo (el pago no tiene sentido ni existencia fuera del viaje que lo genera). El resto son asociaciones simples; forzar diamantes donde no corresponde es un error de notación común.
- **No se modeló el conductor de un viaje de forma directa.** Se obtiene indirectamente a través del turno vigente del vehículo asignado. Si el proyecto necesita trazabilidad exacta por viaje (por ejemplo, para auditoría), se puede añadir una asociación `Viaje "0..1" -- "0..1" Turno`.
- Los tipos de dato siguen la convención `atributo: Tipo` pedida, con `Integer`, `String`, `Double`, `Date` y `DateTime` según corresponda (sin tipos genéricos).

## Conexión con Visual Paradigm

El modelo está en Visual Paradigm, en [`../smartflet-diagramas.vpp`](../smartflet-diagramas.vpp), diagrama *"01 - Modelo de Clases de Dominio — SmartFleet (Gestión de Flota de Taxis)"*, con las mismas clases, atributos, operaciones y multiplicidades del `.puml`. Su exportación es `smartflet-clases-dominio-vp.png`.

El `.puml` sigue siendo autocontenido (sin `!include` ni `!define`) por si se quiere importar en otra herramienta: **File → Import → PlantUML**.

## Regenerar el PNG

```bash
java -DPLANTUML_LIMIT_SIZE=8192 -jar plantuml.jar -tpng smartflet-clases-dominio.puml
```

(Requiere `plantuml.jar`, descargable desde los releases de PlantUML en GitHub; no necesita Graphviz para este diagrama.)
