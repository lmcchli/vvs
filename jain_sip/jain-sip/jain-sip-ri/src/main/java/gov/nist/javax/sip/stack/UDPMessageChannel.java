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
/*****************************************************************************
 *   Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
 *****************************************************************************/

package gov.nist.javax.sip.stack;

import java.net.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.javax.sip.message.*;
import java.io.IOException;
import java.lang.String;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sip.address.Hop;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.ListeningPoint;

import org.apache.log4j.Logger;

/*
 * Kim Kirby (Keyvoice) suggested that duplicate checking
 * should be added to the stack (later removed). Lamine Brahimi suggested a
 * single threaded behavior flag be added to this. Niklas Uhrberg suggested that
 * thread pooling support be added to this for performance and resource
 * management. Peter Parnes found a bug with this code that was sending it into
 * an infinite loop when a bad incoming message was parsed.
 * Bug fix by viswashanti.kadiyala@antepo.com. Hagai Sela addded fixes
 * for NAT traversal. Jeroen van Bemmel fixed up for buggy clients (such
 * as windows messenger) and added code to return BAD_REQUEST.
 *  David Alique fixed an address recording bug.
 * Jeroen van Bemmel fixed a performance issue where the stack was doing
 * DNS lookups (potentially unnecessary). Ricardo Bora (Natural Convergence )
 * added code that prevents the stack from exitting when an exception is encountered.
 *
 */

/**
 * This is the UDP Message handler that gets created when a UDP message needs to
 * be processed. The message is processed by creating a String Message parser
 * and invoking it on the message read from the UDP socket. The parsed structure
 * is handed off via a SIP stack request for further processing. This stack
 * structure isolates the message handling logic from the mechanics of sending
 * and recieving messages (which could be either udp or tcp.
 *
 *
 * @author M. Ranganathan <br/>
 *
 *
 *
 * @version 1.2 $Revision: 1.38 $ $Date: 2006/11/02 04:06:17 $
 */
