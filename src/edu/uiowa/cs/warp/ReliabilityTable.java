package edu.uiowa.cs.warp;

public class ReliabilityTable extends Table<Double,ReliabilityRow> {

	private static final long serialVersionUID = 1L;

	// default constructor
	public ReliabilityTable() {
		super();
	}
	
	ReliabilityTable(Integer row, Integer column) {
		super();
		for(int i=0; i < row; i++) {
			ReliabilityRow emptyRow = new ReliabilityRow();
			for(int j=0; j < column; j++) {
				emptyRow.add(0.0);
			}
			this.add(emptyRow);
		}
	}
	
}

