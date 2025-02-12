package edu.uiowa.cs.warp;

/**
 * @author sgoddard
 * @version 1.4
 *
 */
public class ChannelAnalysis {

  private Program program;
  private ProgramSchedule programTable;
  private Boolean conflictExists;

  ChannelAnalysis(WarpInterface warp) {
    this.program = warp.toProgram();
    this.programTable = program.getSchedule();
    conflictExists = false;
  }

  ChannelAnalysis(Program program) {
    this.program = program;
    this.programTable = program.getSchedule();
    conflictExists = false;
  }

  public Boolean isChannelConflict() {
    return conflictExists;
  }
}
