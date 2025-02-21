/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp.server;


import com.mobeon.common.util.M3Utils;
import com.mobeon.common.xmp.XmpConstants;
import com.mobeon.common.xmp.XmpErrorHandler;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * XmpHandler parses the XMP request and routes it to the appropriate service
 * handler. It also keeps track of all XMP transactions, and handles the response
 * queue for the current session.
 *
 * If a transaction expires, it
 * is cancelled towards the service and an error response is returned to the
 * client. Since the XMP response does not contain a client id, XMP can not
 * handle two clients on the same connection and the client id is useless. Thus
 * a transaction is uniquely determined by its transactionId within this
 * XmpHandler.
 *
 * For each request sent to a service, we must remember the expiration time, the
 * transaction id (to be able to return the error response) and the service
 * handler (to be able to cancel the request in the correct service handler).
 *
 * When a job (for a request) is done the response will put in the response queue.
 * The run method in this class continually retrieves jobs from the response
 * queue and sends the response back to the XMP client.
 */

public class XmpHandler extends Thread {

    private static HashMap<String, XmpResponseQueue> responseQueues;
    static{
        responseQueues = new HashMap<String, XmpResponseQueue>();
    }

    /** The handler for HTTP communication for this XMP transaction */
    private HttpHandler httpHandler = null;
    /** The service of this request */
    private String serviceId = null;

    /** How long this request is valid */
    private int validity = 0;
    /** Unique id of this client */
    private String clientId = null;
    /** Unique id (for this clientId) of this transaction */
    private Integer transactionId = null;
    /** The text of the request */
    private char[] xmpReq = null;
    /** Directory of registered service handlers, keyed by service id (String)  */
    private static Hashtable<String, ServiceHandler> handlers = new Hashtable<String, ServiceHandler>();

    private XmpResponseQueue responseQueue = null;

    private synchronized static XmpResponseQueue getResponseQueue( String clientId, HttpServer server ) {

        XmpResponseQueue queue = (XmpResponseQueue) responseQueues.get(clientId);
        if( queue == null )  {
            server.debug("Making new responseQueue for client " + clientId);
            queue = new XmpResponseQueue(clientId, server);
            queue.setLogger(server);
            responseQueues.put(clientId, queue);
        } else {
            server.debug("Using cached responseQueue for client " + clientId );
        }

        return queue;
    }

    /**
     * Constructor.
     *@param hh Http handler the requets comes from.
     *@param i Que?
     */
    protected XmpHandler(HttpHandler hh, int i) {
        super("XmpHandler-" + i);
        httpHandler = hh;

    }


    /**
     * Starts processing of a request.
     *@param b text of the request message.
     */
    public synchronized void xmpRequestHandler(char[] b, ArrayList attachments ) {
        httpHandler.getServer().debug("Got request " + new String(b));
        processGeneralXmpTags(b, attachments);
    }

    /**
     * Returns response to the XMP client via the HTTP server.
     * Use this when an error occurs before the request is sent to the
     * ServiceHandler.
     *@param transId identity of the transaction.
     *@param statusCode return code of the response.
     *@param statusText readable text describing the outcome of the request.
     */
    public void xmpPreResponseHandler(Integer transId, int statusCode, String statusText) {
        String responseText = "<status-code>" + statusCode + "</status-code>" +
            "<status-text>" + statusText + "</status-text>";

        try {
            sendXmpResponse(transId, responseText);
        } catch (IOException e) {
            //ignore
        }
    }



    /**
     * Returns response to the XMP client via the HTTP server.
     *@param transId identity of the transaction.
     *@param responseText readable text describing the outcome of the request.
     */
    public void xmpResponseHandler(Integer transId, String responseText, ArrayList attachments)
            throws IOException {
        if( attachments == null || attachments.size() == 0 ) {
            sendXmpResponse(transId, responseText);
        } else {
            sendXmpResponse(transId, responseText, attachments);
        }

    }

    private void sendXmpResponse(Integer transId, String responseText, ArrayList attachments)
            throws IOException {

        String xmpResponse =
            "<?xml version=\"1.0\"?>\n<xmp-message xmlns=\"http://www.mobeon.com/xmp-1.0\">\n";
        xmpResponse += "<xmp-service-response transaction-id=\"" + transId + "\">\n";
        xmpResponse += responseText
            + "</xmp-service-response>\n</xmp-message>\n";


        httpHandler.httpResponseHandler(xmpResponse, attachments);

    }

