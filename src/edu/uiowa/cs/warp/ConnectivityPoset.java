package edu.uiowa.cs.warp;

import java.util.ArrayList;

/**
 * ConnectivityPoset extends Poset by selecting
 * primary coordinator in the flow based on highest
 * global connectivity.
 * 
 * @author sgoddard
 */
public class ConnectivityPoset extends Poset {

	ConnectivityPoset(WorkLoad workload) {
		super(workload);
		
	}
	
	
	/**
	 *
	 * @param nodesInFlow
	 * @return the node in the flow with highest global connectivity.
	 */
	@Override
	protected Integer findCoordinator(ArrayList<Node> nodesInFlow) {
		
		var coordinatorIndex = 0;
		var maxConnections = 0;
		var size = nodesInFlow.size();
		for (Integer i = 0; i < size; i++) {
			/* get the #connections for this node from the
			 * connectivity Map and see if it is the max so far
			 * if so, store its index in the flow
			 */
			var node = nodesInFlow.get(i);
			var numConnections = nodeConnectivityMap.get(node.getName());
			if (numConnections > maxConnections) {
				maxConnections = numConnections;
				coordinatorIndex = i;
			}
		}	
		/* return the index with the largest connectivity */
		return coordinatorIndex;
	}
	
}
