/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SessionDescription;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.ArrayList;


/**
 * This class parses incoming MRCP/RTSP responses and requests.
 * Ths MessageParser reads data from an {@link InputStream}.
 * The data is parsed into MRCP/RTSP messages.
 */
public class MessageParser {
    private static ILogger logger = ILoggerFactory.getILogger(MessageParser.class);
    private static final String SEPARATOR = "</null>";

    /**
     * Parses an {@link InputStream} and returns {@link RtspMessage}.
     * This is the public parse method.
     * @param input
     * @return
     * @throws Exception
     */
    public static RtspMessage parse(InputStream input) throws Exception {
        if (logger.isDebugEnabled()) logger.debug("--> parse(input stream)");
        RtspMessage message = parseRtspHeader(input);
        if (message != null) {
          parseRtspHeader(input, message);
        }
        if (logger.isDebugEnabled()) logger.debug("<-- parse(input stream)");
        return message;
    }

    /**
     * Determines the type of the RTSP message.
     * Checks the first header line and creates either a {@link RtspResponse}
     * or a {@link RtspRequest}.
     * @param input the message input stream.
     * @return a message object.
     * @throws Exception
     */
    private static RtspMessage parseRtspHeader(InputStream input) throws Exception {
        if (logger.isDebugEnabled()) logger.debug("--> parseRtspHeader(input stream)");
        RtspMessage message = null;

        // We will read the first line of the message header so
        // that we can determine if this is a request or response.
        String line = readLine(input);
        if (logger.isDebugEnabled()) logger.debug("RTSP Header: [" + line + "]");
        if (line != null && line.length() > 0) {
            // Retrieve the first line of the message
            StringTokenizer tokens = new StringTokenizer(line);
            // From the first token of the first line we can determine
            // if the message is a response or a request.
            // Message  | Request       | Response
            // ---------|---------------|------------
            // Token #1 | <version>     | <command>
            // Token #2 | <status code> | <url>
            // Token #3 | <status text> | <version>
            String firstToken = tokens.nextToken();
            String secondToken = tokens.nextToken();
            String thirdToken = tokens.nextToken();
            while (tokens.hasMoreTokens()) thirdToken += " " + tokens.nextToken();
            if (firstToken.equals(RtspMessage.rtspVersion)) {
                if (logger.isDebugEnabled()) logger.debug("The first token is 'RTSP/1.0' hence this is a reponse.");
                message = new RtspResponse(Integer.parseInt(secondToken), thirdToken);
            } else {
                if (logger.isDebugEnabled()) logger.debug("The message is assumed to be a request");
                message = new RtspRequest(firstToken, secondToken);
            }        }
        if (logger.isDebugEnabled()) logger.debug("<-- parseRtspHeader(input stream)");
        return message;
    }

