package simulator.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    /**
     * Esta clase actúa como adaptador. La vista solo conoce lo necesario
     * para pintar la fila, desacoplándose de las interfaces del modelo.
     */
    private class SpeciesTableModelRow implements Comparable<SpeciesTableModelRow> {
        private String speciesName;
        private int[] counts; // Índices corresponden a State.ordinal()

        SpeciesTableModelRow(String speciesName) {
            this.speciesName = speciesName;
            this.counts = new int[State.values().length];
        }

        @Override
        public int compareTo(SpeciesTableModelRow o) {
            return this.speciesName.compareTo(o.speciesName);
        }
    }

    private List<SpeciesTableModelRow> rows;
    private String[] columnNames;

    SpeciesTableModel(Controller ctrl) {
        this.rows = new ArrayList<>();
        
        // Configurar nombres de columnas (Species + Nombres de Estados)
        State[] states = State.values();
        this.columnNames = new String[states.length + 1];
        this.columnNames[0] = "Species";
        for (int i = 0; i < states.length; i++) {
            this.columnNames[i + 1] = states[i].name();
        }

        ctrl.addObserver(this);
    }

    @Override
    public int getRowCount() {
        return rows.size();
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
        SpeciesTableModelRow row = rows.get(rowIndex);
        if (columnIndex == 0) {
            return row.speciesName;
        } else {
            return row.counts[columnIndex - 1];
        }
    }

    private void update(List<AnimalInfo> animals) {
        // Transformar la información del modelo a nuestra estructura privada de la vista
        Map<String, SpeciesTableModelRow> map = new HashMap<>();

        for (AnimalInfo a : animals) {
            String gCode = a.getGeneticCode();
            SpeciesTableModelRow row = map.get(gCode);
            if (row == null) {
                row = new SpeciesTableModelRow(gCode);
                map.put(gCode, row);
            }
            row.counts[a.getState().ordinal()]++;
        }

        this.rows = new ArrayList<>(map.values());
        Collections.sort(this.rows); // Mantenemos el orden alfabético por nombre
        fireTableDataChanged();
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) { update(animals); }
    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) { update(animals); }
    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) { update(animals); }
    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) { update(animals); }
    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {}
}