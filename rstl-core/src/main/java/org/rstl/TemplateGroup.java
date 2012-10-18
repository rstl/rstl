/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rstl.context.TemplateContext;
import org.rstl.context.TemplateContextImpl;

/**
 * Template compilation and rendering functionality for a group of colocated
 * templates.
 */
public class TemplateGroup {
	private static final String CLASS_NAME = TemplateGroup.class.getCanonicalName();
	private static final Logger _LOGGER = Logger.getLogger(CLASS_NAME);
	static final private String TEMPLATE_PRIORITY_FILENAME = "templateorder.properties";
	static final private String STOREROOT = "store/";
	private static final int MAX_BUF_SIZ = 16384;

	
	private String name = "";
	private File templateSrcDir;

	private File genTmpDir;

	private File templateClassDir;
	private TemplateClassLoader templateClassLoader;
	
	// TODO: make this a indirect relationship through TemplateGroupRegistry
	private TemplateGroup parentTemplateGroup;
	private List<Exception> exceptionList = new ArrayList<Exception>();
	private ByteArrayOutputStream compileOut = new ByteArrayOutputStream(),
			compileErr = new ByteArrayOutputStream();
	private Properties templateClassMap = new Properties();
	private Map<String, List<String>> templateMap;
	private Map<String, List<String>> prioritizedTemplateMap;
	private Set<String> storeList;
	private Map<String, Template> templateObjectCache = Collections.synchronizedMap(new HashMap<String, Template>());

	/**
	 * Create a template group from a template source directory
	 * @param templateSrcDir
	 */
	public TemplateGroup(String templateSrcDir) {
		this(templateSrcDir, null, null, null);
		// Auto generate
		// Compile and create a class loader
	}

	/**
	 * Create a template group from a template src, class and java directory
	 * @param templateSrcDir
	 * 			the templates to be used to define the template group
	 * @param classDir
	 * 			the directory location where the template classes will be
	 *          generated and loaded from. If null a generated temp directory
	 *          will used
	 * @param tmpJavaDir
	 * 			the temporary directory where the java code for the templates
	 *          are generated. If null, a generated temp directory will be
	 *          used. This directory will be deleted on exit.
	 */
	public TemplateGroup(String templateSrcDir, String classDir, String tmpJavaDir) {
		this(templateSrcDir, classDir, tmpJavaDir, "", null);
	}
	
	/**
	 * 
	 * @param templateSrcDir
	 * 			the templates to be used to define the template group
	 * @param classDir
	 * 			the directory location where the template classes will be
	 *          generated and loaded from. If null a generated temp directory
	 *          will used
	 * @param tmpJavaDir 
	 *  		the temporary directory where the java code for the templates
	 *          are generated. If null, a generated temp directory will be
	 *          used. This directory will be deleted on exit.
	 * @param name 
	 * 			the name of the template group
	 */
	public TemplateGroup(String templateSrcDir, String classDir, String tmpJavaDir, String name) {
		this(templateSrcDir, classDir, tmpJavaDir, name, null);
	}
	
