package edu.uiowa.cs.warp;

public class CombinedActivation extends Activation implements Comparable<Activation> {

	
	CombinedActivation(String name){
		super(name);
	}
			
	CombinedActivation(Activation elem, Integer time) {
		super(elem, time);	
	} 
	
	CombinedActivation (Activation priorEntry, Activation next) {
		/* create a new this and add priorEntry 
		 * and next as subObjects. The new name is 
		 * "PriorName + NextName". 
		 */
		super(priorEntry,next.getLastUpdateTime());
		String combinedName = 
				priorEntry.getName() + "+" + next.getName();
		/* initially, this will be a clone of priorEntry */;
		/* now update its name and add relevant attributes from next */
		this.setName(combinedName);
		this.addSubObject(priorEntry);
		this.addSubObject(next);
		var endTime = Math.max(priorEntry.getEndTime(), next.getEndTime());
		this.setEndTime(endTime);
		var startTime = Math.min(priorEntry.getStartTime(), next.getStartTime());
		this.setStartTime(startTime);
		this.addEdges(next.getEdges());
		this.addCoordinators(next.getCoordinators());
		this.addConflicts(next.getConflicts());
		
	}

	@Override
	public Boolean canCombine(Activation a2) {
		Boolean result = false;
		/* make sure a2 is not null */
		if (a2 != null) {
			var newEntryCoordinator = a2.getCoordinators().get(0);
			if (getCoordinators().contains(newEntryCoordinator)) {
				/* The (first) coordinator of the newEntry
				 * is in the list of coordinators of the prior entry.
				 * Thus, these two can be combined for efficiency 
				 */

				result = true;
			}
		}
		return result;
	}
}
