
package edu.uiowa.cs.warp;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Reads the input file, whose name is passed as input parameter to the constructor, and builds a
 * Description object based on the contents. Each line of the file is an entry (string) in the
 * Description object.
 * 
 * @author sgoddard
 * @version 1.4 Fall 2022
 */
public class WorkLoadDescription extends VisualizationObject {
  //Example
  private static final String EMPTY = "";
  private static final String INPUT_FILE_SUFFIX = ".wld";
  public static String hello = "Hello World";
  private Description description;
  private String inputGraphString;
  private FileManager fm;
  private String inputFileName;
  public String pubInputGraphName;
  public static String pubInputGraphString;
  public static String pubInputFileName;

  WorkLoadDescription(String inputFileName) {
    super(new FileManager(), EMPTY, INPUT_FILE_SUFFIX); // VisualizationObject constructor
    this.fm = this.getFileManager();
    initialize(inputFileName);
    
  }

  @Override
  public Description visualization() {
    return description;
  }

  @Override
  public Description fileVisualization() {
    return description;
  }

  // @Override
  // public Description displayVisualization() {
  // return description;
  // }

  @Override
  public String toString() {
    return inputGraphString;
  }

  public String getInputFileName() {
    return inputFileName;
  }

  private void initialize(String inputFile) {
    // Get the input graph file name and read its contents
    InputGraphFile gf = new InputGraphFile(fm);
    inputGraphString = gf.readGraphFile(inputFile);
    this.inputFileName = gf.getGraphFileName();
    description = new Description(inputGraphString); 
    		
  }
  public static void main(String[] args) throws IOException {
	  //Initialize the file into the WorkLoadDesc Object
	  WorkLoadDescription workLoadDesc = new WorkLoadDescription("StressTest.txt");
	  String temp = workLoadDesc.toString();
	  String fileName = workLoadDesc.getInputFileName();
	  fileName = fileName.substring(0, fileName.length()-4);
	  //Prints out the name of the file
	  System.out.println(fileName);
	  //Converts the file from being one string into an array of strings split by a line break
	  String[] linesOfTheFile = temp.split("\n");
	  int fileLineCounter = 1;
	  int totalLinesInFile = 0;
	  int currentLineOfTheFile = 1;
	  
	  //Temporary holder arraylist used for sorting the flows in the right way
	  ArrayList<String> tempHolderForFileLines = new ArrayList<String>();
	  for(String line : linesOfTheFile) {
		  //These first 2 cases catch the bracket portion of files as that's not meant to be in the desired output
		  if(fileLineCounter==1){}
		  else if(fileLineCounter==linesOfTheFile.length){}
		  else {
			  tempHolderForFileLines.add(line);
		  }
		  currentLineOfTheFile++;
		  fileLineCounter++;
		  
	  }	 
	  //Sort the list first in standard typical order, and then reverse it 
	  Collections.sort(tempHolderForFileLines);
	  Collections.reverse(tempHolderForFileLines);
	  int lengthOfTheArrayList = tempHolderForFileLines.size();
	  //Go through each element of the arraylist and output it to the desired output
	  for(int i=0; i<lengthOfTheArrayList; i++) {
		  System.out.println("Flow "+(i+1)+": "+tempHolderForFileLines.get(i));
	  }
  }
}
