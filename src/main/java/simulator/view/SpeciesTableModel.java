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
     * Fila interna de la tabla: especie y conteo por estado.
     */
    private class SpeciesTableModelRow implements Comparable<SpeciesTableModelRow> {
        private String speciesName;
        // counts[i] guarda cuántos animales hay en el estado i.
        private int[] counts; // i = State.ordinal()

        SpeciesTableModelRow(String speciesName) {
            this.speciesName = speciesName;
            this.counts = new int[State.values().length];
        }

        @Override
        public int compareTo(SpeciesTableModelRow o) {
            // Orden alfabético por nombre de especie.
            return this.speciesName.compareTo(o.speciesName);
        }
    }

    // Filas que se muestran en la tabla.
    private List<SpeciesTableModelRow> rows;
    // Columnas: Species + una columna por cada estado.
    private String[] columnNames;

    SpeciesTableModel(Controller ctrl) {
        // Empieza vacío; se rellenará al recibir eventos.
        this.rows = new ArrayList<>();
        
        // Crea nombres de columnas dinámicamente según los estados del enum.
        State[] states = State.values();
        this.columnNames = new String[states.length + 1];
        this.columnNames[0] = "Species";
        for (int i = 0; i < states.length; i++) {
            this.columnNames[i + 1] = states[i].name();
        }

        // Se registra para actualizar la tabla cuando cambie el simulador.
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
        // Devuelve nombre de especie (columna 0) o conteo por estado.
        SpeciesTableModelRow row = rows.get(rowIndex);
        if (columnIndex == 0) {
            return row.speciesName;
        } else {
            return row.counts[columnIndex - 1];
        }
    }

    private void update(List<AnimalInfo> animals) {
        // Agrupa por especie y cuenta cuántos hay en cada estado.
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
        Collections.sort(this.rows); // Mantiene orden alfabético por especie.
        // Notifica a la tabla que su contenido se ha actualizado.
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