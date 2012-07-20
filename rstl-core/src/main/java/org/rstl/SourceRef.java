/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

/**
 * A reference to an element in a template and where it is defined or referenced
 *
 */
public class SourceRef implements Comparable {
	/* The name of the identifier */
	private String identifier;
	private String sourceTemplate;
	private int line;
	private int startIndex;
	private int stopIndex;

	public SourceRef(String identifier, String sourceTemplate, int line) {
		this.identifier = identifier;
		if (sourceTemplate == null) {
			this.sourceTemplate = new String();
		} else {
			this. sourceTemplate = sourceTemplate;
		}
		this. line = line;	
	}
	
	public SourceRef(String identifier, String sourceTemplate, int line, int startIndex, int stopIndex) {
		this(identifier, sourceTemplate, line);
		this.startIndex = startIndex;
		this.stopIndex = stopIndex;
	}
	
	
	
	public String getName() {
		return identifier;
	}
	
	/**
	 * Get the name of the source template
	 * @return
	 */
	public String getSourceTemplate() {
		return sourceTemplate;
	}
	
	/**
	 * Get the line number where this element was referenced in source template
	 * @return
	 */
	public int getLine() {
		return line;
	}
	
	public int getStartIndex() {
		return startIndex;
	}
	
	public int getStopIndex() {
		return stopIndex;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(identifier).append('@').append(sourceTemplate).append(':').append(line);
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof SourceRef))
			return false;
		SourceRef tObj = (SourceRef) obj;
		if (toString().equals(tObj.toString())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	public int compareTo(Object obj) {
		if (obj == this)
			return 0;

		if (!(obj instanceof SourceRef))
			throw new IllegalArgumentException("Comparison with SourceRef requires SourceRef type");
		return toString().compareTo(obj.toString());
	}
}
