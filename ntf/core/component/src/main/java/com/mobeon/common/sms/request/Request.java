/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.common.sms.request;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms.NotifierSmppPduType;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.sms.SMSUnit;

/**
 *The Request class stores the request that is sent to the smsc. It is responsible to 
 *make an SMSMessage depending on the protocol and type of message.
 *Each type of message must extend this class and implement the getSMSMessage method.
 */
public abstract class Request {

    /**
     * TP-Data Coding Scheme
     * For further info please see:
     * GSM 23.038, V9.1.1, 2010-02-03, Section 4
     */
    public static final short GSM_DCS_GENERAL_DATA_CODING_INDICATION_GSM_7_BIT_DEFAULT_ALPHABET = 0x00; // 0000 0000
    public static final short GSM_DCS_GENERAL_DATA_CODING_INDICATION_8_BIT_DATA = 0x04; // 0000 0100

    /** Protected members */
    protected SMSAddress from; 
    protected SMSAddress to; 
    protected SMSResultHandler resultHandler; 
    protected int validity;
    protected int id;
    protected int delay = 0;
    protected String message;
    protected NotifierSmppPduType notifierSmppPduType = NotifierSmppPduType.SUBMIT_SM;
    protected boolean setDpf = false;
    protected String serviceType = null;

    public Request(SMSAddress from, SMSAddress to, int validity, SMSResultHandler resultHandler, int id) {
        this.from = from;
        this.to = to;
        this.resultHandler = resultHandler;
        this.validity = validity;
        this.id = id;
        this.message = null;
    }

    public SMSAddress getFromAddress() {
        return from;
    }

    public SMSAddress getToAddress() {
        return to;
    }
    
    public SMSResultHandler getResultHandler() {
        return resultHandler;
    }
   
    public int getValidity() {
        return validity;
    }
    
    public int getId() {
        return id;
    }

    public int getDelay() {
        return delay;
    }

    public String getMessage() {
        return message;
    }

    public NotifierSmppPduType getNotifierSmppPduType() {
        return notifierSmppPduType;
    }

    public void setNotifierSmppPduType(NotifierSmppPduType notifierSmppPduType) {
        this.notifierSmppPduType = notifierSmppPduType;
    }

    public boolean getSetDpf() {
        return setDpf;
    }

    public void setSetDpf(boolean setDpf) {
        this.setDpf =  setDpf;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType =  serviceType;
    }
    
    public abstract SMSMessage getSMSMessage(SMSUnit unit);
        
}

