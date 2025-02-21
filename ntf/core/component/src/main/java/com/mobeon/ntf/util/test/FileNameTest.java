package com.mobeon.ntf.util.test;

import com.mobeon.ntf.util.FileName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * FileName Tester.
 *
 * @author emahagl
 */
public class FileNameTest extends TestCase {
    public FileNameTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateFileName() throws Exception {
        FileName fileName = FileName.createFileName("name", "txt");
        assertEquals("name", fileName.getName());
        assertEquals("txt", fileName.getExtension());
        assertEquals("name.txt", fileName.getFullname());
    }

    public void testCreateFileName1() throws Exception {
        FileName fileName = FileName.createFileName("name.txt");
        assertEquals("name", fileName.getName());
        assertEquals("txt", fileName.getExtension());
        assertEquals("name.txt", fileName.getFullname());
    }


    public void testHashCode() throws Exception {
        FileName fileName = FileName.createFileName("h.doc");
        assertNotNull(fileName.hashCode());
    }

    public void testEquals() throws Exception {
        FileName fileName1 = FileName.createFileName("h.doc");
        FileName fileName2 = FileName.createFileName("h.doc");
        FileName fileName3 = FileName.createFileName("h", "doc");

        assertTrue(fileName1.equals(fileName2));
        assertTrue(fileName1.equals(fileName3));

        assertTrue(fileName2.equals(fileName1));
        assertTrue(fileName2.equals(fileName3));

        assertTrue(fileName3.equals(fileName2));
        assertTrue(fileName3.equals(fileName1));
    }

    public void testNotEquals() throws Exception {
        FileName fileName1 = FileName.createFileName("h", "doc");
        FileName fileName2 = FileName.createFileName("a");
        FileName fileName3 = FileName.createFileName("h", "dox");

        assertFalse(fileName1.equals(fileName2));
        assertFalse(fileName1.equals(fileName3));
        assertFalse(fileName1.equals(null));

        assertFalse(fileName2.equals(fileName1));
        assertFalse(fileName2.equals(fileName3));
        assertFalse(fileName2.equals(null));

        assertFalse(fileName3.equals(fileName2));
        assertFalse(fileName3.equals(fileName1));
        assertFalse(fileName3.equals(null));
    }

    public void testToString() throws Exception {
        FileName fileName = FileName.createFileName("h.doc");
        assertNotNull(fileName.toString());
    }

    public static Test suite() {
        return new TestSuite(FileNameTest.class);
    }
}
