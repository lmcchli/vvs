/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.sms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.vvs.ntf.notifier.NotifierSMSResultHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierResultHandlerSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms.NotifierPhoneOnMethod;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms.NotifierSmppPduType;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup;
import com.abcxyz.services.moip.provisioning.businessrule.ProvisioningConstants;
import com.mobeon.common.sms.SMSClient;
import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.sms.request.FormattedSMSRequest;
import com.mobeon.common.sms.request.MultiRequest;
import com.mobeon.common.sms.request.Request;
import com.mobeon.common.sms.request.SMSMessagePayload;
import com.mobeon.common.sms.request.SMSRequest;
import com.mobeon.common.smscom.ConnectionStateListener;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.out.vvm.VvmEvent;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.Constants.depositType;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.out.vvm.VvmEvent.VvmEventTypes;
import com.mobeon.ntf.slamdown.CallerInfo;
import com.mobeon.ntf.slamdown.SlamdownPayload;
import com.mobeon.ntf.slamdown.SlamdownList;
import com.mobeon.ntf.text.ByteArrayUtils;
import com.mobeon.ntf.text.Phrases;
import com.mobeon.ntf.text.TemplateBytes;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.text.TextCreator;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.UserInfo.NotifState;
import com.mobeon.ntf.util.Logger;


/**
 *The SMSOut is the entry point for sending SMS notifications.
 *Normally this class forwards requests to a handler thread from a pool,
 *holding the calling thread only for a short time. If too many handlers are
 *already running, the calling thread will hang until a handler is released.
 */
public class SMSOut implements Constants, com.mobeon.common.smscom.Logger, ConnectionStateListener {
    private final static Logger log = Logger.getLogger(SMSOut.class);
    //The roaming phrase used to append/prepend to a standard c,s,h when sending roaming if configured.
    private static final String SMS_ROAMING_TEMPLATE = "roaming"; 
    private static SMSOut instance = null;
    private static SMSClient smsClient = null;
    private static IntfServiceTypePidLookup serviceTypeLookup = ServiceTypePidLookupImpl.get();
    private SMSListener smsListener = null;
    private SMSInfoListener smsInfoListener = null;
    private SMSPhoneOnListener smsPhoneOnListener = null;
    private VvmSmsListener vvmListener = null;

    /**
     * The constructor initializes the SMS center table. Clients do nou use it,
     * the single instance is retrieved with get instead.
     */
    private SMSOut() throws ConfigurationDataException {
        SMSConfigWrapper configWrapper = new SMSConfigWrapper();
        SMSConfigWrapper.initSMPPErrorCodeActions();

        if (smsClient == null) {
            smsClient = SMSClient.get(this, configWrapper);
        }

        smsClient.setConnectionStateListener(this);
        smsListener = new SMSListener();
        smsInfoListener = new SMSInfoListener();
        smsPhoneOnListener = new SMSPhoneOnListener();
        vvmListener = new VvmSmsListener();
        instance = this;
    }

    /**
     *Returns the single instance of SMSOut, creating it if necessary.
     *@return the SMSOut.
     */
    public static SMSOut get() {
        try {
            if (instance == null) {
                instance = new SMSOut();
            }
        } catch (ConfigurationDataException cde) {
            log.logMessage("NTF will be shuting down because it couldn't load the ntf configuration files " +
                    "(notification.conf, xmpErrorCodes.conf, smppErrorCodes.conf) under the directory " +
                    Config.getNtfHome() + ". Error: " + cde.getMessage(), Logger.L_ERROR);
            instance = null;
        }
        return instance;
    }

    public static void setSmsClient(SMSClient sms) {
        smsClient = sms;
    }

    /**
     * Connect to all configured SMS units at startup. 
     */
    public void connectSmsUnits() {
        smsClient.connectSmsUnits();
    }

    public int handleSMS(UserInfo user, SmsFilterInfo info, FeedbackHandler ng, NotificationEmail email, UserMailbox inbox, SMSAddress source, int validity, int delay) {
        try {
            return handleSMS(user, info, ng, email, inbox, source, validity, delay, null);
        } catch (TemplateMessageGenerationException e) {
            log.logMessage("TemplateMessageGenerationException on handle SMS.", Logger.L_ERROR);
            return 0;
        }
    }

