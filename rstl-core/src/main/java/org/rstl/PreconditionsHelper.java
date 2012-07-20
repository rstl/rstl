/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rstl.context.TemplateContext;

public class PreconditionsHelper {
	private static final String CLASS_NAME = PreconditionsHelper.class.getCanonicalName();
	private static final Logger _LOGGER = Logger.getLogger(CLASS_NAME);
	enum PRE {google, yahoo, authorized, weekend, seasonal, search};
	
	public static boolean preconditionsMatch(Template t, TemplateContext ctxt) {
		if (null == t) {
			System.out.println("Not template to check");
			return false;
		}
		_LOGGER.logp(Level.FINE, CLASS_NAME, "preconditionsMatch", "Checking preconditons on " + t.getTemplateName());
		List<String> pre = t.getPreconditions();
		if (pre.isEmpty()) {
			return true;
		}
		
		return false;
	}
	
	public static void addPreconditionMap(List<String> actual, TemplateContext c) {
		for (PRE p : PRE.values()) {
			String preKey = "precondition."+p.name();
			if (actual.contains(p.name())) {
				c.put(preKey, "yes");
			} else {
				c.put(preKey, "no");
			}
		}
	}

}
