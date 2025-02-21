/**
COPYRIGHT (C) ABCXYZ INTERNET APPLICATIONS INC.

THIS SOFTWARE IS FURNISHED UNDER A LICENSE ONLY AND IS
PROPRIETARY TO ABCXYZ INTERNET APPLICATIONS INC. IT MAY NOT BE COPIED
EXCEPT WITH THE PRIOR WRITTEN PERMISSION OF ABCXYZ INTERNET APPLICATIONS
INC.  ANY COPY MUST INCLUDE THE ABOVE COPYRIGHT NOTICE AS
WELL AS THIS PARAGRAPH.  THIS SOFTWARE OR ANY OTHER COPIES
TO ANY OTHER PERSON OR ENTITY.
TITLE TO AND OWNERSHIP OF THIS SOFTWARE SHALL AT ALL
TIMES REMAIN WITH ABCXYZ INTERNET APPLICATIONS INC.
 */
package com.abcxyz.services.moip.mrdhandler;


import java.io.File;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.StringTokenizer;

import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.MessageIdentifier;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.PropertyFileConfigManagerGen2;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.message.MfsStateFolderType;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.InformEventResult;
import com.abcxyz.messaging.mrd.data.InformEventType;
import com.abcxyz.messaging.mrd.data.Reason;
import com.abcxyz.messaging.mrd.data.RecipientMsa;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.messaging.mrd.operation.InformEventReq;
import com.abcxyz.messaging.mrd.operation.InformEventResp;
import com.abcxyz.messaging.mrd.operation.MsgServerOperations;
import com.abcxyz.messaging.mrd.util.COSRetentionDaysChangedEvent;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.cmnaccess.UserInbox;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.mdr.MdrConstants;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.abcxyz.services.moip.provisioning.businessrule.ProvisioningConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.ITrafficEventSender;
import com.mobeon.common.trafficeventsender.MfsClient;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventMdrHandler;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.common.util.FaxNumber;
import com.mobeon.masp.callmanager.SubscribeCall;
import com.mobeon.masp.callmanager.notification.OutboundNotification;
import com.mobeon.masp.execution_engine.platformaccess.AutoProvProfExpiryScheduler;
import com.mobeon.masp.execution_engine.platformaccess.EventType;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessException;
import com.mobeon.masp.execution_engine.platformaccess.SubscriptionExpiryScheduler;
import com.mobeon.masp.execution_engine.platformaccess.util.MessageTypeUtil;
import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.IMailboxAccountManager;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.mailbox.mfs.MfsUtil;


/**
 *
 * This class is use to handle request from MRD for the destination mas.
 * The destination can be change by calling setMsgServerClass before passing this
 * class to the MRDListener
 * @author esebpar
 * @since MIO2.0
 *
 */
public class VMEventHandler extends MsgServerOperations{
	private static ILogger log = ILoggerFactory.getILogger(VMEventHandler.class);

	private IMailboxAccountManager mailboxAccountManager;
	private MfsEventManager mfsEventManager = null;
	private ITrafficEventSender trafficEventSender;
	private String msgServerClass;

	private static String DATETIME_PARAMNAME = "Date-time";
	private static String TO_PARAMNAME = "To";

	public VMEventHandler(){
		msgServerClass = MoipMessageEntities.MESSAGE_SERVICE_MAS;
		if(log.isDebugEnabled()){
			log.debug("VMEventHandler: Creating the a message server to " +
					"received MRD request for the destination message class " +
					"of " + msgServerClass);
		}
		mfsEventManager = MfsEventFactory.getMfsEvenManager();
	}

	/**
	 * Inject session factory.
	 */
	public void setMailboxAccountManager(IMailboxAccountManager mailboxAccountManager) {
		this.mailboxAccountManager = mailboxAccountManager;
	}

	public void setTrafficEventSender(ITrafficEventSender trafficEventSender) {
		this.trafficEventSender = trafficEventSender;
	}
	/*
    public void setMfsClient(MfsClient mfsClient) {
        this.mfsClient = mfsClient;
    }
	 */

	/**
	 * Setting the messaging server class, this value, is use for the routing by
	 * MRD.
	 */
	public void setMsgServerClass(String msgServerClass) {
		this.msgServerClass = msgServerClass;
	}

	/**
	 * Return the messaging server class.
	 * @Return the messaging server class.
	 */
	@Override
	public String getMsgServerClass() {
		return msgServerClass;
	}


	public void init(){
		verifyResources();
	}

	/**
	 * Verify if every mandatory parameters have been set
	 */
	private void verifyResources() throws MissingResourceException {
		if(mailboxAccountManager == null){
			throw new MissingResourceException("mailboxFactory is null. Must be injected.",
					"IMailboxAccountManager", "MailboxAccountManager");
		}
	}

