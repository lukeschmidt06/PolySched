package edu.uiowa.cs.warp;

/**
 * PreemptiveSchedule is an instance of the abstract class Schedule.
 * 
 * @author sgoddard2
 *
 */
public class NonPreemptiveSchedule extends Schedule {
	NonPreemptiveSchedule(Poset poset, Integer numChannels) {
		super(poset, numChannels);
	}
}
