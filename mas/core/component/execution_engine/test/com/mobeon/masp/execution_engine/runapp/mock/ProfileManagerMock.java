package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.masp.mailbox.IMailboxAccountManager;
import com.mobeon.masp.profilemanager.*;
import com.mobeon.masp.profilemanager.subscription.Subscription;
import com.mobeon.masp.profilemanager.search.ProfileCriteriaVisitor;
import com.mobeon.masp.util.criteria.Criteria;

import java.util.concurrent.Future;

/**
 * The mock object for the profile manager.
 *
 * @author Tomas Stenlund, Mobeon
 */
public class ProfileManagerMock extends BaseMock implements IProfileManager {

    /**
     * The configuratio for this mock object.
     */
    private IConfiguration config;

    /**
     * The service locator for this mock object.
     */
    private ILocateService serviceLocator;

    /**
     * The mailbox account manager for this mock object.
     */
    private IMailboxAccountManager mailboxAccountManager;


    /**
     * The constructor for this mock object.
     */
    public ProfileManagerMock() {
        super ();
        log.info ("MOCK: ProfileManagerMock.ProfileManagerMock");
    }

    /**
     * Sets the configuration for this mock object.
     *
     * @param config
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     */
    public void setConfiguration(IConfiguration config) throws GroupCardinalityException, UnknownGroupException {
        this.config = config;
    }

    /**
     * Sets the service locator for this mock object.
     *
     * @param serviceLocator
     */
    public void setServiceLocator(ILocateService serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    /**
     * Sets the mailbox account manager for this object.
     *
     * @param mailboxAccountManager
     */
    public void setMailboxAccountManager(IMailboxAccountManager mailboxAccountManager) {
        this.mailboxAccountManager = mailboxAccountManager;
    }


	/* (non-Javadoc)
	 * @see com.mobeon.masp.profilemanager.IProfileManager#getProfile(java.lang.String)
	 */
	public IProfile getProfile(String phoneNumber) {
        log.info("MOCK: ProfileManagerMock.getProfile");

        return new ProfileMock();
	}

    /**
     * @param criteria search criteria used when searching for subscribers
     * @return the subscriber profiles matching filter
     * @throws com.mobeon.masp.profilemanager.HostException
     *          if no host is found or host is experiencing problems, e.g. timeout
     */
    public IProfile[] getProfile(Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException {

        log.info("MOCK: ProfileManagerMock.getProfile");
        log.info("MOCK: ProfileManagerMock.getProfile criteria "+criteria.toString());
        return new ProfileMock[] {new ProfileMock ()};
    }

    /**
     * Retrieves subscriber profiles
     *
     * @param base     the base from which to search
     * @param criteria search criteria used when searching for subscribers
     * @return the subscriber profiles matching filter
     * @throws HostException
     *          if no host is found or host is experiencing problems, e.g. timeout
     */
    public IProfile[] getProfile(String base, Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException {
        log.info("MOCK: ProfileManagerMock.getProfile");
        log.info("MOCK: ProfileManagerMock.getProfile base "+base);
        log.info("MOCK: ProfileManagerMock.getProfile criteria "+criteria.toString());
        return new ProfileMock[] {new ProfileMock ()};
    }

    public IProfile[] getProfile(Criteria<ProfileCriteriaVisitor> criteria, boolean limit) throws UnknownAttributeException, HostException {
        log.info("MOCK: ProfileManagerMock.getProfile");
        log.info("MOCK: ProfileManagerMock.getProfile criteria "+criteria.toString());
        log.info("MOCK: ProfileManagerMock.getProfile limit "+Boolean.toString(limit));
        return new ProfileMock[] {new ProfileMock ()};
    }

    public IProfile[] getProfile(String base, Criteria<ProfileCriteriaVisitor> criteria, boolean limit) throws UnknownAttributeException, HostException {
        log.info("MOCK: ProfileManagerMock.getProfile");
        log.info("MOCK: ProfileManagerMock.getProfile base "+base);
        log.info("MOCK: ProfileManagerMock.getProfile criteria "+criteria.toString());
        log.info("MOCK: ProfileManagerMock.getProfile limit "+Boolean.toString(limit));
        return new ProfileMock[] {new ProfileMock ()};
    }

    /**
     * Retrieves subscriber profiles asynchronously
     *
     * @param criteria search criteria used when searching for subscribers
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the profile are
     *         catched in the Future's ExecutionException.
     * @throws HostException
     *          if no host is found or host is experiencing problems, e.g. timeout
     */
    public Future<IProfile[]> getProfileAsync(Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException {
        log.info("MOCK: ProfileManagerMock.getProfileAsync");
        log.info("MOCK: ProfileManagerMock.getProfileAsync unimplemented!");
        return null;
    }

    /**
     * Retrieves subscriber profiles asynchronously
     *
     * @param base     the base from which to search
     * @param criteria search criteria used when searching for subscribers
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the profile are
     *         catched in the Future's ExecutionException.
     * @throws HostException
     *          if no host is found or host is experiencing problems, e.g. timeout
     */
    public Future<IProfile[]> getProfileAsync(String base, Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException {

        log.info("MOCK: ProfileManagerMock.getProfileAsync");
        log.info("MOCK: ProfileManagerMock.getProfileAsync unimplemented!");
        return null;
    }

    public Future<IProfile[]> getProfileAsync(Criteria<ProfileCriteriaVisitor> criteria, boolean limit) throws UnknownAttributeException, HostException {
        log.info("MOCK: ProfileManagerMock.getProfileAsync");
        log.info("MOCK: ProfileManagerMock.getProfileAsync unimplemented!");
        return null;
    }

    public Future<IProfile[]> getProfileAsync(String base, Criteria<ProfileCriteriaVisitor> criteria, boolean limit) throws UnknownAttributeException, HostException {
        log.info("MOCK: ProfileManagerMock.getProfileAsync");
        log.info("MOCK: ProfileManagerMock.getProfileAsync unimplemented!");
        return null;
    }


    public void createSubscription(Subscription subscription, String adminUid) throws ProfileManagerException {
        log.info("MOCK: ProfileManagerMock.createSubscription");
        log.info("MOCK: ProfileManagerMock.createSubscription subscription " + subscription.toString());
        log.info("MOCK: ProfileManagerMock.createSubscription adminUid " + adminUid);
    }

    public void createSubscription(Subscription subscription, String adminUid, String cosName) throws ProfileManagerException {
        log.info("MOCK: ProfileManagerMock.createSubscription");
        log.info("MOCK: ProfileManagerMock.createSubscription subscription " + subscription.toString());
        log.info("MOCK: ProfileManagerMock.createSubscription adminUid " + adminUid);
        log.info("MOCK: ProfileManagerMock.createSubscription cosName " + cosName);
    }

    public void deleteSubscription(Subscription subscription, String adminUid) throws ProfileManagerException {
        log.info("MOCK: ProfileManagerMock.deleteSubscription");
        log.info("MOCK: ProfileManagerMock.deleteSubscription subscription " + subscription.toString());
        log.info("MOCK: ProfileManagerMock.deleteSubscription adminUid " + adminUid);
    }

    @Override
    public boolean isProfileUpdatePossible(String phoneNumber) {
        return true;
    }

    public boolean removeProfileFromCache(String profileClass, String phoneNumber) {
        return true;
    }
    
    public boolean deleteProfile(String muid) {
    	return true;
    }
    
    public boolean autoprovisionProfile(String phoneNumber, String subscriberTemplate) {
    	return true;
    }
    

}
