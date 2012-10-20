/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.eclipse.jdt.internal.compiler.batch.Main;

public class TemplateUtil {
	private static final String CLASS_NAME = TemplateUtil.class.getCanonicalName();
	private static final Logger _LOGGER = Logger.getLogger(CLASS_NAME);
	public final static String TEMPLATE_PACKAGE_NAME = "org.rstl.gen";
	public final static String TEMPLATE_MAP_PROPERTY_FILENAME = "templatemap.properties";

	/**
	 * This template prefix is prepended to the template name to create the
	 * generated class name and must start with a capital letter.
	 */
	private static String TEMPLATE_PREFIX = "T";
	/*
	 * private static String SLASH_R_LITERAL_REPLACE = "\\r\" + \n\t\""; private
	 * static String SLASH_N_LITERAL_REPLACE = "\\n\" + \n\t\"";
	 */
	// The following are regular expressions not just Java strings
	private static String SLASH_RN_LITERAL_REPLACE = "\\r\\n\" + \n\t\"";
	private static String SLASH_R_REGEX_REPLACE = "\\\\r\\\" + \n\t\\\"";
	private static String SLASH_N_REGEX_REPLACE = "\\\\n\\\" + \n\t\\\"";
	private static Pattern TEMPLATE_SUFFIX_PATTERN = Pattern.compile(".+\\.(ct|htm)l$");

	/**
	 * Return the class name from the name of the template
	 * 
	 * @param template
	 * @return
	 */
	public static String getClassName(String templateName) {
		StringBuilder sb = new StringBuilder();
		sb.append(TEMPLATE_PREFIX);
		String baseName = templateName.replace("/", "__");
		sb.append(baseName.replace('.', '_'));
		return sb.toString();
	}

	/**
	 * Get the qualified class name which includes the package name
	 * 
	 * @param templateName
	 * @return
	 */
	public static String getQualifiedClassName(String templateName) {
		// TODO: Should the package name include the template group ? This
		// would support inheritance of identically located and named templates 
		// across templategroups.
		return getPackageName() + '.' + getClassName(templateName);
	}

	/**
	 * Return the java package name for the generated java files
	 * 
	 * @return
	 */
	public static String getPackageName() {
		return TEMPLATE_PACKAGE_NAME;
	}

	/**
	 * Return the package directory for the generated class files
	 * 
	 * @return
	 */
	public static String getPackageDir() {
		return TEMPLATE_PACKAGE_NAME.replace('.', '/');
	}

	/**
	 * Literalize a string that may have escape characters that Java recognizes
	 * For instance if a string has a \n in it, it needs to be escaped so that
	 * the java source file containing the string can compile correctly.
	 * 
	 * @param s
	 * @return
	 */
	public static String literalize(String s) {
		// TODO: fix this so that we do not iterate over the string multiple
		// times through the string
		// Better yet, update in the grammar so that we do not have to
		// literalize the text here.
		// May be compile patterns
		String retval = s;

		// Escape any single backslash first
		retval = retval.replace("\\", "\\\\");

		// Follow up with other escape characters
		retval = retval.replace("\t", "\\t");
		retval = retval.replace("\b", "\\b");
		retval = retval.replace("\f", "\\f");
		retval = retval.replace("\"", "\\\"");

		/*
		 * Treat \r\n \r and \n specially by adding a real line break and
		 * continuing the quoted literal in the next line TODO: fix /n/n
		 * replacement
		 */
		/*
		 * // replace any \n that is not preceeded by a \r retval =
		 * retval.replaceAll("([^\r])\n", "$1" + SLASH_N_REGEX_REPLACE); //
		 * replace any \n at the beginning of input (missed by the regex above)
		 * retval = retval.replaceAll("^\n", SLASH_N_REGEX_REPLACE); // replace
		 * any \r that is not followed by a \n retval =
		 * retval.replaceAll("\r([^\n])", SLASH_R_REGEX_REPLACE + "$1"); //
		 * replace any \r at the end of input - missed by above retval =
		 * retval.replaceAll("\r$", SLASH_R_REGEX_REPLACE); // replace any \r\n
		 * retval = retval.replace("\r\n", SLASH_RN_LITERAL_REPLACE);
		 */

		// All together in a single line - ugly for debugging but works
		retval = retval.replace("\n", "\\n");
		retval = retval.replace("\r", "\\r");

		return retval;
	}

