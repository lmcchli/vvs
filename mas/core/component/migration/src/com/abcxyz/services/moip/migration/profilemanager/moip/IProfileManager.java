/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.abcxyz.services.moip.migration.profilemanager.moip.search.ProfileCriteriaVisitor;
import com.abcxyz.services.moip.migration.profilemanager.moip.subscription.Subscription;
import com.mobeon.common.util.criteria.Criteria;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.UnknownAttributeException;
import com.mobeon.masp.profilemanager.UserProvisioningException;

import java.util.concurrent.Future;

/**
 * Interface used to retrieve subscriber profiles. Profiles can be requested synchronously and asynchronously.
 */
public interface IProfileManager {

    /**
     * Retrieves a subscriber profile
     *
     * @param dn Distinguished name for the subscriber profile
     * @return the subscriber profiles with the passed dn, or null if not found
     * @throws HostException when problems occur with the directory server
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding user register attribute name
     */
    public IProfile getProfile(String dn)
            throws UnknownAttributeException, HostException;

    /**
     * Retrieves subscriber profiles
     *
     * @param criteria search criteria used when searching for subscribers
     * @return the subscriber profiles matching filter
     * @throws HostException             when problems occur with the directory server
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding user register attribute name
     */
    public IProfile[] getProfile(Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException;

    /**
     * Retrieves subscriber profiles
     *
     * @param base     the base from which to search
     * @param criteria search criteria used when searching for subscribers
     * @return the subscriber profiles matching filter
     * @throws HostException             when problems occur with the directory server
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding user register attribute name
     */
    public IProfile[] getProfile(String base, Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException;

    /**
     * Retrieves subscriber profiles
     *
     * @param criteria search criteria used when searching for subscribers
     * @param limit    if search could be limited to user directories in a local subdomain
     * @return the subscriber profiles matching filter
     * @throws HostException             when problems occur with the directory server
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding user register attribute name
     */
    public IProfile[] getProfile(Criteria<ProfileCriteriaVisitor> criteria, boolean limit)
            throws UnknownAttributeException, HostException;

    /**
     * Retrieves subscriber profiles
     *
     * @param base     the base from which to search
     * @param criteria search criteria used when searching for subscribers
     * @param limit    if search could be limited to user directories in a local subdomain
     * @return the subscriber profiles matching filter
     * @throws HostException             when problems occur with the directory server
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding user register attribute name
     */
    public IProfile[] getProfile(String base, Criteria<ProfileCriteriaVisitor> criteria, boolean limit)
            throws UnknownAttributeException, HostException;

    /**
     * Retrieves subscriber profiles asynchronously
     *
     * @param criteria search criteria used when searching for subscribers
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the profile are
     *         catched in the Future's ExecutionException.
     * @throws HostException             when problems occur with the directory server
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding user register attribute name
     */
    public Future<IProfile[]> getProfileAsync(Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException;

    /**
     * Retrieves subscriber profiles asynchronously
     *
     * @param base     the base from which to search
     * @param criteria search criteria used when searching for subscribers
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the profile are
     *         catched in the Future's ExecutionException.
     * @throws HostException             when problems occur with the directory server
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding user register attribute name
     */
    public Future<IProfile[]> getProfileAsync(String base, Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException;

    /**
     * Retrieves subscriber profiles asynchronously
     *
     * @param criteria search criteria used when searching for subscribers
     * @param limit    if search could be limited to user directories in a local subdomain
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the profile are
     *         catched in the Future's ExecutionException.
     * @throws HostException             when problems occur with the directory server
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding user register attribute name
     */
    public Future<IProfile[]> getProfileAsync(Criteria<ProfileCriteriaVisitor> criteria, boolean limit)
            throws UnknownAttributeException, HostException;

    /**
     * Retrieves subscriber profiles asynchronously
     *
     * @param base     the base from which to search
     * @param criteria search criteria used when searching for subscribers
     * @param limit    if search could be limited to user directories in a local subdomain
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the profile are
     *         catched in the Future's ExecutionException.
     * @throws HostException             when problems occur with the directory server
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding user register attribute name
     */
    public Future<IProfile[]> getProfileAsync(String base, Criteria<ProfileCriteriaVisitor> criteria, boolean limit)
            throws UnknownAttributeException, HostException;

    /**
     * Retrieves a class of service
     *
     * @param dn the distinguished name of the class of service
     * @return a class of service
     * @throws HostException             when problems occur with the directory server
     * @throws UserProvisioningException when search fails due to provision errors
     */
    public ICos getCos(String dn) throws ProfileManagerException;

    /**
     * Retrieves a class of service 
     * Special 2-arg version that considers user compound service referemce(s)
     *
     * @param dn the distinguished name of the class of service
     * @param userCompoundServiceDn the distinguished name of the users compound services (with overrides or complements)
     * @return a class of service
     * @throws HostException             when problems occur with the directory server
     * @throws UserProvisioningException when search fails due to provision errors
     */
    public ICos getCos(String dn, String [] userCompoundServiceDn) throws ProfileManagerException;

    /**
     * Retrieves a "class of service" part identified by a compoundServiceId.
     *
     * @param compoundServiceId id of a service corresponding to emCompoundServiceId to look for
     * @return a "class of service" containing only the matching compoundServiceId
     * @throws ProfileManagerException when search fails due to provision errors
     */
    public ICos getCos(int compoundServiceId) throws ProfileManagerException;
    /**
     * Retrieves a community
     *
     * @param dn the distinguished name of the community
     * @return a community
     * @throws HostException             when problems occur with the directory server
     * @throws UserProvisioningException when search fails due to provision errors
     */
    public ICommunity getCommunity(String dn) throws ProfileManagerException;

    /**
     * Creates a subscriber with the specified characteristics.
     *
     * @param subscription data used for creating the subscriber
     * @param adminUid     the provisioning administrator to use
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding provisioning attribute name
     * @throws UserProvisioningException if the administrator does not exist or if the create request fails
     * @throws HostException             if administrator data cannot be retrieved due to host problems
     */
    public void createSubscription(Subscription subscription, String adminUid) throws ProfileManagerException;

    /**
     * Creates a subscriber with the specified characteristics
     *
     * @param subscription data used for creating the subscriber
     * @param adminUid     the provisioning administrator to use
     * @param cosName      the name of the COS the subscriber should belong to
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding provisioning attribute name
     * @throws UserProvisioningException if the administrator does not exist or if the create request fails
     * @throws HostException             if administrator data cannot be retrieved due to host problems
     */
    public void createSubscription(Subscription subscription, String adminUid, String cosName)
            throws ProfileManagerException;

    /**
     * Deletes a subscriber with the specified characteristics.
     *
     * @param subscription data used for deleting the subscriber, only the telephonenumber attribute is used
     * @param adminUid     the provisioning administrator to use
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding provisioning attribute name
     * @throws UserProvisioningException if the administrator does not exist or if the create request fails
     * @throws HostException             if administrator data cannot be retrieved due to host problems
     */
    public void deleteSubscription(Subscription subscription, String adminUid) throws ProfileManagerException;
}
