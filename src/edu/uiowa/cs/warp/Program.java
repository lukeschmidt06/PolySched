package edu.uiowa.cs.warp;

import edu.uiowa.cs.utilities.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * @author sgoddard
 *
 */
// Old Scheduler Worked as class Scheduler{} on Feb 17
public class Program implements SystemAttributes {

  private static final String UNKNOWN = "Unknown";
  private static final String SLEEP_INSTRUCTION = "sleep";
  private static final String WAIT_FRAGMENT = "wait(";
  private static final String ELSE_PULL_FRAGMENT = "else pull(";

  // Global flags and data structures
  // FileManager wfm; // class to provide basic WARP file management functions
  WorkLoad workLoad; // WarpScheduler build schedules for flows in WARPflows class
  ProgramSchedule scheduleBuilt;
  ScheduleChoices SchedulerSelected; // set the type of scheduler selected
  String schChoice; // Name of the scheduler selected for output file name
  String schedulerName; // test string of scheduler selected
  Integer nTransmissions;
  Boolean realTimeHARTflag;
  Boolean optimizationRequested;
  Channels channelsAvailable; // channels available for each time slot
  Integer nChannels;
  Boolean verbose;
  Boolean reportLatency;
  private Description deadlineMisses;

  Program(WorkLoad workLoad, Integer nChannels, ScheduleChoices choice, Boolean verbose,
      Boolean reportLatency) {
    setDefaultParameters(workLoad, nChannels, verbose, reportLatency);
    buildProgram(choice);
  }

  Program(WorkLoad workLoad, Integer nChannels, ScheduleChoices choice) {
    setDefaultParameters(workLoad, nChannels, false, false);
    buildProgram(choice);
  }

  private void setDefaultParameters(WorkLoad workLoad, Integer nChannels, Boolean verbose,
      Boolean reportLatency) {
    this.workLoad = workLoad; // flows for which schedules will be built
    this.scheduleBuilt = new ProgramSchedule();
    this.SchedulerSelected = ScheduleChoices.PRIORITY; // set the type of scheduler selected
    workLoad.setFlowsInPriorityOrder();
    this.schedulerName = "Priority";
    this.schChoice = "Priority";
    this.nTransmissions = 0;
    this.realTimeHARTflag = false;
    this.optimizationRequested = true;
    this.nChannels = nChannels;
    this.verbose = verbose;
    this.channelsAvailable = new Channels(nChannels, verbose);
    this.reportLatency = reportLatency;
    this.deadlineMisses = new Description();
  }
  
  /**
   * Returns the Workload object used to build the program
   * 
   * @return WorkLoad object containing a graph, flows, and
   * nodes that this program's schedule was generated for.
   */
  public WorkLoad toWorkLoad() {
    return workLoad;
  }

  public void buildProgram(ScheduleChoices choice) {
    /*
     * Switch on the scheduler choice. If it is POSET-based scheduler, create the POSET that matches
     * the name and then use the newer schedule object to convert the POSET to a program. If it is
     * an original scheduler choice, build set the scheduler type and build the schedule and WARP
     * program by calling buildOriginalProgram();
     */
    switch (choice) { // select the requested scheduler
      case WARP_POSET_PRIORITY: // fall through
      case WARP_POSET_RM: // fall through
      case WARP_POSET_DM: // fall through
        setScheduleSelected(choice);
        var poset1 = new WarpPoset(workLoad);
        var schedule1 = new NonPreemptiveSchedule(poset1, this.nChannels);
        var newProgram = schedule1.toProgram();
        setSchedule(newProgram); // store the schedule built
        break;
      case CONNECTIVITY_POSET_PRIORITY: // fall through
      case CONNECTIVITY_POSET_RM: // fall through
      case CONNECTIVITY_POSET_DM: // fall through
      case POSET_PRIORITY: // fall through
      case POSET_RM: // fall through
      case POSET_DM:
        setScheduleSelected(choice);
        // var poset = new BasicPoset(workLoad);
        var poset2 = new ConnectivityPoset(workLoad);
        var schedule2 = new NonPreemptiveSchedule(poset2, this.nChannels);
        newProgram = schedule2.toProgram();
        setSchedule(newProgram); // store the schedule built
        break;
      //// case CONNECTIVITY_POSET_PREEMPTIVE_PRIORITY: // fall through
      //// case CONNECTIVITY_POSET_PREEMPTIVE_RM: // fall through
      //// case CONNECTIVITY_POSET_PREEMPTIVE_DM: // fall through
      // var poset3 = new ConnectivityPoset(workLoad);
      // var schedule3 = new PreemptiveSchedule(poset3, this.nChannels);
      // newProgram = schedule3.toProgram();
      // setSchedule(newProgram); // store the schedule built
      // break;
      case PRIORITY:
        selectPriority();
        buildOriginalProgram(); // build the requested schedule
        break;
      case RM:
        selectRM();
        buildOriginalProgram(); // build the requested schedule
        break;
      case DM:
        selectDM();
        buildOriginalProgram(); // build the requested schedule
        break;
      case RTHART:
        selectRtHART();
        buildOriginalProgram(); // build the requested schedule
        break;
      default:
        selectPriority();
        buildOriginalProgram(); // build the requested schedule
        break; // break from switch
    }
  }

