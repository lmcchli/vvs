/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.common.sms.request;

import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.sms.SMSUnit;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.ntf.NotificationConfigConstants;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup;
import com.mobeon.ntf.out.sms.ServiceTypePidLookupImpl;

/**
 *The Request class stores the request that is sent to the smsc. It is responsible to 
 *make an SMSMessage depending on the protocol and type of message.
 *Each type of message must extend this class and implement the getSMSMessage method.
 */
public class SMSRequest extends Request { 
    private String message;
    private int replacePosition;
    
    private final byte[] byteMessage;
   
    private static IntfServiceTypePidLookup serviceTypeLookup = ServiceTypePidLookupImpl.get();
    
    public SMSRequest(SMSAddress from, SMSAddress to, int validity, SMSResultHandler resultHandler, int id, SMSMessagePayload payload, int replacePosition, int delay) {
        super(from, to, validity, resultHandler, id);  
        this.message = payload.message;
        this.byteMessage = payload.payload;
        this.replacePosition = replacePosition;
        this.delay = delay;
    }
    
    public SMSRequest(SMSAddress from, SMSAddress to, int validity, SMSResultHandler resultHandler, int id, String message, byte[] payload, int replacePosition, int delay) {
        this(from, to, validity, resultHandler, id, new SMSMessagePayload(message, payload), replacePosition, delay);
    }
        
    public SMSMessage getSMSMessage(SMSUnit unit) {
        SMSMessage msg;
        if (message != null)
        {
            msg = unit.getConverter().unicodeToMessage(message,
                unit.getConfig().getSmsStringLength()
                * unit.getConfig().getNumberOfSms());
        }
        else
        {
            msg = new SMSMessage(byteMessage, 0);
            msg.setConverter(unit.getConverter());
            msg.setPacked(unit.getConverter().getPack());
        }
     
        msg.setExpiryTimeRelative(validity);
        msg.setFragmentSize(unit.getConfig().getSmsStringLength());
        msg.setPriority(unit.getConfig().getSmsPriority());
        
        if( delay >= 30 ) {
            msg.setScheduledDeliveryTime(delay);
        }

        if (msg.isLargerThanOneFragment()) {
            msg.setReplace(false);
            msg.setServiceType(unit.getConfig().getSmeServiceType());
            msg.setPID(0);
        } else {
            msg.setPID(serviceTypeLookup.getPid(replacePosition));
            String serviceType = serviceTypeLookup.getServiceType(replacePosition);
            if (serviceType != null) {
                msg.setReplace(true);
                msg.setServiceType(serviceType);
            } else {
                //set to default but no replace.
                msg.setServiceType(unit.getConfig().getSmeServiceType());
            }
        }

        msg.setNotifierSmppPduType(this.getNotifierSmppPduType());
        msg.setSetDpf(this.getSetDpf());
        
        /*
         * If service type has not been set or is not set to use SME_SERVICE_TYPE from NTF configuration.
         * then override NTF's value.
         * 
         * This is used by notifier plug-ins to use NTF's default replace table or 
         * allow plug-in or other notification types to override the replace table.
         * 
         * An empty string is a legal value, so instead if the service type is set to the SME_SERVICE_TYPE
         * means use default NTF behaviour.
         */
        if (this.serviceType != null && !this.serviceType.equals(NotificationConfigConstants.SME_SERVICE_TYPE) ) {
            msg.setServiceType(this.serviceType);
        }

        return msg;
    }
        
    public String getSMSRequestMessage() {
    	return message;
    }
    
    /**
     * Returns whether or not this SMSRequest has a message in the form of bytes.
     */
    public boolean isBytes()
    {
        return (byteMessage != null);
    }
    
    /**
     * Returns the array of bytes this SMSRequest contains.
     */
    public byte[] getBytes()
    {
        return byteMessage;
    }
}