	/**
	 * This is the call back use in this class, this call back, make it
	 * possible to receive inform event request from MRD
	 */
	@Override
	public InformEventResp informEvent(InformEventReq req){
		InformEventResp resp = null ;
		if(log.isDebugEnabled()){
			log.debug("VMEventHandler.informEvent: Processing a new informEvent request.");
		}
		if(req != null){
			if(log.isDebugEnabled()){
				log.debug("VMEventHandler.informEvent: Received an informEvent request. req:" + req.toString());
			}

			String accessPoint = "OutsideVM";
			if(req.origMsgClass != null && req.origMsgClass.value != null){
				accessPoint = req.origMsgClass.value;
			}

			if (req.rMsa != null && req.rMsa.value != null && !req.rMsa.value.isEmpty()) {
				MSA rMsa = new MSA(req.rMsa.value);
				log.debug("VMEventHandler.informEvent: Subscriber: " + rMsa.toString());
				if(!rMsa.isInternal()){
					//return the recipient have to be internal, only the omsa can be external in those case
					if(log.isDebugEnabled()){
						log.debug("VMEventHandler.informEvent: The subscriber is external, must be internal");
					}
					return new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, Reason.BAD_REQUEST_400 + " - RMSA is external to the system, must be internal");
				}else{
					IDirectoryAccessSubscriber subscriberProfile = DirectoryAccess.getInstance().lookupSubscriber(rMsa.toString());
					if(subscriberProfile == null){
						if(log.isDebugEnabled()){
							log.debug("VMEventHandler.informEvent: The subscriber doesn't exist return permanent fail reason user profile 1002");
						}
						return new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, Reason.USER_PROFILE_1002);
					}
				}
			}else{
				//The RMSA is a mandatory attribute of the inform event. See InformEvent SEC 
				if(log.isDebugEnabled()){
					log.debug("VMEventHandler.informEvent: Missing the RMSA value inside the inform event request.");
				}
				return new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, Reason.BAD_REQUEST_400 + " - Missing mandatory field RMSA");
			}

			if(InformEventType.MSG_READ.equals(req.informEventType.value)){
				try {
					setMessageState(new MessageIdentifier(new MSA(req.oMsa.value), new MSA(req.rMsa.value), req.oMsgID.value, req.rMsgID.value), req.msgFolder != null ? req.msgFolder.value : null, StoredMessageState.READ, accessPoint);
				} catch (MailboxException e) {
					log.error("VMEventHandler.informEvent: Could not change message state to state " + InformEventType.MSG_READ, e);
					resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when trying to access the Message Store");
				}
			}else if(InformEventType.MSG_TO_BE_DELETED.equals(req.informEventType.value) || InformEventType.MSG_DELETED.equals(req.informEventType.value)){
				//try {
				//    setMessageState(new MessageIdentifier(new MSA(req.oMsa.value), new MSA(req.rMsa.value), req.oMsgID.value, req.rMsgID.value), req.msgFolder != null ? req.msgFolder.value : null, StoredMessageState.DELETED, accessPoint);
				//} catch (MailboxException e) {
				//    log.error("VMEventHandler.informEvent: Could not delete message", e);
				//    resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when trying to access the Message Store");
				//}
				// HX98472 
				resp = handleMsgDeleteEvent(req, accessPoint);
			}else if(InformEventType.MSG_COPIED_TO.equals(req.informEventType.value)){
				try {
					updateMessageState(new MessageIdentifier(new MSA(req.oMsa.value), new MSA(req.rMsa.value), req.oMsgID.value, req.rMsgID.value), req.msgFolder != null ? req.msgFolder.value : null, accessPoint);
				} catch (MailboxException e) {
					log.error("VMEventHandler.informEvent: Could not update the message state.", e);
					resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when trying to access the Message Store");
				}
			}else if(InformEventType.NEWARRIVAL.equals(req.informEventType.value)){
				//If trash deal as if it is a copied message
				if(req.msgFolder.value != null && req.msgFolder.value.toLowerCase().equals("trash")){
					try {
						updateMessageState(new MessageIdentifier(new MSA(req.oMsa.value), new MSA(req.rMsa.value), req.oMsgID.value, req.rMsgID.value), req.msgFolder != null ? req.msgFolder.value : null, accessPoint);
					} catch (MailboxException e) {
						log.error("VMEventHandler.informEvent: Could not update the message state." + InformEventType.MSG_READ, e);
						resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when trying to access the Message Store");
					}
				}else{
					try {
						MessageIdentifier msgId = informEventReqToMessageIdentifier(req);
						IStoredMessage iStoredMessage = setMessageState(msgId,
								(req.msgFolder != null ? req.msgFolder.value : null), StoredMessageState.NEW, accessPoint);

						String tel = lookupTelForRMSA(req.rMsa);
						CommonMessagingAccess.getInstance().informNtf(EventTypes.DELIVERY, new MessageInfo(msgId), tel, null);

						//If the message is a fax check if the fax should be auto printed
						if (isFaxMessage(iStoredMessage, (req.msgFolder != null ? req.msgFolder.value : null))) {
							sendFaxAutoPrint(iStoredMessage, msgId, req, tel);
						}
					} catch (MailboxException e) {
						log.error("VMEventHandler.informEvent: Could create a new message in side the folder: " + (req.msgFolder != null && !req.msgFolder.value.equals("")? req.msgFolder.value : "inbox"), e);
						resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when trying to access the Message Store");
					}
				}
			}
			else if(InformEventType.SUBSCRIBER_ACTIVITY_DETECTED.equals(req.informEventType.value)){
				// Forward the event to NTF
				MessageIdentifier msgId = informEventReqToMessageIdentifier(req);
				String tel = lookupTelForRMSA(req.rMsa);
				CommonMessagingAccess.getInstance().informNtf(EventTypes.SUBSCRIBER_ACTIVITY_DETECTED, new MessageInfo(msgId), tel, null);
			}
			else if(InformEventType.SUBSCRIBER_VVM_SYSTEM_DEACTIVATED.equals(req.informEventType.value)){
				// Forward the event to NTF
				MessageIdentifier msgId = informEventReqToMessageIdentifier(req);
				String tel = lookupTelForRMSA(req.rMsa);
				CommonMessagingAccess.getInstance().informNtf(EventTypes.SUBSCRIBER_VVM_SYSTEM_DEACTIVATED, new MessageInfo(msgId), tel, null);
			}
			else if(InformEventType.SUBSCRIBER_IMAP_ACTIVITY_DETECTED.equals(req.informEventType.value)){
			    // Forward the event to NTF
			    MessageIdentifier msgId = informEventReqToMessageIdentifier(req);
			    String tel = lookupTelForRMSA(req.rMsa);
			    CommonMessagingAccess.getInstance().informNtf(EventTypes.SUBSCRIBER_VVM_IMAP_ACTIVITY_DETECTED, new MessageInfo(msgId), tel, null);
			}
			else if("UserSessionEnd".equals(req.informEventType.value)){
				//Don't add the UserSessionEnd inside the InformEventType, this is not a generic event that should be inside the InformEventType

				MessageInfo msgInfo = new MessageInfo();
				msgInfo.rmsa = new MSA(req.rMsa.value);
				//get subscriber phone number

				IDirectoryAccessSubscriber profile = CommonMessagingAccess.getInstance().getMcd().lookupSubscriber(msgInfo.rmsa.toString());//(msgInfo.rmsa.toString());
				if (profile != null)
				{
					String tel = profile.getSubscriberIdentity("tel");
					if(tel!=null)
					{
						Properties  properties = new Properties();
						properties.put("force", "false");
						properties.put("mailboxId", tel);
						properties.put("callednumber", tel);
						properties.put("vvmNotify", "false");
						CommonMessagingAccess.getInstance().notifyNtf(EventTypes.MAILBOX_UPDATE, msgInfo, tel, properties);
					}
					else
					{
						log.error("VMEventHandler.informEvent: Can't find tel number for identity: " +req.rMsa.value);
					}
				}
				else
				{
					log.error("VMEventHandler.informEvent: Can't find subscriber for identity: " +req.rMsa.value);
				}
			}else if ("NotifyNewMove".equals(req.informEventType.value)) {
				if(log.isDebugEnabled()) log.debug("VMEventHandler.informEvent: get NotifyNewMove: req="+req.toString());
				resp = handleNotifyNewMsg(req);

			} else if ("ScheduleExpiry".equals(req.informEventType.value)) {
				if(log.isDebugEnabled()) log.debug("VMEventHandler informEvent ScheduleExpiry "+ req.toString());
				resp = handleScheduleExpiry(req);

			} else if ("SlamdownEvent".equals(req.informEventType.value)) {
				if(log.isDebugEnabled()) log.debug("VMEventHandler informEvent SLAMDOWN "+ req.toString());
				resp = handleSlamdownEvent(req);

            } else if ("ScheduleAutodeletion".equals(req.informEventType.value)) {
                if(log.isDebugEnabled()) log.debug("VMEventHandler informEvent ScheduleAutodeletion "+ req.toString());
                resp = handleScheduleAutodeletion(req);

			}else if("SipMwiExpire".equals(req.informEventType.value)) {
				if(log.isDebugEnabled()){
					log.debug("VMEventHandler.informEvent: Received a sipmwi expire inform event.");
				}
				if(req.informEventProp != null && req.informEventProp.value != null) {
					String[] mwiPrivateFiles;
					if(req.informEventProp.value.contains(";")) {
						mwiPrivateFiles = req.informEventProp.value.split(";");
					}else {
						mwiPrivateFiles = new String[] { req.informEventProp.value };
					}

					if(mwiPrivateFiles != null) {
						for(String mwiPrivateFile: mwiPrivateFiles) {

							try {
								///opt/mfs/internal/<msid>/private/moip/33333/2222.subscriber
								//==> MIO 5.0 MIO5_MFS
								///opt/mfs/internal/<msid>/private_moip_33333_2222.subscriber
								//System.out.println("@@@@@@@@@MIO5_MFS_?: VMEventHandler.informEvent(): Potential problem - can see where private_moip_33333_2222.subscriber was set");
								String[] parts = mwiPrivateFile.split(File.separator);
								String filename = parts[parts.length - 1];
								String userAgent = filename.replace(".subscriber", "");
								if(log.isDebugEnabled()){
									log.debug("VMEventHandler.informEvent: userAgent for sipmwi is: " + userAgent);
								}

								String mailboxId = parts[parts.length - 2];
								if(log.isDebugEnabled()){
									log.debug("VMEventHandler.informEvent: mailboxId for sipmwi is: " + mailboxId);
								}

								Properties properties = new Properties();
								properties.put(SubscriptionExpiryScheduler.USER_AGENT, userAgent);
								properties.put(SubscriptionExpiryScheduler.MAILBOX_ID, mailboxId);
								properties.put(SubscriptionExpiryScheduler.CALLED_NUMBER, mailboxId);
								properties.put(SubscriptionExpiryScheduler.FORCE, "true");

								SubscriptionExpiryScheduler.getInstance().handleExpiry(properties,null);
							}catch(Exception e) {
								log.error("VMEventHandler.informEvent: Exception while processing mwi files " + e.getMessage());

							}

						}
					}
				}
			} else if(COSRetentionDaysChangedEvent.name().equals(req.informEventType.value)) {
				if(log.isDebugEnabled()){
					log.debug("VMEventHandler.informEvent: Received a COS Retention Days Changed inform event.");
				}

				COSRetentionDaysChangedEvent cosRetentionDaysChangedEvent = null;

				if(req.informEventProp != null && req.informEventProp.value != null) {
					cosRetentionDaysChangedEvent = COSRetentionDaysChangedEvent.fromString(req.informEventProp.value);
				}

				if (cosRetentionDaysChangedEvent != null) {
					try {
						CommonMessagingAccess.getInstance().handleCosRetentionDaysChangedEvent(cosRetentionDaysChangedEvent);
					} catch (MsgStoreException e) {
						log.error("VMEventHandler.informEvent: Exception while processing " + req.informEventType.value + ": " + e.getMessage());
					}
				} else {
					log.error("VMEventHandler.informEvent: " + req.informEventType.value + " event has no data to process");
					resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, req.informEventType.value + " event has no data to process");
				}
			} else if (InformEventType.MGS_UNDELETE_GET_CONF.equals(req.informEventType.value)) { // MSG Undelete
				try {
					//String allConfigs = getMessageUndeleteConf();
					String conf = ConfigParam.ENABLE_MSG_UNDELETE + "=" + isMessageUndeleteEnabled();
					resp = new InformEventResp(req.transID.value, InformEventResult.OK, conf);
				} catch (Exception e) {
					resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, e.getMessage());
				}
			} else if (InformEventType.MGS_UNDELETE.equals(req.informEventType.value)) { // MSG Undelete
				resp = handleMsgUndeleteEvent(req);
			} else{
				log.warn("VMEventHandler.informEvent: " + req.informEventType.value + " not a valid event type" );
				resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, req.informEventType.value + " not a valid event type");
			}
		}else{
			if(log.isDebugEnabled()){
				log.debug("VMEventHandler.informEvent: Received a null informEvent request.");
			}
			resp = new InformEventResp("0", InformEventResult.PERMFAIL, "Request was null");
		}

		//The code is made so the respond is not set when we did not received any error.
		if(resp == null){
			if(req != null) {
				resp = new InformEventResp(req.transID.value, InformEventResult.OK);
			}
		}
		return resp;
	}


	/**
	 * Notify new Message on the new opco when subcriber is moved from one opco to a new opco
	 * @param req the InformEventReq defining request context.
	 * @return the InformEventResp for errors.  Null is returned to caller, expecting that method to construct a success response.
	 */
	private InformEventResp handleNotifyNewMsg(InformEventReq req) {
		InformEventResp resp = null;
		IDirectoryAccessSubscriber profile = CommonMessagingAccess.getInstance().getMcd().lookupSubscriber(req.destRcptID.value);

		if (profile != null) {

			if(log.isDebugEnabled())log.debug("VMEventHandler.handleNotifyNewMsg(): get NotifyNewMsg  " + profile.toString());
			String tel = profile.getSubscriberIdentity("tel");

			if(tel!=null) {

				try {
					//filter new voice/fax/video messages
					MSA rmsa = new MSA(profile.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MSID));
					StateAttributesFilter stateAttributesFilter = new StateAttributesFilter();
					stateAttributesFilter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE , MoipMessageEntities.MESSAGE_NEW);
					stateAttributesFilter.setAttributeValue(StateAttributes.getC1StateName(Container1.Message_class),  new String[] {ServiceName.VOICE, ServiceName.FAX, ServiceName.VIDEO});
					StateFile[] stateFiles = MsgStoreServer.getInstance().getStateFiles(rmsa, stateAttributesFilter);

					/**
					 *  We want to notify every number that has a new message.  We only want to notify each number once
					 *  So scan the list of new state files and get the lates one for each number in the list.
					 */
					if(stateFiles != null && stateFiles.length > 0) {
						HashMap <String, VMNotifyEvent> notifyEvents = new HashMap <String, VMNotifyEvent>();

						for (StateFile stateFile : stateFiles){
							/*
							 * Create a new VMNotifyEvent, and if it throws an exception, log it and ignore this statefile.
							 */
							try {
								VMNotifyEvent event = new VMNotifyEvent(stateFile);
								// next, get any event previous stored for this 'to' number
								VMNotifyEvent previousEvent = notifyEvents.get(event.toField);
								// Now, if there is none, or if the new event date is more recent, then put this event in the map
								if (previousEvent == null || event.compareTo(previousEvent)>0){
									if (log.isDebugEnabled())log.debug("VMEventHandler.handleNotifyNewMsg() adding VMNotifyEvent for " + event.toField + ", time = " + event.dateTime);
									notifyEvents.put(event.toField, event);
								}
							}catch (InvalidParameterException e){
								log.warn("VMEventHandler.handleNotifyNewMsg(): Exception while processing state file.  Ignoring this statefile" + e.getMessage() );
							}
						}
						/**
						 * now send out all the events placed in map
						 */
						if (notifyEvents.size() > 0){
							Iterator<Entry<String, VMNotifyEvent>> iter = notifyEvents.entrySet().iterator();

							while (iter.hasNext()){
								Entry <String,VMNotifyEvent> entry = iter.next();
								if (log.isDebugEnabled())log.debug("VMEventHandler.handleNotifyNewMsg() sending informNtf for subscriber = " + entry.getValue().toField + ", for date " + entry.getValue().dateTime);
								CommonMessagingAccess.getInstance().informNtf(EventTypes.DELIVERY, entry.getValue().msgInfo, entry.getValue().toField, null);
							}
						}
					}else {
						if (log.isDebugEnabled())  log.debug("VMEventHandler.handleNotifyNewMsg(): There were no new messages for tel identity = " + tel);
					}
				} catch (MsgStoreException e) {
					log.error("VMEventHandler.handleNotifyNewMsg: Could get state file for subscriber:"+tel , e);
					resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when trying to access the Message Store");
				}
			} else  {
				log.error("VMEventHandler.handleNotifyNewMsg(): Can't find tel number for identity: " +req.rMsa.value);
				resp = new InformEventResp("0", InformEventResult.PERMFAIL, "Subscriber doesn't have tel number");
			}
		} else {
			log.error("VMEventHandler.handleNotifyNewMsg(): Can't find subscriber for identity: " +req.destRcptID.value);
			resp = new InformEventResp("0", InformEventResult.PERMFAIL, "Subscriber not found");
		}
		return resp;
	}



	/**
	 * schedule a new expiry event in C1 container when subscriber is moved to a new opco
	 */
	private InformEventResp handleScheduleExpiry(InformEventReq req) {
		InformEventResp resp = null;
		MessageInfo msgInfo = new MessageInfo(new MSA(req.oMsa.value), new MSA(req.rMsa.value), req.oMsgID.value, req.rMsgID.value);

		if(log.isDebugEnabled())log.debug("VMEventHandler.handleScheduleExpiry(): entered method for message : " + msgInfo.toString());

		try {
			MfsStateFolderType folderType;
			try {
				folderType = MfsStateFolderType.valueOf(req.msgFolder.value.toUpperCase());
			} catch (Exception e) {
				folderType = MfsStateFolderType.INBOX;
			}
			
			StateFile stateFile = CommonMessagingAccess.getInstance().getStateFile(msgInfo, folderType.toString());
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
			setMessageExpiry(dateFormat.parse(req.informEventProp.value), stateFile);

			if(log.isDebugEnabled())log.debug("VMEventHandler.handleScheduleExpiry(): about to update state file expiry for : " + msgInfo.toString());

			CommonMessagingAccess.getInstance().updateState(stateFile);

			if(log.isDebugEnabled())log.debug("VMEventHandler.handleScheduleExpiry(): successfully updated state file expiry for : " + msgInfo.toString());

		} catch (MsgStoreException e) {
			log.error("VMEventHandler.handleScheduleExpiry. ScheduleExpiry:MsgStoreException   ", e);
			resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, req.informEventType.value + " gets MsgStoreException.");

		} catch (ParseException e) {
			log.error("VMEventHandler.handleScheduleExpiry. ScheduleExpiry:MsgStoreException   ", e);
			resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, req.informEventType.value + " Date format Exception ");
		}
		return resp;
	}

	/**
	 * schedule slamdown event when subscriber is moved to a new opco
	 */
	private InformEventResp handleSlamdownEvent(InformEventReq req) {

		InformEventResp resp = null;
		String prop = req.informEventProp.value;
		StringTokenizer tokensWithNewLine = new StringTokenizer(prop, "||");
		ArrayList<String> arr = new ArrayList<String>();
		tokensWithNewLine.nextToken();//ignore the first line

		if(log.isDebugEnabled())log.debug("VMEventHandler.handleSlamdownEvent(): entered method : " );

		while (tokensWithNewLine.hasMoreTokens()) {
			arr.add(tokensWithNewLine.nextToken());
		}
		// send to trafficEventSender each slamdown
		for (int index=0; index< arr.size(); index++) {
			StringTokenizer tokens = new StringTokenizer(arr.get(index), "/");
			TrafficEvent trafficEvent = new TrafficEvent();
			String s = null;
			StringTokenizer p = null;
			while (tokens.hasMoreTokens()) {
				s= tokens.nextToken();
				p = new StringTokenizer(s, "=");
				trafficEvent.setProperty(p.nextToken(), p.nextToken());
			}

			trafficEvent.setName(MfsClient.EVENT_SLAMDOWNINFORMATION);
			try {
				if (log.isDebugEnabled()) log.debug("VMEventHandler.handleSlamdownEvent(): about to call reportTrafficEvent() for:"+trafficEvent.getProperties().toString());
				trafficEventSender.reportTrafficEvent(trafficEvent); //mfs.sendTrafficEvent(trafficEvent);
				if (log.isDebugEnabled()) log.debug("VMEventHandler.handleSlamdownEvent(): completed call to reportTrafficEvent()");

			} catch (TrafficEventSenderException e) {
				log.error(" TrafficEventSenderException. ===", e);
				resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, req.informEventType.value + " TrafficEventSender Exception.");
			}
		}

		return resp;
	}

	/**
	 * schedule an autodeletion event for a subscriber that was just migrated to our system
	 */
	private InformEventResp handleScheduleAutodeletion(InformEventReq req) {
		InformEventResp resp = null;
		String msid = req.rMsa.value;
		String fileName = null;

		if(log.isDebugEnabled())log.debug("VMEventHandler.handleScheduleAutodeletion(): entered method for msid: " + msid);

		String muid = null;
		IDirectoryAccessSubscriber subscriberProfile = DirectoryAccess.getInstance().lookupSubscriber(msid);
		if (subscriberProfile != null) {
			muid = subscriberProfile.getSubscriberIdentity(MCDConstants.IDENTITY_SCHEME_MUID);
		}
		if (muid == null) {
			log.warn("VMEventHandler.handleScheduleAutodeletion(): Could not find muid for msid: " +
					msid + ". Failed to schedule Autodeletion.");
			resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL,
					req.informEventType.value + " can't find muid for msid: " + msid + ".");
			return resp;
		}

		// Prepend muid value with "muid:"...
		muid = "muid:" + muid;
		String userAgent = subscriberProfile.getSubscriberIdentity(MCDConstants.IDENTITY_SCHEME_TEL);
		if(log.isDebugEnabled())log.debug("VMEventHandler.handleScheduleAutodeletion(): msid: " + msid + ", muid: " +
				muid + ", tel: " + userAgent);

		int[] AutoProvProfRetention = subscriberProfile.getIntegerAttributes("MOIPAutoProvProfileRetention");

		if ((AutoProvProfRetention == null) || (AutoProvProfRetention[0] == 0)) {
			if (log.isDebugEnabled()) log.debug("VMEventHandler.handleScheduleAutodeletion(): Autodeletion is off; " +
					"will not schedule autodeletion event for msid " + msid);
			return resp;
		}
		int expiresI = AutoProvProfRetention[0];

		try {
			fileName = OutboundNotification.getAutodeletionFileName(userAgent);
			if (log.isDebugEnabled()) log.debug("VMEventHandler.handleScheduleAutodeletion(): File is: " + fileName);

			Properties newProp = new Properties();
			newProp.put(AutoProvProfExpiryScheduler.MUID, muid);
			newProp.put(AutoProvProfExpiryScheduler.USER_AGENT, userAgent);
			//long expiresL = System.currentTimeMillis() + 60*1000*expiresI;  // int minutes to long milliseconds
			long expiresL = System.currentTimeMillis() + 24*60*60*1000*expiresI;  // int days to long milliseconds

			// Check if the file already exists in order to cancel the previous expiry timer
			// Theoretically, this should not happen because subscriber is supposed to just have been created
			// but better not to take any chance and make sure there is no existing expiry event
			if(mfsEventManager.fileExists(userAgent, fileName, true)){
				if (log.isDebugEnabled()) log.debug("VMEventHandler.handleScheduleAutodeletion(): File already exists - replacing and canceling the previous expiry event");
				// cancel the previous expiration timer
				Properties oldProp = mfsEventManager.getProperties(userAgent, fileName);
				if(oldProp != null){
					//Here we don't care canceling the timer after the new private file is stored since it is obsolete anyway
					AutoProvProfExpiryScheduler.getInstance().cancelExpiry(oldProp.getProperty(AutoProvProfExpiryScheduler.EXPIRY_EVENT_ID));
				}
			}
			String eventId = AutoProvProfExpiryScheduler.getInstance().scheduleExpiryTimer(userAgent, muid, expiresL);
			newProp.put(AutoProvProfExpiryScheduler.EXPIRY_EVENT_ID, eventId);
			newProp.put(SubscribeCall.EXPIRY_DATE, Long.toString(expiresL));
			mfsEventManager.storeProperties(userAgent, fileName, newProp);
		} catch (TrafficEventSenderException e) {
			log.error("VMEventHandler.subscriberScheduleAutodeletion(): Cannot write to file "+ fileName + ": " + e.getMessage(), e);
			resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL,
					req.informEventType.value + " gets TrafficEventSenderException for subscriber " + userAgent + ".");
			return resp;
		} catch (Exception e) {
			log.error("VMEventHandler.subscriberScheduleAutodeletion(): Exception occured: " + e.getMessage(),  e);
			resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL,
					req.informEventType.value + " gets Exception for subscriber " + userAgent + ".");
			return resp;
		}

		return resp;
	}

	private void sendFaxAutoPrint(IStoredMessage iStoredMessage, MessageIdentifier msgId, InformEventReq req, String tel) {
		try
		{
			log.debug("sendFaxAutoPrint new fax message received for tel "+tel + " checking for auto print fax");

			IDirectoryAccessSubscriber profile = getProfile(req.rMsa);
			if(profile!=null)
			{
				boolean fax_auto_print_service =false;

				//Check subscriber has fax autoprint service
				String [] services = profile.getStringAttributes(DAConstants.ATTR_SERVICES);
				for (int i = 0; services!=null && i < services.length; i++) {
					if (services[i].indexOf(ProvisioningConstants.SERVICES_AUTOPRINT_FAX) > -1) {
						fax_auto_print_service = true;
						break;
					}
				}
				//Check auto print service is not disable
				if (fax_auto_print_service)
				{
					boolean autoprintDisabled = false;
					String[] strDisabled = profile.getStringAttributes(DAConstants.ATTR_AUTOPRINT_FAX_DISABLED);

					if (strDisabled != null && strDisabled.length == 1
							&& strDisabled[0].equalsIgnoreCase(ProvisioningConstants.YES))
					{
						autoprintDisabled = true;
					}

					if (!autoprintDisabled)
					{
						FaxNumber printNumber = FaxNumber.create(profile);
						iStoredMessage.print(tel, printNumber, true);
						log.info("Autoprint fax successfully triggered from[" + tel + "] to fax destination[" + printNumber + "]");
					}
					else
					{
						log.debug("sendFaxAutoPrint subscriber has fax service print disable tel: "+tel);
					}
				}
				else
				{
					log.debug("sendFaxAutoPrint subscriber doesn't have fax print service tel: "+tel);
				}
			}
			else
			{
				log.debug("sendFaxAutoPrint unable to find profile for tel "+tel);
			}

		} catch (Exception e)
		{
			log.info("sendFaxAutoPrint failed for tel: "+tel+" msgId: "+msgId,e);
		}
	}

	private String lookupTelForRMSA(RecipientMsa rMsa) {
		String tel = "";
		IDirectoryAccessSubscriber profile = CommonMessagingAccess.getInstance().getMcd().lookupSubscriber(rMsa.value);

		if (profile != null) {
			tel = profile.getSubscriberIdentity("tel");
			if (tel != null) {
				int plusSignIndex = tel.indexOf('+');
				if(plusSignIndex > 0){
					tel = tel.substring(plusSignIndex + 1);
				}
			}
		}

		return tel;
	}


	private IDirectoryAccessSubscriber getProfile(RecipientMsa rMsa) {
		IDirectoryAccessSubscriber profile = CommonMessagingAccess.getInstance().getMcd().lookupSubscriber(rMsa.value);
		return profile;
	}

	private MessageIdentifier informEventReqToMessageIdentifier(InformEventReq req) {
		return new MessageIdentifier(new MSA(req.oMsa.value), new MSA(req.rMsa.value), req.oMsgID.value, req.rMsgID.value);
	}

	/**
	 * This method is to only update the message.
	 * It will restart a new expire event
	 * @param msgId the message identifier of the message
	 * @param folderName  the folder where the message is saved
	 */
	private void updateMessageState(MessageIdentifier msgId, String folderName, String accessPoint) throws MailboxException{
		IStoredMessage iStoredMessage = getMessage(msgId, folderName);
		iStoredMessage.setMessageAccessPoint(accessPoint);
		iStoredMessage.saveChanges();
	}

	/**
	 * This method will change the state of a message and start an expire event
	 * @param msgId the message identifier of the message
	 * @param folderName the folder where the message is saved
	 * @param state the new state of the message
	 * @return  the updated message
	 */
	private IStoredMessage setMessageState(MessageIdentifier msgId, String folderName, StoredMessageState state, String accessPoint) throws MailboxException{
		IStoredMessage iStoredMessage = getMessage(msgId, folderName);
		iStoredMessage.setMessageAccessPoint(accessPoint);
		iStoredMessage.setState(state);
		iStoredMessage.saveChanges();
		return iStoredMessage;
	}

	/**
	 * This method will change the state of a message and start an expire event
	 * @param folderName the folder where the message is saved
	 */
	private boolean isFaxMessage(IStoredMessage iStoredMessage, String folderName) {
		if (iStoredMessage != null) {
			log.debug("isFaxMessage message type  is: " + iStoredMessage.getType());
			return MailboxMessageType.FAX.equals(iStoredMessage.getType());
		} else {
			log.error("isFaxMessage iStoredMessage is null");
			return false;
		}
	}

	/**
	 * This method is use to get the message.
	 * @param msgId the message identifier of the message
	 * @param folderName  the folder where the message is saved
	 */
	private IStoredMessage getMessage(MessageIdentifier msgId, String folderName) throws MailboxException{
		MailboxProfile mailboxProfile = new MailboxProfile(msgId.rmsa.getId(), null, null);
		IMailbox mailbox = mailboxAccountManager.getMailbox(null, mailboxProfile);
		IFolder folder = mailbox.getFolder(folderName == null || folderName.equals("") ? "inbox" : folderName);
		return folder.getMessage(msgId);
	}

	private void setMessageExpiry(Date expiryDate, StateFile stateFile) throws MsgStoreException {
		if(expiryDate == null){
			return;
		}
		CommonMessagingAccess.getInstance().setMessageExpiry(expiryDate, stateFile);
	}


	public static class VMNotifyEvent implements Comparable<VMNotifyEvent> {

		public String toField = null;
		public MessageInfo msgInfo = null;
		public StateFile statefile = null;
		public Date date = null;
		public String dateTime = null;

		public VMNotifyEvent (StateFile statefile)  throws InvalidParameterException{

			/**
			 * Step 1: Validate that the parameters passed are not null
			 */
			if (statefile == null){
				throw new InvalidParameterException("VMNotifyEvent.VMNotifyEvent(): statefile parameter is null.");
			}

			this.statefile = statefile;
			msgInfo = new MessageInfo(statefile.omsa, statefile.rmsa, statefile.omsgid, statefile.rmsgid);
			toField = CommonMessagingAccess.getInstance().denormalizeNumber(statefile.getC1Attribute(TO_PARAMNAME));

			/**
			 * Step 2: extract the date of this file and store the parsed value for later comparisons
			 */

			dateTime = statefile.getAllC1Attributes().getValue(DATETIME_PARAMNAME);
			if (dateTime != null ){
				try {
					DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy k:mm:ss");
					date = formatter.parse(dateTime);
				} catch (ParseException e) {
					throw new InvalidParameterException("VMNotifyEvent.VMNotifyEvent(): Unable to parse Date-time field of state file : " + dateTime);
				}
			}
		}

		/**
		 * Compare the dates of the statefile's "Date-time" c1 parameter:
		 * @param event  VMNotifyEvent object to compare to
		 * @return the value 0 if the argument Date is equal to this Date;<br>
		 * a value less than 0 if this Date is before the Date argument;<br>
		 * a value greater than 0 if this Date is after the Date argument.
		 */
		@Override
		public int compareTo(VMNotifyEvent event) {
			return (date.compareTo(event.date));
		}
	}

	/*********
    IGroup backEndGroup = null;
    public String getMessageUndeleteConf() throws Exception {
    	if (this.backEndGroup == null) {
    		try {
    			loadBackendConfig();
    		} catch (Exception e) {
    			throw e;
    		}
    	}
    	String res = "";
        try {
        	res += ConfigParam.ENABLE_MSG_UNDELETE + "=" + backEndGroup.getString(ConfigParam.ENABLE_MSG_UNDELETE) + SEPARATOR;
        	res += ConfigParam.MAX_NBR_MSG_UNDELETE + "=" + backEndGroup.getString(ConfigParam.MAX_NBR_MSG_UNDELETE) + SEPARATOR;
        	res += ConfigParam.GENERATE_CDR_MSG_UNDELETE + "=" + backEndGroup.getString(ConfigParam.GENERATE_CDR_MSG_UNDELETE) + SEPARATOR;
        	res += ConfigParam.RECYCLE_MSG_EXPIRY_DAYS + "=" + backEndGroup.getString(ConfigParam.RECYCLE_MSG_EXPIRY_DAYS) + SEPARATOR;        	
        	return res;
        } catch (Exception e) {
            log.error("VMEventHandler.getMessageUndeleteConf(): exception getting msg undelete config: " + e, e);
            throw e;
        }
    }
	 ********/

	PropertyFileConfigManagerGen2 props = null;
	String SEPARATOR = "|";

	@Deprecated 
	public String getMessageUndeleteConf() throws Exception {
		if (this.props == null) {
			props = PropertyFileConfigManagerGen2.getInstance();
			props.loadConfigFile(ConfigParam.MSG_UNDELETE_PROP_FILE, true, null, null);
		}
		String res = "";
		try {
			res += ConfigParam.ENABLE_MSG_UNDELETE + "=" + props.getBooleanValue(ConfigParam.ENABLE_MSG_UNDELETE) + SEPARATOR;
			res += ConfigParam.MAX_NBR_MSG_UNDELETE + "=" + props.getProperty(ConfigParam.MAX_NBR_MSG_UNDELETE) + SEPARATOR;
			res += ConfigParam.GENERATE_CDR_MSG_UNDELETE + "=" + props.getProperty(ConfigParam.GENERATE_CDR_MSG_UNDELETE) + SEPARATOR;
			res += ConfigParam.RECYCLE_MSG_EXPIRY_DAYS + "=" + props.getProperty(ConfigParam.RECYCLE_MSG_EXPIRY_DAYS) + SEPARATOR;        	
			return res;
		} catch (Exception e) {
			log.error("VMEventHandler.getMessageUndeleteConf(): exception getting msg undelete config: " + e, e);
			throw e;
		}
	}

	public boolean isMessageUndeleteEnabled() {
		if (this.props == null) {
			this.props = PropertyFileConfigManagerGen2.getInstance();
			this.props.loadConfigFile(ConfigParam.MSG_UNDELETE_PROP_FILE, true, null, null);
		}
		return this.props.getBooleanValue(ConfigParam.ENABLE_MSG_UNDELETE);
	}


	InformEventResp handleMsgUndeleteEvent(InformEventReq req) {

		InformEventResp resp = null;
		MessageInfo msgInfo = new MessageInfo(new MSA(req.oMsa.value), new MSA(req.rMsa.value), req.oMsgID.value, req.rMsgID.value);
		if (log.isDebugEnabled()) log.debug("VMEventHandler.handleMsgUndeleteEvent(): entered method for message : " + msgInfo.toString());

		boolean enabled = isMessageUndeleteEnabled();

		if (!enabled) {
			log.error("VMEventHandler.handleMsgUndeleteEvent(): msg undelete feature not enabled on this TN; please check " + ConfigParam.MSG_UNDELETE_PROP_FILE);
			resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, "msg undelete feature not enabled on this TN; please check " + ConfigParam.MSG_UNDELETE_PROP_FILE);
			return resp;
		}


		int retention = 0;
		String state = null;
		String[] attrs = req.informEventProp.value.split("\\|");
		for (String attr : attrs) {
			String[] keyVal = attr.split("=");
			if (keyVal.length == 2 && keyVal[0].equals("retentionDay")) {
				try {
					retention = Integer.parseInt(keyVal[1]);
				} catch (Exception e) {
					log.error("VMEventHandler.handleMsgUndeleteEvent():  ", e);
					resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, "Invalid properties: " + req.informEventProp.value);
					return resp;
				}
			}
			if (keyVal.length == 2 && keyVal[0].equals("state")) {
				state = keyVal[1];
			}
		}
		if (retention < 0 || state == null) {
			log.error("VMEventHandler.handleMsgUndeleteEvent(): invalid properties: " + req.informEventProp.value);
			resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, "Invalid properties: " + req.informEventProp.value);
			return resp;        	
		}

		if (log.isDebugEnabled()) log.debug("VMEventHandler.handleMsgUndeleteEvent(): with " + req.informEventProp.value);
		try {
			StateFile stateFile = CommonMessagingAccess.getInstance().getStateFile(msgInfo);
			Calendar expiryDate = Calendar.getInstance();
			expiryDate.add(Calendar.DATE, retention);
			stateFile.setMsgState(state);
			// If there is any VVM/IMAP flag, reset it to ""
			String imapFlag = stateFile.getAttribute("imap.statusflag");
			if (imapFlag != null && !imapFlag.isEmpty()) stateFile.setAttribute("imap.statusflag", "");
			setMessageExpiry(expiryDate.getTime(), stateFile);                       
			CommonMessagingAccess.getInstance().updateState(stateFile);
			if (log.isDebugEnabled()) log.debug("VMEventHandler.handleMsgUndeleteEvent(): to generate CDR");
			(new GenerateCDR(stateFile)).start(); // Generates CDR failure or block shall not prevent us to return OK
		} catch (MsgStoreException e) {
			log.error("VMEventHandler.handleMsgUndeleteEvent(): exception: " + e, e);
			resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, e.getMessage());
		} 

		//
		// HX68830 MIO501 VVM should get updated upon message undelete
		//
		IDirectoryAccessSubscriber profile = CommonMessagingAccess.getInstance().getMcd().lookupSubscriber(msgInfo.rmsa.toString());
		if (profile != null) {
			String tel = profile.getSubscriberIdentity("tel");
			if (tel != null && isVvmActivated(profile)) {
				Properties  properties = new Properties();
				properties.put("force", "false");
				properties.put("mailboxId", tel);
				properties.put("callednumber", tel);
				//properties.put("vvmNotify", "true");
				CommonMessagingAccess.getInstance().notifyNtf(EventTypes.MAILBOX_UPDATE, msgInfo, tel, properties);
			} else {
				log.debug("VMEventHandler.handleMsgUndeleteEvent: not a VVM activated sub; tel: " + tel + "; rmsa: " + req.rMsa.value);
			}
		} else {
			log.error("VMEventHandler.handleMsgUndeleteEvent: Can't find subscriber for identity: " + req.rMsa.value);
		}

		return resp;

	}

	// HX98472 
	InformEventResp handleMsgDeleteEvent(InformEventReq req, String accessPoint) {
		InformEventResp resp = null;
		//MessageInfo msgInfo = new MessageInfo(new MSA(req.oMsa.value), new MSA(req.rMsa.value), req.oMsgID.value, req.rMsgID.value);
		MessageIdentifier msgId = new MessageIdentifier(new MSA(req.oMsa.value), new MSA(req.rMsa.value), req.oMsgID.value, req.rMsgID.value);        
		if (log.isDebugEnabled()) log.debug("VMEventHandler.handleMsgDeleteEvent(): entered method for message : " + msgId.toString() + "; msgfolder = " + req.msgFolder.value);

		if (this.props == null) {
			this.props = PropertyFileConfigManagerGen2.getInstance();
			this.props.loadConfigFile(ConfigParam.MSG_UNDELETE_PROP_FILE, true, null, null);
		}
		boolean msgUndeleteEnabled = isMessageUndeleteEnabled();
		if (!msgUndeleteEnabled) {
			try { // Really delete the message
				setMessageState(msgId, req.msgFolder != null ? req.msgFolder.value : null, StoredMessageState.DELETED, accessPoint);
			} catch (MailboxException e) {
				log.error("VMEventHandler.informEvent: Could not delete message", e);
				resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when trying to access the Message Store");
			}
			return resp;
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////      
		// If we are here, msgundelete is enabled
		////////////////////////////////////////////////////////////////////////////////////////////////////////////    
		CommonMessagingAccess cms = CommonMessagingAccess.getInstance();
		StateFile stateFile = null;

		// For msg in trash, if inbox counterpart exists, then really delete the msg in trash 
		// (because this is the case where VVM was simply moving the message back from trash into the inbox); 
		// Else, move the msg from trash to inbox first, then apply undelete logic
		if (req.msgFolder.value != null && req.msgFolder.value.equalsIgnoreCase("trash")) {
			if (cms.checkIfStateFileExist(msgId, MfsStateFolderType.INBOX)) {
				log.debug("VMEventHandler.informEvent: msg is in trash with existing counterpart in inbox, so really delete; " + msgId);
				try { // Inbox counterpart exists, really delete the trash message
					setMessageState(msgId, req.msgFolder != null ? req.msgFolder.value : null, StoredMessageState.DELETED, accessPoint);
				} catch (MailboxException e) {
					log.error("VMEventHandler.informEvent: Could not delete message; " + msgId, e);
					resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when trying to access the Message Store");
				}
				return resp;
			}
			// Inbox counterpart does not exist, move trash to inbox first before doing cms.saveChangesForRecycle()
			log.debug("VMEventHandler.informEvent: msg is in trash with no counterpart in inbox, move it from trash to inbox; " + msgId);
			MsgStoreServer mfs = MsgStoreServer.getInstance();
			try {
				stateFile = cms.getStateFile(new MessageInfo(msgId), "trash");
				mfs.cloneStateFromTrash(stateFile);
				mfs.deleteStateInTrash(stateFile);
			} catch (Exception e) {
				log.error("VMEventHandler.informEvent: error occured when trying to move msg from trash to inbox; " + msgId, e);
				resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when when trying to move msg from trash to inbox");
			}
		}

		// For msg in inbox, if trash counterpart exists, then really delete the msg in trash 
		// (because this is the case where VVM was simply moving the message from inbox into the trash);
		if (req.msgFolder.value == null || req.msgFolder.value.equalsIgnoreCase("inbox")) {
			if (cms.checkIfStateFileExist(msgId, MfsStateFolderType.TRASH)) {
				log.debug("VMEventHandler.informEvent: msg is in inbox with existing counterpart in trash, so really delete; " + msgId);
				try { 
					setMessageState(msgId, req.msgFolder != null ? req.msgFolder.value : null, StoredMessageState.DELETED, accessPoint);
				} catch (MailboxException e) {
					log.error("VMEventHandler.informEvent: Could not delete message; " + msgId, e);
					resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when trying to access the Message Store");
				}
				return resp;    			
			}
		}

		try {
			stateFile = cms.getStateFile(new MessageInfo(msgId), "inbox");
			cms.saveChangesForRecycle(stateFile, 
					this.props.getIntValue(ConfigParam.MAX_NBR_MSG_UNDELETE, 10, null, null),
					this.props.getIntValue(ConfigParam.RECYCLE_MSG_EXPIRY_DAYS, 30, null, null));
		} catch (Exception e) {
			log.error("VMEventHandler.handleMsgDeleteEvent(): exception: " + e + "; " + msgId, e);
			resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, e.getMessage());
		} 

		(new GenerateCDR(stateFile, MdrConstants.DELETE)).start();

		//
		// update VVM (we could now be called by MAPS (MiO' REST interface server)
		//
		MessageInfo msgInfo = new MessageInfo(msgId);
		IDirectoryAccessSubscriber profile = CommonMessagingAccess.getInstance().getMcd().lookupSubscriber(msgInfo.rmsa.toString());
		if (profile != null) {
			String tel = profile.getSubscriberIdentity("tel");
			if (tel != null && isVvmActivated(profile)) {
				Properties  properties = new Properties();
				properties.put("force", "false");
				properties.put("mailboxId", tel);
				properties.put("callednumber", tel);
				//properties.put("vvmNotify", "true");
				CommonMessagingAccess.getInstance().notifyNtf(EventTypes.MAILBOX_UPDATE, msgInfo, tel, properties);
			} else {
				log.debug("VMEventHandler.handleMsgDeleteEvent: not a VVM activated sub; tel: " + tel + "; rmsa: " + req.rMsa.value);
			}
		} else {
			log.error("VMEventHandler.handleMsgDeleteEvent: Can't find subscriber for identity: " + req.rMsa.value);
		}

		return resp;

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

	/**
	 * For message undelete feature
	 */
	@Deprecated // Use CommonMessagingAccess.saveChangesForRecycle()
	public void saveChangesForRecycle(StateFile stateFile, int maxNumberMsgUndelete, int daysToExpire) throws Exception {
		//checkFaxMessageBeforeDeletion();        
		CommonMessagingAccess cms = CommonMessagingAccess.getInstance();
		try {
			String prioryState = stateFile.getMsgState();
			String timeStamp = "" + System.currentTimeMillis();
			stateFile.setMsgState(timeStamp + "|recycled|" + prioryState);
			cms.cancelScheduledEvent(stateFile);
			cms.setMessageExpiry(daysToExpire, stateFile);
			cms.updateState(stateFile, false);
		} catch (MsgStoreException e) {
			log.error("VMEventHandler.saveChangesForRecycle() error updating " + stateFile + ": " + e, e);
			throw new Exception(e.toString());
		}
		// Check total number of messages in recycle bin; if exceeding max, delete the oldest ones
		MsgStoreServer mfs = MsgStoreServer.getInstance();
		try {
			ArrayList<StateFile> sfs = mfs.getsortedListOfRecycledMessages(stateFile.rmsa.getId());
			if (sfs != null && sfs.size() > maxNumberMsgUndelete) {
				int numberOfOldestToDelete = sfs.size() - maxNumberMsgUndelete;
				log.debug("VMEventHandler.saveChangesForRecycle(): total number of msg in recycle bin exceeds the limit " + 
						maxNumberMsgUndelete + " by " + numberOfOldestToDelete + "; to permanently delete some oldest msg in recycle bin");
				for (int i = 0; i < numberOfOldestToDelete; i++) {
					StateFile sf = sfs.get(i);
					cms.cancelScheduledEvent(sf);
					cms.deleteMessage(sf);
				}
			}
		} catch (MsgStoreException e) {
			log.error("VMEventHandler.saveChangesForRecycle() error updating recycled msg list for " + stateFile.rmsa.getId() + ": " + e, e);
			// Do not throw exception as the recylcing op itself succeeded
		} catch (Exception e) {
			log.error("VMEventHandler.saveChangesForRecycle() error updating recycled msg list for " + stateFile.rmsa.getId() + ": " + e, e);
			// Do not throw exception as the recylcing op itself succeeded
		}
	}


	public class GenerateCDR extends Thread {

		public static final int UN_DEFINED_TYPE = -1000;

		StateFile stateFile = null;
		int eventType = GenerateCDR.UN_DEFINED_TYPE;

		/**
		 * Shall be called by the MSG_UNDELETE event only in order to generate CDR for msg_undelete operation
		 * @param sf
		 */
		public GenerateCDR(StateFile sf) {
			if (log.isDebugEnabled()) log.debug("VMEventHandler.handleMsgUndeleteEvent(): GenerateCDR constructor called");
			stateFile = sf;
		}

		/**
		 * Can be called by any event to generate CDR for event type = et
		 * @param sf
		 * @param et
		 */
		public GenerateCDR(StateFile sf, int et) {
			if (log.isDebugEnabled()) log.debug("VMEventHandler.handleMsgUndeleteEvent(): GenerateCDR constructor called");
			stateFile = sf;
			eventType = et;
		}

		/**
		 * generate CDR for msg_undelete
		 */
		public void run() {

			if (eventType != GenerateCDR.UN_DEFINED_TYPE) {
				generateCDR();
				return;
			}

			boolean	cdrEnabled = props.getBooleanValue(ConfigParam.GENERATE_CDR_MSG_UNDELETE);

			String recipient = stateFile.getC1Attribute(Container1.To);
			String state = stateFile.getMsgState();
			// eventType: Refer to CPI: Message Detail Record (MDR) Interface Description
			//                          INTERWORK DESCRIPTION 2/1540-HDB 101 04 Uen G  
			//                          Chapter 3.4.14 MAE-Evnt-Type, table 9
			eventType = 4; // created/new
			if (state.equalsIgnoreCase("read") || state.equalsIgnoreCase("retrieved")) eventType = 3;
			if (state.equalsIgnoreCase("saved") || state.equalsIgnoreCase("deposit")) eventType = 2;
			if (state.equalsIgnoreCase("stored") || state.equalsIgnoreCase("store")) eventType = 13; // won't get this, but future prove
			String messageType = stateFile.getC1Attribute(Container1.Message_class);
			if (messageType == null) messageType = stateFile.getC1Attribute(Container1.msgClass);
			if (messageType == null) messageType = stateFile.getAttribute(StateAttributes.GLOBAL_DEST_MSG_CLASS_KEY);
			if (messageType == null) messageType = "voice";
			ConfigManager localConfig = CommonOamManager.getInstance().getLocalConfig();
			boolean mdrEnabled = localConfig.getBooleanValue(MoipMessageEntities.mdrEnabled);
			if (log.isDebugEnabled()) log.debug("VMEventHandler.handleMsgUndeleteEvent(): MoipMessageEntities.mdrEnabled = " +
					mdrEnabled + "; cdrEnabled = " + cdrEnabled);
			//if (mdrEnabled && cdrEnabled){
			if (cdrEnabled) {
				try {
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
					String[] propertyNames = {MdrConstants.USERNAME, MdrConstants.OBJECTTYPE, MdrConstants.EVENTTYPE, 
							MdrConstants.EVENTREASON, "voiceinventory", "videoinventory", "faxinventory", 
							"messagetype", "description"};  
					String[] propertyValues = {recipient, String.valueOf(MdrConstants.MESSAGE), String.valueOf(eventType),
							String.valueOf(MdrConstants.TUI), voiceInventory, videoInventory, faxInventory,
							messageType, "msgUndelete"}; 
					String eventName = "messagestatus";
					if (log.isDebugEnabled()) log.debug("VMEventHandler.handleMsgUndeleteEvent(): to call TrafficEventMdrHandler.sendTrafficEvent() with et = " + eventType);
					TrafficEventMdrHandler.sendTrafficEvent(makeEvent(eventName, propertyNames, propertyValues));
				} catch (Exception e){
					log.error("VMEventHandler.handleMsgUndeleteEvent().GenerateCDR(): error generating CDR: " + e, e); 
				}
			}
		}

		/**
		 * Generate CDR for the eventType passed by the constructor
		 */
		private void generateCDR() {
			String recipient = stateFile.getC1Attribute(Container1.To);
			String messageType = stateFile.getC1Attribute(Container1.Message_class);
			if (messageType == null) messageType = stateFile.getC1Attribute(Container1.msgClass);
			if (messageType == null) messageType = stateFile.getAttribute(StateAttributes.GLOBAL_DEST_MSG_CLASS_KEY);
			if (messageType == null) messageType = "voice";
			ConfigManager localConfig = CommonOamManager.getInstance().getLocalConfig();
			boolean mdrEnabled = localConfig.getBooleanValue(MoipMessageEntities.mdrEnabled);
			if (log.isDebugEnabled()) log.debug("VMEventHandler.handleMsgUndeleteEvent(): MoipMessageEntities.mdrEnabled = " + mdrEnabled);
			try {
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
				String[] propertyNames = {MdrConstants.USERNAME, MdrConstants.OBJECTTYPE, MdrConstants.EVENTTYPE, 
						MdrConstants.EVENTREASON, "voiceinventory", "videoinventory", "faxinventory", "messagetype"};  
				String[] propertyValues = {recipient, String.valueOf(MdrConstants.MESSAGE), String.valueOf(eventType),
						String.valueOf(MdrConstants.TUI), voiceInventory, videoInventory, faxInventory,messageType}; 
				String eventName = "messagestatus";
				if (log.isDebugEnabled()) log.debug("VMEventHandler.generateCDR(): to call TrafficEventMdrHandler.sendTrafficEvent() with et = " + eventType);
				TrafficEventMdrHandler.sendTrafficEvent(makeEvent(eventName, propertyNames, propertyValues));
			} catch (Exception e){
				log.error("VMEventHandler.handleMsgUndeleteEvent().GenerateCDR(): error generating CDR: " + e, e); 
			}
		}
	}

	private TrafficEvent makeEvent(String eventName, String[] propertyNames, String[] propertyValues) {
		TrafficEvent trEvent = new TrafficEvent(eventName);
		for (int i = 0; i < propertyNames.length; i++) {
			trEvent.setProperty(propertyNames[i].trim(), propertyValues[i]);
		}
		return trEvent;
	}

}