    /**
     * handleSMS acquires an SMSClient thread that handles the job of creating
     * and sending the SMS message.
     *@param user - information about the user receiving the notification.
     *@param info - SMS-specific information from notification filters.
     *@param feedbackHandler - the entity outside smsout handling the response to a
     * complete SMS message. That SMS may have been sent as several fragments
     * each handled by an internal responsehandler. If ng is null, a best effort
     * is made to send the message, without any feedback.
     *@param inbox - the different message counts in the users mailbox. If inbox
     * is null, no message count will be done.
     *@param source - M3 address.
     *@param validity - how long the message is valid in the smsc.
     *@return the number of acknowledgements that will be sent for SMS.
     */
    public int handleSMS(UserInfo user,
            SmsFilterInfo info,
            FeedbackHandler feedbackHandler,
            NotificationEmail email,
            UserMailbox inbox,
            SMSAddress source,
            int validity,
            int delay,
            ANotifierSendInfoSms sendInfoSms) throws TemplateMessageGenerationException {
        
        String[] numbers = info.getNumbers();
        if (numbers.length == 0) {
            return 0;
        }
               String charsetname = Config.getCharsetEncoding();
        if (charsetname.equals("")) charsetname = null;

        int count = 0;
        SMSResultHandler resultHandler = null;
        int destinationTON = 0;
        int destinationNPI = 0;
        if(sendInfoSms != null) {
            resultHandler = NotifierSMSResultHandler.get();
            destinationTON = sendInfoSms.getDestinationAddressTypeOfNumber();
            destinationNPI = sendInfoSms.getDestinationAddressNumberingPlanIndicator();
        } else {
            resultHandler = smsListener;
            destinationTON = user.getTypeOfNumber();
            destinationNPI = user.getNumberingPlan();
        }
        
        for (int i = 0; i < numbers.length; i++) {
            MultiRequest multiRequest = new MultiRequest(delay);
            int thisCount = 1;
            depositType type = depositType.VOICE;
            
            if (email != null) {
                type = email.getDepositType();                
            }

            if (info.isMwi(numbers[i]) && info.hasMwiCount() && (type == depositType.VOICE  || type == depositType.VOICE)) {
                if (inbox != null) {
                    thisCount = inbox.getNewTotalCount();
                    log.logMessage("SMSOut: Counted messages: " + thisCount, Logger.L_DEBUG);
                } else {
                    thisCount = 1;
                }
            }

            SMSAddress adr = new SMSAddress(destinationTON, destinationNPI, numbers[i]);

            // SMS + SMSMWI
            if ((info.isSms(numbers[i]) && info.isMwi(numbers[i])) && !Config.isSplitMwiAndSms()) {
                int id = smsClient.getNextId();
                
                SMSMessagePayload smsPayload = getSMSPayload(charsetname, info.getNotifContent(), inbox, email, user, info.isRoaming());

                int replacePosition = getReplacePositionWithTerminalCheck(info.getNotifContent(), user);
                int typeOfSMS = SMSClient.TYPE_SMSMWI;
                if( !user.terminalSupportsMwi() ) {
                    typeOfSMS = SMSClient.TYPE_SMS;
                }
                Request request = SMSClient.makeRequest(source, adr, user, inbox, validity, resultHandler, id,
                    typeOfSMS, smsPayload.message, smsPayload.payload, replacePosition, thisCount, 0 );
                setRequestParameters(request, sendInfoSms); 

                multiRequest.addRequest(request);
                addToListener(id, user, feedbackHandler, NTF_SMS, sendInfoSms);

                ++count;
            } else {

                // SMS only
                if (info.isSms(numbers[i])) {
                    int id = smsClient.getNextId();
                    
                    SMSMessagePayload smsPayload = getSMSPayload(charsetname, info.getNotifContent(), inbox, email, user, info.isRoaming());

                    int replacePosition = getReplacePositionWithTerminalCheck(info.getNotifContent(), user);

                    Request request = SMSClient.makeRequest(source, adr, user, inbox, validity, resultHandler, id,
                            SMSClient.TYPE_SMS, smsPayload.message, smsPayload.payload, replacePosition, 0 , 0);
                    setRequestParameters(request, sendInfoSms);

                    addToListener(id, user, feedbackHandler, NTF_SMS, sendInfoSms);
                    multiRequest.addRequest(request);
                    ++count;
                }

                // SMSMWI only
                if (info.isMwi(numbers[i])) {
                    if( !user.terminalSupportsMwi() && !info.isSms(numbers[i] )) {
                        log.logMessage("User does not support MWI, sending MWI as SMS instead", Logger.L_DEBUG);
                        int id = smsClient.getNextId();
                        SMSMessagePayload smsPayload = getSMSPayload(charsetname, "smsinsteadofwmi", inbox, email, user, false);
                        Request request = SMSClient.makeRequest(source, adr, user, inbox, validity, resultHandler, id,
                                SMSClient.TYPE_SMS, smsPayload.message, smsPayload.payload, -1, thisCount, 0 );
                        setRequestParameters(request, sendInfoSms);

                        addToListener(id, user, feedbackHandler, NTF_SMS, sendInfoSms);
                        multiRequest.addRequest(request);
                        ++count;
                    } else if( user.terminalSupportsMwi() ) {
                        int id = smsClient.getNextId();
                        SMSMessagePayload smsPayload = getSMSPayload(charsetname, "mwiontext", inbox, email, user, false);
                        int mwiValidity = user.getValidity_mwiOn();
                        SMSAddress sourceMwi = Config.getSourceAddress("mwion", user.getCosName());

                        Request request = SMSClient.makeRequest(sourceMwi, adr, user, inbox, mwiValidity, resultHandler, id,
                                SMSClient.TYPE_MWI, smsPayload.message, smsPayload.payload, -1, thisCount, 0 );
                        setRequestParameters(request, sendInfoSms);

                        addToListener(id, user, feedbackHandler, NTF_MWI, sendInfoSms);
                        multiRequest.addRequest(request);
                        ++count;
                    }
                }
            }

            if( info.isFlash(numbers[i])) {
                int id = smsClient.getNextId();
                
                SMSMessagePayload smsPayload = getSMSPayload(charsetname, info.getFlashContent(), inbox, email, user, info.isRoaming());

                int typeOfSMS = SMSClient.TYPE_FLASH;
                //If a flash message should be sent but the terminal does not
                //support flash and the email is urgent, do not send anything at
                //all. This is a special feature for Vodafone NL depending on the
                //following:
                // - Vodafone NL only uses flash for urgent mails, as an
                // addition to a normal SMS notification.
                // - If the terminal does not support flash, they do not want
                // the additional SMS at all.
                // - The risk that other customers are affected by this is low -
                // it could only affect customers that have the terminal
                // capability feature specially developed for Vodafone NL.
                //
                // And of course, there is an exception, since an unread message
                // reminder as flash SMS when there is a single urgent email in
                // the inbox still should be sent. We identify that case by
                // checking if the content is "unreadmessagereminder".
                if (user.terminalSupportsFlash()
                    || email == null
                    || !email.isUrgent()
                    || "unreadmessagereminder".equals(info.getFlashContent())) {
                    if (!user.terminalSupportsFlash()) { typeOfSMS = SMSClient.TYPE_SMS; }
                    Request request = SMSClient.makeRequest(source, adr, user, inbox, validity, resultHandler, id,
                            typeOfSMS, smsPayload.message, smsPayload.payload, -1, 0, 0 );
                    setRequestParameters(request, sendInfoSms);

                    addToListener(id, user, feedbackHandler, NTF_FLS, sendInfoSms);
                    multiRequest.addRequest(request);
                    ++count;
                }
            }


            if (Config.isDiscardSmsWhenCountIs0()
                && inbox != null
                && inbox.isCountFetched()
                && inbox.getNewTotalCount() == 0 ) {

                // if the received mail is newer than 10 minutes we should make a retry
                // since that mail could end up in the users inbox later due to heavy load.
                long diff = 1000*60*10;
                if(email != null && !email.isReminderNotification()){
                    Date emailDate = email.getMessageReceivedDate();
                    Date now = new Date();
                    if (emailDate != null){
                        diff = now.getTime() - emailDate.getTime();
                    } // if no email Date, assume too old - leave diff as initialized and skip retry
                }
                
                if( diff < (1000*60*10)) {
                    log.logMessage("No mail in users inbox and the mail is new, retrying later. ", Logger.L_DEBUG);              
                    while( multiRequest.getCount() > 0 ) {
                        Request request = multiRequest.getNextRequest();
                        request.getResultHandler().retry(request.getId(), "No mail in users inbox and the mail is new, retrying later. ");
                        multiRequest.requestDone();
                    }
                    
                } else {
                    log.logMessage("Notification expired since count was 0. ", Logger.L_DEBUG);                  
                    while( multiRequest.getCount() > 0 ) {
                        Request request = multiRequest.getNextRequest();
                        request.getResultHandler().expired(request.getId());
                        multiRequest.requestDone();
                    }
                    
                }
            } else if( multiRequest.getCount() > 0 ) {
                int result = smsClient.sendMulti(multiRequest);
                if( result != SMSClient.SEND_OK ) {
                    while( multiRequest.getCount() > 0 ) {
                        Request request = multiRequest.getNextRequest();
                        request.getResultHandler().retry(request.getId(), "Failed to send request");
                        multiRequest.requestDone();
                    }
                }                
            }
        }


        if (feedbackHandler != null) {
            return count;
        } else {
            return 0;
        }
    }

