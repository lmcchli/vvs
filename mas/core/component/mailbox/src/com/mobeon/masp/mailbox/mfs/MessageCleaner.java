/**
 * 
 */
package com.mobeon.masp.mailbox.mfs;

import java.util.Properties;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.MessageIdentifier;
import com.abcxyz.messaging.common.oam.PropertyFileConfigManagerGen2;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.message.MfsStateFolderType;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.AbstractEventHandler;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Removes messages when their retention time has expired. 
 * @author egeobli
 */
class MessageCleaner extends AbstractEventHandler {

	static private final ILogger logger = ILoggerFactory.getILogger(MessageCleaner.class);

	private ICommonMessagingAccess commonMessagingAccess;


	MessageCleaner(ICommonMessagingAccess cma) {
		commonMessagingAccess = cma;
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
	public int eventFired(AppliEventInfo eventInfo) {
		Properties properties = eventInfo.getEventProperties();
		String omsa = properties.getProperty(MfsMessageAdapter.OMSA);
		String rmsa = properties.getProperty(MfsMessageAdapter.RMSA);
		String omsgid = properties.getProperty(MfsMessageAdapter.OMSGID);
		String rmsgid = properties.getProperty(MfsMessageAdapter.RMSGID);
		String folder = properties.getProperty(MoipMessageEntities.FOLDER);
		MessageInfo messageInfo = new MessageInfo(new MSA(omsa), new MSA(rmsa), omsgid, rmsgid);
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Removing expired message: " + messageInfo.getMsgId());
			}
			StateFile stateFile = commonMessagingAccess.getStateFile(messageInfo, folder);

			//commonMessagingAccess.deleteMessage(stateFile);
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Applying msg undelete logic if enabled and conditions fit:
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			MessageIdentifier msgId = new MessageIdentifier(stateFile.omsa, stateFile.rmsa, stateFile.omsgid, stateFile.rmsgid);            
			if (!(isMessageUndeleteEnabled())) {
				commonMessagingAccess.deleteMessage(stateFile);
			} else {
				String prioryState = stateFile.getMsgState();
				if (prioryState.contains("recycled")) { // expiry of a msg that's already in the undelete hidden recycle bin; just delete it
					commonMessagingAccess.deleteMessage(stateFile);
				}	
				// For msg in trash, if inbox counterpart exists, then really delete the msg in trash; 
				// (because this is the case where VVM was simply moving the message back from trash into the inbox);
				// else, move the msg from trash to inbox first, then apply undelete logic
				boolean applyUndelete = true; 
				if (stateFile.folderType != null && stateFile.folderType.equals(MfsStateFolderType.TRASH)) {
					if (((CommonMessagingAccess)commonMessagingAccess).checkIfStateFileExist(msgId, MfsStateFolderType.INBOX)) {
						logger.debug("MessageCleaner.eventFired(): msg is in trash with existing counterpart in inbox, so really delete; " + msgId);
						applyUndelete = false;
						commonMessagingAccess.deleteMessage(stateFile);
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
					if (((CommonMessagingAccess)commonMessagingAccess).checkIfStateFileExist(msgId, MfsStateFolderType.TRASH)) {
						logger.debug("MessageCleaner.eventFired(): msg is in inbox with existing counterpart in trash, so really delete; " + msgId);
						applyUndelete = false;
						commonMessagingAccess.deleteMessage(stateFile);
					}
				}

				if (applyUndelete) {
					StateFile stateFileInbox = commonMessagingAccess.getStateFile(new MessageInfo(msgId), "inbox"); // redo get stateFile because we may have moved it above
					((CommonMessagingAccess)commonMessagingAccess).saveChangesForRecycle(stateFileInbox, 
							this.props.getIntValue(ConfigParam.MAX_NBR_MSG_UNDELETE, 10, null, null),
							this.props.getIntValue(ConfigParam.RECYCLE_MSG_EXPIRY_DAYS, 30, null, null));
				}
			} // if (!(isMessageUndeleteEnabled()))
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot delete expired message: " + e.getMessage());
			}
		}

		return EventHandleResult.OK;
	}

}
