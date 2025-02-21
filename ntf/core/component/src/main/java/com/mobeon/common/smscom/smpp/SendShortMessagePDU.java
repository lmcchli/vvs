package com.mobeon.common.smscom.smpp;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;

public abstract class SendShortMessagePDU extends SMPP_PDU {

    SendShortMessagePDU(SMPPCom conn) {
        super(conn);
    }

    SendShortMessagePDU(SMPPCom conn, String eventName) {
        super(conn, eventName);
    }
    
    public abstract int getExpectedPduResponseCommandId();

    public abstract byte[] getBuffer(SMSAddress to, SMSAddress from, SMSMessage msg);
}
