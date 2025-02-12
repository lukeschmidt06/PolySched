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

class LatencyAnalysisTest {

	private WarpInterface mockWarp;
	private WorkLoad workLoad;
	private LatencyAnalysis latAnaly;
	private ProgramSchedule programTable;
	
	@BeforeEach
	void setUp() {
		workLoad = new WorkLoad(1,0.9,0.99, "ExampleX.txt");
		int channels = 3;
		ScheduleChoices scheduleChoices = ScheduleChoices.PRIORITY;
		Program program = new Program(workLoad,channels,scheduleChoices);
		latAnaly = new LatencyAnalysis(program);
		mockWarp = SystemFactory.create(workLoad, 2, ScheduleChoices.PRIORITY);
	}
	 
	@Test
	@Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
	public void test_checkRelease_1(){
		String flowName = (workLoad.getFlowNames())[1];
		int timeslot = 2;
		latAnaly.checkAndRecordRelease(flowName, timeslot);
		String latencyTableEntry = latAnaly.getLatencyTable().get(latAnaly.getFlowIndex(flowName), timeslot);
		assertTrue(latencyTableEntry == null || latencyTableEntry.equals("-"));
	}
	
	@Test
	@Timeout(value = 9000, unit = TimeUnit.MILLISECONDS)
	public void test_checkRelease_2(){
		String flowName = (workLoad.getFlowNames())[1];
		int timeslot = 0;

		latAnaly.checkAndRecordRelease(flowName, timeslot);
		String latency_TableEntry = latAnaly.getLatencyTable().get(latAnaly.getFlowIndex(flowName), timeslot);
		assert("R" == latency_TableEntry);
	}
	
	@Test
	@Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
	public void testCheckInstructions3(){

	}
	
	@Test
	@Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
	public void test_CheckAndRecordDeadline1() {

		String flowName = workLoad.getFlowNames()[1];
		int timeslot = 0;

		latAnaly.checkAndRecordDeadline(flowName, timeslot);

		// make sure the timeslot is still null at a given timeslot
		assertEquals("R",latAnaly.getLatencyTable().get(latAnaly.getFlowIndex(flowName), timeslot));
	}
	
	@Test
	@Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
	public void test_CheckAndRecordDeadline2() {

		String flowName = workLoad.getFlowNames()[1];
		int timeslot = 10;

		latAnaly.checkAndRecordDeadline(flowName, timeslot);

		// check if the correct symbol was updated in the Table given a correct deadline
		assert((latAnaly.getLatencyTable().get(latAnaly.getFlowIndex(flowName), timeslot)).indexOf("D") > -1);
	}
	
	@Test
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_updateEntry1() {
		
		String flowName = workLoad.getFlowNames()[1];
		int timeslot = 10;
		String EXECUTING = "R";
        // Perform the update
        latAnaly.updateEntry(flowName, timeslot, EXECUTING);
        latAnaly.buildLatencyTable();
		programTable = latAnaly.getLatencyTable();
        // Check if the entry in the latency table is updated correctly
        assert((programTable.get(10,timeslot).indexOf(EXECUTING) > -1);
    }
	
	@Test
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void test_updateEntry2() {
		String flowName = workLoad.getFlowNames()[1];
		int timeslot = 10;
		String RELEASE = "R";
        // Perform the update
        latAnaly.updateEntry(flowName, timeslot, RELEASE);
        // Check if the entry in the latency table is updated correctly
        assert((latAnaly.getLatencyTable().get(latAnaly.getFlowIndex(flowName), timeslot)).indexOf("R") > -1);
    }
	
	@Test
	@Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
	public void test_numMatchingTx() {
		// Arrange
		String flowName = "ExampleFlow";
		String src = "NodeA";
		String snk = "NodeB";
		String instr1 = "push(NodeA,NodeB)";
		String instr2 = "pull(NodeA,NodeB)";
		int numTx1 = latAnaly.numMatchingTx(flowName, src, snk, instr1);
		int numTx2 = latAnaly.numMatchingTx(flowName, src, snk, instr2);
		assertEquals(1, numTx2);
	}
	@Test
	@Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
	public void test_buildLatencyTable() {
		latAnaly.buildLatencyTable();
		ProgramSchedule prgmSch = latAnaly.getLatencyTable();
		assertEquals(prgmSch,programTable);
	}
	@Test
	@Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
	public void test_getFlowIndex() {
		String flowName = (workLoad.getFlowNames())[1];
		int timeslot = 2;
		int flowIndex = 0;
		assertEquals(latAnaly.getFlowIndex(flowName),flowIndex);
	}
	

}
