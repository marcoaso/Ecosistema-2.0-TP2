package simulator.factories;

import org.json.JSONObject;
import simulator.model.SelectionStrategy;
import simulator.model.SelectClosest;

public class SelectClosestBuilder extends Builder<SelectionStrategy> {

    public SelectClosestBuilder() {
        super("closest", "Selects the closest animal"); //Registra este builder con el tag "closest". 
    }

    @Override
    protected SelectionStrategy createInstance(JSONObject data) {
        return new SelectClosest();
    }
}
