package smartflet.dto;

import smartflet.modelo.Ruta;

/**
 * Datos de una ruta que se devuelven al cliente.
 */
public record RutaRespuesta(
        String id,
        String origen,
        String destino,
        Double distanciaKm,
        Integer duracionEstimadaMinutos
) {

    public static RutaRespuesta desde(Ruta ruta) {
        return new RutaRespuesta(
                ruta.getId(),
                ruta.getOrigen(),
                ruta.getDestino(),
                ruta.getDistanciaKm(),
                ruta.getDuracionEstimadaMinutos());
    }
}
