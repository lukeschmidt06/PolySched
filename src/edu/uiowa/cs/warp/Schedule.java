package edu.uiowa.cs.warp;

import java.util.HashMap;
import java.util.ArrayList;

import edu.uiowa.cs.warp.WarpDSL.InstructionParameters;

/**
 * Schedule builds a non-preemptive, priority schedule 
 * for the Partially Ordered Set (Poset) using numChannels. 
 * It ensures, precedence constraints are preserved, and 
 * reports deadline misses.
 * 
 * @author sgoddard2
 *
 */
public abstract class Schedule {

	private static final String UNKNOWN = "unknown";
	
	protected Boolean schedulable;
	protected Integer numChannels;
	private Integer fakeTime = 0; // for fake scheduling to test
	protected Integer endScheduleTime;
	/* schedule is a 2D table of partition elements.
	 * The row will be time, ranging from 0 -> endScheduleTime-1
	 * The column will be channels + 1, so we can track
	 * unschedulable elements, should the partition set not
	 * be schedulable
	 */
	protected ScheduleTable schedule;
	protected Poset poset; // partially ordered set (poset)

	Schedule(Poset poset, Integer numChannels) {
		this.poset = poset;
		this.numChannels = numChannels;
		var maxPhase = poset.getMaxPhase();
		endScheduleTime = poset.getHyperPeriod();
		if (maxPhase > 0) {
			/* need to schedule longer to cover start-up period */
			endScheduleTime = maxPhase + 2*endScheduleTime ;
		}
		schedule = new ScheduleTable(numChannels+1,endScheduleTime);
		schedulable = true;
		buildSchedule();
	}

	protected class ScheduleLocation {
		Integer channel;
		Integer time;
		
		ScheduleLocation() {
			this.channel = 0;
			this.time = 0;
		}
		ScheduleLocation(Integer channel, Integer time) {
			this.channel = channel;
			this.time = time;
		}
		/**
		 * @return the channel
		 */
		protected Integer getChannel() {
			return channel;
		}
		/**
		 * @return the time
		 */
		protected Integer getTime() {
			return time;
		}
		
	}
	
	/**
	 * @return the schedule
	 */
	public ScheduleTable getSchedule() {
		return schedule;
	}

