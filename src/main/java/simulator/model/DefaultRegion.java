package simulator.model;

public class DefaultRegion extends Region {

    //Constantes relacionadas con la comida para herbívoros.
    final static double FOOD_EAT_RATE_HERBS = 60.0;         //Cantidad base de comida que se obtiene.
    final static double FOOD_SHORTAGE_TH_HERBS = 5.0;       //Umbral a partir del cual empieza la escasez.
    final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;      //Factor exponencial de penalización por escasez.

    @Override
    public double getFood(AnimalInfo a, double dt) {

        //Si es carnívoro no obtiene comida de la región.
        if (a.getDiet() == Animal.Diet.CARNIVORE)
            return 0.0;

        long n = animals.stream().filter(an -> an.getDiet() == Animal.Diet.HERBIVORE).count(); //n = numero de animales herbivoros.

        return FOOD_EAT_RATE_HERBS * Math.exp(-Math.max(0, n - FOOD_SHORTAGE_TH_HERBS) * FOOD_SHORTAGE_EXP_HERBS) * dt;
    }

    @Override
    public void update(double dt) {
        //No hace nada.
    }

    public String toString() {
        return "DefaultRegion";
    }
}
