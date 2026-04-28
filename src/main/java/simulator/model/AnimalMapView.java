package simulator.model;

import java.util.List;
import java.util.function.Predicate;
import simulator.misc.Vector2D;

public interface AnimalMapView extends MapInfo, FoodSupplier {

    //Devuelve todos los animales en el rango visual que cumplen el filtro.
    List<Animal> getAnimalsInRange(Animal e, Predicate<Animal> filter);

    Vector2D adjustPosition(Vector2D pos); //Ajusta posición dentro del mapa.

    boolean isOutside(Vector2D pos); //Comprueba si está fuera del mapa.

    Vector2D getRandomPosition(); //Devuelve una posición aleatoria.

    boolean isInSight(Animal a, Animal other); //Comprueba si otro animal está en rango visual.

    //Opcional: versión con rango explícito
    List<Animal> getAnimalsInRange(Animal a, double range, Predicate<Animal> filter);
}
