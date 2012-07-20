/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

public class NullRef {
	static private NullRef instance = new NullRef();
	static public NullRef getInstance() {
		return instance;
	}
	
	private NullRef() {
		
	}

}
