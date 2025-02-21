/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.mail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.ParseException;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.MailDateFormat;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container3;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.Message;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.mfs.data.MessageFileHandle;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.message.MfsFileFolder;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.transcoderfacade.Transcoder;
import com.abcxyz.services.moip.ntf.coremgmt.DelayedEventTriggerHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.ContentFormatUtil;
import com.mobeon.common.cmnaccess.oam.MoipOamManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.masp.util.xml.ssml.Voice;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.mcd.McdUserInfo;

/**
 * NTF interpretation of an MFS message.
 */
public class NotificationEmail extends AMessageDepositInfo {

    private static final String CONTENT_DURATION = "Content-Duration";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String ADDITIONAL_PROPERTY = "addlproperty_";

    private String senderDisplayName = null;
    private String userAgentNumber = null;
    private boolean forced = false;
    private int messageSize = 0;
    private int numberOfAttachments = -1;
    private boolean quotaExceeded = false;
    private boolean quotaAlmostExceeded = false;

    private String lengthOfMessage = null;
    private String messageText = null;
    private Message message = null;
    private MimeMessage mimeMessage = null;
    private UserMailbox	userMailbox;
    private boolean vvmNotify = false;

    private static final CommonMessagingAccess commonMessagingAccess = CommonMessagingAccess.getInstance();
    private NtfEvent ntfEvent = null;
	private boolean voiceQuotaAlmostExceeded = false;
	private boolean videoQuotaAlmostExceeded = false;
	private boolean faxQuotaAlmostExceeded = false;
	private boolean voiceQuotaExceeded = false;
	private boolean faxQuotaExceeded = false;
	private Transcoder transcoder = new Transcoder(new MoipOamManager(), "NotificationEmail");
	private boolean videoQuotaExceeded = false;
    private boolean urgentReminder = false;

    /**
     * Map holding configured additional properties.
     */
    protected Map<String, String> additionalProperties;
	
    /**
     * Constructor
     * @param ntfEvent
     */
    public NotificationEmail(NtfEvent ntfEvent) {
        log = NtfCmnLogger.getLogAgent(NotificationEmail.class);
        this.ntfEvent = ntfEvent;
        additionalProperties = new HashMap<String, String>();
    }

