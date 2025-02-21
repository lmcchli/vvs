/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.mfs;

import static com.mobeon.masp.mailbox.MailboxMessageType.EMAIL;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.Container3;
import com.abcxyz.messaging.common.message.Message;
import com.abcxyz.messaging.common.message.MessageIdentifier;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.util.SystemPropertyHandler;
import com.abcxyz.messaging.common.util.UIDGenerator;
import com.abcxyz.messaging.mfs.MFS;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.exception.SchemaException;
import com.abcxyz.messaging.mfs.message.MfsFileFolder;
import com.abcxyz.messaging.mfs.message.MfsStateFolderType;
import com.abcxyz.messaging.mfs.MsgStoreServerFactory;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mfs.statefile.StateFileServices;
import com.abcxyz.messaging.mfs.statefile.StateFileServicesNoSQL;
import com.abcxyz.services.broadcastannouncement.BroadcastAnnouncement;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.util.FaxNumber;
import com.mobeon.common.util.FaxPrintStatus;
import com.mobeon.masp.mailbox.BaseStoredMessage;
import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.IMessageContent;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.mediaobject.IMediaObject;



/**
 */
public class MfsMessageAdapter extends BaseStoredMessage<MfsContext> implements IStoredMessage {

	static final String OMSA = "omsa";
	static final String RMSA = "rmsa";
	static final String OMSGID = "omsgid";
	static final String RMSGID = "rmsgid";

	private static final ILogger log = ILoggerFactory.getILogger(MfsMessageAdapter.class);

	private Message adaptedMessage;
	//    private MimePart spokenNameOfSenderPart;
	private StateFile stateFile;
	private CommonMessagingAccess cms;
	private String accessPoint = null;

	private static boolean updateUID = Boolean.parseBoolean(SystemPropertyHandler.getProperty("abcxyz.mfs.AppleStateChangeFix", "false"));

	MfsMessageAdapter(StateFile stateFile, MfsContext context) {
		super(context);
		this.cms = CommonMessagingAccess.getInstance();
		this.stateFile = stateFile;
		parseStateFile();
	}


	/**
	 * Get the contents of the state files
	 */
	void parseStateFile() {
		try {
			receivedDate = CommonMessagingAccess.DateFormatter.get().parse(stateFile.getC1Attribute(Container1.Date_time));
		} catch (Exception e) {
			log.warn("Error parsing " + stateFile.getPath() + " for receivedDate: " + e.getMessage());
		}

		//the delivery report job is remove from here
		deliveryStatus = null;

		subject = stateFile.getC1Attribute(Container1.Subject);
		sender = stateFile.getC1Attribute(Container1.From);
		replyToAddress = stateFile.getAttribute(MoipMessageEntities.REPLY_TO_HEADER);
		String recipientList = stateFile.getC1Attribute(Container1.To);
		if(recipientList == null) {
			recipients = null;
		} else {
			recipients = recipientList.split("[;,]");
		}

		String ccRecipientList = stateFile.getC1Attribute(Container1.Cc);
		if(ccRecipientList == null) {
			secondaryRecipients = null;
		} else {
			secondaryRecipients = ccRecipientList.split("[;,]");
		}

		String msgState = stateFile.getMsgState();
		StoredMessageState mfsState = MfsUtil.toMasMessageState(msgState);
		if (mfsState == null) {
			mfsState = StoredMessageState.NEW;
		}

		state = mfsState;

		String contentType = stateFile.getC1Attribute(Container1.Message_class);
		MailboxMessageType t = MfsUtil.toMasMessageType(contentType);
		if (t == null) {
			t = EMAIL;
		}
		type = t;

		senderVisibility = stateFile.getC1Attribute(Container1.Sender_visibility);

		String mfsPriority = stateFile.getC1Attribute(Container1.Priority);
		if (mfsPriority == null) {
			urgent = false;
		}

		try {
			urgent = MfsUtil.toMasPriority(Integer.parseInt(mfsPriority));
		} catch (NumberFormatException e) {
			urgent = false;
		} catch (IllegalArgumentException e) {
			urgent = false;
		}


		String headerValue = stateFile.getAttribute(MoipMessageEntities.CONFIDENTIALITY_HEADER);
		if (headerValue == null) {
			// If not in C2, try search in C1 for backward compatibility
			if (log.isDebugEnabled()) log.debug("confidentiality is not in C2. Try search in C1.");
			headerValue = stateFile.getC1Attribute(Container1.Privacy);
			if (headerValue == null){
				// Shouldn't happen. Something wrong. Log it.
				if (log.isInfoEnabled()) log.info("Getting confidentiality failed. Neither in C1 nor in C2. Something wrong in state file.");
				confidential = false;
			} else {
				if (log.isDebugEnabled()) log.debug("confidentiality is in C1.");
			}
		}

		if (headerValue != null) {
			try {
				confidential = MfsUtil.toMasConfidentialState(headerValue);
			} catch (IllegalArgumentException e) {
				confidential = false;
			}
		}

		language = stateFile.getC1Attribute(MoipMessageEntities.LANGUAGE_HEADER);

		broadcastAnnouncementLanguage = stateFile.getAttribute(BroadcastAnnouncement.BROADCAST_LANGUAGE_STATEFILE_HEADER);
	}


