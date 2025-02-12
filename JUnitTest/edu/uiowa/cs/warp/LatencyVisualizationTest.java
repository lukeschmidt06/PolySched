package edu.uiowa.cs.warp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import edu.uiowa.cs.warp.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import edu.uiowa.cs.warp.SystemAttributes.ScheduleChoices;

class LatencyVisualizationTest {

	private WorkLoad workLoad;
	private LatencyAnalysis latencyAnalysis;
	private Program program;
	private WarpInterface warp;
	private	LatencyVisualization latViz;
	
	@BeforeEach
	void setUp() {
		workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
		int channels = 3;
		ScheduleChoices scheduleChoices = ScheduleChoices.PRIORITY;
		Program program = new Program(workLoad,channels,scheduleChoices);
		warp = SystemFactory.create(workLoad, 2, ScheduleChoices.PRIORITY);
		latViz = new LatencyVisualization(warp);
	}

  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
  void testCreateHeader() {
    Description header = latViz.createHeader();
    Description actualHeader = latViz.createHeader();
    assertNotNull(header);
    assertNotNull(actualHeader);
    assertEquals("Latency Analysis for graph Example1A\n" +
    			 "467a0a390c8cd30422f5643bd94de631a6472769"+
                 "Scheduler Name: Priority\n" +
                 "numFaults: 1\n", header.toString());
  }

  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
  void testCreateColumnHeader() {
	String[] columnNames = latViz.createColumnHeader();

    assertNotNull(columnNames);
    assertEquals("Flow/Time Slot", columnNames[0]);
    for (int i = 1; i <= 10; i++) {
      assertEquals(String.valueOf(i - 1), columnNames[i]);
    }
  }

  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
  void testCreateVisualizationData() {
	String[][] visualizationData = latViz.createVisualizationData();
	assertNotNull(visualizationData);
    assertEquals(2, visualizationData.length);
    assertEquals(21, visualizationData[0].length);

    // Add assertions based on the expected visualization data
    assertEquals("F0", visualizationData[0][0]);
    assertEquals("F1", visualizationData[1][0]);
    // Add more assertions based on the expected visualization data
  	}
  
  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
  public void testCreateVisualizationDataNumTxAttemptsF0() {
	LatencyVisualization latVis = new LatencyVisualization(warp);
	WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
  	String[][] visData = latVis.createVisualizationData();
  	//Num of tx attempts found within latVis for flow F0. 
  	int numTxs = 0;
  	//Flow Index of F0
  	int flowIndex = 0;
  	
  	for (int i = 1; i < workLoad.getHyperPeriod(); i++) {
  		//if flow at timeslot is not idle or not a release, add to numTxs
  		
	  	if (!latVis.visualizationData[flowIndex][i].equals("-") && 
	  		!latVis.visualizationData[flowIndex][i].contains("R")){
  				numTxs++;	
  				}
  	}
	  	assertEquals(workLoad.getTotalTxAttemptsInFlow("F0"),numTxs);
  }
  
  //Checks to see if deadline from Visualization data created
  //matches with the  deadline from Flow F0
  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
  public void testCreateVisualizationDataDeadlineF0() {
  	LatencyVisualization latVis = new LatencyVisualization(warp);
  	WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
  	String[][] visData = latVis.createVisualizationData();
  	int flowIndex = workLoad.getFlowIndex("F0");
  	int expectedDeadline = workLoad.getFlowDeadline("F0");
  	int actualDeadline = 0;
  	for (int i = 1; i < workLoad.getHyperPeriod(); i++) {
  		if (latVis.visualizationData[flowIndex][i].contains("D")) {
  			actualDeadline = i - 1;
  		}
  	}
  	assertEquals(expectedDeadline,actualDeadline);
  }
  
  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
  void testCreateTitle() {
    String title = latViz.createTitle();

    assertNotNull(title);
    assertEquals("Latency Analysis for graph Example1A\n", title);
  }

  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
  void test_DisplayVisualization1() {
    GuiVisualization guiVisualization = latViz.displayVisualization();
    assertNotNull(guiVisualization);
    assert(guiVisualization instanceof GuiVisualization);
  }

  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
  void test_DisplayVisualization2() {
    GuiVisualization guiVisualization = latViz.displayVisualization();
    assertNotNull(guiVisualization);
    assert(guiVisualization instanceof GuiVisualization);
  }
  

  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
  void testFileVisualization() {
    Description fileContent = latViz.fileVisualization();

    assertNotNull(fileContent);
    // Add more assertions based on the expected behavior of Description
  }

	@Test
	@Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void testCreateColumnHeader2() {
        //Creates the columnHeader string array using the latencyVisualization createColumnHeader method
        String[] columnHeader = latViz.createColumnHeader();
        // Assert the expected column headers based on the hyperperiod
        assertArrayEquals(new String[]{"Flow/Time Slot", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        		"10", "11", "12", "13", "14", "15", "16", "17", "18", "19"}, columnHeader);

    }

    @Test
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void testCreateVisualizationData2() {
        String[][] visualizationData = latViz.createVisualizationData();

        // Assert the expected visualization data based on the provided example
        assert(new String[][]{
                {"F0", "Rx", "x", "xL", "-", "-", "DRx", "x", "xL", "-", "-"},
                {"F1", "R", "-", "-", "x", "x", "-", "-", "-", "xL", "-"}
        } == visualizationData);
    }
    
    @Test
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void testFileVisualization2() {
    	Description fileContent = latViz.createHeader();
    	fileContent.addAll(latViz.visualization());
        //Assert equals that the fileContent and latencyVisualiation fileVisualization are the same
    	assertEquals(fileContent,latViz.fileVisualization());
    }
    
}