  public void buildOriginalProgram() { // builds a Priority schedule

    if (verbose) {
      var scheduleDetails =
          String.format("\nSystem schedule for graph %s created with the following parameters:\n",
              workLoad.getName());
      scheduleDetails =
          String.format(scheduleDetails + "\tScheduler Name:\t%s\n", getSchedulerName());
      scheduleDetails = String.format(scheduleDetails + "\tM:\t%s\n",
          String.valueOf(workLoad.getMinPacketReceptionRate()));
      scheduleDetails =
          String.format(scheduleDetails + "\tE2E:\t%s\n", String.valueOf(workLoad.getE2e()));
      scheduleDetails = String.format(scheduleDetails + "\tnChannels:\t%d\n", getNumChannels());
      // The following parameters are only output when special schedules are requested
      if (getNumTransmissions() > 0) { // only specify when NumTransmissions is fixed
        scheduleDetails =
            String.format(scheduleDetails + "\tnTransmissions:\t%d\n", getNumTransmissions());
      }
      if (getOptimizationFlag()) { // only specify when optimization not requested
        scheduleDetails += "\tOptimization Requested:\tFalse\n";
      }
      System.out.println(scheduleDetails);
    }
    var nTx = 0;// support nTx per transmission, which is set based on input parameter if
                // nTransmissions == n 0 else flow.nTXperLink
    var nTransmissions = getNumTransmissions();
    Boolean realtimeHART = getRealTimeHartFlag();
    var optimizationRequested = getOptimizationFlag();
    var schedule = new ProgramSchedule(); // create an empty schedule
    var newInstruction = SLEEP_INSTRUCTION; // initialize the new Instruction with a sleep
    var prioritizedFlows = workLoad.getFlowNamesInPriorityOrder();
    // var orderedNodes = workLoad.getNodeNamesOrderedAlphabetically(); // create an array of node
    // names
    // sorted alphabetically
    var nodeIndex = getNodeMapIndex(); // get the mapping from node names to index in schedule
    var nNodes = nodeIndex.size();
    /*
     * for (int index = 0; index < nNodes; index++) { // set up the node to index mapping var name =
     * orderedNodes[index]; nodeIndex.put(name, index); // add name, index mapping to NodeIndex map
     * }
     */
    var hyperPeriod = workLoad.getHyperPeriod();
    for (int i = 0; i < hyperPeriod; i++) { // This loop makes sure the schedule is full up to the
                                            // period and the channels entries are all initialized
      var newInstructionTimeSlot = new InstructionTimeSlot(nNodes, SLEEP_INSTRUCTION); // create a
                                                                                       // time slot
                                                                                       // for
                                                                                       // nNodes,
                                                                                       // each
                                                                                       // initialized
                                                                                       // with a
                                                                                       // SLEEP_INSTRUCTION
      schedule.add(newInstructionTimeSlot);
      channelsAvailable.addNewChannelSet();// initially, all channels are available in each time
                                           // slot
    }
    // create an instance of the Warp DSL class for parsing instructions
    var dsl = new WarpDSL();

    for (String flowName : prioritizedFlows) { // loop through all of the nodes in priority order
      var nodesInFlow = workLoad.getNodesInFlow(flowName);
      var nNodesInFlow = nodesInFlow.length;
      var e2eFlag = false; // default is to not use e2e to achieve E2E
      if (nTransmissions == 0) { // use nTx from flow.nTXperLink for realtimeHart and the linkTX
                                 // array otherwise
        e2eFlag = true; // need to meet e2e to hit E2E for flow
        if (realtimeHART) {
          nTx = workLoad.getFlowTxAttemptsPerLink(flowName);
        } // else we will computer nTx from the linkTx array for the flow as we go

      } else {
        nTx = nTransmissions;
      }
      var snk = "";
      var linkTxCosts = workLoad.getNumTxAttemptsPerLink(flowName);
      if (verbose) {
        var totalCost = workLoad.getTotalTxAttemptsInFlow(flowName);
        var minCost = nNodesInFlow - 1; // nEdges in the flow is the minimum communication cost,
                                        // i.e., min nTx to go E2E with no errors
        System.out.printf(
            "Flow %s has a minimum communication cost of %d and a worst-case communication cost of %d\n",
            flowName, minCost, totalCost);
      }
      if (nTx == 0) { // this is true when !realtimeHART and nTransmssions wasn't specified => nTx
                      // is dependent on reliability target
        nTx = linkTxCosts[0]; // set nTx to the nTx for the first link in the flow
      }
      var previousNodeInstruction = new Integer[nTx]; // set instruction location in schedule for
                                                      // previous slots to 0
      Arrays.fill(previousNodeInstruction, 0); // initialize locations to 0
      var flowPhase = workLoad.getFlowPhase(flowName); // used to compute latency after done
                                                       // scheduling this flow
      var flowLastInstruction = flowPhase; // used to compute latency after done scheduling this
                                           // flow
      var flowDeadline = workLoad.getFlowDeadline(flowName); // used to check schedulability for
                                                             // this flow
      var flowPeriod = workLoad.getFlowPeriod(flowName);
      var FlowSrcInstructionTimeSlot = flowPhase; // initialize the time at which the flow's src
                                                  // node can first transmit
      for (int instance = 0; instance < hyperPeriod / flowPeriod; instance++) {
        for (int flowNodeIndex = 0; flowNodeIndex < nNodesInFlow - 1; flowNodeIndex++) { // don't
                                                                                         // push for
                                                                                         // last
                                                                                         // node, so
                                                                                         // stop at
                                                                                         // node
                                                                                         // n-1,
                                                                                         // which is
                                                                                         // 2 less
                                                                                         // than
                                                                                         // count
          var currentNodeName = nodesInFlow[flowNodeIndex]; // get name of current node in the flow
                                                            // (aka source of data)
          if (e2eFlag) {
            nTx = linkTxCosts[flowNodeIndex]; // set nTx for this link
          }
          var currentNodeInstruction = new Integer[nTx]; // store instruction location in schedule
                                                         // for previous node
          Arrays.fill(currentNodeInstruction, 0); // initialize the currentNondeInstruction
          for (int index = 0; index < nTx; index++) { // repeat instruction nTx times so that this
                                                      // flow meets desired reliability
            var instructionNodeName = currentNodeName; // name of node for which we write an
                                                       // instruction in its program
            var priorSrcInstruction = UNKNOWN; // variable used to store and possibly update the
                                               // priorSrcInstruction during optimization
            var priorSrcUpdated = false; // flag to indicate if priorSrcInstruction is updated or
                                         // not
            snk = nodesInFlow[flowNodeIndex + 1]; // should get a valid sink node, set name to
                                                  // UNKNOWN if we don't
            // when not optimizing instructionNodeName is current node, but we might update a
            // different node's instruction to optimize
            var phase = instance * flowPeriod + flowPhase; // update phase for the instance of the
                                                           // flow release, which happens once every
                                                           // period in the hyperPeriod
            var firstPossibleSlot = Math.max(phase, FlowSrcInstructionTimeSlot); // make sure
                                                                                 // precedence
                                                                                 // constraints on
                                                                                 // flow are
                                                                                 // preserved
            Integer instructionIndex = findNextAvailableInstructionTimeSlot(schedule,
                firstPossibleSlot, flowNodeIndex, index, nTx, previousNodeInstruction,
                currentNodeInstruction, SLEEP_INSTRUCTION, realtimeHART, optimizationRequested,
                nodeIndex.get(currentNodeName), nodeIndex.get(snk));
            if (instructionIndex >= schedule.size()) { // instructionIndex is beyond current
                                                       // schedule length. Increase schedule and
                                                       // channelsAvailable tables
              for (int i = schedule.size(); i <= instructionIndex; i++) {
                var newInstructionTimeSlot = new InstructionTimeSlot(nNodes, SLEEP_INSTRUCTION); // create
                                                                                                 // a
                                                                                                 // time
                                                                                                 // slot
                                                                                                 // for
                                                                                                 // nNodes,
                                                                                                 // each
                                                                                                 // initialized
                                                                                                 // with
                                                                                                 // a
                                                                                                 // SLEEP_INSTRUCTION
                schedule.add(newInstructionTimeSlot);
                channelsAvailable.addNewChannelSet(); // need make sure we have channels available
                                                      // for all slots. instructionIndex may be >
                                                      // initialized size of array because of
                                                      // phases...
              }
            }
            String channel = findNextAvailableChannel(schedule, instructionNodeName,
                instructionIndex, nodeIndex.get(currentNodeName), nodeIndex.get(snk));

            // Now check if we have a valid channel. If not we need to find a new time slot further
            // down the schedule.
            // So, initialize the search to start at the current slot and then increase by one each
            // time we loop if the current slot
            // Now check if we have a valid channel. If not we need to find a new time slot further
            // down the schedule.
            // So, initialize the search to start at the current slot and then increase by one each
            // time we loop if the current slot
            while (channel.equals(UNKNOWN)) { // findNextAvailableChannel will return UNKNOWN if no
                                              // channel was available. In that case, we need to
                                              // find a new slot for this instruction after the last
                                              // index found
              var startSearchIndex = instructionIndex + 1;
              instructionIndex = findNextAvailableInstructionTimeSlot(schedule, startSearchIndex,
                  flowNodeIndex, index, nTx, previousNodeInstruction, currentNodeInstruction,
                  SLEEP_INSTRUCTION, realtimeHART, optimizationRequested,
                  nodeIndex.get(currentNodeName), nodeIndex.get(snk));
              if (instructionIndex >= schedule.size()) { // instructionIndex is beyond current
                                                         // schedule lenght. Increase schedule and
                                                         // channelsAvailable tables
                for (int i = schedule.size(); i <= instructionIndex; i++) {
                  var newInstructionTimeSlot = new InstructionTimeSlot(nNodes, SLEEP_INSTRUCTION); // create
                                                                                                   // a
                                                                                                   // time
                                                                                                   // slot
                                                                                                   // for
                                                                                                   // nNodes,
                                                                                                   // each
                                                                                                   // initialized
                                                                                                   // with
                                                                                                   // a
                                                                                                   // SLEEP_INSTRUCTION
                  schedule.add(newInstructionTimeSlot);
                  channelsAvailable.addNewChannelSet(); // need make sure we have channels available
                                                        // for all slots. instructionIndex may be >
                                                        // initialized size of array because of
                                                        // phases...
                }
              }
              channel = findNextAvailableChannel(schedule, instructionNodeName, instructionIndex,
                  nodeIndex.get(currentNodeName), nodeIndex.get(snk));
            }
            workLoad.setNodeChannel(instructionNodeName, Integer.valueOf(channel));
            if (realtimeHART) {
              newInstruction = pushInstruction(flowName, currentNodeName, snk, channel);
            } else {
              newInstruction = hasPushInstruction(flowName, currentNodeName, snk, channel);
            }

            // if optimization flag is set, look to see if any optimizations are possible
            if (optimizationRequested && instructionIndex > 0) {
              var priorInstructionTimeSlotArrayList = schedule.get(instructionIndex - 1);
              var priorInstructionTimeSlot = priorInstructionTimeSlotArrayList
                  .toArray(new String[priorInstructionTimeSlotArrayList.size()]);
              var priorInstruction = priorInstructionTimeSlot[nodeIndex.get(snk)]; // get
                                                                                   // instruction
                                                                                   // scheduled for
                                                                                   // snk to execute
                                                                                   // in the prior
                                                                                   // time slot
              if (index == (nTx - 1)) {
                String hasSubstring = String.format("if has(%s", flowName);
                String elseSubString = "else pull(";
                if (priorInstruction.contains(hasSubstring)
                    && !priorInstruction.contains(elseSubString) && !realtimeHART) {
                  // The sink node is not waiting, so add this instruction as an else to it
                  // Also need to make sure we haven't already combined a prior instruction by
                  // moving it to the else clause. May need a smater way to do this later...
                  // The combined instructions both use channels for their respective time slots!
                  // The else is being added to the prior time slot, so we need to
                  // get a new channel for that time slot and put back the channel we extraced from
                  // this time slot. And of course, clean up if any errors occur
                  // in the process
                  var instructionParametersArrayList =
                      dsl.getInstructionParameters(priorInstruction);
                  var priorInstructionChannel = UNKNOWN;
                  if (instructionParametersArrayList.size() == 1) {
                    var instructionParameters = instructionParametersArrayList.get(0); // get a copy
                                                                                       // of the
                                                                                       // parameters
                    priorInstructionChannel = instructionParameters.getChannel();
                  } else { // we just checked above that the prior instruction did not have an else
                           // pull, wait, or sleep. So, there should be only one instruction here
                    String msg = String.format(
                        "ERROR: More than one instruction in prior instruction string: %s",
                        priorInstruction);
                    System.err.println(msg);
                  }
                  workLoad.setNodeChannel(instructionNodeName,
                      Integer.valueOf(priorInstructionChannel));
                  channelsAvailable.addChannel(instructionIndex, channel); // return channel for
                                                                           // this slot
                  channel = priorInstructionChannel; // update this so that the corresponding wait
                                                     // uses this channel
                  newInstruction = priorInstruction + String.format("else pull(%s: %s -> %s, #%s)",
                      flowName, currentNodeName, snk, priorInstructionChannel); // was channel
                  instructionIndex -= 1; // set instructionIndex to the priorInstruction index so we
                                         // replace that slot with the new, optimized instruction
                  instructionNodeName = snk; // change currntNodeName to the snk node, which is the
                                             // node name of the prior instruction
                }
              } else if ((flowNodeIndex == 0 && index == 0 && e2eFlag)
                  && !(priorInstruction.contains(SLEEP_INSTRUCTION)
                      || priorInstruction.contains(WAIT_FRAGMENT))) {
                // We hit this case when combining the last push of one flow with the first push of
                // a new flow--making an if has (Fi) push(Fi) else pull pull(Fi+1)
                // print ("Skipping optimization of combining \(priorInstruction) with
                // \(newInstruction)\n")
                // May want to add code here to combine when e2eFlag is true but need to compute new
                // reliability values given less than 1 probablity that
                // this first push/now pull will happen
                // System.err.printf("New instruction is: %s\nPrior instruction is %s\n",
                // newInstruction, priorInstruction);
              } else { // not sure when we hit this when combining a sleep or wait() from prior flow
                       // with a new instruction
                // Anywhere else? :-) Probably better figure it out!!
                if (!priorInstruction.contains(ELSE_PULL_FRAGMENT)
                    && !priorInstruction.contains(WAIT_FRAGMENT)
                    && !priorInstruction.contains(SLEEP_INSTRUCTION) && !realtimeHART) { // The sink
                                                                                         // node is
                                                                                         // not
                                                                                         // waiting,
                                                                                         // so add
                                                                                         // this
                                                                                         // instruction
                                                                                         // as an
                                                                                         // else to
                                                                                         // it
                  // Also need to make sure we haven't already combined a prior instruction by
                  // moving it to the else clause. May need a smarter way to do this later...
                  var instructionParametersArrayList =
                      dsl.getInstructionParameters(priorInstruction);
                  var priorInstructionChannel = UNKNOWN;
                  var priorInstructionSnk = UNKNOWN;
                  if (instructionParametersArrayList.size() == 1) {
                    var instructionParameters = instructionParametersArrayList.get(0); // get a copy
                                                                                       // of the
                                                                                       // parameters
                    priorInstructionSnk = instructionParameters.getSnk();
                    priorInstructionChannel = instructionParameters.getChannel();
                  } else { // we just checked above that the prior instruction did not have an else
                           // pull, wait, or sleep. So, there should be only one instruction here
                    String msg = String.format(
                        "ERROR: More than one instruction in prior instruction string: %s",
                        priorInstruction);
                    System.err.println(msg);
                  }
                  priorSrcInstruction = priorInstructionTimeSlot[nodeIndex.get(currentNodeName)]; // get
                                                                                                  // instruction
                                                                                                  // scheduled
                                                                                                  // for
                                                                                                  // snk/curentNodeName)
                                                                                                  // to
                                                                                                  // execute
                                                                                                  // in
                                                                                                  // the
                                                                                                  // prior
                                                                                                  // time
                  if (priorInstructionSnk.equals(currentNodeName)) { // will try to push and pull
                                                                     // to/from the same node, so
                                                                     // use the prior instruction
                                                                     // channel
                    newInstruction = priorInstruction
                        + elsePullClause(flowName, currentNodeName, snk, priorInstructionChannel);
                    instructionIndex -= 1; // set instructionIndex to the priorInstruction index so
                                           // we replace that slot with the new, optimized
                                           // instruction
                    instructionNodeName = snk; // change currntNodeName to the snk node, which is
                                               // the node name of the prior instruction
                  } else if (priorSrcInstruction.contains(SLEEP_INSTRUCTION)) { // was
                                                                                // !priorSrcInstruction.contains("wait(");
                                                                                // I think it needs
                                                                                // to be a sleep and
                                                                                // then we can add a
                                                                                // wait there
                    // Make sure the currentNodeName entry for this time slot has a sleep. If so,
                    // then we combine instructions with new as an else push and add wait at
                    // currentNodeName
                    workLoad.setNodeChannel(instructionNodeName,
                        Integer.valueOf(priorInstructionChannel));
                    newInstruction = priorInstruction
                        + elsePullClause(flowName, currentNodeName, snk, priorInstructionChannel); // was
                                                                                                   // channel
                    // need to change priorSrcInstruction from sleep to wait on channel
                    priorSrcInstruction = waitInstruction(priorInstructionChannel); // was channel
                    priorSrcUpdated = true; // set flag so this instruction is updated when we
                                            // insert currenInstruction into the
                                            // currentInstructionTimeSlot
                    instructionIndex -= 1; // set instructionIndex to the priorInstruction index so
                                           // we replace that slot with the new, optimized
                                           // instruction
                    instructionNodeName = snk; // change currntNodeName to the snk node, which is
                                               // the node name of the prior instruction
                  }
                }
              }
            }
            ArrayList<String> currentInstructionTimeSlotArrayList; // =
                                                                   // schedule.get(instructionIndex);
            String[] currentInstructionTimeSlot; // =
                                                 // currentInstructionTimeSlotArrayList.toArray(new
                                                 // String[currentInstructionTimeSlotArrayList.size()]);
            currentNodeInstruction[index] = instructionIndex; // store the slot index for this
                                                              // instruction
            if (instructionIndex < schedule.size()) {
              currentInstructionTimeSlotArrayList = schedule.get(instructionIndex); // load current
                                                                                    // instruction,
                                                                                    // in case
                                                                                    // another
                                                                                    // instruction
                                                                                    // is in this
                                                                                    // time slot for
                                                                                    // another node
                                                                                    // that doesn't
                                                                                    // conflict
                                                                                    // channels
              currentInstructionTimeSlot = currentInstructionTimeSlotArrayList
                  .toArray(new String[currentInstructionTimeSlotArrayList.size()]);
            } else {
              currentInstructionTimeSlotArrayList =
                  new InstructionTimeSlot(nNodes, SLEEP_INSTRUCTION);
              currentInstructionTimeSlot = currentInstructionTimeSlotArrayList
                  .toArray(new String[currentInstructionTimeSlotArrayList.size()]);
            }
            currentInstructionTimeSlot[nodeIndex.get(instructionNodeName)] = newInstruction; // an
                                                                                             // error
                                                                                             // finding
                                                                                             // the
                                                                                             // right
                                                                                             // location
                                                                                             // will
                                                                                             // result
                                                                                             // in
                                                                                             // location
                                                                                             // of
                                                                                             // snk
                                                                                             // or 0
            if (priorSrcUpdated) {
              currentInstructionTimeSlot[nodeIndex.get(currentNodeName)] = priorSrcInstruction; // an
                                                                                                // error
                                                                                                // finding
                                                                                                // the
                                                                                                // right
                                                                                                // location
                                                                                                // will
                                                                                                // result
                                                                                                // in
                                                                                                // location
                                                                                                // of
                                                                                                // snk
                                                                                                // or
                                                                                                // 0
            }
            // If this is a basic push() then the snk node will have the initial wait instruction in
            // its time slot. Thus, need to add the channel # to that wait
            var snkInstruction = currentInstructionTimeSlot[nodeIndex.get(snk)]; // get instruction
                                                                                 // scheduled for
                                                                                 // snk to execute
                                                                                 // in the current
                                                                                 // time slot
            if (snkInstruction.equals(SLEEP_INSTRUCTION)) {
              currentInstructionTimeSlot[nodeIndex.get(snk)] = waitInstruction(channel);
            }
            if (flowNodeIndex > 0) {
              var priorNodeName = nodesInFlow[flowNodeIndex - 1];// get name of prior node in the
                                                                 // flow (aka predecessor)
              var possibleCombineInstruction =
                  String.format("if has(%1$s) push(%1$s: %2$s -> %3$s, #", flowName, priorNodeName,
                      currentNodeName);
              if (optimizationRequested && !realtimeHART && nTx > 1) { // One more optimization
                                                                       // possible: combine two
                                                                       // conditional pushes at the
                                                                       // same slot involving a
                                                                       // common node
                var upstreamNodeInstruction =
                    currentInstructionTimeSlot[nodeIndex.get(priorNodeName)];
                var instr = hasPushInstruction(flowName, currentNodeName, snk, channel);
                if (newInstruction.equals(instr)
                    && upstreamNodeInstruction.contains(possibleCombineInstruction)
                    && !upstreamNodeInstruction.contains("else")) {
                  var instructionParametersArrayList =
                      dsl.getInstructionParameters(upstreamNodeInstruction);
                  var upstreamNodeChannel = UNKNOWN;
                  if (instructionParametersArrayList.size() == 1) {
                    var instructionParameters = instructionParametersArrayList.get(0); // get a copy
                                                                                       // of the
                                                                                       // paramaters
                    upstreamNodeChannel = instructionParameters.getChannel();
                    // return upstreamNodeChannel to the available channels for this time slot
                    channelsAvailable.addChannel(instructionIndex, upstreamNodeChannel);
                  } else { // we just checked above that the prior instruction did not have an else
                           // pull, wait, or sleep. So, there should be only one instruction here
                    String msg = String.format(
                        "ERROR: More than one instruction in upstream node  instruction string: %s",
                        upstreamNodeInstruction);
                    System.err.println(msg);
                  }
                  // use the newInstruction channel in upstream instruction that is being combined
                  // and return the upstream channel to the available channel set
                  newInstruction = newInstruction
                      + elsePullClause(flowName, priorNodeName, currentNodeName, channel); // was
                                                                                           // upstreamNodeChannel
                  currentInstructionTimeSlot[nodeIndex.get(instructionNodeName)] = newInstruction; // an
                                                                                                   // error
                                                                                                   // finding
                                                                                                   // the
                                                                                                   // right
                                                                                                   // location
                                                                                                   // will
                                                                                                   // result
                                                                                                   // in
                                                                                                   // location
                                                                                                   // of
                                                                                                   // snk
                                                                                                   // or
                                                                                                   // 0
                  currentInstructionTimeSlot[nodeIndex.get(priorNodeName)] =
                      waitInstruction(channel);; // an error finding the right location will result
                                                 // in location of snk or 0 // was
                                                 // upstreamNodeChannel
                } else if (newInstruction
                    .contains(elsePullClause(flowName, priorNodeName, currentNodeName, channel))
                    && currentInstructionTimeSlot[nodeIndex.get(priorNodeName)]
                        .contains(possibleCombineInstruction)) {
                  currentInstructionTimeSlot[nodeIndex.get(priorNodeName)] =
                      waitInstruction(channel); // Replace combined instruction in prior node
                                                // program with wait()
                }
              }
              if (!realtimeHART && nTx > 1) { // WARP schedule with multiple tx per link requested,
                                              // so add 'else wait()' for down upstream node when
                                              // needed
                var upstreamNodeInstruction =
                    currentInstructionTimeSlot[nodeIndex.get(priorNodeName)];
                if (newInstruction
                    .equals(hasPushInstruction(flowName, currentNodeName, snk, channel))
                    && upstreamNodeInstruction.contains(possibleCombineInstruction)) {
                  String upstreamNodeChannel =
                      getFirstChannelInInstruction(upstreamNodeInstruction);
                  newInstruction = newInstruction + elseWaitInstruction(upstreamNodeChannel); // this
                                                                                              // node
                                                                                              // needs
                                                                                              // to
                                                                                              // wait
                                                                                              // for
                                                                                              // upstream
                                                                                              // node
                                                                                              // if
                                                                                              // message
                                                                                              // hasn't
                                                                                              // arrived
                  currentInstructionTimeSlot[nodeIndex.get(instructionNodeName)] = newInstruction; // an
                                                                                                   // error
                                                                                                   // finding
                                                                                                   // the
                                                                                                   // right
                                                                                                   // location
                                                                                                   // will
                                                                                                   // result
                                                                                                   // in
                                                                                                   // location
                                                                                                   // of
                                                                                                   // snk
                                                                                                   // or
                                                                                                   // 0
                  // Don't replace the push instruction in the upstream node in this case, because
                  // we added the else in this nodes time slot instead
                }
              }
            }
            var timeSlot = new InstructionTimeSlot(currentInstructionTimeSlot);
            if (instructionIndex < schedule.size()) { // This should always be the case, but check
                                                      // anyway
              schedule.set(instructionIndex, timeSlot);
            } else {
              schedule.add(timeSlot);
              String msg = String.format(
                  "ERROR: instructionIndex = %d > schedule length = %d. Appended instruction to avoid crash or throw...",
                  instructionIndex, schedule.size());
              System.err.println(msg);
              System.err.println(
                  "\tProbably due to a flow having a phase > 0. Need to addjust schedule to have one hyperperiod as a startup period in this case");
            }
            flowLastInstruction = instructionIndex;
            if (index == 0) {
              FlowSrcInstructionTimeSlot = instructionIndex; // Store the time slot in which the
                                                             // flow's src node first transmits
            }
          }
          previousNodeInstruction = currentNodeInstruction; // copy current to previous node...don't
                                                            // worry about clearing
                                                            // currentNodeInstrucitons; will be set
                                                            // in loop
        }
        Integer latency = flowLastInstruction - (flowPhase + (instance * flowPeriod)) + 1;
        String latencyMsg =
            String.format("Maximum latency for %s:%d is %d\n", flowName, instance, latency);
        if (verbose) {
          System.out.printf(latencyMsg);
        }
        if (latency > flowDeadline) {
          String deadlineMsg = String.format(
              "WARNING: This workload is not schedulable: Flow %s:%d latency %d > deadline %d\n",
              flowName, instance, latency, flowDeadline);
          deadlineMisses.add(deadlineMsg);
          // System.out.printf(deadlineMsg);
        }
      }
    }
    setSchedule(schedule); // store the schedule built
  }

