package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.factories.Builder;
import simulator.factories.BuilderBasedFactory;
import simulator.factories.DefaultRegionBuilder;
import simulator.factories.DynamicSupplyRegionBuilder;
import simulator.factories.Factory;
import simulator.factories.SelectClosestBuilder;
import simulator.factories.SelectFirstBuilder;
import simulator.factories.SelectYoungestBuilder;
import simulator.factories.SheepBuilder;
import simulator.factories.WolfBuilder;
import simulator.misc.Utils;
import simulator.model.Animal;
import simulator.model.Region;
import simulator.model.SelectionStrategy;
import simulator.model.Simulator;
import simulator.view.MainWindow;

public class Main {

  // Enumerado con los modos de ejecución del programa: BATCH y GUI.
  private enum ExecMode {
    BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

    private String tag; // Nombre corto.
    private String desc; // Descripción.

    private ExecMode(String modeTag, String modeDesc) {
      tag = modeTag;
      desc = modeDesc;
    }

    public String getTag() {
      return tag;
    }

    public String getDesc() {
      return desc;
    }
  }

  // Valores por defecto.
  private final static Double DEFAULT_TIME = 10.0; // Segundos
  private static final Double DEFAULT_DT = 0.03;
  private static final ExecMode DEFAULT_MODE = ExecMode.GUI;

  // Variables globales de configuración.
  private static Double time = DEFAULT_TIME; // Tiempo de ejecución.
  public static Double deltaTime = DEFAULT_DT; // Tiempo por tick.
  private static String inFile = null; // Archivo JSON de entrada.
  private static String outFile = null; // Archivo de salida.
  private static boolean simpleViewer = false; // Mostrar visor.
  private static ExecMode mode = DEFAULT_MODE; // Modo por defecto GUI.

  // Factorias.
  public static Factory<SelectionStrategy> selectionStrategyFactory;
  public static Factory<Animal> animalsFactory;
  public static Factory<Region> regionsFactory;

  private static void parseArgs(String[] args) {

    // Define qué opciones son válidas.
    Options cmdLineOptions = buildOptions();
    // Crea el parser.
    CommandLineParser parser = new DefaultParser();

    try {
      CommandLine line = parser.parse(cmdLineOptions, args);
      
      parseHelpOption(line, cmdLineOptions);
      parseModeOption(line); // Importante: parsear el modo antes que el input file
      parseInFileOption(line);
      parseTimeOption(line);
      parseDeltaTimeOption(line);
      parseOutputFileOption(line);
      parseSimpleViewerOption(line);

      String[] remaining = line.getArgs();
      if (remaining.length > 0) {
        String error = "Illegal arguments:";
        for (String o : remaining)
          error += (" " + o);
        throw new ParseException(error);
      }

    } catch (ParseException e) {
      System.err.println(e.getLocalizedMessage());
      System.exit(1);
    }
  }

  private static Options buildOptions() {
    Options cmdLineOptions = new Options();

    // help
    cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

    // delta time
    cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg()
        .desc("A real number representing actual time, in seconds, per simulation step. Default value: " + DEFAULT_DT + ".")
        .build());

