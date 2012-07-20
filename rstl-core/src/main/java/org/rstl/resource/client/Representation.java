/*
 * Copyright IBM Corp. 2012
 */

package org.rstl.resource.client;

import java.io.InputStream;

/**
 * The interface for the representation of a resource
 *
 */
public interface Representation {
	
	/**
	 * @return true if the representation is valid
	 */
	public boolean isValid();
	
	/**
	 * @return the content type associated with the representation
	 */
	public String getContentType();
	
	/**
	 * @return the body associated with the representation
	 */
	public InputStream getEntity();
	
}
