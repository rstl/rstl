/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.util.List;

public class ConditionalImpl extends GenericStatementImpl implements Conditional {
	private static int idGen = 0;
	
	private String expr;
	private Block ifClauseBlock = null;
	private Block elseClauseBlock = null;
	private Block current;
	private StatementFactory fac;
	
	protected ConditionalImpl(String expr, StatementFactory fac) {
		this(expr, 0, fac);
	}
	
	protected ConditionalImpl(String expr, int line, StatementFactory fac) {
		super(StatementType.conditionalstatement, String.valueOf(idGen++), line);
		this.expr = expr;
		this.fac = fac;
		ifClauseBlock = new BlockImpl("_internal_if_block_" + getId(), line, fac);
		elseClauseBlock = new BlockImpl("_internal_else_block_" + getId(), line, fac);
		current = ifClauseBlock;
	}
	
	public void addElseClause(int line) {
		elseClauseBlock = new BlockImpl("_internal_else_block_" + getId(), line, fac);
		current = elseClauseBlock;
	}
	
	public String getExpression() {
		return expr;
	}

	public Block getElseClause() {
		return elseClauseBlock;
	}

	public Block getIfClause() {
		return ifClauseBlock;
	}

	public void addStatement(Statement s) {
		current.addStatement(s);
	}

	public void addText(String s, int line) {
		current.addText(s, line);
		
	}

	public void addVariable(String s, int line) {
		current.addVariable(s, line);
	}

	public List<Statement> getStatements() {
		// Should use the ifClause or else clause
		return current.getStatements();
	}
	
	public String getCodedExpression() {
		return "c.getBoolean(\"" + expr +"\")";
		
	}

}
