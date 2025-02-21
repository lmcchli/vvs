/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.


 */
package com.mobeon.masp.execution_engine.platformaccess;

import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.SubscriptionState;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.parser.FromParser;
import gov.nist.javax.sip.parser.ToParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import jakarta.activation.MimeType;
import javax.sip.header.ExtensionHeader;

import com.abcxyz.messaging.authentication.api.AuthenticationException;
import com.abcxyz.messaging.authentication.api.AuthenticationFactory;
import com.abcxyz.messaging.authentication.api.AuthenticationMechanism;
import com.abcxyz.messaging.authentication.crp.CrpDirectInterface;
import com.abcxyz.messaging.authentication.daf.DAFAuthenticationMechanism;
import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.impl.InMemoryConfigAgent;
import com.abcxyz.messaging.common.oam.impl.PropertyFileConfigManager;
import com.abcxyz.messaging.common.ssmg.AnyTimeInterrogationResult;
import com.abcxyz.messaging.common.util.event.BasicEvent;
import com.abcxyz.messaging.common.util.event.DailyFileEventStorage;
import com.abcxyz.messaging.common.util.event.EventRecorder;
import com.abcxyz.messaging.common.util.event.EventStorage;
import com.abcxyz.messaging.oe.common.perfmgt.PerformanceData;
import com.abcxyz.messaging.oe.common.system.OE;
import com.abcxyz.messaging.oe.impl.bpmanagement.perfmgt.PerformanceDataFactory;
import com.abcxyz.services.broadcastannouncement.BroadcastAnnouncement;
import com.abcxyz.services.broadcastannouncement.BroadcastException;
import com.abcxyz.services.moip.broadcastannouncement.BroadcastManager;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessBroadcastAnnouncement;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.directoryaccess.MoipProfile;
import com.abcxyz.services.moip.common.mdr.MdrConstants;
import com.abcxyz.services.moip.common.ss7.ISs7Manager;
import com.abcxyz.services.moip.common.ss7.Ss7Exception;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.common.configuration.UnknownParameterException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.ITrafficEventSender;
import com.mobeon.common.trafficeventsender.MfsClient;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException.TrafficEventSenderExceptionCause;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.callmanager.SubscribeCall;
import com.mobeon.masp.callmanager.notification.OutboundNotification;
import com.mobeon.masp.callmanager.sip.header.SipHeaderFactory;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.externaldocument.ResourceLocator;
import com.mobeon.masp.execution_engine.platformaccess.util.GlobalDirectoryAccessUtil;
import com.mobeon.masp.execution_engine.platformaccess.util.GlobalMigrationAccessUtil;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.RuntimeConstants;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.IMailboxAccountManager;
import com.mobeon.masp.mailbox.IStorableMessageFactory;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mediacontentmanager.IMediaContentManager;
import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediahandler.MediaHandler;
import com.mobeon.masp.mediahandler.MediaHandlerFactory;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.numberanalyzer.IAnalysisInput;
import com.mobeon.masp.numberanalyzer.INumberAnalyzer;
import com.mobeon.masp.numberanalyzer.NumberAnalyzerException;
import com.mobeon.masp.profilemanager.IProfileManager;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.servicerequestmanager.ServiceRequestManagerException;
import com.mobeon.masp.servicerequestmanager.ServiceResponse;
import com.mobeon.masp.util.NamedValue;
import com.abcxyz.messaging.common.hlr.HlrAccessManager;
import com.mobeon.common.cmnaccess.oam.MoipOamManager;

/**
 * Implements all functions in the PlatformAccess interface. Delegates different functions to private "managers". For
 * example the subscriber profile related functions are taken care of the SubscriberProfileManager and so on.
 *
 * @author ermmaha
 */
public class PlatformAccessImpl implements PlatformAccess {
    private static final String CLASSNAME = "PlatformAccessImpl";


    private static final String MAILBOXMANAGER_KEY = "MailboxManager";
    private static final String SUBSCRIBERPROFILEMANAGER_KEY = "SubscriberProfileManager";
    private static final String MEDIAMANAGER_KEY = "MediaManager";
    private static final String GREETING_CHANGED_KEY = "GreetingChanged";
    private static final String PROPRIETARY_HEADER_FOR_RESPONSE_KEY = "addProprietaryHeaderForSessionProgressResponse";

    private static final String BACK_END_CONF = "backend.conf";

    /**
     * logger
     */
    private static ILogger log = ILoggerFactory.getILogger(PlatformAccessImpl.class);
    private static EventRecorder eventRecorder = null;
    private static Object eventRecorderLock = new Object();

    private ExecutionContext executionContext;
    private INumberAnalyzer numberAnalyzer;
    private IConfiguration configuration;
    private MailboxManager mailboxManager;
    private SubscriberProfileManager subscriberProfileManager;
    private MediaManager mediaManager;
    private TrafficEventManager trafficEventManager;
    private ServiceResponse serviceResponse;
    private ContentTypeMapper contentTypeMapper;
    private MediaHandlerFactory mediaHandlerFactory;
    private ISs7Manager ss7Manager;
    private GlobalDirectoryAccessUtil globalDirectoryAccessUtil = null;
    private GlobalMigrationAccessUtil globalMigrationAccessUtil = null;
    private IMailboxAccountManager mailboxAccountManager;
    private MfsEventManager mfsEventManager = null;
    private List<String> customAttributeList = null;

    public PlatformAccessImpl(ExecutionContext executionContext,
                              INumberAnalyzer iNumberAnalyzer,
                              IProfileManager iProfileManager,
                              IConfiguration iConfiguration,
                              IStorableMessageFactory iStorableMessageFactory,
                              IMediaContentManager iMediaContentManager,
                              ITrafficEventSender iTrafficEventSender,
                              MediaHandlerFactory mediaHandlerFactory,
                              ContentTypeMapper contentTypeMapper,
                              ConfigManager anAuthenticationBackendConfigurationManager) {

        this.executionContext = executionContext;
        this.numberAnalyzer = iNumberAnalyzer;
        this.configuration = iConfiguration.getConfiguration();
        this.mediaHandlerFactory = mediaHandlerFactory;
        this.contentTypeMapper = contentTypeMapper;

        mailboxManager = getMailboxManager(iStorableMessageFactory);
        subscriberProfileManager = getSubscriberProfileManager(iProfileManager);
        mediaManager = getMediaManager(iMediaContentManager);
        trafficEventManager = new TrafficEventManager(executionContext, iTrafficEventSender);
		ss7Manager = CommonMessagingAccess.getInstance().getSs7Manager();
		mfsEventManager = MfsEventFactory.getMfsEvenManager();
		init();
    }

    private void init()
    {
        try {
            customAttributeList = this.configuration.getGroup(MdrConstants.CONFIG_FILE_NAME).getList(MdrConstants.CONFIG_LIST_NAME);
        } catch (GroupCardinalityException e) {
            log.error("CommonOamManager.getLocalConfiguration(): "+e.getMessage(), e);
        } catch (UnknownGroupException e) {
            log.error("CommonOamManager.getLocalConfiguration(): "+e.getMessage(), e);
        }
    }
    private SubscriberProfileManager getSubscriberProfileManager(IProfileManager iProfileManager) {

        // Below, we read from session object and then write to session object.
        // It may look like a race condition if there are several PlatformAccess in a session and
        // they all run the code below in parallel, but it works fine since
        // PlatformAccessFactory.create is synchronized.

        SubscriberProfileManager manager =
                (SubscriberProfileManager) executionContext.getSession().getData(SUBSCRIBERPROFILEMANAGER_KEY);
        if (manager == null) {
            manager = new SubscriberProfileManager(iProfileManager);
            executionContext.getSession().setData(SUBSCRIBERPROFILEMANAGER_KEY, manager);
        }
        return manager;
    }

    private MailboxManager getMailboxManager(IStorableMessageFactory iStorableMessageFactory) {
        // See comments in getSubscriberProfileManager
        MailboxManager manager =
                (MailboxManager) executionContext.getSession().getData(MAILBOXMANAGER_KEY);
        if (manager == null) {
            manager = new MailboxManager(iStorableMessageFactory);
            executionContext.getSession().setData(MAILBOXMANAGER_KEY, manager);
        }
        return manager;
    }

    private MediaManager getMediaManager(IMediaContentManager iMediaContentManager) {
        // See comments in getSubscriberProfileManager
        MediaManager manager =
                (MediaManager) executionContext.getSession().getData(MEDIAMANAGER_KEY);
        if (manager == null) {
            manager = new MediaManager(iMediaContentManager, executionContext);
            executionContext.getSession().setData(MEDIAMANAGER_KEY, manager);
        }
        return manager;
    }

    private EventRecorder getEventRecorder() {
        if (eventRecorder == null) {
            synchronized (eventRecorderLock) {
                // Double check: Make sure the eventRecorder was not initialized while waiting to acquire the lock
                if (eventRecorder == null) {
                    File storageDir = new File(OE.getLocalPerfDir());
                    String prefix = "imapevent_" + "activeVmInbox";
                    EventStorage eventStorage = new DailyFileEventStorage(storageDir, prefix, ".txt");
                    eventRecorder = new EventRecorder(eventStorage);
                }
            }
        }
        return eventRecorder;
    }