    /**
     * Send response to the XMP client via the HTTP server.
     *@param transId identity of the transaction.
     *@param responseText readable text describing the outcome of the request.
     */
    private void sendXmpResponse(Integer transId, String responseText) throws IOException {
        String httpPost = "HTTP/1.1 200 OK\r\n";
        httpPost += "Content-Type: text/xml; charset=utf-8\r\n";
        String xmpResponse =
            "<?xml version=\"1.0\"?>\n<xmp-message xmlns=\"http://www.mobeon.com/xmp-1.0\">\n";
        xmpResponse += "<xmp-service-response transaction-id=\"" + transId + "\">\n";
        xmpResponse += responseText
            + "</xmp-service-response>\n</xmp-message>\n";
        httpPost += "Content-Length: " + xmpResponse.length() + "\r\n\r\n";
        httpHandler.httpResponseHandler(httpPost + xmpResponse);
    }

    /**
     * Send response to an empty request when there are no other responses to send.
     */
    private void sendEmptyXmpResponse() {
        String httpPost = "HTTP/1.1 200 OK\r\n";
        httpPost += "Content-Type: text/xml; charset=utf-8\r\n";
        String xmpResponse =
            "<?xml version=\"1.0\"?>\n<xmp-message xmlns=\"http://www.mobeon.com/xmp-1.0\">\n"
            + "<xmp-service-response>\n"
            + "</xmp-service-response>\n"
            + "</xmp-message>\n";
        httpPost += "Content-Length: " + xmpResponse.length() + "\r\n\r\n";
        try {
            httpHandler.httpResponseHandler(httpPost + xmpResponse);
        } catch (IOException e) {
            //ignore
        }
    }

    /**
     * Parses the XMP document for general tags.
     *@param b the XMP document.
     */
    private void processGeneralXmpTags(char[] b, ArrayList attachments) {
      transactionId = new Integer(0); // Otherwise it is the last tid.
      httpHandler.getServer().debug("Parses the XMP document for general tags");

      try {
        int startHere = new String(b).indexOf("<");
        if (startHere < 0) {
          httpHandler.getServer().error("Request body is not a valid XML document");
          xmpPreResponseHandler(transactionId, XmpConstants.CLIENT_ERROR,
                                "Request error, body is not a valid XML document.");
          return;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new XmpErrorHandler(httpHandler.getServer().getLog()));
        String req = new String(b, startHere, (b.length - startHere));
        StringReader sr = new StringReader(req);
        InputSource insrc = new InputSource(sr);
        Document xmpDoc = builder.parse(insrc);
        boolean success = true;

        success = processServiceRequestTag(xmpDoc);


        if (success) { success = processValidityTag(xmpDoc); }
        if (success) { sendXmpRequest(xmpDoc, attachments); }
      } catch (FactoryConfigurationError e) {
        httpHandler.getServer().error("DocumentBuilderFactory is not available or cannot be instantiated. " + e);
        xmpPreResponseHandler(transactionId, XmpConstants.NEVER,
                              "Internal error while parsing XMP document.");
      } catch (ParserConfigurationException e) {
        httpHandler.getServer().error("DocumentBuilder cannot be created. " + e);
        xmpPreResponseHandler(transactionId, XmpConstants.NEVER,
                              "Internal error while parsing XMP document.");
      } catch (IllegalArgumentException e) {
        httpHandler.getServer().error("Inputstream is null. " + e);
        xmpPreResponseHandler(transactionId, XmpConstants.CLIENT_ERROR,
                              "Inputstream is null, could not parse XMP content.");
      } catch (IOException e) {
        httpHandler.getServer().error("IOError: " + e);
        xmpPreResponseHandler(transactionId, XmpConstants.CLIENT_ERROR,
                              "IOError while parsing XMP document.");
      } catch (SAXException e) {
        httpHandler.getServer().error("Could not parse xmp data: " + e);
        xmpPreResponseHandler(transactionId, XmpConstants.CLIENT_ERROR,
                              "Parse error, could not parse XMP content.");
      } catch (NullPointerException e) {
        httpHandler.getServer().error("" + e + M3Utils.stackTrace(e).toString());
        xmpPreResponseHandler(transactionId, XmpConstants.NEVER,
                              "Internal error while parsing XMP document.");
      } catch (Exception e) {
        httpHandler.getServer().error("Unknown exception: " + e + M3Utils.stackTrace(e).toString());
        xmpPreResponseHandler(transactionId, XmpConstants.NEVER,
                              "Internal error while parsing XMP document.");
      }
    }

