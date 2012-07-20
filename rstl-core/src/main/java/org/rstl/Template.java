/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.rstl.context.TemplateContext;

/**
 * The interface that every generated template class will implement
 */
public interface Template {
	
	/**
	 * Return the list of variables referenced in template
	 * @return
	 */
	public List<String> getVariables();

	/**
	 * Return the set of defined blocks for this template. The list
	 * includes any blocks that are defined by a template's super class.
	 * 
	 * @return Set of Soruce
	 */
	public Set<SourceRef> getBlockRefs();
	
	/**
	 * Return the set of rgroups declared on this template. This list includes any
	 * rgroups that are declared by the template's super class.
	 * @return Set of SourceRef objects representing resource groups
	 */
	public Set<SourceRef> getRGroupRefs();

	/**
	 * Return the set of resource references declared on this template.
	 * @return Set of SourceRef objects representing resources
	 */
	public Set<SourceRef> getResourceRefs();
	
	/**
	 * Retrieve the set of block names defined in this template (includes any blocks defined
	 * in the template's inheritance hierarchy)
	 * @return Set of Strings identifying the names of the blocks
	 */
	public Set<String> getBlockNames();
	
	/**
	 * Retrieve the set of resource group names defined in this template (includes any resource 
	 * groups defined in the template's inheritance hierarchy
	 * @return Set of Strings identifying the names of the resource groups
	 */
	public Set<String> getRGroupNames();

	/**
	 * Get the list of preconditions for rendering this template.
	 * Preconditions are not inherited
	 * @return List of Strings identifying the preconditions
	 */
	public List<String> getPreconditions();
	
	/**
	 * @return the list of the names of the included templates
	 */
	public List<String> getIncludes();
	
	/**
	 * The rendering of the template using the context provided into a stream of
	 * characters in the buffered writer
	 * 
	 * @param c
	 * 			The template context
	 * @param w
	 * 			The writer to which the template will be rendered
	 * @param includeTemplateMetadata
	 * 			If true, the rendered data will contain references to the template rendering the data. Use only for internal/debug apps
	 */
	public void render(TemplateContext c, Writer w, boolean includeTemplateMetadata);
	
	/**
	 * Render this template as a layout. This means that any block or rgroup definitions should first be resolved against the
     * main template before using the current (layout) template's definition
	 * @param mainTemplate
	 * 			The template that invoked this template as a layout template
	 * @param c
	 * 			The template context
	 * @param w
	 * 			The writer to which the template will be rendered
	 * @param includeTemplateMetadata
	 * 			If true, the rendered data will contain references to the template rendering the data. Use only for internal/debug apps
	 */
	public void renderAsLayout(Template mainTemplate, TemplateContext c, Writer w, boolean includeTemplateMetadata);
	
	/**
	 * Invoke a specific method in this template. Used from layout templates to invoke blocks and rgroups
	 * @param methodName
	 * @param c
	 * @param w
	 * @param includeMetadata
	 */
	public void invokeMethod(String methodName, TemplateContext c, Writer w, boolean includeMetadata);
	
	/**
	 * Get the template name of the parent
	 */
	public String getSuperTemplateName();
	
	/**
	 * Get the name of the layout if explicitly specified or inherited from a parent
	 * @return null if there is no layout defined for this template 
	 */
	public String getLayoutTemplateName();
	
	/**
	 * @return the name of the template
	 */
	public String getTemplateName();
	
	/**
	 * Return the source ref associated with a rgroup
	 * @param name
	 * @return
	 */
	public SourceRef getRGroupRef(String name);
	
	/**
	 * Fetch any resources specifically those that are assigned to variables
	 */
	//public void resolveVariables();
	
}
