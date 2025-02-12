package edu.uiowa.cs.warp;

import java.util.ArrayList;
import java.util.HashMap;

import edu.uiowa.cs.warp.WarpDSL.InstructionParameters;

/**
 * LatencyAnalysis checks the latency (finishTime - releaseTime) for each
 * release of each flow for the WARP system. To be correct, the latency must be
 * less than or equal than the flows deadline parameter.
 * 
 * @author sgoddard
 * @version 1.6
 */
public class LatencyAnalysis {

	private static String DEADLINE_MISS = " => DEADLINE MISS";
	private static String FLOW_SEPARATOR = "******************************\n";
	private static String DEADLINE = "D";
	private static String COMPLETE = "L";
	private static String RELEASE = "R";
	private static String EXECUTING = "x";
	private Description latencyReport;
	private WorkLoad workload;
	private ProgramSchedule programTable;
	private ProgramSchedule latencyTable;
	private HashMap<String, Integer> nodeIndex;
	private WarpDSL dsl;
	private ArrayList<String> flows;

	/**
	 * Constructor for warp objects. Both constructors do the same thing.
	 * 
	 * @param warp Object providing an interface to the program to be analyzed.
	 */
	LatencyAnalysis(WarpInterface warp) {
		this(warp.toProgram());
  }
  
	/**
	 * Constructor that program objects. Both constructors do the same thing, but
	 * this constructor can be called by an object that doesn't have access to the
	 * parent Warp object.
	 * 
	 * @param program Warp program to be analyzed
	 */
	LatencyAnalysis(Program program) {
		this.workload = program.toWorkLoad();
		this.programTable = program.getSchedule();
		flows = workload.getFlowNamesInPriorityOrder();
		this.nodeIndex = program.getNodeMapIndex();
		/* get a Warp instruction parser object */
		dsl = new WarpDSL();
		buildLatencyTable();
		buildLatencyReport();
	}

	/**
	 * Creates a ProgramSchedule for the latency table using the flows as rows and
	 * the rows of program table as columns.
	 * 
	 * @return a ProgramSchedule corresponding to the latency table.
	 */
	public ProgramSchedule getLatencyTable() {
		return latencyTable;
	}
	
	/**
	 * This method builds the latency table whose entries are used in LatencyVisualization. It iterates through each flow and timeslot, checking and
	 * and then updating the entry with a deadline, release, instruction, or the completion of a released flow instance. It uses checkAndRecordDeadline(),
	 * checkAndRecordRelease(), checkAndRecordInstructions(), and checkAndRecordComplete() as helper methods, and they are called in that order to align
	 * with visualization formatting. An entry in the table can have multiple constants, or defaults to "-" if a flow is idle at a timeslot. It is called in
	 * constructor for the class.
	 * 
	 */
	public void buildLatencyTable() {
		// This creates a new ProgramSchedule that will have all the latency information to be used in LatencyVisualization
		latencyTable = new ProgramSchedule(flows.size(),programTable.getNumRows());
		// Iterate through each flow and then each timeslot in the flow
		for(String flowName: flows) {
			for(int row = 0; row<programTable.getNumRows(); row++) {
				// Check each entry in the table and update it if the flow is not idle
				checkAndRecordDeadline(flowName,row);
				checkAndRecordRelease(flowName,row);
				checkAndRecordInstructions(flowName,row);
				checkAndRecordComplete(flowName,row);
			}
		}
	}

	/**
	 * Get the latencyReport build by buildLatencyReport().
	 * 
	 * @return latencyReport
	 */
	public Description latencyReport() {
		
		return latencyReport;
	}

