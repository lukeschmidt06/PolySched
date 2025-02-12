package edu.uiowa.cs.warp;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public abstract class SchedulableObject {

	private static final Integer DEFAULT = 0;
	private static final Integer DEFAULT_PERIOD = 100; 
	private static final Integer DEFAULT_DEADLINE = 100;
	private static final String UNKNOWN = "unknown";
	private static final Integer BEFORE_START = -1;
	
	private String name;
	private Integer priority;
	private Integer period;
	private Integer deadline;
	private Integer phase;
	private Integer releaseTime;
	private Integer startTime;
	private Integer endTime;
	private Integer lastUpdateTime = BEFORE_START;
	private SchedulableObject predecessor;
	/* endTimes are used to keep track of multiple
	 * releases and executions of this object in
	 * a hyperPeriod, so that precedence constraints
	 * are maintained.
	 */
	private PriorityQueue<Integer> endTimes;
	/* subObjects contains objects iff this object
	 * is some combination of the subObjects. In that
	 * case the the start and end times of this object
	 * reflect the execution slice of the super object.
	 * The name will be a combination of the subObjects.
	 */
	private Queue<SchedulableObject> subObjects;
	
	SchedulableObject(){
		setDefaultParameters();
		setNextReleaseTime(0); // initialize parameters
		this.lastUpdateTime = -1; // reset lastUpdate 
	}
	
	/**
	 * Constructor that creates a copy of schedulable 
	 * parameters of the input obj, but setting its
	 * lastUpdateTime to input time.
	 * 
	 * @param obj
	 * @param time
	 */
	SchedulableObject(SchedulableObject obj, Integer time){
		this.name = obj.getName();
		this.priority = obj.getPriority();
		this.period = obj.getPeriod();
		this.deadline = obj.getDeadline();
		this.phase = obj.getPhase();
		this.startTime = obj.getStartTime();
		this.endTime = obj.getEndTime();
		this.releaseTime = obj.getReleaseTime(); // initialize parameters
		this.lastUpdateTime = time; // reset lastUpdate 
		this.predecessor = obj.getPredecessor();
		this.endTimes = new PriorityQueue<Integer>(); 
		this.endTimes.addAll(obj.getAllEndTimes());
		this.subObjects = new LinkedList<SchedulableObject>();
		this.subObjects.addAll(obj.getSubObjects());
		
	}
	SchedulableObject(Integer priority, Integer period, Integer deadline, Integer phase){
		setDefaultParameters();
		this.priority = priority;
		this.period = period;
		this.deadline = deadline;
		this.phase = phase;
		setNextReleaseTime(0); // initialize parameters
		this.lastUpdateTime = BEFORE_START; // reset lastUpdate 
	}
	
	SchedulableObject(String name, Integer priority, Integer period, Integer deadline, Integer phase){
		setDefaultParameters();
		this.name = name;
		this.priority = priority;
		this.period = period;
		this.deadline = deadline;
		this.phase = phase;
		setNextReleaseTime(0); // initialize parameters
		this.lastUpdateTime = BEFORE_START; // reset lastUpdate 
	}
	
	private void setDefaultParameters() {
		this.name = UNKNOWN;
		this.priority = DEFAULT;
		this.period = DEFAULT_PERIOD;
		this.deadline = DEFAULT_DEADLINE;
		this.phase = DEFAULT;
		this.startTime = BEFORE_START;
		this.endTime = BEFORE_START;
		this.predecessor = null; 
		this.endTimes = new PriorityQueue<Integer>(); 
		this.subObjects = new LinkedList<SchedulableObject>();
	}

	/**
	 * @return the endTimes queue
	 */
	public PriorityQueue<Integer> getAllEndTimes() {
		return endTimes;
	}
	
	public void addToEndTimes(Integer time) {
		/* Add the endTime to the endTimes Q
		 * so that we can track predecessor endTimes
		 * and make sure successors don't start before 
		 * predecessors
		 */
		endTimes.add(time);
	}
	
	public Integer peekEndTimes() {
		return endTimes.peek();
	}
	
	public void pollEndTimes() {
		endTimes.poll();
	}
	
	public void addSubObject(SchedulableObject obj){
		subObjects.add(obj);
	}
	
	/**
	 * @return the subObjects
	 */
	public Queue<SchedulableObject> getSubObjects(){
		return subObjects;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * @return the releaseTime
	 */
	public Integer getReleaseTime() {
		return releaseTime;
	}

	/**
	 * @return the period
	 */
	public Integer getPeriod() {
		return period;
	}

	/**
	 * @return the deadline
	 */
	public Integer getDeadline() {
		return deadline;
	}

	/**
	 * @return the phase
	 */
	public Integer getPhase() {
		return phase;
	}

	/**
	 * @return the lastUpdateTime
	 */
	public Integer getLastUpdateTime() {
		return lastUpdateTime;
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
	 * @return the predecessor
	 */
	public SchedulableObject getPredecessor() {
		return predecessor;
	}

	/**
	 * @param predecessor the predecessor to set
	 */
	public void setPredecessor(SchedulableObject predecessor) {
		this.predecessor = predecessor;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Integer endTime) {
		this.endTime = endTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Integer startTime) {
		this.startTime = startTime;
	}

	/**
	 * @param releaseTime the releaseTime to set
	 */
	public void setReleaseTime(Integer releaseTime) {
		this.releaseTime = releaseTime;
	}

	/**
	 * @param lastUpdateTime the lastUpdateTime to set
	 */
	public void setLastUpdateTime(Integer lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	/**
	 * @param period the period to set
	 */
	public void setPeriod(Integer period) {
		this.period = period;
	}

	/**
	 * @param deadline the deadline to set
	 */
	public void setDeadline(Integer deadline) {
		this.deadline = deadline;
	}

	/**
	 * @param phase the phase to set
	 */
	public void setPhase(Integer phase) {
		this.phase = phase;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * @param releaseTime the releaseTime to set
	 */
	public void setNextReleaseTime(Integer currentTime) {
		if (currentTime >= lastUpdateTime) {
			// released every (j*period)+phase, for all j >=0
			Integer j = 0;
			if (period > 0) {
				j = (int) Math.ceil((double)currentTime/(double)period); // gives floor
			} 
			Integer nextRelease = (j*period)+phase ;
			releaseTime = nextRelease;
			lastUpdateTime = currentTime;
		}
	}
	
	public int maxPhaseComparison(SchedulableObject obj2) {
		// for ascending order of priority
		return compareDescendingOrder(this.getDeadline(), obj2.getDeadline());
	}
	
	public int maxPhaseComparison(SchedulableObject obj1, SchedulableObject obj2) {
		return obj1.maxPhaseComparison(obj2);
	}
	
	public int deadlineComparison(SchedulableObject obj2) {
		// for ascending order of priority
		return compareAscendingOrder(this.getDeadline(), obj2.getDeadline());
	}
	
	public int deadlineComparison(SchedulableObject obj1, SchedulableObject obj2) {
		return obj1.deadlineComparison(obj2);
	}
	public int periodComparison(SchedulableObject obj2) {
		// for ascending order of priority
		return compareAscendingOrder(this.getPeriod(), obj2.getPeriod());
	}
	
	public int periodComparison(SchedulableObject obj1, SchedulableObject obj2) {
		return obj1.periodComparison(obj2);
	}
	
	public int priorityComparison(SchedulableObject obj2) {
		// for ascending order of priority
		return compareAscendingOrder(this.getPriority(), obj2.getPriority());
	}
	
	public int priorityComparison(SchedulableObject obj1, SchedulableObject obj2) {
		return obj1.priorityComparison(obj2);
	}
	
	public int releaseTimeComparison(SchedulableObject obj2) {
		// for ascending order of release time
		return compareAscendingOrder(this.getReleaseTime(), obj2.getReleaseTime());
	}
	
	public int releaseTimeComparison(SchedulableObject obj1, SchedulableObject obj2) {
		return obj1.releaseTimeComparison(obj2); 
	}
	
	public int LatestReleaseTimeComparison(SchedulableObject obj2) {
		// for ascending order of release time
		return compareDescendingOrder(this.getReleaseTime(), obj2.getReleaseTime());
	}
	
	public int LatestReleaseTimeComparison(SchedulableObject obj1, SchedulableObject obj2) {
		return obj1.releaseTimeComparison(obj2); 
	}
	
	public int compareAscendingOrder(int v1, int v2) {
		if (v1 < v2)
			return -1; 
		else if (v1 > v2)
			return 1; 
		return 0; // tied: values are the same 
	}
	
	public int compareDescendingOrder(int v1, int v2) {
		if (v1 < v2)
			return 1; 
		else if (v1 > v2)
			return -1; 
		return 0; // tied: values are the same 
	}
	
	public void print(String headerMsg) {
		System.out.printf("\n%s",headerMsg);
		print();
	}
	
	public void print() {
		System.out.printf("\nName: %s\n",getName());
		System.out.printf("\tPriority: %d\n",getPriority());
		System.out.printf("\tPeriod: %d\n",getPeriod());
		System.out.printf("\tDeadline: %d\n",getDeadline());
		System.out.printf("\tPhase: %d\n", getPhase());
		System.out.printf("\tReleaseTime: %d\n",getReleaseTime());
		System.out.printf("\tStartTime: %d\n",getStartTime());
		System.out.printf("\tEndTime: %d\n",getEndTime());
	}
}
