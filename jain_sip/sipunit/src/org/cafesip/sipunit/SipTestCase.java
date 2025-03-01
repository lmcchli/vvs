/*
 * Created on Feb 19, 2005
 * 
 * Copyright 2005 CafeSip.org 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *	http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 */
package org.cafesip.sipunit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.message.Request;

import junit.framework.TestCase;

/**
 * The SipTestCase class provides a test environment and set of assert methods
 * geared toward unit testing Java SIP applications. It uses the JUnit test
 * framework and API (http://junit.sourceforge.net) and extends
 * junit.framework.TestCase. A SIP application test program (a) uses the SipUnit
 * API to invoke SIP operations against a test target and (b) uses the assert
 * methods of this class in addition to any of the JUnit Assert methods to
 * verify the results of the test.
 * <p>
 * 
 * To write a SIP application test class, or program: <br>
 * 
 * <li>1) implement a subclass of SipTestCase <br>
 * <li>2) define instance attributes that store information needed by each test
 * in the class <br>
 * <li>3) initialize the setup for each test by overriding method setUp() <br>
 * <li>4) clean-up after each test by overriding tearDown() <br>
 * <li>5) write one or more tests, each of which is a method beginning with the
 * letters "test"
 * <p>
 * 
 * The code in the SIP application test class creates SipUnit API objects, calls
 * their methods to set up and initiate action toward a SIP test target, and
 * verifies the results involving the test target using the assert methods of
 * this class and the standard JUnit assert methods. Messages are only displayed
 * when an assert fails. See below for an example test class.
 * <p>
 * 
 * To execute the test program and run the tests, use the standard JUnit test
 * runners which can run a test suite and collect/output the results. This is
 * commonly done using IDE Junit facilities or via the Ant junit target.
 * <p>
 * EXAMPLE SIP APPLICATION TEST CLASS: (see the SipUnit User Guide for more
 * information on how to use the API effectively)
 * 
 * <pre><code>
 * 
 *  
 *   
 *    
 *     
 *      
 *       
 *        
 *             import org.cafesip.sipunit.*;
 *             
 *             public class TestNoProxy extends SipTestCase 
 *             {
 *              private SipStack sipStack;
 *              private SipPhone ua;
 *              private String thisHostAddr = &quot;127.0.0.1&quot;;
 *              
 *              public TestNoProxy(String arg0)
 *              {
 *                  super(arg0);
 *              }
 *                       
 *              public void setUp() throws Exception
 *              {
 *                  sipStack = new SipStack(null, -1);
 *                  ua = sipStack.createSipPhone(&quot;sip:amit@nist.gov&quot;);
 *              }
 *              
 *              public void tearDown() throws Exception
 *              {
 *                  ua.dispose();
 *                  sipStack.dispose();
 *              }
 *              
 *              public void testBothSides() 
 *              // test target: SipUnit API classes, incoming and outgoing SIP handling (a calls b)
 *              {
 *                  try
 *                  {
 *                      SipPhone ub = sipStack.createSipPhone(&quot;sip:becky@nist.gov&quot;);
 *                       
 *                      SipCall a = ua.createSipCall();
 *                      SipCall b = ub.createSipCall();
 *                       
 *                      b.listenForIncomingCall(); // non-blocking
 *                      Thread.sleep(5);
 *                       
 *                      a.initiateOutgoingCall(&quot;sip:becky@nist.gov&quot;, thisHostAddr
 *                              + &quot;:5060/UDP&quot;);
 *                      assertLastOperationSuccess(&quot;a initiate call - &quot; + a.format(), a);
 *                       
 *                      b.waitForIncomingCall(10000); // blocking
 *                      assertLastOperationSuccess(&quot;b wait incoming call - &quot; + b.format(),
 *                              b);
 *                       
 *                      b.sendIncomingCallResponse(Response.RINGING, null, -1);
 *                      assertLastOperationSuccess(&quot;b send RINGING - &quot; + b.format(), b);
 *                       
 *                      Thread.sleep(1000);
 *                       
 *                      b.sendIncomingCallResponse(Response.OK, &quot;Answer - Hello world&quot;, 0);
 *                      assertLastOperationSuccess(&quot;b send OK - &quot; + b.format(), b);
 *                       
 *                      a.waitOutgoingCallResponse(10000); // block waiting for a response
 *                      assertLastOperationSuccess(&quot;a wait 1st response - &quot; + a.format(), a);
 *                      assertEquals(&quot;Unexpected 1st response received&quot;, Response.RINGING,
 *                              a.getReturnCode());
 *                      assertNotNull(&quot;Default response reason not sent&quot;, a
 *                              .getLastReceivedResponse().getReasonPhrase());
 *                      assertEquals(&quot;Unexpected default reason&quot;, &quot;Ringing&quot;, a
 *                              .getLastReceivedResponse().getReasonPhrase());
 *                       
 *                      a.waitOutgoingCallResponse(10000); // block waiting for next response
 *                      assertLastOperationSuccess(&quot;a wait 2nd response - &quot; + a.format(), a);
 *                       
 *                      assertEquals(&quot;Unexpected 2nd response received&quot;, Response.OK, a
 *                              .getReturnCode());
 *                       
 *                      a.sendInviteOkAck();
 *                      assertLastOperationSuccess(&quot;Failure sending ACK - &quot; + a.format(), a);
 *                       
 *                      Thread.sleep(1000);
 *                       
 *                      a.listenForDisconnect(); // non-blocking
 *                      assertLastOperationSuccess(&quot;a listen disc - &quot; + a.format(), a);
 *                       
 *                      b.disconnect();
 *                      assertLastOperationSuccess(&quot;b disc - &quot; + b.format(), b);
 *                       
 *                      a.waitForDisconnect(5000); // blocking
 *                      assertLastOperationSuccess(&quot;a wait disc - &quot; + a.format(), a);
 *                       
 *                      a.respondToDisconnect();
 *                      assertLastOperationSuccess(&quot;a respond to disc - &quot; + a.format(), a);
 *                       
 *                      ub.dispose();
 *                  }
 *                  catch (Exception e)
 *                  {
 *                      fail(&quot;Exception: &quot; + e.getClass().getName() + &quot;: &quot; + e.getMessage());
 *                  }
 *              }
 *         
 *        
 *       
 *      
 *     
 *    
 *   
 *  
 * </code></pre>
 * 
 * @author aab
 *  
 */
