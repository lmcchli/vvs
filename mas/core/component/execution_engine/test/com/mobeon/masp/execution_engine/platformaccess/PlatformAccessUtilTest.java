/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.io.ByteArrayInputStream;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediacontentmanager.MediaQualifierException;
import com.mobeon.masp.mediahandler.MediaHandler;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.execution_engine.platformaccess.util.TimeUtil;

import org.jmock.Mock;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

/**
 * Testcases for the PlatformAccessUtil class.
 *
 * @author ermmaha
 */
public class PlatformAccessUtilTest extends PlatformAccessTest {
    private static String VVA_DATEFORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    public PlatformAccessUtilTest(String name) {
        super(name);
    }

    /**
     * Tests the parseVvaTime function. (In TimeUtil)
     * Redundant test which may be removed
     *
     * @throws Exception if testcase fails.
     */
    public void testParseVvaTime() throws Exception {
        String vvaTime = "2005-11-10 14:33:32";
        Date date = TimeUtil.parseVvaTime(vvaTime);
        assertEquals("Thu Nov 10 14:33:32 CET 2005", date.toString());

        // test parseexception (add some invalid spaces)
        vvaTime = "2005-11-10 14:33 : 32";
        try {
            TimeUtil.parseVvaTime(vvaTime);
            fail("Expected ParseException");
        } catch (ParseException ex) {
        }
    }

    /**
     * Tests the systemGetCurrentTime function.
     *
     * @throws Exception if testcase fails.
     */
    public void testSystemGetCurrentTime() throws Exception {
        String compString = getCompareTime("GMT+1");
        String time = platformAccessUtil.getCurrentTime("GMT+1"); //ex 2005-11-09 18:40:29
        assertEquals(compString, time);

        compString = getCompareTime("GMT-7:00");
        time = platformAccessUtil.getCurrentTime("GMT-7:00");
        assertEquals(compString, time);

        compString = getCompareTime("America/Chicago");
        time = platformAccessUtil.getCurrentTime("America/Chicago");
        assertEquals(compString, time);

        compString = getCompareTime("Europe/Paris");
        time = platformAccessUtil.getCurrentTime(null);
        assertEquals(compString, time);
    }

