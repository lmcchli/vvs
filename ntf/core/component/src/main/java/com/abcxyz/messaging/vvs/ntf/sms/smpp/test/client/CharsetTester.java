package com.abcxyz.messaging.vvs.ntf.sms.smpp.test.client;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.text.TestUser;
import com.mobeon.ntf.util.Logger;

public class CharsetTester {
    private NtfEventSimulator ntfEventSimulator = null;
    private SMSOut smsOut = null;
    private TestUser testUser = null;
    private final String charsetUsed;
    private final String preferredLanguage;

    CharsetTester() {
        this.ntfEventSimulator = new NtfEventSimulator();
        this.testUser = this.ntfEventSimulator.getUserInfo();
        this.smsOut = SMSOut.get();
        this.charsetUsed = Config.getCharsetEncoding();
        this.preferredLanguage = this.testUser.getPreferredLanguage();
    }

    /**
     * Runs all the charset tests.
     */
    void runTests() {
        SMPPTestClientLogger
                .writeLogMessageToFile("CharsetTester.sendDataSmUsingShiftJisCharset(): About to send out data_sm requests to test the charset feature.\n");
        sendDataSmUsingEnDotCphr();
        sendDataSmUsingJaDotCphr();
        sendDataSmUsingShiftJisCharset();
    }

    /**
     * Sends a data_sm request using the en.cphr template.
     */
    void sendDataSmUsingEnDotCphr() {
        this.testUser.setPreferredLanguage("en");
        Config.setCfgVar(NotificationConfigConstants.CHARSET_ENCODING, "");
        SMPPTestClientLogger
                .writeLogMessageToFile("CharsetTester.sendDataSmUsingShiftJisCharset(): The user's preferred language is en. The following template will be used: en.cphr.\n");
        sendToHandleSMS(this.ntfEventSimulator);
        this.testUser.setPreferredLanguage(this.preferredLanguage);
        Config.setCfgVar(NotificationConfigConstants.CHARSET_ENCODING, this.charsetUsed);   
    }

    /**
     * Sends a data_sm request using the ja.chpr template.
     */
    void sendDataSmUsingJaDotCphr() {
        this.testUser.setPreferredLanguage("ja");
        Config.setCfgVar(NotificationConfigConstants.CHARSET_ENCODING, "");
        SMPPTestClientLogger
                .writeLogMessageToFile("CharsetTester.sendDataSmUsingShiftJisCharset(): The user's preferred language is ja. The following template will be used: ja.cphr.\n");
        sendToHandleSMS(this.ntfEventSimulator);
        this.testUser.setPreferredLanguage(this.preferredLanguage);
        Config.setCfgVar(NotificationConfigConstants.CHARSET_ENCODING, this.charsetUsed);   
    }

    /**
     * Sends a data_sm request using the ja-c-shiftjis.cphr template.
     */
    void sendDataSmUsingShiftJisCharset() {
        this.testUser.setPreferredLanguage("ja");
        Config.setCfgVar(NotificationConfigConstants.CHARSET_ENCODING, "shiftjis");
        SMPPTestClientLogger
                .writeLogMessageToFile("CharsetTester.sendDataSmUsingShiftJisCharset(): The user's preferred language is ja. The following template will be used: ja-c-shiftjis.cphr.\n");
        sendToHandleSMS(this.ntfEventSimulator);
        this.testUser.setPreferredLanguage(this.preferredLanguage);
        Config.setCfgVar(NotificationConfigConstants.CHARSET_ENCODING, this.charsetUsed);   
    }

    /**
     * Calls the handleSMS method in SMSOut to send out the data_sm request.
     * @param nes Contains all the necessary information that is required when calling the handleSMS method in SMSOut.
     */
    void sendToHandleSMS(NtfEventSimulator nes) {
        //TODO Need to fix due to split of notifier into a framework and plug-in 
//        try {
//            this.smsOut.handleSMS(nes.getUserInfo(), nes.getSmsFilterInfo(), null, nes.getNotificationEmail(), nes.getUserMailbox(),
//                    nes.getSourceSMSAddress(), nes.getValidity(), nes.getType(), nes.isRoamingFilterUsed(), nes.getTextMailNotifierEvent());
//        } catch (TemplateMessageGenerationException e) {
//            SMPPTestClientLogger
//            .writeLogMessageToFile("TemplateMessageGenerationException on handle SMS.");
//        }
    }
}
