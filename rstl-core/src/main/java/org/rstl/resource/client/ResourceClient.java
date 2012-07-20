/*
 * Copyright IBM Corp. 2012
 */

package org.rstl.resource.client;


/**
 * Interface that defines the contract for implementations that can retrieve resources
 */
public interface ResourceClient {
	
	/**
	 * @param ref reference to a resource
	 * @return true if the reference can be retrieved by this client
	 */
	public boolean canRetrieve(String url, String contentType);
	
	/**
	 * Fetch the resource representation
	 * @param ref
	 */
	public Representation fetchResource(String url, String contentType);
	
}
