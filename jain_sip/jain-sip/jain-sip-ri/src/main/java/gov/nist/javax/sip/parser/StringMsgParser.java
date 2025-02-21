/*
 * Conditions Of Use 
 * 
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *  
 * .
 * 
 */

/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)        *
 ******************************************************************************/

package gov.nist.javax.sip.parser;

import java.util.*;
import gov.nist.javax.sip.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;
import gov.nist.core.*;
import org.apache.log4j.Logger;

/**
 * Parse SIP message and parts of SIP messages such as URI's etc from memory and
 * return a structure. Intended use: UDP message processing. This class is used
 * when you have an entire SIP message or SIPHeader or SIP URL in memory and you
 * want to generate a parsed structure from it. For SIP messages, the payload
 * can be binary or String. If you have a binary payload, use
 * parseSIPMessage(byte[]) else use parseSIPMessage(String) The payload is
 * accessible from the parsed message using the getContent and getContentBytes
 * methods provided by the SIPMessage class. If SDP parsing is enabled using the
 * parseContent method, then the SDP body is also parsed and can be accessed
 * from the message using the getSDPAnnounce method. Currently only eager
 * parsing of the message is supported (i.e. the entire message is parsed in one
 * feld swoop).
 * 
 * 
 * @version 1.2 $Revision: 1.16 $ $Date: 2006/09/17 14:34:51 $
 * 
 * @author M. Ranganathan <br/>
 * 
 * 
 * 
 */
public class StringMsgParser {

    private static final Logger log = Logger.getLogger(StringMsgParser.class);

    protected boolean readBody;

    // Unprocessed message  (for error reporting)
    private byte[] rawMessage;

    private ParseExceptionListener parseExceptionListener;

    private boolean bodyIsString;

    protected int contentLength;

    /**
     * @since v0.9
     */
    public StringMsgParser() {
        super();
        readBody = true;
    }

    /**
     * Constructor (given a parse exception handler).
     * @since 1.0
     * @param exhandler is the parse exception listener for the message parser.
     */
    public StringMsgParser(ParseExceptionListener exhandler) {
        this();
        parseExceptionListener = exhandler;
    }



    /**
     * add a handler for header parsing errors.
     * @param  pexhandler is a class
     *  	that implements the ParseExceptionListener interface.
     */
    public void setParseExceptionListener(ParseExceptionListener pexhandler) {
        parseExceptionListener = pexhandler;
    }

    /**
     * Return true if the body is encoded as a string.
     * If the parseSIPMessage(String) method is invoked then the body
     * is assumed to be a string.
     */
    protected boolean isBodyString() {
        return bodyIsString;
    }

    //////////////////////////////////////

    /**
     * Parse a String containing a single SIP Message.
     * @param msgBuffer a String containing the messages to be parsed.
     * @return SIPMessage (request or response)
     * 			containing the parsed SIP message.
     * @exception  ParseException is thrown when an
     * 			illegal message has been encountered (and
     *			the rest of the buffer is discarded).
     * @see ParseExceptionListener
     */
    public SIPMessage parseSIPMessage(String msgBuffer) throws ParseException {
        return parseSIPMessage(msgBuffer.getBytes());
    }

