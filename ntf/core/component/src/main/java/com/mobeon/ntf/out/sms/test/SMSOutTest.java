/****************************************************************
 * Copyright (c) 2002 Abcxyz Radio Systems AB
 * All Rights Reserved
 ****************************************************************/
package com.mobeon.ntf.out.sms.test;

import java.util.*;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.test.*;
import com.mobeon.ntf.userinfo.mur.*;
import com.mobeon.ntf.mail.*;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.Config;


/****************************************************************
 *The Test_SMSOut is the entry point for sending SMS notifications.
 *Normally this class forwards requests to a handler thread from a pool,
 *holding the calling thread only for a short time. If too many handlers are
 *already running, the calling thread will hang until a handler is released.
 */
public class SMSOutTest extends NtfTestCase {

    int okCount = 0;
    int retryCount = 0;
    int expireCount = 0;
    int failCount = 0;

    private void resetCount() {
        okCount = 0;
        retryCount = 0;
        expireCount = 0;
        failCount = 0;
    }

    public SMSOutTest(String name) {
        super(name);
    }

    public void testSourceAddressPerCos() throws Exception {

        String smeSourceaddress = "310000035";
        Config.setCfgVar("smesourceaddress", smeSourceaddress);
        String configValues[] = {"fax",
                                 "voice",
                                 "video",
                                 "voicemailoffreminder",
                                 "temporarygreetingonreminder",
                                 "smstype0",
                                 "ivrtsosms",
                                 "slamdown",
                                 "mwion",
                                 "mwioff",
                                 "mailquotaexceeded",
                                 "flash",
                                 "email",
                                 "cfuonreminder"};
        String cosNames[] = {"cos1", "cos2", "cos3"};
        int nextNr = 101;

        /* Make sure config values are set ok. */
        /* Unset video should return smesourceaddress */
        assertEquals(smeSourceaddress,
                     Config.getSourceAddress("video").getNumber());
        /* Test fallback system notification name w/o CoS name */
        Config.setCfgVar("sourceaddress_video",   "310000001");
        assertEquals("310000001",
                     Config.getSourceAddress("video").getNumber());

        for (int i = 0; i < configValues.length; i++) {
            for (int j = 0; j < cosNames.length; j++) {
                    Config.setCfgVar("sourceaddress_"
                                     + configValues[i] + "_" + cosNames[j],
                                     "310000" + nextNr);
                    nextNr++;
            }
        }
        nextNr=101;
        /* Make sure all cos-specific names are set */
        for (int i = 0; i < configValues.length; i++) {
            for (int j = 0; j < cosNames.length; j++) {
                assertEquals( "310000" + nextNr,
                              Config.getSourceAddress(configValues[i] + "_"
                                                      + cosNames[j]).getNumber());
                nextNr++;
            }
        }
    }


    public void testDiscard() throws Exception {
        Config.setCfgVar("discardsmswhencountis0", "true");
        SMSOut smsOut = SMSOut.get();
        TestUser user = null;
        NotificationEmail mail = null;
        SmsFilterInfo filter = null;
        FeedbackHandler ng = new MyFeedbackHandler();
        SMSAddress source = null;
        UserMailbox inbox = null;

        mail = new NotificationEmail(9999, "Content-type:multipart/voice-message\nFrom:12345 <12345@host.domain>\nX-priority:3\nSubject:testemail\n\n");
        mail.setReceivedDate(new Date());
        //mail = new NotificationEmail()
        user = new TestUser();
        inbox = new UserMailbox(0,0,0,0,false);
        Properties props = new Properties();
        props.put("SMS", "c");
        filter = new SmsFilterInfo(props, new String[] { "123123" }, null);

        resetCount();
        smsOut.handleSMS(user, filter, ng, mail, inbox, source, 3, 0);
        Thread.sleep(200);
        assertEquals(1, retryCount);
        assertEquals(0, expireCount);

        Calendar cal = new GregorianCalendar();
        cal.roll(Calendar.MINUTE, -5);
        mail.setReceivedDate(cal.getTime());

        resetCount();
        smsOut.handleSMS(user, filter, ng, mail, inbox, source, 3, 0);
        Thread.sleep(200);
        assertEquals(1, retryCount);
        assertEquals(0, expireCount);

        cal = new GregorianCalendar();
        cal.roll(Calendar.HOUR, -1);
        mail.setReceivedDate(cal.getTime());

        resetCount();
        smsOut.handleSMS(user, filter, ng, mail, inbox, source, 3, 0);
        Thread.sleep(200);
        assertEquals(0, retryCount);
        assertEquals(1, expireCount);
    }

