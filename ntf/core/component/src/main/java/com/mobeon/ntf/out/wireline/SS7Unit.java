/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wireline;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.oe.common.topology.ComponentInfo;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.util.time.NtfTime;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

class SS7Unit extends Thread implements Constants {

    /****************************************************************
     * SS7ConnectionPool handles a dynamic pool of SS7Connections, and knows if
     * there are free connections and how to create new connections when
     * needed.
     */
    private final static Logger log = Logger.getLogger(SS7Unit.class);
    
    private class SS7ConnectionPool {
        
        private SS7Connection[] allConns;
        private ArrayList<SS7Connection> freeConns;
        private int connCount;
        private int connLimitMsgId;

        SS7ConnectionPool() {
            connLimitMsgId= log.createMessageId();
            allConns= new SS7Connection[Config.getMaxXmpConnections()];
            freeConns= new ArrayList<SS7Connection>();
        }

        /****************************************************************
         * takeConnection reserves an SS7Connection, creating one if
         * necessary (and possible).
         * @return a free connection. This function blocks until a free
         * connection is available.
         */
        public synchronized SS7Connection takeConnection(String unitName) {
            SS7Connection c;
            while (freeConns.isEmpty()) {
                if (connCount < Config.getMaxXmpConnections()) {
                    c = new SS7Connection(connCount, this, unitName);
                    if (Config.getLogLevel() >= Logger.L_DEBUG) {
                        log.logMessage("SS7ConnectionPool.takeConnection: created " + c.getName(), Logger.L_DEBUG);
                    }
                    allConns[connCount++] = c;
                    c.start();
                    freeConns.add(c);
                }
                if (freeConns.isEmpty()) {
                    log.logReduced(connLimitMsgId, "SS7ConnectionPool.takeConnection:"
                            + "No free connections available. Wait for a connection to be released.", Logger.L_ERROR);
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            c = (freeConns.remove(freeConns.size() - 1));
            log.logReducedOff(connLimitMsgId, "OK SS7ConnectionPool.takeConnection: Using a freed connection", Logger.L_ERROR);
            return c;
        }

        /****************************************************************
         * releaseConnection returns a connection for reuse, when it has
         * finished a request.
         * @param conn the free connection.
         */
        public void releaseConnection(SS7Connection conn) {
            if (Config.getLogLevel() >= Logger.L_DEBUG) {
                log.logMessage(" SS7ConnectionPool.releaseConnection: " + conn.getName(), Logger.L_DEBUG);
            }
            
            synchronized(this) {
                freeConns.add(conn);
                this.notifyAll();
            }
        }

        /****************************************************************
         * Reconnect, if disconnected, each active SS7Connection.
         */
        public synchronized void reconnectConnections(){
            for(int i = 0; i < connCount; i++){
                allConns[i].connect();
            }
        }
    }
    /**************** END of  SS7ConnectionPool ***********************/









    /****************************************************************
     *Start of SS7Connection
     */
    private class SS7Connection extends Thread {



        /****************************************************************
         * XmpResponseReader parses the responses from the xmp server.
         * When a response is recived, XmpResponseReader reports the result
         * to the SS7ResponseHandler
         */
        private class SS7ResponseReader extends Thread {

            SS7ResponseReader(int id, String unitName) {
                setName(unitName + ".SS7Connection-" + id + "_r");
            }

            public void run() {
                log.logMessage("SS7ResponseReader Starting", Logger.L_VERBOSE);
                while (true) { // Loop forever no matter what
                    try {
                        while (true) { // Loop forever as long as nothing unexpected happens
                            synchronized (connection_object) {
                                while (!connected) {
                                    try {
                                        connection_object.wait();
                                    } catch (InterruptedException e) {
                                    }
                                    if (shouldBeBurtallyKilled)
                                        return;
                                }
                            }

                            try {
                                String temp = null;
                                
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                XMPProtocol xp = new XMPProtocol();
                                while ((temp = in.readLine()) != null) {
                                    if (temp.toLowerCase().indexOf("content-length") != -1) {
                                        synchronized (in) {
                                            int length = Integer.parseInt(temp.substring(temp.indexOf(":") + 1).trim());
                                            char[] b = new char[length + 2];
                                            in.read(b, 0, length + 2);
                                            xp.parseXmpResponse(b);
                                        }
                                    }
                                }
                                if (shouldBeBurtallyKilled)
                                    return;
                                connected = false;
                                if (Config.getLogLevel() >= Logger.L_DEBUG) {
                                    log.logMessage("Nothing to read. ", Logger.L_DEBUG);
                                }
                                disconnect();
                            } catch (InterruptedIOException e) {
                                if (Config.getLogLevel() >= Logger.L_DEBUG) {
                                    log.logMessage("Nothing to read. " + e, Logger.L_DEBUG);
                                }
                                disconnect();
                            } catch (EOFException e) {
                                if (Config.getLogLevel() >= Logger.L_DEBUG) {
                                    log.logMessage("Connection closed. " + e, Logger.L_DEBUG);
                                }
                                disconnect();
                            } catch (IOException e) {
                                log.logMessage("Unexpected: " + e, Logger.L_ERROR);
                                disconnect();
                            }
                        }// end of while(true)
                    } catch (Exception e) {
                        log.logMessage("Unexpected: " + NtfUtil.stackTrace(e), Logger.L_ERROR);
                    } catch (OutOfMemoryError e) {
                        try {
                            ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                            log.logMessage("NTF out of memory, shutting down..." + NtfUtil.stackTrace(e), Logger.L_ERROR);
                        } catch (OutOfMemoryError e2) {
                            ;
                        } // ignore
                    }
                }
            }
        }

        /***************************************
         *End of SS7ResponseReader
         */












        private String xmpReq;
        private boolean connected = false;
        private Boolean connection_object;
        private SS7ConnectionPool pool;
        private Socket socket = null;
        private SS7ResponseReader reader = null;

        /****************************************************************
         * @param id unique integer among SS7Connections, normaly used for thread name.
         */
        SS7Connection(int id, SS7ConnectionPool pool, String unitName) {
            setName(unitName + ".SS7Connection-" + id + "_w");
            this.pool= pool;
            connection_object = new Boolean("");
            reader = new SS7ResponseReader(id, unitName);
            reader.start();
            connect();
        }

        /** Connects to the xmp server*/
        public void connect(){
            try{
                if(!connected){
                    socket = new Socket(host, port);
		    socket.setSoTimeout(Config.getXmpTimeout() * 1000 );
                    if(Config.getLogLevel() >= Logger.L_DEBUG){
                        log.logMessage(" Connected to " + host + " on port " + port, Logger.L_DEBUG);
                    }
                    xmpHostIsDown = false;
                    connected = true;
                    synchronized (connection_object) {
                        connection_object.notifyAll();
                    }
                }
            }
            catch(UnknownHostException e){
                log.logMessage(super.getName() + " SS7Connection.connect: Could not connect to " + host + " Error: " + e, Logger.L_ERROR);
                xmpHostIsDown = true;
                socket = null;
                retakeConnection.reportConnectionProblem();
            }
            catch(IOException e){
                log.logMessage(super.getName() + " SS7Connection.connect: Could not connect to " + host + " Error: " + e, Logger.L_ERROR);
                xmpHostIsDown = true;
                socket = null;
                retakeConnection.reportConnectionProblem();
            }
        }

        /****************************************************************
         * Disconnect closes the socket connection.
         */
        public void disconnect() {
            log.logMessage("Disconnecting from " + host, Logger.L_VERBOSE);
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {}
            }
            socket = null;
            xmpHostIsDown = true;
            connected = false;
            retakeConnection.reportConnectionProblem();
        }
        /****************************************************************
         * Run does the real work in SS7Connection. It loops forever waiting for
         * a request (a xmp request). When a xmp request is available, all connection stuff is
         * handled and the request is delivered to the SS7Gateway. The result is
         * handles by SS7ResponseReader.
         */
        public void run() {
            while (true) {
                synchronized(retakeConnection){
                    while (socket == null) {
                        if(Config.getLogLevel() >= Logger.L_DEBUG){
                            log.logMessage(" run: Waiting for a new socketconnection", Logger.L_DEBUG);
                        }
                        try {retakeConnection.wait();} catch (InterruptedException e) {}
                        if(shouldBeBurtallyKilled)
                            return;
                    }
                }
                synchronized(this){
                    while (xmpReq == null) {
                        if(Config.getLogLevel() >= Logger.L_DEBUG){
                            log.logMessage(" run: waiting for a new XMP-request", Logger.L_DEBUG);
                        }
                        try {wait();} catch (InterruptedException e) {}
                        if(shouldBeBurtallyKilled)
                            return;
                    }
                }
                if (xmpReq != null) {
                    if (sendRequest()) {
                        if(Config.getLogLevel() >= Logger.L_DEBUG){
                            log.logMessage(" run: Successfully sent a new XMP request.", Logger.L_DEBUG);
                        }
                    }
                    pool.releaseConnection(this);
                }//end of if (xmpReq != null)
            }//end of while (true)
        }

        private synchronized boolean sendRequest() {
            try{
                OutputStream out = socket.getOutputStream();
                OutputStreamWriter wout = new OutputStreamWriter(out);
                if(Config.getLogLevel() == Logger.L_DEBUG){
                    log.logMessage("sendRequest: \n " +  xmpReq, Logger.L_DEBUG);
                }
                wout.write(xmpReq);
                wout.flush();
                xmpReq = null;
                numberOfSentRequest++;
		if(Config.getLogLevel() >= Logger.L_DEBUG){
		    log.logMessage("sendRequest: Number of pending requests is " +
				   (numberOfSentRequest - numberOfReceivedResponses), Logger.L_DEBUG);
		}
                return true;
            }catch(IOException e){
                log.logMessage(super.getName() + ".sendRequest: Could not send XMP-request. " + e, Logger.L_VERBOSE);
                disconnect();
                return false;
            }
            catch(Exception e){
                log.logMessage(super.getName() + ".sendRequest: Unknown error. Could not send XMP-request. " + e, Logger.L_VERBOSE);
                disconnect();
                return false;
            }
        }

        /****************************************************************
         * Makes this connections thread access the URL asynchronously,
         * i.e. this method returns quickly and the result is discarded by the
         * connection thread
         * @param req The request URL including parameters
         */
        public synchronized void ss7RequestDialogue(String req) {
            if(xmpHostIsDown) return;
            xmpReq= req;
            notify();
        }

        /****************************************************************
         */
        public String toString() {
            return getClass().getName() + ": " + "Request in progress = " + (xmpReq == null) + " Reader = " + reader.isAlive();
        }
    }
    /**************** END of OAConnection ***************************/