    /**
     * Parse a buffer containing a single SIP Message where the body
     * is an array of un-interpreted bytes. This is intended for parsing
     * the message from a memory buffer.
     * @param msgBuffer a byte buffer containing the messages to be parsed.
     * @return SIPMessage (request or response)
     * 			containing the parsed SIP message.
     * @exception  ParseException is thrown when an
     * 			illegal message has been encountered (and
     *			the rest of the buffer is discarded).
     * @see ParseExceptionListener
     */
    public SIPMessage parseSIPMessage(byte[] msgBuffer) throws ParseException {

        rawMessage = msgBuffer;

        /** mmath Performance improvements:
         *  Compare using byte instead of char
         */
        ArrayList<ByteArray> headers = new ArrayList<ByteArray>(20);

        int bufferPointer = 0;
        bodyIsString = false;
        int s;

        // Strip leading CR's, LF's and \0's
        for (s = bufferPointer; s < msgBuffer.length; s++) {
            if ( msgBuffer[s] != (byte)'\r'
                    && msgBuffer[s] != (byte)'\n'
                    && msgBuffer[s] != (byte)'\0')
                break;
        }

        if (s == msgBuffer.length) {
            log.warn("Cannot parse empty message");
            throw new ParseException("Cannot parse empty message",0);
        }


        // Process first line

        // Scan headers line by line
        int bodyFoundAt = -1;
        ByteArray byteHdr = null;

        int sol = s;            // Start Of Line inclusive
        int eol;                // End Of Line exclusive
        for (int i=s; i<msgBuffer.length; i++) {
            byte b = msgBuffer[i];
            if (b == (byte)'\n') {

                if (i > 0 && msgBuffer[i-1] == (byte)'\r') {
                    eol = i-1;
                } else {
                    eol = i;
                }

                // Reached message-body?
                if (eol==sol) {
                    // Found line with only CRLF (or just LF) => End of headers
                    if (i+1 < msgBuffer.length) {
                        // Still more bytes to read => Body found
                        bodyFoundAt = i+1;
                    }
                    break;
                }

                // Handle folding
                if ((msgBuffer[sol] == (byte)' ' ||
                        msgBuffer[sol] == (byte)'\t') &&
                        byteHdr != null) {

                    // Append this folded line to the last read header
                    byteHdr.append(msgBuffer,sol,eol-sol);

                } else {

                    // Add new header line
                    byteHdr = new ByteArray(msgBuffer,sol,eol-sol);
                    headers.add(byteHdr);

                }
                sol = i+1;

            }

        }

        if (log.isDebugEnabled())
            log.debug("Collected "+headers.size() + " headers");
        SIPMessage sipmsg = parseMessage(headers);

        if (sipmsg==null) {
            log.warn("parseMessage failed)");
            throw new ParseException("Error parsing headers",0);
        }

        int contentLength;
        if (sipmsg.getContentLength() == null) {
            contentLength = -1;
        } else {
            contentLength = sipmsg.getContentLength().getContentLength();
        }


        // todo:
        boolean streamOriented = false;
        int bodyLen;
        if (readBody) {

            if (bodyFoundAt > 0) {

                // Body found

                if (!streamOriented) {
                    // For packet oriented protocols like UDP let
                    // body be rest of packet. RFC3261 18.3
                    bodyLen = msgBuffer.length - bodyFoundAt;

                    if (contentLength >= 0) {

                        // Check validity of Content-length
                        if (contentLength > bodyLen) {
                            log.warn("Premature end of message. Packet dropped.");
                            throw new ParseException("Premature end of message.",
                                    msgBuffer.length);
                        } else if (contentLength < bodyLen) {
                            // Discard extra info at end of packet
                            if (log.isDebugEnabled())
                                log.debug("Extra bytes at end of message discarded.");
                            bodyLen = contentLength;
                        }

                    }

                } else {

                    if (contentLength < 0) {
                        // Content-length is mandatory for stream
                        // oriented protocols. RFC3261 18.3

                    }
                    log.error("Stream protocol not supported (yet)");
                    throw new ParseException("Stream protocol not supported (yet)",0);

                }

                byte[] body = new byte[bodyLen];
                System.arraycopy(msgBuffer, bodyFoundAt,
                        body, 0, body.length);

                if (log.isDebugEnabled())
                    log.debug("Message body found");
                sipmsg.setMessageContent(body);

            } else {

                // No body found
                if (!streamOriented) {

                    // Check validity of Content-length
                    if (contentLength > 0) {
                        log.warn("No body found, but Content-Length is non-zero. Packet dropped.");
                        throw new ParseException("No body found, but Content-Length is non-zero. Packet dropped.",
                                msgBuffer.length);
                    }

                } else {

                    // Content-length is mandatory for stream
                    // oriented protocols. RFC3261 18.3
                    if (contentLength != 0) {
                        log.warn("No body found, but Content-Length is non-zero or missing. Packet dropped.");
                        throw new ParseException("No body found, but Content-Length is non-zero or missing. Packet dropped.",
                                msgBuffer.length);

                    }
                    log.error("Stream protocol not supported (yet)");
                    throw new ParseException("Stream protocol not supported (yet)",0);

                }

            }
        }

        sipmsg.setSize(msgBuffer.length);
        return sipmsg;

    }



