package com.mobeon.masp.execution_engine.runapp;

import java.util.List;
import java.util.concurrent.Callable;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.execution_engine.runapp.mock.SubscribeCallMock;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.stream.RecordFinishedEvent;

import junit.framework.Test;



public class ApplicationMiscSipSubscribeTest extends ApplicationBasicTestCase<ApplicationMiscSipSubscribeTest> {

	

	/**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("misc");
        testSubdir("sipsubscribe");
        testCases(
                testCase("MWISubscribe")
        );
        store(ApplicationMiscSipSubscribeTest.class);
    }


    public ApplicationMiscSipSubscribeTest(String event) {
		super(event,"test_log_debug.xml");
	}

    
    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationMiscSipSubscribeTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationMiscSipSubscribeTest.class);
    }   
    
    
    /**
     * This testcase simulate a SIP subscribe
     *
     * @throws Exception
     */
    public void testSIPSubscribe () throws Exception {

        // Setup the call
        final SubscribeCallMock icm = createSubscribe ("MWISubscribe");
        
        icm.startSubscribe();
        
        //icm.fireEvent(event)
      //  icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);
        
        
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
        
        lfe.add2LevelRequired(".*\\sSIP Subscribe connection alerting*");
        
        lfe.add2LevelRequired(".*\\sSIP User Agent is Valid.*");

       /* lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play\\sPlayjob\\sstarted.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");*/
      //  lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

    }
    
}
