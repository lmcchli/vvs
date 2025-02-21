/**
 * Copyright (c) 2003, 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.sms.request;


import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.sms.SMSUnit;

public class PollRequest extends Request {
   
    public PollRequest() {
        super(null, null, 0, null, 0);
    }
    
    public SMSMessage getSMSMessage(SMSUnit unit) {
        return null;
    }       
}

