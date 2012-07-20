package org.rstl;

import java.util.List;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;

public class RSTLToken extends CommonToken {
	List tokenargs = null;

	public RSTLToken(CharStream input, int type, int channel, int start, int stop) {
		super(input, type, channel, start, stop);
	}
		
	public void setargs(List args) {
		tokenargs = args;
	}
		
	public List getargs() { return tokenargs;}

	public String toString() { return super.toString();}
}