    /****************************************************************
     *Start of XMPProtocol
     */
    private class XMPProtocol{

        public synchronized void parseXmpResponse(char[] b){
            try{
		String res = new String(b);
		int start = res.indexOf("<");

                if(Config.getLogLevel() == Logger.L_DEBUG){
                    log.logMessage("XmpResponse: \n " +  res, Logger.L_DEBUG);
                }

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                StringReader sr = new StringReader(new String(b, start, (b.length-start)));
                InputSource iSrc = new InputSource(sr);
                Document doc = builder.parse(iSrc);

                for(int i = 0; i < doc.getElementsByTagName("xmp-service-response").getLength(); i++){
                    String transaction_id = "";
                    String code = "";
		    String statusText = "";
		    numberOfReceivedResponses++;
		    if(Config.getLogLevel() >= Logger.L_DEBUG){
			log.logMessage("parseXmpResponse: Number of pending requests is " +
				       (numberOfSentRequest - numberOfReceivedResponses), Logger.L_DEBUG);
		    }

		    Element xsr = (Element)doc.getElementsByTagName("xmp-service-response").item(i);
		    if(xsr.getAttributes().getNamedItem("transaction-id") != null){
			transaction_id = xsr.getAttributes().getNamedItem("transaction-id").getNodeValue();
                        if(Config.getLogLevel() >= Logger.L_DEBUG){
                            log.logMessage("XMPProtocol.parseXmpResponse: transaction-id: " + transaction_id, Logger.L_DEBUG);
                        }
		    }

                    if(xsr.getElementsByTagName("status-code").getLength() != 0){
                        code = xsr.getElementsByTagName("status-code").item(0).getFirstChild().getNodeValue();
                        if(Config.getLogLevel() >= Logger.L_DEBUG){
                            log.logMessage("XMPProtocol.parseXmpResponse: status-code: " + code, Logger.L_DEBUG);
                        }
		    }
		    if(xsr.getElementsByTagName("status-text").getLength() != 0){
			statusText = xsr.getElementsByTagName("status-text").item(0).getFirstChild().getNodeValue();
                        if(Config.getLogLevel() >= Logger.L_DEBUG){
                            log.logMessage("XMPProtocol.parseXmpResponse: Status-text: " + statusText, Logger.L_DEBUG);
                        }
		    }
		    response.handleResponse(code, transaction_id);
                }
            }
            catch(Exception e){
                log.logMessage("XMPProtocol.parseXmpResponse: Could not parse response. " + e + " Stack: "
                + NtfUtil.stackTrace(e), Logger.L_ERROR);
            }
        }

