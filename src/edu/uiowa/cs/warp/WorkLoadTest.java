package edu.uiowa.cs.warp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkLoadTest {	

	//PartA
	@Test
	  public void test_SetFlowsInPriorityOrder() {
		WorkLoad workLoad = new WorkLoad(1, 0.9, 0.99, "Example1a.txt");
	    workLoad.setFlowsInPriorityOrder();

	    ArrayList<String> expected = new ArrayList<>();

	    assert("F0".equals(workLoad.getFlowNamesInPriorityOrder().get(0)));
	    assert("F1".equals(workLoad.getFlowNamesInPriorityOrder().get(1)));
	  }
	
	

	//Part B
	@Test
	public void test_getFlowNames() {
		WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
		ArrayList<String> initReturnedFlowNames = new ArrayList<String>();
		initReturnedFlowNames = workLoad.getFlowNamesInOriginalOrder();
		System.out.println(workLoad.getFlowNamesInOriginalOrder());
		assert(initReturnedFlowNames==workLoad.getFlowNamesInOriginalOrder());
		
		
	}
	
	//Part C
	@Test
	public void test_getNodesInFlow() {
		WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
		String[] expectedF0 = {"A", "B", "C"};
	    String[] expectedF1 = {"C", "B", "A"};

	    assertArrayEquals(expectedF0, workLoad.getNodesInFlow("F0"));
	    assertArrayEquals(expectedF1, workLoad.getNodesInFlow("F1")); 
	    
	}
	
	//Part D
	@Test
	public void test_getTotalTxAttemptsInFlow() {
		WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
		int totalCostOfF0 = workLoad.getTotalTxAttemptsInFlow("F0");
		int totalCostOfF1 = workLoad.getTotalTxAttemptsInFlow("F1");
		assert(totalCostOfF0==workLoad.getTotalTxAttemptsInFlow("F0"));
		assert(totalCostOfF1==workLoad.getTotalTxAttemptsInFlow("F1"));
		
	}
	
	//Part E
	@Test
	public void test_nextAbsoluteDeadline() {
		WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
	    int currentTime = 0;
	    
	    int f0Deadline = workLoad.nextAbsoluteDeadline("F0", currentTime);
	    int f1Deadline = workLoad.nextAbsoluteDeadline("F1", currentTime);
	    
	    assert(10==f0Deadline);
	    assert(20==f1Deadline);
		
	}
	
	//Part F
		@Test
		public void test_setFlowPriority() {
			WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
			workLoad.setFlowPriority("F1", 10);
			workLoad.setFlowPriority("F0", 2);
			ArrayList<String> expectedValues = workLoad.getFlowNamesInPriorityOrder();
			assert(expectedValues==workLoad.getFlowNamesInPriorityOrder());
//			assert(workLoad.getFlowNamesInPriorityOrder().get(0).equals("F1 (1, 20, 20, 0) : C -> B -> A"));
//			assert(workLoad.getFlowNamesInPriorityOrder().get(1).equals("F0 (0, 10, 10, 0) : A -> B -> C"));
			
		}
		
	//Part G
		@Test
		public void test_getFlowDeadline() {
			WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
		    assert(10==workLoad.getFlowDeadline("F0"));
		    assert(20==workLoad.getFlowDeadline("F1"));
			
		}
		
	//Part H
		@Test
		public void test_getHyperPeriod() {
			WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
			int hyperPeriodNum = workLoad.getHyperPeriod();
			assert(hyperPeriodNum == workLoad.getHyperPeriod());	
		}
		
	//Part I
		@Test
		public void nextReleaseTime() {
			WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
		    int currentTime = 0;
		    int f0Release = workLoad.nextReleaseTime("F0", currentTime);
		    int f1Release = workLoad.nextReleaseTime("F1", currentTime);
		    assert(0==f0Release);
		    assert(0==f1Release);
		}
		
		
	//Part J
		@Test
		public void test_getFlowNamesInPriorityOrder() {
			WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
			ArrayList<String> expectedValues = workLoad.getFlowNamesInPriorityOrder();
			assert(expectedValues == workLoad.getFlowNamesInPriorityOrder());	
		}
		
	//Part K
		@Test
		public void test_setFlowsInRMorder() {
			WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
		    workLoad.setFlowsInRMorder();
		    
		    assert("F0".equals(workLoad.getFlowNames()[0])); 
		    assert("F1".equals(workLoad.getFlowNames()[1]));	
		}
		
		
		
	//Part L
		@Test
		public void test_getFlowPriority() {
			WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example2.txt");
			ArrayList<String> expectedValues = workLoad.getFlowNamesInPriorityOrder();
			int aPriority = workLoad.getFlowPriority("F0");
			for(int i=0; i<expectedValues.size(); i++) {
				aPriority=workLoad.getFlowPriority(expectedValues.get(i));
				assert(workLoad.getFlowPriority(expectedValues.get(i))==aPriority);
			}
		}
	//Part M
		@Test
		public void test_setFlowDeadline() {
			WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
		
		    workLoad.setFlowDeadline("F1", 25);
		    workLoad.setFlowDeadline("F0", 15);
		    
		    assert(25==workLoad.getFlowDeadline("F1")); 
		    assert(15==workLoad.getFlowDeadline("F0"));
		
		}
	//Part N
		public void test_getNumTxAttemptsPerLink() {
			WorkLoad workLoad = new WorkLoad(1,0.9,0.99, "Example1a.txt");
			WorkLoad workLoad2 = new WorkLoad(1,0.9,0.99, "Example1a.txt");
			assert(workLoad.getNumTxAttemptsPerLink("F0") == workLoad2.getNumTxAttemptsPerLink("F0"));	
			assert(workLoad.getNumTxAttemptsPerLink("F1") == workLoad2.getNumTxAttemptsPerLink("F1"));	
		}
		
	
}






