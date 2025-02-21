/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.session.SIP.event;

import com.mobeon.session.SIP.SIPConnection;
import com.mobeon.session.SIP.SIPServer;
import com.mobeon.event.types.MASHangup;

import javax.sip.*;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-dec-10
 * Time: 11:13:43
 * To change this template use File | Settings | File Templates.
 */
public class ByeEventHandler {
    private SIPConnection connection;
    private SIPServer server;
    static Logger logger = Logger.getLogger(ByeEventHandler.class);


    public ByeEventHandler(SIPServer server,SIPConnection connection) {
        this.connection = connection;
        this.server = server;
    }

    public void handle(RequestEvent requestEvent, ServerTransaction serverTransaction) {
    	//	SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			logger.debug("Got a BYE");
            if (request != null && server != null)  {
                Response response = server.messageFactory.createResponse(200, request);
                if (serverTransaction != null)  {
                    serverTransaction.sendResponse(response);
                    logger.debug("Dialog State is " + serverTransaction.getDialog().getState());
                    logger.debug("200 sent as response");
                }
            }

		} catch (Exception ex) {
			ex.printStackTrace();
		}
        if (connection.getDispatcher() != null)
            connection.getDispatcher().fire(new MASHangup(this));
        logger.error("Removing URI from connections: " + connection.getURI());
        server.removeConnection(connection.getURI());
        connection.setHasEnded(true);
        connection.setRunning(false);
    }

    public void sendBye() {
       if (connection.getDialog() != null)  {
            Request byeRequest = null;
            try {
                byeRequest = connection.getDialog().createRequest(Request.BYE);
            } catch (SipException e) {
                logger.error("Failed to create BYE message", e);
                return;
            }
            ClientTransaction tr = null;
            try {
                tr = server.udpProvider.getNewClientTransaction(byeRequest);
            } catch (TransactionUnavailableException e) {
                logger.error("Failed to create client transaction");
                return;
            }
            try {
                logger.debug("Sending BYE");
                connection.getDialog().sendRequest(tr);
            } catch (SipException e) {
                logger.error("Failed to send BYE message");
                return;
            }
        }
        else {
            logger.error("NO DIALOG, CAN NOT SEND BYE REQUEST");
        }
    }
}
