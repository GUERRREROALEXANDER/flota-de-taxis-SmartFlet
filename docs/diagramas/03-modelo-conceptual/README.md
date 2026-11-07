# 03 — Modelo Conceptual y de Dominio: SmartFleet

Modelo conceptual con las entidades del negocio, sus atributos tipados, la herencia y las asociaciones dirigidas.

## Archivos

| Archivo | Contenido |
|---|---|
| `smartflet-modelo-conceptual.puml` | Código fuente PlantUML (autocontenido). |
| `smartflet-modelo-conceptual.png` | Diagrama exportado desde Visual Paradigm. |

El modelo editable está en [`../smartflet-diagramas.vpp`](../smartflet-diagramas.vpp), diagrama *"03 - Modelo Conceptual y de Dominio — SmartFleet"*.

## Clases

| Clase | Atributos principales | Operación |
|---|---|---|
| **Usuario** | id, nombre, email, telefono, rol | `autenticar(): boolean` |
| **Conductor** (hereda de Usuario) | licencia, estado | `verificarDisponibilidad(): boolean` |
| **Vehiculo** | id, placa, modelo, capacidad, tipo, estadoOperativo | `actualizarEstado(): void` |
| **Ruta** | id, origen, destino, distanciaKm, duracionEstimada | — |
| **Viaje** | id, fechaHoraSalida, precioBase, estado | `calcularTarifa(): float` |
| **Tiquete** | id, numeroAsiento, fechaReserva, estado, precioTotal | `confirmarReserva(): void` |
| **Pago** | id, monto, fechaPago, metodoPago, estado | `procesarPago(): boolean` |

## Relaciones

| Relación | Lectura |
|---|---|
| Usuario ◁— Conductor | Un conductor es un usuario del sistema (herencia). |
| Conductor **1** → **0..1** Vehiculo | Un conductor opera como máximo un vehículo a la vez. |
| Vehiculo **1** → **0..\*** Viaje | Un vehículo atiende cero o muchos viajes. |
| Ruta **1** → **0..\*** Viaje | Una ruta define el trayecto de cero o muchos viajes. |
| Viaje **1** → **0..\*** Tiquete | Un viaje genera cero o muchos tiquetes (uno por asiento vendido). |
| Usuario **1** → **1..\*** Tiquete | Un usuario realiza la reserva de uno o más tiquetes. |
| Tiquete **1** → **1** Pago | Cada tiquete tiene asociado exactamente un pago. |

## Decisiones

- Las asociaciones son **dirigidas** (→) para indicar la navegabilidad desde la clase que conoce a la otra.
- En Visual Paradigm las clases están dentro del paquete **SmartFleet - Modelo Conceptual**, para que no se mezclen con las clases del mismo nombre del diagrama 01 (Vehiculo, Viaje, Pago), que tienen otros atributos.
