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

import java.util.Properties;

/**
 * Test important methods in the Operation Class.
 */
public class CommandHandlerTest extends TestCase
{

    public CommandHandlerTest(String testName)
    {
        super(testName);
    }


    public static Test suite()
    {
        // Make sure that we are initialized
        Class ch = CommandHandler.class;
        TestSuite suite = new TestSuite(CommandHandlerTest.class);
        return suite;
    }

    public void setUp()
        throws CommandException
    {
        SDLogger.setLogger(new DelayLoggerProxy());
    }

    private Properties makeBasicProps()
    {
        // We have two states (0,1) and three events (1,2,3)
        // Mostly the default transitions is used but both states
        // defines their own specifics too
        Properties props = new Properties();
        props.setProperty("maxwaithours", "25");
        props.setProperty("initialstate", "0");
        props.setProperty("numberofstates", "3");
        props.setProperty("default.1", "END/");
        props.setProperty("default.2", "1 / waiton; wait 200 ; call");
        props.setProperty("default.3", "END / fallback");

        props.setProperty("state.0.2", "1/waiton; WAIT 500; CALL");
        props.setProperty("state.1.3", "0/wait 3600; CALL");
        props.setProperty("state.0.default", "1/wait 200; call");
        props.setProperty("state.1.default", "END/fallback");
        // Only global defaults in state 3, used for checking bad code
        return props;
    }

    public void testBasicCreation() throws CommandException
    {
        Properties props = makeBasicProps();
        CommandHandler handler = new CommandHandler(props);
        verifyNextState(handler, 0, 1, -1);
        verifyNextState(handler, 0, 2, 1);
        verifyNextState(handler, 0, 3, -1);
        verifyNextState(handler, 1, 1, -1);
        verifyNextState(handler, 1, 2, 1);
        verifyNextState(handler, 1, 3, 0);

        verifyActionCount(handler, 0, 1, 0);
        verifyActionCount(handler, 0, 2, 3);
        verifyActionCount(handler, 0, 3, 1);
        verifyActionCount(handler, 1, 1, 0);
        verifyActionCount(handler, 1, 2, 3);
        verifyActionCount(handler, 1, 3, 2);

        verifyOpCodes(handler, 0, 1, new int[]{}, new String[]{});
        verifyOpCodes(handler, 0, 2,
                      new int[]{CommandHandler.OP_WAITON,
                                CommandHandler.OP_WAITTIME,
                                CommandHandler.OP_CALL},
                      new String[]{"", "500", ""});
        verifyOpCodes(handler, 0, 3,
                      new int[]{CommandHandler.OP_FALLBACK},
                      new String[]{});

        verifyOpCodes(handler, 0, 1, new int[]{}, new String[]{});

        verifyOpCodes(handler, 1, 2,
                      new int[]{CommandHandler.OP_WAITON,
                                CommandHandler.OP_WAITTIME,
                                CommandHandler.OP_CALL},
                      new String[]{"", "200", ""});
        props.setProperty("state.1.3", "0/wait 3600; CALL");

        verifyOpCodes(handler, 1, 3,
                      new int[]{CommandHandler.OP_WAITTIME,
                                CommandHandler.OP_CALL},
                      new String[]{"3600", ""});

        assertEquals("Max wait hour", 25, handler.getMaxWaitHours());
        assertEquals("Number of states", 3, handler.getNoStates());
    }


    public void testFailsOnBadTransition()
    {
        Properties props = makeBasicProps();
        props.setProperty("state.1.2", "3/WAIT 20");
        try {
            CommandHandler h = new CommandHandler(props);
            fail("Should disallow transition to non existing state");

        } catch (CommandException expected) {
            CHLogger.log(CHLogger.DEBUG, "Got expected exception :" + expected);
        }
    }

    public void testFailsOnBadCode()
        throws CommandException
    {
        Properties props = makeBasicProps();
        CommandHandler h = new CommandHandler(props);

        try {
            Command cmd = h.getCommand(2, 222);
            fail("Should not get command when bad event code");
        } catch (CommandException expected) {
            CHLogger.log(CHLogger.DEBUG, "Got expected exception :" + expected);
        }
    }


    public void testExceptionOnBadState()
        throws CommandException
    {
        Properties props = makeBasicProps();

        CommandHandler h = new CommandHandler(props);
        try {
            Command cmd = h.getCommand(55, 1);
            fail("Should not get command when bad state");
        } catch (CommandException expected) {
            CHLogger.log(CHLogger.DEBUG, "Got expected exception :" + expected);
        }
    }

    public void testDefaultFromState()
       throws CommandException
    {
        Properties props = makeBasicProps();
        CommandHandler handler = new CommandHandler(props);
        verifyNextState(handler, 0, 7, 1);
        verifyNextState(handler, 1, 8, -1);
        verifyActionCount(handler, 0, 6, 2);
        verifyActionCount(handler, 1, 11, 3);
    }


    private void verifyActionCount(CommandHandler handler, int state,
                                   int eventCode, int wantedCount) throws CommandException
    {
        Command cmd = handler.getCommand(state, eventCode);
        assertEquals("ActionCount", wantedCount, cmd.getOperationCount());

    }

    private void verifyNextState(CommandHandler handler, int state,
                                 int eventCode,
                                 int wantedState)
        throws CommandException
    {
        Command cmd = handler.getCommand(state, eventCode);
        assertEquals("Nextstate", wantedState, cmd.getNextState());
    }


    private void verifyOpCodes(CommandHandler handler, int state,
                               int eventCode, int[] opcodes,
                               String[] params)
        throws CommandException
    {
        Command cmd = handler.getCommand(state, eventCode);
        assertEquals("Operation count", opcodes.length, cmd.getOperationCount());
        for (int i = 0; i < opcodes.length; i++) {
            Operation op = cmd.getCurrentOperation();
            assertEquals("Operation code", opcodes[i], op.getOpcode());
            assertEquals("Operation param", params[i], op.getParam());
            cmd.operationDone();
        }
    }
}
