/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.profilemanager;

import junit.framework.Test;
import junit.framework.TestSuite;

import javax.naming.directory.*;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.CommunicationException;

import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;
import com.mobeon.masp.profilemanager.greetings.SpokenNameSpecification;
import com.mobeon.masp.profilemanager.greetings.GreetingType;
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.pool.DirContextPoolManager;
import com.mobeon.masp.mediaobject.IMediaObject;
import org.jmock.Mock;

/**
 * DistributionListImpl Tester.
 *
 * @author MANDE
 * @since <pre>06/08/2006</pre>
 * @version 1.0
 */
public class DistributionListImplTest extends DistributionListMockObjectBaseTestCase {

    protected DistributionListImpl distributionList;
    private static final String MURHOST = "murhost";
    private static final String MURPORT = "389";
    private static final String PROVIDER_URL = "ldap://" + MURHOST + ":" + MURPORT;
    protected Mock mockSubscriber;
    private static final int TRIES = 3;

    public DistributionListImplTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        setUpProfileContext(getConfiguration(PROFILEMANAGERCFG));
        setUpDirContext();

        // Make sure that each test gets a new pool
        DirContextPoolManager.getInstance().removeDirContextPool(PROVIDER_URL);

        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("createSubcontext").with(eq(DIST_LIST_DN), eq(getCreateSubcontextAttributesComponent(DIST_LIST_ID)));
        setUpMockSubscriber();
        distributionList = new DistributionListImpl(baseContext, DIST_LIST_ID, getMockSubscriber(), BASE_DN);
    }

    private void setUpMockSubscriber() {
        mockSubscriber = mock(IProfile.class);
        mockSubscriber.expects(once()).method("getStringAttribute").with(eq("mail")).will(returnValue(MAIL));
        mockSubscriber.expects(once()).method("getStringAttribute").with(eq("mailhost")).will(returnValue(MAIL_HOST));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateCommunicationException() throws Exception {
        // Increase timeouts, since it is try limit that is tested
        baseContext.getConfig().setTryTimeLimit(1000);
        baseContext.getConfig().setWriteTimeout(1000);
        NamingException exception = new CommunicationException("communicationexception");
        for (int i = 0; i < TRIES; i++) {
            IServiceInstance serviceInstance = getMockUserRegisterInstance();
            mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(serviceInstance));
            mockServiceLocator.expects(once()).method("reportServiceError").with(eq(serviceInstance));
            mockDirContext.expects(once()).method("createSubcontext").with(eq(DIST_LIST_DN), eq(getCreateSubcontextAttributesComponent(DIST_LIST_ID))).
                    will(throwException(exception));
            mockDirContext.expects(once()).method("close");
        }
        setUpMockSubscriber();
        try {
            new DistributionListImpl(baseContext, DIST_LIST_ID, getMockSubscriber(), BASE_DN);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertSame(exception, e.getCause());
        }
    }

    public void testCreateUnexpectedException() throws Exception {
        Exception exception = new NullPointerException();
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("createSubcontext").with(eq(DIST_LIST_DN), eq(getCreateSubcontextAttributesComponent(DIST_LIST_ID))).
                will(throwException(exception));
        setUpMockSubscriber();
        try {
            new DistributionListImpl(baseContext, DIST_LIST_ID, getMockSubscriber(), BASE_DN);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertSame(exception, e.getCause());
        }
    }

    public void testCreateExisting() throws Exception {
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        NamingException exception = new NameAlreadyBoundException();
        mockDirContext.expects(once()).method("createSubcontext").with(eq(DIST_LIST_DN), eq(getCreateSubcontextAttributesComponent(DIST_LIST_ID))).
                will(throwException(exception));
        setUpMockSubscriber();
        try {
            new DistributionListImpl(baseContext, DIST_LIST_ID, getMockSubscriber(), BASE_DN);
            fail("Expected HostException");
        } catch (HostException e) {
            assertEquals(exception, e.getCause());
        }
    }

    public void testCreateFromSearchResult() throws Exception {
        // Test create with no members
        SearchResult searchResult = getDistributionListSearchResult(DIST_LIST_ID);
        DistributionListImpl distributionList = new DistributionListImpl(baseContext, searchResult, getMockSubscriber());
        assertEquals("Distribution list id should be <" + DIST_LIST_ID + ">", DIST_LIST_ID, distributionList.getID());
        String[] members = distributionList.getMembers();
        assertNotNull("Members should not be null", members);
        assertEquals(0, members.length);

        // Test create with members
        String member1 = "member1";
        String member2 = "member2";
        searchResult = getDistributionListSearchResult(DIST_LIST_ID, member1, member2);
        distributionList = new DistributionListImpl(baseContext, searchResult, getMockSubscriber());
        assertEquals("Distribution list id should be <" + DIST_LIST_ID + ">", DIST_LIST_ID, distributionList.getID());
        members = distributionList.getMembers();
        assertNotNull("Members should not be null", members);
        assertEquals(2, members.length);

        String member3 = "member3";
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER, member1, member2, member3));
        distributionList.addMember(member3);
        members = distributionList.getMembers();
        assertNotNull("Members should not be null", members);
        assertEquals(3, members.length);

        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER, member2, member3));
        distributionList.removeMember(member1);
        members = distributionList.getMembers();
        assertNotNull("Members should not be null", members);
        assertEquals(2, members.length);
    }

    public void testCreateFromSearchResultNamingException() throws Exception {
        SearchResult searchResult = getDistributionListSearchResultNamingException();
        try {
            new DistributionListImpl(baseContext, searchResult, getMockSubscriber());
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetID() throws Exception {
        assertEquals("Distribution list id should be <" + DIST_LIST_ID + ">", DIST_LIST_ID, distributionList.getID());
    }

    public void testGetMembers() throws Exception {
        String[] members = distributionList.getMembers();
        assertNotNull("Members should not be null", members);
        assertEquals(0, members.length);
    }

    public void testAddRemoveMembers() throws Exception {
        String member1 = "member1";
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER, member1));
        distributionList.addMember(member1);
        // Same member is only added once
        distributionList.addMember(member1);
        distributionList.addMember(member1);
        String[] members = distributionList.getMembers();
        assertNotNull("Members should not be null", members);
        assertEquals(1, members.length);

        String member2 = "member2";
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER, member1, member2));
        distributionList.addMember(member2);
        members = distributionList.getMembers();
        assertNotNull("Members should not be null", members);
        assertEquals(2, members.length);

        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER, member2));
        distributionList.removeMember(member1);
        // Nonexisting members are removed silently
        distributionList.removeMember(member1);
        distributionList.removeMember(member1);
        members = distributionList.getMembers();
        assertNotNull("Members should not be null", members);
        assertEquals(1, members.length);

        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER));
        distributionList.removeMember(member2);
        members = distributionList.getMembers();
        assertNotNull("Members should not be null", members);
        assertEquals(0, members.length);
    }

    public void testAddMemberCommunicationException() throws Exception {
        // Increase timeouts, since it is try limit that is tested
        baseContext.getConfig().setTryTimeLimit(1000);
        baseContext.getConfig().setWriteTimeout(1000);
        String member1 = "member1";
        for (int i = 0; i < TRIES; i++) {
            IServiceInstance serviceInstance = getMockUserRegisterInstance();
            mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(serviceInstance));
            mockServiceLocator.expects(once()).method("reportServiceError").with(eq(serviceInstance));
            Exception exception = new CommunicationException("communicationexception");
            mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER, member1)).
                    will(throwException(exception));
            mockDirContext.expects(once()).method("close");
        }
        try {
            distributionList.addMember(member1);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testRemoveMemberCommunicationException() throws Exception {
        // Increase timeouts, since it is try limit that is tested
        baseContext.getConfig().setTryTimeLimit(1000);
        baseContext.getConfig().setWriteTimeout(1000);
        String member1 = "member1";
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER, member1));
        distributionList.addMember(member1);
        String[] members = distributionList.getMembers();
        assertNotNull("Members should not be null", members);
        assertEquals(1, members.length);
        for (int i = 0; i < TRIES; i++) {
            IServiceInstance serviceInstance = getMockUserRegisterInstance();
            mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(serviceInstance));
            mockServiceLocator.expects(once()).method("reportServiceError").with(eq(serviceInstance));
            Exception exception = new CommunicationException("communicationexception");
            mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER)).
                    will(throwException(exception));
            mockDirContext.expects(once()).method("close");
        }
        try {
            distributionList.removeMember(member1);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetSpokenName() throws Exception {
        GreetingSpecification specification = new SpokenNameSpecification(
                GreetingType.DIST_LIST_SPOKEN_NAME,
                GreetingFormat.VOICE,
                "1"
        );
        Mock mockMediaObject = mock(IMediaObject.class);
        mockSubscriber.expects(once()).method("getGreeting").with(eq(specification)).will(returnValue(mockMediaObject.proxy()));
        assertEquals("MediaObject should be equal", mockMediaObject.proxy(), distributionList.getSpokenName());
    }

    public void testGetSpokenNameProfileManagerException() throws Exception {
        GreetingSpecification specification = new SpokenNameSpecification(
                GreetingType.DIST_LIST_SPOKEN_NAME,
                GreetingFormat.VOICE,
                "1"
        );
        mockSubscriber.expects(once()).method("getGreeting").with(eq(specification)).
                will(throwException(new ProfileManagerException("profilemanagerexception")));
        try {
            distributionList.getSpokenName();
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testSetSpokenName() throws Exception {
        GreetingSpecification specification = new SpokenNameSpecification(
                GreetingType.DIST_LIST_SPOKEN_NAME,
                GreetingFormat.VOICE,
                "1"
        );
        Mock mockMediaObject = mock(IMediaObject.class);
        mockSubscriber.expects(once()).method("setGreeting").with(eq(specification), same(mockMediaObject.proxy()));
        distributionList.setSpokenName((IMediaObject)mockMediaObject.proxy());
    }

    public void testSetSpokenNameProfileManagerException() throws Exception {
        GreetingSpecification specification = new SpokenNameSpecification(
                GreetingType.DIST_LIST_SPOKEN_NAME,
                GreetingFormat.VOICE,
                "1"
        );
        Mock mockMediaObject = mock(IMediaObject.class);
        mockSubscriber.expects(once()).method("setGreeting").with(eq(specification), same(mockMediaObject.proxy())).
                will(throwException(new ProfileManagerException("profilemanagerexception")));
        try {
            distributionList.setSpokenName((IMediaObject)mockMediaObject.proxy());
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testToString() throws Exception {
        // Test empty list
        assertEquals("DistributionList(1)", distributionList.toString());
        // Test one member
        String member1 = "member1";
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER, member1));
        distributionList.addMember(member1);
        assertEquals("DistributionList(1)[member1]", distributionList.toString());
        // Test many members
        String member2 = "member2";
        String member3 = "member3";
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER, member1, member2));
        distributionList.addMember(member2);
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).will(returnValue(getMockUserRegisterInstance()));
        mockDirContext.expects(once()).method("modifyAttributes").with(eq(DIST_LIST_DN), isModificationItem(MGRPRFC822MAILMEMBER, member1, member2, member3));
        distributionList.addMember(member3);
        assertEquals("DistributionList(1)[member3, member2, member1]", distributionList.toString());
    }

    private IServiceInstance getMockUserRegisterInstance() {
        Mock mockServiceInstance = mock(IServiceInstance.class);
        mockServiceInstance.expects(once()).method("getProperty").with(eq("hostname")).will(returnValue(MURHOST));
        mockServiceInstance.expects(once()).method("getProperty").with(eq("port")).will(returnValue(MURPORT));
        return (IServiceInstance)mockServiceInstance.proxy();
    }

    private SearchResult getDistributionListSearchResultNamingException() {
        Attributes attrs = getCreateSubcontextAttributesComponent(DIST_LIST_ID);
        Mock mockAttribute = mock(Attribute.class);
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockAttribute.expects(once()).method("getID").will(returnValue(MGRPRFC822MAILMEMBER));
        mockAttribute.expects(once()).method("getAll").will(returnValue(mockNamingEnumeration.proxy()));
        mockNamingEnumeration.expects(once()).method("hasMore").will(throwException(new NamingException("namingexception")));
        attrs.put((Attribute)mockAttribute.proxy());
        return new SearchResult(DIST_LIST_DN, null, attrs);
    }

    private IProfile getMockSubscriber() {
        return (IProfile)mockSubscriber.proxy();
    }

    public static Test suite() {
        return new TestSuite(DistributionListImplTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
