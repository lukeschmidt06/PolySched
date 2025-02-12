package edu.uiowa.cs.warp;

import java.util.ArrayList;

public class Row<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;
	public Row() {
		super();
	}

	Row (Integer numElements, T element) {
		super();
		for (int i=0; i < numElements; i++) {  
			this.add(element); // create the the row initialized with element
		}
	}
	Row (T [] rowArray) {
		super();
		for (int i=0; i < rowArray.length; i++) {  
			this.add(rowArray[i]); // create the the row initialized with rowArray
		}
	}
	Row (Integer numElements) {
		super(numElements);
	}
}
