/**
 * 
 */
package com.abcxyz.services.moip.masevent;

import java.util.Properties;


import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.MessageIdentifier;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.PropertyFileConfigManagerGen2;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.message.MfsStateFolderType;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.cmnaccess.UserInbox;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.mdr.MdrEvent;
import com.abcxyz.services.moip.common.mdr.MdrConstants;
import com.abcxyz.services.moip.masevent.AbstractEventHandler;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.SystemTopologyHelper;
import com.mobeon.common.cmnaccess.TopologyException;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Removes messages when their retention time has expired. 
 */
public class MessageCleaner extends AbstractEventHandler {

	static private final ILogger logger = ILoggerFactory.getILogger(MessageCleaner.class);
	int maximumNumberOfTries = 10;
	public static final String MESSAGE_STATE = "msgstate";

	public MessageCleaner() {
	}

	public void start(String serviceName) {
		ConfigManager localConfig = CommonOamManager.getInstance().getLocalConfig();
		int masExpiryIntervalInMin = localConfig.getIntValue(MoipMessageEntities.messageDeletionExpiryIntervalInMin);

		RetryEventInfo info = new RetryEventInfo(serviceName);
		info.setEventRetrySchema(masExpiryIntervalInMin + "m CONTINUE");
		start(info);

		maximumNumberOfTries = localConfig.getIntValue(MoipMessageEntities.messageDeletionExpiryRetries);
	}


	// Msg undelete
	PropertyFileConfigManagerGen2 props = null;
	public boolean isMessageUndeleteEnabled() {
		if (this.props == null) {
			this.props = PropertyFileConfigManagerGen2.getInstance();
			this.props.loadConfigFile(ConfigParam.MSG_UNDELETE_PROP_FILE, true, null, null);
		}
		return this.props.getBooleanValue(ConfigParam.ENABLE_MSG_UNDELETE);
	}

