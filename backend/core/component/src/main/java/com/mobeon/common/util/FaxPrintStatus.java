package com.mobeon.common.util;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;


/**
 * Provides an enumeration for the "moip.fax-print-status" header
 * in the statefile.
 * 
 *
 * @author lmchemc
 */
public enum FaxPrintStatus {

    neverprinted,
    printing,
    done;
    
    public static final String STATEFILE_PROPERTY_NAME = "moip.fax-print-status";
    
    private static final CommonMessagingAccess MFS_ACCESS = CommonMessagingAccess.getInstance();
    private static final LogAgent LOGGER = CommonOamManager.getInstance().getLogger();

    /**
     * Sets the fax print status for the given fax message (in the state file).
     *
     * @param stateFile  the state file from the fax message to be updated
     * @param status  <code>FaxPrintStatus.printing</code> if fax status to be changed to printing,
     *          <code>FaxPrintStatus.done</code> if fax status to be changed to done
     * @return  <code>true</code> if the state file on the file system was updated successfully,
     *          <code>false</code> otherwise
     */
    public static boolean changeStatus(StateFile stateFile, FaxPrintStatus status) {
        if(stateFile == null) {
            throw new NullPointerException("stateFile cannot be null");
        }

        boolean success = true;

        try {
            stateFile.setAttribute(FaxPrintStatus.STATEFILE_PROPERTY_NAME, status.name());
            MFS_ACCESS.updateState(stateFile);
            success = true;
        } catch (MsgStoreException e) {
            LOGGER.error("Unable to update state file! - " + e.getMessage(), e);
            success = false;
        }

        return success;
    }
    
    /**
     * Returns the fax print status for the given fax message (from the state file).
     * 
     * @return  the fax print status. 
     *          If the fax message has never been printed then <code>FaxPrintStatus.neverprinted</code> is returned.
     */
    public static FaxPrintStatus getStatus(StateFile stateFile) {
        if (stateFile == null) {
            throw new NullPointerException("stateFile cannot be null");
        }
        
        String status = stateFile.getAttribute(FaxPrintStatus.STATEFILE_PROPERTY_NAME);
        
        if (status == null || status.isEmpty()) {
            return FaxPrintStatus.neverprinted;
        } else {
            return FaxPrintStatus.valueOf(status);
        }
    } 
}