    /**
     * Parses the contents of the RTSP header.
     * The parser parses the header contents and determines the content type of the message.
     * @param input the message input stream.
     * @param message the message object (which currently is processed).
     * @throws Exception
     */
    private static void parseRtspHeader(InputStream input, RtspMessage message) throws Exception {
        if (logger.isDebugEnabled()) logger.debug("--> parseRtspHeader(input stream, message)");
        // Get the rest the rest of the header and determine
        // if there is content attached and which type of content.
        // The content can be either SDP or MRCP.
        String line;
        int contentLength = 0;
        MessageContentType contentType = MessageContentType.UNKNOWN;
        // The header consist of a list of name-values:
        //  <name>: <value>
        //  <name>: <value>
        // The list is terminated by an empty line
        do {
            // Read a line line
            line = readLine(input);
            // If the line is not empty it should be parsed
            if (line != null && line.length() > 0) {
                // Searching for the "separator"
                int pos = line.indexOf(":");
                if (pos > 0) {
                    // Split the string into <name> <value>
                    String key = line.substring(0, pos);
                    String value = line.substring(pos+2);
                    // Parse content information since further parsing
                    // relies upon that infomation.
                    // Test for content type
                    if (key.equals("Content-Type")) {
                        if (value.equals("application/sdp")) {
                            contentType = MessageContentType.SDP;
                        }
                        if (value.equals("application/mrcp")) {
                            contentType = MessageContentType.MRCP;
                        }
                    }
                    // Test for length of content
                    if (key.equals("Content-Length")) {
                        contentLength = Integer.parseInt(value);
                    }
                    // Add the header line to the parsed message
                    message.setHeaderField(key, value);
                } else {
                    // The RTSP header is malformed.
                    logger.error("Malformed line in RTSP header: [" + line + "] ':' is missing.");
                }
            }
        } while (line != null && line.length() > 0);

        // Handle content, if any ...
        if (contentLength > 0) {
            // Read the complete content data (which can be either SDP or MRCP).
            byte[] content = new byte[contentLength];
            int nOfBytesRead = input.read(content);
            if (contentLength != nOfBytesRead) {
                logger.warn("Wrong content size, expected " + contentLength + " bytes got " + nOfBytesRead);
            }

            // If attached content is SDP, parse the SDP information ...
            if (contentType == MessageContentType.SDP) {
                // Parse the SDP data
                SdpFactory.setPathName("gov.nist");
                SdpFactory sdpFactory = SdpFactory.getInstance();
                SessionDescription sessionDescription
                            = sdpFactory.createSessionDescription(new String(content));
                // Add the SDP to the parsed message
                message.setSDP(sessionDescription);
            }

            // If attached content is MRCP, parse the MRCP information
            if (contentType == MessageContentType.MRCP) {
                MrcpMessage mrcp = parseMrcpHeader(content);
                message.setMrcpMessage(mrcp);
            }
        }

        if (logger.isDebugEnabled()) logger.debug("<-- parseRtspHeader(input stream, message)");
    }

    /**
     * Parses an MRCP message (header and content).
     * @param data the MRCP message data
     * @return an MRCP message
     * @throws Exception
     */
    private static MrcpMessage parseMrcpHeader(byte[] data) throws Exception {
        if (logger.isDebugEnabled()) logger.debug("--> parseMrcpHeader() : ");
        // Initializing local variables
        MrcpMessage mrcp = null;
        String contentType = "";
        String content = "";
        boolean parsingHeader = true;
        // Splitting the data into lines.
        ArrayList<String> message = split(data);
        // Parsing the message lines
        if (message.size() > 0) {
            // Pop the first line
            String head = message.remove(0);
            if (logger.isDebugEnabled()) logger.debug("MRCP Header: [" + head + "]");
            // Retrieve the first line of the message
            StringTokenizer tokens = new StringTokenizer(head);
            // From the first token of the first line we can determine
            // if the message is a response or an event.
            // Message  | Request      | Response        | Event
            // ---------|--------------|-----------------|------------
            // Token #1 | <method>     | <version>       | <event>
            // Token #2 | <request id> | <request id>    | <request id>
            // Token #3 | <version>    | <status code>   | <request-state>
            // Token #4 | N/A          | <request state> | <version>
            String firstToken = tokens.nextToken();
            String secondToken = tokens.nextToken();
            String thirdToken = tokens.nextToken();
            String fourthToken = tokens.hasMoreTokens() ? tokens.nextToken() : "";
            if (firstToken.equals(MrcpMessage.mrcpVersion)) {
                if (logger.isDebugEnabled()) logger.debug("Ok, this aught to be a response");
                mrcp = new MrcpResponse(Integer.parseInt(secondToken),
                        Integer.parseInt(thirdToken), fourthToken);
            } else if (fourthToken.length() == 0) {
                if (logger.isDebugEnabled()) logger.debug("Ok, this should be a request");
                mrcp = new MrcpRequest(firstToken, Integer.parseInt(secondToken));
            } else {
                if (logger.isDebugEnabled()) logger.debug("It must be an event then");
                mrcp = new MrcpEvent(firstToken, Integer.parseInt(secondToken), thirdToken);
            }
            // Once the header is processed. The rest of the message is parsed.
            for (String line : message) {
                if (SEPARATOR.equals(line)) {
                    // The MRCP header is separated (from the content) by a separator (line feed).
                    // Once the separator is encountered we just stop parsing and collects the
                    // lines.
                    // TODO: this could be implemented in a more obvious manner.
                    parsingHeader = false;
                    continue;
                }
                // Header parsing
                if (parsingHeader && line.length() > 0) {
                    // Get separator
                    int pos = line.indexOf(":");
                    if (pos > 0) {
                        // Separate into <name> <value>
                        String key = line.substring(0, pos);
                        String value = line.substring(pos + 2);
                        if (logger.isDebugEnabled()) logger.debug("Key: " + key + ", Value: " + value);
                        // Add the header line to the parsed message
                        // Test for content type
                        if (key.equals("Content-Type")) {
                            contentType = value;
                            if (logger.isDebugEnabled()) logger.debug("Content: " + contentType);
                        } else if (key.equals("Content-Length")) {
                            if (logger.isDebugEnabled()) logger.debug("Content Length: " + value);
                        } else {
                            mrcp.setHeaderField(key, value);
                        }
                    }
                } else {
                    // Just accumulate all the lines into one content string
                    if (content.length() > 0) content += MrcpMessage.nl;
                    if (line.length() > 0) content += line;
                }
            }
            // Save content ...
            mrcp.setContent(contentType, content);
        }
        if (logger.isDebugEnabled()) logger.debug("<-- parseMrcpHeader()");
        // Returning the parsed MRCP message
        return mrcp;
    }

