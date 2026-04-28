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
    public Controller(Simulator sim) {
        if (sim == null)
            throw new IllegalArgumentException("Simulator cannot be null");
        this.sim = sim;
    }


    public void loadData(JSONObject data) {
        if (data == null)
            return;

        //Carga regiones.
        if (data.has("regions")) {
            setRegions(data);  // data ya tiene la clave "regions", está bien
        }

        //Carga animales.
        if (data.has("animals")) {
            JSONArray animals = data.getJSONArray("animals");
            for (int i = 0; i < animals.length(); i++) {
                JSONObject a = animals.getJSONObject(i);

                int amount = a.getInt("amount"); //Cuantos animales crear de un tipo.
                JSONObject spec = a.getJSONObject("spec"); //Especificación del animal.

                for (int j = 0; j < amount; j++) {
                    sim.addAnimal(spec);
                }
            }
        }
    }

    //Ejecuta la simulación.
    public void run(double t, double dt, boolean sv, OutputStream out) {
        if (out == null)
            return;

        //Guarda el estado inicial.
        JSONObject initState = sim.asJSON();

        //Inicializar visor.
        SimpleObjectViewer view = null;
        if (sv) {
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
        if (sv) {
            view.close();
        }
        
        //Guarda el estado final.
        JSONObject finalState = sim.asJSON();

        //Crea el JSON resultado.
        JSONObject result = new JSONObject();
        result.put("in", initState);
        result.put("out", finalState);

        //Escribir en el OutputStream.
        PrintStream p = new PrintStream(out);
        p.println(result.toString(2));  //El 2 es para formateo con indentación.
    }

    //Convierte los animales en objetos gráficos para el visor.
    private List<ObjInfo> toAnimalsInfo(List<? extends AnimalInfo> animals) {
        List<ObjInfo> ol = new ArrayList<>(animals.size());
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
