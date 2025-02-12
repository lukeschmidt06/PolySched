package edu.uiowa.cs.warp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

public class Activation extends SchedulableObject implements Comparable<Activation> { 

	private ArrayList<String> coordinators;
	private ArrayList<Edge> edges;
	private ArrayList<ScheduleTime> additionalScheduleTimes;

	private Set<String> conflicts; // nodes with conflicts
	

	Activation (String name){
		super();
		setName(name);
		this.edges = new ArrayList<Edge>();
		this.coordinators = new ArrayList<String>();
		this.conflicts = new HashSet<String>();
		this.additionalScheduleTimes = new ArrayList<ScheduleTime>();
	}
	
	Activation (Activation elem, Integer time){
		super(elem, time);
		this.edges = new ArrayList<Edge>();
		this.edges.addAll(elem.getEdges());
		this.coordinators = new ArrayList<String>();
		this.coordinators.addAll(elem.getCoordinators());
		// conflicts = new Set<String>();
		this.conflicts = new HashSet<String>();
		this.conflicts.addAll(elem.getConflicts());
		this.additionalScheduleTimes = new ArrayList<ScheduleTime>();
		this.additionalScheduleTimes.addAll(elem.getAdditionalScheduleTimes());
	}
	
	@Override 
	public Activation clone() {
		var time = getLastUpdateTime();
		var clone = new Activation(this, time);
		return clone;
	}
	
	/**
	 * @return the edgesByReleaseTime
	 */
	public PriorityQueue<Edge> getEdgesByReleaseTime() {
		return new 
		SchedulableObjectQueue<Edge>(new ReleaseTimeComparator<Edge>(),this.edges);
	}

	/**
	 * @return the edgesByReleaseTime
	 */
	public PriorityQueue<Edge> getEdgesByLatestReleaseTime() {
		return new 
		SchedulableObjectQueue<Edge>(new LatestReleaseTimeComparator<Edge>(),this.edges);
	}
	
	/**
	 * @return the edgesByPeriod
	 */
	public PriorityQueue<Edge> getEdgesByPeriod() {
		return new 
		SchedulableObjectQueue<Edge>(new PeriodComparator<Edge>(),this.edges);
	}
	
	/**
	 * @return the edgesByDeadline
	 */
	public PriorityQueue<Edge> getEdgesByDeadline() {
		return new 
		SchedulableObjectQueue<Edge>(new DeadlineComparator<Edge>(),this.edges);
	}
	
	/**
	 * @return the edgesByPriority
	 */
	public PriorityQueue<Edge> getEdgesByPriority() {
		return new 
			SchedulableObjectQueue<Edge>(new PriorityComparator<Edge>(),this.edges);
	}
	
