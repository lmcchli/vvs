/*
* Copyright (c) 2004 Mobeon AB. All Rights Reserved.
*/
package com.mobeon.session.SIP.event;

import com.mobeon.session.SIP.SIPConnection;
import com.mobeon.session.SIP.SIPServer;
import com.mobeon.frontend.rtp.PortFactory;
import com.mobeon.frontend.rtp.RTPStream;
import com.mobeon.frontend.StreamFactory;
import com.mobeon.frontend.Stream;

import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;


import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-dec-10
 * Time: 11:13:43
 * To change this template use File | Settings | File Templates.
 */
public class AckEventHandler {
    private SIPConnection connection;
    private SIPServer server;
    static Logger logger = Logger.getLogger(AckEventHandler.class);

    

    public AckEventHandler(SIPServer server,SIPConnection connection) {
        this.connection = connection;
        this.server = server;
    }

    public void handle(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        // SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        try {
            logger.debug(" got an ACK "
                    + requestEvent.getRequest());
            // Allocate and set up streams
            allocateStreams();
            connection.startApplication();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void allocateStreams() {
        ArrayList inURLs = this.connection.getInStreamURLs();
        String url = null;
        ArrayList outURLs = this.connection.getOutStreamURLs();

        for (Iterator it = inURLs.iterator(); it.hasNext(); ) {
            url = (String) it.next();
            String ipaddress  = url.substring(0,url.indexOf(':'));
            Stream s = StreamFactory.create(ipaddress, connection.getControlSig(), connection.getLocalPort(), connection.getRemotePort(), 0);
            this.connection.addInStream(s);
        }

        for (Iterator it = outURLs.iterator(); it.hasNext();) {
            String uri = (String) it.next();
            String ipaddress  = url.substring(0,url.indexOf(':'));
            int remotePort = Integer.parseInt(url.substring(url.indexOf(':') + 1,url.lastIndexOf('/')));
            Stream s = StreamFactory.create(ipaddress,  connection.getControlSig(), PortFactory.createInstance().allocatePortNumber(), remotePort,0);            
            this.connection.addOutStream(s);
        }


    }
}
