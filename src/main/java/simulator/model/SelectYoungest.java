package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {

    @Override
    public Animal select(Animal a, List<Animal> as) { //Devuelve el animal más joven.
        if( as == null) return null;
        return as.stream().min((a1,a2) -> Double.compare(a1.getAge(), a2.getAge())).orElse(null);
    }
}