public class SipTestCase extends TestCase
{
    /**
     * A constructor for this test case.
     * 
     * @param arg0
     *            the name of the test case
     */
    public SipTestCase(String arg0)
    {
        super(arg0);
    }

    /**
     * A no-arg constructor to enable serialization. Call setName() if you use
     * this.
     */
    public SipTestCase()
    {
        super();
    }

    /**
     * Sets up the environment for each test method prior to execution.
     */
    public void setUp() throws Exception
    {
    }

    /**
     * Cleans up from the test method just executed.
     */
    public void tearDown() throws Exception
    {
    }

    /**
     * Asserts that the last SIP operation performed by the given object was
     * successful.
     * 
     * @param op
     *            the SipUnit object that executed an operation.
     */
    public void assertLastOperationSuccess(SipActionObject op)
    {
        assertLastOperationSuccess(null, op);
    }

    /**
     * Asserts that the last SIP operation performed by the given object was
     * successful. Assertion failure output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param op
     *            the SipUnit object that executed an operation.
     */
    public void assertLastOperationSuccess(String msg, SipActionObject op)
    {
        assertNotNull("Null assert object passed in", op);
        assertTrue(msg, op.getErrorMessage().length() == 0);
    }

    /**
     * Asserts that the last SIP operation performed by the given object failed.
     * 
     * @param op
     *            the SipUnit object that executed an operation.
     */
    public void assertLastOperationFail(SipActionObject op)
    {
        assertLastOperationFail(null, op);
    }

    /**
     * Asserts that the last SIP operation performed by the given object failed.
     * Assertion failure output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param op
     *            the SipUnit object that executed an operation.
     */
    public void assertLastOperationFail(String msg, SipActionObject op)
    {
        assertNotNull("Null assert object passed in", op);
        assertTrue(msg, op.getErrorMessage().length() > 0);
    }

    /**
     * Asserts that the given SIP message contains at least one occurrence of
     * the specified header.
     * 
     * @param sipMessage
     *            the SIP message.
     * @param header
     *            the string identifying the header, as specified in RFC-3261.
     *  
     */
    public void assertHeaderPresent(SipMessage sipMessage, String header)
    {
        assertHeaderPresent(null, sipMessage, header); //header is case
        // sensitive?
    }

