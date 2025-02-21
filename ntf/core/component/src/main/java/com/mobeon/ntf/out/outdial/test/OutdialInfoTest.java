/*
 * OutdialInfoTest.java
 * JUnit based test
 *
 * Created on den 20 september 2004, 16:42
 */

package com.mobeon.ntf.out.outdial.test;

import java.util.*;

import com.mobeon.common.commands.Command;
import com.mobeon.common.commands.Operation;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.storedelay.DelayInfo;

import junit.framework.*;
import com.mobeon.ntf.out.outdial.*;
import com.mobeon.ntf.util.DelayLoggerProxy;
import com.mobeon.common.storedelay.SDLogger;

/**
 *
 * @author QMIER
 */
public class OutdialInfoTest extends TestCase
{

    public OutdialInfoTest(java.lang.String testName)
    {
        super(testName);
    }
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        SDLogger.setLogger(new DelayLoggerProxy());
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(OutdialInfoTest.class);
        return suite;
    }


    public void testPackUnpackNoCommand()
    {
        SDLogger.log(SDLogger.DEBUG, "testPackUnpackNoCommand");

        OdlInfo info = new OdlInfo("123456", "nisse@nissecorp.com",
                                   456909358094L,
                                   "nm=nisse,ou=nissecorp.com,l=abroad");
        SDLogger.log(SDLogger.DEBUG, "Created : " + info);
        DelayInfo di = info.getPersistentRepresentation();
        OdlInfo info2 = new OdlInfo(di);
        SDLogger.log(SDLogger.DEBUG, "Retreived: " + info2);
        verifyEqual(info, info2);
    }


    public void testPackUnpackCommand()
    {
        SDLogger.log(SDLogger.DEBUG, "testPackUnpackCommand");

        OdlInfo info = new OdlInfo("123456", "nisse@nissecorp.com",
                                   456909358094L,
                                   "nm=nisse,ou=nissecorp.com,l=abroad");

        List basicOps = new LinkedList();
        Operation firstOperation = new Operation(CommandHandler.OP_WAITON, "waiton", "");

        Operation secondOperation = new Operation(CommandHandler.OP_WAITTIME, "wait", "5");
        basicOps.add(secondOperation);
        basicOps.add(firstOperation);
        Operation thirdOp = new Operation(CommandHandler.OP_CALL, "call", "");
        basicOps.add(thirdOp);
        Command baseCommand = new Command(1, basicOps);

        info.setCurrentCommand(baseCommand);
        SDLogger.log(SDLogger.DEBUG, "Created : " + info);

        DelayInfo di = info.getPersistentRepresentation();
        SDLogger.log(SDLogger.DEBUG, "Persistent rep : " + di);
        OdlInfo info2 = new OdlInfo(di);
        SDLogger.log(SDLogger.DEBUG, "Retreived: " + info2);
        verifyEqual(info, info2);
    }

    private static void verifyEqual(OdlInfo i1, OdlInfo i2)
    {
        assertEquals("Dial number", i1.getDialNumber(), i2.getDialNumber());
        assertEquals("Email", i1.getUserEmail(), i2.getUserEmail());
        assertEquals("Starttime", i1.getStartTime(), i2.getStartTime());
        assertEquals("UserDN", i1.getUserDN(), i2.getUserDN());
        assertEquals("", i1.getVersion(), i2.getVersion());

        Command cmd1 = i1.getCurrentCommand();
        Command cmd2 = i2.getCurrentCommand();


        if (cmd1 == null) {
            assertNull("Both commands null", cmd2);
        } else {
            assertNotNull("No of the commands null", cmd2);
            assertEquals("Same next state", cmd1.getNextState(), cmd2.getNextState());
            assertEquals("Same op count", cmd1.getOperationCount(), cmd2.getOperationCount());

            while (cmd1.getOperationCount() != 0) {
                Operation op1 = cmd1.getCurrentOperation();
                Operation op2 = cmd2.getCurrentOperation();
                assertEquals("Same operation", op1.getOpcode(), op2.getOpcode());
                assertEquals("Same parameter", op1.getParam(), op2.getParam());
                cmd1.operationDone();
                cmd2.operationDone();

            }

        }

    }


}
