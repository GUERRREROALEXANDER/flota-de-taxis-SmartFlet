package smartflet.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Taxi de la flota.
 */
@Document(collection = "vehiculos")
public class Vehiculo {

    @Id
    private String id;

    @Indexed(unique = true)
    private String placa;

    private String modelo;

    private Integer capacidad;

    private String tipo;

    /** true = disponible para operar; false = fuera de servicio. */
    private Boolean estadoOperativo;

    /** Constructor vacio que usa Spring Data para leer el documento. */
    protected Vehiculo() {
    }

    public Vehiculo(String placa, String modelo, Integer capacidad, String tipo, Boolean estadoOperativo) {
        this.placa = placa;
        this.modelo = modelo;
        this.capacidad = capacidad;
        this.tipo = tipo;
        this.estadoOperativo = estadoOperativo;
    }

    public String getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getEstadoOperativo() {
        return estadoOperativo;
    }

    public void setEstadoOperativo(Boolean estadoOperativo) {
        this.estadoOperativo = estadoOperativo;
    }
}
