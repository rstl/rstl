/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;


public interface Resource extends Statement {
	
	/**
	 * Get the widget name to use to render the resource
	 * If there is no widget specified, return null.
	 * @return
	 */
	public String getWidgetName();
	
	/**
	 * Get the variable name that can be used as a reference in rest of the template
	 * If there is no variable specified, return null.
	 */
	public String getVariableName();
	
	/**
	 * Get the representation format of the resource 
	 * @return xhtml or json
	 */
	public String getRepresentationFormat();
	
	/**
	 * Get the title associated with the resource
	 * @return
	 */
	public String getTitle();
}
