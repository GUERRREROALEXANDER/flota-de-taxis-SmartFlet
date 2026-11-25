package smartflet.configuracion;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comprueba lo mismo que se prueba en Postman: con la base vacia se cargan los datos de ejemplo
 * con IDs 1, 2, 3... y GET /api/conductores/3 devuelve justo el conductor 3.
 */
@SpringBootTest(properties = "smartflet.datos-iniciales=true")
@AutoConfigureMockMvc
class DatosInicialesTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongo;

    @Autowired
    private DatosIniciales datosIniciales;

    /** Base vacia (incluidos los contadores de ID) y carga de nuevo los datos de ejemplo. */
    @BeforeEach
    void baseVaciaConDatosDeEjemplo() {
        mongo.getCollectionNames().forEach(mongo::dropCollection);
        datosIniciales.run();
    }

    @Test
    void listarDevuelveLosCincoConductoresConMensaje() throws Exception {
        mockMvc.perform(get("/api/conductores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Conductores encontrados"))
                .andExpect(jsonPath("$.datos", hasSize(5)))
                .andExpect(jsonPath("$.datos[0].id").value(1));
    }

    @Test
    void buscarPorIdTresDevuelveElConductorTres() throws Exception {
        mockMvc.perform(get("/api/conductores/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Conductor encontrado"))
                .andExpect(jsonPath("$.datos.id").value(3))
                .andExpect(jsonPath("$.datos.nombre").value("Jorge Ivan Benavides"))
                .andExpect(jsonPath("$.datos.estadoConductor").value("ACTIVO"));
    }

    @Test
    void idQueNoExisteDevuelve404ConMensaje() throws Exception {
        mockMvc.perform(get("/api/conductores/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Conductor con ID 99 no encontrado"));
    }

    @Test
    void elFiltroPorEstadoDevuelveSoloElConductorInactivo() throws Exception {
        mockMvc.perform(get("/api/conductores").param("estado", "INACTIVO"))
                .andExpect(jsonPath("$.datos", hasSize(1)))
                .andExpect(jsonPath("$.datos[0].id").value(5));
    }
}