    public void handleSMS(ANotifierSendInfoSms sendInfoSms, SMSAddress source, SMSAddress destination, SMSMessagePayload smsPayload) {
        NotifierSMSResultHandler resultHandler = NotifierSMSResultHandler.get();
        
        int id = smsClient.getNextId();
        int replacePosition = sendInfoSms.getNotificationReplacePosition();
        
        SMSRequest request = new SMSRequest(source, destination, sendInfoSms.getNotificationValidity(), 
                resultHandler, id, smsPayload.message, smsPayload.payload, replacePosition, 0);
        setRequestParameters(request, sendInfoSms); 
        
        resultHandler.add(id, sendInfoSms.getNotificationResultHandler());
        int result = smsClient.sendSMS(request);

        if( result != SMSClient.SEND_OK ) {
            resultHandler.retry(id, "Failed to send request");
        }
    }
    
    private void addToListener(int id, UserInfo user, FeedbackHandler feedbackHandler, int notifType, ANotifierSendInfoSms sendInfoSms) {
        if (sendInfoSms != null) {
            ANotifierResultHandlerSms notificationResultHandler = sendInfoSms.getNotificationResultHandler();
            NotifierSMSResultHandler.get().add(id, notificationResultHandler);
        } else {
            smsListener.add(id, user, feedbackHandler, notifType);
        }
    }

