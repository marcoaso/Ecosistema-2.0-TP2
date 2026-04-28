package simulator.factories;

import org.json.JSONObject;
import simulator.model.SelectionStrategy;
import simulator.model.SelectYoungest;

public class SelectYoungestBuilder extends Builder<SelectionStrategy> {

    public SelectYoungestBuilder() {
        super("youngest", "Selects the youngest animal"); //Registra este builder con el tag "youngest". 
    }

    @Override
    protected SelectionStrategy createInstance(JSONObject data) {
        return new SelectYoungest();
    }
}