        public synchronized String makeXmpRequest(String transid,
                                                  String mailbox_id,
                                                  String dnis,
                                                  int indicator_type,
                                                  int nvoice,
                                                  int nfax,
                                                  int nemail,
                                                  int ntotal,
                                                  String type_of_number,
                                                  String numbering_plan_id,
                                                  int validity,
                                                  boolean message_report){
            Document doc= new DocumentImpl();
            Element root;
            Element service;
	    try{
		root = doc.createElement("xmp-message");
		root.setAttribute("xmlns", "http://www.abcxyz.se/xmp-1.0");
		service = doc.createElement("xmp-service-request");
		service.setAttribute("service-id", IServiceName.MWI_NOTIFICATION);
		service.setAttribute("transaction-id", transid);
		service.setAttribute("client-id", ntfID);
		addElement(doc, service, "validity", Integer.toString(validity));
		addElement(doc, service, "message-report", ""+ message_report);
                addParameterElement(doc, service, "indicator-type", "" + determineIndicatorType(indicator_type));
		addParameterElement(doc, service, "mailbox-id", mailbox_id);
		addParameterElement(doc, service, "number", dnis);
		addParameterElement(doc, service, "type-of-number", type_of_number);
		addParameterElement(doc, service, "numbering-plan-id", numbering_plan_id);
		if(ntotal != -1){
		    addParameterElement(doc, service, "new-total", Integer.toString(ntotal));
		}
		if(nemail != -1){
		    addParameterElement(doc, service, "new-email", Integer.toString(nemail));
		}
		if(nfax != -1){
		    addParameterElement(doc, service, "new-fax", Integer.toString(nfax));
		}
		if(nvoice != -1){
		    addParameterElement(doc, service, "new-voice", Integer.toString(nvoice));
		}
		root.appendChild( service );
		doc.appendChild( root );
		String d = getDocumentAsString(doc);
		try{
		    String http_content_length    = "Content-Length: " + d.length() + "\r\n\r\n";
		    return new String(httpHeaders + http_content_length + d);
		}
		catch(Exception e){
		    log.logMessage("XMPProtocol: Unknown error " + e , Logger.L_ERROR);
		    return null;
		}
	    }catch(Exception e){
		log.logMessage("XMPProtocol: Unknown error " + e , Logger.L_ERROR);
		return null;
	    }
        }

