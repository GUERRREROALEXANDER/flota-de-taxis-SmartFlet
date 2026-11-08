package smartflet.controlador;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import smartflet.repositorio.RutaRepositorio;
import smartflet.repositorio.VehiculoRepositorio;

@SpringBootTest
@AutoConfigureMockMvc
class VehiculoYRutaControladorTests {

    private static final String VEHICULO_VALIDO = """
            {"placa": "abc-123", "modelo": "Chevrolet Spark", "capacidad": 4,
             "tipo": "Sedan", "estadoOperativo": true}
            """;

    private static final String RUTA_VALIDA = """
            {"origen": "Terminal", "destino": "Aeropuerto",
             "distanciaKm": 12.5, "duracionEstimadaMinutos": 25}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehiculoRepositorio vehiculoRepositorio;

    @Autowired
    private RutaRepositorio rutaRepositorio;

    @BeforeEach
    void limpiarBaseDeDatos() {
        vehiculoRepositorio.deleteAll();
        rutaRepositorio.deleteAll();
    }

    @Test
    void crearVehiculoDevuelve201YNormalizaLaPlaca() throws Exception {
        mockMvc.perform(post("/api/vehiculos").contentType(MediaType.APPLICATION_JSON).content(VEHICULO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.placa").value("ABC123"));
    }

    @Test
    void placaRepetidaDevuelve400() throws Exception {
        mockMvc.perform(post("/api/vehiculos").contentType(MediaType.APPLICATION_JSON).content(VEHICULO_VALIDO))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/vehiculos").contentType(MediaType.APPLICATION_JSON).content(VEHICULO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un vehiculo con la placa ABC123"));
    }

    @Test
    void vehiculoConCamposInvalidosDevuelve400ConDetalle() throws Exception {
        String invalido = """
                {"placa": "12", "modelo": "", "capacidad": 0, "tipo": "Sedan", "estadoOperativo": true}
                """;

        mockMvc.perform(post("/api/vehiculos").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.placa").exists())
                .andExpect(jsonPath("$.errores.modelo").exists())
                .andExpect(jsonPath("$.errores.capacidad").exists());
    }

    @Test
    void vehiculoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/vehiculos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Vehiculo con id 999 no existe"));

        mockMvc.perform(delete("/api/vehiculos/999")).andExpect(status().isNotFound());
    }

    @Test
    void cambiarEstadoYFiltrarPorEstado() throws Exception {
        String creado = mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON).content(VEHICULO_VALIDO))
                .andReturn().getResponse().getContentAsString();
        String id = creado.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(patch("/api/vehiculos/" + id + "/estado")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estadoOperativo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoOperativo").value(false));

        mockMvc.perform(get("/api/vehiculos").param("estadoOperativo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/api/vehiculos/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void crudCompletoDeRuta() throws Exception {
        String creada = mockMvc.perform(post("/api/rutas")
                        .contentType(MediaType.APPLICATION_JSON).content(RUTA_VALIDA))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = creada.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(get("/api/rutas").param("origen", "term"))
                .andExpect(jsonPath("$", hasSize(1)));

        String actualizada = """
                {"origen": "Terminal", "destino": "Centro", "distanciaKm": 5.0, "duracionEstimadaMinutos": 15}
                """;
        mockMvc.perform(put("/api/rutas/" + id).contentType(MediaType.APPLICATION_JSON).content(actualizada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destino").value("Centro"));

        mockMvc.perform(delete("/api/rutas/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/rutas/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rutaConOrigenIgualAlDestinoDevuelve400() throws Exception {
        String circular = """
                {"origen": "Centro", "destino": "centro", "distanciaKm": 1.0, "duracionEstimadaMinutos": 5}
                """;

        mockMvc.perform(post("/api/rutas").contentType(MediaType.APPLICATION_JSON).content(circular))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El origen y el destino no pueden ser el mismo lugar"));
    }

    @Test
    void idNoNumericoYJsonMalFormadoDevuelven400() throws Exception {
        mockMvc.perform(get("/api/rutas/abc")).andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/rutas").contentType(MediaType.APPLICATION_JSON).content("{origen:"))
                .andExpect(status().isBadRequest());
    }
}
