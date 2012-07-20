/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TemplateClassLoader extends ClassLoader {
	private static final String CLASS_NAME = TemplateClassLoader.class.getCanonicalName();
	private static final Logger _LOGGER = Logger.getLogger(CLASS_NAME);
	/**
	 * Location of the generated template classes
	 */
	private File classDir;
	
	public TemplateClassLoader(String templateClassDir) {
		this(templateClassDir, null);
	}
	
	public TemplateClassLoader(String templateClassDir, ClassLoader parent) {
		super(null == parent? TemplateClassLoader.class.getClassLoader(): parent);
		classDir = new File(templateClassDir);
	}
	
	public Class<?> loadClass(String className) throws ClassNotFoundException{
		Class<?> clazz = null;
		
		// Check in VM cache
		clazz = findLoadedClass(className);
		if (null != clazz) {
			return clazz;
		}
		
		// Do not bother with any class that does pertain to templates
		if (!className.startsWith(TemplateUtil.TEMPLATE_PACKAGE_NAME)) {
			clazz = getParent().loadClass(className);
		} else {
			clazz = findClass(className);
			// If we could not locate the class - let the parent try to load it.
			if (null == clazz) {
				clazz = getParent().loadClass(className);
			}
		}
		return clazz;
	}
	
	public Class<?> findClass(String className) {
		byte classBytes[];
		Class<?> ret = null;
		String classloc = className.replace('.', File.separatorChar) + ".class";
		File classFile = new File(classDir, classloc);
		if (!classFile.exists()) {
			return null;
		}
		int len = (int)classFile.length();
		classBytes = new byte[len];
		DataInputStream dis = null;
		try {
			FileInputStream fis = new FileInputStream(classFile);
			dis = new DataInputStream(fis);
			dis.readFully(classBytes);
			ret = defineClass(className, classBytes, 0, len);
		} catch (Exception ex) {
			_LOGGER.logp(Level.WARNING, CLASS_NAME, "findClass", "Failed to load " + className);
		}
		finally {
			if (null != dis) {
				try {
					dis.close();
				} catch (Exception e) {
					// IGNORED exception
				}
			}
		}
		return ret;	
	}
}