  private String waitInstruction(String channel) {
    var size = channel.length();
    if (!Utilities.isInteger(channel)) {
      System.out.println("channel length is " + String.valueOf(size));
      System.err.println("ERROR: channel is not an Integer: " + channel);
    }
    return String.format("wait(#%s)", channel);
  }


  private String elseWaitInstruction(String channel) {
    var size = channel.length();
    if (!Utilities.isInteger(channel)) {
      System.err.println("ERROR: channel is not an Integer: " + channel);
      System.err.println("\t channel size is not an Integer: " + String.valueOf(size));
    }
    return String.format(" else wait(#%s)", channel);
  }

  private String elsePullClause(String flow, String src, String snk, String channel) {
    return String.format(" else pull(%s: %s -> %s, #%s)", flow, src, snk, channel);
  }

  private String hasPushInstruction(String flow, String src, String snk, String channel) {
    return String.format("if has(%1$s) push(%1$s: %2$s -> %3$s, #%4$s)", flow, src, snk, channel);
  }

  private String pushInstruction(String flow, String src, String snk, String channel) {
    return String.format("push(%1$s: %2$s -> %3$s, #%4$s)", flow, src, snk, channel);
  }

  private String getFirstChannelInInstruction(String Instruction) {
    var beginIndex = Instruction.indexOf('#') + 1; // get index of the start of the channel #
    var endIndex = Instruction.indexOf(')', beginIndex); // get index of the last character of the
                                                         // channel #
    var channel = Instruction.substring(beginIndex, endIndex); // this substring has the 1st channel
    return channel;
  }

