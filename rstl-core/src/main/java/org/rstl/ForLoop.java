/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

public interface ForLoop extends Block {
	/**
	 * 
	 * @return the name of the collection to iterate over
	 */
	public String getCollection();

	public String getValue();

	public boolean getReversed();
	
	public String getKey();

}
