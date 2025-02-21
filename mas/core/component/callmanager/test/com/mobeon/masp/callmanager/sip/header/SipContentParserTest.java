package com.mobeon.masp.callmanager.sip.header;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;

import gov.nist.javax.sip.parser.StringMsgParser;
import gov.nist.javax.sip.message.SIPMessage;

import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentLanguageHeader;
import javax.sip.header.ContentEncodingHeader;
import java.util.Collection;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

/**
 * SipContentParser Tester.
 *
 * @author Malin Flodin
 */
public class SipContentParserTest extends MockObjectTestCase
{
    private static String ciscoInput =
            "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
            "MIME-Version: 1.0\r\n" +
            "Content-Type: multipart/mixed;boundary=uniqueBoundary\r\n" +
            "Content-Length: 400\r\n" +
            "\r\n"+
            "--uniqueBoundary\r\n" +
            "Content-Type: application/sdp\r\n" +
            "\r\n" +
            "v=0\r\n" +
            "o=CiscoSystemsSIP-GW-UserAgent 3807 8936 IN IP4 10.21.0.74\r\n" +
            "s=SIP Call\r\n" +
            "c=IN IP4 10.21.0.74\r\n" +
            "t=0 0\r\n" +
            "m=audio 18922 RTP/AVP 0\r\n" +
            "c=IN IP4 10.21.0.74\r\n" +
            "a=rtpmap:0 PCMU/8000\r\n" +
            "a=ptime:40\r\n" +
            "--uniqueBoundary\r\n" +
            "Content-Type: application/gtd\r\n" +
            "Content-Disposition: signal;handling=optional\r\n" +
            "\r\n" +
            "IAM,\r\n" +
            "GCI,aa1f2adec97611d9adcc0003ba909185\r\n" +
            "\r\n" +
            "--uniqueBoundary--\r\n";

    private static StringMsgParser stringMsgParser = new StringMsgParser();

    public SipContentParserTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that a leaf can be create correctly and that the returned
     * SipContentData contains the information inserted when creating the leaf.
     * @throws Exception if test case fails.
     */
    public void testCreateLeaf() throws Exception {
        Mock mockContentLength = mock(ContentLengthHeader.class);
        Mock mockContentType = mock(ContentTypeHeader.class);
        Mock mockContentDisp = mock(ContentDispositionHeader.class);
        Mock mockContentLang = mock(ContentLanguageHeader.class);
        Mock mockContentEncoding = mock(ContentEncodingHeader.class);

        mockContentLength.expects(once()).method("getContentLength").will(returnValue(5));
        SipContentData leaf = SipContentParser.getInstance().createLeaf(
                "contentString",
                (ContentLengthHeader)mockContentLength.proxy(),
                (ContentTypeHeader)mockContentType.proxy(),
                (ContentDispositionHeader)mockContentDisp.proxy(),
                (ContentLanguageHeader)mockContentLang.proxy(),
                (ContentEncodingHeader)mockContentEncoding.proxy());
        assertEquals("contentString", leaf.getContent());
        assertEquals(5, leaf.getContentLength());
        assertEquals(mockContentType.proxy(), leaf.getContentTypeHeader());
        assertEquals(mockContentDisp.proxy(), leaf.getContentDispositionHeader());
        assertEquals(mockContentLang.proxy(), leaf.getContentLanguageHeader());
        assertEquals(mockContentEncoding.proxy(), leaf.getContentEncodingHeader());
    }

    /**
     * Verify method hasContent.
     * If no Content-Length exists in the message, or if it is zero, hasContent
     * returns false. Otherwise, hasContent returns true. These three scenarios
     * are verified by this method.
     * @throws Exception if test case fails.
     */
    public void testHasContent() throws Exception {
        SIPMessage sipMessage;

        String msgWithoutContentLength =
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "Content-Type: application/body\r\n" +
                "\r\n"+
                "body\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(msgWithoutContentLength);
        assertFalse(SipContentParser.getInstance().hasContent(sipMessage));

        String msgWithZeroContentLength =
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "Content-Type: application/body\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n"+
                "body\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(msgWithZeroContentLength);
        assertFalse(SipContentParser.getInstance().hasContent(sipMessage));

        String msgWithContentLength =
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "Content-Type: application/body\r\n" +
                "Content-Length: 6\r\n" +
                "\r\n"+
                "body\r\n";
        sipMessage = stringMsgParser.parseSIPMessage(msgWithContentLength);
        assertTrue(SipContentParser.getInstance().hasContent(sipMessage));
    }

    /**
     * Verifies that an empty collection of SipContentData is returned if the
     * message to parse contains no content.
     * @throws Exception if test case fails.
     */
    public void testParseContentWithNoContent() throws Exception {
        String msgWithoutContentLength =
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "Content-Type: application/body\r\n" +
                "\r\n"+
                "body\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(msgWithoutContentLength);

        Collection<SipContentData> contents =
                SipContentParser.getInstance().parseMessageContent(sipMessage);
        assertTrue(contents.isEmpty());
    }

    /**
     * Verifies that an empty collection of SipContentData is returned if the
     * message to parse contains no Content-Type.
     * @throws Exception if test case fails.
     */
    public void testParseContentWithNoContentType() throws Exception {
        String msgWithoutContentLength =
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "Content-Length: 6\r\n" +
                "\r\n"+
                "body\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(msgWithoutContentLength);

        Collection<SipContentData> contents =
                SipContentParser.getInstance().parseMessageContent(sipMessage);
        assertTrue(contents.isEmpty());
    }

