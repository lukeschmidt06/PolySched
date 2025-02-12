/**
 * 
 */
package edu.uiowa.cs.warp;

import edu.uiowa.cs.warp.Visualization.SystemChoices;
import edu.uiowa.cs.warp.Visualization.WorkLoadChoices;

/**
 * Factory Class to create a visualization instance.
 * This factory can be used to create different
 * types of visualizations.
 * @author sgoddard
 *
 */
public class VisualizationFactory {
	
	public static Visualization createProgramVisualization(WarpInterface warp, 
			String outputDirectory, SystemChoices choice) {
		return new VisualizationImplementation(warp, outputDirectory, choice);
	}
	public static Visualization createWorkLoadVisualization(
			WorkLoad workload, String outputDirectory, WorkLoadChoices choice) {
		return new VisualizationImplementation(workload, outputDirectory, choice);
	}
}

