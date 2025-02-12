package edu.uiowa.cs.warp;

import java.util.Comparator;

public class ReleaseTimeComparator<T extends SchedulableObject> implements Comparator<T> {

	// Overriding compare() method of Comparator 
	// for ascending order of release time
	@Override
	public int compare(T obj1, T obj2) {	
		var result = obj1.releaseTimeComparison(obj2);
		if (result == 0) {
			// tied for priority, so break tie on release time
			result = obj1.priorityComparison(obj2);
		}
		return result; // 0 => tie on priority and release time
	}
}
