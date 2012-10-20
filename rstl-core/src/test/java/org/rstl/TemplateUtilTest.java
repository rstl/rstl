/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;
import org.rstl.TemplateUtil;

public class TemplateUtilTest {
	
	@Test
	public void scantest() {
		String templateDir = "templates/scantest";
		String tmpTestGenDir = "target/tests/scantest";
		String expectedTemplateNames[] =  { "dir1/123.html", "xyz.html/abc.html", "foobar.html"};
		
		File f = new File(tmpTestGenDir);
		if (f.exists()) {
			f.delete();
		}
		f.mkdirs();
		List<String> templateNames = new ArrayList<String>();
		List<String> errorList = new ArrayList<String>();
		
		TemplateUtil.scanDirectory(templateDir, tmpTestGenDir, templateNames, errorList);
		
		assertEquals("The number of templates is incorrect", expectedTemplateNames.length, templateNames.size());
		assertEquals("The number of exceptions is unacceptable", 0, errorList.size());
		
		if (!errorList.isEmpty()) {
			System.err.println("The Exceptions were ...");
			for (String e: errorList) {
				System.err.println("Error : " + e);
			}
		}
	}
	
	@Test
	public void patternTest() {
	
		Pattern p = Pattern.compile(".+\\.(ct|htm)l$");
		
		System.out.println("Pattern for xyz.ctl + " + p.matcher("xyz.ctl").matches() );
		System.out.println("Pattern for xyz.html + " + p.matcher("xyz.html").matches() );
		System.out.println("Pattern for xyzhtml + " + p.matcher("xyzhtml").matches() );
		System.out.println("Pattern for .html + " + p.matcher(".html").matches() );
		
		Pattern p2 = Pattern.compile("^(?:((?:[0-9a-f]{1,4}))?\\:){7}(?:[0-9a-f]{1,4})$");
		String addrs[] = {"2011:abcd:0000:0000:0000:0000:1872:efef",  "2011:abcd::1872:efef", "2011:abcd:::::1872:efef"};
		for (String addr : addrs)
			System.out.println("Pattern for " + addr + ": " + p2.matcher(addr).matches());
		
	}
	
}
