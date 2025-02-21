/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.header;

import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.ParserFactory;

import javax.sip.message.Message;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentEncodingHeader;
import javax.sip.header.ContentLanguageHeader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.ArrayList;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sip.SipConstants;


/**
 * This class is a singleton used for parsing SIP message contents.
 * It is needed in order to be able to parse MIME multipart message bodies since
 * they are not supported by the SIP stack.
 * <p>
 * The content of a SIP message is parsed using
 * {@link #parseMessageContent(Message)} and the content is returned as a
 * collection of {@link SipContentData}.
 * <p>
 * The following terminology is used in this class:
 *                                                                    Message
 * |--------------------------------------------------------------------------|
 * |   Header1: value1                                        Message headers |
 * |   Header2: value2                                                        |
 * |   Content-Type: multipart/mixed;boundary=uniqueBoundary                  |
 * |--------------------------------------------------------------------------|
 * |   --uniqueBoundary                   Message content (of type multipart) |
 * |  _______________________________________________________________         |
 * | | Content-Type: application/sdp                    Part headers |        |
 * | |_______________________________________________________________|        |
 * | |  v=0                                             Part content |        |
 * | |  ...                                                          |        |
 * | |_______________________________________________________________|        |
 * |   --uniqueBoundary                                                       |
 * |  _______________________________________________________________         |
 * | |  Content-Type: application/gtd                   Part headers |        |
 * | |  Content-Disposition: signal;handling=optional                |        |
 * | |_______________________________________________________________|        |
 * | |  IAM                                             Part content |        |
 * | |  ...                                                          |        |
 * | |_______________________________________________________________|        |
 * |   --uniqueBoundary--                                                     |
 * |--------------------------------------------------------------------------|
 *
 * A content (either a message content or a part content) can be of
 * multipart type or of a "simple" type such as e.g. audio, video or application.
 * If the content is of a "simple" type it is also called a leaf.
 *
 * @author Malin Flodin
 */
public class SipContentParser {

    private static final SipContentParser INSTANCE = new SipContentParser();

    public static final String REGEXP_BOUNDARY_ENDING =
            "(--)?(" + SipConstants.CRLF + "|" + SipConstants.LF + ")";

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public static SipContentParser getInstance() {
        return INSTANCE;
    }

    private SipContentParser() {
    }

    /**
     * Parses the content of the given <param>message</param>.
     * A collection of all leafs found in the message as {@link SipContentData}.
     * A leaf that cannot be parsed is ignored.
     * @return      A collection of one {@link SipContentData} for each leaf
     *              found in the message content.
     */
    public Collection<SipContentData> parseMessageContent(Message message) {
        Collection<SipContentData> foundLeafs = new ArrayList<SipContentData>();

        if (hasContent(message)) {
            ContentTypeHeader contentTypeHeader =
                    (ContentTypeHeader)message.getHeader(ContentTypeHeader.NAME);

            if (contentTypeHeader != null) {
                try {
                    parseContent(
                            contentTypeHeader,
                            message.getContentLength(),
                            message.getContentDisposition(),
                            message.getContentLanguage(),
                            message.getContentEncoding(),
                            new String(message.getRawContent()),
                            foundLeafs);
                } catch (IOException e) {
                    if (log.isDebugEnabled())
                        log.debug("Could not parse the message content: " +
                                e.getMessage(), e);
                }
            } else {
                if (log.isDebugEnabled())
                    log.debug("No Content-Type header found in message. " +
                            "Message content is not parsed.");
            }
        }

        return foundLeafs;
    }

