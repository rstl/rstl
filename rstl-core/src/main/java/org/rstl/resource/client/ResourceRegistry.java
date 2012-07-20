/*
 * Copyright IBM Corp. 2012
 */

package org.rstl.resource.client;

import java.util.List;

import org.rstl.ResourceRef;


/**
 * A Registry that can identify the source of a Resource for the current
 * application. The source of the URL may be a remote resource, the registry
 * should identify how the resource should be fetched (scheme, host, port). The
 * registry will default to resources that can be fetched from localhost:80.
 * Local resources whose representations are in the filesystem may be referenced
 * using the Java file:// URI scheme.
 * 
 */
public class ResourceRegistry {
	private static final String BUILTIN_SCHEME_PART = "http://localhost";
	private static String default_scheme_part = BUILTIN_SCHEME_PART;
	
	private static List<ResourceClient> resClientList;
	
	public static ResourceClient getClient(ResourceRef resRef) {
		return null;
	}

	public static String getAbsoluteUri(String uri) {
		if (uri.startsWith("http")) {
			return uri;
		} else {
			return default_scheme_part + uri;
		}
	}

	/**
	 * Set the default scheme part for fetching resources. For instance to
	 * retrieve resources from the localhost at port 8080 by default, the
	 * parameter should be set to "http://localhost:8080"
	 * 
	 * @param schemePart
	 */
	public static void setDefaultSchemePart(String schemePart) {
		default_scheme_part = schemePart;
	}

}