	/**
	 * Get the message content.
	 * Returns null if list not has been initialized.
	 *
	 * @return list of message content.
	 */
	public List<IMessageContent> getContent() throws MailboxException {

		if(content == null) {
			MessageInfo messageInfo = new MessageInfo(stateFile.omsa, stateFile.rmsa, stateFile.omsgid, stateFile.rmsgid);
			try {
				adaptedMessage = cms.readMessage(messageInfo);
				parseBodyPart(adaptedMessage.getContainer3());
			} catch (MsgStoreException e) {
				//e.printStackTrace();
				throw new MailboxException("MfsMessageAdapter getContent() fails when calling readMessage()");
			}
		}

		List<IMessageContent> result = content != null ? Collections.unmodifiableList(content) : null;

		return result;
	}

	/**
	 * Returns the spoken name of sender.
	 *
	 * @return the spoken name of sender as a media object. (If exists)
	 * @throws com.mobeon.masp.mailbox.SpokenNameNotFoundException
	 *          if no spoken name is set.
	 *
	 */
	public IMediaObject getSpokenNameOfSender() throws MailboxException {
		if (log.isInfoEnabled()) log.info("getSpokenNameOfSender()");
		if (log.isDebugEnabled()) log.debug("Getting spokenNameOfSender for " + this);
		IMediaObject result = null;
		if (spokenNameOfSender != null) {
			try {
				getContext().getMailboxLock().lock();
				result = spokenNameOfSender.getMediaObject();
			} finally {
				getContext().getMailboxLock().unlock();
			}
		}
		if (log.isInfoEnabled()) log.info("getSpokenNameOfSender() returns " + result);
		return result;
	}

	private void parseBodyPart(Container3 parts) {

		if (log.isDebugEnabled()) log.debug("Parsing body part " + this);

		if (content == null) {
			content = new ArrayList<IMessageContent>();
		}
		int numParts = parts.numberOfParts();
		if (log.isDebugEnabled())
			log.debug("MfsMessageAdapter.parseBodyPart : - Number of parts in container3 = " + numParts);

		for(MsgBodyPart part : parts.getContents())
		{
			parseBodyPart(part);
		}
	}

	private void parseBodyPart(MsgBodyPart part) {

		log.debug("xxx parseBodyPart: "+part.toString());
		String disposition = part.getPartHeader(Constants.CONTENT_DISPOSITION);

		if(disposition == null) {
			log.debug("disposition null ");
			MfsPartAdapter newPart = new MfsPartAdapter(part, getContext());
			content.add(newPart);
			return;
		}
		if (disposition.contains(Constants.ORIGINATOR_SPOKEN_NAME_STRING)){
			spokenNameOfSender = new MfsPartAdapter(part, getContext());
			if (log.isDebugEnabled()){
				log.debug("MfsMessageAdapter.parseBodyPart(): Adding Spoken Name - disposition=[" + disposition + "]");
			}
		} else {
			MfsPartAdapter newPart = new MfsPartAdapter(part, getContext());
			content.add(newPart);
			if (log.isDebugEnabled()){
				log.debug("MfsMessageAdapter.parseBodyPart(): Adding Content - disposition=[" + disposition + "]");
			}
		}

	}

