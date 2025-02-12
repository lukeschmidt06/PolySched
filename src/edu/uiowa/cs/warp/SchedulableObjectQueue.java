package edu.uiowa.cs.warp;

import java.util.Comparator;

public class SchedulableObjectQueue <T extends SchedulableObject> extends java.util.PriorityQueue<T> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a priority queue of schedulable objects based 
	 * on the input Comparator.
	 */
	SchedulableObjectQueue(Comparator<T> comparitor) {
		super(1, comparitor);
	}
	
	/**
	 * Construct a priority queue of schedulable objects based
	 * on release time and initialized with a collection.
	 */
	SchedulableObjectQueue(Comparator<T> comparitor, java.util.Collection<T> schedulableObjects) {
		super(comparitor);
		this.addAll(schedulableObjects);
	}
}