	/**
	 * Create a template group with the templates in the
	 * <code>templateSrcDir</code> and compile with template inheritance from
	 * the <code>parent</code> template group. The parent relationship allows
	 * inheritance relationships among templates as well as the ability to
	 * satisfy template rendering requests that may not be in the subclassed
	 * template group.
	 * 
	 * @param templateSrcDir
	 *            the templates to be used to define the template group
	 * @param classDir
	 *            the directory location where the template classes will be
	 *            generated and loaded from. If null a generated temp directory
	 *            will used
	 * @param tmpJavaDir
	 *            the temporary directory where the java code for the templates
	 *            are generated. If null, a generated temp directory will be
	 *            used. This directory will be deleted on exit.
	 *  @param name
	 *            the name of the template group
	 *  @param parent
	 *            the template group that will be used to satisfy inheritance
	 *            relationships and requests for missing templates
	 * 
	 */
	public TemplateGroup(String templateSrcDir, String classDir,
			 String tmpJavaDir, String name, TemplateGroup parent) {
		this.name = name;
		this.templateSrcDir = new File(templateSrcDir);
		if (null == tmpJavaDir) {
			try {
				genTmpDir = File.createTempFile("ctlc", null);
			} catch (Exception ex) {
				_LOGGER.logp(Level.SEVERE, CLASS_NAME, "constructor", "Failed to create java tmp dir");
				throw new IllegalArgumentException(
						"Failed to create java temporary directory");
			}
		} else {
			genTmpDir = new File(tmpJavaDir);
		}

		if (null == classDir) {
			try {
				templateClassDir = File.createTempFile("ctlj", null);
			} catch (Exception ex) {
				_LOGGER.logp(Level.SEVERE, CLASS_NAME, "constructor", "Failed to create template class dir");
				throw new IllegalArgumentException(
						"Failed to create a temporary class directory");
			}
		} else {
			templateClassDir = new File(classDir);
		}
		// This relationship should probably be indirect so that an update to the parent template group picks up the updated parent group here
		this.parentTemplateGroup = parent;
		update();

	}
	
	
	public String getTemplateSrcDir() {
		return templateSrcDir.getAbsolutePath();
	}

	public String getGenTmpDir() {
		return genTmpDir.getAbsolutePath();
	}

	public String getTemplateClassDir() {
		return templateClassDir.getAbsolutePath();
	}

	public TemplateClassLoader getTemplateClassLoader() {
		return templateClassLoader;
	}

	public TemplateGroup getParentTemplateGroup() {
		return parentTemplateGroup;
	}
	
	public Map<String, List<String>> getTemplateMap() {
		return Collections.unmodifiableMap(templateMap);
	}
	
	public Set<String> getStoreList() {
		return Collections.unmodifiableSet(storeList);
	}
	
	/**
	 * Render a pre-compiled template using the ClassLoader provided.
	 * The classloader is initialized with the location of the class dir used during the compile phase.
	 * 
	 * @param templateName
	 * @param c
	 * @param w
	 */
	public void render(String templateName, TemplateContext c, Writer w) {
		Template template = getTemplate(templateName);		
		// Render the template if we have an instance of it.
		if (null != template) {
			// TODO: timing, statistics (count)
			if (null == c) {
				c = new TemplateContextImpl(new HashMap<String, Object>(), this);
			}
			template.render(c, w, false);
		} else {
			// TODO: statistics., error page ??
			_LOGGER.logp(Level.SEVERE, CLASS_NAME, "render", "Failed to render template " + templateName);
		}		
	}
	
	public Set<Object> getTemplateList() {
		Set<Object> ret = null;
		ret = templateClassMap.keySet();
		return ret;
	}
	
	/**
	 * Render precompiled template with template metadata embedded in the rendered output
	 * @param templateName
	 * @param c
	 * @param w
	 */
	public void renderWithMetadata(String templateName, TemplateContext c, Writer w) {
		Template template = getTemplate(templateName);		
		// Render the template if we have an instance of it.
		if (null != template) {
			// TODO: timing, statistics (count)
			template.render(c, w, true);
		} else {
			// TODO: statistics., error page ??
			_LOGGER.logp(Level.SEVERE, CLASS_NAME, "renderWithMetadata", "Failed to render template " + templateName);
		}				
	}
	
	/**
	 * Return the template object singleton of type <code>WCSTemplate</code> associated with specified templateName
	 * @param templateName
	 * @return WCSTemplate 
	 */
	public Template getTemplate(String templateName) {
		String className = TemplateUtil.getPackageName() + '.' + TemplateUtil.getClassName(templateName);
		// Check cache - templates are singletons
		Template template = templateObjectCache.get(templateName);
		if (null == template) {
			Class<?> clazz;
			Method getInstanceMethod;
			try {
				clazz = Class.forName(className, true, templateClassLoader);
				Class<?>[] varargs = null;
				Object[] parms = null;
				getInstanceMethod = clazz.getMethod("getInstance", varargs);
				Object obj = getInstanceMethod.invoke(null, parms);
				template = (Template) obj;
				templateObjectCache.put(templateName, template);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				_LOGGER.logp(Level.SEVERE, CLASS_NAME, "getTemplate", "Check if java file ("+ TemplateUtil.getClassName(templateName) +
						" got generated or has errors at " + getGenTmpDir() + " can be compiled");
			}
		}
		return template;
	}
	
