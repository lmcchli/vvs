/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;
import org.jmock.core.Invocation;
import org.jmock.core.InvocationMatcher;

import junit.framework.TestCase;

/**
 * Utilities for JUnit-tests.
 * 
 * @author Jörgen Terner
 */
public final class JUnitUtil {

    private JUnitUtil() {
        // no instances of this class should be created.
    }

    /**
     * Controls that an exception is the expected one. An exception is 
     * expected if <code>actual</code> is of the same class as 
     * <code>expected</code>. No subclasses to <code>expected</code>
     * will be accepted.
     * <p>
     * Example of use:
     * <pre>
     * public void testXXX() {
     *     try {
     *         myComplicatedMethod(null);
     *         fail("null should cause an IllegalArgumentException to be thrown");
     *     }
     *     catch (Exception e)
     *     {
     *         assertException("Unexpected exception when calling " +
     *             "myComplicatedMethod with null",
     *             IllegalArgumentException.class, e);
     *     }
     *     ...
     * }
     * </pre>
     *
     * @param message  Message displayed if the test fails.
     * @param expected Expected exceptionclass.
     * @param actual   Actual exception instance.
     */
    public static void assertException(String message,
       Class expected, Exception actual)
    {
       if (message == null)
       {
           TestCase.fail("assertException(...) message==null");
       }
       if (expected == null)
       {
           TestCase.fail(message + " expected==null");
       }
       if (actual == null)
       {
           TestCase.fail(message + " actual==null");
       }
       if (!expected.equals(actual.getClass()))
       {
           actual.printStackTrace();
           TestCase.fail(message + " actual=" + actual + ":<" + 
                   expected.getName() + "> but was:<" + 
                   actual.getClass().getName() + ">");
       }
    }
    
    /**
     * Gets an invocation matcher that matches the number of made calls 
     * against the given expected count.
     * 
     * @param expectedCount Expected number of calls.
     * 
     * @return A count matcher instance.
     */
    public static InvocationMatcher getCountMatcher(int expectedCount) {
        return new InvokeCountMatcher(expectedCount);
    }
    
    /**
     * Matches the number of made calls against the given expected count.
     * 
     * @author Copied from JMock examples
     */
    public static class InvokeCountMatcher
            implements InvocationMatcher
    {
        int expectedCount;
        int invocationCount = 0;

        public InvokeCountMatcher( int expectedCount ) {
            this.expectedCount = expectedCount;
        }

        public boolean matches( Invocation invocation ) {
            return invocationCount < expectedCount;
        }

        public void verify() {
            Assert.assertTrue("Invoked wrong number of times", expectedCount == invocationCount);
        }

        public boolean hasDescription() {
            return true;
        }

        public StringBuffer describeTo( StringBuffer buffer ) {
            return buffer.append("expected ").append(expectedCount)
                    .append(" times, invoked ").append(invocationCount).append(" times");
        }

        public void invoked( Invocation invocation ) {
            invocationCount++;
        }
    }    

    /**
     * This method creates a copy of an object or an object hierarchy.
     * The copy is made by serialization. After this, there can not be
     * any references between the original object and the object returned.
     *
     * @param obj The object that should be copied.
     *
     * @return A copy of the object. If <code>obj</code> is <code>null</code>,
     *         <code>null</code> is returned.
     */
    public static Object copyObject(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream(512);
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(obj);
            out.close();
            ObjectInputStream in = 
                new ObjectInputStream(new ByteArrayInputStream(stream.toByteArray()));
            return in.readObject();
        }
        catch (Exception e) {
            TestCase.fail("Failed to serialize object: " + e);
            
            // To make the compiler happy, this line is never reached.
            return null;
        }
    }
}
