/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.common.sms;


public interface SMSInfoResultHandler extends SMSResultHandler {   
 
    public void partlyFailed(int id, boolean result[], int okCount);
    
    public void allOk(int id, int okCount);

    public void setNumberSmsToSend(int id, int numberOfResults);    
} 
