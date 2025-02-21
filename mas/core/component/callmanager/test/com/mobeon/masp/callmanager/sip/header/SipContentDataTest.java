/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.header;

import junit.framework.TestCase;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.ParserFactory;
import gov.nist.javax.sip.header.SIPHeaderList;

import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentLanguageHeader;
import javax.sip.header.ContentEncodingHeader;
import javax.sip.header.ContentTypeHeader;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;

/**
 * SipContentData Tester.
 *
 * @author Malin Flodin
 */
public class SipContentDataTest extends TestCase
{
    SipContentData sipContentData;

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that the content length is returned. If the content length is
     * zero, the size of the content is returned.
     * @throws Exception if test case fails.
     */
    public void testGetContentLength() throws Exception {
        // Verify for zero content length and no content
        sipContentData = new SipContentData();
        assertEquals(0, sipContentData.getContentLength());

        // Verify for non-zero content length
        sipContentData = new SipContentData();
        sipContentData.setContentLength(5);
        assertEquals(5, sipContentData.getContentLength());

        // Verify for zero content length but with content
        sipContentData = new SipContentData();
        sipContentData.setContent("The content");
        assertEquals(11, sipContentData.getContentLength());
    }

    /**
     * Verifies that a Content-Disposition header parameter handling set to
     * optional indicates that the body is not required.
     * If the Content-Disposition header is missing, the handling parameter is
     * missing or set to required, the body is required.
     *
     * @throws Exception if test case fails.
     */
    public void testIsContentRequired() throws Exception {
        HeaderParser hdrParser;
        ContentDispositionHeader header;

        // Verify that if no Content-Disposition header is present, the body
        // is considered required.
        sipContentData = new SipContentData();
        assertTrue(sipContentData.isContentRequired());

        // Verify that if Content-Disposition header lacks handling parameter,
        // the body is considered required
        hdrParser = ParserFactory.createParser("Content-Disposition: render\n");
        header = (ContentDispositionHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentDispositionHeader(header);
        assertTrue(sipContentData.isContentRequired());

        // Verify that if Content-Disposition header parameter handling is set to
        // to "required", the body is considered required.
        hdrParser = ParserFactory.createParser(
                "Content-Disposition: render; handling=required\n");
        header = (ContentDispositionHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentDispositionHeader(header);
        assertTrue(sipContentData.isContentRequired());

        // Verify that if Content-Disposition header parameter handling is set to
        // to "optional", the body is considered required.
        hdrParser = ParserFactory.createParser(
                "Content-Disposition: session; handling=optional\n");
        header = (ContentDispositionHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentDispositionHeader(header);
        assertFalse(sipContentData.isContentRequired());
    }

    /**
     * Verifies that the encoding is considered supported if there is no
     * Content-Encoding header or if it is set to "" or "identity".
     * Otherwise, the encoding is not supported.
     *
     * @throws Exception if test case fails.
     */
    public void testIsContentEncodingSupported() throws Exception {
        HeaderParser hdrParser;
        ContentEncodingHeader header;

        // Verify that if no Content-Encoding header is present, the encoding
        // is supported.
        sipContentData = new SipContentData();
        assertTrue(sipContentData.isContentEncodingSupported());

        // Verify that if Content-Encoding is set to "identity" the encoding is
        // supported.
        hdrParser = ParserFactory.createParser("Content-Encoding: identity\n");
        header = (ContentEncodingHeader)((SIPHeaderList)hdrParser.parse()).getFirst();
        sipContentData = new SipContentData();
        sipContentData.setContentEncodingHeader(header);
        assertTrue(sipContentData.isContentEncodingSupported());

        // Verify that if Content-Encoding is set to gzip the encoding is not
        // supported.
        hdrParser = ParserFactory.createParser("Content-Encoding: gzip\n");
        header = (ContentEncodingHeader)((SIPHeaderList)hdrParser.parse()).getFirst();
        sipContentData = new SipContentData();
        sipContentData.setContentEncodingHeader(header);
        assertFalse(sipContentData.isContentEncodingSupported());
    }

    /**
     * Verify that all languages are supported, regardless of Content-Language
     * header.
     * @throws Exception if test case fails.
     */
    public void testIsContentLanguageSupported() throws Exception {
        HeaderParser hdrParser;
        ContentLanguageHeader header;

        // Verify that if no Content-Language header is present, the language
        // is supported.
        sipContentData = new SipContentData();
        assertTrue(sipContentData.isContentLanguageSupported());

        // Verify that if Content-Language is set to "us" the language is supported.
        hdrParser = ParserFactory.createParser("Content-Language: en\n");
        header = (ContentLanguageHeader)((SIPHeaderList)hdrParser.parse()).getFirst();
        sipContentData = new SipContentData();
        sipContentData.setContentLanguageHeader(header);
        assertTrue(sipContentData.isContentLanguageSupported());
    }

    /**
     * The following is verified with regards to Content-Type:
     * <ul>
     * <li>If no Content-Type header is present, the type is supported if there
     * is no content.</li>
     * <li>If no Content-Type header is present, the type is NOT supported if
     * there is a content.</li>
     * <li>If charset differs from UTF-8 the type is NOT supported</li>
     * <li>If charset is UTF-8 the type is supported</li>
     * <li>If type differs from application, the type is NOT supported</li>
     * <li>If subtype differs from sdp or media_contro+xml, the type is NOT
     * supported.</li>
     * <li>The type application/sdp is supported.</li>
     * <li>The type application/media_control+xml is supported.</li>
     * </ul>
     * @throws Exception if test case fails.
     */
    public void testIsContentTypeSupported() throws Exception {
        HeaderParser hdrParser;
        ContentTypeHeader header;

        // Verify that if no Content-Type header is present, the type
        // is supported if there is no content.
        sipContentData = new SipContentData();
        assertTrue(sipContentData.isContentTypeSupported());

        // Verify that if no Content-Type header is present, the type
        // is NOT supported if there is a content.
        sipContentData = new SipContentData();
        sipContentData.setContent("content");
        assertFalse(sipContentData.isContentTypeSupported());

        // Verify that if charset differs from UTF-8 the type is NOT supported
        hdrParser = ParserFactory.createParser("Content-Type: application/sdp;charset=UTF-16\n");
        header = (ContentTypeHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentTypeHeader(header);
        assertFalse(sipContentData.isContentTypeSupported());

        // Verify that if charset is UTF-8 the type is supported
        hdrParser = ParserFactory.createParser("Content-Type: application/sdp;charset=UTF-8\n");
        header = (ContentTypeHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentTypeHeader(header);
        assertTrue(sipContentData.isContentTypeSupported());

        // Verify that if type differs from application, the type is NOT supported
        hdrParser = ParserFactory.createParser("Content-Type: audio/sdp\n");
        header = (ContentTypeHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentTypeHeader(header);
        assertFalse(sipContentData.isContentTypeSupported());

        // Verify that if subtype differs from sdp or media_contro+xml,
        // the type is NOT supported
        hdrParser = ParserFactory.createParser("Content-Type: application/other\n");
        header = (ContentTypeHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentTypeHeader(header);
        assertFalse(sipContentData.isContentTypeSupported());

        // Verify that if application/sdp is supported
        hdrParser = ParserFactory.createParser("Content-Type: application/sDp\n");
        header = (ContentTypeHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentTypeHeader(header);
        assertTrue(sipContentData.isContentTypeSupported());

        // Verify that if application/media_control+xml is supported
        hdrParser = ParserFactory.createParser("Content-Type: application/media_control+Xml\n");
        header = (ContentTypeHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentTypeHeader(header);
        assertTrue(sipContentData.isContentTypeSupported());
    }

    /**
     * Verifies that the content is of type SDP.
     * @throws Exception if test case fails.
     */
    public void testIsContentSdp() throws Exception {
        HeaderParser hdrParser;
        ContentTypeHeader header;

        // Verify that false is returned if Content-Type header is missing.
        sipContentData = new SipContentData();
        assertFalse(sipContentData.isContentSdp());

        // Verify that false is returned if content-type is NOT SDP
        hdrParser = ParserFactory.createParser("Content-Type: application/media_control+Xml\n");
        header = (ContentTypeHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentTypeHeader(header);
        assertFalse(sipContentData.isContentSdp());

        // Verify that true is returned if content-type is SDP
        hdrParser = ParserFactory.createParser("Content-Type: application/sDp\n");
        header = (ContentTypeHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentTypeHeader(header);
        assertTrue(sipContentData.isContentSdp());
    }

    /**
     * Verifies that the content is of type Media Control.
     * @throws Exception if test case fails.
     */
    public void testIsContentMediaControl() throws Exception {
        HeaderParser hdrParser;
        ContentTypeHeader header;

        // Verify that false is returned if Content-Type header is missing.
        sipContentData = new SipContentData();
        assertFalse(sipContentData.isContentMediaControl());

        // Verify that false is returned if content-type is NOT media control
        hdrParser = ParserFactory.createParser("Content-Type: application/sDp\n");
        header = (ContentTypeHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentTypeHeader(header);
        assertFalse(sipContentData.isContentMediaControl());

        // Verify that true is returned if content-type is media control
        hdrParser = ParserFactory.createParser("Content-Type: application/media_control+Xml\n");
        header = (ContentTypeHeader)hdrParser.parse();
        sipContentData = new SipContentData();
        sipContentData.setContentTypeHeader(header);
        assertTrue(sipContentData.isContentMediaControl());
    }

}