    // mmath: new version of parseMessage
    // Todo: Do not use ParseException to handle unknown(unparsed) headers
    private SIPMessage parseMessage(List<ByteArray> headers)
            throws ParseException {
        if (log.isDebugEnabled())
            log.debug("Enter: parseMessage() size=" + headers.size());
        // Message must at least have a start-line (Request-line or Status-line)
        if (headers.size() < 1) {
            log.warn("No headers to parse");
            throw new ParseException("No headers to parse",0);
        }


        if (rawMessage == null) {
            rawMessage = "Raw message unavailable".getBytes();
        }


        SIPMessage sipmsg;

        Iterator<ByteArray> i = headers.listIterator();

        ByteArray firstLine = i.next();
        if (firstLine.startsWith(SIPConstants.SIP_VERSION_STRING)) {
            // Starts with SIP/2.0 => Status-Line = SIP Response

            sipmsg = new SIPResponse();
            try {
                StatusLine sl = new StatusLineParser(firstLine + "\n").parse();
                ((SIPResponse)sipmsg).setStatusLine(sl);
            } catch (ParseException ex) {
                if (this.parseExceptionListener != null) {
                    this.parseExceptionListener.handleException(
                        ex,
                        sipmsg,
                        StatusLine.class,
                        firstLine.toString(),
                        new String(rawMessage));
                } else
                    throw ex;

            }

        } else {
            // Otherwise => Request-Line
            sipmsg = new SIPRequest();
            try {
                RequestLine rl =
                    new RequestLineParser(firstLine + "\n").parse();
                ((SIPRequest) sipmsg).setRequestLine(rl);
            } catch (ParseException ex) {
                if (this.parseExceptionListener != null)
                    this.parseExceptionListener.handleException(
                        ex,
                        sipmsg,
                        RequestLine.class,
                        firstLine.toString(),
                        new String(rawMessage));
                else
                    throw ex;

            }
        }

        HeaderParser hdrParser;
        while (i.hasNext()) {
            ByteArray hdrLine = i.next();

            // Skip empty lines
            if (hdrLine == null || hdrLine.isEmptyLine())
                continue;

            try {
                hdrParser = ParserFactory.createParser(hdrLine + "\n");
            } catch (ParseException e) {
                this.parseExceptionListener.handleException(e,sipmsg,
                        null,hdrLine.toString(),new String(rawMessage));
                continue;
            }
            try {
                SIPHeader sipHeader = hdrParser.parse();
                sipmsg.attachHeader(sipHeader, false);
            } catch (ParseException e) {
                // mmath: todo: temp, remove later
                if (log.isDebugEnabled())
                    log.debug("ParseException at offset: "+e.getErrorOffset());
                if (this.parseExceptionListener != null) {
                    String hdrName = Lexer.getHeaderName(hdrLine.toString());
                    Class hdrClass = NameMap.getClassFromName(hdrName);
                    try {
                        if (hdrClass == null) {
                            hdrClass =
                                Class.forName(
                                    PackageNames.SIPHEADERS_PACKAGE
                                        + ".ExtensionHeaderImpl");
                        }
                        this.parseExceptionListener.handleException(e,sipmsg,
                            hdrClass, hdrLine.toString(), new String(rawMessage));

                    } catch (ClassNotFoundException ex1) {
                        InternalErrorHandler.handleException(ex1);
                    }
                }
            }

        }

        return sipmsg;

    }






    ////////////////////////////////


    /**
     * Parse an address (nameaddr or address spec)  and return and address
     * structure.
     * @param address is a String containing the address to be parsed.
     * @return a parsed address structure.
     * @since v1.0
     * @exception  ParseException when the address is badly formatted.
     */
    public AddressImpl parseAddress(String address) throws ParseException {
        AddressParser addressParser = new AddressParser(address);
        return addressParser.address();
    }

    /**
     * Parse a host:port and return a parsed structure.
     * @param hostport is a String containing the host:port to be parsed
     * @return a parsed address structure.
     * @since v1.0
     * @exception ParseException when the address is badly formatted.
    public HostPort parseHostPort(String hostport) throws ParseException {
        Lexer lexer = new Lexer("charLexer", hostport);
        return new HostNameParser(lexer).hostPort();

    }
     */

    /**
     * Parse a host name and return a parsed structure.
     * @param host is a String containing the host name to be parsed
     * @return a parsed address structure.
     * @since v1.0
     * @exception ParseException when the hostname is badly formatted.
     */
    public Host parseHost(String host) throws ParseException {
        Lexer lexer = new Lexer("charLexer", host);
        return new HostNameParser(lexer).host();

    }

    /**
     * Parse a telephone number return a parsed structure.
     * @param telephone_number is a String containing
     * the telephone # to be parsed
     * @return a parsed address structure.
     * @since v1.0
     * @exception ParseException when the address is badly formatted.
     */
    public TelephoneNumber parseTelephoneNumber(String telephone_number)
        throws ParseException {
        // Bug fix contributed by Will Scullin
        return new URLParser(telephone_number).parseTelephoneNumber();

    }

    /**
     * Parse a  SIP url from a string and return a URI structure for it.
     * @param url a String containing the URI structure to be parsed.
     * @return A parsed URI structure
     * @exception ParseException  if there was an error parsing the message.
     */

    public SipUri parseSIPUrl(String url) throws ParseException {
        try {
            return (SipUri) new URLParser(url).parse();
        } catch (ClassCastException ex) {
            throw new ParseException(url + " Not a SIP URL ", 0);
        }
    }

    /**
     * Parse a  uri from a string and return a URI structure for it.
     * @param url a String containing the URI structure to be parsed.
     * @return A parsed URI structure
     * @exception ParseException  if there was an error parsing the message.
     */

