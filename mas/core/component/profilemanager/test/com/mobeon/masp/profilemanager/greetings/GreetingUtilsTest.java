/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

import junit.framework.Assert;
import jakarta.activation.MimeType;

import org.jmock.integration.junit4.JMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;

/**
 * GreetingUtils Tester.
 *
 * @author mande
 * @since <pre>01/18/2006</pre>
 * @version 1.0
 */
@RunWith(JMock.class)
public class GreetingUtilsTest extends GreetingMockObjectBaseTestCase
{
    private Map<GreetingType, Map<GreetingFormat, String>> subjectMap;
    private Map<GreetingType, String> formatMap;
    /**
     * Set containing enums having a subid.
     */
    private static final Set<GreetingType> SUB_ID_TYPES = EnumSet.of(GreetingType.CDG, GreetingType.DIST_LIST_SPOKEN_NAME);


    @Before
    public void setUp() throws Exception {
        super.setUp();

        setUpSubjectMap();
        setUpFormatMap();
    }

    private void setUpFormatMap() {
        formatMap = new EnumMap<GreetingType, String>(GreetingType.class);
        formatMap.put(GreetingType.ALL_CALLS, "AllCalls");
        formatMap.put(GreetingType.BUSY, "Busy");
        formatMap.put(GreetingType.CDG, "CDG12345#");
        formatMap.put(GreetingType.EXTENDED_ABSENCE, "Extended_Absence");
        formatMap.put(GreetingType.NO_ANSWER, "NoAnswer");
        formatMap.put(GreetingType.OUT_OF_HOURS, "OutOfHours");
        formatMap.put(GreetingType.OWN_RECORDED, "OwnRecorded");
        formatMap.put(GreetingType.SPOKEN_NAME, "SpokenName");
        formatMap.put(GreetingType.TEMPORARY, "Temporary");

    }

