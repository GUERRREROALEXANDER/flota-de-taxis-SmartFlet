package smartflet.configuracion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import smartflet.dto.ConductorSolicitud;
import smartflet.dto.PasajeroSolicitud;
import smartflet.dto.RutaSolicitud;
import smartflet.dto.TarifaSolicitud;
import smartflet.dto.TurnoSolicitud;
import smartflet.dto.VehiculoSolicitud;
import smartflet.modelo.EstadoRegistro;
import smartflet.repositorio.ConductorRepositorio;
import smartflet.repositorio.PasajeroRepositorio;
import smartflet.repositorio.RutaRepositorio;
import smartflet.repositorio.TarifaRepositorio;
import smartflet.repositorio.TurnoRepositorio;
import smartflet.repositorio.VehiculoRepositorio;
import smartflet.servicio.ConductorServicio;
import smartflet.servicio.PasajeroServicio;
import smartflet.servicio.RutaServicio;
import smartflet.servicio.TarifaServicio;
import smartflet.servicio.TurnoServicio;
import smartflet.servicio.VehiculoServicio;

/**
 * Carga datos de ejemplo al arrancar, para probar la API en Postman sin crear nada a mano.
 *
 * - Solo llena una coleccion si esta VACIA: reiniciar el backend no duplica los datos.
 * - Usa los mismos servicios que la API, asi los datos pasan por las mismas validaciones
 *   y reciben IDs 1, 2, 3... en orden.
 * - Se apaga con  smartflet.datos-iniciales=false  (las pruebas automaticas lo apagan).
 *
 * Los datos son ficticios.
 */