	private void buildSchedule() {

		var next = poset.getNextActivation();
		while (next != null) {
			/* Add next to the schedule if max of its release time 
			 * and end time is less than the endScheduleTime.
			 * Otherwise, its releaseTime is >= endScheduleTime
			 * or endTime >= currentTime, which means can't
			 * schedule at this time.
			 */
			if (!addToSchedule(next)) {
				schedulable = false;
				reportNotSchedulable(next);
				/* Record unschedulable element */
				fakeSchedulingToTestRest(next);
				next.setNextReleaseTime(next.getEndTime());
				if (next.getReleaseTime() < endScheduleTime) {
					poset.addActivation(next);
				}
			} else {
				/* update the next release time and add it back into
				 * the priority queue
				 */
				next.setNextReleaseTime(next.getEndTime());
				if (next.getReleaseTime() < endScheduleTime) {
					poset.addActivation(next);
				}

			}
			next = poset.getNextActivation();
		}
		print();
	}

	
	public ProgramSchedule toProgram() {
		var nodeNames = poset.getNodesNamesOrderedAlphabetically();
		var numNodes = nodeNames.length;
		var program = new ProgramSchedule(endScheduleTime,numNodes);
		CodeFragment codeFragment = null;
		var duration = 0; // default duration for SLEEP;
		String name = null;
		var startTime = 0;
		var endTime = 0;
		HashMap<String, Integer> nodeIndexMap = new HashMap<>();
		
		if (!schedulable) {
			/* not Schedulable, so we will not create code */
			return program; // return empty program
		}
		
		/* build the nodeIndexMap by putting nodes in alphabetic
		 * order and then hashing its name to its rank
		 */
		for (int i = 0; i < numNodes; i++) {
			nodeIndexMap.put(nodeNames[i], i);
		}
		
		for (int channel = 0; channel < numChannels; channel++) {
			for (int time = 0; time < endScheduleTime; time++) {
				var entry = schedule.get(channel,time);
				if (entry == null) {
					/* no entry here, so instruction will be SLEEP instruction */
					codeFragment = new CodeFragment();
					startTime = time;
					endTime = time;
					name = "Empty";
				} else {
					/* create the codeFragment for this activation entry */
					codeFragment = new CodeFragment(entry.getDelta(),entry.getEdges(),
							entry.getCoordinators(),channel);
					startTime = entry.getStartTime();
					endTime = entry.getEndTime();
					name = entry.getName();
					duration =  endTime - startTime + 1;
					if (duration > codeFragment.size()) {
						/* for combination fragments, we can sometimes generate
						 * more efficient code than originally thought. So,
						 * update schedule by replacing the entry with null
						 * and update the activation with the the new end time.
						 */
						for (int k = 0; k < (duration - codeFragment.size()); k++) {
							schedule.set(channel,endTime-k,null);
						}
						endTime = codeFragment.size() + startTime - 1;
						entry.setEndTime(endTime);
						
					}
				}
				duration =  endTime - startTime + 1;
				/* duration can be < fragment size  when two activations 
				 * were combined but they had different delta values. 
				 * The larger was used, but we should make sure duration isn't
				 * > size, else we may overwrite something. Just report for now.
				 */
				if (duration > codeFragment.size()) {
					System.err.printf("activation %s: duration = %d, codeSize = %d", 
							name,duration, codeFragment.size());
				} else if (time != startTime ){
					System.err.printf("activation %s: time = %d, startTime = %d", 
							name, time, startTime);

				} else {
					var instructions = codeFragment.instructions();
					var coordinator = codeFragment.getCoordinator();

					if (coordinator != null) {
						/* coordinator is null the entry is sleep,
						 * which happens when the schedule entry was null.
						 * We skip this entry and then replace nulls in the
						 * program with sleep instructions at the end
						 */
						for (int i = 0; i < duration; i++) {
							var instr = instructions.get(i);
							/* enter the instruciton in the program */
							var row = time+i;
							var column = nodeIndexMap.get(coordinator);		
							program.set(row,column,instr);

							/* get the listeners from the instruction 
							 * and insert the wait() instructions into
							 * the program for each listener
							 * */
							var dsl = new WarpDSL();
							var instructionParametersArray = dsl.getInstructionParameters(instr); 
							for (InstructionParameters fragment: instructionParametersArray) {
								String listener = fragment.getListener();
								if(!listener.equals(UNKNOWN)) {
									/* need to add a wait instruction for node listener
									 * wait(#channel)
									 * at time,listener location of program table
									 */
									program.set(time+i,nodeIndexMap.get(listener),codeFragment.wait(channel));
								}
							}
						}
					}
					time += duration-1;
				}

			}
		}
		replaceNullWithSleep(program);
		return program;
	}
	
