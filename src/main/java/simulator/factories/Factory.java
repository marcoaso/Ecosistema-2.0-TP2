package simulator.factories;

import java.util.List;

import org.json.JSONObject;

public interface Factory<T> {
    public T createInstance(JSONObject info); //El método principal. Recibe un JSON y devuelve un objeto de tipo T. Es el que se llama cuando se necesita crear un animal, región o estrategia.

    public List<JSONObject> getInfo(); //Devuelve información sobre todos los tipos que esta factoría sabe construir.
}