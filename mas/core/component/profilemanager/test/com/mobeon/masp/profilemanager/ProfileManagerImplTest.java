/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.jmock.core.Stub;
import org.jmock.core.Invocation;
import com.mobeon.masp.profilemanager.search.ProfileStringCriteria;
import com.mobeon.masp.profilemanager.ldap.MockLdapCtxFactory;
import com.mobeon.masp.profilemanager.subscription.Subscription;
import com.mobeon.masp.profilemanager.pool.DirContextPoolManager;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.common.provisionmanager.ProvisioningException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.naming.directory.*;
import javax.naming.NamingEnumeration;
import javax.naming.CommunicationException;
import javax.naming.NamingException;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;


/**
 * Tests getting user profiles
 *
 * @author mande
 */
public class ProfileManagerImplTest extends ProfileManagerMockObjectBaseTestCase {
    private static ILogger log;

    private static final long COSCACHETIMEOUT = 1000;
    private static final String COS_DN = "cos=6,ou=c6,o=mobeon.com";
    private static final String COMMUNITY_DN = "ou=c6,o=mobeon.com";
    private static final String HOSTNAME = "ockelbo.lab.mobeon.com";
    private static final String PORT = "389";
    private static final String PROVIDER_URL = "ldap://" + HOSTNAME + ":" + PORT;
    private static final int READ_TIMEOUT = 2000;
    private static final int TRY_LIMIT = 3;
    private static final String phoneNumber = "5143457900";

    IProfileManager profileManager;
    private Mock mockServiceInstance;
    private static final int DELTA = 30;
    private static final int INTERRUPT_WAIT = 200;

    public ProfileManagerImplTest(String string) {
        super(string);
    }

    protected void setUp() throws Exception {
        super.setUp();
        log = ILoggerFactory.getILogger(ProfileManagerImplTest.class);

        this.profileManager = new ProfileManagerImpl();
        ProfileManagerImpl profileManager = (ProfileManagerImpl)this.profileManager;
        setUpDirContext();

        // Mock service instance
        // Todo: use expect method instead of stubs method
        mockServiceInstance = mock(IServiceInstance.class);
        mockServiceInstance.stubs().method("getServiceName").will(returnValue(IServiceName.MEDIA_ACCESS_SERVER));
        mockServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).
                will(returnValue(HOSTNAME));
        mockServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).
                will(returnValue(PORT));

        setUpProfileContext(getConfiguration(PROFILEMANAGERCFG));
        mockServiceLocator.stubs().method("locateService").
                will(returnValue(mockServiceInstance.proxy()));
        profileManager.setContext(baseContext);
