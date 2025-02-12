/**
 * 
 */
package edu.uiowa.cs.warp;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import edu.uiowa.cs.warpdsls.WARPBaseListener;
import edu.uiowa.cs.warpdsls.WARPLexer;
import edu.uiowa.cs.warpdsls.WARPParser;

/**
 * @author sgoddard
 *
 */
//Extend WARP Base Listener Class to extract info for Flows
final class WorkLoadListener extends WARPBaseListener {
    
	WorkLoad workLoad;
    String currentFlow; 
    private WorkLoadListener(WorkLoad workLoad){
        this.workLoad = workLoad; // used to populate the workLoad as the input file is read
        this.currentFlow = new String();
     // Read input file and build AST of graphFile
        try {
        	CharStream inputGraph = CharStreams.fromString(workLoad.toString());
        	var lexer = new WARPLexer(inputGraph);
        	var tokens = new CommonTokenStream(lexer);
        	var parser = new WARPParser(tokens);
        	ParseTree warpTree = parser.warp(); // begin parsing at rule 'warp'
        	var warp = new ParseTreeWalker();
        	 // Now populate build the workLoad by walking the input Graph tokens
        	warp.walk(this, warpTree);
        } catch (Exception e) {
        	e.printStackTrace();
        	System.err.println("ERROR: Unable to parse graphFile" + e.getMessage());
        }
    }
    
    public static void buildNodesAndFlows (WorkLoad workLoad){
    	/*
    	 * Create the listener object, which will read the workLoad 
    	 * description and then buid the node and flow objects that
    	 * instantiates the workLoad. This object is not needed
    	 * after that.
    	 */
    	new WorkLoadListener(workLoad); 
    }
    
    public void testPrintStdOut(String msg) {
    	System.out.printf("\n In WARPworkLoad Listener!!\n");
    	System.out.printf(msg);
    }

    @Override public void enterWarpName(WARPParser.WarpNameContext ctx) {
        var name = ctx.getText();  // get the name of the graph 
        workLoad.setName(name); // store the  name for later reference
    }
    

    @Override public void enterFlowName(WARPParser.FlowNameContext ctx ) {
        currentFlow = ctx.getText();
        workLoad.addFlow(currentFlow);
    }
   
    @Override public void exitPriority(WARPParser.PriorityContext ctx ) {
    	var priority = Integer.parseInt(ctx.getText());  // get priority from the AST
        workLoad.setFlowPriority(currentFlow, priority);
    }
    
    @Override public void exitPeriod(WARPParser.PeriodContext ctx ) {
        var period = Integer.parseInt(ctx.getText());// get period from the AST
        workLoad.setFlowPeriod(currentFlow, period);
    }
    
    @Override public void exitDeadline(WARPParser.DeadlineContext ctx ) {
        var deadline = Integer.parseInt(ctx.getText());// get deadline from the AST
        workLoad.setFlowDeadline(currentFlow, deadline);
    }
    
    @Override public void exitPhase(WARPParser.PhaseContext ctx ) {
        var phase = Integer.parseInt(ctx.getText());// get phase from the AST
        workLoad.setFlowPhase(currentFlow, phase);
    }

    @Override public void exitSrcNode(WARPParser.SrcNodeContext ctx ) {
        var nodeName = ctx.getText(); // get the src node name
        workLoad.addNodeToFlow(currentFlow, nodeName);
    }

    @Override public void exitSnkNode(WARPParser.SnkNodeContext ctx ) {
        var nodeName = ctx.getText(); // get the snk node name
        workLoad.addNodeToFlow(currentFlow, nodeName);
    }
        
    @Override public void exitFlow(WARPParser.FlowContext ctx ) {
        // Now determine flow length and then set nTXperLink for the Flow
        workLoad.finalizeCurrentFlow(currentFlow);
    }
 
}

