package com.abcxyz.services.moip.common.directoryaccess;

import com.abcxyz.messaging.common.mcd.MCDException;
import com.abcxyz.messaging.common.mcd.exceptions.AuthenticationException;
import com.abcxyz.messaging.common.mcd.exceptions.DBUnavailableException;

/**
 * Directory Access interface to MCD
 *
 * This interface is present for unit testing purposes.
 */
public interface IDirectoryAccess {

    /**
     * Performs lookup of the subscriber and its cos in MCD
     * and combines the attributes together
     * @param subscriberIdentity
     * @return the DirectoryAccessSubscriber
     */
    public IDirectoryAccessSubscriber lookupSubscriber(String subscriberIdentity);

    /**
     * Performs lookup of the subscriber and its COS in MCD and combines the attributes together.
     * Throws
     * @param subscriberIdentity Subscriber id
     * @param throwException To throw exception instead of inhibiting it if a fault occurs
     * @return DirectoryAccessSubscriber
     * @throws AuthenticationException, DBUnavailableException, MCDException, Exception
     */
    public IDirectoryAccessSubscriber lookupSubscriber(String subscriberIdentity, boolean throwException)
            throws AuthenticationException, DBUnavailableException, MCDException, Exception;

    /**
     * Updates a subscriber in MCD
     * @param subscriber
     * @throws DirectoryAccessException
     */
    public void updateSubscriber(IDirectoryAccessSubscriber subscriber, String attrName, String value) throws DirectoryAccessException;

	/**
	 * Performs lookup of a cos in MCD
	 * @param cosIdentity
	 * @return
	 */
	public MoipProfile lookupCos(String cosIdentity);


	/**
	 * Performs lookup of a broadcast announcement in MCD
	 * @param baName  Name of Broadcast Announcement
	 * @return
	 */
	public IDirectoryAccessProfile lookupBroadcastAnnouncement(String baName);

	/**
	 * In Georedundancy configuration, verify that MCD is not in read-only mode
	 * @return true if all MCD operations are available
	 */
	public boolean isProfileUpdatePossible();
}