    private void setRequestParameters(Request request, ANotifierSendInfoSms sendInfoSms) {
        if (sendInfoSms != null) {
            // Set the PDU type (will be used by SMPP Client)
            request.setNotifierSmppPduType(sendInfoSms.getSmppPduType());

            // ServiceType applies to both SubmitSm and DataSm
            request.setServiceType(sendInfoSms.getSmppServiceType());
            
            // Set the setDpf flag (will be used by SMPP Client)
            if (NotifierSmppPduType.DATA_SM.equals(sendInfoSms.getSmppPduType()) &&
                NotifierPhoneOnMethod.SMS.equals(sendInfoSms.getPhoneOnMethod())) {
                request.setSetDpf(true);
            }
        }
    }
    
    /**
     * Builds an SMSMessagePayload object based on the given information. 
     */
    public SMSMessagePayload getSMSPayload(String charsetname, String content, UserMailbox inbox, NotificationEmail email, UserInfo user, boolean appendRoaming) throws TemplateMessageGenerationException
    {
        String msg = null;
        byte[] byteContent = null;
        
        if (charsetname == null)
        {
            msg = makeTextMessage(content, inbox, email, user);
        }
        else
        {
            byteContent = makeByteMessage(charsetname, content, inbox, email, user);
            if (byteContent == null || ByteArrayUtils.arrayEquals(byteContent, TemplateBytes.EMPTY))
            {
                log.logMessage("SMSOut.getSMSPayload(): could not construct array of bytes from phrases; defaulting to no charset.", Logger.L_ERROR);
                msg = makeTextMessage(content, inbox, email, user);
                byteContent = null;
            }
        }
        
        if (byteContent == null) msg = getMsgWithRoamingTemplate(appendRoaming, inbox, email, user, msg);
        else byteContent = getMsgWithRoamingTemplateBytes(charsetname, appendRoaming, inbox, email, user, byteContent);
        
        return new SMSMessagePayload(msg, byteContent);
    }

    
    /**
     *handleMWIOff acquires an SMSClient thread that handles the job of creating
     *and sending the MWI off message.
     *If mwiOffCheckCount is yes the message is turned into an mwi on if the user has
     *mails in his mailbox.
     *@param user - information from about the user receiving the notification.
     *@param ng - the entity outside smsout handling the response to a
     * complete SMS message. That SMS may have been sent as several fragments
     * each handled by an internal responsehandler.
     *@param forced - forced means dont check if MWI is disabled.
     *@param email - The notificationemail sent from MVAS.
     *@param someNumbers numbers to send to. If null, then will fetch from MCD filter 
     *       or receiver number if none.
     *@return the number of requests that are tried to send out.
     * @throws TemplateMessageGenerationException on error
     */
    public int handleMWIOff(UserInfo user,
                            FeedbackHandler ng,
                            NotificationEmail email,
                            boolean forced,
                            String[] someNumbers,
                            UserMailbox inbox) throws TemplateMessageGenerationException {
        return handleMWIOff(user, ng, email, forced, someNumbers, inbox, null); 
            
    }
    /**
     *handleMWIOff acquires an SMSClient thread that handles the job of creating
     *and sending the MWI off message.
     *If mwiOffCheckCount is yes the message is turned into an mwi on if the user has
     *mails in his mailbox.
     *@param user - information from about the user receiving the notification.
     *@param ng - the entity outside smsout handling the response to a
     * complete SMS message. That SMS may have been sent as several fragments
     * each handled by an internal responsehandler.
     *@param forced - forced means dont check if MWI is disabled.
     *@param email - The notificationemail sent from MVAS.
     *@param someNumbers numbers to send to. If null, will look at filterInfo, 
     *       if that is also null will check filter for MWI notification numbers, finally defaulting to receiver number if none.
     *@param filterInfo containing numbers to send, optional. This will contain the MWI numbers, if null will check the MCD filter.
     * sent to.
     *@return the number of requests that are tried to send out.
     * @throws TemplateMessageGenerationException on error
     */
    public int handleMWIOff(UserInfo user,
                            FeedbackHandler ng,
                            NotificationEmail email,
                            boolean forced,
			                String[] someNumbers,
                            UserMailbox inbox,
                            SmsFilterInfo filterInfo) throws TemplateMessageGenerationException {
        int count = 0;

        String []numbers = someNumbers;
        if (numbers == null) { 
            if (filterInfo == null) {
                numbers = user.getFilter().getMatchingDeliveryProfileNumbers("MWI", TRANSPORT_MOBILE);
                if (numbers == null) {
                    String[] ipMwiNumbers = user.getFilter().getMatchingDeliveryProfileNumbers("MWI", TRANSPORT_IP);
                    if (ipMwiNumbers == null) {
                        // There is no delivery profile defined for MWI, use the notification number/receiver phone number
                        String notifNumber = user.getNotifNumber();
                        if (notifNumber != null && !notifNumber.isEmpty()) {
                            numbers = new String[] { notifNumber };
                        } else {
                            numbers = new String[] { email.getReceiverPhoneNumber() };
                        }
                        log.logMessage(email.getReceiver() + " will use SMSMWI number " + Arrays.toString(numbers), Logger.L_DEBUG);
                    } else {
                        // There is a delivery profile defined for MWI (an IP MWI (TRANSPORT_IP)), no SMSMWI to be sent.
                        log.logMessage(email.getReceiver() + ", no SMSMWI will be sent since IP MWI number found " + Arrays.toString(ipMwiNumbers), Logger.L_DEBUG);
                    }
                }
            } else {
                String[] tmpnums = filterInfo.getNumbers();
                if (tmpnums == null || tmpnums.length == 0) {
                    return 0;
                }
                numbers=new String[tmpnums.length];
                int index=0;
                for (String number:tmpnums)
                if (filterInfo.isMwi(number)) {
                    numbers[index++] = number;
                }
            }
        }

        if (numbers == null || numbers.length == 0) {
            return 0;
        }

        //for MWI off we don't check the roaming, as otherwise would never go off in certain circumstances.
        if (forced || user.getFilter().isNotifTypeDisabledOnUser(NTF_MWI,null) == NotifState.ENABLED ) {
            if( inbox == null ) {
                inbox = email.getUserMailbox();
            }

            for( int i=0;i<numbers.length;i++ ) {
                String notifNumber = numbers[i];
                if (notifNumber==null) {
                    continue;
                }

                SMSAddress adr = new SMSAddress(user.getTypeOfNumber(),
                user.getNumberingPlan(), notifNumber);

                log.logMessage("SMSOut.handleMWIOff: to " + adr, Logger.L_DEBUG);
                int id = smsClient.getNextId();
                SMSAddress source = Config.getSourceAddress("mwioff", user.getCosName());
                int mwiOffValidity = user.getValidity_mwiOff();
                int mailCount = 0;
                int notifType = NTF_MWIOff;

                String msg = makeTextMessage("mwiofftext", inbox, email, user);

                //note for unsubscribed we need to send MWI off regardless of the setting. As is a forced unsubscribe from an MWI and 
                //therefore nothing to do with mail count.  even though the count will be zero in this case as the email is fake...
                //This makes for clarity, any way we check if unsubscribed..
                if (Config.isMwiOffCheckCount() && !email.isMwiOffUnsubscribed()) {
                    log.logMessage("The isMwiOffCheckCount is set to on, need to check the unread messages", Logger.L_DEBUG);
                    mailCount = inbox.getNewVideoCount()+inbox.getNewVoiceCount();

                    // If new messages still in user's mailbox, send out an MWI-On
                    if (mailCount > 0) {
                        notifType = NTF_MWI;
                        source =  Config.getSourceAddress("mwion", user.getCosName());
                        GregorianCalendar receivedDate = new GregorianCalendar();
                        NotificationFilter filter = user.getFilter();
                        email.setDepositType(depositType.VOICE);
                        SmsFilterInfo smsFilter = filter.getSmsFilterInfo(email, receivedDate, ng);
                        if (smsFilter != null && smsFilter.isMwi()) {
                            msg = makeTextMessage("mwiontext", inbox, email, user);
                            log.logMessage("Sending MWI On to " + adr + " since there are " + mailCount + " unread messages", Logger.L_DEBUG);
                        } else {
                          //don't send but indicate ok, just don't send an MWI off or on if disabled in the filter and unread messages.
                            log.logMessage("MWI disabled in user profile to " + adr + " will not send an MWI on.", Logger.L_DEBUG);
                            return count;  //should be 0 in this case.
                        }
                    }
                }

                smsListener.add(id, user, ng, notifType);
                int result = smsClient.sendMWI(source, adr, user, inbox, mwiOffValidity,
                smsListener, id, mailCount, msg);

                if (result != SMSClient.SEND_OK) {
                    smsListener.retry(id, "failed to send request");
                }
                ++count;
            }
       }
        return count;
    }


