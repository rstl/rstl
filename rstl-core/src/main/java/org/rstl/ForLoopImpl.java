/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.util.List;

/**
 * The definition of a For loop 
 */
public class ForLoopImpl extends GenericStatementImpl implements ForLoop {
	private String collectionName;
	private String key;
	private String value;
	private boolean reversed;
	private BlockImpl block = null;

	protected ForLoopImpl(String key, String cname, StatementFactory fac) {
		this(key, cname, 0, fac);
	}
	
	protected ForLoopImpl(String key, String cname, int line, StatementFactory fac) {
		this(key, null, cname, false, fac);
	}

	protected ForLoopImpl(String key, String value, String cname,
			boolean reversed, StatementFactory fac) {
		this(key, value, cname, reversed, 0, fac);
	}
	
	protected ForLoopImpl(String key, String value, String cname,
			boolean reversed, int line, StatementFactory fac) {
		super(StatementType.forstatement, String.valueOf(fac.getStatementId()), line);
		this.key = key;
		collectionName = cname;
		block = new BlockImpl("_internal_for_loop_" + cname, line, fac);
		if (null != value && !value.isEmpty())
			this.value = value;
		this.reversed = reversed;
	}

	public String getCollection() {
		return collectionName;
	}

	public String getValue() {
		return value;
	}

	public boolean getReversed() {
		return reversed;
	}
	
	public String getKey() {
		return key;
	}

	public List<Statement> getStatements() {
		return block.getStatements();
	}

	public void addStatement(Statement s) {
		block.addStatement(s);
	}
	
	public void addText(String s, int line) {
		block.addText(s, line);
	}

	public void addVariable(String s, int line) {
		block.addVariable(s, line);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append(':').append(collectionName).append(':').append(value).append(':').append(reversed);
		sb.append('[');
		sb.append(block.toString());
		sb.append(']');
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			ForLoopImpl tObj = (ForLoopImpl) obj;

			if (null != collectionName && !(collectionName.equals(tObj.collectionName))) {
				return false;
			} 
			if (null == collectionName && null != tObj.collectionName) {
				return false;
			} 
			if (null != value && !(value.equals(tObj.value))){
				return false;
			}
			if (null == value && null != tObj.value){
				return false;
			}
			if (reversed != tObj.reversed) {
				return false;
			}
			if (!block.equals(tObj.block)) {
				return false;
			}
			return true;	
		}
		return false;
	}



}
