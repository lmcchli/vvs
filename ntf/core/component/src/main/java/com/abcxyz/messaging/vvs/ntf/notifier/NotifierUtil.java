/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.Properties;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.NotifierUtilException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.NotifierUtilException.NotifierUtilMessageException;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.cmnaccess.UserInbox;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;


public class NotifierUtil implements INotifierUtil {

    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierUtil.class);
    private static NotifierUtil instance = null;
    
    private NotifierUtil() {        
    }
    
    public static NotifierUtil get() {
        if(instance == null) {
            instance = new NotifierUtil();
        }
        return instance;
    }

    @Override
    public String getNormalizedTelephoneNumber(String telephoneNumber) {
        String normalizedNumber = CommonMessagingAccess.getInstance().denormalizeNumber(telephoneNumber);
        log.debug("Telephone number " + telephoneNumber + " normalized to: " + normalizedNumber);
        return normalizedNumber;
    }

    @Override
    public boolean isFileStorageOperationsAvailable(String telephoneNumber) {
        boolean isAvailable = CommonMessagingAccess.getInstance().isStorageOperationsAvailable(telephoneNumber);
        log.debug("Storage operations for telephone number " + telephoneNumber + " is available: " + isAvailable);
        return isAvailable;
    }

         
    /************************************
     * EVENT SCHEDULING UTILITY METHODS *
     ************************************/    
    
    @Override
    public String getUniqueEventSchedulingId() {
        return NtfEvent.getUniqueId();
    }
    
    @Override
    public String getEventServiceName(String eventIdString) {
        String serviceName = null;
        try {
            EventID eventId = new EventID(eventIdString);
            serviceName = eventId.getServiceName();
            log.debug("Service name extracted from " + eventIdString + ": " + serviceName);
        } catch (InvalidEventIDException e) {
            log.error("InvalidEventIDException in getEventServiceName: " + e.getMessage(), e);
        }
        return serviceName;
    }

    @Override
    public Properties getEventProperties(String eventIdString) {
        Properties properties = null;
        try {
            EventID eventId = new EventID(eventIdString);
            properties = eventId.getEventProperties();
            log.debug("Properties extracted from " + eventIdString + ": " + properties);
        } catch (InvalidEventIDException e) {
            log.error("InvalidEventIDException in getEventServiceName: " + e.getMessage(), e);
        }
        return properties;
    }


    /***************************
     * MESSAGE UTILITY METHODS *
     ***************************/    

    @Override
    public boolean isNewMessagesInInbox(String msid) throws NotifierUtilException {
        boolean result = false;
        try {
            if (msid != null && !msid.isEmpty()) {
                MSA rmsa = new MSA(msid);
                UserInbox userInbox = new UserInbox(rmsa);
                userInbox.addFilter(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
                int count = userInbox.countStateFile(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
                if (count > 0) {
                    log.debug(count + " new message(s) for " + rmsa + " found");
                    result = true;
                } else {
                    log.debug("No new message(s) for " + rmsa + " found");
                }

            } else {
                String msg = "No valid msid to fetch if new messages exists, msid: " + msid;
                log.error(msg);
                throw new NotifierUtilException(msg, NotifierUtilMessageException.PERMANENT_ERROR);
            }
        } catch (MsgStoreException mse) {
            log.error("NotifierUtil MsgStoreException: " + mse.getMessage(), mse);
            if (mse.getIsPermanent()) {
                throw new NotifierUtilException(mse.getMessage(), NotifierUtilMessageException.PERMANENT_ERROR);
            } else {
                throw new NotifierUtilException(mse.getMessage(), NotifierUtilMessageException.TEMPORARY_ERROR);
            }
        } catch (Exception e) {
            log.error("NotifierUtil Exception: " + e.getMessage(), e);
            throw new NotifierUtilException(e.getMessage(), NotifierUtilMessageException.PERMANENT_ERROR);
        }
        return result;
    }

}
