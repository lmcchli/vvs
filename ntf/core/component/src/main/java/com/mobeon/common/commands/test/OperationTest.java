/*
 * OperationTest.java
 * JUnit based test
 *
 * Created on den 26 augusti 2004, 16:12
 */

package com.mobeon.common.commands.test;

import junit.framework.*;
import com.mobeon.common.commands.*;
import com.mobeon.ntf.util.DelayLoggerProxy;
import com.mobeon.common.storedelay.SDLogger;

/**
 * Test important methods in the Operation Class.
 */
public class OperationTest extends TestCase
{


    private static short TEST_OP_CODE = 1;
    private static String TEST_OP_NAME = "TEST";
    private static String TEST_OP_PARAM = "5";
    private static String TEST_OP_NAMES[] =
        {"NOOP", TEST_OP_NAME};

    private Operation op;


    public OperationTest(java.lang.String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(OperationTest.class);
        return suite;
    }

    public void setUp()
    {
        op = new Operation(TEST_OP_CODE, TEST_OP_NAME, TEST_OP_PARAM);
        SDLogger.setLogger(new DelayLoggerProxy());
    }


    /**
     * Test of pack method, of class commands.Operation.
     */
    public void testPackRestore()
    {
        CHLogger.log(CHLogger.DEBUG, "testPackRestore for operation " + op);

        byte[] data = new byte[100];
        int bytes = op.pack(data, 0);
        assertEquals("Packed bytes should be same as expected",
                     op.getPackSize(),
                     bytes);

        CommandUtil.printByteArray(data);
        CHLogger.log(CHLogger.DEBUG, "");

        Operation newOp = new Operation();
        CHLogger.log(CHLogger.DEBUG, "New operation : " + newOp);

        newOp.restore(data, 0, TEST_OP_NAMES);
        CHLogger.log(CHLogger.DEBUG, "Restored op :" + newOp);
        assertEquals("Opcode should be restored",
                     op.getOpcode(), newOp.getOpcode());
        assertEquals("Parameter should be restored",
                     op.getParam(), newOp.getParam());
        assertEquals("Name should be restored",
                     op.getOpname(), newOp.getOpname());


    }

    /**
     * Test of packSize method, of class commands.Operation.
     */
    public void testPackSize()
    {
        CHLogger.log(CHLogger.DEBUG, "Test pack size");
        Operation op2 = new Operation(TEST_OP_CODE, TEST_OP_NAME, null);
        int size = op2.getPackSize();
        assertEquals("Constant packsize for operations without param", 6, size);

        Operation op3 = new Operation(TEST_OP_CODE, TEST_OP_NAME, "");
        size = op3.getPackSize();
        assertEquals("Constant packsize for operations with empty param", 6, size);

        Operation op4 = new Operation(TEST_OP_CODE, TEST_OP_NAME, "c(0,0,0)");
        size = op4.getPackSize();
        CHLogger.log(CHLogger.DEBUG, "Packsize for c(0,0,0) == " + size);

    }

}
