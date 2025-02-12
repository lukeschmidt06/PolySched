package edu.uiowa.cs.warp;

public class SortedPeriodQueue <T extends SchedulableObject> extends java.util.PriorityQueue<T> {

	private static final long serialVersionUID = 1L;

	/**
	 * Construct a priority queue of schedulable objects based 
	 * on release time.
	 */
	SortedPeriodQueue() {
		super(1, new PeriodComparator<T>());
	}
	
	/**
	 * Construct a priority queue of schedulable objects based
	 * on release time and initialized with a collection.
	 */
	SortedPeriodQueue(java.util.Collection<T> schedulableObjects) {
		super(new PeriodComparator<T>());
		this.addAll(schedulableObjects);
	}

}
