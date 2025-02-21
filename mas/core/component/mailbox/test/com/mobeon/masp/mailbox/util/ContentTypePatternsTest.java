package com.mobeon.masp.mailbox.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import static com.mobeon.masp.mailbox.util.ContentTypePatterns.*;
import com.mobeon.masp.mailbox.MailboxMessageType;

import java.util.regex.Pattern;

/**
 * ContentTypePatterns Tester.
 *
 * @author qhast
 */
public class ContentTypePatternsTest extends TestCase
{
    public ContentTypePatternsTest(String name)
    {
        super(name);
    }

    /**
     * This test just executes the contructor to increase code coverage.
     * @throws Exception
     */
    public void testConstruct() throws Exception {
        new ContentTypePatterns();
    }


    /**
     * Tests that strings containing the Multipart Message pattern matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsMultiPartPattern() throws Exception {
        tryPattern(MULTIPART_MESSAGE_PATTERN,"multipart/voice-message");
        tryPattern(MULTIPART_MESSAGE_PATTERN,"multipart/fax-message");
        tryPattern(MULTIPART_MESSAGE_PATTERN,"multipart/mixed");
        tryPattern(MULTIPART_MESSAGE_PATTERN,"multIPart/ha dad\tdas\n\r");
    }

    /**
     * Tests that strings NOT containing the Multipart Message pattern NOT matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsNotMultiPartPattern() throws Exception {
        antiTryPattern(MULTIPART_MESSAGE_PATTERN,"multipartner/textsallad");
        antiTryPattern(MULTIPART_MESSAGE_PATTERN,"message/rfc822");
    }

    /**
     * Tests that strings containing the Voice Message pattern matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsVoicePattern() throws Exception {
        tryPattern(VOICE_MESSAGE_PATTERN,"multipart/voice-message\t\rhuhuhu\n;dashkj");
        tryPattern(VOICE_MESSAGE_PATTERN,"multipart/voice-message");
    }

    /**
     * Tests that strings NOT containing the Voice Message pattern NOT matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsNotVoicePattern() throws Exception {
        antiTryPattern(VOICE_MESSAGE_PATTERN,"multipartner/textsallad");
        antiTryPattern(VOICE_MESSAGE_PATTERN,"multipart/fax; -----");
    }


    /**
     * Tests that strings containing the Video Message pattern matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsVideoPattern() throws Exception {
        tryPattern(VIDEO_MESSAGE_PATTERN,    "multipart/x-video-message\t\rhuhuhu\n;dashkj");
        tryPattern(VIDEO_MESSAGE_PATTERN,    "multipart/x-video-message");
    }

    /**
     * Tests that strings NOT containing the Video Message pattern NOT matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsNotVideoPattern() throws Exception {
        antiTryPattern(VIDEO_MESSAGE_PATTERN,"multipartner/textsallad");
        antiTryPattern(VIDEO_MESSAGE_PATTERN,"multipart/fax; -----");
    }

    /**
     * Tests that strings containing the Fax Message pattern matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsFaxPattern() throws Exception {
        tryPattern(FAX_MESSAGE_PATTERN,    "multipart/fax-message\t\rhuhuhu\n;dashkj");
        tryPattern(FAX_MESSAGE_PATTERN,    "multipart/fax-message");
    }

    /**
     * Tests that strings NOT containing the Fax Message pattern NOT matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsNotFaxPattern() throws Exception {
        antiTryPattern(FAX_MESSAGE_PATTERN,"multipartner/textsallad");
        antiTryPattern(FAX_MESSAGE_PATTERN,"multipart/fax-me; -----");
    }

    /**
     * Tests that strings containing the Delivery Report Message pattern matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsDeliveryReportPattern() throws Exception {
        tryPattern(DELIVERY_REPORT_PATTERN,    "multipart/report; report-type=delivery-status\t\rhuhuhu\n;dashkj");
        tryPattern(DELIVERY_REPORT_PATTERN,    "multipart/report; report-type=delivery-status");
    }

    /**
     * Tests that strings NOT containing the Delivery Report Message pattern NOT matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsNotDeliveryReportPattern() throws Exception {
        antiTryPattern(DELIVERY_REPORT_PATTERN,"multipart/report; report-type=no-report");
        antiTryPattern(DELIVERY_REPORT_PATTERN,"multipart/fax-me; -----");
    }

    /**
     * Tests that strings containing the Delivery Status part pattern matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsDeliveryStatusPartPattern() throws Exception {
        tryPattern(DELIVERY_STATUS_PART_PATTERN, "message/delivery-status; \t\rhuhuhu\n;dashkj");
        tryPattern(DELIVERY_STATUS_PART_PATTERN, "message/delivery-status");
    }

    /**
     * Tests that strings NOT containing the Delivery status part pattern NOT matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsNotDeliveryStatusPartPattern() throws Exception {
        antiTryPattern(DELIVERY_STATUS_PART_PATTERN, "message/rfc822; charset=usisis");
        antiTryPattern(DELIVERY_STATUS_PART_PATTERN, "multipart/delivery-status");
        antiTryPattern(DELIVERY_STATUS_PART_PATTERN, "multipart/report; report-type=delivery-status");
    }



    /**
     * Tests that strings containing the RFC822 Message pattern matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsRfc822Pattern() throws Exception {
        tryPattern(RFC822_MESSAGE_PATTERN,    "message/rfc822; r\t\rhuhuhu\n;dashkj");
        tryPattern(RFC822_MESSAGE_PATTERN,    "message/rfc822");
    }

    /**
     * Tests that strings NOT containing the RFC822 Message pattern NOT matches the compiled pattern.
     * @throws Exception
     */
    public void testContainsNotRfc822Pattern() throws Exception {
        antiTryPattern(RFC822_MESSAGE_PATTERN,"multipart/rfc822");
        antiTryPattern(RFC822_MESSAGE_PATTERN,"multipart/fax-me; -----");
    }

    private void tryPattern(Pattern p, String s) throws Exception {
        assertTrue(p.pattern()+" does not match \""+s+"\"",p.matcher(s).matches());
    }

    private void antiTryPattern(Pattern p, String s) throws Exception {
        assertFalse(p.pattern()+" should not match \""+s+"\"",p.matcher(s).matches());
    }

    public void testGetMultipartSubType() throws Exception {
        assertEquals("x-video-message",ContentTypePatterns.getMultipartSubType(MailboxMessageType.VIDEO));
        assertEquals("voice-message",ContentTypePatterns.getMultipartSubType(MailboxMessageType.VOICE));
        assertEquals("fax-message",ContentTypePatterns.getMultipartSubType(MailboxMessageType.FAX));
        assertEquals("mixed",ContentTypePatterns.getMultipartSubType(MailboxMessageType.EMAIL));
    }

    public void testGetContentType() throws Exception {
        assertEquals("multipart/x-video-message",ContentTypePatterns.getContentType(MailboxMessageType.VIDEO));
        assertEquals("multipart/voice-message",ContentTypePatterns.getContentType(MailboxMessageType.VOICE));
        assertEquals("multipart/fax-message",ContentTypePatterns.getContentType(MailboxMessageType.FAX));
        assertEquals("multipart/mixed",ContentTypePatterns.getContentType(MailboxMessageType.EMAIL));
    }



    public static Test suite()
    {
        return new TestSuite(ContentTypePatternsTest.class);
    }
}
