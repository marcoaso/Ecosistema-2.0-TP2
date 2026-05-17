package simulator.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class MapWindow extends JFrame implements EcoSysObserver {

    // Controlador para registrar/quitar observadores.
    private Controller ctrl;
    // Componente que dibuja el mapa y animales.
    private AbstractMapViewer viewer;
    // Ventana padre, usada para centrar esta ventana.
    private Frame parent;

    MapWindow(Frame parent, Controller ctrl) {
        super("[MAP VIEWER]");
        this.ctrl = ctrl;
        this.parent = parent;
        intiGUI();
        // Se registra como observador para recibir eventos del simulador.
        this.ctrl.addObserver(this);
    }

    private void intiGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        // Panel principal de la ventana.
        setContentPane(mainPanel);

        // Crea el visor del mapa y lo coloca en el centro.
        this.viewer = new MapViewer();
        mainPanel.add(this.viewer, BorderLayout.CENTER);

        // Al cerrar, se desregistra para no seguir recibiendo eventos.
        addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                ctrl.removeObserver(MapWindow.this);
            }

            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {}
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });

        pack();
        if (this.parent != null) {
            setLocation(
                this.parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2,
                this.parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
        }
        setResizable(false);
        setVisible(true);
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        // Primer pintado cuando la vista se registra en el simulador.
        SwingUtilities.invokeLater(() -> {
            this.viewer.reset(time, map, animals);
            pack();
        });
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        // Reinicia la vista completa tras un reset del simulador.
        SwingUtilities.invokeLater(() -> {
            this.viewer.reset(time, map, animals);
            pack();
        });
    }

    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        // Actualización normal en cada paso de simulación.
        SwingUtilities.invokeLater(() -> {
            this.viewer.update(animals, time);
        });
    }

    // Eventos que esta ventana no necesita tratar.
    @Override public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {}
    @Override public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {}
}