/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.session.SIP.event;

import com.mobeon.session.SIP.SIPConnection;
import com.mobeon.session.SIP.SIPServer;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.header.ToHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.ParseException;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-dec-10
 * Time: 11:13:43
 * To change this template use File | Settings | File Templates.
 */
public class InfoEventHandler {
    private SIPConnection connection;
    private SIPServer server;
    static Logger logger = Logger.getLogger(InfoEventHandler.class);


    public InfoEventHandler(SIPServer server,SIPConnection connection) {
        this.connection = connection;
        this.server = server;
    }

    public void handle(RequestEvent requestEvent, ServerTransaction serverTransaction) {
          try {
              Request request = requestEvent.getRequest();
              logger.debug(" got an  INFO" + request);
              String infoData =  null;
              try {
                  Object content=request.getContent();
                  if (content instanceof String)
                      infoData=(String)content;
                  else if (content instanceof byte[] ) {
                          infoData=new String(  (byte[])content  );
                      }
                      else {
                      }
              } catch (Exception e) {
                  e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                  logger.debug("The content is of type " + requestEvent.getRequest().getContent().getClass());
                  System.exit(0);
              }
              String type = request.getHeader("Content-Type").toString();
              if (type.indexOf("application/dtmf-relay") > 0) {
                   String signal = "NO SIGNAL";
                   String duration = "DEFAULT";
                    Pattern p = Pattern.compile("Signal=(\\d+|\\*|\\#|[A-D])");
                    Matcher m = p.matcher(infoData);
                    if (m.find()){
                        signal = m.group(1);
                    }
                    p = Pattern.compile("Duration=(\\d+)");
                    m = p.matcher(infoData);
                    if (m.find()) {
                        duration = m.group(1);
                    }
                    logger.debug("DTMF signal received.\n Signal = " + signal + " Duration = " + duration);
                    connection.getControlSig().putToken(signal);
              }
              else {
                    logger.debug(" MSG DATA = " + infoData );
              }

              // Send OK back
              Response response = server.messageFactory.createResponse(200, request);
              ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
              toHeader.setTag("4321"); // Application is supposed to set.
              Address address = server.addressFactory.createAddress("SIPServer <sip:" + server.myAddress+ ":" + server.myPort + ">");
              ContactHeader contactHeader =
                  server.headerFactory.createContactHeader(address);
              response.addHeader(contactHeader);
              serverTransaction.sendResponse(response);


             } catch (Exception ex) {
              ex.printStackTrace();
              System.exit(0);
          }


    }

     public void sendInfo(String msg) {
        if (connection.getDialog() != null)  {
            Request infoRequest = null;
            msg = "Signal=" + msg;
            try {
                infoRequest = connection.getDialog() .createRequest(Request.INFO);
                ContentTypeHeader cth = server.headerFactory.createContentTypeHeader("application", "dtmf-relay");
                infoRequest.setContent(msg, cth);
            } catch (SipException e) {
                logger.error("FAILED TO CREATE infoRequest", e);
                return;
            } catch (ParseException e) {
                logger.error("FAILED TO PARSE EXPRESSION",e);
                return;

            }
            ClientTransaction ct = null;
            try {
                ct = server.udpProvider.getNewClientTransaction(infoRequest);
            } catch (TransactionUnavailableException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                try {
                    ct = server.tcpProvider.getNewClientTransaction(infoRequest);
                } catch (TransactionUnavailableException e1) {
                    logger.error("NO TCP PROVIDER AVAILABLE, BAILING OUT",e);
                }
            }
            try {
                connection.getDialog().sendRequest(ct);
                logger.debug("SipClientUA: INFO request sent");
            } catch (SipException e) {
                logger.error("FAILED TO SEND REQUEST",e);
            }

        }
        else {
            logger.error("NO DIALOG, CAN NOT SEND INFO REQUEST");
        }
    }
}
