/*
 * Copyright IBM Corp. 2012
 */

package org.rstl.context;

/**
 * This interface is implemented by all context providers
 *
 */
public interface IContextProvider {
	/**
	 * Look up the variable name in the context. If the variable has a value within this context, return the value.
	 * If the variable does not have a value, but processing should stop, return a instance of NullRef. If processing should
	 * continue return null.
	 * @param vName the variable name
	 * @return the value of the variable with in this context
	 */
	public Object lookup(String vName);
	
	/**
	 * The Context provider may choose to lookup the entire reference, particularly if the attributes associated with
	 * a variable name need special handling. For instance "block.title" invokes the block method named titleBlock with in the 
	 * context of the original template.
	 * @return true if the lookup will consume the entire variable name (attributes, indexing and all)
	 */
	public boolean looksUpCompleteReference();
}
