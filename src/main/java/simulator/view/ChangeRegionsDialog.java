package simulator.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class ChangeRegionsDialog extends JDialog implements EcoSysObserver {

    private DefaultComboBoxModel<String> regionsModel;
    private DefaultComboBoxModel<String> fromRowModel;
    private DefaultComboBoxModel<String> toRowModel;
    private DefaultComboBoxModel<String> fromColModel;
    private DefaultComboBoxModel<String> toColModel;

    private DefaultTableModel dataTableModel;
    private Controller ctrl;
    private List<JSONObject> regionsInfo;

    private String[] headers = { "Key", "Value", "Description" };
    private int status; // 0 para Cancel, 1 para OK

    ChangeRegionsDialog(Controller ctrl) {
        super((Frame) null, true);
        this.ctrl = ctrl;
        initGUI();
        this.ctrl.addObserver(this);
    }

    private void initGUI() {
        setTitle("Change Regions");
        status = 0;

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // 1. Panel de Ayuda
        JLabel helpLabel = new JLabel("<html>Select a region type and its parameters, then specify the range of rows and columns.</html>");
        helpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(helpLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 2. Panel de la Tabla
        this.regionsInfo = Main.regionsFactory.getInfo();
        this.dataTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Solo la columna "Value" es editable
            }
        };
        this.dataTableModel.setColumnIdentifiers(this.headers);
        JTable dataTable = new JTable(this.dataTableModel);
        mainPanel.add(new JScrollPane(dataTable));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 3. Panel de Configuración (Región y Coordenadas)
        JPanel configPanel = new JPanel();
        
        // Selector de tipo de región
        this.regionsModel = new DefaultComboBoxModel<>();
        for (JSONObject info : regionsInfo) {
            this.regionsModel.addElement(info.getString("desc"));
        }
        JComboBox<String> regionsCombo = new JComboBox<>(this.regionsModel);
        regionsCombo.addActionListener(e -> updateTableModel(regionsCombo.getSelectedIndex()));
        
        configPanel.add(new JLabel("Region Type:"));
        configPanel.add(regionsCombo);

        // Modelos de coordenadas
        fromRowModel = new DefaultComboBoxModel<>();
        toRowModel = new DefaultComboBoxModel<>();
        fromColModel = new DefaultComboBoxModel<>();
        toColModel = new DefaultComboBoxModel<>();

        configPanel.add(new JLabel("Row From:"));
        configPanel.add(new JComboBox<>(fromRowModel));
        configPanel.add(new JLabel("To:"));
        configPanel.add(new JComboBox<>(toRowModel));
        configPanel.add(new JLabel("Col From:"));
        configPanel.add(new JComboBox<>(fromColModel));
        configPanel.add(new JLabel("To:"));
        configPanel.add(new JComboBox<>(toColModel));

        mainPanel.add(configPanel);

        // 4. Panel de Botones
        JPanel buttonsPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            status = 0;
            setVisible(false);
        });

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> handleOk());

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(okButton);
        mainPanel.add(buttonsPanel);

        // Inicializar tabla con la primera región
        if (!regionsInfo.isEmpty()) updateTableModel(0);

        setPreferredSize(new Dimension(800, 450));
        pack();
        setResizable(false);
        setVisible(false);
    }

    private void updateTableModel(int index) {
        dataTableModel.setRowCount(0);
        JSONObject info = regionsInfo.get(index);
        JSONObject data = info.getJSONObject("data");
        for (String key : data.keySet()) {
            dataTableModel.addRow(new Object[]{ key, "", data.get(key) });
        }
    }

    private void handleOk() {
        try {
            // 1. Obtener region_data de la tabla
            JSONObject regionData = new JSONObject();
            for (int i = 0; i < dataTableModel.getRowCount(); i++) {
                String key = (String) dataTableModel.getValueAt(i, 0);
                String val = (String) dataTableModel.getValueAt(i, 1);
                if (val != null && !val.trim().isEmpty()) {
                    regionData.put(key, val);
                }
            }

            // 2. Obtener tipo y coordenadas
            int selIdx = regionsModel.getIndexOf(regionsModel.getSelectedItem());
            String regionType = regionsInfo.get(selIdx).getString("type");

            int rowFrom = Integer.parseInt((String) fromRowModel.getSelectedItem());
            int rowTo = Integer.parseInt((String) toRowModel.getSelectedItem());
            int colFrom = Integer.parseInt((String) fromColModel.getSelectedItem());
            int colTo = Integer.parseInt((String) toColModel.getSelectedItem());

            // 3. Crear JSON final
            JSONObject spec = new JSONObject();
            spec.put("type", regionType);
            spec.put("data", regionData);

            JSONObject region = new JSONObject();
            region.put("row", new JSONArray().put(rowFrom).put(rowTo));
            region.put("col", new JSONArray().put(colFrom).put(colTo));
            region.put("spec", spec);

            JSONObject finalJSON = new JSONObject();
            finalJSON.put("regions", new JSONArray().put(region));

            // 4. Enviar al controlador
            ctrl.setRegions(finalJSON);
            status = 1;
            setVisible(false);

        } catch (Exception e) {
            ViewUtils.showErrorMsg("Error setting regions: " + e.getMessage());
        }
    }

    public void open(Frame parent) {
        setLocation(
            parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2,
            parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
        pack();
        setVisible(true);
    }

    private void updateCoordModels(int rows, int cols) {
        fromRowModel.removeAllElements();
        toRowModel.removeAllElements();
        fromColModel.removeAllElements();
        toColModel.removeAllElements();
        for (int i = 0; i < rows; i++) {
            fromRowModel.addElement(String.valueOf(i));
            toRowModel.addElement(String.valueOf(i));
        }
        for (int i = 0; i < cols; i++) {
            fromColModel.addElement(String.valueOf(i));
            toColModel.addElement(String.valueOf(i));
        }
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        updateCoordModels(map.getRows(), map.getCols());
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        updateCoordModels(map.getRows(), map.getCols());
    }

    @Override public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {}

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
        // No es necesario actualizar el diálogo cuando cambia una región    
    }

    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        // No es necesario actualizar el diálogo en cada paso de simulación    
    }
}