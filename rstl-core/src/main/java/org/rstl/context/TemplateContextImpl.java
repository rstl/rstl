/*
 * Copyright IBM Corp. 2012
 */

package org.rstl.context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rstl.Constants;
import org.rstl.NullRef;
import org.rstl.TemplateGroup;

public class TemplateContextImpl implements TemplateContext, IContextProvider {
	private static final String CLASS_NAME = TemplateContextImpl.class.getCanonicalName();
	private static final Logger _LOGGER = Logger.getLogger(CLASS_NAME);

	private List<IContextProvider> providerList = new ArrayList<IContextProvider>();
	Map<String, Object> ctx;

	public TemplateContextImpl(Map<String, Object> arg, TemplateGroup tg) {
		if (null != arg) {
			ctx = arg;
		} else {
			ctx = new HashMap<String, Object>();
		}
		// Add the following into the context automatically, the template group associated with the request
		// an ID generator for resource div ids, and a set of templates that have been included
		// These will be used by the template renderer for various things like locating included templates,
		// automatically generating ids for binding with widgets and templates included so far by the engine
		ctx.put(Constants.TEMPLATE_GROUP, tg);
		AtomicInteger idgen = new AtomicInteger();
		ctx.put(Constants.DIVID_GENERATOR, idgen);
		Set<String> templatesIncluded = new HashSet<String>();
		ctx.put(Constants.TEMPLATES_INCLUDED, templatesIncluded);
		addProvider(this);
		addProvider(new BlockContextProvider(this));
	}

	/**
	 * Add a context processor
	 * @param n
	 */
	public void addProvider(IContextProvider p) {
		providerList.add(p);
	}
	
	/**
	 * Set the HTTP request context for the template context - namespaced with HTTP
	 * @param req Java bean that can be used to retrieve request attributes
	 */
	public void setRequestContext(Object req) {
		addProvider(new HttpContextProvider(req));
	}
	
	/**
	 * Retrieve a variable from the Template Context
	 * @return Object representing the value of the variable, null if it does not exist
	 */
	public Object get(String l) {
		if (_LOGGER.isLoggable(Level.FINE)) {
			_LOGGER.logp(Level.FINE, CLASS_NAME, "get", "Get context variable: " + l);
		}
		Object retValue = null;

		// Check to see if the value needs to be dereferenced
		int varAt = l.indexOf('.');
		String firstVarSeg = l;
		if (varAt > 0) { 
			// Check for attribute references
			firstVarSeg = l.substring(0, varAt);
		} 
		
		for (IContextProvider p : providerList) {
			if (p.looksUpCompleteReference()) {
				retValue = p.lookup(l);
				if (null != retValue) {
					// reset attribute references
					varAt = -1;
				}
			} else {
				retValue = p.lookup(firstVarSeg);
			}
			if (null != retValue) {
				break;
			}
		}
		
		if (retValue == NullRef.getInstance()) {
			retValue = null;
		}
		// Look for attribute references if the first segment returned an object and there are additional segments to process
		if (null != retValue && varAt > 0) {
			String[] attributes = l.substring(varAt + 1).split("\\.");
			for (String attr: attributes) {
				retValue = dereferenceAttribute(retValue, attr, l);
				if (null == retValue) {
					break;
				}
			}
			// TODO: Cache result ?? intermediates ?
		}
		if (_LOGGER.isLoggable(Level.FINE)) {
			_LOGGER.logp(Level.FINE, CLASS_NAME, "get", "Retrieved value for context variable: " + l + " value: " + retValue);
		}
		
		return retValue;
	}
	
