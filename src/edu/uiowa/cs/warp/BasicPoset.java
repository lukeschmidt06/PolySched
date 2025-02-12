package edu.uiowa.cs.warp;

/**
 * ConnectivityPoset extends Poset by selecting
 * primary coordinator in the flow based on highest
 * local connectivity.
 * 
 * @author sgoddard
 */
public class BasicPoset extends Poset {
	BasicPoset(WorkLoad workload) {
		super(workload);
	}
}
