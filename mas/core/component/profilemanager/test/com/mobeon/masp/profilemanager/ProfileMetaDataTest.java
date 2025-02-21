/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownParameterException;

import java.util.List;
import java.util.ArrayList;

/**
 * ProfileMetaData Tester.
 *
 * @author mande
 * @since <pre>11/22/2005</pre>
 * @version 1.0
 */
public class ProfileMetaDataTest extends ProfileManagerMockObjectBaseTestCase
{
    private static final String USER_REGISTER_NAME = "userregistername";
    private Mock mockIGroup;
    private Mock mockParent;
    private List<ProfileLevel> defaultSearchOrder;
    private List<ProfileLevel> cosSearchOrder;
    private List<ProfileLevel> cosUserSearchOrder;
    private List<ProfileLevel> cosUserBillingSearchOrder;
    private List<ProfileLevel> cosBillingUserSearchOrder;

    public ProfileMetaDataTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        setUpSearchOrders();
        mockIGroup = mock(IGroup.class);
        mockParent = mock(IGroup.class);
        mockParent.stubs().method("getFullName").will(returnValue("parent"));
    }

    private void setUpSearchOrders() {
        defaultSearchOrder = new ArrayList<ProfileLevel>();
        defaultSearchOrder.add(ProfileLevel.COMMUNITY);
        defaultSearchOrder.add(ProfileLevel.COS);
        defaultSearchOrder.add(ProfileLevel.USER);
        defaultSearchOrder.add(ProfileLevel.BILLING);

        cosSearchOrder = new ArrayList<ProfileLevel>();
        cosSearchOrder.add(ProfileLevel.COS);

        cosUserSearchOrder = new ArrayList<ProfileLevel>();
        cosUserSearchOrder.add(ProfileLevel.COS);
        cosUserSearchOrder.add(ProfileLevel.USER);

        cosUserBillingSearchOrder = new ArrayList<ProfileLevel>();
        cosUserBillingSearchOrder.add(ProfileLevel.COS);
        cosUserBillingSearchOrder.add(ProfileLevel.USER);
        cosUserBillingSearchOrder.add(ProfileLevel.BILLING);

        cosBillingUserSearchOrder = new ArrayList<ProfileLevel>();
        cosBillingUserSearchOrder.add(ProfileLevel.COS);
        cosBillingUserSearchOrder.add(ProfileLevel.BILLING);
        cosBillingUserSearchOrder.add(ProfileLevel.USER);
    }

    public void tearDown() throws Exception {
    }

    public void testGetMetaData() throws Exception {
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "string", "billing");
        ProfileMetaData metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.STRING, metaData.getType());
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.BILLING, metaData.getWriteLevel());
        assertNull("Default value should not exist", metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test xstring type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "xstring", "user");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.XSTRING, metaData.getType());
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.USER, metaData.getWriteLevel());
        assertNull("Default value should not exist", metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test integer type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "integer", "cos");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.INTEGER, metaData.getType());
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.COS, metaData.getWriteLevel());
        assertNull("Default value should not exist", metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test boolean type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "boolean", "community");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.BOOLEAN, metaData.getType());
        assertEquals(TRUESTRING, metaData.getBooleanString(true));
        assertEquals(FALSESTRING, metaData.getBooleanString(false));
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.COMMUNITY, metaData.getWriteLevel());
        assertNull("Default value should not exist", metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
    }


    public void testGetMetaDataReadOnly() throws Exception {
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "string");
        ProfileMetaData metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.STRING, metaData.getType());
        assertTrue("Attribute should be read only", metaData.isReadOnly());
        try {
            metaData.getWriteLevel();
            fail("Expected MetaDataException");
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }
        assertNull("Default value should not exist", metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test xstring type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "xstring");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.XSTRING, metaData.getType());
        assertTrue("Attribute should be read only", metaData.isReadOnly());
        try {
            metaData.getWriteLevel();
            fail("Expected MetaDataException");
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }
        assertNull("Default value should not exist", metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test integer type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "integer");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.INTEGER, metaData.getType());
        assertTrue("Attribute should be read only", metaData.isReadOnly());
        try {
            metaData.getWriteLevel();
            fail("Expected MetaDataException");
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }
        assertNull("Default value should not exist", metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test boolean type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "boolean");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.BOOLEAN, metaData.getType());
        assertEquals(TRUESTRING, metaData.getBooleanString(true));
        assertEquals(FALSESTRING, metaData.getBooleanString(false));
        assertTrue("Attribute should be read only", metaData.isReadOnly());
        try {
            metaData.getWriteLevel();
            fail("Expected MetaDataException");
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }
        assertNull("Default value should not exist", metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
    }

    public void testGetMetaDataDefaultValue() throws Exception {
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "string", "billing", "default");
        ProfileMetaData metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.STRING, metaData.getType());
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.BILLING, metaData.getWriteLevel());
        assertEquals(new String[]{"default"}, metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test xstring type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "xstring", "user", "default");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.XSTRING, metaData.getType());
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.USER, metaData.getWriteLevel());
        assertEquals(new String[]{"default"}, metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test integer type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "integer", "cos", "default");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.INTEGER, metaData.getType());
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.COS, metaData.getWriteLevel());
        assertEquals(new String[]{"default"}, metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test boolean type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "boolean", "community", "default");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.BOOLEAN, metaData.getType());
        assertEquals(TRUESTRING, metaData.getBooleanString(true));
        assertEquals(FALSESTRING, metaData.getBooleanString(false));
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.COMMUNITY, metaData.getWriteLevel());
        assertEquals(new String[]{"default"}, metaData.getDefaultValue());
        assertEquals(defaultSearchOrder, metaData.getSearchOrder());
    }

    public void testGetMetaDataSearchOrder() throws Exception {
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "string", "billing", "default", "cos");
        ProfileMetaData metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.STRING, metaData.getType());
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.BILLING, metaData.getWriteLevel());
        assertEquals(new String[]{"default"}, metaData.getDefaultValue());
        assertEquals(cosSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test xstring type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "xstring", "user", "default", "cos,user");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.XSTRING, metaData.getType());
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.USER, metaData.getWriteLevel());
        assertEquals(new String[]{"default"}, metaData.getDefaultValue());
        assertEquals(cosUserSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test integer type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "integer", "cos", "default", "cos,user,billing");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.INTEGER, metaData.getType());
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.COS, metaData.getWriteLevel());
        assertEquals(new String[]{"default"}, metaData.getDefaultValue());
        assertEquals(cosUserBillingSearchOrder, metaData.getSearchOrder());
        testBooleanString(metaData);

        // Test boolean type
        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "boolean", "community", "default", "cos,billing,user");
        metaData = new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        assertEquals(APPLICATION_NAME, metaData.getApplicationName());
        assertEquals(USER_REGISTER_NAME, metaData.getUserRegisterName());
        assertEquals(AttributeType.BOOLEAN, metaData.getType());
        assertEquals(TRUESTRING, metaData.getBooleanString(true));
        assertEquals(FALSESTRING, metaData.getBooleanString(false));
        assertFalse("Attribute should not be read only", metaData.isReadOnly());
        assertEquals(ProfileLevel.COMMUNITY, metaData.getWriteLevel());
        assertEquals(new String[]{"default"}, metaData.getDefaultValue());
        assertEquals(cosBillingUserSearchOrder, metaData.getSearchOrder());
    }

    public void testCreateMetaDataUnknownParameterException() throws Exception {
        mockIGroup.expects(once()).method("getName").will(returnValue(APPLICATION_NAME));
        mockIGroup.expects(once()).method("getString").with(eq(CFG_USER_REGISTER_NAME)).
                will(throwException(new UnknownParameterException(CFG_USER_REGISTER_NAME,
                        (IGroup)mockParent.proxy())));
        try {
            new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
            fail("Expected MissingParameterValueException");
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetSearchOrderIllegalArgumentException() throws Exception {
        List<ProfileLevel> searchOrder = ProfileMetaData.getSearchOrder("unknownlevel");
        assertEquals(0, searchOrder.size());
    }

    public void testCheckValidity() throws Exception {
        mockIGroup = getMockAttributeGroup("", USER_REGISTER_NAME, "string", "billing");
        try {
            new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }

        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, "", "string", "billing");
        try {
            new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }

        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "string", "");
        try {
            new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }

        mockIGroup = getMockAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "string", "billing", "default", "");
        try {
            new ProfileMetaData((IGroup)mockIGroup.proxy(), DEFAULT_SEARCH_ORDER);
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }

        IGroup mockGroup = getMockBooleanAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, "", CFG_FALSE);
        try {
            new ProfileMetaData(mockGroup, DEFAULT_SEARCH_ORDER);
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }
        
        mockGroup = getMockBooleanAttributeGroup(APPLICATION_NAME, USER_REGISTER_NAME, CFG_TRUE, "");
        try {
            new ProfileMetaData(mockGroup, DEFAULT_SEARCH_ORDER);
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    private void testBooleanString(ProfileMetaData metaData) {
        try {
            metaData.getBooleanString(true);
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }
        try {
            metaData.getBooleanString(false);
        } catch (MetaDataException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    private IGroup getMockBooleanAttributeGroup(String applicationName, String userRegisterName,
                                                String trueString, String falseString) {
        return getMockBooleanAttributeGroup(applicationName, userRegisterName, trueString, falseString, null, null, null);
    }

    private IGroup getMockBooleanAttributeGroup(String applicationName, String userRegisterName,
                                                String trueString, String falseString, String writeLevel,
                                                String defaultValue, String searchOrder)
    {
    Mock mockIGroup = mock(IGroup.class, "mock" + capitalize(applicationName) + "Group");
        mockIGroup.expects(once()).method("getName").will(returnValue(applicationName));
        mockIGroup.expects(once()).method("getString").with(eq(CFG_USER_REGISTER_NAME)).
                will(returnValue(userRegisterName));
        mockIGroup.expects(once()).method("getString").with(eq(CFG_TYPE)).
                will(returnValue("boolean"));
        mockIGroup.expects(once()).method("getString").with(eq(CFG_TRUE)).
                will(returnValue(trueString));
        mockIGroup.expects(once()).method("getString").with(eq(CFG_FALSE)).
                will(returnValue(falseString));
        if (writeLevel != null) {
            mockIGroup.expects(once()).method("getString").with(eq(CFG_WRITELEVEL), eq("unknown")).
                    will(returnValue(writeLevel));
        } else {
            mockIGroup.expects(once()).method("getString").with(eq(CFG_WRITELEVEL), eq("unknown")).
                    will(returnValue("unknown"));
        }
        if (defaultValue != null) {
            mockIGroup.expects(once()).method("getString").with(eq(CFG_DEFAULTVALUE)).
                    will(returnValue(defaultValue));
        } else {
            mockIGroup.expects(once()).method("getString").with(eq(CFG_DEFAULTVALUE)).
                    will(throwException(new UnknownParameterException(CFG_DEFAULTVALUE, getParentGroup())));
        }
        if (searchOrder != null) {
            mockIGroup.expects(once()).method("getString").with(eq(CFG_SEARCHORDER)).
                    will(returnValue(searchOrder));
        } else {
            mockIGroup.expects(once()).method("getString").with(eq(CFG_SEARCHORDER)).
                    will(throwException(new UnknownParameterException(CFG_SEARCHORDER, getParentGroup())));
        }

        return (IGroup)mockIGroup.proxy();
    }

    public static Test suite() {
        return new TestSuite(ProfileMetaDataTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
