package edu.uiowa.cs.warp;

import java.util.ArrayList;

/**
 * Represents a flow in the system, both extending the SchedulableObject class
 * and implementing Comparable for flow comparisons.
 * 
 * Encapsulates information about the flow, including its name,
 * priority, index, number of transmissions per link, nodes, and edges.
 * 
 * @author sgoddard
 *
 */
public class Flow extends SchedulableObject implements Comparable<Flow>{

	private static final Integer UNDEFINED = -1;
	private static final Integer DEFAULT_FAULTS_TOLERATED = 0; 
	private static final Integer DEFAULT_INDEX = 0;
	private static final Integer DEFAULT_PERIOD = 100; 
	private static final Integer DEFAULT_DEADLINE = 100;
	private static final Integer DEFAULT_PHASE = 0;
	

    Integer initialPriority = UNDEFINED;
    Integer index;  // order in which the node was read from the Graph file
    Integer numTxPerLink; //  determined by fault model
    ArrayList<Node> nodes; // Flow src is 1st element and flow snk is last element in array
    /*
     *  nTx needed for each link to reach E2E reliability target. Indexed by src node of the link. 
     *  Last entry is total worst-case E2E Tx cost for schedulability analysis
     */
    ArrayList<Integer> linkTxAndTotalCost; 
    ArrayList<Edge> edges; //used in Partition and scheduling
    Node nodePredecessor;
    Edge edgePredecessor;
    
    /**
     * Constructor that sets name, priority, and index
     * 
     * @param name     The name of the flow.
     * @param priority The priority of the flow.
     * @param index    The index of the flow.
     */
    Flow (String name, Integer priority, Integer index){
    	super(name, priority, DEFAULT_PERIOD, DEFAULT_DEADLINE, DEFAULT_PHASE);
    	this.index = index;
        /*
         *  Default numTxPerLink is 1 transmission per link. Will be updated based
         *  on flow updated based on flow length and reliability parameters
         */
        this.numTxPerLink = DEFAULT_FAULTS_TOLERATED + 1; 
        this.nodes = new ArrayList<>();
        this.edges  = new ArrayList<>();
        this.linkTxAndTotalCost = new ArrayList<>();
        this.edges = new ArrayList<>();	
        this.nodePredecessor = null;
        this.edgePredecessor = null;
    }
    
    /**
     * Default constructor for creating a Flow object with default values.
     * It initializes the Flow with default index, numTxPerLink, and empty ArrayLists for nodes,
     * linkTxAndTotalCost, and edges. Predecessors for node and edge are set to null.
     */
    Flow () {
    	super();
    	this.index = DEFAULT_INDEX;
    	/*
    	 *  Default numTxPerLink is 1 transmission per link. Will be updated based
    	 *  on flow updated based on flow length and reliability parameters
    	 */
    	this.numTxPerLink = DEFAULT_FAULTS_TOLERATED + 1; 
    	this.nodes = new ArrayList<>();
    	this.linkTxAndTotalCost = new ArrayList<>();
    	this.edges = new ArrayList<>();
    	this.nodePredecessor = null;
        this.edgePredecessor = null;
    }

	/**
	 * @return the initialPriority
	 */
	public Integer getInitialPriority() {
		return initialPriority;
	}

	/**
	 * @return the index
	 */
	public Integer getIndex() {
		return index;
	}

	/**
	 * @return the numTxPerLink
	 */
	public Integer getNumTxPerLink() {
		return numTxPerLink;
	}

	/**
	 * @return the nodes
	 */
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	
	/**
	 * @return the nodes
	 */
	public ArrayList<Edge> getEdges() {
		return edges;
	}

	/**
	 * Add and edge to the flow.
	 */
	public void addEdge(Edge edge) {
		/* set predecessor and add edge to flow */
		edge.setPredecessor(edgePredecessor);
		edges.add(edge);
		/* update predecessor for next edge added */
		edgePredecessor = edge;
	}
	
	/**
	 * Add and edge to the flow.
	 */
	public void addNode(Node node) {
		/* set predecessor and add edge to flow */
		node.setPredecessor(nodePredecessor);
		nodes.add(node);
		/* update predecessor for next edge added */
		nodePredecessor = node;
	}
	/**
	 * @return the linkTxAndTotalCost
	 */
	public ArrayList<Integer> getLinkTxAndTotalCost() {
		return linkTxAndTotalCost;
	}

	/**
	 * @param initialPriority the initialPriority to set
	 */
	public void setInitialPriority(Integer initialPriority) {
		this.initialPriority = initialPriority;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(Integer index) {
		this.index = index;
	}

	/**
	 * @param numTxPerLink the numTxPerLink to set
	 */
	public void setNumTxPerLink(Integer numTxPerLink) {
		this.numTxPerLink = numTxPerLink;
	}

	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}

	/**
	 * @param linkTxAndTotalCost the linkTxAndTotalCost to set
	 */
	public void setLinkTxAndTotalCost(ArrayList<Integer> linkTxAndTotalCost) {
		this.linkTxAndTotalCost = linkTxAndTotalCost;
	}

	@Override
    public int compareTo(Flow flow) {
    	// ascending order (0 is highest priority)
        return flow.getPriority() > this.getPriority() ? -1 : 1;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
}
