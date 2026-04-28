package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import simulator.control.Controller;

public class MainWindow extends JFrame {

  private Controller ctrl;

  public MainWindow(Controller ctrl) {
    super("[ECOSYSTEM SIMULATOR]");
    this.ctrl = ctrl;
    initGUI();
  }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // TODO crear ControlPanel y añadirlo en PAGE_START de mainPanel
        ControlPanel controlPanel = new ControlPanel(ctrl);
        mainPanel.add(controlPanel, BorderLayout.PAGE_START);

        // TODO crear StatusBar y añadirlo en PAGE_END de mainPanel
        StatusBar statusBar = new StatusBar(ctrl);
        mainPanel.add(statusBar, BorderLayout.PAGE_END);

        // Definición del panel de tablas (usa un BoxLayout vertical)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // TODO crear la tabla de especies y añadirla a contentPanel.
        // Usa setPreferredSize(new Dimension(500, 250)) para fijar su tamaño
        SpeciesTableModel speciesModel = new SpeciesTableModel(ctrl);
        JTable speciesTable = new JTable(speciesModel);
        JScrollPane speciesScrollPane = new JScrollPane(speciesTable);
        speciesScrollPane.setPreferredSize(new Dimension(500, 250));
        speciesScrollPane.setBorder(BorderFactory.createTitledBorder("Species"));
        contentPanel.add(speciesScrollPane);

        // TODO crear la tabla de regiones.
        // Usa setPreferredSize(new Dimension(500, 250)) para fijar su tamaño
        RegionsTableModel regionsModel = new RegionsTableModel(ctrl);
        JTable regionsTable = new JTable(regionsModel);
        JScrollPane regionsScrollPane = new JScrollPane(regionsTable);
        regionsScrollPane.setPreferredSize(new Dimension(500, 250));
        regionsScrollPane.setBorder(BorderFactory.createTitledBorder("Regions"));
        contentPanel.add(regionsScrollPane);

        // TODO llama a ViewUtils.quit(MainWindow.this) en el método windowClosing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                ViewUtils.quit(MainWindow.this);
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // Opcional: centrar la ventana en pantalla
        setVisible(true);
    }
}