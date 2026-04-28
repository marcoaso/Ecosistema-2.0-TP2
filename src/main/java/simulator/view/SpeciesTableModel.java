package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.Animal.State;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {

    private Controller ctrl;
    private List<String> speciesNames;
    private Map<String, int[]> speciesCounts;
    private String[] columnNames = { "Species", "Number" };

    SpeciesTableModel(Controller ctrl) {
        this.ctrl = ctrl;
        this.speciesNames = new ArrayList<>();
        this.speciesCounts = new LinkedHashMap<>();

        State[] states = State.values();
        this.columnNames = new String[states.length + 1];
        this.columnNames[0] = "Species";
        for (int i = 0; i < states.length; i++) {
            this.columnNames[i + 1] = states[i].name();
        }
        // Nos registramos como observadores para recibir datos
        this.ctrl.addObserver(this);
    }

    // --- Métodos de AbstractTableModel ---

    @Override
    public int getRowCount() {
        return speciesNames.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String species = speciesNames.get(rowIndex);
        if (columnIndex == 0) {
            return species;
        } else {
            return speciesCounts.get(species)[columnIndex - 1];
        }
    }

    // --- Lógica de actualización (EcoSysObserver) ---

    private void update(List<AnimalInfo> animals) {
        // 1. Limpiamos los datos actuales
        speciesNames.clear();
        speciesCounts.clear();

        State[] states = State.values();

        // 2. Contamos cuántos animales hay de cada especie
        for (AnimalInfo a : animals) {
            String species = a.getGeneticCode();
            if (!speciesCounts.containsKey(species)) {
                speciesNames.add(species);
                speciesCounts.put(species, new int[states.length]);
            }
            
            speciesCounts.get(species) [a.getState().ordinal()]++;
            
        }

        // 3. Avisamos a la JTable de que los datos han cambiado para que se repinte
        fireTableDataChanged();
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        update(animals);
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        update(animals);
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
        update(animals);
    }

    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        update(animals);
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
        // El cambio de regiones no afecta al conteo de especies
    }
}