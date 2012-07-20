/*
 * Copyright IBM Corp. 2012
 */

package org.rstl.context;

/**
 * The template renderer uses this interface to retrieve context information.
 * The context information could be from a variety of sources including a Map,
 * Context providers such as Authn, HTTP headers etc.
 * 
 */
public interface TemplateContext {

	public Object get(String l);

	public String getString(String l);
	
	public Iterable<Object> getList(String l);
	
	public int getListSize(String l);
	
	public boolean getBoolean(String l);
	
	public void put(String key, Object val);

	public void remove(String key);
	
	// TODO: allow for getCollection so that an empty collection/list can be returned if not available in context.

}