    /**
     * handleSendSMS stores the phone on data, and notifies the handler
     * thread. The calling thread then returns and the handler thread continues
     * working on the data.
     *@param validity - how long the request is valid in the smsc.
     * @throws TemplateMessageGenerationException on error
     *
     */


    public int handleSendSMS(UserInfo user,
            SmsFilterInfo info,
            FeedbackHandler ng,
            NotificationEmail email,
            UserMailbox inbox,
            SMSAddress source,
            int validity,
            int delay,
            int type) throws TemplateMessageGenerationException {
        
        if (info ==null) {
            return 0;
        }
        
        String[] numbers = info.getNumbers();

        int count = 0;

        if (numbers.length == 0) {
            return 0;
        }
        
        String charsetname = Config.getCharsetEncoding();
        if (charsetname.equals("")) charsetname = null;

        for (int i = 0; i < numbers.length; i++) {
            MultiRequest multiRequest = new MultiRequest(delay);
            SMSAddress adr = new SMSAddress(user.getTypeOfNumber(),
                    user.getNumberingPlan(), numbers[i]);


            if (info.isSms(numbers[i])) {

                int id = smsClient.getNextId();
                
                SMSMessagePayload smsPayload = getSMSPayload(charsetname, info.getNotifContent(), inbox, email, user, false);

                Request request = SMSClient.makeRequest(source, adr, user, inbox, validity, smsListener, id,
                        SMSClient.TYPE_SMS, smsPayload.message, smsPayload.payload, -1, 0 , 0);
                smsListener.add(id, user, ng, type);
                multiRequest.addRequest(request);
                ++count;
            }

            if( multiRequest.getCount() > 0 ) {
                int result = smsClient.sendMulti(multiRequest);
                if( result != SMSClient.SEND_OK ) {
                    while( multiRequest.getCount() > 0 ) {
                        Request request = multiRequest.getNextRequest();
                        request.getResultHandler().retry(request.getId(), "Failed to send request");
                        multiRequest.requestDone();
                    }

                }
            }
        }


        if (ng != null) {
            return count;
        } else {
            return 0;
        }
    }

