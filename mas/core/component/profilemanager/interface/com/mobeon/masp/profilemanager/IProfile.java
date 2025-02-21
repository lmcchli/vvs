/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import com.abcxyz.messaging.common.mcd.Modification;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.greetings.GreetingNotFoundException;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Future;

/**
 * Interface used to manage a subscriber profile.
 */
public interface IProfile {


    /**
     * Retrieves a string attribute's value. If the attribute is multivalue, the first entry is returned.
     * @param attribute the attribute to retrieve
     * @return the value of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    String getStringAttribute(String attribute) throws UnknownAttributeException;

    /**
     * Retrieves a string attribute's values
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    String[] getStringAttributes(String attribute) throws UnknownAttributeException;

    /**
     * Retrieves an integer attribute's value. If the attribute is multivalue, the first entry is returned.
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    int getIntegerAttribute(String attribute) throws UnknownAttributeException;

    /**
     * Retrieves an integer attribute's values
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    int[] getIntegerAttributes(String attribute) throws UnknownAttributeException;

    /**
     * Retrieves a boolean attribute's value. If the attribute is multivalue, the first entry is returned.
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    boolean getBooleanAttribute(String attribute) throws UnknownAttributeException;

    /**
     * Retrieves a boolean attribute's values
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    boolean[] getBooleanAttributes(String attribute) throws UnknownAttributeException;


    /**
     * Sets a string attribute's value
     * @param attribute the attribute to set
     * @param value the value to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    void setStringAttribute(String attribute, String value) throws ProfileManagerException;

    /**
     * Sets a string attribute's values
     * @param attribute the attribute to set
     * @param values the values to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    void setStringAttributes(String attribute, String[] values) throws ProfileManagerException;

    /**
     * Sets a string attribute's values
     * @param attribute the attribute to set
     * @param values the values to set for the supplied attribute
     * @param op the operation ADD or REPLACE or REMOVE
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    void setStringAttributes(String attribute, String[] values, Modification.Operation op) throws ProfileManagerException;

    /**
     * Sets an integer attribute's value
     * @param attribute the attribute to set
     * @param value the value to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    void setIntegerAttribute(String attribute, int value) throws ProfileManagerException;

    /**
     * Sets an integer attribute's values
     * @param attribute the attribute to set
     * @param value the values to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    void setIntegerAttributes(String attribute, int[] value) throws ProfileManagerException;

    /**
     * Sets a boolean attribute's value
     * @param attribute the attribute to set
     * @param value the value to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    void setBooleanAttribute(String attribute, boolean value) throws ProfileManagerException;

    /**
     * Sets a boolean attribute's values
     * @param attribute the attribute to set
     * @param value the values to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    void setBooleanAttributes(String attribute, boolean[] value) throws ProfileManagerException;


    /**
     * Retrieves a subscriber's greeting
     * @param specification specifies which greeting to retrieve
     * @return the requested greeting
     * @throws GreetingNotFoundException if no greeting was found for supplied specification
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public IMediaObject getGreeting(GreetingSpecification specification)
            throws ProfileManagerException;

    /**
     * Retrieves a subscriber's greeting asynchronously
     * @param specification specifies which greeting to retrieve
     * @return a Future which can be used to poll status and/or retrieve the result from the
     * asynchronous operation. Note that exceptions thrown when retrieving the greeting are
     * catched in the Future's ExecutionException.
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public Future<IMediaObject> getGreetingAsync(GreetingSpecification specification)
            throws UnknownAttributeException;

    /**
     * Sets a subscriber's greeting
     * @param specification specifies which greeting to set
     * @param greeting the greeting which should be set
     * @throws UnknownAttributeException if an invalid filter attribute was supplied
     */
    public void setGreeting(GreetingSpecification specification, IMediaObject greeting)
            throws ProfileManagerException;

    /**
     * Gets a subscriber's spoken name
     * @param format
     * @return the requested spoken name
     * @throws GreetingNotFoundException if no greeting was found for supplied specification
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public IMediaObject getSpokenName(GreetingFormat format)
            throws ProfileManagerException;

    /**
     * Gets a subscriber's spoken name asynchronously
     * @param format
     * @return a Future which can be used to poll status and/or retrieve the result from the
     * asynchronous operation. Note that exceptions thrown when retrieving the spoken name are
     * catched in the Future's ExecutionException.
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public Future<IMediaObject> getSpokenNameAsync(GreetingFormat format)
            throws UnknownAttributeException;

    /**
     * Sets a subscriber's greeting
     * @param format
     * @param spokenName the spoken name which should be set
     * @param duration duration of the spoken name
     * @throws UnknownAttributeException if an invalid filter attribute was supplied
     */
    public void setSpokenName(GreetingFormat format, IMediaObject spokenName, String duration)
            throws ProfileManagerException;

    /**
     * Retrieves all the distribution lists that a subscriber has
     * @return all distribution lists for the subscriber
     */
    public IMultimediaDistributionList[] getDistributionLists() throws ProfileManagerException;

    /**
     * Retrieves a specific list for the subscriber
     * @return
     * @throws ProfileManagerException
     */
    public IMultimediaDistributionList getDistributionList(String ID) throws ProfileManagerException;

    /**
     * Creates a new distribution list for a subscriber
     * @param ID the ID of the new distribution list
     * @return the new distribution list
     */
    public void createDistributionList(String ID) throws ProfileManagerException;

    /**
     * Retreive MSID of a distribution list
     * @param ID the ID of the new distribution list
     */
    public String getDistributionListsMsid(String aListNumber)throws ProfileManagerException;

    public IMediaObject getSpokenNameForDistributionList(String ID) throws ProfileManagerException;

    public void setSpokenNameForDistributionList(String ID, IMediaObject aSpokenName) throws ProfileManagerException;

    /**
     * Deletes a distribution list for a subscriber
     * @param distributionList the distribution list to delete for the subscriber
     */
    public void deleteDistributionList(String ID) throws ProfileManagerException;

    /**
     * Add an entry to the specified distribution list
     * @param distListNumber
     * @param distListMember
     * @throws ProfileManagerException
     */
    public void addMemberToDistributionList(String distListNumber, String distListMember) throws ProfileManagerException;

    /**
     * Remove an entry from the specified distribution list
     * @param distListNumber
     * @param distListMember
     * @throws ProfileManagerException
     */
    public void deleteMemberFromDistributionList(String distListNumber, String distListMember) throws ProfileManagerException;

    /**
     *
     * @return  a mailbox
     */
    public IMailbox getMailbox() throws HostException;

    public IMailbox getMailbox(String mailHost, String accountID);

    /**
     * Retrieves a mailbox
     * @param mailHost the mail host to use
     * @param accountID the mail account to login to
     * @param accountPassword the password to use
     * @return a mailbox
     */
    public IMailbox getMailbox(String mailHost, String accountID, String accountPassword)
            throws MailboxException;

	public String[] getIdentities(String scheme) throws ProfileManagerException;

	public String getIdentity(String scheme) throws ProfileManagerException;


 }
