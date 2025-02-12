package edu.uiowa.cs.warp;

import java.util.ArrayList;

/**
 * ConnectivityPoset extends Poset by selecting
 * the base station in the flow as the primary 
 * coordinator
 * 
 * @author sgoddard
 */
public class WarpPoset extends Poset {
	
	WarpPoset(WorkLoad workload) {
		super(workload);
	}
	
	/**
	 *
	 * @param nodesInFlow
	 * @return the base station location in the flow.
	 */
	@Override
	protected Integer findCoordinator(ArrayList<Node> nodesInFlow) {
		/* first find base station(s) */
		var baseStation = findBaseStation();
		var baseName = baseStation.getName();
		var size = nodesInFlow.size();
		/* assume flow sink is the coordinator */
		Integer coordinatorIndex = size-1;

		if (baseName.equals(nodesInFlow.get(0).getName())) {
			/* flow src is coordinator, so update index */
			coordinatorIndex = 0;
		}
		return coordinatorIndex;
	}
}
