/**
 * 
 */
package edu.uiowa.cs.warp;

import edu.uiowa.cs.warp.SystemAttributes.ScheduleChoices;

/**
 * Factory Class to create a schedule instance.
 * This factory can be used to create different
 * types of schedules.
 * @author sgoddard
 *
 */
public class SystemFactory {
	public static WarpInterface create(WorkLoad workload, Integer numChannels, ScheduleChoices choice) {
		return new WarpSystem(workload,numChannels, choice);
	}
}
