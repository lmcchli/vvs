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
/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)       *
 ******************************************************************************/
package gov.nist.javax.sip.parser;
/*
 *
 * Lamine Brahimi and Yann Duponchel (IBM Zurich) noticed that the parser was
 * blocking so I threw out some cool pipelining which ran fast but only worked
 * when the phase of the moon matched its mood. Now things are serialized
 * and life goes slower but more reliably.
 *
 */
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import java.text.ParseException;
import java.io.*;

import org.apache.log4j.Logger;

/**
 * This implements a pipelined message parser suitable for use
 * with a stream - oriented input such as TCP. The client uses
 * this class by instatiating with an input stream from which
 * input is read and fed to a message parser.
 * It keeps reading from the input stream and process messages in a
 * never ending interpreter loop. The message listener interface gets called
 * for processing messages or for processing errors. The payload specified
 * by the content-length header is read directly from the input stream.
 * This can be accessed from the SIPMessage using the getContent and
 * getContentBytes methods provided by the SIPMessage class. 
 *
 * @version 1.2 $Revision: 1.18 $ $Date: 2006/07/13 09:02:10 $
 *
 * @author  M. Ranganathan 
 *
 * @see  SIPMessageListener
 */
public final class PipelinedMsgParser implements Runnable {

    private static final Logger log = Logger.getLogger(PipelinedMsgParser.class);

    private final SIPMessageListener sipMessageListener;
    private final BufferedInputStream inputStream;
    private final int maxMessageSize;

    private int sizeCounter;


    /**
     * Constructor when we are given a message listener and an input stream
     * (could be a TCP connection or a file)
     * @param sipMessageListener Message listener which has
     * methods that  get called
     * back from the parser when a parse is complete
     * @param in Input stream from which to read the input.
     * @param maxMessageSize
     */
    public PipelinedMsgParser(SIPMessageListener sipMessageListener,
                              BufferedInputStream in,
                              int maxMessageSize ) {

        this.inputStream = new BufferedInputStream(in);
        this.sipMessageListener = sipMessageListener;
        this.maxMessageSize = maxMessageSize;

    }


    /**
     * Start reading and processing input.
     */
    public void processInput() {
        run();
    }


    /**
     * Create a new pipelined parser from an existing one.
     * @return A new pipelined parser that reads from the same input
     * stream.
     */
    protected Object clone() {
        return new PipelinedMsgParser(
                sipMessageListener, inputStream, maxMessageSize);

    }

    /**
     * read a line of input (I cannot use buffered reader because we
     * may need to switch encodings mid-stream!
     */
    private String readLine(BufferedInputStream inputStream) throws IOException, PipelinedParserException {
        StringBuffer retval = new StringBuffer("");
        while (true) {
            try {
                char ch;
                int i = inputStream.read();
                if (i == -1) {
                    throw new PipelinedParserException("Received incomplete data from stream (end of stream): "
                    		+ retval.toString());
                } else
                    ch = (char) i;
                // reduce the available read size by 1 ("size" of a char).
                if ( maxMessageSize > 0 ) {
                    sizeCounter --;
                    if (sizeCounter <= 0)
                    	throw new PipelinedParserException("Max message size exceeded from stream (" + 
                    			maxMessageSize + " byte(s))");
                }
                if (ch != '\r')
                    retval.append(ch);
                if (ch == '\n') {
                    break;
                }
            } catch (IOException ex) {
                throw ex;
            }
        }
        return retval.toString();
    }


