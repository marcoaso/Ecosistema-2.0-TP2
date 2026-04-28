package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {
    protected List<Animal> animals;

    Region() {
        this.animals = new ArrayList<>();
    };

    //Añade un animal a la región.
    final void addAnimal(Animal a) {
        animals.add(a);
    };

    //Elimina un animal de la región.
    public final void removeAnimal(Animal a) {
        animals.remove(a);
    }

    //Devuelve una lista inmodificable de animales.
    public final List<Animal> getAnimals() {
        return Collections.unmodifiableList(animals);
    }

    //Representación JSON.
    @Override
    public JSONObject asJSON() {
        JSONArray arrayJSON = new JSONArray();
        for (Animal a : animals) {
            arrayJSON.put(a.asJSON());
        }

        JSONObject json = new JSONObject();
        json.put("animals", arrayJSON);
        return json;
    }

    public List<AnimalInfo> getAnimalsInfo() {
        List<AnimalInfo> infoList = new ArrayList<>();
        for (Animal a : animals) {
            infoList.add(a);
        }
        return Collections.unmodifiableList(infoList);
    }

}
