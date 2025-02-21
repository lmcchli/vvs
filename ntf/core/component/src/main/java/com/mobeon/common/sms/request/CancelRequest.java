package com.mobeon.common.sms.request;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.sms.SMSUnit;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.out.sms.ServiceTypePidLookupImpl;

/**
 *The Request class stores the request that is sent to the smsc. It is responsible to
 *make an SMSMessage depending on the protocol and type of message.
 *Each type of message must extend this class and implement the getSMSMessage method.
 *
 *
 */
public class CancelRequest extends Request {
    private int replacePosition;

    public CancelRequest(SMSAddress from, SMSAddress to, String serviceType) {
        super(from, to, 0, null, 0);
        this.replacePosition =-1;
        this.serviceType=serviceType;
    }

    public CancelRequest(SMSAddress from, SMSAddress to, int replacePosition) {
        super(from, to, 0, null, 0);
        this.replacePosition = replacePosition;
    }

    public String getServiceType() {
        if (serviceType != null && !serviceType.isEmpty() ) {
            return serviceType;
        } 
        
        if (replacePosition >= 0 && replacePosition <= 99) {
            return(ServiceTypePidLookupImpl.get().getServiceType(replacePosition));
        } else {
            return null;
        }
    }


    public SMSMessage getSMSMessage(SMSUnit unit) {
        return null;
    }
}
