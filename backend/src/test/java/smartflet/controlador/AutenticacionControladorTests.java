package smartflet.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import smartflet.modelo.Rol;
import smartflet.modelo.Usuario;
import smartflet.repositorio.UsuarioRepositorio;

/**
 * Login, sesion y permisos por rol. El administrador "admin" / "clave-de-pruebas" lo crea
 * CreadorAdministradorInicial con los valores de src/test/resources/application.yml.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AutenticacionControladorTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder cifrador;

    @BeforeEach
    void crearOperador() {
        usuarioRepositorio.findByNombreUsuario("operador1").ifPresent(usuarioRepositorio::delete);
        usuarioRepositorio.save(new Usuario("operador1", "Operador Uno", cifrador.encode("clave-operador"), Rol.OPERADOR));
    }

    private static String login(String usuario, String clave) {
        return "{\"usuario\": \"" + usuario + "\", \"clave\": \"" + clave + "\"}";
    }

    @Test
    void sinSesionLaApiResponde401ConPista() throws Exception {
        mockMvc.perform(get("/api/vehiculos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.pista").exists());
    }

    @Test
    void loginCorrectoAbreSesionYPermiteConsultar() throws Exception {
        MockHttpSession sesion = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login("ADMIN", "clave-de-pruebas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andReturn().getRequest().getSession();

        mockMvc.perform(get("/api/auth/sesion").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Administrador de pruebas"));
        mockMvc.perform(get("/api/vehiculos").session(sesion)).andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout").session(sesion)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/vehiculos").session(sesion)).andExpect(status().isUnauthorized());
    }

    @Test
    void claveIncorrectaDevuelve401YLuegoBloquea() throws Exception {
        // application.yml de pruebas: 3 intentos maximos
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login("operador1", "mala")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contrasena incorrectos"))
                .andExpect(jsonPath("$.pista").exists());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login("operador1", "mala")));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login("operador1", "mala")));

        // Bloqueado: ni siquiera la clave correcta entra
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login("operador1", "clave-operador")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void loginSinCamposDevuelve400PorCampo() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login("", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.usuario").exists())
                .andExpect(jsonPath("$.errores.clave").exists());
    }

    @Test
    @WithMockUser(username = "operador1", roles = "OPERADOR")
    void operadorConsultaPeroNoPuedeCrearVehiculosNiUsuarios() throws Exception {
        mockMvc.perform(get("/api/vehiculos")).andExpect(status().isOk());

        mockMvc.perform(post("/api/vehiculos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.pista").value(org.hamcrest.Matchers.containsString("OPERADOR")));

        mockMvc.perform(get("/api/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operador1", roles = "OPERADOR")
    void operadorSiPuedeOperarViajes() throws Exception {
        // 400 (cuerpo invalido) y no 403: paso el filtro de permisos
        mockMvc.perform(post("/api/viajes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMINISTRADOR")
    void administradorCreaUsuariosYNoSeVeLaClave() throws Exception {
        usuarioRepositorio.findByNombreUsuario("nuevo.op").ifPresent(usuarioRepositorio::delete);

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombreUsuario": "Nuevo.Op", "nombre": "Nuevo Operador", "clave": "segura123", "rol": "OPERADOR"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreUsuario").value("nuevo.op"))
                .andExpect(jsonPath("$.claveCifrada").doesNotExist());

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombreUsuario": "con espacio", "nombre": "X", "clave": "corta", "rol": "OPERADOR"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombreUsuario").exists())
                .andExpect(jsonPath("$.errores.clave").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMINISTRADOR")
    void administradorNoPuedeDesactivarseASiMismo() throws Exception {
        String id = usuarioRepositorio.findByNombreUsuario("admin").orElseThrow().getId();
        mockMvc.perform(patch("/api/usuarios/" + id + "/estado")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\": \"INACTIVO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No puedes desactivar tu propia cuenta"));
    }
}
