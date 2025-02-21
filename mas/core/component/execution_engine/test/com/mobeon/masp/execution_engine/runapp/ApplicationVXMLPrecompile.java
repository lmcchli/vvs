/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

import java.util.List;

public class ApplicationVXMLPrecompile extends ApplicationBasicTestCase {


    private static ApplicationTestCase testCases[] = {
            new ApplicationTestCase("catch_shorthand", "test:/test/com/mobeon/masp/execution_engine/runapp/applications/vxml/precompile/catch_shorthand.xml")
    };


    /**
     * Constructor for this test suite, must be called from the testclass that inherits
     * this class through a super(event) call.
     *
     * @param event
     */
    public ApplicationVXMLPrecompile(String event) {
        super(event);
    }

    public static Test suite() {


        TestSuite suite = new TestSuite();

        suite.addTest(new ApplicationVXMLPrecompile("testCatchShorthand"));

        return new TestSetup(suite) {
            protected void setUp() {
                oneTimeSetUp(testCases);
            }

            protected void tearDown() {
                oneTimeTearDown();
            }
        };
    }

    public void testCatchShorthand() throws Exception {
        boolean exited = createCallAndWaitForCompletion("catch_shorthand", 20000);
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        
        lfe.add2LevelRequired(".*\\sTCPASS VXML HELP.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML ERROR.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML NOINPUT.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML NOMATCH.*");

    }
}
