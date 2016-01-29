
package org.rstl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

/**
 * Generate a template class from a template document
 */
public class TemplateClassGenerator {

	/**
	 * Generate a Java file from the standard DTL template class generator
	 * 
	 * <code>
	 * The DTLContext must have the following keys
	 * 	name 							String
	 * 	className 						String
	 *  superClassName (Optional)		String
	 *  chunks (Optional)				List<Map<String,String>>
	 *  	Map has keys : 
	 *  	name						String
	 *  	value						String
	 *  variables (Optional)			List<String>
	 *  blocks	(Optional)				List<Map<String,Object>>
	 *  	Map has keys:
	 *  	name						String
	 *  	statements					List<Map<String, Object>>
	 *  		Map has keys :
	 *  		type					String (chunkstatement, forstatement,
	 *  									blockstatement, variablestatement)
	 *  		collection				String (for forstatement)
	 *  		id						String (for forstatement)
	 *  		id						String (for variablestatement)
	 *  		id						String (for chunkstatement)
	 *  		id						String (for blockstatement)
	 *  mainstatements					List<Map<String, Object>>
	 *  	Map has keys the same as statements attribute of blocks above .
	 *  	
	 *  </code>
	 * 
	 * @param w
	 * @param dtlContext
	 * @throws IOException
	 */
	public void generate(Writer w, Map<String, Object> dtlContext)
			throws IOException {
		InputStream is =  TemplateClassGenerator.class.getResourceAsStream("rstljavatemplate.stg");
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		//Reader r = new FileReader(stgTemplateUrl.getFile());
		StringTemplateGroup group = new StringTemplateGroup(r);
		StringTemplate t = group.getInstanceOf("javaclass");
		t.setAttribute("ctxt", dtlContext);
		w.append(t.toString(80));
	}

}
