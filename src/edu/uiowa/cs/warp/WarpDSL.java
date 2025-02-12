package edu.uiowa.cs.warp;

/**
 *  Created by Steve Goddard on 12/8/20.
 *  Copyright © 2020 Steve Goddard. All rights reserved.
 */

import java.util.*; 
import edu.uiowa.cs.warpdsls.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

/**
 * @author sgoddard
 *
 */


public class WarpDSL {
    
	private static final String UNKNOWN = "unknown";
	public static final String UNUSED = "unused";
	public static final String PUSH = "push";
	
	public class InstructionParameters {
		// initially parameters are marked unused. Once the grammar is updated, this will be set 
		// to the name of instruction--push, pull, wait, sleep--by the Listener functions
		private String name = UNUSED; 
		private String flow = UNUSED;
		private String src = UNUSED;
		private String snk = UNUSED;
		private String channel = UNUSED;
		private String coordinator = UNKNOWN;
		private String listener = UNKNOWN;
		
		public String getName() {
        	return name;
        }
		public String getFlow() {
        	return flow;
        }
		public String getSrc() {
        	return src;
        }
		public String getSnk() {
        	return snk;
        }
		public String getChannel() {
        	return channel;
        }
		/**
		 * @return the coordinator
		 */
		public String getCoordinator() {
			return coordinator;
		}
		/**
		 * @return the listener
		 */
		public String getListener() {
			return listener;
		}
		/**
		 * @param coordinator the coordinator to set
		 */
		public void setCoordinator(String coordinator) {
			this.coordinator = coordinator;
		}
		/**
		 * @param listener the listener to set
		 */
		public void setListener(String listener) {
			this.listener = listener;
		}
		private void setName(String name) {
        	 this.name = name;
        }
		private void setFlow(String flow) {
        	 this.flow = flow;
        }
		private void setSrc(String src) {
        	 this.src = src;
        }
		private void setSnk(String snk) {
        	 this.snk = snk;
        }
		private void setChannel(String channel) {
       	 this.channel = channel;
        }
		public String unused() {
			return UNUSED;
		}
	
	}

	
    public ArrayList<InstructionParameters> getInstructionParameters(String instruction)  {
        
    	ArrayList<InstructionParameters> instructionParametersArrayList;
    	var dsl = new ListenerDsl();
        // Read input file and build AST of graph
        try {
        	CharStream inputInstruction = CharStreams.fromString(instruction);
        	var lexer = new WARPdslLexer(inputInstruction);
        	var instructionTokens = new CommonTokenStream(lexer);
        	var parser = new WARPdslParser(instructionTokens);
        	ParseTree instructionTree = parser.instruction(); // begin parsing at rule 'warp'
        	var warp = new ParseTreeWalker();
        	warp.walk(dsl, instructionTree);
        } catch (Exception e) {
        	e.printStackTrace();
        	System.err.println("Unable to parse instruction instruction from the schedule entry:" + e.getMessage());
        }
        instructionParametersArrayList = dsl.getInstructionParameters();
        return instructionParametersArrayList;
    }

    // Listener Class for DSL
    private final class ListenerDsl extends WARPdslBaseListener { // parses one instruction
        Boolean inAction; 
        ArrayList<InstructionParameters> instructionParametersArrayList;// vector of instruction parameters (each entry contains the parameters for a single structure
        InstructionParameters instructionParameters;
        
        ListenerDsl() {
            inAction = false;
            instructionParametersArrayList = new ArrayList<InstructionParameters>();
        }
        
        public  ArrayList<InstructionParameters> getInstructionParameters() {
            return instructionParametersArrayList;
        }
        
        @Override public void enterAction(WARPdslParser.ActionContext ctx) {
            inAction = true;
            instructionParameters = new InstructionParameters();  // create a new instance of the parameter structure with default initialization
        }
        
        @Override public void exitAction(WARPdslParser.ActionContext ctx) {
            inAction = false;
            instructionParametersArrayList.add(instructionParameters);  // this action is done, so add the parameters to the list
        }
        
        @Override public void exitFlowName(WARPdslParser.FlowNameContext ctx) {
            if (inAction) {
                String flowName = ctx.getText(); // get the flow name
                instructionParameters.setFlow(flowName);
            }
        }
        
        @Override public void exitCmd(WARPdslParser.CmdContext ctx) {
            String command = ctx.getText(); // get the command/instruction name
            instructionParameters.setName(command);
        }
        
        @Override public void exitSrcNode(WARPdslParser.SrcNodeContext ctx) {
            String srcName = ctx.getText(); // get the src node name
            instructionParameters.setSrc(srcName);
            var command = instructionParameters.getName();
            if (command.equals(PUSH)) {
            	/* if push then coordinator is src 
            	 */
            	instructionParameters.setCoordinator(srcName);
            }else {
            	/* otherwise listener is src */
            	instructionParameters.setListener(srcName);
            }
        }

        @Override public void exitSnkNode(WARPdslParser.SnkNodeContext ctx) {
            String snkName = ctx.getText(); // get the snk node name
            instructionParameters.setSnk(snkName);
            var command = instructionParameters.getName();
            if (command.equals(PUSH)) {
            	/* if push then listener is snk */
            	instructionParameters.setListener(snkName);
            }else {
            	/* otherwise coordinator is snk 
            	 */
            	instructionParameters.setCoordinator(snkName);
            }
        }
        @Override public void exitChannel(WARPdslParser.ChannelContext ctx)  {
        	String channelString = ctx.getText(); // get the snk node name
        	String channel; // resulting channel that will be extracted from the channel string
        	Integer index = channelString.indexOf('#');
        	if (index >= 0 && index < channelString.length()) {
        		channel = channelString.substring(index+1); // get string starting at first channel number
        	} else {
        		channel = UNKNOWN; // no valid channel in the channelString, so set to UNKNOWN
        	}	
            instructionParameters.setChannel(channel); 
        }
    }

}

