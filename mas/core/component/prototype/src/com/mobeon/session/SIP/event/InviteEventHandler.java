/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.session.SIP.event;

import com.mobeon.session.SIP.SIPConnection;
import com.mobeon.session.SIP.SIPServer;
import com.mobeon.backend.LookupClient;
import com.mobeon.backend.LookupClientFactory;
import com.mobeon.frontend.rtp.PortFactory;

import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.address.Address;
import javax.sip.header.ToHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-dec-10
 * Time: 11:13:43
 * To change this template use File | Settings | File Templates.
 */
public class InviteEventHandler {
    private SIPConnection connection;
    private SIPServer server;
    static Logger logger = Logger.getLogger(InviteEventHandler.class);


    public InviteEventHandler(SIPServer server,SIPConnection connection) {
        this.connection = connection;
        this.server = server;
    }

    public void handle(RequestEvent requestEvent, ServerTransaction serverTransaction) {
    	SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
        Response response = null;
        ToHeader toHeader = null;
        ContactHeader contactHeader = null;
        Address address = null;
        ServerTransaction st = null;
		logger.debug("Got an INVITE  " + request);
        if (connection.getDialog() != null) {
            // This is a re-invite, discard for now
            logger.debug("Dialog has already been established! Th re-invite is discarded!");
            return;
        }
        byte[] content = request.getRawContent();
        parseContent(content);
		try {
			logger.debug(" got an Invite sending OK");
            address =
				server.addressFactory.createAddress("SIPServer <sip:" + server.myAddress+ ":" + server.myPort + ">");

		    contactHeader =
				server.headerFactory.createContactHeader(address);

            response = server.messageFactory.createResponse(180, request);
			toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag(generateTag()); // Application is supposed to set.

			response.addHeader(contactHeader);
            st = serverTransaction;

            if (st == null) {
                try {
                    st = sipProvider.getNewServerTransaction(request);
                }
                catch (TransactionAlreadyExistsException e) {
                    st = requestEvent.getServerTransaction();
                    if (st == null) {
                        logger.error("Failed to get server transaction ", e);
                        System.exit(1);
                    }
                }
            } else {
                logger.debug("This is a RE INVITE ");
                if (st.getDialog() != connection.getDialog()) {
                    logger.debug("Whoopsa Daisy Dialog Mismatch");
                }
            }

			logger.debug("got a server tranasaction " + st);
            // TODO: Should there be any contents in this event???
			connection.setDialog(st.getDialog());
			if (connection.getDialog() == null) {
				logger.debug("Dialog is null");
			}
			st.sendResponse(response);
            logger.debug("Response sent");

            LookupClient lc = LookupClientFactory.create();
            Hashtable ht = lc.getSubInfo(connection.getLocalURI());

            if (ht != null) {
		        response = server.messageFactory.createResponse(200, request);
                // TODO: A bunch of other cases where the calling user is admin apply as well
               if (connection.getPeerURI().indexOf("111")!= -1)  {
                    ht.put("ISMAILBOXADMIN", "1");
                }
                ht.put("MAILBOXID", connection.getLocalURI());                
                connection.getSessionData().putAll(ht);
            }
            else {
                // Respond with Not found
                response = server.messageFactory.createResponse(404, request);
            }
			toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag(generateTag()); // Application is supposed to set.
			response.addHeader(contactHeader);

            content = generateSDPContent();
            ContentTypeHeader contentTypeHeader =
            server.headerFactory.createContentTypeHeader("application", "sdp");
            response.setContent(content, contentTypeHeader);
            logger.debug("response = " + response);

			st.sendResponse(response);  // Send the 200/OK response

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
    }
    private String getUser() {
        return "QDALO";
    }

    private String getNetworkTimestamp() {
        return "0";
    }

    private String getIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("Failed to retrieve local address", e);
            return "";
        }
    }

    private int allocatePort() {
        return PortFactory.createInstance().allocatePortNumber();
    }

    private byte[] generateSDPContent() {
        String timestamp = getNetworkTimestamp() ;
        int port = connection.getLocalPort();
        String sdpData =
				    "v=0\r\n"
					+ "o=" + getUser() + " " + timestamp + " " + timestamp
					+ " IN IP4 " + getIP() + "\r\n"
					+ "s=MAS prompt session\r\n"
					+ "c=IN IP4 " + getIP() + "\r\n"
					+ "t=0 0\r\n"
					+ "m=audio " + port + " RTP/AVP 0\r\n"
					+ "a=rtpmap:0 PCMU/8000\r\n"
                    + "a=rtpmap:101 telephone-event/8000\r\n"
                    + "a=fmtp:101 0-15\r\n";
        String outURL = connection.getRemoteIP() + ":" + connection.getRemotePort() + "/audio";
        this.connection.addOutStreamURL(outURL);
		return sdpData.getBytes();
    }

    private void parseContent(byte[] content) {
        String strContent = new String(content);

        String address = null;
        int port = -1;
        String contentType = null;
        String protocol = null;
        String codec = null;
        String inURL = null;
        // Retrieve address/port info
        Pattern p = Pattern.compile("c=IN\\s+IP4\\s+(\\S+)");
        Matcher m = p.matcher(strContent);
        if (m.find()) {
            address = m.group(1);
        }

        // Find media descriptor(s)
        // TODO: Add support for multiple streams
        p = Pattern.compile("m=(\\w+)\\s+(\\d+)\\s+(\\w+)");
        m = p.matcher(strContent);
        if (m.find()) {
            contentType = m.group(1);
            port = Integer.parseInt(m.group(2));
            protocol = m.group(3);
        }
        // Find codec info
        p = Pattern.compile("a=rtpmap:(\\d+)\\s+G711/(\\d+)");
        m = p.matcher(strContent);
        if (m.find()) {
            codec = "G711/" + m.group(2);
            this.connection.setCodec(codec);
        }

        // Save the port as the remotePort in the session
        connection.setRemoteIP(address);
        connection.setRemotePort(port);
        connection.setLocalPort(allocatePort());
        inURL = address + ":" + connection.getRemotePort() + "/audio";
        this.connection.addInStreamURL(inURL);
    }

    private String generateTag() {
        return "4321";
    }
}