	/**
	 * Build a latency report. Flows are output in priority order (based on the
	 * priority used to build the program. The latency for each instance of the flow
	 * is reported as follows "Maximum latency for FlowName:Instance is Latency"
	 * 
	 * For flow instances that have latency > deadline, then the latency message is
	 * appended with the string " => DEADLINE MISS"
	 * 
	 * A line of 30 '*' characters separates each group of flow instance reports.
	 * 
	 * When there are not enough transmissions attempted between the release and the
	 * next release of an instance, then the latency is not computed (as we assume
	 * deadline less than or equal to the period. Thus, the report is: "UNKNOWN
	 * latency for FlowName:Instance; Not enough transmissions attempted".
	 * 
	 */
	public void buildLatencyReport() {

		latencyReport = new Description();
		for (String flowName : flows) {
			/* now remove the last flowSeparator line before returning */
			// var lastLineIndex = latencyReport.size() - 1;
			// latencyReport.remove(lastLineIndex);
			reportLatency(flowName);
		}
	}

	/**
	 * Computer and report the latency incurred for each release of Flow flowName.
	 * This method finds the flow's source and sink nodes. It also gets the number
	 * of transmission attempts required for last link in the flow. Then it loops
	 * through the hyperperiod calling the
	 * {@link #computeAndRecordFlowInstanceLatency(Integer, String, Integer, String, Integer, String, Integer, Integer)
	 * computeAndRecordFlowInstanceLatency} method to get the actual number of
	 * attempts made for each instance of the flow before its next release. If the
	 * number of attempts is less than that required, the deadline was missed and
	 * recorded by {@link #recordMissedDeadline(String, Integer, Integer)
	 * recordMissedDeadline}. A flow separation line is printed out after each
	 * instance's latency is reported.
	 * 
	 * @param flowName for which the latency of each instance will reported
	 */
	public void reportLatency(String flowName) {
		var time = 0;

		var nodes = workload.getNodesInFlow(flowName); // names of nodes in flow
		var flowSnkIndex = nodes.length - 1;
		/* get snk of last link in the flow, which is also the Flow snk node */
		String snk = nodes[flowSnkIndex];
		/* get the src of last link in the flow */
		String src = nodes[flowSnkIndex - 1];
		/* get (column) indexes into programTable of these nodes */
		var snkIndex = nodeIndex.get(snk);
		var srcIndex = nodeIndex.get(src);
		/*
		 * get the array containing the number of transmissions required for each link
		 * in the flow
		 */
		var numTxAttemptsPerLink = workload.getNumTxAttemptsPerLink(flowName);
		/* get the number of transmission required for the last link in the flow */
		var numTxRequired = numTxAttemptsPerLink[numTxAttemptsPerLink.length - 1];
		var numTxProcessed = 0; // num of Tx seen in the program schedule so far
		var instance = 0;
		var endTime = workload.getHyperPeriod();
		while (time < endTime) {
			numTxProcessed = computeAndRecordFlowInstanceLatency(time, flowName, instance, src, srcIndex, snk, snkIndex,
					numTxRequired);
			var deadline = workload.nextAbsoluteDeadline(flowName, time);
			if (numTxProcessed < numTxRequired) {
				/*
				 * This flow missed its deadline with required number of Tx!! This message
				 * should not be printed with the schedulers built
				 */
				recordMissedDeadline(flowName, instance, deadline);
			}
			instance++;
			time = workload.nextReleaseTime(flowName, time + 1);
		}
		recordFlowSeparator();
	}

	/**
	 * Computes the latency for the specified instance of the flow and calls
	 * {@link #recordLatency(String, Integer, Integer, Integer, Integer)
	 * recordLatency} to record the latency if all numTxRequired transmission
	 * attempts complete before the next release. The number of transmission
	 * attempts made before the next release is returned.
	 * 
	 * @param time          the current time slot
	 * @param flowName      flow to be analyzed
	 * @param instance      instance of the flow to be analyzed
	 * @param src           source node of the instruction being analyzed
	 * @param srcIndex      column index of the program table for the source node
	 * @param snk           sink node of the instruction being analyzed
	 * @param snkIndex      column index of the program table for the sink node
	 * @param numTxRequired number of transmission attempts required
	 * @return
	 */
	public Integer computeAndRecordFlowInstanceLatency(Integer time, String flowName, Integer instance, String src,
			Integer srcIndex, String snk, Integer snkIndex, Integer numTxRequired) {

		/* get next release time and absolute deadline of the flow */
		var releaseTime = workload.nextReleaseTime(flowName, time);
		var deadline = workload.nextAbsoluteDeadline(flowName, releaseTime);
		var nextReleaseTime = workload.nextReleaseTime(flowName, deadline);
		time = releaseTime;
		var numTxProcessed = 0; // num of Tx seen in the program schedule so far

		while (time < nextReleaseTime) {
			/* get instruction strings at these to locations */
			String instr1 = programTable.get(time, srcIndex);
			String instr2 = programTable.get(time, snkIndex);
			numTxProcessed += numMatchingTx(flowName, src, snk, instr1);
			numTxProcessed += numMatchingTx(flowName, src, snk, instr2);
			if (numTxProcessed == numTxRequired) {
				/*
				 * all required Tx attempts have been made compute and record latency
				 */
				recordLatency(flowName, time, releaseTime, deadline, instance);
				time = nextReleaseTime;
			} else {
				time++;
			}

		}
		return numTxProcessed;
	}