    /****************************************************************
     *The constructor initializes the SMS center table. Clients do nou use it,
     *the single instance is retrieved with get instead.

    private SMSOutTest() {
	super();
    }


    public static void main(String[] args) {

        //	args[0]= "SMS-C Real";

        //	out("\nTesting SMSOut to " + args[0]);
        //	out("================================================================");

        //	SMSOutTest t= new SMSOutTest();
        //	SMSOut so= SMSOut.get();
        //	int res;
        //	boolean needsC;
        //	int ton= 1;
        //	int npi= 1;
        //
        //	out("handleSMS...");
	//	for (ton= 0; ton < 8; ton++) {
	//	    for (npi= 0; npi < 16; npi++) {
        //	res= so.handleSMS(new MessageCount(),
        //			  new NotificationEmail(),
        //			  new SMSAddress(ton, npi, "46123456789"),
        //			  new UserPrefs("sv",
        //					"c",
        //					NTF_SMS,
        //					"yyyy/mm/dd",
        //					24),
        //			  args[0],
        //			  new MessageResponse(1, fm, 1),
        //			  new Date(new Date().getTime()+100000));
        //	out("..." + res);
	//	    }
	//	}

        //	out("handleMWIOff...");
	//res= so.handleMWIOff(new SMSAddress(1, 1, "46705354198"), args[0],
        //			     new MessageResponse(1, fm, 1));
        //	out("..." + res);
        //
        //	out("handleSMS...");
        //	res= so.handleSMS(new MessageCount(),
        //			  new NotificationEmail(),
        //			  new SMSAddress(1, 1, "46705354298"),
        //			  new UserPrefs("sv",
        //					"c",
        //					NTF_MWI,
        //					"yyyy/mm/dd",
        //					24),
        //			  args[0],
        //			  new MessageResponse(1, fm, 1),
        //			  new Date(new Date().getTime()+100000));
        //	out("..." + res);
        //
        //	try {Thread.sleep(10000);} catch (InterruptedException e) {}
        //	out("handleSMS...");
        //	res= so.handleSMS(new MessageCount(),
        //			  new NotificationEmail(),
        //			  new SMSAddress(1, 1, "46705354398"),
        //			  new UserPrefs("sv",
        //					"h",
        //					NTF_SMS,
        //					"yyyy/mm/dd",
        //					24),
        //			  args[0],
        //			  new MessageResponse(1, fm, 1),
        //			  new Date(new Date().getTime()+100000));
        //	out("..." + res);
        //
        //	out("needsCount c...");
        //	needsC= so.needsCount("sv", "c");
        //	out("..." + needsC);
        //
        //	out("needsCount s...");
        //	needsC= so.needsCount("sv", "s");
        //	out("..." + needsC);
        //
        //	for (int i= 0; i < 20; i++) {
        //	    out("sms " + i + "...");
        //	    res= so.handleSMS(new MessageCount(),
        //			      new NotificationEmail(),
        //			      new SMSAddress(1, 1, "467053543" + i),
        //			      new UserPrefs("sv",
        //					    "h",
        //					    NTF_SMS,
        //					    "yyyy/mm/dd",
        //					    24),
        //			      args[0],
        //			      new MessageResponse(1, fm, 1),
        //			      new Date(new Date().getTime()+100000));
        //	    try {Thread.sleep(50);} catch (InterruptedException e) {}
        //	}
        //
        //	try {Thread.sleep(60*1000);} catch (InterruptedException e) {}
        //	out("\nTest completed");
    }
     */

    private class MyFeedbackHandler implements FeedbackHandler {

        public void expired(com.mobeon.ntf.userinfo.UserInfo user, int notifType) {
            expireCount++;
        }

        public void failed(com.mobeon.ntf.userinfo.UserInfo user, int notifType, String msg) {
            failCount++;
        }

        public void ok(com.mobeon.ntf.userinfo.UserInfo user, int notifType) {
            okCount++;
        }

        public void retry(com.mobeon.ntf.userinfo.UserInfo user, int notifType, String msg) {
            retryCount++;
        }

    }
}


