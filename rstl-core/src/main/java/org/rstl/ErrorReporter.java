/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation class for errors in template
 */
public class ErrorReporter implements IErrorReporter{
	private static final boolean ERROR = true;
	private static final boolean WARN  = false;
	
	String  templateName;
	private List<ErrorReport> errors;
	
	ErrorReporter(String templateName) {
		this.templateName = templateName;
	}
	
	public void reportError(int line, int charInLine,
			String message) {
		ErrorReport report = new ErrorReport(line, charInLine, message, ERROR);
		report(report);
	}
	
	public void reportWarning(int line, int charInLine, String message) {
		ErrorReport report = new ErrorReport(line, charInLine, message, WARN);
		report(report);
	}
	
	private void report(ErrorReport report) {
		if (null == errors) {
			errors = new ArrayList<ErrorReport>();
		}
		errors.add(report);
	}
	
	public List<String> getErrors() {
		if (null == errors) {
			return Collections.emptyList();
		}
		List<String> ret = new ArrayList<String>();
		for (ErrorReport r : errors) {
			StringBuilder sb = new StringBuilder();
			sb.append(templateName).append(" ").append(r.message).append(" (").append(r.error?"E) ":"W)");
			ret.add(sb.toString());
		}
		return ret;
	}
	
	public int getErrorCount() {
		if (null == errors) {
			return 0;
		}
		return errors.size();
	}
	
	public class ErrorReport {
		int 	line;
		int		column;
		boolean error;
		String 	message;
		
		ErrorReport(int line, int column, String msg, boolean error) {
			this.line = line;
			this.column = column;
			this.message = msg;
			this.error = error;
		}
	}

}
