package edu.uiowa.cs.warp;

import com.mkyong.system.OSValidator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;

/**
 * @author sgoddard
 * @version 1.5
 *
 */
public class FileManager {
  // uncomment next line to tag course file
  // private static final String COURSE_TAG = "// Course CS2820 Authentication Tag";
  private String baseDirectory;
  private Boolean verbose;

  /**
   * @param args
   */
  public FileManager() {
    baseDirectory = System.getProperty("user.dir"); // Get current directory path and set to the
                                                    // base directory
    // constructor for this class
    verbose = false; // initialize verbose to false
  }

  public FileManager(Boolean verbose) {
    baseDirectory = System.getProperty("user.dir"); // Get current directory path and set to the
                                                    // base directory
    // constructor for this class
    this.verbose = verbose; // initialize verbose as specified

  }

  public String getDocumentsDirectory() { // return the 'Documents Directory' for the appropriate OS
    String documentsDirectory;
    if (OSValidator.isWindows()) {
      documentsDirectory = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
    } else if (OSValidator.isMac()) {
      documentsDirectory = System.getProperty("user.home") + File.separator + "Documents";
    } else { // for all other Unix systems
      documentsDirectory = System.getProperty("user.home"); // assume $HOME for Unix systems (not
                                                            // MACOS)
    }
    return documentsDirectory;
  }

  public String getCurrentDirectory() {
    return System.getProperty("user.dir");
  }

  public void setBaseDirectory(String directory) {
    baseDirectory = directory;
  }

  public String getBaseDirectory() {
    return baseDirectory;
  }

  /*
   * Build the name extension used for all files created by this tool. The nameExtension will be
   * added to the base input file name so that all of the output files can be associated with the
   * input file The file naming pattern is: baseFileNameFileType-NonDefaultParameterList where
   * FileType is Schedule, SimInput, ReliabilityMatrix, or ReliabilityArray Default parameters are
   * not identified in the NameExtension All parameters used to create the file, should be listed
   * near the top of the file in the Parameter Section, followed by the file contents.
   * 
   */
  public String createFile(String file, String nameExtension, String suffix) {
    /*
     * we don't actually create the file...it will be created when written to this routine really
     * just creates the file name ;-)
     */
    Integer suffixIndex = file.lastIndexOf('.');
    String fileString = file;
    if (suffixIndex > 0) { // if a suffix exists, index will be > 0
      fileString = file.substring(0, suffixIndex); // get the file string sans the suffix
    }
    /*
     * fileString has no suffix at this point (removed if it existed) Now add file name extension
     * and suffix
     */
    fileString = fileString + nameExtension + suffix;

    if (verbose) {
      System.out.println("File " + fileString + " is created!");
    }
    return fileString;
  }

  public String createFile(String file, String suffix) {
    Integer suffixIndex = file.lastIndexOf('.');
    String fileString = file;
    if (suffixIndex > 0) { // if a suffix exists, index will be > 0
      fileString = file.substring(0, suffixIndex - 1); // get the file string sans the suffix
    }
    // fileString has no suffix at this point (removed if it existed)
    // Now add file name extension and suffix
    fileString = fileString + suffix;
    // we don't actually create the file...it will be created when written to
    // this routine really just creates the file name ;-)
    if (verbose) {
      System.out.println("File " + fileString + " is created!");
    }
    return fileString;
  }

  public String createDirectory(String directory, String subDirectory) {
    String newDirectory;
    if (subDirectory.startsWith("/")) { // check if full path provided
      newDirectory = subDirectory; // if subDirectory is a full path, use it.
    } else { // subDirectory has relative path, so just append
      newDirectory = directory + File.separator + subDirectory;
    }
    try {
      Path path = Paths.get(newDirectory);
      Files.createDirectories(path);
      if (verbose) {
        System.out.println("Directory " + newDirectory + " is created!");
      }
    } catch (IOException e) {
      System.err.println("Failed to create directory!" + e.getMessage());
      newDirectory = directory; // in case of error, just use the initial directory
    }
    return newDirectory;
  }

  public void writeFile(String file, String fileContents) {
    // Authentication tag = new Authentication(COURSE_TAG);
    Path fileName = Path.of(file);
    try {
      // Files.writeString(fileName, tag.sign(fileContents)); // uncomment to tag course file
      Files.writeString(fileName, fileContents); // comment out if tag is being used (line above)
    } catch (IOException e) {
      // handle error
      System.err.println("Error on writing file contents to file" + file + ": " + e.getMessage());
    } // the file will be closed automatically upon exit of this try block
  }

  public String readFile(String file) {
    // String contents = null;
    Path fileName = Path.of(file);
    String contents = null;
    try {
      contents = Files.readString(fileName);
    } catch (IOException e) {
      // handle error
      System.err.println("Error on reading file" + file + ": " + e.getMessage());
    } // the file will be closed automatically upon exit of this try block
    return contents;
  }

}
