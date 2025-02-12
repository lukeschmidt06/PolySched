package edu.uiowa.cs.warp;

import java.util.*;

/**
 * PreemptiveSchedule extends Schedule to build a preemptive, 
 * priority schedule for the Poset using numChannels. 
 * 
 * @author sgoddard2
 *
 */
public class PreemptiveSchedule extends Schedule {
	
	PreemptiveSchedule(Poset poset, Integer numChannels) {
		super(poset,numChannels);
	}
	
	@Override
	protected ScheduleLocation continueSearch(Integer time, Activation activation) {
		return findPreeptiveChannelAndTime(time,activation);
	}
	
	@Override
	public ProgramSchedule toProgram() {
		var program = new ProgramSchedule();	
		
//TODO write code to support preemption
		
		return program;
	}
	
	private ScheduleLocation findPreeptiveChannelAndTime(Integer startTime, Activation schedulableObject ) {
		
		var channel = numChannels;
		var time = startTime;
		var result = findMaxTimeAvailable(time, schedulableObject);
		var channelConsidered = result.channel;
		var timeAvailable = result.time;
		
		
		if (channelConsidered == numChannels) {
			/* no available time slots near this time, even for preemption */
			return findChannelAndTime(time+1,schedulableObject);
		} else {
			/* can use timeAvailable.time here on timeAvailable.channel
			 * then must find additional time on same channel until 
			 * done
			 */
			// return findAdditionalTime(timeAvailable.channel,time+1,schedulableObject);
			var duration = schedulableObject.getExecutionDuration();
			var timeRemaining = duration-timeAvailable;
			var additionalScheduleTimes = findAdditionalTime(channelConsidered,time+timeAvailable,schedulableObject,timeRemaining);
			var numStartTimes = additionalScheduleTimes.size();
			var lastStartTime = endScheduleTime; 
			if (numStartTimes > 0 ) {
				lastStartTime = additionalScheduleTimes.get(numStartTimes-1).getStartTime();
			}
			if (lastStartTime < endScheduleTime) {
				// schedulableObject.addAdditionalStartTime(nextStartTime);
				channel = channelConsidered;
				schedulableObject.addScheduleTimes(additionalScheduleTimes);
			} else {
				time = lastStartTime;
			}
		}
		return new ScheduleLocation(channel, time);	
	}
	
	private ArrayList<ScheduleTime> findAdditionalTime(Integer channel, Integer startTime,
			Activation a1, Integer timeRemaining) {
		var time = startTime;
		var results = new ArrayList<ScheduleTime>();
		
		// if (timeRemaining <= )
		while (time < endScheduleTime && conflictsExist(a1, time, timeRemaining )) {
			time ++; 
		}
		if (time >= endScheduleTime) {
			/* no conflict time available */
			results.add(new ScheduleTime(time,time));
			return results;
		} 
		
		if (isAvailable(a1, channel, time, timeRemaining)) {
			results.add(new ScheduleTime(time,time+timeRemaining-1));
		} else {
			var timeAvailable = availableTime(a1,  channel, time);
			if (timeAvailable > 0) {
				/* found at least one time slot available */
//				a1.addAdditionalStartTime(time);
				results.addAll(findAdditionalTime(channel,time+timeAvailable,a1,timeRemaining-timeAvailable));
			} else {
				/* look ahead in time by one unit */
				results.addAll(findAdditionalTime(channel,time+1,a1,timeRemaining));
			}
			
		}
		
		return results;
	}
	
	
	private ScheduleLocation findMaxTimeAvailable(Integer time, Activation schedulableObject) {
		var maxTime = 0;
		var bestChannel = numChannels;
		
		for (int channel = 0; channel < numChannels ; channel++) {
			var timeAvailable = availableTime(schedulableObject,channel,time);
			if (timeAvailable > maxTime) {
				maxTime = timeAvailable;
				bestChannel = channel;
			}
		}
// TEMP
		if (maxTime > 0) {
			System.err.printf("\n\t\tMaxTime is %d and bestChannel is %d\n", maxTime, bestChannel);
		}
// TEMP
		return new ScheduleLocation(bestChannel,maxTime);
	}
	
	private Integer availableTime(Activation a1, Integer channel, Integer start) {
		Integer result = 0;
		
		while (isAvailable(a1,channel,start+result,1)) {
			result++;
		}
		return result;
	}
	