	/**
	 * Evaluates the latency based on input parameters and records the maximum
	 * latency encountered or a deadline miss, as appropriate, in the latencyReport.
	 * 
	 * @param flowName    flow for which the latency is being recorded
	 * @param time        of the last push/pull instruction for this instance of the
	 *                    flow
	 * @param releaseTime time when this instance of the flow was released
	 * @param deadline    time which this instance of the flow must be completed
	 * @param instance    the number of times (-1) the flow has been released (1st
	 *                    release is instance 0)
	 */
	public void recordLatency(String flowName, Integer time, Integer releaseTime, Integer deadline, Integer instance) {
		var latency = time - releaseTime + 1;
		// report latency
		String latencyMsg = String.format("Maximum latency for %s:%d is %d", flowName, instance, latency);
		if (latency > deadline) {
			/* deadline missed, so color the text red */
			latencyMsg += DEADLINE_MISS;
		}
		latencyMsg += "\n";
		latencyReport.add(latencyMsg);
	}

	/**
	 * Adds an UNKNOWN latency report to the latencyReport when not enough
	 * transmission attempts were made before the next release of the flow.
	 * 
	 * @param flowName flow for which the deadline miss is being recorded
	 * @param instance instance number for the flow that missed its deadline
	 * @param deadline time which this instance of the flow was to have been
	 *                 completed
	 */
	public void recordMissedDeadline(String flowName, Integer instance, Integer deadline) {
		String latencyMsg = String.format(
				"UNKNOWN latency for %s:%d with deadline %d; Not enough transmissions attempted\n", flowName, instance,
				deadline);
		latencyReport.add(latencyMsg);
	}

	/**
	 * Adds the {@link #FLOW_SEPARATOR FLOW_SEPARATOR} line to the latencyReport.
	 */
	public void recordFlowSeparator() {
		String flowSeparator = FLOW_SEPARATOR;
		latencyReport.add(flowSeparator);
	}

	/**
	 * Counts the number of push/pull instructions in the instruction string (input
	 * parameter) whose flow, src, and snk parameters match the input parameters.
	 * 
	 * @param flow  name of the flow to match
	 * @param src   source node of the instruction to match
	 * @param snk   sink node of the instruction to match
	 * @param instr string containing the instructions to be searched for matches
	 * @return numTx the number of matching push/pull instructions
	 */
	public Integer numMatchingTx(String flow, String src, String snk, String instr) {
		var numTx = 0;

		if (flow == null || src == null || snk == null || instr == null) {
			/* make sure all parameters are valid */
			return numTx;
		}
		/*
		 * get a Warp instruction parser object and then get the instruction parameters
		 * from the instruction string.
		 */
		var instructionParametersArray = dsl.getInstructionParameters(instr);

		for (InstructionParameters entry : instructionParametersArray) {
			String flowName = entry.getFlow();
			if (flowName.equals(flow)) {
				/*
				 * This instruction is for the flow we want. (flow name is set for push/pull
				 * instructions, which are all we want. If not push/pull, then we skip this
				 * instruction.) If flow, src, and snk names in instruction match input
				 * parameters, then we have a Tx attempt.
				 */
				if (src.equals(entry.getSrc()) && snk.equals(entry.getSnk())) {
					/* flow, src, and snk match, so increment Tx attempts */
					numTx++;
				}
			}
		}
		return numTx;
	}
	
