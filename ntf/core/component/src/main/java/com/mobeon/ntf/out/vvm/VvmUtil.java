package com.mobeon.ntf.out.vvm;

import java.io.File;
import java.io.FileFilter;
import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;


public class VvmUtil {

    private LogAgent log;
    private MfsEventManager mfsEventManager;
    
    protected VvmUtil(MfsEventManager mfsEventManager) {
        log = NtfCmnLogger.getLogAgent(VvmUtil.class);
        this.mfsEventManager = mfsEventManager;
    }
    
    protected Properties getEventProperties(String eventIdString) {
        Properties properties = null;
        try {
            EventID eventId = new EventID(eventIdString);
            properties = eventId.getEventProperties();
            
            if(log.isDebugEnabled()) {
                log.debug("Properties extracted from " + eventIdString + ": " + properties);
            }
                
        } catch (InvalidEventIDException e) {
            log.error("InvalidEventIDException in getEventProperties: " + e.getMessage(), e);
        }
        return properties;
    }

    protected File[] getPendingNotifications(String notificationNumber) {

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {

                return file.getName().startsWith(VvmEvent.VVM_DEPOSIT_STATUS_FILE)||
                       file.getName().startsWith(VvmEvent.VVM_EXPIRY_STATUS_FILE) ||
                       file.getName().startsWith(VvmEvent.VVM_LOGOUT_STATUS_FILE) ||
                       file.getName().startsWith(VvmEvent.VVM_GREETING_STATUS_FILE);
            }
        };

        return mfsEventManager.getEventFiles(notificationNumber, filter);
    }
    
    protected File[] getPendingSimSwapNotifications(String notificationNumber) {

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                // Does not include VVM_GREETING_STATUS_FILE
                return file.getName().startsWith(VvmEvent.VVM_DEPOSIT_STATUS_FILE) /*||
                       file.getName().startsWith(VvmEvent.VVM_EXPIRY_STATUS_FILE) ||
                       file.getName().startsWith(VvmEvent.VVM_LOGOUT_STATUS_FILE) */;
            }
        };

        return mfsEventManager.getEventFiles(notificationNumber, filter);
    }

}
