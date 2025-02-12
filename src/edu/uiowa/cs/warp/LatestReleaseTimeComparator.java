package edu.uiowa.cs.warp;

import java.util.Comparator;

public class LatestReleaseTimeComparator <T extends SchedulableObject> implements Comparator<T> {

	// Overriding compare() method of Comparator 
	// for descending order of release time
	@Override
	public int compare(T obj1, T obj2) {	
		var result = obj1.LatestReleaseTimeComparison(obj2);
		return result; // 0 => tie on release time
	}
}
