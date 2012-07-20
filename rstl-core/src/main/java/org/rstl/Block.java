/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.util.List;

public interface Block extends Statement {
	/**
	 * 
	 * @return the list of statement for this block
	 */
	public List<Statement> getStatements();

	/**
	 * Add a statement to this block
	 * 
	 * @param s
	 */
	public void addStatement(Statement s);
	
	/**
	 * Add text or a chunk to this block
	 */
	public void addText(String s, int line);
	
	/**
	 * Add a variable reference to this block
	 */
	public void addVariable(String s, int line);
	

}
