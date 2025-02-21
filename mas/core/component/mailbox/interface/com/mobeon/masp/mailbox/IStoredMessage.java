/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import java.util.Date;

import com.mobeon.common.util.FaxNumber;
import com.mobeon.common.util.FaxPrintStatus;

/**
 * Represent a stored message in the subscribers mailbox.
 */
public interface IStoredMessage extends IMailboxMessage {

    /**
     * Prints the message att the givemn destination.
     * 
     * @param sender  address
     * @param destination  address
     * @param autoprint  <code>true</code> when print is triggered automatically, 
     *          <code>false</code> if triggered manually
     * @throws MailboxException  if the fax message cannot be printed
     */
    public void print(String sender, FaxNumber destination, boolean autoprint) throws MailboxException;

    /**
     * Returns the fax print status for a fax message.
     * 
     * @return  a constant from the <code>FaxPrintStatus</code> enumeration 
     */
    public FaxPrintStatus getFaxPrintStatus();

    /**
     * Creates a storeable message with this message as content.
     * @return storable message.
     * @throws MailboxException
     */
    public IStorableMessage forward() throws MailboxException;

    /**
     * Save any changes made to this message into the message-store. 
     * @throws MailboxException
     */
    public void saveChanges() throws MailboxException;

    /**
     * @param prioryState
     * @param maxNumberMsgUndelete
     * @param daysToExpire
     */
    @Deprecated // Use CommonMessagingAccess.saveChangesForRecycle()
	public void saveChangesForRecycle(StoredMessageState prioryState, int maxNumberMsgUndelete, int daysToExpire)
			 throws MailboxException;    

    /**
     * Copy this message into the specified Folder.
     * This operation appends this Message to the destination Folder.
     * @param folder target folder.
     * @throws MailboxException if a problem occurs.
     */
    public void copy(IFolder folder) throws MailboxException;


    /**
     * Get the date and time when the message arrived in users mailbox.
     * @return the received date.
     */
    public Date getReceivedDate();


    /**
     * Indicates if this message is a forwarded message.
     * @return true if this message includes another message forwarded through calling {@link #forward()}.
     */
    public boolean isForward();

    /**
     * Indicates if this message is a delivery report.
     * Equals to <code>this.getDeliveryStatus() != null;</code>
     * @return true if this message includes a delivery report.
     */
    public boolean isDeliveryReport();

    /**
     * Gets the mailbox message state.
     * @return state
     */
    public StoredMessageState getState();

    /**
     * Sets the mailbox message state.
     * @param state
     */
    public void setState(StoredMessageState state);

    /**
     * Renews date when message was "issued" to now.
     * Implementation should have an internal date used as date when the message was issued.
     * Application may want to renew issued date to extend the message rentention time.
     * Initially issued date is equal or near equal to received date.
     * Different to issued date received date can not be changed.
     * @throws MailboxException if a problem occurs.
     */
    public void messageSetExpiryDate(String expiryDate) throws MailboxException;

    /**
     * Returns the result of a delivery (print or store)
     * if the message is a delivery report.
     * @return delivery status (null if message not is a delivery report).
     */
    public DeliveryStatus getDeliveryReport();

    /**
     * Returns if the sender's phone number must shown or not
     * @return sender's visibility
     */
    public String getSenderVisibility();
    
    /**
     * Set the message access point
     * @param accessPoint
     */
    public void setMessageAccessPoint(String accessPoint);

}