    /**
     * Asserts that the given SIP message contains at least one occurrence of
     * the specified header. Assertion failure output includes the given message
     * text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param sipMessage
     *            the SIP message.
     * @param header
     *            the string identifying the header as specified in RFC-3261.
     */
    public void assertHeaderPresent(String msg, SipMessage sipMessage,
            String header)
    {
        assertNotNull("Null assert object passed in", sipMessage);
        assertTrue(msg, sipMessage.getHeaders(header).hasNext());
    }

    /**
     * Asserts that the given SIP message contains no occurrence of the
     * specified header.
     * 
     * @param sipMessage
     *            the SIP message.
     * @param header
     *            the string identifying the header as specified in RFC-3261.
     */
    public void assertHeaderNotPresent(SipMessage sipMessage, String header)
    {
        assertHeaderNotPresent(null, sipMessage, header);
    }

    /**
     * Asserts that the given SIP message contains no occurrence of the
     * specified header. Assertion failure output includes the given message
     * text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param sipMessage
     *            the SIP message.
     * @param header
     *            the string identifying the header as specified in RFC-3261.
     */
    public void assertHeaderNotPresent(String msg, SipMessage sipMessage,
            String header)
    {
        assertNotNull("Null assert object passed in", sipMessage);
        assertFalse(msg, sipMessage.getHeaders(header).hasNext());
    }

    /**
     * Asserts that the given SIP message contains at least one occurrence of
     * the specified header and that at least one occurrence of this header
     * contains the given value. The assertion fails if no occurrence of the
     * header contains the value or if the header is not present in the mesage.
     * 
     * @param sipMessage
     *            the SIP message.
     * @param header
     *            the string identifying the header as specified in RFC-3261.
     * @param value
     *            the string value within the header to look for. An exact
     *            string match is done against the entire contents of the
     *            header. The assertion will pass if any part of the header
     *            matches the value given.
     */
    public void assertHeaderContains(SipMessage sipMessage, String header,
            String value)
    {
        assertHeaderContains(null, sipMessage, header, value); // value is case
        // sensitive?
    }

    /**
     * Asserts that the given SIP message contains at least one occurrence of
     * the specified header and that at least one occurrence of this header
     * contains the given value. The assertion fails if no occurrence of the
     * header contains the value or if the header is not present in the mesage.
     * Assertion failure output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param sipMessage
     *            the SIP message.
     * @param header
     *            the string identifying the header as specified in RFC-3261.
     * @param value
     *            the string value within the header to look for. An exact
     *            string match is done against the entire contents of the
     *            header. The assertion will pass if any part of the header
     *            matches the value given.
     */
    public void assertHeaderContains(String msg, SipMessage sipMessage,
            String header, String value)
    {
        assertNotNull("Null assert object passed in", sipMessage);
        ListIterator l = sipMessage.getHeaders(header);
        while (l.hasNext())
        {
            String h = ((Header) l.next()).toString();

            if (h.indexOf(value) != -1)
            {
                assertTrue(true);
                return;
            }
        }

        fail(msg);
    }

    /**
     * Asserts that the given SIP message contains no occurrence of the
     * specified header with the value given, or that there is no occurrence of
     * the header in the message. The assertion fails if any occurrence of the
     * header contains the value.
     * 
     * @param sipMessage
     *            the SIP message.
     * @param header
     *            the string identifying the header as specified in RFC-3261.
     * @param value
     *            the string value within the header to look for. An exact
     *            string match is done against the entire contents of the
     *            header. The assertion will fail if any part of the header
     *            matches the value given.
     */
    public void assertHeaderNotContains(SipMessage sipMessage, String header,
            String value)
    {
        assertHeaderNotContains(null, sipMessage, header, value);
    }

    /**
     * Asserts that the given SIP message contains no occurrence of the
     * specified header with the value given, or that there is no occurrence of
     * the header in the message. The assertion fails if any occurrence of the
     * header contains the value. Assertion failure output includes the given
     * message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param sipMessage
     *            the SIP message.
     * @param header
     *            the string identifying the header as specified in RFC-3261.
     * @param value
     *            the string value within the header to look for. An exact
     *            string match is done against the entire contents of the
     *            header. The assertion will fail if any part of the header
     *            matches the value given.
     */
    public void assertHeaderNotContains(String msg, SipMessage sipMessage,
            String header, String value)
    {
        assertNotNull("Null assert object passed in", sipMessage);
        ListIterator l = sipMessage.getHeaders(header);
        while (l.hasNext())
        {
            String h = ((Header) l.next()).toString();

            if (h.indexOf(value) != -1)
            {
                fail(msg);
            }
        }

        assertTrue(true);
    }

