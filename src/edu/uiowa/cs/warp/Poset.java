package edu.uiowa.cs.warp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Poset is a partially ordered set of activations
 * that are partitions of the workload to be scheduled.
 * This version assumes each activation is a node
 * and each node has a list of edges associated with it.
 * Each node/activation in an edge is identified as either 
 * a transmitter or sender for that edge.
 * 
 * @author sgoddard
 */
public abstract class Poset {	

	private Activations activations;
	private WorkLoad workload;
	protected Collection<Flow> flows;
	private Collection<Node> nodes;
	private NodeMap nodeMap;
	private PriorityQueue<Activation> priorityQueue;
	protected HashMap<String, Integer> nodeConnectivityMap;

	FlowNodeToActivationMap activationMap;
	/*
	 * Partition types:
	 *  
	 *  by node--each node is a partition using only one channel
	 *  by channel
	 *  	by node then identify when a coordinator and when not
	 *  	by flow and coordinator in that flow
	 *  	many others...
	 */

	/*
	 * Heuristics for finding coordinators
	 * 	First coordinator has max edges
	 * 	Then for each flow with that coordinator
	 * 		identify other coordinators two away, etc.
	 */
	Poset(WorkLoad workload) {
		this.workload = workload;
		this.activations = new Activations();
		this.activationMap = new FlowNodeToActivationMap();
		/* init map to return a null activation if null key intered */
		this.activationMap.put(null, null);
		flows = workload.getFlows().values();
		nodeMap = workload.getNodes();
		nodes = nodeMap.values();
		priorityQueue = new 
				SchedulableObjectQueue<Activation>(new 
						PriorityComparator<Activation>());
		/* get global node connectivity */
		nodeConnectivityMap = getNodeConnectivity();
		buildPosetOfActivations();
	}

	private HashMap<String, Integer> getNodeConnectivity() {
		HashMap<String, Integer> map = new HashMap<>();
		
		for (Flow flow: flows) {
			var nodesInFlow = flow.getNodes();
			var size = nodesInFlow.size();
			for (Integer i =0 ; i < size; i++) {
				var nodeName = nodesInFlow.get(i).getName();
				var connectivity = map.get(nodeName);
				var connections = 2; // default # of connections
				if (i == 0 || i == size-1) {
					/* src or snk => connections = 1 */
					connections = 1;
				}
				if (connectivity != null) {
					connectivity += connections;
					map.put(nodeName,connectivity);
				} else {
					map.put(nodeName,connections); // initial connection
				}
			}
		}
		return map;
	}

	public ArrayList<String> getNodeNames() {
		var keys = nodeMap.keySet();
		var nodeNames = new ArrayList<String>(keys.size());
		nodeNames.addAll(keys);
		return nodeNames;
	}
	
	public String[] getNodesNamesOrderedAlphabetically() {
		var nodeNames = workload.getNodeNamesOrderedAlphabetically();
		return nodeNames;
	}

	public void buildPosetOfActivations() {
		/* assumes flow priorities have already been set */

		/*
		 * (1) Create edges, setting priority and release time
		 * 		add edges to node respective node lists as edge is created
		 * (2) Create conflict set for each node (adjacent nodes)
		 * (3) Create activations by selecting coordinators within 
		 * 		each flow and adding that coordinator's edges and
		 * 		release times to an activation with the coordinator's name
		 * 		(each activation will have one coordinator and the
		 * 			number of activations <= number nodes)
		 * (4) Order the activations
		 */

		createEdgeLists(); // (1)
		createConflictLists(); // (2)
		createInitialActivations(); // (3)
		orderActivations(); // (4)
		/* At this point, each flow has been partitioned 
		 */
	}

