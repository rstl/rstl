/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;


import java.util.ArrayList;
import java.util.List;

/**
 * The definition for a Block
 * 
 */
public class BlockImpl extends GenericStatementImpl implements Block {
	
	StatementFactory fac;

	protected BlockImpl(String id, StatementFactory fac) {
		this(id, 0, fac);
	}
	
	protected BlockImpl(String id, int line, StatementFactory fac) {
		super(StatementType.blockstatement, id, line);
		this.fac = fac;
	}

	private List<Statement> statements = new ArrayList<Statement>();

	public List<Statement> getStatements() {
		return statements;
	}

	public void addStatement(Statement s) {
		statements.add(s);
	}
	

	public void addText(String s, int line) {
		Chunk c = fac.createChunk(s, line);
		statements.add(c);
	}

	public void addVariable(String s, int line) {
		Statement v = fac.createVariable(s, line);
		statements.add(v);	
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append('{');
		sb.append(statements.size());
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			BlockImpl tObj = (BlockImpl) obj;
			if (statements.size() == tObj.getStatements().size()) {
				int ix = 0;
				for(Statement s : statements) {
					if (s.equals(tObj.statements.get(ix))) {
						return false;
					}
					ix++;
				}
				return true;
			}
		}
		return false;
	}

}