        private void addParameterElement(Document doc, Element service, String attributeName, String textValue){
            Element parameter = doc.createElement("parameter");
            parameter.setAttribute("name",  attributeName);
            parameter.appendChild( doc.createTextNode(textValue) );
            service.appendChild( parameter );
        }

	private void addElement(Document doc, Element service, String attributeName, String textValue){
	    Element el = doc.createElement(attributeName);
	    el.appendChild(doc.createTextNode(textValue));
	    service.appendChild(el);
	}

        private String getDocumentAsString(Document doc){
            StringWriter  stringOut;
            try{
                OutputFormat format     = new OutputFormat( doc );
                stringOut               = new StringWriter();
                XMLSerializer serial    = new XMLSerializer( stringOut, format );
                serial.asDOMSerializer();
                serial.serialize( doc.getDocumentElement() );
                return stringOut.toString();
            }catch(Exception e){
                log.logMessage("XMPProtocol: Internal error. Could not create a XML string to OA. " + e , Logger.L_ERROR);
                return "";
            }
        }


    }

    private int determineIndicatorType(int t){
        switch(t){
            case NTF_MWIOff:
                return 0;
            case NTF_VOICE:
                return 1;
            case NTF_FAX:
                return 2;
            case NTF_EMAIL:
                return 3;
            default:
                return 1;
        }
    }