	@Override
	public int eventFired(AppliEventInfo firedEventInfo) {
		int result = EventHandleResult.OK;

		try {
			if (firedEventInfo.getNumberOfTried() >= maximumNumberOfTries) {
				logger.info("Expiry event " + firedEventInfo.getEventId() + ", will not retry (number of expiry retries reached");
				return EventHandleResult.STOP_RETRIES;
			}

			Properties properties = firedEventInfo.getEventProperties();
			String omsa = properties.getProperty(MoipMessageEntities.OMSA);
			String rmsa = properties.getProperty(MoipMessageEntities.RMSA);
			String omsgid = properties.getProperty(MoipMessageEntities.OMSGID);
			String rmsgid = properties.getProperty(MoipMessageEntities.RMSGID);
			String folder = properties.getProperty(MoipMessageEntities.FOLDER);
			MessageInfo messageInfo = new MessageInfo(new MSA(omsa), new MSA(rmsa), omsgid, rmsgid);
			StateFile stateFile = null;

			CommonMessagingAccess cma = CommonMessagingAccess.getInstance();
			try {
				stateFile = cma.getStateFile(messageInfo, folder);
			} catch (MsgStoreException ex) {
				// Verify if MsgStoreException was thrown because state file was not found
				MessageInfo[] msgInfoList = cma.listStateFile(new MSA(omsa), folder);
				MessageInfo tempMessageInfo = new MessageInfo(new MSA(omsa), new MSA(rmsa), omsgid, rmsgid);

				for(MessageInfo msgInfo : msgInfoList) {
					if( msgInfo.equals(tempMessageInfo) ) {
						//file exist, retry later
						throw ex;
					}
				}

				//file not found, cancel event
				if (logger.isDebugEnabled()) {
					logger.debug("MessageCleaner.eventFired(): State file not found for " + messageInfo.getMsgId());
				}
				return EventHandleResult.STOP_RETRIES;
			}

			String msgState = stateFile.getMsgState();
			/**
			 * Compare the keys of the firedEventId against the storedEventId.
			 * If the two eventIds match, the fired event is the expected one and the message must be deleted.
			 * If not, it means that the firedEvendId is obsolete since another instance (in a geo-distributed scenario)
			 * either cancelled the event (without any success) or re-schedule another one and the fired one is obsolete.
			 */

			String storedEventId = stateFile.getAttribute(MoipMessageEntities.EXPIRY_EVENT_ID);           
			if (storedEventId != null) {
				boolean shouldProcessFiredEvent = CommonMessagingAccess.getInstance().compareEventIds(firedEventInfo, storedEventId);
				if (shouldProcessFiredEvent) {
					logger.info("MessageCleaner.eventFired(): Deleting message " + messageInfo.getMsgId());
					// cma.deleteMessage(stateFile);
					///////////////////////////////////////////////////////////////////////////////////////////////////////////
					// Applying msg undelete logic if enabled and conditions fit:
					///////////////////////////////////////////////////////////////////////////////////////////////////////////
					MessageIdentifier msgId = new MessageIdentifier(stateFile.omsa, stateFile.rmsa, stateFile.omsgid, stateFile.rmsgid);

					if (!(isMessageUndeleteEnabled())) {
						cma.deleteMessage(stateFile);
					} else {
						String prioryState = stateFile.getMsgState();
						if (prioryState.contains("recycled")) { // expiry of a msg that's already in the undelete hidden recycle bin; just delete it
							cma.deleteMessage(stateFile);
						}	
						// For msg in trash, if inbox counterpart exists, then really delete the msg in trash; 
						// (because this is the case where VVM was simply moving the message back from trash into the inbox);
						// else, move the msg from trash to inbox first, then apply undelete logic
						boolean applyUndelete = true; 
						if (stateFile.folderType != null && stateFile.folderType.equals(MfsStateFolderType.TRASH)) {
							if (cma.checkIfStateFileExist(msgId, MfsStateFolderType.INBOX)) {
								logger.debug("MessageCleaner.eventFired(): msg is in trash with existing counterpart in inbox, so really delete; " + msgId);
								applyUndelete = false;
								cma.deleteMessage(stateFile);
							} else {
								// Inbox counterpart does not exist, move trash to inbox first before doing cms.saveChangesForRecycle()
								logger.debug("MessageCleaner.eventFired(): msg is in trash with no counterpart in inbox, move it from trash to inbox; " + msgId);
								MsgStoreServer mfs = MsgStoreServer.getInstance();
								try {
									mfs.cloneStateFromTrash(stateFile);
									mfs.deleteStateInTrash(stateFile);
								} catch (Exception e) {
									logger.error("MessageCleaner.eventFired(): error occured when trying to move msg from trash to inbox; " + msgId, e);
									throw e;
								}       
							}
						} // if (sf.folderType != null && sf.folderType.equals(MfsStateFolderType.TRASH))
						// For msg in inbox, if trash counterpart exists, then really delete the msg in trash 
						// (because this is the case where VVM was simply moving the message from inbox into the trash);
						if (stateFile.folderType == null || stateFile.folderType.equals(MfsStateFolderType.INBOX)) {
							if (cma.checkIfStateFileExist(msgId, MfsStateFolderType.TRASH)) {
								logger.debug("MessageCleaner.eventFired(): msg is in inbox with existing counterpart in trash, so really delete; " + msgId);
								applyUndelete = false;
								cma.deleteMessage(stateFile);
							}
						}

						if (applyUndelete) {
							StateFile stateFileInbox = cma.getStateFile(new MessageInfo(msgId), "inbox"); // redo get stateFile because we may have moved it above
							cma.saveChangesForRecycle(stateFileInbox, 
									this.props.getIntValue(ConfigParam.MAX_NBR_MSG_UNDELETE, 10, null, null),
									this.props.getIntValue(ConfigParam.RECYCLE_MSG_EXPIRY_DAYS, 30, null, null));
						}
					} // if (!(isMessageUndeleteEnabled()))


					// Add msgstate to mwioff event properties. This property will be used by NTF to determine whether to trigger notification or not
					properties.setProperty(MESSAGE_STATE, msgState);

					String phoneNumber = stateFile.getC1Attribute(Container1.To);
					if (phoneNumber != null && phoneNumber.length() > 0) {
						phoneNumber = CommonMessagingAccess.getInstance().denormalizeNumber(phoneNumber);
					}

					CommonMessagingAccess.getInstance().notifyNtf(EventTypes.MWI_OFF, messageInfo, phoneNumber, properties);
					result = EventHandleResult.STOP_RETRIES;                   
					/**
					 * VVM notifies NTF that a message has been deleted
					 * NTF should then send a SYNC message to the phone, only if the message is not in the trash folder.
					 */
					if (!"trash".equalsIgnoreCase(folder)) {
						CommonMessagingAccess.getInstance().notifyNtf(EventTypes.MSG_EXPIRY, messageInfo, stateFile.getC1Attribute(Container1.To), null);

						// MDR are not generated when the message is in the trash folder
						sendMessageDeletedMdr(stateFile);
					}
				} else {
					logger.info("MessageCleaner.eventFired(): ignored " + messageInfo.getMsgId() + "Event possibly cancelled by different site");
					return EventHandleResult.STOP_RETRIES;
				}
			}
		} catch (Exception e) {

			if (logger.isDebugEnabled()) {
				String message = "MessageCleaner.eventFired() failed to delete expired message: " + firedEventInfo.getEventId();
				if (firedEventInfo.getNumberOfTried() >= maximumNumberOfTries || firedEventInfo.getNextEventInfo() == null || firedEventInfo.isLastExpire()) {
					logger.debug(message + ", will not retry. ", e);
				} else {
					logger.debug(message + ", will retry. ", e);
				}
			}
		}
		return result;
	}

