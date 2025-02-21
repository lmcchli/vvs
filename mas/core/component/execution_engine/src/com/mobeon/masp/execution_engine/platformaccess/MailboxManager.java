/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.MessageIdentifier;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.PropertyFileConfigManagerGen2;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.message.MfsStateFolderType;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.InformEventResult;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.messaging.mrd.operation.InformEventResp;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.cmnaccess.UserInbox;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.mdr.MdrConstants;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.masp.mailbox.mfs.MfsMessageAdapter;
import com.mobeon.masp.execution_engine.platformaccess.util.MediaUtil;
import com.mobeon.masp.execution_engine.platformaccess.util.MessageTypeUtil;
import com.mobeon.masp.execution_engine.platformaccess.util.SearchUtil;
import com.mobeon.masp.execution_engine.platformaccess.util.TimeUtil;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.ParameterTypeException;
import com.mobeon.common.configuration.UnknownParameterException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.util.DataException;
import com.mobeon.common.util.FaxNumber;
import com.mobeon.common.util.FaxPrintStatus;
import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.util.markup.Detagger;

import jakarta.activation.MimeType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.ParseException;

/**
 * Manages the object and objectid's used in the mailboxrelated functions in the platformaccess interface.
 * <p/>
 * When an error occur a PlatformException is thrown. The exception contains an eventname and detailed information about
 * the error. The errors are also reported to the error log with debug level.
 *
 * @author ermmaha
 */
public class MailboxManager {
	private static final String MSG_PROPERTY_SENDER = "sender";
	private static final String MSG_PROPERTY_RECIPIENTS = "recipients";
	private static final String MSG_PROPERTY_SUBSCRIBERID = "recipientsSubscriberId";
	private static final String MSG_PROPERTY_SECONDARYRECIPIENTS = "secondaryrecipients";
	private static final String MSG_PROPERTY_SUBJECT = "subject";
	private static final String MSG_PROPERTY_REPLYTOADDR = "replytoaddr";
	private static final String MSG_PROPERTY_TYPE = "type";
	private static final String MSG_PROPERTY_LANGUAGE = "language";
	private static final String MSG_PROPERTY_URGENT = "urgent";
	private static final String MSG_PROPERTY_CONFIDENTIAL = "confidential";
	private static final String MSG_PROPERTY_DELIVERYDATE = "deliverydate";
	private static final String MSG_PROPERTY_STATE = "state";
	private static final String MSG_PROPERTY_RECEIVEDDATE = "receiveddate";
	private static final String MSG_PROPERTY_FORWARDED = "forwarded";
	private static final String MSG_PROPERTY_DELIVERYREPORT = "deliveryreport";
	private static final String MSG_PROPERTY_DELIVERYSTATUS = "deliverystatus";
	private static final String MSG_PROPERTY_SENDERVISIBILITY = "sendervisibility";
	private static final String MSG_PROPERTY_FAXPRINTSTATUS = "faxprintstatus";

	private static final String CONTENT_LENGTH_TYPE_MILLISECONDS = "milliseconds";
	private static final String CONTENT_LENGTH_TYPE_PAGES = "pages";

	/**
	 * logger
	 */
	private static ILogger log = ILoggerFactory.getILogger(MailboxManager.class);

	private IStorableMessageFactory iStorableMessageFactory;

	private int mailBoxId;
	private int folderId;
	private int messageListId;
	private int messageId;
	private int messageContentId;
	private int storableMessageId;

	/**
	 * phonenumber to mailboxid mapping
	 */
	private HashMap<String, Integer> mailboxIds = new HashMap<String, Integer>();

	/**
	 * mailboxid to mailboxentry mapping
	 */
	private HashMap<Integer, MailboxEntry> mailBoxes = new HashMap<Integer, MailboxEntry>();
	/**
	 * folderid to mailboxid mapping
	 */
	private HashMap<Integer, Integer> folderIdToMailboxId = new HashMap<Integer, Integer>();
	/**
	 * messageid to folderid mapping
	 */
	private HashMap<Integer, Integer> messageListIdToFolderId = new HashMap<Integer, Integer>();
	/**
	 * messageid to messagelistid mapping
	 */
	private HashMap<Integer, Integer> messageIdToMessageListId = new HashMap<Integer, Integer>();
	/**
	 * messagecontentid to messageid mapping
	 */
	private HashMap<Integer, Integer> messageContentIdToMessageId = new HashMap<Integer, Integer>();
	/**
	 * storableMessagesids to storableMessages mapping
	 */
	private HashMap<Integer, IStorableMessage> storableMessages = new HashMap<Integer, IStorableMessage>();

	/**
	 * storableMessagesids to "set of mailhosts" mapping
	 * The set contains the mailhosts to which the storable message is intended to be sent.
	 * Note that the code may decide to simple send the message to just one mailhost and let the mailhost
	 * do eventual routing!
	 */
	private HashMap<Integer, Set<String>> storableMessagesMailHosts = new HashMap<Integer, Set<String>>();

	/**
	 * Constructor
	 *
	 * @param iStorableMessageFactory for creating IStorableMessage
	 */
	public MailboxManager(IStorableMessageFactory iStorableMessageFactory) {
		this.iStorableMessageFactory = iStorableMessageFactory;
	}

	/**
	 * Returns the Map containing all mailboxid's keyed by telephonenumber.
	 *
	 * @return mailboxid map
	 */
	public HashMap<String, Integer> getMailboxIds() {
		return mailboxIds;
	}

	/**
	 * Retrieves mailboxid and adds the incoming IMailbox to the cache
	 *
	 * @return int mailboxid
	 */
	public int subscriberGetMailboxId(IMailbox mailbox) {
		int newId = generateMailboxId();
		mailBoxes.put(newId, new MailboxEntry(mailbox, newId));
		return newId;
	}

