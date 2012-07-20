/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

public class GenericStatementImpl implements Statement {
	private String id = null;
	private StatementType type;
	private int line;
	private int lineEnd;
	private int startIndex;
	private int stopIndex;

	protected GenericStatementImpl(StatementType type, String id, int line) {
		this(type, id, line, 0, 0);
	}

	protected GenericStatementImpl(StatementType type, String id, int line, int startIndex) {
		this(type, id, line, startIndex, 0);
	}
	
	protected GenericStatementImpl(StatementType type, String id, int line, int startIndex, int stopIndex) {
		this.id = id;
		this.type = type;
		this.line = line;
		this.startIndex = startIndex;
		this.stopIndex = stopIndex;
	}
	
	public String getType() {
		return type.name();
	}

	public String getId() {
		return id;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getLineEnd() {
		return lineEnd;
	}
	
	public int getDeclStart() {
		return startIndex;
	}

	public int getDeclStop() {
		return stopIndex;
	}

	public void setDeclStop(String stopIndexStr) {
		try {
			this.stopIndex = Integer.parseInt(stopIndexStr);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
	}
	
	public void setLineEnd(int endOfDecl) {
		this.lineEnd = endOfDecl;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		sb.append((null != id) ? ":" + id : "");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof GenericStatementImpl))
			return false;
		GenericStatementImpl tObj = (GenericStatementImpl) obj;
		if (tObj.type.equals(type)) {
			if ((null != id && null != tObj.id
					&& id.equals(tObj.id) || null == id
					&& null == tObj.id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
