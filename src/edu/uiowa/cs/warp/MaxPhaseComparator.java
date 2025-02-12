package edu.uiowa.cs.warp;

import java.util.Comparator;

public class MaxPhaseComparator <T extends SchedulableObject> implements Comparator<T> {

	// Overriding compare() method of Comparator 
	// for ascending order of release time
	@Override
	public int compare(T obj1, T obj2) {	
		/* only sort by period, as we only want shortest period */
		return obj1.maxPhaseComparison(obj2); 
	}

}
