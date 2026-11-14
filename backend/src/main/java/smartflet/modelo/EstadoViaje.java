package smartflet.modelo;

/**
 * Estados del viaje. Transiciones validas: SOLICITADO -> ASIGNADO -> EN_CURSO -> FINALIZADO; SOLICITADO o ASIGNADO -> CANCELADO.
 */
public enum EstadoViaje {
    SOLICITADO,
    ASIGNADO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO
}
