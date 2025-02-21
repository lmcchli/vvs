/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import com.mobeon.masp.util.Ignore;
import com.mobeon.masp.util.Tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ApplicationTestSetup<T extends ApplicationBasicTestCase> extends TestSetup {

    final String PROPERTY = "com.mobeon.junit.runapp.test";
    private ApplicationBasicTestCase.ApplicationTestCase[] cases;
    private Method beforeSuiteMethod;
    private Method afterSuiteMethod;
    
    public ApplicationTestSetup(Class<T> cls) {
        super(new TestSuite());
        this.cases = ApplicationBasicTestCase.testCaseForClass(cls);
        beforeSuiteMethod =  Tools.Reflection.staticMethod(cls,"beforeSuite");
        afterSuiteMethod =  Tools.Reflection.staticMethod(cls,"afterSuite");
        if(cases == null || cases.length == 0)
            throw new IllegalArgumentException("At least one testcase must be defined, or have you forgot to call store() ?");

        TestSuite suite = (TestSuite)getTest();

        String testMethod = System.getProperty(PROPERTY);
        if(testMethod == null  || testMethod.trim().length() == 0) {
            suite.addTest(new FilteringTestSuite(cls));
        } else {
            try {
                Constructor<T> ctor = cls.getConstructor(String.class);
                suite.addTest(ctor.newInstance("test"+testMethod));
            } catch (NoSuchMethodException e) {
                Ignore.reflectionException(e);
            } catch (IllegalAccessException e) {
                Ignore.reflectionException(e);
            } catch (InvocationTargetException e) {
                Ignore.reflectionException(e);
            } catch (InstantiationException e) {
                Ignore.reflectionException(e);
            }
            if(suite.countTestCases() == 0)
                fail("Test method specified with -D"+PROPERTY+"="+testMethod+" was not found in "+cls.getName());
        }
    }

    public void run(TestResult testResult) {
        super.run(testResult);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void basicRun(TestResult testResult) {
        super.basicRun(testResult);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void setUp() throws Exception {
        Tools.Reflection.call(beforeSuiteMethod);
        ApplicationBasicTestCase.oneTimeSetUp(cases);
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        Tools.Reflection.call(afterSuiteMethod);
        ApplicationBasicTestCase.oneTimeTearDown();
    }
}
