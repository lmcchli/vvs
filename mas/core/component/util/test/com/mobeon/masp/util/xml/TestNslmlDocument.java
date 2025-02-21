/*
 * Copyright (c) 2005, Mobeon. All Rights Reserved.
 */

package com.mobeon.masp.util.xml;

import junit.framework.TestCase;

/**
 *   Unis tests for the class NlsmlDocument.
 */
public class TestNslmlDocument extends TestCase {
    String nlsmlText = "<?xml version='1.0'?>" +
            "<result>" +
            "<interpretation grammar=\"session:myGrammar\" confidence=\"94\">" +
            "<input mode=\"speech\">one</input>" +
            "<instance>" +
            "<SWI_literal>one</SWI_literal>" +
            "<SWI_grammarName>session:myGrammar</SWI_grammarName>" +
            "<SWI_meaning>{SWI_literal:one}</SWI_meaning>" +
            "</instance>" +
            "</interpretation>" +
            "</result>";

    public void testParse() {
        NlsmlDocument nlsml = new NlsmlDocument();

        nlsml.parse(nlsmlText);

        assertTrue("Parse should not fail", nlsml.isOk());
        assertEquals("Should respond with one", "one", nlsml.getInput());
        assertEquals("Confidence should be high", 94, nlsml.getConfidence());
    }
}
