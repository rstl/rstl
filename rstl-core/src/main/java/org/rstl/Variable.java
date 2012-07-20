/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.util.List;

public interface Variable extends Statement {
	/**
	 * Get the name of the variable 
	 * For eg. a variable declared like c.name|urlencode will result in a variable name "c.name" being returned to the caller
	 * @return variable name
	 */
	public String getVariableName();
	
	/**
	 * Get the list of filters to be applied to the variable
	 * For eg. a variable declared like c.name|urlencode will result in a list containing "urlencode" being returned to the caller
	 * @return List of filter names
	 */
	public List<String> getFilters();
	
	/**
	 * Get the list of respective arguments for the filters to be applied to the variable
	 * For eg. a variable declared like c.name|encode:"UTF8" will result in a list containing "UTF8" 
	 * being returned to the caller. If the filter takes no arguments, the element will have a value null.
	 * @return List of filter names
	 */
	public List<String> getFilterArgs();

	/**
	 * Return the first part of a string of applied filter functions that should be applied to the variable value
	 * For eg. a variable declared like c.name|urlencode|upper will result in a string "VarUtil.upper(VarUtil.urlencode(" being returned to the caller.
	 * The exact functions associated with the filter is dependent on the implementation.
	 * @return
	 */
	public String getAppliedFilterPrefixString();
	
	/**
	 * Return the final part of a string of applied filter functions that should be applied to a given variable value
	 * For eg. a variable declared like c.name|urlencode|upper will result in a string "))" being returned to the caller, denoting 
	 * the terminating parens for the function calls from getAppliedFilterPrefixString
	 * @return
	 */
	public String getAppliedFilterSuffixString();
}
