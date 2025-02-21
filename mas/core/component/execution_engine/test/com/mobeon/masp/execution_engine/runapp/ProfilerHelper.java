/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;
import junit.framework.TestResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class used when profiling through Application* test cases.
 *
 * Usage: java com.mobeon.masp.execution_engine.runapp.ProfileHelper  <TESTCASECLASS>
 *
 * @author David Looberger
 */
public class ProfilerHelper {
    private static final String DEFAULTPACKAGE = "com.mobeon.masp.execution_engine.runapp";
    public static void main(String[] args) {
        // Retrieve the Test case to run
        String testcaseclassStr = null;
        if (args.length > 0)
            testcaseclassStr = args[0];

        if (testcaseclassStr == null) {
            System.out.println("No test case selected");
            return;
        }
        if (!runTest(testcaseclassStr)) {
            System.out.println("Trying using default package name " + DEFAULTPACKAGE);
            runTest(DEFAULTPACKAGE + "." + testcaseclassStr);
        }
        System.exit(0);
    }

    private static boolean runTest(String testclass) {
        try {
            Class testcaseclass = Class.forName(testclass);
            Constructor ctor = testcaseclass.getConstructor(String.class);
            Method method = testcaseclass.getMethod("suite");
            Object obj = ctor.newInstance("");
            Test test = (Test) method.invoke(obj);
            TestResult tr = new TestResult();
            test.run(tr);
            tr.endTest(test);

        }
        catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + testclass);
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
