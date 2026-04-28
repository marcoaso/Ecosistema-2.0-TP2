package simulator.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

public class StatusBar extends JPanel implements EcoSysObserver {
    private JLabel timeLabel;
    private JLabel totalAnimalsLabel;
    private JLabel dimensionLabel;

    // El constructor ahora recibe el controlador para registrarse como observador
    public StatusBar(Controller ctrl) {
        this.initGUI();
        ctrl.addObserver(this);
    }

    private void initGUI() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setBorder(BorderFactory.createBevelBorder(1));

        // Inicializamos las etiquetas
        this.timeLabel = new JLabel("Time: 0.00");
        this.totalAnimalsLabel = new JLabel("Total Animals: 0");
        this.dimensionLabel = new JLabel("Dimension: 0x0");

        // Las añadimos al panel (this) con separadores
        this.add(this.timeLabel);
        this.add(createSeparator());
        this.add(this.totalAnimalsLabel);
        this.add(createSeparator());
        this.add(this.dimensionLabel);
    }

    // Método auxiliar para crear los separadores verticales
    private JSeparator createSeparator() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(10, 20));
        return s;
    }

    // Método privado para actualizar la información común
    private void updateInfo(double time, MapInfo map, List<AnimalInfo> animals) {
        this.timeLabel.setText(String.format("Time: %.2f", time));
        this.totalAnimalsLabel.setText("Total Animals: " + animals.size());
        this.dimensionLabel.setText(String.format("Dimension: %dx%d (%dx%d)",
                map.getCols(), map.getRows(), map.getWidth(), map.getHeight()));
    
        this.revalidate();
        this.repaint();
    }

    // --- Implementación de EcoSysObserver ---

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        updateInfo(time, map, animals);
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        updateInfo(time, map, animals);
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
        updateInfo(time, map, animals);
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
        // No requiere actualizar etiquetas generales
    }

    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        updateInfo(time, map, animals);
    }
}