package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.launcher.Main;

class ControlPanel extends JPanel {

  private Controller ctrl;
  private ChangeRegionsDialog changeRegionsDialog;

  private JToolBar toolsBar;
  private JFileChooser fc;
  private boolean stopped = true; // utilizado en los botones de run/stop

  private JButton openButton;
  private JButton viewerButton;
  private JButton regionsButton;
  private JButton runButton;
  private JButton stopButton;
  private JButton quitButton;

  private JSpinner stepsSpinner;
  private JTextField dtText;

  ControlPanel(Controller ctrl) {
    this.ctrl = ctrl;
    initGUI();
  }

  // Método para cargar iconos desde el recurso "icons"
  private ImageIcon loadIcon(String name) {
    java.net.URL url = getClass().getClassLoader().getResource("icons/" + name);
    if (url != null) {
      return new ImageIcon(url);
    } else {
      System.err.println("Icon not found: icons/" + name);
      return new ImageIcon();
    }
  }

  private void initGUI() {
    setLayout(new BorderLayout());
    toolsBar = new JToolBar();
    add(toolsBar, BorderLayout.PAGE_START);

    // Inicializar JFileChooser
    this.fc = new JFileChooser();
    this.fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/resources/examples"));

    // Inicializar el diálogo de regiones
    this.changeRegionsDialog = new ChangeRegionsDialog(ctrl);

    // Botón Open
    this.openButton = new JButton();
    this.openButton.setToolTipText("Open");
    this.openButton.setIcon(loadIcon("open.png"));
    this.openButton.addActionListener((e) -> {
      int ret = this.fc.showOpenDialog(ViewUtils.getWindow(this));
      if (ret == JFileChooser.APPROVE_OPTION) {
        try (InputStream is = new FileInputStream(this.fc.getSelectedFile())) {
          JSONObject jo = new JSONObject(new JSONTokener(is));
          this.ctrl.reset(jo.getInt("cols"), jo.getInt("rows"), jo.getInt("width"), jo.getInt("height"));
          this.ctrl.loadData(jo);
        } catch (Exception ex) {
          ViewUtils.showErrorMsg(this, "Error loading file: " + ex.getMessage());
        }
      }
    });
    this.toolsBar.add(this.openButton);

    this.toolsBar.addSeparator();

    // Botón Viewer
    this.viewerButton = new JButton();
    this.viewerButton.setToolTipText("Viewer");
    this.viewerButton.setIcon(loadIcon("viewer.png"));
    this.viewerButton.addActionListener((e) -> new MapWindow(null, this.ctrl)); //Revisar constructor?
    this.toolsBar.add(this.viewerButton);

    // Botón Regions
    this.regionsButton = new JButton();
    this.regionsButton.setToolTipText("Regions");
    this.regionsButton.setIcon(loadIcon("regions.png"));
    this.regionsButton.addActionListener((e) -> this.changeRegionsDialog.open(ViewUtils.getWindow(this)));
    this.toolsBar.add(this.regionsButton);

    this.toolsBar.addSeparator();

    // Botón Run
    this.runButton = new JButton();
    this.runButton.setToolTipText("Run");
    this.runButton.setIcon(loadIcon("run.png"));
    this.runButton.addActionListener((e) -> {
      this.stopped = false;
      setButtonsEnabled(false);
      try {
        int steps = (Integer) this.stepsSpinner.getValue(); // Obtener el número de pasos del spinner
        double dt = Double.parseDouble(this.dtText.getText());
        runSim(steps, dt);
      } catch (NumberFormatException nfe) {
        ViewUtils.showErrorMsg(this, "Invalid Delta-Time value.");
        this.stopped = true;
        setButtonsEnabled(true);
      }
    });
    this.toolsBar.add(this.runButton);

    // Botón Stop
    this.stopButton = new JButton();
    this.stopButton.setToolTipText("Stop");
    this.stopButton.setIcon(loadIcon("stop.png"));
    this.stopButton.addActionListener((e) -> this.stopped = true);
    this.toolsBar.add(this.stopButton);

    // Spinner de Pasos
    this.toolsBar.add(new JLabel(" Steps: "));
    this.stepsSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, 100000, 1));
    this.stepsSpinner.setToolTipText("Simulation steps");
    this.stepsSpinner.setMaximumSize(new Dimension(80, 40));
    this.toolsBar.add(this.stepsSpinner);

    // Texto de Delta-Time
    this.toolsBar.add(new JLabel(" Delta-Time: "));
    this.dtText = new JTextField(Main.deltaTime.toString());
    this.dtText.setToolTipText("Delta-time");
    this.dtText.setMaximumSize(new Dimension(80, 40));
    this.toolsBar.add(this.dtText);

    // Botón Quit (Alineado a la derecha)
    this.toolsBar.add(Box.createGlue());
    this.toolsBar.addSeparator();
    this.quitButton = new JButton();
    this.quitButton.setToolTipText("Quit");
    this.quitButton.setIcon(loadIcon("exit.png"));
    this.quitButton.addActionListener((e) -> ViewUtils.quit(this));
    this.toolsBar.add(this.quitButton);
  }

  // Método para habilitar/deshabilitar botones según el enunciado
  private void setButtonsEnabled(boolean enabled) {
    this.openButton.setEnabled(enabled);
    this.viewerButton.setEnabled(enabled);
    this.regionsButton.setEnabled(enabled);
    this.runButton.setEnabled(enabled);
    this.quitButton.setEnabled(enabled);
    this.stopButton.setEnabled(!enabled); // Stop es inverso al resto
  }

  // Método runSim según el esquema del enunciado
  private void runSim(int n, double dt) {
    if (n > 0 && !this.stopped) {
      try {
        this.ctrl.advance(dt);
        SwingUtilities.invokeLater(() -> runSim(n - 1, dt));
      } catch (Exception e) {
        ViewUtils.showErrorMsg(this, "Simulation error: " + e.getMessage());
        setButtonsEnabled(true);
        this.stopped = true;
      }
    } else {
      setButtonsEnabled(true);
      this.stopped = true;
    }
  }
}