	/**
	 * Replace all null entries in program with sleep instructions
	 * 
	 * @param program
	 */
	private void replaceNullWithSleep(ProgramSchedule program) {
		var rows = program.getNumRows();
		var columns = program.getNumColumns();

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				var entry = program.get(i,j);
				if (entry == null) {
					program.set(i, j, CodeFragment.sleep());
				}
			}
		}
	}
	
	private void fakeSchedulingToTestRest(Activation next) {
		var pred = next.getPredecessor();
		var startTime = fakeTime;
		var channel = numChannels; // use last channel
		if (pred != null) {
			/* remove pred end time */
			pred.pollEndTimes(); 
		} 
		next.setStartTime(next.getReleaseTime());
		var duration = next.getExecutionDuration();
		fakeTime = startTime + duration - 1;
		if (fakeTime >= endScheduleTime) {
			System.err.print("NOT SCHEDULABLE: CAN'T STORE ANY MORE FAKE ENTRIES");
			print();
		}
		next.setEndTime(next.getReleaseTime()+next.getDeadline()-next.getPhase());
		/* Add the endTime to the elements endTimes Q
		 * so that we can track predecessor endTimes
		 * and make sure successors don't start before 
		 * predecessors
		 */
		next.addToEndTimes(endScheduleTime); 
		/* Create a new entry, based on next, to be entered
		 * into the scheduling tale
		 */
		var entry = new Activation(next, startTime);
		for (int i = startTime; i <= fakeTime; i++) {
			/* Add this object to the schedule for the
			 * from startTime to endTime.
			 */
			schedule.set(channel, i, entry);
		}
		fakeTime++;
	}

	/**
	 * getChannel checks for conflicts and if none, returns a free
	 * channel. <br>
	 * If return value == numChannels, then no channel can be assigned
	 * due to either conflicts or no availability.
	 * @param schedulableObject
	 * 
	 * @return available channel or numChannels when none available
	 */
	protected Integer getChannelAndSetStartTime(Activation schedulableObject) {
		var time = schedulableObject.getReleaseTime();
		var channel = numChannels; // default is no channel available
		var endTime = endScheduleTime; // duration of the schedule
		schedulableObject.setStartTime(endTime);
		schedulableObject.setEndTime(endTime);
		/* Create a flag indicating it might be possible to combine
		 * to partitions. This flag will be used to create a channel
		 * affinity, in case scheduler wants to combine to partitions.
		 */
		var pred = schedulableObject.getPredecessor();
		if (pred != null) {
			/* peek at the head of the endTimes for pred */
			var predEndTime = pred.peekEndTimes();
			if (predEndTime == null) {
				return numChannels; // return indicates no channel/time
			} else if (predEndTime >= time) {
				/* can't schedule here; need to look later */
				time = predEndTime+1;
			} 
		}
		var scheduleLocation = findChannelAndTime(time,schedulableObject );
		channel = scheduleLocation.getChannel();
		time = scheduleLocation.getTime();

		if (channel != numChannels && time < endScheduleTime) {
			/* Found a channel with no conflicts.
			 * Set the object's start time to the that
			 * time.
			 */
			schedulableObject.setStartTime(time);

		}
		return channel;
	}

	protected ScheduleLocation findChannelAndTime(Integer time, Activation schedulableObject ) {
		var channel = numChannels; // default is no channel available 
		var duration = schedulableObject.getExecutionDuration();
		
		while (time < endScheduleTime && conflictsExist(schedulableObject, time, duration )) {
			time ++; //+= duration+1;
		}
		if (time >= endScheduleTime) {
			/* no conflict time available */
			return new ScheduleLocation(channel, time);	
		} 
		
		/* can schedule at current value of time, so find the best channel.
		 * Bias toward and empty channel at near lowest end of spectrum */
		for (int i = numChannels-1; i >= 0 ; i--) {
			/* Start at numChannels and work down to 0
			 * checking for conflicts and finding an
			 * empty slot at the same time. This will
			 * result in the lowest possible channel
			 * being used if their are no conflicts
			 * with any channel entries.
			 * 
			 * This version combines entries as soon as.
			 * possible, without looking for other, low-channel
			 * combinations.
			 */
			// var entry = schedule.get(i,time);
			if (time > 0 && schedulableObject.getReleaseTime() < time) {
				var priorEntry = schedule.get(i, time-1);
				if (priorEntry != null) {
					var combinedDuration = priorEntry.getCombinedExecutionDuration(schedulableObject);
					if (priorEntry.canCombine(schedulableObject) && 
							noOtherChannelConflicts(i,schedulableObject, time-1) &&
							isAvailable(schedulableObject,i,priorEntry.getStartTime(),combinedDuration)) {
						channel = i;
						time = priorEntry.getStartTime();
						break;
					}
				}
			}
			if (isAvailable(schedulableObject, i, time, duration)) {
				channel = i;
			}

		}
		/* no time and channel found in interval [time, time+duration]
		 * so, advance time and check again
		 */
		if (channel == numChannels) {
			return continueSearch(time,schedulableObject);
		} else {
			return new ScheduleLocation(channel, time);	
		}
	}
	
	/** continueSearch continues the search for a channel time.<br>
	 * The default implementation continues at time+1 by calling
	 * findChannelAndTime(time+1,schedulableObject). This method
	 * can be overridden to provide preemptive searches or any
	 * other method of continuing the search.
	 * 
	 * @param time
	 * @param activation
	 * @return ScheduleLocation
	 */
	protected ScheduleLocation continueSearch(Integer time, Activation activation) {
		return findChannelAndTime(time+1,activation);
	}
	
	protected Boolean noOtherChannelConflicts(Integer exceptedChannel, Activation schedulableObject, Integer time) {
		var result = true;

		if (time >= endScheduleTime) {
			/* not enough time left in the schedule,
			 * so return false 
			 */
			return false;
		}
		for (Integer channel = 0; channel < numChannels ; channel++) {
			var entry = schedule.get(channel,time);

			if (entry != null && schedulableObject != null && channel != exceptedChannel) {
				if (entry.conflictExists(schedulableObject)) {
					result = false;
					return result;
				}
			} 
		}
		return result;
	}
	
	protected Boolean conflictsExist(Activation obj, Integer time, Integer duration) {
		var result = false;

		if (time+duration >= endScheduleTime) {
			/* not enough time left in the schedule,
			 * so return false 
			 */
			return false;
		}
		if (duration <= 0) {
			result  = true;
		} else {
			for (Integer i = time; i < time+duration; i++ ) {
				for (Integer channel = 0; channel < numChannels ; channel++) {
					var entry = schedule.get(channel,i);
					if (entry != null && obj != null) {
						if (entry.conflictExists(obj)) {
							result = true;
							return result;
						}
					} 
				}
			}	
		}
		return result;
	}
	
	protected Boolean freeOfCoordinatorConflicts(Activation obj, Integer requestedChannel, Integer time, Integer duration) {
		var result = true;

		if (time+(duration-1) >= endScheduleTime) {
			/* not enough time left in the schedule,
			 * so return false 
			 */
			return false;
		}
		if (duration <= 0) {
			result  = false;
		} else {
			for (Integer i = time; i < time+duration; i++ ) {
				for (Integer channel = 0; channel < numChannels ; channel++) {
					var entry = schedule.get(channel,time);
					if (entry != null && obj != null) {
						if (entry.canCombine(obj) && channel != requestedChannel) {
							result = false; // same coordinator is scheduled
							return result;
						}
					} 
				}
			}	
		}
		return result;
	}

	/**
	 * isAvailable returns true if the schedule for the channel is available in the 
	 * interval [start, start+duration-1]
	 */
	protected Boolean isAvailable(Activation a1, Integer channel, Integer start, Integer duration) {
		var available = true;
		
		if (conflictsExist(a1,start,duration)) {
			return false;
		}
		for (Integer j = start; j < start+duration; j++ ) {
			var currentEntry = schedule.get(channel,j);
			if (currentEntry != null) {
				if (currentEntry.canCombine(a1)) {
					/* no conflict with this entry, and can
					 * combine so check for combined entry time
					 *  to see if we can combine activations
					 */
					var combinedDuration = currentEntry.getCombinedExecutionDuration(a1);
					var combinedEntry = new CombinedActivation(currentEntry,a1);
					var searchTime = currentEntry.getEndTime()+1;
					var searchLength = combinedDuration - (duration-(j-start));
					return isAvailable(combinedEntry, channel, searchTime, searchLength);
				} else {
					available = false;
					break;
				}
			}
		}
		if (available) {
			available = freeOfCoordinatorConflicts(a1, channel, start, duration);
		}
		return available;
	}
	
	/**
	 * isAvailable returns true if the schedule is available in the 
	 * interval [start, start+duration-1] in any channel
	 */
	protected Boolean isAvailable(Activation a1, Integer start, Integer duration) {
		var available = false;
		
		for (Integer channel = 0; channel < numChannels; channel++) {
			if (isAvailable(a1,channel,start,duration)) {
			available = true;
			break;
			}
		}
		return available;
	}
	/**
	 * timeRemaining returns duration minus the number of vacant slots found. 
	 * 
	 * @return duration-vacantSlots
	 */
	protected Integer timeRemaining(Integer channel, Integer start, Integer duration) {
		var result = duration;

		var row = schedule.get(channel);
		for (Integer i = start; i < start+duration; i++, result-- ) {
			if (row.get(i) != null) {
				break;
			}
		}
		return result;
	}
	
	
	private Boolean canCombineEntriesInPlace(Activation a1, Activation a2) {
		var result = false;
		if (a1 == null || a2 == null) {
			return false;
		}
		var minStartTime = Math.min(a1.getStartTime(), a2.getStartTime()); 
		var maxEndTime = Math.max(a1.getEndTime(), a2.getEndTime()); 
		
		if (a2.canCombine(a1)) {
			 var combinedDuration = a2.getCombinedExecutionDuration(a1);
			 var newEndTime = minStartTime + combinedDuration - 1;
			 if (newEndTime <= maxEndTime) {
				result = true;
			 }
		}
		return result;
	}
	
		
	private Boolean combinedEntriesInPlace(Activation a1, Activation a2, Integer channel) {
		var result = false;
		if (canCombineEntriesInPlace(a1,a2)) {
			var entry = new CombinedActivation(a1, a2);
			var startTime = entry.getStartTime();
			var endTime = entry.getEndTime();
			for (int i = startTime; i <= endTime; i++) {
				/* Add this object to the schedule for the
				 * from startTime to endTime.
				 */
				schedule.set(channel, i, entry);
			}
			result = true;
		}
		return result;
	}
	
	protected Boolean addToSchedule(Activation next) {
		var channel = getChannelAndSetStartTime(next);
		if (channel == numChannels) {
			return false;
		}
		/* found a time and channel  that are valid */
		Activation currentEntry = null;	
		var combineTime = endScheduleTime;
		var startTime = next.getStartTime(); // set by getChannel()
		var duration = next.getExecutionDuration();	
		var endTime = startTime + duration - 1;
		/* Check the schedule to see if we need to 
		 * combine next with a higher priority entry 
		 * in the schedule during its execution
		 */
		for (int i = startTime; i < startTime + duration; i++) {
			currentEntry = schedule.get(channel,i);
			if (currentEntry != null && next.canCombine(currentEntry)) {
				/* found an entry to combine, record the time,
				 * update the combined duration and new
				 * endTime, then exit the loop
				 */
				/* need to check if combined time is available or
				 * if we have to combine yet another entry 
				 */
				 var endCurrentEntry = currentEntry.getEndTime();
				 var combinedDuration = next.getCombinedExecutionDuration(currentEntry);
				 var newEndTime = startTime + combinedDuration - 1;
				 /* need to check to make sure there are no conflicts on other 
				  * channels for this new interval
				  */
				 var available = isAvailable(next, channel, startTime, combinedDuration);
				 if (available) {	 
					 var additionalTimeNeeded = newEndTime - endCurrentEntry;
					 if (additionalTimeNeeded > 0) {
						 /* see if we can combine the two existing adjacent entries
						  * in place. If so, then try to recursively schedule next
						  */
						 var additionalCombine = schedule.get(channel,endCurrentEntry+1);
						 if (additionalCombine != null) {
							 if (!combinedEntriesInPlace(currentEntry,additionalCombine,channel)) {
								 return false;
							 }
							 addToSchedule(next);
							 return true;
						 }
					 }
					 combineTime = i;
					 duration = next.getCombinedExecutionDuration(currentEntry);
					 endTime = startTime + duration - 1;
					 break;
				 }
			}
		}
		next.setEndTime(endTime);
		var responseTime = endTime - next.getReleaseTime();
		if (responseTime > next.getDeadline()) {
			return false; // unable to schedule this entry
		}
		/* Add the endTime to the elements endTimes Q
		 * so that we can track predecessor endTimes
		 * and make sure successors don't start before 
		 * predecessors
		 */
		next.addToEndTimes(endTime); 
		/* remove predecessor end time that matches this start time */
		var pred = next.getPredecessor();
		if (pred != null) {
			pred.pollEndTimes(); 
		}
		/* Create a new entry, based on next, to be entered
		 * into the scheduling tale
		 */
		var entry = new Activation(next, startTime);
		if (combineTime < endScheduleTime) {
			/* change entry to point to combinedActivation
			 * and set startTime to the beginning of the 
			 * combined entries */
			var combinedActivation = new CombinedActivation(currentEntry, entry);
			entry = combinedActivation;
			startTime = entry.getStartTime();
			duration = entry.getExecutionDuration();
			endTime = startTime + duration - 1;
			entry.setEndTime(endTime);
			next.setEndTime(endTime);
		}
		for (int i = startTime; i <= endTime; i++) {
			/* Add this object to the schedule for the
			 * from startTime to endTime.
			 */
			schedule.set(channel, i, entry);
		}
		return true;
	}

	private void reportNotSchedulable(Activation object) {
		object.print("This entry is not Schedulable:");
	}

	public void printVerbose() {
		System.out.println("\nSchedule:");
		var endSchedule = schedule.get(0).size();
		for (int i = 0; i < numChannels; i++) {
			System.out.printf("\nChannel: %d",i);
			var name = UNKNOWN;
			for (int j = 0; j < endSchedule; j++) {
				var entry = schedule.get(i,j);
				if (entry != null) {
					var entryName = entry.getName();
					if (!name.equals(entryName)) {
						name = entryName;
						entry.print("Time " + j +":");
					}
				}
			}
		}
	}

	public void print() {
		System.out.printf("\nSchedule (length is %d):\n", endScheduleTime);
		for (int i = 0; i < schedule.size(); i++) {
			System.out.printf("\nChannel: %d: ",i);
			var name = UNKNOWN;
			var startTime = -1;
			for (int j = 0; j < endScheduleTime; j++) {
				var entry = schedule.get(i,j);
				if (entry != null) {
					var entryName = entry.getName();
					if (!name.equals(entryName) || startTime != entry.getStartTime()) {
						/* only print out unique entries (not duplicates in the interval */
						name = entryName;
						startTime = entry.getStartTime();
						System.out.printf("%s[%d,%d] ", 
								name, entry.getStartTime(),entry.getEndTime());
					} 
				}
			}
		}
		/* report the number of Activations not schedulable, if any */
		ArrayList<Activation> notSchedulable = schedule.get(numChannels);
		var numNotSchedulable = 0; 	
		var name = UNKNOWN;
		var lastStartTime = -1;
		for (int i = 0; i < endScheduleTime; i++) {
			var entry = notSchedulable.get(i);
			if (entry != null) {
				var entryName = entry.getName();
				if (!name.equals(entryName) || (name.equals(entryName) && lastStartTime != entry.getStartTime())) {
					name = entryName;
					lastStartTime = entry.getStartTime();
					numNotSchedulable++;
				}
			}
		}
		if (numNotSchedulable > 0 ) {
			System.out.printf("\n%d Activations were not schedulable\n", numNotSchedulable);
		}
		System.out.println();
	}
	
}
