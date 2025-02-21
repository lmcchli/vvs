/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.common.sms;


public interface SMSResultHandler {

    public void ok(int id);

    public void retry(int id, String errorText);

    public void failed(int id, String errorText);
    
    public void expired(int id);

    public void waitForPhoneOn(int id);

    public void incrementNumberSmsResultPending(int id);

    public void setSendingCompleted(int id);

    public void oneOk(int id);

    public void oneFailed(int id);

    public void oneRetry(int id);

    public void sendStatusIfAllResultsReceived(int id);
}