    // input file
    cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg()
        .desc("A configuration file (optional in GUI mode).").build());

    // mode
    cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg()
        .desc("Execution Mode. Possible values: 'batch' (Batch mode), 'gui' (Graphical User Interface mode). Default value: 'gui'.")
        .build());

    // output file
    cmdLineOptions.addOption(Option.builder("o").longOpt("output").hasArg()
        .desc("A file where output is written (only for BATCH mode).").build());

    // simple viewer
    cmdLineOptions.addOption(Option.builder("sv").longOpt("simple-viewer")
        .desc("Show the viewer window in BATCH mode.").build());

    // time
    cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
        .desc("An real number representing the total simulation time in seconds. Default value: " + DEFAULT_TIME + ". (only for BATCH mode).")
        .build());

    return cmdLineOptions;
  }

  private static void parseHelpOption(CommandLine line, Options cmdLineOptions) {
    if (line.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
      System.exit(0);
    }
  }

  private static void parseModeOption(CommandLine line) throws ParseException {
    String m = line.getOptionValue("m");
    if (m != null) {
      if (m.equalsIgnoreCase("batch")) {
        mode = ExecMode.BATCH;
      } else if (m.equalsIgnoreCase("gui")) {
        mode = ExecMode.GUI;
      } else {
        throw new ParseException("Invalid mode: " + m);
      }
    }
  }

  private static void parseInFileOption(CommandLine line) throws ParseException {
    inFile = line.getOptionValue("i");
    // El input es obligatorio SOLO en modo BATCH
    if (mode == ExecMode.BATCH && inFile == null) {
      throw new ParseException("In BATCH mode, the -i (input) option is mandatory.");
    }
  }

  private static void parseTimeOption(CommandLine line) throws ParseException {
    String t = line.getOptionValue("t", DEFAULT_TIME.toString());
    try {
      time = Double.parseDouble(t);
      assert (time >= 0);
    } catch (Exception e) {
      throw new ParseException("Invalid value for time: " + t);
    }
  }

  private static void parseDeltaTimeOption(CommandLine line) throws ParseException {
    String dt = line.getOptionValue("dt", DEFAULT_DT.toString());
    try {
      deltaTime = Double.parseDouble(dt);
      assert (deltaTime > 0);
    } catch (Exception e) {
      throw new ParseException("Invalid value for delta time: " + dt);
    }
  }

  private static void parseOutputFileOption(CommandLine line) {
    outFile = line.getOptionValue("o");
  }

  private static void parseSimpleViewerOption(CommandLine line) {
    simpleViewer = line.hasOption("sv");
  }

  private static void initFactories() {
    // Estrategias de selección
    List<Builder<SelectionStrategy>> ssBuilders = new ArrayList<>();
    ssBuilders.add(new SelectFirstBuilder());
    ssBuilders.add(new SelectClosestBuilder());
    ssBuilders.add(new SelectYoungestBuilder());
    selectionStrategyFactory = new BuilderBasedFactory<>(ssBuilders);

    // Animales
    List<Builder<Animal>> animalBuilders = new ArrayList<>();
    animalBuilders.add(new SheepBuilder(selectionStrategyFactory));
    animalBuilders.add(new WolfBuilder(selectionStrategyFactory));
    animalsFactory = new BuilderBasedFactory<>(animalBuilders);

    // Regiones
    List<Builder<Region>> regionBuilders = new ArrayList<>();
    regionBuilders.add(new DefaultRegionBuilder());
    regionBuilders.add(new DynamicSupplyRegionBuilder());
    regionsFactory = new BuilderBasedFactory<>(regionBuilders);
  }

  private static JSONObject loadJSONFile(InputStream in) {
    return new JSONObject(new JSONTokener(in));
  }

  private static void start_batch_mode() throws Exception {
    InputStream is = new FileInputStream(new File(inFile));
    JSONObject input = loadJSONFile(is);
    is.close();

    OutputStream os = null;
    if (outFile != null)
      os = new FileOutputStream(new File(outFile));

    int width = input.getInt("width");
    int height = input.getInt("height");
    int rows = input.getInt("rows");
    int cols = input.getInt("cols");

    Simulator simulator = new Simulator(cols, rows, width, height, animalsFactory, regionsFactory);
    Controller controller = new Controller(simulator);
    controller.loadData(input);

    controller.run(time, deltaTime, simpleViewer, os);

    if (os != null)
      os.close();
  }

  private static void start_GUI_mode() throws Exception {
    Simulator simulator = null;
    Controller controller = null;

    if (inFile != null) {
      // Caso 1: Se proporciona archivo de entrada
      InputStream is = new FileInputStream(new File(inFile));
      JSONObject input = loadJSONFile(is);
      is.close();

      int width = input.getInt("width");
      int height = input.getInt("height");
      int rows = input.getInt("rows");
      int cols = input.getInt("cols");

      simulator = new Simulator(cols, rows, width, height, animalsFactory, regionsFactory);
      controller = new Controller(simulator);
      
      // Cargar animales y regiones del JSON
      controller.loadData(input);
    } else {
      // Caso 2: No se proporciona archivo, usar valores por defecto (800, 600, 15, 20)
      // Nota: Simulator(cols, rows, width, height, ...)
      simulator = new Simulator(20, 15, 800, 600, animalsFactory, regionsFactory);
      controller = new Controller(simulator);
    }

    // Crear la ventana principal en el hilo de Swing
    Controller finalCtrl = controller;
    javax.swing.SwingUtilities.invokeAndWait(() -> new MainWindow(finalCtrl));
  }

  private static void start(String[] args) throws Exception {
    initFactories();
    parseArgs(args);
    switch (mode) {
      case BATCH:
        start_batch_mode();
        break;
      case GUI:
        start_GUI_mode();
        break;
    }
  }

  public static void main(String[] args) {
    Utils.RAND.setSeed(2147483647l);
    try {
      start(args);
    } catch (Exception e) {
      System.err.println("Something went wrong ...");
      System.err.println();
      e.printStackTrace();
    }
  }
}