package com.mobeon.common.sms.request;

import com.mobeon.ntf.util.Logger;

/**
 * A helper class which will contain either a byte
 * array or a text message, to use in request objects.
 */
public class SMSMessagePayload {
    
    /** The byte array representing the payload. If this is null, then this object contains a String message. */
    public final byte[] payload;
    
    /** The String representing the message. If this is null, then this object contains a byte array payload. */
    public final String message;
    
    public SMSMessagePayload(byte[] payload)
    {
        this(null, payload);
    }
    
    public SMSMessagePayload(String message)
    {
        this(message, null);
    }
    
    public SMSMessagePayload(String message, byte[] payload)
    {
        this.payload = payload;
        this.message = message;
        if (payload != null && message != null)
            Logger.getLogger().logMessage("WARN: SMSMessagePayload contains both a byte array and a string. This should not happen.", Logger.L_ERROR);
    }

}
