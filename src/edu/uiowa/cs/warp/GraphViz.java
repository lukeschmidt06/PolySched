package edu.uiowa.cs.warp;

/**
 * @author sgoddard2
 * Copyright © 2020 Steve Goddard. All rights reserved.
 * 
 */
public class GraphViz extends VisualizationObject {

	/* 
	 * The gv (GraphViz) file has the format:
	 * 
	 strict digraph InputFileBaseName {
	     C  -> B  -> A;
	 labelloc  =  "t"
	 label = <Graph InputFileBaseName <br/>
	 Flow F0: C -&#62; B -&#62; A<br/>
	 >
	 }
	*
	*/

	private static final String EMPTY = "";
	private static final String GRAPH_VIZ_SUFFIX = ".gv";
	
    String gvFileContents; // contents of Graph Viz File to be created
    String gvTitleCaption; // Title Caption to be merged with gvFileContents
    FileManager wfm;  // class to provide basic WARP file management functions
    String graphFile; 
    Boolean verbose; // verbose flag
    GraphViz gv;
    
    GraphViz(FileManager wfm, String graphFile) {
    	super(wfm, EMPTY, GRAPH_VIZ_SUFFIX); // VisualizationObject constructor
        this.wfm = wfm; // used to access the basic WARP file management functions
        this.graphFile = graphFile;
        this.gv = this;
        verbose = false;
        createGraphVizContent();
    }

    GraphViz(FileManager wfm, String graphFile, Boolean verbose) {
    	super(wfm, EMPTY, GRAPH_VIZ_SUFFIX); // VisualizationObject constructor
        this.wfm = wfm; // used to access the basic WARP file management functions
        this.graphFile = graphFile;
        this.gv = this;
        this.verbose = verbose;
        createGraphVizContent();
    }
    
    @Override
	public Description visualization() {
		return new Description(getGraphVizContent());
	}
    
    public void initializeGraphVizContent (String graphName)  {
        /*start the gv file by adding the 1st line to the contents string */
        gvFileContents = String.format("strict digraph %s {\n",graphName);
        /* remove 'strict' if multiple edges in the same direction are to be shown */
        gvTitleCaption = "labelloc  =  \"t\" \n"; // Place the rentered graph's title on top.
        /* 
         * create a title caption for the rendered graph in the .gv file
         * label string is in html format, so need an end delimeter after adding flows
         */
        gvTitleCaption = String.format(gvTitleCaption + "label = <Graph %s <br/>\n",graphName);
    }
   
    public void finalizeGraphVizContent ( )  {
        /* 
         * wrap up the gv file content by finalizing title caption
         * then add it to the file contents and terminate the graph viz structure
         */
        gvTitleCaption += ">\n";
        /* 
         * end the gv file by adding the caption for rendering and the last line 
         * to the contents string
         */
        gvFileContents = gvFileContents + gvTitleCaption + "} \n";  
    }
   
    /* 
     * public function to return the string gvFileContents.
     * designed to be called after finalizeGraphVizContent() is called by the listener
     * but will return the current state of the contents string anytime it is called
     */
    public String getGraphVizContent ( )  {
        return gvFileContents;
    }
    
    public String createGraphVizContent ( ) {
    	/*
    	 * GraphVizListener reads the graphFile string and calls the functions
    	 * in this class, as it walks the input graphFile structure to build
    	 * the GraphViz contents.
    	 */
        GraphVizListener.buildGraphViz(gv); 
        if (verbose) {
        	System.out.println("************************************");
        	System.out.println("GraphViz File Contents:");
        	System.out.println(getGraphVizContent());
        	System.out.println("************************************\n");
        }
        return getGraphVizContent();
    }

    public void addFlowToGraphViz (String flowName)  {
    /* add a 3 spaces before the start of the chain defining this flow to the gv contents string */
        gvFileContents = gvFileContents + "   ";  
        gvTitleCaption = String.format(gvTitleCaption + "Flow %s: ", flowName);
    }

    public void addSrcNodeToGraphViz (String nodeName)  {
    	String fileContents = String.format(" %s  ->",nodeName);
    	String titleCaption = String.format("%s -&#62; ",nodeName);
    	addNodeName(fileContents,titleCaption);
    }
   
    public void addSnkNodeToGraphViz (String nodeName)  {
    	String fileContents = String.format(" %s",nodeName);
    	String titleCaption = String.format("%s",nodeName);
    	addNodeName(fileContents,titleCaption);
    }
   
    public void finalizeCurrentFlowInGraphViz ( )  {
    	/* add the ';' and '\n' to end this line of the chain defined in this flow */
        gvFileContents = gvFileContents + "; \n" ;
        gvTitleCaption += "<br/>\n"; // terminate this line of the rendered graph caption
    }

    private void addNodeName(String fileContents, String titleCaption) {
    	/* add the node name to the gv file contents string */
        gvFileContents += fileContents;  
        /* add the node name and edge to this line of the rendered graph caption */
        gvTitleCaption += titleCaption;
    }
	

}
