package simulator.factories;

import org.json.JSONObject;
import simulator.model.SelectionStrategy;
import simulator.model.SelectFirst;

public class SelectFirstBuilder extends Builder<SelectionStrategy> {

    public SelectFirstBuilder() {
        super("first", "Selects the first animal in the list"); //Registra este builder con el tag "first". 
    }

    @Override
    protected SelectionStrategy createInstance(JSONObject data) {
        return new SelectFirst();
    }
}