    /**
     * handlePhoneOnRequest stores the phone on data, and notifies the handler
     * thread. The calling thread then returns and the handler thread continues
     * working on the data.
     *@param to - the address of the phone.
     *@param validity - how long the request is valid in the smsc.
     *@param preferredLanguage - what phrase file to use.
     *
     */
    public synchronized boolean handlePhoneOnRequest(SMSAddress to,
                                                     int validity,
                                                     String mail,
                                                     String preferredLanguage,
                                                     String cosName) {
        log.logMessage("SMSOut.handlePhoneOnRequest: to " + to, Logger.L_DEBUG);

        boolean result = false;
        int id = smsClient.getNextId();
        String msg = Phrases.getTemplateStrings(preferredLanguage, cosName).getProperty("smstype0text", "");
        SMSAddress source = Config.getSourceAddress(Constants.depositType.SMS_TYPE_0.source(), cosName);

        // Add the current request to the smsPhoneOnListener
        smsPhoneOnListener.add(id, to, mail);

        // Send the request to the smsClient
        int smsClientResult = smsClient.sendPhoneOn(source, to, validity, smsPhoneOnListener, id, msg);

        if (smsClientResult == SMSClient.SEND_OK) {
            result = true;
        } else if (smsClientResult == SMSClient.SEND_FAILED_TEMPORARY) {
            smsPhoneOnListener.retry(id, "Failed to send request, will retry.");
        } else {
            smsPhoneOnListener.failed(id, "Failed to send request.");
        }


        return result;
    }

    /**
     * handleVvm requests
     * @throws TemplateMessageGenerationException on error
     */
    public boolean handleVvm(VvmEvent vvmEvent) throws TemplateMessageGenerationException {

        boolean result = false;
        
        String subscriberNumber = vvmEvent.getSubscriberNumber();
        VvmEventTypes notificationType =vvmEvent.getNotificationType();
        UserInfo userInfo = vvmEvent.getUserInfo();
        NotificationEmail email = vvmEvent.getNotificationEmail();
        UserMailbox userMailbox = vvmEvent.getUserMailbox();
        
        String charsetname = Config.getCharsetEncoding();
        if (charsetname.equals("")) charsetname = null;
        
        // Create the VVM message
        SMSAddress smsAddressFrom = Config.getSourceAddress("vvm");
        SMSAddress smsAddressTo = new SMSAddress(userInfo.getTypeOfNumber(), userInfo.getNumberingPlan(), subscriberNumber);
        
        SMSMessagePayload smsPayload = getSMSPayload(charsetname, notificationType.getTemplate(), userMailbox, email, userInfo, false); // roamingFilterIsUsed is always false

        log.logMessage("SMSOut.handleVvm: to " + smsAddressTo, Logger.L_DEBUG);

        // Add the current request to the vvmListener
        int id = smsClient.getNextId();
        vvmListener.add(id, subscriberNumber, vvmEvent);
        // Package the VVM request
        Request request = SMSClient.makeRequest(smsAddressFrom, smsAddressTo, userInfo, userMailbox, Config.getValidity_vvm(), vvmListener, id, notificationType.getSmsRequestType(), smsPayload.message, smsPayload.payload, -1, 0, 0);
        int smsClientResult = smsClient.sendVvm(request);

        if (smsClientResult == SMSClient.SEND_OK) {
        	result = true;
        }  else if (smsClientResult == SMSClient.SEND_FAILED_TEMPORARY) {
        	vvmListener.retry(id, "Failed to send VvmRequest, will retry");
        } else {
        	vvmListener.failed(id, "Failed to send VvmRequest");
        }

        return result;
    }

