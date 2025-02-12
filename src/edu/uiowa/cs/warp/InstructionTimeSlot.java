package edu.uiowa.cs.warp;

public class InstructionTimeSlot extends Row<String> {

	private static final long serialVersionUID = 1L;

	// default constructor
	public InstructionTimeSlot() {
		super();
	}

	InstructionTimeSlot (Integer numNodes, String element){
		super(numNodes, element);
	}

	InstructionTimeSlot (String [] rowArray) {
		super(rowArray);
	}
}
