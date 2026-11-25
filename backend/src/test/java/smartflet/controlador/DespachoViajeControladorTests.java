package smartflet.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import smartflet.repositorio.ConductorRepositorio;
import smartflet.repositorio.PasajeroRepositorio;
import smartflet.repositorio.TarifaRepositorio;
import smartflet.repositorio.TurnoRepositorio;
import smartflet.repositorio.VehiculoRepositorio;
import smartflet.repositorio.ViajeRepositorio;

/**
 * Flujo completo de despacho: tarifa, pasajero, conductor, vehiculo, turno y viaje hasta el pago.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DespachoViajeControladorTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper json;
    @Autowired private ViajeRepositorio viajeRepositorio;
    @Autowired private TurnoRepositorio turnoRepositorio;
    @Autowired private TarifaRepositorio tarifaRepositorio;
    @Autowired private ConductorRepositorio conductorRepositorio;
    @Autowired private PasajeroRepositorio pasajeroRepositorio;
    @Autowired private VehiculoRepositorio vehiculoRepositorio;

    @BeforeEach
    @AfterEach
    void limpiarBaseDeDatos() {
        viajeRepositorio.deleteAll();   // los pagos viven dentro de los viajes
        turnoRepositorio.deleteAll();
        tarifaRepositorio.deleteAll();
        conductorRepositorio.deleteAll();
        pasajeroRepositorio.deleteAll();
        vehiculoRepositorio.deleteAll();
    }

    @Test
    void viajeCompletoGeneraPagoConLaTarifaVigente() throws Exception {
        crearTarifa();
        Long pasajeroId = crearPasajero();
        Long viajeId = viajeEnCurso(pasajeroId);

        // 4000 + 1500 * 10 = 19000.00
        enviar(patch("/api/viajes/" + viajeId + "/finalizar"), "{\"distanciaKm\": 10, \"metodoPago\": \"EFECTIVO\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estadoViaje").value("FINALIZADO"))
                .andExpect(jsonPath("$.datos.pago.monto").value(19000.00))
                .andExpect(jsonPath("$.datos.pago.estadoPago").value("PAGADO"))
                .andExpect(jsonPath("$.datos.pago.viajeId").value(viajeId));

        mockMvc.perform(get("/api/viajes").param("pasajeroId", String.valueOf(pasajeroId)))
                .andExpect(jsonPath("$.datos[0].id").value(viajeId));
    }

    @Test
    void elPrecioUsaLaTarifaFijadaAlSolicitarAunqueLaTarifaCambie() throws Exception {
        Long tarifaId = crearTarifa();
        Long viajeId = viajeEnCurso(crearPasajero());

        String tarifaCara = """
                {"nombre": "Diurna", "valorBase": 9000, "valorPorKm": 9000, "fechaInicioVigencia": "%s"}
                """.formatted(LocalDate.now().minusDays(1));
        enviar(put("/api/tarifas/" + tarifaId), tarifaCara).andExpect(status().isOk());

        enviar(patch("/api/viajes/" + viajeId + "/finalizar"), "{\"distanciaKm\": 10, \"metodoPago\": \"TARJETA\"}")
                .andExpect(jsonPath("$.datos.pago.monto").value(19000.00));
    }

    @Test
    void elPagoSeConsultaYSeAnulaPorSuId() throws Exception {
        crearTarifa();
        Long viajeId = viajeEnCurso(crearPasajero());
        String cuerpo = enviar(patch("/api/viajes/" + viajeId + "/finalizar"),
                        "{\"distanciaKm\": 2, \"metodoPago\": \"TRANSFERENCIA\"}")
                .andReturn().getResponse().getContentAsString();
        Long pagoId = json.readTree(cuerpo).get("datos").get("pago").get("id").asLong();

        mockMvc.perform(get("/api/pagos/" + pagoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.viajeId").value(viajeId));
        mockMvc.perform(patch("/api/pagos/" + pagoId + "/anular"))
                .andExpect(jsonPath("$.datos.estadoPago").value("ANULADO"));
        mockMvc.perform(patch("/api/pagos/" + pagoId + "/anular"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/pagos/999")).andExpect(status().isNotFound());
    }

    @Test
    void noSeAsignaUnVehiculoSinTurnoEnCurso() throws Exception {
        crearTarifa();
        Long viajeId = solicitarViaje(crearPasajero());
        Long vehiculoId = crearVehiculo();

        enviar(patch("/api/viajes/" + viajeId + "/asignar"), "{\"vehiculoId\": " + vehiculoId + "}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El vehiculo TAX101 no tiene un turno en curso"));
    }

    @Test
    void noSeFinalizaUnViajeQueNoHaIniciado() throws Exception {
        crearTarifa();
        Long viajeId = solicitarViaje(crearPasajero());

        enviar(patch("/api/viajes/" + viajeId + "/finalizar"), "{\"distanciaKm\": 5, \"metodoPago\": \"TARJETA\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void sinTarifaVigenteNoSePuedeSolicitarViaje() throws Exception {
        Long pasajeroId = crearPasajero();

        enviar(post("/api/viajes"), viajeJson(pasajeroId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No hay una tarifa vigente: no se pueden solicitar viajes"));
    }

    @Test
    void noSeRegistraUnConductorConLicenciaVencida() throws Exception {
        String vencida = """
                {"identificacion": "900", "nombre": "Ana Ruiz", "numeroLicencia": "lic-900",
                 "fechaVencimientoLicencia": "%s"}
                """.formatted(LocalDate.now().minusDays(1));

        enviar(post("/api/conductores"), vencida).andExpect(status().isBadRequest());
    }

    @Test
    void datosRepetidosDevuelven409() throws Exception {
        crearPasajero();
        enviar(post("/api/pasajeros"), pasajeroJson()).andExpect(status().isConflict());

        crearConductor();
        enviar(post("/api/conductores"), conductorJson()).andExpect(status().isConflict());
    }

    @Test
    void noSeCreanDosTarifasActivasQueSeCruzanPeroUnaTarifaSePuedeModificar() throws Exception {
        Long tarifaId = crearTarifa();
        enviar(post("/api/tarifas"), tarifaJson()).andExpect(status().isConflict());

        // Modificarse a si misma no cuenta como cruce.
        enviar(put("/api/tarifas/" + tarifaId), tarifaJson()).andExpect(status().isOk());
        mockMvc.perform(get("/api/tarifas/vigente")).andExpect(jsonPath("$.datos.id").value(tarifaId));
    }

    @Test
    void turnosQueSeCruzanDevuelven409() throws Exception {
        Long vehiculoId = crearVehiculo();
        Long conductorId = crearConductor();
        programarTurno(conductorId, vehiculoId);

        LocalDateTime inicio = LocalDateTime.now().plusHours(1).withNano(0);
        enviar(post("/api/turnos"), turnoJson(conductorId, vehiculoId, inicio, inicio.plusHours(2)))
                .andExpect(status().isConflict());
    }

    @Test
    void vehiculoConTurnosNoSePuedeEliminar() throws Exception {
        Long vehiculoId = crearVehiculo();
        programarTurno(crearConductor(), vehiculoId);

        mockMvc.perform(delete("/api/vehiculos/" + vehiculoId))
                .andExpect(status().isBadRequest());
    }

    // ---------- ayudas ----------

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String cuerpo) throws Exception {
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        return mockMvc.perform(peticion);
    }

    private Long idDe(ResultActions resultado) throws Exception {
        JsonNode nodo = json.readTree(resultado.andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        return nodo.get("datos").get("id").asLong();
    }

    private String tarifaJson() {
        return """
                {"nombre": "Diurna", "valorBase": 4000, "valorPorKm": 1500,
                 "fechaInicioVigencia": "%s"}
                """.formatted(LocalDate.now().minusDays(1));
    }

    private Long crearTarifa() throws Exception {
        return idDe(enviar(post("/api/tarifas"), tarifaJson()));
    }

    private String pasajeroJson() {
        return """
                {"identificacion": "1001", "nombre": "Luis Perez", "telefono": "3001234567",
                 "correoElectronico": "luis@correo.com"}
                """;
    }

    private Long crearPasajero() throws Exception {
        return idDe(enviar(post("/api/pasajeros"), pasajeroJson()));
    }

    private String conductorJson() {
        return """
                {"identificacion": "2002", "nombre": "Marta Gil", "numeroLicencia": "lic-2002",
                 "fechaVencimientoLicencia": "%s"}
                """.formatted(LocalDate.now().plusYears(2));
    }

    private Long crearConductor() throws Exception {
        return idDe(enviar(post("/api/conductores"), conductorJson()));
    }

    private Long crearVehiculo() throws Exception {
        return idDe(enviar(post("/api/vehiculos"), """
                {"placa": "TAX101", "modelo": "Kia Picanto", "capacidad": 4,
                 "tipo": "Sedan", "estadoOperativo": true}
                """));
    }

    private String turnoJson(Long conductorId, Long vehiculoId, LocalDateTime inicio, LocalDateTime fin) {
        return """
                {"conductorId": %s, "vehiculoId": %s, "fechaInicio": "%s", "fechaFin": "%s"}
                """.formatted(conductorId, vehiculoId, inicio, fin);
    }

    private Long programarTurno(Long conductorId, Long vehiculoId) throws Exception {
        LocalDateTime inicio = LocalDateTime.now().withNano(0);
        return idDe(enviar(post("/api/turnos"), turnoJson(conductorId, vehiculoId, inicio, inicio.plusHours(8))));
    }

    private String viajeJson(Long pasajeroId) {
        return """
                {"pasajeroId": %s, "origen": "Centro", "destino": "Aeropuerto"}
                """.formatted(pasajeroId);
    }

    private Long solicitarViaje(Long pasajeroId) throws Exception {
        return idDe(enviar(post("/api/viajes"), viajeJson(pasajeroId)));
    }

    /** Prepara vehiculo con turno en curso, solicita, asigna e inicia un viaje. Devuelve su id. */
    private Long viajeEnCurso(Long pasajeroId) throws Exception {
        Long vehiculoId = crearVehiculo();
        Long turnoId = programarTurno(crearConductor(), vehiculoId);
        enviar(patch("/api/turnos/" + turnoId + "/iniciar"), null).andExpect(status().isOk());

        Long viajeId = solicitarViaje(pasajeroId);
        enviar(patch("/api/viajes/" + viajeId + "/asignar"), "{\"vehiculoId\": " + vehiculoId + "}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estadoViaje").value("ASIGNADO"));
        enviar(patch("/api/viajes/" + viajeId + "/iniciar"), null)
                .andExpect(jsonPath("$.datos.estadoViaje").value("EN_CURSO"));
        return viajeId;
    }
}
