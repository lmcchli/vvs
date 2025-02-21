package com.mobeon.ntf.userinfo.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.userinfo.UserLogins;
import com.mobeon.ntf.mail.NotificationEmail;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-jun-01
 * Time: 15:37:39
 */
public class UserLoginsTest extends NtfTestCase {

    public UserLoginsTest(String name) {
        super(name);
    }

    public void test() throws Exception {
        String number = "444555666";
        String number2 = "111222333";
        NotificationEmail mail1 = new NotificationEmail
            (9999, "Return-path: <@>\r\n"
             + "Received: from as.ipt21.su.erm.abcxyz.se\r\n"
             + " (mvas2.ipt21.su.erm.abcxyz.se [10.15.1.13]) by as.ipt21.su.erm.abcxyz.se\r\n"
             + " (iPlanet Messaging Server 5.2 HotFix 1.04 (built Oct 21 2002))\r\n"
             + " with ESMTP id <0HF600IRBJCJ4L@as.ipt21.su.erm.abcxyz.se> for\r\n"
             + " timmgr@ims-ms-daemon; Tue, 20 May 2003 11:50:43 +0200 (MEST)\r\n"
             + "Date: Tue, 20 May 2003 11:50:39 +0100\r\n"
             + "From: 5255110309 <>\r\n"
             + "Subject: =?iso-8859-1?Q?Voice_Message_?= =?iso-8859-1?Q?from_?=\r\n"
             + " =?UTF-8?Q?5255110309?=\r\n"
             + "To: timmgr@ipt21.su.erm.abcxyz.se\r\n"
             + "Reply-to:\r\n"
             + "Message-id: <3EC9FA6F.00000B.011CC@unknown.host>\r\n"
             + "MIME-version: 1.0 (Voice 2.0)\r\n"
             + "Content-type: multipart/voice-message;\r\n"
             + " boundary=\"------------Boundary-01=_FCJ6QYRXFQQMYJ0CCJD0\"\r\n"
             + "Original-recipient: rfc822;timmgr@ipt21.su.erm.abcxyz.se\r\n"
             + "\r\nbody");
        mail1.setIMAPThread(new Integer(5));

        NotificationEmail mail2 = new NotificationEmail
            (9999, "Return-path: <@>\r\n"
             + "Received: from as.ipt21.su.erm.abcxyz.se\r\n"
             + " (mvas2.ipt21.su.erm.abcxyz.se [10.15.1.13]) by as.ipt21.su.erm.abcxyz.se\r\n"
             + " (iPlanet Messaging Server 5.2 HotFix 1.04 (built Oct 21 2002))\r\n"
             + " with ESMTP id <0HF600IRBJCJ4L@as.ipt21.su.erm.abcxyz.se> for\r\n"
             + " timmgr@ims-ms-daemon; Tue, 20 May 2003 11:50:43 +0200 (MEST)\r\n"
             + "Date: Tue, 20 May 2003 11:50:39 +0100\r\n"
             + "From: 5255110309 <>\r\n"
             + "Subject: =?iso-8859-1?Q?Voice_Message_?= =?iso-8859-1?Q?from_?=\r\n"
             + " =?UTF-8?Q?5255110309?=\r\n"
             + "To: timmgr@ipt21.su.erm.abcxyz.se\r\n"
             + "Reply-to:\r\n"
             + "Message-id: <ASDHHJ54A.098300B.011CC@unknown.host>\r\n"
             + "MIME-version: 1.0 (Voice 2.0)\r\n"
             + "Content-type: multipart/voice-message;\r\n"
             + " boundary=\"------------Boundary-01=_FCJ6QYRXFQQMYJ0CCJD0\"\r\n"
             + "Original-recipient: rfc822;timmgr@ipt21.su.erm.abcxyz.se\r\n"
             + "\r\nbody");

        UserLogins.loginUser(number);
        assertTrue(UserLogins.isUserLoggedIn(number));
        int count = UserLogins.getStoredEmailCount(number);
        assertEquals(0, count);
        UserLogins.logoutUser(number);
        assertFalse(UserLogins.isUserLoggedIn(number));
        count = UserLogins.getStoredEmailCount(number2);
        assertEquals(0, count);


        assertFalse(UserLogins.isUserLoggedIn(number2));
        UserLogins.logoutUser(number2);


        UserLogins.loginUser(number);
        UserLogins.storeMail(number, mail1);
        UserLogins.storeMail(number, mail2);
        assertTrue( UserLogins.isUserLoggedIn(number));
        count = UserLogins.getStoredEmailCount(number);
        assertEquals(2, count);
        UserLogins.logoutUser(number);


    }
}
