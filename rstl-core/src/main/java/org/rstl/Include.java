/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

public class Include extends GenericStatementImpl {

	String className;

	protected Include(String id) {
		this(id, 0);
	}

	protected Include(String id, int line) {
		super(StatementType.includestatement, id, line);
		className = TemplateUtil.getClassName(id);
	}

	/**
	 * Get the class name of the included template
	 * 
	 * @return
	 */
	public String getClassName() {
		// TODO Auto-generated method stub
		return className;
	}

}
