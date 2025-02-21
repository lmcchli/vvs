package com.mobeon.common.commands.test;

/*
 * StateTest.java
 * JUnit based test
 *
 * Created on den 26 augusti 2004, 16:12
 */

import junit.framework.*;
import com.mobeon.common.commands.*;
import com.mobeon.ntf.util.DelayLoggerProxy;
import com.mobeon.common.storedelay.SDLogger;

/**
 * Test important methods in the Operation Class.
 */
public class StateTest extends TestCase
{

    private State testState;

    // NOTE: We are using opcodes defined in CommandHandler
    private static final int CODE_NOOP = 0;
    private static final int CODE_OP1 = 1;
    private static final int CODE_OP2 = 2;
    private static final int CODE_OP3 = 3;

    private static final int UNUSED_EVENT = 5;
    private static final int EVENT_1 = 10;
    private static final int EVENT_2 = 20;
    private static final int EVENT_3 = 30;
    private static final int EVENT_4 = 40;
    private static final int EVENT_1_NEXT = 2;
    private static final int EVENT_2_NEXT = 3;
    private static final int EVENT_3_NEXT = State.STATE_END;
    private static final int DEFAULT_NEXT = 4;


    static
    {

    }

    public StateTest(String testName)
    {
        super(testName);
    }


    public static Test suite()
    {
        // Make sure that we are initialized
        Class ch = CommandHandler.class;
        TestSuite suite = new TestSuite(StateTest.class);
        return suite;
    }

    public void setUp()
        throws CommandException
    {
        testState = new State();
        testState.addTransition(EVENT_1, "waitOn 5;  wait 10; fallback", EVENT_1_NEXT);
        testState.addTransition(EVENT_2, "", EVENT_2_NEXT);
        testState.addTransition(EVENT_3, "nooP", State.STATE_END);
        SDLogger.setLogger(new DelayLoggerProxy());
    }

    public void testNoTransition()
    {
        Command cmd = testState.getTransitionCommand(UNUSED_EVENT);
        assertNull("No transition should be found for unused", cmd);
    }

    public void testEvent1()
    {
        Command cmd = testState.getTransitionCommand(EVENT_1);
        CHLogger.log( CHLogger.DEBUG, "Command for event1 : " + cmd);
        assertEquals("Next state for EVENT_1", EVENT_1_NEXT, cmd.getNextState());
        assertEquals("No of Operations for EVENT_1", 3, cmd.getOperationCount());
        Operation firstOp = cmd.getCurrentOperation();
        CHLogger.log( CHLogger.DEBUG, "Found firstop : " + firstOp);
        assertEquals("Firstop should be op1", CODE_OP1, firstOp.getOpcode());
        assertEquals("Name of first op", "waiton", firstOp.getOpname());
        assertEquals("First ops param", "5", firstOp.getParam());
        cmd.operationDone();
        Operation secondOp = cmd.getCurrentOperation();
        assertEquals("Second op", CODE_OP2, secondOp.getOpcode());
        assertEquals("Second op name", "wait", secondOp.getOpname());
        assertEquals("Second op param", "10", secondOp.getParam());
        cmd.operationDone();
        Operation thirdOp = cmd.getCurrentOperation();
        assertEquals("Third op", CODE_OP3, thirdOp.getOpcode());
        assertEquals("Third op name", "fallback", thirdOp.getOpname());
        assertEquals("Thrird op param", "c(1,2)", thirdOp.getParam());
        cmd.operationDone();
        Operation fourthOp = cmd.getCurrentOperation();
        assertNull("Only three operations", fourthOp);
    }

    public void testEvent2()
    {
        Command cmd = testState.getTransitionCommand(EVENT_2);
        CHLogger.log( CHLogger.DEBUG, "Command for event 2 : " + cmd);
        assertEquals("Next state for EVENT_2", EVENT_2_NEXT, cmd.getNextState());
        assertEquals("No of ops for EVENT_2", 0, cmd.getOperationCount());
    }

    public void testEvent3()
    {
        Command cmd = testState.getTransitionCommand(EVENT_3);
        CHLogger.log( CHLogger.DEBUG, "Command for event 3 : " + cmd);
        assertEquals("Next state for EVENT_3", EVENT_3_NEXT, cmd.getNextState());
        assertEquals("No of ops for EVENT_3", 1, cmd.getOperationCount());
        Operation firstOp = cmd.getCurrentOperation();
        assertEquals("First op", CODE_NOOP, firstOp.getOpcode());
        assertEquals("First op name", "noop", firstOp.getOpname());
        assertEquals("First op param", "", firstOp.getParam());
    }


    public void testDefault()
        throws CommandException
    {
        testState.addDefaultTransition("waitOn 10; fallback", DEFAULT_NEXT);
        Command cmd = testState.getTransitionCommand(EVENT_4);
        CHLogger.log( CHLogger.DEBUG, "Command for EVENT_4 : " + cmd);
        assertEquals("Null explicit command for event 4", cmd, null);
        cmd = testState.getDefaultCommand();
        CHLogger.log( CHLogger.DEBUG, "Default command : " + cmd);
        assertEquals("Next state for EVENT_4", DEFAULT_NEXT, cmd.getNextState());
        assertEquals("No of ops for EVENT_4", 2, cmd.getOperationCount());
    }

}