  private Integer findNextAvailableInstructionTimeSlot(ProgramSchedule schedule,
      Integer startLocation, Integer nodeInFlow, Integer transIndex, Integer nTx,
      Integer[] previousNodeInstruction, Integer[] currentNodeInstruction, String sleepInstruction,
      Boolean realtimeHART, Boolean optimizationRequested, Integer srcNodeIndex,
      Integer snkNodeIndex) {
    var currentTime = startLocation; // Make sure we don't start looking before the starting
                                     // location
    if (transIndex > 0) {
      currentTime = Math.max(startLocation, currentNodeInstruction[transIndex - 1]); // Make sure we
                                                                                     // don't start
                                                                                     // looking
                                                                                     // before the
                                                                                     // startLocation
                                                                                     // AND the
                                                                                     // previous
                                                                                     // instruction
                                                                                     // we inserted
    }
    var done = false; // Flag indicating time slot search is done
    if (realtimeHART || (nTx == 1 || nodeInFlow == 0 || transIndex >= nTx - 1)) {
      // do the same thing for all nodes when nTx == 1 as for first node of flow when nTx == 1 and
      // for kth retry
      while (currentTime < schedule.size() && !done) {
        var currentInstructionTimeSlot = schedule.get(currentTime);
        if (slotIsAvailable(currentInstructionTimeSlot, srcNodeIndex, snkNodeIndex)) {
          done = true;
        } else {
          currentTime += 1;
        }
      }
    } else { // !reatimeHART and (nTx > 1 and nodeInFlow > 0)
      Integer slotOffsetFromLastPreviousInstructionEntry;
      if (transIndex >= (previousNodeInstruction.length - 1)) {
        slotOffsetFromLastPreviousInstructionEntry =
            transIndex - (previousNodeInstruction.length - 1) + 1;
        var possibleInstructionTimeSlotIndex =
            previousNodeInstruction[previousNodeInstruction.length - 1]
                + slotOffsetFromLastPreviousInstructionEntry; // same as previous node's next retry
                                                              // entry
        // or continuous past it if more attempts for this instruction
        if (startLocation < possibleInstructionTimeSlotIndex) { // make sure this time isn't earlier
                                                                // than the starting location
          // (which can also mean the start time for searching available slots due to channel
          // unavailability)
          currentTime = possibleInstructionTimeSlotIndex; // OK to update to this time slot index
        }
        done = false; // However, we need to check if the current slot is available, and if not keep
                      // searching until we find one. This happens with preemptions
        if (!optimizationRequested) {
          while (currentTime < schedule.size() && !done) {
            var currentInstructionTimeSlot = schedule.get(currentTime);
            if (slotIsAvailable(currentInstructionTimeSlot, srcNodeIndex, snkNodeIndex)) { // get
                                                                                           // the
                                                                                           // proposed
                                                                                           // currentInstructionTimeSlot.
                                                                                           // If
                                                                                           // available,
                                                                                           // done
                                                                                           // is
                                                                                           // true,
                                                                                           // else
                                                                                           // keep
                                                                                           // checking
                                                                                           // for a
                                                                                           // time
              done = true;
            } else {
              currentTime += 1;
            }
          }
        } else {
          // do something to support the optimization
          while (currentTime < schedule.size() && !done) {
            var currentInstructionTimeSlot = schedule.get(currentTime);
            if (slotIsAvailable(currentInstructionTimeSlot, srcNodeIndex, snkNodeIndex)) { // get
                                                                                           // the
                                                                                           // proposed
                                                                                           // currentInstructionTimeSlot.
                                                                                           // If
                                                                                           // available,
                                                                                           // done
                                                                                           // is
                                                                                           // true,
                                                                                           // else
                                                                                           // keep
                                                                                           // checking
                                                                                           // for a
                                                                                           // time
              done = true;
            } else {
              currentTime += 1;
            }
          }
        }
      } else {
        var possibleInstructionTimeSlotIndex = previousNodeInstruction[transIndex + 1]; // same as
                                                                                        // previous
                                                                                        // node's
                                                                                        // next
                                                                                        // retry
                                                                                        // entry
        if (startLocation <= possibleInstructionTimeSlotIndex) { // make sure this time isn't
                                                                 // earlier than the starting
                                                                 // location
          // (which can also mean the start time for searching available slots due to channel
          // unavailability)
          currentTime = possibleInstructionTimeSlotIndex; //
        }
        done = false; // However, we need to check if the current slot is available, and if not keep
                      // searching until we find one. This happens with preemptions
        if (!optimizationRequested) {
          while (currentTime < schedule.size() && !done) {
            var currentInstructionTimeSlot = schedule.get(currentTime);
            if (slotIsAvailable(currentInstructionTimeSlot, srcNodeIndex, snkNodeIndex)) { // get
                                                                                           // the
                                                                                           // proposed
                                                                                           // currentInstructionTimeSlot.
                                                                                           // If
                                                                                           // available,
                                                                                           // done
                                                                                           // is
                                                                                           // true,
                                                                                           // else
                                                                                           // keep
                                                                                           // checking
                                                                                           // for a
                                                                                           // time
              done = true;
            } else {
              currentTime += 1;
            }
          }
        } else {
          // do something to support the optimization
          if (currentTime < schedule.size()) { // First make sure we haven't exceeded the current
                                               // schedule table. If so, no need to optimize.
            // Just return current time and let caller figure out what to do.
            var tmpInstructionTimeSlot = schedule.get(currentTime);
            var srcInstruction = tmpInstructionTimeSlot.get(srcNodeIndex);
            var snkInstruction = tmpInstructionTimeSlot.get(snkNodeIndex);
            if (!srcInstruction.contains("wait(") || !snkInstruction.contains("sleep")) { // instruction
                                                                                          // should
                                                                                          // contain
                                                                                          // a wait,
                                                                                          // so we
                                                                                          // can use
                                                                                          // it
                                                                                          // during
                                                                                          // optimization.
                                                                                          // Only a
                                                                                          // wait
                                                                                          // for
                                                                                          // this
                                                                                          // time
                                                                                          // slot is
                                                                                          // expected
                                                                                          // at this
                                                                                          // point
              // we also need to be sure the snk is sleeping
              // If either condition is not true, then we need to find another time slot
              while (currentTime < schedule.size() && !done) {
                var currentInstructionTimeSlot = schedule.get(currentTime);
                if (slotIsAvailable(currentInstructionTimeSlot, srcNodeIndex, snkNodeIndex)) { // get
                                                                                               // the
                                                                                               // proposed
                                                                                               // currentInstructionTimeSlot.
                                                                                               // If
                                                                                               // available,
                                                                                               // done
                                                                                               // is
                                                                                               // true,
                                                                                               // else
                                                                                               // keep
                                                                                               // checking
                                                                                               // for
                                                                                               // a
                                                                                               // time
                  done = true;
                } else {
                  currentTime += 1;
                }
              }
            } else if (srcInstruction.contains("else(")) { // This should never be the case, but not
                                                           // sure if it is bad...print an error
                                                           // indicating the issue
              var errorString = String.format(
                  "POSSIBLE ERROR, but not sure...   At time slot %d of the schedule, ",
                  currentTime);
              errorString +=
                  "instruction for the src node contains\n   an unexpected 'else wait' instruction:";
              errorString += srcInstruction;
              System.err.println(errorString);
            }
          }
        }
      }
    }
    return currentTime;
  }

