/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import com.mobeon.masp.profilemanager.search.ProfileStringCriteria;
import com.mobeon.masp.profilemanager.greetings.*;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaLength;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Manual tests of Subscriber class
 *
 * @author mande
 */
public class SubscriberMTest extends ProfileManagerMockObjectBaseMTestCase {
    private IProfile profile;

    public SubscriberMTest(String string) {
        super(string);
    }

    public void setUp() throws Exception {
        super.setUp();

        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        profile = profiles[0];
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test getting an existing subscriber's mailbox
     * @throws Exception
     */
    public void testGetMailbox() throws Exception {
        IMailbox mailbox = profile.getMailbox();
        assertNotNull("Mailbox should not be null", mailbox);
        // Todo: more tests?
    }

    /**
     * Test getting and setting a subscriber's all calls greeting
     */
    public void testGreetings() throws Exception {
        String subId = "12345";
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME && format == GreetingFormat.VIDEO) {
                    // No support for video distlistspokenname yet
                    continue;
                }
                GreetingSpecification specification = new SpokenNameSpecification(type, format);
                if (SUBID_TYPES.contains(type)) {
                    specification.setSubId(subId);
                }
                // Remove greeting
                profile.setGreeting(specification, null);

                // Greeting should not exist
                try {
                    profile.getGreeting(specification);
                    fail("Expected GreetingNotFoundException");
                } catch (GreetingNotFoundException e) {
                    assertTrue(true); // For statistical purposes
                }

                // Create greeting
                profile.setGreeting(specification, mediaObjectMap.get(type).get(format));

                // Get greeting
                IMediaObject greeting = profile.getGreeting(specification);
                assertNotNull("Greeting should not be null", greeting);
                MediaProperties mediaProperties = greeting.getMediaProperties();
                String contentType = getContentType(format);
                assertTrue("Content type should be " + contentType, mediaProperties.getContentType().match(contentType));
                String fileExtension = getFileExtension(format);
                assertEquals("File extension should be " + fileExtension, fileExtension, mediaProperties.getFileExtension());
                long mediaLength = lengthMap.get(type);
                assertEquals("Media length should be " + mediaLength, mediaLength,
                        mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
                long size = sizeMap.get(type).get(format);
                assertEquals("Size should be " + size, size, mediaProperties.getSize());
            }
        }
    }

    /**
     * Test getting an existing subscriber's spoken name
     */
    public void testSpokenName() throws Exception {
        GreetingType type = GreetingType.SPOKEN_NAME;
        for (GreetingFormat format : GreetingFormat.values()) {
            // Remove spoken name
            profile.setSpokenName(format, null);

            // Spoken name should not exist
            try {
                profile.getSpokenName(format);
                fail("Expected GreetingNotFoundException");
            } catch (GreetingNotFoundException e) {
                assertTrue(true); // For statistical purposes
            }

            // Create spoken name
            profile.setSpokenName(format, mediaObjectMap.get(type).get(format));

            // Get spokenName
            IMediaObject spokenName = profile.getSpokenName(format);
            assertNotNull("Spoken name should not be null", spokenName);
            MediaProperties mediaProperties = spokenName.getMediaProperties();
            String contentType = getContentType(format);
            assertTrue("Content type should be " + contentType, mediaProperties.getContentType().match(contentType));
            String fileExtension = getFileExtension(format);
            assertEquals("File extension should be " + fileExtension, fileExtension, mediaProperties.getFileExtension());
            long mediaLength = lengthMap.get(type);
            assertEquals("Media length should be " + mediaLength, mediaLength,
                    mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
            long size = sizeMap.get(type).get(format);
            assertEquals("Size should be " + size, size, mediaProperties.getSize());
        }
    }

    public void testGetEmServiceDnCosAttribute() throws Exception {
        assertEquals(
                new String[] {
                        "emservicename=webmail, ou=services, o=mobeon.com",
                        "emservicename=msgtype_email, ou=services, o=mobeon.com",
                        "emservicename=msgtype_voice, ou=services, o=mobeon.com",
                        "emservicename=msgtype_video, ou=services, o=mobeon.com",
                        "emservicename=sms_notification, ou=services, o=mobeon.com",
                        "emservicename=address_book, ou=services, o=mobeon.com",
                        "emservicename=call_handling, ou=services, o=mobeon.com"
                },
                profile.getStringAttributes("emservicedn")
        );
    }

    public void testGetEmServiceDnUserAttribute() throws Exception {
        assertEquals(
                new String[] {"cos=24,ou=C6,o=mobeon.com"},
                profile.getStringAttributes("emservicednuser")
        );
    }

    public void testSetAttribute() throws Exception {
        profile.setStringAttribute("inhoursstart", "0800");
    }

    public void testGetCos() throws Exception {
        ICos cos = profile.getCos();
        testCos(cos);
    }

    public void testGetDistributionLists() throws Exception {
        IDistributionList[] distributionLists = profile.getDistributionLists();
        assertEquals("3 distribution lists should be returned", 3, distributionLists.length);
    }

    public void testCreateDistributionList() throws Exception {
        String id = "9999";
        IDistributionList distributionList = profile.createDistributionList(id);
        assertEquals("ID should be " + id, id, distributionList.getID());
        assertEquals("Members should be 0", 0, distributionList.getMembers().length);
        distributionList.addMember("mande2@lab.mobeon.com");
        assertEquals("Members should be 1", 1, distributionList.getMembers().length);
        distributionList.addMember("mande3@lab.mobeon.com");
        assertEquals("Members should be 2", 2, distributionList.getMembers().length);
        distributionList.removeMember("mande3@lab.mobeon.com");
        assertEquals("Members should be 1", 1, distributionList.getMembers().length);
        distributionList.removeMember("mande2@lab.mobeon.com");
        assertEquals("Members should be 0", 0, distributionList.getMembers().length);

        try {
            distributionList.getSpokenName();
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
        IMediaObject spokenName = mediaObjectMap.get(GreetingType.DIST_LIST_SPOKEN_NAME).get(GreetingFormat.VOICE);
        distributionList.setSpokenName(spokenName);
        assertNotNull("Spoken name should not be null", distributionList.getSpokenName());
        // Remove distribution list
        profile.deleteDistributionList(distributionList);
    }

    private void testCos(ICos cos) throws UnknownAttributeException {
        assertEquals("Video", cos.getStringAttribute("cosname"));
        assertEquals(
                new String[] {
                        "emservicename=webmail, ou=services, o=mobeon.com",
                        "emservicename=msgtype_email, ou=services, o=mobeon.com",
                        "emservicename=msgtype_voice, ou=services, o=mobeon.com",
                        "emservicename=msgtype_video, ou=services, o=mobeon.com",
                        "emservicename=sms_notification, ou=services, o=mobeon.com",
                        "emservicename=address_book, ou=services, o=mobeon.com",
                        "emservicename=call_handling, ou=services, o=mobeon.com"
                },
                cos.getStringAttributes("emservicedn")
        );
        assertEquals("0800", cos.getStringAttribute("inhoursstart"));
        assertEquals("1630", cos.getStringAttribute("inhoursend"));
    }

    public static Test suite() {
        return new TestSuite(SubscriberMTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
