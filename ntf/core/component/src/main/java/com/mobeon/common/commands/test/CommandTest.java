/*
 * CommandTest.java
 * JUnit based test
 *
 * Created on den 26 augusti 2004, 18:11
 */

package com.mobeon.common.commands.test;

import java.util.List;
import java.util.LinkedList;

import junit.framework.*;

import com.mobeon.common.commands.*;
import com.mobeon.ntf.util.DelayLoggerProxy;
import com.mobeon.common.storedelay.SDLogger;

/**
 *
 * @author QMIER
 */
public class CommandTest extends TestCase
{
    /** Command that we test against */
    private Command baseCommand;
    /** First operation in baseCommands operation list */
    private Operation firstOperation;
    private Operation secondOperation;

    /** Next state in baseCommand */
    private static final int BASE_NEXT_STATE = 1;

    public CommandTest(java.lang.String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(CommandTest.class);
        return suite;
    }

    private static String[] OP_NAMES =
        {"TEST1", "TEST2"};

    public void setUp()
    {
        SDLogger.setLogger(new DelayLoggerProxy());
        List basicOps = new LinkedList();
        firstOperation = new Operation((short) 0, "TEST1", "c(0)");
        basicOps.add(firstOperation);
        secondOperation = new Operation((short) 1, "TEST2", "sms_template_1");
        basicOps.add(secondOperation);
        baseCommand = new Command(BASE_NEXT_STATE, basicOps);
    }

    /**
     * Test of clone method, of class commands.Command.
     */
    public void testClone()
    {
        CHLogger.log(CHLogger.DEBUG, "testClone");
        Command cloned = (Command) baseCommand.clone();
        checkForEquality(baseCommand, cloned);


    }

    private void checkForEquality(Command orig, Command copy)
    {
        assertEquals("Nextstate should be same",
                     copy.getNextState(), orig.getNextState());
        assertEquals("Number of commands should be same",
                     copy.getOperationCount(), orig.getOperationCount());
        Operation opBase = orig.getCurrentOperation();
        assertNotNull("Current op should exist for orig", opBase);
        Operation opCopy = copy.getCurrentOperation();
        assertNotNull("Current op should exist for copy", copy);
        assertTrue("Operations should not be the same object",
                   opBase != opCopy);
        assertEquals("Operations should be equal",
                     opBase, opCopy);
        copy.operationDone();
        assertEquals("Operation done should decrease commands with one",
                     orig.getOperationCount(), copy.getOperationCount() + 1);

    }


    /**
     * Test of pack method, of class commands.Command.
     */
    public void testPackRestore()
    {
        CHLogger.log(CHLogger.DEBUG, "testPackRestore");

        int packSize = baseCommand.getPackSize();
        byte[] data = new byte[packSize];
        baseCommand.pack(data, 0);
        CommandUtil.printByteArray(data);
        Command newCommand = new Command();
        newCommand.restore(data, 0, OP_NAMES);
        checkForEquality(baseCommand, newCommand);

    }

    /**
     * Test of packSize method, of class commands.Command.
     */
    public void testPackSize()
    {
        CHLogger.log(CHLogger.DEBUG, "testPackSize");
        int packSize = baseCommand.getPackSize();
        int expected = 8 + firstOperation.getPackSize() +
            secondOperation.getPackSize();
        assertEquals("Packsize for cmd+2ops = 20", expected, packSize);
    }

    /**
     * Test of currentOperation method, of class commands.Command.
     */
    public void testCurrentOperation()
    {
        CHLogger.log(CHLogger.DEBUG, "testCurrentOperation");
        Operation currOp = baseCommand.getCurrentOperation();
        assertEquals("First op equal to currOp", firstOperation, currOp);
        assertNotSame("First op has been copyed", firstOperation, currOp);

    }

    /**
     * Test of operationCount method, of class commands.Command.
     */
    public void testGetOperationCount()
    {
        CHLogger.log(CHLogger.DEBUG, "testOperationCount");
        int opCount = baseCommand.getOperationCount();
        assertEquals("We have two basic operations", 2, opCount);
    }

    /**
     * Test of getNextState method, of class commands.Command.
     */
    public void testGetNextState()
    {
        CHLogger.log(CHLogger.DEBUG, "testGetNextState");
        int nextState = baseCommand.getNextState();
        assertEquals("Next state should be as set in constructor",
                     BASE_NEXT_STATE, nextState);
    }


}
