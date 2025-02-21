/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package xmpserver;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.*;
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
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.OutputFormat;

import com.mobeon.common.externalcomponentregister.IServiceName;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import xmpserver.config.Config;
import xmpserver.responsecodes.XMPResponseCodes;


/**
 * XmpHandler parses the XMP request and routes it to the appropriate service
 * handler.
 *
 * It also keeps track of all XMP transactions and if a transaction expires, it
 * is cancelled towards the service and an error response is returned to the
 * client. Since the XMP response does not contain a client id, XMP can not
 * handle two clients on the same connection and the client id is useless. Thus
 * a transaction is uniquely determined by its transactionId within this
 * XmpHandler.
 *
 * For each request sent to a service, we must remember the expiration time, the
 * transaction id (to be able to return the error response) and the service
 * handler (to be able to cancel the request in the correct service handler).
 */
public class XmpHandler extends Thread {
    private static final Integer zero = new Integer(0);

    /** The handler for HTTP communication for this XMP transaction */
    private HttpHandler httphandler = null;
    /** The service of this request */
    private String serviceId = null;
    /** How long this request is valid */
    private int validity = -1;
    /** Unique id of this client */
    private String clientId = null;
    /** Unique id (for this clientId) of this transaction */
    private Integer transactionId = null;
    /** Log class */
    private Logger log = null;
    /** True when  done */
    private boolean quit = false;
    /** The text of the request */
    private char[] xmpReq = null;
    /** Pending requests */
    private Map /* of XmpTransaction keyed by Integer */ transactions = new HashMap();
    /** Cache of get methods for services */
    private static Hashtable /*of Method, keyed by service id (String)*/ getMethods = new Hashtable();

    private XMPResponseCodes xmpcodes = null;
    
    private static Random random;

    private static int wantedCode = 200;

    private ArrayList attachments = null;
    
    static int [] statusCodes = { 421, 521, 501, 502 };
    static int codeIndex = 0;
    
    static {
        random = new Random();
        new ResponseCodeSetter().start();
        
    }

    /**
     * Constructor.
     *@param hh Http handler the requets comes from.
     *@param l Logger to log to.
     *@param i Que?
     */
    protected XmpHandler(HttpHandler hh, Logger l, int i) {
        httphandler = hh;
        log = l;
        xmpcodes = new XMPResponseCodes(l);
    }

