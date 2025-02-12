package edu.uiowa.cs.warp;

/**
 * @author sgoddard2
 *
 */
public class CommunicationGraph extends VisualizationObject {

	FileManager wfm; // Warp File Manager object
	String graphFileName; // name of the input graph file
	Boolean verbose; // verbose flag
	WorkLoad workLoad; // used access flows as the some of the output files are built
	
	public CommunicationGraph(FileManager fm, WorkLoad workLoad) {
		// constructor for GraphFile class
		super(fm, workLoad, ".wf");
		this.wfm = fm;
		this.workLoad = workLoad;
		verbose = false; // initialize verbose to false
	}
	
	public CommunicationGraph(FileManager fm, WorkLoad workLoad, Boolean verbose) {
		// constructor for GraphFile class
		super(fm, workLoad, ".wf");
		this.wfm = fm;
		this.workLoad = workLoad;
		this.verbose = verbose; // initialize verbose to false
	}
	
	@Override
	public Description createHeader ( )  {
		Description header = new Description();
		header.add(String.format("WARP_%s{\n",workLoad.getName()));
		return header;
	}
	
	@Override
	public Description createFooter ( )  {
		Description footer = new Description();
		footer.add("}\n");
		return footer;
	}
	
	@Override
    public Description visualization ( )  {
        // start the gv file by adding the 1st line to the contents string
    	Description content = new Description();
        String line = String.format(
        	"// M = %s and End-to-End reliability = %s\n",
        	String.valueOf(workLoad.getMinPacketReceptionRate()),
        	String.valueOf(workLoad.getE2e()));
        content.add(line);
        var flowNames = workLoad.getFlowNames();
        for (int flowIndex = 0; flowIndex < flowNames.length; flowIndex++) {
            var flowName = flowNames[flowIndex];
            var nodesInFlow = workLoad.getNodesInFlow(flowName);
            var nNodesInFlow = (nodesInFlow.length);
            /* array of communications costs per link */
            var linkCostArray = workLoad.getNumTxAttemptsPerLink(flowName); 
            var totalCost = workLoad.getTotalTxAttemptsInFlow(flowName);
            var minCost = nNodesInFlow - 1 ;
            /*
             * nEdges in the flow is the minimum communication cost, 
             * i.e., min nTx to go E2E with no errors
             */
            line = String.format(
            		"// Flow %s has a minimum communication cost of %d "
            		+ "and a worst-case communication cost of %d\n", 
            		flowName, minCost, totalCost);
            content.add(line);
            line = String.format(
            		" %s (%d,%d,%d,%d) : ", flowName, 
            		workLoad.getFlowPriority(flowName), workLoad.getFlowPeriod(flowName), 
            		workLoad.getFlowDeadline(flowName), workLoad.getFlowPhase(flowName)); 
            String nodeName;
            for (int i = 0; i < nNodesInFlow-1; i++) {  
                nodeName = nodesInFlow[i];
                var nTx = linkCostArray[i];
                line = String.format(line + "%s -%d-> ",nodeName,nTx); 
                /* This version of the file has nTx in the edges of the flow */
            }
            nodeName = nodesInFlow[nNodesInFlow-1];
            line = String.format(line + "%s\n",nodeName); // add last node on the flow
            content.add(line);
        }
        return content;
    }
    
    public String createWarpFileContent ( )  {
    	String wfFileContents = fileVisualization().toString();
        if (verbose) {
        	System.out.println("************************************");
        	System.out.println("Warp File Contents:");
        	System.out.println(wfFileContents);
        	System.out.println("************************************\n");
        }
        return wfFileContents;
    }

}
