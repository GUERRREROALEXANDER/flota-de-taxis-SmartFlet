package smartflet.dto;

import smartflet.modelo.Vehiculo;

/**
 * Datos de un vehiculo que se devuelven al cliente.
 */
public record VehiculoRespuesta(
        String id,
        String placa,
        String modelo,
        Integer capacidad,
        String tipo,
        Boolean estadoOperativo
) {

    public static VehiculoRespuesta desde(Vehiculo vehiculo) {
        return new VehiculoRespuesta(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getModelo(),
                vehiculo.getCapacidad(),
                vehiculo.getTipo(),
                vehiculo.getEstadoOperativo());
    }
}
