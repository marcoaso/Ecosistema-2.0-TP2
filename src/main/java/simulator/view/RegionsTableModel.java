package simulator.view;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.Animal.Diet;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.MapInfo.RegionData;

class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {

    /**
     * Representa la información de una fila. Desacopla la tabla de 
     * los objetos RegionInfo y RegionData del modelo.
     */
    private class RegionTableModelRow {
        private int row, col;
        private String desc;
        private int[] dietCounts;

        RegionTableModelRow(RegionData rd) {
            this.row = rd.row();
            this.col = rd.col();
            this.desc = rd.r().toString();
            this.dietCounts = new int[Diet.values().length];
            
            // Adaptamos la información: contamos animales por dieta en la región
            for (AnimalInfo a : rd.r().getAnimalsInfo()) {
                this.dietCounts[a.getDiet().ordinal()]++;
            }
        }
    }

    private List<RegionTableModelRow> regions;
    private String[] columnNames;

    RegionsTableModel(Controller ctrl) {
        this.regions = new ArrayList<>();

        Diet[] diets = Diet.values();
        this.columnNames = new String[diets.length + 3];
        this.columnNames[0] = "Row";
        this.columnNames[1] = "Col";
        this.columnNames[2] = "Desc";
        for (int i = 0; i < diets.length; i++) {
            this.columnNames[i + 3] = diets[i].name();
        }

        ctrl.addObserver(this);
    }

    @Override
    public int getRowCount() {
        return regions.size();
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
        RegionTableModelRow row = regions.get(rowIndex);
        switch (columnIndex) {
            case 0: return row.row;
            case 1: return row.col;
            case 2: return row.desc;
            default:
                return row.dietCounts[columnIndex - 3];
        }
    }

    private void update(MapInfo map) {
        this.regions = new ArrayList<>();
        // El modelo nos da un iterable de RegionData, nosotros lo adaptamos a filas privadas
        for (RegionData rd : map) {
            this.regions.add(new RegionTableModelRow(rd));
        }
        fireTableDataChanged();
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) { update(map); }
    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) { update(map); }
    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) { update(map); }
    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) { update(map); }
    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) { update(map); }
}