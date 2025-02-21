/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.search;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.*;

import com.mobeon.masp.profilemanager.ProfileMetaData;
import com.mobeon.masp.profilemanager.UnknownAttributeException;
import com.mobeon.masp.profilemanager.ProfileManagerMockObjectBaseTestCase;
import com.mobeon.common.configuration.IGroup;
import org.jmock.Mock;

/**
 * LdapFilterFactory Tester.
 *
 * @author mande
 * @since <pre>11/15/2005</pre>
 * @version 1.0
 */
public class LdapFilterFactoryTest extends ProfileManagerMockObjectBaseTestCase
{
    private static final String STRINGAPPNAME_1 = "appString1";
    private static final String STRINGMURNAME_1 = "murString1";
    private static final String STRINGAPPNAME_2 = "appString2";
    private static final String STRINGMURNAME_2 = "murString2";
    private static final String STRINGVALUE_1 = "stringValue1";
    private static final String STRINGVALUE_2 = "stringValue2";
    private static final String BOOLAPPNAME_1 = "appBool1";
    private static final String BOOLMURNAME_1 = "murBool1";
    private static final String BOOLAPPNAME_2 = "appBool2";
    private static final String BOOLMURNAME_2 = "murBool2";
    private static final boolean BOOLVALUE_1 = true;
    private static final boolean BOOLVALUE_2 = false;
    private static final String INTAPPNAME_1 = "appBool1";
    private static final String INTMURNAME_1 = "murBool1";
    private static final String INTAPPNAME_2 = "appBool2";
    private static final String INTMURNAME_2 = "murBool2";
    private static final int INTVALUE_1 = 1;
    private static final int INTVALUE_2 = 2;
    private static final ProfileStringCriteria STRINGCRITERIA_1 = new ProfileStringCriteria(STRINGAPPNAME_1, STRINGVALUE_1);
    private static final ProfileStringCriteria STRINGCRITERIA_2 = new ProfileStringCriteria(STRINGAPPNAME_2, STRINGVALUE_2);
    private static final ProfileBooleanCriteria BOOLCRITERIA_1 = new ProfileBooleanCriteria(BOOLAPPNAME_1, BOOLVALUE_1);
    private static final ProfileBooleanCriteria BOOLCRITERIA_2 = new ProfileBooleanCriteria(BOOLAPPNAME_2, BOOLVALUE_2);
    private static final ProfileIntegerCriteria INTCRITERIA_1 = new ProfileIntegerCriteria(INTAPPNAME_1, INTVALUE_1);
    private static final ProfileIntegerCriteria INTCRITERIA_2 = new ProfileIntegerCriteria(INTAPPNAME_2, INTVALUE_2);
    private static final String FILTER_ERROR = "LDAP filter not correct.";
    private Map<String, ProfileMetaData> attributeMap;
    private static final String STRINGTYPE = "string";
    private static final String INTEGERTYPE = "integer";
    private static final String BOOLEANTYPE = "boolean";
    public LdapFilterFactoryTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        attributeMap = new HashMap<String, ProfileMetaData>();
        addAttributes();
    }

    private void addAttributes() throws Exception {
        addAttribute(STRINGAPPNAME_1, STRINGMURNAME_1, STRINGTYPE);
        addAttribute(STRINGAPPNAME_2, STRINGMURNAME_2, STRINGTYPE);
        addAttribute(INTAPPNAME_1, INTMURNAME_1, INTEGERTYPE);
        addAttribute(INTAPPNAME_2, INTMURNAME_2, INTEGERTYPE);
        addAttribute(BOOLAPPNAME_1, BOOLMURNAME_1, BOOLEANTYPE);
        addAttribute(BOOLAPPNAME_2, BOOLMURNAME_2, BOOLEANTYPE);
    }

    private void addAttribute(String applicationName, String userRegisterName, String type)
            throws Exception {

        Mock mockGroup = getMockAttributeGroup(applicationName, userRegisterName, type);

        ProfileMetaData metaData = new ProfileMetaData((IGroup) mockGroup.proxy(), DEFAULT_SEARCH_ORDER);
        attributeMap.put(applicationName, metaData);
    }

    /**
     * Test getting an LDAP filter for a single string criteria
     * @throws Exception
     */
    public void testGetLdapStringAttributeFilter() throws Exception {
        String filter = LdapFilterFactory.getLdapFilter(STRINGCRITERIA_1, attributeMap);
        assertEquals(FILTER_ERROR, getFilter(STRINGMURNAME_1, STRINGVALUE_1), filter);
    }

    /**
     * Test getting an LDAP filter for an unknonw single string criteria
     * @throws Exception
     */
    public void testGetLdapStringAttributeFilterUnknownAttributeException() throws Exception {
        try {
            LdapFilterFactory.getLdapFilter(new ProfileStringCriteria("unknown", "value"), attributeMap);
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting an LDAP filter for a single boolean criteria
     * @throws Exception
     */
    public void testGetLdapBooleanAttributeFilter() throws Exception {
        String filter = LdapFilterFactory.getLdapFilter(BOOLCRITERIA_1, attributeMap);
        assertEquals(FILTER_ERROR, getFilter(BOOLMURNAME_1, BOOLVALUE_1), filter);
    }

    /**
     * Test getting an LDAP filter for an unknonw single string criteria
     * @throws Exception
     */
    public void testGetLdapBooleanAttributeFilterUnknownAttributeException() throws Exception {
        try {
            LdapFilterFactory.getLdapFilter(new ProfileBooleanCriteria("unknown", true), attributeMap);
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting an LDAP filter for a single integer criteria
     * @throws Exception
     */
    public void testGetLdapIntegerAttributeFilter() throws Exception {
        String filter = LdapFilterFactory.getLdapFilter(INTCRITERIA_1, attributeMap);
        assertEquals(FILTER_ERROR, getFilter(INTMURNAME_1, INTVALUE_1), filter);
    }

    /**
     * Test getting an LDAP filter for an unknonw single string criteria
     * @throws Exception
     */
    public void testGetLdapIntegerAttributeFilterUnknownAttributeException() throws Exception {
        try {
            LdapFilterFactory.getLdapFilter(new ProfileIntegerCriteria("unknown", 1), attributeMap);
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting a filter for an or criteria
     * @throws Exception
     */
    public void testGetLdapOrFilter() throws Exception {
        // Test simple or
        ProfileOrCritera orCriteria = new ProfileOrCritera(STRINGCRITERIA_1, STRINGCRITERIA_2);
        String filter = LdapFilterFactory.getLdapFilter(orCriteria, attributeMap);
        assertEquals(FILTER_ERROR, getOrFilter(getFilter(STRINGMURNAME_1, STRINGVALUE_1), getFilter(STRINGMURNAME_2, STRINGVALUE_2)), filter);

        orCriteria = new ProfileOrCritera(STRINGCRITERIA_1, BOOLCRITERIA_2);
        filter = LdapFilterFactory.getLdapFilter(orCriteria, attributeMap);
        assertEquals(FILTER_ERROR, getOrFilter(getFilter(STRINGMURNAME_1, STRINGVALUE_1),
                getFilter(BOOLMURNAME_2, BOOLVALUE_2)), filter);

        orCriteria = new ProfileOrCritera(INTCRITERIA_1, BOOLCRITERIA_2);
        filter = LdapFilterFactory.getLdapFilter(orCriteria, attributeMap);
        assertEquals(FILTER_ERROR, getOrFilter(getFilter(INTMURNAME_1, INTVALUE_1),
                getFilter(BOOLMURNAME_2, BOOLVALUE_2)), filter);

        // Test multiple or
        Collection<ProfileStringCriteria> attributeCriterias = new ArrayList<ProfileStringCriteria>();
        Collection<String> filters = new ArrayList<String>();
        for (int i = 0; i < 100; i++) {
            String applicationName = "applicationAttribute" + Integer.toString(i);
            String userRegisterName = "userRegisterAttribute" + Integer.toString(i);
            String value = "value" + Integer.toString(i);
            attributeCriterias.add(new ProfileStringCriteria(applicationName, value));
            filters.add(getFilter(userRegisterName, value));
            addAttribute(applicationName, userRegisterName, BOOLEANTYPE);
        }
        orCriteria = new ProfileOrCritera(attributeCriterias.toArray(new ProfileStringCriteria[] {} ));
        filter = LdapFilterFactory.getLdapFilter(orCriteria, attributeMap);
        assertEquals(FILTER_ERROR, getOrFilter(filters.toArray(new String[] {} )), filter);
    }

    /**
     * Test getting a filter for an and criteria
     * @throws Exception
     */
    public void testGetLdapAndFilter() throws Exception {
        // Test simple and
        ProfileAndCritera andCriteria = new ProfileAndCritera(STRINGCRITERIA_1, STRINGCRITERIA_2);
        String filter = LdapFilterFactory.getLdapFilter(andCriteria, attributeMap);
        assertEquals(FILTER_ERROR, getAndFilter(getFilter(STRINGMURNAME_1, STRINGVALUE_1), getFilter(STRINGMURNAME_2, STRINGVALUE_2)), filter);

        // Test multiple and
        Collection<ProfileStringCriteria> attributeCriterias = new ArrayList<ProfileStringCriteria>();
        Collection<String> filters = new ArrayList<String>();
        for (int i = 0; i < 100; i++) {
            String applicationName = "applicationAttribute" + Integer.toString(i);
            String userRegisterName = "userRegisterAttribute" + Integer.toString(i);
            String value = "value" + Integer.toString(i);
            attributeCriterias.add(new ProfileStringCriteria(applicationName, value));
            filters.add(getFilter(userRegisterName, value));
            addAttribute(applicationName, userRegisterName, STRINGTYPE);
        }
        andCriteria = new ProfileAndCritera(attributeCriterias.toArray(new ProfileStringCriteria[] {} ));
        filter = LdapFilterFactory.getLdapFilter(andCriteria, attributeMap);
        assertEquals(FILTER_ERROR, getAndFilter(filters.toArray(new String[] {} )), filter);
    }

    /**
     * Test getting a filter for a not criteria
     * @throws Exception
     */
    public void testGetLdapNotFilter() throws Exception {
        ProfileNotCritera notCriteria = new ProfileNotCritera(STRINGCRITERIA_1);
        String filter = LdapFilterFactory.getLdapFilter(notCriteria, attributeMap);
        assertEquals(FILTER_ERROR, getNotFilter(getFilter(STRINGMURNAME_1, STRINGVALUE_1)), filter);
    }

    /**
     * Test getting a filter for a more complicated criteria
     * @throws Exception
     */
    public void testGetLdapFilter() throws Exception {
        // Test a more complicated tree
        ProfileAndCritera criteria = new ProfileAndCritera(
                new ProfileAndCritera(
                        STRINGCRITERIA_1,
                        new ProfileOrCritera(BOOLCRITERIA_2, INTCRITERIA_1)),
                new ProfileOrCritera(
                        STRINGCRITERIA_1,
                        new ProfileNotCritera(INTCRITERIA_2))
                );
        String filter = LdapFilterFactory.getLdapFilter(criteria, attributeMap);
        String EXPECTED = "(&(&" + "(" + STRINGMURNAME_1 + "=" + STRINGVALUE_1 + ")" + "(|" +
                "(" + BOOLMURNAME_2 + "=" + Boolean.toString(BOOLVALUE_2) + ")" +
                "(" + INTMURNAME_1 + "=" + Integer.toString(INTVALUE_1) + ")" + ")" +
                ")" + "(|" + "(" + STRINGMURNAME_1 + "=" + STRINGVALUE_1 + ")" +
                "(!" + "(" + INTMURNAME_2 + "=" + Integer.toString(INTVALUE_2) + ")" + ")))";
        assertEquals(FILTER_ERROR, EXPECTED, filter);
    }

    /**
     * Helper method for creating an LDAP filter string from an attribute name and string value
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @return an LDAP filter string for the attribute
     */
    private String getFilter(String name, String value) {
        return "(" + name + "=" + value + ")";
    }

    /**
     * Helper method for creating an LDAP filter string from an attribute name and boolean value
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @return an LDAP filter string for the attribute
     */
    private String getFilter(String name, boolean value) {
        return "(" + name + "=" + Boolean.toString(value) + ")";
    }

    /**
     * Helper method for creating an LDAP filter string from an attribute name and integer value
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @return an LDAP filter string for the attribute
     */
    private String getFilter(String name, int value) {
        return "(" + name + "=" + Integer.toString(value) + ")";
    }

    /**
     * Helper method for creating an LDAP OR filter string from other filters
     * @param filters the filters that should be OR:ed
     * @return an LDAP filter string for the OR expression
     */
    private String getOrFilter(String... filters) {
        StringBuffer b = new StringBuffer();
        b.append("(|");
        appendFilters(b, filters);
        b.append(")");
        return b.toString();
    }

    /**
     * Helper method for creating an LDAP AND filter string from other filters
     * @param filters the filters that should be AND:ed
     * @return an LDAP filter string for the AND expression
     */
    private String getAndFilter(String... filters) {
        StringBuffer b = new StringBuffer();
        b.append("(&");
        appendFilters(b, filters);
        b.append(")");
        return b.toString();
    }

    /**
     * Helper method for creating an LDAP NOT filter string from other filters
     * @param filter the filter that should be negated
     * @return an LDAP filter string for the NOT expression
     */
    private String getNotFilter(String filter) {
        return "(!" + filter + ")";
    }

    /**
     * Helper method for appending filter strings to each other
     * @param stringBuffer the StringBuffer to append to
     * @param filters the filters that should be appended
     */
    private void appendFilters(StringBuffer stringBuffer, String... filters) {
        for (String filter : filters) {
            stringBuffer.append(filter);
        }
    }

    public static Test suite() {
        return new TestSuite(LdapFilterFactoryTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
