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
        SelectionStrategy mateStrategy = new SelectFirst();
        SelectionStrategy secondStrategy = new SelectFirst();

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