package edu.uiowa.cs.warp;

/**
 * LatencyVisualization creates the visualizations for the liability analysis of the WARP program.
 * <p>
 * 
 * CS2820 Fall 2023 Project: Implement this class to create the file visualization that is
 * requested in Warp.
 * 
 * @author sgoddard
 * @version 1.6
 *
 */
public class LatencyVisualization extends VisualizationObject {

  // TODO Auto-generated class stub for unimplemented visualization

  private static final String SOURCE_SUFFIX = ".la";
  private static final String OBJECT_NAME = "Latency Analysis";
  private WarpInterface warp;
  private LatencyAnalysis la;
  private Program program;
  private WorkLoad workLoad;
  private ProgramSchedule latencyTable;
  private int numRows;
  private int numColumns;


  LatencyVisualization(WarpInterface warp) {
    super(new FileManager(), warp, SOURCE_SUFFIX);
    this.warp = warp;
    this.la = warp.toLatencyAnalysis();
    this.program = warp.toProgram();
    this.latencyTable = la.getLatencyTable();
    this.workLoad = program.toWorkLoad();
    this.numRows = latencyTable.getNumRows();
    this.numColumns = latencyTable.getNumColumns();
  }

  /**
  * This method overrides the createHeader from VisualizationObject. Instead of taking parameters, it
  * uses the .add() method from the Description object to add formatted strings containing the schedular
  * name and the number of faults, which can be gotten from the get methods for the Program object, 
  * and the inputGraphString.
  *
  * Return: A description object containing the full header of the latency visualization, complete with a
  * graph name, the name of the schedular used, and the maximum amount of faults the workload can
  * have.
  **/
  @Override
  protected Description createHeader() {
	  Description header = new Description();
	  // Use the createTitle() helper method to add a formatted title of the workLoad to the description
	  header.add(createTitle());
	  // Get the name of the scheduler that the program uses and add it to the description
	  header.add(String.format("Scheduler Name: %s\n", program.getSchedulerName()));
	  // Get the number of faults for the program and add it to the description
	  header.add(String.format("numFaults: %s\n", program.getNumFaults()));
	  // Return the header with all of the added information
	  return header;
  }

  /**
  *	This method creates a column header in the form of a list, which contains labels for each column of the latencyTable. The first element
  *	of the list will be "Flow/Time Slot," as it describes what each row and column contains. The other list elements will be numbers of timeslots. 
  *
  *Return: A list of strings corresponding to the names of the columns, where the first column is "Flow/Time Slot," and the rest
  * are numbers corresponding to timeslots.
  **/
  @Override
  protected String[] createColumnHeader() {
	  String[] columnNames = new String[numColumns + 1];
	  // Sets the first column header to "Flow/Time Slot" to describe row and column headers
	  columnNames[0] = "Flow/Time Slot";
	  for (int i=0;i<numColumns;i++) {
		  // Names each column after the 0 index column up until the number of columns in the latency table
		  columnNames[i+1] = String.format("%s", i);
	  }
	  // Returns the column headers for the visualization, which includes the header descriptions and the number of each timeslot
	  return columnNames;
  }

  /**
  * Creates a table in the form of a 2D array which contains the flows as rows and the timeslots as columns. These rows and
  * columns visualize deadlines, push/pull instructions, releases, and their corresponding timeslots for 
  * each row. This data comes from entries in the latencyTable created in the LatencyAnalysis class.
  *
  * Return: A 2D array containing rows and columns of the visualized data.
  **/
  @Override
  protected String[][] createVisualizationData() {
	  // The visualizationData variable comes from the VisualizationObject class
	  if (visualizationData == null) {
		  // Creates a new table with the rows and columns of the latencyTable.
		  visualizationData = new String[numRows][numColumns + 1];
		  
		  // Iterate through the rows of the latencyTable and sets the first column to the name of each flow
		  for (int row = 0; row<numRows;row++) {
			  var flows = workLoad.getFlowNamesInPriorityOrder();
			  var flowNames = flows.get(row);
			  visualizationData[row][0] = String.format("%s",flowNames);
			  // Then iterate through each column of the latencyTable and update the 2D array with the entry of the latencyTable
			  for (int column = 0; column<numColumns;column++) {
				  // If there is an entry in the table (any constant), it is gotten and set to the corresponding spot in the 2D array
				  // (excluding the column with the flow name headers)
				  var entry = latencyTable.get(row,column);
				  // If there is not an entry, the visualization defaults the data to "-", meaning the flow is idle at that time
				  if (entry == null) {
					  entry = "-";
				  }
				  visualizationData[row][column + 1] = entry;
			  }
		  }
	  }
	  // Return the 2D array corresponding to the latencyTable as visualizationData
	  return visualizationData;
  }


  /**
  * This method is so that the class can be compatible with the -gui option, which displays the data in a
  * separate interface as opposed to saving the visualization to a file. A GuiVisualization object is created
  * with a title, which is created with the .createTitle() method, an array of column names, which is
  * created with the .createColumnHeader() method, and a 2D array of rows and columns with data which 
  * is created with the createVisualizationData.
  *
  * Return: A GuiVisualization object of the latency visualization.
  **/
  @Override
  public GuiVisualization displayVisualization() {
	  // Creates the visualization with the title, column header, and data (which includes the flow name row headers)
	  return new GuiVisualization(createTitle(), createColumnHeader(), createVisualizationData());
  }

  /**
  * This method builds a visualization to be output for a file using the createHeader() and visualization()
  * methods.
  *
  * Return: The latency visualization as contents for a file.
  **/
  @Override
  public Description fileVisualization() {
	  // Creates a new description with the LatencyVisualization header
	  Description fileContent = createHeader();
	  // The visualization() method is inherited from the VisualizationObject class. It adds
	  // all the data from createVisualizationData() into the fileContent description.
	  fileContent.addAll(visualization());
	  return fileContent;
  }

  /**
  * This method creates a title using the name of the workload to be used as the graph name. It should be private since it is only called
  * within this file, but it is public so that we can create tests for this helper method.
  *
  * Return: A formatted string that includes the name of the workload as the graph name.
  **/
  public String createTitle() {
	  // Creates a title to be used for the header with the name of the workLoad.
	  return String.format("Latency Analysis for graph %s\n", workLoad.getName());
  }

}
