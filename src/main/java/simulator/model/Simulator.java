package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observer;

import org.json.JSONObject;

import simulator.factories.Factory;

public class Simulator implements JSONable, Observable<EcoSysObserver> {

    //Fábricas para crear animales y regiones.
    private Factory<Animal> animalsFactory;
    private Factory<Region> regionsFactory;

    //Gestiona el mapa.
    private RegionManager regionManager;

    //Lista de animales vivos.
    private List<Animal> animals;
    private List<EcoSysObserver> observers;

    //Tiempò actual de la simulación.
    private double time;

    //Constructora.
    public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory,
            Factory<Region> regionsFactory) {

        if (animalsFactory == null || regionsFactory == null)
            throw new IllegalArgumentException("Factorias no pueden ser null");

        this.animalsFactory = animalsFactory;
        this.regionsFactory = regionsFactory;

        this.regionManager = new RegionManager(cols, rows, width, height); //Crea el mapa.
        this.animals = new ArrayList<>(); //Crea los animles (Empieza sin animales).
        this.observers = new ArrayList<>(); //Creo lista observadores.
        this.time = 0.0; //Inicializa el tiempo a 0.
    }

    //Métodos para añadir regiones.
    private void setRegion(int row, int col, Region r) { //Este método (privado) delega en el RegionManager (Trabaja con el objeto ya creado). 
        if (r == null)
            throw new IllegalArgumentException("Region no puede ser null");
        regionManager.setRegion(row, col, r);
    }

    public void setRegion(int row, int col, JSONObject rJson) { //Este método (público) crea el objeto desde JSON y delega en el otro setRegion.
        Region r = regionsFactory.createInstance(rJson);
        setRegion(row, col, r);
        notifyOnRegionSet(row, col, r);
    }

    private void addAnimal(Animal a) { //Este método (privado) delega en el RegionManager.
        if (a == null)
            throw new IllegalArgumentException("Animal no puede ser null");
        animals.add(a);
        regionManager.registerAnimal(a);
        notifyOnAnimalAdded(a);
    }

    public void addAnimal(JSONObject aJson) { //Este método (público) crea el objeto desde JSON y delega en el otro addAnimal.
        Animal a = animalsFactory.createInstance(aJson);
        addAnimal(a);
    }

    //Getters.
    public MapInfo getMapInfo() { //Devuelve info del mapa.
        return regionManager;
    }

    public List<? extends AnimalInfo> getAnimals() { //Devuelve una lista inmutable (no se puede modificar).
        return Collections.unmodifiableList(animals);
    }

    public double getTime() { //Devuelve el tiempo actual.
        return time;
    }

    //Avanzar la simulación.
    public void advance(double dt) {
        if (dt <= 0.0) //Para validar.
            return;

        time += dt;

        //Quita los animales muertos.
        Iterator<Animal> it = animals.iterator(); //Usamos iterator para eliminar mientras recorre.
        while (it.hasNext()) {
            Animal a = it.next();
            if (a.getState() == Animal.State.DEAD) {
                regionManager.unregisterAnimal(a); //Si está muerto se quita de la región y de la lista.
                it.remove();
            }
        }

        //Actualiza cada animal y su región.
        for (Animal a : animals) {
            a.update(dt);
            regionManager.updateanimalRegion(a);
        }

        //Actualiza todas las regiones (recursos).
        regionManager.updateAllRegions(dt);

        //Gestión nacimientos.
        List<Animal> newborns = new ArrayList<>();
        for (Animal a : animals) {
            if (a.isPregnant()) { //Si el animal está embarazado se genera bebé y se añade a la lista temporal.
                Animal baby = a.deliverBaby();
                if (baby != null)
                    newborns.add(baby);
            }
        }
        for (Animal b : newborns) {
            addAnimal(b);
        }
        notifyOnAdvance(dt);
    }

    //Representación JSON.
    @Override
    public JSONObject asJSON() {
        JSONObject json = new JSONObject();
        json.put("time", time);
        json.put("state", regionManager.asJSON());
        return json;
    }

    public void reset(int cols, int rows, int width, int height) { //Reinicia la simulación (limpia todo y vuelve a crear el mapa).
        this.regionManager = new RegionManager(cols, rows, width, height);
        this.animals.clear();
        this.time = 0.0;
        notifyOnReset();
    }

    @Override
    public void addObserver(EcoSysObserver o) {
        if (o == null) return;
        if (!observers.contains(o)) {
            observers.add(o);
            // Solo notificar al observer recién registrado, no a todos
            List<AnimalInfo> animalsInfo = new ArrayList<>(animals);
            o.onRegister(time, regionManager, animalsInfo);
        }
    }

    @Override
    public void removeObserver(EcoSysObserver o) {
        if (o== null) return;
        if (observers.contains(o)) {
            observers.remove(o);
        }
    }

    private void notifyOnReset() {
        List<AnimalInfo> animalsInfo = new ArrayList<>(animals);
        for (EcoSysObserver o : observers) {
            o.onReset(time, regionManager, animalsInfo);
        }
    }

    private void notifyOnAnimalAdded(Animal a) {
        List<AnimalInfo> animalsInfo = new ArrayList<>(animals);
        for (EcoSysObserver o : observers) {
            o.onAnimalAdded(time, regionManager, animalsInfo, a);
        }
    }

    private void notifyOnRegionSet(int row, int col, Region r) {
        List<AnimalInfo> animalsInfo = new ArrayList<>(animals);
        for (EcoSysObserver o : observers) {
            o.onRegionSet(row, col, regionManager, r);
        }
    }

    private void notifyOnAdvance(double dt) {
        List<AnimalInfo> animalsInfo = new ArrayList<>(animals);
        for (EcoSysObserver o : observers) {
            o.onAdvance(time, regionManager, animalsInfo, dt);
        }
    }

    private void notifyOnRegister() {
        List<AnimalInfo> animalsInfo = new ArrayList<> (animals);            
        for (EcoSysObserver o : observers) {
            o.onRegister(time, regionManager,animalsInfo);        
        }
    }
}