  private Boolean slotIsAvailable(InstructionTimeSlot currentInstructionTimeSlot,
      Integer srcNodeIndex, Integer snkNodeIndex) {
    var vacantSlot = false; // assume slot is not vacant ----// Flag indicating time slot search is
                            // done
    if (SLEEP_INSTRUCTION.equals(currentInstructionTimeSlot.get(srcNodeIndex))
        && SLEEP_INSTRUCTION.equals(currentInstructionTimeSlot.get(snkNodeIndex))) { // src and snk
                                                                                     // are both
                                                                                     // sleeping, so
                                                                                     // slot is
                                                                                     // available
      vacantSlot = true;
    }
    return vacantSlot;
  }

  private String findNextAvailableChannel(ProgramSchedule schedule, String nodeName,
      Integer currentTime, Integer srcNodeIndex, Integer snkNodeIndex) {

    var newChannel = UNKNOWN; // indicates no channel was available. The caller will need to check
                              // this result

    // create an instance of the Warp DSL class for parsing instructions
    var dsl = new WarpDSL();
    InstructionTimeSlot priorInstructionTimeSlot;

    var channels = channelsAvailable.getChannelSet(currentTime);
    if (currentTime > 0) { // get the prior schedule time slot to see what channels were used in
                           // that slot, which have to be avoided here
      Integer priorTime = currentTime - 1;
      priorInstructionTimeSlot = schedule.get(priorTime); // get a copy of the prior time slot
      var srcPriorInstruction = priorInstructionTimeSlot.get(srcNodeIndex);
      var snkPriorInstruction = priorInstructionTimeSlot.get(snkNodeIndex);

      // extract the channels used by the src and snk nodes in the prior time slot and store them in
      // an array
      var instructionParametersArrayList = dsl.getInstructionParameters(srcPriorInstruction); // get
                                                                                              // the
                                                                                              // parameters
                                                                                              // from
                                                                                              // the
                                                                                              // instruction
                                                                                              // in
                                                                                              // the
                                                                                              // src
                                                                                              // node's
                                                                                              // prior
                                                                                              // time
                                                                                              // slot
      for (int i = 0; i < instructionParametersArrayList.size(); i++) {
        var instructionParameters = instructionParametersArrayList.get(i); // get a copy of the
                                                                           // paramaters
        channels.remove(instructionParameters.getChannel());
      }
      instructionParametersArrayList = dsl.getInstructionParameters(snkPriorInstruction); // get the
                                                                                          // parameters
                                                                                          // from
                                                                                          // the
                                                                                          // instruction
                                                                                          // in the
                                                                                          // snk
                                                                                          // node's
                                                                                          // prior
                                                                                          // time
                                                                                          // slot
      for (int i = 0; i < instructionParametersArrayList.size(); i++) {
        var instructionParameters = instructionParametersArrayList.get(i); // get a copy of the
                                                                           // paramaters
        channels.remove(instructionParameters.getChannel());
      }
    }
    Integer channel = workLoad.getNodeChannel(nodeName); // get the last used channel for the node
    channel++; // increment the channel because we don't use the same channel in consecutive time
               // slots for the same node
    if (channel >= getNumChannels()) { // valid range is 0..NumChannels-1. Reset when channel hits
                                       // max
      channel = 0;
    }
    var channelFound = false;
    while (!channelFound && !channels.isEmpty()) { // loop until a channel is found or we run out of
                                                   // channels to assign
      var channelString = String.valueOf(channel);
      var channelRemoved = channels.remove(channelString);
      if (channelRemoved) {
        // newChannel has the channel
        newChannel = channelString;
        channelFound = true;
      } else {
        // try another channel
        channel += 1;
        if (channel >= getNumChannels()) { // valid range is 0..NumChannels-1. Reset when channel
                                           // hits max
          channel = 0;
        }
      }
    }
    return newChannel; // returns UNKNOWN to indicate no channel found. This should never happen.
  }