    /**
     * This method parses the actual content of a message or part.
     * If the content type indicates a multipart, the content is parsed
     * further using {@link #parsePart(String, Collection<SipContentData>)}.
     * Otherwise the content type is a "simple" type (i.e. a leaf is found)
     * and the content has already been parsed completely. A representation of
     * this leaf content is created using
     * {@link #createLeaf(
     *          String, ContentLengthHeader, ContentTypeHeader,
     *          ContentDispositionHeader, ContentLanguageHeader,
     *          ContentEncodingHeader)}
     * <p>
     * The headers belonging to this content is passed to this method.
     * <p>
     * The found leafs are returned in <param>foundLeafs</param>.
     *
     * @param contentTypeHeader
     * @param contentLengthHeader
     * @param contentDispHeader
     * @param contentLanguageHeader
     * @param contentEncodingHeader
     * @param content
     * @param foundLeafs        Contains the leafs found during parsing.
     * @throws  IOException     IOException is thrown if the content could
     *                          not be read as expected.
     */
    private void parseContent(ContentTypeHeader contentTypeHeader,
                              ContentLengthHeader contentLengthHeader,
                              ContentDispositionHeader contentDispHeader,
                              ContentLanguageHeader contentLanguageHeader,
                              ContentEncodingHeader contentEncodingHeader,
                              String content,
                              Collection<SipContentData> foundLeafs)
            throws IOException {

        String contentType = contentTypeHeader.getContentType();
        if (contentType != null) {

            if (log.isDebugEnabled())
                log.debug("Parsing content for content-type: " +
                        contentType + "/" +
                        contentTypeHeader.getContentSubType());

            if (contentType.equals(SipConstants.CT_MULTIPART)) {
                // This is a multipart MIME (RFC 2046).

                String boundary = contentTypeHeader.
                        getParameter(SipConstants.PARAM_BOUNDARY);
                if (boundary != null) {
                    parseMultipart("--" + boundary, content, foundLeafs);
                } else {
                    if (log.isDebugEnabled())
                        log.debug("Could not parse a multipart content since " +
                                "no boundary parameter was found. " +
                                "The multipart is ignored.");
                }

            } else {
                // This is a simple type, e.g. audio, video or application.
                foundLeafs.add(
                        createLeaf(
                                content,
                                contentLengthHeader,
                                contentTypeHeader,
                                contentDispHeader,
                                contentLanguageHeader,
                                contentEncodingHeader));
            }
        }
    }

    /**
     * Parses a <param>multipart</param> by retrieving the parts surrounded by
     * the given <param>boundary</param>.
     * Each part are then parsed separatey using
     * {@link #parsePart(String, Collection<SipContentData>)}.
     * <p>
     * The found leafs are returned in <param>foundLeafs</param>.
     *
     * @param boundary
     * @param multipart
     * @param foundLeafs        Contains the leafs found during parsing.
     * @throws  IOException     IOException is thrown if the multipart could
     *                          not be read as expected.
     */
    private void parseMultipart(
            String boundary,
            String multipart,
            Collection<SipContentData> foundLeafs)
            throws IOException {

        if (log.isDebugEnabled())
            log.debug("Parsing multipart with boundary = " + boundary);

        String regexp = boundary + REGEXP_BOUNDARY_ENDING;
        String[] boundaryParts = multipart.split(regexp);

        for (String part : boundaryParts) {
            if (part.trim().length() > 0 ) {
                if (log.isDebugEnabled())
                    log.debug("Multipart: \"" + part + "\"");
                parsePart(part, foundLeafs);
            }
        }
    }

