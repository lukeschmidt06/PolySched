package edu.uiowa.cs.warp;

public class Edge extends SchedulableObject implements Comparable<Edge> {

	private static final String UNKNOWN = "Unknown";

	private String flow;
	private String src;
	private String snk;
	private Integer instance;
	private String coordinator;
	private EdgeState state;
	private Integer numTx;

	private enum EdgeState {
		RELEASED, EXECUTING, NOT_READY 
	}

	Edge() {
		super();
		this.flow = UNKNOWN;
		this.src = UNKNOWN;
		this.snk = UNKNOWN;
		this.instance = 0;
		this.coordinator = UNKNOWN;
		this.state = EdgeState.NOT_READY;
		this.numTx = 0;
	}
	
	Edge(String flow, String src, String snk, Integer priority, 
		Integer period, Integer deadline, Integer phase, Integer numTx) {
		super(priority, period, deadline, phase);
		this.flow = flow;
		this.src =src;
		this.snk = snk;
		this.instance = 0;
		this.coordinator = UNKNOWN;
		this.state = EdgeState.NOT_READY;	
		this.numTx = numTx;
	}

	@Override
    public int compareTo(Edge edge) {
        return edge.getPriority() > this.getPriority() ? 1 : -1;
    }

    @Override
    public String toString() {
    	String result = String.format("%s:(%s,%s)", 
    			this.flow, this.src, this.snk);
        return result;
    }

	/**
	 * @return the flow
	 */
	public String getFlow() {
		return flow;
	}

	/**
	 * @param flow the flow to set
	 */
	public void setFlow(String flow) {
		this.flow = flow;
	}

	/**
	 * @return the src
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * @param src the src to set
	 */
	public void setSrc(String src) {
		this.src = src;
	}

	/**
	 * @return the snk
	 */
	public String getSnk() {
		return snk;
	}

	/**
	 * @param snk the snk to set
	 */
	public void setSnk(String snk) {
		this.snk = snk;
	}

	/**
	 * @return the instance
	 */
	public Integer getInstance() {
		return instance;
	}

	/**
	 * @param instance the instance to set
	 */
	public void setInstance(Integer instance) {
		this.instance = instance;
	}

	/**
	 * @return the coordinator
	 */
	public String getCoordinator() {
		return coordinator;
	}

	/**
	 * @return the numTx
	 */
	public Integer getNumTx() {
		return numTx;
	}

	/**
	 * @param coordinator the coordinator to set
	 */
	public void setCoordinator(String coordinator) {
		this.coordinator = coordinator;
	}

	/**
	 * @return the state
	 */
	public EdgeState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(EdgeState state) {
		this.state = state;
	}

	@Override
	public void print(String headerMsg) {
		System.out.printf("\n%s",headerMsg);
		this.print();
	}
	
	@Override
	public void print() {
		super.print();
		System.out.printf("\tFlow:%s\n", this.flow);
		System.out.printf("\tSrc:%s\n", this.src);
		System.out.printf("\tSnk:%s\n", this.snk);
	}
	
    
}
