/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.util.ContentTypePatterns;
import static com.mobeon.masp.mailbox.MailboxMessageType.*;

import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.HeaderTerm;
import jakarta.mail.search.NotTerm;
import jakarta.mail.search.OrTerm;

/**
 * @author Håkan Stolt
 */
public class ContentTypeHeaderUtil {

    static final String HEADERNAME_CONTENT_TYPE = "Content-Type";

    private static final SearchTerm REPORT_CONTENT_TYPE = new HeaderTerm(HEADERNAME_CONTENT_TYPE, "multipart/report");
    private static final SearchTerm VOICE_CONTENT_TYPE = new HeaderTerm(HEADERNAME_CONTENT_TYPE, ContentTypePatterns.getContentType(VOICE));
    private static final SearchTerm VIDEO_CONTENT_TYPE = new HeaderTerm(HEADERNAME_CONTENT_TYPE, ContentTypePatterns.getContentType(VIDEO));
    private static final SearchTerm FAX_CONTENT_TYPE = new HeaderTerm(HEADERNAME_CONTENT_TYPE, ContentTypePatterns.getContentType(FAX));

    static final SearchTerm VOICE_SEARCHTERM = new OrTerm(VOICE_CONTENT_TYPE, REPORT_CONTENT_TYPE);
    static final SearchTerm VIDEO_SEARCHTERM = new OrTerm(VIDEO_CONTENT_TYPE, REPORT_CONTENT_TYPE);
    static final SearchTerm FAX_SEARCHTERM   = new OrTerm(FAX_CONTENT_TYPE, REPORT_CONTENT_TYPE);
    static final SearchTerm EMAIL_SEARCHTERM = new OrTerm(new NotTerm(new OrTerm(new SearchTerm[]{VOICE_SEARCHTERM,VIDEO_SEARCHTERM,FAX_SEARCHTERM})), REPORT_CONTENT_TYPE);

}
