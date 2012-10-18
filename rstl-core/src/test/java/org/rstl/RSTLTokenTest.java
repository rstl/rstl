package org.rstl;

import java.io.File;
import java.io.IOException;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

public class RSTLTokenTest {

	@Test
	public void testTokens() {
		File templateFile = new File("templates/tokentest/base.html");
		ErrorReporter reporter = new ErrorReporter(templateFile.getAbsolutePath());

		ANTLRFileStream stream = null;
		try {
			stream = new ANTLRFileStream(templateFile.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null == stream) {
			return;
		}
		RSTLLexer lexer = new RSTLLexer(stream);
		lexer.setErrorReporter(reporter);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		for (Object token:tokens.getTokens()) {
			System.out.println("Token: " + token);
		}
		RSTLParser parser = new RSTLParser(tokens);
		parser.setErrorReporter(reporter);
		try {
			parser.rule();
		} catch (RecognitionException re) {
			re.printStackTrace();
		}

	}
}
