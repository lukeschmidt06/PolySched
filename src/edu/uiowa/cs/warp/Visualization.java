/**
 * 
 */
package edu.uiowa.cs.warp;

/**
 * @author sgoddard
 * @version 1.4
 */
public interface Visualization {

  public enum SystemChoices {
    SOURCE, RELIABILITIES, SIMULATOR_INPUT, LATENCY, LATENCY_REPORT, DEADLINE_REPORT, CHANNEL
  }

  public enum WorkLoadChoices {
    INPUT_GRAPH, COMUNICATION_GRAPH, GRAPHVIZ
  }

  public void toDisplay();

  public void toFile();

  public String toString();
}
