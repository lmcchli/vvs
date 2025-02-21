/**
 * Copyright (c) 2003, 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.sms.request;


import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.sms.SMSUnit;

public class PhoneOnRequest extends Request {
    private String message;
    
    public PhoneOnRequest(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, String message) {
        super(from, to, validity, rh, id);
        this.message = message;
    }
    
    public SMSMessage getSMSMessage(SMSUnit unit) {
        SMSMessage msg = unit.getConverter().unicodeToMessage(message,
                unit.getConfig().getSmsStringLength()
                * unit.getConfig().getNumberOfSms());
     
        msg.setExpiryTimeRelative(validity);
        msg.setDeliveryReceipt(true);
        msg.setPriority(unit.getConfig().getSmsPriority());
        msg.setPID(0x40); // SMS Type 0
        msg.setUseReplyPath(unit.getConfig().isReplyPath()); 
        return msg;
        
    }
        
}

