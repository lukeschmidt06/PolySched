/**
 * 
 */
package edu.uiowa.cs.warp;

import java.util.ArrayList;

/**
 * @author sgoddard
 *
 */
public class Description extends ArrayList<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// default constructor
	public Description() {
		super();
	}

	public Description(String descriptionString) {
		super();
		String[] lines = descriptionString.split("\\r?\\n");
		for (String line : lines) {
			this.add(line+"\n");
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (String row: this) { 
			sb.append(row);
		}
		return sb.toString();
	}	
}