    /**
     * Asserts that the given message listener object received a response with
     * the indicated status code.
     * 
     * @param statusCode
     *            The response status code to check for (eg,
     *            SipResponse.RINGING)
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertResponseReceived(int statusCode, MessageListener obj)
    {
        assertResponseReceived(null, statusCode, obj);
    }

    /**
     * Asserts that the given message listener object received a response with
     * the indicated status code, CSeq method and CSeq sequence number.
     * 
     * @param statusCode
     *            The response status code to check for (eg,
     *            SipResponse.RINGING)
     * @param method
     *            The CSeq method to look for (SipRequest.INVITE, etc.)
     * @param sequenceNumber
     *            The CSeq sequence number to look for
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertResponseReceived(int statusCode, String method,
            long sequenceNumber, MessageListener obj)
    {
        assertResponseReceived(null, statusCode, method, sequenceNumber, obj);
    }

    /**
     * Asserts that the given message listener object received a response with
     * the indicated status code. Assertion failure output includes the given
     * message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param statusCode
     *            The response status code to check for (eg,
     *            SipResponse.RINGING)
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertResponseReceived(String msg, int statusCode,
            MessageListener obj)
    {
        assertNotNull("Null assert object passed in", obj);
        assertTrue(msg, responseReceived(statusCode, obj));
    }

    private boolean responseReceived(int statusCode, MessageListener obj)
    {
        ArrayList responses = obj.getAllReceivedResponses();

        Iterator i = responses.iterator();
        while (i.hasNext())
        {
            int response_code = ((SipResponse) i.next()).getStatusCode();
            if (response_code == statusCode)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Asserts that the given message listener object received a response with
     * the indicated status code, CSeq method and CSeq sequence number.
     * Assertion failure output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param statusCode
     *            The response status code to check for (eg,
     *            SipResponse.RINGING)
     * @param method
     *            The CSeq method to look for (SipRequest.INVITE, etc.)
     * @param sequenceNumber
     *            The CSeq sequence number to look for
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertResponseReceived(String msg, int statusCode,
            String method, long sequenceNumber, MessageListener obj)
    {
        assertNotNull("Null assert object passed in", obj);
        assertTrue(msg, responseReceived(statusCode, method, sequenceNumber,
                obj));
    }

    private boolean responseReceived(int statusCode, String method,
            long sequenceNumber, MessageListener obj)
    {
        ArrayList responses = obj.getAllReceivedResponses();

        Iterator i = responses.iterator();
        while (i.hasNext())
        {
            SipResponse resp = (SipResponse) i.next();
            if (resp.getStatusCode() == statusCode)
            {
                CSeqHeader hdr = (CSeqHeader) resp.getMessage().getHeader(
                        CSeqHeader.NAME);
                if (hdr != null)
                {
                    if (hdr.getMethod().equals(method))
                    {
                        if (hdr.getSeqNumber() == sequenceNumber)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Asserts that the given message listener object has not received a
     * response with the indicated status code.
     * 
     * @param statusCode
     *            The response status code to verify absent (eg,
     *            SipResponse.RINGING)
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     *  
     */
    public void assertResponseNotReceived(int statusCode, MessageListener obj)
    {
        assertResponseNotReceived(null, statusCode, obj);
    }

    /**
     * Asserts that the given message listener object has not received a
     * response with the indicated status code. Assertion failure output
     * includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param statusCode
     *            The response status code to verify absent (eg,
     *            SipResponse.RINGING)
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertResponseNotReceived(String msg, int statusCode,
            MessageListener obj)
    {
        assertNotNull("Null assert object passed in", obj);
        assertFalse(msg, responseReceived(statusCode, obj));
    }

    /**
     * Asserts that the given message listener object has not received a
     * response with the indicated status code, CSeq method and sequence number.
     * 
     * @param statusCode
     *            The response status code to verify absent (eg,
     *            SipResponse.RINGING)
     * @param method
     *            The CSeq method to verify absent (SipRequest.INVITE, etc.)
     * @param sequenceNumber
     *            The CSeq sequence number to verify absent
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     *  
     */
    public void assertResponseNotReceived(int statusCode, String method,
            long sequenceNumber, MessageListener obj)
    {
        assertResponseNotReceived(null, statusCode, method, sequenceNumber, obj);
    }

