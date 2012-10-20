/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.rstl.BlockImpl;
import org.rstl.Chunk;
import org.rstl.SourceRef;
import org.rstl.Statement;
import org.rstl.StatementFactory;
import org.rstl.TemplateClassGenerator;
import org.rstl.TemplateUtil;
import org.rstl.context.TemplateContext;
import org.rstl.context.TemplateContextImpl;

/**
 * Test whether the template class generator can correctly generate the required
 * class
 */

public class TemplateClassGeneratorTest {

	StatementFactory fac = new StatementFactory();
	
	@Test
	public void testLiteralize() {
		String val = "\"\\nfoo\n\";";
		String ret = TemplateUtil.literalize(val);
		System.out.println("Literalize:" + ret);
		assertEquals("Literalize failed to match",
				"\\\"\\\\nfoo\\n\\\";", ret);
	}

	@Test
	public void testClassPackageUtil() {
		String name = "foo";
		assertEquals("Does not match class name", "Tfoo",
				TemplateUtil.getClassName(name));

		name = "foo/bar";
		assertEquals("Does not match class name", "Tfoo__bar",
				TemplateUtil.getClassName(name));

		name = "foo.html";
		assertEquals("Does not match class name", "Tfoo_html",
				TemplateUtil.getClassName(name));

		name = "abc/def/foo";
		assertEquals("Does not match class name", "Tabc__def__foo",
				TemplateUtil.getClassName(name));

		name = "abc.xyz/def/foo.html.old";
		assertEquals("Does not match class name", "Tabc_xyz__def__foo_html_old",
				TemplateUtil.getClassName(name));
	}

	@Test
	@Ignore
	public void testBasicClassGeneration() throws Exception {
		String teststr = "adbadafdadsa";
		String templateName = "basictemplate.html";
		String className = TemplateUtil.getClassName(templateName);
		String packageDir = TemplateUtil.getPackageDir();
		File outDir = new File("target/testclasses", packageDir);
		TemplateClassGenerator gen = new TemplateClassGenerator();
		Writer w = new StringWriter();
		File javaFile = new File(outDir, className + ".java");
		if (javaFile.exists()) {
			javaFile.delete();
		}
		Writer w2 = new FileWriter(javaFile);
		Map<String, Object> dtlContext = new HashMap<String, Object>();
		dtlContext.put("name", templateName);
		dtlContext.put("className", className);
		dtlContext.put("packageName", TemplateUtil.getPackageName());
		List<Chunk> l = new ArrayList<Chunk>();
		for (int ix = 0; ix < 5; ix++) {
			Chunk m = fac.createChunk(teststr);
			l.add(m);
		}
		dtlContext.put("chunks", l);
		List<BlockImpl> blocks = new ArrayList<BlockImpl>();
		for (int ix = 0; ix < 5; ix++) {
			BlockImpl block = fac.createBlock("block" + ix);
			for (Statement s : l) {
				block.addStatement(s);
			}
			blocks.add(block);
		}
		dtlContext.put("blocks", blocks);
		dtlContext.put("main", blocks);
		gen.generate(w, dtlContext);
		gen.generate(w2, dtlContext);
		w2.flush();
		w2.close();
		System.out.println(w);

		StringWriter w3 = new StringWriter();
		Class<?> clazz = Class.forName(TemplateUtil.getQualifiedClassName(templateName));
		Map<String, Object> foo = new HashMap<String, Object>();
		List<String> names = Arrays.asList("Hello", "Abcd", "Foobar");
		foo.put("List", names);

		TemplateContextImpl ctx = new TemplateContextImpl(foo, null);
		Method getInstanceMethod = clazz.getMethod("getInstance", null);
		Method renderMethod = clazz.getMethod("render",
				TemplateContext.class, Writer.class);
		Object obj = getInstanceMethod.invoke(null, null);
		renderMethod.invoke(obj, ctx, w3);
		StringBuilder expected = new StringBuilder();
		for (int ix = 0; ix < 25; ix++) {
			expected.append(teststr);
		}

		assertEquals("Failed to match output", expected.toString(),
				w3.toString());
		Method getBlocksMethod = clazz.getMethod("getBlockRefs", null);
		Set<SourceRef> blockNames = (Set<SourceRef>) getBlocksMethod.invoke(obj,
				null);
		for (BlockImpl b : blocks) {
			assertTrue("Failed to match expected block:" + b.getId(),
					blockNames.contains(new SourceRef(b.getId(), templateName, 0)));
		}

		// Test that the template name is returned
		Method getTemplNameMethod = clazz.getMethod("getTemplateName", null);
		String tmplName = (String) getTemplNameMethod.invoke(obj, null);
		assertEquals("The template name does not match", templateName, tmplName);
	}

