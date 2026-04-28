package simulator.model;

import java.util.List;

public interface SelectionStrategy {
    Animal select(Animal animal, List<Animal> animalList);
}
