/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.util.List;

public class ResourceGroup extends GenericStatementImpl implements Block {
	private static int sequence = 0;
	private Block block;

	
	protected ResourceGroup(String rGroupId, StatementFactory fac) {
		this(rGroupId, 0, 0, fac);
	}
	
	protected ResourceGroup(String rGroupId, int line, int startIndex, StatementFactory fac) {
		super(StatementType.rgroupstatement, rGroupId, line, startIndex);
		block = new BlockImpl("_internal_rgroup_block_" + sequence++ + rGroupId, line, fac);
	}

	public void addStatement(Statement s) {
		block.addStatement(s);
	}

	public List<Statement> getStatements() {
		return block.getStatements();
	}

	public void addText(String s, int line) {
		block.addText(s, line);
	}

	public void addVariable(String s, int line) {
		block.addVariable(s, line);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append('[');
		sb.append(block.toString());
		sb.append(']');
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			ResourceGroup tObj = (ResourceGroup) obj;
			if (block == tObj.block) {
				return true;
			}
		}
		return false;
	}
}
