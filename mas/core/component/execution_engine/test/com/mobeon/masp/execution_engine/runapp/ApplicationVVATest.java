/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.stream.RecordFinishedEvent;
import com.mobeon.masp.stream.ControlToken;
import junit.framework.Test;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Test suite for the VVA.
 */
public class ApplicationVVATest  extends ApplicationBasicTestCase<ApplicationVVATest> {

    /**
     * The list of all testcases that we need to execute
     */

    static {
         testLanguage("vva");
         testSubdir("");
         testCases(
                 testCase("vva")
                 );
        store(ApplicationVVATest.class);
     }

    /**
     * Creates this test case
     */
    public ApplicationVVATest (String event)
    {
      super (event, "test_log_info.xml");
    }

     /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVVATest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVVATest.class);
    }

    /**
     * This testcase tries to play a beep.wav with no parameters.
     *
     * @throws Exception
     */
    public void testVVA () throws Exception {

        // Setup the call
        final InboundCallMock icm = createCall ("vva");
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // When record starts, send DTMF
        icm.invokeWhenRecord(
                new Callable(){
                    public Object call() throws Exception {
                        new Thread() {
                            public void run() {
                                try {
                                    icm.sleep(1000);
                                    icm.sendDTMF(ControlToken.DTMFToken.HASH, 100, 500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                            }
                        }.start();
                        return null;
                    }
                });


        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(120000);
        TestAppender.stopSave(log);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play\\sPlayjob\\sstarted.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }
}
