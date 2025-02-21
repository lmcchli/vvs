/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.test;

import com.mobeon.common.smscom.test.SMSAddressTest;
import com.mobeon.common.smscom.test.SMSMessageTest;
import com.mobeon.ntf.deferred.test.DeferredInfoTest;
import com.mobeon.ntf.fax.test.FaxPrinterTest;
import com.mobeon.ntf.mail.test.NotificationEmailTest;
import com.mobeon.ntf.mail.test.SystemNotificationTest;
import com.mobeon.ntf.out.sip.test.SIPCommandHandlerTest;
import com.mobeon.ntf.out.sms.test.SMSOutTest;
import com.mobeon.ntf.slamdown.test.CallerInfoTest;
import com.mobeon.ntf.slamdown.test.SlamdownFormatterTest;
import com.mobeon.ntf.slamdown.test.SlamdownListTest;
import com.mobeon.ntf.text.test.TextCreatorTest;
import com.mobeon.ntf.userinfo.mail.test.MailUserInfoTest;
import com.mobeon.ntf.userinfo.mur.test.MurConnectionPoolTest;
import com.mobeon.ntf.userinfo.mur.test.MurConnectionTest;
import com.mobeon.ntf.userinfo.mur.test.MurCosCacheTest;
import com.mobeon.ntf.userinfo.mur.test.MurCosTest;
import com.mobeon.ntf.userinfo.mur.test.MurUserFactoryTest;
import com.mobeon.ntf.userinfo.test.NotificationFilterTest;
import com.mobeon.ntf.userinfo.test.PagFilterInfoTest;
import com.mobeon.ntf.userinfo.test.SmsFilterInfoTest;
import com.mobeon.ntf.util.delayline.test.PersistentDelayItemTest;
import com.mobeon.ntf.util.test.GroupedPropertiesTest;

import junit.framework.*;
import junit.runner.BaseTestRunner;

/**
 * TestSuite for all JUnit tests in NTF that can be executed in less than 10 seconds
 *
 */
public class QuickTests {




    /** main runs the tests.*/
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /** suite lists all quick JUnit tests in NTF.*/
    public static Test suite() {
        TestSuite suite= new TestSuite("NTF quick Tests");
        //suite.addTestSuite(ComponentRegisterTest.class);
        //NEEDS REWRITE FOR CHANGED INTERFACE suite.addTestSuite(ConverterTest.class);
        suite.addTestSuite(GroupedPropertiesTest.class);
        suite.addTestSuite(MailUserInfoTest.class);
        suite.addTestSuite(MurConnectionPoolTest.class);
        suite.addTestSuite(MurConnectionTest.class);
        suite.addTestSuite(MurCosCacheTest.class);
        suite.addTestSuite(MurCosTest.class);
        suite.addTestSuite(MurUserFactoryTest.class);
        suite.addTestSuite(NotificationEmailTest.class);
        suite.addTestSuite(NotificationFilterTest.class);
        suite.addTestSuite(NotificationGroupTest.class);
        suite.addTestSuite(PagFilterInfoTest.class);
        suite.addTestSuite(SmsFilterInfoTest.class);
        suite.addTestSuite(SMSOutTest.class);
        suite.addTestSuite(SystemNotificationTest.class);
        suite.addTestSuite(TextCreatorTest.class);
        suite.addTestSuite(SlamdownListTest.class);
	suite.addTestSuite(CallerInfoTest.class);
	suite.addTestSuite(SlamdownFormatterTest.class);
        suite.addTestSuite(SMSAddressTest.class);
        suite.addTestSuite(SMSMessageTest.class);
        suite.addTestSuite(com.mobeon.common.commands.test.CommandHandlerTest.class);
        suite.addTestSuite(com.mobeon.common.commands.test.CommandTest.class);
        suite.addTestSuite(com.mobeon.common.commands.test.OperationTest.class);
        suite.addTestSuite(com.mobeon.common.commands.test.StateTest.class);
        suite.addTestSuite(com.mobeon.ntf.out.outdial.test.OutdialInfoTest.class);
        suite.addTestSuite(com.mobeon.ntf.out.outdial.test.PhoneOnMapTest.class);
        //suite.addTestSuite(PhrasesTest.class);
        suite.addTestSuite(PersistentDelayItemTest.class);
        suite.addTestSuite(CallerInfoTest.class);
        suite.addTestSuite(DeferredInfoTest.class);
        suite.addTestSuite(FaxPrinterTest.class);
        suite.addTestSuite(com.mobeon.ntf.out.sms.test.SMSConfigWrapperTest.class);
        suite.addTestSuite(NotificationSMSAddressTest.class);
        suite.addTestSuite(SIPCommandHandlerTest.class);
    return suite;
    }
}
