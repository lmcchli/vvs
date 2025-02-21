/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.send.sms;

import java.util.concurrent.BlockingQueue;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierResultHandlerSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.SMSAddressInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.database.NotifierDatabaseHelper;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierConfig;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierConfigConstants;

/**
 * The NotifierSendInfoSms is a container for the information regarding the sending of a notification.
 */
public class NotifierSendInfoSms extends ANotifierSendInfoSms {

    private static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierSendInfoSms.class);
    
 
    private int notificationReplacePosition = -1;
    private int notificationValidity = NOTIFICATION_SMSC_VALIDITY_HOURS_DEFAULT;
    private NotifierSmppPduType notifierSmppPduType = null;
    private String smppServiceType = null;
    private int sourceTon = 0;
    private int sourceNpi = 0;
    private String sourceNumber = null;
    private int destinationTon = 0;
    private int destinationNpi = 0;
    private String destinationNumber = null;
    private NotifierPhoneOnMethod notifierPhoneOnMethod = null;
    private ANotifierResultHandlerSms notificationResultHandler = null;
    
    public NotifierSendInfoSms(NotifierEvent notifierEvent, BlockingQueue<NotifierEvent>  notifierWorkerQueue) {        
        TemplateType notifierType = notifierEvent.getNotifierType();
        
        this.notificationValidity = getNotifValidityInSmsc(notifierEvent.getNotifierTypeName(), notifierEvent.getSubscriberProfile());
        this.notifierSmppPduType = notifierType.getNotifierTypePdu();        
        this.smppServiceType = notifierType.getServiceType();
        
        this.notificationReplacePosition=notifierType.getReplacePosition();
        //if not already disabled (-1), check profile.
        if (this.notificationReplacePosition > -1 && NotifierDatabaseHelper.isSubscriberReplaceDisabled(notifierEvent.getSubscriberProfile())) {
        	this.notificationReplacePosition=-1; //no replace for this sub, disabled in profile.
        }
        this.destinationTon = NotifierConfig.getTypeOfNumber();
        this.destinationNpi = NotifierConfig.getNumberingPlanIndicator();
        this.destinationNumber = notifierEvent.getNotificationNumber();
        setSourceAddress(notifierEvent);
        
        this.notificationResultHandler = new NotifierResultHandlerSms(notifierEvent, notifierWorkerQueue);
    }
    
    private int getNotifValidityInSmsc(String notifierTypeName, ANotifierDatabaseSubscriberProfile subscriberProfile) {
        int validity = NotifierConfig.getValidity(notifierTypeName);
        if (validity == NotifierConfigConstants.DEFAULT_VALIDITY_VALUE) {
            validity = NotifierDatabaseHelper.getNotificationSmscExpirationTime(subscriberProfile);
        }
        return validity;
    }
    
    private void setSourceAddress(NotifierEvent notifierEvent) {
        String cphrtemplateName = notifierEvent.getCphrtemplateName();
        String cosName = NotifierDatabaseHelper.getCosName(notifierEvent.getSubscriberProfile());
        SMSAddressInfo sourceAddress = NotifierConfig.getSourceAddress(cphrtemplateName, cosName);
        if (sourceAddress != null) { 
        	sourceTon=sourceAddress.getTON();
        	sourceNpi=sourceAddress.getNPI();
        	sourceNumber = sourceAddress.getAddress();
        	log.debug("Sms source address set with TON=" + sourceTon + " NPI=" + sourceNpi + " sourceNumber=" + sourceNumber);
        } else {
        	log.warn("Unable to read source Address for CPHR template name: " + cphrtemplateName);
        }
    }
    

    @Override
    public int getNotificationReplacePosition() {
        return notificationReplacePosition;
    }

    @Override
    public int getNotificationValidity() {
        return notificationValidity;
    }

    @Override
    public NotifierSmppPduType getSmppPduType() {
        return notifierSmppPduType;
    }

    @Override
    public String getSmppServiceType() {
        return smppServiceType;
    }

    @Override
    public int getSourceAddressTypeOfNumber() {
        return sourceTon;
    }

    @Override
    public int getSourceAddressNumberingPlanIndicator() {
        return sourceNpi;
    }

    @Override
    public String getSourceAddressNumber() {
        return sourceNumber;
    }

    @Override
    public int getDestinationAddressTypeOfNumber() {
        return destinationTon;
    }

    @Override
    public int getDestinationAddressNumberingPlanIndicator() {
        return destinationNpi;
    }

    @Override
    public String getDestinationAddressNumber() {
        return destinationNumber;
    }
    
    @Override
    public boolean getIsDestinationAddressNumberNormalized() {
        //The notification number is always being normalised in this plug-in.
        //So, hard-code true here instead of storing and propagating it in an event property.
        return true;
    }
    
    @Override
    public NotifierPhoneOnMethod getPhoneOnMethod() {
        return notifierPhoneOnMethod;
    }

    @Override
    public ANotifierResultHandlerSms getNotificationResultHandler() {
        return notificationResultHandler;
    }    
}