	/**
	 * Prints the message at the given destination.
	 *
	 * @param sender  address
	 * @param destination  address
	 * @param autoprint  <code>true</code> when print is triggered automatically,
	 *          <code>false</code> if triggered manually
	 * @throws MailboxException  if the fax message cannot be printed
	 */
	public void print(String sender, FaxNumber destination, boolean autoprint)
			throws MailboxException {
		if (sender == null || destination == null) {
			throw new NullPointerException("sender[" + sender + "] or destination[" + destination + "] cannot be null");
		}

		if (type != MailboxMessageType.FAX) {
			throw new MailboxException("This message type[" + type + "] cannot be printed to a fax machine!");
		}

		if (getFaxPrintStatus() == FaxPrintStatus.printing) {
			throw new MailboxException("Multiple concurrent fax prints of the same message is not allowed");
		}



		String omsa = stateFile.omsa.toString();

		String rmsa = stateFile.rmsa.getId();


		Properties properties = new Properties();
		properties.put(MoipMessageEntities.FAX_PRINT_NUMBER_PROPERTY, destination.toString());
		properties.put(MoipMessageEntities.FAX_AUTOPRINT_ENABLE_PROPERTY, Boolean.toString(autoprint));
		properties.put(MoipMessageEntities.FAX_PRINT_FAXMSG_RMSA, rmsa);
		properties.put(MoipMessageEntities.FAX_PRINT_FAXMSG_RMSGID, stateFile.rmsgid);
		properties.put(MoipMessageEntities.FAX_PRINT_FAXMSG_OMSA, omsa);
		properties.put(MoipMessageEntities.FAX_PRINT_FAXMSG_OMSGID, stateFile.omsgid);

		if (log.isDebugEnabled()){
			log.debug("MfsMessageAdaptor.print(): Sending new Fax Print event to NTF from[" + sender + "] with properties: " + properties);
		}




		// Order of calls is important here...if "printing" status is set first and the thread dies before
		// the NTF event is created then the message will be left in a "printing" status (not good, msg would not
		// be allowed to be deleted or printed by end user). Instead, create NTF event and then update "printing" status.
		// If thread dies before status is updated then worse case we could have two prints ongoing at same time.
		MessageIdentifier msgId = new MessageIdentifier(stateFile.omsa, stateFile.rmsa, stateFile.omsgid, stateFile.rmsgid);
		cms.notifyNtf(EventTypes.FAX_PRINT, new MessageInfo(msgId), sender, properties);
		FaxPrintStatus.changeStatus(stateFile, FaxPrintStatus.printing);
	}

	/**
	 * Returns the fax print status for a fax message.
	 *
	 * @return  a constant from the <code>FaxPrintStatus</code> enumeration
	 */
	public FaxPrintStatus getFaxPrintStatus() {
		return FaxPrintStatus.getStatus(stateFile);
	}

	/**
	 * Creates a storeable message with this message as content.
	 *
	 * @return storable message.
	 *
	 */
	public MfsForwardMessage forward() throws MailboxException {
		if (log.isInfoEnabled()) log.info("forward()");
		if (log.isDebugEnabled()) log.debug("Forward " + this);
		MfsForwardMessage javamailForwardMessage = new MfsForwardMessage(getContext(), this);
		if (log.isInfoEnabled()) log.info("forward() returns " + javamailForwardMessage);
		return javamailForwardMessage;
	}

	/**
	 * Copy this message into the specified Folder.
	 * This operation appends this Message to the destination Folder.
	 *
	 * @param folder target folder.
	 * @throws com.mobeon.masp.mailbox.MailboxException
	 *          if a problem occurs.
	 */
	public void copy(IFolder folder) throws MailboxException {
		//        if (log.isInfoEnabled()) log.info("copy(" + folder + ")");
		//        if (log.isDebugEnabled()) log.debug("Copy " + this + " to " + folder);
		//        if (folder instanceof MfsFolderAdapter) {
		//            try {
		//            	MfsFolderAdapter jfa = (MfsFolderAdapter) folder;
		//                getContext().getMailboxLock().lock();
		////                folderAdapter.open();
		//                adaptedMessage.getFolder().copyMessages(new Message[]{adaptedMessage}, jfa.folder);
		//            } catch (MessagingException e) {
		//
		//                MailboxException e2 = new MailboxException("Could not copy " + this + " to " + folder + ": " + e.getMessage()+". URL: "+getURL());
		//                log.debug(e2.getMessage(), e);
		//                throw e2;
		//            } finally {
		//                getContext().getMailboxLock().unlock();
		//            }
		//        } else {
		//            ClassCastException e = new ClassCastException("Could not cast " + folder.getClass()
		//                    .getName() + " to an " + MfsFolderAdapter.class.getName());
		//            log.error(e.getMessage());
		//            throw e;
		//        }
		//        if (log.isInfoEnabled()) log.info("copy(IFolder) returns void");
		if (log.isInfoEnabled()) log.info("copy(IFolder) returns null ... not implemented");
	}