    /**
     * Starts processing of a request.
     *@param b text of the request message.
     */
    public synchronized void xmpRequestHandler(char[] b) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("    >> Processing general XMP tags <<");
        }
        attachments = null;
        processGeneralXmpTags(b);
    }
    
    public synchronized void xmpRequestHandler(char[] b, ArrayList attachments) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("    >> Processing general XMP tags, and storing attachments <<");
        }
        this.attachments = attachments;
        processGeneralXmpTags(b);
    }

    /**
     * Returns response to the XMP client via the HTTP server.
     *@param transId identity of the transaction.
     *@param statusCode return code of the response.
     *@param statusText readable text describing the outcome of the request.
     */
    public void xmpResponseHandler(Integer transId, int statusCode, String statusText) {
        sendXmpResponse(transId, statusCode, statusText);
    }

    /**
     * Send response to the XMP client via the HTTP server.
     *@param transId identity of the transaction.
     *@param statusCode return code of the response.
     *@param statusText readable text describing the outcome of the request.
     */
    private void sendXmpResponse(Integer transId, int statusCode, String statusText) {
        int code = statusCode;
        if( random.nextInt(100) < Config.getFailureCodesPercent() ) {
            code = statusCodes[codeIndex];
        }
        
        if( random.nextInt(100) < Config.getWrongIdPercent() ) {
            transId = new Integer(random.nextInt(255));
        }
        log.fine("response on transid=" + transId);
        if(attachments != null ) {
            sendXmpResponse(transId, code, statusText, attachments);
            return;
        }
        
        String httpPost = "HTTP/1.1 200 OK\r\n";
        httpPost += "Content-Type: text/xml; charset=utf-8\r\n";
        String xmpResponse =
            "<?xml version=\"1.0\"?><xmp-message xmlns=\"http://www.mobeon.com/xmp-1.0\">";
        if (zero.equals(transId)) {
            xmpResponse += "<xmp-service-response></xmp-service-response></xmp-message>\r\n";
        } else {
            xmpResponse += "<xmp-service-response transaction-id=\"" + transId + "\">";
            xmpResponse += "<status-code>" + code + "</status-code>";
            xmpResponse += "<status-text>" + statusText
                + "</status-text></xmp-service-response></xmp-message>\r\n";
        }
        
        
        
        httpPost += "Content-Length: " + xmpResponse.length() + "\r\n\r\n";
        if( random.nextInt(100) >= Config.getAnswerDropPercent() ) {
            httphandler.httpResponseHandler(httpPost + xmpResponse);
        } else {
            log.fine("missed sending response for " + transId);
        }
        
    }
    
    
    private void sendXmpResponse(Integer transId, int statusCode, String statusText, ArrayList attachments) {
        String xmpResponse =
            "<?xml version=\"1.0\"?><xmp-message xmlns=\"http://www.mobeon.com/xmp-1.0\">";
        if (zero.equals(transId)) {
            xmpResponse += "<xmp-service-response></xmp-service-response></xmp-message>\r\n";
        } else {
            xmpResponse += "<xmp-service-response transaction-id=\"" + transId + "\">";
            xmpResponse += "<status-code>" + statusCode + "</status-code>";
            xmpResponse += "<status-text>" + statusText
                + "</status-text></xmp-service-response></xmp-message>\r\n";
        }
        
        if( random.nextInt(100) >= Config.getAnswerDropPercent() ) {
            httphandler.httpResponseHandler(xmpResponse, attachments);
        } else {
            log.fine("missed sending response for " + transId);
        }
    }

    /**
     * Parses the XMP document for general tags.
     *@param b the XMP document.
     */
    private void processGeneralXmpTags(char[] b) {
        quit = false;
        try {
            int startHere = new String(b).indexOf("<");
            if (startHere < 0) {
                log.severe("Request body is not a valid XML document");
                xmpResponseHandler(transactionId,
                                   500, "Request error, body is not a valid XML document.");
                return;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String req = new String(b, startHere, (b.length - startHere));
            StringReader sr = new StringReader(req);
            InputSource insrc = new InputSource(sr);
            Document xmpDoc = builder.parse(insrc);

            if (!quit) { processServiceRequestTag(xmpDoc); }
            if (!quit && !zero.equals(transactionId)) { processValidityTag(xmpDoc); }
            logXmpRequest(xmpDoc);
            System.out.println(serviceId + "    " + transactionId);

            if (wantedCode == 9999) {
                return; //Simulate no response
            }

            if (wantedCode > 99) {
                log.fine("    >> Response code: " + wantedCode + " response text: Result-" + wantedCode +" <<");
                sendXmpResponse(transactionId, wantedCode, "Result-" + wantedCode);
            } else {                
                String[] s = null;
                if( serviceId.equalsIgnoreCase(IServiceName.PAGER_NOTIFICATION )){
                    s = xmpcodes.getPAGCodes();
                }
                else if( serviceId.equalsIgnoreCase(IServiceName.OUT_DIAL_NOTIFICATION )){
                    s = xmpcodes.getODLCodes();
                }
                else if(serviceId.equalsIgnoreCase(IServiceName.MWI_NOTIFICATION)){
                    s = xmpcodes.getMWICodes();
                }
                
                if (s == null) {
                    log.fine("    >> Response code: " + wantedCode + " response text: Result-" + wantedCode +" <<");
                    sendXmpResponse(transactionId, wantedCode, "Result-" + wantedCode);
                } else {
                    log.fine("    >> Response code: " + s[wantedCode] + " respose text: " + s[wantedCode + 1] +" <<");
                    sendXmpResponse(transactionId, Integer.parseInt(s[wantedCode]), s[wantedCode + 1]);
                }
            }

        } catch (FactoryConfigurationError e) {
            log.severe("DocumentBuilderFactory is not available or cannot be instantiated. " + e);
            xmpResponseHandler(transactionId,
                               500, "Internal error, could not parse XMP content.");
        } catch (ParserConfigurationException e) {
            log.severe("DocumentBuilder cannot be created. " + e);
            xmpResponseHandler(transactionId,
                               500, "Internal error, could not parse XMP content.");
        } catch (IllegalArgumentException e) {
            log.severe("Inputstream is null. " + e);
            xmpResponseHandler(transactionId,
                               500, "Internal error, could not parse XMP content.");
        } catch (IOException e) {
            log.severe("IOError: " + e);
            xmpResponseHandler(transactionId,
                               500, "Internal error, could not parse XMP content.");
        } catch (SAXException e) {
            log.severe("Could not parse xmp data: " + e);
            xmpResponseHandler(transactionId,
                               500, "Internal error, could not parse XMP content.");
        } catch (NullPointerException e) {
            log.severe("" + e + stackTrace(e));
            xmpResponseHandler(transactionId,
                               500, "Internal error, could not parse XMP content.");
        } catch (Exception e) {
            log.severe("Unknown exception: " + e + stackTrace(e));
            xmpResponseHandler(transactionId,
                               500, "Internal error, could not parse XMP content.");
        }
        quit = true;
    }

    /**
     * Processes a service request
     *@param xmpDoc parsed XMP text.
     */
    private void processServiceRequestTag(Document xmpDoc) {
        transactionId = zero;
        NodeList nlServiceRequest = xmpDoc.getElementsByTagName("xmp-service-request");
        if ((nlServiceRequest.getLength() != 0) && (nlServiceRequest != null)) {
            for (int i = 0; i < nlServiceRequest.getLength(); i++) {
                try {
                    Node n = nlServiceRequest.item(i);
                    if (n != null && /*n.hasChildNodes() &&*/ n.getAttributes() != null) {
                        for (int x = 0; x < n.getAttributes().getLength(); x++) {
                            String name = n.getAttributes().item(x).getNodeName().trim();
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
                    log.warning("Could not parse string to int. " + e);
                    xmpResponseHandler(transactionId,
                                       501, "Invalid transaction id parameter.");
                    quit = true;
                } catch (DOMException e) {
                    log.warning("Could not get the nodevalue from the TEXT_NODE. " + e);
                    xmpResponseHandler(transactionId,
                                       500, "Internal error, could not parse XMP content.");
                    quit = true;
                } catch (Exception e) {
                    log.warning("Unknown error. " + e + stackTrace(e));
                    xmpResponseHandler(transactionId,
                                       500, "Internal error, could not handle XMP request.");
                    quit = true;
                }
            }
        }
    }

    /**
     * Processes the validity tag.
     *@param xmpDoc the parsed XMP request.
     */
    private void processValidityTag(Document xmpDoc) {
        NodeList nlValidity = xmpDoc.getElementsByTagName("validity");
        String v = null;
        if ((nlValidity.getLength() != 0) && (nlValidity != null)) {
            for (int i = 0; i < nlValidity.getLength(); i++) {
                try {
                    Node n = nlValidity.item(i);
                    if (n != null) {
                        v = n.getFirstChild().getNodeValue().trim();
                        validity = Integer.parseInt(v);
                    }
                } catch (NumberFormatException e) {
                    log.warning("Validity " + v + " is non-numeric");
                    xmpResponseHandler(transactionId,
                                       501, "Validity " + v + " is non-numeric");
                    quit = true;
                } catch (DOMException e) {
                    log.warning("Could not get the validity value from the TEXT_NODE. " + e);
                } catch (Exception e) {
                    log.warning("Unknown error. " + e + stackTrace(e));
                }
            }
        }
    }

    /**
     * Sends an XMP request to the service handler.
     *@param xmpDoc the parsed XMP document
     */
    private void logXmpRequest(Document xmpDoc) {
        if (xmpDoc == null) { return; }

        try {
            if (log.isLoggable(Level.FINE)) {
                OutputFormat format  = new OutputFormat( xmpDoc );
                StringWriter stringOut = new StringWriter();
                XMLSerializer serial = new XMLSerializer( stringOut, format );
                log.fine("    >> ========= Received request =========== <<");
                log.fine(stringOut.toString());
            }
        } catch (NullPointerException e) {
            log.severe("" + e + stackTrace(e));
            xmpResponseHandler(transactionId, 500, "Internal error");
            return;
        } catch (Exception e) {
            log.severe("Unknown error: " + e + stackTrace(e));
            xmpResponseHandler(transactionId, 500, "Internal error");
            return;
        }
    }

    /**
     * stackTrace puts the stack trace of a Throwable into a StringBuffer
     * suitable for logging to file.
     *@param e the Throwable.
     *@return a StringBuffer with the stack trace in.
     */
    public static StringBuffer stackTrace(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        return sw.getBuffer();
    }

    private static class ResponseCodeSetter extends Thread {
        private ServerSocket sock;
        private static Logger log = Logger.getLogger("xmpserver");

        public void run() {
            try {
                while (true) {
                    try {
                        sock = new ServerSocket(Config.getControlPort());
                        while (true) {
                            log.severe("Control port waiting for connection");
                            Socket s = sock.accept();
                            log.severe("Control port connected");
                            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                            String line;
                            do {
                                line = in.readLine();
                                if (line != null) {
                                    log.severe("Control port read " + line);
                                    try {
                                        wantedCode = Integer.parseInt(line);
                                        out.println("Set response code to " + wantedCode);
                                    } catch (NumberFormatException e) {
                                        out.println("Bad integer " + line + ", keeping code " + wantedCode);
                                    }
                                }
                            } while (line != null);
                        }
                    } catch (IOException e) {
                        log.severe("Could not establish a connection to port "
                                   + Config.getHttpServerPort() + e);
                    } catch (Exception e) {
                        log.severe("Unknown exception: " + e);
                    } catch (OutOfMemoryError e) {
                        System.exit(-1);
                    }
                    try {
                        sock.close();
                        sleep(5000);
                    } catch (InterruptedException e) {
                        ;
                    } catch (IOException e) {
                        ;
                    }
                }
            } catch (OutOfMemoryError e) {
                System.exit(-1);
            }
        }

    }
}
