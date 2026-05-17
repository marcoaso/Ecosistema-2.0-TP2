package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;
import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.*;

public abstract class AnimalBuilder extends Builder<Animal> {

    protected Factory<SelectionStrategy> strategyFactory;

    public AnimalBuilder(String typeTag, String desc, Factory<SelectionStrategy> strategyFactory) {
        super(typeTag, desc);
        this.strategyFactory = strategyFactory;
    }

    @Override
    protected Animal createInstance(JSONObject data) {
        // 1. Estrategias por defecto
        SelectionStrategy mateStrategy = new SelectFirst(); //mate strategy es la estrategia de selección que se usará para elegir a la pareja de apareamiento. Por defecto, se inicializa con SelectFirst, lo que significa que el animal seleccionará la primera pareja disponible sin considerar otras características como la distancia o el nivel de energía. Sin embargo, esta estrategia por defecto puede ser sobrescrita si el JSON de entrada incluye una especificación para la estrategia de apareamiento, permitiendo así una mayor variedad de comportamientos en los animales creados.
        SelectionStrategy secondStrategy = new SelectFirst(); //second strategy es la estrategia de selección que se usará para elegir a la presa (en el caso de los carnívoros) o a la región más segura (en el caso de los herbívoros). Por defecto, se inicializa también con SelectFirst, lo que significa que el animal seleccionará la primera presa o región segura que encuentre sin considerar otras características como la distancia o el nivel de energía. Sin embargo, esta estrategia por defecto puede ser sobrescrita si el JSON de entrada incluye una especificación para esta segunda estrategia, permitiendo así una mayor variedad de comportamientos en los animales creados.

        // 2. Parsear mate_strategy (común a ambos)
        if (data.has("mate_strategy"))
            mateStrategy = strategyFactory.createInstance(data.getJSONObject("mate_strategy"));

        // 3. Parsear la segunda estrategia (hunt o danger)
        // Usamos una clave que definirá la subclase
        String secondStrategyKey = getSecondStrategyKey();
        if (data.has(secondStrategyKey))
            secondStrategy = strategyFactory.createInstance(data.getJSONObject(secondStrategyKey));

        // 4. Parsear posición
        Vector2D pos = null;
        if (data.has("pos")) {
            pos = randomPosition(data.getJSONObject("pos"));
        }

        return createAnimal(mateStrategy, secondStrategy, pos);
    }

    // Métodos abstractos para las diferencias
    protected abstract String getSecondStrategyKey();
    protected abstract Animal createAnimal(SelectionStrategy s1, SelectionStrategy s2, Vector2D pos);

    private Vector2D randomPosition(JSONObject pos) {
        JSONArray xr = pos.getJSONArray("x_range");
        JSONArray yr = pos.getJSONArray("y_range");
        double x = Utils.RAND.nextDouble(xr.getDouble(0), xr.getDouble(1));
        double y = Utils.RAND.nextDouble(yr.getDouble(0), yr.getDouble(1));
        return new Vector2D(x, y);
    }
}