	public List<String> getPrioritizedTemplates(String resourceName) {
		List<String> templates = prioritizedTemplateMap.get(resourceName);
		if (templates != null) {
			templates = Collections.unmodifiableList(templates);
		}
		return templates;
	}
	
	/**
	 * Update the template group by deleting any temporary java and class directories and recompiling the templates in this group.
	 * This method expects that any parent template groups have already been updated and compiled ahead of this template group and will use 
	 * the compiled class directories from those template groups so that it is available for compiling the templates in this group.
	 */
	public void update() {
		genTmpDir.delete();
		genTmpDir.mkdir();
		templateClassDir.delete();
		templateClassDir.mkdir();
		// Generate java files

		// TODO: compute additional classpath elements from parent group
		String additionalClassPath = null;
		TemplateGroup ancestor = parentTemplateGroup;
		while (null != ancestor) {
			additionalClassPath = File.pathSeparator + ancestor.getTemplateClassDir();
			ancestor = ancestor.parentTemplateGroup;
		}
		_LOGGER.logp(Level.FINE, CLASS_NAME, "update", "The additional class path is computed as " + additionalClassPath);
		TemplateUtil.compile(this.templateSrcDir, genTmpDir, templateClassDir,
				new PrintWriter(compileOut), new PrintWriter(compileErr),
				exceptionList, additionalClassPath);

		if (compileErr.size()  > 0) {
			_LOGGER.logp(Level.WARNING, CLASS_NAME, "update", "Errors in templates");
			System.err.print(compileErr);
		}
		_LOGGER.logp(Level.INFO, CLASS_NAME, "update", compileOut.toString());
		File templateMapDir = new File(templateClassDir, TemplateUtil
				.getPackageDir());
		File templateMapFile = new File(templateMapDir,
				TemplateUtil.TEMPLATE_MAP_PROPERTY_FILENAME);
		templateClassMap = new Properties();
		try {
			FileInputStream fis = new FileInputStream(templateMapFile);
			templateClassMap.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}
		createTemplateMaps();
		
		templateObjectCache.clear();

		templateClassLoader = new TemplateClassLoader(templateClassDir
				.getAbsolutePath(), (null == parentTemplateGroup) ? null
				: parentTemplateGroup.getTemplateClassLoader());
	}
	
	/**
	 * Update a single template named by the template name
	 * @param templateName
	 */
	public void updateSingleTemplate(String templateName) {
		String additionalClassPath = null;
		TemplateGroup ancestor = parentTemplateGroup;
		while (null != ancestor) {
			additionalClassPath = File.pathSeparator + ancestor.getTemplateClassDir();
			ancestor = ancestor.parentTemplateGroup;
		}
		ByteArrayOutputStream cout = new ByteArrayOutputStream(),	cerr = new ByteArrayOutputStream();
		TemplateUtil.compileSingle(templateName, this.templateSrcDir, genTmpDir, templateClassDir, 
				new PrintWriter(cout), new PrintWriter(cerr), exceptionList, additionalClassPath);
		if (cerr.size()  > 0) {
			_LOGGER.logp(Level.SEVERE, CLASS_NAME, "updateSingleTemplate", "Errors in compile:" + cerr);
		}
		_LOGGER.logp(Level.INFO, CLASS_NAME, "updateSingleTemplate", "Compilation output:" + cout);
		
		createTemplateMaps();
		templateObjectCache.clear();

		templateClassLoader = new TemplateClassLoader(templateClassDir
				.getAbsolutePath(), (null == parentTemplateGroup) ? null
				: parentTemplateGroup.getTemplateClassLoader());
	}
	
