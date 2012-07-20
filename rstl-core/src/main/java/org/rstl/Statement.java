/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

public interface Statement {
	/**
	 * @return the type of statement, should be one of forstatement,
	 *         blockstatement, chunkstatement, resourcestatement,
	 */
	public String getType();

	/**
	 * 
	 * @return the primary id for the statement
	 */
	public String getId();
	
	/**
	 * 
	 * @return the line number where the statement is specified
	 */
	public int getLine();
	
	/**
	 * @return the line number where the statement definition ends
	 */
	public int getLineEnd();
	
	/**
	 *  return the index in the template where the rgroup definition began
	 */
	public int getDeclStart();
	
	/**
	 * Get the index in the template where the rgroup defintion ended
	 * @return
	 */
	public int getDeclStop();
	
	/**
	 * Set the index in the template where the rgroup definition ended.
	 * @param stopIndex
	 */
	public void setDeclStop(String stopIndex);
	
	/**
	 * Set the line number where the statement declaration ends.
	 * @param endOfDecl
	 */
	public void setLineEnd(int endOfDecl);
}