	/**
	 * Save any changes made to this message into the message-store.
	 *
	 *
	 */
	public void saveChanges() throws MailboxException {
		if (log.isInfoEnabled()) log.info("saveChanges()");
		if (log.isDebugEnabled()) log.debug("Saving changes for " + this);
		try {
			getContext().getMailboxLock().lock();
			saveState();
		} catch (Exception e) {
			throw new MailboxException("Could not save message changes." , e);
		} finally {
			getContext().getMailboxLock().unlock();
		}
		if (log.isInfoEnabled()) log.info("saveChanges() returns void");
	}

	/**
	 * For message undelete feature
	 */
	@Deprecated // Use CommonMessagingAccess.saveChangesForRecycle()
	public void saveChangesForRecycle(StoredMessageState prioryState, int maxNumberMsgUndelete, int daysToExpire)  throws MailboxException {
		checkFaxMessageBeforeDeletion();        
		try {
			String timeStamp = "" + System.currentTimeMillis();
			stateFile.setMsgState(timeStamp + "|recycled|" + MfsUtil.toMfsMessageState(prioryState));
			cms.cancelScheduledEvent(stateFile);
			setMessageExpiry(daysToExpire, stateFile);
			cms.updateState(stateFile, false);
		} catch (MsgStoreException e) {
			log.error("MfsMessageAdapter.saveChangesForRecycle() error updating " + stateFile + ": " + e, e);
			throw new MailboxException(e.toString());
		}
		// Check total number of messages in recycle bin; if exceeding max, delete the oldest ones
		MsgStoreServer mfs = MsgStoreServer.getInstance();
		try {
			ArrayList<StateFile> sfs = mfs.getsortedListOfRecycledMessages(stateFile.rmsa.getId());
			if (sfs != null && sfs.size() > maxNumberMsgUndelete) {
				int numberOfOldestToDelete = sfs.size() - maxNumberMsgUndelete;
				log.debug("MfsMessageAdapter.saveChangesForRecycle(): total number of msg in recycle bin exceeds the limit " + 
						maxNumberMsgUndelete + " by " + numberOfOldestToDelete + "; to permanently delete some oldest msg in recycle bin");
				for (int i = 0; i < numberOfOldestToDelete; i++) {
					StateFile sf = sfs.get(i);
					cms.deleteMessage(sf);
				}
			}
		} catch (MsgStoreException e) {
			log.error("MfsMessageAdapter.saveChangesForRecycle() error updating recycled msg list for " + stateFile.rmsa.getId() + ": " + e, e);
			// Do not throw exception as the recylcing op itself succeeded
		} catch (Exception e) {
			log.error("MfsMessageAdapter.saveChangesForRecycle() error updating recycled msg list for " + stateFile.rmsa.getId() + ": " + e, e);
			// Do not throw exception as the recylcing op itself succeeded
		}
	}

	public StateFile getStateFile() {
		return stateFile;
	}

	private void saveState() throws MailboxException {
		try {
			updateUID();
			stateFile.setMsgState(MfsUtil.toMfsMessageState(state));
			String subscriber = stateFile.getC1Attribute(Container1.To);

			switch (state) {
			case NEW:
				setMessageExpiry(getRetentionTime(subscriber, state, stateFile.folderType), stateFile);
				cms.updateState(stateFile);
				break;
			case SAVED:
				setMessageExpiry(getRetentionTime(subscriber, state, stateFile.folderType), stateFile);
				cms.updateState(stateFile, false);
				break;
			case READ:
				setMessageExpiry(getRetentionTime(subscriber, state, stateFile.folderType), stateFile);
				cms.updateState(stateFile, false);
				break;
			case DELETED:
				checkFaxMessageBeforeDeletion();
				cms.cancelScheduledEvent(stateFile);
				cms.deleteMessage(stateFile);
				break;
			}

		} catch (Exception e) {
			throw new MailboxException("Could not save message change to state " + state + ". " , e);
		}
	}