    /**
     * Read one line from an input stream.
     * This method collects characters from the input until
     * two line breaks are received.
     * @param input
     * @return the read string.
     */
    private static String readLine(InputStream input) {
        if (logger.isDebugEnabled()) logger.debug("--> readLine(input stream)");
        int c;
        int nlCounter = 0;
        String line = "";
        try {
            while ((c = input.read()) != -1) {
                switch (c) {
                    case '\r':
                        nlCounter++;
                        break;
                    case '\n':
                        nlCounter++;
                        break;
                    default:
                        line += (char)c;
                        break;
                }
                // Ok, two line feeds, we are done.
                if (nlCounter == 2) break;
            }
        } catch (Exception exception) {
            if (logger.isDebugEnabled()) logger.debug("<-- readLine(input stream) : failed");
            return null;
        }
        if (logger.isDebugEnabled()) logger.debug("<-- readLine(input stream) : [" + line + "]");
        return line;
    }

    /**
     * Converting a message (vector of bytes) into a list of strings.
     *
     * @param buffer contains the message.
     * @return the buffer converted into lines.
     */
    private static ArrayList<String> split(byte[] buffer) {
        if (logger.isDebugEnabled()) logger.debug("--> split()");
        ArrayList<String> strings = new ArrayList<String>();
        int nlCounter = 0;
        String line = "";

        // Iterating over the buffer. A line is terminated by two line feeds.
        // The message header and content is separated by an empty line.
        for (byte b : buffer) {
            switch (b) {
                case '\r':
                    nlCounter++;
                    break;
                case '\n':
                    nlCounter++;
                    break;
                default:
                    line += (char) b;
                    break;
            }
            // Checking for end of line ...
            if (nlCounter == 2) {
                // If line is empty, we have found the separator between
                // header and content.
                if (line.length() == 0) line = SEPARATOR;
                // Add the line
                strings.add(line);
                // Reset line and counter ...
                line = "";
                nlCounter = 0;
            }
        }
        // Must handle the case where we missed the last line
        if (line.length() > 0)
            strings.add(line);
        if (logger.isDebugEnabled()) logger.debug("<-- split()");
        return strings;
    }
}
