/*
 * Created on Aug 17, 2005
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
package org.cafesip.sipunit.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;

/**
 * This class tests some SipUnit API methods.
 * 
 * Tests in this class require that a Proxy/registrar server be running with
 * authentication turned off. Defaults: proxy host = 127.0.0.1, port = 4000,
 * protocol = udp; user amit password a1b2c3d4 and user becky password a1b2c3d4
 * defined at the proxy.
 * 
 * For the Proxy/registrar, I used JAIN-SIP Proxy for the People!
 */

public class ExampleTestWithProxyNoAuth extends SipTestCase
{
    private SipStack sipStack;

    private SipPhone ua;

    private int proxyPort;

    private int myPort;

    private String testProtocol;

    private String myUrl;

    private static final Properties defaultProperties = new Properties();
    static
    {
        String host = null;
        try
        {
            host = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            host = "localhost";
        }

        defaultProperties.setProperty("javax.sip.IP_ADDRESS", host);
        defaultProperties.setProperty("javax.sip.STACK_NAME", "testAgent");
        defaultProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");
        defaultProperties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "testAgent_debug.txt");
        defaultProperties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "testAgent_log.txt");
        defaultProperties
                .setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
        defaultProperties.setProperty(
                "gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");

        defaultProperties.setProperty("sipunit.trace", "true");
        defaultProperties.setProperty("sipunit.test.port", "5060");
        defaultProperties.setProperty("sipunit.test.protocol", "udp");

        defaultProperties.setProperty("sipunit.test.domain", "nist.gov");
        defaultProperties.setProperty("sipunit.proxy.host", "127.0.0.1");
        defaultProperties.setProperty("sipunit.proxy.port", "4000");
    }

    private Properties properties = new Properties(defaultProperties);

    public ExampleTestWithProxyNoAuth(String arg0)
    {
        super(arg0);
        properties.putAll(System.getProperties());

        try
        {
            myPort = Integer.parseInt(properties
                    .getProperty("sipunit.test.port"));
        }
        catch (NumberFormatException e)
        {
            myPort = 5061;
        }

        try
        {
            proxyPort = Integer.parseInt(properties
                    .getProperty("sipunit.proxy.port"));
        }
        catch (NumberFormatException e)
        {
            proxyPort = 5060;
        }

        testProtocol = properties.getProperty("sipunit.test.protocol");
        myUrl = "sip:amit@" + properties.getProperty("sipunit.test.domain");
    }

    /*
     * @see SipTestCase#setUp()
     */
    public void setUp() throws Exception
    {
        sipStack = new SipStack(testProtocol, myPort, properties);
        SipStack.setTraceEnabled(properties.getProperty("sipunit.trace")
                .equalsIgnoreCase("true")
                || properties.getProperty("sipunit.trace").equalsIgnoreCase(
                        "on"));
        ua = sipStack.createSipPhone(properties
                .getProperty("sipunit.proxy.host"), testProtocol, proxyPort,
                myUrl);
    }

    /*
     * @see SipTestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        ua.dispose();
        sipStack.dispose();
    }

    /**
     * This test illustrates usage of SipTestCase. In it, user a calls user b,
     * user b sends RINGING and OK, the test verifies these are received by user
     * a, then the call proceeds through disconnect (BYE).
     */
    public void testBothSidesCallerDisc()
    {
        // invoke the Sip operation, then separately check positive result;
        // include all error details in output (via ua.format()) if the test
        // fails:

        ua.register(null, 1800);
        assertLastOperationSuccess("Caller registration failed - "
                + ua.format(), ua);

        try
        {
            String user_b = "sip:becky@"
                + properties.getProperty("sipunit.test.domain");
            SipPhone ub = sipStack.createSipPhone(properties
                    .getProperty("sipunit.proxy.host"), testProtocol,
                    proxyPort, user_b);

            // invoke the Sip operation, then separately check positive result;
            // no failure/error details, just the standard JUnit fail output:

            ub.register(null, 600);
            assertLastOperationSuccess(ub);

            SipCall a = ua.createSipCall();
            SipCall b = ub.createSipCall();

            b.listenForIncomingCall();
            Thread.sleep(10);

            // another way to invoke the operation and check the result
            // separately:

            boolean status_ok = a.initiateOutgoingCall(user_b,
                    null);
            assertTrue("Initiate outgoing call failed - " + a.format(),
                    status_ok);

            // invoke the Sip operation and check positive result in one step,
            // no operation error details if the test fails:

            assertTrue("Wait incoming call error or timeout", b
                    .waitForIncomingCall(5000));

            // invoke the Sip operation and result check in one step,
            // only standard JUnit output if the test fails:

            assertTrue(b.sendIncomingCallResponse(Response.RINGING, "Ringing",
                    0));

            Thread.sleep(1000);

            // although the 2-step method is not as compact, it's easier
            // to follow what a test is doing since the Sip operations are not
            // buried as parameters in assert statements:

            b.sendIncomingCallResponse(Response.OK, "Answer - Hello world", 0);
            assertLastOperationSuccess("Sending answer response failed - "
                    + b.format(), b);

            // note with the single step method, you cannot include operation
            // error details for when the test fails: ' + a.format()' wouldn't
            // work in the first parameter here:

            assertTrue("Wait response error", a.waitOutgoingCallResponse(10000));

            SipResponse resp = a.getLastReceivedResponse(); // watch for TRYING
            int status_code = resp.getStatusCode();
            while (status_code != Response.RINGING)
            {
                assertFalse("Unexpected final response, status = "
                        + status_code, status_code > 200);

                assertFalse("Got OK but no RINGING", status_code == Response.OK);

                a.waitOutgoingCallResponse(10000);
                assertLastOperationSuccess(
                        "Subsequent response never received - " + a.format(), a);
                resp = a.getLastReceivedResponse();
                status_code = resp.getStatusCode();
            }

            // if you want operation error details in your test fail output,
            // you have to invoke and complete the operation first:

            a.waitOutgoingCallResponse(10000);
            assertLastOperationSuccess("Wait response error - " + a.format(), a);

            // throw out any 'TRYING' responses
            // Note, you can also get the response status code from the SipCall
            // class itself (in addition to getting it from the response as
            // above)
            while (a.getReturnCode() == Response.TRYING)
            {
                a.waitOutgoingCallResponse(10000);
                assertLastOperationSuccess(
                        "Subsequent response never received - " + a.format(), a);
            }
            resp = a.getLastReceivedResponse();

            // check for OK response.
            assertEquals("Unexpected response received", Response.OK, a
                    .getReturnCode());

            // check out some header asserts
            assertHeaderContains(resp, "From", myUrl);
            assertHeaderNotContains(resp, "From", myUrl + 'm');
            assertHeaderPresent(resp, "CSeq");
            assertHeaderNotPresent(resp, "Content-Type");

            // continue with the test call
            a.sendInviteOkAck();
            assertLastOperationSuccess("Failure sending ACK - " + a.format(), a);

            Thread.sleep(1000);

            b.listenForDisconnect();
            assertLastOperationSuccess("b listen disc - " + b.format(), b);

            a.disconnect();
            assertLastOperationSuccess("a disc - " + a.format(), a);

            b.waitForDisconnect(10000);
            assertLastOperationSuccess("b wait disc - " + b.format(), b);

            b.respondToDisconnect();
            assertLastOperationSuccess("b disc - " + b.format(), b);

            ub.unregister(null, 10000);
            assertLastOperationSuccess("unregistering user b - " + ub.format(),
                    ub);
        }
        catch (Exception e)
        {
            fail("Exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