	@Test
	@Ignore
	public void testNestedBlocks() throws Exception {

		String teststr = "badbad";
		String templateName = "nestblocktemplate.html";
		String className = TemplateUtil.getClassName(templateName);
		String packageDir = TemplateUtil.getPackageDir();
		File outDir = new File("target/testclasses", packageDir);
		TemplateClassGenerator gen = new TemplateClassGenerator();
		Writer w = new StringWriter();
		File javaFile = new File(outDir, className + ".java");
		if (javaFile.exists()) {
			javaFile.delete();
		}
		Writer w2 = new FileWriter(javaFile);
		Map<String, Object> dtlContext = new HashMap<String, Object>();
		dtlContext.put("name", templateName);
		dtlContext.put("className", className);
		dtlContext.put("packageName", TemplateUtil.getPackageName());
		List<Chunk> l = new ArrayList<Chunk>();
		Chunk inMainBefore = fac.createChunk("inMainBefore");
		Chunk inMainAfter = fac.createChunk("inMainAfter");
		Chunk inBlock1Before = fac.createChunk("inBlock1Before");
		Chunk inBlock2 = fac.createChunk("inBlock2");
		Chunk inBlock1After = fac.createChunk("inBlock1After");
		l.add(inMainBefore);
		l.add(inBlock1Before);
		l.add(inBlock2);
		l.add(inBlock1After);
		l.add(inMainAfter);
		dtlContext.put("chunks", l);

		List<BlockImpl> blocks = new ArrayList<BlockImpl>();
		BlockImpl block2 = fac.createBlock("block2");
		block2.addStatement(inBlock2);
		BlockImpl block1 = fac.createBlock("block1");
		block1.addStatement(inBlock1Before);
		block1.addStatement(block2);
		block1.addStatement(inBlock1After);
		blocks.add(block1);
		blocks.add(block2);
		List<Statement> main = new ArrayList<Statement>();
		main.add(inMainBefore);
		main.add(block1);
		main.add(inMainAfter);
		dtlContext.put("blocks", blocks);
		dtlContext.put("main", main);

		gen.generate(w, dtlContext);
		gen.generate(w2, dtlContext);
		w2.flush();
		w2.close();
		System.out.println(w);

		StringWriter w3 = new StringWriter();
		Class<?> clazz = Class.forName(TemplateUtil.getQualifiedClassName(templateName));
		Map<String, Object> foo = new HashMap<String, Object>();
		List<String> names = Arrays.asList("Hello", "Abcd", "Foobar");
		foo.put("List", names);

		TemplateContextImpl ctx = new TemplateContextImpl(foo, null);
		Method getInstanceMethod = clazz.getMethod("getInstance", null);
		Method renderMethod = clazz.getMethod("render",
				TemplateContext.class, Writer.class);
		Object obj = getInstanceMethod.invoke(null, null);
		renderMethod.invoke(obj, ctx, w3);

		StringBuilder expected = new StringBuilder();
		for (Chunk x : l) {
			expected.append(x.getValue());
		}
		assertEquals("Failed to match output", expected.toString(),
				w3.toString());

	}

}
