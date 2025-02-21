/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Iterator;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseCosProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.mcd.McdUserInfo;


public class NotifierDatabaseSubscriberProfile extends ANotifierDatabaseSubscriberProfile {
    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierDatabaseSubscriberProfile.class);
    
    String subscriberNumber = null;
    IDirectoryAccessSubscriber directoryAccessSubscriber = null;
    
    public NotifierDatabaseSubscriberProfile(String subNumber, IDirectoryAccessSubscriber subProfile) {
        subscriberNumber = subNumber;
        directoryAccessSubscriber = subProfile;
    }

    @Override
    public String[] getSubscriberIdentities(String scheme) {            
        return directoryAccessSubscriber.getSubscriberIdentities(scheme);
    }

    @Override
    public String[] getStringAttributes(String attributeName) {
        return directoryAccessSubscriber.getStringAttributes(attributeName);
    }

    @Override
    public int[] getIntegerAttributes(String attributeName) {
        return directoryAccessSubscriber.getIntegerAttributes(attributeName);
    }

    @Override
    public boolean[] getBooleanAttributes(String attributeName) {
        return directoryAccessSubscriber.getBooleanAttributes(attributeName);
    }

    @Override
    public ANotifierDatabaseCosProfile getCosProfile() {
        return new NotifierDatabaseCosProfile(directoryAccessSubscriber.getCosProfile());
    }        

    @Override
    public Iterator<String> getAttributeNameIterator() {
        return directoryAccessSubscriber.getSubscriberProfile().getProfile().attributeIterator();
    }        
    
    @Override
    public String[] getSubscriberNotificationNumbers(NotificationType notificationType, ANotifierNotificationInfo notificationInfo) {
        String[] notifNumbers;
        
        //When getting notification numbers, put check for MOIPNotifDisabled as false.  So, MOIPNotifDisabled is not implicitly checked.
        //If the plug-in would like to apply NotifDisabled, it can fetch the MOIPNotifDisabled attribute before calling this method.
        String [] filterstrings  = getStringAttributes(DAConstants.ATTR_FILTER);
        String[] profileStrings = getStringAttributes(DAConstants.ATTR_DELIVERY_PROFILE);
        NotificationFilter filter = new NotificationFilter(filterstrings, false, new McdUserInfo(directoryAccessSubscriber), profileStrings);
        
        switch(notificationType) {
            case SMS:
                notifNumbers = filter.getNotifNumbers("SMS", Constants.TRANSPORT_MOBILE, subscriberNumber, false);
                break;
            default:
                log.error("getSubscriberNotificationNumbers: NotificationType is not supported: " + notificationType.toString());
                notifNumbers = new String[0];
        }
        log.debug("getSubscriberNotificationNumbers: notification numbers retrieved for " + subscriberNumber + ": " + Arrays.toString(notifNumbers));
        return notifNumbers;
    }

    @Override
    public String[] getSubscriberNotificationNumbersForNewMessageDeposit(NotificationType notificationType, ANotifierNotificationInfo notificationInfo) throws NotifierMfsException {
        String[] notifNumbers;
        try {
            //By calling this implementation, the plug-in is agreeing to use the same rules that NTF normally uses to get the notification numbers,
            //including checking MOIPNotifDisabled, etc.
            NotificationEmail email = new NotificationEmail(new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), notificationInfo.getProperties()));
            email.init();
            UserInfo userInfo = new McdUserInfo(directoryAccessSubscriber);
            NotificationFilter filter = userInfo.getFilter();
            switch(notificationType) {
                case SMS:
                    SmsFilterInfo smsFilterInfo = filter.getSmsFilterInfo(email, new GregorianCalendar(), null);
                    if (smsFilterInfo == null) {
                        log.error("getSubscriberNotificationNumbersForNewMessageDeposit: smsFilterInfo is null");
                        notifNumbers = new String[0];
                    } else {
                        notifNumbers = smsFilterInfo.getNumbers();
                    }
                    break;
                default:
                    log.error("getSubscriberNotificationNumbersForNewMessageDeposit: NotificationType is not supported: " + notificationType.toString());
                    notifNumbers = new String[0];
            }
        } catch (MsgStoreException e) {
            log.error("getSubscriberNotificationNumbersForNewMessageDeposit: MsgStoreException occured: " + e.getMessage());
            throw new NotifierMfsException(e.getMessage());
        }
        log.debug("getSubscriberNotificationNumbersForNewMessageDeposit: notification numbers retrieved for " + subscriberNumber + ": " + Arrays.toString(notifNumbers));
        return notifNumbers;
    }


    public String toString() {
        return directoryAccessSubscriber.getSubscriberProfile().toString();
    }
}