	/**
	 * This method gets the index of a flow by iterating through the ArrayList of flows and comparing the given flowName with each one until
	 * it gets the correct index. This is used as a helper method for updateEntry() so it can be used in a .get() statement for the latency table.
	 * It would normally be private, since it only needs to be called in this class, but is public for testing purposes.
	 * 
	 * @param flowName The string name of a flow that is analyzed for its index.
	 * @return The index of a flow that has the input flow name.
	 */
	public int getFlowIndex(String flowName) {
		var index = 0;
		// Iterates through the ArrayList of flows
		for (int flow = 0; flow < flows.size(); flow++) {
			// Compares input flow name with each flow and updates the index to the
			// index of the flow if they match
			if (flowName.equals(flows.get(flow))) {
				index  = flow;
				break;
			}
		}
		return index;
	}

	/**
	 * This method checks for each characteristic of a flow, and updates a latency
	 * table entry corresponding to the given flow and timeslot with that
	 * characteristic. It can be called multiple times for each entry, and it adds constants
	 * to the existing entry if there is more than one characteristic in an entry.
	 * 
	 * @param flowName The string name of a flow, used to access an entry in the
	 *                 latency table.
	 * @param timeslot An integer which corresponds to a timeslot in the schedule,
	 *                 used to access an entry in the latency table.
	 * @param value    A constant string that corresponds to the character
	 *                 representing a characteristic of the flow.
	 */
	public void updateEntry(String flowName, int timeslot, String value) {
		// Uses the getFlowIndex() helper method to get the index of a flow with only its name
		var flow = getFlowIndex(flowName);
		// Gets an entry in the table based on the indices of its flow and timeslot
		var entry = latencyTable.get(flow, timeslot);
		if (entry == null) {
			entry = value;
		}
		else {
			// This adds to the previous entry instead of setting the entry to the constant,
			// since there may be multiple constants in an entry
			entry += value;
		}
		// Sets the table entry to the new value that was created; either just the constant given,
		// or a combination of existing values and the new value
		latencyTable.set(flow,timeslot, entry);
	}

	/**
	 * This method checks if an instance of the flow is released and then updates an
	 * entry corresponding to the given flow and timeslot in the latency table with
	 * the RELEASE constant.
	 * 
	 * @param flowName The string name of a flow, used to update the latency table.
	 * @param timeslot An integer which corresponds to a timeslot in the schedule,
	 *                 used to update the latency table.
	 */
	public void checkAndRecordRelease(String flowName, int timeslot) {
		// Gets the next release time for the flow
		int releaseTime = workload.nextReleaseTime(flowName, timeslot);

		// Checks if the next release time is the same as the current time, and if so,
		// there is a release at the current timeslot, which means the table is updated
		// to have the RELEASE constant at the current timeslot.
		if (timeslot == releaseTime) {
			updateEntry(flowName, timeslot, RELEASE);
		}
	}

	/**
	 * This method checks if the last instruction of the flow instance is released
	 * and then updates an entry corresponding to the given flow and timeslot in the
	 * latency table with the COMPLETE constant.
	 * 
	 * @param flowName The string name of a flow, used to update the latency table.
	 * 
	 * @param timeslot An integer which corresponds to a timeslot in the schedule,
	 *                 used to update the latency table.
	 */
	public void checkAndRecordComplete(String flowName, int timeslot) {
		var numTxAttemptsPerLink = workload.getNumTxAttemptsPerLink(flowName);
  		var numTxRequired = numTxAttemptsPerLink[numTxAttemptsPerLink.length - 1];

		String start = latencyTable.get(flows.indexOf(flowName), timeslot);
		if(start != null && start.contains("x")) {
			Integer executions = excPerFlowPeriod(flowName,timeslot);
			if (executions >= numTxRequired) {
				updateEntry(flowName ,timeslot, COMPLETE);
			}
		}
	}
	
