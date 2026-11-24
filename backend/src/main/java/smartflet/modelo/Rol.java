package smartflet.modelo;

/**
 * Roles del sistema. Spring Security los ve como "ROLE_ADMINISTRADOR" y "ROLE_OPERADOR".
 * Que puede hacer cada uno esta en seguridad/ConfiguracionSeguridad (metodo reglasDeAcceso).
 */
public enum Rol {
    /** Gestiona todo: flota, conductores, tarifas, pagos y usuarios. */
    ADMINISTRADOR,
    /** Central de operaciones: consulta todo y opera viajes, turnos y pasajeros. */
    OPERADOR
}
