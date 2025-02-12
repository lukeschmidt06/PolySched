package edu.uiowa.cs.warp;

public class ReportVisualization extends VisualizationObject{

	private static final String SUFFIX = ".txt";
	private Description content;
	private String title;
	

	ReportVisualization (FileManager fm, SystemAttributes warp, 
			Description content, String title) {
		super(fm, warp, new String(title + "Report"), SUFFIX);
		this.content = content;
		this.title = title;
	}


	@Override
	public Description visualization() {
		return createReport();
	}
	
	private Description createReport () {
		Description report;
		if (content.size() > 0) {
			report = new Description(new String (title + " Report"));
			report.addAll(content);
		} else {
			report = new Description(new String ("No " + title + " to report"));
		}
		return report;
	}
	
}
