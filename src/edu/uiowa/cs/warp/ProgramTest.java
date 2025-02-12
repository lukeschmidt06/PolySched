package edu.uiowa.cs.warp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.uiowa.cs.warp.SystemAttributes.ScheduleChoices;

class ProgramTest {
	WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
	int channels = 3;
	ScheduleChoices scheduleChoices = ScheduleChoices.PRIORITY;
	Program program = new Program(workLoad,channels,scheduleChoices);
	
	@Test
	public void test_getNodeMapIndex() {
		
		var nodeIndexMap = program.getNodeMapIndex();
		assert(nodeIndexMap.get("A")==0);
		assert(nodeIndexMap.get("B")==1);
		assert(nodeIndexMap.get("C")==2);
	}
}
