/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.util.List;


public interface IErrorReporter {
	/**
	 * Report an error in the template from either the lexer or parser 
	 * @param line
	 * @param charInLine
	 * @param Message
	 */
	void reportError(int line, int charInLine, String Message);
	
	/**
	 * Report a warning in the template from either the lexer or parser 
	 * @param line
	 * @param charInLine
	 * @param Message
	 */
	void reportWarning(int line, int charInLine, String message);
	
	/**
	 * @return a list of strings representing the error messages
	 */
	List<String> getErrors();
	
	/**
	 * @return the count of errors reported for the template
	 */
	int getErrorCount();
}
