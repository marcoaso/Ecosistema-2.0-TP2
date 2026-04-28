package simulator.model;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {

    public enum State {
        NORMAL,
        MATE,
        HUNGER,
        DANGER,
        DEAD
    }

    public enum Diet {
        HERBIVORE,
        CARNIVORE
    }

    //Atributos de los animales.
    protected String geneticCode;
    protected Diet diet;
    protected State state;

    protected Vector2D pos;
    protected Vector2D dest;

    protected double energy;
    protected double speed;
    protected double age;
    protected double desire;
    protected double sightRange;

    protected Animal mateTarget;
    protected Animal baby;

    protected AnimalMapView regionMngr;
    protected SelectionStrategy mateStrategy;

    final static double INIT_ENERGY = 100.0;
    final static double MUTATION_TOLERANCE = 0.2;
    final static double NEARBY_FACTOR = 60.0;
    final static double COLLISION_RANGE = 8;
    final static double HUNGER_DECAY_EXP_FACTOR = 0.007;
    final static double MAX_ENERGY = 100.0;
    final static double MAX_DESIRE = 100.0;

    //Constructores.
    protected Animal(String geneticCode, Diet diet, double sightRange,
            double initSpeed, SelectionStrategy mateStrategy,
            Vector2D pos) {

        //Validaciones.
        if (geneticCode == null || geneticCode.isEmpty())
            throw new IllegalArgumentException("Genetic code must be non-empty");

        if (sightRange <= 0)
            throw new IllegalArgumentException("Sight range must be positive");

        if (initSpeed <= 0)
            throw new IllegalArgumentException("Initial speed must be positive");

        if (mateStrategy == null)
            throw new IllegalArgumentException("Mate strategy cannot be null");

        this.geneticCode = geneticCode;
        this.diet = diet;
        this.sightRange = sightRange;
        this.mateStrategy = mateStrategy;
        this.pos = pos; // puede ser null (se inicializa en init).

        //Inicialización calculada.
        this.speed = Utils.getRandomizedParameter(initSpeed, 0.1);

        //Valores por defecto.
        this.state = State.NORMAL;
        this.energy = 100.0;
        this.desire = 0.0;
        this.age = 0.0;

        this.dest = null;
        this.mateTarget = null;
        this.baby = null;
        this.regionMngr = null;
    }

    protected Animal(Animal p1, Animal p2) {

        //Valores heredados.
        this.geneticCode = p1.geneticCode;
        this.diet = p1.diet;
        this.mateStrategy = p2.mateStrategy;

        //Estado inicial.
        this.state = State.NORMAL;
        this.desire = 0.0;
        this.age = 0.0;

        this.dest = null;
        this.mateTarget = null;
        this.baby = null;
        this.regionMngr = null;

        //Energía.
        this.energy = (p1.energy + p2.energy) / 2.0;

        //Posición cerca del progenitor p1.
        this.pos = p1.getPosition().plus(Vector2D.getRandomVector(-1, 1).scale(60.0 * (Utils.RAND.nextGaussian() + 1)));

        //Mutaciones.
        this.sightRange = Utils.getRandomizedParameter((p1.getSightRange() + p2.getSightRange()) / 2.0, 0.2);

        this.speed = Utils.getRandomizedParameter((p1.getSpeed() + p2.getSpeed()) / 2.0, 0.2);
    }

    //Getters (AnimalInfo).
    @Override
    public State getState() {
        return state;
    }

    @Override
    public Vector2D getPosition() {
        return pos;
    }

    @Override
    public String getGeneticCode() {
        return geneticCode;
    }

    @Override
    public Diet getDiet() {
        return diet;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public double getSightRange() {
        return sightRange;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public double getAge() {
        return age;
    }

    @Override
    public Vector2D getDestination() {
        return dest;
    }

    @Override
    public boolean isPregnant() {
        return baby != null;
    }

    //Métodos.
    public void init(AnimalMapView regMngr) {
        this.regionMngr = regMngr;

        //Inicializa posición.
        if (this.pos == null) {
            double x = Utils.RAND.nextDouble() * (regMngr.getWidth() - 1);
            double y = Utils.RAND.nextDouble() * (regMngr.getHeight() - 1);
            this.pos = new Vector2D(x, y);
        } else {
            //Ajusta la posición para que esté dentro del mapa.
            if (this.pos != null) {
                double x = Math.max(0, Math.min(this.pos.getX(), regMngr.getWidth() - 1));
                double y = Math.max(0, Math.min(this.pos.getY(), regMngr.getHeight() - 1));
                this.pos = new Vector2D(x, y);
            }
        }

        //Elege destino aleatorio.
        double dx = Utils.RAND.nextDouble() * (regMngr.getWidth() - 1);
        double dy = Utils.RAND.nextDouble() * (regMngr.getHeight() - 1);
        this.dest = new Vector2D(dx, dy);
    }

    public Animal deliverBaby() {
        Animal b = baby;
        baby = null;
        return b;
    }

    protected void move(double speed) {
        if (dest != null) {
            pos = pos.plus(dest.minus(pos).direction().scale(speed));
        }
    }

    protected void setState(State state) {
        this.state = state;

        switch (state) {
            case NORMAL:
                setNormalStateAction();
                break;
            case MATE:
                setMateStateAction();
                break;
            case HUNGER:
                setHungerStateAction();
                break;
            case DANGER:
                setDangerStateAction();
                break;
            case DEAD:
                setDeadStateAction();
                break;
        }
    }

    abstract protected void setNormalStateAction();
    abstract protected void setMateStateAction();
    abstract protected void setHungerStateAction();
    abstract protected void setDangerStateAction();
    abstract protected void setDeadStateAction();

    @Override
    public JSONObject asJSON() {
        JSONObject json = new JSONObject();

        json.put("pos", new double[] { pos.getX(), pos.getY() });
        json.put("gcode", geneticCode);
        json.put("diet", diet.toString());
        json.put("state", state.toString());

        return json;
    }

    protected double distanceTo(Animal other) {
        return pos.distanceTo(other.getPosition());
    }

    //Lógica del animal.
    @Override
    public void update(double dt){
        //Si el lobo esta muerto no realiza ninguna accion.
        if (state == State.DEAD)
            return;

        //Actualiza el comportamiento segun el estado actual.
        switch (state) {
            case NORMAL:
                updateNormal(dt);
                break;
            case HUNGER:
                updateHunger(dt);
                break;
            case MATE:
                updateMate(dt);
                break;
            case DANGER:
                updateDanger(dt);
                break;
            default:
                break;
        }

        //Ajusta la posicion si sale fuera del mapa.
        if (regionMngr.isOutside(pos)) {
            pos = regionMngr.adjustPosition(pos);
            setState(State.NORMAL);
        }
    }

    protected abstract void updateDanger(double dt);

    protected abstract void updateMate(double dt);

    protected abstract void updateHunger(double dt);

    protected abstract void updateNormal(double dt);
}