    /*******************************************
     *End of XMPProtocol
     */











    /****************************************************************
     *Start of RetakeConnection
     */
    private class RetakeConnection extends Thread{

        private boolean notConnected = true;
        private boolean sleep = true;
        private boolean errorReported = false;
        private int ss7ConnDown;
        private Boolean start;
        private Socket s = null;

        public RetakeConnection(String unitName) {
            setName(unitName + ".rc");
            ss7ConnDown = log.createMessageId();
            start = new Boolean("");
            this.start();
        }

        private void connect(){
            try{
                s = new Socket(host, port);
                log.logReducedOff(ss7ConnDown, "OK Reconnected to " + host + " on port " + port, Logger.L_ERROR);
                notConnected = false;
                sleep = true;
                pool.reconnectConnections();
                synchronized(this){
                    this.notifyAll();
                }
                center.reportConnectionEstablished(host);
                errorReported = false;
                s.close();
                s = null;
            }
            catch(UnknownHostException e){
                log.logReduced(ss7ConnDown, "Could not connect to " + host + " Error: " + e, Logger.L_ERROR);
                s = null;
            }
            catch(IOException e){
                log.logReduced(ss7ConnDown, "Could not connect to " + host + " Error: " + e, Logger.L_ERROR);
                s = null;
            }
        }

        public synchronized void reportConnectionProblem(){
            if(!errorReported){
                if(Config.getLogLevel() >= Logger.L_DEBUG){
                    log.logMessage("We have a connectionproblem. Try to established a new connection.", Logger.L_DEBUG);
                }
                sleep = false;
                center.reportConnectionProblem(host);
                synchronized(start){
                    start.notify();
                }
                errorReported = true;
                notConnected = true;
            }
        }

        public void run() {

            while(true){

                synchronized(start) {
                    while(sleep) {
                        try {start.wait();} catch (InterruptedException e) {}
                    }
                }
                while(notConnected){
                    if(shouldBeBurtallyKilled){
                        return;
                    }
                    connect();
                    try{Thread.sleep(5000);}catch(Exception e){};
                }
            }
        }
    }

    /*******************************************
     *END of RetakeConnection
     */


















    private String host;
    private String ntfID = null;
    private String httpHeaders = null;
    private int port;
    private int numberOfSentRequest = 0;
    private int numberOfReceivedResponses = 0;
    private boolean xmpHostIsDown = false;
    private boolean islocal = true;
    private boolean shouldBeBurtallyKilled = false;
    private SS7GatewayHandler center;
    private SS7ConnectionPool pool = null;
    private SS7ResponseHandler response = null;
    private RetakeConnection retakeConnection = null;

