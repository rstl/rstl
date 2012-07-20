package org.rstl.context;

import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rstl.Constants;
import org.rstl.NullRef;
import org.rstl.Template;

public class BlockContextProvider implements IContextProvider {
	private static final String CLASS_NAME = BlockContextProvider.class.getCanonicalName();
	private static final Logger _LOGGER = Logger.getLogger(CLASS_NAME);
	static private final String BLOCK_NAMESPACE = "block";
	
	TemplateContext ctx = null;
	
	/**
	 * Initialize the BlockContextProvider with the Template Context
	 * It needs this to identify the template reference associated with the TemplateContext.
	 * @param ctx Template Context
	 */
	BlockContextProvider(TemplateContext ctx) {
		this.ctx = ctx;
	}

	public Object lookup(String vName) {
		Object retVal = null;
		if (vName.startsWith(BLOCK_NAMESPACE)) {
			// Only check in block name space if the original variable name starts with "block"
			if (_LOGGER.isLoggable(Level.FINE)) {
				_LOGGER.logp(Level.FINE, CLASS_NAME, "get", "Checking block name space");
			}
			String methodName = vName.substring(BLOCK_NAMESPACE.length() + 1);
			Template template = (Template)ctx.get(Constants.TEMPLATEREF);
			if (template.getBlockNames().contains(methodName)) {
				Writer w = new StringWriter();
				template.invokeMethod(methodName + "Block", ctx, w, false);
				retVal = w.toString();
			} else {
				_LOGGER.log(Level.WARNING, "Block: " + methodName + " does not exist in template: " + template.getTemplateName());
				retVal = NullRef.getInstance();
			}
		}
		return retVal;
	}

	public boolean looksUpCompleteReference() {
		return true;
	}
}
