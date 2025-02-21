/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.test;

import com.mobeon.ntf.mail.test.UserMailboxTest;
import com.mobeon.ntf.management.test.ManagementInfoTest;
import com.mobeon.ntf.out.sip.test.SIPCallerTest;
import com.mobeon.ntf.out.sip.test.SIPOutTest;
import com.mobeon.ntf.test.*;
import com.mobeon.ntf.userinfo.mur.test.MurUserInfoTest;
import com.mobeon.ntf.util.test.ErrorLogLimiterTest;

import junit.framework.*;
import junit.runner.BaseTestRunner;

/**
 * TestSuite for all JUnit tests in NTF
 *
 */
public class AllTests {


    /** main runs all JUnit tests in NTF.*/
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /** suite lists all JUnit tests in NTF.*/
    public static Test suite() {
        TestSuite suite= new TestSuite("NTF Tests");
        suite.addTest(QuickTests.suite());
        suite.addTestSuite(MurUserInfoTest.class);
        suite.addTestSuite(UserMailboxTest.class);
        suite.addTestSuite(com.mobeon.common.storedelay.test.DelayInfoDAOTest.class);
        suite.addTestSuite(com.mobeon.common.storedelay.test.DelayerTest.class);
        suite.addTestSuite(com.mobeon.ntf.out.outdial.test.OutdialListenerTest.class);
        suite.addTestSuite(ManagementInfoTest.class);
        suite.addTestSuite(SIPOutTest.class);
        suite.addTestSuite(SIPCallerTest.class);
        suite.addTestSuite(ErrorLogLimiterTest.class);

        /*
         * TESTS THAT SHOULD NOT BE RUN

         * REASON
         * MailboxPoller can not be reliably tested, since its threads work
         * independently of the test code and corrupt the data.
         * FIX
         * The test program should be able to halt the mailbox pollers.
         suite.addTestSuite(MailboxPollerTest.class);

         * REASON
         * The delay line is not used any more, and the tests are
         * timing-sensitive.
         * FIX
         * Make time margins longer (and test slower) when the delay lines are
         * used again.
         suite.addTestSuite(DelayLineTest.class);
         suite.addTestSuite(PersistentDelayLineTest.class);


         * REASON
         * DeferredCmdHandlerTest calls exit and ruins the test suite.
         * FIX
         * ?
         suite.addTestSuite(com.mobeon.ntf.deferred.test.DeferredCmdHandlerTest.class);

         */
	return suite;
    }
}
