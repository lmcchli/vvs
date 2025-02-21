/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.Collection;
import java.util.GregorianCalendar;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.vvs.ntf.notifier.cancel.CancelSmsHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.NotifierMessageGenerationException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.NotifierSendException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.NotifierSendException.NotifierSendExceptionCause;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.INotifierSenderSms;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.sms.request.SMSMessagePayload;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.mcd.McdUserInfo;


public class NotifierSenderSms implements INotifierSenderSms {

    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierSenderSms.class);
    private static NotifierSenderSms instance = null;
    
    private NotifierSenderSms() {        
    }
    
    public static NotifierSenderSms get() {
        if(instance == null) {
            instance = new NotifierSenderSms();
        }
        return instance;
    }

    @Override
    public void sendNewMessageDepositNotificationSMS(ANotifierNotificationInfo notificationInfo, 
            ANotifierDatabaseSubscriberProfile subscriberProfile, ANotifierSendInfoSms sendInfoSms) throws NotifierMessageGenerationException, NotifierSendException {

        if(notificationInfo == null || subscriberProfile == null || sendInfoSms == null) {
            String errorMsg = "Failed to send notification SMS due to null value(s) for " 
                    + (notificationInfo==null ? "notificationInfo " : "") + (subscriberProfile==null ? "subscriberProfile " : "")
                    + (sendInfoSms==null ? "sendInfoSms " : "");
            log.error(errorMsg);
            throw new NotifierSendException(errorMsg);
        }
        log.debug("sendNewMessageDepositNotificationSMS called with sending info: " + sendInfoSms.toString());
        try {
            UserInfo userInfo = new McdUserInfo(new NotifierDirectoryAccessSubscriber(subscriberProfile));
            NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), notificationInfo.getProperties());
            NotificationEmail email = new NotificationEmail(ntfEvent);
            email.init();

            NotificationFilter filter = userInfo.getFilter();
            SmsFilterInfo smsFilterInfo = filter.getSmsFilterInfo(email, new GregorianCalendar(), null); 
            if(smsFilterInfo != null) {
                if(smsFilterInfo.getNumbers().length > 0) {
                    boolean isNotifNumberSet = smsFilterInfo.limitNotificationNumbersTo(sendInfoSms.getDestinationAddressNumber(), sendInfoSms.getIsDestinationAddressNumberNormalized(), SmsFilterInfo.SmsNotificationType.SMS);
                    if(isNotifNumberSet) {
                        log.debug("Notification numbers limited to: " + sendInfoSms.getDestinationAddressNumber());
                        SMSAddress smsAddressSource = new SMSAddress(sendInfoSms.getSourceAddressTypeOfNumber(), sendInfoSms.getSourceAddressNumberingPlanIndicator(), sendInfoSms.getSourceAddressNumber());
                        SMSOut.get().handleSMS(userInfo, smsFilterInfo, null, email, email.getUserMailbox(), smsAddressSource, sendInfoSms.getNotificationValidity(), 0, sendInfoSms);
                    } else {
                        String errorMsg = "The notification number is invalid according to subscriber's SmsFilterInfo; notification not sent to: " + sendInfoSms.getDestinationAddressNumber();
                        log.error(errorMsg);
                        throw new NotifierSendException(errorMsg, NotifierSendExceptionCause.SUBSCRIBER_DATABASE_PROFILE);
                    }
                } else {
                    String errorMsg = "No notification number in subscriber's SmsFilterInfo; notification not sent to: " + sendInfoSms.getDestinationAddressNumber();
                    log.error(errorMsg);
                    throw new NotifierSendException(errorMsg, NotifierSendExceptionCause.SUBSCRIBER_DATABASE_PROFILE);
                }
            } else {
                String errorMsg = "Subscriber's SmsFilterInfo is null; notification not sent to: " + sendInfoSms.getDestinationAddressNumber();
                log.error(errorMsg);
                throw new NotifierSendException(errorMsg, NotifierSendExceptionCause.SUBSCRIBER_DATABASE_PROFILE);
            }
        } catch (MsgStoreException e) {
            String errorMsg = "MsgStoreException thrown while trying to send SMS to " + sendInfoSms.getDestinationAddressNumber() + ": " + e.getMessage();
            log.error(errorMsg);
            throw new NotifierSendException(errorMsg, NotifierSendExceptionCause.MESSAGE_STORE_ERROR);
        } catch (TemplateMessageGenerationException e) {
            String errorMsg = "TemplateMessageGenerationException thrown while trying to send SMS to " + sendInfoSms.getDestinationAddressNumber() + ": " + e.getMessage();
            log.error(errorMsg);
            throw new NotifierMessageGenerationException(errorMsg, NotifierMessageGenerator.get().getMessageGenerationExceptionCause(e));
        }  
    }

    @Override
    public void sendNotificationSms(ANotifierSendInfoSms sendInfoSms, String notificationMessage) throws NotifierSendException{
        
        SMSMessagePayload notificationPayload = new SMSMessagePayload(notificationMessage);
        sendNotificationSms(sendInfoSms, notificationPayload);
    }

    @Override
    public void sendNotificationSms(ANotifierSendInfoSms sendInfoSms, byte[] notificationMessage) throws NotifierSendException{
        
        SMSMessagePayload notificationPayload = new SMSMessagePayload(notificationMessage);
        sendNotificationSms(sendInfoSms, notificationPayload);
    }
    
    
    private void sendNotificationSms(ANotifierSendInfoSms sendInfoSms, SMSMessagePayload notificationPayload) throws NotifierSendException{

        if(sendInfoSms == null || notificationPayload == null) {
            String errorMsg = "Failed to send notification SMS due to null value(s) for " 
                    + (sendInfoSms==null ? "sendInfoSms " : "") + (notificationPayload==null ? "notificationPayload " : "");
            log.error(errorMsg);
            throw new NotifierSendException(errorMsg);
        }
        log.debug("sendNewMessageDepositNotificationSMS called with sending info: " + sendInfoSms.toString());
        try {
            SMSAddress smsAddressDestination = new SMSAddress(sendInfoSms.getDestinationAddressTypeOfNumber(), sendInfoSms.getDestinationAddressNumberingPlanIndicator(), sendInfoSms.getDestinationAddressNumber());
            SMSAddress smsAddressSource = new SMSAddress(sendInfoSms.getSourceAddressTypeOfNumber(), sendInfoSms.getSourceAddressNumberingPlanIndicator(), sendInfoSms.getSourceAddressNumber());
            SMSOut.get().handleSMS(sendInfoSms, smsAddressSource, smsAddressDestination, notificationPayload);
        } catch (Throwable t) {
            log.error("Failed to send notification SMS: ", t);
            throw new NotifierSendException("Exception while sending message.");
        }
    }
    
    @Override
    public void sendCancelSMS(CancelInfo info) {
        CancelSmsHandler.get().cancel(info);
    }
    
    @Override
    public void sendCancelSMS(Collection<CancelInfo> infoSet) {
        CancelSmsHandler.get().cancel(infoSet);
    }

}