	/**
	 * Compile the java source and classes for a given template directory
	 * 
	 * @param templateDir
	 * @param templateName
	 * @param javaDir
	 * @param classDir
	 */
	public static void compile(File templateDir, File tmpJavaDir, File classDir) {
		compile(templateDir, tmpJavaDir, classDir, null, null, null, null);
	}

	/**
	 * Compile the java source and classes for a given template directory
	 * 
	 * @param templateDir
	 * @param tmpJavaDir
	 * @param classDir
	 * @param outWriter
	 * @param errWriter
	 * @param exceptionList
	 * @param additionalClassPath
	 */
	public static void compile(File templateDir, File tmpJavaDir,
			File classDir, PrintWriter outWriter, PrintWriter errWriter,
			List<Exception> exceptionList, String additionalClassPath) {

		File javaGenDir = new File(tmpJavaDir, getPackageDir());
		// Delete the original contents
		javaGenDir.delete();
		// Create directory structure if it does not exist
		javaGenDir.mkdirs();
		
		ArrayList<String> templateNames = new ArrayList<String>();
		List<String> errorList = new ArrayList<String>();
		scanDirectory(templateDir, templateDir, 0, javaGenDir, templateNames,
				errorList);
		for (String error: errorList) {
			errWriter.println(error);
		}
		commonCompile(templateNames, templateDir, tmpJavaDir, classDir, outWriter, errWriter, exceptionList, additionalClassPath, false);
	}

	/**
	 * Compile a single template file. Note this does not update the template
	 * map properties file.
	 * 
	 * @param templateName
	 * @param outWriter
	 * @param errWriter
	 * @param exceptionList
	 * @param additionalClassPath
	 */
	public static void compileSingle(String templateName, File templateDir,
			File tmpJavaDir, File classDir, PrintWriter outWriter,
			PrintWriter errWriter, List<Exception> exceptionList,
			String additionalClassPath) {

		File javaGenDir = new File(tmpJavaDir, getPackageDir());
		
		List<String> eList = new ArrayList<String>();
		generateJavaFile(templateDir, templateName, javaGenDir, eList);
		for (String error: eList) {
			errWriter.println(error);
		}
		
		List<String> templatesToCompile = new ArrayList<String>();
		templatesToCompile.add(templateName);
		commonCompile(templatesToCompile, templateDir, tmpJavaDir, classDir, outWriter, errWriter, exceptionList, additionalClassPath, true);
	}
	
