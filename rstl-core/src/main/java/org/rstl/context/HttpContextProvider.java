/*
 * Copyright IBM Corp. 2012
 */

package org.rstl.context;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rstl.NullRef;

public class HttpContextProvider implements IContextProvider {
	private static final String CLASS_NAME = HttpContextProvider.class.getCanonicalName();
	private static final Logger _LOGGER = Logger.getLogger(CLASS_NAME);
	static private final String HTTP_NAMESPACE = "Http";
	
	Map<String, Object> httpAttrCache = new HashMap<String, Object>();
	Object httpReq;
	
	/**
	 * Initialize the provider with the Http Request object
	 * @param httpReq
	 */
	HttpContextProvider(Object httpReq) {
		this.httpReq = httpReq;
	}
	
	/**
	 * The lookup method uses reflection to try to retrieve a http request attribute associated with 
	 * variable name Http<AttributeName> using the get<AttributeName> method on the Http Request object.
	 * If the method cannot be invoked, the NullRef object instance is returned to indicate no further processing
	 * is necessary.
	 * The lookup also caches any lookups for the scope of the ContextProvider (request)
	 */
	public Object lookup(String vName) {
		Object retValue = null;
		if (vName.startsWith(HTTP_NAMESPACE)) {
			// Check outside of local context
			if (_LOGGER.isLoggable(Level.FINE)) {
				_LOGGER.logp(Level.FINE, CLASS_NAME, "lookup", "Checking Http name space");
			}
			Object attr = httpAttrCache.get(vName);
			if (null != attr) {
				retValue =  attr;
			} else {
				String methodName = "get" + vName.substring(HTTP_NAMESPACE.length());
				Class<?> c = httpReq.getClass();
				retValue = "";
				try {
					Method m = c.getMethod(methodName);
					retValue = m.invoke(httpReq);
				} catch (Exception e) {
					_LOGGER.logp(Level.FINE, CLASS_NAME, "lookup", "Failed to invoke method:" + methodName + " for Http Request object", e);
					retValue = NullRef.getInstance();
				}
				httpAttrCache.put(vName, retValue);
			}
		} 
		return retValue;
	}

	public boolean looksUpCompleteReference() {
		return false;
	}

}
