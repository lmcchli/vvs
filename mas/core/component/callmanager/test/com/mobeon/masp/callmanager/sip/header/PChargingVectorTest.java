package com.mobeon.masp.callmanager.sip.header;

import org.jmock.MockObjectTestCase;
import gov.nist.javax.sip.parser.StringMsgParser;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;
import gov.nist.javax.sip.message.SIPMessage;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;

/**
 * History-Info header Tester.
 *
 */
public class PChargingVectorTest extends MockObjectTestCase
{

    private static StringMsgParser stringMsgParser = new StringMsgParser();

    public PChargingVectorTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
        super.setUp();

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }


    private PChargingVectorHeader getPChargingVectorHeader(String request)
            throws Exception
    {
        SIPMessage msg = PChargingVectorTest.stringMsgParser.parseSIPMessage(request);
        return (PChargingVectorHeader)msg.getHeader(PChargingVectorHeader.NAME);
    }

    /**
     * Test getter for Privacy header values
     * @throws Exception
     */
    public void testPChargingVector() throws Exception {
        PChargingVectorHeader pcv;

        pcv = getPChargingVectorHeader(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "P-Charging-Vector: term-ioi=PC123;icid-value=1jhdgv-PC225-5060;icid-generated-at=a.b.c.d;orig-ioi=PC345\r\n" +
                "Content-Length: 0\r\n");

        assertNotNull(pcv);
        assertEquals("1jhdgv-PC225-5060",pcv.getICID());
        assertEquals("a.b.c.d",pcv.getICIDGeneratedAt());
        assertEquals("PC345", pcv.getOriginatingIOI());
        assertEquals("PC123", pcv.getTerminatingIOI());


        pcv = getPChargingVectorHeader(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "P-Charging-Vector: icid-value=abc123\r\n" +
                "Content-Length: 0\r\n");

        assertNotNull(pcv);
        assertEquals("abc123",pcv.getICID());
        assertNull(pcv.getICIDGeneratedAt());
        assertNull(pcv.getOriginatingIOI());
        assertNull(pcv.getTerminatingIOI());

        // Mandatory icid-value missing => no header parsed
        pcv = getPChargingVectorHeader(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "P-Charging-Vector: \r\n" +
                "Content-Length: 0\r\n");

        assertNull(pcv);

    }

}
