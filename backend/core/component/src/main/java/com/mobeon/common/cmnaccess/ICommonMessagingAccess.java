package com.mobeon.common.cmnaccess;

import java.util.Date;
import java.util.Properties;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.Message;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.scheduler.handling.AppliEventOperations;
import com.abcxyz.services.moip.masevent.EventTypes;

public interface ICommonMessagingAccess {

	/**
	 * This method should be used to store a message in the MFS
	 *
	 * @param c1 Contains all the container 1 headers
	 * @param c2 Contains all the container 3 headers
	 * @param c3Parts c3Parts An array of all the messages contents parts
	 * @param attributes  All the attributes value pairs that should be stored in the StateFile
	 * @throws Exception
	 */
	public int storeMessage(Container1 c1, Container2 c2,
			MsgBodyPart[] c3Parts, StateAttributes attributes) throws Exception;

	/**
	 * Retrieve a single Message from the MFS
	 *
	 * @param msgInfo Message info object use to find the message in question
	 * @return Message Object
	 * @throws MsgStoreException
	 */
	public Message readMessage(MessageInfo msgInfo) throws MsgStoreException;

	/**
	 * Searches for all the messages in a users Mail
	 *
	 * @param msa
	 * @param filter
	 * @return
	 * @throws MsgStoreException
	 */
	public Message[] searchMessages(MSA msa, StateAttributesFilter filter)
			throws MsgStoreException;

	/**
	 * Searches for all the messageInfos in a users Mail
	 *
	 * @param msa
	 * @param filter
	 * @return
	 * @throws MsgStoreException
	 */
	public MessageInfo[] searchMessageInfos(MSA msa, StateAttributesFilter filter)
			throws MsgStoreException;

	/**
	 * Search the state files matching the filter
	 * @param msa
	 * @param filter
	 * @return StateFile[]
	 * throws MsgStoreException
	 */
	public StateFile[] searchStateFiles(MSA mas, StateAttributesFilter filter)
			throws MsgStoreException;

	/**
	 * Counts the number of messages that match the filter
	 *
	 * @param msa users MFS mailbox
	 * @param filter State attribute filters
	 * @return The number of message
	 * @throws MsgStoreException
	 */

	public int countMessages(MSA msa, StateAttributesFilter filter)
			throws MsgStoreException;

	/**
	 * update the messages StateFile
	 *
	 * @param state the new messages sates
	 * @throws MsgStoreException
	 */
	public void updateState(StateFile state) throws MsgStoreException;

	/**
	 * return stateFile for messageInfo file
	 *
	 * @param msgInfo
	 * @return
	 * @throws MsgStoreException
	 */
	public StateFile getStateFile(MessageInfo msgInfo) throws MsgStoreException;

	public StateFile getStateFile(MessageInfo msgInfo, String folder) throws MsgStoreException;

	/**
	 * Lists all the state files for a user in the given folder
	 *
	 * @param msa
	 * @param folder
	 * @return
	 * @throws MsgStoreException
	 */
	public MessageInfo[] listStateFile(MSA msa, String folder) throws MsgStoreException;

	/**
	 * Deletes the Message from the MFS
	 *
	 * @param state State file of the message to be deleted.
	 * @throws MsgStoreException
	 */
	public void deleteMessage(StateFile state) throws MsgStoreException;

    public String getMoipPrivateFolder(String msid, boolean internal);

    public AppliEventOperations getMasEventOpeartor();

    public String scheduleEvent(long when, MessageInfo msgInfo, EventTypes eventType, Properties props);

    public void cancelEvent(String eventId);

    public void notifyNtf(EventTypes eventType, MessageInfo msgInfo, String recipientId, Properties properties);

    public void setMessageExpiry(int expiryInDays, StateFile stateFile) throws MsgStoreException;

    public void setMessageExpiry(Date expiryDate, StateFile stateFile) throws MsgStoreException;

    public void cancelScheduledEvent(StateFile stateFile);

    public String denormalizeNumber(String number);

    /**
     * Checks if MFS storage is available. The storage is unavailable on a replicated site during Geo-Redundancy failover.
     * @param originator The A number
     * @param recipient The B number
     * @return true if storage is possible in the Geo Redundant system
     */
    public boolean isStorageOperationsAvailable(String originator, String recipient);

    public boolean isStorageOperationsAvailable(MSA originator, MSA recipient);

}