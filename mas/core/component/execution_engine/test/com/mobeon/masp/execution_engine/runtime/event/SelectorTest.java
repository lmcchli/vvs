package com.mobeon.masp.execution_engine.runtime.event;

import static com.mobeon.masp.util.Tools.*;
import com.mobeon.masp.execution_engine.Case;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Selector Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/05/2005</pre>
 */
public class SelectorTest extends Case
{
    enum Ordering {
        GREATER,
        LESS,
        EQUAL,
    }

    public SelectorTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(SelectorTest.class);
    }


    public void testCCXMLParseExact() throws Exception
    {
        String event = "exact.event";
        String expectedPrefix = event;
        validateCCXMLParse(event,null,expectedPrefix,Selector.Kind.EXACT);
    }

    public void testCCXMLParsePattern1() throws Exception
    {
        String event = "pattern.event.*";
        String expectedPrefix = "pattern.event";
        String expectedWildcard = ".*?";
        String pattern = "*";

        validateCCXMLParse(event,expectedWildcard,expectedPrefix,Selector.Kind.PREFIX_AND_REGEX);
    }

    public void testCCXMLParsePattern2() throws Exception
    {
        validateCCXMLParse("*", ".*?", null, Selector.Kind.REGEX);
    }

    public void testCCXMLMatchErrrorConnection() throws Exception {
        String evSel = "error.*";
        Selector s = CCXMLSelector.parse(evSel);
        String eventFAIL = "connection.connected";
        validateMatch(false,s, eventFAIL, evSel);
    }
    public void testCCXMLMatch() throws Exception {
        String evSel = "event.test.*";
        Selector s = CCXMLSelector.parse(evSel);
        String eventFAIL = "event.test";
        String eventOK = "event.test.ok";

        validateMatch(false,s, eventFAIL, evSel);
        validateMatch(true,s, eventOK, evSel);

        evSel = "event.test*";
        s=CCXMLSelector.parse(evSel);
        validateMatch(true,s, eventFAIL, evSel);
        validateMatch(true,s, eventOK, evSel);

        evSel = "event.test.ok";
        s = CCXMLSelector.parse(evSel);
        validateMatch(false,s, eventFAIL, evSel);
        validateMatch(true,s, eventOK, evSel);

        evSel = "*";
        s = CCXMLSelector.parse(evSel);
        validateMatch(true,s, eventFAIL, evSel);
        validateMatch(true,s, eventOK, evSel);

    }

    public void testVXMLMatch() throws Exception {
        String evSel = "event.test";
        Selector s = VoiceXMLSelector.parse(evSel);
        String eventShort = "event.test";
        String eventLong = "event.test.ok";

        validateMatch(true,s, eventShort, evSel);
        validateMatch(true,s, eventLong, evSel);

        evSel = "event.test.ok";
        s = VoiceXMLSelector.parse(evSel);
        validateMatch(false,s, eventShort, evSel);
        validateMatch(true,s, eventLong, evSel);

        evSel = ".";
        s = VoiceXMLSelector.parse(evSel);
        validateMatch(true,s, eventShort, evSel);
        validateMatch(true,s, eventLong, evSel);
    }

    public void testVXMLInternal() throws Exception {
        String evSel = "";
        Selector s = VoiceXMLSelector.parse(evSel);
        String blocked = "internal.play.finished";
        String allowed = "com.mobeon";
        validateMatch(false,s, blocked, evSel);
        validateMatch(true,s, allowed, evSel);
    }
    private void validateCCXMLParse(String event, String expectedWildcard, String expectedPrefix, Selector.Kind kind) {

        Selector s = CCXMLSelector.parse(event);
        validateParse(s, kind, expectedPrefix, event, expectedWildcard, event);
    }

    private void validateVXMLParse(String event, String expectedWildcard, String expectedPrefix, Selector.Kind kind) {

        Selector s = VoiceXMLSelector.parse(event);
        validateParse(s, kind, expectedPrefix, event, expectedWildcard, event);
    }

    private void validateParse(Selector s, Selector.Kind kind, String expectedPrefix, String event, String expectedWildcard, String pattern) {
        validateKind(s, kind);
        if(isNull(expectedPrefix)) {
            validateNoPrefix(s, event);
        } else {
            validateExpectedPrefix(s,expectedPrefix,event);
        }
        validateExpectedWildcard(expectedWildcard, s, pattern);
    }

    private void validateNoPrefix(Selector s, String event) {
        if(s.getPrefix() != null)
            die("The prefix should always be null for '"+event+"'");
    }

    private void validateExpectedWildcard(String expectedWildcard, Selector s, String pattern) {
        if( notNull( expectedWildcard) ) {
            validateExistingWildcard(s);
            if(!expectedWildcard.equals(s.getWildcard()))
                die(   "Incorrect wildcard for '"+pattern+
                        "'. It was '"+s.getWildcard()+
                        "', but it should be '"+expectedWildcard+"'");
        } else {
            if( notNull( s.getWildcard()) ) {
                die( "Wildcard found where none expected. Found wildcard: "+s.getWildcard());
            }
        }
    }

    private void validateExistingPrefix(Selector s,String event) {
        if(null == s.getPrefix())
            die("Prefix should never be null for '"+event+"'");
    }

    private void validateExistingWildcard(Selector s) {
        if(s.getWildcard() == null)
            die("Wildcard should newer be null for a prefixed event");
    }

    private void validateExpectedPrefix(Selector s,String expectedPrefix,String event) {
        validateExistingPrefix(s,event);
        if(!expectedPrefix.equals(s.getPrefix()))
            die("The prefix should be '"+expectedPrefix+"', but it was '"+s.getPrefix()+"'");
    }

    private void validateKind(Selector s, Selector.Kind kind) {
        if(s.getKind() != kind)
            die("Wrong selector kind");
        if(kind == Selector.Kind.EXACT) {
            if(s.getWildcard() != null)
                die("Wildcard should always be null for an exact event");
        }
    }

    private void validateMatch(boolean fail,Selector s, String event, String evSel) {
        if(!fail) {
            if(s.match(event)) die("Selector matches too greedy. "+event +" should not match "+evSel);
        } else {
            if(!s.match(event)) die("Selector matches too reluctantly. "+event +" should match "+evSel);
        }
    }


}
