package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {

    @Override
    public Animal select(Animal a, List<Animal> as) { //Devuelve el animal más joven.
        if( as == null) return null;
        return as.stream().min((a1,a2) -> Double.compare(a1.getAge(), a2.getAge())).orElse(null);
        // El método select recibe un animal a y una lista de animales as, y devuelve el animal más joven de la lista as. Si la lista as es null, devuelve null. 
        // Si la lista no es null, utiliza un stream para encontrar el animal con la edad mínima, comparando las edades de cada animal en as utilizando el método getAge. 
        // Si la lista as está vacía, devuelve null.
    }
}
