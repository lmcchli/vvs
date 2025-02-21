/**
 * 
 */
package com.mobeon.masp.profilemanager.greetings;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import com.mobeon.masp.profilemanager.mediafile.AbstractMediaFile;

import junit.framework.TestCase;

/**
 * @author egeobli
 *
 */
public class GreetingTest extends TestCase {
	
	private String testDir;
	
	public GreetingTest() {
		File[] roots = File.listRoots();
		if (roots != null && roots.length > 0) {
			testDir = roots[0].getAbsolutePath() + "tmp" + File.separator;

			File dir = new File(testDir);
			if (!dir.exists()) {
				assertTrue(dir.mkdirs());
			}
		} else {
			testDir = "/tmp/";
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test method for {@link com.mobeon.masp.profilemanager.greetings.Greeting#getMedia()} and
	 * {@link com.mobeon.masp.profilemanager.greetings.Greeting#setMedia()}.
	 */
	public void testGetMedia() {
		try {
			Greeting greeting = new Greeting(testDir + "GreetingTest");
			byte[] buffer = new byte[2048];
			ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer); 
			greeting.setMedia(byteStream);
			
			assertEquals(byteStream, greeting.getMedia());
		} catch (IOException e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.mobeon.masp.profilemanager.greetings.Greeting#getName()}.
	 */
	public void testGetName() {
		try {
			final String name = testDir + "MyGreeting";
			Greeting greeting = new Greeting(name, ".wav");
			assertEquals(name, greeting.getName());
		} catch (IOException e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.mobeon.masp.profilemanager.greetings.Greeting#getProperty(java.lang.String)}
	 * and {@link com.mobeon.masp.profilemanager.greetings.Greeting#setProperty(java.lang.String, java.lang.String)}.
	 */
	public void testGetProperty() {
		try {
			final String key = "MyProperty";
			final String value = "My Property Value";
			Greeting greeting = new Greeting(testDir + "testGetProperty");
			greeting.setProperty(key, value);
			
			assertEquals(value, greeting.getProperty(key));
			assertEquals(Greeting.MEDIA_EXTENSION, greeting.getProperty(Greeting.MEDIA_EXTENSION_PROPERTY));
		} catch (IOException e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}


	/**
	 * Test method for {@link com.mobeon.masp.profilemanager.greetings.Greeting#store()}.
	 */
	public void testStore() {
	    System.setProperty("abcxyz.mfs.userdir.create", "true");
		try {
			final String prop1 = "property1";
			final String value1 = "value1";
			final String prop2 = "property2";
			final String value2 = "value2";
			final String fname = testDir + "testStore";
			final String ext = ".mov";
			Greeting greeting = new Greeting(fname, ext);
			
			greeting.setProperty(prop1, value1);
			greeting.setProperty(prop2, value2);
			
			byte[] buffer = new byte[2048];
			Arrays.fill(buffer, (byte)0x5A);
			ByteArrayInputStream is = new ByteArrayInputStream(buffer);
			greeting.setMedia(is);
			
			greeting.store();
			
			File mFile = new File(fname + ext);
			File pFile = new File(fname + ".properties");
			assertTrue(mFile.exists());
			assertTrue(pFile.exists());
			
			Greeting readBackGreeting = new Greeting(fname, ext);
			
			assertEquals(value1, readBackGreeting.getProperty(prop1));
			assertEquals(value2, readBackGreeting.getProperty(prop2));
			
			byte[] rbBuffer = new byte[buffer.length];
			int nbBytes = readBackGreeting.getMedia().read(rbBuffer);
			assertEquals(rbBuffer.length, nbBytes);
			assertTrue(Arrays.equals(buffer, rbBuffer));
			
		} catch (IOException e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.mobeon.masp.profilemanager.greetings.Greeting#exists()}.
	 */
	public void testExists() {
		final String fname = testDir + Greeting.DEFAULT_GREETING_FNAME;
		Properties properties = new Properties();
		properties.setProperty("Mytest", "Succeeded");
		
		try {
			File pFile = new File(fname + ".properties");
			FileOutputStream os = new FileOutputStream(pFile);
			properties.store(os, null);
			os.close();
			
			File mFile = new File(fname + Greeting.MEDIA_EXTENSION);
			byte[] buffer = new byte[2048];
			Arrays.fill(buffer, (byte)0x5A);
			os = new FileOutputStream(mFile);
			os.write(buffer);
			os.close();
			
			Greeting greeting = new Greeting(fname);
			assertTrue(greeting.exists());
			
			pFile.delete();
			mFile.delete();
			
		} catch (IOException e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.mobeon.masp.profilemanager.greetings.Greeting#delete()}.
	 */
	public void testDelete() {
		final String fName = testDir + Greeting.DEFAULT_GREETING_FNAME;
		final String pName = fName + Greeting.PROPERTY_EXTENSION;
		final String mName = fName + Greeting.MEDIA_EXTENSION;
		
		try {
			Greeting g1 = new Greeting(fName);
			g1.setProperty("MyTest", "True");
			
			byte[] buffer = new byte[2048];
			Arrays.fill(buffer, (byte)0x5A);
			ByteArrayInputStream is = new ByteArrayInputStream(buffer);
			g1.setMedia(is);
			
			g1.store();
			
			File file = new File(pName);
			file = new File(mName);
			assertTrue(file.exists());
			assertTrue(file.exists());

			g1.delete();
			
			assertFalse(file.exists());
			assertFalse(file.exists());
			
		} catch (IOException e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}

}
