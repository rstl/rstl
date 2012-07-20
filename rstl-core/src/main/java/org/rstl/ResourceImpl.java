/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

public class ResourceImpl extends GenericStatementImpl implements Resource {

	private String widgetName = "";
	private String variableName = "";
	private String representationFormat = "";
	private String title = "";
	
	protected ResourceImpl(String resourceId, String format) {
		this(resourceId, null, null, null, format, 0);
	}
	/**
	 * Constructor to create a resource reference with a given identifier
	 * @param resourceId
	 */
	protected ResourceImpl(String resourceId, String format, int line) {
		this(resourceId, null, null, null, format, line);
	}
	
	protected ResourceImpl(String resourceId, String widgetName, String format, String variableName) {
		this(resourceId, widgetName, variableName, null, format, 0);
	}
	
	protected ResourceImpl(String resourceId, String widgetName, String format, String variableName, int line) {
		this(resourceId, widgetName, variableName, null, format, line);
	}
	
	/**
	 * Constructor to create a resource reference with a given identifier, which should be rendered with a specific widget and whose 
	 * attributes can be referenced in the template using the specified variable name
	 * @param resourceId the identifier of the resource with optional variables
	 * @param widgetName name of widget to render the resource
	 * @param variableName name of variable referenced in the template
	 * @param format xhtml or json
	 * @param line the line in the template file where the resource was referenced
	 */
	protected ResourceImpl(String resourceId, String widgetName, String variableName, String title, String format, int line) {
		super(StatementType.resourcestatement, resourceId, line);
		if (null != widgetName && !widgetName.isEmpty()) {
			this.widgetName = widgetName;
		}
		if (null != variableName && !variableName.isEmpty()) {
			this.variableName = variableName;
		}
		if (null != title && !title.isEmpty()) {
			this.title = title;
		}
		representationFormat = format;
	}
	
	public String getVariableName() {
		// TODO Auto-generated method stub
		return variableName;
	}

	public String getWidgetName() {
		// TODO Auto-generated method stub
		return widgetName;
	}
	
	public String getRepresentationFormat() {
		return representationFormat;
	}
	
	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append(':').append(widgetName).append(':').append(variableName).append(':').append(title);
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			ResourceImpl tObj = (ResourceImpl) obj;
			if (null != widgetName && !(widgetName.equals(tObj.widgetName))) {
				return false;
			} 
			if (null == widgetName && null != tObj.widgetName) {
					return false;
			}
			if (null != variableName && !(variableName.equals(tObj.variableName))){
					return false;
			}
			if (null == variableName && null != tObj.variableName) {
				return false;
			}
			return true;
		}
		return false;
	}
}
