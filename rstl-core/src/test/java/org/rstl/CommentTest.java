package org.rstl;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.rstl.context.TemplateContext;
import org.rstl.context.TemplateContextImpl;

public class CommentTest {
	TemplateGroup tg = new TemplateGroup("templates", "target/testclasses", "target/testclasses");
	
	@Test
	public void testComments() {
		String expectedString = "<html>\n<head>\n\t\t<link href=\"foobar\"/>\n</head>\n</html>";
		Map<String, Object> foo = new HashMap<String, Object>();
		TemplateContext ctxt = new TemplateContextImpl(foo, tg);
		StringWriter w = new StringWriter();
		tg.render("commenttest/test1.html", ctxt, w);
		assertEquals("Template output incorrect", expectedString, w.toString());
	}
}
