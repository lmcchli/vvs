/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;

import com.mobeon.masp.profilemanager.ProfileManagerMockObjectBaseTestCase;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

import jakarta.activation.MimeType;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;

import junit.framework.Assert;

/**
 * Documentation
 *
 * @author mande
 */
public abstract class GreetingMockObjectBaseTestCase extends ProfileManagerMockObjectBaseTestCase {
    protected Map<GreetingType, String> greetingMap;
    protected Map<GreetingType, Map<GreetingFormat, Message>> greetingMessageMap;
    protected Map<GreetingType, String> fileNameMap;
    protected Map<GreetingType, Map<GreetingFormat, Long>> sizeMap;
    protected Map<GreetingType, Long> lengthMap;
    protected Map<GreetingType, Map<GreetingFormat, IMediaObject>> mediaObjectMap;
    protected static final String GREETING_TYPE   = "X-M3-Greeting-Type";
    protected static final String GREETING_FORMAT = "X-M3-Greeting-Format";
    protected static final String TELEPHONE_NUMBER = "19161";
    protected static final String SUBID = "12345";
    protected static final String DIST_LIST_SPOKEN_NAME_SUBID = "1";
    protected static final Session SESSION = Session.getInstance(new Properties());
    private static final String MESSAGE_PATH = "test/com/mobeon/masp/profilemanager/greetings/messages/";
    private static final String MEDIA_FILE_PATH = "profilemanager/";
    
    
    /**
     * Set containing enums for all types that can be represented by strings (with the valueOf method).
     */
    protected static final Set<GreetingType> STRING_REPRESENTED_TYPES = EnumSet.complementOf(
            EnumSet.of(GreetingType.SPOKEN_NAME, GreetingType.DIST_LIST_SPOKEN_NAME)
    );
    /**
     * Set containing enums for all types that requires a subid.
     */
    protected static final Set<GreetingType> SUBID_TYPES = EnumSet.of(GreetingType.CDG, GreetingType.DIST_LIST_SPOKEN_NAME);


    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpGreetingMap();
        setUpFileNameMap();
        setUpSizeMap();
        setUpLengthMap();
        setUpMediaObjectMap();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Sets up the map with greeting names for the different greetings
     */
    private void setUpGreetingMap() {
        greetingMap = new EnumMap<GreetingType, String>(GreetingType.class);
        greetingMap.put(GreetingType.ALL_CALLS, "allcalls");
        greetingMap.put(GreetingType.BUSY, "busy");
        greetingMap.put(GreetingType.CDG, "cdg");
        greetingMap.put(GreetingType.EXTENDED_ABSENCE, "extended_absence");
        greetingMap.put(GreetingType.NO_ANSWER, "noanswer");
        greetingMap.put(GreetingType.OUT_OF_HOURS, "outofhours");
        greetingMap.put(GreetingType.OWN_RECORDED, "ownrecorded");
        greetingMap.put(GreetingType.TEMPORARY, "temporary");
    }

    protected void setUpFileNameMap() {
        fileNameMap = new EnumMap<GreetingType, String>(GreetingType.class);
        fileNameMap.put(GreetingType.ALL_CALLS, "allcalls");
        fileNameMap.put(GreetingType.BUSY, "busy");
        fileNameMap.put(GreetingType.CDG, "cdg" + SUBID);
        fileNameMap.put(GreetingType.EXTENDED_ABSENCE, "extendedabsence");
        fileNameMap.put(GreetingType.NO_ANSWER, "noanswer");
        fileNameMap.put(GreetingType.OUT_OF_HOURS, "outofhours");
        fileNameMap.put(GreetingType.OWN_RECORDED, "ownrecorded");
        fileNameMap.put(GreetingType.TEMPORARY, "temporary");
        fileNameMap.put(GreetingType.SPOKEN_NAME, "spokenname");
        fileNameMap.put(GreetingType.DIST_LIST_SPOKEN_NAME, "distlistspokenname" + DIST_LIST_SPOKEN_NAME_SUBID);
    }

    protected String getFileExtension(GreetingFormat format) {
        return getFileExtension(format, null);
    }