@Component
@ConditionalOnProperty(name = "smartflet.datos-iniciales", havingValue = "true", matchIfMissing = true)
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final ConductorServicio conductorServicio;
    private final PasajeroServicio pasajeroServicio;
    private final VehiculoServicio vehiculoServicio;
    private final RutaServicio rutaServicio;
    private final TarifaServicio tarifaServicio;
    private final TurnoServicio turnoServicio;

    private final ConductorRepositorio conductorRepositorio;
    private final PasajeroRepositorio pasajeroRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;
    private final RutaRepositorio rutaRepositorio;
    private final TarifaRepositorio tarifaRepositorio;
    private final TurnoRepositorio turnoRepositorio;

    public DatosIniciales(ConductorServicio conductorServicio, PasajeroServicio pasajeroServicio,
                          VehiculoServicio vehiculoServicio, RutaServicio rutaServicio,
                          TarifaServicio tarifaServicio, TurnoServicio turnoServicio,
                          ConductorRepositorio conductorRepositorio, PasajeroRepositorio pasajeroRepositorio,
                          VehiculoRepositorio vehiculoRepositorio, RutaRepositorio rutaRepositorio,
                          TarifaRepositorio tarifaRepositorio, TurnoRepositorio turnoRepositorio) {
        this.conductorServicio = conductorServicio;
        this.pasajeroServicio = pasajeroServicio;
        this.vehiculoServicio = vehiculoServicio;
        this.rutaServicio = rutaServicio;
        this.tarifaServicio = tarifaServicio;
        this.turnoServicio = turnoServicio;
        this.conductorRepositorio = conductorRepositorio;
        this.pasajeroRepositorio = pasajeroRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.rutaRepositorio = rutaRepositorio;
        this.tarifaRepositorio = tarifaRepositorio;
        this.turnoRepositorio = turnoRepositorio;
    }

    @Override
    public void run(String... args) {
        cargarConductores();
        cargarPasajeros();
        cargarVehiculos();
        cargarRutas();
        cargarTarifas();
        cargarTurnos();
    }

    /** Conductores 1 a 5. El 5 queda INACTIVO para probar el filtro ?estado=INACTIVO. */
    private void cargarConductores() {
        if (conductorRepositorio.count() > 0) {
            return;
        }
        LocalDate vence = LocalDate.now().plusYears(2);
        conductorServicio.registrar(new ConductorSolicitud("1085300111", "Carlos Andres Rosero", "LIC-1001", vence));
        conductorServicio.registrar(new ConductorSolicitud("1085300222", "Maria Fernanda Lopez", "LIC-1002", vence));
        conductorServicio.registrar(new ConductorSolicitud("1085300333", "Jorge Ivan Benavides", "LIC-1003", vence));
        conductorServicio.registrar(new ConductorSolicitud("1085300444", "Luisa Maria Ortega", "LIC-1004", vence));
        var inactivo = conductorServicio.registrar(
                new ConductorSolicitud("1085300555", "Pedro Pablo Guerrero", "LIC-1005", vence));
        conductorServicio.cambiarEstado(inactivo.id(), EstadoRegistro.INACTIVO);
        log.info("Datos de ejemplo: 5 conductores cargados");
    }

    /** Pasajeros 1 a 5. */
    private void cargarPasajeros() {
        if (pasajeroRepositorio.count() > 0) {
            return;
        }
        pasajeroServicio.registrar(new PasajeroSolicitud("1004100111", "Ana Lucia Paz", "3001112233", "ana.paz@correo.com"));
        pasajeroServicio.registrar(new PasajeroSolicitud("1004100222", "Diego Alejandro Mora", "3002223344", "diego.mora@correo.com"));
        pasajeroServicio.registrar(new PasajeroSolicitud("1004100333", "Camila Andrea Ruiz", "3003334455", "camila.ruiz@correo.com"));
        pasajeroServicio.registrar(new PasajeroSolicitud("1004100444", "Santiago Burbano", "3004445566", "santiago.b@correo.com"));
        pasajeroServicio.registrar(new PasajeroSolicitud("1004100555", "Valentina Cordoba", "3005556677", "vale.cordoba@correo.com"));
        log.info("Datos de ejemplo: 5 pasajeros cargados");
    }

    /** Vehiculos 1 a 5. El 5 queda fuera de servicio (en el taller). */
    private void cargarVehiculos() {
        if (vehiculoRepositorio.count() > 0) {
            return;
        }
        vehiculoServicio.crear(new VehiculoSolicitud("TAX101", "Chevrolet Spark 2022", 4, "Sedan", true));
        vehiculoServicio.crear(new VehiculoSolicitud("TAX102", "Hyundai Accent 2023", 4, "Sedan", true));
        vehiculoServicio.crear(new VehiculoSolicitud("TAX103", "Kia Picanto 2021", 4, "Hatchback", true));
        vehiculoServicio.crear(new VehiculoSolicitud("TAX104", "Renault Duster 2024", 5, "Camioneta", true));
        vehiculoServicio.crear(new VehiculoSolicitud("TAX105", "Nissan Versa 2020", 4, "Sedan", false));
        log.info("Datos de ejemplo: 5 vehiculos cargados");
    }

    /** Rutas 1 a 4. */
    private void cargarRutas() {
        if (rutaRepositorio.count() > 0) {
            return;
        }
        rutaServicio.crear(new RutaSolicitud("Centro", "Aeropuerto", 32.5, 40));
        rutaServicio.crear(new RutaSolicitud("Centro", "Terminal de transportes", 4.2, 12));
        rutaServicio.crear(new RutaSolicitud("Universidad", "Centro comercial", 6.8, 18));
        rutaServicio.crear(new RutaSolicitud("Terminal de transportes", "Hospital", 5.1, 15));
        log.info("Datos de ejemplo: 4 rutas cargadas");
    }

    /** Tarifa 1, vigente desde hoy y sin fecha de fin: con ella ya se pueden solicitar viajes. */
    private void cargarTarifas() {
        if (tarifaRepositorio.count() > 0) {
            return;
        }
        tarifaServicio.crear(new TarifaSolicitud("Tarifa urbana", new BigDecimal("5000"),
                new BigDecimal("1600"), LocalDate.now(), null));
        log.info("Datos de ejemplo: 1 tarifa cargada");
    }

    /** Turnos 1 y 2 para hoy: conductor 1 con vehiculo 1 y conductor 2 con vehiculo 2. */
    private void cargarTurnos() {
        if (turnoRepositorio.count() > 0) {
            return;
        }
        LocalDateTime inicio = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        turnoServicio.programar(new TurnoSolicitud(1L, 1L, inicio, inicio.plusHours(8)));
        turnoServicio.programar(new TurnoSolicitud(2L, 2L, inicio, inicio.plusHours(8)));
        log.info("Datos de ejemplo: 2 turnos programados");
    }
}
