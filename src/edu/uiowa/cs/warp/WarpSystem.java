/**
 * 
 */
package edu.uiowa.cs.warp;

/**
 * @author sgoddard
 * @version 1.4
 */
public class WarpSystem implements WarpInterface {

  // private static final String SOURCE_SUFFIX = ".dsl";

  private Program program;
  private WorkLoad workLoad;
  private ReliabilityAnalysis ra;
  private LatencyAnalysis la;
  private ChannelAnalysis ca;
  private Integer numChannels;
  private Boolean verboseMode = false;
  private Boolean latencyRequested = false;

  public WarpSystem(WorkLoad workLoad, Integer numChannels, ScheduleChoices choice) {
    this.workLoad = workLoad;
    this.numChannels = numChannels;
    createProgram(workLoad, numChannels, choice);
  }

  @Override
  public WorkLoad toWorkload() {
    return workLoad;
  }

  @Override
  public Program toProgram() {
    return program;
  }

  @Override
  public ReliabilityAnalysis toReliabilityAnalysis() {
    // TODO Auto-generated method stub
    ra = new ReliabilityAnalysis(program);
    return ra;
  }

  @Override
  public SimulatorInput toSimulator() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LatencyAnalysis toLatencyAnalysis() {
    // TODO Auto-generated method stub
    la = new LatencyAnalysis(this);
    return la;
  }

  @Override
  public ChannelAnalysis toChannelAnalysis() {
    // TODO Auto-generated method stub
    ca = new ChannelAnalysis(this);
    return ca;
  }

  @Override
  public Boolean reliabilitiesMet() {
    if (ra == null) {
      ra = new ReliabilityAnalysis(program);
    }
    return ra.verifyReliabilities();
  }

  @Override
  public Boolean deadlinesMet() {
    Boolean result = true;
    if (program.deadlineMisses().size() > 0) {
      result = false;
    }
    return result;
  }


  private void createProgram(WorkLoad workLoad, Integer numChannels, ScheduleChoices choice) {
    program = new Program(workLoad, numChannels, choice, verboseMode, latencyRequested);

  }


  @Override
  public Integer getNumChannels() {
    return numChannels;
  }

  @Override
  public Integer getNumFaults() {
    return program.getNumFaults();
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
  public String getSchedulerName() {
    return program.getSchedulerName();
  }

  @Override
  public Integer getNumTransmissions() {
    return program.getNumTransmissions();
  }

  @Override
  public Boolean getOptimizationFlag() {
    return program.getOptimizationFlag();
  }

  @Override
  public void toSensorNetwork() {
    // TODO Auto-generated method stub

  }


}
