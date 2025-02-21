/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.session.SIP;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.mobeon.session.SessionServer;



public class SIPServer implements SipListener, SessionServer, Runnable {

	public  AddressFactory addressFactory;
	public  MessageFactory messageFactory;
	public  HeaderFactory headerFactory;
	public  SipStack sipStack;
	public  static String myAddress = null;
	public  final int myPort    = 5060;
    public  SipProvider udpProvider;
    public SipProvider tcpProvider;

    // static Logger logger = Logger.getLogger("com.mobeon");
    static Logger logger = Logger.getLogger("session");

  //  private SIPConnection conn;
    private Hashtable connections;
    private Object mutex;


    public SIPServer() {
        if (myAddress == null)
            try {
                myAddress = InetAddress.getLocalHost().getHostAddress();
                logger.debug("Binding to local address " + myAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        connections = new Hashtable();
        mutex = new Object();
    }

    public void removeConnection(String URI) {
        logger.error("Removing connection " + URI);
        synchronized(mutex) {
            SIPConnection conn = (SIPConnection) connections.get(URI);
            if (conn != null)
                conn.releaseObjects();
            if (connections.remove(URI) != null)
                logger.debug("Removed URIValidator " + URI + " from connections");
            logger.error("Number of connections :" + connections.size());
        }
    }

    public SIPConnection addConnection(String fromURI, String toURI) {
        SIPConnection conn = new SIPConnection(fromURI,toURI, this);
        conn.start();
        String URI = fromURI + ":" + toURI;
        logger.debug(" storing connection " + URI);
        synchronized(mutex) {
            connections.put(URI, conn);
            logger.error("Added URI " + URI + " to connections");
            logger.error("Number of connections :" + connections.size());            
        }
        return conn;
    }

    public void processRequest(RequestEvent requestEvent) {
        logger.debug(" Got request");
        SIPConnection conn = null;

        Request req = (Request) requestEvent.getRequest().clone();
        FromHeader fh = (FromHeader) req.getHeader("From");
        ToHeader th = (ToHeader) req.getHeader("To");
        String URI =  fh.getAddress().getURI().toString() + ":" + th.getAddress().getURI().toString();

        if (connections.containsKey(URI)) {
            logger.debug(" retrieving connection " + URI);
           conn = (SIPConnection) connections.get(URI);
            logger.debug("connection retrieved "+ URI);
        }
        else {
            conn =  addConnection(fh.getAddress().getURI().toString(), th.getAddress().getURI().toString());
        }
        conn.addRequest(requestEvent);
	}

	public void processResponse(ResponseEvent responseEvent) {
		logger.debug(" Got a response");
        SIPConnection conn = null;

        Response resp = (Response) responseEvent.getResponse().clone();
        FromHeader fh = (FromHeader) resp.getHeader("From");
        ToHeader th = (ToHeader) resp.getHeader("To");
        String URI = th.getAddress().getURI().toString()  + ":" +  fh.getAddress().getURI().toString();

        if (connections.containsKey(URI)) {
           logger.debug(" retrieving connection " + URI);
           conn = (SIPConnection) connections.get(URI);
           logger.debug("connection retrieved "+ URI);
        }
        else {
            // How to handle responses for no longer active connections?
            return ;
            /*
            conn =  addConnection(fh.getAddress().getURI().toString(), th.getAddress().getURI().toString());
           */
        }
        conn.addResponse(responseEvent);
	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        logger.debug(" Got a timeout");
        SIPConnection conn = null;
        Transaction transaction = null;
		if (timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
		} else {
			transaction = timeoutEvent.getClientTransaction();
		}
        Request req = (Request) transaction.getRequest().clone();
        FromHeader fh = (FromHeader) req.getHeader("From");
        ToHeader th = (ToHeader) req.getHeader("To");
        String URI =  fh.getAddress().getURI().toString() + ":" + th.getAddress().getURI().toString();

        if (connections.containsKey(URI)) {
            logger.debug(" retrieving connection " + URI);
            conn = (SIPConnection) connections.get(URI);
            logger.debug("connection retrieved "+ URI);
        }
        else {
           // How to handle timeouts for no longer existing connections?
            return;
            /*
            conn =  addConnection(fh.getAddress().getURI().toString(), th.getAddress().getURI().toString());
           */
        }
        conn.addTimeout(timeoutEvent);
	}

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.IP_ADDRESS", myAddress );
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
		properties.setProperty("javax.sip.STACK_NAME", "SIPServer");
		// You need  16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty(
			"gov.nist.javax.sip.DEBUG_LOG",
			"sipserverdebug.txt");
		properties.setProperty(
			"gov.nist.javax.sip.SERVER_LOG",
			"sipserverlog.txt");
		// Guard against starvation.
		properties.setProperty(
			"gov.nist.javax.sip.READ_TIMEOUT", "1000");
		// properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "4096");
		properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			logger.debug("sipStack = " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			if (e.getCause() != null)
				e.getCause().printStackTrace();
			System.exit(0);
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			ListeningPoint lp = sipStack.createListeningPoint(myPort, "udp");
			ListeningPoint lp1 = sipStack.createListeningPoint(myPort, "tcp");

			SIPServer listener = this;

			udpProvider = sipStack.createSipProvider(lp);
			logger.debug("udp provider " + udpProvider);
			udpProvider.addSipListener(listener);
			tcpProvider = sipStack.createSipProvider(lp1);
			logger.debug("tcp provider " + tcpProvider);
			tcpProvider.addSipListener(listener);

		} catch (Exception ex) {
			logger.debug(ex.getMessage());
		}

	}

	public static void main(String args[]) {
        PropertyConfigurator.configure("lib/mobeon.properties");
		new SIPServer().init();
	}

    public void run() {
        PropertyConfigurator.configure("lib/mobeon.properties");
		init();
    }

    public void fail(Exception e, String msg) {
        // TODO: implement and call a shutdown method, that closes the server.
    }
}