	private void createTemplateMaps() {
		templateMap = new HashMap<String, List<String>>();
		Set<String> templateNames =  templateClassMap.stringPropertyNames();
		for (String templateName: templateNames) {
			int baseIndex = templateName.lastIndexOf('/');
			String resourceName, resourceTemplate;
			if (baseIndex != -1) {
				resourceName = templateName.substring(0, baseIndex);
				resourceTemplate = templateName.substring(baseIndex + 1);
			} else {
				resourceName = "";
				resourceTemplate = templateName;
			}
			List<String> resourceTemplates = templateMap.get(resourceName);
			if (null == resourceTemplates) {
				resourceTemplates = new ArrayList<String>();
				templateMap.put(resourceName, resourceTemplates);
			}
			resourceTemplates.add(resourceTemplate);
		}
		prioritizedTemplateMap = new HashMap<String, List<String>>();
		storeList = new HashSet<String>();
		for (String resourceName :templateMap.keySet()) {
			File resDir = new File(templateSrcDir, resourceName);
			File priorityFile = new File(resDir, TEMPLATE_PRIORITY_FILENAME);
			Properties templOrder = new Properties();
			if (priorityFile.exists()) {
				try {
					templOrder.load(new FileInputStream(priorityFile));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			List<String> availableTemplates = templateMap.get(resourceName);
			List<String> orderedTemplates = new ArrayList<String>();
			if (!templOrder.isEmpty()) {
				Set<String> orderedList = templOrder.stringPropertyNames();
				for (String orderedTemplateName : orderedList) {
					if (availableTemplates.contains(orderedTemplateName)) {
						orderedTemplates.add(orderedTemplateName);
					}
				}	
			} else {
				Collections.sort(availableTemplates);
				orderedTemplates = availableTemplates;
			}
			prioritizedTemplateMap.put(resourceName, orderedTemplates);
			String store = getStoreFromTemplateName(resourceName);
			if (null != store && !store.equals("default")) {
				storeList.add(store);
			}
		}
	}
	
	public Template storeTemplateMatch( String storeId, String resName, TemplateContext ctxt, Writer w) {
		List<String> rNames = new ArrayList<String>();
		if (getStoreList().contains(storeId)) {
			StringBuilder sb = new StringBuilder(STOREROOT);
			sb.append(storeId).append('/').append(resName);
			rNames.add(sb.toString());
		}
		rNames.add(STOREROOT + "default/" +resName);
		
		Template t = findEligibleTemplate(rNames, ctxt);

		if (null == t) {
			// Check parent template group
			if (null != parentTemplateGroup) {
				return parentTemplateGroup.storeTemplateMatch(storeId, resName, ctxt, w);
			}
			return null;
		}
		// Render template
		_LOGGER.logp(Level.FINE, CLASS_NAME, "storeTemplateMatch", "Matched template " + t.getTemplateName() + " for uri " + ctxt.get("HttpRequestURI") + " from template group" + templateSrcDir.getAbsolutePath());
		return t;
	}
	
	/**
	 * Update the definition of the template specified.
	 * The template source file will be updated, recompiled and the classes in the template group will be reloaded.
	 * On error, this method should return the list of errors associated.
	 * @param fullTemplateName
	 * 			The name of template relative to the template root.
	 * @param templateUpdates
	 * 			A list of updates to the templates. Each update should include the "type" of update which may be "rgroup" or "layout".
	 *          If the type is rgroup, then the attribute "name" specifies the rgroup to be updated and the attribute "resourceList" specifies
	 *          a list of resource references. Each resource reference has a "type" which specifies if this is a real "resource" or a "inline" xhtml fragment.
	 *          A type "resource" implies that there is an attribute "uri" that specifies the generic id of the resource an an attribute "widget" which identifies
	 *          a binding to a widget that should render the resource.
	 *          A type "inline" implies an attribute called "value" which contains the inline xhtml fragment source.
	 */
	public void updateTemplateDefinition(String fullTemplateName, List<Map<String, Object>> templateUpdates) {
		Template template = getTemplate(fullTemplateName);
		
		if (null == templateUpdates) {
			System.out.println("No updates to process");
			return;
		}
		
		File templateSrcDir = new File(getTemplateSrcDir());
		File templateFile = new File(templateSrcDir, fullTemplateName);

		StringBuffer fileContents = new StringBuffer();
		if (null == template) {
			System.out.println("Failed to load template; will create new file");

		} else {
			int readlen = 0;
			try {
				char[] cbuf = new char[MAX_BUF_SIZ];
				FileReader fReader = new FileReader(templateFile);
				while ((readlen = fReader.read(cbuf))!= -1) {
					fileContents.append(cbuf, 0, readlen);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Could not read contents out file: " + templateFile.getAbsolutePath());
				return;
			}
		}

		Map<Integer, Map<String,Object>> sortedRefs = new TreeMap<Integer, Map<String, Object>>();
		// Buffer for rgroups not defined directly in template
		StringBuffer abuf = new StringBuffer();
		for (Map<String, Object> rgroupDef : templateUpdates) {
			// Process all rgroups
			if (rgroupDef.get("type").equals("rgroup")) {
				String rgroupName = (String)rgroupDef.get("name");
				SourceRef ref = null;
				if (null != template) {
					ref = template.getRGroupRef(rgroupName);
				}
				if (null == ref) {
					abuf = abuf.append(TemplateUtil.createRGroupDef(rgroupName, rgroupDef)).append("\n");
				} else {
					sortedRefs.put(ref.getStartIndex(), rgroupDef);
				}
			}
		}
		
		// Buffer for in-place edits
		StringBuffer nbuf = new StringBuffer();
		// Process any in-place edits first.
		if (null != template) {
			int start = 0;
			for(Map<String,Object> rgroupDef: sortedRefs.values()) {
				String rgroupName = (String)rgroupDef.get("name");
				SourceRef ref = template.getRGroupRef(rgroupName);
				nbuf = nbuf.append(fileContents.substring(start, ref.getStartIndex()));	
				nbuf = nbuf.append(TemplateUtil.createRGroupDef(rgroupName, rgroupDef));
				start = ref.getStopIndex() + 1;
			}
			if (start <= fileContents.length()) {
				// Append till the end of buffer.
				nbuf = nbuf.append(fileContents.substring(start));
			}
		}
		// Append any rgroups not originally not defined in template
		nbuf = nbuf.append(abuf);
		FileWriter tWriter = null;
		try {
			if (templateFile.exists()) {
				templateFile.delete();
				templateFile.createNewFile();
			} else {
				templateFile.createNewFile();
			}
			tWriter = new FileWriter(templateFile);
			tWriter.write(nbuf.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Failed to write out updates into template file :" + templateFile);
		}
		finally {
			if (tWriter != null) {
				try {
					tWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		// Update the template group so that the changes take effect
		updateSingleTemplate(fullTemplateName);
	}

	
	public String getName() {
		return name;
	}
	
	private List<String> generateTemplDirs(List<String> attrNames, List<List<String>> attrVals) {
		for(List<String> attrLevel: attrVals) {
			
		}
		return null;
	}
	
	
	private String getStoreFromTemplateName(String resourceName) {
		String store = null;
		if (resourceName.startsWith(STOREROOT)) {
			int index = resourceName.indexOf('/', STOREROOT.length() + 1);
			store = resourceName.substring(STOREROOT.length() , index);
		}
		return store;
	}
	
	private Template findEligibleTemplate(List<String> rNames, TemplateContext ctxt) {
		Template templateObj = null;
		for (String resourceName: rNames) {
			List<String> templates = getPrioritizedTemplates(resourceName);
			if (null != templates && !templates.isEmpty()) {
				// Check if preconditions apply
				for (String t: templates) {
					StringBuilder templateName = new StringBuilder(resourceName); 
					templateName.append('/').append(t).toString();
					templateObj = getTemplate(templateName.toString());
					if (PreconditionsHelper.preconditionsMatch(templateObj, ctxt)) {
						return templateObj;
					}
				}
			}
		}
		return null;
	}
}
