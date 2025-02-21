package com.mobeon.masp.profilemanager;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * ProfileSettings Tester.
 *
 * @author <Authors name>
 * @since <pre>03/24/2006</pre>
 * @version 1.0
 */
public class ProfileSettingsTest extends ProfileManagerMockObjectBaseTestCase {
    private static final String CFGFILE = "test/com/mobeon/masp/profilemanager/profilemanager.xml";
    private ProfileSettings profileSettings;

    public ProfileSettingsTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        setUpProfileContext(getConfiguration(CFGFILE));

        // Setup subscriber
        ProfileAttributes profileAttributes = new ProfileAttributes(baseContext);
        // String attributes
        profileAttributes.put("billingnumber", getProfileAttribute("billingnumber"));
        profileAttributes.put("emservicedn", getProfileAttribute("emservicedn1", "emservicedn2"));
        // Xstring attributes
        profileAttributes.put("password", getProfileAttribute("3Z191R240A0L472u"));
        profileAttributes.put("umpassword", getProfileAttribute("08123b0z06742U0C", "3Z191R240A0L472u"));
        // Boolean attributes
        profileAttributes.put("autoplay", getProfileAttribute("yes"));
        profileAttributes.put("callerxfer", getProfileAttribute("no", "yes"));
        // Integer attributes
        profileAttributes.put("badlogincount", getProfileAttribute("1"));
        profileAttributes.put("cdgmax", getProfileAttribute("2", "3"));

        profileSettings = new ProfileSettings(baseContext, profileAttributes);
    }

    private ProfileAttribute getProfileAttribute(String... data) {
        return new ProfileAttribute(data);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetStringAttribute() throws Exception {
        assertEquals("billingnumber", profileSettings.getStringAttribute("billingnumber"));
        assertEquals("emservicedn1", profileSettings.getStringAttribute("emservicedn"));
        assertEquals("12345", profileSettings.getStringAttribute("inhoursdow")); // Test default value
        assertEquals("abcd", profileSettings.getStringAttribute("password"));
        assertEquals("1111", profileSettings.getStringAttribute("umpassword"));
        for (String attribute : new String[] {"unknownattribute", "cn", "badlogincount", "autoplay"}) {
            try {
                profileSettings.getStringAttribute(attribute);
            } catch (UnknownAttributeException e) {
                assertTrue(true); // For statistical purposes
            }
        }
    }

    public void testGetStringAttributes() throws Exception {
        assertEquals(
                new String[] {"billingnumber"},
                profileSettings.getStringAttributes("billingnumber")
        );
        assertEquals(
                new String[] {"emservicedn1", "emservicedn2"},
                profileSettings.getStringAttributes("emservicedn")
        );
        assertEquals(new String[] {"12345"}, profileSettings.getStringAttributes("inhoursdow")); // Test default value
        assertEquals(new String[] {"abcd"}, profileSettings.getStringAttributes("password"));
        assertEquals(new String[] {"1111", "abcd"}, profileSettings.getStringAttributes("umpassword"));
        for (String attribute : new String[] {"unknownattribute", "cn", "badlogincount", "autoplay"}) {
            try {
                profileSettings.getStringAttributes(attribute);
            } catch (UnknownAttributeException e) {
                assertTrue(true); // For statistical purposes
            }
        }
    }

    public void testGetIntegerAttribute() throws Exception {
        assertEquals(1, profileSettings.getIntegerAttribute("badlogincount"));
        assertEquals(2, profileSettings.getIntegerAttribute("cdgmax"));
        assertEquals(15, profileSettings.getIntegerAttribute("dlentriesmax")); // Test default value
        for (String attribute : new String[] {"unknownattribute", "billingnumber", "mailquota", "autoplay"}) {
            try {
                profileSettings.getIntegerAttribute(attribute);
            } catch (UnknownAttributeException e) {
                assertTrue(true); // For statistical purposes
            }
        }
    }

    public void testGetIntegerAttributes() throws Exception {
        assertEquals(new int[] {1}, profileSettings.getIntegerAttributes("badlogincount"));
        assertEquals(new int[] {2, 3}, profileSettings.getIntegerAttributes("cdgmax"));
        assertEquals(new int[] {15}, profileSettings.getIntegerAttributes("dlentriesmax")); // Test default value
        for (String attribute : new String[] {"unknownattribute", "billingnumber", "mailquota", "autoplay"}) {
            try {
                profileSettings.getIntegerAttributes(attribute);
            } catch (UnknownAttributeException e) {
                assertTrue(true); // For statistical purposes
            }
        }
    }

    public void testGetBooleanAttribute() throws Exception {
        assertEquals(true, profileSettings.getBooleanAttribute("autoplay"));
        assertEquals(false, profileSettings.getBooleanAttribute("callerxfer"));
        assertEquals(false, profileSettings.getBooleanAttribute("callerxfertocoverage")); // Test default value
        for (String attribute : new String[] {"unknownattribute", "billingnumber", "badlogincount"}) {
            try {
                profileSettings.getBooleanAttribute(attribute);
            } catch (UnknownAttributeException e) {
                assertTrue(true); // For statistical purposes
            }
        }
    }

    public void testGetBooleanAttributes() throws Exception {
        assertEquals(new boolean[] {true}, profileSettings.getBooleanAttributes("autoplay"));
        assertEquals(new boolean[] {false, true}, profileSettings.getBooleanAttributes("callerxfer"));
        // Test default value
        assertEquals(
                new boolean[] {false},
                profileSettings.getBooleanAttributes("callerxfertocoverage")
        );
        for (String attribute : new String[] {"unknownattribute", "billingnumber", "badlogincount"}) {
            try {
                profileSettings.getBooleanAttributes(attribute);
            } catch (UnknownAttributeException e) {
                assertTrue(true); // For statistical purposes
            }
        }
    }

    public static Test suite() {
        return new TestSuite(ProfileSettingsTest.class);
    }
}
