

package org.rstl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.rstl.Chunk;
import org.rstl.StatementFactory;
public class ChunkTest {
	StatementFactory fac = new StatementFactory();
	
	@Test
	public void testEmptyChunk() {
		Chunk ch = fac.createChunk("");
		assertEquals("Chunk not empty as expected", "", ch.getValue());
		ch.trimToLastLine();
		assertEquals("Chunk not empty as expected", "", ch.getValue());
	}
	
	@Test
	public void testStandardChunk() {
		String testValue = "Hello There";
		Chunk ch = fac.createChunk(testValue);
		assertEquals("Chunk does not have expected standard value", testValue, ch.getValue());
		ch.trimToLastLine();
		assertEquals("Chunk does not have expected standard value ", testValue, ch.getValue());
	}
	
	@Test
	public void testNonStandardChunk() {
		String value = "Hello There";
		String testValue = value + "\r\n\t\t\n\b\"\\";
		String expectedValue = value + "\\r\\n\\t\\t\\n\\b\\\"\\\\"; 
		Chunk ch = fac.createChunk(testValue);
		assertEquals("Chunk does not have expected standard value", expectedValue, ch.getValue());
		ch.trimToLastLine();
		assertEquals("Chunk does not have expected standard value ", expectedValue, ch.getValue());
		
		//Add linefeed to the end of string
		testValue = testValue + "\n";
		expectedValue = expectedValue + "\\n";
		ch = fac.createChunk(testValue);
		assertEquals("Chunk does not have expected standard value", expectedValue, ch.getValue());
		ch.trimToLastLine();
		assertEquals("Chunk value should not change", expectedValue, ch.getValue());
		
		// Add whitespace to the end of string
		String newValue = testValue + " \t";
		String newexpectedValue = expectedValue + " \\t";
		ch = fac.createChunk(newValue);
		assertEquals("Chunk does not have expected standard value", newexpectedValue, ch.getValue());
		ch.trimToLastLine();
		assertEquals("Chunk value should not change", expectedValue, ch.getValue());
		
	}
	
	@Test
	public void testTrimming() {
		String  testvalue = "Hello there \n   \t\t\t\t  ";
		String expectedValue = "Hello there \\n";
		
		Chunk ch = fac.createChunk(testvalue);
		ch.trimToLastLine();
		assertEquals("Trimmed value of chunk not correct", expectedValue, ch.getValue());
		
		testvalue ="\n\t\t\t";
		expectedValue = "\\n";
		
		ch = fac.createChunk(testvalue);
		ch.trimToLastLine();
		assertEquals("Trimmed value of chunk not correct", expectedValue, ch.getValue());
		
		testvalue ="\t\t\t";
		expectedValue = "\\t\\t\\t";
		
		ch = fac.createChunk(testvalue);
		ch.trimToLastLine();
		assertEquals("Trimmed value of chunk not correct", expectedValue, ch.getValue());
	}

	@Test
	public void testHidingLF() {
		String testvalue ="\n\t\t\t";
		String expectedValue = "\\n";
		
		Chunk ch = fac.createChunk(testvalue);
		ch.hideAnyStartingLF();
		assertEquals("Hidden value of chunk not correct", "\\t\\t\\t", ch.getValue());
		ch.trimToLastLine();
		assertEquals("trimmed value of chunk not correct", "", ch.getValue());
		
		testvalue ="\r\n\t\t\t";
		expectedValue = "\\r\\n";
		
		ch = fac.createChunk(testvalue);
		ch.hideAnyStartingLF();
		assertEquals("Hidden value of chunk not correct", "\\t\\t\\t", ch.getValue());
		ch.trimToLastLine();
		assertEquals("trimmed value of chunk not correct", "", ch.getValue());
		
		testvalue = "Hello there \n   \t\t\t\t  ";
		expectedValue = "Hello there \\n   \\t\\t\\t\\t  ";
		ch = fac.createChunk(testvalue);
		ch.hideAnyStartingLF();
		assertEquals("Hidden value of chunk not correct", expectedValue, ch.getValue());
		ch.trimToLastLine();
		assertEquals("trimmed value of chunk not correct", "Hello there \\n", ch.getValue());
	}
}