    /**
     *Sends a slamdown list to a smsc.
     *@param to - Where to send the list.
     *@param validity - How long the message is valid in the smsc.
     *@param payload - Byte payload to send, if applicable.
     *@param list - Used when sending callbacks.
     *@param resultHandler - Where to send callbacks if L3 Notifier framework is not being used.
     */
    public void sendInfo(SMSAddress to,
            int validity,
            SlamdownPayload payload,
            SlamdownList list,
            InfoResultHandler resultHandler) {

        int id = smsClient.getNextId();
        int maxLinesPerSms;
        SMSAddress source;

        if (list.getNotificationType()==SlamdownList.NOTIFICATION_TYPE_SLAMDOWN) {
            // Slamdown case
            maxLinesPerSms = Config.getSlamdownMaxCallers();
            source = Config.getSourceAddress("slamdown", list.getCosName());
        } else {
            // Mcn case
            maxLinesPerSms = Config.getMcnMaxCallers();
            source = Config.getSourceAddress("mcn", list.getCosName());
            if (Config.isMcnSubscribedEnabled()) {
                /*
                 * The MCN-Subscribed feature is enabled.
                 * We need to check if the user's COS has MCN-Subscribed service and
                 * that the user has this feature enabled before sending the SMS
                 * notification.
                 */
                UserInfo user = list.getUserInfo();
                if (user.hasMcnSubscribedService()) {
                    String mcnState = user.getMcnSubscribedState();
                    if (mcnState != null) {
                        if (mcnState.equalsIgnoreCase(ProvisioningConstants.MCN_SUBSCRIBED_SINGLE)) {
                            // One caller per SMS
                            log.logMessage("MCN-Subscribed enabled with single caller profile for subscriber " + 
                                    to.getNumber(), Logger.L_DEBUG);
                            maxLinesPerSms = 1;
                            source = Config.getSourceAddress("mcnsinglecaller", list.getCosName());
                        } else if (mcnState.equalsIgnoreCase(ProvisioningConstants.MCN_SUBSCRIBED_MULTI)) {
                            // Multiple callers per SMS. Put the maximum number of callers in SMSs.
                            maxLinesPerSms = 0;
                            log.logMessage("MCN-Subscribed enabled with multiple callers profile for subscriber " + 
                                    to.getNumber(), Logger.L_DEBUG);
                            source = Config.getSourceAddress("mcnmultiplecallers", list.getCosName());
                        } else if (mcnState.equalsIgnoreCase(ProvisioningConstants.MCN_SUBSCRIBED_DISABLED)) {
                            // Feature disabled by user.
                            log.logMessage("MCN-Subscribed feature disabled for subscriber " +
                                    to.getNumber() + ". Discarding MCN information.", Logger.L_DEBUG);
                            resultHandler.allOk(list, 0);
                            return;
                        } else {
                            // Default value is one caller per SMS.
                            log.logMessage("Invalid MCN-Subcribed configuration value "  + mcnState + " for subscriber " 
                                    + to.getNumber() +
                                    ": defaulting to single caller profile", 
                                    Logger.L_VERBOSE);
                            maxLinesPerSms = 1;
                            source = Config.getSourceAddress("mcnsinglecaller", list.getCosName());
                        }
                    } else {
                        // Default value is one caller per SMS.
                        log.logMessage("Empty MCN-Subcribed configuration value for subscriber " + to.getNumber() +
                                ": defaulting to single caller profile", 
                                Logger.L_VERBOSE);
                        maxLinesPerSms = 1;
                        source = Config.getSourceAddress("mcnsinglecaller", list.getCosName());
                    }
                }
            }
        }

        CallerInfo[] cis = list.sortCallers();
        if (cis.length == 0) {
            log.logMessage("Failed to send sms to " + to.getNumber() + ", no callers found for called " + list.getNumber(), Logger.L_ERROR);
            // No need to go through the smsInfoListener, sending back response directly to resultHandler.
            resultHandler.failed(list);
            return;
        }

        ArrayList<String> callList = new ArrayList<String>();

        for( int i=0;i<cis.length; i++ ) {
            if( cis[i] != null ) {
                callList.add(cis[i].getNumber());
            }
        }
        String[] callers = callList.toArray(new String[0]);

        // If not denormalizing From address, make sure it does not contain a + sign
        if (!Config.denormalizeFromTag()) {
            for (int i=0; i<callers.length; i++ ) {
            	if (callers[i].startsWith("+"))
            		callers[i]=callers[i].substring(1);
            }
        }

        smsInfoListener.add(id, list, resultHandler);
        FormattedSMSRequest request = new FormattedSMSRequest(source, to, validity, smsInfoListener, id, maxLinesPerSms, callers, payload);
        int result = smsClient.sendFormattedSMS(request);

        if (result == SMSClient.SEND_OK) {
            return;
        } else if (result == SMSClient.SEND_FAILED_TEMPORARY) {
            smsInfoListener.retry(id, "Failed to send request, will retry.");
        } else {
            smsInfoListener.failed(id, "Failed to send request.");
        }
    }

    /**
     * Sends a cancel to the smsc
     * @param from the from address to cancel on
     * @param to the to address to cancel on
     * @param cancelByReplacePosition true if cancel should be made per replacePosition
     * @param notifContent the content to be used to determine replacePosition
     */
    public void sendCancel(SMSAddress from, SMSAddress to, boolean cancelByReplacePosition, String notifContent) {
        int cancelPosition = -1;
        if( cancelByReplacePosition && notifContent != null ) {
            cancelPosition = getCancelPosition(notifContent);
        }

        int result = smsClient.sendCancel(from, to, cancelPosition);
        if (result != SMSClient.SEND_OK) {
            log.logMessage("Failed to send cancel sms to " + to, Logger.L_VERBOSE);
        }

    }
    
    
    /**
     * Sends a cancel to the smsc
     * @param from the from address to cancel on
     * @param to the to address to cancel on
     * @param Position - the position in the replace/cancel table.
     */
    public void sendCancelbyPosition(SMSAddress from, SMSAddress to, int Position) {

        int result;
        
        result = smsClient.sendCancel(from, to, Position);

        if (result != SMSClient.SEND_OK) {
            log.logMessage("Failed to send cancel sms to " + to, Logger.L_VERBOSE);
        }
    }
    
    
    /**
     * Sends a cancel to the SMSC
     * @param from the from address to cancel on
     * @param to the to address to cancel on
     * @param user the user to send cancel to
     * @param ServiceType - the ServiceType to cancel, null if none.
     */
    public void sendCancelbyServiceType(SMSAddress from, SMSAddress to, String ServiceType) {

        int result;
        if( ServiceType != null) {
           result = smsClient.sendCancel(from,to,ServiceType);
        } else {
            result = smsClient.sendCancel(from, to, -1);
        }
        if (result != SMSClient.SEND_OK) {
            log.logMessage("Failed to send cancel sms to " + to, Logger.L_VERBOSE);
        }
    }
    
    
    
    
    

