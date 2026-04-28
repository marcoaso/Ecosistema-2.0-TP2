package simulator.view;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class InfoTable extends JPanel {

    private String title;
    private TableModel tableModel;

    InfoTable(String title, TableModel tableModel) {
        this.title = title;
        this.tableModel = tableModel;
        initGUI();
    }

    private void initGUI() {
        // 1. Cambiar el layout del panel a BorderLayout
        this.setLayout(new BorderLayout());

        // 2. Añadir un borde con título al JPanel
        // Usamos BorderFactory para crear un borde con el texto de 'this.title'
        this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(java.awt.Color.BLACK), // Borde de línea opcional
                this.title));

        // 3. Crear el JTable con el modelo recibido
        JTable table = new JTable(this.tableModel);

        // 4. Añadir el JTable dentro de un JScrollPane (barra de desplazamiento)
        // Es fundamental meter la tabla en un scroll pane para que se vean las
        // cabeceras
        JScrollPane scrollPane = new JScrollPane(table);

        // Lo añadimos al centro del BorderLayout
        this.add(scrollPane, BorderLayout.CENTER);
    }
}