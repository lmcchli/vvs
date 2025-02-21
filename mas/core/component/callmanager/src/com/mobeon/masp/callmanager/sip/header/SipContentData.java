/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.header;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sip.SipConstants;

import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ContentEncodingHeader;
import javax.sip.header.ContentLanguageHeader;
import javax.sip.header.ContentDispositionHeader;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This container represents a leaf in the SIP message content. It contains the
 * content itself together with its properties such as for example
 * Content-Type and Content-Disposition.
 * <p>
 * This class is thread-safe.
 * @author Malin Flodin
 */
public class SipContentData {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private AtomicReference<String> content = new AtomicReference<String>();
    private int contentLength;
    private AtomicReference<ContentDispositionHeader> contentDispositionHeader =
            new AtomicReference<ContentDispositionHeader>();
    private AtomicReference<ContentEncodingHeader> contentEncodingHeader =
            new AtomicReference<ContentEncodingHeader>();
    private AtomicReference<ContentLanguageHeader> contentLanguageHeader =
            new AtomicReference<ContentLanguageHeader>();
    private AtomicReference<ContentTypeHeader> contentTypeHeader =
            new AtomicReference<ContentTypeHeader>();

    public String getContent() {
        return content.get();
    }

    public void setContent(String content) {
        this.content.set(content);
    }

    public String getContentDispositionHandling() {
        String result = null;
        ContentDispositionHeader disposition = getContentDispositionHeader();
        if (disposition != null)
            result = disposition.getHandling();
        return result;
    }

    public String getContentDispositionType() {
        String result = null;
        ContentDispositionHeader disposition = getContentDispositionHeader();
        if (disposition != null)
            result = disposition.getDispositionType();
        return result;
    }

    public ContentDispositionHeader getContentDispositionHeader() {
        return contentDispositionHeader.get();
    }

    public void setContentDispositionHeader(
            ContentDispositionHeader contentDispositionHeader) {
        this.contentDispositionHeader.set(contentDispositionHeader);
    }

    public ContentEncodingHeader getContentEncodingHeader() {
        return contentEncodingHeader.get();
    }

    public void setContentEncodingHeader(
            ContentEncodingHeader contentEncodingHeader) {
        this.contentEncodingHeader.set(contentEncodingHeader);
    }

    public ContentLanguageHeader getContentLanguageHeader() {
        return contentLanguageHeader.get();
    }

    public void setContentLanguageHeader(
            ContentLanguageHeader contentLanguageHeader) {
        this.contentLanguageHeader.set(contentLanguageHeader);
    }

