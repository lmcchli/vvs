package com.mobeon.masp.callmanager.sip.header;

import org.jmock.MockObjectTestCase;
import gov.nist.javax.sip.parser.StringMsgParser;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.header.ims.HistoryInfoHeader;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.SipUtils;

import java.util.LinkedList;
import java.util.Iterator;

/**
 * History-Info header Tester.
 *
 */
public class HistoryInfoTest extends MockObjectTestCase
{

    private static StringMsgParser stringMsgParser = new StringMsgParser();

    public HistoryInfoTest(String name) {
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


    private HistoryInfoHeader[] getHistInfoHeaders(String request)
            throws Exception
    {
        SIPMessage msg = stringMsgParser.parseSIPMessage(request);
        LinkedList<HistoryInfoHeader> list = new LinkedList<HistoryInfoHeader>();
        Iterator it = msg.getHeaders(HistoryInfoHeader.NAME);
        while (it.hasNext()) {
            HistoryInfoHeader historyInfoHeader = (HistoryInfoHeader) it.next();
            list.add(historyInfoHeader);
        }
        return list.toArray(new HistoryInfoHeader[0]);
    }

    /**
     * Test getter for Privacy header values
     * @throws Exception
     */
    public void testGetPrivacyValues() throws Exception {
        HistoryInfoHeader[] histList;

        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:123@abc?Privacy=history>;index=1\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(1,histList[0].getPrivacyValues().length);
        assertEquals("history",histList[0].getPrivacyValues()[0]);


        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:123@abc?Privacy=none>;index=1\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(1,histList[0].getPrivacyValues().length);
        assertEquals("none",histList[0].getPrivacyValues()[0]);

        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:123@abc?Privacy=none%3bhistory>;index=1\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(2,histList[0].getPrivacyValues().length);
        assertEquals("none",histList[0].getPrivacyValues()[0]);
        assertEquals("history",histList[0].getPrivacyValues()[1]);

        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:123@abc?Privacy=none%3Bhistory>;index=1\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(2,histList[0].getPrivacyValues().length);
        assertEquals("none",histList[0].getPrivacyValues()[0]);
        assertEquals("history",histList[0].getPrivacyValues()[1]);


        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:123@abc?Privacy=none%3bhistory%3Bfoo>;index=1\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(3,histList[0].getPrivacyValues().length);
        assertEquals("none",histList[0].getPrivacyValues()[0]);
        assertEquals("history",histList[0].getPrivacyValues()[1]);
        assertEquals("foo",histList[0].getPrivacyValues()[2]);


        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:123@abc?Privacy=no%33%34%3b%41%3bfoo>;index=1\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(3,histList[0].getPrivacyValues().length);
        assertEquals("no%33%34",histList[0].getPrivacyValues()[0]);
        assertEquals("%41",histList[0].getPrivacyValues()[1]);
        assertEquals("foo",histList[0].getPrivacyValues()[2]);


        // Test non existing Privacy
        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:123@abc>;index=1\r\n" +
                "Content-Length: 0\r\n");

        assertNull(histList[0].getPrivacyValues());

        // Test empty Privacy
        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:123@abc?Privacy=>;index=1\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(1,histList[0].getPrivacyValues().length);
        assertEquals("",histList[0].getPrivacyValues()[0]);

    }

