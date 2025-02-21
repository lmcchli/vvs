/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.UnknownAttributeException;
import com.mobeon.masp.profilemanager.UserProvisioningException;
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;

import java.util.Map;
import java.util.Set;

import java.util.concurrent.Future;

/**
 * Interface used to manage a subscriber profile.
 */
public interface IProfile extends SubscriberSetting{

    /**
     * Returns the distinguished name for the entry representing the
     * ProfileAttributes
     *
     * @return The distinguished name for the user profile.
     */
    public String getDistinguishedName() throws UserProvisioningException;

    /**
     * Retrieves a subscriber's community
     *
     * @return a community
     */
    public ICommunity getCommunity() throws ProfileManagerException;

    /**
     * Retrieves a subscriber's class of service
     *
     * @return a class of service
     */
    public ICos getCos() throws ProfileManagerException;

    /**
     * Retrieves a subscriber's greeting
     *
     * @param specification specifies which greeting to retrieve
     * @return the requested greeting
     * @throws com.mobeon.masp.profilemanager.greetings.GreetingNotFoundException
     *                                   if no greeting was found for supplied specification
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public IMediaObject getGreeting(GreetingSpecification specification)
            throws ProfileManagerException;

    /**
     * Retrieves a subscriber's greeting Message Id
     * @param specification specifies which greeting to retrieve
     * @return the requested greeting Message Id
     * @throws GreetingNotFoundException if no greeting was found for supplied specification
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public String getGreetingMessageId(GreetingSpecification specification)
            throws ProfileManagerException;

    /**
     * Retrieves a subscriber's greeting asynchronously
     *
     * @param specification specifies which greeting to retrieve
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the greeting are
     *         catched in the Future's ExecutionException.
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public Future<IMediaObject> getGreetingAsync(GreetingSpecification specification)
            throws UnknownAttributeException;

    /**
     * Sets a subscriber's greeting
     *
     * @param specification specifies which greeting to set
     * @param greeting      the greeting which should be set
     * @throws UnknownAttributeException if an invalid filter attribute was supplied
     */
    public void setGreeting(GreetingSpecification specification, IMediaObject greeting)
            throws ProfileManagerException;

    /**
     * Gets a subscriber's spoken name
     *
     * @param format
     * @return the requested spoken name
     * @throws com.mobeon.masp.profilemanager.greetings.GreetingNotFoundException
     *                                   if no greeting was found for supplied specification
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public IMediaObject getSpokenName(GreetingFormat format)
            throws ProfileManagerException;

    /**
     * Gets a subscriber's spoken name asynchronously
     *
     * @param format
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the spoken name are
     *         catched in the Future's ExecutionException.
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public Future<IMediaObject> getSpokenNameAsync(GreetingFormat format)
            throws UnknownAttributeException;

    /**
     * Sets a subscriber's greeting
     *
     * @param format
     * @param spokenName the spoken name which should be set
     * @throws UnknownAttributeException if an invalid filter attribute was supplied
     */
    public void setSpokenName(GreetingFormat format, IMediaObject spokenName)
            throws ProfileManagerException;

    /**
     * Retrieves all the distribution lists that a subscriber has
     *
     * @return all distribution lists for the subscriber
     */
    public IDistributionList[] getDistributionLists() throws ProfileManagerException;

    /**
     * Creates a new distribution list for a subscriber
     *
     * @param ID the ID of the new distribution list
     * @return the new distribution list
     */
    public IDistributionList createDistributionList(String ID) throws ProfileManagerException;

    /**
     * Deletes a distribution list for a subscriber
     *
     * @param distributionList the distribution list to delete for the subscriber
     */
    public void deleteDistributionList(IDistributionList distributionList) throws ProfileManagerException;

    /**
     * @return a mailbox
     */
    public IMailbox getMailbox() throws HostException, UserProvisioningException;

    public IMailbox getMailbox(String mailHost);

    public IMailbox getMailbox(String mailHost, String accountID);

    /**
     * Retrieves a mailbox
     *
     * @param mailHost        the mail host to use
     * @param accountID       the mail account to login to
     * @param accountPassword the password to use
     * @return a mailbox
     */
    public IMailbox getMailbox(String mailHost, String accountID, String accountPassword)
            throws MailboxException;

    /**
     * Closes the resources connected with this profile
     */
    public void close();
    
    /**
     * Get the aggregated set of attributes interpreted as strings.
     * I.e. the boolean or integer attributes are parsed to "true" "false" 
     * and "12345". It is assumed that this is only used for listing attributes.
     *
     * @return set of requested attributes in string format representing <key, multi/single-value attribute>  
     */ 
    Set<Map.Entry<String, String[]>> getAttributes();

    /**
     * Get the set of attributes interpreted as strings.
     * I.e. the boolean or integer attributes are parsed to "true" "false" 
     * and "12345". It is assumed that this is only used for listing attributes.
     * Valid profile levels: BILLING, USER, COS, COMMUNITY
     * @param profileLevel get attribute set for this level only
     *
     * @return set of requested attributes in string format representing <key, multi/single-value attribute>  
     */ 
    Set<Map.Entry<String, String[]>> getAttributes(ProfileLevel profileLevel);
}