    public void distributionListAddMember(String phoneNumber, String distListNumber, String distListMember) {
       	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:distributionListAddMember is called with phoneNumber = " + phoneNumber
            		+ ", distListNumber = " + distListNumber + ", distListMember = " + distListMember);
        }
        subscriberProfileManager.distributionListAddMember(phoneNumber, distListNumber, distListMember);
    }

    public void distributionListDeleteMember(String phoneNumber, String distListNumber, String distListMember) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:distributionListDeleteMember is called with phoneNumber = " + phoneNumber
            		+ ", distListNumber = " + distListNumber + ", distListMember = " + distListMember);
        }
        subscriberProfileManager.distributionListDeleteMember(phoneNumber, distListNumber, distListMember);
    }

    public String[] distributionListGetMembers(String phoneNumber, String distListNumber) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:distributionListGetMembers is called with phoneNumber = " + phoneNumber
            		+ ", distListNumber = " + distListNumber);
        }
        return subscriberProfileManager.distributionListGetMembers(phoneNumber, distListNumber);
    }

    public IMediaObject distributionListGetSpokenName(String phoneNumber, String distListNumber) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:distributionListGetSpokenName is called with phoneNumber = " + phoneNumber
            		+ ", distListNumber = " + distListNumber);
        }
        return subscriberProfileManager.distributionListGetSpokenName(phoneNumber, distListNumber);
    }

    public void distributionListSetSpokenName(String phoneNumber, String distListNumber, IMediaObject spokenName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:distributionListGetSpokenName is called with phoneNumber = " + phoneNumber
            		+ ", distListNumber = " + distListNumber + ", spokenName = " + spokenName);
        }
        subscriberProfileManager.distributionListSetSpokenName(phoneNumber, distListNumber, spokenName);
    }

    public void mailboxAddFolder(int mailboxId, String folderName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxAddFolder is called with mailboxId = " + mailboxId
            		+ ", folderName = " + folderName);
        }
        mailboxManager.mailboxAddFolder(mailboxId, folderName);
    }

    public void mailboxAddFolder(int mailboxId, int folderId, String folderName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxAddFolder is called with mailboxId = " + mailboxId
            		+ ", folderId = " + folderId + ", folderName = " + folderName);
        }
        mailboxManager.mailboxAddFolder(mailboxId, folderId, folderName);
    }

    public int mailboxUsage(int mailboxId) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxUsage is called with mailboxId = " + mailboxId);
        }
        return mailboxManager.mailBoxUsage(mailboxId);
    }

    public int mailboxGetFolder(int mailboxId, String folderName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxGetFolder is called with mailboxId = " + mailboxId
            		+ ", folderName = " + folderName);
        }
        return mailboxManager.mailboxGetFolder(mailboxId, folderName);
    }

    public void mailboxSetReadonly(int mailboxId) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxSetReadonly is called with mailboxId = " + mailboxId );
        }
        mailboxManager.mailboxSetReadonly(mailboxId);
    }

    public void mailboxSetReadwrite(int mailboxId) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxSetReadwrite is called with mailboxId = " + mailboxId);
        }
        mailboxManager.mailboxSetReadwrite(mailboxId);
    }

    public int mailboxGetMessageList(int folderId, String types, String states, String priorities, String orders, String timeOrder) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxGetMessageList is called with folderId = " + folderId
            		+ " types = " + types);
        }
    	return mailboxManager.mailboxGetMessageList(folderId, types, states, priorities, orders, timeOrder, null);
    }

    public int mailboxGetMessageSubList(int messageListId, String types, String states, String priorities, String orders, String timeOrder) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:distributionListGetSpokenName is called with messageListId = " + messageListId + ", types= " + types +
            		", states=" + states + ", priorities=" + priorities + ", orders=" + orders + ", timeOrder=" + timeOrder);
        }
    	return mailboxManager.mailboxGetMessageSubList(messageListId, types, states, priorities, orders, timeOrder);
    }

    public int[] mailboxGetMessages(int messageListId) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxGetMessages is called with messageListId = " + messageListId );
        }
    	return mailboxManager.mailboxGetMessages(messageListId);
    }

    public int mailboxGetMessageListAsync(int folderId, String types, String states, String priorities, String orders, String timeOrder) {
        return 0;
    }

    public int[] mailboxGetMessageList(int transactionId) {
        return new int[0];
    }

    public int mailboxGetMessageUsage(int usageId) {
    	Object perf = null;
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxGetMessageUsage is called with usageId = " + usageId);
        }
    	try{
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.mailboxGetMessageUsage");
            }
            return mailboxManager.mailboxGetMessageUsage(usageId);
    	}finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }

    public int mailboxGetMessageUsage(int usageId, String msgType) {
    	Object perf = null;
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxGetMessageUsage is called with usageId = " + usageId +", msgType="+msgType);
        }
    	try{
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.mailboxGetMessageUsage");
            }
            return mailboxManager.mailboxGetMessageUsage(usageId,msgType);
    	}finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }

    public int mailboxGetNumberOfMessages(int messageListId, String types, String states, String priorities) {
		Object perf = null;
		if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:mailboxGetNumberOfMessages is called with messageListId = " + messageListId + ", types= " + types +
            		", states=" + states + ", priorities=" + priorities);
        }
    	try{
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.mailboxGetNumberOfMessages");
            }
            return mailboxManager.mailboxGetNumberOfMessages(messageListId, types, states, priorities);
		}finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }

    public void messageAddMediaObject(String fromPhoneNumber, int storableMessageId, IMediaObject mediaObject, String description,
            String fileName, String language) {
        messageAddMediaObject(fromPhoneNumber, storableMessageId, mediaObject, description, fileName, language, null);
    }

    public void messageAddMediaObject(String fromPhoneNumber, int storableMessageId, IMediaObject mediaObject, String description,
                                      String fileName, String language, String duration) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageAddMediaObject is called with storableMessageId = " + storableMessageId +
            		", mediaObject = " + mediaObject + ", description = " + description + ", fileName = " + fileName +
            		", language = " + language + ", duration: " + duration);
        }
		boolean append = false;
    	String appendOrPrependString = "";
    	try {
    		String stringValues[] = subscriberGetStringAttribute(fromPhoneNumber, DAConstants.ATTR_APPENDCOMMENTS);
    		if ((stringValues != null) && (stringValues.length == 1)){
    			if (log.isDebugEnabled()) {
    				log.debug("PlatformAccessImpl:messageAddMediaObjectFound should append value " + stringValues[0]);
    			}
    			appendOrPrependString = stringValues[0];
    		}
    		// For some strange reason, the second value of my picklist
    		// (append) ends up being returned here, instead of the discreet
    		// value in the GUI. So just in case (and in the meantime) no harm
    		// in checking for both values to decide to append
    		if ( (appendOrPrependString.equalsIgnoreCase("append")) || (appendOrPrependString.equalsIgnoreCase("no"))){
    			append = true;
    		}
    	}
    	catch (Exception e){
    		// We can get here if the subscriber doesn't exist.
        	if (log.isDebugEnabled()) {
                log.debug("PlatformAccessImpl:messageAddMediaObject on a phone number that we can't find. Lets default to the MOIP behavior (append)");
            }
    	}
    	mailboxManager.messageAddMediaObject(storableMessageId, mediaObject, description, fileName, language, duration, append);
    }

    public void messageCopyToFolder(int mailboxId, int messageId, String folderName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageCopyToFolder is called with mailboxId = " + mailboxId +
            		", messageId = " + messageId + ", folderName = " + folderName );
        }
        mailboxManager.messageCopyToFolder(mailboxId, messageId, folderName);
    }

    public void messageCopyToFolder(int mailboxId, int folderId, int messageId, String folderName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageCopyToFolder is called with mailboxId = " + mailboxId + ", folderId = " + folderId +
            		", messageId = " + messageId + ", folderName = " + folderName);
        }
        mailboxManager.messageCopyToFolder(mailboxId, folderId, messageId, folderName);
    }

    public int messageCreateNew() {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageCreateNew is called");
        }
		Object perf = null;
    	try{
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.messageCreateNew");
            }
            return mailboxManager.messageCreateNew();
		}finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }

    public int messageForward(int messageId) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageForward is called with messageId = " + messageId);
        }
        return mailboxManager.messageForward(messageId);
    }

    public int[] messageGetContent(int messageId) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageGetContent is called with messageId = " + messageId);
        }
    	Object perf = null;
    	try{
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.messageGetContent");
            }
            return mailboxManager.messageGetContent(messageId);
    	}finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }

    public IMediaObject messageGetMediaObject(int messageContentId) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageGetMediaObject is called with messageContentId = " + messageContentId);
        }
		Object perf = null;
    	try{
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.messageGetMediaObject");
            }
            return mailboxManager.messageGetMediaObject(messageContentId);
    	}finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }

    public String messageGetMediaProperties(int messageContentId) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageGetMediaProperties is called with messageContentId = " + messageContentId );
        }
        return mailboxManager.messageGetMediaProperties(messageContentId);
    }

    public String[] messageGetStoredProperty(int messageId, String propertyName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageGetStoredProperty is called with messageId = " + messageId + ", propertyName = " + propertyName);
        }
        return mailboxManager.messageGetStoredProperty(messageId, propertyName);
    }

    public String[] messageGetStorableProperty(int storableMessageId, String propertyName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageGetStorableProperty is called with storableMessageId = " + storableMessageId + ", propertyName = " + propertyName);
        }
        return mailboxManager.messageGetStorableProperty(storableMessageId, propertyName);
    }

    public int messageContentSize(int messageContentId) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageContentSize is called with messageContentId = " + messageContentId);
        }
        return mailboxManager.messageContentSize(messageContentId);
    }

    public int messageContentLength(int messageContentId, String type) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageContentLength is called with messageContentId = " + messageContentId + ", type= " + type);
        }
        return mailboxManager.messageContentLength(messageContentId, type);
    }

    public IMediaObject messageGetSpokenNameOfSender(int messageId) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageGetSpokenNameOfSender is called with messageId = " + messageId );
        }
		Object perf = null;
    	try{
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.messageGetSpokenNameOfSender");
            }
            return mailboxManager.messageGetSpokenNameOfSender(messageId);
		}finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }

    public void messagePrint(int messageId, String destination, String sender) {
    	if (log.isDebugEnabled()) {
    	        String [] subcsriberfaxno = subscriberProfileManager.subscriberGetIdentitiesAsString(sender,"fax");
    	        for(int i =0;subcsriberfaxno!=null && i<subcsriberfaxno.length;i++)
    	        {
    	            if(CommonMessagingAccess.getInstance().denormalizeNumber(subcsriberfaxno[i]).equalsIgnoreCase(CommonMessagingAccess.getInstance().denormalizeNumber(destination))){
                        throw new PlatformAccessException(EventType.MAILBOX, "messagePrint:messageId=" + messageId, "Destination fax number is the same has the user fax number");
    	            }
    	        }
            log.debug("PlatformAccessImpl:messagePrint is called with messageId = " + messageId + ", destination = " + destination +
            		", sender =" + sender);
        }
        mailboxManager.messagePrint(messageId, destination, sender);
    }

    public void messageSetStorableProperty(int storableMessageId, String propertyName, String[] propertyValue) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageSetStorableProperty is called with storableMessageId = " + storableMessageId +
            		", propertyName = " + propertyName + ", propertyValue =" + (propertyValue != null ? Arrays.asList(propertyValue) : null));
        }
        mailboxManager.messageSetStorableProperty(storableMessageId, propertyName, propertyValue, subscriberProfileManager);
    }

    public void messageSetStoredProperty(int messageId, String propertyName, String[] propertyValue) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageSetStoredProperty is called with messageId = " + messageId + ", propertyName= " + propertyName +
            		", propertyValue =" + (propertyValue != null ? Arrays.asList(propertyValue) : null));
        }
    	mailboxManager.messageSetStoredProperty(messageId, propertyName, propertyValue, trafficEventManager);
    }

    public void messageSetSpokenNameOfSender(int storableMessageId, IMediaObject spokenName, String description,
                                             String fileName, String language) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageSetSpokenNameOfSender is called with storableMessageId = " + storableMessageId + ", spokenName= " + spokenName +
            		", description =" + description + ", fileName =" + fileName + ", language =" + language);
        }
    	mailboxManager.messageSetSpokenNameOfSender(storableMessageId, spokenName, description, fileName, language);
    }

    public String[] messageStore(int storableMessageId) {
		Object perf = null;
		String[] failedRecipients = new String[0];

		if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageStore is called with storableMessageId = " + storableMessageId);
        }
		try{
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.messageStore");
            }
            failedRecipients = mailboxManager.messageStore(storableMessageId, trafficEventManager);
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
        return failedRecipients;
    }

    public void messageSetExpiryDate(int messageId, String expiryDate) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:messageSetExpiryDate is called with messageId = " + messageId + ", expiryDate = " + expiryDate );
        }
        mailboxManager.messageSetExpiryDate(messageId, expiryDate);
    }

    public boolean subscriberAutoprovision(String phoneNumber) {
        return subscriberAutoprovision(phoneNumber, null);
    }

    public boolean subscriberAutoprovision(String phoneNumber, String subscriberTemplate) {

        if (!subscriberProfileManager.subscriberAutoprovision(phoneNumber, subscriberTemplate)) {
            log.error(CLASSNAME + ".subscriberAutoprovision: Could not autoprovision phoneNumber " + phoneNumber);
            return false;
        }

        // subscriber is now autoprovisioned; based on its CoS, may also need to schedule autodeletion
        subscriberRescheduleAutodeletionIfNecessary(phoneNumber);

        return true;
    }

    public void subscriberRescheduleAutodeletionIfNecessary(String phoneNumber) {

        // Based on sub's CoS Auto Prov Profile Retention parameter, may need to schedule or re-schedule autodeletion
        int[] AutoProvProfRetention = subscriberProfileManager.subscriberGetIntegerAttribute(
                phoneNumber, "MOIPAutoProvProfileRetention");

        if ((AutoProvProfRetention != null) && (AutoProvProfRetention[0] > 0)) {
            subscriberScheduleAutodeletion(phoneNumber);
        }
    }

    public void subscriberScheduleAutodeletion(String userAgent) {

        if (log.isDebugEnabled()) log.debug(CLASSNAME + ".subscriberScheduleAutodeletion called - userAgentNumber=" + userAgent);
        String fileName = null;

        String muid = subscriberProfileManager.subscriberGetMuid(userAgent);
        if (muid == null) {
            log.warn(CLASSNAME + ".subscriberScheduleAutodeletion: Cannot find muid for userAgent " + userAgent + ". Autodeletion will not be scheuled.");
            return;
        } else {
            if (log.isDebugEnabled()) log.debug(CLASSNAME + ".subscriberScheduleAutodeletion muid for userAgent " + userAgent + " is " + muid);
        }

        int[] AutoProvProfRetention = subscriberProfileManager.subscriberGetIntegerAttribute(
                userAgent, "MOIPAutoProvProfileRetention");

        if ((AutoProvProfRetention == null) || (AutoProvProfRetention[0] == 0)) {
            if (log.isDebugEnabled()) log.debug(CLASSNAME + ".subscriberScheduleAutodeletion: Autodeletion is off; " +
                    "will not schedule autodeletion event for userAgent " + userAgent);
            return;
        }
        int expiresI = AutoProvProfRetention[0];

        try {
            fileName = OutboundNotification.getAutodeletionFileName(userAgent);
            if (log.isDebugEnabled()) log.debug(CLASSNAME + ".subscriberScheduleAutodeletion: File is: " + fileName);

            Properties newProp = new Properties();
            newProp.put(AutoProvProfExpiryScheduler.MUID, muid);
            newProp.put(AutoProvProfExpiryScheduler.USER_AGENT, userAgent);
            //long expiresL = System.currentTimeMillis() + 60*1000*expiresI;  // int minutes to long milliseconds
            long expiresL = System.currentTimeMillis() + 24*60*60*1000*expiresI;  // int days to long milliseconds

            // Check if the file already exists in order to cancel the previous expiry timer
            if(mfsEventManager.fileExists(userAgent, fileName, true)){
                if (log.isDebugEnabled()) log.debug(CLASSNAME + ".subscriberScheduleAutodeletion: File already exists - replacing and canceling the previous expiry event");
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
            log.error(CLASSNAME + ".subscriberScheduleAutodeletion: Cannot write to file " + fileName + ": " + e.getMessage(),  e);
            return;
        } catch (Exception e) {
            log.error(CLASSNAME + ".subscriberScheduleAutodeletion: Exception occured: " + e.getMessage(),  e);
            return;
        }

        return;
    }

    public void subscriberAddDistributionList(String phoneNumber, String distListNumber) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberAddDistributionList is called with phoneNumber = " + phoneNumber + ", distListNumber = " + distListNumber );
        }
        subscriberProfileManager.subscriberAddDistributionList(phoneNumber, distListNumber);
    }

    public void subscriberDeleteDistributionList(String phoneNumber, String distListNumber) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberDeleteDistributionList is called with phoneNumber = " + phoneNumber + ", distListNumber = " + distListNumber );
        }
        subscriberProfileManager.subscriberDeleteDistributionList(phoneNumber, distListNumber);
    }

    public boolean subscriberExist(String phoneNumber) {
		Object perf = null;
		if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberExist is called with phoneNumber = " + phoneNumber);
        }
    	try{
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.subscriberExist");
            }
            return subscriberProfileManager.subscriberExist(phoneNumber);
		}finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }

    public int subscriberExistAsync(String phoneNumber) {
        return 0;
    }

    public boolean subscriberExist(int transactionId) {
        return false;
    }

    /**
     * Return the list of identities associated to inbox of a given h=number.
     * @return String array of all "tel" identities
     */
    public String[] subscriberGetIdetitiesAsString(String phoneNumber) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetIdetitiesAsString is called with phoneNumber = " + phoneNumber);
        }
    	return subscriberProfileManager.subscriberGetIdentitiesAsString(phoneNumber, "tel");
    }

    /**
     * Return the list of identities associated to inbox of a given h=number.
     * @return String array of all "scheme" identities
     */
    public String[] subscriberGetIdetitiesAsString(String phoneNumber, String scheme) {
        if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetIdetitiesAsString is called with phoneNumber = " + phoneNumber + " and scheme = " + scheme);
        }
        return subscriberProfileManager.subscriberGetIdentitiesAsString(phoneNumber, scheme);
    }

    public String[] subscriberGetStringAttribute(String phoneNumber, String attrName) {
		Object perf = null;
		if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetStringAttribute is called with phoneNumber = " + phoneNumber + ", attrName = " + attrName );
        }
    	try{
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.subscriberGetStringAttribute");
            }
            return subscriberProfileManager.subscriberGetStringAttribute(phoneNumber, attrName);
		}finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
    }

    public int[] subscriberGetIntegerAttribute(String phoneNumber, String attrName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetIntegerAttribute is called with phoneNumber = " + phoneNumber + ", attrName = " + attrName );
        }
        return subscriberProfileManager.subscriberGetIntegerAttribute(phoneNumber, attrName);
    }

    public boolean[] subscriberGetBooleanAttribute(String phoneNumber, String attrName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetBooleanAttribute is called with phoneNumber = " + phoneNumber + ", attrName = " + attrName );
        }
        return subscriberProfileManager.subscriberGetBooleanAttribute(phoneNumber, attrName);
    }

    public String[] subscriberGetCosStringAttribute(String phoneNumber, String attrName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetCosStringAttribute is called with phoneNumber = " + phoneNumber + ", attrName = " + attrName );
        }
        return subscriberProfileManager.subscriberGetCosStringAttribute(phoneNumber, attrName);
    }

    public int[] subscriberGetCosIntegerAttribute(String phoneNumber, String attrName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetCosIntegerAttribute is called with phoneNumber = " + phoneNumber + ", attrName = " + attrName );
        }
        return subscriberProfileManager.subscriberGetCosIntegerAttribute(phoneNumber, attrName);
    }

    public boolean[] subscriberGetCosBooleanAttribute(String phoneNumber, String attrName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetCosBooleanAttribute is called with phoneNumber = " + phoneNumber + ", attrName = " + attrName );
        }
        return subscriberProfileManager.subscriberGetCosBooleanAttribute(phoneNumber, attrName);
    }

    public String[] subscriberGetDistributionListIds(String phoneNumber) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetDistributionListIds is called with phoneNumber = " + phoneNumber );
        }
        return subscriberProfileManager.subscriberGetDistributionListIds(phoneNumber);
    }

    public String subscriberGetDistributionListMsid(String phoneNumber, String distListNumber) {
        if (log.isDebugEnabled()) {
            log.debug("subscriberGetDistributionListMsid is called with phoneNumber = " + phoneNumber +" distListNumber = "+distListNumber);
        }
        return subscriberProfileManager.subscriberGetDistributionListMsid(phoneNumber, distListNumber);
    }

    public IMediaObject subscriberGetGreeting(String phoneNumber, String greetingType, String mediaType, String cdgNumber) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetGreeting is called with phoneNumber = " + phoneNumber +
            		", greetingType = " + greetingType + ", mediaType = " + mediaType + ", cdgNumber = " + cdgNumber);
        }
        return subscriberProfileManager.subscriberGetGreeting(phoneNumber, greetingType, mediaType, cdgNumber);
    }

    public int subscriberGetGreetingAsync(String phoneNumber, String greetingType, String mediaType, String cdgNumber) {
        return 0;
    }

    public IMediaObject subscriberGetGreeting(int transactionId) {
        return null;
    }

    public int subscriberGetMailbox(String phoneNumber) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetMailbox is called with phoneNumber = " + phoneNumber );
        }
        return subscriberGetMailbox(phoneNumber, null, null, null);
    }

    //ToDo fix call with all params (only phonenumber is used now)
    /*
     * @deprecated
     */
    public int subscriberGetMailbox(String phoneNumber, String mailHost, String accountId, String accountPwd) {
        //Checks first if phoneNumber is mapped to an mailboxid. If not it retrieves
        //an IMailbox from the SubscriberProfileManager and a new id is assigned via the MailboxManager
        //and is returned.
    	Object perf = null;
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetMailbox is called with phoneNumber = " + phoneNumber +
            		", mailHost = " + mailHost + ", accountId = " + accountId + ", accountPwd = " + accountPwd );
        }
        try {
	    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.subscriberGetMailbox(phoneNumber,mailHost,accountId,accountPwd)");
	        }

	        Integer id = mailboxManager.getMailboxIds().get(phoneNumber);
	        if (id != null) {
	            return id;
	        }

	        IMailbox iMailbox = subscriberProfileManager.subscriberGetMailbox(phoneNumber);
	        if (iMailbox != null) {
	            id = mailboxManager.subscriberGetMailboxId(iMailbox);
	            mailboxManager.getMailboxIds().put(phoneNumber, id);
	            return id;
	        }
	        throw new PlatformAccessException(
	                EventType.DATANOTFOUND, "subscriberGetMailbox:phoneNumber=" + phoneNumber, "phoneNumber is not a valid subscriber");
        } finally {
        	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
        }
     }

    public String subscriberGetOperatorName(String phoneNumber) {
        // Delay the creation of the GlobalDirectoryAccessUtil to the first call.
        if (this.globalDirectoryAccessUtil == null) {
            this.globalDirectoryAccessUtil = new GlobalDirectoryAccessUtil();
        }
        String operatorName = null;
        try {
            operatorName = this.globalDirectoryAccessUtil.getOperatorName(phoneNumber);
        } catch (PlatformAccessException e) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberGetOperatorName:phoneNumber=" + phoneNumber, e);
        }
        return operatorName;
    }

    public String operatorGetSubscriberSingleStringAttribute(String operatorName, String phoneNumber, String attributeName) {
        // Delay the creation of the GlobalDirectoryAccessUtil to the first call.
        if (this.globalDirectoryAccessUtil == null) {
            this.globalDirectoryAccessUtil = new GlobalDirectoryAccessUtil();
        }
        String [] attributeValues = null;
        try {
            attributeValues = this.globalDirectoryAccessUtil.getSubscriberStringAttribute(operatorName, phoneNumber, attributeName);
        } catch (PlatformAccessException e) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "operatorGetSubscriberSingleStringAttribute:operatorName="+operatorName+",phoneNumber="+phoneNumber+"attributeName="+attributeName, e);
        }
        if(attributeValues != null){
        return attributeValues[0];
    }
        else {
        	return null;
        }
    }

    public Boolean subscriberIsBeingMigrated(String phoneNumber) {
        // Delay the creation of the GlobalDirectoryAccessUtil to the first call.
        if (this.globalMigrationAccessUtil == null) {
            this.globalMigrationAccessUtil = new GlobalMigrationAccessUtil();
        }
        Boolean result = null;
        try {
            result = this.globalMigrationAccessUtil.isBeingMigrated(phoneNumber);
        } catch (PlatformAccessException e) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberIsBeingMigrated:phoneNumber=" + phoneNumber, e);
        }
        return result;
    }
    
    
    public IMediaObject subscriberGetSpokenName(String phoneNumber, String mediaType) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberGetSpokenName is called with phoneNumber = " + phoneNumber +
            		", mediaType = " + mediaType);
        }
        return subscriberProfileManager.subscriberGetSpokenName(phoneNumber, mediaType);
    }

    public int subscriberGetSpokenNameAsync(String phoneNumber) {
        return 0;
    }

    public IMediaObject subscriberGetSpokenName(int transactionId) {
        return null;
    }

    public void subscriberSetStringAttribute(String phoneNumber, String attrName, String[] attrValues, Modification.Operation op) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberSetStringAttribute is called with phoneNumber = " + phoneNumber +
            		", attrName = " + attrName + ", attrValues = " + (attrValues != null ? Arrays.asList(attrValues) : null) );
        }
        subscriberProfileManager.subscriberSetStringAttribute(phoneNumber, attrName, attrValues, op);
        if("MOIPActiveGreetingId".equalsIgnoreCase(attrName)){
            executionContext.getSession().setData(GREETING_CHANGED_KEY, Boolean.TRUE);
        }
        sendProfileChangeMdr(phoneNumber);
    }

    public void subscriberSetStringAttribute(String phoneNumber, String attrName, String[] attrValues) {
    	subscriberSetStringAttribute(phoneNumber, attrName, attrValues, Modification.Operation.REPLACE);
    }

    public void subscriberSetIntegerAttribute(String phoneNumber, String attrName, int[] attrValues) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberSetIntegerAttribute is called with phoneNumber = " + phoneNumber +
            		", attrName = " + attrName + ", attrValues = " + attrValues);
        }
        subscriberProfileManager.subscriberSetIntegerAttribute(phoneNumber, attrName, attrValues);
        sendProfileChangeMdr(phoneNumber);
    }

    public void subscriberSetBooleanAttribute(String phoneNumber, String attrName, boolean[] attrValues) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberSetBooleanAttribute is called with phoneNumber = " + phoneNumber +
            		", attrName = " + attrName + ", attrValues = " + attrValues );
        }
        subscriberProfileManager.subscriberSetBooleanAttribute(phoneNumber, attrName, attrValues);
        sendProfileChangeMdr(phoneNumber);
    }

    public void subscriberSetGreeting(String phoneNumber, String greetingType, String mediaType, String cdgNumber, IMediaObject greeting) {
        subscriberSetGreeting(phoneNumber, greetingType, mediaType, cdgNumber, greeting, null);
    }

    public void subscriberSetGreeting(String phoneNumber, String greetingType, String mediaType, String cdgNumber, IMediaObject greeting, String duration) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberSetGreeting is called with phoneNumber = " + phoneNumber +
            		", greetingType = " + greetingType + ", mediaType = " + mediaType + ", cdgNumber = " + cdgNumber + ", greeting = " + greeting );
        }
        subscriberProfileManager.subscriberSetGreeting(phoneNumber, greetingType, mediaType, cdgNumber, greeting, duration);
        executionContext.getSession().setData(GREETING_CHANGED_KEY, Boolean.TRUE);
    }

    public void subscriberSetSpokenName(String phoneNumber, String mediaType, IMediaObject spokenName) {
        subscriberSetSpokenName(phoneNumber, mediaType, spokenName, null);
    }

    public void subscriberSetSpokenName(String phoneNumber, String mediaType, IMediaObject spokenName, String duration) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberSetSpokenName is called with phoneNumber = " + phoneNumber +
            		", mediaType = " + mediaType + ", spokenName = " + spokenName );
        }
        subscriberProfileManager.subscriberSetSpokenName(phoneNumber, mediaType, spokenName, duration);
        executionContext.getSession().setData(GREETING_CHANGED_KEY, Boolean.TRUE);
    }

    public boolean subscriberRemoveFromCache(String phoneNumber) {
        if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:subscriberRemoveFromCache is called with phoneNumber = " + phoneNumber);
        }        
        return subscriberProfileManager.subscriberRemoveFromCache(phoneNumber);
    }
    
    