	private void createEdgeLists() {
		/* assumes flow priorities have already been set */
		var maxFlowLength = workload.maxFlowLength();
		for (Flow flow: flows) {
			var flowNodes = flow.getNodes();
			var numTxArray = workload.getNumTxAttemptsPerLink(flow.getName());
			var flowPhase = flow.getPhase();
			var flowPriority = flow.getPriority();
			var period = flow.getPeriod();
			var deadline = flow.getDeadline();
			Edge edge = null;
			for (Integer i = 0; i < flowNodes.size() -1 ; i++) {
				/* Create edges, setting priority and release time */
				/* This version adds edges to the main node in NodeMap.
				 * This allows creation of large partitions, with at most one per node.
				 * An alternative, we can have a lot of small partitions that can be
				 * combined later, which may create more parallelism.
				 * So, both the global NodeMap nodes and the nodes in the
				 * flows are updated with the same edge information. 
				 * This let's us play with partition creation options later.
				 */
				var srcNode = nodeMap.get(flowNodes.get(i).getName()); 
				var snkNode = nodeMap.get(flowNodes.get(i+1).getName());
				var flowSrc = flowNodes.get(i);  // node in the flow
				var flowSnk = flowNodes.get(i+1);
				var src = srcNode.getName();
				var snk = snkNode.getName();

				/*
				 * Edge priority within a flow is based on position
				 * in flow. Each Flow has its own priority. This 
				 * creates a 2-dimensional priority. Thus, we
				 * can keep a total ordering of edges if we fold the
				 * table into a single row where we assume each new flow
				 * starts maxFlowSize away from the previous flow nodes:
				 * edgePriority = flowPriority*maxFlowLength + edgePosition
				 */
				// var flowPriority = flow.getPriority();
				var priority = (flowPriority * maxFlowLength) + i;
				//var period = flow.getPeriod();
				// var deadline = flow.getDeadline();
				/* edge phase = flow phase + edge position in flow */
				var phase = flowPhase + i; 
				var numTx = numTxArray[i];
				edge = new Edge(flow.getName(),src,snk,priority, 
						period, deadline, phase, numTx);
				/* Add edge to the nodes in the NodeMap */
				srcNode.addEdge(edge);
				snkNode.addEdge(edge);
				/* Add edge to the nodes in the flow */
				flowSrc.addEdge(edge);
				flowSnk.addEdge(edge);
				/* Add the edege to the flow */
				flow.addEdge(edge);

			}
		}
	}

	private void createConflictLists() {
		/* create conflict lists at global and
		 * local levels (i.e., NodeMap and Flow)
		 */
		createGlobalConflictLists();
		createLocalConflictLists();
	}

	private void createGlobalConflictLists() {
		for (Node node: nodes) {
			for (Edge edge: node.getEdges()) {
				/* get src and snk nodes and names */
				var src = edge.getSrc();
				var snk = edge.getSnk();
				var srcNode = nodeMap.get(src);
				var snkNode = nodeMap.get(snk);
				/* 
				 * conflict list is a set, so just add
				 * don't need to worry about duplicates
				 * as Set ensures that doesn't happen.
				 */
				srcNode.addConflict(snk);
				snkNode.addConflict(src);
			}
		}
	}

	private void createLocalConflictLists() {
		for (Flow flow: flows) {
			var flowNodes = flow.getNodes();
			for (Integer i = 0; i < flowNodes.size() -1 ; i++) {
				var srcNode = flowNodes.get(i); 
				var snkNode = flowNodes.get(i+1);
				var src = srcNode.getName();
				var snk = snkNode.getName();
				/* 
				 * conflict list is a set, so just add
				 * don't need to worry about duplicates
				 * as Set ensures that doesn't happen.
				 */
				srcNode.addConflict(snk);
				snkNode.addConflict(src);
			}
		}


	}

	/**
	 *
	 * @param nodesInFlow
	 * @return the node in the flow with highest local connectivity.
	 */
	protected Integer findCoordinator(ArrayList<Node> nodesInFlow) {
		var size = nodesInFlow.size();
		var coordinatorIndex = 0;
		var maxEdges = 0;
		for (Integer i = 0; i < size; i++) {
			var node = nodesInFlow.get(i);
			var numEdges = node.numEdges();
			if (numEdges > maxEdges) {
				maxEdges = numEdges;
				coordinatorIndex = i;
			}
		}
		return coordinatorIndex;
	}
	
	private Integer[] buildTxArray(ArrayList<Node> nodesInFlow) {
		var size = nodesInFlow.size();
		var txArray = new Integer[size];
		for (Integer i = 0; i < size; i++) {
			var node = nodesInFlow.get(i);
			txArray[i] = getMaxTx(node);
		}
		return txArray;
		
	}
	
	private void createInitialActivations() {
		for (Flow flow: flows) {
			var nodesInFlow = flow.getNodes();
			Integer coordinatorIndex = 0;
			/* Find the node with max edges connected.
			 * This will be the first coordinator selected
			 * for this flow.
			 */
			coordinatorIndex = findCoordinator(nodesInFlow);
			var txArray = buildTxArray(nodesInFlow);
			/* if coordinatorIndex is odd, then other coordinators
			 * are also odd numbers, even otherwise. 
			 * */
			Integer startIndex = 0; // for even coordinatorIndex
			if ((coordinatorIndex % 2) == 1) {
				/* coordinatorIndex is odd, so start at 1 */
				startIndex = 1;
			}
			if (startIndex >= nodesInFlow.size()) {
				/* create a partition the single partition */
			}
			var activationPhaseOffset = 0;
			for (Integer i = startIndex; i < nodesInFlow.size(); i+=2) {
				var node = nodesInFlow.get(i);
				/* create a partition consisting of node i */
				String name = flow.getName() + ":" + node.getName();
				Activation partition = getActivation(name, node);
				/* Update phase for each edge in new partition based on 
				 * the current offset, which increases after each
				 * partition created.
				 * */
				partition.increaseEdgePhases(activationPhaseOffset);
				activations.put(name,partition);
				/* increase offset for next partition based on the 
				 * max numTx in this partition/node 
				 * But need to account for phase already assumes
				 * 1 Tx already
				 * */
				activationPhaseOffset += txArray[i]-1;  // adjust by one
			} 
		}
	}
	
