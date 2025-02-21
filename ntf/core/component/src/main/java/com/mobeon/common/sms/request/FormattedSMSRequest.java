/**
 * Copyright (c) 2003, 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.sms.request;


import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.sms.SMSInfoResultHandler;
import com.mobeon.common.sms.SMSUnit;
import com.mobeon.common.smscom.charset.ConvertedInfo;
import com.mobeon.ntf.slamdown.SlamdownPayload;


/**
 *The Request class stores the request that is sent to the smsc. It is responsible to 
 *make an SMSMessage depending on the protocol and type of message.
 *Each type of message must extend this class and implement the getSMSMessage method.
 *
 * 
 */
public class FormattedSMSRequest extends Request { 
    private String[] lines;
    private int maxLinesPerSms;
    private String[] callers;
    private SlamdownPayload payload;
    
    public FormattedSMSRequest(SMSAddress from, SMSAddress to, int validity, SMSInfoResultHandler rh, 
                    int id, int maxLinesPerSms, String[] callers, SlamdownPayload payload) { 
        super(from, to, validity, rh, id);   
        this.lines = payload.getStringBody();
        this.maxLinesPerSms = maxLinesPerSms;
        this.callers = callers;
        this.payload = payload;
    }
       
    public SMSMessage getSMSMessage(SMSUnit unit) {
        return null;
    }
    
    public String[] getLines() {
        return lines;
    }
    
    public SMSAddress getSourceAddress(int msgPos) {
        if( from.getNumber().equals("callers_number") ) {
            if( callers != null && callers.length > msgPos && callers[msgPos] != null ) {
                return new SMSAddress(from.getTON(), from.getNPI(), callers[msgPos]);
            } else {
                
            }
        }
        return from;
    }

    private void setParameters(SMSMessage msg, SMSUnit unit) {
        
        msg.setExpiryTimeRelative(validity);
        msg.setFragmentSize(unit.getConfig().getSmsStringLength());
        msg.setPriority(unit.getConfig().getSmsPriority());
        
        msg.setReplace(false);
        msg.setServiceType(unit.getConfig().getSmeServiceType());
        msg.setPID(0);
       
        msg.setNotifierSmppPduType(this.getNotifierSmppPduType());
        msg.setSetDpf(this.getSetDpf());
        if (this.getServiceType() != null) {
            msg.setServiceType(this.getServiceType());
        }

    }
       
    public ConvertedInfo getConvertedInfo(SMSUnit unit) {
        ConvertedInfo convertedMessages = unit.getConverter().unicodeToMessages(
                    unit.getConfig().getSmsStringLength(),
                    maxLinesPerSms, payload);
        
        SMSMessage[] msgs = convertedMessages.getMessages();
        
        for (int i = 0; i < msgs.length; i++) {
            SMSMessage msg = msgs[i];

            setParameters(msg, unit);
        }
        return convertedMessages;
    }
   
}