  public void selectPriority() {
    setScheduleSelected(ScheduleChoices.PRIORITY);
  }

  public void selectRM() {
    setScheduleSelected(ScheduleChoices.RM);
  }

  public void selectDM() {
    setScheduleSelected(ScheduleChoices.DM);
  }

  public void selectRtHART() {
    setScheduleSelected(ScheduleChoices.RTHART);
  }

  private void setScheduleSelected(ScheduleChoices choice) {
    switch (choice) {
      case PRIORITY:
        schedulerName = "Priority";
        schChoice = "-Priority";
        workLoad.setFlowsInPriorityOrder();
        break;
      case RM:
        schedulerName = "RateMonotonic";
        schChoice = "-RM";
        workLoad.setFlowsInRMorder();
        break;
      case DM:
        schedulerName = "DeadlineMonotonic";
        schChoice = "-DM";
        workLoad.setFlowsInDMorder();
        break;
      case RTHART:
        schedulerName = "RealtimeHART";
        schChoice = "-RealTimeHART";
        workLoad.setFlowsInRealTimeHARTorder();
        setRealTimeHartFlag(true);
        break;
      case CONNECTIVITY_POSET_DM:
        schedulerName = "ConnectivityPosetDM";
        schChoice = "-CPdm";
        workLoad.setFlowsInDMorder();
        break;
      case CONNECTIVITY_POSET_PRIORITY:
        schedulerName = "ConnectivityPoset";
        schChoice = "-CPoset";
        workLoad.setFlowsInPriorityOrder();
        break;
      case CONNECTIVITY_POSET_RM:
        schedulerName = "ConnectivityPosetRM";
        schChoice = "-CPrm";
        workLoad.setFlowsInRMorder();
        break;
      case POSET_DM:
        schedulerName = "PosetDM";
        schChoice = "-PosetDM";
        workLoad.setFlowsInDMorder();
        break;
      case POSET_PRIORITY:
        schedulerName = "Poset";
        schChoice = "-Poset";
        workLoad.setFlowsInPriorityOrder();
        break;
      case POSET_RM:
        schedulerName = "PosetRM";
        schChoice = "-PosetDM";
        workLoad.setFlowsInRMorder();
        break;
      case WARP_POSET_DM:
        schedulerName = "WarpPosetDM";
        schChoice = "-WPdm";
        workLoad.setFlowsInDMorder();
        break;
      case WARP_POSET_PRIORITY:
        schedulerName = "WarpPoset";
        schChoice = "-WarpPoset";
        workLoad.setFlowsInPriorityOrder();
        break;
      case WARP_POSET_RM:
        schedulerName = "WarpPosetRM";
        schChoice = "-WPrm";
        workLoad.setFlowsInRMorder();
        break;
      default:
        schedulerName = "Priority";
        schChoice = "-Priority";
        workLoad.setFlowsInPriorityOrder();
        break;
    }
    SchedulerSelected = choice;
  }

