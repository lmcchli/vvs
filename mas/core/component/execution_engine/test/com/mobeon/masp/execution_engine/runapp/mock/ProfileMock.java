package com.mobeon.masp.execution_engine.runapp.mock;

import com.abcxyz.messaging.common.mcd.Modification.Operation;
import com.mobeon.masp.profilemanager.*;
import com.mobeon.masp.profilemanager.greetings.GreetingNotFoundException;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.execution_engine.platformaccess.DistributionListNotFoundException;

import java.io.File;
import java.util.concurrent.Future;

/**
 * The profile mock.
 */
public class ProfileMock extends BaseMock implements IProfile {

    public ProfileMock ()
    {
        super ();
        log.info ("MOCK: ProfileMock.ProfileMock");
    }

    public String getPrivateFolder() throws HostException { return null; };
    /**
     * Retrieves subscriber string attribute. If the attribute is multivalue, the first entry is returned.
     * @param attribute the attribute to retrieve
     * @return the value of the retrieved attribute
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException if an invalid attribute was supplied
     */
    public String getStringAttribute(String attribute) throws UnknownAttributeException
    {
        log.info ("MOCK: ProfileMock.getStringAttribute");
        log.info ("MOCK: ProfileMock.getStringAttribute attribute "+attribute);
        if (attribute.equals ("unknown"))
            throw new UnknownAttributeException ("Unknown attribute");
        return attribute;
    }

    /**
     * Retrieves subscriber string attributes
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public String[] getStringAttributes(String attribute) throws UnknownAttributeException
    {
        log.info ("MOCK: ProfileMock.getStringAttribute");
        log.info ("MOCK: ProfileMock.getStringAttribute attribute "+attribute);
        if (attribute.equals ("unknown"))
            throw new UnknownAttributeException ("Unknown attribute");
        String[] a = {attribute};
        return a;
    }

    /**
     * Sets subscriber string attribute
     * @param attribute the attribute to set
     * @param value the value to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public void setStringAttribute(String attribute, String value) throws ProfileManagerException
    {

    }

    /**
     * Sets subscriber string attribute
     * @param attribute the attribute to set
     * @param values the values to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public void setStringAttributes(String attribute, String[] values) throws ProfileManagerException
    {

    }

    /**
     * Retrieves subscriber integer attribute. If the attribute is multivalue, the first entry is returned.
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public int getIntegerAttribute(String attribute) throws UnknownAttributeException
    {
        return 0;
    }

    /**
     * Retrieves subscriber integer attributes
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public int[] getIntegerAttributes(String attribute) throws UnknownAttributeException
    {
        return null;
    }

    /**
     * Sets subscriber integer attribute
     * @param attribute the attribute to set
     * @param value the value to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public void setIntegerAttribute(String attribute, int value) throws ProfileManagerException
    {

    }

    /**
     * Sets subscriber integer attribute
     * @param attribute the attribute to set
     * @param value the values to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public void setIntegerAttributes(String attribute, int[] value) throws ProfileManagerException
    {

    }

    /**
     * Retrieves subscriber boolean attribute. If the attribute is multivalue, the first entry is returned.
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public boolean getBooleanAttribute(String attribute) throws UnknownAttributeException
    {
        return false;
    }

    /**
     * Retrieves subscriber boolean attributes
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public boolean[] getBooleanAttributes(String attribute) throws UnknownAttributeException
    {
        return null;
    }

    /**
     * Sets subscriber boolean attribute
     * @param attribute the attribute to set
     * @param value the value to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public void setBooleanAttribute(String attribute, boolean value) throws ProfileManagerException
    {

    }

    /**
     * Sets subscriber boolean attribute
     * @param attribute the attribute to set
     * @param value the values to set for the supplied attribute
     * @throws com.mobeon.masp.profilemanager.HostException
     *          If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public void setBooleanAttributes(String attribute, boolean[] value) throws ProfileManagerException
    {

    }

    /**
     * Retrieves a subscriber's greeting
     * @param specification specifies which greeting to retrieve
     * @return the requested greeting
     * @throws com.mobeon.masp.profilemanager.greetings.GreetingNotFoundException if no greeting was found for supplied specification
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public IMediaObject getGreeting(GreetingSpecification specification)
            throws GreetingNotFoundException, UnknownAttributeException
    {
        return null;
    }

    /**
     * Retrieves a subscriber's greeting asynchronously
     * @param specification specifies which greeting to retrieve
     * @return a Future which can be used to poll status and/or retrieve the result from the
     * asynchronous operation. Note that exceptions thrown when retrieving the greeting are
     * catched in the Future's ExecutionException.
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public Future<IMediaObject> getGreetingAsync(GreetingSpecification specification)
            throws UnknownAttributeException
    {
        return null;
    }

    /**
     * Sets a subscriber's greeting
     * @param properties specifies which greeting to set
     * @param greeting the greeting which should be set
     * @throws UnknownAttributeException if an invalid filter attribute was supplied
     */
    public void setGreeting(GreetingSpecification properties, IMediaObject greeting)
            throws UnknownAttributeException
    {
    }

