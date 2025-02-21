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
package gov.nist.javax.sip;

import java.text.ParseException;
import java.net.InetAddress;

import javax.sip.*;

import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.stack.*;
import gov.nist.core.HostPort;
import gov.nist.core.Host;
import gov.nist.core.InternalErrorHandler;

/**
 * Implementation of the ListeningPoint interface
 *
 * @version 1.2 $Revision: 1.8 $ $Date: 2006/07/13 09:02:53 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 *
 */
public class ListeningPointImpl implements javax.sip.ListeningPoint {


    protected final String transport;

    /**
     * My IP address
     */
    private final InetAddress ipAddress;

    /** My port. (same thing as in the message processor) */

    private final int port;

    /**
     * Pointer to the imbedded mesage processor.
     */
    private MessageProcessor messageProcessor;

    /**
     * Provider back pointer
     */
    private SipProviderImpl sipProvider;

    /**
     * Our stack
     */
    private final SIPTransactionStack sipStack;

    private final String key;

    // TODO mmany and mmath: Where should these be handled?
    /**
     * My Sent by string ( which I use to set the outgoing via header)
     */
    private String sentBy;
    private HostPort sentByHostPort;


    /**
     * Constructor
     * @param sipStack Our sip stack
     */
    protected ListeningPointImpl(
            SIPTransactionStack sipStack,
            InetAddress ipAddress,
            int port,
            String transport) {
        this.sipStack = sipStack;
        this.ipAddress = ipAddress;
        this.port = port;
        this.transport = transport;

        key = makeKey(getIPAddress(), port, transport);

        this.sentByHostPort = new HostPort();
        this.sentByHostPort.setHost(new Host(ipAddress.getHostAddress()));
        this.sentByHostPort.setPort(port);
    }


    /**
     * Construct a key to refer to this structure from the SIP stack
     * @param host host string
     * @param port port
     * @param transport transport
     * @return a string that is used as a key
     */
    public static String makeKey(String host, int port, String transport) {
        return new StringBuffer(host)
                .append(":")
                .append(port)
                .append("/")
                .append(transport)
                .toString()
                .toLowerCase();
    }

    /**
     * Get the key for this listening point
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Remove the sip provider from this listening point.
     */
    protected void removeSipProvider() {
        this.sipProvider = null;
    }

    /**
     * Clone this listening point. Note that a message Processor is not
     * started. The transport is set to null.
     * @return cloned listening point.
     */
    public Object clone() {
        return new ListeningPointImpl(
                this.sipStack, this.ipAddress, this.port, null);
    }



    /**
     * Gets the port of the ListeningPoint. The default port of a ListeningPoint
     * is dependent on the scheme and transport.  For example:
     * <ul>
     * <li>The default port is 5060 if the transport UDP the scheme is <i>sip:</i>.
     * <li>The default port is 5060 if the transport is TCP the scheme is <i>sip:</i>.
     * <li>The default port is 5060 if the transport is SCTP the scheme is <i>sip:</i>.
     * <li>The default port is 5061 if the transport is TLS over TCP the scheme is <i>sip:</i>.
     * <li>The default port is 5061 if the transport is TCP the scheme is <i>sips:</i>.
     * </ul>
     *
     * @return port of ListeningPoint
     */
    public int getPort() {
        return port;
    }

    public InetAddress getInetAddress() {
        return ipAddress;
    }

    /**
     * Gets transport of the ListeningPoint.
     *
     * @return transport of ListeningPoint
     */
    public String getTransport() {
        return transport;
    }

    /**
     * Get the provider.
     *
     * @return the provider.
     */
    public SipProviderImpl getSipProvider() {
        return this.sipProvider;
    }

    /**
     * Set the sip provider for this structure.
     * @param sipProviderImpl provider to set
     */
    protected void setSipProvider(SipProviderImpl sipProviderImpl) {
        this.sipProvider = sipProviderImpl;
    }

    public SIPTransactionStack getSipStack() {
        return sipStack;
    }

    /* (non-Javadoc)
    * @see javax.sip.ListeningPoint#getIPAddress()
    */
    public String getIPAddress() {
        return ipAddress.getHostAddress();
    }

    public MessageProcessor getMessageProcessor() {
        return this.messageProcessor;
    }

    public void setMessageProcessor(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    // TODO mmany and mmath: Can the 4 methods below be moved or removed?
    /* (non-Javadoc)
    * @see javax.sip.ListeningPoint#setSentBy(java.lang.String)
    */
    public void setSentBy(String sentBy) throws ParseException {

        int ind = sentBy.indexOf(":");
        if (ind == -1) {
            this.sentByHostPort = new HostPort();
            this.sentByHostPort.setHost(new Host(sentBy));
        } else {
            this.sentByHostPort = new HostPort();
            this.sentByHostPort.setHost(new Host(sentBy.substring(0, ind)));
            String portStr = sentBy.substring(ind + 1);
            try {
                int port = Integer.parseInt(portStr);
                this.sentByHostPort.setPort(port);
            } catch (NumberFormatException ex) {
                throw new ParseException("Bad format encountered at ", ind);
            }
        }
        this.sentBy = sentBy;
    }

    /* (non-Javadoc)
    * @see javax.sip.ListeningPoint#getSentBy()
    */
    public String getSentBy() {
        if ( this.sentBy == null && this.sentByHostPort != null) {
            this.sentBy = this.sentByHostPort.toString();
        }
        return this.sentBy;
    }

    /**
     * Get the Via header to assign for this message processor. The topmost via
     * header of the outoging messages use this.
     *
     * @return the ViaHeader to be used by the messages sent via this message processor.
     */
    public Via getViaHeader() {
        try {
            Via via = new Via();
            if (this.sentByHostPort != null) {
                via.setSentBy(sentByHostPort);
                via.setTransport(this.getTransport());
            } else {
                Host host = new Host();
                host.setHostname(ipAddress.getHostAddress());
                via.setHost(host);
                via.setPort(this.getPort());
                via.setTransport(this.getTransport());
            }
            return via;
        } catch (ParseException ex) {
            InternalErrorHandler.handleException(ex);
            return null;
        } catch (InvalidArgumentException ex) {
            InternalErrorHandler.handleException(ex);
            return null;
        }
    }

    /**
     * Get the defalt port for the message processor.
     *
     * @param transport
     * @return -- the default port for the message processor.
     */

    public static int getDefaultPort(String transport) {
        return transport.equalsIgnoreCase("TLS")?5061:5060;
    }

}
