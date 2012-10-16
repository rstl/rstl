/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.rstl.context.TemplateContextImpl;

public class TemplateServiceTest {
	TemplateGroup tg = new TemplateGroup("templates", "target/testclasses", "target/testclasses");
	
	
	@Test
	public void testTemplateGeneration() throws RecognitionException {

		String templateName = "templateservicetest/home/custom.html";
		// Create context for the template to render
		Map<String, Object> foo = new HashMap<String, Object>();
		List<String> names = Arrays.asList("Apples", "Bananas", "Carrots");
		foo.put("List", names);

		StringWriter w = new StringWriter();
		TemplateContextImpl ctx = new TemplateContextImpl(foo, tg);
		tg.render(templateName, ctx, w);
		
		Template templ = tg.getTemplate(templateName);
		assertEquals("Parent template does not match", "templateservicetest/home/default.html", templ.getSuperTemplateName());
		
		//Expected output
		StringWriter w2 = new StringWriter();
		tg.render("templateservicetest/home/expectedcustom.html", null, w2);
		assertEquals("Output of template templateservicetest/home/xyz.ctl does not match1", w2.toString(), w.toString());
		
		Template template = tg.getTemplate(templateName);
		StringWriter w3 = new StringWriter();
		template.render(ctx, w3, false);
		assertEquals("Output of template templateservicetest/home/custom.html does not match2", w2.toString(), w3.toString());
		
		assertEquals("Template name does not match", templateName, template.getTemplateName());
		assertEquals("Template blocks mismatch", 3, template.getBlockRefs().size());
		// Check each block ref
		assertEquals("Template preconditions do not match", "google", template.getPreconditions().get(0));
	}
	
	@Test
	public void testTemplateSelection() {
		
		String resourceName = "templateservicetest/home";
		Map<String, List<String>> tMap = tg.getTemplateMap();
		List<String> tList = tMap.get(resourceName);
		assertEquals("Does not match the number of templates for home resource", 4, tList.size());
		
		List<String> templates = tg.getPrioritizedTemplates(resourceName);
		assertEquals("Does not match the number of prioritzed templates for home resource", 2, templates.size());
		assertEquals("Does not match the prioritized template", "custom.html", templates.get(0));
		assertEquals("Does not match the prioritized template", "default.html", templates.get(1));

	}
	

	
	@Test
	public void updateTemplateDefinitionTest() throws IOException {
		Map<String, Object> init = new HashMap<String, Object>();
		init.put("storeid", "10101");
		TemplateContextImpl ctx = new TemplateContextImpl(init, tg);
		StringWriter w3 = new StringWriter();
		
		// Create an update for "centerspot" with just one resource
		Map<String, String> resourceDef = new HashMap<String, String>();
		resourceDef.put("type", "resource");
		resourceDef.put("uri", "/resources/stores/{storeid}/home");
		resourceDef.put("widget", "dropdown");
		List<Map<String, String>> resourceList = new ArrayList<Map<String, String>>();
		resourceList.add(resourceDef);
		Map<String, Object> rgroupDef = new HashMap<String, Object>();
		rgroupDef.put("type", "rgroup");
		rgroupDef.put("name", "centerspot");
		rgroupDef.put("resourceList", resourceList);
		List<Map<String, Object>> templateUpdates = new ArrayList<Map<String, Object>>();
		templateUpdates.add(rgroupDef);
		// Add a new rgroup "newspot"
		rgroupDef = new HashMap<String, Object>();
		rgroupDef.put("type", "rgroup");
		rgroupDef.put("name", "newspot");
		rgroupDef.put("resourceList", resourceList);
		templateUpdates.add(rgroupDef);
		
		// Start with known state
		File srcFile = new File(tg.getTemplateSrcDir(), "templateservicetest/guest.html");
		
		File templateDir = new File("target/templatetmp");
		templateDir.delete();
		templateDir.mkdir();
		// Wire up a parent-child relationship with the default template group
		TemplateGroup tg2 = new TemplateGroup("target/templatetmp", "target/templatetmp", "target/templatetmp", "templatetmp", tg);
				
		File destFile = new File(tg2.getTemplateSrcDir(), "testguest.html");
		destFile.createNewFile();
		//destFile.deleteOnExit();
		FileInputStream fis = new FileInputStream(srcFile);
		FileOutputStream fos = new FileOutputStream(destFile);
		byte[] buf = new byte[8192];
		int readLen = 0;
		while ((readLen = fis.read(buf)) != -1) {
			fos.write(buf, 0, readLen);
		}
		fis.close();
		fos.close();
		tg2.update();
		tg2.updateTemplateDefinition("testguest.html", templateUpdates);
		tg2.render("testguest.html", ctx, w3);
		StringWriter expected = new StringWriter();
		tg.render("templateservicetest/expectedtestguest.html", ctx, expected);
		assertEquals("template output does not match", expected.toString(), w3.toString());
	}
	

}

