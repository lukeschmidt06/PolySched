/**
 * 
 */
package edu.uiowa.cs.warp;

/**
 * @author sgoddard
 * @version 1.4
 */
public interface WarpInterface extends SystemAttributes {

  public WorkLoad toWorkload();

  public Program toProgram();

  public ReliabilityAnalysis toReliabilityAnalysis();

  public SimulatorInput toSimulator();

  public LatencyAnalysis toLatencyAnalysis();

  public ChannelAnalysis toChannelAnalysis();

  public void toSensorNetwork(); // deploys code

  public Boolean reliabilitiesMet();

  public Boolean deadlinesMet();

}
