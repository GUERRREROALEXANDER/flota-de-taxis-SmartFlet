package smartflet.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Trayecto entre un origen y un destino.
 */
@Document(collection = "rutas")
public class Ruta {

    @Id
    private String id;

    private String origen;

    private String destino;

    private Double distanciaKm;

    private Integer duracionEstimadaMinutos;

    /** Constructor vacio que usa Spring Data para leer el documento. */
    protected Ruta() {
    }

    public Ruta(String origen, String destino, Double distanciaKm, Integer duracionEstimadaMinutos) {
        this.origen = origen;
        this.destino = destino;
        this.distanciaKm = distanciaKm;
        this.duracionEstimadaMinutos = duracionEstimadaMinutos;
    }

    public String getId() {
        return id;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public Integer getDuracionEstimadaMinutos() {
        return duracionEstimadaMinutos;
    }

    public void setDuracionEstimadaMinutos(Integer duracionEstimadaMinutos) {
        this.duracionEstimadaMinutos = duracionEstimadaMinutos;
    }
}
