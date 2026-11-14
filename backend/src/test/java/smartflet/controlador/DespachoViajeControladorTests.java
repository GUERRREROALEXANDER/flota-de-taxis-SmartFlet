package smartflet.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        viajeRepositorio.deleteAll();   // borra tambien los pagos (cascade)
        turnoRepositorio.deleteAll();
        tarifaRepositorio.deleteAll();
        conductorRepositorio.deleteAll();
        pasajeroRepositorio.deleteAll();
        vehiculoRepositorio.deleteAll();
    }

    @Test
    void viajeCompletoGeneraPagoConLaTarifaVigente() throws Exception {
        crearTarifa();
        long pasajeroId = crearPasajero();
        long vehiculoId = crearVehiculo();
        long turnoId = programarTurno(crearConductor(), vehiculoId);
        enviar(patch("/api/turnos/" + turnoId + "/iniciar"), null).andExpect(status().isOk());

        long viajeId = solicitarViaje(pasajeroId);

        enviar(patch("/api/viajes/" + viajeId + "/asignar"), "{\"vehiculoId\": " + vehiculoId + "}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoViaje").value("ASIGNADO"));
        enviar(patch("/api/viajes/" + viajeId + "/iniciar"), null)
                .andExpect(jsonPath("$.estadoViaje").value("EN_CURSO"));

        // 4000 + 1500 * 10 = 19000.00
        enviar(patch("/api/viajes/" + viajeId + "/finalizar"), "{\"distanciaKm\": 10, \"metodoPago\": \"EFECTIVO\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoViaje").value("FINALIZADO"))
                .andExpect(jsonPath("$.pago.monto").value(19000.00))
                .andExpect(jsonPath("$.pago.estadoPago").value("PAGADO"));

        mockMvc.perform(get("/api/viajes").param("pasajeroId", String.valueOf(pasajeroId)))
                .andExpect(jsonPath("$[0].id").value(viajeId));
    }

    @Test
    void noSeAsignaUnVehiculoSinTurnoEnCurso() throws Exception {
        crearTarifa();
        long viajeId = solicitarViaje(crearPasajero());
        long vehiculoId = crearVehiculo();

        enviar(patch("/api/viajes/" + viajeId + "/asignar"), "{\"vehiculoId\": " + vehiculoId + "}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El vehiculo TAX101 no tiene un turno en curso"));
    }

    @Test
    void noSeFinalizaUnViajeQueNoHaIniciado() throws Exception {
        crearTarifa();
        long viajeId = solicitarViaje(crearPasajero());

        enviar(patch("/api/viajes/" + viajeId + "/finalizar"), "{\"distanciaKm\": 5, \"metodoPago\": \"TARJETA\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void sinTarifaVigenteNoSePuedeSolicitarViaje() throws Exception {
        long pasajeroId = crearPasajero();

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
    void noSeCreanDosTarifasActivasQueSeCrucen() throws Exception {
        crearTarifa();
        enviar(post("/api/tarifas"), tarifaJson()).andExpect(status().isBadRequest());
    }

    @Test
    void vehiculoConTurnosNoSePuedeEliminar() throws Exception {
        long vehiculoId = crearVehiculo();
        programarTurno(crearConductor(), vehiculoId);

        mockMvc.perform(delete("/api/vehiculos/" + vehiculoId))
                .andExpect(status().isBadRequest());
    }

    // ---------- ayudas ----------

    private ResultActions enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder peticion,
                                 String cuerpo) throws Exception {
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        return mockMvc.perform(peticion);
    }

    private long idDe(ResultActions resultado) throws Exception {
        JsonNode nodo = json.readTree(resultado.andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        return nodo.get("id").asLong();
    }

    private String tarifaJson() {
        return """
                {"nombre": "Diurna", "valorBase": 4000, "valorPorKm": 1500,
                 "fechaInicioVigencia": "%s"}
                """.formatted(LocalDate.now().minusDays(1));
    }

    private long crearTarifa() throws Exception {
        return idDe(enviar(post("/api/tarifas"), tarifaJson()));
    }

    private long crearPasajero() throws Exception {
        return idDe(enviar(post("/api/pasajeros"), """
                {"identificacion": "1001", "nombre": "Luis Perez", "telefono": "3001234567",
                 "correoElectronico": "luis@correo.com"}
                """));
    }

    private long crearConductor() throws Exception {
        return idDe(enviar(post("/api/conductores"), """
                {"identificacion": "2002", "nombre": "Marta Gil", "numeroLicencia": "lic-2002",
                 "fechaVencimientoLicencia": "%s"}
                """.formatted(LocalDate.now().plusYears(2))));
    }

    private long crearVehiculo() throws Exception {
        return idDe(enviar(post("/api/vehiculos"), """
                {"placa": "TAX101", "modelo": "Kia Picanto", "capacidad": 4,
                 "tipo": "Sedan", "estadoOperativo": true}
                """));
    }

    private long programarTurno(long conductorId, long vehiculoId) throws Exception {
        LocalDateTime inicio = LocalDateTime.now().withNano(0);
        return idDe(enviar(post("/api/turnos"), """
                {"conductorId": %d, "vehiculoId": %d, "fechaInicio": "%s", "fechaFin": "%s"}
                """.formatted(conductorId, vehiculoId, inicio, inicio.plusHours(8))));
    }

    private String viajeJson(long pasajeroId) {
        return """
                {"pasajeroId": %d, "origen": "Centro", "destino": "Aeropuerto"}
                """.formatted(pasajeroId);
    }

    private long solicitarViaje(long pasajeroId) throws Exception {
        return idDe(enviar(post("/api/viajes"), viajeJson(pasajeroId)));
    }
}
