/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.util.Arrays;
import java.util.Iterator;

import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPParamHandler;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPSMSInfo;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPSMSInfoFacade;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.util.Logger;

/**
 * AlertNotificationSmPDU implements an SMPP AlertNotificationSm PDU for receiving from the SMSc.
 */

public class AlertNotificationSmPDU extends SMPP_PDU {

    private SMSAddress sourceAddress;
    private SMSAddress destAddress; // esmeAddress
    private MSAvailabilityStatusEventTypes msAvailabilityStatus;// = MSAvailabilityStatus.MS_STATUS_AVAILABLE;
    
    final int MS_AVAILABILITY_STATUS_AVAILABLE = 0;
    final int MS_AVAILABILITY_STATUS_DENIED = 1; // suspended, no sms capability
    final int MS_AVAILABILITY_STATUS_UNAVAILABLE = 2;

    AlertNotificationSmPDU(SMPPCom conn) {
        super(conn, "alert_notification_pdu");
        commandId = SMPPCMD_ALERT_NOTIFICATION;
    }

    public void parseBody() {
        pos = 4 * 4; 
        sourceAddress = new SMSAddress(getInt(1), getInt(1), getNTS());
        destAddress = new SMSAddress(getInt(1), getInt(1), getNTS()); // esmeAddress\
        
        // Change the destination address based on formatting rules
        String sourceNumber = sourceAddress.getNumber();
        SMPPSMSInfo smsInfo = new SMPPSMSInfoFacade(sourceAddress, destAddress, null);
        String newNumber = SMPPParamHandler.get().getSMPPParamValuesInstance().getDestinationAddrInbound(sourceNumber, smsInfo);
        if (!newNumber.equals(sourceNumber)) {
            Logger.getLogger().logMessage("Replacing " + sourceNumber + " with " + newNumber + " in AlertNotificationSmPDU", Logger.L_DEBUG);
            sourceAddress.setNumber(newNumber);
        }

        /** Parse the optional parameters to check if the ms_availability_status parameter is present */
        getOptionalParameters();
        if (optionalParameters != null) {
            Integer msAvailabilityStatusValue = (Integer) optionalParameters.get(new Integer(SMPPTAG_MS_AVAILABILITY_STATUS));

            if (msAvailabilityStatusValue != null) {
                Iterator<MSAvailabilityStatusEventTypes> it = Arrays.asList(MSAvailabilityStatusEventTypes.values()).iterator();
                while (it.hasNext()) {
                    MSAvailabilityStatusEventTypes currentMSAvailabilityStatus = it.next();
                    if (currentMSAvailabilityStatus.getMsAvailabilityStatusValue() == msAvailabilityStatusValue.intValue()) {
                        msAvailabilityStatus = currentMSAvailabilityStatus;
                        break;
                    }
                }
            } else { // the parameter has been included in the pdu but the value for it has not been set in the pdu
                msAvailabilityStatus = MSAvailabilityStatusEventTypes.MS_STATUS_AVAILABLE;
            }

        } else {
            /*
             * the optional parameter 'ms_availability_status' has not been included in the 'Alert_Notification' PDU. Hence gets the
             * default value = 0 i.e. Availale
             */
            msAvailabilityStatus = MSAvailabilityStatusEventTypes.MS_STATUS_AVAILABLE;
        }
    }

    public SMSAddress getSourceAddress() {
        return sourceAddress;
    }

    public SMSAddress getDestAddress() {
        return destAddress;
    }

    public MSAvailabilityStatusEventTypes getMsAvailabilityStatus() {
        return msAvailabilityStatus;
    }

}// class