    /**
     * Processes a service request
     *@return true if the document is a valid request for a service, false if
     *the request is invalid or if it is an empty request.
     *@param xmpDoc parsed XMP text.
     */
    private boolean processServiceRequestTag(Document xmpDoc) {
        NodeList nlServiceRequest =
                xmpDoc.getElementsByTagName("xmp-service-request");

        httpHandler.getServer().debug("Processes service request");

        if ((nlServiceRequest.getLength() != 0) && (nlServiceRequest != null)) {
            for (int i = 0; i < nlServiceRequest.getLength(); i++) {
                try {
                    Node n = nlServiceRequest.item(i);
                    if (n != null && n.getAttributes() != null) {
                        for (int x = 0; x < n.getAttributes().getLength(); x++) {
                            String name =
                                    n.getAttributes().item(x).getNodeName().trim();
                            if (name.equalsIgnoreCase("service-id")) {
                                serviceId = n.getAttributes()
                                        .getNamedItem("service-id")
                                        .getNodeValue().trim();
                            } else if (name.equalsIgnoreCase("transaction-id")) {
                                transactionId = new Integer(n.getAttributes()
                                        .getNamedItem("transaction-id")
                                        .getNodeValue().trim());
                            } else if (name.equalsIgnoreCase("client-id")) {
                                clientId = n.getAttributes()
                                        .getNamedItem("client-id")
                                        .getNodeValue().trim();
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    httpHandler.getServer().error("Could not parse string to int. " + e);
                    xmpPreResponseHandler(transactionId, XmpConstants.CLIENT_ERROR,
                            "Invalid transaction id parameter.");
                    return false;
                } catch (DOMException e) {
                    httpHandler.getServer().error("Could not get the nodevalue from the TEXT_NODE. " + e);
                    xmpPreResponseHandler(transactionId, XmpConstants.CLIENT_ERROR,
                            "Could not get the nodevalue.");
                    return false;
                } catch (Exception e) {
                    httpHandler.getServer().error("Unknown error. " + e + M3Utils.stackTrace(e).toString());
                    xmpPreResponseHandler(transactionId, XmpConstants.NEVER,
                            "Internal error while parsing XMP document.");
                    return false;
                }
            }
        }

        if( responseQueue == null ) {
            responseQueue = getResponseQueue(clientId, httpHandler.getServer());
        }

        if (serviceId != null && transactionId != null && clientId != null) {
            if( transactionId.intValue() == 0 && serviceId.equals("Empty")) {
                    sendEmptyXmpResponse();
                return false;
            } else {
                return true;
            }
        } else {
            xmpPreResponseHandler(transactionId, XmpConstants.CLIENT_ERROR,
                    "Could not find service-id, transaction-id or client-id.");
            return false;
        }
    }

    /**
     * Processes the validity tag.
     *@param xmpDoc the parsed XMP request.
     */
    private boolean processValidityTag(Document xmpDoc) {
        NodeList nlValidity = xmpDoc.getElementsByTagName("validity");
        String v = null;
        boolean result = false;
        String errorMessage = "Could not find validity.";

        httpHandler.getServer().debug("Processes the validity tag...");

        if ((nlValidity.getLength() != 0) && (nlValidity != null)) {
            for (int i = 0; i < nlValidity.getLength(); i++) {
                try {
                    Node n = nlValidity.item(i);
                    if (n != null) {
                        v = n.getFirstChild().getNodeValue().trim();
                        validity = Integer.parseInt(v);
                        result = true;
                    }
                } catch (NumberFormatException e) {
                    httpHandler.getServer().error("Validity " + v + " is non-numeric");
                    errorMessage = "Validity " + v + " is non-numeric";
                } catch (DOMException e) {
                    httpHandler.getServer().error("Could not get the validity value from the TEXT_NODE. " + e);
                    errorMessage = "Could not get the validity value";
                } catch (Exception e) {
                    httpHandler.getServer().error("Unknown error. " + e + M3Utils.stackTrace(e).toString());
                    errorMessage = "Internal error while parsing XMP document.";
                }
            }
        }
        if (validity > 100000) { //Max allowed validity is 100 000 seconds
            validity = 100000;
        }
        if (validity < 0) { //Min allowed validity is 0 seconds
            validity = 0;
        }

        httpHandler.getServer().debug("validity tag: " + validity);

        if (!result) {
            xmpPreResponseHandler(transactionId, XmpConstants.CLIENT_ERROR,
                                  errorMessage);
        }
        return result;
    }

    /**
     * Sets the handler for an XMP service.
     *@param serviceId is the name of the service.
     *@param handler is the new handler for the service.
     *@return the previous handler for the service.
     */
    public static ServiceHandler setServiceHandler(String serviceId, ServiceHandler handler) {
        ServiceHandler old = (ServiceHandler) handlers.get(serviceId);
        handlers.put(serviceId, handler);
        return old;
    }

    /**
     * Gets a service handler class from the service id using java reflection.
     *@param serviceId the name of the service.
     *@return a service handler for the specified service.
     */
    private ServiceHandler getServiceHandler(String serviceId) {
        return (ServiceHandler) handlers.get(serviceId);
    }

    /**
     * Sends an XMP request to the service handler.
     *@param xmpDoc the parsed XMP document
     */
    private void sendXmpRequest(Document xmpDoc, ArrayList attachments) {
        ServiceHandler sh = getServiceHandler(serviceId);
        if (sh == null) {
            httpHandler.getServer().debug("No service handler for serviceId: " + serviceId);
            xmpPreResponseHandler(transactionId, XmpConstants.NO_SERVICE,
                               "Service not available");
            return;
        }

        try {
            httpHandler.getServer().debug("Sending request " + transactionId +
                               " to service handler " + serviceId);

            if( responseQueue == null ) {
                responseQueue = getResponseQueue(clientId, httpHandler.getServer());
            }

            XmpTransaction t = new XmpTransaction(validity, transactionId, sh);
            responseQueue.addTransaction(t);

            sh.handleRequest(responseQueue,
                             serviceId,
                             clientId,
                             transactionId,
                             validity,
                             xmpDoc,
                             attachments);
        } catch (NullPointerException e) {
            httpHandler.getServer().error("" + e + M3Utils.stackTrace(e).toString());
            xmpPreResponseHandler(transactionId, XmpConstants.NEVER,
                               "Internal error");
            return;
        } catch (Exception e) {
            httpHandler.getServer().error("Unknown error: " + e + M3Utils.stackTrace(e).toString());
            xmpPreResponseHandler(transactionId, XmpConstants.NEVER,
                               "Internal error");
            return;
        }
    }





    /**
     * The run methods peridocally scans pending transactions for those that
     * have expired and returns an error response for each expired transaction. It
     * also continually retrieves jobs from the response queue and sends the
     * response back to the XMP client.
     */
    public void run() {
        IXmpAnswer answer = null;
        while (httpHandler.isConnected()) {
            try {
                if( responseQueue == null ) {
                    try {
                        sleep(200);
                    } catch(Exception e) {}
                } else if (responseQueue.isAResponseReady()) {
                    Integer[] transIdOfResponses = responseQueue.getAnswerIds();
                    for (Integer trId : transIdOfResponses) {
                        answer = responseQueue.getAnswer(trId);
                        if (answer != null) {
                            httpHandler.getServer().debug("Sending response with transactionID: " +
                                    answer.getTransactionId());

                            try {
                                xmpResponseHandler(answer.getTransactionId(),
                                        answer.getRspAsXml(),
                                        answer.getAttachments());
                            } catch (IOException e) {
                                httpHandler.getServer().debug("Put answer back in queue.");
                                responseQueue.reAddResponse(answer);
                            }
                        }
                    }
                    // Wait for another response to be added to the queue before checking again.
                    synchronized (responseQueue) {
                        responseQueue.wait(1000);
                	}
                } else {
                	// wait for an element to appear in the responseQueue
                	// Prefer timed wait to simply wait in case something goes wrong.
                	// Not sure about how long should the wait be. 
                	// For the moment, set to 1000ms.
                	synchronized (responseQueue) {
                		responseQueue.wait(1000);
                	}
                }
            } catch (Exception e) {
                httpHandler.getServer().error("Unexpected exception " + ": " +
                        M3Utils.stackTrace(e));

            }
        } // while
    }

}

