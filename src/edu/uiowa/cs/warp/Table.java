package edu.uiowa.cs.warp;

import java.util.ArrayList;

public class Table<T,E extends ArrayList<T>> extends ArrayList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Table() {
		super();
	}
	
	public Table(Integer row, Integer column) {
		super();
		
		for(int i=0; i < row; i++) {
			ArrayList<E> nullRow = new ArrayList<E>();
			for(int j=0; j < column; j++) {
				nullRow.add(null);
			}
			this.add((E) nullRow);
		}
	}

	public Integer getNumRows() {
		return this.size();
	}
	
	public Integer getNumColumns() {
		var numColumns = 0;
		var numRows = this.size();
		if (numRows > 0) {
			var lastRow = this.get(numRows-1);
			numColumns = lastRow.size();
		}
		return numColumns;
	}
	
	public void set(Integer row, Integer column, T element ) {
		E fullRow;
		if (row < this.size()) {
			fullRow = this.get(row);
			if (column < fullRow.size()) {
				fullRow.set(column, element);
			}
		}
	}

	public T get(Integer row, Integer column ) {
		T element = null;
		E fullRow;
		if (row < this.size()) {
			fullRow = this.get(row);
			if (column < fullRow.size()) {
				element = fullRow.get(column);
			}
		}
		return element;
	}
	
}