  private void setSchedule(ProgramSchedule schedule) {
    scheduleBuilt = schedule;
  }
  
  /**
   * Returns the program schedule that was built.
   * 
   * @return	Program schedule object containing the instructions.
   */
  public ProgramSchedule getSchedule() {
    return scheduleBuilt;
  }

  @Override
  public String getSchedulerName() {
    return schedulerName;
  }

  public String getSchChoice() {
    return schChoice;
  }

  @Override
  public Integer getNumChannels() {
    return nChannels;
  }

  @Override
  public Integer getNumTransmissions() {
    return nTransmissions;
  }

  private Boolean getRealTimeHartFlag() {
    return realTimeHARTflag;
  }

  private void setRealTimeHartFlag(Boolean flag) {
    realTimeHARTflag = flag;
  }

  @Override
  public Double getMinPacketReceptionRate() {
    return workLoad.getMinPacketReceptionRate();
  }

  @Override
  public Double getE2e() {
    return workLoad.getE2e();
  }

  @Override
  public String getName() {
    return workLoad.getName();
  }

  @Override
  public Boolean getOptimizationFlag() {
    return optimizationRequested;
  }

  public Description deadlineMisses() {
    return deadlineMisses;
  }

  @Override
  public Integer getNumFaults() {
    return workLoad.getNumFaults();
  }

  /**
   * Creates a map from the node names to index locations in the schedule table.
   * 
   * @return HashMap that maps node names to their index in the schedule table.
   */
  public HashMap<String, Integer> getNodeMapIndex() {
    var orderedNodes = workLoad.getNodeNamesOrderedAlphabetically(); // create an array of node
                                                                     // names
    // sorted alphabetically
    var nodeIndexMap = new HashMap<String, Integer>(); // create a new mapping from node names to
                                                       // index in schedule
    // table
    var nNodes = orderedNodes.length;
    for (int index = 0; index < nNodes; index++) { // set up the node to index mapping
      var name = orderedNodes[index];
      nodeIndexMap.put(name, index); // add name, index mapping to NodeIndex map
    }
    return nodeIndexMap;
  }

}
