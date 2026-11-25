package smartflet.modelo;

/**
 * Toda entidad que se guarda en MongoDB con un ID numerico (1, 2, 3...) implementa esta interfaz.
 * Asi AsignadorIdsMongo sabe leer y poner el ID sin conocer la clase concreta.
 */
public interface EntidadConId {

    Long getId();

    void setId(Long id);
}
