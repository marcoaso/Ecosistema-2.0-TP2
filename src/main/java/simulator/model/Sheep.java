package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {

    //Define los atributos especificos de la oveja.
    private Animal dangerSource;
    private SelectionStrategy dangerStrategy;

    //Define las constantes propias del comportamiento de la oveja.
    final static String SHEEP_GENETIC_CODE = "Sheep";
    final static double INIT_SIGHT_SHEEP = 40;
    final static double INIT_SPEED_SHEEP = 35;
    final static double BOOST_FACTOR_SHEEP = 2.0;
    final static double MAX_AGE_SHEEP = 8;
    final static double FOOD_DROP_BOOST_FACTOR_SHEEP = 1.2;
    final static double FOOD_DROP_RATE_SHEEP = 20.0;
    final static double DESIRE_THRESHOLD_SHEEP = 65.0;
    final static double DESIRE_INCREASE_RATE_SHEEP = 40.0;
    final static double PREGNANT_PROBABILITY_SHEEP = 0.9;

    //Constructor principal.
    public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy, Vector2D pos) {

        super(SHEEP_GENETIC_CODE, Diet.HERBIVORE, INIT_SIGHT_SHEEP, INIT_SPEED_SHEEP, mateStrategy, pos);

        if (dangerStrategy == null)
            throw new IllegalArgumentException("Danger strategy cannot be null");

        this.dangerStrategy = dangerStrategy;
        this.dangerSource = null;
    }

    //Constructor de una oveja bebe.
    protected Sheep(Sheep p1, Animal p2) {
        super(p1, p2);

        this.dangerStrategy = p1.dangerStrategy;
        this.dangerSource = null;
    }

    //Comportamiento de la oveja.
    @Override
    public void update(double dt) {

        super.update(dt);

        //Comprueba muerte por falta de energia o por superar la edad maxima.
        if (energy <= 0.0 || age > MAX_AGE_SHEEP) {
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

        //Busca peligro.
        if (dangerSource == null)
            findDanger();

        //Cambia a estado de peligro si detecta amenaza o a apareamiento si el deseo es alto.
        if (dangerSource != null)
            setState(State.DANGER);
        else if (desire > DESIRE_THRESHOLD_SHEEP)
            setState(State.MATE);
    }

    //UPDATE DANGER.
    protected void updateDanger(double dt) {

        //Elimina la fuente de peligro si ha muerto.
        if (dangerSource != null && dangerSource.getState() == State.DEAD)
            dangerSource = null;

        //Si no hay peligro se mueve normalmente.
        if (dangerSource == null) {
            moveNormally(dt);

        } else {
            //Huir del peligro.
            dest = pos.plus(pos.minus(dangerSource.getPosition()).direction());
            move(BOOST_FACTOR_SHEEP * speed * dt * Math.exp((energy - MAX_DESIRE) * HUNGER_DECAY_EXP_FACTOR));

            //Actualiza edad, energia y deseo durante la huida.
            age += dt;
            energy = clampEnergy(energy - FOOD_DROP_RATE_SHEEP * FOOD_DROP_BOOST_FACTOR_SHEEP * dt);
            desire = clampDesire(desire + DESIRE_INCREASE_RATE_SHEEP * dt);
        }

        //Reevalua el estado cuando desaparece el peligro.
        if (dangerSource == null || !regionMngr.isInSight(this, dangerSource)) {

            findDanger();
            
            if (dangerSource == null) {
                if (desire < DESIRE_THRESHOLD_SHEEP) {
                    setState(State.NORMAL);
                }

                else {
                    setState(State.MATE);
                }
            }
        }  
    }

    //UPDATE MATE.
    protected void updateMate(double dt) {

        //Descarta la pareja si ha muerto o esta fuera del rango visual.
        if (mateTarget != null && (mateTarget.getState() == State.DEAD || !regionMngr.isInSight(this, mateTarget))) {
            mateTarget = null;
        }

        //Busca nueva pareja si no tiene una asignada.
        if (mateTarget == null) {
            findMate();
            if (mateTarget == null) {
                moveNormally(dt);
            } 
        
        } else {
            goForMate(dt);
        }

        //Busca peligro.
        if (dangerSource == null)
            findDanger();

        if (dangerSource != null)
            setState(State.DANGER);
        else if (desire < DESIRE_THRESHOLD_SHEEP)
            setState(State.NORMAL);
    }

    //Gestiona el movimiento hacia la pareja y la posible reproduccion.
    private void goForMate(double dt){

        //Se dirige hacia la posicion de la pareja.
        dest = mateTarget.getPosition();
        move(BOOST_FACTOR_SHEEP * speed * dt * Math.exp((energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR));

        //Actualiza edad, energia y deseo durante el acercamiento.
        age += dt;
        energy = clampEnergy(energy - FOOD_DROP_RATE_SHEEP * FOOD_DROP_BOOST_FACTOR_SHEEP * dt);
        desire = clampDesire(desire + DESIRE_INCREASE_RATE_SHEEP * dt);

        //Emparejamiento.
        if (pos.distanceTo(mateTarget.getPosition()) < COLLISION_RANGE) {

            desire = 0.0;
            mateTarget.desire = 0.0;

            //Crea una nueva oveja con cierta probabilidad si no hay una pendiente.
            if (baby == null && Utils.RAND.nextDouble() < PREGNANT_PROBABILITY_SHEEP) {
                baby = new Sheep(this, mateTarget);
            }

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

        age += dt;
        energy = clampEnergy(energy - FOOD_DROP_RATE_SHEEP * dt);
        desire = clampDesire(desire + DESIRE_INCREASE_RATE_SHEEP * dt);
    }

    //Busca depredadores dentro del rango visual.
    private void findDanger() {
        List<Animal> dangers = regionMngr.getAnimalsInRange(this, sightRange, a -> a.getDiet() == Diet.CARNIVORE);
        dangerSource = dangerStrategy.select(this, dangers);
    }

    //Busca posibles parejas de la misma especie dentro del rango visual.
    private void findMate() {
        List<Animal> mates = regionMngr.getAnimalsInRange(this, sightRange,
                a -> a.getGeneticCode().equals(this.geneticCode));

        mateTarget = mateStrategy.select(this, mates);
    }

    private double clampEnergy(double e) {
        return Math.max(0.0, Math.min(MAX_ENERGY, e));
    }

    private double clampDesire(double d) {
        return Math.max(0.0, Math.min(MAX_DESIRE, d));
    }

    //Acciones al cambiar de estado.
    @Override
    protected void setNormalStateAction() {
        dangerSource = null;
        mateTarget = null;
    }

    @Override
    protected void setMateStateAction() {
        dangerSource = null;
    }

    @Override
    protected void setHungerStateAction() {
        // Nunca se usa
    }

    @Override
    protected void setDangerStateAction() {
        mateTarget = null;
    }

    @Override
    protected void setDeadStateAction() {
        dangerSource = null;
        mateTarget = null;
    }

    @Override
    protected void updateHunger(double dt) {
        //Nunca se usa.
    }
}
