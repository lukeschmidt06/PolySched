package edu.uiowa.cs.warp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

public class CodeFragment {
	
	
	private static final String SLEEP = "sleep";
	ArrayList<Edge> edges;
	String coordinator;
	ArrayList<String> codeFragment;
	Integer delta;
	Integer channel;
	
	/**
	 * Constructor generates a SLEEP instruction for the
	 * code fragment.
	 */
	CodeFragment() {
		this.delta = 0;
		this.channel = 0;
		this.edges = null;
		this.coordinator = null;
		this.codeFragment = new ArrayList<String>();
		this.codeFragment.add(SLEEP);
	}
	
	/**
	 * Constructor generates DSL instructions to transmit
	 * the data over edges using the coordinator and channel
	 * to determine the type of instruction and delta to 
	 * determine the number of times each instructions must
	 * be attempted.
	 * 
	 * @param delta
	 * @param edges
	 * @param coordinators
	 * @param channel
	 */
	CodeFragment(Integer delta, ArrayList<Edge> edges,
			ArrayList<String> coordinators, Integer channel) {
		this.delta = delta;
		this.channel = channel;
		this.edges = edges;
		this.coordinator = coordinators.get(0);
		this.codeFragment = new ArrayList<String>();
		if (multipleCoordinators(coordinators)) {
			reportIfDifferentCoordinators(coordinators);
		}	
		generateCodeFragment();
	}
	
	/**
	 * @return the coordinator
	 */
	public String getCoordinator() {
		return coordinator;
	}

	/**
	 * @return the codeFragment
	 */
	public ArrayList<String> instructions() {
		return codeFragment;
	}

	/**
	 * @return the size
	 */
	public Integer size() {
		return codeFragment.size();
	}
	
	
	private void generateCodeFragment () {
		var edgeQueue = new SchedulableObjectQueue<Edge>(new 
				PriorityComparator<Edge>(),this.edges);
		Iterator<Edge> q = edgeQueue.iterator();
		var size = edgeQueue.size();
		/* the code length will be equal to 
		 * delta + #edges -1
		 */
		var codeLength = delta + size -1;
		var basicInstructions = new ArrayList<String>();
		/* create basic instructions for each edge 
		 * and initial receiver set for each instruction
		 * */
		while(q.hasNext()) {
			var edge = edgeQueue.poll();
			var src = edge.getSrc();
			var snk = edge.getSnk();
			var flow = edge.getFlow();
			var instr = getBaseInstruction(flow,src,snk);
			basicInstructions.add(instr);
		}
		/* start building code fragments */
		
		codeFragment.add(basicInstructions.get(0));
		
		/* when size < delta, we need to repeat the
		 * middle stage delta - size times
		 */
		var numRepeatStages = Math.max(0, delta - size);
		if (size < delta) {
			numRepeatStages = delta - size;
		}
		
		/* build the initial code fragment */
		Integer localDelta = Math.min(size, delta);
		var instr = new String();
		var dropInstr = new String();
		var priorInstr = new String();
		for (int i = 1; i < localDelta ; i++) { 
			instr = codeFragment.get(i-1) +
					" else " + basicInstructions.get(i);
			codeFragment.add(instr);
		}
		/* now repeat middle stage if size < delta 
		 * otherwise, this for loop will be skipped
		 */
		for (int i = localDelta; (i < localDelta + numRepeatStages) && i < codeLength ; i++) { 
			instr = codeFragment.get(i-1);
			codeFragment.add(instr);
		}
		/* The first Instruction has been repeated
		 * delta times. Now it gets removed from
		 * the pipeline. E.g., with delta = 3 and
		 * size = 3
		 *  a <-- dropInstr
		 *  ab
		 *  abc <-- priorInstr
		 *  bc <-- instr
		 */

		/* Now loop through dropping old instructions
		 * and adding new ones for size - localDelta times.
		 * This loop will be skipped if size = localDelta.
		 */
		for (int i = localDelta + numRepeatStages; (i < numRepeatStages + size)  && i < codeLength ; i++) { 
			priorInstr = codeFragment.get(i-1);
			dropInstr =  basicInstructions.get(i-(localDelta + numRepeatStages)) + " else ";
			instr = priorInstr.replaceFirst(Pattern.quote(dropInstr),"");
			instr += " else " + basicInstructions.get(i-numRepeatStages);
			codeFragment.add(instr);
		}
		/* Now loop through dropping old instructions, draining
		 * the pipleline.
		 */
		for (int i = numRepeatStages + size; i < codeLength ; i++) { 
			priorInstr = codeFragment.get(i-1);
			dropInstr =  basicInstructions.get(i-delta)  + " else ";
			instr = priorInstr.replaceFirst(Pattern.quote(dropInstr),"");
			codeFragment.add(instr);
		}
	}
	
	private String getBaseInstruction(String flow, String src, String snk) {
		var instr = new String();
		if (coordinator.equals(src)) {
			/* push instruction */
			instr += ifHas(flow,src,snk) + push(flow,src,snk);
		} else {
			/* pull instruction */
			instr += ifNotHas(flow,src,snk) + pull(flow,src,snk);
		}
		return instr;
	}
	
	private String push(String flow, String src, String snk) {
		var instruction = String.format("push(%s: %s -> %s, #%d)", flow, src, snk, channel);
		return instruction;
	}
	
	private String pull(String flow, String src, String snk) {
		var instruction = String.format("pull(%s: %s -> %s, #%d)", flow, src, snk, channel);
		return instruction;
	}
	
	private String ifHas(String flow, String src, String snk) {
		var instruction = String.format("if has(%s: %s -> %s) ", flow, src, snk);
		return instruction;
	}
	
	private String ifNotHas(String flow, String src, String snk) {
		var instruction = String.format("if !has(%s: %s -> %s) ", flow, src, snk);
		return instruction;
	}
	
	public String wait(int channel) {
		var instruction = String.format("wait(#%d)", channel);
		return instruction;
	}
	
	public static String sleep() {
		var instruction = SLEEP;
		return instruction;
	}
	
	private void reportIfDifferentCoordinators(ArrayList<String> coordinators) {
		var numUniqueCoordinators = 1;
		var coordinatorNames = this.coordinator;
		if (coordinators.size() > 1) {
			for (int i = 1 ; i < coordinators.size(); i++) {
				if (!this.coordinator.equals(coordinators.get(i))) {
					coordinatorNames += ", " + coordinators.get(i);
					numUniqueCoordinators++;
				}
			}
			if (numUniqueCoordinators > 1) {
				System.err.printf("\n%d Coordinators: %s\n", 
						numUniqueCoordinators, this.coordinator, coordinatorNames);
			}
		}
	}
	
	private Boolean multipleCoordinators(ArrayList<String> coordinators) {
		var result = false;
		if (coordinators.size() > 1) {
			for (int i = 1 ; i < coordinators.size(); i++) {
				if (!this.coordinator.equals(coordinators.get(i))) {
					result = true;
				}
			}
		}
		return result;
	}
}
