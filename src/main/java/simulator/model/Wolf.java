package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {

    //Define los atributos especificos del lobo.
    private Animal huntTarget;
    private SelectionStrategy huntingStrategy;

    //Define las constantes propias del comportamiento del lobo.
    final static String WOLF_GENETIC_CODE = "Wolf";
    final static double INIT_SIGHT_WOLF = 50;
    final static double INIT_SPEED_WOLF = 60;
    final static double BOOST_FACTOR_WOLF = 3.0;
    final static double MAX_AGE_WOLF = 14.0;
    final static double FOOD_THRSHOLD_WOLF = 50.0;
    final static double FOOD_DROP_BOOST_FACTOR_WOLF = 1.2;
    final static double FOOD_DROP_RATE_WOLF = 18.0;
    final static double FOOD_DROP_DESIRE_WOLF = 10.0;
    final static double FOOD_EAT_VALUE_WOLF = 50.0;
    final static double DESIRE_THRESHOLD_WOLF = 65.0;
    final static double DESIRE_INCREASE_RATE_WOLF = 30.0;
    final static double PREGNANT_PROBABILITY_WOLF = 0.75;

    //Constructor principal que crea un lobo con estrategias y posicion inicial.
    public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy, Vector2D pos) {
        super(WOLF_GENETIC_CODE, Diet.CARNIVORE, INIT_SIGHT_WOLF, INIT_SPEED_WOLF, mateStrategy, pos);

        if (huntingStrategy == null)
            throw new IllegalArgumentException("Hunting strategy cannot be null");

        this.huntingStrategy = huntingStrategy;
        this.huntTarget = null;
    }

    //Constructor protegido que se utiliza cuando nace un nuevo lobo.
    protected Wolf(Wolf p1, Animal p2) {
        super(p1, p2);

        //Hereda la estrategia de caza del primer progenitor (p1).
        this.huntingStrategy = p1.huntingStrategy;
        this.huntTarget = null;
    }

    //Metodo principal que actualiza el estado del lobo en cada tick.
    @Override
    public void update(double dt) {
        super.update(dt);
        //Comprueba muerte por falta de energia o por superar la edad maxima.
        if (energy <= 0.0 || age > MAX_AGE_WOLF) {
            setState(State.DEAD);
            return;
        }

        //Obtiene comida de la region y actualiza la energia.
        double food = regionMngr.getFood(this, dt);
        energy = clampEnergy(energy + food);
    }

    //UPDATE NORMAL.
    protected void updateNormal(double dt) {

        //Se mueve de forma normal.
        moveNormally(dt);

        //Cambia a estado de hambre si la energia es baja o a apareamiento si el deseo es alto.
        if (energy < FOOD_THRSHOLD_WOLF)
            setState(State.HUNGER);
        else if (desire > DESIRE_THRESHOLD_WOLF)
            setState(State.MATE);
    }

    //UPDATE HUNGER.
    protected void updateHunger(double dt) {

        //Busca una nueva presa si no tiene o si ya no es valida.
        if (huntTarget == null || huntTarget.getState() == State.DEAD || !regionMngr.isInSight(this, huntTarget)) {
            findHuntTarget();
        }

        //Si no encuentra presa se mueve normal.
        if (huntTarget == null) {
            moveNormally(dt);
        } else {
            //Persigue a la presa con velocidad aumentada.
            dest = huntTarget.getPosition();
            move(BOOST_FACTOR_WOLF * speed * dt * Math.exp((energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR));

            //Actualiza edad, energía y deseo durante la persecucion.
            age += dt;
            energy = clampEnergy(energy - FOOD_DROP_RATE_WOLF * FOOD_DROP_BOOST_FACTOR_WOLF * dt);
            desire = clampDesire(desire + DESIRE_INCREASE_RATE_WOLF * dt);

            //Si la distancia a la presa es menor a 8 la mata y recupera energia.
            if (distanceTo(huntTarget) < COLLISION_RANGE) {

                huntTarget.setState(State.DEAD);
                huntTarget = null;

                energy = clampEnergy(energy + FOOD_EAT_VALUE_WOLF);
            }
        }

        //Cambia de estado cuando recupera suficiente energia.
        if (energy > FOOD_THRSHOLD_WOLF) {
            if (desire < DESIRE_THRESHOLD_WOLF)
                setState(State.NORMAL);
            else
                setState(State.MATE);
        }
    }

    //UPDATE MATE.
    protected void updateMate(double dt) {

        //Descarta la pareja si ha muerto o esta fuera del rango visual.
        if (mateTarget != null && (mateTarget.getState() == State.DEAD || !regionMngr.isInSight(this, mateTarget))) {
            mateTarget = null;
        }

        //Busca pareja si no tiene una asignada.
        if (mateTarget == null) {
            findMate();
            if (mateTarget == null) {
                moveNormally(dt);
            } else {
                goForMate(dt);
            }
        } else {
            goForMate(dt);
        }

        //Tiene hambre?.
        if (energy < FOOD_THRSHOLD_WOLF)
            setState(State.HUNGER);
        else if (desire < DESIRE_THRESHOLD_WOLF)
            setState(State.NORMAL);
    }

    //Gestiona el movimiento hacia la pareja y la posible reproduccion.
    private void goForMate(double dt){

        //Se dirige hacia la posicion de la pareja.
        dest = mateTarget.getPosition();
        move(BOOST_FACTOR_WOLF * speed * dt * Math.exp((energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR));

        //Actualiza edad, energia y deseo durante el acercamiento.
        age += dt;
        energy = clampEnergy(energy - FOOD_DROP_RATE_WOLF * FOOD_DROP_BOOST_FACTOR_WOLF * dt);
        desire = clampDesire(desire + DESIRE_INCREASE_RATE_WOLF * dt);

        //Comprueba colision para iniciar reproduccion.
        if (pos.distanceTo(mateTarget.getPosition()) < COLLISION_RANGE) {

            //Reinicia el deseo tras el apareamiento.
            desire = 0.0;
            mateTarget.desire = 0.0;

            //Crea una nueva cria con cierta probabilidad.
            if (baby == null && Utils.RAND.nextDouble() < PREGNANT_PROBABILITY_WOLF) {
                baby = new Wolf(this, mateTarget);
            }

            //Reduce la energia tras reproducirse.
            energy = clampEnergy(energy - 10.0);
            mateTarget = null;
        }
    }

    //Realiza movimiento normal y actualiza atributos.
    private void moveNormally(double dt) {

        //Genera nuevo destino si no existe o ya ha sido alcanzado.
        if (dest == null || pos.distanceTo(dest) < COLLISION_RANGE) {
            dest = regionMngr.getRandomPosition();
        }

        //Se mueve segun velocidad y energia disponible.
        move(speed * dt * Math.exp((energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR));

        //Actualiza edad, energia y deseo.
        age += dt;
        energy = clampEnergy(energy - FOOD_DROP_RATE_WOLF * dt);
        desire = clampDesire(desire + DESIRE_INCREASE_RATE_WOLF * dt);
    }

    //Busca presas herbivoras dentro del rango visual.
    private void findHuntTarget() {
        List<Animal> preys = regionMngr.getAnimalsInRange(this, sightRange, a -> a.getDiet() == Diet.HERBIVORE);
        huntTarget = huntingStrategy.select(this, preys);
    }

    //Busca pareja de la misma especie dentro del rango visual.
    private void findMate() {
        List<Animal> mates = regionMngr.getAnimalsInRange(this, sightRange,
                a -> a.getGeneticCode().equals(geneticCode));
        mateTarget = mateStrategy.select(this, mates);
    }

    private double clampEnergy(double e) {
        return Math.max(0.0, Math.min(MAX_ENERGY, e));
    }

    private double clampDesire(double d) {
        return Math.max(0.0, Math.min(MAX_DESIRE, d));
    }

    //Se ejecuta al cambiar al estado normal.
    @Override
    protected void setNormalStateAction() {
        huntTarget = null;
        mateTarget = null;
    }

    //Se ejecuta al cambiar al estado de apareamiento.
    @Override
    protected void setMateStateAction() {
        huntTarget = null;
    }

    //Se ejecuta al cambiar al estado de hambre.
    @Override
    protected void setHungerStateAction() {
        mateTarget = null;
    }

    //No se utiliza en el lobo.
    @Override
    protected void setDangerStateAction() {
    }

    //Se ejecuta al cambiar al estado muerto.
    @Override
    protected void setDeadStateAction() {
        huntTarget = null;
        mateTarget = null;
    }

    @Override
    protected void updateDanger(double dt) {
    //Nunca se usa.
    }

}