//        profileManager.init();

        // Register MockLdapCtxFactory as Verifiable object
        MockLdapCtxFactory.registerToVerify(this);

        // Make sure that each test gets a new pool
        DirContextPoolManager.getInstance().removeDirContextPool(PROVIDER_URL);
    }

    private void setUpZeroProfilesResult() {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(billingnumber=nonexisting)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE)
        ).will(returnValue(mockNamingEnumeration.proxy()));
    }

    private void setUpEmptySearchResult() {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(true));
        mockNamingEnumeration.expects(once()).method("next").will(returnValue(getEmptySearchResult()));
        mockDirContext.expects(once()).method("search").with(
                eq("searchbase"),
                eq("(billingnumber=19161)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE)
        ).will(returnValue(mockNamingEnumeration.proxy()));
    }

    private void setUpUserProvisioningException() {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(true));
        mockNamingEnumeration.expects(once()).method("next").will(returnValue(getCommunitySearchResult()));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(billingnumber=19161)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE)
        ).will(returnValue(mockNamingEnumeration.proxy()));
    }

    private void setUpOneProfileResult() throws Exception {
        setUpOneProfileResult(false, false);
    }

    private void setUpOneProfileResultEmServiceDn() throws Exception {
        setUpOneProfileResult(true, false);
    }

    private void setUpOneProfileResultLimitScope() throws Exception {
        setUpOneProfileResult(true, true);
    }

    private void setUpOneProfileResult(boolean emservicedn, boolean limitScope) throws Exception {
        Mock mockNamingEnumeration1 = mock(NamingEnumeration.class);
        mockNamingEnumeration1.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false)));
        // rdn-names are returned with both upper and lower case to check that this is handled by ProfM.
        mockNamingEnumeration1.expects(once()).method("next").will(returnValue(getBillingNumberSearchResult("BillingNumber=19161,UniqueIdentifier=um35,ou=c6,o=mobeon.com")));
        Mock mockNamingEnumeration2 = mock(NamingEnumeration.class);
        mockNamingEnumeration2.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false),
                returnValue(false)));
        mockNamingEnumeration2.expects(once()).method("next").will(returnValue(getUniqueIdentifierSearchResult()));
        Mock mockNamingEnumeration3 = mock(NamingEnumeration.class);
        mockNamingEnumeration3.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false),
                returnValue(false)));
        mockNamingEnumeration3.expects(once()).method("next").will(returnValue(getCosSearchResult(emservicedn)));
        Mock mockNamingEnumeration4 = mock(NamingEnumeration.class);
        mockNamingEnumeration4.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false),
                returnValue(false)));
        mockNamingEnumeration4.expects(once()).method("next").will(returnValue(getCommunitySearchResult()));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(billingnumber=19161)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE, limitScope)
        ).will(returnValue(mockNamingEnumeration1.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq("UniqueIdentifier=um35,ou=c6,o=mobeon.com"),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration2.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq(COS_DN),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration3.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq("ou=c6,o=mobeon.com"),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration4.proxy()));
    }

    private void setUpOneProfileResultThrowsException(Exception exception) throws Exception {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false)));
        mockNamingEnumeration.expects(once()).method("next").
                will(returnValue(getSearchResultThrowsException(exception)));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(billingnumber=19161)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE, false)
        ).will(returnValue(mockNamingEnumeration.proxy()));
        if (exception instanceof CommunicationException) {
            mockDirContext.expects(once()).method("close");
        }
    }

    private void setUpOneProfileResultEmptyCosDn() throws Exception {
        Mock mockNamingEnumeration1 = mock(NamingEnumeration.class);
        mockNamingEnumeration1.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false)));
        mockNamingEnumeration1.expects(once()).method("next").will(returnValue(getBillingNumberEmptyCosDnSearchResult()));
        Mock mockNamingEnumeration2 = mock(NamingEnumeration.class);
        mockNamingEnumeration2.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false),
                returnValue(false)));
        mockNamingEnumeration2.expects(once()).method("next").will(returnValue(getUniqueIdentifierSearchResult()));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(billingnumber=19161)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE, false)
        ).will(returnValue(mockNamingEnumeration1.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration2.proxy()));
    }

    private void setUpOneProfileInvalidName(String nameInNamespace) throws Exception {
        Mock mockNamingEnumeration1 = mock(NamingEnumeration.class);
        mockNamingEnumeration1.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false)));
        mockNamingEnumeration1.expects(once()).method("next").
                will(returnValue(getBillingNumberSearchResult(nameInNamespace)));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(billingnumber=19161)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE, false)
        ).will(returnValue(mockNamingEnumeration1.proxy()));
    }

    private void setUpOneProfileResultUniqueIdentifier() throws Exception {
        Mock mockNamingEnumeration1 = mock(NamingEnumeration.class);
        mockNamingEnumeration1.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false)));
        mockNamingEnumeration1.expects(once()).method("next").will(returnValue(getUniqueIdentifierSearchResult()));
        Mock mockNamingEnumeration2 = mock(NamingEnumeration.class);
        mockNamingEnumeration2.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false),
                returnValue(false)));
        mockNamingEnumeration2.expects(once()).method("next").will(returnValue(getBillingNumberSearchResult("billingnumber=19161,uniqueidentifier=um35,ou=c6,o=mobeon.com")));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(mail=mande1@lab.mobeon.com)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE, false)
        ).will(returnValue(mockNamingEnumeration1.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                eq("(billingnumber=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(returnValue(mockNamingEnumeration2.proxy()));
        setUpCosResult();
        setUpCommunityResult();
    }

    private void setUpOneProfileResultUniqueIdentifierBillingResultThrowsException(Exception exception)
            throws Exception
    {
        Mock mockNamingEnumeration1 = mock(NamingEnumeration.class);
        mockNamingEnumeration1.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false)));
        mockNamingEnumeration1.expects(once()).method("next").will(returnValue(getUniqueIdentifierSearchResult()));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(mail=mande1@lab.mobeon.com)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE, false)
        ).will(returnValue(mockNamingEnumeration1.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                eq("(billingnumber=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(throwException(exception));
        if (exception instanceof CommunicationException) {
            mockDirContext.expects(once()).method("close");
        }
    }

    private void setUpOneProfileResultUniqueIdentifierNoBillingResults() throws Exception {
        Mock mockNamingEnumeration1 = mock(NamingEnumeration.class);
        mockNamingEnumeration1.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false)));
        mockNamingEnumeration1.expects(once()).method("next").will(returnValue(getUniqueIdentifierSearchResult()));
        Mock mockNamingEnumeration2 = mock(NamingEnumeration.class);
        mockNamingEnumeration2.expects(once()).method("hasMore").will(returnValue(false));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(mail=mande1@lab.mobeon.com)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE, false)
        ).will(returnValue(mockNamingEnumeration1.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                eq("(billingnumber=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(returnValue(mockNamingEnumeration2.proxy()));
    }

    private void setUpOneProfileResultUniqueIdentifierMultipleBillingResults() throws Exception {
        Mock mockNamingEnumeration1 = mock(NamingEnumeration.class);
        mockNamingEnumeration1.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false)));
        mockNamingEnumeration1.expects(once()).method("next").will(returnValue(getUniqueIdentifierSearchResult()));
        Mock mockNamingEnumeration2 = mock(NamingEnumeration.class);
        mockNamingEnumeration2.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(true),
                returnValue(false)));
        mockNamingEnumeration2.expects(once()).method("next").will(returnValue(getBillingNumberSearchResult("billingnumber=19161,uniqueidentifier=um35,ou=c6,o=mobeon.com")));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(mail=mande1@lab.mobeon.com)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE, false)
        ).will(returnValue(mockNamingEnumeration1.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                eq("(billingnumber=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(returnValue(mockNamingEnumeration2.proxy()));
        setUpCosResult();
        setUpCommunityResult();
    }

    private void setUpThreeProfilesResult() throws Exception {
        Mock mockNamingEnumeration1 = mock(NamingEnumeration.class);
        mockNamingEnumeration1.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(true),
                returnValue(true),
                returnValue(false)));
        mockNamingEnumeration1.expects(atLeastOnce()).method("next").will(returnValue(getBillingNumberSearchResult("billingnumber=19161,uniqueidentifier=um35,ou=c6,o=mobeon.com")));

        Mock mockNamingEnumeration2 = mock(NamingEnumeration.class);
        mockNamingEnumeration2.expects(atLeastOnce()).method("hasMore").will(new ReturnPatternStub(true, false));
        mockNamingEnumeration2.expects(atLeastOnce()).method("next").will(returnValue(getUniqueIdentifierSearchResult()));
        Mock mockNamingEnumeration3 = mock(NamingEnumeration.class);
        mockNamingEnumeration3.expects(atLeastOnce()).method("hasMore").will(new ReturnPatternStub(true, false));
        mockNamingEnumeration3.expects(atLeastOnce()).method("next").will(returnValue(getCosSearchResult(true)));
        Mock mockNamingEnumeration4 = mock(NamingEnumeration.class);
        mockNamingEnumeration4.expects(atLeastOnce()).method("hasMore").will(new ReturnPatternStub(true, false));
        mockNamingEnumeration4.expects(atLeastOnce()).method("next").will(returnValue(getCommunitySearchResult()));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(billingnumber=1916*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE)
        ).will(returnValue(mockNamingEnumeration1.proxy()));
        mockDirContext.expects(atLeastOnce()).method("search").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration2.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq(COS_DN),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration3.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq("ou=c6,o=mobeon.com"),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration4.proxy()));
    }

    private void setUpGreetingAdminResult() throws Exception {
        Mock mockNamingEnumeration1 = mock(NamingEnumeration.class);
        mockNamingEnumeration1.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false)));
        mockNamingEnumeration1.expects(once()).method("next").will(returnValue(getGreetingAdminUniqueIdentiefierSearchResult()));
        Mock mockNamingEnumeration2 = mock(NamingEnumeration.class);
        mockNamingEnumeration2.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false),
                returnValue(false)));
        mockNamingEnumeration2.expects(once()).method("next").will(returnValue(getGreetingAdminBillingNumberSearchResult()));
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(uniqueidentifier=um33)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE, false)
        ).will(returnValue(mockNamingEnumeration1.proxy()));
        mockDirContext.expects(once()).method("search").with(
                eq("uniqueidentifier=um33,ou=c6,o=mobeon.com"),
                eq("(billingnumber=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(returnValue(mockNamingEnumeration2.proxy()));
        setUpCommunityResult();
    }

    private void setUpCommunityResult() {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false),
                returnValue(false)));
        mockNamingEnumeration.expects(once()).method("next").will(returnValue(getCommunitySearchResult()));

        mockDirContext.expects(once()).method("search").with(
                eq("ou=c6,o=mobeon.com"),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration.proxy()));
    }

    private void setUpObjectSearchException(String searchBase, Throwable exception) {
        mockDirContext.expects(once()).method("search").with(
                eq(searchBase),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(throwException(exception));
        if (exception instanceof CommunicationException) {
            mockDirContext.expects(once()).method("close");
        }
    }

    private void setUpObjectSearchException(String searchBase, long sleep, Exception exception) {
        mockDirContext.expects(once()).method("search").with(
                eq(searchBase),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(sleepAndThrowException(sleep, exception));
        if (exception instanceof CommunicationException) {
            mockDirContext.expects(once()).method("close");
        }
    }

    private void setUpSubTreeSearchException(String searchBase, String filter, Throwable exception) {
        mockDirContext.expects(once()).method("search").with(
                eq(searchBase),
                eq(filter),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE)
        ).will(throwException(exception));
        if (exception instanceof CommunicationException) {
            mockDirContext.expects(once()).method("close");
        }
    }

    private void setUpSubTreeSearchException(String searchBase, String filter, long timeout, Exception exception) {
        mockDirContext.expects(once()).method("search").with(
                eq(searchBase),
                eq(filter),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE)
        ).will(sleepAndThrowException(timeout, exception));
        if (exception instanceof CommunicationException) {
            mockDirContext.expects(once()).method("close");
        }
    }

    private void setUpCommunityResultNamingException() {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(atLeastOnce()).method("hasMore").will(returnValue(true));
        mockNamingEnumeration.expects(once()).method("next").will(throwException(new NamingException()));
        mockDirContext.expects(once()).method("search").with(
                eq("ou=c6,o=mobeon.com"),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration.proxy()));
    }

    private void setUpCosResult() {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(atLeastOnce()).method("hasMore").will(onConsecutiveCalls(
                returnValue(true),
                returnValue(false),
                returnValue(false)));
        mockNamingEnumeration.expects(once()).method("next").will(returnValue(getCosSearchResult(true)));
        mockDirContext.expects(once()).method("search").with(
                eq(COS_DN),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration.proxy()));
    }

    private void setUpCosResultNamingException() {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(atLeastOnce()).method("hasMore").will(returnValue(true));
        mockNamingEnumeration.expects(once()).method("next").will(throwException(new NamingException()));
        mockDirContext.expects(once()).method("search").with(
                eq(COS_DN),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(returnValue(mockNamingEnumeration.proxy()));
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test getting a subscriber using invalid attribute
     */
    public void testGetProfileUnkownAttributeException() throws Exception {
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
        setUpZeroProfilesResult();
        // Create a searchcriteria matching no subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "nonexisting");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("No profiles should be returned.", 0, profiles.length);
    }

    public void testGetProfileWithSearchBase() throws Exception {
        // Return empty result
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
        String searchBase = "searchBase";
        mockDirContext.expects(once()).method("search").with(
                eq(searchBase),
                eq("(billingnumber=19161)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE)
        ).will(returnValue(mockNamingEnumeration.proxy()));
        // Create a searchcriteria matching no subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(searchBase, filter, false);
        assertEquals("No profiles should be returned.", 0, profiles.length);
    }

    /**
     * Test getting an existing subscriber with billingnumber search filter
     * @throws Exception
     */
    public void testGetOneProfileBilling() throws Exception {
        setUpOneProfileResultEmServiceDn();
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }
    }

    /**
     * Test getting a subscriber when SearchResult contains no attributes
     * @throws Exception
     */
    public void testGetProfileEmptySearchResult() throws Exception {
        setUpEmptySearchResult();
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile("searchbase", filter);
        assertEquals("One profile should be returned.", 0, profiles.length);
    }

    /**
     * Test getting a subscriber when dircontext creation fails
     * @throws Exception
     */
    public void testGetProfileHostExceptionOneRetry() throws Exception {
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");

        // Failure
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException(),
                getNamingException()
        );
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }

        // Success
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException()
        );
        setUpOneProfileResultEmServiceDn();
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }
    }

    /**
     * Test getting a subscriber when dircontext creation fails
     * @throws Exception
     */
    public void testGetProfileHostExceptionTwoRetries() throws Exception {
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");

        // Failure
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException(),
                getCommunicationException(),
                getNamingException()
        );
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }

        // Success
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException(),
                getCommunicationException()
        );
        setUpOneProfileResultEmServiceDn();
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }
    }

    public void testGetProfileHostException() throws Exception {
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");

        // Failure
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException(),
                getCommunicationException(),
                getCommunicationException()
        );
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }
    }

    public void testGetProfileSearchHostExceptionOneRetry() throws Exception {
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");

        // Failure
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getNamingException());
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }

        // Success
        setUpOneProfileResultEmServiceDn();
        setUpSubTreeSearchException(
                "o=mobeon.com",
                "(billingnumber=19161)",
                getCommunicationException()
        );
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }
    }

    public void testGetProfileSearchHostExceptionTwoRetries() throws Exception {
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");

        // Failure
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getNamingException());
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getCommunicationException());
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }

        // Success
        setUpOneProfileResultEmServiceDn();
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getCommunicationException());
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }
    }

    public void testGetProfileSearchHostExceptionThreeRetries() throws Exception {
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");

        // Failure
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getCommunicationException());
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getCommunicationException());
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }
    }

    public void testGetProfileSearchHostExceptionTimeout() throws Exception {
        // Decrease timeout, so we don't have to wait so long
        int tryTimeLimit = 100;
        baseContext.getConfig().setTryTimeLimit(tryTimeLimit);
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");

        // Failure
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", tryTimeLimit + DELTA, getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be CommunicationException", e.getCause() instanceof CommunicationException);
        }
    }

    /**
     * Test getting a subscriber when search throws exception
     * @throws Exception
     */
    public void testGetProfileNamingException() throws Exception {
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", getNamingException());
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }
    }

    public void testGetProfileSearchResultNamingException() throws Exception {
        setUpOneProfileResultThrowsException(getNamingException());
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }
    }

    public void testGetProfileSearchResultCommunicationException() throws Exception {
        setUpOneProfileResultThrowsException(getCommunicationException());
        setUpOneProfileResultThrowsException(getCommunicationException());
        setUpOneProfileResultThrowsException(getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }
    }

    /**
     * Test getting a subscriber when parsing search result throws exception
     * @throws Exception
     */
    public void testGetProfileUserProvisioningException() throws Exception {
        setUpUserProvisioningException();
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("Zero profile should be returned.", 0, profiles.length);
    }

    public void testGetProfileNullPointerException() throws Exception {
        setUpSubTreeSearchException("o=mobeon.com", "(billingnumber=19161)", new NullPointerException());
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NullPointerException", e.getCause() instanceof NullPointerException);
        }
    }

    public void testGetProfileEmptyCosDn() throws Exception {
        setUpOneProfileResultEmptyCosDn();
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("Zero profile should be returned.", 0, profiles.length);
    }

    /**
     * Test getting an existing subscriber with userlevel search filter
     * @throws Exception
     */
    public void testGetOneProfileUniqueIdentifier() throws Exception {
        setUpOneProfileResultUniqueIdentifier();
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("mail", "mande1@lab.mobeon.com");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }
    }

    public void testGetOneProfileUniqueIdentifierMultipleBillingResults() throws Exception {
        setUpOneProfileResultUniqueIdentifierMultipleBillingResults();
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("mail", "mande1@lab.mobeon.com");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }
    }

    public void testGetOneProfileUniqueIdentifierNoBillingResults() throws Exception {
        setUpOneProfileResultUniqueIdentifierNoBillingResults();
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("mail", "mande1@lab.mobeon.com");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("Zero profiles should be returned.", 0, profiles.length);
    }

    public void testGetOneProfileUniqueIdentifierBillingResultNamingException() throws Exception {
        setUpOneProfileResultUniqueIdentifierBillingResultThrowsException(getNamingException());
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("mail", "mande1@lab.mobeon.com");
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }
    }

    public void testGetOneProfileUniqueIdentifierBillingResultCommunicationException() throws Exception {
        setUpOneProfileResultUniqueIdentifierBillingResultThrowsException(getCommunicationException());
        setUpOneProfileResultUniqueIdentifierBillingResultThrowsException(getCommunicationException());
        setUpOneProfileResultUniqueIdentifierBillingResultThrowsException(getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("mail", "mande1@lab.mobeon.com");
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be CommunicationException", e.getCause() instanceof CommunicationException);
        }
    }

    public void testGetProfileInvalidNameException() throws Exception {
        setUpOneProfileInvalidName("billingnumber=19161,invalidname");
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("Zero profiles should be returned.", 0, profiles.length);
    }

    public void testGetProfileLimitScope() throws Exception {
        setUpOneProfileResultLimitScope();
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(filter, true);
        assertEquals("One profile should be returned.", 1, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }
    }

    public void testSearchOrderNoData() throws Exception {
        setUpOneProfileResult();
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        try {
            profiles[0].getStringAttribute("emservicedn");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetGreetingAdmin() throws Exception {
        setUpGreetingAdminResult();
        ProfileStringCriteria filter = new ProfileStringCriteria("uniqueidentifier", "um33");
        IProfile[] grtAdms = profileManager.getProfile(filter);
        assertEquals("One greeting admin should be returned.", 1, grtAdms.length);
        for (IProfile grtAdm : grtAdms) {
            testGreetingAdmin(grtAdm);
        }
    }

    /**
     * Test getting a profile when the search time out
     * @throws Exception
     */
    public void testGetProfileTimeout() throws Exception {
        // Decrease timeout, so we don't have to wait so long
        int readTimeout = 100;
        baseContext.getConfig().setReadTimeout(readTimeout);
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(billingnumber=19161)"),
                aSearchControl(readTimeout, SearchControls.SUBTREE_SCOPE, false)
        ).will(sleep(2 * readTimeout));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        try {
            profileManager.getProfile(filter);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be TimeoutException", e.getCause() instanceof TimeoutException);
        }
    }

    /**
     * Test getting a profile when the search interrupted
     * @throws Exception
     */
    public void testGetProfileInterrupted() throws Throwable {
        mockDirContext.expects(once()).method("search").with(
                eq("o=mobeon.com"),
                eq("(billingnumber=19161)"),
                aSearchControl(READ_TIMEOUT, SearchControls.SUBTREE_SCOPE, false)
        ).will(sleep(READ_TIMEOUT + DELTA));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        // Create a searchcriteria matching one subscriber
        final ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    log.debug("Calling getProfile");
                    profileManager.getProfile(filter);
                    fail("Expected InterruptedException");
                } catch (HostException e) {
                    assertTrue("Cause should be InterruptedException", e.getCause() instanceof InterruptedException);
                    log.debug("Caught HostException due to InterruptedException");
                } catch (UnknownAttributeException e) {
                    String errmsg = "getProfile threw UnknownAttributeException";
                    log.debug(errmsg);
                    fail(errmsg);
                }
            }
        });
        thread.setUncaughtExceptionHandler(this);
        thread.start();
        // Wait for thread to call search on DirContext
        Thread.sleep(100);
        thread.interrupt();
        thread.join();
        if (exceptionMap.containsKey(thread)) {
            throw exceptionMap.get(thread);
        }
    }

    /**
     * Test getting a mailbox for an existing subscriber
     * @throws Exception
     */
    public void testGetMailbox() throws Exception {
        setUpOneProfileResult();
        // Create a searchcriteria matching one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "19161");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("One profile should be returned.", 1, profiles.length);
        IProfile profile = profiles[0];
        Mock mockMailbox = mock(IMailbox.class);
        mockMailboxAccountManager.expects(once()).method("getMailbox").
                with(eq(mockServiceInstance.proxy()), isMailboxProfile("mande1", "abcd", "mande1@lab.mobeon.com")).
                will(returnValue(mockMailbox.proxy()));
        IMailbox mailbox = profile.getMailbox();
        assertEquals("Mailbox should be equal", mailbox, mockMailbox.proxy());
    }
    
    public void testGetMailboxMcd() throws Exception 
    {
    	//setUpOneProfileResult();
    	IProfile profile = profileManager.getProfile(phoneNumber);
    	assertEquals(McdSubscriber.class, profile.getClass());
    }

    /**
     * Test getting a number of existing subscribers
     * @throws Exception
     */
    public void testGetManyProfiles() throws Exception {
        setUpThreeProfilesResult();

        // Create a searchcriteria matching more than one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "1916*");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("Three profiles should be returned.", 3, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }
    }

    /**
     * Test that community and cos cache is emptied after expired time
     * @throws Exception
     */
    public void testCachesExpired() throws Exception {
        setUpThreeProfilesResult();

        // Create a searchcriteria matching more than one subscriber
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "1916*");
        IProfile[] profiles = profileManager.getProfile(filter);
        assertEquals("Three profiles should be returned.", 3, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }

        synchronized(this) {
            wait(COSCACHETIMEOUT + DELTA);
        }
        setUpThreeProfilesResult();

        // Create a searchcriteria matching more than one subscriber
        filter = new ProfileStringCriteria("billingnumber", "1916*");
        profiles = profileManager.getProfile(filter);
        assertEquals("Three profiles should be returned.", 3, profiles.length);
        for (IProfile profile : profiles) {
            testProfile(profile);
        }
    }




    public void testGetCos() throws Exception {
        setUpCosResult();
        ICos cos = profileManager.getCos(COS_DN);
        testCos(cos);

        // Cos should be cached
        cos = profileManager.getCos(COS_DN);
        testCos(cos);
    }

    public void testGetCosHostExceptionOneRetry() throws Exception {
        // Failure
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException(),
                getNamingException()
        );
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getCos(COS_DN);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }

        // Success
        MockLdapCtxFactory.throwInitialException(new CommunicationException());
        setUpCosResult();
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        ICos cos = profileManager.getCos(COS_DN);
        testCos(cos);
    }

    public void testGetCosHostExceptionTwoRetries() throws Exception {
        // Failure
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException(),
                getCommunicationException(),
                getNamingException()
        );
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getCos(COS_DN);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }

        // Success
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException(),
                getCommunicationException()
        );
        setUpCosResult();
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        ICos cos = profileManager.getCos(COS_DN);
        testCos(cos);
    }

    public void testGetCosHostException() throws Exception {
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException(),
                getCommunicationException(),
                getCommunicationException()
        );
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getCos(COS_DN);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue(
                    "Cause should be CommunicationException",
                    e.getCause() instanceof CommunicationException
            );
        }
    }

    public void testGetCosSearchHostExceptionOneRetry() throws Exception {
        // Failure
        setUpObjectSearchException(COS_DN, getNamingException());
        setUpObjectSearchException(COS_DN, getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getCos(COS_DN);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }

        // Success
        setUpCosResult();
        setUpObjectSearchException(COS_DN, getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        ICos cos = profileManager.getCos(COS_DN);
        testCos(cos);
    }

    public void testGetCosSearchHostExceptionTwoRetries() throws Exception {
        // Failure
        setUpObjectSearchException(COS_DN, getNamingException());
        setUpObjectSearchException(COS_DN, getCommunicationException());
        setUpObjectSearchException(COS_DN, getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getCos(COS_DN);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }

        // Success
        setUpCosResult();
        setUpObjectSearchException(COS_DN, getCommunicationException());
        setUpObjectSearchException(COS_DN, getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        ICos cos = profileManager.getCos(COS_DN);
        testCos(cos);
    }

    public void testGetCosSearchHostExceptionThreeRetries() throws Exception {
        // Failure
        setUpObjectSearchException(COS_DN, getCommunicationException());
        setUpObjectSearchException(COS_DN, getCommunicationException());
        setUpObjectSearchException(COS_DN, getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getCos(COS_DN);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue(
                    "Cause should be CommunicationException",
                    e.getCause() instanceof CommunicationException
            );
        }
    }

    public void testGetCosSearchHostExceptionTimeout() throws Exception {
        // Decrease timeout, so we don't have to wait so long
        int tryTimeLimit = 100;
        baseContext.getConfig().setTryTimeLimit(tryTimeLimit);
        // Failure
        setUpObjectSearchException(COS_DN, tryTimeLimit + DELTA, getCommunicationException());
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        try {
            profileManager.getCos(COS_DN);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue(
                    "Cause should be CommunicationException",
                    e.getCause() instanceof CommunicationException
            );
        }
    }

    public void testGetCosNamingException() throws Exception {
        setUpCosResultNamingException();
        try {
            profileManager.getCos(COS_DN);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue(
                    "Cause should be NamingException",
                    e.getCause() instanceof NamingException
            );
        }
    }

    public void testGetCosNoServiceFoundException() throws Exception {
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER)).
                will(throwException(new NoServiceFoundException("noservicefoundexception")));
        try {
            profileManager.getCos(COS_DN);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(
                    "Cause should be NoServiceFoundException",
                    e.getCause() instanceof NoServiceFoundException
            );
        }
    }

    /**
     * Test getting a cos when the search time out
     * @throws Exception
     */
    public void testGetCosTimeout() throws Exception {
        // Decrease timeout, so we don't have to wait so long
        int readTimeout = 100;
        baseContext.getConfig().setReadTimeout(readTimeout);
        mockDirContext.expects(once()).method("search").with(
                eq(COS_DN),
                eq("(objectclass=*)"),
                aSearchControl(readTimeout, SearchControls.OBJECT_SCOPE)
        ).will(sleep(2 * readTimeout));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        try {
            profileManager.getCos(COS_DN);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be TimeoutException", e.getCause() instanceof TimeoutException);
        }
    }

    /**
     * Test getting a cos when the search interrupted
     * @throws Exception
     */
    public void testGetCosInterrupted() throws Throwable {
        mockDirContext.expects(once()).method("search").with(
                eq(COS_DN),
                eq("(objectclass=*)"),
                aSearchControl(READ_TIMEOUT, SearchControls.OBJECT_SCOPE)
        ).will(sleep(READ_TIMEOUT + DELTA));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    log.debug("Calling getCos");
                    profileManager.getCos(COS_DN);
                    fail("Expected InterruptedException");
                } catch (ProfileManagerException e) {
                    assertTrue("Cause should be InterruptedException", e.getCause() instanceof InterruptedException);
                    log.debug("Caught ProfileManagerException due to InterruptedException");
                }
            }
        });
        thread.setUncaughtExceptionHandler(this);
        thread.start();
        // Wait for thread to call search on DirContext
        Thread.sleep(100);
        thread.interrupt();
        thread.join();
        if (exceptionMap.containsKey(thread)) {
            throw exceptionMap.get(thread);
        }
    }


    public void testGetProfileAsync() throws Exception {
        try {
            profileManager.getProfileAsync(null);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true); // For statistical purposes
        }
        try {
            profileManager.getProfileAsync("searchbase", null);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true); // For statistical purposes
        }
        try {
            profileManager.getProfileAsync(null, false);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true); // For statistical purposes
        }
        try {
            profileManager.getProfileAsync("searchbase", null, false);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true); // For statistical purposes
        }
    }
    /**
     * Test that a received profile seems correct
     * @param profile
     */
    private void testProfile(IProfile profile) throws Exception {
        assertNotNull("Profile should not be null", profile);
        // Test billingnumber entries
        assertEquals(new String[]{"19161"}, profile.getStringAttributes("billingnumber"));
        assertEquals("inhoursstart should be from billing level", "0830", profile.getStringAttribute("inhoursstart"));
        // Test userlevel entries
        assertEquals(new String[]{"mande1@lab.mobeon.com"}, profile.getStringAttributes("mail"));
        assertEquals(new String[]{"emservicednuser"}, profile.getStringAttributes("emservicednuser"));
        // Test cos entries
        assertEquals(new String[]{"normal"}, profile.getStringAttributes("cosname"));
        assertEquals(new String[]{"emservicedncos"}, profile.getStringAttributes("emservicedn"));
        assertEquals("inhoursend should be from cos level", "1700", profile.getStringAttribute("inhoursend"));
        // Test community entries
        assertEquals(new String[]{"lab.mobeon.com"}, profile.getStringAttributes("emallowedmaildomains"));
    }

    private void testGreetingAdmin(IProfile grtAdm) throws Exception {
        assertNotNull("Profile should not be null", grtAdm);
        // Test billingnumber entries
        assertEquals(new String[]{"999933"}, grtAdm.getStringAttributes("billingnumber"));
        // Test userlevel entries
        assertEquals(new String[]{"sn33@ockelbo.lab.mobeon.com"}, grtAdm.getStringAttributes("mail"));
        // Test community entries
        assertEquals(new String[]{"lab.mobeon.com"}, grtAdm.getStringAttributes("emallowedmaildomains"));
    }


    private void testCos(ICos cos) throws UnknownAttributeException {
        assertEquals("normal", cos.getStringAttribute("cosname"));
        assertEquals("emservicedncos", cos.getStringAttribute("emservicedn"));
        assertEquals("0800", cos.getStringAttribute("inhoursstart"));
        assertEquals("1700", cos.getStringAttribute("inhoursend"));
    }

    private SearchResult getEmptySearchResult() {
        Attributes attributes = new BasicAttributes();
        String nameInNamespace = "billingnumber=19161,uniqueidentifier=um35,ou=c6,o=mobeon.com";
        SearchResult result = new SearchResult(nameInNamespace, null, attributes);
        result.setNameInNamespace(nameInNamespace);
        return result;
    }

    private SearchResult getBillingNumberSearchResult(String nameInNamespace) {
        Attributes attributes = new BasicAttributes();
        attributes.put("billingnumber", "19161");
        attributes.put("cosdn", COS_DN);
        attributes.put("inhoursstart", "0830");
        SearchResult result = new SearchResult(nameInNamespace, null, attributes);
        result.setNameInNamespace(nameInNamespace);
        return result;
    }

    private SearchResult getBillingNumberEmptyCosDnSearchResult() {
        Attributes attributes = new BasicAttributes();
        attributes.put("billingnumber", "19161");
        attributes.put("cosdn", null);
        attributes.put("inhoursstart", "0830");
        String nameInNamespace = "billingnumber=19161,uniqueidentifier=um35,ou=c6,o=mobeon.com";
        SearchResult result = new SearchResult(nameInNamespace, null, attributes);
        result.setNameInNamespace(nameInNamespace);
        return result;
    }

    private SearchResult getGreetingAdminBillingNumberSearchResult() {
        Attributes attributes = new BasicAttributes();
        attributes.put("billingnumber", "999933");
        attributes.put("password", "Gr8Pw4GA");
        String nameInNamespace = "billingnumber=999933,uniqueidentifier=um33,ou=c6,o=mobeon.com";
        SearchResult result = new SearchResult(nameInNamespace, null, attributes);
        result.setNameInNamespace(nameInNamespace);
        return result;
    }

    private SearchResult getUniqueIdentifierSearchResult() {
        Attributes attributes = new BasicAttributes();
        attributes.put("mail", "mande1@lab.mobeon.com");
        attributes.put("mailhost", "ockelbo.lab.mobeon.com");
        attributes.put("uid", "mande1");
        attributes.put("password", "3Z191R240A0L472u");
        attributes.put("emservicedn", "emservicednuser");
        String nameInNamespace = "uniqueidentifier=um35,ou=c6,o=mobeon.com";
        SearchResult result = new SearchResult(nameInNamespace, null, attributes);
        result.setNameInNamespace(nameInNamespace);
        return result;
    }

    private SearchResult getGreetingAdminUniqueIdentiefierSearchResult() {
        Attributes attributes = new BasicAttributes();
        attributes.put("mail", "sn33@ockelbo.lab.mobeon.com");
        attributes.put("mailhost", "ockelbo.lab.mobeon.com");
        attributes.put("uid", "GrtAdm_33");
        String nameInNamespace = "uniqueidentifier=um33,ou=c6,o=mobeon.com";
        SearchResult result = new SearchResult(nameInNamespace, null, attributes);
        result.setNameInNamespace(nameInNamespace);
        return result;
    }

    private SearchResult getCosSearchResult(boolean emservicedn) {
        Attributes attributes = new BasicAttributes();
        attributes.put("cosname", "normal");
        if (emservicedn) {
            attributes.put("emservicedn", "emservicedncos");
        }
        attributes.put("inhoursstart", "0800");
        attributes.put("inhoursend", "1700");
        String nameInNamespace = COS_DN;
        SearchResult result = new SearchResult(nameInNamespace, null, attributes);
        result.setNameInNamespace(nameInNamespace);
        return result;
    }

    private SearchResult getCommunitySearchResult() {
        Attributes attributes = new BasicAttributes();
        attributes.put("cn", "AndreasCom");
        attributes.put("emallowedmaildomains", "lab.mobeon.com");
        String nameInNamespace = "ou=c6,o=mobeon.com";
        SearchResult result = new SearchResult(nameInNamespace, null, attributes);
        result.setNameInNamespace(nameInNamespace);
        return result;
    }

    private SearchResult getSearchResultThrowsException(Exception exception) {
        Mock mockAttributes = mock(Attributes.class);
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(once()).method("hasMore").will(throwException(exception));
        mockAttributes.expects(once()).method("size").will(returnValue(1));
        mockAttributes.expects(once()).method("getAll").will(returnValue(mockNamingEnumeration.proxy()));
        String nameInNamespace = "searchresultthrowsexception";
        SearchResult result = new SearchResult(nameInNamespace, null, (Attributes)mockAttributes.proxy());
        result.setNameInNamespace(nameInNamespace);
        return result;
    }

    private NamingEnumeration<SearchResult> getAdminUidNamingEnumeration(String nameInNamespace) {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(true));
        mockNamingEnumeration.expects(once()).method("next").
                will(returnValue(getAdminUidSearchResult(nameInNamespace)));
        return (NamingEnumeration<SearchResult>)mockNamingEnumeration.proxy();
    }

    private SearchResult getAdminUidSearchResult(String nameInNamespace) {
        SearchResult result = new SearchResult("uniqueidentifier=um31,ou=c6", null, getAdminUidAttributes());
        result.setNameInNamespace(nameInNamespace);
        return result;
    }

    private Attributes getAdminUidAttributes() {
        Attributes attrs = new BasicAttributes();
        attrs.put("uid", "andreasadmin");
        return attrs;
    }

    private NamingEnumeration<SearchResult> getCosNamingEnumeration() {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(true));
        mockNamingEnumeration.expects(once()).method("next").will(returnValue(getCosSearchResult(false)));
        return (NamingEnumeration<SearchResult>)mockNamingEnumeration.proxy();
    }

    private NamingEnumeration<SearchResult> getEmptyNamingEnumeration() {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
        return (NamingEnumeration<SearchResult>)mockNamingEnumeration.proxy();
    }

    private Constraint aSubscription(String... attributes) {
        return new SubscriptionConstraint(attributes);
    }

    public class ReturnPatternStub implements Stub {
        private boolean[] args;
        int index = 0;

        public ReturnPatternStub(boolean... args) {
            this.args = args;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            if (index == args.length) {
                index = 0;
            }
            return args[index++];
        }

        public StringBuffer describeTo(StringBuffer stringBuffer) {
            return stringBuffer.append("Returnpattern: ").append(Arrays.toString(args));
        }
    }

    /**
     * Constraint for checking
     */
    public class SubscriptionConstraint implements Constraint {
        private String[] attributes;

        public SubscriptionConstraint(String... attributes) {
            this.attributes = attributes;
        }

        public boolean eval(Object o) {
            if (o instanceof com.mobeon.common.provisionmanager.Subscription) {
                com.mobeon.common.provisionmanager.Subscription subscription = (com.mobeon.common.provisionmanager.Subscription)o;
                Map<String, String[]> attributes = subscription.getAttributes();
                for (int i = 0; i < this.attributes.length; i += 2) {
                    if (attributes.containsKey(this.attributes[i])) {
                        if (!attributes.get(this.attributes[i])[0].equals(this.attributes[i + 1])) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        public StringBuffer describeTo(StringBuffer buffer) {
            return buffer.append("aSubscription(").append(Arrays.toString(attributes)).append(")");
        }
    }

    public static Test suite() {
        return new TestSuite(ProfileManagerImplTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}