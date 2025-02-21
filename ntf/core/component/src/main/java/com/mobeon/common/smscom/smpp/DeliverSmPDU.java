/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.common.smscom.smpp;

import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPParamHandler;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPSMSInfo;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPSMSInfoFacade;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.util.Logger;

import java.util.*;

/**
 * DeliverSmPDU implements an SMPP DeliverSm PDU for receiving from the SMSc.
 */
public class DeliverSmPDU extends SMPP_PDU {
    private String messageId = null;
    private String serviceType;
    private SMSAddress sourceAddress;
    private SMSAddress destAddress;
    private int esmClass;
    private int priorityFlag;
    private int registeredDelivery;
    private int dataCoding;
    private int smLength;
    private String shortMessage;
    private MessageStateTypes messageState = MessageStateTypes.MESSAGE_STATE_NOT_PROVIDED;

    /**
     * Constructor.
     * @param conn the connection that uses this PDU instance.
     */
    DeliverSmPDU(SMPPCom conn) {
        super(conn, "deliver_sm_pdu");
        commandId = SMPPCMD_DELIVER_SM;
    }

    /**
     * Read reads a submit sm response PDU from the supplied input stream and parses all parameters.
     */
    public void parseBody() {
        int dummyInt;
        String dummyString;

        pos= 4*4;
        serviceType = getNTS();
        sourceAddress = new SMSAddress(getInt(1), getInt(1), getNTS());
        destAddress = new SMSAddress(getInt(1), getInt(1), getNTS());
        
        // Change the destination address based on formatting rules
        String sourceNumber = sourceAddress.getNumber();
        SMPPSMSInfo smsInfo = new SMPPSMSInfoFacade(sourceAddress, destAddress, null);
        String newNumber = SMPPParamHandler.get().getSMPPParamValuesInstance().getDestinationAddrInbound(sourceNumber, smsInfo);
        if (!newNumber.equals(sourceNumber)) {
            Logger.getLogger().logMessage("Replacing " + sourceNumber + " with " + newNumber + " in DeliverSmPDU", Logger.L_DEBUG);
            sourceAddress.setNumber(newNumber);
        }
        
        esmClass = getInt(1);
        dummyInt = getInt(1); //Dont care about protocol id
        priorityFlag = getInt(1);
        dummyString = getNTS(); //No schedule delivery time
        dummyString = getNTS(); //No validity period
        registeredDelivery = getInt(1);;
        dummyInt = getInt(1); //No replace if present flag
        dataCoding = getInt(1);
        dummyInt = getInt(1); //No sm default msg id
        smLength = getInt(1);
        shortMessage = getString(smLength);

        /** Parse the optional parameters to check if the message state is present */
        getOptionalParameters();
        if (optionalParameters != null) {
            Integer incomingMessageTypeValue = (Integer)optionalParameters.get(new Integer(SMPPTAG_MESSAGE_STATE));

            if (incomingMessageTypeValue != null) {
                messageState = MessageStateTypes.MESSAGE_STATE_INVALID_VALUE;
                Iterator<MessageStateTypes> it = Arrays.asList(MessageStateTypes.values()).iterator();
                while (it.hasNext()) {
                    MessageStateTypes currentMessageStateType = it.next();
                    if (currentMessageStateType.getValue() == incomingMessageTypeValue.intValue()) {
                        messageState = currentMessageStateType;
                        break;
                    }
                }
            }
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public SMSAddress getSourceAddress() {
        return sourceAddress;
    }

    public SMSAddress getDestAddress() {
        return destAddress;
    }

    public int getEsmClass() {
        return esmClass;
    }

    public int getPriorityFlag() {
        return priorityFlag;
    }

    public int getRegisteredDelivery() {
        return registeredDelivery;
    }

    public int getDataCoding() {
        return dataCoding;
    }

    public int getSmLength() {
        return smLength;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public MessageStateTypes getMessageState() {
        return messageState;
    }
}

