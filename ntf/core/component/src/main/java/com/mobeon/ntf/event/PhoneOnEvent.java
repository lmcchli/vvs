/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.event;

/**
 * SMSDeliveryReceipt carries information about a delivery receipt
 * received from a mobile phone.
 */
@SuppressWarnings("serial")
public class PhoneOnEvent extends java.util.EventObject {

    /**
	 *
	 */
	private String _adr;
    private int _result;
    private String _msg;

    public static final int PHONEON_OK = 0;
    public static final int PHONEON_FAILED = 1;
    public static final int PHONEON_FAILED_TEMPORARY = 2;
    public static final int PHONEON_BUSY = 3;
    public static final int PHONEON_SS7_ERROR = 4;
    /**
     * This PhoneOnEvent object is used by the EventRouter to carry SMS-Unit responses (which SHOULD not).
     * Therefore, the following response types must be added to distinguish client responses (either SMS client or SS7) from PhoneOn responses (SMSc or SS7/HLR).
     */
    public static final int PHONEON_CLIENT_SENT_SUCCESSFULLY = 5;
    public static final int PHONEON_CLIENT_FAILED = 6;
    public static final int PHONEON_CLIENT_FAILED_TEMPORARY = 7;

    public static final String[] responseType = {
        "PhoneOn_RESPONSE_OK",
        "PhoneOn_FAILED",
        "PhoneOn_FAILED_TEMPORARY",
        "PhoneOn_BUSY",
        "PhoneOn_SS7_ERROR",
        "PhoneOn_CLIENT_SENT_SUCCESSFULLY",
        "PhoneOn_CLIENT_FAILED",
        "PhoneOn_CLIENT_FAILED_TEMPORARY"
    };

    /**
     * Constructor.
     */
    public PhoneOnEvent(Object source, String adr, int result, String msg) {
        super(source);
        _adr = adr;
        _result = result;
        _msg = msg;
    }

    /**
     * Gets the result of the phone on check.
     *@return true if the phone is known to have been reachable recently, false
     * if it is known to be off or could not be determined.
     */
    public boolean isOk() {
        return (_result == PHONEON_OK);
    }

    /**
     * Gets the result of the phone on check.
     *@return true if the phone is busy
     */
    public boolean isBusy() {
        return (_result == PHONEON_BUSY);
    }

    public boolean isSs7Error() {
    	return (_result == PHONEON_SS7_ERROR);
    }
    /**
     * Gets the result of the phone on check.
     * @return int result
     */
    public int getResult() {
        return _result;
    }

    /**
     * Gets the phone number this event is about.
     *@return the phone number.
     */
    public String getAddress() {
        return _adr;
    }

    /**
     * Gets a message explaining details of the result.
     *@return result message.
     */
    public String getMessage() {
        return _msg;
    }

    /**
     * Creates a printable version of the message information
     * @return A string,
     */
    public String toString() {
        return "{PhoneOnEvent: " + _adr + " " + responseType[_result] + " " + _msg + "}";
    }
    
    // DO NOT remove the bellow methods. They are used to generate a unique key for
    // the corresponding phoneon queue. 
    // Definition: two phoneon events are the same if they have the same hashcode
    // and the _addr, _msg && _result code are the exact same values
    @Override
    public boolean equals(Object obj) {
        boolean status = false;
        if(obj instanceof PhoneOnEvent) {
            PhoneOnEvent poe = (PhoneOnEvent) obj;
            
            status = (this._adr == poe.getAddress()) && (this._msg == poe.getMessage()) && (this._result == poe.getResult());
        }
        
        return status;
    }
    
    @Override
    public int hashCode() {       
        int hash = 5;
        hash = hash + (this._adr != null ? this._adr.hashCode() : 0);
        hash = hash + (this._result ^ (this._result >>> 32));
        hash = hash + (this._msg != null ? this._msg.hashCode() : 0);
      
        return hash;
    }
}