    /**
     * Gets a subscriber's spoken name
     * @param format
     * @return the requested spoken name
     * @throws GreetingNotFoundException if no greeting was found for supplied specification
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public IMediaObject getSpokenName(GreetingFormat format)
            throws GreetingNotFoundException, UnknownAttributeException
    {
        return null;
    }

    /**
     * Gets a subscriber's spoken name asynchronously
     * @param format
     * @return a Future which can be used to poll status and/or retrieve the result from the
     * asynchronous operation. Note that exceptions thrown when retrieving the spoken name are
     * catched in the Future's ExecutionException.
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public Future<IMediaObject> getSpokenNameAsync(GreetingFormat format)
            throws UnknownAttributeException
    {
        return null;
    }

    /**
     * Sets a subscriber's greeting
     * @param format
     * @param spokenName the spoken name which should be set
     * @throws UnknownAttributeException if an invalid filter attribute was supplied
     */
    public void setSpokenName(GreetingFormat format, IMediaObject spokenName, String duration)
            throws UnknownAttributeException
    {
    }

    /**
     * Retrieves all the distribution lists that a subscriber has
     * @return all distribution lists for the subscriber
     */
    public IMultimediaDistributionList[] getDistributionLists()
    {
        return null;
    }
    public String getDistributionListsMsid(String aListNumber)
    {
        return null;
    }

    /**
     * Retrieves a specified distribution list for a subscriber
     * @param ID the identification of the distribution list to retrieve
     * @return the requested distribution list.
     * @throws com.mobeon.masp.execution_engine.platformaccess.DistributionListNotFoundException if the distribution list could not be found
     */
    public IMultimediaDistributionList getDistributionList(String ID)
    {
        return null;
    }

    /**
     * Creates a new distribution list for a subscriber
     * @param ID the ID of the new distribution list
     * @return the new distribution list
     */
    public void createDistributionList(String ID)
    {

    }

    /**
     * Deletes a distribution list for a subscriber
     * @param distributionList the distribution list to delete for the subscriber
     */
    public void deleteDistributionList(String aString)
    {
    }

    /**
     *
     * @return  a mailbox
     */
    public IMailbox getMailbox() throws HostException
    {
        log.info ("MOCK: ProfileMock.getMailbox");
        return new MailboxMock ();
    }
    public IMailbox getMailbox(String mailHost)
    {
        log.info ("MOCK: ProfileMock.getMailbox");
        log.info ("MOCK: ProfileMock.getMailbox mailhost "+mailHost);
        return new MailboxMock ();
    }
    public IMailbox getMailbox(String mailHost, String accountID)
    {
        log.info ("MOCK: ProfileMock.getMailbox");
        log.info ("MOCK: ProfileMock.getMailbox mailhost "+mailHost);
        log.info ("MOCK: ProfileMock.getMailbox accountid "+accountID);
        return new MailboxMock ();
    }

    /**
     * Retrieves a mailbox
     * @param mailHost the mail host to use
     * @param accountID the mail account to login to
     * @param accountPassword the password to use
     * @return a mailbox
     */
    public IMailbox getMailbox(String mailHost, String accountID, String accountPassword)
            throws MailboxException
    {
        log.info ("MOCK: ProfileMock.getMailbox");
        log.info ("MOCK: ProfileMock.getMailbox mailhost "+mailHost);
        log.info ("MOCK: ProfileMock.getMailbox accountid "+accountID);
        log.info ("MOCK: ProfileMock.getMailbox accountpassword "+accountPassword);
        return new MailboxMock ();
    }

    public void close() {

    }

	@Override
	public String[] getIdentities(String scheme) {
		// TODO Auto-generated method stub
		String [] identities = new String[1];
		identities[0] = "tel:12345678";
		return identities;
	}

	/**
	 *
	 */
	public void addMemberToDistributionList(String distListNumber, String distListMember) throws ProfileManagerException {

	}

	/**
	 *
	 */
	public void deleteMemberFromDistributionList(String distListNumber, String distListMember) throws ProfileManagerException {

	}

    public IMediaObject getSpokenNameForDistributionList(String ID) throws ProfileManagerException {
    	return null;
    }

    public void setSpokenNameForDistributionList(String ID, IMediaObject aSpokenName) throws ProfileManagerException {

    }

	@Override
	public void setStringAttributes(String attribute, String[] values,
			Operation op) throws ProfileManagerException {
		// TODO Auto-generated method stub

	}
	@Override
	public String getIdentity(String scheme) throws ProfileManagerException{
		return null;
	}
}
