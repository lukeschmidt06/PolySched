package edu.uiowa.cs.warp;

public class ReliabilityRow extends Row<Double> {

	private static final long serialVersionUID = 1L;

	// default constructor
	public ReliabilityRow() {
		super();
	}

	ReliabilityRow (Integer numColumns, Double element){
		super(numColumns, element);
	}

	ReliabilityRow (Double [] rowArray) {
		super(rowArray);
	}
}