	public void addEdges(ArrayList<Edge> edges) {
		this.edges.addAll(edges);
	}
	
	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}
	
	public ArrayList<Edge> getEdges() {
		return this.edges;
	}

	/**
	 * Increase the phase of each edge by the input parameter.
	 * 
	 * @param additionalOffset
	 */
	public void increaseEdgePhases(Integer additionalOffset) {
		if (additionalOffset > 0) { // make sure increase is needed
			for (Edge edge: this.edges) {
				var oldPhase = edge.getPhase();
				edge.setPhase(oldPhase+additionalOffset);
			}
		}
	}
		
	/**
	 * Updates priority and release time of 
	 * this activation.<br>
	 * 
	 * The priority of the element is equal to the
	 * edge priority with the earliest release time.
	 * Thus, it can change dynamically. <br>
	 *
	 * The release time of the element is equal to the
	 * edge priority with the earliest release time.
	 * Thus, it can change dynamically. <br>
	 */
	public void updatePriorityAndRelease(Integer currentTime) {
		var lastUpdateTime = getLastUpdateTime();
		if (currentTime >= lastUpdateTime) {
			for(Edge edge: edges) {
				edge.setNextReleaseTime(currentTime);
			}
			
			/* get the edges with earliest release time and 
			 * highest priority ==> get head of the queue just built
			 */
			var edgesByReleaseTime = getEdgesByReleaseTime();
			var firstEdge = edgesByReleaseTime.element();
			setPhase(firstEdge.getPhase()); 
			var edgesByPeriod = getEdgesByPeriod();
			firstEdge = edgesByPeriod.element();
			setPeriod(firstEdge.getPeriod()); // shortest period 
			var edgesByDeadline = getEdgesByDeadline();
			firstEdge = edgesByDeadline.element();
			setDeadline(firstEdge.getDeadline()); // earliest deadline
			var edgesByPrioriy = getEdgesByPriority();
			firstEdge = edgesByPrioriy.element();
			setPriority(firstEdge.getPriority());
			
			/* With the partition scheduling parameters set to match  
			 * the highest priority edge with respect to the parameter, 
			 * we can now set the next release time based on the
			 * current time.
			 * */
			setNextReleaseTime(currentTime);
			
		}
	}
	
	/**
	 * @return the conflicts
	 */
	public Set<String> getConflicts() {
		return conflicts;
	}

	/**
	 * @param conflicts the conflicts to set
	 */
	public void setConflicts(Set<String> conflicts) {
		this.conflicts = conflicts;
	}

	public void addConflict(String name) {
    	conflicts.add(name);
    }
	
	public void addConflicts(Set<String> conflicts) {
		this.conflicts.addAll(conflicts);
	}
	
	public void addCoordinator(String coordinator) {
		coordinators.add(coordinator);
	}
	
	public ArrayList<String> getCoordinators() {
		return coordinators;
	}

	public void addCoordinators(ArrayList<String> coordinators) {
		this.coordinators.addAll(coordinators);
	}
	
	/**
	 * @param additionalScheduleTimes
	 */
	public void addScheduleTimes(ArrayList<ScheduleTime> scheduleTime) {
		this.additionalScheduleTimes.addAll(scheduleTime);
	}
	
	/**
	 * @return additionalStartTimes
	 */
	public ArrayList<ScheduleTime> getAdditionalScheduleTimes() {
		return this.additionalScheduleTimes;
	}
	
	/**
	 * getNextScheduleTime returns the next entry in the additional
	 * schedule time array list whose start time is greater than the input
	 * parameter time. MAX_INT is returned if such a time
	 * is not found
	 * @param next
	 * @param time
	 * @return next schedule time or MAX_VALUEs if not found
	 */
	public ScheduleTime getNextScheduleTime(Integer time) {
		var result = new ScheduleTime(Integer.MAX_VALUE, Integer.MAX_VALUE);
		var moreScheduleTimes = getAdditionalScheduleTimes();
		Iterator<ScheduleTime> iter = moreScheduleTimes.iterator();
		while (iter.hasNext() ) {
			var next = iter.next();
			if (next.getStartTime() > time) {
				result = next;
				break;
			}
		}
		return result;
	}
	
	/**
	 * getMatchingEndTime returns the next end time in the additional
	 * schedule time array list that is greater than the input
	 * parameter time. MAX_INT is returned if such a time
	 * is not found
	 * @param next
	 * @param time
	 * @return next start time or MAX_VALUE if not found
	 */
	public Integer getMatchingEndTime(Integer time) {
		var result = Integer.MAX_VALUE;
		var moreScheduleTimes = getAdditionalScheduleTimes();
		Iterator<ScheduleTime> iter = moreScheduleTimes.iterator();
		// var nextEndTime = time;
		while (iter.hasNext()) { //  && nextEndTime <= time  ) {
			// nextEndTime = iter.next().getEndTime();
			var scheduleTime = iter.next();
			if (time == scheduleTime.getStartTime()) {
				result = scheduleTime.getEndTime();
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * getNextStartTime returns the next start time in the additional
	 * schedule time array list that is greater than the input
	 * parameter time. MAX_INT is returned if such a time
	 * is not found
	 * @param next
	 * @param time
	 * @return next start time or MAX_VALUE if not found
	 */
	public Integer getNextStartTime(Integer time) {
		var moreScheduleTimes = getAdditionalScheduleTimes();
		Iterator<ScheduleTime> iter = moreScheduleTimes.iterator();
		var nextStartTime = time;
		while (iter.hasNext() && nextStartTime <= time  ) {
			nextStartTime = iter.next().getStartTime();
		}
		if (nextStartTime == time) {
			/* no next StartTime found 
			 * could be an error 
			 */
			nextStartTime = Integer.MAX_VALUE;
			
		}
		return nextStartTime;
	}
	
	/**
	 * getNextEndTime returns the next end time in the additional
	 * schedule time array list that is greater than the input
	 * parameter time. MAX_INT is returned if such a time
	 * is not found
	 * @param next
	 * @param time
	 * @return next start time or MAX_VALUE if not found
	 */
	public Integer getNextEndTime(Integer time) {
		var moreScheduleTimes = getAdditionalScheduleTimes();
		Iterator<ScheduleTime> iter = moreScheduleTimes.iterator();
		var nextEndTime = time;
		while (iter.hasNext() && nextEndTime <= time  ) {
			nextEndTime = iter.next().getEndTime();
		}
		if (nextEndTime == time) {
			/* no next StartTime found 
			 * could be an error 
			 */
			nextEndTime = Integer.MAX_VALUE;
			
		}
		return nextEndTime;
	}
	
	public Integer getNumEdges() {
		return edges.size();
	}
	
	@Override
	public int compareTo(Activation obj) {
		return priorityComparison(obj);
	}
	
	public Boolean conflictExists(Activation obj) {
		Boolean result = false;
		/* create a copy of this.conflicts and obj.conflicts
		 * and then check the intersection of the two 
		 * sets. 
		 * Conflicts exists if |intersection| > 0
		 */
		var conflicts = new HashSet<String>();
		conflicts.addAll(getConflicts());
		var objConflicts = new HashSet<String>();
		objConflicts.addAll(obj.getConflicts());
		conflicts.retainAll(objConflicts); // get the intersection
		if (conflicts.size() > 0) {
			result = true; // |intersection| > 0 => conflict
		}
		return result;
	}
	
	public Boolean canCombine(Activation a2) {
		Boolean result = false;
		/* make sure a2 is not null */
		if (a2 != null) {
			var newEntryCoordinator = a2.getCoordinators().get(0);
			if (getCoordinators().contains(newEntryCoordinator)) {
				/* The (first) coordinator of the newEntry
				 * is in the list of coordinators of the prior entry.
				 * Thus, these two can be combined for efficiency.
				 */
				result = true;
			}
		}
		return result;
	}
	
	/*
	 * Returns the maximum number of transmissions
	 * needed on any link in activation to ensure e2e 
	 * reliability is met.
	 */
	public Integer getDelta() {
		Integer maxTxPerLink = 0;
		for (Edge edge: getEdges()) {
			maxTxPerLink = Math.max(maxTxPerLink, edge.getNumTx());
		}
		return maxTxPerLink;
	}

	public Integer getExecutionDuration() {
		/* Each edge requires delta time units. However, assuming 
		 * the WARP pipeline scheduling approach, the activation only
		 * executes for Delta + (numEdges - 1) time units, because
		 * a new edge will be added to the pipeline every time unit
		 * it executes.
		 */
		return getDelta() + (getNumEdges() - 1);
	}

	public Integer getCombinedExecutionDuration(Activation a2) {
		/* Each edge requires delta time units. However, assuming 
		 * the WARP pipeline scheduling approach, the activation only
		 * executes for Delta + (numEdges - 1) time units, because
		 * a new edge will be added to the pipeline every time unit
		 * it executes.
		 * So, if combined, we consider edges in both activations
		 */
		var result = 0;
		if (a2 != null) {
			result = Math.max(getDelta(), a2.getDelta()) + (getNumEdges() + a2.getNumEdges() - 1);
		}
		return result;
	}
	
	public void printAll(String headerMsg) {
		System.out.printf("\n%s",headerMsg);
		this.printAll();
	}
	
	public void printAll() {
		super.print();
		printEdges("Parition "+getName(),new ReleaseTimeQueue<Edge>(edges));
	}
	
	private void printEdges(String header, ReleaseTimeQueue<Edge> edges) {
		System.out.printf("\n%s\nEdges in edge list:", header);
		printEdges(edges);
	}
       
	private void printEdges(ReleaseTimeQueue<Edge> edges) {
    		var q = new ReleaseTimeQueue<Edge>();
    		q.addAll(edges);
    		Iterator<Edge> qValue = q.iterator();
            while (qValue.hasNext()) {
            	var next = q.poll();
            	next.print();
            }
	}
}
