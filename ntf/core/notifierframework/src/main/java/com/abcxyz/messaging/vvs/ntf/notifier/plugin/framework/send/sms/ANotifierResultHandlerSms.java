/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms;


/**
 * The ANotifierResultHandlerSms abstract class defines the methods that the NTF component can invoke to
 * allow the Notifier plug-in to handle the result of sending a SMS notification.
 */
public abstract class ANotifierResultHandlerSms {

    /**
     * Handles the successful sending of the notification.
     */
    public void ok() {
        return;
    }

    /**
     * Handles the failed but retry-able sending of the notification.
     * <p>
     * The sending is deemed retry-able based on the error code received from the SMSC and 
     * the action mapped to this error code in the NTF configuration.
     * @param errorText the error text containing details of the failure
     */
    public void retry(String errorText) {
        return;
    }

    /**
     * Handles the failed sending of the notification.
     * <p>
     * The sending is deemed not retry-able based on the error code received from the SMSC and 
     * the action mapped to this error code in the NTF configuration.
     * @param errorText the error text containing details of the failure
     */
    public void failed(String errorText) {
        return;
    }

    /**
     * Handles the expiration of the notification.
     * <p>
     * Currently, this is called only when a new message deposit notification is to be sent but
     * there is no longer any new messages in the subscriber's mailbox.
     */
    public void expired() {
        return;
    }

    /**
     * Handles the case when the Notifier plug-in must wait for the phone to which the notification should be sent is turned on.
     * <p>
     * The need to wait for the phone to be turn on is based on the error code received from the SMSC and 
     * the action mapped to this error code in the NTF configuration.
     */
    public void waitForPhoneOn() {
        return;
    }
    
    /**
     * Processes the results of sending the notification when the notification message was too long to send in one SMPP PDU.
     * <p>
     * The NTF component invokes this method after the responses for all the SMPP PDU requests sent for this notification is received.
     * @param numOk the number of successful responses
     * @param numRetry the number of retry-able error responses
     * @param numFailed the number of non-retry-able error responses
     */
    public void processResults(int numOk, int numRetry, int numFailed) {              
        if(numFailed != 0) {
            failed("ANotifierResultHandlerSms.processResults: failed to send");
        }
        else if (numRetry != 0) {
            retry("ANotifierResultHandlerSms.processResults: need to retry send");
        }
        else {
            ok();
        }
    }
}
