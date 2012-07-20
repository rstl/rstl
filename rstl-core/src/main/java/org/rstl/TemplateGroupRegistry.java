/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TemplateGroupRegistry {
	private static final String CLASS_NAME = TemplateGroupRegistry.class.getCanonicalName();
	private static final Logger _LOGGER = Logger.getLogger(CLASS_NAME);
	// Map of store to template loaders
	static private Map<String, TemplateGroup> instanceMap = new HashMap<String, TemplateGroup>();
	// The default location of the DTL jar so that templates can be compiled
	static private String rotlLoc = "lib/rstl.jar";
	static private String publicWebDir = "web";

	static {
		File f = new File(System.getProperty("user.dir"), rotlLoc);
		if (f.exists()) {
			rotlLoc = f.getAbsolutePath();			
		} else {
			System.err.println("Template engine jar (rstl.jar) missing - needed for compiling templates");
		}
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static TemplateGroup getInstance(String name) {
		return instanceMap.get(name);
	}

	/**
	 * 
	 * @param name
	 * @param tg
	 */
	public static void register(String name, TemplateGroup tg) {
		instanceMap.put(name, tg);
	}

	public static Set<String> getStoreNames() {
		return instanceMap.keySet();
	}

	/**
	 * The location of the DTL jar relative to the current working directory
	 * This is used by the template class generator as it is needed to compile
	 * the templates into classes
	 * 
	 * @return the location of the DTL jar relative to the current working
	 *         directory
	 */
	public static String getROTLLocation() {
		return rotlLoc;
	}

	public static void setROTLLocation(String location) {
		File f = null;
		if (location.startsWith("/")) {
			f = new File(location);
		} else {
			f = new File(System.getProperty("user.dir"), location);
		}
		if (f.exists()) {
			rotlLoc = f.getAbsolutePath();
			_LOGGER.logp(Level.FINE, CLASS_NAME, "setDTLLocation", "DTL Location set to " + rotlLoc);
		} else {
			_LOGGER.logp(Level.SEVERE, CLASS_NAME, "setDTLLocation", "DTL Location incorrect" + f.getAbsolutePath());
		}
	}
	
	public static String getPublicWebDir() {
		return publicWebDir;
	}
	
	public void setPublicWebDir(String webDir) {
		publicWebDir = webDir;
	}
}
