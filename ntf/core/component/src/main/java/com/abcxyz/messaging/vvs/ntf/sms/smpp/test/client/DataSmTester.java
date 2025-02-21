package com.abcxyz.messaging.vvs.ntf.sms.smpp.test.client;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.generic.NotifierEvent;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.text.TemplateMessageGenerationException;

public class DataSmTester implements NotificationConfigConstants {
    private NtfEventSimulator ntfEventSimulator = null;
    private SMSOut smsOut = null;

    DataSmTester() {
        this.ntfEventSimulator = new NtfEventSimulator();
        this.smsOut = SMSOut.get();
    }

    /**
     * Run all of the data_sm tests.
     */
    void runTests() {
        sendDataSmForMessageNotification();
        sendDataSmForTextMail();
        sendDataSmForIncomingCallNotification();
    }

    /**
     * Sends a data_sm request for text mail. The service type is set to cmt.
     */
    void sendDataSmForTextMail() {
        sendToHandleSMS(this.ntfEventSimulator,this.ntfEventSimulator.getTextMailNotifierEvent());
        SMPPTestClientLogger
                .writeLogMessageToFile("ShortMessageTester.sendDataSmForMessageNotification(): Sending data_sm request for text mail. The service type should be set to cmt.\n");
    }

    /**
     * Sends a data_sm request for an incoming call notification. The service type is set to sdm.
     */
    void sendDataSmForIncomingCallNotification() {
        sendToHandleSMS(this.ntfEventSimulator,this.ntfEventSimulator.getIncomingCallNotifcationNotifierEvent());
        SMPPTestClientLogger
                .writeLogMessageToFile("ShortMessageTester.sendDataSmForMessageNotification(): Sending data_sm request for incoming call notification. The service type should be set to sdm.\n");
    }

    /**
     * Sends a data_sm request for a new message notification. The service type is set to nvn.
     */
    void sendDataSmForMessageNotification() {
        sendToHandleSMS(this.ntfEventSimulator,this.ntfEventSimulator.getNewMessageDepositNotifierEvent());
        SMPPTestClientLogger
                .writeLogMessageToFile("ShortMessageTester.sendDataSmForMessageNotification(): Sending data_sm request for new message notification. The service type should be set to nvn.\n");
    }

    /**
     * Sends a data_sm request for a read message notification. The service type is set to rnm.
     */
    void sendDataSmForReadMessageNotification(){
        sendToHandleSMS(this.ntfEventSimulator,this.ntfEventSimulator.getReadMessageNotificationNotifierEvent());
        SMPPTestClientLogger
                .writeLogMessageToFile("ShortMessageTester.sendDataSmForMessageNotification(): Sending data_sm request for new message notification. The service type should be set to rnm.\n");
    }
    
    /**
     * Sends a submit_sm request for an fmc notification. The service type is sdm.
     */
    void sendSubmitSmForFMC(){
        sendToHandleSMS(this.ntfEventSimulator,this.ntfEventSimulator.getFMCNotiferEvent());
        SMPPTestClientLogger
                .writeLogMessageToFile("ShortMessageTester.sendDataSmForMessageNotification(): Sending data_sm request for new message notification. The service type should be set to sdm.\n");
    }
    
    /**
     * Calls the handleSMS method in SMSOut to send out the data_sm request.
     * @param nes Contains all the necessary information that is required when calling the handleSMS method in SMSOut.
     */
    void sendToHandleSMS(NtfEventSimulator nes,NotifierEvent notifierEvent) {
        //TODO Need to fix due to split of notifier into a framework and plug-in 
//        try {
//            this.smsOut.handleSMS(nes.getUserInfo(), nes.getSmsFilterInfo(), null, nes.getNotificationEmail(), nes.getUserMailbox(),
//                    nes.getSourceSMSAddress(), nes.getValidity(), nes.getType(), nes.isRoamingFilterUsed(), notifierEvent);
//        } catch (TemplateMessageGenerationException e) {
//            SMPPTestClientLogger
//            .writeLogMessageToFile("TemplateMessageGenerationException on handle SMS.");
//        }
    }
}
