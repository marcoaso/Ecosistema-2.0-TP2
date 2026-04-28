package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region {

    //Atributos.
    private double food;
    private double factor;

    //Constantes.
    private static final double FOOD_EAT_RATE_HERBS = 60.0;         //Tasa base de comida para herbívoros.
    private static final double FOOD_SHORTAGE_TH_HERBS = 5.0;       //Umbral de escasez.
    private static final double FOOD_SHORTAGE_EXP_HERBS = 2.0;      //Factor exponencial de penalización.
    private static final double INIT_FOOD = 100.0;                  //Comida inicial por defecto.
    private static final double FACTOR = 2.0;                       //Factor de crecimiento por defecto.

    //Constructora.
    public DynamicSupplyRegion(double food, double factor) {

        if (food <= 0.0 || factor < 0.0) {
            throw new IllegalArgumentException("food must be > 0 and factor >= 0");
        }
        this.food = food;
        this.factor = factor;
    }

    //Constructora por defecto con valores del enunciado.
    public DynamicSupplyRegion() {
        this(INIT_FOOD, FACTOR);
    }

    @Override
    public double getFood(AnimalInfo a, double dt) {

        //Los carnívoros no comen aquí.
        if (a.getDiet() == Animal.Diet.CARNIVORE) {
            return 0.0;
        }

        //Número de herbívoros en la región.
        long nHerbs = animals.stream().filter(an -> an.getDiet() == Animal.Diet.HERBIVORE).count();

        double maxFood = FOOD_EAT_RATE_HERBS
                * Math.exp(-Math.max(0, nHerbs - FOOD_SHORTAGE_TH_HERBS) * FOOD_SHORTAGE_EXP_HERBS) * dt;

        double eaten = Math.min(food, maxFood);

        //Restamos la comida consumida del total disponible.
        food -= eaten;

        return eaten;
    }

    @Override
    public void update(double dt) {

        //Con probabilidad 0.5 la comida crece.
        if (Utils.RAND.nextDouble() < 0.5) {
            food += dt * factor;
        }
    }

    public String toString() {
        return "DynamicRegion";
    }
}
