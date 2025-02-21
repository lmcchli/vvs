package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.util.FaxNumber;
import com.mobeon.common.util.FaxPrintStatus;
import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: 2007-jan-23
 * Time: 21:07:01
 * To change this template use File | Settings | File Templates.
 */
public class StoredMessageMock implements IStoredMessage {
    private String subject;
    private MailboxMessageType type;
    private boolean urgent = false;
    private StoredMessageState state = StoredMessageState.NEW;

    public void setMessageContent(List<IMessageContent> messageContent) {
        this.messageContent = messageContent;
    }

    private List<IMessageContent> messageContent = new ArrayList<IMessageContent>();
	private String senderVisibility = "";

    public void print(String sender, FaxNumber destination, boolean autoprint) throws MailboxException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
    
    /**
     * Dummy implementation;
     */
    public FaxPrintStatus getFaxPrintStatus() {
        throw new UnsupportedOperationException();
    }

    public IStorableMessage forward() throws MailboxException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void saveChanges(int savedRetentionDays, int readRetentionDays) throws MailboxException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void copy(IFolder folder) throws MailboxException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Date getReceivedDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isForward() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDeliveryReport() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public StoredMessageState getState() {
        return state;
    }

    public void setState(StoredMessageState state) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void messageSetExpiryDate(String expiryDate) throws MailboxException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public DeliveryStatus getDeliveryReport() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IMediaObject getSpokenNameOfSender() throws MailboxException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<IMessageContent> getContent() throws MailboxException {
        return messageContent;
    }

    public String getSender() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getRecipients() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getSecondaryRecipients() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getSubject() {
        return subject;
    }

    public String getReplyToAddress() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public MailboxMessageType getType() {
        return type;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public boolean isConfidential() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getLanguage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAdditionalProperty(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setType(MailboxMessageType type) {
        this.type = type;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

	@Override
	public String getSenderVisibility() {
		return senderVisibility;
	}

	@Override
	public void saveChanges() throws MailboxException {

	}
	@Override
	public void saveChangesForRecycle(StoredMessageState prioryState, int maxNumberMsgUndelete, int daysToExpire)  throws MailboxException {

	}

    @Override
    public void setMessageAccessPoint(String accessPoint) {

    }

	@Override
	public String getBroadcastLanguage() {
		// TODO Auto-generated method stub
		return null;
	}
}
