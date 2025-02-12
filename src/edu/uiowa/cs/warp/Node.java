package edu.uiowa.cs.warp;

/**
 * @author sgoddard
 *
 */

import java.util.*;

public class Node extends SchedulableObject implements Comparable<Node> {
	
	private static final Integer DEFAULT_CHANNEL = 0; 
	
	private Integer channel; // used to track the current channel node is using for Tx/Rx
	private Integer index; // used as an alternate name for the simulator input file
    private ArrayList<Edge> edges; // edges connected to the node
    private Set<String> conflicts; // nodes with conflicts
    
    // constructor that sets name, and index
    Node(String name, Integer priority, Integer index) {
    	super();
    	setName(name);
    	setPriority(priority);
    	this.index = index;
    	this.channel = DEFAULT_CHANNEL;
    	this.edges = new ArrayList<Edge>();
    	this.conflicts = new HashSet<String>();
    }
    
    @Override
    public int compareTo(Node node) {
    	// ascending order (0 is highest priority)
        return node.getPriority() > this.getPriority() ? -1 : 1;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    /**
	 * @return the edges
	 */
	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public void addEdge(Edge edge) {
    	edges.add(edge);
    }
    
	/**
	 * @return the conflicts
	 */
	public Set<String> getConflicts() {
		return conflicts;
	}

	public void addConflict(String name) {
    	conflicts.add(name);
    }
	
    /**
	 * @return the size of edges
	 */
    public Integer numEdges() {
    	return edges.size();
    }

	/**
	 * @return the channel
	 */
	public Integer getChannel() {
		return channel;
	}

	/**
	 * @return the index
	 */
	public Integer getIndex() {
		return index;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(Integer channel) {
		this.channel = channel;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(Integer index) {
		this.index = index;
	}

	@Override
	public void print(String headerMsg) {
		System.out.printf("\n%s",headerMsg);
		this.print();
	}
	
	@Override
	public void print() {
		super.print();
		System.out.print("Edge info for this partiion\n");
		for (Edge edge: edges) {
			edge.print();
		}
	}

}

