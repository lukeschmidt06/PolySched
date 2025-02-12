package edu.uiowa.cs.warp;

public class ScheduleTime {
	private Integer startTime;
	private Integer endTime;

	ScheduleTime(Integer startTime, Integer endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * @return the startTime
	 */
	public Integer getStartTime() {
		return startTime;
	}

	/**
	 * @return the endTime
	 */
	public Integer getEndTime() {
		return endTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Integer startTime) {
		this.startTime = startTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Integer endTime) {
		this.endTime = endTime;
	}
}
