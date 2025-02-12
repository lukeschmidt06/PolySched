package edu.uiowa.cs.warp;

public class ScheduleTable extends Table<Activation,Row<Activation>> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ScheduleTable(){
		super();
	}
	
	/**
	 * Constructor creates a table row x column in size, full
	 * of null values.
	 * 
	 * @param row
	 * @param column
	 */
	ScheduleTable(Integer row, Integer column){
		super(row,column);
		
	}

}
