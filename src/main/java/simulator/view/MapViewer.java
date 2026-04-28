package simulator.view;

import simulator.model.Animal;
import simulator.model.AnimalInfo;
import simulator.model.MapInfo;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {

    private int width;
    private int height;
    private int rows;
    private int cols;
    int rWidth;
    int rHeight;

    Animal.State currentState;

    volatile private Collection<AnimalInfo> objs;
    volatile private Double time;

    private static class SpeciesInfo {
        private Integer count;
        private Color color;

        SpeciesInfo(Color color) {
            count = 0;
            this.color = color;
        }
    }

    Map<String, SpeciesInfo> kindsInfo = new HashMap<>();
    private Font textFont = new Font("Arial", Font.BOLD, 12);
    private boolean showHelp;

    public MapViewer() {
        initGUI();
    }

    private void initGUI() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 'h':
                        showHelp = !showHelp;
                        repaint();
                        break;
                    case 's':
                        // Cambiar currState de forma circular
                        Animal.State[] states = Animal.State.values();
                        if (currentState == null) {
                            currentState = states[0];
                        } else {
                            int idx = currentState.ordinal();
                            if (idx + 1 < states.length) {
                                currentState = states[idx + 1];
                            } else {
                                currentState = null;
                            }
                        }
                        repaint();
                        break;
                    default:
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                requestFocus();
            }
        });

        currentState = null;
        showHelp = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D gr = (Graphics2D) g;
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setFont(textFont);

        gr.setBackground(Color.WHITE);
        gr.clearRect(0, 0, width, height);

        if (objs != null)
            drawObjects(gr, objs, time);

        // Mostrar ayuda
        if (showHelp) {
            gr.setColor(Color.RED);
            gr.drawString("h: toggle help", 5, 15);
            gr.drawString("s: show animals of a specific state", 5, 30);
        }
    }

    private boolean visible(AnimalInfo a) {
        return currentState == null || a.getState() == currentState;
    }

    private void drawObjects(Graphics2D g, Collection<AnimalInfo> animals, Double time) {

        // Dibujar grid de regiones
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= rows; i++) {
            g.drawLine(0, i * rHeight, width, i * rHeight);
        }
        for (int j = 0; j <= cols; j++) {
            g.drawLine(j * rWidth, 0, j * rWidth, height);
        }

        // Dibujar animales
        for (AnimalInfo a : animals) {
            if (!visible(a))
                continue;

            SpeciesInfo speciesInfo = kindsInfo.get(a.getGeneticCode());

            // Si no existe la especie, añadirla al mapa
            if (speciesInfo == null) {
                speciesInfo = new SpeciesInfo(ViewUtils.getColor(a.getGeneticCode()));
                kindsInfo.put(a.getGeneticCode(), speciesInfo);
            }

            // Incrementar contador
            speciesInfo.count++;

            // Dibujar animal
            int size = (int) (a.getAge() / 2 + 2);
            int x = (int) a.getPosition().getX() - size / 2;
            int y = (int) a.getPosition().getY() - size / 2;
            g.setColor(speciesInfo.color);
            g.fillOval(x, y, size, size);
        }

        // Dibujar etiqueta del estado visible
        if (currentState != null) {
            g.setColor(Color.BLACK);
            drawStringWithRect(g, 5, height - 30, "Showing: " + currentState.toString());
        }

        // Dibujar etiqueta del tiempo
        if (time != null) {
            g.setColor(Color.BLACK);
            drawStringWithRect(g, 5, height - 10, "Time: " + String.format("%.3f", time));
        }

        // Dibujar información de especies y resetear contadores
        int yOffset = 50;
        for (Entry<String, SpeciesInfo> e : kindsInfo.entrySet()) {
            g.setColor(e.getValue().color);
            drawStringWithRect(g, width - 150, yOffset,
                    e.getKey() + ": " + e.getValue().count);
            e.getValue().count = 0; // Resetear contador
            yOffset += 20;
        }
    }

    void drawStringWithRect(Graphics2D g, int x, int y, String s) {
        Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
        g.drawString(s, x, y);
        g.drawRect(x - 1, y - (int) rect.getHeight(), (int) rect.getWidth() + 1, (int) rect.getHeight() + 5);
    }

    @Override
    public void update(List<AnimalInfo> objs, Double time) {
        this.objs = objs;
        this.time = time;
        repaint();
    }

    @Override
    public void reset(double time, MapInfo map, List<AnimalInfo> animals) {
        this.width = map.getWidth();
        this.height = map.getHeight();
        this.rows = map.getRows();
        this.cols = map.getCols();
        this.rWidth = width / cols;
        this.rHeight = height / rows;
        this.kindsInfo.clear();

        setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));
        update(animals, time);
    }
}