    /**
     * This is input reading thread for the pipelined parser.
     * You feed it input through the input stream (see the constructor)
     * and it calls back an event listener interface for message
     * processing or error.
     * It cleans up the input - dealing with things like line continuation
     */
    public void run() {
        // TODO: Optimize away the byte->char->byte conversion
        // TODO: Reimplement max-time when reading one message (4s?) or
        // just use the channel timeout?
        try {
            while (true) {
                this.sizeCounter = this.maxMessageSize;
                StringBuffer inputBuffer = new StringBuffer();

                if (log.isDebugEnabled())
                    log.debug("Start parsing input");

                String line1;
                String line2;

                while (true) {
                    try {
                        line1 = readLine(inputStream);

                        // ignore blank lines.
                        if (line1.equals("\n")) {
//                            if (log.isDebugEnabled())
//                                log.debug("Discarding " + line1);
//                            continue;
                        } else
                            break;
                    } catch (PipelinedParserException ex) {
                    	if (log.isDebugEnabled()) {
                    		log.debug("Data error while stripping leading blank lines: " + 
                    				ex.getMessage());
                    	}
                    	return;
                    } catch (IOException ex) {
                        if (log.isDebugEnabled())
                            log.debug("IOException while stripping leading " +
                                    "blank lines", ex);
//                        this.rawInputStream.stopTimer();
                        return;

                    }
                }

                inputBuffer.append(line1);
                // Guard against bad guys.
//                this.rawInputStream.startTimer();


                while (true) {
                    try {
                        line2 = readLine(inputStream);
                        inputBuffer.append(line2);
                        if (line2.trim().equals(""))
                            break;
                    } catch (PipelinedParserException ex) {
                    	if (log.isDebugEnabled()) {
                    		log.debug("Data error while reading headers: " + 
                    				ex.getMessage());
                    	}
                    	return;
                    } catch (IOException ex) {
//                        this.rawInputStream.stopTimer();
                        if (log.isDebugEnabled())
                                log.debug("IOException while reading headers", ex);
                        return;

                    }
                }

                // Stop the timer that will kill the read.
//                this.rawInputStream.stopTimer();
                inputBuffer.append(line2);
                StringMsgParser smp = new StringMsgParser(sipMessageListener);
                smp.readBody = false;
                SIPMessage sipMessage;

                try {
                    sipMessage = smp.parseSIPMessage(inputBuffer.toString());
                    if (sipMessage == null) {
//                        this.rawInputStream.stopTimer();
                        log.warn("parseSIPMEssage returned null for:\n" +
                                inputBuffer.toString());
                        continue;
                    }
                } catch (ParseException ex) {
                    log.warn("ParseException in parseSIPMEssage for:\n" +
                            inputBuffer.toString(), ex);
                    // TODO: Is that ok
                    // Just ignore the parse exception.
                    continue;
                }

                if (log.isDebugEnabled())
                    log.debug("Completed parsing message");

                ContentLength cl =
                        (ContentLength) sipMessage.getContentLength();
                int contentLength;
                if (cl != null) {
                    contentLength = cl.getContentLength();
                    if (log.isDebugEnabled())
                        log.debug("Content-Length header found with value=" + contentLength);
                } else {
                    contentLength = 0;
                    if (log.isDebugEnabled())
                        log.debug("Content-Length header not found, assuming 0");
                }

                if (log.isDebugEnabled()) {
                    log.debug("sizeCounter=" + sizeCounter +
                            " maxMessageSize=" + maxMessageSize);
                }

                if (contentLength == 0) {
                    sipMessage.removeContent();
                } else if ( maxMessageSize == 0 ||
                        contentLength < this.sizeCounter ) {
                    byte[] message_body = new byte[contentLength];
                    int nread = 0;
                    while (nread < contentLength ) {
                        // Start my starvation timer.
                        //This ensures that the other end
                        //writes at least some data in
                        //or we will close the pipe from
                        //him. This prevents DOS attack
                        //that takes up all our connections.
//                        this.rawInputStream.startTimer();
                        try {
                            int readlength =
                                    inputStream.read(
                                            message_body,
                                            nread,
                                            contentLength - nread);
                            if (readlength > 0) {
                                nread += readlength;
                            } else {
                                break;
                            }
                        } catch (IOException ex) {
                            if (log.isDebugEnabled())
                                log.debug("IOException while reading body", ex);
                            break;
                        } finally {
                            // Stop my starvation timer.
//                            this.rawInputStream.stopTimer();
                        }
                    }
                    sipMessage.setMessageContent(message_body);
                }
                // Content length too large - process the message and
                // return error from there.
                if (sipMessageListener != null) {
                    try {
                        sipMessageListener.processMessage(sipMessage);
                    } catch (Exception ex) {
                        log.warn("Exception while processing message", ex);

                        // fatal error in processing - close the
                        // connection.
                        break;
                    }
                }
            }
        } finally {
            try {
                if (log.isDebugEnabled())
                    log.debug("PipelinedMsgParser finished");
                inputStream.close();
            } catch (IOException e) {
                //
            }
        }
    }
    
    
    /**
     * PipelinedParserException is an internal exception for reporting incomplete
     * data transmission from the remote end.
     *
     */
    private class PipelinedParserException extends Exception {
    	
    	public PipelinedParserException(String message) {
    		super(message);
    	}
    	
    }
}
