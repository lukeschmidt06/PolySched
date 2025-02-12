package edu.uiowa.cs.warp;

public class NotImplentedVisualization extends VisualizationObject {

	private static final String SUFFIX = ".txt";
	private static final String NAME_EXTENSTION = "NotImplementedFeature";
	private static final String CONTENT = "Not Implemented";
	
	NotImplentedVisualization() {
		super(new FileManager(), NAME_EXTENSTION, SUFFIX);
	}
	
	NotImplentedVisualization(String extension) {
		super(new FileManager(), extension, SUFFIX);
	}

	@Override
	public Description visualization() {
		return new Description(CONTENT);
	}

}