//    public void subscriberCreate(String[] attrNames, String[] attrValues, String adminUid, String cosName) {
//        subscriberProfileManager.subscriberCreate(attrNames, attrValues, adminUid, cosName);
//    }
//
//    public void subscriberDelete(String telephoneNumber, String adminUid) {
//        subscriberProfileManager.subscriberDelete(telephoneNumber, adminUid);
//    }

    public String systemAnalyzeNumber(String rule, String phoneNumber, String informationContainingRegionCode) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemAnalyzeNumber is called with rule = " + rule +
            		", phoneNumber = " + phoneNumber + ", informationContainingRegionCode = " + informationContainingRegionCode );
        }
        IAnalysisInput iAnalysisInput = numberAnalyzer.getAnalysisInput();
        iAnalysisInput.setRule(rule);
        iAnalysisInput.setNumber(phoneNumber);
        iAnalysisInput.setInformationContainingRegionCode(informationContainingRegionCode);

        try {
            return numberAnalyzer.analyzeNumber(iAnalysisInput);
        } catch (NumberAnalyzerException e) {
            String msg = "systemAnalyzeNumber";
            String reason = e.getReason();
            if (reason != null) msg += ":" + reason;

            throw new PlatformAccessException(EventType.NUMBERANALYSIS, msg, e.getMessage());
        }
    }

    public String systemGetConfigurationParameter(String group, String parameterName) {
    	String CONF_FILE_EXTENSION = "conf";
    	String DOT = ".";
    	String result = null;

    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemGetConfigurationParameter is called with group = " + group +
            		", parameterName = " + parameterName );
        }
    	try {
    		int position = group.indexOf(DOT);
    		if (position > -1) {
    			String applicationName = group.substring(0, position);
    			String combinedParameterName = group.substring(position+1) + parameterName;
    			IGroup applicationConfiguration = configuration.getGroup(applicationName + DOT + CONF_FILE_EXTENSION);
    			if (applicationConfiguration != null) {
    				result = applicationConfiguration.getString(combinedParameterName);
    			} else {
    				throw new ConfigurationException("Couldn't get a configuration object for " + applicationName + DOT + CONF_FILE_EXTENSION + " !");
    			}
    		} else {
    			throw new ConfigurationException("The group " + group + " doesn't contain a '.'! Unexpected format of the group name!");
    		}
    	} catch (ConfigurationException e) {
        	log.error(new PlatformAccessException(EventType.DATANOTFOUND,
                    "systemGetConfigurationParameter:parameterName=" + parameterName, e).getDescription());
            throw new PlatformAccessException(EventType.DATANOTFOUND,
                    "systemGetConfigurationParameter:parameterName=" + parameterName, e);
        }
        return result;
    }

    public String systemGetConfigurationTableParameter(String applicationName, String tableName, String tableItemKey, String parameterName){
    	String result = null;
    	if (log.isDebugEnabled()) {
    		log.debug("PlatformAccessImpl:systemGetConfigurationTableParameter is called with applicationName = " + applicationName +
    				", tableName = " + tableName + ", tableItemKey = " + tableItemKey + ", parameterName = " + parameterName );
    	}
    	try{
    		IGroup applicationConfiguration = configuration.getGroup(applicationName + ".conf");
    		if (applicationConfiguration != null) {
    			result = applicationConfiguration.getTableParameter(tableName, tableItemKey, parameterName);
    		} else {
        		throw new ConfigurationException("Couldn't get a configuration object for " + applicationName + ".conf!");
        	}
    	} catch (ConfigurationException e) {
    		log.error(new PlatformAccessException(EventType.DATANOTFOUND,
    				"systemGetConfigurationTableParameter:tableItemKey=" + tableItemKey + ",parameterName=" + parameterName, e).getDescription());
    		throw new PlatformAccessException(EventType.DATANOTFOUND,
    				"systemGetConfigurationTableParameter:tableItemKey=" + tableItemKey + ",parameterName=" + parameterName, e);
    	}
    	return result;
    }

    /** Is this method really used? It seems that it was used by ChargingAccountManager, the old code. **/
    /*public String systemGetConfigurationGroupParameter(String group,
			String parameterName, String groupIdName, String groupIdValue) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemGetConfigurationGroupParameter is called with group = " + group +
            		", parameterName = " + parameterName + ", groupIdName = " + groupIdName + ", groupIdValue = " + groupIdValue );
        }
		try {
			List<IGroup> groupList = configuration.getGroups(group);
			Iterator<IGroup> it = groupList.iterator();
			while (it.hasNext()) {
				IGroup iGroup = it.next();

				try {
					String value = iGroup.getString(groupIdName);
					if (value.equalsIgnoreCase(groupIdValue)) {
						try {
							return iGroup.getString(parameterName);
						} catch (ConfigurationException e) {
							throw new PlatformAccessException(
									EventType.DATANOTFOUND,
									"systemGetConfigurationGroupParameter:parameterName="
											+ parameterName, e);
						}
					}
				} catch (ConfigurationException e) {
					throw new PlatformAccessException(EventType.DATANOTFOUND,
							"systemGetConfigurationGroupParameter:groupIdName="
									+ groupIdName, e);
				}
			}
			throw new PlatformAccessException(EventType.DATANOTFOUND,
					"systemGetConfigurationGroupParameter:parameterName="
							+ parameterName, "No groupIdValue " + groupIdValue
							+ " found");
		} catch (ConfigurationException e) {
			throw new PlatformAccessException(EventType.DATANOTFOUND,
					"systemGetConfigurationGroupParameter:group=" + group, e);
		}
	}*/

    public IMediaObject[] systemGetMediaContent(String mediaContentResourceType, String mediaContentIdentity) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemGetMediaContent is called with mediaContentResourceType = " + mediaContentResourceType +
            		", mediaContentIdentity = " + mediaContentIdentity);
        }
        return mediaManager.systemGetMediaContent(mediaContentResourceType, mediaContentIdentity);
    }

    public IMediaObject[] systemGetMediaContent(String mediaContentResourceType, String mediaContentIdentity,
                                                IMediaQualifier[] qualifier) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemGetMediaContent is called with mediaContentResourceType = " + mediaContentResourceType +
            		", mediaContentIdentity = " + mediaContentIdentity + ", qualifier = " + qualifier );
        }
        return mediaManager.systemGetMediaContent(mediaContentResourceType, mediaContentIdentity, qualifier);
    }

    public String[] systemGetMediaContentIds(String mediaContentResourceType, IMediaQualifier[] qualifier) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemGetMediaContentIds is called with mediaContentResourceType = " + mediaContentResourceType +
            		", qualifier = " + (qualifier != null ? Arrays.asList(qualifier) : null));
        }
        return mediaManager.systemGetMediaContentIds(mediaContentResourceType, qualifier);
    }

    public String[] systemGetSubscribers(String attribute, String value) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemGetSubscribers is called with attribute = " + attribute +
            		", value = " + value);
        }
        return subscriberProfileManager.systemGetSubscribers(attribute, value);
    }

    public boolean systemIsAsyncFinished(int transactionId) {
        return false;
    }

    public void systemSendServiceRequest(String hostName, String serviceName, int ValidityTime, boolean ReportIndication, String [] parameterNames, String [] parameterValues) {

        // clear the old service response
        serviceResponse = null;
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemSendServiceRequest is called with hostName = " + hostName +
            		", serviceName = " + serviceName + ", ValidityTime = " + ValidityTime + ", ReportIndication = " + ReportIndication
            		+ ", parameterNames = " + parameterNames + ", parameterValues = " + parameterValues);
        }
        if(serviceName == null || parameterNames == null || parameterValues == null){
            throw new PlatformAccessException(
                    EventType.SYSTEMERROR, "systemSendServiceRequest: parameter was null. Parameters:"+
                    "hostName:" + hostName +", serviceName:"+serviceName+", validityTime:"+ValidityTime+", ReportIndication"+
                    ReportIndication+", parameterNames:"+parameterNames,", parameterValues:"+parameterValues);
        }
        if(parameterNames.length != parameterValues.length){
            throw new PlatformAccessException(
                    EventType.SYSTEMERROR, "systemSendServiceRequest: parameterNames and parameterValues arrays had different size."+
                    " parameterNames size: "+parameterNames.length+", parameterValues size:"+parameterValues.length);
        }
        // don't allow null in the parameter arrays
        for (int i=0;i<parameterNames.length;i++) {
            String parameterName = parameterNames[i];
            if(parameterName == null){
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, "systemSendServiceRequest: parameterNames array contained null at position "+i);
            }
        }
        for (int i=0;i<parameterValues.length;i++) {
            String parameterValue = parameterValues[i];
            if(parameterValue == null){
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, "systemSendServiceRequest: parameterValues array contained null at position "+i);
            }
        }
        IServiceRequestManager serviceRequestManager = executionContext.getServiceRequestManager();
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId(serviceName);
        serviceRequest.setValidityTime(ValidityTime);
        serviceRequest.setResponseRequired(ReportIndication);
        for(int i=0;i<parameterNames.length;i++){
            serviceRequest.setParameter(parameterNames[i], parameterValues[i]);
        }
        if(hostName == null){
            serviceResponse = serviceRequestManager.sendRequest(serviceRequest);
        } else {
            serviceResponse = serviceRequestManager.sendRequest(serviceRequest, hostName);
        }
        // ServiceRequestManager made an error if a response was requested but there was none
        if(ReportIndication && serviceResponse == null){
            throw new PlatformAccessException(
                    EventType.SYSTEMERROR, "systemSendServiceRequest:a null response from ServiceRequestManager. Parameters:" +
                    "hostName:" + hostName +", serviceName:"+serviceName+", validityTime:"+ValidityTime+", ReportIndication"+
                    ReportIndication+", parameterNames:"+parameterNames,", parameterValues:"+parameterValues);
        }
    }

    public String systemGetServiceResponseParameter(String parameterName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemGetServiceResponseParameter is called with parameterName = " + parameterName);
        }
        if(parameterName == null){
            throw new PlatformAccessException(
                    EventType.SYSTEMERROR, "systemGetServiceResponseParameter: application supplied null for parameter name");
        }
        if(serviceResponse == null){
            throw new PlatformAccessException(
                    EventType.SYSTEMERROR, "systemGetServiceResponseParameter: application tried to retrieve a parameter" +
                    " but there was no serviceResponse. parameterName:"+parameterName);
        }
        Object parameter = serviceResponse.getParameter(parameterName);
        if(parameter == null){
            throw new PlatformAccessException(
                    EventType.DATANOTFOUND, "systemGetServiceResponseParameter: parameter "+parameterName+" did not exist in the response");
        }
        return parameter.toString();
    }

    public String systemGetServiceResponseHeaderParameter(String parameterName) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemGetServiceResponseHeaderParameter is called with parameterName = " + parameterName);
        }
        if(parameterName == null){
            throw new PlatformAccessException(
                    EventType.SYSTEMERROR, "systemGetServiceResponseHeaderParameter: application supplied null for parameter name");
        }
        if(serviceResponse == null){
            throw new PlatformAccessException(
                    EventType.SYSTEMERROR, "systemGetServiceResponseHeaderParameter: application tried to retrieve a parameter" +
                    " but there was no serviceResponse. parameterName:"+parameterName);
        }
        if(parameterName.equals("statuscode")){
            return Integer.toString(serviceResponse.getStatusCode());
        } else if(parameterName.equals("statustext")){
            return serviceResponse.getStatusText();
        } else if(parameterName.equals("clientid")){
            return serviceResponse.getClientId();
        } else if(parameterName.equals("transactionid")){
            return Integer.toString(serviceResponse.getTransactionId());
        }
        throw new PlatformAccessException(
                EventType.SYSTEMERROR, "systemGetServiceResponseHeaderParameter: application tried to retrieve non-defined header parameter: "+parameterName);
    }

    public void systemSetMediaResources(String language, String voiceVariant, String videoVariant) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemSetMediaResources is called with language = " + language +
            		", voiceVariant = " + voiceVariant + ", videoVariant = " + videoVariant );
        }
        mediaManager.systemSetMediaResources(language, voiceVariant, videoVariant);
    }

    public void systemSetMediaResource(String mediaContentResourceType, String language, String voiceVariant, String videoVariant) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemSetMediaResource is called with mediaContentResourceType = " + mediaContentResourceType +
            		", language = " + language + ", voiceVariant = " + voiceVariant + ", videoVariant = " + videoVariant );
        }
        mediaManager.systemSetMediaResource(mediaContentResourceType, language, voiceVariant, videoVariant);
    }

    public void systemSetEarlyMediaResource(String language, String voiceVariant, String videoVariant) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemSetEarlyMediaResource is called with language = " + language +
            		", voiceVariant = " + voiceVariant + ", videoVariant = " + videoVariant );
        }
        mediaManager.systemSetEarlyMediaResource(language, voiceVariant, videoVariant);
    }
    
    public void systemSetEarlyMediaResource(String language, String voiceVariant, String videoVariant,
                                            String[] extraProprietaryHeadersNames, String[] extraProprietaryHeadersValues) {
        if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemSetEarlyMediaResource is called with language = " + language +
                    ", voiceVariant = " + voiceVariant + ", videoVariant = " + videoVariant +
                    ", extraProprietaryHeadersNames = " + (extraProprietaryHeadersNames != null ? Arrays.asList(extraProprietaryHeadersNames) : null) +
                    ", extraProprietaryHeadersValues = " + (extraProprietaryHeadersValues != null ? Arrays.asList(extraProprietaryHeadersValues) : null) );
        }
        
        if(extraProprietaryHeadersNames == null){
            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSetEarlyMediaResource: extraProprietaryHeadersNames was null");
        }
        if(extraProprietaryHeadersValues == null){
            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSetEarlyMediaResource: extraProprietaryHeadersValues was null");
        }
        if(extraProprietaryHeadersNames.length != extraProprietaryHeadersValues.length){
            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSetEarlyMediaResource: extraProprietaryHeadersNames array was size "+extraProprietaryHeadersNames.length +" and extraProprietaryHeadersValues array was size "+ extraProprietaryHeadersValues.length);
        }
       
        ArrayList<ExtensionHeader> headers = new ArrayList<ExtensionHeader>(extraProprietaryHeadersNames.length);
        SipHeaderFactory sipHeaderFactory = CMUtils.getInstance().getSipHeaderFactory();
        
        for (int i=0;i<extraProprietaryHeadersNames.length;i++){
                    
            ExtensionHeader header;
            try {
                 
                header = sipHeaderFactory.createProprietaryHeader(extraProprietaryHeadersNames[i], extraProprietaryHeadersValues[i]);
            } catch(ParseException e) {
                throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSetEarlyMediaResource: Could not parse extraProprietaryHeaders name " + extraProprietaryHeadersNames[i] + " with value " + extraProprietaryHeadersValues[i], e);
            }
            
            headers.add(header);
        }
        executionContext.getSession().setData(PROPRIETARY_HEADER_FOR_RESPONSE_KEY, headers.toArray(new ExtensionHeader[headers.size()]));
        
        systemSetEarlyMediaResource(language, voiceVariant, videoVariant); 
    }
    
    public void systemSetProprietaryHeadersForResponse(String proprietaryHeaderForResponseKey, String[] extraProprietaryHeadersNames,
                                                      String[] extraProprietaryHeadersValues)
        throws PlatformAccessException {
        
        if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemSetProprietaryHeadersForResponse is called with proprietaryHeaderForResponseKey = " +
                    proprietaryHeaderForResponseKey +
                    ", extraProprietaryHeadersNames = " +
                    (extraProprietaryHeadersNames != null ? Arrays.asList(extraProprietaryHeadersNames) : null) +
                    ", extraProprietaryHeadersValues = " +
                    (extraProprietaryHeadersValues != null ? Arrays.asList(extraProprietaryHeadersValues) : null) );
            }
        
        if (proprietaryHeaderForResponseKey == null || proprietaryHeaderForResponseKey.length() == 0) {
            throw new PlatformAccessException(EventType.SYSTEMERROR,
                    "systemSetProprietaryHeadersForResponse: proprietaryHeaderForResponseKey is null or zero-length");
        }
        
        if (extraProprietaryHeadersNames == null) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSetProprietaryHeadersForResponse: extraProprietaryHeadersNames is null");
        }
        
        if (extraProprietaryHeadersValues == null) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSetProprietaryHeadersForResponse: extraProprietaryHeadersValues is null");
        }
        
        if (extraProprietaryHeadersNames.length != extraProprietaryHeadersValues.length) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSetProprietaryHeadersForResponse: extraProprietaryHeadersNames array is size " +
                    extraProprietaryHeadersNames.length + " and extraProprietaryHeadersValues array is size " +
                    extraProprietaryHeadersValues.length);
        }

        ArrayList<ExtensionHeader> headers = new ArrayList<ExtensionHeader>(extraProprietaryHeadersNames.length);
        SipHeaderFactory sipHeaderFactory = CMUtils.getInstance().getSipHeaderFactory();

        for (int i=0;i<extraProprietaryHeadersNames.length;i++) {
            ExtensionHeader header;
            try {
                header = sipHeaderFactory.createProprietaryHeader(extraProprietaryHeadersNames[i], extraProprietaryHeadersValues[i]);
            } catch(ParseException e) {
                throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSetProprietaryHeadersForResponse: Could not parse extraProprietaryHeaders name " + extraProprietaryHeadersNames[i] + " with value " + extraProprietaryHeadersValues[i], e);
            }

            headers.add(header);
        }
        
        executionContext.getSession().setData(proprietaryHeaderForResponseKey, headers.toArray(new ExtensionHeader[headers.size()]));
    }
    
    public void systemSetPartitionRestriction(boolean limit) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemSetPartitionRestriction is called with limit = " + limit);
        }
        subscriberProfileManager.systemSetPartitionRestriction(limit);
    }


    public void systemSetProperty(String name, String value) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemSetProperty is called with name = " + name +
            		", value = " + value);
        }
        if (executionContext instanceof VXMLExecutionContext) {
            ((VXMLExecutionContext) executionContext).getProperties().putProperty(name, value);
        }
    }

    public String systemGetServiceRequestParameter(String name) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemGetServiceRequestParameter is called with name = " + name);
        }
        ServiceRequest serviceRequest = (ServiceRequest) executionContext.getSession().
                getData(ISession.SERVICE_REQUEST);

        if (serviceRequest != null) {
            String param = (String) serviceRequest.getParameter(name);
            if (log.isDebugEnabled()) {
                log.debug("In systemGetServiceRequestParameter, value for " + name + "=" + param);
            }
            return param;
        }
        throw new PlatformAccessException(EventType.SYSTEMERROR, "systemGetServiceRequestParameter:name=" + name,
                "ServiceRequest was null in session");
    }

    public void systemSendSIPMessage(String messageName, String[] paramNames, String[] paramValues)throws IllegalArgumentException{
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:systemSendSIPMessage is called with messageName = " + messageName +
            		", paramNames = " + (paramNames != null ? Arrays.asList(paramNames) : null) +
            		", paramValues = " + (paramValues != null ? Arrays.asList(paramValues) : null) );
        }
    	Object perf = null;
    	try {
	    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.systemSendServiceResponse");
	        }
	    	if(messageName == null){
	            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSendSIPMessage: messageName was null");
	        }
	        if(paramNames == null){
	            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSendSIPMessage: paramNames was null");
	        }
	        if(paramValues == null){
	            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSendSIPMessage: paramValues was null");
	        }
	        if(paramNames.length != paramValues.length){
	            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSendSIPMessage: paramNames array was size "+paramNames.length +" and paramValues array was size "+ paramValues.length);
	        }
	        if(! (executionContext instanceof CCXMLExecutionContext)){
	            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSendSIPMessage: method was not invoked from CCXML");
	        }

	        // end of error checks: real code starts
	        CCXMLExecutionContext ccxmlExecutionContext = (CCXMLExecutionContext) executionContext;
	        CallManager callManager = ccxmlExecutionContext.getCallManager();

	        List<NamedValue<String,String>> parameters = new ArrayList<NamedValue<String,String>>(paramNames.length);
	        for (int i=0;i<paramNames.length;i++){
	            NamedValue<String,String> nameValue = new NamedValue<String,String>(paramNames[i], paramValues[i]);
	            parameters.add(nameValue);
	        }
	        superviseSIPResponse();
	        // find the mailboxid
	        callManager.sendSipMessage(messageName, ccxmlExecutionContext.getEventDispatcher(), executionContext.getSession(), parameters);
    	} finally {
          	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
  				CommonOamManager.profilerAgent.exitCheckpoint(perf);
  			}
        }
    }

    private void superviseSIPResponse() {
        int callManagerWaitTimeout = 60000; // millisecs. in case we can't read from config
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:superviseSIPResponse is called." );
        }
        try {
            callManagerWaitTimeout = configuration.getGroup(RuntimeConstants.CONFIG.GROUP_NAME).getInteger(RuntimeConstants.CONFIG.CALL_MANAGER_WAIT_TIME);
        } catch (Exception e) {
            log.warn("Failed to read parameter "+RuntimeConstants.CONFIG.CALL_MANAGER_WAIT_TIME+" from configuration group "+
                    RuntimeConstants.CONFIG.GROUP_NAME+", using "+callManagerWaitTimeout +" milliseconds");
        }
        String messageForFiredEvent = "Expected event for systemSendSIPMessage did not arrive in time";
        String[] eventNames = {Constants.Event.MOBEON_PLATFORM_SIPMESSAGERESPONSEEVENT};
        CCXMLExecutionContext ex = (CCXMLExecutionContext) executionContext;
        ex.waitForEvent(Constants.Event.ERROR_REQUEST_TIMEOUT, messageForFiredEvent,callManagerWaitTimeout, null,null, eventNames);
    }


    public void systemSendServiceResponse(int statusCode, String statusText, String[] paramName, String[] paramValue) {
    	Object perf = null;
    	try {
	    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("PlatformAccessImpl.systemSendServiceResponse");
	        }
	    	if (log.isDebugEnabled()) {
	            log.debug("PlatformAccessImpl:systemSendServiceResponse, statusCode=" + statusCode + ", statusText=" + statusText +
	                    ", paramName=" + (paramName != null ? Arrays.asList(paramName) : null) +
	                    ", paramValue=" + (paramValue != null ? Arrays.asList(paramValue) : null));
	        }
	        ServiceResponse serviceResponse = new ServiceResponse();
	        serviceResponse.setStatusCode(statusCode);
	        serviceResponse.setStatusText(statusText);

	        if (paramName != null && paramValue != null) {
	            if (paramName.length != paramValue.length) {
	                throw new PlatformAccessException(
	                        EventType.SYSTEMERROR, "systemSendServiceResponse:paramName.length != paramValue.length");
	            }
	            for (int i = 0; i < paramName.length; i++) {
	                serviceResponse.setParameter(paramName[i], paramValue[i]);
	            }
	        }
	        String sessionId = executionContext.getSession().getId();

            executionContext.getServiceRequestManager().sendResponse(sessionId, serviceResponse);
        } catch (ServiceRequestManagerException e) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "systemSendServiceResponse:", e);
	    } finally {
	     	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
					CommonOamManager.profilerAgent.exitCheckpoint(perf);
				}
	     }
    }

    public void trafficEventSend(String eventName, String[] propertyName, String[] propertyValue, boolean restrictEndUsers) {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:trafficEventSend is called with eventName = " + eventName +
            		", propertyName = " + (propertyName != null ? Arrays.asList(propertyName) : null) +
            		", propertyValue = " + (propertyValue != null ? Arrays.asList(propertyValue) : null) +
            		", restrictEndUsers = " + restrictEndUsers );
        }

    	if(MfsClient.EVENT_LOGOUTINFORMATION.equals(eventName)){
    	    Object obj = executionContext.getSession().getData(GREETING_CHANGED_KEY);
    	    if(obj instanceof Boolean){
    	        if((Boolean) obj){
    	            //Need to add a property to the logout information to inform that the greeting have changed
    	            ArrayList<String> propertyNameList = new ArrayList<String>();
    	            ArrayList<String> propertyValueList = new ArrayList<String>();
    	            if(propertyName != null){
    	                for(String property : propertyName){
    	                    propertyNameList.add(property);
    	                }
    	            }
    	            if(propertyValue != null){
                        for(String value : propertyValue){
                            propertyValueList.add(value);
                        }
                    }

    	            propertyNameList.add(GREETING_CHANGED_KEY);
    	            propertyValueList.add("true");

    	            propertyName = propertyNameList.toArray(new String[propertyNameList.size()]);
    	            propertyValue = propertyValueList.toArray(new String[propertyValueList.size()]);
    	        }
    	    }
    	}

    	trafficEventManager.trafficEventSend(eventName, propertyName, propertyValue, restrictEndUsers);
    }

    public enum EventName {
        MAKECALLSTOP, SLAMDOWN, VOICEDEPOSIT, VIDEODEPOSIT, LOGIN, UNKNOWN;

        public String toString() {
            return name().toUpperCase();
        }

        public static EventName getValue(String value) {
            try {
                return valueOf(value.toUpperCase());
            } catch (Exception e) {
                return UNKNOWN;
            }
        }
    }

    public static final String NUMBER_OF_RETURN_CALLS = "NumberOfReturnCalls";
    public static final String NUMBER_OF_SLAMDOWNS = "NumberOfSlamdowns";
    public static final String NUMBER_OF_DEPOSIT_CALLS = "NumberOfDepositCalls";
    public static final String NUMBER_OF_OUTDIAL_NOTIF = "NumberOfOutdialNotif";

    /**
     * Increments a specific counter based on the traffic event specified in the
     * call flow.
     */
    public void trafficEventCount(String eventName, String[] propertyName, String[] propertyValue) {
        if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:trafficEventCount is called with eventName = " + eventName + ", propertyName = "
                    + (propertyName != null ? Arrays.asList(propertyName) : null) + ", propertyValue = "
                    + (propertyValue != null ? Arrays.asList(propertyValue) : null));
        }

        switch (EventName.getValue(eventName)) {
        case MAKECALLSTOP:
            if (propertyName != null && propertyName.length > 0) {
                int index = (Arrays.asList(propertyName)).indexOf("logintype");
                if (index >= 0 && propertyValue[index].equalsIgnoreCase("retrieval")) {
            if (log.isDebugEnabled())
                log.debug("PlatformAccessImpl:trafficEventCount incrementing " + NUMBER_OF_RETURN_CALLS);
                    systemIncrementAutoResetPerfCounter(NUMBER_OF_RETURN_CALLS);
                }
            }
            break;
        case SLAMDOWN:
            if (log.isDebugEnabled())
                log.debug("PlatformAccessImpl:trafficEventCount incrementing " + NUMBER_OF_SLAMDOWNS);
            systemIncrementAutoResetPerfCounter(NUMBER_OF_SLAMDOWNS);
            // No break here since we also need to increment the
            // NumberofDepositCalls on a slamdown
        case VOICEDEPOSIT:
        case VIDEODEPOSIT:
            if (log.isDebugEnabled())
                log.debug("PlatformAccessImpl:trafficEventCount incrementing " + NUMBER_OF_DEPOSIT_CALLS);
            systemIncrementAutoResetPerfCounter(NUMBER_OF_DEPOSIT_CALLS);
            break;
        case LOGIN:
            if (propertyName != null && propertyName.length > 0) {
                int index = (Arrays.asList(propertyName)).indexOf("logintype");
                if (index >= 0 && propertyValue[index].equalsIgnoreCase("outdialnotification")) {
            if (log.isDebugEnabled())
                log.debug("PlatformAccessImpl:trafficEventCount incrementing " + NUMBER_OF_OUTDIAL_NOTIF);
                    systemIncrementAutoResetPerfCounter(NUMBER_OF_OUTDIAL_NOTIF);
            }
            }

            try {
                int index = (Arrays.asList(propertyName)).indexOf("username");
                if (index >= 0 && propertyValue.length > index) {
                    String username = propertyValue[index];
                    if(username!=null && username.length()>0)
                    {
                        username = getNormalizedPhoneNumber(username);
                    	if (log.isDebugEnabled()) {
                        	log.debug("PlatformAccessImpl:trafficEventCount Adding inbox access event for subscriber: " + username);
                    	}
                    	getEventRecorder().recordEvent(new BasicEvent(username));
                    }
                    else
                    {
                        log.warn("trafficEventCount username is empty");
                    }
                }
            } catch (IOException e) {
                log.warn("PlatformAccessImpl:trafficEventCount Unable to record inbox access event: " + e.getMessage());
            }

            break;
        default:
            break;
        }
    }

    public void close() {
    	if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:close is called." );
        }
    }

    public void systemLog(int severity, String msg) {
        if (severity == 0) {
            log.fatal(msg);
        } else if (severity == 1) {
            log.error(msg);
        } else if (severity == 2) {
            log.warn(msg);
        } else if (severity == 3) {
            if (log.isInfoEnabled()) log.info(msg);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }
        }
    }

    public void systemSendDTMF(String utterance, int paus_interval) {
        // TODO:  Not implemented. Will be used by VVA phase 2
    }


    public boolean systemIsSimulated() {
        return false;
    }

    public String systemGetMediaContentPath() {
        ModuleCollection moduleCollection = executionContext.getModuleCollection();
        if (moduleCollection.getBaseURI() != null) {
            String path = moduleCollection.getBaseURI().toString();
            path = mediaManager.systemGetMediaContentPath(path);

            if (log.isDebugEnabled()) {
                log.debug("PlatformAccessImpl:systemGetMediaContentPath, path=" + path);
            }

            return path;
        } else {
            log.error("PlatformAccessImpl:systemGetMediaContentPath, baseURI is null");
        }
        return "";
    }

    /**
     * @see PlatformAccess interface for description
     */
    public boolean systemIsAppendSupported(String resourceType) {
    	 if (log.isDebugEnabled()) {
             log.debug("PlatformAccessImpl:systemIsAppendSupported is called with resourceType =" + resourceType);
         }
    	MimeType contentType = mediaManager.systemGetMediaContentResourceContentType(
    			resourceType, contentTypeMapper);

    	MediaHandler mediaHandler = mediaHandlerFactory.getMediaHandler(contentType);
    	if (mediaHandler != null && mediaHandler.hasConcatenate())
    		return true;
    	else
    		return false;

    }

    public IMediaObject getCachedMedia(String url) {
        if (log.isDebugEnabled()) {
            log.debug("PlatformAccessImpl:getCachedMedia, url=" + url);
        }
        if (executionContext instanceof VXMLExecutionContext) {
            ResourceLocator resourceLocator = ((VXMLExecutionContext) executionContext).getResourceLocator();
            resourceLocator.setMediaObjectFactory(((VXMLExecutionContext) executionContext).getMediaObjectFactory());
            return resourceLocator.getMedia(url, "0s", null);
        }
        return null;
    }

    public String systemGetApplicationPath() {
        ModuleCollection moduleCollection = executionContext.getModuleCollection();
        if (moduleCollection.getBaseURI() != null) {
            String baseURI = moduleCollection.getBaseURI().toString();
            if (log.isDebugEnabled()) {
                log.debug("PlatformAccessImpl:getApplicationPath, baseURI=" + baseURI);
            }
            return baseURI;
        } else {
            log.error("PlatformAccessImpl:getApplicationPath, baseURI is null");
        }
        return "";
    }
    
    public String[] crpDoGetUserInfo(String aPhoneNumber, String applicationName) {
        CommonOamManager commonOamManager = CommonOamManager.getInstance();
        OAMManager oamManager = commonOamManager.getMcdOam();
        if (oamManager == null){
            if (log.isDebugEnabled()) {
                log.debug("PlatformAccessImpl:crpDoGetUserInfo Could not find MCD OAMManager");
            }
        }
        ConfigManager localAuthenticationBackendConfigurationManager = getAuthenticationConfigManager(applicationName);
        String[] attributes = {"", ""};
        
        CrpDirectInterface crpInterface = new CrpDirectInterface(oamManager, localAuthenticationBackendConfigurationManager);
        
        
        if (crpInterface.doGetUserInfo(aPhoneNumber)) {
            attributes[0] = crpInterface.getGetUserInfoResStatusCode();
            // only read the SPID if response is 60 - successful
            if (attributes[0].compareTo("60") == 0) {
                log.debug("PlatformAccessImpl:crpDoGetUserInfo response is 60-successful; getting SPID");
                attributes[1] = crpInterface.getGetUserInfoResSpid();
            } else {
                log.debug("PlatformAccessImpl:crpDoGetUserInfo response is not 60; setting SPID as empty string");
            }
        }
        
        return attributes;
    }
    
    public String[] crpDoBlindAuthenticationRegistration(String aPhoneNumber, String applicationName) {
        CommonOamManager commonOamManager = CommonOamManager.getInstance();
        OAMManager oamManager = commonOamManager.getMcdOam();
        if (oamManager == null){
            if (log.isDebugEnabled()) {
                log.debug("PlatformAccessImpl:crpDoBlindAuthenticationRegistration Could not find MCD OAMManager");
            }
        }
        ConfigManager localAuthenticationBackendConfigurationManager = getAuthenticationConfigManager(applicationName);
        String[] attributes = {"", ""};
        
        CrpDirectInterface crpInterface = new CrpDirectInterface(oamManager, localAuthenticationBackendConfigurationManager);
        
        if (crpInterface.doBlindAuthenticationRegistration(aPhoneNumber)) {
            attributes[0] = crpInterface.getBlindAuthRegResStatusCode();
            attributes[1] = crpInterface.getBlindAuthRegResResSpid();
        }
        
        return attributes;
    }
    
    public boolean checkPin(String aPhoneNumber, String aPin, String applicationName) {
        return checkPin(aPhoneNumber, aPin, applicationName, null, null);
    }
    
    public boolean checkPin(String aPhoneNumber, String aPin, String applicationName, String attributeName, String algorithmName){
    	boolean isPasswordValid = false;

    	try {
    		AuthenticationMechanism mechanism = getAuthenticationMechanism(applicationName, attributeName, algorithmName);

    		if (mechanism != null){
    			if (log.isDebugEnabled()) {
    				log.debug("PlatformAccessImpl:checkPin retrieved mechanism and will now check the pin");
    			}
    			isPasswordValid = mechanism.isPasswordValid(aPhoneNumber, aPin);
    			if (log.isDebugEnabled()) {
    				log.debug("PlatformAccessImpl:checkPin retrieved mechanism and authenticateResult was " + String.valueOf(isPasswordValid));
    			}
    		}
    		else {
    			if (log.isDebugEnabled()) {
    				log.debug("PlatformAccessImpl:checkPin retrieved null mechanism from factory. Deny login.");
    			}
    		}
    		if (log.isDebugEnabled()) {
    			log.debug("PlatformAccessImpl:checkPin returning variable isPasswordValid=" + String.valueOf(isPasswordValid));
    		}
    		return isPasswordValid;
    	}
    	catch (AuthenticationException e){
    		if (log.isDebugEnabled()) {
    			log.debug("PlatformAccessImpl:checkPin got an AuthenticationException (" + e.getMessage() + ") while using mechanism to check pin. Check the CRP or MCD is up.");
    		}
    		throw new PlatformAccessException(EventType.ACCOUNT,
                    "checkPin " + aPhoneNumber,e);
    	}
    	catch (Exception e) {
    		if (log.isDebugEnabled()) {
				log.debug("PlatformAccessImpl:checkPin got an unknown exception (" + e.getMessage() + ") while using mechanism to check pin");
			}
    	}
    	if (log.isDebugEnabled()) {
			log.debug("PlatformAccessImpl:checkPin something bad happened - returning the default (false)");
		}
    	return false;
  	}

    public void changePassword(String userName, String oldPassword, String newPassword, String reason, String applicationName) throws AuthenticationException {
        changePassword(userName, oldPassword, newPassword, reason, applicationName, null, null);
    }
    
    public void changePassword(String userName, String oldPassword, String newPassword, String reason, String applicationName, String attributeName, String algorithmName) throws AuthenticationException {
    	try {
    		AuthenticationMechanism mechanism = getAuthenticationMechanism(applicationName, attributeName, algorithmName);

    		if (mechanism != null){
    			if (log.isDebugEnabled()) {
    				log.debug("PlatformAccessImpl:changePassword retrieved mechanism and will now set the pin");
    			}
    			mechanism.changePassword(userName, oldPassword, newPassword, "PIN Changed from TUI by user");
    			if (log.isDebugEnabled()) {
    				log.debug("PlatformAccessImpl:changePassword Back from ChangePassword");
    			}
    			sendProfileChangeMdr(userName);
    		}
    		else {
    			if (log.isDebugEnabled()) {
    				log.debug("PlatformAccessImpl:changePassword retrieved null mechanism from factory. Don't do the pin change");
    			}
    		}
    	}
    	catch (AuthenticationException e){
    		if (log.isDebugEnabled()) {
    			log.debug("PlatformAccessImpl:changePassword got an AuthenticationException (" + e.getMessage() + ") while using mechanism to change pin. Check the CRP or MCD is up.");
    		}
    		throw new PlatformAccessException(EventType.ACCOUNT,
                    "changePassword " + userName, e);
    	}
    	catch (Exception e) {
    		if (log.isDebugEnabled()) {
				log.debug("PlatformAccessImpl:changePassword got an unknown exception (" + e.getMessage() + ") while using mechanism to change pin");
			}
    	}
    }


    public void generatePassword(String aPhoneNumber, boolean sendSMS, String reason, String applicationName) throws PlatformAccessException{

		AuthenticationMechanism mechanism = getAuthenticationMechanism(applicationName);
		if (mechanism != null){
			if (log.isDebugEnabled()) {
				log.debug("PlatformAccessImpl:generatePassword() retrieved mechanism and will now attempt to generate the password for user " + aPhoneNumber);
			}
			try {
				mechanism.generatePassword(aPhoneNumber, sendSMS, reason);
				if (log.isDebugEnabled()) {
					log.debug("PlatformAccessImpl:generatePassword() : successfully generated the password for user " + aPhoneNumber);
				}

			}catch (AuthenticationException ae){
				if (log.isDebugEnabled()) {
					log.debug("PlatformAccessImpl:generatePassword() caught AuthenticationException " );
				}
	    		throw new PlatformAccessException(EventType.ACCOUNT, "generatePassword" + aPhoneNumber, ae);
			}
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug("PlatformAccessImpl:generatePassword() retrieved null mechanism from factory. Deny login.");
			}
		}
    }

    public void registerUser(String aPhoneNumber, String applicationName)    throws AuthenticationException{
		AuthenticationMechanism mechanism = getAuthenticationMechanism(applicationName);
		if (mechanism != null){
			if (log.isDebugEnabled()) {
				log.debug("PlatformAccessImpl:registerUser() retrieved mechanism and will now register the user " + aPhoneNumber);
			}
			try {
				mechanism.registerUser(aPhoneNumber);
				if (log.isDebugEnabled()) {
					log.debug("PlatformAccessImpl:registerUser(): retrieved mechanism and isPasswordSet result was " );
				}

			}catch (AuthenticationException ae){
				if (log.isDebugEnabled()) {
					log.debug("PlatformAccessImpl:registerUser():  caught AuthenticationException" );
				}
	    		throw new PlatformAccessException(EventType.ACCOUNT, "generatePassword" + aPhoneNumber, ae);
			}
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug("PlatformAccessImpl:registerUser retrieved null mechanism from factory. Deny login.");
			}
		}
    }

    public boolean isPinSet(String aPhoneNumber, String applicationName){
        return isPinSet(aPhoneNumber, applicationName, null, null);
    }

    public boolean isPinSet(String aPhoneNumber, String applicationName, String attributeName, String algorithmName) {
    	boolean isPasswordSet = false;

    	try {
    		AuthenticationMechanism mechanism = getAuthenticationMechanism(applicationName, attributeName, algorithmName);

    		if (mechanism != null){
    			if (log.isDebugEnabled()) {
    				log.debug("PlatformAccessImpl:isPinSet retrieved mechanism and will now check the pin");
    			}
    			isPasswordSet = mechanism.isPasswordSet(aPhoneNumber);
    			if (log.isDebugEnabled()) {
    				log.debug("PlatformAccessImpl:isPinSet retrieved mechanism and isPasswordSet result was " + String.valueOf(isPasswordSet));
    			}
    		}
    		else {
    			if (log.isDebugEnabled()) {
    				log.debug("PlatformAccessImpl:isPinSet retrieved null mechanism from factory. Deny login.");
    			}
    		}
    		if (log.isDebugEnabled()) {
    			log.debug("PlatformAccessImpl:isPinSet returning variable isPasswordSet=" + String.valueOf(isPasswordSet));
    		}
    		return isPasswordSet;
    	}
    	catch (AuthenticationException e){
    		if (log.isDebugEnabled()) {
    			log.debug("PlatformAccessImpl:isPinSet got an AuthenticationException (" + e.getMessage() + ") while using mechanism to check if pin is set. Check the CRP or MCD is up.");
    		}
    		throw new PlatformAccessException(EventType.ACCOUNT,
                    "isPinSet " + aPhoneNumber, e);

    	}
    	catch (Exception e) {
    		if (log.isDebugEnabled()) {
				log.debug("PlatformAccessImpl:isPinSet got an unknown exception (" + e.getMessage() + ") while using mechanism to check if pin is set");
			}
    	}
    	if (log.isDebugEnabled()) {
			log.debug("PlatformAccessImpl:checkPin something bad happened - returning the default (false)");
		}
    	return false;
  	}

    protected ConfigManager getAuthenticationConfigManager(String applicationName){
        String pathToConfigurationFile = systemGetConfigurationParameter(applicationName + ".authentication",
                "MechanismConfigurationFile");
        
        if ((pathToConfigurationFile != null) && (pathToConfigurationFile.length() != 0)) {
            ConfigManager theConfigManager;
            try {
                theConfigManager = new PropertyFileConfigManager(pathToConfigurationFile);
                if (log.isDebugEnabled()) {
                    log.debug("PlatformAccessImpl:getAuthenticationConfigManager>>Succesfully create ConfigManager over file "
                            + pathToConfigurationFile);
                    log.debug("PlatformAccessImpl:getAuthenticationConfigManager>>toString of configurationmanager");
                    log.debug(theConfigManager.toString());
                }
                return theConfigManager;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            ConfigManager theConfigManager;
            try {
                theConfigManager = new PropertyFileConfigManager(null);
                if (log.isDebugEnabled()) {
                    log.debug("PlatformAccessImpl:getAuthenticationConfigManager>>pathToConfigurationFile was null or empty");
                    log.debug("PlatformAccessImpl:getAuthenticationConfigManager>>toString of configurationmanager");
                    log.debug(theConfigManager.toString());
                }
                return theConfigManager;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }

    protected ConfigManager getHardcodedAuthenticationConfigManager(){
    	ConfigManager theConfigManager = new InMemoryConfigAgent();
    	try {
    		theConfigManager.setParameter("hostname", "142.133.151.38");
    		theConfigManager.setParameter("port", "8080");
    		theConfigManager.setParameter("service", "VM");
    		theConfigManager.setParameter("partner", "Abcxyz");
    		theConfigManager.setParameter("auditUserId", "Abcxyz");
    		theConfigManager.setParameter("auditOrganisationId", "Abcxyz");
    		theConfigManager.setParameter("auditSystemId", "mio2.0");
    		theConfigManager.setParameter("encryptionFile", "//opt//moip//config//passwordfile");
    	} catch (ConfigurationDataException e) {
        	if (log.isDebugEnabled()) {
    			log.debug("getAuthenticationConfigManager Problem creating ConfigManager instance for Authentication mechanism. Got exception with message " + e.getMessage());
    		}
    		e.printStackTrace();
    	}

    	return theConfigManager;
    }

    protected AuthenticationMechanism getAuthenticationMechanism(String applicationName){
        return getAuthenticationMechanism(applicationName, null, null);
    }
    
    protected AuthenticationMechanism getAuthenticationMechanism(String applicationName, String attributeName, String algorithmName){
    	AuthenticationMechanism mechanism = null;
    	if (log.isDebugEnabled()) {
    		log.debug("PlatformAccessImpl:getAuthenticationMechanism invoked.");
    	}
    	CommonOamManager commonOamManager = CommonOamManager.getInstance();
    	OAMManager oamManager = commonOamManager.getMcdOam();
    	if (oamManager == null){
    		if (log.isDebugEnabled()) {
    			log.debug("PlatformAccessImpl:getAuthenticationMechanism Could not find MCD OAMManager");
    		}
    	}
    	else {
	    	try {
	    		String authClassName = "";
	    		try {
	    			authClassName = systemGetConfigurationParameter(applicationName + ".authentication", "MechanismClassname");
	    		}
	    		catch (PlatformAccessException e){
	    			// ignore this, not specified in the file
	    			if (log.isDebugEnabled()) {
	    				log.debug("PlatformAccessImpl:getAuthenticationMechanism could not find authentication class name in configuration. Using default");
	    			}
	    			authClassName = "com.abcxyz.messaging.authentication.daf.DAFAuthenticationMechanism";
	    		}

	    		oamManager.getConfigManager().setParameter(AuthenticationFactory.AUTHENTICATION_MECHANISM_CLASSNAME,authClassName);

	    		if (log.isDebugEnabled()) {
	    			log.debug("PlatformAccessImpl:getAuthenticationMechanism using authentication class name " + authClassName);
	    		}

	    		String authClasspath = "";
	    		try {
	    			authClasspath = systemGetConfigurationParameter(applicationName + ".authentication", "MechanismClasspath");
	    		}
	    		catch (PlatformAccessException e){
	    			// Ignore this
	    			if (log.isDebugEnabled()) {
	    				log.debug("PlatformAccessImpl:getAuthenticationMechanism could not find authentication class path in configuration. Not specifying it");
	    			}
	    		}
	    		oamManager.getConfigManager().setParameter(AuthenticationFactory.AUTHENTICATION_MECHANISM_CLASSPATH,authClasspath);

	    		ConfigManager localAuthenticationBackendConfigurationManager = getAuthenticationConfigManager(applicationName);
	    		
	    		if (authClassName.contains("DAFAuthenticationMechanism")) {
	                if (attributeName != null) {
	                    localAuthenticationBackendConfigurationManager.setParameter(DAFAuthenticationMechanism.PASSWORD_ATTRIBUTE_PARAMETER, attributeName);
	                }
	                
	                if (algorithmName != null) {
                        localAuthenticationBackendConfigurationManager.setParameter(DAFAuthenticationMechanism.PASSWORD_ALGORITHM_PARAMETER, algorithmName);
	                }
	    		}
	    		
	    		mechanism = AuthenticationFactory.getAuthenticationMechanism(oamManager, localAuthenticationBackendConfigurationManager);

	    		if (mechanism != null){
	    			if (log.isDebugEnabled()) {
	    				log.debug("PlatformAccessImpl:getAuthenticationMechanism retrieved mechanism");
	    			}
	    		}
	    	}
	    	catch (Exception e){
				if (log.isDebugEnabled()) {
					log.debug("PlatformAccessImpl:getAuthenticationMechanism Unexpected exception " + e.getMessage());
				}
	    	}
    	}
    	return mechanism;
    }


    public void setUnconditionalDivertInHlr(String aPhoneNumber) {
    	try {
			ss7Manager.setUnconditionalDivertInHlr(aPhoneNumber);
		} catch (Ss7Exception e) {
			handleSs7Exception(e);
		}
    }

    public void setConditionalDivertInHlr(String aPhoneNumber) {
    	try {
			ss7Manager.setConditionalDivertInHlr(aPhoneNumber);
		} catch (Ss7Exception e) {
			handleSs7Exception(e);
		}
    }

    public void cancelUnconditionalDivertInHlr(String aPhoneNumber) {
    	try {
			ss7Manager.cancelUnconditionalDivertInHlr(aPhoneNumber);
		} catch (Ss7Exception e) {
			handleSs7Exception(e);
		}
    }

    public void cancelConditionalDivertInHlr(String aPhoneNumber) {
    	try {
			ss7Manager.cancelConditionalDivertInHlr(aPhoneNumber);
		} catch (Ss7Exception e) {
			handleSs7Exception(e);
		}
    }

    public Boolean getDivertStatusInHlr(String aPhoneNumber, String divertType){
    	Boolean result = false;

    	try {
			result = ss7Manager.getDivertStatusInHlr(aPhoneNumber, divertType);
		} catch (Ss7Exception e) {
			handleSs7Exception(e);
		}
		return result;
    }


    /**
     * @deprecated
     */
    public Boolean isSubscriberRoaming(String aPhoneNumber) {
        //Keep previous behaviour which was to default to not roaming when
        //- MSC number is not available
        //- Ss7Exception
        Boolean isRoaming = false;
        try {
            AnyTimeInterrogationResult result = ss7Manager.requestATI(aPhoneNumber);
            if(result.getMscNumber() != null){
                isRoaming = ss7Manager.isRoaming_ATI(result);
            }
        } catch (Ss7Exception e) {
            //assume not roaming
        }
        return isRoaming;
    }

    public Boolean isSubscriberLoggedIn(String telephoneNumber, int validityPeriodInMin){        
        if(validityPeriodInMin>=0){
            Boolean isSubscriberLoggedIn = mfsEventManager.loginFileExistsAndValidDate(telephoneNumber, validityPeriodInMin);
            if(log.isDebugEnabled())
            {
                log.debug("PlatformAccessImpl:isSubscriberLoggedIn:isSubscriberLoggedIn:" + isSubscriberLoggedIn);
            }
            return isSubscriberLoggedIn;
        }        
        else {
            if(log.isDebugEnabled())
            {
                log.debug("PlatformAccessImpl:isSubscriberLoggedIn: " +
                        "The validity period is < 0. (validityPeriod="+validityPeriodInMin+")");
            }
            return false;
        }
    }
    


    public int getSubscriberRoamingStatus(String aPhoneNumber) {
        int result = -1; // Roaming status is unknown.
        
        // VFE_NL MFD
        IGroup backEndGroup = null;
        boolean tryLoadBackendConf = false;
        try {
            backEndGroup = CommonOamManager.getInstance().getConfiguration().getGroup(BACK_END_CONF);
        } catch (Exception e) {
            log.warn("PlatformAccessImpl.getSubscriberRoamingStatus(): exception calling CommonOamManager.getInstance().getConfiguration().getGroup(GlobalDirectoryAccessUtil.BACK_END_CONF); " +
                     "will try to load : " + BACK_END_CONF + ": " + e);
            tryLoadBackendConf = true;
        }
        if (backEndGroup == null || tryLoadBackendConf) { // load backend.conf
            backEndGroup = loadBackendConfig();
            if(backEndGroup == null){
                log.error("PlatformAccessImpl.getSubscriberRoamingStatus(): failed to load " + BACK_END_CONF);
                return result;
            }
        }
        
        try {
            if (!(backEndGroup.getBoolean(ConfigParam.ENABLE_HLR_ACCESS))) {
                log.warn("PlatformAccessImpl.getSubscriberRoamingStatus(): Cm.enableHlrAccess in backend.conf is set to false; will not get toaming stat from HLR");
                return result;
            }
        } catch (Exception e) {
            log.error("PlatformAccessImpl.getSubscriberRoamingStatus(): failed to get value for " + ConfigParam.ENABLE_HLR_ACCESS + ": " + e);
            e.printStackTrace();
            return result;
        }
        
        String hlrMethod = "";
        try {
            hlrMethod = backEndGroup.getString(ConfigParam.SUBSCRIBER_STATUS_HLR_METHOD);
        } catch (Exception e) {
            log.error("PlatformAccessImpl.getSubscriberRoamingStatus(): failed to get value for " + ConfigParam.SUBSCRIBER_STATUS_HLR_METHOD + ": " + e);
            e.printStackTrace();
            return result;
        }
        

        if (ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_CUSTOM.equalsIgnoreCase(hlrMethod)) {
            log.debug("PlatformAccessImpl.getSubscriberRoamingStatus(): @@@HLR Access method = " + ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_CUSTOM + " to use HlrAccessManager");
            HlrAccessManager ham = null;            
            try {
                ham = CommonMessagingAccess.getInstance().getHlrAccessManager();
            } catch (Exception e) {
                ham = null;
            }
            if (ham == null) {
                log.info("PlatformAccessImpl.getSubscriberRoamingStatus(): cannot get CommonMessagingAccess.getInstance().getHlrAccessManager(); try get HlrAccessManager directly");
                try {
                    ham = HlrAccessManager.getInstance(backEndGroup.getString(ConfigParam.SUBSCRIBER_STATUS_HLR_METHOD_CUSTOM_CLASS_PATH),
                            backEndGroup.getString(ConfigParam.SUBSCRIBER_STATUS_HLR_METHOD_CUSTOM_CONFIG_FILE), new MoipOamManager());
                } catch (Exception e) {
                    log.error("PlatformAccessImpl.getSubscriberRoamingStatus(): exception getting config or calling HlrAccessManager.getInstance(): " + e);
                    e.printStackTrace();
                    return result;          
                }
                if (ham == null) {
                    log.error("PlatformAccessImpl.getSubscriberRoamingStatus(): failed to get HlrAccessMaanger by calling HlrAccessManager.getInstance()");
                    return result;
                }
                CommonMessagingAccess.getInstance().setHlrAccessManager(ham); // will be there for next times
            }
            try {
                result = ham.getSubscriberRoamingStatus(aPhoneNumber);            
            } catch (Exception e) {
                log.error("PlatformAccessImpl.getSubscriberRoamingStatus(): exception calling CommonMessagingAccess.getInstance().getHlrAccessManager() or   " + e);
                e.printStackTrace();
            }
            return result;
        }

        try {
            result = ss7Manager.getSubscriberRoamingStatus(aPhoneNumber);            
        } catch (Ss7Exception e) {
            handleSs7Exception(e);
        }
        return result;
        
    }
    
    private IGroup loadBackendConfig() {
        IGroup bg = null;
        Collection<String> configFilenames = new LinkedList<String>();
        String configFilename = "/opt/moip/config/backend/backend.conf";
        File backendXml = new File(configFilename);
        if (!backendXml.exists()) {
            log.error("PlatformAccessImpl.loadBackendConfig(): cannot find " + configFilename);
            return bg;
        }

        configFilenames.add(configFilename);
        IConfiguration configuration;
        try {
            configuration = new ConfigurationImpl(null,configFilenames,false);
            bg = configuration.getGroup(BACK_END_CONF);
        } catch (Exception e) {
            log.error("PlatformAccessImpl.loadBackendConfig(): error trying to init COmmonMessagingAccess with backend.conf: " + e);
            e.printStackTrace();
        } finally {
            return bg;
        }

    }


    
    private void handleSs7Exception(Ss7Exception ex) throws PlatformAccessException{

    	String eventType = EventType.UNKNOWN;


    	/**
    	 * The Ss7Exception has several types of errors which must be mapped to PlatformAccessException eventTypes
    	 */
    	switch (ex.getErrorType()){
    	case UNKNOWN_SUBSCRIBER_ERROR:
    		eventType = EventType.UNKNOWN_SUBSCRIBER_ERROR;
    		break;
		case SUPPLEMENTARY_SERVICE_ERROR:
    		eventType = EventType.SUPPLEMENTARY_SERVICE_ERROR;
    		break;
		case TEMPORARY_ERROR:
    		eventType = EventType.TEMPORARY_ERROR;
    		break;
		case IMSI_NOT_FOUND_ERROR:
    		eventType = EventType.IMSI_NOT_FOUND_ERROR;
    		break;
		case DIVERT_FAILED_ERROR:
    		eventType = EventType.IMSI_NOT_FOUND_ERROR;
    		break;
		case INVALID_PARAMETER:
    		eventType = EventType.INVALID_PARAMETER;
    		break;
		case SS7MGR_CREATION_ERROR:
    		eventType = EventType.SS7MGR_CREATION_ERROR;
    		break;
		case UNKNOWN:
    		eventType = EventType.UNKNOWN;
    		break;
    	}

    	throw new PlatformAccessException(eventType, ex.getMessage());
    }


	@Override
	public boolean phoneNumberCanSubscribeToMWIForMailbox(
			String userAgentNumber, String mailboxId) {

		boolean hasMwi = false;
		boolean hasDeliveryProfile = false;
		//boolean hasFilter = false;

		try {

			String services[] = subscriberProfileManager.subscriberGetStringAttribute(mailboxId, DAConstants.ATTR_SERVICES);
			String deliveryProfileStrings[] = subscriberProfileManager.subscriberGetStringAttribute(mailboxId, DAConstants.ATTR_DELIVERY_PROFILE);

			for (String service: services) {
				log.debug("PlatformAccessImpl:phoneNumberCanSubscribeToMWIForMailbox:service:"+service);
				if(service.contains("mwi_notification")){
					hasMwi = true;
				}
			}
			log.debug("PlatformAccessImpl:phoneNumberCanSubscribeToMWIForMailbox:hasMwi:"+Boolean.toString(hasMwi));
			if (!hasMwi) {
				return false;
			}

			for (String deliveryProfileString: deliveryProfileStrings) {

				log.debug("PlatformAccessImpl:phoneNumberCanSubscribeToMWIForMailbox:deliveryProfileString:"+deliveryProfileString);

				final int NUMBERS = 0;
				final int TYPE = 1;
				final int INFO = 2;

				StringTokenizer st = new StringTokenizer(deliveryProfileString, ";");

				String[] deliveryProfileStringArray = {"","",""};

				int i=0;
	            while (st.hasMoreTokens()&& i<deliveryProfileStringArray.length)
	            {
	            	deliveryProfileStringArray[i++]=st.nextToken().toUpperCase();
	            }

				if(deliveryProfileStringArray[TYPE].contains("MWI") &&
				   deliveryProfileStringArray[INFO].contains("I") ){
				   if (log.isDebugEnabled()) {
					   log.debug("Checking if " + userAgentNumber.toLowerCase() + " matches the " + deliveryProfileStringArray[NUMBERS].toLowerCase() + " string" );
				   }
				   if (deliveryProfileStringArray[NUMBERS].equalsIgnoreCase(userAgentNumber)) {
					   log.debug("Result is true");
					   hasDeliveryProfile = true;
					   break;
				   }

				}
			}
		} catch (Exception e) {}
        if (hasDeliveryProfile) {
        	return true;
        } else {
        	return false;
        }
	}

	public boolean systemMWIInitialSubscribe(String mailboxId, String userAgent, String expires, String dialogInfo) {

	    if (log.isDebugEnabled()) log.debug(CLASSNAME + ".systemMWIInitialSubscribe called - mailboxId=" + mailboxId + " userAgentNumber=" + userAgent + " expires(s)=" + expires + " dialogInfo=" + dialogInfo);
	    String fileName = null;

	    try {
	        fileName = OutboundNotification.getSubscriptionFileName(userAgent);
	        if (log.isDebugEnabled()) log.debug(CLASSNAME + ".systemMWIInitialSubscribe: File is: " + fileName);

	        //TODO - a lot of CPU in this validation due to the re-parsing - should be maybe change to plain tag values instead of entire header
	        // or there is maybe a way to send the Properties type over the CCXM interface
	        ByteArrayInputStream byteStream = new ByteArrayInputStream(dialogInfo.getBytes());
	        Properties newProp = new Properties();
	        newProp.load(byteStream);
	        int expiresI = Integer.parseInt(expires);
	        long expiresL = System.currentTimeMillis() + 1000*expiresI;  // milisec

	        // Check if the file already exists in order to cancel the previous expiry timer
	        if(mfsEventManager.fileExists(mailboxId, fileName, true)){
	            if (log.isDebugEnabled()) log.debug(CLASSNAME + ".systemMWIInitialSubscribe: File already exists - replacing and canceling the previous expiry event");
	            // cancel the previous expiration timer
	            Properties oldProp = mfsEventManager.getProperties(mailboxId, fileName);
	            if(oldProp != null){
	                //Here we don't care canceling the timer after the new private file is stored since it is obsolete anyway
	                SubscriptionExpiryScheduler.getInstance().cancelExpiry(oldProp.getProperty(SubscriptionExpiryScheduler.EXPIRY_EVENT_ID));
	            }
	        }
	        String eventId = SubscriptionExpiryScheduler.getInstance().scheduleExpiryTimer(mailboxId, userAgent, expiresL);
	        newProp.put(SubscriptionExpiryScheduler.EXPIRY_EVENT_ID, eventId);
	        //add the expiry date - required for the Expires header in the Notify request
	        newProp.put(SubscribeCall.EXPIRY_DATE, Long.toString(expiresL));
	        mfsEventManager.storeProperties(mailboxId, fileName, newProp);
	    } catch (IOException e) {
	        log.error(CLASSNAME + ".systemMWIInitialSubscribe: Cannot read properties from dialogInfo: " + e.getMessage(),  e);
	        return false;
	    } catch (TrafficEventSenderException e) {
	        log.error(CLASSNAME + ".systemMWIInitialSubscribe: Cannot write to file " + fileName + ": " + e.getMessage(),  e);
	        return false;
	    } catch (Exception e) {
	        log.error(CLASSNAME + ".systemMWIInitialSubscribe: Exception occured: " + e.getMessage(),  e);
	        return false;
	    }

	    return true;
	}

	public boolean systemMWISubsequentSubscribe(String mailboxId, String userAgent, String expires, String dialogInfo) {

	    if (log.isDebugEnabled()) log.debug(CLASSNAME + ".systemMWISubsequentSubscribe called - mailboxId=" + mailboxId + " userAgentNumber=" + userAgent + " expires(s)=" + expires + " dialogInfo=" + dialogInfo);
	    String fileName = null;        
	    try {	  
	        fileName = OutboundNotification.getSubscriptionFileName(userAgent);
	        if (log.isDebugEnabled()) log.debug(CLASSNAME + ".systemMWISubsequentSubscribe: File is: " + fileName);

	        //TODO - a lot of CPU in this validation due to the re-parsing - should be maybe change to plain tag values instead of entire header
	        // or there is maybe a way to send the Properties type over the CCXM interface
	        ByteArrayInputStream byteStream = new ByteArrayInputStream(dialogInfo.getBytes());
	        Properties newProp = new Properties();
	        newProp.load(byteStream);
	        int expiresI = Integer.parseInt(expires);
	        long expiresL = System.currentTimeMillis() + 1000*expiresI;  // milisec

	        //TODO - should the info in the file be updated entirely with the new dialog info? What if the re-subscribe brings a new Route
	        //read the old dialog info from the file
	        Properties oldProp = mfsEventManager.getProperties(mailboxId, fileName);
	        if (log.isDebugEnabled()) log.debug(CLASSNAME + ".systemMWISubsequentSubscribe: Old dialog info: " + oldProp);

	        //validate if is the same dialog
	        To newTo = (To) new ToParser(newProp.getProperty(SIPHeader.TO).trim() + "\n").parse();
	        From oldFrom = (From) new FromParser(oldProp.getProperty(SIPHeader.FROM).trim() + "\n").parse();
	        if (!newTo.getTag().equals(oldFrom.getTag())) {
	            log.error(CLASSNAME + ".systemMWISubsequentSubscribe: New To tag doesn't match the old From tag");
	            return false;
	        }
	        To oldTo = (To) new ToParser(oldProp.getProperty(SIPHeader.TO).trim() + "\n").parse();
	        From newFrom = (From) new FromParser(newProp.getProperty(SIPHeader.FROM).trim() + "\n").parse();
	        if (!oldTo.getTag().equals(newFrom.getTag())) {
	            log.error(CLASSNAME + ".systemMWISubsequentSubscribe: New From tag doesn't match the old To tag");
	            return false;
	        }
	        if (!newProp.getProperty(SIPHeader.CALL_ID).trim().equals(oldProp.getProperty(SIPHeader.CALL_ID).trim())) {
	            log.error(CLASSNAME + ".systemMWISubsequentSubscribe: New CallId doesn't match the old CallId");
	            return false;
	        }
	        String oldEventID = oldProp.getProperty(SubscriptionExpiryScheduler.EXPIRY_EVENT_ID);
	        if (expiresI == 0) {
	            // un-subscribe
	            if (log.isDebugEnabled()) log.debug(CLASSNAME + ".systemMWISubsequentSubscribe: This is un-subscribe case");
	            //update the subscription state in the private file
	            //add the subscription state
	            SubscriptionState ssHeader = new SubscriptionState();
	            ssHeader.setState(SubscriptionState.TERMINATED);
	            oldProp.put(SIPHeader.SUBSCRIPTION_STATE, ssHeader.toString());
	            // cancel the expiry timer
	        } else {
	            if (log.isDebugEnabled()) log.debug(CLASSNAME + ".systemMWISubsequentSubscribe: This is re-subscribe case");
	            // cancel the old event and reschedule a new one
	            String eventId = SubscriptionExpiryScheduler.getInstance().scheduleExpiryTimer(mailboxId, userAgent, expiresL);
	            oldProp.put(SubscriptionExpiryScheduler.EXPIRY_EVENT_ID, eventId);
	            //add the expiry date - required for the Expires header in the Notify request
	            oldProp.put(SubscribeCall.EXPIRY_DATE, Long.toString(expiresL));
	        }

	        //TODO - there are quite some IOs here - should use the java nio - or use the new dialog info to generate the content of the file
	        mfsEventManager.storeProperties(mailboxId, fileName, oldProp);
	        SubscriptionExpiryScheduler.getInstance().cancelExpiry(oldEventID);

	    } catch (IOException e) {
	        log.error(CLASSNAME + ".systemMWISubsequentSubscribe: Cannot read properties from dialogInfo: " + e.getMessage(),  e);
	        return false;
	    } catch (ParseException e) {
	        log.error(CLASSNAME + ".systemMWISubsequentSubscribe: Parsing error: " + e.getMessage(), e);
	        return false;
	    } catch (TrafficEventSenderException e) {
	        log.error(CLASSNAME + ".systemMWISubsequentSubscribe: Cannot write to file " + fileName + ": " + e.getMessage(),  e);
	        return false;
	    } catch (Exception e) {
	        log.error(CLASSNAME + ".systemMWISubsequentSubscribe: Exception occured: " + e.getMessage(),  e);
	        return false;
	    }
	    return true;
	}


	public boolean systemRemoveSolicitedSubscription(String mailboxId, String userAgent) {
	    String fileName = null;
	    try {
	        fileName = OutboundNotification.getSubscriptionFileName(userAgent);
	        if (log.isDebugEnabled()) log.debug(CLASSNAME + ".systemRemoveSolicitedSubscription: Removing solicited subscription: " + fileName);

	        // cancel the expiry event
	        Properties oldProp = mfsEventManager.getProperties(mailboxId, fileName);			
	        // remove the subscription file
	        mfsEventManager.removeFile(mailboxId, fileName, true);
	        SubscriptionExpiryScheduler.getInstance().cancelExpiry(oldProp.getProperty(SubscriptionExpiryScheduler.EXPIRY_EVENT_ID));
	        return true;
	    } catch (TrafficEventSenderException e) {
	        log.error(CLASSNAME + ".systemRemoveSolicitedSubscription: Cannot remove the subscription file: " + fileName, e);
	        return false;
	    } catch (Exception e) {
	        log.error(CLASSNAME + ".systemRemoveSolicitedSubscription: Exception occured: " + e.getMessage(),  e);
	        return false;
	    }

	}

	public boolean checkSubcriptionExist(String mailboxId, String userAgent)
	{
	    try{
	        String fileName = OutboundNotification.getSubscriptionFileName(userAgent);
	        if (log.isDebugEnabled()) log.debug(CLASSNAME + ".checkSubcriptionExist():File is: " + fileName);
	        return mfsEventManager.fileExists(mailboxId, fileName, true);
	    } catch (Exception e) {
	        log.error(CLASSNAME + ".checkSubcriptionExist: Exception occured: " + e.getMessage(),  e);
	        return false;
	    }
	}

	public void systemSetSessionVariable(String name, String value)
	{
		executionContext.getSession().setData(name, value);
	}

	public String systemGetSessionVariable(String name)
	{
		Object obj = executionContext.getSession().getData(name);

		if(obj != null){
			return (String) obj;
		}
		return null;
    }

    public void systemIncrementAutoResetPerfCounter(String counterName) {
        PerformanceDataFactory perfDataFactory = CommonOamManager.getInstance().getPerformanceDataFactory();
        PerformanceData counter = perfDataFactory.getPerformanceDataAutoResetCounter(counterName);
        if (counter != null) {
            if (log.isDebugEnabled())
                log.debug(CLASSNAME + ".systemIncrementAutoResetPerfCounter : incrementing " + counterName);
            counter.increment();
            log.debug(counter.toString());
        } else
            log.warn(CLASSNAME
                    + ".systemIncrementAutoResetPerfCounter : Performance Manager not initialized.  We're probably in a design environment.  Counters won't be incremented.");
    }

    public void systemIncrementAutoResetPerfCounter(String counterName, long increment) {
        PerformanceDataFactory perfDataFactory = CommonOamManager.getInstance().getPerformanceDataFactory();
        PerformanceData counter = perfDataFactory.getPerformanceDataAutoResetCounter(counterName);
        if (counter != null) {
            if (log.isDebugEnabled())
                log.debug(CLASSNAME + ".systemIncrementAutoResetPerfCounter : incrementing " + counterName + " by " + increment);
            counter.increment(increment);
            log.debug(counter.toString());
        } else
            log.warn(CLASSNAME
                    + ".systemIncrementAutoResetPerfCounter : Performance Manager not initialized.  We're probably in a design environment.  Counters won't be incremented.");
    }

    public String[] getBroadcastAnnouncements(String subscriberPhoneNumber) {
	   if(log.isDebugEnabled()) {
		   log.debug("PlatformAccessImpl.getBroadcastAnnouncements for " + subscriberPhoneNumber);
	   }

       boolean featureEnabled = false;
       IGroup applicationConfiguration;
       try {
           applicationConfiguration = configuration.getGroup("vva.conf");
           if (applicationConfiguration != null) {
                String result = applicationConfiguration.getString("broadcastAnnouncementEnabled");
                if(result != null && result.equalsIgnoreCase("yes")) {
                    if(log.isDebugEnabled()) {
                        log.debug("PlatformAccessImpl.getBroadcastAnnouncements: feature is enabled");
                    }
                    featureEnabled = true;
                }
           }
       } catch (UnknownParameterException e) {
           log.error("PlatformAccessImpl.getBroadcastAnnouncements: unknownParameterException " + e.getMessage());
       } catch (GroupCardinalityException e) {
           log.error("PlatformAccessImpl.getBroadcastAnnouncements: GroupCardinalityException " + e.getMessage());
       } catch (UnknownGroupException e) {
           log.error("PlatformAccessImpl.getBroadcastAnnouncements: UnknownGroupException " + e.getMessage());
       }

       if(featureEnabled) {
	   ArrayList<BroadcastAnnouncement> list;
	   try {
	       list = BroadcastManager.getInstance().getBroadcastAnnouncements(subscriberPhoneNumber);
	   } catch (BroadcastException e1) {
		   throw new PlatformAccessException("BroadcastException", e1.getMessage());
	   }

	   if(log.isDebugEnabled()) {
		   log.debug("PlatformAccessImpl.getBroadcastAnnouncementMessageIds: number of broadcasts: " + list.size());
	   }

	   String[] result = new String[list.size()];
	   Iterator<BroadcastAnnouncement> itr = list.iterator();
	   int index = 0;
	   while(itr.hasNext()){
		   BroadcastAnnouncement ba = itr.next();
		   result[index] = ba.getName();
		   index++;
	   }

	   return result;
       } else {
           return new String[0];
       }
   }

   public int getBroadcastMessageId(String broadcastAnnouncementName) {
	   return getBroadcastMessageId(broadcastAnnouncementName, null, null, null);
   }

   public int getBroadcastMessageId(String broadcastAnnouncementName, String mediaType, String brand, String language) {
	   Integer mailboxId = mailboxManager.getMailboxIds().get(broadcastAnnouncementName);
	   BroadcastAnnouncement ba = null;
		   try {
		      ba = BroadcastManager.getInstance().getBroadcastAnnouncement(broadcastAnnouncementName);
		   } catch (BroadcastException e1) {
		      throw new PlatformAccessException("BroadcastException", e1.getMessage());
		   }

	   if (mailboxId == null) {
    	   MailboxProfile mailboxProfile = new MailboxProfile(ba.getMsid(), null, null);

    	   IMailbox iMailbox = null;
    	   try {
    		   iMailbox = mailboxAccountManager.getMailbox(null, mailboxProfile);
    	   } catch (MailboxException e) {
    		   log.error("PlatformAccessImpl.getBroadcastAnnoucementMessageIds: Mailbox exception: " + e.getMessage());
    		   return -1;
    	   }
    	   mailboxId = mailboxManager.subscriberGetMailboxId(iMailbox);
    	   mailboxManager.getMailboxIds().put(broadcastAnnouncementName, mailboxId);
       }

	   int folderId = mailboxGetFolder(mailboxId, "inbox");

	   String[] broadcastAnnouncementLanguages = ba.getLanguage();
	   String languageFilter = null;

	   if(broadcastAnnouncementLanguages != null && broadcastAnnouncementLanguages.length > 0) {
		   //BA language is not empty. This means that the operator has provisioned the broadcast
		   //with language specific versions. We match this with the subscriber's profile.
		   String brandedlanguage = "";
		   if(brand != null && brand.length() > 0 && language != null) {
			   brandedlanguage = brand + "_" + language;
		   }else if(language != null) {
			   brandedlanguage = language;
		   }

		   //Try to match brand and language
		   for(String balang: broadcastAnnouncementLanguages) {
			   if(brandedlanguage.equalsIgnoreCase(balang)) {
				   languageFilter = balang;
				   break;
			   }
		   }

		   //No match for brand and language, try to match only language
		   if(languageFilter == null) {
			   for(String balang: broadcastAnnouncementLanguages) {
				   if(language != null && language.equalsIgnoreCase(balang)) {
					   languageFilter = balang;
					   break;
				   }
			   }
		   }

	   }
	   if(languageFilter == null) {
           languageFilter = "default";
       }

	   //If the media type is video, try to get the video recorded broadcast; if no video ing, try to get the audio recording.
       if("video".equalsIgnoreCase(mediaType)){
           int messageListId = mailboxManager.mailboxGetMessageList(folderId, "video", "new,read,saved", "urgent,nonurgent", "", "fifo", languageFilter);
           int[] messageIds = mailboxGetMessages(messageListId);
           if(messageIds != null && messageIds.length > 0) {
               return messageIds[0];
           }
       }

	   int messageListId = mailboxManager.mailboxGetMessageList(folderId, "voice", "new,read,saved", "urgent,nonurgent", "", "fifo", languageFilter);
	   int[] messageIds = mailboxGetMessages(messageListId);
	   if(messageIds != null && messageIds.length > 0) {
		   return messageIds[0];
	   }else {
		   throw new PlatformAccessException("BroadcastException", "No message ids found");
	   }
   }


   public void setBroadcastPlayed(String subscriberPhoneNumber, String broadcastName) {
	   if(log.isDebugEnabled()) {
		   log.debug("PlatformAccessImpl.setBroadcastPlayed: broadcast " + broadcastName + " has been played for " + subscriberPhoneNumber);
	   }
	   String[] played = subscriberGetStringAttribute(subscriberPhoneNumber, DAConstants.ATTR_BROADCAST_ANNOUNCEMENT_PLAYED);

	   for(String p: played) {
		   if(p.equalsIgnoreCase(broadcastName)) {
			   return;
		   }
	   }
	   subscriberSetStringAttribute(subscriberPhoneNumber, DAConstants.ATTR_BROADCAST_ANNOUNCEMENT_PLAYED, new String[] {broadcastName}, Modification.Operation.ADD);
   }

   public String[] broadcastGetStringAttribute(String broadcastName, String attributeName) {
	   IDirectoryAccessBroadcastAnnouncement daBroadcast = DirectoryAccess.getInstance().lookupBroadcastAnnouncement(broadcastName);
	   if(daBroadcast != null){
		   return daBroadcast.getStringAttributes(attributeName);
	   }else {
		   throw new PlatformAccessException("BroadcastException", "The broadcast was not found");
	   }
   }

   public int[] broadcastGetIntegerAttribute(String broadcastName, String attributeName) {
	   IDirectoryAccessBroadcastAnnouncement daBroadcast = DirectoryAccess.getInstance().lookupBroadcastAnnouncement(broadcastName);
	   if(daBroadcast != null){
		   return daBroadcast.getIntegerAttributes(attributeName);
	   }else {
		   throw new PlatformAccessException("BroadcastException", "The broadcast was not found");
	   }
   }


   public boolean[] broadcastGetBooleanAttribute(String broadcastName, String attributeName) {
	   IDirectoryAccessBroadcastAnnouncement daBroadcast = DirectoryAccess.getInstance().lookupBroadcastAnnouncement(broadcastName);
	   if(daBroadcast != null){
		   return daBroadcast.getBooleanAttributes(attributeName);
	   }else {
		   throw new PlatformAccessException("BroadcastException", "The broadcast was not found");
	   }
   }


   public void setMailboxAccountManager(IMailboxAccountManager mailboxAccountManager) {
       this.mailboxAccountManager = mailboxAccountManager;
   }

   public String getNormalizedPhoneNumber(String phoneNumber) {
       String result = CommonMessagingAccess.getInstance().denormalizeNumber(phoneNumber);
       if(log.isDebugEnabled()) {
           log.debug("PlatformAccessImpl.normalizePhoneNumber: normalized phonenumber to " + result);
       }
       return result;
   }
   
   public int getPayloadFileCount(String telephoneNumber, String type) {
       int count = MfsEventManager.getPayloadFileCount(telephoneNumber, type);
       log.debug("Number of payload files for number " + telephoneNumber + " of event type " + type + " is: " + count);
       return count;
   }

   public String[] getMfsEventFiles(String telephoneNumber, final String eventName, String order) throws TrafficEventSenderException {
      return  mfsEventManager.getSendStatusEventFiles(telephoneNumber, eventName, order);
   }
   public TrafficEvent[] retrieveMfsEvents(String phoneNumber, String name) throws TrafficEventSenderException {
       try {
           return mfsEventManager.retrieveEvents( phoneNumber, name, true);
       } catch(TrafficEventSenderException e) {
           //For backward compatibility, do not throw exception to call flow if file does not exist or the file path could not be generated.
           if(e.getTrafficEventSenderExceptionCause() == TrafficEventSenderExceptionCause.PAYLOAD_FILE_DOES_NOT_EXIST ||
                   e.getTrafficEventSenderExceptionCause() == TrafficEventSenderExceptionCause.PAYLOAD_FILE_PATH_NOT_ACCESSIBLE) {
               return new TrafficEvent[]{};
           } else {
               throw e;
           }
       }
   }

   public void removeMfsEventFile(String telephoneNumber, String fileName) throws TrafficEventSenderException {
       mfsEventManager.removeFile(telephoneNumber, fileName, true);
   }

   public boolean isStorageOperationsAvailable(String originator, String recipient) {
       return mfsEventManager.isStorageOperationsAvailable(originator, recipient);
   }

   public boolean isProfileUpdatePossible(String telephoneNumber) {
       return subscriberProfileManager.isProfileUpdatePossible(telephoneNumber);

   }
   
   
   /**
    * Build the Mdr and call trafficEventsend to write the Mdr 
    * @param phoneNumber
    * @throws PlatformAccessException
    */
   private void sendProfileChangeMdr(String phoneNumber)throws PlatformAccessException{
       // getting subscriber profile and msid for the MDR use

       IDirectoryAccessSubscriber subscriberProfile = DirectoryAccess.getInstance().lookupSubscriber(MCDConstants.IDENTITY_SCHEME_TEL+":"+phoneNumber);
       
       String[] subProfilePhoneNumbers = subscriberProfile.getSubscriberIdentities(MCDConstants.IDENTITY_SCHEME_TEL);
       String msid = subscriberProfile.getSubscriberIdentity(MCDConstants.IDENTITY_SCHEME_MSID);;
       String mdrAttributes[][] = new String[2][];
       for (String profilePhoneNumber : subProfilePhoneNumbers){
           mdrAttributes[0] = new String[]{MdrConstants.USERNAME,MdrConstants.OBJECTTYPE , MdrConstants.EVENTTYPE,MdrConstants.EVENTREASON ,MdrConstants.OBJECTID};  
           mdrAttributes[1] = new String[]{profilePhoneNumber,String.valueOf(MdrConstants.OBJECT_TYPE),String.valueOf(MdrConstants.MODIFY),String.valueOf(MdrConstants.SUBSCRIBER_PROFILE_MODIFICATION),"msid:"+msid};
           mdrAttributes=getAttributesValues(subscriberProfile.getSubscriberProfile(),mdrAttributes);
           trafficEventManager.trafficEventSend("profilechange", mdrAttributes[0], mdrAttributes[1], false);
       }
   }
   
   private String[][] getAttributesValues(MoipProfile profile,String[][] tab){
       if(customAttributeList != null){
           int index = 0;
           for(int i = 0; i <  customAttributeList.size(); i++){
               String[] AttributeValues = null ;
               if (profile != null){
                   AttributeValues  = profile.getStringAttributes(customAttributeList.get(i));
                   if (AttributeValues != null && AttributeValues.length  > 0){
                       StringBuilder attributeValue = new StringBuilder();
                       tab[0] =  Arrays.copyOf(tab[0], tab[0].length+ 1);
                       tab[0][tab[0].length-1] = "p"+String.valueOf((char) ("a".charAt(0)+index));
                       Iterator<String> iterator =  Arrays.asList(AttributeValues).iterator();
                       while(iterator.hasNext()){
                           attributeValue.append(customAttributeList.get(i)+":"+iterator.next());
                           if(iterator.hasNext()){
                               attributeValue.append(",");
                           }
                       }
                       tab[1] = Arrays.copyOf(tab[1], tab[1].length+ 1);
                       tab[1][tab[1].length-1] = attributeValue.toString();
                       index++;
                   }
               }
           } 
       }
       return tab; 
   } 
   
   /**
    * To test getSubscriberRoamingStatus
    * @param args
    * @throws Exception
    */
   public static void main(String[] args) throws Exception {    
       String aPhoneNumber = args[0];       
       if (aPhoneNumber == null || aPhoneNumber.isEmpty()) {
           System.out.println("\nUsage: must pass a phonenumber as an argument");
           System.exit(1);
       }
       System.out.println("Phone number = " + aPhoneNumber);
       PlatformAccessImpl pai = new PlatformAccessImpl();       
       System.out.println("Roaming status = " + pai.getSubscriberRoamingStatus(aPhoneNumber));
   }
   private PlatformAccessImpl() {
       
   }

}
