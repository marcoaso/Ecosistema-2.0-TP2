package simulator.model;

import simulator.misc.Vector2D;

public interface AnimalInfo extends JSONable {
    public Animal.State getState();

    public Vector2D getPosition();

    public String getGeneticCode();

    public Animal.Diet getDiet();

    public double getSpeed();

    public double getSightRange();

    public double getEnergy();

    public double getAge();

    public Vector2D getDestination();

    public boolean isPregnant();

}