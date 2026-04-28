package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class RegionManager implements AnimalMapView {

    //Información del mapa.
    private int cols;
    private int rows;
    private int width;
    private int height;
    private int regionWidth;
    private int regionHeight;

    //Regiones y asignación animal.
    private Region[][] regions; //Cada celda es una región del mapa.
    private Map<Animal, Region> animalRegion; //Diccionario que para cada animal te dice en que región está. Animal es la clave y region el valor.

    //Constructora.
    public RegionManager(int cols, int rows, int width, int height) {
        //Comprobamos dimensiones válidas.
        if (cols <= 0 || rows <= 0 || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid map dimensions");
        }

        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;

        //Calculamos tamaño de cada región.
        this.regionWidth = width / cols;
        this.regionHeight = height / rows;

        //Inicializamos matriz de regiones con DefaultRegion.
        regions = new Region[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                regions[i][j] = new DefaultRegion(); //inicializa todas las regiones a DefaultRegion.
            }
        }

        //Mapa que asocia cada animal con su región.
        animalRegion = new HashMap<>(); //Inicializa el diccionario animal→región vacío.
    }

    //Devuelve la fila correspondiente a una posición.
    private int getRow(Vector2D pos) {
        return Math.min(rows - 1, Math.max(0, (int) (pos.getY() / regionHeight)));
    }

    //Devuelve la columna correspondiente a una posición.
    private int getCol(Vector2D pos) {
        return Math.min(cols - 1, Math.max(0, (int) (pos.getX() / regionWidth)));
    }

    //Devuelve la región correspondiente a una posición.
    private Region getRegion(Vector2D pos) {
        return regions[getRow(pos)][getCol(pos)];
    }

    //Sustituye una región concreta del mapa.
    public void setRegion(int row, int col, Region r) {
        if (r == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }

        Region old = regions[row][col];
        regions[row][col] = r;

        //Mover animales de la región antigua a la nueva.
        for (Animal a : old.getAnimals()) {
            r.addAnimal(a);
            animalRegion.put(a, r);
        }
    }

    //Registra un animal en el mapa.
    public void registerAnimal(Animal a) {
        if (a == null)
            return;

        //Inicializa el animal con el mapa.
        a.init(this);

        //Lo añadimos a su región correspondiente.
        Region r = getRegion(a.getPosition());
        r.addAnimal(a);
        animalRegion.put(a, r); //Lo añadimos al diccionario.
    }

    //Elimina un animal del mapa.
    public void unregisterAnimal(Animal a) {
        Region r = animalRegion.remove(a);
        if (r != null) {
            r.removeAnimal(a);
        }
    }

    //Actualiza la región de un animal si ha cambiado de posición.
    public void updateanimalRegion(Animal a) {
        Region current = animalRegion.get(a);
        Region newRegion = getRegion(a.getPosition());

        if (current != newRegion) {
            if (current != null)
                current.removeAnimal(a);

            newRegion.addAnimal(a);
            animalRegion.put(a, newRegion);
        }
    }

    //Ajusta una posición para que esté dentro del mapa.
    public Vector2D adjustPosition(Vector2D pos) {
        double x = Math.max(0, Math.min(pos.getX(), width - 1));
        double y = Math.max(0, Math.min(pos.getY(), height - 1));
        return new Vector2D(x, y);
    }

    //Comprueba si una posición está fuera del mapa.
    public boolean isOutside(Vector2D pos) {
        return pos.getX() < 0 || pos.getX() >= width ||
               pos.getY() < 0 || pos.getY() >= height;
    }

    //Devuelve una posición aleatoria dentro del mapa.
    public Vector2D getRandomPosition() {
        double x = Utils.RAND.nextDouble() * width;
        double y = Utils.RAND.nextDouble() * height;
        return new Vector2D(x, y);
    }

    //Comprueba si otro animal está dentro del rango visual.
    public boolean isInSight(Animal a, Animal other) {
        return a.getPosition().distanceTo(other.getPosition()) <= a.getSightRange();
    }

    //Devuelve animales en rango.
    public List<Animal> getAnimalsInRange(Animal a, double range, java.util.function.Predicate<Animal> filter) {
        List<Animal> animals = getAnimalsInRange(a, filter);
        animals.removeIf(animal -> animal.getPosition().distanceTo(a.getPosition()) > range);
        return animals;
    }

    //Obtiene comida de la región donde está el animal.
    @Override
    public double getFood(AnimalInfo a, double dt) {
        Region r = animalRegion.get(a);
        return (r != null) ? r.getFood(a, dt) : 0.0;
    }

    //Actualiza todas las regiones del mapa
    public void updateAllRegions(double dt) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                regions[i][j].update(dt);
            }
        }
    }

    //Devuelve animales visibles.
    @Override
    public List<Animal> getAnimalsInRange(Animal a, Predicate<Animal> filter) {
        List<Animal> result = new ArrayList<>();
        Vector2D pos = a.getPosition();
        double range = a.getSightRange();

        //Calculamos las regiones que podrían contener animales en rango
        int minRow = Math.max(0, getRow(new Vector2D(pos.getX(), pos.getY() - range)));
        int maxRow = Math.min(rows - 1, getRow(new Vector2D(pos.getX(), pos.getY() + range)));
        int minCol = Math.max(0, getCol(new Vector2D(pos.getX() - range, pos.getY())));
        int maxCol = Math.min(cols - 1, getCol(new Vector2D(pos.getX() + range, pos.getY())));

        //Recorremos solo esas regiones
        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                for (Animal other : regions[i][j].getAnimals()) {
                    if (other != a &&
                        pos.distanceTo(other.getPosition()) <= range &&
                        filter.test(other)) {

                        result.add(other);
                    }
                }
            }
        }

        return result;
    }

    //Info del mapa.
    @Override
    public int getCols() {
        return cols;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getRegionWidth() {
        return regionWidth;
    }

    @Override
    public int getRegionHeight() {
        return regionHeight;
    }

    //Devuelve el estado del mapa en formato JSON
    @Override
    public JSONObject asJSON() {
        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();

        //Serializamos cada región con su posición
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JSONObject o = new JSONObject();
                o.put("row", i);
                o.put("col", j);
                o.put("data", regions[i][j].asJSON());
                arr.put(o);
            }
        }

        json.put("regions", arr);
        return json;
    }
   
    @Override
    public Iterator<RegionData> iterator() {
        return new Iterator<RegionData>() {
            private int currentRow = 0;
            private int currentCol = 0;

            @Override
            public boolean hasNext() {
                // Hay más elementos si no hemos llegado al final de las filas
                return currentRow < rows;
            }

            @Override
            public RegionData next() {
                if (!hasNext()) {
                        throw new IllegalStateException("No more regions");
                }

                // Guardamos los datos de la región actual
                RegionData data = new RegionData(currentRow, currentCol, regions[currentRow][currentCol]);

                // Avanzamos a la siguiente posición (por filas, de izquierda a derecha)
                currentCol++;
                if (currentCol >= cols) {
                    currentCol = 0;
                    currentRow++;
                }

                return data;
            }
        };
    }
}