    protected String getFileExtension(GreetingFormat format, MimeType contentType) {
        if (format == GreetingFormat.VOICE) {
            if (contentType != null) {
                if (contentType.toString().equals("audio/3gpp")) return "3gp";
            }
            return "wav";
        } else {
            if (contentType != null) {
                if (contentType.toString().equals("video/3gpp")) return "3gp";
            }
            return "mov";
        }
    }

    protected String getContentType(GreetingFormat format) {
        switch (format) {
            case VOICE: return "audio/wav";
            case VIDEO: return "video/quicktime";
            default:    return "unknown";
        }
    }

    /**
     * Sets up the length map with lengths for the different greetings
     */
    private void setUpLengthMap() {
        lengthMap = new EnumMap<GreetingType, Long>(GreetingType.class);
        for (GreetingType type : GreetingType.values()) {
            lengthMap.put(type, (long)1000);
        }
    }

    /**
     * Sets up the size map with sizes for the different greetings
     */
    private void setUpSizeMap() {
        sizeMap = new EnumMap<GreetingType, Map<GreetingFormat, Long>>(GreetingType.class);
        Map<GreetingFormat, Long> formatMap = new EnumMap<GreetingFormat, Long>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, (long)12858);
        formatMap.put(GreetingFormat.VIDEO, (long)13743);
        sizeMap.put(GreetingType.ALL_CALLS, formatMap);
        formatMap = new EnumMap<GreetingFormat, Long>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, (long)16378);
        formatMap.put(GreetingFormat.VIDEO, (long)17351);
        sizeMap.put(GreetingType.BUSY, formatMap);
        formatMap = new EnumMap<GreetingFormat, Long>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, (long)19578);
        formatMap.put(GreetingFormat.VIDEO, (long)20631);
        sizeMap.put(GreetingType.CDG, formatMap);
        formatMap = new EnumMap<GreetingFormat, Long>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, (long)24698);
        formatMap.put(GreetingFormat.VIDEO, (long)25879);
        sizeMap.put(GreetingType.EXTENDED_ABSENCE, formatMap);
        formatMap = new EnumMap<GreetingFormat, Long>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, (long)18618);
        formatMap.put(GreetingFormat.VIDEO, (long)19647);
        sizeMap.put(GreetingType.NO_ANSWER, formatMap);
        formatMap = new EnumMap<GreetingFormat, Long>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, (long)19578);
        formatMap.put(GreetingFormat.VIDEO, (long)20631);
        sizeMap.put(GreetingType.OUT_OF_HOURS, formatMap);
        formatMap = new EnumMap<GreetingFormat, Long>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, (long)17658);
        formatMap.put(GreetingFormat.VIDEO, (long)18663);
        sizeMap.put(GreetingType.OWN_RECORDED, formatMap);
        formatMap = new EnumMap<GreetingFormat, Long>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, (long)19258);
        formatMap.put(GreetingFormat.VIDEO, (long)20303);
        sizeMap.put(GreetingType.TEMPORARY, formatMap);
        formatMap = new EnumMap<GreetingFormat, Long>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, (long)18618);
        formatMap.put(GreetingFormat.VIDEO, (long)19647);
        sizeMap.put(GreetingType.SPOKEN_NAME, formatMap);
        formatMap = new EnumMap<GreetingFormat, Long>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, (long)12218);
        sizeMap.put(GreetingType.DIST_LIST_SPOKEN_NAME, formatMap);
    }

    protected void setUpMediaObjectMap() throws Exception {
        mediaObjectMap = new EnumMap<GreetingType, Map<GreetingFormat, IMediaObject>>(GreetingType.class);
        MediaObjectFactory mediaObjectFactory = new MediaObjectFactory();

        for (GreetingType type : GreetingType.values()) {
            EnumMap<GreetingFormat, IMediaObject> formatMap = new EnumMap<GreetingFormat, IMediaObject>(GreetingFormat.class);
            for (GreetingFormat format : GreetingFormat.values()) {
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME && format == GreetingFormat.VIDEO) {
                    // No support for video distlistspokenname yet
                    continue;
                }
                MediaProperties mediaProperties = new MediaProperties(new MimeType(getContentType(format)));
                MediaLength length = new MediaLength(
                        MediaLength.LengthUnit.MILLISECONDS,
                        lengthMap.get(type)
                );
                mediaProperties.addLength(length);
                String fileName = MEDIA_FILE_PATH + fileNameMap.get(type) + "." + getFileExtension(format);
                formatMap.put(format, mediaObjectFactory.create(new File(fileName), mediaProperties));
            }
            mediaObjectMap.put(type, formatMap);
        }
    }

    /**
     * Test that a greeting message is correctly created
     * @throws Exception
     */
    public void testGreetingMessage(Message message, GreetingSpecification specification) throws Exception {
        if (specification.getType() == GreetingType.DIST_LIST_SPOKEN_NAME) {
            Assert.assertNull("Header " + GREETING_TYPE + " should not exist", message.getHeader(GREETING_TYPE));
            Assert.assertNull("Header " + GREETING_FORMAT + " should not exist", message.getHeader(GREETING_FORMAT));
        } else {
            assertEquals(
                    "Wrong greeting type header",
                    new String[]{GreetingUtils.getTypeHeader(specification)},
                    message.getHeader(GREETING_TYPE)
            );
            assertEquals(
                    "Wrong greeting format header",
                    new String[]{GreetingUtils.getFormatHeader(specification.getFormat())},
                    message.getHeader(GREETING_FORMAT)
            );
        }
        assertEquals("Wrong To header", new String[]{TELEPHONE_NUMBER}, message.getHeader("To"));
        assertEquals("Wrong From header", new String[]{"GrtAdm_33"}, message.getHeader("From"));
        Assert.assertEquals("Wrong subject", GreetingUtils.getSubjectString(specification), message.getSubject());
        assertMatches(
                "Wrong content type",
                new String[]{"multipart/Greeting-Message.*"},
                message.getHeader("Content-Type")
        );
        Object content = message.getContent();
        Assert.assertTrue("Content should be multipart", content instanceof Multipart);
        Multipart multipart = (Multipart)content;
        Assert.assertEquals("Multipart should contain only 1 part", 1, multipart.getCount());
        BodyPart bodyPart = multipart.getBodyPart(0);
        String fileName = fileNameMap.get(specification.getType()) + "." + getFileExtension(specification.getFormat());
        String type = getDispositionType(specification.getFormat());
        assertMatches(
                "Wrong Content-Disposition",
                new String[]{"inline; " + type + "=Greeting-Message;\\s+filename=" + fileName},
                bodyPart.getHeader("Content-Disposition")
        );
        assertEquals(
                "Wrong content type",
                new String[]{getContentType(specification.getFormat()) + "; name=" + fileName},
                bodyPart.getHeader("Content-Type")
        );
        Assert.assertEquals("Wrong Content-Description", "Greeting Message (1 seconds)", bodyPart.getDescription());
        assertEquals("Wrong Content-Duration", new String[]{"1"}, bodyPart.getHeader("Content-Duration"));
        // Todo: test content
    }

    public static void assertMatches(String applicationName, String[] expected, String[] actual) {
    	Assert.assertTrue(applicationName + "\nExpected:" + Arrays.toString(expected) + "\nActual  :" + Arrays.toString(actual),
                matches(expected, actual));
    }


    public static void assertMatches(String[] expected, String[] actual) {
        assertMatches("", expected,  actual);
    }

    private static boolean matches(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (!actual[i].matches("(?s)" + expected[i])) { // (?s) - dotall mode
                return false;
            }
        }
        return true;
    }

    private String getDispositionType(GreetingFormat format) {
        switch (format) {
            case VOICE: return "voice";
            case VIDEO: return "video";
            default:    return "unknown";
        }
    }

    protected Message getGreetingMessage(GreetingType type, GreetingFormat format) throws Exception {
        String fileName = MESSAGE_PATH + fileNameMap.get(type) + format.toString().toLowerCase() + ".txt";
        return new MimeMessage(SESSION, new FileInputStream(fileName));
    }
}
