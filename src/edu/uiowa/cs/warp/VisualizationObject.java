/**
 * 
 */
package edu.uiowa.cs.warp;

/**
 * This class handles the code related to the VisualizationObject,
 * which is related to how the stress tests are visualized
 * @author sgoddard
 * @version 1.5
 *
 */
abstract class VisualizationObject {

  private FileManager fm;
  private String suffix;
  private String nameExtension;
  private static final String NOT_IMPLEMENTED = "This visualization has not been implemented.";
  protected String[][] visualizationData;

  /**
   * Constructor that takes in a file manager, workload and a suffix string
   * nothing is returned as it is a constructor
   * @param fm
   * @param workLoad
   * @param suffix
   */
  VisualizationObject(FileManager fm, WorkLoad workLoad, String suffix) {
    this.fm = fm;
    this.nameExtension = String.format("-%sM-%sE2E",
        String.valueOf(workLoad.getMinPacketReceptionRate()), String.valueOf(workLoad.getE2e()));
    this.suffix = suffix;
    visualizationData = null;
  }

  /**
   * Another constructor that takes in a file manager, and suffix string but this time also takes in SystemAtrribtues
   * @param fm
   * @param warp
   * @param suffix
   */
  VisualizationObject(FileManager fm, SystemAttributes warp, String suffix) {
    this.fm = fm;
    if (warp.getNumFaults() > 0) {
      this.nameExtension = nameExtension(warp.getSchedulerName(), warp.getNumFaults());
    } else {
      this.nameExtension =
          nameExtension(warp.getSchedulerName(), warp.getMinPacketReceptionRate(), warp.getE2e());
    }
    this.suffix = suffix;
    visualizationData = null;
  }

  /**
   * A constructor that takes in a file manager, system attributes, a name extension string and suffix string
   * @param fm
   * @param warp
   * @param nameExtension
   * @param suffix
   */
  VisualizationObject(FileManager fm, SystemAttributes warp, String nameExtension, String suffix) {
    this.fm = fm;
    if (warp.getNumFaults() > 0) {
      this.nameExtension =
          nameExtension(warp.getSchedulerName(), warp.getNumFaults()) + nameExtension;
    } else {
      this.nameExtension =
          nameExtension(warp.getSchedulerName(), warp.getMinPacketReceptionRate(), warp.getE2e())
              + nameExtension;
    }

    this.suffix = suffix;
    visualizationData = null;
  }

  /**
   * Another constructor that takes in a file manager, name extension string and a suffix string
   * @param fm - File Manager
   * @param nameExtension - Name Extension String
   * @param suffix - Suffix String
   */
  VisualizationObject(FileManager fm, String nameExtension, String suffix) {
    this.fm = fm;
    this.nameExtension = nameExtension;
    this.suffix = suffix;
    visualizationData = null;
  }
/**
 * Method that handles the nameExtension string
 * @param schName - String schName
 * @param m - Double m
 * @param e2e - Double e2e
 * @return - The extension string that's built by the method
 */
  private String nameExtension(String schName, Double m, double e2e) {
    String extension =
        String.format("%s-%sM-%sE2E", schName, String.valueOf(m), String.valueOf(e2e));
    return extension;
  }
  /**
   * Another nameExtension method to deal with cases where there isn't a specific double m and double e2e
   * @param schName - String schName
   * @param numFaults - Integer numFaults
   * @return - Extension String
   */
  private String nameExtension(String schName, Integer numFaults) {
    String extension = String.format("%s-%sFaults", schName, String.valueOf(numFaults));
    return extension;
  }

  /**
   * Public method that returns the value of the private fileManager
   * @return the fm
   */
  public FileManager getFileManager() {
    return fm;
  }

  /**
   * Visualization method that takes in no parameters
   * Handles the creation of visualization data
   * @return - the visualization content
   */
  public Description visualization() {
    Description content = new Description();
    var data = createVisualizationData();

    if (data != null) {
      String nodeString = String.join("\t", createColumnHeader()) + "\n";
      content.add(nodeString);

      for (int rowIndex = 0; rowIndex < data.length; rowIndex++) {
        var row = data[rowIndex];
        String rowString = String.join("\t", row) + "\n";
        content.add(rowString);
      }
    } else {
      content.add(NOT_IMPLEMENTED);
    }
    return content;
  }

  /**
   * Public string method that handles the creation of files from the fileNameTemplate string
   * @param fileNameTemplate - File Name Template String
   * @return - the created file
   */
  public String createFile(String fileNameTemplate) {
    return fm.createFile(fileNameTemplate, nameExtension, suffix);
  }

  /**
   * Method that handles file visualization
   * @return - the file's content
   */
  public Description fileVisualization() {
    Description fileContent = createHeader();
    fileContent.addAll(visualization());
    fileContent.addAll(createFooter());
    return fileContent;
  }

  /**
   * GUI Visualization method that may be implemented at a later date
   * @return - nothing at the moment
   */
  public GuiVisualization displayVisualization() {
    return null; // not implemented
  }

  /**
   * Method that creates a description header
   * @return - the description's header
   */
  protected Description createHeader() {
    Description header = new Description();
    return header;
  }
  /**
   * Method that creates a description footer
   * @return - the description's footer
   */
  protected Description createFooter() {
    Description footer = new Description();
    return footer;
  }
  /**
   * Method that will eventually return the column header - not implemented yet
   * 
   * @return - null at the moment
   */
  protected String[] createColumnHeader() {
    return new String[] {NOT_IMPLEMENTED};
  }

  /**
   * Method that will eventually return visualization data - not implemented yet
   * 
   * @return - null at the moment
   */
  protected String[][] createVisualizationData() {
    return visualizationData; // not implemented--returns null
  }
}