    /****************************************************************
     * @param id unique integer among SS7Units. Used for thread name.
     */
    SS7Unit(int id, SS7GatewayHandler center, SS7ResponseHandler r, String h, int p, boolean local) {
        host = h;
        port = p;
        islocal = local;
        setName("SS7Unit-" + id);
        this.center = center;
        pool= new SS7ConnectionPool();
        retakeConnection = new RetakeConnection(this.getName());
        response = r;
        String clientId = MoipMessageEntities.MESSAGE_SERVICE_NTF;
        ComponentInfo ci;
		try {
			ci = OEManager.getSystemTopologyInfo().getLocalComponentInfoOfType(MoipMessageEntities.MESSAGE_SERVICE_NTF);
	        if(ci != null) {
	        	if(ci.getName() != null) {
	        		clientId = ci.getName();
	        	}
	        }

		} catch (ConfigurationDataException e) {
			e.printStackTrace();
		}
        ntfID = clientId;
        makeGeneralHTTPHeaders();
    }

    private void makeGeneralHTTPHeaders(){
        String http_post              = "POST /MWINotification HTTP/1.1\r\n";
        String http_content_type      = "Content-Type: text/xml; charset=utf-8\r\n";
        String http_host              = "Host: " + host + "\r\n";
        String http_keepAlive         = "Connection: Keep-Alive\r\n";
        httpHeaders = http_post + http_content_type + http_host + http_keepAlive;
    }

    public boolean isLocal(){
        return islocal;
    }

    public void stopUnit(boolean kill){
        shouldBeBurtallyKilled = kill;
    }

    public String getHost(){
        return host;
    }
    /****************************************************************
     * wmwRequest creates an mwm request, sends it to
     * the SS7gateway host and reads the result.
     * The communication, read and write, is handled by a separate thread, so this method returns
     * quickly.
     * @param transid Identity of this notification.
     * @param mailbox_id Unique identity of the end-user that initiaded xmp-message-request
     * @param dnis phone number of the target for notification.
     * @param type_of_number Specifies the type of number, e.g. international, National, Network, IP-address.
     * @param numbering-plan-id Specifies the numbering plan identification. Format in accordance with GSM std. E.g. e.164, Data, National, private.
     * @param validity Specifies the maximum time (in seconds) the xmp-request is valid.
     * @param message-report Indicates if the user wants an xmp-service-response.
     * @param nvoice number of new voice messages.
     * @param nfax number of new fax messages.
     * @param nemail number of new email messages.
     * @param ntotal total number of new messages.
     * The "n-parameters" can be set to -1 if they should be ignored. ntotal is
     * used only if all the other ones are -1, i.e. if the notification should
     * only give a summary for all types.
     * @return fals if the request can not be sent
     */
    public synchronized boolean wmwRequest(String transid,
                                           String mailbox_id,
                                           String dnis,
                                           int indicator_type,
                                           int nvoice,
                                           int nfax,
                                           int nemail,
                                           int ntotal,
                                           String type_of_number,
                                           String numbering_plan_id,
                                           int validity,
                                           boolean message_report) {
        if (xmpHostIsDown) return false;
        try {
            String xmp_request="";
            try{
                XMPProtocol xp = new XMPProtocol();
                xmp_request = new XMPProtocol().makeXmpRequest(transid,
                                                               mailbox_id,
                                                               dnis,
                                                               indicator_type,
                                                               nvoice,
                                                               nfax,
                                                               nemail,
                                                               ntotal,
                                                               type_of_number,
                                                               numbering_plan_id,
                                                               validity,
                                                               message_report);
            }catch(Exception e){
                log.logMessage("Could not create a XMP request for transid " + transid + " : " + e.getMessage(), Logger.L_ERROR);
                return false;
            }
            if(xmp_request.length() == 0){
                return false;
            }
            else{
                SS7Connection conn= pool.takeConnection(this.getName());
                if (conn == null) return false;
                conn.ss7RequestDialogue(xmp_request);
                return true;
            }
        } catch (Exception e) {
            log.logMessage("Unknown error for transid " + transid + " : " + e.getMessage(), Logger.L_ERROR);
            return false;
        }
    }

    private int determineEmailType(int t){
        switch(t){
            case NTF_MWIOff:
                return 0;
            case NTF_VOICE:
                return 1;
            case NTF_FAX:
                return 2;
            case NTF_EMAIL:
                return 3;
            default:
                return 1;
        }
    }
}
