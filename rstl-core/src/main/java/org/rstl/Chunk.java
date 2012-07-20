/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

public class Chunk extends GenericStatementImpl {
	private String value;
	private String origValue;
	private int startIndex;

	protected Chunk(String id, String value, String origValue, int line) {
		super(StatementType.chunkstatement, id, line);
		this.value = value;
		this.origValue = origValue;
	}

	/**
	 * 
	 * @return the literal value associated with this statement
	 */
	public String getValue() {
		return value;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append('[');
		sb.append(value.length());
		sb.append(']');
		return sb.toString();
	}
	
	/**
	 * Trim any space or tab characters up until the last line feed character.
	 */
	public void trimToLastLine() {
		int lastlinefeed = origValue.lastIndexOf('\n');
		if (lastlinefeed >= 0 && lastlinefeed < origValue.length()-1) {
			String remaining = origValue.substring(lastlinefeed  + 1);
			if (remaining.trim().isEmpty()) {
				String newvalue = origValue.substring(startIndex, lastlinefeed + 1);
				value = TemplateUtil.literalize(newvalue);
			}
		}
	}
	
	/**
	 * Hide any LF or CRLF characters that may be at the beginning of the chunk from the rendered output.
	 */
	public void hideAnyStartingLF() {
		if (origValue.startsWith("\n")) {
			startIndex = 1;
		} else if (origValue.startsWith("\r\n")) {
			startIndex = 2;
		}
		if (startIndex > 0) {
			String newValue = origValue.substring(startIndex);
			value = TemplateUtil.literalize(newValue);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			Chunk tObj = (Chunk) obj;
			if (value.equals(tObj.value)) {
				return true;
			}
		}
		return false;
	}

}
