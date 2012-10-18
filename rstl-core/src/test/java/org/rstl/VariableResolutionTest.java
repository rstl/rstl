/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.rstl.context.TemplateContextImpl;

public class VariableResolutionTest {
	private static TemplateGroup tg = new TemplateGroup("templates", "target/testclasses", "target/testclasses");
	private static RSTLLexer lexer = null;
	
	@Test
	public void testVarFilter1() throws IOException, RecognitionException {
		String input = "{{myvariable.attr|urlencode}}{{value|time:\"TIME_FORMAT\"|urlencode}}";
		RSTLParser parser = createParser(input);
		parser.rule();
		List<Statement> result = parser.getMain();
		assertEquals("Failed to return variable", 2,
				result.size());
		Variable v = (Variable) result.get(0);
		assertEquals("Failed to match variable name", "myvariable.attr",	v.getVariableName());
		assertEquals("Failed to match filter size", 1,	v.getFilters().size());
		assertEquals("Failed to match filter name", "urlencode", v.getFilters().get(0));
		assertEquals("Failed to match prefix", "VarUtil.urlencode(", v.getAppliedFilterPrefixString());
		assertEquals("Failed to match suffix", ")", v.getAppliedFilterSuffixString());
		v = (Variable) result.get(1);
		assertEquals("Failed to match variable name", "value", v.getVariableName());
		assertEquals("Failed to match filter size", 2,	v.getFilters().size());
		assertEquals("Failed to match filter args size", 2, v.getFilterArgs().size());
		assertEquals("Failed to match filter1 name", "time", v.getFilters().get(0));
		assertEquals("Failed to match filter1 argument", "\"TIME_FORMAT\"", v.getFilterArgs().get(0));
		assertEquals("Failed to match filter2 name", "urlencode", v.getFilters().get(1));
		assertEquals("Failed to match filter2 argument", null, v.getFilterArgs().get(1));
	}
	
	@Test
	public void testVarFilter2() throws IOException, RecognitionException {
		String input = "{{myvariable}}";
		RSTLParser parser = createParser(input);
		parser.rule();
		List<Statement> result = parser.getMain();
		assertEquals("Failed to return variable", 1,
				result.size());
		Variable v = (Variable) result.get(0);
		assertEquals("Failed to match variable name", "myvariable",	v.getVariableName());
		assertEquals("Failed to match filter size", 0,	v.getFilters().size());
		assertEquals("Failed to match prefix", "", v.getAppliedFilterPrefixString());
		assertEquals("Failed to match suffix", "", v.getAppliedFilterSuffixString());
	}
	
	@Test
	public void testVarFilter3() throws IOException, RecognitionException {
		String input = "{{myvariable|urlencode|upper|nosuchfunc|lower}}";
		RSTLParser parser = createParser(input);
		parser.rule();
		List<Statement> result = parser.getMain();
		assertEquals("Failed to return variable", 1,
				result.size());
		Variable v = (Variable) result.get(0);
		assertEquals("Failed to match variable name", "myvariable",	v.getVariableName());
		assertEquals("Failed to match filter size", 4,	v.getFilters().size());
		assertEquals("Failed to match filter name", "urlencode", v.getFilters().get(0));
		assertEquals("Failed to match filter name", "upper", v.getFilters().get(1));
		assertEquals("Failed to match filter name", "nosuchfunc", v.getFilters().get(2));
		assertEquals("Failed to match filter name", "lower", v.getFilters().get(3));
		assertEquals("Failed to match prefix", "VarUtil.lower(VarUtil.noop(VarUtil.upper(VarUtil.urlencode(", v.getAppliedFilterPrefixString());
		assertEquals("Failed to match suffix", "))))", v.getAppliedFilterSuffixString());
	}
	
	@Test
	public void testVarUtilUrlEncode() {
		String foobar = "string with spaces";
		assertEquals("urlencode does not seem to do the right thing", "string%20with%20spaces", VarUtil.urlencode(foobar));	
	}
	

	
	@Test
	public void testVariableArraysAndLists() {
		String templateName = "variabletest/variabletemplates.html";
		String expectedResult ="{String array default 0: foobar}" +
				"{String list default 0: barbar}" +
				"{String list list default 0 : barbar}" +
				"{String array explicit 0: foobar}" +
				"{String array explicit 1: foobaz}" +
				"{String list explicit 0 : barbar}" +
				"{String list explicit 1 : barbaz}" +
				"{String list list explicit implicit 0: barbar}" +
				"{String list list explicit implicit 1: foobar}" +
				"{String list list explicit expliict 0 0: barbar}" +
				"{String list list explicit explicit 0 1: barbaz}" +
				"{String list list explicit expliict 1 0: foobar}" +
				"{String list list explicit expliict 1 1: foobaz}" +
				"{Map String list list default 0: barbar}" +
				"{Map String list list explicit 1 0: foobar}" +
				"{List Map default : map1value}" +
				"{List Map explicit 0: map1value}" +
				"{List Map explicit 1: map2value}";
		
		
		String[] stringArray = {"foobar", "foobaz"};
		List<String> stringList = new ArrayList<String>();
		stringList.add("barbar");
		stringList.add("barbaz");
		List<Object> stringListList = new ArrayList<Object> ();
		stringListList.add(stringList);
		stringListList.add(stringArray);
		Map<String, List<Object>> mymap = new HashMap<String, List<Object>>();
		mymap.put("mykey", stringListList);
		List<Map<String, String>> maplist = new ArrayList<Map<String, String>>();
		Map<String,String> simpleMap = new HashMap<String, String>();
		simpleMap.put("map1key", "map1value");
		maplist.add(simpleMap);
		simpleMap = new HashMap<String, String>();
		simpleMap.put("map2key", "map2value");
		maplist.add(simpleMap);
		
		Map<String, Object> foo = new HashMap<String, Object>();
		foo.put("strarr", stringArray);
		foo.put("strlist", stringList);
		foo.put("strlistlist", stringListList);
		foo.put("amap", mymap);
		foo.put("alist", maplist);

		StringWriter w = new StringWriter();
		TemplateContextImpl ctx = new TemplateContextImpl(foo, tg);
		tg.render(templateName, ctx, w);

		assertEquals("template result does not match", expectedResult, w.toString());
	}
	
	@Test
	public void testBlockVariableSimple() {
		String templateName = "variabletest/test1.html";
		String expectedResult= "<html>\n<head>\n	<title>Foobar</title>\n</head>\n\n<body>\nFoobar\n</body>\n</html>";
		Map<String, Object> foo = new HashMap<String, Object>();
		foo.put("foobar", "FooBar");

		StringWriter w = new StringWriter();
		TemplateContextImpl ctx = new TemplateContextImpl(foo, tg);
		tg.render(templateName, ctx, w);

		assertEquals("template result does not match", expectedResult, w.toString());
	}
		
	private RSTLParser createParser(String testString) throws IOException {
		CharStream stream = new ANTLRStringStream(testString);
		lexer = new RSTLLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		RSTLParser parser = new RSTLParser(tokens);
		return parser;
	}

}
