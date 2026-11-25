package smartflet.seguridad;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import smartflet.dto.ErrorRespuesta;

/**
 * Seguridad de la API.
 *
 * Como funciona, en corto:
 *  1. El frontend envia usuario y clave a POST /api/auth/login.
 *  2. Si son correctos, el backend guarda la autenticacion en la sesion HTTP y el navegador
 *     recibe la cookie JSESSIONID (HttpOnly, SameSite=Strict).
 *  3. En cada peticion siguiente el navegador manda la cookie y Spring sabe quien es.
 *
 * Para cambiar quien puede hacer que, edita SOLO el metodo reglasDeAcceso.
 */
@Configuration
public class ConfiguracionSeguridad {

    @Bean
    SecurityFilterChain cadenaDeFiltros(HttpSecurity http, ObjectMapper json) throws Exception {
        http
            // CSRF: la cookie es SameSite=Strict y la API solo acepta JSON, asi que otro sitio
            // no puede enviar peticiones con la sesion del usuario. Por eso se desactiva el token CSRF.
            .csrf(AbstractHttpConfigurer::disable)
            .securityContext(c -> c.securityContextRepository(repositorioContexto()))
            .authorizeHttpRequests(this::reglasDeAcceso)
            // Sin formulario HTML ni ventana de usuario/clave del navegador: el login es por JSON.
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .exceptionHandling(e -> e
                // 401: no hay sesion (nunca inicio o expiro).
                .authenticationEntryPoint((req, res, ex) -> escribirError(res, json, HttpStatus.UNAUTHORIZED,
                        "Debes iniciar sesion para usar esta funcion",
                        "Tu sesion pudo haber expirado por inactividad. Vuelve a iniciar sesion."))
                // 403: hay sesion, pero el rol no alcanza.
                .accessDeniedHandler((req, res, ex) -> escribirError(res, json, HttpStatus.FORBIDDEN,
                        "Tu rol no tiene permiso para esta accion",
                        "Tu rol es " + rolDe(req.getUserPrincipal()) + ". Pide a un administrador que la haga "
                                + "o que te asigne el rol ADMINISTRADOR.")));
        return http.build();
    }

    /**
     * Quien puede hacer que. Spring evalua las reglas EN ORDEN y aplica la primera que coincide.
     *
     * - Cualquier usuario con sesion puede CONSULTAR (GET) todo.
     * - OPERADOR y ADMINISTRADOR operan viajes, turnos y pasajeros.
     * - Lo demas de la API (vehiculos, conductores, rutas, tarifas, pagos, usuarios) es solo ADMINISTRADOR.
     * - Lo que no es /api (el frontend: index.html, js, css) es publico.
     */
    private void reglasDeAcceso(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry reglas) {
        reglas
            // logout tambien es publico: cerrar sesion sin tenerla no es un error (responde 204 igual).
            .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
            .requestMatchers("/api/auth/**").authenticated()
            .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
            .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
            .requestMatchers("/api/viajes/**", "/api/turnos/**", "/api/pasajeros/**")
                .hasAnyRole("ADMINISTRADOR", "OPERADOR")
            .requestMatchers("/api/**").hasRole("ADMINISTRADOR")
            .anyRequest().permitAll();
    }

    /** BCrypt: guarda un hash con sal; aunque roben la base, no ven las contrasenas. */
    @Bean
    PasswordEncoder cifradorClaves() {
        return new BCryptPasswordEncoder();
    }

    /** Verifica usuario y clave contra la coleccion "usuarios". Lo usa AutenticacionControlador. */
    @Bean
    AuthenticationManager gestorAutenticacion(UserDetailsService usuarios, PasswordEncoder cifrador) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(cifrador);
        proveedor.setUserDetailsService(usuarios);
        return new ProviderManager(proveedor);
    }

    /** Donde se guarda "quien inicio sesion": en la sesion HTTP. */
    @Bean
    SecurityContextRepository repositorioContexto() {
        return new HttpSessionSecurityContextRepository();
    }

    private static String rolDe(java.security.Principal principal) {
        if (principal instanceof Authentication auth) {
            return auth.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .findFirst().orElse("desconocido");
        }
        return "desconocido";
    }

    private static void escribirError(HttpServletResponse res, ObjectMapper json, HttpStatus estado,
                                      String mensaje, String pista) throws IOException {
        res.setStatus(estado.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(), ErrorRespuesta.conPista(estado.value(), mensaje, pista));
    }
}
