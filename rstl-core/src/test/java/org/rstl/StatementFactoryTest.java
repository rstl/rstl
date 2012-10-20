/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.rstl.ForLoop;
import org.rstl.StatementFactory;

public class StatementFactoryTest {
	
	StatementFactory fac = new StatementFactory();

	@Test
	public void testForStatementCreation() {
		String forloop = "x^^y^";
		ForLoop loop = fac.createForLoop(forloop);
		assertEquals("Failed to match key", "x", loop.getKey());
		assertEquals("failed to match value", null, loop.getValue());
		assertEquals("failed to match collection", "y", loop.getCollection());
		assertEquals("failed to match reversed", false, loop.getReversed());

		forloop = "x.add^y^z^";
		loop = fac.createForLoop(forloop);
		assertEquals("Failed to match key", "x.add", loop.getKey());
		assertEquals("failed to match value", "y", loop.getValue());
		assertEquals("failed to match collection", "z", loop.getCollection());
		assertEquals("failed to match reversed", false, loop.getReversed());

		forloop = "x.add^^z^reversed";
		loop = fac.createForLoop(forloop);
		assertEquals("Failed to match key", "x.add", loop.getKey());
		assertEquals("failed to match value", null, loop.getValue());
		assertEquals("failed to match collection", "z", loop.getCollection());
		assertEquals("failed to match reversed", true, loop.getReversed());

		forloop = "x^y^z^ads";
		loop = fac.createForLoop(forloop);
		assertEquals("Failed to match key", "x", loop.getKey());
		assertEquals("failed to match value", "y", loop.getValue());
		assertEquals("failed to match collection", "z", loop.getCollection());
		assertEquals("failed to match reversed", false, loop.getReversed());

	}
}
