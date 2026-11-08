package smartflet.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Taxi de la flota. Atributos tomados del modelo conceptual (diagrama 03).
 */
@Entity
@Table(name = "vehiculo")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(nullable = false, length = 60)
    private String modelo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false, length = 30)
    private String tipo;

    /** true = disponible para operar; false = fuera de servicio. */
    @Column(name = "estado_operativo", nullable = false)
    private Boolean estadoOperativo;

    /** Constructor vacio que exige JPA. */
    protected Vehiculo() {
    }

    public Vehiculo(String placa, String modelo, Integer capacidad, String tipo, Boolean estadoOperativo) {
        this.placa = placa;
        this.modelo = modelo;
        this.capacidad = capacidad;
        this.tipo = tipo;
        this.estadoOperativo = estadoOperativo;
    }

    public Long getId() {
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