	/**
	 * Common compile utility for compiling a template directory or a single template file
	 * @param templatesToCompile
	 * @param templateDir
	 * @param tmpJavaDir
	 * @param classDir
	 * @param outWriter
	 * @param errWriter
	 * @param exceptionList
	 * @param additionalClassPath
	 * @param spotUpdate if true then the specified template or templates will be updated in place
	 */
	private static void commonCompile(List<String> templatesToCompile, File templateDir,
			File tmpJavaDir, File classDir, PrintWriter outWriter,
			PrintWriter errWriter, List<Exception> exceptionList,
			String additionalClassPath, boolean spotUpdate) 
	{
			
		File rstlClassPath = new File(TemplateUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		
		if (!spotUpdate) {
			// Delete original contents of classdir ?
			classDir.delete();
		}

		File srcDir = new File(tmpJavaDir, getPackageDir());
		File templateMapDir = new File(classDir, getPackageDir());
		File templateMap = new File(templateMapDir,
				TEMPLATE_MAP_PROPERTY_FILENAME);
		Properties p = new Properties();
		System.out.println("Additional Classpath: " + additionalClassPath);
		String classPath = "-cp "
				+ rstlClassPath
				+ ((null != additionalClassPath) ? additionalClassPath : "");
		if (null != outWriter) {
			outWriter.println("Compiling " + templatesToCompile.size() + " source files to " + classDir.getAbsolutePath());
		}
		StringBuffer sb = new StringBuffer();
		for (String templateName : templatesToCompile) {
			File javaFile = new File(srcDir, getClassName(templateName)
					+ ".java");
			sb.append(javaFile.getAbsolutePath()).append(" ");
			p.put(templateName, getClassName(templateName));
		}
		
		String compileStr = "-5 " + classPath + " -nowarn " + sb.toString()
				+ " -d " + classDir.getAbsolutePath();
		_LOGGER.logp(Level.FINE, CLASS_NAME, "compile", "The compile string is :" + compileStr);
		System.out.println("The compile str is " + compileStr);
		Main.compile(compileStr, outWriter, errWriter);

		try {
			templateMap.createNewFile();
			if (spotUpdate) {
				FileInputStream fis = new FileInputStream(templateMap);
				p.load(fis);
				for (String templateName: templatesToCompile) {
					p.put(templateName, getClassName(templateName));
				}
				fis.close();
			}
			FileOutputStream fos = new FileOutputStream(templateMap);
			p.store(new PrintWriter(fos),
					"This file is automatically generated");
			fos.close();
		} catch (Exception e) {
			if (null != exceptionList) {
				exceptionList.add(e);
			} else {
				e.printStackTrace();
			}
		}
		
	}

	public static void generateJavaFile(File templateDir, String templateName,
			File tmpJavaDir, List<String> errorList) {
		File templateFile = new File(templateDir, templateName);
		ErrorReporter reporter = new ErrorReporter(templateFile.getAbsolutePath());
		ANTLRFileStream stream = null;
		try {
			stream = new ANTLRFileStream(templateFile.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null == stream) {
			_LOGGER.logp(Level.WARNING, CLASS_NAME, "generateJavaFile", "Failed to find template:"
					+ templateFile.getAbsolutePath());
			return;
		}
		RSTLLexer lexer = new RSTLLexer(stream);
		lexer.setErrorReporter(reporter);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		RSTLParser parser = new RSTLParser(tokens);
		parser.setErrorReporter(reporter);
		try {
			parser.rule();
		} catch (RecognitionException re) {
			re.printStackTrace();
		}
		errorList.addAll(reporter.getErrors());
		parser.generateCode(templateName, tmpJavaDir);
	}

	/**
	 * Scan a template directory for templates and generate the appropriate java class files for the matching templates
	 * Also generate the template names and a list of exception that occurred during the scan
	 * @param templateDir the root directory of the templates
	 * @param tmpJavaDir the directory where the temporary generated java files will be created
	 * @param templateNames the names of the templates that were matched and generated
	 * @param errors the list of errors during the scan
	 */
	public static void scanDirectory(String templateDir, String tmpJavaDir, List<String> templateNames, List<String> errorList) {
		File templateSrcDir = new File(templateDir);
		File genDestDir = new File(tmpJavaDir);
		scanDirectory(templateSrcDir, templateSrcDir, 0, genDestDir, templateNames, errorList);
	}
	
	/**
	 * Scan a template directory for templates and generate a java classes for any matching templates
	 * 
	 * @param templateDir - the root directory of the templates
	 * @param workingDir - the current working directory within the root directory
	 * @param level - the directory level
	 * @param tmpJavaDir - the java directory where the temporary generated files str
	 * @param templateNames - the list of template names
	 * @param errors - The list of errors
	 */
	private static void scanDirectory(File templateDir, File workingDir,
			int level, File tmpJavaDir, List<String> templateNames,
			List<String> errors) {
		File[] files = workingDir.listFiles();
		for (File candidate : files) {
			if (candidate.isDirectory()) {
				scanDirectory(templateDir, candidate, level + 1, tmpJavaDir,
						templateNames, errors);
			} else if (candidate.isFile()
					&& TEMPLATE_SUFFIX_PATTERN.matcher(candidate.getName()).matches()) {
				String templateName = candidate.getAbsolutePath();
				int index = templateName.lastIndexOf(File.separator);
				for (int ix = 0; ix < level; ix++) {
					index = templateName.substring(0, index).lastIndexOf(
							File.separator);
				}
				templateName = templateName.substring(index + 1).replace("\\",
						"/");
				templateNames.add(templateName);
				generateJavaFile(templateDir, templateName, tmpJavaDir,
						errors);
			}
		}
	}

	/**
	 * Utility method to render the given template. This utility assumes that
	 * the template class will be automatically compiled (by eclipse) by
	 * generating the source java file in a eclipse source directory.
	 * 
	 * Only used for testing in an eclipse environment with the OUTPUT_DIR set
	 * up as a source folder. This method is deprecated use
	 * <code>TemplateGroup.render()</code> instead.
	 * 
	 * @param templateName
	 * @param c
	 * @param w
	 * @param options
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws RecognitionException
	 * @deprecated
	 * 
	 *             public static void render(String templateName,
	 *             WCSTemplateContext c, Writer w) throws
	 *             ClassNotFoundException, SecurityException,
	 *             NoSuchMethodException, IllegalArgumentException,
	 *             IllegalAccessException, InvocationTargetException,
	 *             IOException, RecognitionException {
	 * 
	 *             File myfile = new File(TEMPLATE_DIR, templateName); File
	 *             outDir = new File(OUTPUT_DIR, getPackageDir()); if
	 *             (!outDir.exists()) { outDir.mkdirs(); } ANTLRFileStream
	 *             stream = new ANTLRFileStream(myfile.getAbsolutePath());
	 *             DTLLexer lexer = new DTLLexer(stream); CommonTokenStream
	 *             tokens = new CommonTokenStream(lexer); DTLParser parser = new
	 *             DTLParser(tokens);
	 * 
	 *             parser.rule(); parser.generateCode(templateName, outDir);
	 * 
	 *             String className = getPackageName() + '.' +
	 *             getClassName(templateName); Class clazz =
	 *             Class.forName(className);
	 * 
	 *             Class[] varargs = null; Object[] parms = null; Method
	 *             getInstanceMethod = clazz.getMethod("getInstance", varargs);
	 *             Method renderMethod = clazz.getMethod("render",
	 *             WCSTemplateContext.class, Writer.class); Object obj =
	 *             getInstanceMethod.invoke(null, parms);
	 *             renderMethod.invoke(obj, c, w); }
	 */

	/**
	 * Given a stack of frames find the closest parent block or rgroup
	 * 
	 * @param stack
	 * @return
	 */
	public static Statement findParentBlock(Deque stack) {
		Statement ret = null;
		Iterator<Statement> it = stack.iterator();
		while (it.hasNext()) {
			Statement s = it.next();
			if (s.getType().equals(StatementType.blockstatement.name())
					|| s.getType().equals(StatementType.rgroupstatement.name())) {
				if (s.getId().equals("__main")) {
					break;
				}
				ret = s;
				break;
			}
		}
		return ret;
	}
	
	/**
	 * Create template tags for a resource group with the specified name and definition
	 * @param rgroupName
	 * @param rgroupDef
	 * @return
	 */
	public static StringBuffer createRGroupDef(String rgroupName, Map<String, Object> rgroupDef) {
		StringBuffer rgroupBuf = new StringBuffer();
		rgroupBuf.append("{%rgroup ").append(rgroupName).append("%}\n");
		for (Map<String, String> resRef: (List<Map<String, String>>)rgroupDef.get("resourceList")) {
			if (resRef.get("type").equals("resource")) {
				rgroupBuf.append("\t{%resource.xhtml ").append(resRef.get("uri"));
				String widgetName = resRef.get("widget");
				if (null != widgetName && !widgetName.isEmpty()) {
					rgroupBuf.append(" with ").append(resRef.get("widget"));
				}
				rgroupBuf.append("%}\n");
			} else {
				rgroupBuf.append("\t").append(resRef.get("value")).append("\n");
			}
		}
		rgroupBuf.append("{%endrgroup ").append(rgroupName).append("%}");
		return rgroupBuf;
	}
}