    /**
     * Verifies that a collection of one SipContentData is returned if the
     * message contains no multipart, i.e. a leaf only.
     * @throws Exception if test case fails.
     */
    public void testParseContentWithLeafOnly() throws Exception {
        String msgWithoutContentLength =
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "Content-Type: application/sdp\r\n" +
                "Content-Language: en\r\n" +
                "Content-Encoding: gzip\r\n" +
                "Content-Disposition: session;handling=required\r\n" +
                "Content-Length: 6\r\n" +
                "\r\n"+
                "body\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(msgWithoutContentLength);

        Collection<SipContentData> contents =
                SipContentParser.getInstance().parseMessageContent(sipMessage);
        assertEquals(1, contents.size());
        for (SipContentData sipContentData : contents) {
            assertEquals("body", sipContentData.getContent().trim());
            assertEquals("application", sipContentData.getContentType());
            assertEquals("sdp", sipContentData.getContentSubType());
            assertEquals(6, sipContentData.getContentLength());
            assertEquals("required", sipContentData.getContentDispositionHandling());
            assertEquals("session", sipContentData.getContentDispositionType());
            assertEquals("gzip", sipContentData.getContentEncodingHeader().getEncoding());
            assertEquals("en", sipContentData.getContentLanguageHeader().
                    getContentLanguage().getLanguage());
        }
    }

    /**
     * Verifies that an empty collection of SipContentData is returned if the
     * message to parse contains a Content-Type indicating multipart, but no
     * boundary parameter can be found.
     * @throws Exception if test case fails.
     */
    public void testParseContentWithMultipartWithoutBoundary() throws Exception {
        String msgWithoutContentLength =
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "Content-Type: multipart/mixed\r\n" +
                "Content-Length: 6\r\n" +
                "\r\n"+
                "body\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(msgWithoutContentLength);

        Collection<SipContentData> contents =
                SipContentParser.getInstance().parseMessageContent(sipMessage);
        assertTrue(contents.isEmpty());
    }

    /**
     * Verifies that an empty collection of SipContentData is returned if the
     * message to parse contains a multipart, but the part contains only headers
     * and no body.
     * @throws Exception if test case fails.
     */
    public void testParseContentWithMultipartWithoutBodyPart() throws Exception {
        String msgWithoutContentLength =
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "Content-Type: multipart/mixed;boundary=uniqueBoundary\r\n" +
                "Content-Length: 88\r\n" +
                "\r\n" +
                "--uniqueBoundary\r\n" +
                "Content-Type: application/sdp\r\n" +
                "Content-Length: 0\r\n" +
                "--uniqueBoundary--\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(msgWithoutContentLength);

        Collection<SipContentData> contents =
                SipContentParser.getInstance().parseMessageContent(sipMessage);
        assertTrue(contents.isEmpty());
    }

    /**
     * Verifies that a typical multipart message from a Cisco gateway can be
     * parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseContentWithCiscoInput() throws Exception {
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(ciscoInput);
        Collection<SipContentData> contents =
                SipContentParser.getInstance().parseMessageContent(sipMessage);

        for (SipContentData content : contents) {
            if (content.isContentSdp()) {
                assertEquals("application", content.getContentType());
                assertEquals("sdp", content.getContentSubType());
                assertEquals(185, content.getContentLength());

            } else {
                assertEquals("application", content.getContentType());
                assertEquals("gtd", content.getContentSubType());
                assertEquals(46, content.getContentLength());
                assertEquals("signal", content.getContentDispositionType());
                assertEquals("optional", content.getContentDispositionHandling());
            }
        }
    }

    /**
     * Verifies that a nested multipart message can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseContentWithNestedMultipart() throws Exception {
        String nestedMultipart =
                "INVITE sip:343434@150.132.5.119:5060 SIP/2.0\r\n" +
                "MIME-Version: 1.0\r\n" +
                "Content-Type: multipart/mixed;boundary=uniqueBoundary\r\n" +
                "Content-Length: 372\r\n" +
                "\r\n"+
                "--uniqueBoundary\r\n" +
                "Content-Type: multipart/mixed;boundary=newBoundary\r\n" +
                "\r\n" +
                "--newBoundary\r\n" +
                "Content-Type: application/sdp\r\n" +
                "\r\n" +
                "v=0\r\n" +
                "t=0 0\r\n" +
                "--newBoundary\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "This is a text\r\n" +
                "--newBoundary--\r\n" +
                "--uniqueBoundary\r\n" +
                "Content-Type: application/gtd\r\n" +
                "Content-Disposition: signal;handling=optional\r\n" +
                "\r\n" +
                "IAM,\r\n" +
                "GCI,aa1f2adec97611d9adcc0003ba909185\r\n" +
                "\r\n" +
                "--uniqueBoundary--\r\n";
        SIPMessage sipMessage = stringMsgParser.parseSIPMessage(nestedMultipart);
        Collection<SipContentData> contents =
                SipContentParser.getInstance().parseMessageContent(sipMessage);

        for (SipContentData content : contents) {
            if (content.isContentSdp()) {
                assertEquals("application", content.getContentType());
                assertEquals("sdp", content.getContentSubType());
                assertEquals(12, content.getContentLength());

            } else if (content.getContentSubType().equalsIgnoreCase("gtd")) {
                assertEquals("application", content.getContentType());
                assertEquals("gtd", content.getContentSubType());
                assertEquals(46, content.getContentLength());
                assertEquals("signal", content.getContentDispositionType());
                assertEquals("optional", content.getContentDispositionHandling());
            } else if (content.getContentSubType().equalsIgnoreCase("plain")) {
                assertEquals("text", content.getContentType());
                assertEquals("plain", content.getContentSubType());
                assertEquals(16, content.getContentLength());
            } else {
                fail("Unknown content found in message.");
            }
        }
    }

}
