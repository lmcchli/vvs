/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2013.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.ntf.mail;

import java.util.Date;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.Constants.depositType;
import com.mobeon.ntf.util.NtfUtil;


/**
 * 
 *
 * @author ejulgri
 */
public abstract class AMessageDepositInfo implements IMessageDepositInfo, Constants {

    /**
     * UID field for VVM
     * @TODO remove this UID definition when MFS one is available
     */
    private static final String UID = "uid";
    /*********************************************************************/

    protected String from = null;
    protected String senderPhoneNumber = null;
    protected String uid = null;
    protected String receiver = null;
    protected String receiverPhoneNumber = null;
    protected String subject = null;
    protected boolean senderVisible = true;
    protected Date receivedDate = null;
    protected boolean urgent = false;
    protected boolean confidential = false;
    protected depositType depType = depositType.VOICE;

    protected static LogAgent log;

    public enum ProcessContentType { 
        SMS,CC,BCC,REPLYTO,FROM,SUBJECT,ENVELOPEFROM
    }

    protected ProcessContentType processContentType=ProcessContentType.SMS;

    public void setProcessContentType (ProcessContentType pct ) {
        processContentType = pct;
    }

    public ProcessContentType getProcessContentType () {
        return processContentType;
    }
    
    public boolean isNewMessageNotification() {
        switch (getDepositType()) {
            case FAX:
            case VOICE:
            case VIDEO:
            case EMAIL:
                log.debug("isNewMessageNotification for getEmailType " + getDepositType() + " is true");
                return true;

            default:
                log.debug("isNewMessageNotification " + getDepositType() + " is false");
                return false;
        }
    }

    public int getEmailType() {
        return depType.value();
    }
    
    public depositType getDepositType() {
        return depType;
    }

    public String getReceiverPhoneNumber() {
        return receiverPhoneNumber;
    }
    
    public String getSenderPhoneNumber() {
        log.debug("getSenderPhoneNumber=\"" + senderPhoneNumber + "\"");

        return senderPhoneNumber;
    }

    public String getSender() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public boolean isUrgent() {
        return urgent;
    }

    /**
     * Tells if this email is confidential or not.
     * @return true if the email is confidential, false otherwise
     */
    public boolean isConfidential() {
        return confidential;
    }



    /**
     * Set the UID field for VVM
     */
    protected void setUid(StateFile mfsStateFile) {
        uid = mfsStateFile.getAttribute(UID);
    }

    protected void setFrom(StateFile mfsStateFile) {
        from = mfsStateFile.getC1Attribute(Container1.From);
    }
    
    protected void setReceiverPhoneNumber() {
        if(receiver!=null) {
            receiverPhoneNumber = CommonMessagingAccess.getInstance().denormalizeNumber(receiver);
            log.debug("receiverPhoneNumber=\"" + receiverPhoneNumber + "\"");
        }
    }

    protected void setSubject(StateFile mfsStateFile) {
        subject = mfsStateFile.getC1Attribute(Container1.Subject);
    }

    protected void setSenderVisible(StateFile mfsStateFile) {
        String senderVisibility = mfsStateFile.getC1Attribute(Container1.Sender_visibility);
        if (senderVisibility != null && !senderVisibility.isEmpty()) {
            if ("0".equals(senderVisibility)) {
                senderVisible = false;
            }
        }
    }

    protected void setReceivedDate(StateFile mfsStateFile) {
        String date = mfsStateFile.getC1Attribute(Container1.Date_time);
        if (date != null) {
            receivedDate = NtfUtil.stringToDate(date);
        } else {
            receivedDate = new Date();
        }
    }

    protected void setUrgent(StateFile stateFile) {
        String priority = stateFile.getC1Attribute(Container1.Priority);
        if (priority != null && priority.equals(String.valueOf(MoipMessageEntities.MFS_URGENT_PRIORITY))) {
            urgent = true;
        }
    }

    protected void setConfidential(StateFile stateFile) {

        String confidentialValue = stateFile.getAttribute(MoipMessageEntities.CONFIDENTIALITY_HEADER);

        if (confidentialValue == null) {
            // If not in C2, try search in C1 for backward compatibility
            if (log.isDebugEnabled()) log.debug("confidentiality is not in C2. Try search in C1.");
            confidentialValue = stateFile.getC1Attribute(Container1.Privacy);
            if (confidentialValue == null){
                // Shouldn't happen. Something wrong. Log it.
                if (log.isInfoEnabled()) log.info("Getting confidentiality failed. Neither in C1 nor in C2. Something wrong in state file.");
                confidential = false;
            } else {
                if (log.isDebugEnabled()) log.debug("confidentiality is in C1.");
            }
        }

        if (confidentialValue != null) {
            if (confidentialValue.equalsIgnoreCase(MoipMessageEntities.MFS_PRIVATE)) {
                confidential = true;
            } else {
                confidential = false;
            }
        }
    }

    protected void setDefaultEmailType(StateFile mfsStateFile) {
        String serviceClass = mfsStateFile.getDestMsgClass();
        depType = depositType.VOICE; // default type

        if (serviceClass != null) {
            if (serviceClass.equalsIgnoreCase(ServiceName.VOICE)) {
                depType = depositType.VOICE;
            } else if (serviceClass.equalsIgnoreCase(ServiceName.VIDEO)) {
                depType = depositType.VIDEO;
            } else if (serviceClass.equalsIgnoreCase(ServiceName.FAX)) {
                depType = depositType.FAX;
            }

            // Add more checks for other types (fax,... etc) when needed
        }
    }

    protected void setSenderPhoneNumber() {
        if(from!=null) {
            if(senderVisible) {
                senderPhoneNumber = CommonMessagingAccess.getInstance().denormalizeNumber(from);
            } else {
                senderPhoneNumber = from;
            }
            log.debug("setSenderPhoneNumber=\"" + senderPhoneNumber + "\"");
        }
    }
    
    /**
     * Tells if this email is a forced MWI off message for a deleted or modified subscriber.
     * @return true if this email is a MWI off message.
     */
    public boolean isMwiOffUnsubscribed() {
        return false;
    }
}