	/**
	 * Dereference the attribute in the variable object
	 * @param variable
	 * @param attribute
	 */
	private Object dereferenceAttribute(Object variable, String attribute, String originalVariableName) {
		Object retVal = null;
		if (_LOGGER.isLoggable(Level.FINE)) {
			_LOGGER.logp(Level.FINE, CLASS_NAME, "dereferenceAttribute", "Dereferencing attribute:" + attribute + " from variable");
		}

		if (null != attribute && !attribute.isEmpty()) {
			if (variable instanceof Map) {
				if (_LOGGER.isLoggable(Level.FINE)) {
					_LOGGER.logp(Level.FINE, CLASS_NAME, "dereferenceAttribute", "retrieve as a map entry");
				}
				Map<String, ?> map = (Map<String, ?>) variable;
				retVal = map.get(attribute);
			} else if (variable instanceof List || variable.getClass().isArray()) {
				// try to reference attribute as an integer x.2
				if (_LOGGER.isLoggable(Level.FINE)) {
					_LOGGER.logp(Level.FINE, CLASS_NAME, "dereferenceAttribute", "retrieve as a list or array element");
				}
				Integer index = 0;
				boolean attributeConsumed = false;
				try {
					index = Integer.parseInt(attribute);
					attributeConsumed = true;
				} catch (NumberFormatException e) {
					_LOGGER.logp(Level.WARNING, CLASS_NAME, "dereferenceAttribute", "Attempting to index an array/list with something that is not a number (" 
							+ attribute +") when referencing the variable:" + originalVariableName + "; Using 0 as the index in to list");
				}
				retVal = getIndexedValue(variable, index);
				// If the index is not a number, this will default to 0th index for convenience
				// x.abc - if x is a list or array then this reference really evaluates to  x.0.abc
				if (null != retVal && !attributeConsumed) {
					// (sigh) recursion ..
					// We need to consume the attribute if possible before returning the value;
					retVal = dereferenceAttribute(retVal, attribute, originalVariableName);
				}
			} else if (variable instanceof ResourceBundle) {
				if (_LOGGER.isLoggable(Level.FINE)) {
					_LOGGER.logp(Level.FINE, CLASS_NAME, "dereferenceAttribute", "retrieve as a Resource bundle message");
				}

				ResourceBundle rb = (ResourceBundle) variable;
				try {
					retVal = rb.getObject(attribute);
				} catch (MissingResourceException mre) {
					_LOGGER.logp(Level.WARNING, CLASS_NAME, "dereferenceAttribute", "Attempting to retrieve a non existent message (%0) from resource bundle when retrieving variable %1", 
							new Object[] {attribute , originalVariableName});					
				}
			} else {
				// Treat as a bean
				if (_LOGGER.isLoggable(Level.FINE)) {
					_LOGGER.logp(Level.FINE, CLASS_NAME, "dereferenceAttribute", "retrieve as a list or array element");
				}

				String methodName = "get" + attribute.substring(0, 1).toUpperCase() + attribute.substring(1);
				try {
					Class<?>[] varargs = null;
					Object[] parms = null;
					Method m = variable.getClass().getMethod(methodName, varargs);
					retVal = m.invoke(variable, parms);
				
				} catch (Exception e) {
					_LOGGER.logp(Level.WARNING, CLASS_NAME, "dereferenceAttribute", "Attempting to retrieve a non existent/invalid method (%0) from Java Object for variable name: %1", 
							new Object[] {methodName, originalVariableName});
				}
			}
		}
		if (_LOGGER.isLoggable(Level.FINE)) {
			_LOGGER.logp(Level.FINE, CLASS_NAME, "dereferenceAttribute", "Retrieved value:" + retVal + " for attribute:" + attribute);
		}
		
		return retVal;
	}
	
	/**
	 * Tries to reference the element in a list or array specified by the index
	 * @param list
	 * @param index
	 * @return
	 */
	private Object getIndexedValue(Object list, int index) {
		Object retVal = null;
		if (list instanceof List) {
			List tList = (List) list;
			if (index >= 0 && tList.size() > index) {
				retVal = ((List)list).get(index);
			}
		} else if (list.getClass().isArray()) {
			Object[] arr = (Object[])list; 
			if (index >= 0 && arr.length > index) {
				retVal = arr[index];
			}
		}

		return retVal;
	}

	public void put(String key, Object val) {
		ctx.put(key, val);
	}

	public void remove(String key) {
		ctx.remove(key);
	}

	public String getString(String l) {
		Object obj = get(l);
		String s = convertToString(obj);
		return s;
	}
	
	private String convertToString(Object obj) {
		String retVal = "";
		if (null == obj) {
			return "";
		} else {
			if (obj instanceof String) {
				return (String)obj;
			} else if (obj instanceof List || obj.getClass().isArray()) {
				return convertToString(getIndexedValue(obj, 0));
			} else {
				// Stringify collections into one large string
				return obj.toString();
			}
		}
	}

	public Iterable<Object> getList(String l) {
		Object ret = get(l);
		if (null != ret) {
			Class<?> clazz = ret.getClass();
			if (ret instanceof Iterable) {
				return (Iterable) ret;
			} else if (ret instanceof String){
				return Arrays.asList(ret);
			} else if (clazz.isArray()) {
				Class<?> componentType = clazz.getComponentType();
				Object[] arr = (Object[])ret;
				List<Object> list = new ArrayList<Object>();
				for(Object x: arr) {
					list.add(x);
				}
				return list;
			} else {
				_LOGGER.logp(Level.FINE, CLASS_NAME, "getList", "Could not convert variable reference " + l + " to List (" + ret.getClass().getName() +")");
			}
		}
		return Collections.EMPTY_LIST;
	}
	
	public int getListSize(String l) {
		Object ret = get(l);
		if (null != ret) {
			Class<?> clazz = ret.getClass();
			if (ret instanceof Collection) {
				return ((Collection) ret).size();
			} else if (ret instanceof String){
				return 1;
			} else if (clazz.isArray()) {
				Class<?> componentType = clazz.getComponentType();
				Object[] arr = (Object[])ret;
				return arr.length;
			} else {
				_LOGGER.logp(Level.FINE, CLASS_NAME, "getListSize", "Could not convert variable reference " + l + " to List (" + ret.getClass().getName() +")");
			}
		}
		return 0;		
	}

	public boolean getBoolean(String l) {
		Object ret = get(l);
		if (null != ret) {
			if (ret instanceof Boolean) {
				return (Boolean)ret;
			}
			return true;
		}
		return false;
	}

	public Object lookup(String vName) {
		return ctx.get(vName);
	}

	public boolean looksUpCompleteReference() {
		return false;
	}

}