	/**
	 * Retrieves mailbox message usage from the mailbox specified with mailboxId.
	 *
	 * @return usageId used to get the the values later. (it is the mailboxId)
	 */
	public int mailBoxUsage(int mailboxId) {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("MailboxManager.mailBoxUsage(mailboxId)");
			}
			MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
			if (mailboxEntry != null) {
				try {
					IQuotaUsageInventory quotaUsageInventory = mailboxEntry.getMailbox().getQuotaUsageInventory();
					mailboxEntry.setMessageUsage((int) quotaUsageInventory.getQuota(QuotaName.TOTAL).getMessageUsage());
					mailboxEntry.setMessageUsage(QuotaName.FAX, (int) quotaUsageInventory.getQuota(QuotaName.FAX).getMessageUsage());
					mailboxEntry.setMessageUsage(QuotaName.VOICE, (int) quotaUsageInventory.getQuota(QuotaName.VOICE).getMessageUsage());
					mailboxEntry.setMessageUsage(QuotaName.VIDEO, (int) quotaUsageInventory.getQuota(QuotaName.VIDEO).getMessageUsage());
					return mailboxId;
				} catch (MailboxException e) {
					throw new PlatformAccessException(EventType.MAILBOX, "mailBoxUsage:mailboxId=" + mailboxId, e);
				}
			}
			throw new PlatformAccessException(EventType.SYSTEMERROR, "mailBoxUsage:mailboxId=" + mailboxId, "Invalid mailboxId");
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
	}

	/**
	 * Retrieves message usage from the mailbox specified with mailboxId
	 *
	 * @return int message usage
	 */
	public int mailboxGetMessageUsage(int mailboxId) {
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {
			return mailboxEntry.getMessageUsage();
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageUsage:usageId=" + mailboxId, "Invalid usageId");
	}
	/**
	 * Retrieves message usage from the mailbox specified with mailboxId and quota type
	 *
	 * @return int message usage
	 */
	public int mailboxGetMessageUsage(int mailboxId, String msgType) {
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {
			if(msgType.equalsIgnoreCase("voice"))
				return mailboxEntry.getMessageUsage(QuotaName.VOICE);
			else if (msgType.equalsIgnoreCase("video"))
				return mailboxEntry.getMessageUsage(QuotaName.VIDEO);
			else if (msgType.equalsIgnoreCase("fax"))
				return mailboxEntry.getMessageUsage(QuotaName.FAX);
			else
				throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageUsage:msgType=" + msgType, "Invalid messageType");
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageUsage:usageId=" + mailboxId, "Invalid usageId");
	}
	/**
	 * Retrieves a folderId from the specified mailboxid and foldername.
	 *
	 * @return folderId
	 */
	public int mailboxGetFolder(int mailboxId, String folderName) {
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {
			FolderEntry fe = mailboxEntry.getFolderEntry(folderName);
			if (fe != null) {
				return fe.getObjectId();
			}

			try {
				IMailbox iMailbox = mailboxEntry.getMailbox();
				IFolder iFolder = iMailbox.getFolder(folderName);
				return mailboxEntry.putFolder(iFolder);
			} catch (FolderNotFoundException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "mailboxGetFolder:folderName=" + folderName, e);
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "mailboxGetFolder:mailboxId=" + mailboxId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetFolder:mailboxId=" + mailboxId, "Invalid mailboxId");
	}

	/**
	 * Sets the mailbox to readonly.
	 *
	 */
	public void mailboxSetReadonly(int mailboxId) {
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {

			IMailbox iMailbox = mailboxEntry.getMailbox();
			try {
				iMailbox.setReadonly();
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "mailboxGetFolder:mailboxId=" + mailboxId, e);
			}
		} else {
			throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetFolder:mailboxId=" + mailboxId, "Invalid mailboxId");
		}
	}

	/**
	 * Sets the mailbox to readonly.
	 *
	 */
	public void mailboxSetReadwrite(int mailboxId) {
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {

			IMailbox iMailbox = mailboxEntry.getMailbox();
			try {
				iMailbox.setReadwrite();
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "mailboxGetFolder:mailboxId=" + mailboxId, e);
			}
		} else {
			throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetFolder:mailboxId=" + mailboxId, "Invalid mailboxId");
		}
	}

	/**
	 * Adds a folder to the mailbox specified with mailboxId
	 *
	 * @param folderName name on the new folder
	 */
	public void mailboxAddFolder(int mailboxId, String folderName) {
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {
			IFolderParent iFolderParent = mailboxEntry.getMailbox();
			try {
				iFolderParent.addFolder(folderName);
				return;
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "mailboxAddFolder:mailboxId=" + mailboxId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxAddFolder:mailboxId=" + mailboxId, "Invalid mailboxId");
	}

	/**
	 * Adds a folder to a folder specified with folderid in the mailbox specified with mailboxId
	 *
	 * @param folderName name on the new folder
	 */
	public void mailboxAddFolder(int mailboxId, int folderId, String folderName) {
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {
			FolderEntry folderEntry = mailboxEntry.getFolderEntry(folderId);
			if (folderEntry != null) {
				IFolderParent iFolderParent = folderEntry.getFolder();
				try {
					iFolderParent.addFolder(folderName);
					return;
				} catch (MailboxException e) {
					throw new PlatformAccessException(EventType.MAILBOX, "mailboxAddFolder:mailboxId=" + mailboxId, e);
				}
			}
			throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxAddFolder:folderId=" + folderId, "Invalid folderId");
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxAddFolder:mailboxId=" + mailboxId, "Invalid mailboxId");
	}

	/**
	 * Copies the specified message to the specified folder on the top level of the subscribers mailbox
	 *
	 */
	public void messageCopyToFolder(int mailboxId, int messageId, String folderName) {
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {
			try {
				IFolder folder = mailboxEntry.getMailbox().getFolder(folderName);
				MessageEntry entry = getMessageEntry(messageId);
				if (entry != null) {
					IStoredMessage storedMessage = entry.getMessage();
					try {
						storedMessage.copy(folder);
						return;
					} catch (MailboxException e) {
						throw new PlatformAccessException(EventType.MAILBOX, "messageCopyToFolder:mailboxId=" + mailboxId +
								", messageId=" + messageId + ", folderName=" + folderName, e);
					}
				}
				throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:messageId=" + messageId, "Invalid messageId");
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageCopyToFolder:folderName=" + folderName, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:mailboxId=" + mailboxId, "Invalid mailboxId");
	}

	/**
	 */
	public void messageCopyToFolder(int mailboxId, int folderId, int messageId, String folderName) {
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {
			FolderEntry folderEntry = mailboxEntry.getFolderEntry(folderId);
			if (folderEntry != null) {
				MessageEntry entry = getMessageEntry(messageId);
				if (entry != null) {
					IStoredMessage storedMessage = entry.getMessage();
					try {
						IFolder folder = folderEntry.getFolder();
						IFolder subFolder = folder.getFolder(folderName);
						storedMessage.copy(subFolder);
						return;
					} catch (MailboxException e) {
						throw new PlatformAccessException(EventType.MAILBOX, "messageCopyToFolder:mailboxId=" + mailboxId +
								", messageId=" + messageId + ", folderName=" + folderName, e);
					}
				}
				throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:messageId=" + messageId, "Invalid messageId");
			}
			throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:folderId=" + folderId, "Invalid folderId");
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:mailboxId=" + mailboxId, "Invalid mailboxId");
	}

	/**
	 * Searches for messages in the mailserver via the Mailbox interface.
	 *
	 * @return id of the search
	 */
	public int mailboxGetMessageList(int folderId, String types, String states, String priorities, String orders,
			String timeOrder, String language) {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("MailboxManager.mailboxGetMessageList(folderId,types,states,priorities,orders)");
			}
			if (!folderIdToMailboxId.containsKey(folderId)) {
				throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageList:folderId=" + folderId, "Invalid folderId");
			}
			if (log.isDebugEnabled()) {
				log.debug("In mailboxGetMessageList: folderId=" + folderId + ", types= " + types +
						", states=" + states + ", priorities=" + priorities + ", orders=" + orders + ", timeOrder=" + timeOrder
						+ ", language=" + language);
			}
			int mailboxId = folderIdToMailboxId.get(folderId);
			MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
			if (mailboxEntry != null) {
				try {
					FolderEntry folderEntry = mailboxEntry.getFolderEntry(folderId);
					if (folderEntry != null) {

						IFolder iFolder = folderEntry.getFolder();

						SearchUtil searchUtil = new SearchUtil(types, states, priorities, orders, timeOrder, language);

						List<IStoredMessage> list = iFolder.searchMessages(
								searchUtil.getSearchCriteria(), searchUtil.getSearchComparator());

						return folderEntry.putMessageList(list);
					}
				} catch (SearchCriteriaException e) {
					//add all parameters to this error message?
					throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageList:folderId=" + folderId, e);

				} catch (MailboxException e) {
					throw new PlatformAccessException(EventType.MAILBOX, "mailboxGetMessageList:folderId=" + folderId, e);
				}
			}
			return -1;
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
	}

	/**
	 * Searches for messages in the previous listing that was made and referenced by messageListId.
	 *
	 * @return id of the search
	 */
	public int mailboxGetMessageSubList(int messageListId, String types, String states, String priorities,
			String orders, String timeOrder) {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("MailboxManager.mailboxGetMessageSubList(messageListId,types,states,priorities,orders,timeOrder)");
			}
			if (!messageListIdToFolderId.containsKey(messageListId)) {
				throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageSubList:messageListId=" + messageListId,
						"Invalid messageListId");
			}
			if (log.isDebugEnabled()) {
				log.debug("In mailboxGetMessageSubList: messageListId=" + messageListId + ", types= " + types +
						", states=" + states + ", priorities=" + priorities + ", orders=" + orders + ", timeOrder=" + timeOrder);
			}
			int folderId = messageListIdToFolderId.get(messageListId);
			int mailboxId = folderIdToMailboxId.get(folderId);
			MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
			if (mailboxEntry != null) {
				FolderEntry folderEntry = mailboxEntry.getFolderEntry(folderId);
				if (folderEntry != null) {
					MessageListEntry messageListEntry = folderEntry.getMessageListEntry(messageListId);
					IStoredMessageList iStoredMessageList = (IStoredMessageList) messageListEntry.getMessageList();

					try {
						SearchUtil searchUtil = new SearchUtil(types, states, priorities, orders, timeOrder, null);
						IStoredMessageList result = iStoredMessageList.select(searchUtil.getSearchCriteria());
						Collections.sort(result, searchUtil.getSearchComparator());

						return folderEntry.putMessageList(result);

					} catch (SearchCriteriaException e) {
						//add all parameters to this error message?
						throw new PlatformAccessException(EventType.SYSTEMERROR,
								"mailboxGetMessageSubList:messageListId=" + messageListId, e);
					}
				}
			}
			return -1;
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
	}

	/**
	 * Retrieves number of messages that was retrieved in a previous search referenced by messageListId
	 *
	 * @param messageListId id for the search result list
	 * @param types         a string containing the type of messages to get, separated by comma. Valid values are "voice", "video", "fax" and "email".
	 * @param states        a string containing the state for the messages to get, separated by comma. Valid values are "new", "read", "deleted" and "saved".
	 * @param priorities    a string containing the priority for the messages to get, separated by comma. Valid values are "urgent" and "nonurgent".
	 * @return the number of messages
	 */
	public int mailboxGetNumberOfMessages(int messageListId, String types, String states, String priorities) {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("MailboxManager.mailboxGetNumberOfMessages(messageListId,types,states,priorities)");
			}
			if (log.isDebugEnabled()) {
				log.debug("In mailboxGetNumberOfMessages: messageListId=" + messageListId + ", types= " + types +
						", states=" + states + ", priorities=" + priorities);
			}

			if (!messageListIdToFolderId.containsKey(messageListId)) {
				throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetNumberOfMessages:messageListId=" + messageListId,
						"Invalid messageListId");
			}

			int folderId = messageListIdToFolderId.get(messageListId);
			int mailboxId = folderIdToMailboxId.get(folderId);
			MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
			if (mailboxEntry != null) {
				FolderEntry folderEntry = mailboxEntry.getFolderEntry(folderId);
				if (folderEntry != null) {
					MessageListEntry messageListEntry = folderEntry.getMessageListEntry(messageListId);
					IStoredMessageList iStoredMessageList = (IStoredMessageList) messageListEntry.getMessageList();

					try {
						SearchUtil searchUtil = new SearchUtil(types, states, priorities, null, null, null);
						IStoredMessageList result = iStoredMessageList.select(searchUtil.getSearchCriteria());

						int numberOfMessages = result.size();
						if (log.isDebugEnabled()) log.debug("In mailboxGetNumberOfMessages: numberOfMessages=" + numberOfMessages);
						return numberOfMessages;

					} catch (SearchCriteriaException e) {
						//add all parameters to this error message?
						throw new PlatformAccessException(EventType.SYSTEMERROR,
								"mailboxGetNumberOfMessages:messageListId=" + messageListId, e);
					}
				}
			}
			return -1;
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
	}

	/**
	 * @return array of messageId's
	 */
	public int[] mailboxGetMessages(int messageListId) {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("MailboxManager.mailboxGetMessages(messageListId)");
			}
			if (!messageListIdToFolderId.containsKey(messageListId)) {
				throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessages:messageListId=" + messageListId,
						"Invalid messageListId");
			}
			if (log.isDebugEnabled()) {
				log.debug("In mailboxGetMessages: messageListId=" + messageListId);
			}
			int folderId = messageListIdToFolderId.get(messageListId);
			int mailboxId = folderIdToMailboxId.get(folderId);
			MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
			if (mailboxEntry != null) {
				FolderEntry folderEntry = mailboxEntry.getFolderEntry(folderId);
				if (folderEntry != null) {
					MessageListEntry messageListEntry = folderEntry.getMessageListEntry(messageListId);
					return messageListEntry.getMessageIds();
				}
			}
			return new int[0];
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
	}

	/**
	 * Retrieves a property from the message specified with messageId
	 *
	 * @return String value for the property
	 */
	public String[] messageGetStoredProperty(int messageId, String propertyName) {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("MailboxManager.messageGetStoredProperty(messageId,propertyName)");
			}
			if (log.isDebugEnabled()) {
				log.debug("In messageGetStoredProperty: messageId=" + messageId + ", propertyName= " + propertyName);
			}
			MessageEntry entry = getMessageEntry(messageId);
			if (entry != null) {
				IStoredMessage iStoredMessage = entry.getMessage();
				if (propertyName.equals(MSG_PROPERTY_STATE)) {
					StoredMessageState value = iStoredMessage.getState();
					if (value != null) {
						return new String[]{MessageTypeUtil.messageStateToString(value)};
					}
				} else if (propertyName.equals(MSG_PROPERTY_SENDER)) {
					String value = iStoredMessage.getSender();
					if (value != null) {
						return new String[]{value};
					}
				} else if (propertyName.equals(MSG_PROPERTY_RECIPIENTS)) {
					String[] value = iStoredMessage.getRecipients();
					if (value != null) {
						return value;
					}
				} else if (propertyName.equals(MSG_PROPERTY_SECONDARYRECIPIENTS)) {
					String[] value = iStoredMessage.getSecondaryRecipients();
					if (value != null) {
						return value;
					}
				} else if (propertyName.equals(MSG_PROPERTY_SUBJECT)) {
					String value = iStoredMessage.getSubject();
					if (value != null) {
						return new String[]{value};
					}
				} else if (propertyName.equals(MSG_PROPERTY_REPLYTOADDR)) {
					String value = iStoredMessage.getReplyToAddress();
					if (value != null) {
						return new String[]{value};
					}else {
						return new String[]{""};
					}
				} else if (propertyName.equals(MSG_PROPERTY_TYPE)) {
					MailboxMessageType value = iStoredMessage.getType();
					if (value != null) {
						return new String[]{MessageTypeUtil.messageTypeToString(value)};
					}
				} else if (propertyName.equals(MSG_PROPERTY_LANGUAGE)) {
					String value = iStoredMessage.getLanguage();
					if (value != null) {
						return new String[]{value};
					}
				} else if (propertyName.equals(MSG_PROPERTY_RECEIVEDDATE)) {
					Date value = iStoredMessage.getReceivedDate();
					if (value != null) {
						return new String[]{TimeUtil.dateToVvaTime(value)};
					}
				} else if (propertyName.equals(MSG_PROPERTY_FAXPRINTSTATUS)) {
					FaxPrintStatus value = iStoredMessage.getFaxPrintStatus();
					if (value != null) {
						return new String[]{value.name()};
					} else {
						return new String[]{""};
					}
				} else if (propertyName.equals(MSG_PROPERTY_DELIVERYREPORT)) {
					return new String[]{iStoredMessage.isDeliveryReport() ? "true" : "false"};
				} else if (propertyName.equals(MSG_PROPERTY_DELIVERYSTATUS)) {
					DeliveryStatus value = iStoredMessage.getDeliveryReport();
					if (value == null) {
						return new String[]{"false"};
					}
					return new String[]{MessageTypeUtil.deliveryStatusToString(value)};
				} else if (propertyName.equals(MSG_PROPERTY_URGENT)) {
					return new String[]{iStoredMessage.isUrgent() ? "true" : "false"};
				} else if (propertyName.equals(MSG_PROPERTY_CONFIDENTIAL)) {
					return new String[]{iStoredMessage.isConfidential() ? "true" : "false"};
				} else if (propertyName.equals(MSG_PROPERTY_FORWARDED)) {
					return new String[]{iStoredMessage.isForward() ? "true" : "false"};
				} else if (propertyName.equals(MSG_PROPERTY_SENDERVISIBILITY)) {
					String value = iStoredMessage.getSenderVisibility();
					if (value != null) {
						return new String[]{value};
					} else {
						return new String[]{""};
					}
				} else {
					throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetStoredProperty:propertyName="
							+ propertyName, "Invalid propertyName " + propertyName);
				}
				//No value found for the property
				throw new PlatformAccessException(EventType.DATANOTFOUND, "messageGetStoredProperty:propertyName="
						+ propertyName, "No value found");
			}
			throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetStoredProperty:messageId=" + messageId, "Invalid messageId");
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
	}

	/**
	 * Sets a property to the message specified with messageId
	 *
	 * @param propertyValue values
	 */
	public void messageSetStoredProperty(int messageId, String propertyName, String[] propertyValue, TrafficEventManager trafficEventManager) {
		MessageEntry entry = getMessageEntry(messageId);
		if (entry == null) {
			throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStoredProperty:messageId=" + messageId, "Invalid messageId");
		}
		if (log.isDebugEnabled()) log.debug("messageSetStoredProperty " + propertyName + "=" + Arrays.asList(propertyValue));

		if (this.props == null) {
			this.props = PropertyFileConfigManagerGen2.getInstance();
			this.props.loadConfigFile(ConfigParam.MSG_UNDELETE_PROP_FILE, true, null, null);
		}

		IStoredMessage iStoredMessage = entry.getMessage();
		if (propertyName.equals(MSG_PROPERTY_STATE)) {
			StoredMessageState state = MessageTypeUtil.stringToMessageState(propertyValue[0]);
			try {
				CommonMessagingAccess cma = CommonMessagingAccess.getInstance();
				StateFile stateFile = ((MfsMessageAdapter)iStoredMessage).getStateFile();
				MessageIdentifier msgId = new MessageIdentifier(stateFile.omsa, stateFile.rmsa, stateFile.omsgid, stateFile.rmsgid);
				if (state.equals(StoredMessageState.DELETED) && isMessageUndeleteEnabled()) {
					// For msg in trash, if inbox counterpart exists, then really delete the msg in trash; 
					// (because this is the case where VVM was simply moving the message back from trash into the inbox); 
					// else, move the msg from trash to inbox first, then apply undelete logic
					boolean applyUndelete = true; 
					if (stateFile.folderType != null && stateFile.folderType.equals(MfsStateFolderType.TRASH)) {
						if (cma.checkIfStateFileExist(msgId, MfsStateFolderType.INBOX)) {
							log.debug("MailboxManager.messageSetStoredProperty: msg is in trash with existing counterpart in inbox, so really delete; " + msgId);
							applyUndelete = false;
							iStoredMessage.setState(state);
							iStoredMessage.saveChanges();
						} else {
							// Inbox counterpart does not exist, move trash to inbox first before doing cms.saveChangesForRecycle()
							log.debug("MailboxManager.messageSetStoredProperty: msg is in trash with no counterpart in inbox, move it from trash to inbox; " + msgId);
							MsgStoreServer mfs = MsgStoreServer.getInstance();
							try {
								stateFile = cma.getStateFile(new MessageInfo(msgId), "trash");
								mfs.cloneStateFromTrash(stateFile);
								mfs.deleteStateInTrash(stateFile);
							} catch (Exception e) {
								log.error("MailboxManager.messageSetStoredProperty: error occured when trying to move msg from trash to inbox; " + msgId, e);
								throw e;
							}       
						}
					} // if (sf.folderType != null && sf.folderType.equals(MfsStateFolderType.TRASH))
					// For msg in inbox, if trash counterpart exists, then really delete the msg in trash 
					// (because this is the case where VVM was simply moving the message from inbox into the trash);
					if (stateFile.folderType == null || stateFile.folderType.equals(MfsStateFolderType.INBOX)) {
						if (cma.checkIfStateFileExist(msgId, MfsStateFolderType.TRASH)) {
							log.debug("MailboxManager.messageSetStoredProperty: msg is in inbox with existing counterpart in trash, so really delete; " + msgId);
							applyUndelete = false;
							iStoredMessage.setState(state);
							iStoredMessage.saveChanges();
						}
					}

					if (applyUndelete) {
						stateFile = cma.getStateFile(new MessageInfo(msgId), "inbox"); // redo get stateFile because we may have moved it above
						cma.saveChangesForRecycle(stateFile, 
								this.props.getIntValue(ConfigParam.MAX_NBR_MSG_UNDELETE, 10, null, null),
								this.props.getIntValue(ConfigParam.RECYCLE_MSG_EXPIRY_DAYS, 30, null, null));
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						// update VVM (we could now be called by MAPS (MiO' REST interface server)
						///////////////////////////////////////////////////////////////////////////////////////////////////////
						/* No, delete triggered by MAPS does not come here, it goes to VMEventHandler.handleMsgDeleteEvent().
						 * So we only do VVM update there. We are only called by call flow which does the VVM notif already
						 * (We also do VVM update in VMEventHandler.handleMsgUndeleteEvent() when msg is undeleted)
						MessageInfo msgInfo = new MessageInfo(stateFile.omsa, stateFile.rmsa, stateFile.omsgid, stateFile.rmsgid);
						IDirectoryAccessSubscriber profile = cma.getMcd().lookupSubscriber(msgInfo.rmsa.toString());
						if (profile != null) {
							String tel = profile.getSubscriberIdentity("tel");
							profile.getStringAttributes("visual_voicemail");
							String[] services = profile.getStringAttributes("MOIPServices");
							if (tel != null && isVvmActivated(profile)) {
								Properties  properties = new Properties();
								properties.put("force", "false");
								properties.put("mailboxId", tel);
								properties.put("callednumber", tel);
								//properties.put("vvmNotify", "true");
								cma.notifyNtf(EventTypes.MAILBOX_UPDATE, msgInfo, tel, properties);
							} else {
								log.debug("MailboxManager.messageSetStoredProperty: not a VVM activated sub; tel: " + tel + "; rmsa: " + stateFile.rmsa);
							}
						} else {
							log.error("MailboxManager.messageSetStoredProperty: Can't find subscriber for identity: " + stateFile.rmsa);
						}
						*/
					}
				} else { // if (state.equals(StoredMessageState.DELETED) && isMessageUndeleteEnabled())
					iStoredMessage.setState(state);
					iStoredMessage.saveChanges();
				}

				//send Mdr (For CDR and charging)
				if(state.equals(StoredMessageState.DELETED)){
					sendMessageStatusMdr(iStoredMessage, MdrConstants.DELETE, trafficEventManager);                    
				}
				else{
					sendMessageStatusMdr(iStoredMessage, MdrConstants.MODIFY, trafficEventManager);
				}

			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageSetStoredProperty:messageId=" + messageId, e);
			} catch (Exception e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageSetStoredProperty:messageId=" + messageId, e);
			} 
		} else {
			throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStoredProperty:messageId=" + messageId,
					"Invalid propertyName " + propertyName);
		}
	}

	public boolean isVvmActivated(IDirectoryAccessSubscriber profile) {
		String[] services = profile.getStringAttributes(DAConstants.ATTR_SERVICES);
		for (String service: services) {
			if (service == null || service.trim().isEmpty()) continue;
			if (service.trim().equalsIgnoreCase("visual_voicemail")) {
				String[] s = profile.getStringAttributes(DAConstants.ATTR_VVM_ACTIVATED);
				if (s == null || s.length <= 0) return false;
				if (s[0] == null) return false;
				if (s[0].trim().equalsIgnoreCase("yes") || s[0].trim().equalsIgnoreCase("true")) {
					return true;
				}
			}
		}
		return false;
	}

	private void sendMessageStatusMdr(IMailboxMessage iMailBoxMessage, int eventType, TrafficEventManager trafficEventManager){
		ConfigManager localConfig = CommonOamManager.getInstance().getLocalConfig();
		if(localConfig.getBooleanValue(MoipMessageEntities.mdrEnabled)){
			try{
				for(String recipient:iMailBoxMessage.getRecipients()){

					// Query mfs for voice, fax and video msgs
					String voiceInventory = UserInbox.getInventory(recipient, ServiceName.VOICE, MoipMessageEntities.MESSAGE_NEW)+","+
							UserInbox.getInventory(recipient, ServiceName.VOICE, MoipMessageEntities.MESSAGE_READ)+","+
							UserInbox.getInventory(recipient, ServiceName.VOICE, MoipMessageEntities.MESSAGE_SAVED);

					String videoInventory = UserInbox.getInventory(recipient, ServiceName.VIDEO, MoipMessageEntities.MESSAGE_NEW)+","+
							UserInbox.getInventory(recipient, ServiceName.VIDEO, MoipMessageEntities.MESSAGE_READ)+","+
							UserInbox.getInventory(recipient, ServiceName.VIDEO, MoipMessageEntities.MESSAGE_SAVED);

					String faxInventory = UserInbox.getInventory(recipient, ServiceName.FAX, MoipMessageEntities.MESSAGE_NEW)+","+
							UserInbox.getInventory(recipient, ServiceName.FAX, MoipMessageEntities.MESSAGE_READ)+","+
							UserInbox.getInventory(recipient, ServiceName.FAX, MoipMessageEntities.MESSAGE_SAVED);

					String[] propertyNames = {MdrConstants.USERNAME, MdrConstants.OBJECTTYPE, MdrConstants.EVENTTYPE , MdrConstants.EVENTREASON,"voiceinventory","videoinventory","faxinventory","messagetype"};  
					String[] propertyValues ={recipient,String.valueOf(MdrConstants.MESSAGE),String.valueOf(eventType),String.valueOf(MdrConstants.TUI),
							voiceInventory,videoInventory,faxInventory,String.valueOf(MdrConstants.messageTypeMap.get(MessageTypeUtil.messageTypeToString(iMailBoxMessage.getType())))}; 
					trafficEventManager.trafficEventSend("messagestatus", propertyNames, propertyValues, false);
				}
			}
			catch (MsgStoreException e){
				throw new PlatformAccessException(EventType.MAILBOX, "sendMessageStatusMdr:messageId=" + messageId, e);
			}
		}
	}

	/**
	 * Renews date when message was "issued" to now.
	 *
	 * @param messageId the identity of the stored message to renew date on
	 */
	public void messageSetExpiryDate(int messageId, String expiryDate) {
		MessageEntry entry = getMessageEntry(messageId);
		if (entry != null) {
			IStoredMessage iStoredMessage = entry.getMessage();
			try {
				iStoredMessage.messageSetExpiryDate(expiryDate);
				return;
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageSetExpiryDate:messageId=" + messageId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetExpiryDate:messageId=" + messageId, "Invalid messageId");
	}

	/**
	 * Prints the message specified with messageId
	 *
	 */
	public void messagePrint(int messageId, String destination, String sender) {
		MessageEntry entry = getMessageEntry(messageId);
		if (entry != null) {
			try {
				IStoredMessage iStoredMessage = entry.getMessage();
				FaxNumber printNumber = FaxNumber.parse(destination);
				iStoredMessage.print(sender, printNumber, false);
				log.info("Manual fax print successfully triggered from[" + sender + "] to fax destination[" + destination + "]");
				return;
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messagePrint:messageId=" + messageId, e);
			} catch (DataException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messagePrint:messageId=" + messageId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messagePrint:messageId=" + messageId, "Invalid messageId");
	}

	public int[] messageGetContent(int messageId) {
		MessageEntry entry = getMessageEntry(messageId);
		if (entry != null) {
			try {
				IStoredMessage iStoredMessage = entry.getMessage();
				List<IMessageContent> list = iStoredMessage.getContent();
				return entry.putMessageContents(list);
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageGetContent:messageId=" + messageId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetContent:messageId=" + messageId, "Invalid messageId");
	}

	/**
	 * Returns the size of the MessageContent specified with messageContentId.
	 *
	 * @return size in bytes
	 */
	public int messageContentSize(int messageContentId) {
		IMessageContent messageContent = getMessageContent(messageContentId);
		if (messageContent != null) {
			try {
				MediaProperties mediaProperties = messageContent.getMediaProperties();
				if (log.isDebugEnabled()) log.debug("In messageContentSize: MediaProperties " + mediaProperties);
				MimeType mimeType = mediaProperties.getContentType();
				String subType = mimeType.getSubType();
				if (subType.equals("html")) {
					IMediaObject iMediaObject = messageGetMediaObject(messageContentId);
					try {
						String text = MediaUtil.convertMediaObjectToString(iMediaObject);
						text = Detagger.removeHtmlMarkup(text);
						return text.length();
					} catch (IOException e) {
						if (log.isDebugEnabled()) log.debug("Exception in messageContentSize " + e);
					}
				} else {
					return (int) mediaProperties.getSize();
				}
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageContentSize:messageContentId=" + messageContentId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageContentSize:messageContentId=" + messageContentId, "Invalid messageContentId");
	}

	/**
	 * Returns the length of the MessageContent specified with messageContentId.
	 * Length can be retrieved in milliseconds or number of pages.
	 *
	 * @return length in milliseconds or pages. If there is no length associated with this content, -1 is returned.
	 * @throws PlatformAccessException If invalid messageContentId or some error occured in the IMessageContent interface.
	 */
	public int messageContentLength(int messageContentId, String type) {
		IMessageContent messageContent = getMessageContent(messageContentId);
		if (messageContent != null) {
			try {
				MediaProperties mediaProperties = messageContent.getMediaProperties();
				if (log.isDebugEnabled()) log.debug("In messageContentLength: MediaProperties " + mediaProperties);
				if (type.equals(CONTENT_LENGTH_TYPE_MILLISECONDS)) {
					if (mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS)) {
						return (int) mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS);
					} else {
						return -1;
					}
				} else if (type.equals(CONTENT_LENGTH_TYPE_PAGES)) {
					log.debug("messageContentLength Trying to find fax page count in xheaders");
					int numpages=-1;
					MessageEntry msgEntry = getMessageEntryFromContentId(messageContentId);
					if(msgEntry!=null){
						try
						{
							String xheaderFaxPageCount = msgEntry.getMessage().getAdditionalProperty("X-Fax-Pages");
							if(xheaderFaxPageCount!=null)
							{
								numpages = Integer.parseInt(xheaderFaxPageCount);
								log.debug("messageContentLength fax page count from xheaders is : "+numpages);
							}
							else
							{
								log.debug("messageContentLength Not able to get fax page count from xheaders");

							}
						}
						catch(NumberFormatException e)
						{
							log.info("messageContentLength NumberFormatException while trying to get faxpagecount for messageContentId: "+messageContentId);
						}
					}
					else
					{
						log.info("messageContentLength unable to find message for  messageContentId: "+messageContentId);
					}
					return numpages;
				} else {
					throw new PlatformAccessException(EventType.SYSTEMERROR, "messageContentLength:type=" + type, "Invalid length type " + type);
				}
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageContentLength:messageContentId=" + messageContentId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageContentLength:messageContentId=" + messageContentId, "Invalid messageContentId");
	}




	/**
	 * Retrieves the MediaObject of the MessageContent specified with messageContentId.
	 *
	 * @return the MediaObject
	 */
	public IMediaObject messageGetMediaObject(int messageContentId) {
		IMessageContent messageContent = getMessageContent(messageContentId);
		if (messageContent != null) {
			try {
				IMediaObject iMediaObject = messageContent.getMediaObject();
				if (iMediaObject == null) {
					throw new PlatformAccessException(EventType.DATANOTFOUND, "messageGetMediaObject:messageContentId="
							+ messageContentId, "No mediaobject found in the messagecontent");
				}
				return iMediaObject;
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageGetMediaObject:messageContentId=" + messageContentId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetMediaObject:messageContentId=" + messageContentId, "Invalid messageContentId");
	}

	/**
	 * Retrieves a String that represents the MediaProperties of the MessageContent specified with messageContentId.
	 *
	 * @return a String
	 */
	public String messageGetMediaProperties(int messageContentId) {
		IMessageContent messageContent = getMessageContent(messageContentId);
		if (messageContent != null) {
			try {
				MediaProperties mediaProperties = messageContent.getMediaProperties();
				if (log.isDebugEnabled()) log.debug("In messageGetMediaProperties: MediaProperties " + mediaProperties);
				MimeType mimeType = mediaProperties.getContentType();
				return mimeType.toString();
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageGetMediaProperties:messageContentId=" + messageContentId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetMediaProperties:messageContentId=" + messageContentId, "Invalid messageContentId");
	}

	/**
	 * Retrieves the MediaObject used as SpokenName in the message specified with messageId
	 *
	 * @return SpokenName
	 */
	public IMediaObject messageGetSpokenNameOfSender(int messageId) {
		MessageEntry entry = getMessageEntry(messageId);
		if (entry != null) {
			try {
				IStoredMessage iStoredMessage = entry.getMessage();
				IMediaObject iMediaObject = iStoredMessage.getSpokenNameOfSender();
				if (iMediaObject == null) {
					throw new PlatformAccessException(EventType.DATANOTFOUND,
							"messageGetSpokenNameOfSender:messageId=" + messageId,
							" SpokenName of the sender not found (which is plausible), method will return a DataNotFound to inform");
				}
				return iMediaObject;
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageGetSpokenNameOfSender:messageId=" + messageId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetSpokenNameOfSender:messageId=" + messageId, "Invalid messageId");
	}

	/* ---------------- MESSAGE SENDER STARTS HERE --------------- */

	/**
	 * Creates a IStorableMessage and allocates a storableMessageId.
	 *
	 * @return storableMessageId for the new IStorableMessage
	 */
	public int messageCreateNew() {
		try {
			IStorableMessage iStorableMessage = iStorableMessageFactory.create();
			int storableMessageId = generateStorableMessageId();
			storableMessages.put(storableMessageId, iStorableMessage);
			return storableMessageId;
		} catch (MailboxException e) {
			throw new PlatformAccessException(EventType.MAILBOX, "messageCreateNew", e);
		}
	}

	/**
	 * Sets a property on the StorableMessage specified with the storableMessageId.
	 *
	 * @param propertyName      Must be a name defined in Mailbox-FS
	 * @param propertyValue     If single value the first element is used.
	 */
	public void messageSetStorableProperty(int storableMessageId, String propertyName, String[] propertyValue, SubscriberProfileManager subscriberProfileManager) {
		IStorableMessage iStorableMessage = getStorableMessage(storableMessageId);
		if (iStorableMessage == null) {
			if (log.isDebugEnabled()) log.debug("In messageSetStorableProperty iStorableMessage is null");
			throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStorableProperty:storableMessageId=" + storableMessageId,
					"Invalid storableMessageId");
		}
		if (propertyName == null) {
			if (log.isDebugEnabled()) log.debug("In messageSetStorableProperty propertyName is null");
			throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStorableProperty:storableMessageId=" + storableMessageId,
					"Invalid propertyName null. propertyValue was: "+propertyValue);
		}
		if (propertyValue == null) {
			if (log.isDebugEnabled()) log.debug("In messageSetStorableProperty propertyValue is null");
			throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStorableProperty:storableMessageId=" + storableMessageId,
					"Invalid propertyValue null. propertyName was: "+propertyName);
		}

		if (log.isDebugEnabled()) log.debug("messageSetStorableProperty " + propertyName + "=" + Arrays.asList(propertyValue));

		if (propertyName.equals(MSG_PROPERTY_SENDER)) {
			iStorableMessage.setSender(propertyValue[0]);
		} else if (propertyName.equals(MSG_PROPERTY_RECIPIENTS)) {
			setStorablePropertyRecipient(storableMessageId, propertyValue, iStorableMessage);
		} else if(propertyName.equals(MSG_PROPERTY_SUBSCRIBERID)){
			setStorablePropertySubscriberId(iStorableMessage, storableMessageId, propertyValue, subscriberProfileManager);
		} else if (propertyName.equals(MSG_PROPERTY_SECONDARYRECIPIENTS)) {
			iStorableMessage.setSecondaryRecipients(propertyValue);
		} else if (propertyName.equals(MSG_PROPERTY_SUBJECT)) {
			iStorableMessage.setSubject(propertyValue[0]);
		} else if (propertyName.equals(MSG_PROPERTY_REPLYTOADDR)) {
			iStorableMessage.setReplyToAddress(propertyValue[0]);
		} else if (propertyName.equals(MSG_PROPERTY_TYPE)) {
			iStorableMessage.setType(MessageTypeUtil.stringToMessageType(propertyValue[0]));
		} else if (propertyName.equals(MSG_PROPERTY_LANGUAGE)) {
			iStorableMessage.setLanguage(propertyValue[0]);
		} else if (propertyName.equals(MSG_PROPERTY_DELIVERYDATE)) {
			try {
				iStorableMessage.setDeliveryDate(TimeUtil.parseVvaTime(propertyValue[0]));
			} catch (ParseException e) {
				throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStorableProperty:storableMessageId=" + storableMessageId, e);
			}
		} else if (propertyName.equals(MSG_PROPERTY_URGENT)) {
			iStorableMessage.setUrgent(propertyValue[0].equals("true"));
		} else if (propertyName.equals(MSG_PROPERTY_CONFIDENTIAL)) {
			iStorableMessage.setConfidential(propertyValue[0].equals("true"));
		} else if (propertyName.equals(MSG_PROPERTY_SENDERVISIBILITY)) {
			iStorableMessage.setSenderVisibility(propertyValue[0]);
		} else {
			//throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStorableProperty:storableMessageId=" + storableMessageId,
			//        "Invalid propertyName " + propertyName);

			// Now allowing any (other) String value property to be stored in the State File 
			iStorableMessage.setAdditionalProperty(propertyName, propertyValue[0]);
		}
	}

	private void setStorablePropertyRecipient(int storableMessageId, String[] propertyValue, IStorableMessage iStorableMessage) {
		Set<String> mailHosts = getStorableMessagesMailHosts(storableMessageId);
		for (String recipient : propertyValue) {
			iStorableMessage.addRecipient(recipient);
		}
		mailHosts.add("");  // the empty string symbolizes that we don't know the mailhost
	}

	/**
	 * @param storableMessageId Not used 
	 * @param subscriberProfileManager Not used
	 */
	private void setStorablePropertySubscriberId(IStorableMessage iStorableMessage, int storableMessageId, String[] propertyValue, SubscriberProfileManager subscriberProfileManager) {
		for (String subscriberId : propertyValue) {
			iStorableMessage.addRecipient(subscriberId);
		}
	}

	private Set<String> getStorableMessagesMailHosts(int storableMessageId) {
		Set<String> mailHosts = storableMessagesMailHosts.get(storableMessageId);
		if(mailHosts == null){
			mailHosts = new HashSet<String>();
			storableMessagesMailHosts.put(storableMessageId, mailHosts);
		}
		return mailHosts;
	}

	/**
	 * Gets a property from the StorableMessage specified with storableMessageId.
	 *
	 * @return value(s) for the property
	 */
	public String[] messageGetStorableProperty(int storableMessageId, String propertyName) {
		IStorableMessage iStorableMessage = getStorableMessage(storableMessageId);
		if (iStorableMessage == null) {
			throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetStorableProperty:storableMessageId=" +
					storableMessageId, "Invalid storableMessageId");
		}

		if (propertyName.equals(MSG_PROPERTY_SENDER)) {
			String value = iStorableMessage.getSender();
			if (value != null) {
				return new String[]{value};
			}
		} else if (propertyName.equals(MSG_PROPERTY_RECIPIENTS)) {
			String[] value = iStorableMessage.getRecipients();
			if (value != null) {
				return value;
			}
		} else if (propertyName.equals(MSG_PROPERTY_SECONDARYRECIPIENTS)) {
			String[] value = iStorableMessage.getSecondaryRecipients();
			if (value != null) {
				return value;
			}
		} else if (propertyName.equals(MSG_PROPERTY_SUBJECT)) {
			String value = iStorableMessage.getSubject();
			if (value != null) {
				return new String[]{value};
			}
		} else if (propertyName.equals(MSG_PROPERTY_REPLYTOADDR)) {
			String value = iStorableMessage.getReplyToAddress();
			if (value != null) {
				return new String[]{value};
			}
		} else if (propertyName.equals(MSG_PROPERTY_TYPE)) {
			MailboxMessageType value = iStorableMessage.getType();
			if (value != null) {
				return new String[]{MessageTypeUtil.messageTypeToString(value)};
			}
		} else if (propertyName.equals(MSG_PROPERTY_LANGUAGE)) {
			String value = iStorableMessage.getLanguage();
			if (value != null) {
				return new String[]{value};
			}
		} else if (propertyName.equals(MSG_PROPERTY_DELIVERYDATE)) {
			Date value = iStorableMessage.getDeliveryDate();
			if (value != null) {
				return new String[]{TimeUtil.dateToVvaTime(value)};
			}
		} else if (propertyName.equals(MSG_PROPERTY_URGENT)) {
			return new String[]{iStorableMessage.isUrgent() ? "true" : "false"};
		} else if (propertyName.equals(MSG_PROPERTY_CONFIDENTIAL)) {
			return new String[]{iStorableMessage.isConfidential() ? "true" : "false"};
		} else {
			throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetStorableProperty:propertyName="
					+ propertyName, "Invalid propertyName " + propertyName);
		}
		//No value found for the property
		throw new PlatformAccessException(EventType.DATANOTFOUND, "messageGetStorableProperty:propertyName="
				+ propertyName, "No value found");
	}

	/**
	 * Stores a StorableMessage specified with the storableMessageId.
	 *
	 * @param storableMessageId the ID of the message to store
	 * @param trafficEventManager the trafficEventManager that is used to send mdr
	 * @return A list of recipients for which message storing failed
	 */
	public String[] messageStore(int storableMessageId, TrafficEventManager trafficEventManager) {
		String[] failedRecipients = new String[0];

		IStorableMessage iStorableMessage = getStorableMessage(storableMessageId);

		if (iStorableMessage != null) {
			try {
				// If there is only one (non-empty) mailhost, store in that mailhost
				String mailHost = "";

				Set<String> mailHosts = storableMessagesMailHosts.get(storableMessageId);

				if (mailHosts != null && mailHosts.size() == 1) {
					mailHost = mailHosts.iterator().next();
				}

				if (mailHost.length() > 0) {
					iStorableMessage.store(mailHost);
				} else {
					iStorableMessage.store();
				}

				// send mdr
				sendMessageStatusMdr(iStorableMessage, MdrConstants.STORE, trafficEventManager);

			} catch (MailboxException e) {
				// There was an error, but if we have a list of failed recipients return it
				// instead of considering this an exceptional case.
				failedRecipients = e.getFailedRecipients();

				if (failedRecipients == null || failedRecipients.length == 0) {
					throw new PlatformAccessException(EventType.MAILBOX, "messageStore:storableMessageId=" + storableMessageId, e);
				}
			}
		} else {
			throw new PlatformAccessException(EventType.SYSTEMERROR, "messageStore:storableMessageId=" + storableMessageId,
					"Invalid storableMessageId");
		}

		return failedRecipients;
	}

	/**
	 * Creates a StorableMessage from the StoredMessage specified with messageId.
	 *
	 * @param messageId for the StoredMessage
	 * @return storableMessageId for the new IStorableMessage
	 */
	public int messageForward(int messageId) {
		MessageEntry entry = getMessageEntry(messageId);
		if (entry != null) {
			IStoredMessage iStoredMessage = entry.getMessage();
			try {
				IStorableMessage iStorableMessage = iStoredMessage.forward();
				int storableMessageId = generateStorableMessageId();
				storableMessages.put(storableMessageId, iStorableMessage);
				return storableMessageId;
			} catch (MailboxException e) {
				throw new PlatformAccessException(EventType.MAILBOX, "messageForward:messageId=" + messageId, e);
			}
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageForward:messageId=" + messageId, "Invalid messageId");
	}

	/**
	 * Sets spokenname on the StorableMessage specified with messageId.
	 *
	 * @param spokenName        MediaObject used for spokenname
	 */
	public void messageSetSpokenNameOfSender(int storableMessageId, IMediaObject spokenName, String description,
			String fileName, String language) {
		IStorableMessage iStorableMessage = getStorableMessage(storableMessageId);
		if (iStorableMessage != null) {
			iStorableMessage.setSpokenNameOfSender(spokenName, new MessageContentProperties(fileName, description, language, null));
			return;
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetSpokenNameOfSender:storableMessageId=" +
				storableMessageId, "Invalid storableMessageId");
	}

	/**
	 * Adds a MediaObject to the StorableMessage specified with messageId.
	 *
	 * @param mediaObject       to add
	 */
	public void messageAddMediaObject(int storableMessageId, IMediaObject mediaObject, String description,
			String fileName, String language, String duration, boolean appendContent) {
		IStorableMessage iStorableMessage = getStorableMessage(storableMessageId);
		if (iStorableMessage != null) {
			iStorableMessage.addContent(mediaObject, new MessageContentProperties(fileName, description, language, duration), appendContent);
			return;
		}
		throw new PlatformAccessException(EventType.SYSTEMERROR, "messageAddMediaObject:storableMessageId=" +
				storableMessageId, "Invalid storableMessageId");
	}

	/**
	 * Internal function to retrieve the StorableMessage specified with storableMessageId
	 *
	 * @return IStorableMessage
	 */
	private IStorableMessage getStorableMessage(int storableMessageId) {
		if (!storableMessages.containsKey(storableMessageId)) {
			return null;
		}
		return storableMessages.get(storableMessageId);
	}

	/**
	 * Internal function to retrieve the MessageEntry specified with messageId
	 *
	 * @return MessageEntry
	 */
	private MessageEntry getMessageEntry(int messageId) {
		if (!messageIdToMessageListId.containsKey(messageId)) {
			return null;
		}
		int messageListId = messageIdToMessageListId.get(messageId);
		int folderId = messageListIdToFolderId.get(messageListId);
		int mailboxId = folderIdToMailboxId.get(folderId);
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {
			FolderEntry folderEntry = mailboxEntry.getFolderEntry(folderId);
			if (folderEntry != null) {
				MessageListEntry messageListEntry = folderEntry.getMessageListEntry(messageListId);
				if (messageListEntry != null) {
					return messageListEntry.getMessageEntry(messageId);
				}
			}
		}
		return null;
	}

	/**
	 * Internal function to retrieve the IMessageContent specified with messageContentId
	 *
	 * @return IMessageContent
	 */
	private IMessageContent getMessageContent(int messageContentId) {
		if (!messageContentIdToMessageId.containsKey(messageContentId)) {
			return null;
		}
		int messageId = messageContentIdToMessageId.get(messageContentId);
		int messageListId = messageIdToMessageListId.get(messageId);
		int folderId = messageListIdToFolderId.get(messageListId);
		int mailboxId = folderIdToMailboxId.get(folderId);
		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {
			FolderEntry folderEntry = mailboxEntry.getFolderEntry(folderId);
			if (folderEntry != null) {
				MessageListEntry messageListEntry = folderEntry.getMessageListEntry(messageListId);
				if (messageListEntry != null) {
					MessageEntry entry = messageListEntry.getMessageEntry(messageId);
					if (entry != null) {
						return entry.getMessageContent(messageContentId);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Internal function to retrieve the MessageEntry specified with a specific contentId
	 *
	 * @return MessageEntry
	 */
	private MessageEntry getMessageEntryFromContentId(int messageContentId) {
		if (!messageContentIdToMessageId.containsKey(messageContentId)) {
			log.debug("getMessageEntryFromContentId Found message for messageContentId: "+messageContentId);
			return null;
		}
		int messageId = messageContentIdToMessageId.get(messageContentId);
		if (!messageIdToMessageListId.containsKey(messageId)) {
			log.debug("getMessageEntryFromContentId Found message for messageId: "+messageId);
			return null;
		}
		int messageListId = messageIdToMessageListId.get(messageId);
		int folderId = messageListIdToFolderId.get(messageListId);
		int mailboxId = folderIdToMailboxId.get(folderId);

		MailboxEntry mailboxEntry = mailBoxes.get(mailboxId);
		if (mailboxEntry != null) {
			FolderEntry folderEntry = mailboxEntry.getFolderEntry(folderId);
			if (folderEntry != null) {
				MessageListEntry messageListEntry = folderEntry.getMessageListEntry(messageListId);
				if (messageListEntry != null) {
					return messageListEntry.getMessageEntry(messageId);
				}
			}
		}
		return null;
	}


	private Integer generateMailboxId() {
		return mailBoxId++;
	}

	private Integer generateFolderId() {
		return folderId++;
	}

	private Integer generateMessageListId() {
		return messageListId++;
	}

	private Integer generateMessageId() {
		return messageId++;
	}

	private Integer generateMessageContentId() {
		return messageContentId++;
	}

	private Integer generateStorableMessageId() {
		return storableMessageId++;
	}

	/**
	 * Base class for the Entry objects that are stored in the internal Map's
	 * Contains an objectId as reference to itself
	 */
	class ObjectEntry {
		protected int objectId;

		/**
		 * Constructor
		 *
		 */
		ObjectEntry(int objectId) {
			this.objectId = objectId;
		}

		/**
		 * @return int objectId
		 */
		int getObjectId() {
			return objectId;
		}
	}

	/**
	 * Class that holds an IMailbox object and a Map of FolderEntry objects connected to current IMailbox
	 */
	class MailboxEntry extends ObjectEntry {

		private HashMap<Integer, FolderEntry> folders;
		private IMailbox iMailbox;
		private HashMap<QuotaName, Integer> perTypeQuota;
		/**
		 * cached message usage
		 */
		private int messageUsage = -1;

		MailboxEntry(IMailbox iMailbox, int objectId) {
			super(objectId);
			this.iMailbox = iMailbox;
			folders = new HashMap<Integer, FolderEntry>();
			perTypeQuota = new HashMap<QuotaName, Integer>();
		}

		FolderEntry getFolderEntry(int folderId) {
			return folders.get(folderId);
		}

		FolderEntry getFolderEntry(String name) {
			Iterator<Integer> it = folders.keySet().iterator();
			while (it.hasNext()) {
				FolderEntry fe = folders.get(it.next());
				IFolder f = fe.getFolder();
				if (f.getName().equals(name)) return fe;
			}
			return null;
		}

		int putFolder(IFolder iFolder) {
			int id = generateFolderId();
			folders.put(id, new FolderEntry(iFolder, id));
			folderIdToMailboxId.put(id, objectId);
			return id;
		}

		/**
		 * @return the IMailbox object
		 */
		IMailbox getMailbox() {
			return iMailbox;
		}

		/**
		 * Stores the cached message usage value
		 *
		 */
		void setMessageUsage(int messageUsage) {
			this.messageUsage = messageUsage;
		}

		/**
		 * Retrieves the cached message usage value
		 *
		 * @return message usage value
		 */
		int getMessageUsage() {
			return messageUsage;
		}

		/**
		 * Stores the cached per type message usage value
		 */
		void setMessageUsage(QuotaName name, int messageUsage) {
			perTypeQuota.put(name, new Integer(messageUsage));
		}

		/**
		 * Retrieves the cached per type message usage value
		 *
		 * @return message usage value
		 */
		int getMessageUsage(QuotaName name) {
			Integer quota = perTypeQuota.get(name);
			if(quota != null) {
				return quota.intValue();
			} else {
				return -1;
			}
		}

	}

	/**
	 * Class that holds an IFolder object and a Map of MessageListEntry objects connected to current IFolder
	 */
	class FolderEntry extends ObjectEntry {
		private HashMap<Integer, MessageListEntry> messageLists;
		private IFolder iFolder;

		FolderEntry(IFolder iFolder, int objectId) {
			super(objectId);
			this.iFolder = iFolder;
			messageLists = new HashMap<Integer, MessageListEntry>();
		}

		int putMessageList(List<IStoredMessage> list) {
			int id = generateMessageListId();
			messageLists.put(id, new MessageListEntry(list, id));
			messageListIdToFolderId.put(id, objectId);
			return id;
		}

		MessageListEntry getMessageListEntry(int messageListId) {
			return messageLists.get(messageListId);
		}

		IFolder getFolder() {
			return iFolder;
		}
	}

	/**
	 * Class that holds an List<IStoredMessage> object and a Map of MessageEntry objects connected to current IFolder
	 */
	class MessageListEntry extends ObjectEntry {
		private int[] messageIds;
		private HashMap<Integer, MessageEntry> messages;
		private List<IStoredMessage> list;

		MessageListEntry(List<IStoredMessage> list, int objectId) {
			super(objectId);
			this.list = list;
			messages = new HashMap<Integer, MessageEntry>();
			putMessages(list);
		}

		int[] putMessages(List<IStoredMessage> messages) {
			int[] ids = new int[messages.size()];
			int i = 0;
			Iterator<IStoredMessage> it = messages.iterator();
			while (it.hasNext()) {
				ids[i++] = putMessage(it.next());
			}
			messageIds = ids;
			return ids;
		}

		int putMessage(IStoredMessage message) {
			int id = generateMessageId();
			messages.put(id, new MessageEntry(message, id));
			messageIdToMessageListId.put(id, objectId);
			return id;
		}

		List<IStoredMessage> getMessageList() {
			return list;
		}

		MessageEntry getMessageEntry(int messageId) {
			return messages.get(messageId);
		}

		int[] getMessageIds() {
			return messageIds;
		}
	}

	/**
	 * Class that holds an IStoredMessage object and a Map of IMessageContent objects connected to current IStoredMessage
	 */
	class MessageEntry extends ObjectEntry {
		private HashMap<Integer, IMessageContent> messageContents;
		private IStoredMessage iStoredMessage;

		MessageEntry(IStoredMessage iStoredMessage, int objectId) {
			super(objectId);
			this.iStoredMessage = iStoredMessage;
			messageContents = new HashMap<Integer, IMessageContent>();
		}

		IStoredMessage getMessage() {
			return iStoredMessage;
		}

		IMessageContent getMessageContent(int messageContentId) {
			return messageContents.get(messageContentId);
		}

		int putMessageContent(IMessageContent messageContent) {
			int id = generateMessageContentId();
			messageContents.put(id, messageContent);
			messageContentIdToMessageId.put(id, objectId);
			return id;
		}

		int[] putMessageContents(List<IMessageContent> messageContents) {
			int[] ids = new int[messageContents.size()];
			int i = 0;
			Iterator<IMessageContent> it = messageContents.iterator();
			while (it.hasNext()) {
				ids[i++] = putMessageContent(it.next());
			}
			return ids;
		}
	}

	/*****
    IGroup backEndGroup = null;
    public boolean isMessageUndeleteEnabled() {
    	if (this.backEndGroup == null) {
    		loadBackendConfig();
    	}
        if (this.backEndGroup == null) { 
        	log.error("MailboxManager.isMessageUndeleteEnabled(): failed to load backend.conf");
        	return false;
        }
        try {
        	boolean enabled = backEndGroup.getBoolean(ConfigParam.ENABLE_MSG_UNDELETE);
            log.debug("MailboxManager.isMessageUndeleteEnabled(): Cm.enableMessageUndeelte in backend.conf is set to false; featur edisabled");
            return enabled;
        } catch (Exception e) {
            log.error("MailboxManager.isMessageUndeleteEnabled(): failed to get value for " + ConfigParam.ENABLE_MSG_UNDELETE + ": " + e, e);
            return false;
        }

    }

    private void loadBackendConfig() {
        try {
            this.backEndGroup = CommonOamManager.getInstance().getConfiguration().getGroup("backend.conf");
        } catch (Exception e) {
            log.warn("MailboxManager.isMessageUndeleteEnabled(): exception calling CommonOamManager.getInstance().getConfiguration().getGroup(GlobalDirectoryAccessUtil.BACK_END_CONF); " +
                     "will try to load backend.conf: " + e);
        }
        if (this.backEndGroup != null) return;

        Collection<String> configFilenames = new LinkedList<String>();
        String configFilename = "/opt/moip/config/backend/backend.conf";
        File backendXml = new File(configFilename);
        if (!backendXml.exists()) {
            log.error("PlatformAccessImpl.loadBackendConfig(): cannot find " + configFilename);
            return;
        }

        configFilenames.add(configFilename);
        IConfiguration configuration;
        try {
            configuration = new ConfigurationImpl(null,configFilenames,false);
            this.backEndGroup = configuration.getGroup("backend.conf");
        } catch (Exception e) {
            log.error("MailboxManager.loadBackendConfig(): error trying to init CommonMessagingAccess with backend.conf: " + e, e);
        } 

    }
	 ************/

	PropertyFileConfigManagerGen2 props = null;
	public boolean isMessageUndeleteEnabled() {
		if (this.props == null) {
			this.props = PropertyFileConfigManagerGen2.getInstance();
			this.props.loadConfigFile(ConfigParam.MSG_UNDELETE_PROP_FILE, true, null, null);
		}
		return this.props.getBooleanValue(ConfigParam.ENABLE_MSG_UNDELETE);
	}


}