    /**
     * Asserts that the given message listener object has not received a
     * response with the indicated status code, CSeq method and sequence number.
     * Assertion failure output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param statusCode
     *            The response status code to verify absent (eg,
     *            SipResponse.RINGING)
     * @param method
     *            The CSeq method to verify absent (SipRequest.INVITE, etc.)
     * @param sequenceNumber
     *            The CSeq sequence number to verify absent
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertResponseNotReceived(String msg, int statusCode,
            String method, long sequenceNumber, MessageListener obj)
    {
        assertNotNull("Null assert object passed in", obj);
        assertFalse(msg, responseReceived(statusCode, method, sequenceNumber,
                obj));
    }

    /**
     * Asserts that the given message listener object received a request with
     * the indicated request method.
     * 
     * @param method
     *            The request method to check for (eg, SipRequest.BYE)
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertRequestReceived(String method, MessageListener obj)
    {
        assertRequestReceived(null, method, obj);
    }

    /**
     * Asserts that the given message listener object received a request with
     * the indicated CSeq method and CSeq sequence number.
     * 
     * @param method
     *            The CSeq method to look for (SipRequest.REGISTER, etc.)
     * @param sequenceNumber
     *            The CSeq sequence number to look for
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertRequestReceived(String method, long sequenceNumber,
            MessageListener obj)
    {
        assertRequestReceived(null, method, sequenceNumber, obj);
    }

    /**
     * Asserts that the given message listener object received a request with
     * the indicated request method. Assertion failure output includes the given
     * message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param method
     *            The request method to check for (eg, SipRequest.INVITE)
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertRequestReceived(String msg, String method,
            MessageListener obj)
    {
        assertNotNull("Null assert object passed in", obj);
        assertTrue(msg, requestReceived(method, obj));
    }

    private boolean requestReceived(String method, MessageListener obj)
    {
        ArrayList requests = obj.getAllReceivedRequests();

        Iterator i = requests.iterator();
        while (i.hasNext())
        {
            Request req = (Request) ((SipRequest) i.next()).getMessage();
            if (req != null)
            {
                if (req.getMethod().equals(method))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Asserts that the given message listener object received a request with
     * the indicated CSeq method and CSeq sequence number. Assertion failure
     * output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param method
     *            The CSeq method to look for (SipRequest.INVITE, etc.)
     * @param sequenceNumber
     *            The CSeq sequence number to look for
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertRequestReceived(String msg, String method,
            long sequenceNumber, MessageListener obj)
    {
        assertNotNull("Null assert object passed in", obj);
        assertTrue(msg, requestReceived(method, sequenceNumber, obj));
    }

    private boolean requestReceived(String method, long sequenceNumber,
            MessageListener obj)
    {
        ArrayList requests = obj.getAllReceivedRequests();

        Iterator i = requests.iterator();
        while (i.hasNext())
        {
            Request req = (Request) ((SipRequest) i.next()).getMessage();
            if (req != null)
            {
                CSeqHeader hdr = (CSeqHeader) req.getHeader(CSeqHeader.NAME);
                if (hdr != null)
                {
                    if (hdr.getMethod().equals(method))
                    {
                        if (hdr.getSeqNumber() == sequenceNumber)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Asserts that the given message listener object has not received a request
     * with the indicated request method.
     * 
     * @param method
     *            The request method to verify absent (eg, SipRequest.BYE)
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     *  
     */
    public void assertRequestNotReceived(String method, MessageListener obj)
    {
        assertRequestNotReceived(null, method, obj);
    }

    /**
     * Asserts that the given message listener object has not received a request
     * with the indicated request method. Assertion failure output includes the
     * given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param method
     *            The request method to verify absent (eg, SipRequest.BYE)
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertRequestNotReceived(String msg, String method,
            MessageListener obj)
    {
        assertNotNull("Null assert object passed in", obj);
        assertFalse(msg, requestReceived(method, obj));
    }

    /**
     * Asserts that the given message listener object has not received a request
     * with the indicated CSeq method and sequence number.
     * 
     * @param method
     *            The CSeq method to verify absent (SipRequest.INVITE, etc.)
     * @param sequenceNumber
     *            The CSeq sequence number to verify absent
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     *  
     */
    public void assertRequestNotReceived(String method, long sequenceNumber,
            MessageListener obj)
    {
        assertRequestNotReceived(null, method, sequenceNumber, obj);
    }

