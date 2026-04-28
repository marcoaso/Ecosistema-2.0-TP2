package simulator.factories;

import simulator.misc.Vector2D;
import simulator.model.*;

public class WolfBuilder extends AnimalBuilder {
    public WolfBuilder(Factory<SelectionStrategy> strategyFactory) {
        super("wolf", "Wolf", strategyFactory);
    }

    @Override
    protected String getSecondStrategyKey() { return "hunt_strategy"; }

    @Override
    protected Animal createAnimal(SelectionStrategy s1, SelectionStrategy s2, Vector2D pos) {
        return new Wolf(s1, s2, pos);
    }
}