    /**
     * Load the MFS values in this instance.
     * Must be called after the creation of an instance.
     *
     * @throws MsgStoreException
     */
    public void init() throws MsgStoreException {

       if(isReminderNotification()){
            //Since reminder notifications are done for the mailbox (and not for a specific message), there is no message-specific info:
            //These variables should stay null: senderPhoneNumber, from
            //These variables can stay false: urgent (reminder notification does not need to use subscriber's urgent notification type)
            //emailType will be dynamically set according to the mailbox inventory and used only to determine if a specific reminder notification type is in the subscriber's notification filter.
            subject = "";
            messageText = "";
            lengthOfMessage = "-1";
            receivedDate = new Date();
            uid = "";

            // Try to get the 'from'-related information out of the msgInfo that COULD be stored (best effort)
            // This and other information could be used if there is only one new message (the 'from' would make sense in this case)
            // Feel free to add more information if needed...
            if (ntfEvent.getMsgInfo() != null) {
                StateFile mfsStateFile = commonMessagingAccess.getStateFile(ntfEvent.getMsgInfo());

                setSenderVisible(mfsStateFile);
                setFrom(mfsStateFile);
                setUrgent(mfsStateFile);
                setConfidential(mfsStateFile);
                setSenderPhoneNumber();
                setSenderDisplayName();
            }

        } else if(isDefaultNotificationType() || isRoaming() || ntfEvent.isEventServiceType(NtfEventTypes.EVENT_TYPE_NOTIF.getName()) || ntfEvent.isEventServiceType(NtfEventTypes.FAX_L3.getName())){
	        StateFile mfsStateFile = commonMessagingAccess.getStateFile(ntfEvent.getMsgInfo());

            setSenderVisible(mfsStateFile);
	        setFrom(mfsStateFile);
	        setReceiver(mfsStateFile);
	        setSubject(mfsStateFile);
	        setReceivedDate(mfsStateFile);
	        setMessageSize();
	        setUrgent(mfsStateFile);
	        setConfidential(mfsStateFile);
	        setDefaultEmailType(mfsStateFile);
	        setUid(mfsStateFile);
	        setAdditionalProperties(mfsStateFile);
	        setSenderPhoneNumber();
	        setSenderDisplayName();


    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.SLAMDOWN.getName())){
    		//slam down
    		receiver = ntfEvent.getRecipient();
    		depType = depositType.SLAMDOWN;
    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.OUTDIAL.getName())){
    		receiver = ntfEvent.getRecipient();
    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.MWI_OFF.getName())){
    		receiver = ntfEvent.getRecipient();
    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.MAILBOX_UPDATE.getName())){
    		userAgentNumber = ntfEvent.getEventProperties().getProperty("userAgentNumber");
    		receiver = ntfEvent.getRecipient();
    		String forceProps = ntfEvent.getEventProperties().getProperty("force");
    		if(forceProps != null && forceProps.contains("true")){
    			forced = true;
    		}
    		depType = depositType.VOICE;
            String vvmNotifyProps = ntfEvent.getEventProperties().getProperty("vvmNotify");
            if( vvmNotifyProps == null || !vvmNotifyProps.contains("false")){
                vvmNotify = true;
            }

    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.FAX_RECEIPT.getName())){
            depType = depositType.FAX_RECEPT_MAIL_TYPE;
            receiver= ntfEvent.getProperty("origreceiver");
            from= ntfEvent.getProperty("orig_sender");
            String senderVisibility = ntfEvent.getProperty("orig_sender_visibility");
            senderVisible=true;

            if (senderVisibility != null && !senderVisibility.isEmpty()) {
                if ("0".equals(senderVisibility)) {
                    senderVisible = false;
                }
            }
            setSenderPhoneNumber();
            String time = ntfEvent.getProperty("origdate");
            if(time!=null)
            {
                receivedDate=new Date (Long.valueOf(time));
            }
            else
            {
                receivedDate=new Date();
            }
    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.MCN.getName())){
    	    receiver = ntfEvent.getRecipient();
    	    depType = depositType.MCN;
        } else if (ntfEvent.isEventServiceType(NtfEventTypes.FAX_PRINT.getName())){
            receiver = ntfEvent.getRecipient();
            depType = depositType.FAX_PRINT;
    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.VVM_GREETING.getName())){
    	    receiver = ntfEvent.getRecipient();
    	    depType = depositType.VVM_GREETING;
    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.VVM_EXPIRY.getName())){
    	    receiver = ntfEvent.getRecipient();
    	    depType = depositType.VVM_EXPIRY;
    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.VVM_LOGOUT.getName())){
    	    receiver = ntfEvent.getRecipient();
    	    depType = depositType.VVM_LOGOUT;
    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.VVA_SMS.getName())) {
            receiver = ntfEvent.getRecipient();
    	    depType = depositType.VVA_SMS;
    	} else if (ntfEvent.isEventServiceType(NtfEventTypes.AUTO_UNLOCK_PIN.getName())) {
            receiver = ntfEvent.getRecipient();
            depType = depositType.AUTO_UNLOCK_PIN;
        }
    	else if(ntfEvent.isEventServiceType(NtfEventTypes.DELAYED_EVENT.getName())){
            receiver = ntfEvent.getRecipient();
    	}

    	if (receiver == null) {
    		receiver = ntfEvent.getRecipient();
    	}
        setReceiverPhoneNumber();
    }

    public void initFallback() throws MsgStoreException {
        StateFile mfsStateFile = commonMessagingAccess.getStateFile(ntfEvent.getMsgInfo());
        setSenderVisible(mfsStateFile);
        setFrom(mfsStateFile);
        setReceiver(mfsStateFile);
        setSubject(mfsStateFile);
        setReceivedDate(mfsStateFile);
        setMessageSize();
        setUrgent(mfsStateFile);
        setConfidential(mfsStateFile);
        setDefaultEmailType(mfsStateFile);
        setUid(mfsStateFile);
        setSenderPhoneNumber();
        if (receiver == null) {
            receiver = ntfEvent.getRecipient();
        }
        setReceiverPhoneNumber();
    }

    /**
     * Store the NtfEvent
     * @param event
     */
    public void setNtfEvent(NtfEvent event) {
        this.ntfEvent = event;
    }

    /**
     * Get the NtfEvent
     * @return
     */
    public NtfEvent getNtfEvent() {
        return this.ntfEvent;
    }

    public String getMessageId(){
        return ntfEvent.getMessageId();
    }

    public String getSenderDisplayName() {
        log.debug("getSenderDisplayName=\"" + senderDisplayName + "\"");
        return senderDisplayName;
    }
    /** Get the UID field of the message.
     *  @return Message UID as String.
     */
    public String getUID() {
        return uid;
    }

    public boolean isVvmNotify(){
        return vvmNotify;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getUserAgentNumber() {
        return userAgentNumber;
    }

    /**
     * Returns the sender visibility
     * @return TRUE if the sender is visible (in MFS, 1=show, 0=hide)
     */
    public boolean getSenderVisibile() {
        return senderVisible;
    }

    /**
     * Get the java.util.Date object from the Message object.
     * @return Return a java.util.Date object retrieved from the MFS.
     * If no java.util.Date object could be retrieved, a new java.util.Date object
     * will be returned.
     */
    public Date getMessageReceivedDate() {
        return receivedDate;
    }

    /** The size of the message messured in bytes.
     * @return Size of the message in bytes.
     */
    public int getMessageSizeInBytes()
    {
        return messageSize;
    }

    /**
     * @deprecated please use setDepositType(depositType depType) after 2015-07-09
     * @param type
     */
    public void setEmailType(int type){
        depType = depositType.getDepositType(type);
    }

    public void setDepositType(depositType depType){
        this.depType = depType;
    }

    /**
     * Set if the subcriber exceeds it's quota.
     * Stored to minimize the I/Os.
     * @param quotaExceeded
     */
    public void setQuotaExceeded(boolean quotaExceeded) {
    	log.debug("setQuotaExceeded: "+ quotaExceeded);
        this.quotaExceeded = quotaExceeded;
    }

    /**
     * Get is the subscriber exceeds it's quota.
     * @return
     */
    public boolean getQuotaExceeded() {
        return this.quotaExceeded;
    }


	public boolean getQuotaAlmostExceeded() {
		return this.quotaAlmostExceeded;
	}

	public void setQuotaAlmostExceeded(boolean quotaAlmostExceeded) {
    	log.debug("setQuotaAlmostExceeded: "+ quotaAlmostExceeded);
        this.quotaAlmostExceeded = quotaAlmostExceeded;
	}
	public boolean getVoiceQuotaAlmostExceeded() {
		return this.voiceQuotaAlmostExceeded;
	}
	public void setVoiceQuotaAlmostExceeded(boolean voiceQuotaAlmostExceeded) {
    	log.debug("setVoiceQuotaAlmostExceeded: "+ voiceQuotaAlmostExceeded);

        this.voiceQuotaAlmostExceeded = voiceQuotaAlmostExceeded;
	}
	public boolean getVideoQuotaAlmostExceeded() {
		return this.videoQuotaAlmostExceeded;
	}
	public void setVideoQuotaAlmostExceeded(boolean videoQuotaAlmostExceeded) {
    	log.debug("setVideoQuotaAlmostExceeded: "+ videoQuotaAlmostExceeded);

        this.videoQuotaAlmostExceeded = videoQuotaAlmostExceeded;
	}
	public boolean getFaxQuotaAlmostExceeded() {
		return this.faxQuotaAlmostExceeded;
	}
	public void setFaxQuotaAlmostExceeded(boolean faxQuotaAlmostExceeded) {
    	log.debug("setFaxQuotaAlmostExceeded: "+ faxQuotaAlmostExceeded);

        this.faxQuotaAlmostExceeded = faxQuotaAlmostExceeded;
	}

	public boolean getVoiceQuotaExceeded() {
		return this.voiceQuotaExceeded;
	}
	public void setVoiceQuotaExceeded(boolean voiceQuotaExceeded) {
    	log.debug("setVoiceQuotaExceeded: "+ voiceQuotaExceeded);

		this.voiceQuotaExceeded= voiceQuotaExceeded;
	}
	public boolean getVideoQuotaExceeded() {
		return this.videoQuotaExceeded;
	}
	public void setVideoQuotaExceeded(boolean videoQuotaExceeded) {
    	log.debug("setVideoQuotaExceeded: "+ videoQuotaExceeded);

		this.videoQuotaExceeded= videoQuotaExceeded;
	}
	public boolean getFaxQuotaExceeded() {
		return this.faxQuotaExceeded;
	}
	public void setFaxQuotaExceeded (boolean faxQuotaExceeded) {
    	log.debug("setFaxQuotaExceeded: "+ faxQuotaExceeded);

		this.faxQuotaExceeded= faxQuotaExceeded;
	}

	public boolean isQuotaExceededForMsgType(){
		return quotaExceeded || (voiceQuotaExceeded && depType== depositType.VOICE) || (faxQuotaExceeded && depType== depositType.FAX)||(videoQuotaExceeded && depType== depositType.VIDEO);
	}
	public boolean isQuotaAlmostExceededForMsgType(){
		return quotaAlmostExceeded || (voiceQuotaAlmostExceeded && depType== depositType.VOICE) || (faxQuotaAlmostExceeded && depType== depositType.FAX)||(videoQuotaAlmostExceeded && depType== depositType.VIDEO);
	}
    /**
     * Makes a short text description of the NotificationEmail.
     *@return summary of NotificationEmail
     */
    public String summary()
    {
        String result = "";
        result = depType + " message notification";
        return "message is " + result;
    }

    /** The size of the message, in bytes, is converted into
     * Kbytes. If the size is smaller than 512 bytes, 1 (kb)
     * will be returned.
     * @return A int that states the size in Kbyte.
     */
    public int getMessageSizeInKbytes() {
        return (messageSize < 1024) ? 1 : ((messageSize + 512) / 1024);
    }

    /**
     * Tells if this email is default basic notification type.
     *@return true if this email is a forced MWI off message.
     */
    public boolean isDefaultNotificationType() {
        return ntfEvent.isEventServiceType(NtfEventTypes.DEFAULT_NTF.getName());
    }

    /**
     * Tells if this email is a mwi off event from call flow.
     *@return true if this email is a MWI off message.
     */
    public boolean isMwiOff() {
        return ntfEvent.isEventServiceType(NtfEventTypes.MWI_OFF.getName()) ||
               isMwiOffUnsubscribed();
    }

    /**
     * Tells if this email is a forced MWI off message for a deleted or modified subscriber.
     * @return true if this email is a MWI off message.
     */
    public boolean isMwiOffUnsubscribed() {
        return ntfEvent.isEventServiceType(NtfEventTypes.MWI_OFF_UNSUBSCRIBED.getName());
    }

    /**
     * Tells if this email is a mailbox update notification.
     *@return true if this email is a mailbox update notification.
     */
    public boolean isMailboxUpdate() {
        return ntfEvent.isEventServiceType(NtfEventTypes.MAILBOX_UPDATE.getName());
    }


    /**
     * Tells if this email is a mailbox update notification.
     *@return true if this email is a mailbox update notification.
     */
    public boolean isFaxDeliveryReceipt() {
        return ntfEvent.isEventServiceType(NtfEventTypes.FAX_RECEIPT.getName());
    }

    /**
     * Tells if this email is a forced mailbox update notification.
     *@return true if this email is a forced mailbox update notification.
     */
    public boolean isForcedUpdate() {
        return ntfEvent.isEventServiceType(NtfEventTypes.MAILBOX_UPDATE.getName()) && forced;
    }

    /**
     * Tells if this email is a slamdown information message, i.e. a
     * message carrying a slamdown notification to one single user.
     *@return true if this email is a slamdown information message.
     */
    public boolean isSlamdown() {
        return ntfEvent.isEventServiceType(NtfEventTypes.SLAMDOWN.getName());
    }

    /**
     * Tells if this email is a MCN information message, i.e. a
     * message carrying a MCN notification to a non-subscriber.
     *@return true if this email is a MCN information message.
     */
    public boolean isMcn() {
        return ntfEvent.isEventServiceType(NtfEventTypes.MCN.getName());
    }
    /**
     * Tells if this email is a Fax print request.
     *@return true if this email is a Fax Print Request.
     */
    public boolean isFaxPrint() {
        return ntfEvent.isEventServiceType(NtfEventTypes.FAX_PRINT.getName());
    }

    /**
     * Tells if this email is a VVM_GREETING information message
     *@return true if this email is a VVM_GREETING information message.
     */
    public boolean isVvmGreeting() {
        return ntfEvent.isEventServiceType(NtfEventTypes.VVM_GREETING.getName());
    }

    /**
     * Tells if this email is a VVM_EXPIRY information message
     *@return true if this email is a VVM_EXPIRY information message.
     */
    public boolean isVvmExpiry() {
        return ntfEvent.isEventServiceType(NtfEventTypes.VVM_EXPIRY.getName());
    }

    /**
     * Tells if this email is an SMS sent directly from the VVA (Call Flow)
     *@return true / false.
     */
    public boolean isVvaSms() {
        return ntfEvent.getEventServiceTypeKey().contains("vvasms");
    }

    /**
     * Tells if this email is a roam SMS (kind of fall back).
     *@return true / false.
     */
    public boolean isRoamSms() {
        //This type of event is used to send an SMS instead of an out-dial (for now only out-dial).
        //This will end up sending an SMS notification if allowed by the filters, with an optional
        //roaming phrase/content (.cphr) prepended or appended.
        return ntfEvent.isEventServiceType(NtfEventTypes.ROAMING.getName());
    }


    /**
     * Set this email as being an Urgent Reminder
     * Used to indicate there is at least one unread urgent msg in the mailbox
     * This means we will send an Urgent Reminder, using appropriate filter(s)
     */
    public void setUrgentReminder() {
        urgentReminder = true;
    }

    /**
     * Tells if this email is an urgent by definition - or if it's an urgent Reminder
     * (Overrides the AMessageDepositInfo implementation)
     *@return true / false.
     */
    public boolean isUrgent() {
        return (urgent || urgentReminder);
    }

    /**
     * Tells if this email is an auto unlock pin event sent directly from the VVA (Call Flow)
     *@return true / false.
     */
    public boolean isAutoUnlockPin() {
        return ntfEvent.isEventServiceType(NtfEventTypes.AUTO_UNLOCK_PIN.getName());
    }

    /**
     * Tells if this email is an auto unlock pin event L2 event
     *@return true / false.
     */
    public boolean isAutoUnlockPinL2() {
        return ntfEvent.isEventServiceName(NtfEventTypes.AUTO_UNLOCK_PIN_L2.getName());
    }

    /**
     * Tells if this email is a VVM_LOGOUT information message
     *@return true if this email is a VVM_LOGOUT information message.
     */
    public boolean isVvmLogout() {
        return ntfEvent.isEventServiceType(NtfEventTypes.VVM_LOGOUT.getName());
    }

    /**
     * Tells if this email is a ROAMING information message
     *@return true if this email is a ROAMING information message.
     */
    public boolean isRoaming() {
    	return ntfEvent.isEventServiceType(NtfEventTypes.ROAMING.getName());
    }

    /**
     * Tells if this email is a EVENT_TYPE_NOTIF notification
     * @return true if this email is a EVENT_TYPE_NOTIF notification
     */
    public boolean isEventTypeNotif() {
        return ntfEvent.getNtfEventType().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_NOTIF.getName());
    }

    /**
     * @return true if the current notification is a reminder that there are still new messages in subscriber's mailbox
     */
    public boolean isReminderNotification(){
        return ntfEvent.isReminder();
    }

    /**
     * @return int NTF_VIDEO if the internal slamdown message is of calltype video,
     *  NTF_VOICE othervise.
     */
    public int getSlamdownCallType() {
        //return only voice for now, figure out later what we need to do for the VIDEO
        return Constants.NTF_VOICE;
    }

    /**
     * Get the phone number of the user that has logged out from the NtfEvent properties.
     * @return a string with the MWI Subscriber info.
     */
    public String getMWISubscriberUID() {
        Properties prop = ntfEvent.getEventProperties();
        String subscriberInfo = prop.getProperty(NtfEventTypes.MWI_OFF.getName());
    	subscriberInfo = prop.getProperty(Constants.DEST_RECIPIENT_ID);
        if (subscriberInfo == null) { subscriberInfo = ""; }
        return subscriberInfo;
    }

    public String getMailboxUpdateSubscriberUID()
    {
        Properties prop = ntfEvent.getEventProperties();
        String subscriberInfo = prop.getProperty(NtfEventTypes.MAILBOX_UPDATE.getName());
    	subscriberInfo = prop.getProperty(Constants.DEST_RECIPIENT_ID);
        if (subscriberInfo == null) { subscriberInfo = ""; }
        return subscriberInfo;

    }

    /**
     * Parse a date according to RFC822 to a Date.
     *@param maildate the date to parse
     *@return a Date if the mail date could be parsed, or null otherwise.
     */
    public static Date mailDateToDate(String maildate) {
        if (maildate == null) { return null; }
        if (maildate.length() == 0) { return null; }

        Date result = null;
        try {
            MailDateFormat mdf = new MailDateFormat();
            result= mdf.parse(maildate);
        } catch (java.text.ParseException pe) {
            log.error("NotificationEmail.mailDateToDate(): " +
                           "exception when parsing date " + maildate +
                           "Exception message : " + pe);
        }
        return result;
    }

    /**
     * Creates a jakarta.mail.internet.MimeMessage, based on the data retrieved from the MFS Message object
     * @return A jakarta.mail.internet.MimeMessage, null if a jakarta.mail.internet.MimeMessage could not be created.
     */
    public MimeMessage getMimeMessage() throws MsgStoreException, MessagingException {
        synchronized (NotificationEmail.class) {
            if (mimeMessage != null) {return mimeMessage;}
            readMessage();
        }
        Container1 c1 = message.getContainer1();
        Container3 c3 = message.getContainer3();
        mimeMessage = ContentFormatUtil.buildMimeMessage(c1, c3);
        return mimeMessage;
    }

    /**
     * Get the attachment (body) of the first encounter for either a voice / video / fax message
     * within all the MsgBodyParts from container3 and convert it to a jakarta.mail.Part object.
     *
     * Part is matched using following criteria:
     * Content-Disposition: voice=voice-message (voice message)
     * Content-Dispostiion: video=video-message (video message)
     * Content-Type: image/tiff (fax message)
     *
     * @return jakarta.mail.Part object or null otherwise
     * @throws IOException
     */
    public Part getAttachmentPart(depositType depositType)
            throws MsgStoreException, MessagingException, IOException {
        Part part = null;
        switch (depositType) {
            case VOICE:
                part = getVoiceAttachmentPart();
                break;
            case VIDEO:
                part = getVideoAttachmentPart();
                break;
            case FAX:
                part = getFaxAttachmentPart();
                break;
            default:
                log.error("NotificationEmail.getAttachmentPart(): Unhandled msg type[" + depositType + "]. Msg is not a voice / video / fax msg!");
        }

        return part;
    }

    /**
     *
     */
    public Part getFaxAttachmentPart()
            throws MsgStoreException, MessagingException, IOException {
        MimeBodyPart mimeBodyPart = null;

        readMessage();
        Vector<MsgBodyPart> parts = message.getContainer3().getContents();
        MsgBodyPart part = getFaxMessageBody(parts);
        setExternalPart(part);
        mimeBodyPart = ContentFormatUtil.buildMimeBodyPart(part);

        return mimeBodyPart;
    }

    /**
     * Get the attachment (body) of the first encounter of a Content-Disposition=voice=voice-message
     * within all the MsgBodyParts from container3 and convert it to a jakarta.mail.Part object.
     * @return jakarta.mail.Part object or null otherwise
     * @throws IOException
     */
    public Part getVoiceAttachmentPart() throws MsgStoreException, MessagingException, IOException {
        MimeBodyPart mimeBodyPart = null;

        readMessage();
        Vector<MsgBodyPart> parts= message.getContainer3().getContents();
        MsgBodyPart part = getVoiceMessageBody(parts);
        setExternalPart(part);
        mimeBodyPart = ContentFormatUtil.buildMimeBodyPart(part);

        return mimeBodyPart;
    }
    /**
     * Get the attachment (body) of the first encounter of a Content-Disposition=voice=voice-message
     * within all the MsgBodyParts from container3 and convert it to a jakarta.mail.Part object.
     * @return jakarta.mail.Part object or null otherwise
     * @throws IOException
     */
    public Part getVideoAttachmentPart() throws MsgStoreException, MessagingException, IOException {
        MimeBodyPart mimeBodyPart = null;

        readMessage();
        Vector<MsgBodyPart> parts= message.getContainer3().getContents();
        MsgBodyPart part = getVideoMessageBody(parts);
        setExternalPart(part);
        mimeBodyPart = ContentFormatUtil.buildMimeBodyPart(part);

        return mimeBodyPart;
    }
    /**
     * If the part is located externally, then read in the content from the filesystem.
     *
     * @param part
     */
    private void setExternalPart(MsgBodyPart part)
            throws IOException {
        if (part.isExternal()) {
            String fileName = MfsFileFolder.getMsgExternalBodyFile(ntfEvent.getMsgInfo().omsa, ntfEvent.getMsgInfo().rmsa, ntfEvent.getMsgInfo().omsgid, ntfEvent.getMsgInfo().rmsgid, part.getExternalFileName());
            part.setContent(readFile(fileName).getBytes());
        }
    }

    private String readFile(String fileName) throws IOException{
        File f = new File(fileName);
        FileInputStream fis = new FileInputStream(f);
        byte[] b = new byte[(int) f.length()];
        int read = 0;
        while (read < b.length) {
          read += fis.read(b, read, b.length - read);
        }
        String text = new String(b);
        return text;
    }

    /**
     * getHeaders extracts the values for a set of headers from an email.
     *@param names An array with the names of the headers.
     *@return an array with each value at the same index as the corresponding
     * header name in names.
     */
    public String[] getHeaders(String[] names) throws MsgStoreException {
        String[] res = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            res[i] = getMultiValueHeader(names[i]);
        }
        return res;
    }

    /**
     * NOT USED This method will fetch the message body part at the specified index
     * @param index
     * @return the body part at the specified index
     */
    public MsgBodyPart getBodyPart(int index) throws MsgStoreException {
        readMessage();
        Container3 c3 = message.getContainer3();
        Vector<MsgBodyPart> parts= c3.getContents();
        MsgBodyPart msgBodyPart = parts.elementAt(index);
        return msgBodyPart;
        //if, for some reason we need to return a jakarta.mail.Part object just use backend utility
        //method to construct the object
        //part = ContentFormatUtil.buildMimeBodyPart(msgBodyPart);
        //return part;
    }

    /**
     * NOT USED This method will return the voice message body part as an input stream
     * @return
     */
    public InputStream getVoiceMessage() throws MsgStoreException, MessagingException, IOException {
        MimeBodyPart mimeBodyPart;
        InputStream inputStream = null;
        readMessage();
        Container3 c3 = message.getContainer3();
        Vector<MsgBodyPart> parts= c3.getContents();
        MsgBodyPart part = getVoiceMessageBody(parts);
        mimeBodyPart = ContentFormatUtil.buildMimeBodyPart(part);
        inputStream = mimeBodyPart.getInputStream();
        return inputStream;
    }

    /**
     * NOT USED This method will return the Content-Type header of the first
     * MsgBodyPart that is a voice message
     * @return
     */
    public String getVoiceMessageContentType() throws MsgStoreException {
        String contentType = null;
        readMessage();
        Container3 c3 = message.getContainer3();
        Vector<MsgBodyPart> parts= c3.getContents();
        MsgBodyPart part = getVoiceMessageBody(parts);
        contentType = part.getContentType();
        return contentType;
    }

    /**
     * NOT USED  The same logic as getVoiceMessage. See in the future the usage context, might not need it
     * @return
     */
    public InputStream getVideoMessage() throws MsgStoreException, MessagingException, IOException {
        MimeBodyPart mimeBodyPart;
        InputStream inputStream = null;
        readMessage();
        Container3 c3 = message.getContainer3();
        Vector<MsgBodyPart> parts= c3.getContents();
        MsgBodyPart part = getVideoMessageBody(parts);
        mimeBodyPart = ContentFormatUtil.buildMimeBodyPart(part);
        inputStream = mimeBodyPart.getInputStream();
        return inputStream;
    }

    /**
     * NOT USED The same logic as getVoiceMessageContentType(). See in the future the usage context, might not need it
     * @return
     */
    public String getVideoMessageContentType() throws MsgStoreException {
        String contentType = null;
        readMessage();
        Container3 c3 = message.getContainer3();
        Vector<MsgBodyPart> parts= c3.getContents();
        MsgBodyPart part = getVideoMessageBody(parts);
        contentType = part.getContentType();
        return contentType;
    }

    /**
     * The length of the voice message is retrieved.
     * This is done by checking the first Content-Duration from the
     * message body parts when the Content type is a multipart or a message/rfc822 type.
     * If Content-Duration not found, check the Content-Description and get the size.
     * @return A string with the length of the message,
     * measured in seconds.
     */
    public String getMessageLength() throws MsgStoreException, MessagingException, IOException {
        if (lengthOfMessage != null) { return lengthOfMessage; }

        int size = -1;
        lengthOfMessage = "?";
        readMessage();

        if (depType == depositType.FAX) {
			String xheaderFaxPageCount = message.getContainer2().getProtocolHeader("X-Fax-Pages");
			if(xheaderFaxPageCount!=null)
			{
				size = Integer.parseInt(xheaderFaxPageCount);
				log.debug("messageContentLength fax page count from xheaders is : "+size);
			}
			else
			{
				log.debug("messageContentLength Not able to get fax page count from xheaders");

			}

        } else {
            Container3 c3 = message.getContainer3();
            Vector<MsgBodyPart> v= c3.getContents();
            size = getVoiceMessageSize(v);
            if (size < 0) {
                size = getMessageSizeFromFirstContentDescription(v);
            }
        }
        if (size > 0) {
            lengthOfMessage = "" + size;
        }
        return lengthOfMessage;
    }

    /**
     *@return the entire message.
     */
    public jakarta.mail.Message getMessageObject() throws MsgStoreException, MessagingException {
        return getMimeMessage();
    }

    /**
     * Extracts a parameter of the form name=value from a line in the message
     * body.
     *@param name the parameter name.
     *@return The value for the specified name, or null
     */
    public String getBodyParameter(String name) throws MsgStoreException {
        String text = getMessageText();
        StringTokenizer st = new StringTokenizer(text, "\r\n");

        while(st.hasMoreTokens()){
            String line = st.nextToken().trim();

            if (line.startsWith(name + "=")) {
                return (line.substring(line.indexOf("=") + 1));
            }
        }
        return null;
    }

    public String getMessageText() throws MsgStoreException {
        synchronized (NotificationEmail.class) {
            if (messageText != null) {return messageText;}
            readMessage();
        }
        Vector<MsgBodyPart> bodyParts = message.getContainer3().getContents();
        for (MsgBodyPart bodyPart:bodyParts)  {
            if (bodyPart.getContentType().equalsIgnoreCase(MsgBodyPart.TEXT_PLAIN) ){
                messageText = new String(bodyPart.getContent());
            }
        }
        return messageText;
    }

    /**
     * Fetches number of attachments from the Message
     * @return Number of attachments
     */
    public int getNoOfAttachments() throws MsgStoreException {
        synchronized (NotificationEmail.class) {
            if (numberOfAttachments != -1) {return numberOfAttachments;}
            readMessage();
        }
        Container3 c3 = message.getContainer3();
        Vector<MsgBodyPart> parts= c3.getContents();
        numberOfAttachments = 0;
        for (MsgBodyPart part:parts) {
            if (part.isExternal()) {
                numberOfAttachments++;
            }
        }
        return numberOfAttachments;
    }

    private void setReceiver(StateFile stateFile) {
    	String to=stateFile.getC1Attribute(Container1.To);

    	receiver=to;

    	String rcip = ntfEvent.getRecipient();

    	//rcip can be null, needed for the distribution list feature
    	if(rcip != null){
    	    rcip = rcip.replaceFirst("^0*", "");
        	if (to.contains(";") && to.contains(rcip)) {
        		StringTokenizer tokenizer=new StringTokenizer(to,";");
        		while (tokenizer.hasMoreTokens()){
        			String theRcip=tokenizer.nextToken();
        			if (theRcip.contains(rcip)){
        				receiver=theRcip;
        				break;
        			}
        		}
        	}
    	}
    }


    /* Extract display name from "From" field
     * If from field is empty, or from field is malformated, then return "unknown"
     * */
    private void setSenderDisplayName() {
        if(from != null && !from.isEmpty()) {
            int beginIndex = from.indexOf('<');
            int endIndex = from.indexOf('>');

            if ((beginIndex != -1) && (endIndex != -1) && (endIndex > beginIndex)) {
                // displayName with tel uri
                senderDisplayName = from.substring(0, beginIndex).trim();
                log.debug("setSenderDisplayName: extract display name(displayName <teluri>): displayName is " + senderDisplayName);
            } else {
                if ((beginIndex == -1) && (endIndex == -1)) {
                    // displayName without tel uri
                    senderDisplayName = from.trim();
                    log.debug("setSenderDisplayName: extract display name(displayName WITHOUT <teluri>): displayName is " + senderDisplayName);
                } else {
                    // Malformated. Log it and set unknown
                    senderDisplayName = "unknown";
                    log.debug("setSenderDisplayName: extract display name(malformat): displayName is unknown");
                }
            }
        } else {
            senderDisplayName = "unknown";
            log.debug("setSenderDisplayName: extract display name(null or empty): displayName is unknown");
        }
    }

    public void setSenderPhoneNumberTest(String sender) {
        senderPhoneNumber = sender;
        receivedDate=new Date();
    }

    public void setAdditionalProperties(StateFile stateFile) {
        String allEntries = stateFile.getAllEntries();
        String entries[] = allEntries.split("\\n");
        
        for (String entry : entries) {
            if ( (entry.indexOf(ADDITIONAL_PROPERTY) != -1) &&                          // Additional Property String is present
                    (entry.indexOf("=") != -1) &&                                       // And it contains a "="
                    (entry.indexOf("=") > (entry.indexOf(ADDITIONAL_PROPERTY) + ADDITIONAL_PROPERTY.length())) &&  // And Additional Property name is at least one char long
                    (entry.indexOf("=") < (entry.length() - 1)) ) {                     // And there is at least one char to be read after the "="
                String key;
                String value;
                log.debug("setAdditionalProperties: Setting Additional Property from State File: " + entry);

                try {
                    key = entry.substring( entry.indexOf(ADDITIONAL_PROPERTY) + ADDITIONAL_PROPERTY.length(), entry.indexOf("=") );
                } catch (IndexOutOfBoundsException e) {
                    log.debug("setAdditionalProperties: Could not read Additional Property from State File: " + entry);
                    continue;
                }
                
                value = entry.substring(entry.indexOf("=") + 1);
                
                log.debug("setAdditionalProperties: Setting Additional Property with key: " + key + " and value: " + value);
                
                additionalProperties.put(key, value);
            }
        }
    }

    public String getAdditionalProperty(String name) {
        log.debug("getAdditionalProperty: (name=" + name + ")");
        String value = additionalProperties.get(name);
        log.debug("getAdditionalProperty: returning \"" + value + "\"");
        return value;
    }
    
    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }
    
    private boolean isDigits(String s) {
        if (s.length() == 0) { return false; }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) { return false; }
        }
        return true;
    }

    private void setMessageSize() throws MsgStoreException {
        MessageFileHandle messageFileHandle = commonMessagingAccess.getMessageFileHandle(ntfEvent.getMsgInfo());
        if (messageFileHandle != null) {
            messageSize = (int)messageFileHandle.getFileSize();
            messageFileHandle.release();
        }
    }

    private String arrayToString(String[] temp) {
        String[] s = temp;
        if (temp == null) { return null; }
        if (temp.length == 0) { return ""; }
        if (temp.length == 1 && temp[0].indexOf(";")== -1) {
            return temp[0];
        } else {
            s = temp[0].split(";");
        }
        String all = s[0];
        //String all = temp[0];
        for (int i = 1; i < s.length; i++) {
            all += "," + s[i];
        }
        return all;
    }

    /**
     * This is a utility method that will check each MsgBodyPart for the
     * Content-Disposition: voice=voice-message. At the first match it will
     * stop and return the body part.
     * @param parts
     * @return the body of the MsgBodyPart, null otherwise
     */
    private MsgBodyPart getVideoMessageBody(Vector<MsgBodyPart> parts)  {
    	String contentType;

    	for(MsgBodyPart part: parts) {
    		contentType = part.getContentType();
    		if (contentType.contains("multipart") || contentType.contains("message/rfc822")) {
    			part = getVideoMessageBody(part.getMultiParts());
    			return part;

    		} else if (isRecordedMessage(part)) {
    			return part;
    		}
    	}
    	return null;
    }
    /**
     * This is a utility method that will check each MsgBodyPart for the
     * Content-Disposition: voice=voice-message. At the first match it will
     * stop and return the body part.
     * @param allParts
     * @return the body of the MsgBodyPart, null otherwise
     */
    private final static int  BYTES_TO_SKIP = 6;
    private MsgBodyPart getVoiceMessageBody(Vector<MsgBodyPart> allParts)  {
    	log.debug("getVoiceMessageBody Start");
    	MsgBodyPart myMultipart = new MsgBodyPart(true);
    	myMultipart.setContentType("multipart/mixed");
		String outputMimeType = "audio/amr"; // maybe set to "audio/amr-wb" later if content is WB

    	Iterator<MsgBodyPart> allPartIterator = allParts.iterator();
    	Vector<MsgBodyPart> partsVector = new Vector<MsgBodyPart>();
    	MsgBodyPart onlyPart = new MsgBodyPart();
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	int totalDuration = 0;
    	boolean firstOne = true;
    	while (allPartIterator.hasNext()){
    		log.debug("getVoiceMessageBody getting next MsgBodyPart");
    		MsgBodyPart eachPart = allPartIterator.next();

    		//Check if that part is in the message or if it just another part.
    		String partDescription = getPartHeader(eachPart, "Content-Description");
    		if(partDescription != null){
    			partDescription = partDescription.toLowerCase();
    			if(partDescription.contains("spoken") && partDescription.contains("name")){
    				log.debug("getVoiceMessageBody skipping part partDescription="+partDescription);
    				continue;
    			}
    		}

    		ContentType contentType;
    		try {
    			contentType = new ContentType(eachPart.getContentType());
    			byte myContent[] = eachPart.getContent();
    			////////////////////////////////////////////////////////////////////////////////////////////////////////////
    			// AMR-WB: determine outputMimeType based on codec
    			////////////////////////////////////////////////////////////////////////////////////////////////////////////
    			if (log.isDebugEnabled()) {
    				log.debug("@@@getVoiceMessageBody contentType = " + contentType.toString() + " | " + contentType.getParameter("codec") +
    					      " | " + contentType.getPrimaryType() + " | " + contentType.getSubType() + " | " + contentType.getParameterList());
    			}
    			jakarta.mail.internet.ParameterList pl = contentType.getParameterList();
    			java.util.Enumeration<String> paramNames = pl.getNames();
    			while (paramNames.hasMoreElements()) {
    				String name = paramNames.nextElement();
    				if (name == null) continue;
    				if (name.equalsIgnoreCase("codec")) {
    					String codec = contentType.getParameter(name);
    					if (codec != null && codec.equalsIgnoreCase("sawb")) { // codec is AMR-WB
    						outputMimeType = "audio/amr-wb";
    						break;
    					}
    				} else {
    					log.debug("VoiceMessageBody(): contentType param name = " + name);
    				}
    			}    			
    			//byte[] resultTranscoded = transcoder.convertByteArray(myContent, contentType.getBaseType(), "audio/amr");
    			// Use contentType.toString() instead of contentType.getBaseType to capture the parameters such as codec
    			byte[] resultTranscoded = transcoder.convertByteArray(myContent, contentType.toString(), outputMimeType);


    			if (resultTranscoded != null && resultTranscoded.length != 0){
    				if(firstOne){
    					try {
    						baos.write(resultTranscoded);
    					} catch (IOException e) {
    						log.debug("IO Exception", e);
    						e.printStackTrace();
    					}
    					firstOne = false;
    				}else{
    					if (outputMimeType.equals("audio/amr-wb"))
    						// In case of AMR-WB, the header is not "#!AMR<LF>", but "#!AMR-WB<LF>"
    						baos.write(resultTranscoded, BYTES_TO_SKIP + 3, resultTranscoded.length - BYTES_TO_SKIP - 3);
    					else 
    						baos.write(resultTranscoded, BYTES_TO_SKIP, resultTranscoded.length - BYTES_TO_SKIP);
    				}
    				//Only add to the duration if we have a content
    				String partDuration = getPartHeader(eachPart, "Content-Duration");
    				int thisDuration = extractDuration(partDuration);
    				totalDuration += thisDuration;
    				log.debug("getVoiceMessageBody totalDuration="+totalDuration);
    			}

    		}
    		catch (ParseException e1) {
    			log.debug("getVoiceMessageBody ParseException ", e1);
    			e1.printStackTrace();
    		}
    		catch (Exception e){
    			log.debug("getVoiceMessageBody Exception ", e);
    			// if any exception happened, we might have  an empty baos
    		}

    	}
    	onlyPart.addPartHeader("Content-Description", "Voice message (" + String.valueOf(totalDuration) + " second(s))");

    	onlyPart.addPartHeader("Content-Duration", String.valueOf(totalDuration));
        onlyPart.addPartHeader("Content-Disposition", "inline; filename=message.amr; voice=Voice-Message");
		if (outputMimeType.equals("audio/amr-wb"))
	        onlyPart.setContentType("audio/amr-wb; codec=sawb");
		else
			onlyPart.setContentType("audio/amr");    	
    	onlyPart.setContent(baos.toByteArray());
    	onlyPart.setBoundaryPart();
    	log.debug("getVoiceMessageBody returning multipart"+onlyPart.toString());
    	return onlyPart;

    }


    private static int extractDuration(String firstPartDuration) {
        if(firstPartDuration != null){
            int indexOfColon = firstPartDuration.indexOf(":");
            String onlyDurationNumber = firstPartDuration.substring(indexOfColon+1, firstPartDuration.length());
            onlyDurationNumber =  onlyDurationNumber.trim();
            return Integer.valueOf(onlyDurationNumber).intValue();
        }
        return 0;
    }

    private static String getPartHeader(MsgBodyPart part, String header){
        String value = part.getPartHeader(header);

        //Need to lookup case insensitive
        if(value == null){
            HashMap<String, String> allHeaders = part.getPartHeaders().getAll();

            for(String key : allHeaders.keySet()){
                if(key.equalsIgnoreCase(header)){
                    value = allHeaders.get(key);
                    break;
                }
            }
        }
        return value;
    }

    /**
     * This is a utility method that will check each MsgBodyPart for the
     * Content-Type: image/tiff. At the first match it will
     * stop and return the body part.
     *
     * @param parts
     * @return the body of the MsgBodyPart, null otherwise
     */
    private MsgBodyPart getFaxMessageBody(Vector<MsgBodyPart> parts)  {
        String contentType;

        for(MsgBodyPart part: parts) {
            contentType = part.getContentType();
            log.debug("NotificationEmail.getFaxMessageBody(): contentType[" + contentType + "]");

            if (contentType.contains("multipart")) {
                part = getFaxMessageBody(part.getMultiParts());
                return part;
            } else if (contentType.contains("image/tiff")) {
                return part;
            }
            else {
                // ignore this part and keep going!
            }
        }

        return null;
    }

    /**
     * Get value of a named header from the message.
     * This method concatenates multiple instances of the header with "," as separator.
     * @param headerName header to retrieve
     * @return Value of header, if header is not found null is returned
     */
    protected String getMultiValueHeader(String headerName) throws MsgStoreException {

    	try {
    		return arrayToString(getMimeMessage().getHeader(headerName));
    	} catch (MessagingException e) {
    		log.error("MessagingException occured when retrieving multi " +
    				headerName  + ": " , e);
    	}
    	return null;
    }

    /**
     * Utility method, tries to get the message duration (size) from the Content-Description header.
     * @param part
     * @return the size from the Content-Description, -1 if not found
     * @throws IOException
     * @throws MessagingException
     */
    private int getMessageSizeFromContentDescription(MsgBodyPart part) throws IOException, MessagingException {
        String line;
        if (part.isExternal()) {
            line = part.getExternalPartHeaders().getValue("Content-Description");
        } else {
            line = part.getPartHeader("Content-Description");
        }
        if (line != null) {
            int start = line.indexOf("(");
            int end = line.indexOf(" ", start + 1);
            if (end < 0) {
                end = line.indexOf(")", start + 1);
            }
            if (start >= 0 && end >= 0) {
                try {
                    return Integer.parseInt(line.substring(start + 1, end).trim());
                } catch (NumberFormatException e) {
                    log.error("getMessageSizeFromContentDescription(): " , e);;
                }
            }
        }
        return -1;
    }

    /**
     * Iterate through all the message body parts and check for the Content-Description
     * header that contains the duration(size) of the voice message. If not found, return -1.
     * If the MsgBodyPart is a multipart, it will recursively check for the first encounter of the Content-Description header.
     * @param parts
     * @return the size from the Content-Desccription header if found, -1 otherwise
     * @throws MessagingException
     * @throws IOException
     */
    private int getMessageSizeFromFirstContentDescription(Vector<MsgBodyPart> parts)  throws MessagingException, IOException {
        int size;
        for(MsgBodyPart part: parts) {
            String contentType = part.getContentType();
            if (contentType.contains("multipart") || contentType.contains("message/rfc822")) {
                size = getMessageSizeFromFirstContentDescription(part.getMultiParts());
                if (size >= 0) {return size;}

            } else  {
                size = getMessageSizeFromContentDescription(part);
                if ( size >= 0) {return size;}
            }
        }
        return -1;
    }

    /**
     * Check for the "Content-Disposition" header in a message body part. IF found,
     * verify if it is set to voice or video.
     * @param part
     * @return true if the "Content-Disposition" header is found and set to voice or video, false otherwise
     * @throws MessagingException
     */
    private boolean isRecordedMessage(MsgBodyPart part) {
        String h;
        if (part.isExternal()) {
            h = part.getExternalPartHeaders().getValue(CONTENT_DISPOSITION);
        } else {
            h = part.getPartHeader(CONTENT_DISPOSITION);
        }
        return h != null && (h.toLowerCase().indexOf("voice=voice-message") >= 0
                || h.toLowerCase().indexOf("video=video-message") >= 0);
    }

    /**
     * Check for the "Content-Duration" header in a message body part.
     * @param part
     * @return the value of the "Content-Duration" header or -1 if the header is not found
     * @throws MessagingException
     */
    private int contentDuration(MsgBodyPart part) {
        String h;
        if (part.isExternal()) {
            h = part.getExternalPartHeaders().getValue(CONTENT_DURATION);
        } else {
            h = part.getPartHeader(CONTENT_DURATION);
        }
        if (h != null) {
            try {
                return Integer.parseInt(h.trim());
            } catch (NumberFormatException e) {
                log.error("Can not get the voice length from " + h+ " exception ",e);
            }
        }
        return -1;
    }

    /**
     * Utility method, iterate through all the parts; at the first encounter of Content-Disposition/
     * Content-Duration pair headers get the size from the Content-Duration. The method will recursively
     * go into each body part when the Content type is a multipart or a message/rfc822 type.
     * @param parts
     * @return the size of the voice message extracted from the Content-Duration, -1 if not found
     * @throws MessagingException
     * @throws IOException
     */
    private int getVoiceMessageSize(Vector<MsgBodyPart> parts)  throws MessagingException, IOException {
        int size;
        String contentType;
        for(MsgBodyPart part: parts) {
            contentType = part.getContentType();
            if (contentType.contains("multipart") || contentType.contains("message/rfc822")) {
                size = getVoiceMessageSize(part.getMultiParts());
                if (size >= 0) {return size;}

            } else if (isRecordedMessage(part)) {
                size = contentDuration(part);
                if ( size >= 0) {return size;}
            }
        }
        return -1;
    }

    /**
     * Reads the message from MFS.
     *
     * @param extendMultiparts  true indicates if the parts of a multipart/* are to be read from the message
     * @throws MsgStoreException  if the message cannot be read
     */
    private synchronized void readMessage() throws MsgStoreException {
        if (message == null) {
            if (getDepositType() == depositType.FAX) {
                message = commonMessagingAccess.readMessage(ntfEvent.getMsgInfo(), true);
            } else {
                message = commonMessagingAccess.readMessage(ntfEvent.getMsgInfo(), false);
            }
        }
    }

    public UserMailbox getUserMailbox() {
        userMailbox = loadUserMailbox(receiver, userMailbox);
        return userMailbox;
    }

    public UserMailbox getUserMailbox(String subscriberNumber) {
        userMailbox = loadUserMailbox(subscriberNumber, userMailbox);
        return userMailbox;
    }

    private synchronized UserMailbox loadUserMailbox(String to, UserMailbox mailbox) {
    	if (mailbox == null) {
    	    UserInfo userInfo;
            if (isMwiOffUnsubscribed()) {
                // For MWI OFF notification for unsubscribed user, we need to
                // create a special user.
                userInfo = UserFactory.getUnsubscribedUser(this);
            } else {
                userInfo = UserFactory.findUserByTelephoneNumber(to);
            }
            if (userInfo == null) {
                // Non-subscriber case, create an empty McdUserInfo.
                log.error("Unable to find subscriber " + to + ", will try with external user info");
                userInfo = new McdUserInfo(to, Config.getDefaultLanguage());
            }
    		MSA rmsa = null;
    		if(ntfEvent.getMsgInfo() != null && ntfEvent.getMsgInfo().rmsa != null){
    		    rmsa = ntfEvent.getMsgInfo().rmsa;
    		}
    		else{
    		    String msid = MfsEventManager.getMSID(to);
    		    rmsa = new MSA(msid);
    		}
    		UserMailbox umailbox = new UserMailbox(rmsa,
    				userInfo.hasMailType(Constants.NTF_EMAIL),
    				userInfo.hasMailType(Constants.NTF_FAX),
    				userInfo.hasMailType(Constants.NTF_VOICE),
    				userInfo.hasMailType(Constants.NTF_VIDEO));
    		return umailbox;
    	} else {
    		return mailbox;
    	}
    }
}