	private void sendMessageDeletedMdr(StateFile stateFile){
		ConfigManager localConfig = CommonOamManager.getInstance().getLocalConfig();
		if(localConfig.getBooleanValue(MoipMessageEntities.mdrEnabled)){
			String nasIdentifierPrefix = MoipMessageEntities.MESSAGE_SERVICE_MAS;
			String opco = "unknown";

			try{
				opco = SystemTopologyHelper.getOpcoName();
			}
			catch (TopologyException e) {
				logger.debug("MessageCleaner.sendMessageDeletedMdr(): Got exception " + e.getMessage()+ " while getting operator from topology");
			}

			try{
				String recipient = stateFile.getC1Attribute(Container1.To);
				String contentType = stateFile.getC1Attribute(Container1.Message_class);

				MdrEvent mdrEvent = new MdrEvent();
				mdrEvent.setUserName(recipient);               
				mdrEvent.setNasIdentifier(CommonOamManager.getInstance().getLocalInstanceNameFromTopology(nasIdentifierPrefix));            

				mdrEvent.setOpcoId(opco);

				mdrEvent.setObjectType(MdrConstants.MESSAGE); 
				mdrEvent.setEventType(MdrConstants.DELETE); 
				mdrEvent.setEventReason(MdrConstants.RETENTION);   

				String voiceInventory = UserInbox.getInventory(recipient, ServiceName.VOICE, MoipMessageEntities.MESSAGE_NEW)+","+
						UserInbox.getInventory(recipient, ServiceName.VOICE, MoipMessageEntities.MESSAGE_READ)+","+
								UserInbox.getInventory(recipient, ServiceName.VOICE, MoipMessageEntities.MESSAGE_SAVED);

				String videoInventory = UserInbox.getInventory(recipient, ServiceName.VIDEO, MoipMessageEntities.MESSAGE_NEW)+","+
						UserInbox.getInventory(recipient, ServiceName.VIDEO, MoipMessageEntities.MESSAGE_READ)+","+
						UserInbox.getInventory(recipient, ServiceName.VIDEO, MoipMessageEntities.MESSAGE_SAVED);

				String faxInventory = UserInbox.getInventory(recipient, ServiceName.FAX, MoipMessageEntities.MESSAGE_NEW)+","+
						UserInbox.getInventory(recipient, ServiceName.FAX, MoipMessageEntities.MESSAGE_READ)+","+
						UserInbox.getInventory(recipient, ServiceName.FAX, MoipMessageEntities.MESSAGE_SAVED);

				mdrEvent.setVoiceInventory(voiceInventory);
				mdrEvent.setVideoInventory(videoInventory);
				mdrEvent.setFaxInventory(faxInventory);
				mdrEvent.setMessageType(MdrConstants.messageTypeMap.get(contentType));

				mdrEvent.write(); 
			}

			catch (MsgStoreException e){
				logger.error("MessageCleaner.sendMessageDeletedMdr() failed to get inventory", e);
			}

		}
	}


}