public class UDPMessageChannel extends MessageChannel implements
		ParseExceptionListener, Runnable {

    private static final Logger log = Logger.getLogger(UDPMessageChannel.class);

    // TODO: Why do we create a new parser for each packet to parse?
    /**
	 * The parser we are using for messages received from this channel.
	 */
	protected StringMsgParser myParser;

    /** Remote address */
	private InetAddress peerAddress;

    /** Remote port */
    private int peerPort;

    private Thread myThread = null;
    static AtomicInteger threadIndex = new AtomicInteger(0);

    private boolean isRunning = false;

	/**
	 * Constructor
	 *
	 * @param messageProcessor
	 *            is the creating message processor.
	 */
	protected UDPMessageChannel(UDPMessageProcessor messageProcessor) {
		super(messageProcessor);
	}

	/**
	 * Constructor. We create one of these when we send out a message.
	 *
	 * @param targetAddr
	 *            INET address of the place where we want to send messages.
	 * @param port
	 *            target port (where we want to send the message).
	 */
	protected UDPMessageChannel(InetAddress targetAddr, int port,
                                UDPMessageProcessor messageProcessor) {
        super(messageProcessor);
		peerAddress = targetAddr;
		peerPort = port;
		if (log.isDebugEnabled()) {
                    log.debug("Creating message channel " +
                              targetAddr.getHostAddress() + ":" + port);
		}
	}

    /**
     * Start message processing thread.
     */
    public void start() {
        myThread = new Thread(this);
        myThread.setName("UDPMessageChannelThread-" + threadIndex.getAndIncrement());
        myThread.setDaemon(true);
        isRunning = true;
        myThread.start();
    }

    /**
     * Stop message processing thread.
     */
    public void stop() {
        isRunning = false;
        myThread.interrupt();
    }

	/**
	 * Run method specified by runnnable.
	 */
	public void run() {
        // TODO: Investigate what auditor does!
        // Ask the auditor to monitor this thread
		ThreadAuditor.ThreadHandle threadHandle =
                getMessageProcessor().getListeningPoint().getSipStack().
                        getThreadAuditor().addCurrentThread();

		while (isRunning) {
			// Create a new string message parser to parse the list of messages.
			if (myParser == null) {
				myParser = new StringMsgParser();
                // TODO: Why handle exceptions through listener?
                myParser.setParseExceptionListener(this);
			}

            // Send a heartbeat to the thread auditor
            threadHandle.ping();

            try {
                DatagramPacket packet =
                        ((UDPMessageProcessor)getMessageProcessor()).takeMessage();
                processIncomingDataPacket(packet);
            } catch (InterruptedException ex) {
                if (!isRunning) {
                    if (log.isDebugEnabled())
                        log.debug(myThread.getName() + " was stopped.");
                    return;
                } else {
                    if (log.isDebugEnabled())
                        log.debug(myThread.getName() + " was interrupted but not stopped.");
                }
            }

        }
    }

    /**
     * Process an incoming datagram
     *
     * @param packet
     *            is the incoming datagram packet.
     */
    private void processIncomingDataPacket(DatagramPacket packet)
    {
        this.peerAddress = packet.getAddress();
        int packetLength = packet.getLength();
        byte[] bytes = packet.getData();
        byte[] msgBytes = new byte[packetLength];
        // TODO: Is it possible to remove copy?
        System.arraycopy(bytes, 0, msgBytes, 0, packetLength);

        if (packetLength < 1) {
            if (log.isInfoEnabled())
                log.info("Dropping message (Empty packet)");
            return;
        }

        if (log.isDebugEnabled()) {
            String msgString = new String(msgBytes, 0, packetLength);
            log.debug("processIncomingDataPacket: peerAddress=" +
                    peerAddress.getHostAddress() + ":" + packet.getPort() +
                    "packetLength=" + packetLength + "\n" + msgString);
        }

        SIPMessage sipMessage;
        try {
            sipMessage = myParser.parseSIPMessage(msgBytes);
        } catch (ParseException ex) {

            // TODO: This wont work since peerPort not retrieved correctly.
            // Try to separate parseSIPMessage i two: one that gives
            // parse exception since message cannot be parsed at all
            // and one that returns a message but it is considered invalid and
            // a 400 should be sent in response!

            if (log.isInfoEnabled())
                log.info("Dropping message due to ParseException: " +
                        new String(msgBytes), ex);

            // JvB: send a 400 response for requests (except ACK)
            // Currently only UDP, @todo also other transports
            String msgString = new String(msgBytes, 0, packetLength);
            if ( !msgString.startsWith("SIP/") && !msgString.startsWith("ACK ") ) {

                String badReqRes = createBadReqRes( msgString, ex );
                if (badReqRes!=null) {
                    if (log.isDebugEnabled())
                        log.debug( "Sending automatic 400 Bad Request: " + badReqRes);
                    try {
                        this.sendMessage(badReqRes.getBytes());
                    } catch (IOException e) {
                        log.warn(e);
                    } catch (Exception ex1) {
                        if (log.isInfoEnabled())
                            log.info("sendMessage exception " + ex1);
                    }
                } else {
                    if (log.isDebugEnabled())
                        log.debug("Could not formulate automatic 400 Bad Request" );
                }
            }

            return;

        } catch (Exception ex) {
            log.error("Dropping message due to unexpected exception", ex);
            return;
        } finally {
            myParser = null; // let go of the parser reference.
        }

        // No parse exception but null message - reject it and
        // march on (or return).
        // exit this message processor if the message did not parse.

        if (sipMessage == null) {
            if (log.isInfoEnabled())
                log.info("Dropping message (Parsed message is null):\n" +
                         new String(msgBytes));
            return;
        }

        // Check for the required headers.
        ViaList viaList = sipMessage.getViaHeaders();
        if (sipMessage.getFrom() == null
            || sipMessage.getTo() == null
            || sipMessage.getCallId() == null
            || sipMessage.getCSeq() == null
            || sipMessage.getViaHeaders() == null) {

            if (log.isInfoEnabled())
                log.info("Dropping message (At least one required header missing in message):\n" +
                         new String(msgBytes));

            return;
        }

        // For a request first via header tells where the message
        // is coming from.
        // For response, just get the port from the packet.
        if (sipMessage instanceof SIPRequest) {
            Via v = (Via) viaList.getFirst();
            Hop hop = getAddressResolver().resolveAddress(v.getHop());
            this.peerPort = hop.getPort();

            try {
                // Check to see if the received parameter matches
                // the peer address and tag it appropriately.

                // JvB: Better not do a DNS lookup here, this is costly
                // InetAddress sentByAddress = InetAddress.getByName(hop.getHost());

                boolean hasRPort = v.hasParameter(Via.RPORT);
                if (hasRPort || !hop.getHost().equals(this.peerAddress.getHostAddress()) ) {
                    v.setParameter(Via.RECEIVED, this.peerAddress.getHostAddress());
                }

                if (hasRPort) {
                    v.setParameter(Via.RPORT,
                                   Integer.toString(packet.getPort()));
                }
            } catch (java.text.ParseException ex1) {
                log.warn("Parse error",ex1);
                InternalErrorHandler.handleException(ex1);
            }

        } else {
            this.peerPort = packet.getPort();
        }

        if (sipMessage instanceof SIPRequest) {
            SIPRequest sipRequest = (SIPRequest) sipMessage;

            SIPServerTransaction sipServerTransaction =
                    getMessageProcessor().getListeningPoint().getSipStack()
                            .getSIPServerTransactionForRequest(sipRequest, this);

            if (log.isDebugEnabled()) log.debug(
                    "---- sipServerTransaction = " + sipServerTransaction);

            if (sipServerTransaction != null && sipServerTransaction.acquireSem()) {
                if (log.isDebugEnabled()) {
                    log.debug("About to process request: "
                            + sipRequest.getFirstLine() + "/"
                            + sipServerTransaction);
                }

                try {
                    sipServerTransaction.processRequest(sipRequest);
                } catch (Exception ex) {
                    if (log.isInfoEnabled())
                        log.info("processRequest exception " + ex);
                } finally {
                    if (!sipServerTransaction.passToListener()) {
                        sipServerTransaction.releaseSem();
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("Done processing "
                            + sipRequest.getFirstLine() + "/"
                            + sipServerTransaction);
                }
            } else {
                // Drop it if there is no transaction or if semaphore could not be acquired
                log.warn("Dropping request (No server transaction to use.)");

                if (!sipRequest.getMethod().equals(Request.ACK)) {
                    SIPResponse response = sipRequest.createResponse(Response.SERVICE_UNAVAILABLE);
                    response.addHeader(getMessageProcessor().getListeningPoint().getSipStack().createServerHeaderForStack());
                    RetryAfter retryAfter = new RetryAfter();

                    // Be a good citizen and send a decent response code back.
                    try {
                        retryAfter.setRetryAfter((int)(10 * (Math.random())));
                        response.setHeader(retryAfter);
                        response.setReasonPhrase("Service Unavailable due to high load");
                        sendMessage(response);
                        if (log.isInfoEnabled())
                            log.info("503 Service unavailable due to high load sent");
                    } catch (Exception e) {
                        log.warn("Exception while trying to send 503 " +
                                 "Service unavailable due to high load");
                    }
                }
            }

        } else {

            // Handle a SIP Response message.
            SIPResponse sipResponse = (SIPResponse) sipMessage;
            if (sipResponse.getStatusCode() == 100)
                sipResponse.getTo().removeParameter("tag");
            try {
                sipResponse.checkHeaders();
            } catch (ParseException ex) {
                if (log.isInfoEnabled())
                    log.info("Dropping response (Missing header)",ex);
                return;
            }
            SIPClientTransaction sipClientTransaction =
                    getMessageProcessor().getListeningPoint().getSipStack()
                .getSIPClientTransactionForRequest(sipResponse);

            if (sipClientTransaction != null) {
                if (sipClientTransaction.acquireSem()) {
                    try {
                        sipClientTransaction.processResponse(sipResponse);
                    } finally {
                        if (!sipClientTransaction.passToListener())
                            sipClientTransaction.releaseSem();
                    }
                } else {
                    log.warn("Dropping response (Client transaction could not be acquired)");
                }
            } else {
                DialogFilter.processResponse(
                        sipResponse,
                        getMessageProcessor().getListeningPoint().getSipProvider());
            }
        }
    }

    /**
     * Implementation of the ParseExceptionListener interface.
     *
     * @param ex
     *            Exception that is given to us by the parser.
     * @throws ParseException
     *             If we choose to reject the header or message.
     */
    public void handleException(ParseException ex, SIPMessage sipMessage,
                                Class hdrClass, String header, String message)
        throws ParseException {

        if (log.isDebugEnabled()) {
            log.debug("ParseException hdrClass=" + hdrClass + " hdr=" + header +
                    "\n" + message);
        }

        // Should we drop the message or not?
        if ((hdrClass != null)
				&& (hdrClass.equals(From.class) || hdrClass.equals(To.class)
						|| hdrClass.equals(CSeq.class)
						|| hdrClass.equals(Via.class)
						|| hdrClass.equals(CallID.class)
						|| hdrClass.equals(RequestLine.class) || hdrClass
						.equals(StatusLine.class))) {
            log.warn("ParseException hdrClass=" + hdrClass + " hdr=" +
                     header + "\n" + message);
            throw ex;
        } else {
            if (log.isInfoEnabled())
                log.info("Minor ParseException hdrClass=" + hdrClass +
                        " hdr=" + header + "\n" + message);
            sipMessage.addUnparsed(header);
        }
    }

	/**
	 * Return a reply from a pre-constructed reply. This sends the message back
	 * to the entity who caused us to create this channel in the first place.
	 *
	 * @param sipMessage
	 *            Message string to send.
	 * @throws IOException
	 *             If there is a problem with sending the message.
	 */
	public void sendMessage(SIPMessage sipMessage) throws IOException {
		byte[] msg = sipMessage.encodeAsBytes();

		long time = System.currentTimeMillis();

		sendMessage(msg, peerAddress, peerPort,
				sipMessage instanceof SIPRequest);

        // TODO: Can this be removed
        if (getMessageProcessor().getListeningPoint().getSipStack().serverLog.needsLogging(
                ServerLog.TRACE_MESSAGES))
			logMessage(sipMessage, peerAddress, peerPort, time);
	}


    /**
     * Return a reply from a pre-constructed reply. This sends the message back
     * to the entity who caused us to create this channel in the first place.
     *
     * @param message
     *            Message string to send.
     * @throws IOException
     *             If there is a problem with sending the message.
     */
    public void sendMessage(byte[] message) throws IOException {
        // TODO: Rework...
        sendMessage(message, peerAddress, peerPort, false);

    }

	/**
	 * Send a message to a specified receiver address.
	 *
	 * @param msg
	 *            message string to send.
	 * @param peerAddress
	 *            Address of the place to send it to.
	 * @param peerPort
	 *            the port to send it to.
	 * @throws IOException
	 *             If there is trouble sending this message.
	 */
	protected void sendMessage(byte[] msg, InetAddress peerAddress,
			int peerPort, boolean retry)
			throws IOException {
		// Via is not included in the request so silently drop the reply.
		if (peerPort == -1) {

                    if (log.isDebugEnabled())
                        log.debug("sendMessage: Dropping reply!");

			throw new IOException("Receiver port not set ");
		}

//        if (log.isDebugEnabled()) {
//            log.debug("sendMessage "
//                    + peerAddress.getHostAddress() + "/" + peerPort + "\n"
//                    + new String(msg));
//        }

        DatagramPacket reply = new DatagramPacket(msg, msg.length,
                peerAddress, peerPort);

        try {
            DatagramSocket sock;
            if (getMessageProcessor().getListeningPoint().getSipStack().udpFlag) {
                sock = ((UDPMessageProcessor) getMessageProcessor()).sock;

            } else {
                // bind to any interface and port.
                sock = getMessageProcessor().getNetworkLayer().createDatagramSocket();
            }
            if (log.isDebugEnabled()) {
                log.debug(
                        "sendMessage to " + peerAddress.getHostAddress() + ":"
                                + peerPort + "\n" + new String(msg));
            }
            sock.send(reply);

            if (log.isDebugEnabled())
                log.debug("sendMessage: Message sent!");

            if (!getMessageProcessor().getListeningPoint().getSipStack().udpFlag)
                sock.close();
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }
	}

	/**
	 * Return a transport string.
	 *
	 * @return the string "udp" in this case.
	 */
	public String getTransport() {
		return SIPConstants.UDP;
	}

	/**
	 * get the stack address for the stack that received this message.
	 *
	 * @return The stack address for our sipStack.
	 */
	public String getHost() {
		return getMessageProcessor().getListeningPoint().getInetAddress().getHostAddress();
	}

	/**
	 * get the port.
	 *
	 * @return Our port (on which we are getting datagram packets).
	 */
	public int getPort() {
		return getMessageProcessor().getListeningPoint().getPort();
	}

	/**
	 * get the name (address) of the host that sent me the message
	 *
	 * @return The name of the sender (from the datagram packet).
	 */
	public String getPeerName() {
		return peerAddress.getHostName();
	}

	/**
	 * get the address of the host that sent me the message
	 *
	 * @return The senders ip address.
	 */
	public String getPeerAddress() {
		return peerAddress.getHostAddress();
	}

	protected InetAddress getPeerInetAddress() {
		return peerAddress;
	}

	/**
	 * Compare two UDP Message channels for equality.
	 *
	 * @param other
	 *            The other message channel with which to compare oursleves.
	 */
	public boolean equals(Object other) {

		if (other == null)
			return false;
		boolean retval;
		if (!this.getClass().equals(other.getClass())) {
			retval = false;
		} else {
			UDPMessageChannel that = (UDPMessageChannel) other;
			retval = this.getKey().equals(that.getKey());
		}

		return retval;
	}

	public String getKey() {
		return getKey(peerAddress, peerPort, ListeningPoint.UDP);
	}

	/**
	 * Returns "false" as this is an unreliable transport.
	 */
	public boolean isReliable() {
		return false;
	}

	/**
	 * UDP is not a secure protocol.
	 */
	public boolean isSecure() {
		return false;
	}

	public int getPeerPort() {
		return peerPort;
	}

	/**
	 * Close the message channel.
	 */
	public void close() {
	}

	/**
	 * Creates a response to a bad request (ie one that causes a ParseException)
	 *
	 * @param badReq
	 * @return message bytes, null if unable to formulate response
	 */
	private String createBadReqRes( String badReq, ParseException pe ) {

		StringBuffer buf = new StringBuffer( 512 );
        buf.append("SIP/2.0 400 Bad Request (").
                append(pe.getLocalizedMessage()).append(')');

		// We need the following headers: all Vias, CSeq, Call-ID, From, To
		if (!copyViaHeaders(badReq, buf)) return null;
		if (!copyHeader( CSeqHeader.NAME, badReq, buf)) return null;
		if (!copyHeader( CallIdHeader.NAME, badReq, buf)) return null;
		if (!copyHeader( FromHeader.NAME, badReq, buf)) return null;
		if (!copyHeader( ToHeader.NAME, badReq, buf)) return null;

		// Should add a to-tag if not already present...
		int toStart = buf.indexOf( ToHeader.NAME );
		if (toStart!=-1 && buf.indexOf( "tag", toStart) == -1 ) {
			buf.append( ";tag=badreq" );
		}

		// Let's add a Server header too..
		Server s = getMessageProcessor().getListeningPoint().getSipStack().createServerHeaderForStack();
        buf.append("\r\n").append(s.toString());
		return buf.toString();
	}

	/**
	 * Copies a header from a request
	 *
	 * @param name
	 * @param fromReq
	 * @param buf
	 * @return
	 *
	 * Note: some limitations here: does not work for short forms of headers, or continuations;
	 * 	 problems when header names appear in other parts of the request
	 */
	private static boolean copyHeader( String name, String fromReq, StringBuffer buf ) {
		int start = fromReq.indexOf( name );
		if (start!=-1) {
			int end = fromReq.indexOf( "\r\n", start );
			if (end!=-1) {
				// XX Assumes no continuation here...
				buf.append( fromReq.subSequence(start-2, end) );	// incl CRLF in front
				return true;
			}
		}
		return false;
	}

	/**
	 * Copies all via headers from a request
	 *
	 * @param fromReq
	 * @param buf
	 * @return
	 *
	 * Note: some limitations here: does not work for short forms of headers, or continuations
	 */
	private static boolean copyViaHeaders( String fromReq, StringBuffer buf ) {
		int start = fromReq.indexOf( ViaHeader.NAME );
		boolean found = false;
		while (start!=-1) {
			int end = fromReq.indexOf( "\r\n", start );
			if (end!=-1) {
				// XX Assumes no continuation here...
				buf.append( fromReq.subSequence(start-2, end) );	// incl CRLF in front
				found = true;
				start = fromReq.indexOf( ViaHeader.NAME, end );
			} else {
				return false;
			}
		}
		return found;
	}

}
