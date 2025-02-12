/**
 * This class represents the implementation of the Visualization interface and provides methods for creating and displaying visualizations for WARP program outputs.
 */
package edu.uiowa.cs.warp;

import java.io.File;

/**
 * <p>Visualization types include source code, reliabilities, simulator input, latency analysis, channel analysis, latency reports, and deadline reports.
 * For WorkLoad objects, visualization options include communication graphs, GraphViz, and input graphs.
 * 
 * @author sgoddard
 * @version 1.5
 */
public class VisualizationImplementation implements Visualization {

  private Description visualization;
  private Description fileContent;
  private GuiVisualization window;
  private String fileName;
  private String inputFileName;
  private String fileNameTemplate;
  private FileManager fm = null;
  private WarpInterface warp = null;
  private WorkLoad workLoad = null;
  private VisualizationObject visualizationObject;

  /**
   * Constructs a VisualizationImplementation object for a WarpInterface.
   * 
   * @param warp            Object for which visualization is generated.
   * @param outputDirectory Output directory where visualization files will be placed.
   * @param choice          Specifies the type of visualization.
   */

  public VisualizationImplementation(WarpInterface warp, String outputDirectory,
      SystemChoices choice) {
    this.fm = new FileManager();
    this.warp = warp;
    inputFileName = warp.toWorkload().getInputFileName();
    this.fileNameTemplate = createFileNameTemplate(outputDirectory);
    visualizationObject = null;
    createVisualization(choice);
  }
  
  /**
   * Constructs a VisualizationImplementation object for a WorkLoad.
   * 
   * @param workLoad        Object for which visualization is generated.
   * @param outputDirectory Output directory where visualization files will be placed.
   * @param choice          Choices specifying the type of visualization.
   */
  public VisualizationImplementation(WorkLoad workLoad, String outputDirectory,
      WorkLoadChoices choice) {
    this.fm = new FileManager();
    this.workLoad = workLoad;
    inputFileName = workLoad.getInputFileName();
    this.fileNameTemplate = createFileNameTemplate(outputDirectory);
    visualizationObject = null;
    createVisualization(choice);
  }

  /**
   * Displays content in a graphical window.
   */
  @Override
  public void toDisplay() {
    // System.out.println(displayContent.toString());
    window = visualizationObject.displayVisualization();
    if (window != null) {
      window.setVisible();
    }
  }

  /**
   * Writes content to a file.
   */
  @Override
  public void toFile() {
    fm.writeFile(fileName, fileContent.toString());
  }

  /**
   * Returns visualization content as a string.
   * 
   * @return Return content as a string.
   */
  @Override
  public String toString() {
    return visualization.toString();
  }

  /**
   * Creates and configures visualizations based on the specified choice.
   * 
   * @param choice Choices specifying the type of visualization to create.
   */
  private void createVisualization(SystemChoices choice) {
    switch (choice) { // select the requested visualization
      case SOURCE:
        createVisualization(new ProgramVisualization(warp));
        break;

      case RELIABILITIES:
        // TODO Implement Reliability Analysis Visualization
        createVisualization(new ReliabilityVisualization(warp));
        break;

      case SIMULATOR_INPUT:
        // TODO Implement Simulator Input Visualization
        createVisualization(new NotImplentedVisualization("SimInputNotImplemented"));
        break;

      case LATENCY:
        // TODO Implement Latency Analysis Visualization
        createVisualization(new LatencyVisualization(warp));
        break;

      case CHANNEL:
        // TODO Implement Channel Analysis Visualization
        createVisualization(new ChannelVisualization(warp));
        break;

      case LATENCY_REPORT:
        createVisualization(new ReportVisualization(fm, warp,
            new LatencyAnalysis(warp).latencyReport(), "Latency"));
        break;

      case DEADLINE_REPORT:
        createVisualization(
            new ReportVisualization(fm, warp, warp.toProgram().deadlineMisses(), "DeadlineMisses"));
        break;

      default:
        createVisualization(new NotImplentedVisualization("UnexpectedChoice"));
        break;
    }
  }

  /**
   * Creates and configures visualizations based on the specified choice.
   * 
   * @param choice Choices specifying the type of visualization to create.
   */
  private void createVisualization(WorkLoadChoices choice) {
    switch (choice) { // select the requested visualization
      case COMUNICATION_GRAPH:
        // createWarpVisualization();
        createVisualization(new CommunicationGraph(fm, workLoad));
        break;

      case GRAPHVIZ:
        createVisualization(new GraphViz(fm, workLoad.toString()));
        break;

      case INPUT_GRAPH:
        createVisualization(workLoad);
        break;

      default:
        createVisualization(new NotImplentedVisualization("UnexpectedChoice"));
        break;
    }
  }

  /**
   * Creates a visualization based on the VisualizationObject.
   * 
   * @param obj VisualizationObject to create and configure the visualization.
   * @param <T> Type of VisualizationObject.
   */
  private <T extends VisualizationObject> void createVisualization(T obj) {
    visualization = obj.visualization();
    fileContent = obj.fileVisualization();
    /* display is file content printed to console */
    fileName = obj.createFile(fileNameTemplate); // in output directory
    visualizationObject = obj;
  }

  /**
   * Creates a file name template based on the output directory.
   * 
   * @param outputDirectory Output directory where visualization files will be placed.
   * @return File name template.
   */
  private String createFileNameTemplate(String outputDirectory) {
    String fileNameTemplate;
    var workingDirectory = fm.getBaseDirectory();
    var newDirectory = fm.createDirectory(workingDirectory, outputDirectory);
    // Now create the fileNameTemplate using full output path and input filename
    if (inputFileName.contains("/")) {
      var index = inputFileName.lastIndexOf("/") + 1;
      fileNameTemplate = newDirectory + File.separator + inputFileName.substring(index);
    } else {
      fileNameTemplate = newDirectory + File.separator + inputFileName;
    }
    return fileNameTemplate;
  }

}
