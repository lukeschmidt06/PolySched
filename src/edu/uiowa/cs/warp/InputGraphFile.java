package edu.uiowa.cs.warp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author sgoddard
 *
 */
public class InputGraphFile {

	private static final String UNKNOWN = "Unknown";
	private FileManager wfm; // Warp File Manager object
	private String graphFileName; // name of the input graph file
	private Boolean verbose; // verbose flag
	private String graphFileContents = UNKNOWN;

	public InputGraphFile(FileManager fm) {
		wfm = fm;
		graphFileName = wfm.getBaseDirectory();  // input graph file name, accessible only within this class
		verbose = false; // initialize verbose to false
	}

	public InputGraphFile(FileManager fm, Boolean verbose) {
		wfm = fm;
		graphFileName = wfm.getBaseDirectory();  // input graph file name, accessible only within this class
		this.verbose = verbose; // initialize verbose as specified

	}

	public String getGraphFileName ()  {
		return graphFileName;
	}

	private void setGraphFileName (String inputFile) {
		graphFileName = inputFile;
	}

	private void verifyGraphFileName(String inputFile) {
		/* Read the graph input text file and handle all errors, returning its contents */
		try {
			getInputGraphFileName(inputFile);
		} catch (Exception e) {
			// e.printStackTrace();
			System.err.println("Failed get an input file.");
			System.exit(-1);
		}
	}

	public String readGraphFile(String inputFile) {
		/* Read the graph input text file and return its contents */
		verifyGraphFileName(inputFile); // catches exception if a valid file can't be found and exits
		String inputFileName = getGraphFileName();
		graphFileContents = readGraphFileContents(inputFileName);
		return graphFileContents;
	}
	
	private String getInputGraphFileName (String inputFile) throws RuntimeException {
		String workingDirectory;
		String fileName = UNKNOWN;
		workingDirectory = wfm.getBaseDirectory();
		if (inputFile == null) { // if fileName is nil, then prompt for input file
			System.out.printf("Working directory is %s\n", workingDirectory);
			System.out.print("Enter input file: ");
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			try {
				fileName = bufferRead.readLine();
			} catch (IOException e) {
				// e.printStackTrace();
				System.err.println("Failed to read input line from console" + e.getMessage());
			}
		} else {
			fileName = inputFile;
		}
		setGraphFileName(fileName);
		File tempFile = new File(getGraphFileName()); // create a temp File object with graphName
		if (tempFile.exists()) {  // see if the file exists
			if (verbose) {
				printVerboseMessage(workingDirectory, fileName, getGraphFileName());
			}
		} else { // try the working directory
			setGraphFileName(workingDirectory + File.separator + fileName);
			tempFile = new File(getGraphFileName()); // create a temp File object with graphName
			if (tempFile.exists()) {  // see if the file exists
				if (verbose) {
					printVerboseMessage(workingDirectory, fileName, getGraphFileName());
				}
			} else {
				if (verbose) {
					printVerboseMessage(workingDirectory, fileName, getGraphFileName());
				}
				System.err.printf("\n\tERROR: input file %s doesn't exist!!\n", fileName);
				throw new RuntimeException();
			}
		}
		return fileName;
	}

	private String readGraphFileContents (String inputFile)  {
		String graphFileContents = wfm.readFile(inputFile);
		if (verbose) {
			System.out.println("************************************");
			System.out.println("Graph File Read:");
			System.out.println(graphFileContents);
			System.out.println("************************************\n");
		}
		return graphFileContents;
	}

	private void printVerboseMessage(String workingDirectory, String fileName, 
			String graphFileName) {
		System.out.printf("Working directory is %s\n", workingDirectory);
		System.out.printf("Input file name is %s\n", fileName);
		System.out.printf("Graph file %s doesn't exist\n", graphFileName);
	}
}