    /**
     * Asserts that the given message listener object has not received a request
     * with the indicated CSeq method and sequence number. Assertion failure
     * output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param method
     *            The CSeq method to verify absent (SipRequest.INVITE, etc.)
     * @param sequenceNumber
     *            The CSeq sequence number to verify absent
     * @param obj
     *            The MessageListener object (ie, SipCall, Subscription, etc.).
     */
    public void assertRequestNotReceived(String msg, String method,
            long sequenceNumber, MessageListener obj)
    {
        assertNotNull("Null assert object passed in", obj);
        assertFalse(msg, requestReceived(method, sequenceNumber, obj));
    }

    /**
     * Asserts that the given incoming or outgoing call leg was answered.
     * 
     * @param call
     *            The incoming or outgoing call leg.
     */
    public void assertAnswered(SipCall call)
    {
        assertAnswered(null, call);
    }

    /**
     * Asserts that the given incoming or outgoing call leg was answered.
     * Assertion failure output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param call
     *            The incoming or outgoing call leg.
     */
    public void assertAnswered(String msg, SipCall call)
    {
        assertNotNull("Null assert object passed in", call);
        assertTrue(msg, call.isCallAnswered());
    }

    /**
     * Asserts that the given incoming or outgoing call leg has not been
     * answered.
     * 
     * @param call
     *            The incoming or outgoing call leg.
     */
    public void assertNotAnswered(SipCall call)
    {
        assertNotAnswered(null, call);
    }

    /**
     * Asserts that the given incoming or outgoing call leg has not been
     * answered. Assertion failure output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param call
     *            The incoming or outgoing call leg.
     */
    public void assertNotAnswered(String msg, SipCall call)
    {
        assertNotNull("Null assert object passed in", call);
        assertFalse(msg, call.isCallAnswered());
    }

    /**
     * Asserts that the given SIP message contains a body.
     * 
     * @param sipMessage
     *            the SIP message.
     */
    public void assertBodyPresent(SipMessage sipMessage)
    {
        assertBodyPresent(null, sipMessage);
    }

    /**
     * Asserts that the given SIP message contains a body. Assertion failure
     * output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param sipMessage
     *            the SIP message.
     */
    public void assertBodyPresent(String msg, SipMessage sipMessage)
    {
        assertNotNull("Null assert object passed in", sipMessage);
        assertTrue(msg, sipMessage.getContentLength() > 0);
    }

    /**
     * Asserts that the given SIP message contains no body.
     * 
     * @param sipMessage
     *            the SIP message.
     */
    public void assertBodyNotPresent(SipMessage sipMessage)
    {
        assertBodyNotPresent(null, sipMessage);
    }

    /**
     * Asserts that the given SIP message contains no body. Assertion failure
     * output includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param sipMessage
     *            the SIP message.
     */
    public void assertBodyNotPresent(String msg, SipMessage sipMessage)
    {
        assertNotNull("Null assert object passed in", sipMessage);
        assertFalse(msg, sipMessage.getContentLength() > 0);
    }

    /**
     * Asserts that the given SIP message contains a body that includes the
     * given value. The assertion fails if a body is not present in the message
     * or is present but doesn't include the value.
     * 
     * @param sipMessage
     *            the SIP message.
     * @param value
     *            the string value to look for in the body. An exact string
     *            match is done against the entire contents of the body. The
     *            assertion will pass if any part of the body matches the value
     *            given. ??case sensitive?
     */
    public void assertBodyContains(SipMessage sipMessage, String value)
    {
        assertBodyContains(null, sipMessage, value);
    }

    /**
     * Asserts that the given SIP message contains a body that includes the
     * given value. The assertion fails if a body is not present in the message
     * or is present but doesn't include the value. Assertion failure output
     * includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param sipMessage
     *            the SIP message.
     * @param value
     *            the string value to look for in the body. An exact string
     *            match is done against the entire contents of the body. The
     *            assertion will pass if any part of the body matches the value
     *            given.
     */
    public void assertBodyContains(String msg, SipMessage sipMessage,
            String value)
    {
        assertNotNull("Null assert object passed in", sipMessage);
        assertBodyPresent(msg, sipMessage);
        String body = new String(sipMessage.getRawContent());

        if (body.indexOf(value) != -1)
        {
            assertTrue(true);
            return;
        }

        fail(msg);
    }

