/*
 * NotificationSMSAddressTest.java
 * JUnit based test
 *
 * Created 20 februari 2006, 09:38
 */

package com.mobeon.ntf.test;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.Constants;
import com.mobeon.common.smscom.SMSAddress;
import java.io.FileInputStream;
import junit.framework.*;


public class NotificationSMSAddressTest extends NtfTestCase {
    
    private NotificationEmail mail = null;
    private NotificationHandler notifHandler = null;
    
    public NotificationSMSAddressTest(String testName) {
        super(testName);
    }
    
     public void testFaxPrintFailed() throws Exception {
          l("testFaxPrintFailed");
         notifHandler = new NotificationHandler(1);
         mail = new NotificationEmail(9999, new FileInputStream("fax_printfailed.eml"));
         SMSAddress smsAddress = notifHandler.testGetSourceAdress(mail);
         l("smsaddress_fax=" + smsAddress.getNumber());
         assertEquals("310000074", smsAddress.getNumber());
     }
}
