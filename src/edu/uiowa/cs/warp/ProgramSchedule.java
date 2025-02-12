package edu.uiowa.cs.warp;

public class ProgramSchedule extends Table<String,InstructionTimeSlot> {

	private static final long serialVersionUID = 1L;

	// default constructor
	public ProgramSchedule() {
		super();
	}
	
	ProgramSchedule(Integer row, Integer column) {
		super(row,column);
	}
	
}