	private Integer getMaxTx(Node node) {
		var result = 0;
		for (Edge edge: node.getEdges()) {
			result = Math.max(result, edge.getNumTx());
		}
		return result;
	}

	private Activation getActivation(String name, Node node) {
		Activation newPartition = activations.get(name);
		if (newPartition == null) {
			// no partition by that name exists, so create one
			newPartition = new Activation(name);
			activationMap.put(node,newPartition);
		}
		/* add the node as the coordinator */
		newPartition.addCoordinator(node.getName());
		/* set the conflicts */
		newPartition.setConflicts(node.getConflicts());
		/* add the edges associated with this coordinator */
		newPartition.addEdges(node.getEdges());
		/* set the predecessor as the node's predecessor */
		var nodePred = node.getPredecessor();
		var pred = activationMap.get(nodePred);
		if (nodePred != null && pred == null) {
			/* try next node up the flow */
			nodePred = nodePred.getPredecessor();
			pred = activationMap.get(nodePred);
		}
		newPartition.setPredecessor(pred);
		return newPartition;
	}

	public Integer getHyperPeriod() {
		return workload.getHyperPeriod();
	}

	public Integer getMaxPhase() {
		return workload.getMaxPhase();
	}

	public void orderActivations() {
		var currentTime = 0; // set initial time for building queue
		/* Update the release time and priority of each partition activation */
		for (Activation activation: activations.values()) {
			activation.updatePriorityAndRelease(currentTime);
		}
		/* create a queue sorted first by release times and then priorities */
		priorityQueue = new 
				SchedulableObjectQueue<Activation>(new 
						PriorityComparator<Activation>(),activations.values());
	}

	/**
	 * getNextActivation returns the highest priority
	 * partition that has been released at or before input
	 * parameter time.
	 * 
	 * @param time
	 * @return
	 */
	public Activation getNextActivation() {

		return priorityQueue.poll();
	}

	/**
	 * addElemement adds the partitionElement to the priority queue
	 * 
	 * @param activation
	 */
	public void addActivation(Activation activation) {
		priorityQueue.add(activation);
	}

	/**
	 * findBaseStation is used by algorithms that generate the
	 * partially ordered set of activations using a base station. 
	 * This method searches the set of flows to find the node that
	 * is always one of the src or snk nodes. In the case of a tie
	 * where there are two candidates the initial snk node is
	 * returned. If there are multiple base stations, only the fist
	 * one found is returned.
	 * 
	 * @return baseStation for the workload
	 */
	protected Node findBaseStation() {
		
		Node baseStation = null;
		Iterator<Flow> iterator = flows.iterator();
		Flow flow = iterator.next();
		var nodes = flow.getNodes();
		var srcBaseStation = nodes.get(0);
		var srcBaseStationName = srcBaseStation.getName();
		var snkBaseStation = nodes.get(nodes.size()-1);
		var snkBaseStationName = snkBaseStation.getName(); 
		var found = false;
    
        while (!found && iterator.hasNext()) {
        	flow = iterator.next();
    		nodes = flow.getNodes();
        	var src = nodes.get(0);
			var srcName = src.getName();
			var snk = nodes.get(nodes.size()-1);
			var snkName = snk.getName();
			if (!srcBaseStationName.equals(srcName) && 
					snkBaseStationName.equals(snkName)) {
				baseStation = snkBaseStation;
				found = true;
			} else if (srcBaseStationName.equals(srcName) && 
					!snkBaseStationName.equals(snkName)) {
				baseStation = srcBaseStation;
				found = true;
			} else if (!srcBaseStationName.equals(snkName) &&
					snkBaseStationName.equals(srcName)) {
				baseStation = snkBaseStation;
				found = true;
			} else if (srcBaseStationName.equals(snkName) && 
					!snkBaseStationName.equals(srcName)) {
				baseStation = srcBaseStation;
				found = true;
			}
        }
        if (!found) {
        	/* initial snk and src tie for base station. That is
        	 * each is always a flow src with the other a snk or
        	 * vice versa. 
        	 * So pick initial snk as the base station */
        	baseStation = snkBaseStation;
        }
		return baseStation;
	}

}
