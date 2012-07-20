/*
 * Copyright IBM Corp. 2012
 */

package org.rstl.resource.client;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Client that will retrieve a resource representation using a HTTP client
 * connection to a specified endpoint.
 *
 */
public class HttpResourceClientImpl implements ResourceClient {
	
	String resourceEndpoint;
	
	/**
	 * Constructor to create a Apache HTTP client based resource client
	 * @param resourceEndpoint the scheme and http endpoint to use to retrieve resources.
	 */
	public HttpResourceClientImpl(String resourceEndpoint) {
		if (!resourceEndpoint.startsWith("http://")) {
			// Log warning about invalid scheme  - use localhost
		}
	}


	public boolean canRetrieve(String url, String contentType) {
		// TODO Auto-generated method stub
		return true;
	}

	public Representation fetchResource(String url, String contentType) {
		HttpClient client = new DefaultHttpClient();
		HttpGet req = new HttpGet(url);
		return null;
	}

}
