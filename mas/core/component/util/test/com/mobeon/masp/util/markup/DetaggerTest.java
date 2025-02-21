/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.markup;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * Detagger Tester.
 *
 * @author qhast
 */
public class DetaggerTest extends TestCase
{
    public static final String SOURCE =
            "<html>" +
            "<head>" +
            "<meta http-equiv=\"Content-Language\" content=\"en-us\">" +
            "<title>Apache Ant 1.6.5 User Manual</title>" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheets/antmanual.css\">" +
            "</head>" +
            "<body bgcolor=\"#FFFFFF\">" +
            "<div align=\"center\">" +
            "<h1><img src=\"../images/ant_logo_large.gif\" width=\"190\" height=\"120\"></h1>" +
            "<h1>Apache Ant 1.6.5 Manual</h1>" +
            "<p align=\"left\">This is the manual for version 1.6.5 of" +
            "<a href=\"http://ant.apache.org/index.html\">Apache Ant</a>." +
            "If your version of Ant (as verified with <tt>ant -version</tt>) is older or newer than this version</p>" +
            "<p>&nbsp;</p>" +
            "</div>" +
            "<hr>" +
            "<p align=\"center\">Copyright &copy; 2000-2005 The Apache Software Foundation. All rights Reserved.</p>" +
            "</body>" +
            "</html>";


    public static final String CLEAN = "Apache Ant 1.6.5 User Manual" +
            "Apache Ant 1.6.5 Manual" +
            "This is the manual for version 1.6.5 of" +
            "Apache Ant" +
            "." +
            "If your version of Ant (as verified with ant -version) is older or newer than this version"+
            "Copyright  2000-2005 The Apache Software Foundation. All rights Reserved.";

    public DetaggerTest(String name)
    {
        super(name);
    }


    public void testClassCall() throws Exception {
        new Detagger();
    }


    /**
     * Tests removing arbitrary markup from text.
     * @throws Exception
     */
    public void testRemoveMarkup() throws Exception
    {
        assertEquals(CLEAN,Detagger.removeMarkup(SOURCE));
    }

    /**
     * Tests removing HTML markup from text. 
     * @throws Exception
     */
    public void testRemoveHtmlMarkup() throws Exception
    {
        assertEquals(CLEAN,Detagger.removeHtmlMarkup(SOURCE));
    }

    public static Test suite()
    {
        return new TestSuite(DetaggerTest.class);
    }
}