    /**
     * Test various getters in the HistoryInfo class.
     * @throws Exception
     */
    public void testHistoryInfo() throws Exception {

        HistoryInfoHeader[] histList;

        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:1234@ims.example.com?Reason=SIP%3Bcause%3D302>;index=1;foo=bar\r\n" +
                "Privacy: history;id\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(1,histList.length);
        assertEquals("1",histList[0].getIndex());
        assertEquals("<sip:1234@ims.example.com?Reason=SIP%3Bcause%3D302>",
                histList[0].getAddress().toString());
        assertEquals("sip:1234@ims.example.com",histList[0].getUriNoHeaders().toString());
        assertEquals("SIP;cause=302",SipUtils.unescape(histList[0].getReasonHeader()));
        assertNull(histList[0].getPrivacyValues());


        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:CDIV_CFNRT0060_B@abcxyz.com>;index=1," +
                "<sip:CDIV_CFNRT0060_C@abcxyz.com?Reason=SIP%3Bcause%3D408%3B" +
                "text%3D%22Request%20Timeout%22>;index=1.1\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(2,histList.length);
        assertEquals("1",histList[0].getIndex());
        assertEquals("<sip:CDIV_CFNRT0060_B@abcxyz.com>",
                histList[0].getAddress().toString());
        assertEquals("sip:CDIV_CFNRT0060_B@abcxyz.com",
                histList[0].getUriNoHeaders().toString());
        assertNull(histList[0].getReasonHeader());
        assertNull(histList[0].getPrivacyValues());
        assertEquals("1.1",histList[1].getIndex());
        assertEquals("<sip:CDIV_CFNRT0060_C@abcxyz.com?Reason=SIP%3Bcause%3D408%3B" +
                "text%3D%22Request%20Timeout%22>", histList[1].getAddress().toString());
        assertEquals("sip:CDIV_CFNRT0060_C@abcxyz.com", histList[1].getUriNoHeaders().toString());
        assertEquals("SIP;cause=408;text=\"Request Timeout\"",
                SipUtils.unescape(histList[1].getReasonHeader()));
        assertNull(histList[1].getPrivacyValues());



        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:1234@ims.example.com?Reason=SIP%3Bcause%3D302&Privacy=history>;index=1;foo=b%3Dar\r\n" +
                "History-Info: <sip:5678@ims.example.com?Reason=SIP%3Bcause%3D408>;index=1.1;wii=haa\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(2,histList.length);
        assertEquals("1",histList[0].getIndex());
        assertEquals("<sip:1234@ims.example.com?Reason=SIP%3Bcause%3D302&Privacy=history>",
                histList[0].getAddress().toString());
        assertEquals("sip:1234@ims.example.com",histList[0].getUriNoHeaders().toString());
        assertEquals("SIP;cause=302",SipUtils.unescape(histList[0].getReasonHeader()));
        assertEquals("history",SipUtils.unescape(histList[0].getPrivacyValues()[0]));
        assertEquals("1.1",histList[1].getIndex());
        assertEquals("<sip:5678@ims.example.com?Reason=SIP%3Bcause%3D408>",
                histList[1].getAddress().toString());
        assertEquals("sip:5678@ims.example.com",histList[1].getUriNoHeaders().toString());
        assertEquals("SIP;cause=408",
                SipUtils.unescape(histList[1].getReasonHeader()));
        assertNull(histList[1].getPrivacyValues());


        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:1234@ims.example.com;boo=baa;wii?Reason=SIP%3Bcause%3D302&Privacy=history>;foo=b%3Dar\r\n" +
                "History-Info: <sip:5678@ims.example.com;hoo;a=b?Reason=SIP%3Bcause%3D408>;wii=haa\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(2,histList.length);
        assertNull(histList[0].getIndex());
        assertEquals("<sip:1234@ims.example.com;boo=baa;wii?Reason=SIP%3Bcause%3D302&Privacy=history>",
                histList[0].getAddress().toString());
        assertEquals("sip:1234@ims.example.com;boo=baa;wii",histList[0].getUriNoHeaders().toString());
        assertEquals("SIP;cause=302",SipUtils.unescape(histList[0].getReasonHeader()));
        assertEquals("history",SipUtils.unescape(histList[0].getPrivacyValues()[0]));
        assertNull(histList[1].getIndex());
        assertEquals("<sip:5678@ims.example.com;a=b;hoo?Reason=SIP%3Bcause%3D408>",
                histList[1].getAddress().toString());
        // Stack seems to put "value-less" parameters last
        assertEquals("sip:5678@ims.example.com;a=b;hoo",histList[1].getUriNoHeaders().toString());
        assertEquals("SIP;cause=408",
                SipUtils.unescape(histList[1].getReasonHeader()));
        assertNull(histList[1].getPrivacyValues());


        // Missing/empty reason
        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <sip:1234@ims.example.com?Privacy=history>;index=1;foo=b%3Dar\r\n" +
                "History-Info: <sip:5678@ims.example.com?Reason=>;index=1.1;wii=haa\r\n" +
                "Content-Length: 0\r\n");

        assertEquals(2,histList.length);
        assertEquals("1",histList[0].getIndex());
        assertEquals("<sip:1234@ims.example.com?Privacy=history>",
                histList[0].getAddress().toString());
        assertEquals("sip:1234@ims.example.com",histList[0].getUriNoHeaders().toString());
        assertNull(histList[0].getReasonHeader());
        assertEquals("history",SipUtils.unescape(histList[0].getPrivacyValues()[0]));
        assertEquals("1.1",histList[1].getIndex());
        // Stack seems to remove "=" from Header with empty value
        assertEquals("<sip:5678@ims.example.com?Reason>",
                histList[1].getAddress().toString());
        assertEquals("sip:5678@ims.example.com",histList[1].getUriNoHeaders().toString());
        assertEquals("",histList[1].getReasonHeader());
        assertNull(histList[1].getPrivacyValues());


        // Test of TEL URI
        histList = getHistInfoHeaders(
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "History-Info: <tel:+0123456789>;index=1;foo=b%3Dar\r\n" +
                "History-Info: <tel:0123456789*#>;index=1.1;wii=haa\r\n" +
                "History-Info: <tel:0123456789*#?Privacy=history>;index=1.2;wii=haa\r\n" +
                "Content-Length: 0\r\n");

        // Note that the 3rd History-Info header is invalid since a TEL-URI cannot contain headers
        // therefor only two History-Headers are found, the 3rd is discarded by the stack
        // Privacy and Reason can thereofor not be extracted from a History-Info header with TEL-URI's
        assertEquals(2,histList.length);
        assertEquals("1",histList[0].getIndex());
        assertEquals("<tel:+0123456789>", histList[0].getAddress().toString());
        assertEquals("tel:+0123456789",histList[0].getUriNoHeaders().toString());
        assertNull(histList[0].getReasonHeader());
        assertNull(histList[0].getPrivacyValues());
        assertEquals("1.1",histList[1].getIndex());
        assertEquals("<tel:0123456789*#>", histList[1].getAddress().toString());
        assertEquals("tel:0123456789*#",histList[1].getUriNoHeaders().toString());
        assertNull(histList[1].getReasonHeader());
        assertNull(histList[1].getPrivacyValues());

    }


}
