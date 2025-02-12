package edu.uiowa.cs.warp;

public class ReleaseTimeQueue <T extends SchedulableObject> extends java.util.PriorityQueue<T> {

	private static final long serialVersionUID = 1L;

	/**
	 * Construct a priority queue of schedulable objects based 
	 * on release time.
	 */
	ReleaseTimeQueue() {
		super(1, new ReleaseTimeComparator<T>());
	}
	
	/**
	 * Construct a priority queue of schedulable objects based
	 * on release time and initialized with a collection.
	 */
	ReleaseTimeQueue(java.util.Collection<T> schedulableObjects) {
		super(new ReleaseTimeComparator<T>());
		this.addAll(schedulableObjects);
	}
}
