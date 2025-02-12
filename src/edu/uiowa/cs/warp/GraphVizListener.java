package edu.uiowa.cs.warp;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import edu.uiowa.cs.warpdsls.WARPBaseListener;
import edu.uiowa.cs.warpdsls.WARPLexer;
import edu.uiowa.cs.warpdsls.WARPParser;

/*
 * Extend WARP Base Listener Class to extract info for Graph Viz file.
 * GraphVizListener reads the graphFile string and calls the functions
 * in this class, as it walks the input graphFile structure to build
 * the GraphViz contents.
 */
public final class GraphVizListener extends WARPBaseListener {

	GraphViz gv; // gv object creating this object. Yes, it is wierd!
	private GraphVizListener(GraphViz gv){
        this.gv = gv; // use GraphViz object passed to this object
     // Read input file and build AST of graphFile
        try {
        	CharStream inputGraph = CharStreams.fromString(gv.graphFile);
        	var lexer = new WARPLexer(inputGraph);
        	var tokens = new CommonTokenStream(lexer);
        	var parser = new WARPParser(tokens);
        	ParseTree warpTree = parser.warp(); // begin parsing at rule 'warp'
        	var warp = new ParseTreeWalker();
        	 // Now populate build the Graph Viz file content by walking the input Graph tokens
        	warp.walk(this, warpTree);
        } catch (Exception e) {
        	e.printStackTrace();
        	System.err.println("ERROR: Unable to parse graphFile" + e.getMessage());
        }
    }

	public static void buildGraphViz (GraphViz gv){
    	/*
    	 * Create the listener object, which will read the workLoad 
    	 * description and then buid the node and flow objects that
    	 * instantiates the workLoad. This object is not needed
    	 * after that.
    	 */
    	new GraphVizListener(gv); 
    }

    @Override public void enterWarpName(WARPParser.WarpNameContext ctx) {
        var graphName = ctx.getText();  // get the name of the graph from name
        gv.initializeGraphVizContent(graphName); // start creating the gv file content
    }

    @Override public void exitWarp(WARPParser.WarpContext ctx) {
        gv.finalizeGraphVizContent();
    }

    @Override public void enterFlowName(WARPParser.FlowNameContext ctx) {
        var currentFlow = ctx.getText();
        gv.addFlowToGraphViz(currentFlow);
    }

    @Override public void exitSrcNode(WARPParser.SrcNodeContext ctx) {
        var nodeName = ctx.getText(); // get the src node name
        gv.addSrcNodeToGraphViz(nodeName);
    }

    @Override public void exitSnkNode(WARPParser.SnkNodeContext ctx) {
        var nodeName = ctx.getText(); // get the snk node name
        gv.addSnkNodeToGraphViz(nodeName);
    }

    @Override public void exitFlow(WARPParser.FlowContext ctx) {
        gv.finalizeCurrentFlowInGraphViz();
    }

}