	private Boolean addToSchedule(Activation next, Integer channel, 
								Integer startTime, Integer endTime) {
		var success = true;
		var combineTime = endScheduleTime;
		
		var nextScheduleTime = next.getNextScheduleTime(startTime);
		var nextStartTime = nextScheduleTime.getStartTime();
		var nextEndTime = nextScheduleTime.getEndTime();
		
		if (startTime >= endScheduleTime || endTime >= endScheduleTime) {
			/* can't schedule this activation */
			return false;
		}
		
		if (nextStartTime < Integer.MAX_VALUE && nextEndTime < Integer.MAX_VALUE) {
			/* add the next slice of execution to the schedule */
			success = addToSchedule(next,channel,nextStartTime, nextEndTime);
		}
		if (!success) {
			return false;
		}
		
		/* Successful adding next to later points in the schedule.
		 * So, now we see if this is the last
		 * 
		 */
		if (nextStartTime == Integer.MAX_VALUE && nextEndTime == Integer.MAX_VALUE) {
			/* this is the last slice of execution, so wrap things up */
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
		}
		/* Create a new entry, based on next, to be entered
		 * into the scheduling tale
		 */
		var entry = new Activation(next, startTime);
		/* Check to see we need to combine this activation with
		 * an existing activation in any of the slots. If so,
		 * save that time so we can update the actual start time
		 */
		Activation currentEntry = null;
		for (int i = startTime; i <= endTime; i++) {
			currentEntry = schedule.get(channel,i);
			if (currentEntry != null && next.canCombine(currentEntry)) {
				/* found an entry to combine, record the time,
				 * update the combined duration and new
				 * endTime, then exit the loop
				 */
				combineTime = i;
				// currentEntry = entry;
				break;
			}
		}
		entry.setStartTime(startTime); // set start time for this entry
		entry.setEndTime(endTime); // set end time for this entry
		if (combineTime < endScheduleTime) {
			/* change entry to point to combinedActivation
			 * and set startTime to the beginning of the 
			 * combined entries */
			var combinedActivation = new CombinedActivation(currentEntry, entry);
			entry = combinedActivation;
			startTime = entry.getStartTime();
		}
		for (int i = startTime; i <= endTime; i++) {
			/* Add this object to the schedule for the
			 * from startTime to endTime.
			 */
			schedule.set(channel, i, entry);
		}
		
		return success;
	}
	

// THIS IS A COPY AND PASTE OF THE ORIGINAL. NEED TO UPDATE TO SUPPORT
//	THE PREEMPTION IPLEMENTATION THAT ADDS START TIMES WHEN PREEPTED
	@Override
	protected Boolean addToSchedule(Activation next) {
		var channel = getChannelAndSetStartTime(next);
		if (channel == numChannels) {
			return false;
		}
		
		/* found a time and channel  that are valid */
		var startTime = next.getStartTime(); // set by getChannelAndSetStartTime()
		var success = false; 
		
// PREEMPT
		var timeAvailable = availableTime(next, channel, startTime);
		var duration = next.getExecutionDuration();
		
		if (timeAvailable == 0 ) {
			/* then currentEntry at this location in the schedule
			 * is not null and we can combine, but best to check
			 */
			var currentEntry = schedule.get(channel,startTime);
			if (currentEntry != null && currentEntry.canCombine(next)) {
				/* no conflict with this entry, and can
				 * combine so check for combined entry time
				 *  to see if we can combine activations
				 */
				var combinedDuration = currentEntry.getCombinedExecutionDuration(next);
				var combinedEntry = new CombinedActivation(currentEntry,next);
				var searchTime = currentEntry.getEndTime()+1;
				var searchLength = combinedDuration - duration;
				if (isAvailable(combinedEntry, channel, searchTime, searchLength)) {
					timeAvailable = combinedDuration;
					duration = combinedDuration;
				}
			}
		}
			if (duration <= timeAvailable) {
				/* can execute without preemption */
				var endTime = startTime+duration-1;
				success = addToSchedule(next,channel,startTime,endTime);
			} else {
				/* Recursively add 'slices' to the schedule.
				 * success == true => all slices added successfully
				 * otherwise, none of the slices were added
				 */
				var endTime = startTime + timeAvailable-1;
				success = addToSchedule(next,channel,startTime,endTime);
			}
		return success;
	}
}
