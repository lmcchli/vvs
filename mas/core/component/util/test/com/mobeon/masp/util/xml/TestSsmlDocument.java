/*
 * Copyright (c) 2005, Mobeon. All Rights Reserved.
 */

package com.mobeon.masp.util.xml;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.Iterator;

/**
 * Unit tests for the class SsmlDocument.
 * Verifying that SSML is parsed and generated properly.
 */
public class TestSsmlDocument extends TestCase {
    String xmlText = "<?xml version=\"1.0\"?>" +
            "<speak version=\"1.0\" " +
            "xml:lang=\"en-US\">" +
            "<s>Hello world!</s>" +
            "<s>This is the end</s>" +
            "</speak>";

    public void setUp() {
    }

    /**
     * Parsing text and verifying that it contains two sentences.
     */
    public void testParse() {
        SsmlDocument ssml = new SsmlDocument();

        ssml.parse(xmlText);
        Collection<String> sentences = ssml.getSentences();
        assertEquals("There should be two sentences", 2, sentences.size());
        Iterator<String> iterator = sentences.iterator();
        assertEquals("First sentence", "Hello world!", iterator.next());
        assertEquals("Second sentence", "This is the end", iterator.next());
    }

    /**
     * Verifying that it is possible to retrieve parameters and that
     * an exception is thrown when the parameter is unknown.
     */
    public void testGetParameter() {
        SsmlDocument ssml = new SsmlDocument();

        ssml.parse(xmlText);
        assertEquals("Language is parsed", "en-US", ssml.getParameter("language"));
        try {
            ssml.getParameter("lingo");
            fail("Exception should have been thrown");
        } catch (Exception e) {}
    }

    /**
     * Verifying that set parameter works fine for both parsed and generated
     * SSML.
     */
    public void testSetParameter() {
        SsmlDocument parsedSsml = new SsmlDocument();
        SsmlDocument generatedSsml = new SsmlDocument();
        parsedSsml.parse(xmlText);
        generatedSsml.initialize();

        assertEquals("Language is parsed", "en-US", parsedSsml.getParameter("language"));
        parsedSsml.setParameter("language", "en-GB");
        assertEquals("Language is set", "en-GB", parsedSsml.getParameter("language"));
        assertEquals("Language is empty", null, generatedSsml.getParameter("language"));
        generatedSsml.setParameter("language", "en-UK");
        assertEquals("Language is set", "en-UK", generatedSsml.getParameter("language"));
        try {
            parsedSsml.setParameter("lingo", "en-GB");
            fail("Exception should have been thrown");
        } catch (Exception e) {}
        try {
            parsedSsml.setParameter("lingo", "en-GB");
            fail("Exception should have been thrown");
        } catch (Exception e) {}
    }

    /**
     * Verifying that SSML is properly generated.
     * The methods addSentence(s) are also tested here.
     */
    public void testGetXmlText() {
        SsmlDocument parsedSsml = new SsmlDocument();
        SsmlDocument generatedSsml = new SsmlDocument();

        generatedSsml.initialize();
        generatedSsml.setParameter("language", "en-GB");
        generatedSsml.addSentence("Hey Joe!");
        generatedSsml.addSentences("Where are you", "going with that", "gun in your hand?");

//        System.out.println("XML : [" + generatedSsml.getXmlText() + "]");

        parsedSsml.parse(generatedSsml.getXmlText());
        Collection<String> sentences = parsedSsml.getSentences();
        assertEquals("There should be four sentences", 4, sentences.size());
        Iterator<String> iterator = sentences.iterator();
        assertEquals("First sentence", "Hey Joe!", iterator.next());
        assertEquals("Second sentence", "Where are you", iterator.next());
        assertEquals("Third sentence", "going with that", iterator.next());
        assertEquals("Fourth sentence", "gun in your hand?", iterator.next());
    }
}
