/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import static com.mobeon.masp.mailbox.javamail.ContentDispositionHeaderUtil.*;

import java.util.regex.Pattern;

/**
 * ContentDispositionPatterns Tester.
 *
 * @author qhast
 */
public class ContentDispositionPatternsTest extends TestCase
{
    public ContentDispositionPatternsTest(String name)
    {
        super(name);
    }

    /**
     * Tests that strings containing the Originator Spoken Name pattern matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsOriginatorSpokenNamePattern() throws Exception {
        tryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"inline; voice=Originator-Spoken-Name; filename=spokenname.wav");
        tryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"inline; video=Originator-Spoken-Name; filename=spokenname.mov");
        tryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"inline; filename=spokenname.mov; video=Originator-Spoken-Name");
        tryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"inline; video=Originator-Spoken-Name");
        tryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"inline; category=Originator-Spoken-Name");
        tryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"attachment; voice=Originator-Spoken-Name");
        tryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"attachment; voice=Originator-Spoken-Name; filename=spoken.mpeg");
    }

    /**
     * Tests that strings NOT containing the Originator Spoken Name pattern NOT matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsNotOriginatorSpokenNamePattern() throws Exception {
        antiTryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"inline; voice=Voice-Message; filename=message.wav");
        antiTryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"inline; voice=Spoken-Name");
        antiTryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"inline; media-video=Originator-Spoken-Name");
        antiTryPattern(ORIGINATOR_SPOKEN_NAME_PATTERN,"inline; filename=Originator-Spoken-Name");
    }

    private void tryPattern(Pattern p, String s) throws Exception {
        assertTrue(p.pattern()+" does not match \""+s+"\"",p.matcher(s).matches());
    }

    private void antiTryPattern(Pattern p, String s) throws Exception {
        assertFalse(p.pattern()+" should not match \""+s+"\"",p.matcher(s).matches());
    }


    public static Test suite()
    {
        return new TestSuite(ContentDispositionPatternsTest.class);
    }
}
