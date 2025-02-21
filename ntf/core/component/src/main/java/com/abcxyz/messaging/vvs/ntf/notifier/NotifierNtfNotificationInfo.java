/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfNotificationInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.MessageCleaner;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.ntf.Constants;


public class NotifierNtfNotificationInfo implements INotifierNtfNotificationInfo {
    private static final String FROM_PROPERTY = "frm";
    
    private Properties eventProperties = null;
    
    public NotifierNtfNotificationInfo(Properties eventProperties) {
        this.eventProperties = eventProperties;
    }
    

    @Override
    public Properties getNotificationEventProperties() {
        return eventProperties;
    }

    @Override
    public String getSenderTelephoneNumber() {        
        return eventProperties.getProperty(FROM_PROPERTY);
    }

    @Override
    public String getReceiverTelephoneNumber() {
        return eventProperties.getProperty(Constants.DEST_RECIPIENT_ID);
    }

    @Override
    public String getSlamdownNotificationTelephoneNumber() {
        return eventProperties.getProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY);
    }

    @Override
    public String getSlamdownEventFileName() {
        return eventProperties.getProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY);
    }

    private String getMsgState() {
        return eventProperties.getProperty(MessageCleaner.MESSAGE_STATE);
    }

    @Override
    public boolean getIsMsgStateNew() {
        return MoipMessageEntities.MESSAGE_NEW.equalsIgnoreCase(getMsgState());
    }

    @Override
    public boolean getIsMsgStateRead() {
        return MoipMessageEntities.MESSAGE_READ.equalsIgnoreCase(getMsgState());
    }

    @Override
    public boolean getIsMsgStateSaved() {
        return MoipMessageEntities.MESSAGE_SAVED.equalsIgnoreCase(getMsgState());
    }
    
    @Override
    public String getOmsa() {
        return eventProperties.getProperty(NtfEvent.OMSA);
    }

    @Override
    public String getRmsa() {
        return eventProperties.getProperty(NtfEvent.RMSA);
    }

}