    /**
     * This method parses a <param>part</param> separated by MIME boundaries.
     * The part is scanned for headers describing the part. The following
     * headers are retrieved (if present):
     * <ul>
     * <li>Content-Type</li>
     * <li>Content-Length</li>
     * <li>Content-Disposition</li>
     * <li>Content-Language</li>
     * <li>Content-Encoding</li>
     * </ul>
     * <p>
     * The actual content of the <param>part</param> can either be a new
     * multipart or a single leaf. It is further parsed with
     * {@link #parseContent(
     *          ContentTypeHeader, ContentLengthHeader,
     *          ContentDispositionHeader, ContentLanguageHeader,
     *          ContentEncodingHeader, String,
     *          Collection<SipContentData>)}
     *
     * @param   part
     * @throws  IOException     IOException is thrown if the part could
     *                          not be read as expected.
     */
    private void parsePart(String part, Collection<SipContentData> foundLeafs)
            throws IOException {
        BufferedReader partReader =
                new BufferedReader(new StringReader(part));

        ContentDispositionHeader disposition = null;
        ContentEncodingHeader encoding = null;
        ContentLanguageHeader language = null;
        ContentLengthHeader length = null;
        ContentTypeHeader type = null;

        String line = partReader.readLine();
        while ((line != null) && (line.length() > 0)) {

            try {
                if (log.isDebugEnabled())
                    log.debug("Trying to parse header: " + line);
                HeaderParser hdrParser =
                        ParserFactory.createParser(line + SipConstants.LF);
                Header header = hdrParser.parse();

                if (header != null) {
                    if (header instanceof ContentTypeHeader) {
                        type = (ContentTypeHeader)header;
                    } else if (header instanceof ContentLengthHeader ) {
                        length = (ContentLengthHeader)header;
                    } else if (header instanceof ContentDispositionHeader) {
                        disposition = (ContentDispositionHeader)header;
                    } else if (header instanceof ContentLanguageHeader ) {
                        language = (ContentLanguageHeader)header;
                    } else if (header instanceof ContentEncodingHeader ) {
                        encoding = (ContentEncodingHeader)header;
                    }
                }
            } catch (ParseException e) {
                if (log.isDebugEnabled())
                    log.debug("Could not parse header: " + line, e);
            }

            line = partReader.readLine();
        }

        if (line == null) {
            if (log.isDebugEnabled())
                log.debug("Could not find any actual body in the multipart. " +
                        "It is ignored.");
        } else {
            // CRLF found now the remaining is content data.
            String theRest = getRestOfBuffer(partReader);
            parseContent(
                    type, length, disposition, language, encoding,
                    theRest, foundLeafs);
        }
    }

    /**
     * Creates and returns a container for a content leaf.
     * The container (a {@link SipContentData}) contains the content itself
     * together with its properties such as e.g. content-type and
     * content-disposition.
     * @param content
     * @param len
     * @param type
     * @param disp
     * @param lang
     * @param encoding
     * @return  A container with the content and its properties.
     */
    SipContentData createLeaf(String content,
                                      ContentLengthHeader len,
                                      ContentTypeHeader type,
                                      ContentDispositionHeader disp,
                                      ContentLanguageHeader lang,
                                      ContentEncodingHeader encoding) {

        SipContentData sipContentData = new SipContentData();

        sipContentData.setContent(content);
        sipContentData.setContentDispositionHeader(disp);
        sipContentData.setContentEncodingHeader(encoding);
        sipContentData.setContentLanguageHeader(lang);
        sipContentData.setContentTypeHeader(type);

        if (len != null)
            sipContentData.setContentLength(len.getContentLength());

        return sipContentData;
    }

    /**
     * @param reader
     * @return                  the rest of a {@link BufferedReader} as a String.
     * @throws  IOException     IOException is thrown if the reader could
     *                          not be read as expected.
     */
    private String getRestOfBuffer(BufferedReader reader) throws IOException {

        StringBuffer strBuf = new StringBuffer();

        int restChar = reader.read();
        while (restChar != -1) {
            strBuf.append((char)restChar);
            restChar = reader.read();
        }
        return strBuf.toString();
    }

    /**
     * Checks the Content-Length header of the <param>message</param>.
     * If there is no Content-Length header or if the length is zero or less,
     * the message is considered to have no content ans false is returned.
     * Otherwise true is returned.
     * @param   message
     * @return  true if the <param>message</param> has a content.
     *          Otherwise false is returned.
     */
    boolean hasContent(Message message) {
        ContentLengthHeader cl = message.getContentLength();
        return ((cl != null) && (cl.getContentLength() > 0));
    }


}
