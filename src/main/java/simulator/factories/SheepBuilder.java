package simulator.factories;

import simulator.misc.Vector2D;
import simulator.model.*;

public class SheepBuilder extends AnimalBuilder {
    public SheepBuilder(Factory<SelectionStrategy> strategyFactory) {
        super("sheep", "Sheep", strategyFactory);
    }

    @Override
    protected String getSecondStrategyKey() { return "danger_strategy"; }

    @Override
    protected Animal createAnimal(SelectionStrategy s1, SelectionStrategy s2, Vector2D pos) {
        return new Sheep(s1, s2, pos);
    }
}