	/**
	 * Wait until fax message is finished printing before it can be deleted.
	 */
	private void checkFaxMessageBeforeDeletion()
			throws MailboxException {
		if (type == MailboxMessageType.FAX &&
				getFaxPrintStatus() == FaxPrintStatus.printing) {
			throw new MailboxException("Cannot delete fax message while it is still printing");
		}
	}

	private void updateUID() {
		//If accessPoint is null this mean it is access from the tui.
		if(updateUID && (accessPoint == null || "".equalsIgnoreCase(accessPoint) || "tui".equalsIgnoreCase(accessPoint))){
			//If the state will be deleted then don't bother updating the UID
			if(state == StoredMessageState.DELETED){
				return;
			}

			//If the state doesn't change no need to update the UID
			if(stateFile.getMsgState().equalsIgnoreCase(MfsUtil.toMfsMessageState(state))){
				return;
				//If the state goes from READ or SAVED to READ or SAVED, no need to update the UID
			}else if(stateFile.getMsgState().equalsIgnoreCase(MfsUtil.toMfsMessageState(StoredMessageState.READ)) ||
					stateFile.getMsgState().equalsIgnoreCase(MfsUtil.toMfsMessageState(StoredMessageState.SAVED))){
				if(state != StoredMessageState.NEW){
					return;
				}
			}
			//Update the UID if the state have change from NEW to READ or SAVED or from READ or SAVED to NEW

			IDirectoryAccessSubscriber subscriberProfile = DirectoryAccess.getInstance().lookupSubscriber(stateFile.getC1Attribute(Container1.To));

			boolean isApple = false;
			String[] clientTypes = subscriberProfile.getStringAttributes(DAConstants.ATTR_VVM_CLIENT_TYPE);

			for(String clientType: clientTypes){
				if(clientType.equalsIgnoreCase("Apple")){
					isApple = true;
					break;
				}
			}

			if(isApple){
				/*********************************
                long uid = 0;

                MFS anMFS = CommonMessagingAccess.getMfs();
                String userFullPath = MfsFileFolder.getUserPath(stateFile.rmsa);
                String msgFullPath = stateFile.getParentPath();

                // msgFullPath is a superset of userFullPath e.g. /user/full/path/msg/path.  We want just the relative path: msg/path
                String msgRelPath = msgFullPath.substring(userFullPath.length(), msgFullPath.length());

                String mfsPathToPrivate = anMFS.getMsgClassPath(StateAttributes.UID, stateFile.rmsa.getId());
                File uidStorage = new File(mfsPathToPrivate, "uid.properties");

                UIDGenerator uidGenerator = new UIDGenerator(uidStorage);

                try {
                    uid = uidGenerator.getNextUID(msgRelPath.toLowerCase());
                    stateFile.setAttribute(StateAttributes.UID, Long.toString(uid));
                } catch (IOException e) {
                }
				 ************************************/
				//==> MIO 5.0 MIO5_MFS
				try {
					if(MsgStoreServerFactory.isTypeNoSQL()) {
						StateFileServicesNoSQL.updateUID(stateFile);
					} else {
						StateFileServices.addUID(stateFile, CommonMessagingAccess.getMfs());
					}
				} catch (Exception e) {
					log.error("exception caught when adding UID for state file " + stateFile.getPath() + "" + e, e); 
				}
			}
		}
	}


