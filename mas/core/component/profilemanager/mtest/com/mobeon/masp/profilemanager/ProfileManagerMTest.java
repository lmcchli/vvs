/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import com.mobeon.masp.profilemanager.search.ProfileStringCriteria;
import com.mobeon.masp.profilemanager.subscription.Subscription;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Function test of profile manager
 *
 * @author mande
 */
public class ProfileManagerMTest extends ProfileManagerMockObjectBaseMTestCase {
    private static final String COS_DN = "cos=6,ou=c6,o=mobeon.com";
    private static final String COMMUNITY_DN = "ou=c6,o=mobeon.com";
    private static final String ADMIN_UID = "andreasadmin";

    public ProfileManagerMTest(String string) {
        super(string);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test getting a subscriber using invalid attribute
     */
    public void testGetInvalidAttributeProfile() throws Exception {
        ProfileStringCriteria filter = new ProfileStringCriteria("nonexisting", "");
        try {
            profileManager.getProfile(filter);
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting an nonexisting subscriber
     * @throws Exception
     */
    public void testGetZeroProfiles() throws Exception {
        //setUpZeroProfilesResult();
        // Create a searchcriteria matching no subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "nonexisting");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("No profiles should be returned.", 0, profiles.length);
    }

    /**
     * Test getting an existing subscriber from a billingnumber search result
     * @throws Exception
     */
    public void testGetOneProfileBilling() throws Exception {
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        testProfile(profiles[0]);
    }

    /**
     * Test getting an existing subscriber from a billingnumber search result
     * @throws Exception
     */
    public void testGetOneProfileLimitScope() throws Exception {
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(filter, true);
        assertEquals("One profile should be returned.", 1, profiles.length);
        testProfile(profiles[0]);
    }

    /**
     * Test getting an existing subscriber from a userlevel search result
     * @throws Exception
     */
    public void testGetOneProfileUserLevel() throws Exception {
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("mail", "mande1@lab.mobeon.com");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        testProfile(profiles[0]);
    }

    /**
     * Test getting a number of existing subscribers
     * @throws Exception
     */
    public void testGetManyProfiles() throws Exception {
        // Create a searchcriteria matching more than one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "1916*");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("Ten profiles should be returned.", 10, profiles.length);
//        for (IProfile profile : profiles) {
//            testProfile(profile);
//        }
    }


    public void testGetCos() throws Exception {
        ICos cos = profileManager.getCos(COS_DN);
        testCos(cos);
    }


    private Subscription getSubscription() {
        Subscription subscription = new Subscription();
        subscription.addAttribute("billingnumber", "060161916");
        subscription.addAttribute("mailhost", "ockelbo.lab.mobeon.com");
        subscription.addAttribute("cosdn", "cos=6,ou=c6,o=mobeon.com");
        return subscription;
    }

    private void testCos(ICos cos) throws UnknownAttributeException {
        assertEquals("Normal", cos.getStringAttribute("cosname"));
        assertEquals(
                new String[] {
                        "emservicename=webmail, ou=services, o=mobeon.com",
                        "emservicename=msgtype_email, ou=services, o=mobeon.com",
                        "emservicename=msgtype_voice, ou=services, o=mobeon.com",
                        "emservicename=sms_notification, ou=services, o=mobeon.com",
                        "emservicename=call_handling, ou=services, o=mobeon.com"
                },
                cos.getStringAttributes("emservicedn")
        );
        assertEquals("0800", cos.getStringAttribute("inhoursstart"));
        assertEquals("1630", cos.getStringAttribute("inhoursend"));
    }

    /**
     * Test that a received profile seems correct
     * @param profile
     */
    private void testProfile(IProfile profile) throws Exception {
        assertNotNull("Profile should not be null", profile);
        // Test billingnumber entries
        assertEquals(new String[]{"19161"}, profile.getStringAttributes("billingnumber"));
        // Test userlevel entries
        assertEquals(new String[]{"mande1@lab.mobeon.com"}, profile.getStringAttributes("mail"));
        // Test cos entries
        assertEquals(new String[]{"Video"}, profile.getStringAttributes("cosname"));
        // Test community entries
        assertEquals(new String[]{"lab.mobeon.com"}, profile.getStringAttributes("emallowedmaildomains"));
    }

    public static Test suite() {
        return new TestSuite(ProfileManagerMTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