	public Integer excPerFlowPeriod(String flowName, Integer timeslot) {
		int nextDeadline = workload.nextAbsoluteDeadline(flowName, timeslot);
		Integer executions = 0;
		for(int time = 0; time < nextDeadline-timeslot; time ++) {
			for(int num = 0; num < programTable.getNumColumns(); num ++) {
				String instr = programTable.get(timeslot,num);
				if(flowExecuting(flowName,instr)) {
					executions += 1;
				}
				if(instr.contains("R")) {
					return executions;
				}
			}
		}
		return 0;
	}
	
		
		
	/**
	 * This method checks if there are push/pull instructions, and then updates an
	 * entry in the latency table with the push/pull instruction symbol.
	 * 
	 * @param flowName The string name of a flow, used to update the latency table.
	 * @param timeslot An integer which corresponds to a timeslot in the schedule,
	 *                 used to update the latency table.
	 */
	public void checkAndRecordInstructions(String flowName, int timeslot) {
		// Iterates through the entries in programTable
		for (int col = 0; col < programTable.getNumColumns(); col++) {
			// Gets the string name of an instruction and calls the flowExecuting() helper method on it,
			// which uses WarpDSL to parse the instruction and get the flow name, before seeing if it is executing
			String instr = programTable.get(timeslot, col);
			if (flowExecuting(flowName, instr)) {
				// If there is an instruction at the input timeslot for the flowName,
				// updates the latencyTable with the EXECUTING constant
				updateEntry(flowName, timeslot, EXECUTING);
				break;
			}
		}
	}

	/**
	 * This method checks if there is an absolute deadline for the released flow
	 * instance then updates an entry corresponding to the given flow and the timeslot of the next deadline
	 * in the latency table with the DEADLINE constant.
	 * 
	 * @param flowName The string name of a flow, used to update the latency table.
	 * @param timeslot An integer which corresponds to a timeslot in the schedule,
	 *                 used to update the latency table.
  	*/ 
  	public void checkAndRecordDeadline(String flowName, int timeslot){
  	  	// Gets the next release time for the flow and the next deadline for the flow
  		int releaseTime = workload.nextReleaseTime(flowName, timeslot);
  		int nextDeadline = workload.nextAbsoluteDeadline(flowName, timeslot);

  		// Checks if there is a release at the current timeslot, and if so,
  		// updates table entry with the index of the next deadline to have the DEADLINE constant
  		if (timeslot == releaseTime) {
  			updateEntry(flowName, nextDeadline, DEADLINE);
  		}
  	}
  	
  	/**
  	 * This method is a helper method to be used by checkAndRecordInstructions(), and checkAndRecordComplete(). It
  	 * uses WarpDSL to retrieve the flow name from an instruction string that is gotten from the programTable.
  	 * It then checks if the flow being analyzed matches the flow name of the instruction string, and turns to true,
  	 * meaning there is an instruction being executed.
  	 * It would normally be private, since it only needs to be called in this class, but is public for testing purposes.
  	 * 
  	 * @param flowName The string name of a flow.
  	 * @param instr A string corresponding to an entry to programTable that describes an instruction at a timeslot.
  	 * @return
  	 */
  	public boolean flowExecuting(String flowName, String instr) {
  		boolean executing = false;
  		
  		// Makes sure the inputs are valid
  		if (flowName == null || instr == null) {
  			return executing;
  		}
  		// Uses WarpDSL to convert an instruction to a format that can be easily parsed for the flow name.
  		var instructionParametersArray = dsl.getInstructionParameters(instr);
  			
  		for (InstructionParameters entry : instructionParametersArray) {
  			// The InstructionParameters.getFlow() method easily parses an instruction and gets the name of the entry's flow.
  			var flow = entry.getFlow();
  			// This method then checks if the given flowName matches the name of the flow in the instruction.
  			if (flow.equals(flowName)) {
  				executing = true;
  			}
  		}
  		//if the names match, this boolean will return true. If not, this returns false.
  		return executing;
  	}
}