	private int getRetentionTime(String subscriber, StoredMessageState state, MfsStateFolderType folderType){
		int retentionValue = 14;  //default for new and saved
		String[] retention = null;

		IDirectoryAccessSubscriber subscriberProfile = DirectoryAccess.getInstance().lookupSubscriber(stateFile.getC1Attribute(Container1.To));

		if (subscriberProfile != null) {
			switch (folderType) {
			case INBOX:
				switch (state) {
				case NEW:
					if(type == MailboxMessageType.VOICE) {
						retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE);
					} else if(type == MailboxMessageType.VIDEO) {
						retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_NEW_VIDEO);
					} else if(type == MailboxMessageType.FAX) {
						retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_NEW_FAX);
					}
					break;
				case SAVED:
					if(type == MailboxMessageType.VOICE) {
						retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_SAVED_VOICE);
					} else if(type == MailboxMessageType.VIDEO) {
						retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_SAVED_VIDEO);
					} else if(type == MailboxMessageType.FAX) {
						retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_SAVED_FAX);
					}
					break;
				case READ:
					if(type == MailboxMessageType.VOICE) {
						retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_READ_VOICE);
					} else if(type == MailboxMessageType.VIDEO) {
						retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_READ_VIDEO);
					} else if(type == MailboxMessageType.FAX) {
						retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_READ_FAX);
					}
					break;
				}
				break;
			case TRASH:
				retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_DELETED_VOICE);
				break;
			}
		}

		try {
			if(retention != null && retention.length > 0) {
				retentionValue = new Integer(retention[0]).intValue();
				if(log.isDebugEnabled()) {
					log.debug("MfsMessageAdapter.getRetentionTime: Found " + MfsUtil.toMfsMessageType(type) + "/" + MfsUtil.toMfsMessageState(state) + " retention time " + retentionValue + " for subscriber " + subscriber);
				}
				return retentionValue;
			}else {
				log.warn("MfsMessageAdapter.getRetentionTime: Did not find the " + MfsUtil.toMfsMessageType(type) + "/" + MfsUtil.toMfsMessageState(state) + " retention time for " + subscriber);
			}
		} catch (NumberFormatException nme) {
			log.warn("MfsMessageAdapter::saveState Unable to parse the " + MfsUtil.toMfsMessageType(type) + "/" + MfsUtil.toMfsMessageState(state) + " retention time for subscriber " + subscriber + ". Retention [" + (retention == null ? "null" : retention[0]) + "]");
		} 	

		return retentionValue;
	}

	public String toString() {
		return stateFile.rmsgid;

	}



	public void messageSetExpiryDate(String expiryDate) throws MailboxException {
		try {
			setMessageExpiry(expiryDate, stateFile);
			cms.updateState(stateFile);
		} catch (SchemaException e) {
			throw new MailboxException("Could not change expiry date." , e);
		} catch (MsgStoreException e) {
			throw new MailboxException("Could not modify expiry date." , e);
		} catch (ParseException e) {
			throw new MailboxException("Could not change expiry date.", e);
		}
	}

	/**
	 * Sets the expiry date of the current message and schedules an expiry event.
	 *
	 * @param expiryInDays Expiry date.
	 * @param stateFile State file of the message.
	 * @throws MsgStoreException Thrown on error.
	 */
	private void setMessageExpiry(int expiryInDays, StateFile stateFile) throws MsgStoreException {
		cms.setMessageExpiry(expiryInDays, stateFile);
	}

	/**
	 * Sets the expiry date of the current message and schedules an expiry event.
	 *
	 * @param expiryDate Expiry date.
	 * @param stateFile State file of the message.
	 * @throws ParseException Thrown if the date is not in a known format.
	 * @throws MsgStoreException Thrown on error.
	 */
	private void setMessageExpiry(String expiryDate, StateFile stateFile)
			throws ParseException, MsgStoreException {
		Date date = CommonMessagingAccess.DateFormatter.get().parse(expiryDate);
		cms.setMessageExpiry(date, stateFile);
	}
	/**
	 *
	 * Get header from C2 container
	 * (Overide this existing get getAdditionalProperty from parent for mfs
	 * @param headerName Header Name
	 * @return the header value if found or null if not found

	 */
	public String getAdditionalProperty(String headerName)
	{
		Container2 c2 = adaptedMessage.getContainer2();
		if(c2==null)
		{
			return null;
		}
		log.debug("c2 headers: "+c2.toString());
		return adaptedMessage.getContainer2().getProtocolHeader(headerName);
	}


	@Override
	public void setMessageAccessPoint(String accessPoint) {
		this.accessPoint = accessPoint;
	}


	@Override
	public String getBroadcastLanguage() {
		return broadcastAnnouncementLanguage;
	}

}