    public GenericURI parseUrl(String url) throws ParseException {
        return new URLParser(url).parse();
    }

    /**
     * Parse an individual SIP message header from a string.
     * @param header String containing the SIP header.
     * @return a SIPHeader structure.
     * @exception ParseException  if there was an error parsing the message.
     */
    public SIPHeader parseSIPHeader(String header) throws ParseException {
        header += "\n\n";
        // Handle line folding.
        StringBuffer nmessage = new StringBuffer(header.length() + 5);

        // eat leading spaces and carriage returns (necessary??)
        int i = 0;
        while (header.charAt(i) == '\n'
            || header.charAt(i) == '\t'
            || header.charAt(i) == ' ')
            i++;
        for (; i < header.length(); i++) {
            if (i < header.length() - 1
                && (header.charAt(i) == '\n'
                    && (header.charAt(i + 1) == '\t'
                        || header.charAt(i + 1) == ' '))) {
                nmessage.append(' ');
                i++;
            } else {
                nmessage.append(header.charAt(i));
            }
        }

        nmessage.append('\n');

        HeaderParser hp = ParserFactory.createParser(nmessage.toString());
        if (hp == null)
            throw new ParseException("could not create parser", 0);
        return hp.parse();
    }

    /**
     * Parse the SIP Request Line
     * @param  requestLine a String  containing the request line to be parsed.
     * @return  a RequestLine structure that has the parsed RequestLine
     * @exception ParseException  if there was an error parsing the requestLine.
     */

    public RequestLine parseSIPRequestLine(String requestLine)
        throws ParseException {
        requestLine += "\n";
        return new RequestLineParser(requestLine).parse();
    }

    /**
     * Parse the SIP Response message status line
     * @param statusLine a String containing the Status line to be parsed.
     * @return StatusLine class corresponding to message
     * @exception ParseException  if there was an error parsing
     * @see StatusLine
     */

    public StatusLine parseSIPStatusLine(String statusLine)
        throws ParseException {
        statusLine += "\n";
        return new StatusLineParser(statusLine).parse();
    }

}
/*
 * $Log: StringMsgParser.java,v $
 * Revision 1.16  2006/09/17 14:34:51  jbemmel
 * cleaned up URL parsing
 *
 * Revision 1.15  2006/09/17 10:31:33  jbemmel
 * when asked to parse a sip url, parse a sipURL
 *
 * Revision 1.14  2006/09/10 21:14:35  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Fixes the IMS header class cast problems.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 * Revision 1.13 2006/07/13 09:02:18 mranga Issue
 * number: Obtained from: Submitted by: jeroen van bemmel Reviewed by: mranga
 * Moved some changes from jain-sip-1.2 to java.net
 * 
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 * 
 * Revision 1.4 2006/06/19 06:47:27 mranga javadoc fixups
 * 
 * Revision 1.3 2006/06/16 15:26:28 mranga Added NIST disclaimer to all public
 * domain files. Clean up some javadoc. Fixed a leak
 * 
 * Revision 1.2 2006/04/23 15:11:52 mranga *** empty log message ***
 * 
 * Revision 1.1.1.1 2005/10/04 17:12:36 mranga
 * 
 * Import
 * 
 * 
 * Revision 1.11 2005/04/15 19:17:07 mranga Issue number: Obtained from:
 * Submitted by: mranga
 * 
 * Fixed maxforwards test Reviewed by: CVS:
 * ---------------------------------------------------------------------- CVS:
 * Issue number: CVS: If this change addresses one or more issues, CVS: then
 * enter the issue number(s) here. CVS: Obtained from: CVS: If this change has
 * been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 * 
 * Revision 1.10 2005/04/04 10:03:12 dmuresan Optimized
 * StringMsgParser.parseSIPHeader() to use StringBuffer for concatenation.
 * 
 * Revision 1.9 2004/02/29 00:46:34 mranga Reviewed by: mranga Added new
 * configuration property to limit max message size for TCP transport. The
 * property is gov.nist.javax.sip.MAX_MESSAGE_SIZE
 * 
 * Revision 1.8 2004/02/18 14:33:02 mranga Submitted by: Bruno Konik Reviewed
 * by: mranga Remove extraneous newline in encoding messages. Test for empty sdp
 * announce rather than die with null when null is passed to sdp announce
 * parser. Fixed bug in checking for \n\n when looking for message end.
 * 
 * Revision 1.7 2004/02/13 19:20:09 mranga Reviewed by: mranga minor fix for
 * error callback.
 * 
 * Revision 1.6 2004/01/22 13:26:32 sverker Issue number: Obtained from:
 * Submitted by: sverker Reviewed by: mranga
 * 
 * Major reformat of code to conform with style guide. Resolved compiler and
 * javadoc warnings. Added CVS tags.
 * 
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 * 
 */
