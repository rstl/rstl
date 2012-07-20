/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;


public interface Conditional extends Block{
	
	/**
	 * Create an else block for this conditional referenced by the template line number
	 * @param line
	 */
	public void addElseClause(int line);
	
	/** 
	 * Get the conditional expression associated with this statement
	 * @return
	 */
	public String getExpression();
	
	/**
	 * Return the coded expression to be used in a template assume the context is in a variable named "c"
	 * @return
	 */
	public String getCodedExpression();
	
	/**
	 * Get the block of statements that will execute if the expression (identifier) evaluates to true
	 * @return
	 */
	public Block getIfClause();
	
	/**
	 * Get the block of statements that will execute if the expression (identifier) evaluates to false
	 * @return
	 */
	public Block getElseClause();

}