    public int getContentLength() {
        int result = contentLength;
        if ((result <= 0) && (getContent() != null))
            result = getContent().length();
        return result;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public ContentTypeHeader getContentTypeHeader() {
        return contentTypeHeader.get();
    }

    public void setContentTypeHeader(
            ContentTypeHeader contentTypeHeader) {
        this.contentTypeHeader.set(contentTypeHeader);
    }

    public String getContentType() {
        String contentType = "";
        ContentTypeHeader contentTypeHeader = getContentTypeHeader();
        if (contentTypeHeader != null)
            contentType = contentTypeHeader.getContentType();
        return contentType;
    }

    public String getContentSubType() {
        String contentSubType = "";
        ContentTypeHeader contentTypeHeader = getContentTypeHeader();
        if (contentTypeHeader != null)
            contentSubType = contentTypeHeader.getContentSubType();
        return contentSubType;
    }

    /**
     * Returns whether the content is required or not.
     * <p>
     * If the Content-Disposition header is present with the handling parameter
     * set to "optional", the content is optional and false is returned.
     * Otherwise, the content required and true is returned.
     * 
     * @return  True if the content is required. False otherwise.
     */
    public boolean isContentRequired() {
        boolean required = true;

        if (log.isDebugEnabled())
            log.debug("Checking disposition: " + getContentDispositionHeader());

        String handling = getContentDispositionHandling();
        if ((handling != null) && (handling.equals(SipConstants.DISPOSITION_OPTIONAL)))
            required = false;

        if (log.isDebugEnabled())
            log.debug("Is content required?: " + required);

        return required;
    }

    /**
     * Returns whether the Content-Encoding of this content is supported or not.
     * <p>
     * Currently no content encodings are supported so this method only returns
     * true if no content encoding is set for the content. I.e. if the
     * Content-Encoding header is missing or is set to "identity" or "", no
     * encoding is used.
     *
     * @return  True if the content encoding indicated for the content is
     *          supported. False otherwise.
     */
    public boolean isContentEncodingSupported() {
        boolean supported = true;

        ContentEncodingHeader contentEncodingHeader = getContentEncodingHeader();

        if (contentEncodingHeader != null) {
            String encoding = contentEncodingHeader.getEncoding();

            if ((!encoding.equals(SipConstants.ENCODING_NONE)) &&
                    (encoding.length() != 0)) {
                if (log.isDebugEnabled())
                    log.debug("The following ContentEncoding is not supported: " +
                            encoding + ".");
                supported = false;
            }
        }

        if (log.isDebugEnabled())
            log.debug("Is content encoding supported?: " + supported);

        return supported;
    }

    /**
     * Returns whether the Content-Language of this content data is supported.
     * All languages are currently supported, so this method always returns true.
     * @return  True since all content languages are supported.
     */
    public boolean isContentLanguageSupported() {
        // Currently all content-languages are supported.
        boolean supported = true;

        if (log.isDebugEnabled())
            log.debug("Is content language supported?: " + supported);

        return supported;
    }

    /**
     * Returns whether the Content-Type of this content data is supported.
     * The only content types supported are "application/sdp" and
     * "application/media_control+xml", and the only charset allowed is "UTF-8".
     * <p>
     * If the Content-Type header is missing but there is a body present in the
     * message, the content type is considered unsupported.
     * @return  True if the content type is supported, false otherwise.
     */
    public boolean isContentTypeSupported() {
        boolean supported = false;

        ContentTypeHeader contentTypeHeader = getContentTypeHeader();

        if (contentTypeHeader == null) {
            // Missing Content-Type is acceptable if the body is empty
            if (getContent() == null)
                supported = true;

        } else {
            String charset =
                    contentTypeHeader.getParameter(SipConstants.PARAM_CHARSET);

            if ((charset != null) &&
                    (!charset.toLowerCase().equals(SipConstants.CHARSET_UTF8))) {

                if (log.isDebugEnabled())
                    log.debug("The charset of the received Content-Type is not " +
                            "supported: " + charset + ".");

            } else {
                if (isContentSdp())
                    supported = true;
                else if (isContentMediaControl())
                    supported = true;
                else if(isContentGtd()){
                    if (log.isDebugEnabled())
                        log.debug("GTD subtype is supported.");
                    supported = true;
                } else {
                    if (log.isDebugEnabled())
                        log.debug("Required Content-Type is not supported: " +
                                contentTypeHeader);
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("Is content type supported?: " + supported);

        return supported;
    }

    /**
     * Checks if this ContentData is a GTD.
     * @return true if the ContentData is a GTD.
     */

    public boolean isContentGtd() {
        boolean result = false;

        if (getContentType().equalsIgnoreCase(SipConstants.CT_APPLICATION))
            if (getContentSubType().equalsIgnoreCase(SipConstants.CST_GTD))
                result = true;

        return result;
    }

    public boolean isContentSdp() {
        boolean result = false;

        if (getContentType().equalsIgnoreCase(SipConstants.CT_APPLICATION))
            if (getContentSubType().equalsIgnoreCase(SipConstants.CST_SDP))
                result = true;

        return result;
    }

    public boolean isContentMediaControl() {
        boolean result = false;

        if (getContentType().equalsIgnoreCase(SipConstants.CT_APPLICATION))
            if (getContentSubType().equalsIgnoreCase(SipConstants.CST_MEDIA_CONTROL))
                result = true;

        return result;
    }

    public String toString() {
        return "SipContentData: Type = <" + getContentTypeHeader() +
                ">, Length = <Content-Lenght: " + getContentLength() +
                ">, Disposition = <" + getContentDispositionHeader() +
                ">, Encoding = <" + getContentEncodingHeader() +
                ">, Language = <" + getContentLanguageHeader() + ">";
    }
}
