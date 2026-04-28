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

    private Controller ctrl;
    private List<RegionData> regions;
    private MapInfo map;
    private String[] columnNames = { "Row", "Column", "Region Type" };

    RegionsTableModel(Controller ctrl) {
        this.ctrl = ctrl;
        this.regions = new ArrayList<>();

        Diet[] diets = Diet.values();
        this.columnNames = new String[diets.length + 3];
        this.columnNames[0] = "Row";
        this.columnNames[1] = "Col";
        this.columnNames[2] = "Desc";
        for (int i = 0; i < diets.length; i++) {
            this.columnNames[i + 3] = diets[i].name();
        }
        // Nos registramos como observadores
        this.ctrl.addObserver(this);
    }

    // --- Métodos de AbstractTableModel ---

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
        RegionData rd = regions.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return rd.row();
            case 1:
                return rd.col();
            case 2:
                return rd.r().toString();
            default:
                // Contar animales de esta región por dieta
                Diet diet = Diet.values()[columnIndex - 3];
                int count = 0;
                for (AnimalInfo a : rd.r().getAnimalsInfo()) {
                    if (a.getDiet() == diet) {
                        count++;
                    }
                }
                return count;
        }
    }

    // --- Lógica de actualización (EcoSysObserver) ---

    private void update(MapInfo map) {
        this.map = map;
        this.regions.clear();

        // Como MapInfo implementa Iterable<MapInfo.RegionData>,
        // ahora el for funciona perfectamente
        for (RegionData rd : map) {
            this.regions.add(rd);
        }

        // Notificamos el cambio a la tabla
        fireTableDataChanged();
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        update(map);
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        update(map);
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
        update(map);
    }

    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        update(map);
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
        update(map); // Si cambia una región específica, refrescamos la tabla
    }
}