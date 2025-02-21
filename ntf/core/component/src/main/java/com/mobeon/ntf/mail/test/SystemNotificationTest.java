/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail.test;

import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.SystemNotification;
import com.mobeon.ntf.util.GroupedProperties;
import java.io.FileInputStream;
import java.util.*;
import junit.framework.*;


public class SystemNotificationTest extends TestCase {

    private SystemNotification snf;
    private NotificationEmail normal;
    private NotificationEmail quota;
    private NotificationEmail faxfail;

    public SystemNotificationTest(String name) {
	super(name);

	normal= new NotificationEmail(9999 , "To: lennart\nSubject: test");
	quota= new NotificationEmail(9999, "From: Mail_Administrator\nSubject: WARNING: Quota exceeded");
	faxfail= new NotificationEmail(9999, "Content-type: Multipart/report;report-type=x-amteva-fax-print-fail-fax");
    }

    /*
     *
     */
    public void testSystemNotification() throws Exception {
	GroupedProperties gp= new GroupedProperties();
	FileInputStream is= new FileInputStream("systemnotification.cfg");
	gp.load(is);

	snf= new SystemNotification(gp);

	assertNull(snf.getSystemNotificationName(normal));
	assertEquals("mailquotaexceeded", snf.getSystemNotificationName(quota));
	assertEquals("faxprintfailed", snf.getSystemNotificationName(faxfail));
    }
}
