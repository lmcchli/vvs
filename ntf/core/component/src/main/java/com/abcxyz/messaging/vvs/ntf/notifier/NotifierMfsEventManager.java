/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.ANotifierSlamdownCallInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.INotifierMfsEventManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.INotifierNewMessageCallInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException.NotifierMfsExceptionCause;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierSlamdownCallInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager.FileStatusEnum;
import com.mobeon.ntf.mail.NotificationEmail;


public class NotifierMfsEventManager implements INotifierMfsEventManager {

    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierMfsEventManager.class);
    private static NotifierMfsEventManager instance = null;
    
    private MfsEventManager mfsEventManager = null;
    
    private NotifierMfsEventManager() {
        mfsEventManager = MfsEventFactory.getMfsEvenManager();
    }

    public static NotifierMfsEventManager get() {
        if(instance == null) {
            instance = new NotifierMfsEventManager();
        }
        return instance;
    }
    
    @Override
    public NotifierFileStatusEnum fileExistsValidation(String telephoneNumber, String fileName, int validityPeriodInMin) {
        NotifierFileStatusEnum notifierFilestatus = NotifierFileStatusEnum.UNABLE_TO_DETERMINE_STATUS;
        
        FileStatusEnum fileStatus = mfsEventManager.fileExistsValidation(telephoneNumber, fileName, validityPeriodInMin);
        switch(fileStatus) {
            case FILE_DOES_NOT_EXIST:
                notifierFilestatus = NotifierFileStatusEnum.FILE_DOES_NOT_EXIST;
                break;
            case FILE_EXISTS_NO_VALIDATION:
                notifierFilestatus = NotifierFileStatusEnum.FILE_EXISTS_NO_VALIDATION;
                break;
            case FILE_EXISTS_AND_VALID:
                notifierFilestatus = NotifierFileStatusEnum.FILE_EXISTS_AND_VALID;
                break;
            case FILE_EXISTS_AND_INVALID:
                notifierFilestatus = NotifierFileStatusEnum.FILE_EXISTS_AND_INVALID;
                break;
        }
        log.debug("Got file exist/validity status for telephoneNumber=" + telephoneNumber + " fileName=" + fileName +  " validityPeriodInMin=" + validityPeriodInMin + " :" + notifierFilestatus.toString());
        return notifierFilestatus;
    }

    @Override
    public boolean storeProperties(String telephoneNumber, String fileName, Properties properties) {
        boolean isStored = false;
        try {
            mfsEventManager.storeProperties(telephoneNumber, fileName, properties);
            log.debug("Store properties succeeded for telephoneNumber=" + telephoneNumber + " fileName=" + fileName + " properties=" + properties);
            isStored = true;
        } catch (TrafficEventSenderException e) {
            log.error("Store properties failed for telephoneNumber=" + telephoneNumber + " fileName=" + fileName + " properties=" + properties + ": " + e.getMessage());
        }
        return isStored;
    }

    @Override
    public Properties getProperties(String telephoneNumber, String fileName) {
        Properties props = mfsEventManager.getProperties(telephoneNumber, fileName);
        log.debug("Got properties for telephoneNumber=" + telephoneNumber + " fileName=" + fileName + ": " + props);
        return props;
    }

    @Override
    public boolean removeFile(String telephoneNumber, String fileName) {
        boolean isRemoved = false;
        try {
            isRemoved = mfsEventManager.removeFile(telephoneNumber, fileName);
            log.debug("Remove file succeeded for telephoneNumber=" + telephoneNumber + " fileName=" + fileName);
        } catch (TrafficEventSenderException e) {
            log.error("Remove file failed for telephoneNumber=" + telephoneNumber + " fileName=" + fileName + ": " + e.getMessage());
        }
        return isRemoved;
    }

    @Override
    public long acquireLockFile(String telephoneNumber, String lockFileName, int validityPeriodInSeconds) {
        long lockId = 0;
        try {
            lockId = mfsEventManager.acquireLockFile(telephoneNumber, lockFileName, validityPeriodInSeconds);
        } catch (TrafficEventSenderException e) {
            log.error("Exception while acquiring lock file failed for telephoneNumber=" + telephoneNumber + " lockFileName=" + lockFileName +  " validityPeriodInSeconds=" + validityPeriodInSeconds + ": " + e.getMessage());
        }
        log.debug("Returning acquired lock file id for telephoneNumber=" + telephoneNumber + " lockFileName=" + lockFileName +  " validityPeriodInSeconds=" + validityPeriodInSeconds + ": " + lockId);
        return lockId;
    }

    @Override
    public boolean releaseLockFile(String telephoneNumber, String lockFileName, long lockId) {
        boolean isReleased = false;
        try {
            mfsEventManager.releaseLockFile(telephoneNumber, lockFileName, lockId);
            log.debug("Release lock file succeeded for telephoneNumber=" + telephoneNumber + " lockFileName=" + lockFileName +  " lockId=" + lockId);
            isReleased = true;
        } catch (TrafficEventSenderException e) {
            log.error("Release lock file failed for telephoneNumber=" + telephoneNumber + " lockFileName=" + lockFileName +  " lockId=" + lockId + ": " + e.getMessage());
        }
        return isReleased;
    }

    @Override
    public File[] getEventFiles(String telephoneNumber, FileFilter fileFilter) {
        File[] files = mfsEventManager.getEventFiles(telephoneNumber, fileFilter);
        log.debug("Got filtered event files for telephoneNumber=" + telephoneNumber + ": " + Arrays.toString(files));
        return files;
    }

    @Override
    public String getFileContentAsString(String telephoneNumber, String fileName) throws NotifierMfsException {
        if (telephoneNumber == null || fileName == null) {
            String errorMsg = "Unable to get file content because of null value: telephoneNumber=" + telephoneNumber + " fileName=" + fileName;
            log.error(errorMsg);
            throw new NotifierMfsException(errorMsg, NotifierMfsExceptionCause.FILE_DOES_NOT_EXIST);
        }
        String content = null;
        try {
            content = MfsEventManager.getStringPayload(telephoneNumber, fileName);
        } catch (TrafficEventSenderException e) {
            String errorMsg = "Unable to get file content: " + e.getMessage();
            log.error(errorMsg);
            throw new NotifierMfsException(errorMsg, getNotifierMfsExceptionCause(e));
        }
        log.debug("Retrieved file content for telephoneNumber=" + telephoneNumber + " fileName=" + fileName + ": " + content);
        return content;
    }

    @Override
    public byte[] getFileContentAsBytes(String telephoneNumber, String fileName) throws NotifierMfsException {
        if (telephoneNumber == null || fileName == null) {
            String errorMsg = "Unable to get file content because of null value: telephoneNumber=" + telephoneNumber + " fileName=" + fileName;
            log.error(errorMsg);
            throw new NotifierMfsException(errorMsg, NotifierMfsExceptionCause.FILE_DOES_NOT_EXIST);
        }
        byte[] content = null;
        try {
            content = MfsEventManager.getBytePayload(telephoneNumber, fileName);
        } catch (TrafficEventSenderException e) {
            String errorMsg = "Unable to get file content: " + e.getMessage();
            log.error(errorMsg);
            throw new NotifierMfsException(errorMsg, getNotifierMfsExceptionCause(e));
        }
        log.debug("Retrieved file content for telephoneNumber=" + telephoneNumber + " fileName=" + fileName + ": " + content);
        return content;
    }

    @Override
    public ANotifierSlamdownCallInfo[] getSlamdownCallInfo(String telephoneNumber, String fileName) throws NotifierMfsException {
        ANotifierSlamdownCallInfo[] callInfoArray = null;
        try {            
            TrafficEvent[] events = mfsEventManager.retrieveEvents(telephoneNumber, fileName);
            //Slamdown file should contain at least 2 lines: first line is the mas aggregation eventId used by MAS only and other lines are slamdown info.
            if(events != null && events.length >= 2) {
                callInfoArray = new NotifierSlamdownCallInfo[events.length-1];
                //Skip first line containing MAS aggregation eventId.
                for(int i=1; i < events.length; i++) {
                    String caller = events[i].getProperties().get(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY);
                    if(caller == null) {
                        String errorMsg = "Caller of event in file " + fileName + " is null.";
                        log.error(errorMsg);
                        throw new NotifierMfsException(errorMsg, NotifierMfsExceptionCause.FILE_PARSING_ERROR);
                    }
                    long time = new Long(events[i].getProperties().get(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY)).longValue();
                    Date date = new Date(time);
                    //callInfoArray[i-1] = new NotifierSlamdownCallInfo(caller, date);
                    NotifierSlamdownCallInfo callInfo = new NotifierSlamdownCallInfo(caller, date);
                    
                    HashMap<String, String> eventProperties = events[i].getProperties();
                    Properties slamdownProperties = new Properties();
                    for (Map.Entry<String, String> entry : eventProperties.entrySet()) {
                        log.debug("Adding Property: " + entry.getKey() + " with Value: " + entry.getValue() + " to NotifierSlamdownCallInfo");
                        slamdownProperties.put(entry.getKey(), entry.getValue());
                    }
                    callInfo.setProperties(slamdownProperties);
                    
                    callInfoArray[i-1] = callInfo;
                }
            } else {
                String errorMsg = "File " + fileName + " does not contain the expected minimum number of lines.";
                log.error(errorMsg);
                throw new NotifierMfsException(errorMsg, NotifierMfsExceptionCause.FILE_PARSING_ERROR);
            }
        } catch (NumberFormatException nfe) {
            String errorMsg = "Date of event in file " + fileName + " cannot be parsed into a number.";
            log.error(errorMsg);
            throw new NotifierMfsException(errorMsg, NotifierMfsExceptionCause.FILE_PARSING_ERROR);
        } catch (TrafficEventSenderException e) {
            String errorMsg = "Unable to retrieve events from file " + fileName + ": " + e.getMessage();
            log.error(errorMsg);
            throw new NotifierMfsException(errorMsg, getNotifierMfsExceptionCause(e));
        } 
        log.debug("getSlamdownCallInfo: " + Arrays.toString(callInfoArray));
        return callInfoArray;
    }

    @Override
    public INotifierNewMessageCallInfo getNewMessageCallInfo(ANotifierNotificationInfo notificationInfo) throws NotifierMfsException {
        NotifierNewMessageCallInfo callInfo = null;
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), notificationInfo.getProperties());
        NotificationEmail email = new NotificationEmail(ntfEvent);
        try {
            email.init();
            callInfo = new NotifierNewMessageCallInfo(email.getSenderPhoneNumber(), email.getMessageReceivedDate());
            callInfo.setIsCallerVisible(email.getSenderVisibile());
            callInfo.setCallerDisplayName(email.getSenderDisplayName());        
            
            Map<String, String> emailProperties = email.getAdditionalProperties();
            Properties additionalProperties = new Properties();
            for (Map.Entry<String, String> entry : emailProperties.entrySet()) {
                log.debug("Adding Property: " + entry.getKey() + " with Value: " + entry.getValue() + " to NotifierNewMessageCallInfo");
                additionalProperties.put(entry.getKey(), entry.getValue());
            }
            callInfo.setAdditionalProperties(additionalProperties);
            
        } catch (MsgStoreException e) {
            log.error("Unable to retrieve new message deposit info due to MsgStoreException: " + e.getMessage());
            throw new NotifierMfsException(e.getMessage());
        }
        log.debug("getNewMessageDepositInfo: " + callInfo);
        return callInfo;
    }
    

    private static NotifierMfsExceptionCause getNotifierMfsExceptionCause(TrafficEventSenderException trafficEventSenderException) {
        NotifierMfsExceptionCause cause = NotifierMfsExceptionCause.NO_CAUSE_SPECIFIED;
        switch(trafficEventSenderException.getTrafficEventSenderExceptionCause()) {
            case PAYLOAD_FILE_DOES_NOT_EXIST:
                cause = NotifierMfsExceptionCause.FILE_DOES_NOT_EXIST;
                break;
            case PAYLOAD_FILE_PATH_NOT_ACCESSIBLE:
            case PAYLOAD_FILE_NOT_ACCESSIBLE:
                cause = NotifierMfsExceptionCause.FILE_NOT_ACCESSIBLE;
                break;
            case PAYLOAD_FILE_PARSING_ERROR:
                cause = NotifierMfsExceptionCause.FILE_PARSING_ERROR;
                break;
        }
        return cause;
    }

}
