/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.rstl.context.TemplateContextImpl;

public class BasicFunctionTest {
	TemplateGroup tg = new TemplateGroup("templates", "target/testclasses", "target/testclasses");
	
	@Test
	public void testSimpleTemplate() throws IOException, RecognitionException,
			SecurityException, IllegalArgumentException,
			ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		String templateName = "basicfunctiontest/base.html";

		// Create context for the template to render
		Map<String, Object> foo = new HashMap<String, Object>();
		List<String> names = Arrays.asList("Hello", "Abcd", "Foobar");
		foo.put("List", names);

		StringWriter w = new StringWriter();
		TemplateContextImpl ctx = new TemplateContextImpl(foo, tg);
		tg.render(templateName, ctx, w);

		StringWriter w2 = new StringWriter();

		tg.render("basicfunctiontest/expectedbase.html", ctx, w2);
		assertEquals("Template output does not match", w2.toString(),
				w.toString());
		
	}

	@Test
	public void testExtendTemplate() throws IOException, RecognitionException,
			SecurityException, IllegalArgumentException,
			ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		String templateName = "basicfunctiontest/extended.html";
		Map<String, Object> foo = new HashMap<String, Object>();
		List<String> names = Arrays.asList("Hello", "Abcd", "Foobar");
		foo.put("List", names);

		StringWriter w = new StringWriter();
		TemplateContextImpl ctx = new TemplateContextImpl(foo, tg);
		tg.render(templateName, ctx, w);

		StringWriter w2 = new StringWriter();
		tg.render("basicfunctiontest/expectedextended.html", ctx, w2);
		assertEquals("Extended template does not match up", w2.toString(),
				w.toString());
	}
	
	@Test
	public void testRGroupDeclaration() throws IOException {
		String templateName = "basicfunctiontest/base.html";
		Map<String, Object> foo = new HashMap<String, Object>();
		List<String> names = Arrays.asList("Hello", "Abcd", "Foobar");
		foo.put("List", names);

		
		Template t = tg.getTemplate(templateName);
		SourceRef eRef = t.getRGroupRef("adspot");
		assertTrue("rgroup decl start index not correct", eRef.getStartIndex() > 0);
		assertTrue("rgroup decl stop index not correct", eRef.getStopIndex() > 0);
		File tDir = new File(tg.getTemplateSrcDir());
		File tSrc = new File(tDir, templateName);
		RandomAccessFile f = new RandomAccessFile(tSrc, "r");
		f.seek(eRef.getStartIndex());
		assertTrue("Start index should be less than stop index", eRef.getStartIndex() < eRef.getStopIndex());
		int len = eRef.getStopIndex() - eRef.getStartIndex() + 1;
		byte[] bytes = new byte[len];
		f.readFully(bytes);
		String s = new String(bytes);
		assertEquals("Incorrect rgroup declaration", "{% rgroup adspot%}\n		{%resource.xhtml /myresource/{x} %}\n	{% endrgroup %}", s);
		
	}

	@Test
	public void testVariableReferences() throws IOException {
		String templateName = "basicfunctiontest/extended.html";
		Map<String, Object> foo = new HashMap<String, Object>();
		List<String> names = Arrays.asList("Hello", "Abcd", "Foobar");
		foo.put("List", names);

		StringWriter w = new StringWriter();
		TemplateContextImpl ctx = new TemplateContextImpl(foo, tg);
		Template t = tg.getTemplate(templateName);
		assertEquals("Failed to get the right number of variables", 2, t.getVariables().size());
		assertEquals("Failed to get the right variable", "List(x)", t.getVariables().get(0));
		assertEquals("Failed to get the right variable", "x", t.getVariables().get(1));
		t.render(ctx, w, false);
		System.out.println("Output\n" +w.toString());
	}
	
}