    private void setUpSubjectMap() {
        subjectMap = new EnumMap<GreetingType, Map<GreetingFormat, String>>(GreetingType.class);
        Map<GreetingFormat, String> formatMap;
        formatMap = new EnumMap<GreetingFormat, String>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, "AllCalls (voice)");
        formatMap.put(GreetingFormat.VIDEO, "All Calls (video)");
        subjectMap.put(GreetingType.ALL_CALLS, formatMap);
        formatMap = new EnumMap<GreetingFormat, String>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, "Busy (voice)");
        formatMap.put(GreetingFormat.VIDEO, "Occupied (video)");
        subjectMap.put(GreetingType.BUSY, formatMap);
        formatMap = new EnumMap<GreetingFormat, String>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, "This is your greeting for 12345 (voice)");
        formatMap.put(GreetingFormat.VIDEO, "This is your greeting for 12345 (video)");
        subjectMap.put(GreetingType.CDG, formatMap);
        formatMap = new EnumMap<GreetingFormat, String>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, "Extended_Absence (voice)");
        formatMap.put(GreetingFormat.VIDEO, "Extended Absence (video)");
        subjectMap.put(GreetingType.EXTENDED_ABSENCE, formatMap);
        formatMap = new EnumMap<GreetingFormat, String>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, "NoAnswer (voice)");
        formatMap.put(GreetingFormat.VIDEO, "No Answer (video)");
        subjectMap.put(GreetingType.NO_ANSWER, formatMap);
        formatMap = new EnumMap<GreetingFormat, String>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, "OutOfHours (voice)");
        formatMap.put(GreetingFormat.VIDEO, "Out Of Hours (video)");
        subjectMap.put(GreetingType.OUT_OF_HOURS, formatMap);
        formatMap= new EnumMap<GreetingFormat, String>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, "OwnRecorded (voice)");
        formatMap.put(GreetingFormat.VIDEO, "OwnRecorded (video)");
        subjectMap.put(GreetingType.OWN_RECORDED, formatMap);
        formatMap = new EnumMap<GreetingFormat, String>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, "Temporary (voice)");
        formatMap.put(GreetingFormat.VIDEO, "Temporary (video)");
        subjectMap.put(GreetingType.TEMPORARY, formatMap);
        formatMap = new EnumMap<GreetingFormat, String>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, "SpokenName (voice)");
        formatMap.put(GreetingFormat.VIDEO, "Spoken Name (video)");
        subjectMap.put(GreetingType.SPOKEN_NAME, formatMap);
        formatMap = new EnumMap<GreetingFormat, String>(GreetingFormat.class);
        formatMap.put(GreetingFormat.VOICE, "1");
        subjectMap.put(GreetingType.DIST_LIST_SPOKEN_NAME, formatMap);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetFormatHeaderGreetingType() throws Exception {
        String subId = "12345";
        GreetingSpecification specification;
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME) {
                    // Distribution list spoken name has no format header
                    continue;
                }
                if (STRING_REPRESENTED_TYPES.contains(type)) {
                    specification = new GreetingSpecification(greetingMap.get(type), format);
                } else {
                    specification = new GreetingSpecification(type, format);
                }
                if (type == GreetingType.CDG) {
                    specification.setSubId(subId);
                }
                Assert.assertEquals(formatMap.get(type), GreetingUtils.getTypeHeader(specification));
            }
        }
    }

    @Test
    public void testGetFormatHeaderGreetingFormat() throws Exception {
    	Assert.assertEquals("voice", GreetingUtils.getFormatHeader(GreetingFormat.VOICE));
    	Assert.assertEquals("video", GreetingUtils.getFormatHeader(GreetingFormat.VIDEO));
    }

    @Test
    public void testGetSubject() throws Exception {
        GreetingSpecification specification;
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                if (type == GreetingType.CDG) {
                    specification = new GreetingSpecification(greetingMap.get(type), format, "12345");
                } else if (type == GreetingType.DIST_LIST_SPOKEN_NAME) {
                    if (format == GreetingFormat.VIDEO) {
                        // No support for video distlistspokenname yet
                        continue;
                    }
                    specification = new GreetingSpecification(type, format, "1");
                } else {
                    specification = new GreetingSpecification(type, format);
                }
                Assert.assertEquals(subjectMap.get(type).get(format), GreetingUtils.getSubjectString(specification));
            }
        }
    }

    @Test
    public void testGetSubjectStringIllegalArgumentException() throws Exception {
        GreetingSpecification specification;
        String subId = "12345";
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME && format == GreetingFormat.VIDEO) {
                    // No support for video distlistspokenname yet
                    continue;
                }
                try {
                    if (STRING_REPRESENTED_TYPES.contains(type)) {
                        specification = new GreetingSpecification(greetingMap.get(type), format);
                    } else {
                        specification = new GreetingSpecification(type, format);
                    }
                    if (!SUB_ID_TYPES.contains(type)) {
                        specification.setSubId(subId);
                    }
                    GreetingUtils.getSubjectString(specification);
                    Assert.fail("Expected IllegalArgumentException for " + specification);
                } catch (IllegalArgumentException e) {
                	Assert.assertTrue(true); // For statistical purposes
                }
            }
        }
    }

    @Test
    public void testGetTypeHeaderIllegalArgumentException() throws Exception {
        GreetingSpecification specification;
        String subId = "12345";
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                try {
                    if (type == GreetingType.DIST_LIST_SPOKEN_NAME) {
                        // Distribution list spoken name has no type header
                        continue;
                    }
                    if (STRING_REPRESENTED_TYPES.contains(type)) {
                        specification = new GreetingSpecification(greetingMap.get(type), format);
                    } else {
                        specification = new GreetingSpecification(type, format);
                    }
                    if (type != GreetingType.CDG) {
                        specification.setSubId(subId);
                    }
                    GreetingUtils.getTypeHeader(specification);
                    Assert.fail("Expected IllegalArgumentException for " + specification);
                } catch (IllegalArgumentException e) {
                	Assert.assertTrue(true); // For statistical purposes
                }
            }
        }
    }

    @Test
    public void testGetDispositionString() throws Exception {
    	Assert.assertEquals("inline; voice=Greeting-Message", GreetingUtils.getDispositionString(GreetingFormat.VOICE));
    	Assert.assertEquals("inline; video=Greeting-Message", GreetingUtils.getDispositionString(GreetingFormat.VIDEO));
    }

    @Test
    public void testGetGreetingFileName() throws Exception {
        // test wav first
        MimeType contentType = new MimeType("audio/wav");
        GreetingSpecification specification;

        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                StringBuilder fileName = new StringBuilder(fileNameMap.get(type));
                if (STRING_REPRESENTED_TYPES.contains(type)) {
                    specification = new GreetingSpecification(greetingMap.get(type), format);
                } else {
                    specification = new GreetingSpecification(type, format);
                }
                if (type == GreetingType.CDG) {
                    specification.setSubId(SUBID);
                }
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME) {
                    specification.setSubId(DIST_LIST_SPOKEN_NAME_SUBID);
                }
                
                fileName.append(getMediaFilePostFix(format));
                fileName.append(getFileExtension(format));
                Assert.assertEquals(fileName.toString(), GreetingUtils.getGreetingFileName(specification, contentType));
            }
        }

        // test amr
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                if (format == GreetingFormat.VOICE)
                    contentType = new MimeType("audio/3gpp");
                else
                    contentType = new MimeType("video/3gpp");

                StringBuilder fileName = new StringBuilder(fileNameMap.get(type));
                if (STRING_REPRESENTED_TYPES.contains(type)) {
                    specification = new GreetingSpecification(greetingMap.get(type), format);
                } else {
                    specification = new GreetingSpecification(type, format);
                }
                if (type == GreetingType.CDG) {
                    specification.setSubId(SUBID);
                }
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME) {
                    specification.setSubId(DIST_LIST_SPOKEN_NAME_SUBID);
                }
                fileName.append(getMediaFilePostFix(format));
                fileName.append(getFileExtension(format, contentType));
                System.out.println(fileName);
                Assert.assertEquals(fileName.toString(), GreetingUtils.getGreetingFileName(specification, contentType));
            }
        }
    }

    @Test
    public void testGetGreetingFileNameIllegalArgumentException() throws Exception {
        GreetingSpecification specification;

        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                if (STRING_REPRESENTED_TYPES.contains(type)) {
                    specification = new GreetingSpecification(greetingMap.get(type), format);
                } else {
                    specification = new GreetingSpecification(type, format);
                }
                if (!SUB_ID_TYPES.contains(type)) {
                    specification.setSubId("subid");
                }
                try {
                    GreetingUtils.getGreetingFileName(specification, null);
                    Assert.fail("Expected IllegalArgumentException");
                } catch (IllegalArgumentException e) {
                	Assert.assertTrue(true); // For statistical purposes
                }
            }
        }
    }
    
    private String getMediaFilePostFix(GreetingFormat format) {
        switch (format) {
		case VOICE:
			return "_voice.";
			
		case VIDEO:
			return "_video.";

		default:
			Assert.fail("Unexpected greeting format: " + format);
		}

        return null;
    }
}
