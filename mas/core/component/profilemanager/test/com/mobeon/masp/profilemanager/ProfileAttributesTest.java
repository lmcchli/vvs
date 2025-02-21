package com.mobeon.masp.profilemanager;

import junit.framework.Test;
import junit.framework.TestSuite;

import javax.naming.directory.SearchResult;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.Attribute;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.jmock.Mock;
import org.jmock.core.InvocationMatcher;
import org.jmock.builder.NameMatchBuilder;

/**
 * ProfileAttributes Tester.
 *
 * @author <Authors name>
 * @since <pre>03/23/2006</pre>
 * @version 1.0
 */
public class ProfileAttributesTest extends ProfileManagerMockObjectBaseTestCase {
    private static final String BILLING_NUMBER = "billingnumber";
    private static final String MAIL = "mail";
    private static final String UNKNOWN_ATTRIBUTE = "unknownattribute";
    private static final String DISTINGUISHED_NAME = "distinguishedName";

    public ProfileAttributesTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        setUpProfileContext(getConfiguration(PROFILEMANAGERCFG));

        // Create a new ProfileManager. This is only to initialize the metadata maps
        ProfileManagerImpl profileManager = new ProfileManagerImpl();
        profileManager.setContext(baseContext);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParseSearchResult() throws Exception {
        Attributes attrs = new BasicAttributes();
        attrs.put(BILLING_NUMBER, BILLING_NUMBER);
        attrs.put(MAIL, MAIL);
        attrs.put(UNKNOWN_ATTRIBUTE, UNKNOWN_ATTRIBUTE);
        SearchResult searchResult = new SearchResult(DISTINGUISHED_NAME, null, attrs);
        searchResult.setNameInNamespace(DISTINGUISHED_NAME);
        ProfileAttributes profileAttributes = new ProfileAttributes(baseContext, searchResult);
        assertEquals("Distinguished name should be set", DISTINGUISHED_NAME, profileAttributes.getDistinguishedName());
        assertTrue(BILLING_NUMBER + " should exist", profileAttributes.containsKey(BILLING_NUMBER));
        assertTrue(MAIL + " should exist", profileAttributes.containsKey(MAIL));
        assertFalse(UNKNOWN_ATTRIBUTE + " should not exist", profileAttributes.containsKey(UNKNOWN_ATTRIBUTE));
    }

    public void testParseSearchResultAttributesHasMoreNamingException() throws Exception {
        Mock mockAttributes = getMockAttributesAttributesHasMoreNamingException();
        MockSearchResult searchResult = new MockSearchResult(DISTINGUISHED_NAME);
        searchResult.setNameInNamespace(DISTINGUISHED_NAME);
        searchResult.expects(once()).method("getAttributes").will(returnValue(mockAttributes.proxy()));

        try {
            new ProfileAttributes(baseContext, searchResult);
            fail("Expected NamingException");
        } catch (NamingException e) {
            assertEquals("Expected NamingException thrown by hasMore()", "hasMore", e.getMessage());
        }
    }

    public void testParseSearchResultAttributesNextNamingException() throws Exception {
        Mock mockAttributes = getMockAttributesAttributesNextNamingException();
        MockSearchResult searchResult = new MockSearchResult(DISTINGUISHED_NAME);
        searchResult.setNameInNamespace(DISTINGUISHED_NAME);
        searchResult.expects(once()).method("getAttributes").will(returnValue(mockAttributes.proxy()));

        try {
            new ProfileAttributes(baseContext, searchResult);
            fail("Expected NamingException");
        } catch (NamingException e) {
            assertEquals("Expected NamingException thrown by next()", "next", e.getMessage());
        }
    }

    public void testParseSearchResultAttributeHasMoreNamingException() throws Exception {
        Mock mockAttributes = getMockAttributesAttributeHasMoreNamingException();
        MockSearchResult searchResult = new MockSearchResult(DISTINGUISHED_NAME);
        searchResult.setNameInNamespace(DISTINGUISHED_NAME);
        searchResult.expects(once()).method("getAttributes").will(returnValue(mockAttributes.proxy()));

        try {
            new ProfileAttributes(baseContext, searchResult);
            fail("Expected NamingException");
        } catch (NamingException e) {
            assertEquals("Expected NamingException thrown by hasMore()", "hasMore", e.getMessage());
        }
    }

