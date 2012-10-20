/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.rstl.ResourceRef;
import org.rstl.TemplateGroup;
import org.rstl.context.TemplateContextImpl;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ResourceRefTest {
	private static TemplateGroup tg = new TemplateGroup("templates", "target/testclasses", "target/testclasses");
	private static String WEBROOT = "/web";
	
	@Test
	public void testBasicResourceRef() {
		ResourceRef ref = new ResourceRef(WEBROOT + "/resources/stores/10101/home", "dropdown", "xhtml", "common");
		TemplateContextImpl ctxt = new TemplateContextImpl(null, tg);
		assertTrue("Not a bad resource name", !ref.isBadResourceName());
		assertTrue("There should be no variable references", ref.getVarRefsInId().isEmpty());
		assertTrue("Expecting that resource is resolved", ref.resolve(ctxt));
		assertEquals("Resource Id does not match", WEBROOT + "/resources/stores/10101/home", ref.getResolvedId(ctxt, false));
		
	}
	
	@Test
	public void testSimpleVarResourceRef() {
		ResourceRef ref = new ResourceRef(WEBROOT + "/resources/stores/{storeid}/home", "dropdown", "xhtml", "common");
		Map<String, Object> init = new HashMap<String, Object>();
		init.put("storeid", "10101");
		TemplateContextImpl ctxt = new TemplateContextImpl(init, tg);
		assertTrue("Not a bad resource name", !ref.isBadResourceName());
		assertEquals("Variable Ref size does not match", 1, ref.getVarRefsInId().size());
		assertEquals("Variable Ref value does not match", "storeid", ref.getVarRefsInId().get(0));
		assertEquals("Unresolved Variable Ref value does not match", "storeid", ref.getUnResolvedVarRefs().get(0));
		
		assertEquals("Resource Id does not match", WEBROOT + "/resources/stores/10101/home", ref.getResolvedId(ctxt, false));
		assertTrue("Expecting that resource is resolved", ref.resolve(ctxt));		
		
	}
	
	@Test
	public void testAdvancedVarResourceRef() {
		ResourceRef ref = new ResourceRef(WEBROOT + "/resources/stores/{storeid}/home/{foobar}", "dropdown", "xhtml", "common");
		Map<String, Object> init = new HashMap<String, Object>();
		init.put("storeid", "10101");
		TemplateContextImpl ctxt = new TemplateContextImpl(init, tg);
		assertTrue("Not a bad resource name", !ref.isBadResourceName());
		assertEquals("Variable Ref size does not match", 2, ref.getVarRefsInId().size());
		assertEquals("Variable Ref value does not match", "storeid", ref.getVarRefsInId().get(0));
		assertEquals("Variable Ref value does not match", "foobar", ref.getVarRefsInId().get(1));
		assertEquals("Unresolved Variable Ref value does not match", "storeid", ref.getUnResolvedVarRefs().get(0));
		assertEquals("Unresolved Variable Ref value does not match", "foobar", ref.getUnResolvedVarRefs().get(1));
		
		assertEquals("Resource Id does not match", "", ref.getResolvedId(ctxt, false));
		assertTrue("Expecting that resource is not resolved", !ref.resolve(ctxt));		
		
		init.put("foobar", "index");
		assertEquals("Unresolved Variable Ref size does not match", 1, ref.getUnResolvedVarRefs().size());
		assertEquals("Unresolved Variable Ref value does not match", "foobar", ref.getUnResolvedVarRefs().get(0));
		assertEquals("Resource Id does not match", WEBROOT + "/resources/stores/10101/home/index", ref.getResolvedId(ctxt, false));
		assertTrue("Expecting that resource is resolved", ref.resolve(ctxt));		
		
		ref = new ResourceRef(WEBROOT + "/resources/stores/{storeid}/{baz}bar", "", "xhtml" , "");
		assertTrue("Not a bad resource name", !ref.isBadResourceName());
		assertEquals("Resource Id does not match", WEBROOT + "/resources/stores/10101/bar", ref.getResolvedId(ctxt, true));
		assertTrue("Expecting that resource is not resolved", !ref.resolve(ctxt));		
		
	}
	
	@Test
	public void testBadResourceRef() {
		ResourceRef ref = new ResourceRef(WEBROOT + "/resources/stores/{storeid}/home/{foobar", "dropdown", "xhtml", "common");
		Map<String, Object> init = new HashMap<String, Object>();
		init.put("storeid", "10101");
		TemplateContextImpl ctxt = new TemplateContextImpl(init, tg);
		assertEquals("Variable Ref size does not match", 1, ref.getVarRefsInId().size());
		assertTrue("This is bad resource", ref.isBadResourceName());
		assertEquals("Syntax error location", 37, ref.getErrorLocation());
		assertEquals("Resource does not match up", WEBROOT + "/resources/stores/10101/home/", ref.getResolvedId(ctxt, true));
	}
	
	@Test
	@Ignore
	/* Temorarily disable test till resource registry is completed which should allow file resource resolution */
	public void testFetchingResources() throws IOException,  XPathExpressionException {
		ResourceRef ref = new ResourceRef(WEBROOT + "/resources/stores/{storeid}/home", "dropdown", "xhtml", "common");
		Map<String, Object> init = new HashMap<String, Object>();
		init.put("storeid", "10101");
		TemplateContextImpl ctxt = new TemplateContextImpl(init, tg);
		StringWriter w = new StringWriter();
		ref.fetchResource(ctxt, w, null);
		Map<String, String> info = new HashMap<String, String>();
		info.put("template", "foo");
		StringWriter wDebug = new StringWriter();
		ref.fetchResource(ctxt, wDebug, info);
		// Parse XML
		XPathFactory xpfactory = XPathFactory.newInstance();
		XPath xpath = xpfactory.newXPath();
		XPathExpression expr = xpath.compile("//div[@class=\"resourceid\"]");
		StringReader r = new StringReader(w.toString());
		InputSource is = new InputSource(r);

		Object result = expr.evaluate(is, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		assertEquals("Resource value is incorrect", WEBROOT + "/resources/stores/10101/home", nodes.item(0).getTextContent());
		assertEquals("The number of resources is incorrect", 21, nodes.getLength());
		
		expr = xpath.compile("//div[@class=\"templatemeta genericid\"]");
		nodes = (NodeList)expr.evaluate(new InputSource(new StringReader(w.toString())), XPathConstants.NODESET);
		assertEquals("The number of resources is incorrect", 0, nodes.getLength());
		
		
		expr = xpath.compile("//div[@class=\"resourceid\"]");
		nodes = (NodeList)expr.evaluate(new InputSource(new StringReader(wDebug.toString())), XPathConstants.NODESET);
		assertEquals("Resource value is incorrect", WEBROOT + "/resources/stores/10101/home", nodes.item(0).getTextContent());
		assertEquals("The number of resources is incorrect", 21, nodes.getLength());
		
		expr = xpath.compile("//div[@class=\"templatemeta genericid\"]");
		nodes = (NodeList)expr.evaluate(new InputSource(new StringReader(wDebug.toString())), XPathConstants.NODESET);
		assertEquals("Resource value is incorrect", WEBROOT + "/resources/stores/{storeid}/home", nodes.item(0).getTextContent());
		assertEquals("The number of resources is incorrect", 1, nodes.getLength());
		
		//Ensure that the key/value pairs provided to renderDebug are in the representation
		for (String key : info.keySet()) {
			expr = xpath.compile("//div[@class=\"templatemeta " + key + "\"]");
			nodes = (NodeList)expr.evaluate(new InputSource(new StringReader(wDebug.toString())), XPathConstants.NODESET);
			assertEquals("Resource value is incorrect", info.get(key), nodes.item(0).getTextContent());
			assertEquals("The number of resources is incorrect", 1, nodes.getLength());
		}
	}
	
	@Test
	public void testResourceXHTMLrender() {		
		StringWriter w = new StringWriter();
		StringWriter expectedOutput = new StringWriter();
		Map<String, Object> init = new HashMap<String, Object>();
		init.put("storeid", "10101");
		TemplateContextImpl ctxt = new TemplateContextImpl(init, tg);
		ResourceRef ref = new ResourceRef(WEBROOT + "/resources/stores/{storeid}/home", "", "xhtml", "");
		ref.fetchResource(ctxt, expectedOutput, null);
		tg.render("resourcetest/simpleresource.html", ctxt, w);
		assertEquals("Expected output from resource.xhtml does not match", expectedOutput.toString(), w.toString());
	}
	
	@Test 
	@Ignore
	/* Temporarily disable test till resource registry implementation is complete */
	public void testJSONResourceAssignment() {
		StringWriter w = new StringWriter();
		Map<String, Object> init = new HashMap<String, Object>();
		init.put("storeid", "10101");
		TemplateContextImpl ctxt = new TemplateContextImpl(init, tg);
		tg.render("resourcetest/simplejsonresource.html", ctxt, w);
		assertEquals("Expected output from resource.xhtml does not match", WEBROOT + "/resources/stores/10101/home", w.toString());
		tg.renderWithMetadata("resourcetest/simplejsonresource.html", ctxt, w);
		Map<String,Object> home = (Map<String, Object>)ctxt.get("home");
		assertTrue("Attribute home set by template is not available", null != home);
		assertEquals("Attribute resourceid unavailable", WEBROOT + "/resources/stores/10101/home", home.get("resourceid"));
		assertEquals("Attribute genericid unavailable", WEBROOT + "/resources/stores/{storeid}/home", home.get("genericid"));		
	}
	
	@Test
	public void testResourceWithWidgetXHTMLrender() {		
		StringWriter w = new StringWriter();
		StringWriter expectedOutput = new StringWriter();
		Map<String, Object> init = new HashMap<String, Object>();
		init.put("storeid", "10101");
		TemplateContextImpl ctxt = new TemplateContextImpl(init, tg);
		ResourceRef ref = new ResourceRef(WEBROOT + "/resources/stores/{storeid}/home", "", "xhtml", "");
		ref.fetchResource(ctxt, expectedOutput, null);
		tg.render("resourcetest/prodresourcewwidget.html", ctxt, w);
		System.out.println(w.toString());
		assertEquals("Expected output from resource.xhtml does not match", expectedOutput.toString(), w.toString());
	}
}
