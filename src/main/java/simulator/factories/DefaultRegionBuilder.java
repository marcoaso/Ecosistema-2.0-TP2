package simulator.factories;

import org.json.JSONObject;
import simulator.model.DefaultRegion;
import simulator.model.Region;

public class DefaultRegionBuilder extends Builder<Region> {

    //Llama al constructor de Builder<Region> con dos valores fijos. 
    //Esto registra este builder con el tag "default" y la descripción "Default region". 
    //Cuando BuilderBasedFactory busque en su HashMap el tag "default", encontrará exactamente este builder.
    public DefaultRegionBuilder() {
        super("default", "Default region");
    }

    @Override
    protected Region createInstance(JSONObject data) { //Simplemente crea y devuelve una DefaultRegion.
        return new DefaultRegion();
    }

    @Override
    protected void fillInData(JSONObject data) {
        // No necesita datos adicionales
    }

}