    /**
     * Asserts that the body in the given SIP message does not contain the value
     * given, or that there is no body in the message. The assertion fails if
     * the body is present and contains the value.
     * 
     * @param sipMessage
     *            the SIP message.
     * @param value
     *            the string value to look for in the body. An exact string
     *            match is done against the entire contents of the body. The
     *            assertion will fail if any part of the body matches the value
     *            given.
     */
    public void assertBodyNotContains(SipMessage sipMessage, String value)
    {
        assertBodyNotContains(null, sipMessage, value);
    }

    /**
     * Asserts that the body in the given SIP message does not contain the value
     * given, or that there is no body in the message. The assertion fails if
     * the body is present and contains the value. Assertion failure output
     * includes the given message text.
     * 
     * @param msg
     *            message text to output if the assertion fails.
     * @param sipMessage
     *            the SIP message.
     * @param value
     *            the string value to look for in the body. An exact string
     *            match is done against the entire contents of the body. The
     *            assertion will fail if any part of the body matches the value
     *            given.
     */
    public void assertBodyNotContains(String msg, SipMessage sipMessage,
            String value)
    {
        assertNotNull("Null assert object passed in", sipMessage);
        if (sipMessage.getContentLength() > 0)
        {
            String body = new String(sipMessage.getRawContent());

            if (body.indexOf(value) != -1)
            {
                fail(msg);
            }
        }

        assertTrue(true);
    }

    /**
     * Asserts that the given Subscription has not encountered any errors while
     * processing received SUBSCRIBE responses and received NOTIFY requests. If
     * the assertion fails, the encountered error(s) are included in the failure 
     * output.
     * 
     * @param subscription
     *            the Subscription in question.
     */
    public void assertNoPresenceErrors(Subscription subscription)
    {
        assertNoPresenceErrors(null, subscription);
    }

    /**
     * Asserts that the given Subscription has not encountered any errors while
     * processing received SUBSCRIBE responses and received NOTIFY requests.
     * Assertion failure output includes the given message text along with the
     * encountered error(s).
     * 
     * @param msg
     *            message text to include if the assertion fails.
     * @param subscription
     *            the Subscription in question.
     */
    public void assertNoPresenceErrors(String msg, Subscription subscription)
    {
        assertNotNull("Null assert object passed in", subscription);
        
        StringBuffer buf = new StringBuffer(msg == null ? "Presence error(s)" : msg);
        Iterator i = subscription.getEventErrors().iterator();
        while (i.hasNext())
        {
            buf.append(" : ");
            buf.append((String) i.next());
        }
        
        assertEquals(buf.toString(), 0, subscription.getEventErrors().size());
    }

    // Later: ContentDispositionHeader, ContentEncodingHeader,
    // ContentLanguageHeader,
    // ContentLengthHeader, ContentTypeHeader, MimeVersionHeader

    /*
     * From seeing how to remove our stuff from the failure stack:
     * 
     * catch (AssertionFailedError e) // adjust stack trace { ArrayList stack =
     * new ArrayList(); StackTraceElement[] t = e.getStackTrace(); String
     * thisclass = this.getClass().getName(); for (int i = 0; i < t.length; i++) {
     * StackTraceElement ele = t[i]; System.out.println("Element = " +
     * ele.toString()); if (thisclass.equals(ele.getClass().getName()) == true) {
     * continue; } stack.add(t[i]); }
     * 
     * StackTraceElement[] new_stack = new StackTraceElement[stack.size()];
     * e.setStackTrace((StackTraceElement[]) stack.toArray(new_stack)); throw e; }
     */

    /*
     * public class Arguments { public static void notNull(Object arg) { if(arg ==
     * null) { IllegalArgumentException t = new IllegalArgumentException();
     * reorient(t); throw t; } } private static void reorient(Throwable t) {
     * StackTraceElement[] elems = t.getStackTrace(); StackTraceElement[]
     * subElems = new StackTraceElement[elems.length-1]; System.arrayCopy(elems,
     * 1, subElems, 0, elems.length-1); t.setStackTrace(t); } }
     *  
     */

}