    /**
     * Looks up the content in the phrases file to get the message to send.
     * @throws TemplateMessageGenerationException on error
     */
    private String makeTextMessage(String content, UserMailbox inbox, NotificationEmail email, UserInfo user) throws TemplateMessageGenerationException {
        String smsString = null;

        smsString = TextCreator.get().generateText(inbox, email, user, content, true, null);

        return smsString;
    }

    /**
     * Looks up the content in the phrases file to get the message to send, in bytes.
     */
    private byte[] makeByteMessage(String charsetname, String content, UserMailbox inbox, NotificationEmail email,
            UserInfo user) throws TemplateMessageGenerationException {
        byte[] smsBytes = null;
        smsBytes = TextCreator.get().generateBytes(charsetname, inbox, email, user, content, true, null);
        return smsBytes;
    }

    /**
     * Finds the template for roaming only if checkRoaming=true and the user is roaming.
     * If the template is not null, then it will be added to the original message that was supposed to be sent.
     * @throws TemplateMessageGenerationException on error
     */
    private String getMsgWithRoamingTemplate(boolean roamingFilterIsUsed, UserMailbox inbox, NotificationEmail email, UserInfo user, String msg) throws TemplateMessageGenerationException {
    	if (roamingFilterIsUsed  && Config.isRoamingTemplateUsed()) {
    		String roamingString = TextCreator.get().generateText(inbox, email, user, "roaming", true, null);
    		if (roamingString != null) {
    			if (Config.isBeginRoamingTemplatePosition()) {
    				msg = roamingString.concat(msg);
    			} else if (Config.isEndRoamingTemplatePosition() && (msg != null)) {
    				msg = msg.concat(roamingString);
    			}
    		}
    	}
    	return msg;
    }
    /**
     * Finds the template for roaming only if checkRoaming=true, the user is roaming and configured to use roaming template.
     * If the template is not null, then it will be added to the original message that was supposed to be sent.
     * @throws TemplateMessageGenerationException if failed to generate text. 
     */
    
    private byte[] getMsgWithRoamingTemplateBytes(String charsetname, boolean appendRoaming, UserMailbox inbox, NotificationEmail email, UserInfo user, byte[] msg) throws TemplateMessageGenerationException {
        if (appendRoaming && Config.isRoamingTemplateUsed()) {
            byte[] roamingdata = TextCreator.get().generateBytes(charsetname, inbox, email, user, SMS_ROAMING_TEMPLATE, true, null);
            if (roamingdata != null && !ByteArrayUtils.arrayEquals(msg, TemplateBytes.EMPTY)) {
                if (Config.isBeginRoamingTemplatePosition()) {
                    msg = ByteArrayUtils.append(roamingdata, msg);
                } else if (Config.isEndRoamingTemplatePosition() && (msg != null && !ByteArrayUtils.arrayEquals(msg, TemplateBytes.EMPTY))) {
                    msg = ByteArrayUtils.append(msg, roamingdata);
                }
            }
        }
        return msg;
    }

    public int getReplacePositionWithTerminalCheck(String content, UserInfo user) {
        if(  !user.terminalSupportsReplace() ) {
            return -1;
        }
        return getReplacePosition(content, user);
    }

    public int getReplacePosition(String content, UserInfo user) {
        if( !user.hasReplace() ) {
            return -1;
        }
        return (serviceTypeLookup.getPosition(content));

    }
    
    public int getCancelPosition(String content) {
        return (serviceTypeLookup.getPosition(content));

    }

    public boolean ifLog(int level) {
        return Config.getLogLevel() >= level;
    }

    public void logString(String msg, int l) {
        log.logMessage(msg, l);
    }

    public void connectionDown(String name) {
        ManagementInfo.get().getStatus(NotificationConfigConstants.SHORT_MESSAGE_TABLE, name).down();
        log.logMessage("No connection to SMSC " + name, Logger.L_ERROR );
    }

    public void connectionUp(String name) {
        ManagementInfo.get().getStatus(NotificationConfigConstants.SHORT_MESSAGE_TABLE, name).up();
    }

    public void connectionReset(String name) {// do nothing
    }
    
    public void connectionTemporaryUnavailableForNewRequests() {// do nothing
    }
}