    /**
     * Tests the systemConvertTime function.
     *
     * @throws Exception
     */
    public void testSystemConvertTime() throws Exception {
        String vvaTime = "2005-11-10 14:33:32";

        String convertedTime = platformAccessUtil.convertTime(vvaTime, "GMT-1", "GMT-1");
        assertEquals(vvaTime, convertedTime);

        convertedTime = platformAccessUtil.convertTime(vvaTime, "GMT-1", "GMT+2");
        assertEquals("2005-11-10 17:33:32", convertedTime);

        // Test default from timezone
        convertedTime = platformAccessUtil.convertTime(vvaTime, null, "GMT-1");
        assertEquals("2005-11-10 12:33:32", convertedTime);

        // Test default to timezone
        convertedTime = platformAccessUtil.convertTime(vvaTime, "GMT-1", null);
        assertEquals(vvaTime, convertedTime);

        // test parseexception (add some invalid spaces)
        vvaTime = "2005-11-10 14:33 : 32";
        try {
            platformAccessUtil.convertTime(vvaTime, "GMT-1", "GMT+2");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the systemFormatTime function.
     *
     * @throws Exception if testcase fails.
     */
    public void testSystemFormatTime() throws Exception {
        String vvaTime = "2005-12-09 05:54:32";
        String time = platformAccessUtil.formatTime(vvaTime, "yyyy-MM-dd hh:mm:ss");
        System.out.println("Formatted Time: " + time);
        time = platformAccessUtil.formatTime(vvaTime, null);
        System.out.println("Formatted Time: " + time);

        // test parseexception (add some invalid spaces)
        vvaTime = "2005-12-09 05 : 54:32";
        try {
            platformAccessUtil.formatTime(vvaTime, "yyyy-MM-dd hh:mm:ss");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the dateToVvaTime function. (In TimeUtil)
     *
     * @throws Exception if testcase fails.
     */
    public void testDateToVvaTime() throws Exception {
        String timeStr = TimeUtil.dateToVvaTime(Calendar.getInstance().getTime(), "GMT-1");
    }

    /**
     * Tests the getMediaQualifier function.
     *
     * @throws Exception if testcase fails.
     */
    public void testGetMediaQualifier() throws Exception {

        Mock jmockNumberQualifier = mock(IMediaQualifier.class);

        jmockMediaQualifierFactory.expects(once()).method("create").
                with(eq(IMediaQualifier.QualiferType.Number), eq(null), eq("123"), eq(null)).will(returnValue(jmockNumberQualifier.proxy()));
        IMediaQualifier iMediaQualifier = platformAccessUtil.getMediaQualifier("number", "123");
        assertEquals(iMediaQualifier, jmockNumberQualifier.proxy());

        jmockMediaQualifierFactory.expects(once()).method("create").
                with(eq(IMediaQualifier.QualiferType.DateDM), eq(null), eq("2005-08-12 23:13:23 +0200"), eq(null)).will(returnValue(jmockNumberQualifier.proxy()));
        iMediaQualifier = platformAccessUtil.getMediaQualifier("datedm", "2005-08-12 23:13:23 +0200");
        assertEquals(iMediaQualifier, jmockNumberQualifier.proxy());

        jmockMediaQualifierFactory.expects(once()).method("create").
                with(eq(IMediaQualifier.QualiferType.WeekDay), eq(null), eq("2005-08-12"), eq(null)).will(returnValue(jmockNumberQualifier.proxy()));
        iMediaQualifier = platformAccessUtil.getMediaQualifier("weekday", "2005-08-12");
        assertEquals(iMediaQualifier, jmockNumberQualifier.proxy());

        jmockMediaQualifierFactory.expects(once()).method("create").
                with(eq(IMediaQualifier.QualiferType.Time12), eq(null), eq("11:13:23"), eq(null)).will(returnValue(jmockNumberQualifier.proxy()));
        iMediaQualifier = platformAccessUtil.getMediaQualifier("time12", "11:13:23");
        assertEquals(iMediaQualifier, jmockNumberQualifier.proxy());

        jmockMediaQualifierFactory.expects(once()).method("create").
                with(eq(IMediaQualifier.QualiferType.Time24), eq(null), eq("23:13:23"), eq(null)).will(returnValue(jmockNumberQualifier.proxy()));
        iMediaQualifier = platformAccessUtil.getMediaQualifier("time24", "23:13:23");
        assertEquals(iMediaQualifier, jmockNumberQualifier.proxy());

        jmockMediaQualifierFactory.expects(once()).method("create").
                with(eq(IMediaQualifier.QualiferType.String), eq(null), eq("John Doe"), eq(null)).will(returnValue(jmockNumberQualifier.proxy()));
        iMediaQualifier = platformAccessUtil.getMediaQualifier("string", "John Doe");
        assertEquals(iMediaQualifier, jmockNumberQualifier.proxy());

        jmockMediaQualifierFactory.expects(once()).method("create").
                with(eq(IMediaQualifier.QualiferType.CompleteDate), eq(null), eq("23:13:23"), eq(null)).will(returnValue(jmockNumberQualifier.proxy()));
        iMediaQualifier = platformAccessUtil.getMediaQualifier("completedate", "23:13:23");
        assertEquals(iMediaQualifier, jmockNumberQualifier.proxy());

        // test the IMediaObject type
        Mock jmockMedia = mock(IMediaObject.class);

        jmockMediaQualifierFactory.expects(once()).method("create").
                with(eq(null), eq(jmockMedia.proxy()), eq(null)).will(returnValue(jmockNumberQualifier.proxy()));
        iMediaQualifier = platformAccessUtil.getMediaQualifier((IMediaObject) jmockMedia.proxy());
        assertEquals(iMediaQualifier, jmockNumberQualifier.proxy());

        // test invalid type
        jmockMediaQualifierFactory.expects(never()).method("create");
        try {
            platformAccessUtil.getMediaQualifier("X-Weekday", "notatype");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test invalid value
        jmockMediaQualifierFactory.expects(once()).method("create").
                with(eq(IMediaQualifier.QualiferType.WeekDay), eq(null), eq("notadate"), eq(null)).
                will(throwException(new MediaQualifierException("Invalid Qualifier value")));
        try {
            platformAccessUtil.getMediaQualifier("Weekday", "notadate");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the getMediaObject function.
     *
     * @throws Exception
     */
    public void testGetMediaObject() throws Exception {
        Mock jmockMedia = mock(IMediaObject.class);
        jmockMediaObjectFactory.expects(once()).method("create").with(eq("forwarded"), isA(MediaProperties.class)).
                will(returnValue(jmockMedia.proxy()));

        IMediaObject iMediaObject = platformAccessUtil.getMediaObject("forwarded");
        assertNotNull(iMediaObject);
    }
    
    /**
     * Tests the getAppendMediaObjects function.
     *
     * @throws Exception
     */
    public void testAppendMediaObjects() throws Exception {
        Mock jmockMediaObject1 = mock(IMediaObject.class);
        Mock jmockMediaObject2 = mock(IMediaObject.class);
        Mock jmockMediaObject3 = mock(IMediaObject.class);
        Mock jmockMediaHandler = mock(MediaHandler.class);
        MediaProperties mediaProperties = new MediaProperties();
        mediaProperties.setContentType(new MimeType("audio/wav"));
        
        jmockMediaObject1.expects(once()).method("getMediaProperties").
        	will(returnValue(mediaProperties));
        jmockMediaObject2.expects(once()).method("getMediaProperties").
        	will(returnValue(mediaProperties));
        jmockMediaHandlerFactory.expects(once()).method("getMediaHandler").
        	with(isA(MimeType.class)).
            will(returnValue(jmockMediaHandler.proxy()));
        jmockMediaHandler.expects(once()).method("hasConcatenate").
        	will(returnValue(true));
        
        jmockMediaHandler.expects(once()).method("concatenate").
        	with(eq(jmockMediaObject1.proxy()), eq(jmockMediaObject2.proxy())).
        	will(returnValue(jmockMediaObject3.proxy()));
        
        IMediaObject mediaObject = platformAccessUtil.appendMediaObjects(
        		(IMediaObject)jmockMediaObject1.proxy(), 
        		(IMediaObject)jmockMediaObject2.proxy());
        assertNotNull(mediaObject);
    }

    /**
     * Tests the getAppendMediaObjects function.
     * (Error handling)
     *
     * @throws Exception
     */
    public void testAppendMediaObjects_negative() throws Exception {

    	Mock jmockMediaObject1 = mock(IMediaObject.class);
        Mock jmockMediaObject2 = mock(IMediaObject.class);

        MediaProperties mediaProperties1 = new MediaProperties();
        mediaProperties1.setContentType(new MimeType("audio/wav"));
        MediaProperties mediaProperties2 = new MediaProperties();
        mediaProperties2.setContentType(new MimeType("audio/abc"));
        
        jmockMediaObject1.expects(atLeastOnce()).method("getMediaProperties").
        	will(returnValue(mediaProperties1));
        jmockMediaObject2.expects(atLeastOnce()).method("getMediaProperties").
        	will(returnValue(mediaProperties2));

        // Content type differs
        try {
        	platformAccessUtil.appendMediaObjects(
        		(IMediaObject)jmockMediaObject1.proxy(), 
        		(IMediaObject)jmockMediaObject2.proxy());
        	fail("Expected an exception here!");
        } catch (Exception e) {}

        // One argument is null
        try {
        	platformAccessUtil.appendMediaObjects(
        		null, 
        		(IMediaObject)jmockMediaObject2.proxy());
        	fail("Expected an exception here!");
        } catch (Exception e) {}

        // Other argument is null
        try {
        	platformAccessUtil.appendMediaObjects(
        		(IMediaObject)jmockMediaObject1.proxy(), 
        		null);
        	fail("Expected an exception here!");
        } catch (Exception e) {}

    }
    
   

    /**
     * Tests the convertMediaObjectsToString function.
     *
     * @throws Exception
     */
    public void testConvertMediaObjectsToString() throws Exception {
        MediaProperties textProp = getMediaProperties("text/plain;charset=utf-8");
        MediaProperties voiceProp = getMediaProperties("audio/wav");

        Mock jmockMedia0 = mock(IMediaObject.class);
        Mock jmockMedia1 = mock(IMediaObject.class);
        IMediaObject[] iMediaObjects =
                new IMediaObject[]{(IMediaObject) jmockMedia0.proxy(), (IMediaObject) jmockMedia1.proxy()};

        jmockMedia0.stubs().method("getMediaProperties").will(returnValue(textProp));
        ByteArrayInputStream is0 = new ByteArrayInputStream("Hällo ".getBytes("UTF-8"));
        jmockMedia0.stubs().method("getInputStream").will(returnValue(is0));

        jmockMedia1.stubs().method("getMediaProperties").will(returnValue(textProp));
        ByteArrayInputStream is1 = new ByteArrayInputStream("Wörld".getBytes("UTF-8"));
        jmockMedia1.stubs().method("getInputStream").will(returnValue(is1));

        String result = platformAccessUtil.convertMediaObjectsToString(iMediaObjects);
        assertEquals("Hällo Wörld", result);

        //test a voice mediaobject an error will occur then
        jmockMedia0.stubs().method("getMediaProperties").will(returnValue(voiceProp));
        jmockMedia0.expects(never()).method("getInputStream");
        try {
            platformAccessUtil.convertMediaObjectsToString(iMediaObjects);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the getMediaObjectProperty function.
     *
     * @throws Exception
     */
    public void testGetMediaObjectProperty() throws Exception {

        MediaProperties textProp = getMediaProperties("text/plain;charset=utf-8");
        MediaProperties voiceProp = getMediaProperties("audio/wav");
        Mock jmockMedia0 = mock(IMediaObject.class);
        Mock jmockMedia1 = mock(IMediaObject.class);

        String xmlText = "<?xml version=\"1.0\"?>" +
                "<speak version=\"1.0\" " +
                "xml:lang=\"en-US\">" +
                "<s>Hello world!</s>" +
                "<s>This is the end</s>" +
                "</speak>";

        jmockMedia0.stubs().method("getMediaProperties").will(returnValue(textProp));
        ByteArrayInputStream is0 = new ByteArrayInputStream(xmlText.getBytes("UTF-8"));
        jmockMedia0.stubs().method("getInputStream").will(returnValue(is0));

        String result = platformAccessUtil.getMediaObjectProperty((IMediaObject) jmockMedia0.proxy(), "language");
        assertEquals("en-US", result);

        //test the voice MediaObject
        jmockMedia1.stubs().method("getMediaProperties").will(returnValue(voiceProp));
        jmockMedia1.expects(never()).method("getInputStream");

        result = platformAccessUtil.getMediaObjectProperty((IMediaObject) jmockMedia1.proxy(), "language");
        assertEquals("", result);
    }

    /**
     * Tests the setMediaObjectProperty function.
     *
     * @throws Exception
     */
    public void testSetMediaObjectProperty() throws Exception {

        MediaProperties textProp = getMediaProperties("text/plain;charset=utf-8");
        MediaProperties voiceProp = getMediaProperties("audio/wav");
        Mock jmockMedia0 = mock(IMediaObject.class);
        Mock jmockMedia1 = mock(IMediaObject.class);

        String xmlText = "<?xml version=\"1.0\"?>" +
                "<speak version=\"1.0\" " +
                "xml:lang=\"en-US\">" +
                "<s>Hello world!</s>" +
                "<s>This is the end</s>" +
                "</speak>";

        jmockMedia0.stubs().method("getMediaProperties").will(returnValue(textProp));
        ByteArrayInputStream is0 = new ByteArrayInputStream(xmlText.getBytes());
        jmockMedia0.stubs().method("getInputStream").will(returnValue(is0));

        jmockMediaObjectFactory.expects(once()).method("create").with(isA(String.class), isA(MediaProperties.class));

        IMediaObject result = platformAccessUtil.setMediaObjectProperty((IMediaObject) jmockMedia0.proxy(),
                new String[]{"language"}, new String[]{"ja"});

        //test the voice MediaObject
        jmockMedia1.stubs().method("getMediaProperties").will(returnValue(voiceProp));
        jmockMedia1.expects(never()).method("getInputStream");

        platformAccessUtil.setMediaObjectProperty((IMediaObject) jmockMedia1.proxy(),
                new String[]{"language"}, new String[]{"ja"});

        // test illegal propertyName
        try {
            // reset byte buffer to get rid of error when parsing the xmlstring
            is0 = new ByteArrayInputStream(xmlText.getBytes());
            jmockMedia0.stubs().method("getInputStream").will(returnValue(is0));
            platformAccessUtil.setMediaObjectProperty((IMediaObject) jmockMedia0.proxy(),
                    new String[]{"illegalname"}, new String[]{"ja"});
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    public void testDetag() throws Exception {

        // test that a simple HTML document detags to a simple string
        String taggedString = "<html>kalle</html>";

        String s = platformAccessUtil.deTag(taggedString);
        assertEquals(s, "kalle");

        // test that a string with no markup detags to the same string
        taggedString = "hello there my friend, how are you?";

        s = platformAccessUtil.deTag(taggedString);
        assertEquals(s, taggedString);

        // test that a complicated string detags correctly
        taggedString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<vxml xmlns=\"http://www.w3.org/2001/vxml\""+
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
                "xsi:schemaLocation=\"http://www.w3.org/2001/vxml"+
                "http://www.w3.org/TR/voicexml20/vxml.xsd\""+
                "version=\"2.0\">"+
                "<form>"+
                "<field name=\"drink\">"+
                "<prompt>Would you like coffee, tea, milk, or nothing?</prompt>"+
                "<grammar src=\"drink.grxml\" type=\"application/srgs+xml\"/>"+
                "</field>"+
                "<block>"+
                "<submit next=\"http://www.drink.example.com/drink2.asp\"/>"+
                "</block>"+
                "</form>"+
                "</vxml>";


        s = platformAccessUtil.deTag(taggedString);
        assertEquals(s, "Would you like coffee, tea, milk, or nothing?");

    }

    /**
     * Tests the getSupportedTTSLanguages function.
     *
     * @throws Exception if testcase fails.
     */
    public void testGetSupportedTTSLanguages() throws Exception {

        // test that if mediaTranslationManager returns null for TTS languages,
        // platformAccessUtil return an empty array
        jmockMediaTranslationManager.stubs().method("getTextToSpeechLanguages").will(returnValue(null));
        String [] languages = platformAccessUtil.getSupportedTTSLanguages();
        assert(languages.length == 0);

        // test that if mediaTranslationManager returns a Collection with one element,
        // an array with this element will be returned by PlatformAccessUtil.
        List<String> languageCollection = new ArrayList<String>();
        languageCollection.add("en");
        jmockMediaTranslationManager.stubs().method("getTextToSpeechLanguages").will(returnValue(languageCollection));
        languages = platformAccessUtil.getSupportedTTSLanguages();
        assert(languages.length == 1);
        assert(languages[0].equals("en"));

        // test that if mediaTranslationManager returns a Collection with 10 element,
        // an array with these elements will be returned by PlatformAccessUtil.
        languageCollection = new ArrayList<String>();
        languageCollection.add("a1");
        languageCollection.add("a2");
        languageCollection.add("a3");
        languageCollection.add("a4");
        languageCollection.add("a5");
        languageCollection.add("a6");
        languageCollection.add("a7");
        languageCollection.add("a8");
        languageCollection.add("a9");
        languageCollection.add("a10");

        jmockMediaTranslationManager.stubs().method("getTextToSpeechLanguages").will(returnValue(languageCollection));
        languages = platformAccessUtil.getSupportedTTSLanguages();
        assert(languages.length == 10);
        assert(hasMember("a1", languages));
        assert(hasMember("a2", languages));
        assert(hasMember("a3", languages));
        assert(hasMember("a4", languages));
        assert(hasMember("a5", languages));
        assert(hasMember("a6", languages));
        assert(hasMember("a7", languages));
        assert(hasMember("a8", languages));
        assert(hasMember("a9", languages));
        assert(hasMember("a10", languages));
    }


    private boolean hasMember(String s, String[] strings) {
        for (String s1 : strings) {
            if(s1.equals(s)) return true;
        }
        return false;
    }

    private MediaProperties getMediaProperties(String mimeTypeStr) {
        try {
            MimeType mimeType = new MimeType(mimeTypeStr);
            return new MediaProperties(mimeType);
        } catch (MimeTypeParseException e) {
            fail("Could not create MimeTypes for mocking MediaProperties " + e);
        }
        return null;
    }

    private String getCompareTime(String timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(VVA_DATEFORMAT_STRING);
        TimeZone zone = TimeZone.getTimeZone(timezone);
        simpleDateFormat.setTimeZone(zone);
        return simpleDateFormat.format(new Date());
    }

    public static Test suite() {
        return new TestSuite(PlatformAccessUtilTest.class);
    }
}

