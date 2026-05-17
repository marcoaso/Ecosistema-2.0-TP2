
package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy {

    @Override
    public Animal select(Animal a, List<Animal> as) { //Devuelve el animal más cercano.
        
        if( as == null) return null;
        return as.stream().min((a1,a2) -> Double.compare(a1.distanceTo(a), a2.distanceTo(a))).orElse(null);
        // El método select recibe un animal a y una lista de animales as, y devuelve el animal más cercano a a. Si la lista as es null, devuelve null. 
        // Si la lista no es null, utiliza un stream para encontrar el animal con la distancia mínima a a, comparando las distancias de cada animal en as a a utilizando el método distanceTo. 
        // Si la lista as está vacía, devuelve null.
    }
}
