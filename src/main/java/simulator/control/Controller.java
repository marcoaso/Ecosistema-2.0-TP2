package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {

    //Referencia al simulador.
    private Simulator sim;

    //Constructor.
    public Controller(Simulator sim) { //Recibe el simulador como parámetro.
        if (sim == null) // Validación para evitar que el simulador sea null.
            throw new IllegalArgumentException("Simulator cannot be null");
        this.sim = sim; 
    }


    public void loadData(JSONObject data) { //Carga los datos iniciales desde un JSON.
        if (data == null)
            return;

        //Carga regiones.
        if (data.has("regions")) {
            setRegions(data);  // data ya tiene la clave "regions". El método setRegions se encarga de procesar esa parte del JSON y configurar las regiones en el simulador.
        }

        //Carga animales.
        if (data.has("animals")) {
            JSONArray animals = data.getJSONArray("animals"); // data ya tiene la clave "animals". El JSON debe tener una estructura que incluya un campo "animals" que sea un array de objetos, donde cada objeto representa un tipo de animal a crear, con campos como "amount" y "spec".
        
            for (int i = 0; i < animals.length(); i++) {
                JSONObject a = animals.getJSONObject(i); //Obtiene la especificación de cada tipo de animal a crear. El JSON debe tener una estructura que incluya un campo "amount" para indicar cuántos animales crear y un campo "spec" con la especificación de ese tipo de animal.

                int amount = a.getInt("amount"); //Cuantos animales crear de un tipo.
                JSONObject spec = a.getJSONObject("spec"); //Especificación del animal.

                for (int j = 0; j < amount; j++) {
                    sim.addAnimal(spec); //El método addAnimal del simulador se encarga de crear un nuevo animal según la especificación dada en el JSON y agregarlo al simulador. El JSON debe tener la información necesaria para crear el animal, como su código genético, dieta, rango de visión, velocidad inicial, estrategia de apareamiento y posición inicial.
                }
            }
        }
    }

    //Ejecuta la simulación.
    public void run(double t, double dt, boolean sv, OutputStream out) { //Recibe el tiempo total de simulación, el paso de tiempo, un booleano para indicar si se muestra el visor y un OutputStream para escribir el resultado final en formato JSON.
        if (out == null) // Validación para evitar que el OutputStream sea null.
            return;

        //Guarda el estado inicial.
        JSONObject initState = sim.asJSON();

        //Inicializar visor.
        SimpleObjectViewer view = null; //Si el visor se encuentra a true, se creara una instancia de SimpleObjectViewer para mostrar la simulacion de forma grafica. 
        if (sv) { //Si se ha marcado que muestre el visor, se inicializa el visor con la información del mapa y se muestra el estado inicial de la simulación. 
            MapInfo m = sim.getMapInfo();
            view = new SimpleObjectViewer("[ECOSYSTEM]", m.getWidth(), m.getHeight(), m.getCols(), m.getRows()); //Crea la ventana.
            //Muestra el estado inicial.
            view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
        }

        //Bucle de simulación.
        while (sim.getTime() <= t) {
            sim.advance(dt); //Avanza la simulación.

            //Actualiza visor si sv = true.
            if (sv) {
                view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
            }
        }

        //Cerrar visor.
        if (sv) { //Si se ha mostrado el visor, se cierra la ventana al finalizar la simulación.
            view.close();
        }
        
        //Guarda el estado final.
        JSONObject finalState = sim.asJSON();

        //Crea el JSON resultado.
        JSONObject result = new JSONObject();
        result.put("in", initState); //El estado inicial se guarda bajo la clave "in".
        result.put("out", finalState); //El estado final se guarda bajo la clave "out".

        //Escribir en el OutputStream.
        PrintStream p = new PrintStream(out);
        p.println(result.toString(2));  //El 2 es para formateo con sangría. 
    }

    //Convierte los animales en objetos gráficos para el visor.
    private List<ObjInfo> toAnimalsInfo(List<? extends AnimalInfo> animals) { //Creamos una lista de ObjInfo a partir de la lista de animales. Cada ObjInfo representa un animal con su codigo genetico, posición y tamaño (que se calcula a partir de la edad del animal).
        List<ObjInfo> ol = new ArrayList<>(animals.size()); //Creamos una lista de ObjInfo con el mismo tamaño que la lista de animales para almacenar la información gráfica de cada animal.
        for (AnimalInfo a : animals) {
            int size = ((int) Math.round(a.getAge()) + 2); //Tamaño según edad.
            ol.add(new ObjInfo(a.getGeneticCode(),
                    (int) a.getPosition().getX(),
                    (int) a.getPosition().getY(),
                    size)); //Dibuja al animal según su código genético, posición y edad.
        }
        return ol;
    }

    public void reset(int cols, int rows, int width, int height) {
         sim.reset(cols, rows, width, height);
    }

    public void setRegions(JSONObject rs) {
        if (rs == null)
            return;

        JSONArray regions = rs.getJSONArray("regions");
        for (int i = 0; i < regions.length(); i++) {
            JSONObject r = regions.getJSONObject(i);

            int firstRow = r.getJSONArray("row").getInt(0); //fila inicial.
            int finalRow = r.getJSONArray("row").getInt(1); //fila final.
            int firstCol = r.getJSONArray("col").getInt(0); //columna inicial.
            int finalCol = r.getJSONArray("col").getInt(1); //columna final.
            JSONObject spec = r.getJSONObject("spec");  //Especificación de la región (tipo y parámetros).

            for (int row = firstRow; row <= finalRow; row++) {
                for (int col = firstCol; col <= finalCol; col++) {
                    sim.setRegion(row, col, spec);
                }
            }
        }
    }

    public void advance(double dt){
        sim.advance(dt);
    }

    public void addObserver(EcoSysObserver o){
        sim.addObserver(o);
    }

    public void removeObserver(EcoSysObserver o){
        sim.removeObserver(o);
    }
}
