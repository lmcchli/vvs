/**
 * Copyright (c) 2003, 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.sms.request;


import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.sms.SMSUnit;


/**
 *The Request class stores the request that is sent to the smsc. It is responsible to 
 *make an SMSMessage depending on the protocol and type of message.
 *Each type of message must extend this class and implement the getSMSMessage method.
 *
 * 
 */
public class SMSFlashRequest extends Request { 

    /**
     * Data Coding Scheme octet is set to Message waiting indication (Store Message)
     * to indicate message waiting to MS.
     * For further info please see:
     * GSM03.38, V7.2.0, 1999-07, clause 4
     */
    public static final short GSM_DCS_UCS2 = 0x08;
    // Group //Type //decimal

    public static final short GSM_DCS_CLASS0_DEFAULT = 0x10; // 0001  0000    16
    public static final short GSM_DCS_CLASS0_UCS2 = 0x18; // 0001  1000    24
    public static final short GSM_DCS_CLASS0_ALTERNATIVE = 0xF0; // 1111 0000 240 
    
    private String message;
    
    public SMSFlashRequest(SMSAddress from, SMSAddress to, int validity, SMSResultHandler resultHandler, int id, String message) { 
        super(from, to, validity, resultHandler, id);  
        this.message = message;
    }
       
    public SMSMessage getSMSMessage(SMSUnit unit) {
        SMSMessage msg = unit.getConverter().unicodeToMessage(message,
                unit.getConfig().getSmsStringLength()
                * unit.getConfig().getNumberOfSms());
     
        msg.setExpiryTimeRelative(validity);
        msg.setFragmentSize(unit.getConfig().getSmsStringLength());
        msg.setPriority(unit.getConfig().getSmsPriority());
        
        msg.setReplace(false);
        msg.setServiceType(unit.getConfig().getSmeServiceType()); 
        msg.setPID(0);
        
        if (unit.getConfig().isBearingNetworkGSM()) {
            if( unit.getConfig().isAlternativeFlashDcs() ) {
                msg.setDCS(GSM_DCS_CLASS0_ALTERNATIVE);
            } else {
                if (msg.getDCS() == GSM_DCS_UCS2) {
                    msg.setDCS(GSM_DCS_CLASS0_UCS2);
                } else {
                    msg.setDCS(GSM_DCS_CLASS0_DEFAULT);
                }
            }
        
        }
            
        return msg;
    }
        
}

