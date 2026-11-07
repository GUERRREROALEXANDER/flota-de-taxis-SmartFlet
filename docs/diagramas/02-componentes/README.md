# 02 — Diagrama de Componentes: SmartFleet

Vista de los componentes del sistema, las interfaces que cada uno ofrece y las que necesita.

## Archivos

| Archivo | Contenido |
|---|---|
| `smartflet-componentes.puml` | Código fuente PlantUML (autocontenido). |
| `smartflet-componentes.png` | Diagrama exportado desde Visual Paradigm. |

El modelo editable está en [`../smartflet-diagramas.vpp`](../smartflet-diagramas.vpp), diagrama *"02 - Diagrama de Componentes — SmartFleet"*.

## Componentes

| Componente | Interfaz proporcionada | Puerto | Depende de |
|---|---|---|---|
| Autenticación y Seguridad | `IAuthService` | `pAuth` | — |
| Gestión de Vehículos | `IVehicleService` | `pVeh` | `IDBConnector` (puerto `pVehDB`) |
| Gestión de Rutas | `IRouteService` | `pRut` | `IDBConnector` (puerto `pRutDB`) |
| Emisión de Tiquetes y Asientos | `ITicketingService` | `pTiq` | `IAuthService`, `IVehicleService`, `IRouteService`, `IDBConnector` |
| Capa de Persistencia | `IDBConnector` | `pDB` | — |

## Notación

- Interfaz proporcionada: círculo (*lollipop*) unido con línea continua al puerto del componente.
- Interfaz requerida: dependencia en línea discontinua con flecha hacia la interfaz.
- Todo el sistema se agrupa en el paquete **SmartFleet System**.

## Decisiones

- **Emisión de Tiquetes y Asientos** es el componente que orquesta: consume los servicios de autenticación, vehículos, rutas y persistencia, y no lo usa ningún otro componente de negocio.
- Los componentes de negocio solo acceden a datos a través de `IDBConnector`, así que ninguno depende de la base de datos concreta.