    public void testParseSearchResultAttributeNextNamingException() throws Exception {
        Mock mockAttributes = getMockAttributesAttributeNextNamingException();
        MockSearchResult searchResult = new MockSearchResult(DISTINGUISHED_NAME);
        searchResult.setNameInNamespace(DISTINGUISHED_NAME);
        searchResult.expects(once()).method("getAttributes").will(returnValue(mockAttributes.proxy()));

        try {
            new ProfileAttributes(baseContext, searchResult);
            fail("Expected NamingException");
        } catch (NamingException e) {
            assertEquals("Expected NamingException thrown by next()", "next", e.getMessage());
        }
    }

    private Mock getMockAttributesAttributesHasMoreNamingException() {
        Mock mockAttributes;
        Mock attributesNamingEnumeration = mock(NamingEnumeration.class);
        mockAttributes = mock(Attributes.class);
        attributesNamingEnumeration.expects(once()).method("hasMore").will(throwException(new NamingException("hasMore")));
        mockAttributes.expects(once()).method("getAll").will(returnValue(attributesNamingEnumeration.proxy()));
        return mockAttributes;
    }

    private Mock getMockAttributesAttributesNextNamingException() {
        Mock mockAttributes;
        Mock attributesNamingEnumeration = mock(NamingEnumeration.class);
        mockAttributes = mock(Attributes.class);
        attributesNamingEnumeration.expects(once()).method("hasMore").will(returnValue(true));
        attributesNamingEnumeration.expects(once()).method("next").will(throwException(new NamingException("next")));
        mockAttributes.expects(once()).method("getAll").will(returnValue(attributesNamingEnumeration.proxy()));
        return mockAttributes;
    }

    private Mock getMockAttributesAttributeHasMoreNamingException() {
        Mock mockAttributes;
        Mock attributesNamingEnumeration = mock(NamingEnumeration.class);
        Mock mockAttribute = mock(Attribute.class);
        Mock attributeNamingEnumeration = mock(NamingEnumeration.class);

        mockAttributes = mock(Attributes.class);
        mockAttributes.expects(once()).method("getAll").will(returnValue(attributesNamingEnumeration.proxy()));
        attributesNamingEnumeration.expects(once()).method("hasMore").will(returnValue(true));
        attributesNamingEnumeration.expects(once()).method("next").will(returnValue(mockAttribute.proxy()));
        mockAttribute.expects(once()).method("getID").will(returnValue("mail")); // Use a "known" name
        mockAttribute.expects(once()).method("getAll").will(returnValue(attributeNamingEnumeration.proxy()));
        attributeNamingEnumeration.expects(once()).method("hasMore").will(throwException(new NamingException("hasMore")));
        return mockAttributes;
    }

    private Mock getMockAttributesAttributeNextNamingException() {
        Mock mockAttributes;
        Mock attributesNamingEnumeration = mock(NamingEnumeration.class);
        Mock mockAttribute = mock(Attribute.class);
        Mock attributeNamingEnumeration = mock(NamingEnumeration.class);

        mockAttributes = mock(Attributes.class);
        mockAttributes.expects(once()).method("getAll").will(returnValue(attributesNamingEnumeration.proxy()));
        attributesNamingEnumeration.expects(once()).method("hasMore").will(returnValue(true));
        attributesNamingEnumeration.expects(once()).method("next").will(returnValue(mockAttribute.proxy()));
        mockAttribute.expects(once()).method("getID").will(returnValue("mail")); // Use a "known" name
        mockAttribute.expects(once()).method("getAll").will(returnValue(attributeNamingEnumeration.proxy()));
        attributeNamingEnumeration.expects(once()).method("hasMore").will(returnValue(true));
        attributeNamingEnumeration.expects(once()).method("next").will(throwException(new NamingException("next")));
        return mockAttributes;
    }

    private interface ISearchResult {
        public Attributes getAttributes();
    }

    private class MockSearchResult extends SearchResult {
        Mock mockSearchResult;
        ISearchResult searchResult;

        public MockSearchResult(String name) {
            super(name, null, new BasicAttributes());
            mockSearchResult = mock(ISearchResult.class);
            searchResult = (ISearchResult)mockSearchResult.proxy();
        }

        public NameMatchBuilder expects(InvocationMatcher expectation) {
            return mockSearchResult.expects(expectation);
        }

        public Attributes getAttributes() {
            return searchResult.getAttributes();
        }
    }

    public static Test suite() {
        return new TestSuite(ProfileAttributesTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
