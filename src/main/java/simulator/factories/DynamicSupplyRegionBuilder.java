package simulator.factories;

import org.json.JSONObject;
import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

    //Valores que se usan si el JSON no especifica nada.
    private static final double DEFAULT_FOOD = 1250.0;
    private static final double DEFAULT_FACTOR = 2.5;

    public DynamicSupplyRegionBuilder() {
        super("dynamic", "Dynamic supply region"); //Registra este builder con el tag "dynamic". Cuando en el JSON aparezca "type": "dynamic", la factoría usará este builder.
    }

    @Override
    protected Region createInstance(JSONObject data) {

        double food = data.has("food") ? data.getDouble("food") : DEFAULT_FOOD; //Si tiene comida se mete, sino DEFAULT.
        double factor = data.has("factor") ? data.getDouble("factor") : DEFAULT_FACTOR;

        if (food <= 0 || factor < 0)
            throw new IllegalArgumentException("Invalid food/factor values");

        return new DynamicSupplyRegion(food, factor);
    }

    @Override
    protected void fillInData(JSONObject data) {
        data.put("factor", "food increase factor (optional, default 2.0)");
        data.put("food", "initial amount of food (optional, default 100.0)");
    }
}
