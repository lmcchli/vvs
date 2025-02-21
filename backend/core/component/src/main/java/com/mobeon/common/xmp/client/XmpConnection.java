/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp.client;


import com.mobeon.common.util.M3Utils;
import com.mobeon.common.xmp.XmpAttachment;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.Header;
import jakarta.mail.MessagingException;

/**
 * XmpConnection implements a connection to an XmpServer.
 */
public class XmpConnection extends Thread {
    private XmpResponseHandler responseHandler;
    
    private XmpClient client = null;
    private XmpRequestInfo request = null;    
    
    private String host;
    private int port;
    private int timeout;
    private int id;
    private XmpUnit unit;
    private boolean connected = false;
    private Object connectionMonitor = new Object(); 
    private Socket socket = null;
    private XmpResponseReader reader = null;
    
    
    

    /**
     *@param id - unique integer among XmpConnections, normaly used for thread name.
     *@param pool - the connection pool that handles this connection.
     *@param unit - the unit this connection belongs to.
     *@param responseHandler - where responses shall be sent.
     */
    XmpConnection(int id, 
                  XmpUnit unit, 
                  XmpResponseHandler responseHandler) {
        setName(unit.getName() + ".connection-" + id);
        client = XmpClient.get();
        this.id = id;
        host = unit.getHost();
        port = unit.getPort();
        timeout = client.getTimeout() * 1000;
        this.unit = unit;
        this.responseHandler = responseHandler;
        reader = new XmpResponseReader(id, unit.getName());
        reader.start();
        start();
    }
    
    /**
     * Connects to the xmp server
     *@return true iff the connection was successful.
     */
    /*package*/ boolean connect() {
        if (client.isDebugEnabled()) {
            client.debug("Connecting to " + host + " on port " + port);
        }
        for( int i=0;i<2 && !connected;i++ ) {
            try {
                
                if (!connected) {
                    socket = new Socket(host, port);
                    socket.setSoTimeout(timeout);
                    socket.setSoLinger(true, 5);
                    
                    
                    synchronized (connectionMonitor) {
                        connected = true;
                        connectionMonitor.notify();
                    }
                     
                }
            } catch (UnknownHostException e) {
                client.error(" could not connect to " + host + ": " + e);
                
            } catch (IOException e) {
                client.error(" could not connect to " + host + ": " + e);
                
            }
            if( !connected && i == 0 ) {
                try {
                    sleep(5000);
                } catch(Exception e) {}
            }
        }
        return connected;
    }
    
    /**
     * Closes a particular socket connection.
     */
    private void disconnect(Socket s) {
        if (s == null || s == socket) {
            client.info("XmpConnection.disconnect from " + host);
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) { ; }
            }
            connected = false;
        } else { //The supplied socket has been replaced, quietly close the old socket
            try {
                s.close();
            } catch (IOException e) { ; }
        }
    }

    /**
     * Closes the socket connection.
     */
    /*package*/ void disconnect() {
        disconnect(null);
    }
    
   
    
   

    /**
     * Run does the real work in XmpConnection. It loops forever waiting for
     * a request (a xmp request). When a xmp request is available, all connection stuff is
     * handled and the request is delivered to the XmpGateway. The result is
     * handles by XmpResponseReader.
     */
    public void run() {
        while (client.isKeepRunning()) { 
            try {
                while (client.isKeepRunning()) { //Loop forever as long as nothing unexpected happens
                    if( unit.isAvailable() ) {
                        request = unit.getRequest();
                        if (request != null) {
                            sendWithConnect();
                            request = null;
                        } else {
                            unit.tryEmptyRequest();
                        }
                    } else {
                        // sleep and try again later.
                        try {
                            sleep(2000);
                        } catch(Exception e) {}
                    }
                }
            } catch (Exception e) {
                client.error("Unexpected: ", e);
            } 
        }
    }
    
    private synchronized void sendWithConnect() {
        if( !connected ) {
            connect();
        }
        
        boolean sendResult = false;
        if( connected ) {
           sendResult = send();
        } else {
            unit.connectionDown();
        }
        if( sendResult ) {
            client.debug("XmpConnection successfully sent an XMP request.");
            
        } else {
            client.debug("XmpConnection failed to send an XMP request.");
            responseHandler.sendFailed(unit, request.id);
        }
           
    }
    
    
    
    private boolean send() {
        try {
            if( request.attachments != null && request.attachments.length > 0) {
                return sendWithAttachment();
            } else {
                OutputStream out = socket.getOutputStream();
                //OutputStreamWriter wout = new OutputStreamWriter(out);
                DataOutputStream wout = new DataOutputStream(out);
                if (client.isDebugEnabled()) {
                    client.debug("send to: " + host + ":" + port + "\n " +  request.request);
                }
                wout.writeBytes("POST /" + request.service + " HTTP/1.1\r\n"
                    + "Content-Type: text/xml; charset=utf-8\r\n"
                    + "Host: " + host + "\r\n"
                    + "Connection: Keep-Alive\r\n"
                    + "Content-Length: " + request.request.length() + "\r\n\r\n");
                wout.writeBytes(request.request);
                wout.flush();
            }
            return true;
        } catch (IOException e) {
            client.warn(super.getName() + ".send: Could not send XMP-request. " + e);
            disconnect();
            return false;
        } catch (Exception e) {
            client.warn(super.getName() + ".send: Unknown error. Could not send XMP-request. " + e);
            disconnect();
            return false;
        }
        
    }
    
    private boolean sendWithAttachment() {
        try {
            OutputStream out = socket.getOutputStream();
            DataOutputStream wout = new DataOutputStream(out);
            //OutputStreamWriter wout = new OutputStreamWriter(out);
            if (client.isDebugEnabled()) {
                client.debug("send to: " + host + ":" + port + "\n " +  request.request);
            }
            
            StringBuffer buffer = new StringBuffer();
            buffer.append("--MIME_boundery\r\n"
                    + "Content-Type: text/xml; charset=utf-8\r\n"
                    + "Content-Length: " + request.request.length() + "\r\n\r\n");
            buffer.append(request.request);
            int attachmentSize = 0;
            StringBuffer [] attachmentsBuffer = new StringBuffer[request.attachments.length];
            //byte[][] attachmentsBytes = new byte[request.attachments.length][];
            for( int i=0;i<request.attachments.length;i++ ) {
                attachmentsBuffer[i] = new StringBuffer();
                XmpAttachment attachment = request.attachments[i];
                if( attachment != null && attachment.getSize() > -1) {
                    attachmentsBuffer[i].append("\r\n\r\n--MIME_boundery\r\n"
                        + "Content-Type: " + attachment.getContentType() + " ; charset=utf-8\r\n"
                        + "Content-Length: " + attachment.getSize() + "\r\n\r\n");
                    attachmentSize += attachment.getSize() + attachmentsBuffer[i].length();
                }
            }
            
            
            
            String endLine = "\r\n\r\n--MIME_boundery--\r\n\r\n";
            
            wout.writeBytes("POST /" + request.service + " HTTP/1.1\r\n"
                + "Content-Type: Multipart/Related; boundary=MIME_boundery\r\n"
                + "Host: " + host + "\r\n"
                + "Connection: Keep-Alive\r\n"
                + "Content-Length: " + (buffer.length() + attachmentSize + endLine.length()) + "\r\n\r\n" );
            
            wout.writeBytes(buffer.toString());
            
            
            
            for( int i=0;i<request.attachments.length;i++ ) {
                XmpAttachment attachment = request.attachments[i];
                if( attachment != null ) {
                    InputStream reader = attachment.getInputStream();
                    if( reader != null ) {
                        byte [] dataArray = new byte[4096];
                        int bytesRead = 0;
                        wout.writeBytes(attachmentsBuffer[i].toString());
                        while( (bytesRead = reader.read(dataArray)) > 0 ) {
                            wout.write(dataArray, 0, bytesRead);
                        }
                    }
                }
            }
            wout.writeBytes(endLine);
            wout.flush();
            return true;
        } catch (IOException e) {
            client.warn(super.getName() + ".send: Could not send XMP-request. " + e);
            disconnect();
            return false;
        } catch (Exception e) {
            client.warn(super.getName() + ".send: Unknown error. Could not send XMP-request." + M3Utils.stackTrace(e).toString());
            disconnect();
            return false;
        }
    }
    
    
    
    /**
     *@return a printable representation of this connection.
     */
    public String toString() {
        return getClass().getName() + ": " + "Request in progress = "
            + (request == null) + " Reader = " + reader.isAlive();
    }

    /**
     * XmpResponseReader parses the responses from the xmp server.
     * When a response is received, XmpResponseReader reports the result
     * to the XmpResponseHandler
     */
    private class XmpResponseReader extends Thread  {
        
        /**
         * Constructor.
         *@param id - id of this connection.
         *@param unitName name of the unit this connection belongs to.
         */
        XmpResponseReader(int id, String unitName) {
            super(unitName + ".connection-" + id + "_r");
        }
        
        /**
         * ntfRun does all the work of this class.
         * It waits for the sender thread to connect, then continuously reads
         * HTTP responses from the connection, extracts the XMP response and
         * sends it to the response handler.
         */
        public void run() {
            
            Socket s = null;
            while(client.isKeepRunning()) {
                
                
                synchronized (connectionMonitor) {
                    while (!connected && client.isKeepRunning()) {
                        client.info("XmpResponseReader waiting for connection");
                        try {
                            connectionMonitor.wait();
                        } catch (InterruptedException e) { ; }
                    }
                }
                try {
                    s = socket;
                    InputStream in = s.getInputStream();
                    
                    while(connected && client.isKeepRunning()) {
                        InternetHeaders respHeaders = new InternetHeaders();
                        
                        respHeaders.load(in);
                        
                        boolean multipart = false;
                        
                        int headerCount = 0;
                        Enumeration<Header> headerNames = respHeaders.getAllHeaders();
                        while( headerNames.hasMoreElements() ) {
                            headerCount++;
                            Header header = headerNames.nextElement();
                            if( header.getValue().indexOf( "Multipart/Related") != -1 ) {
                                multipart = true;
                            }
                        }
                        if( headerCount == 0 ) {
                            connected = false;
                        } else if(multipart) {
                            readMultipart(respHeaders, in);
                        } else {
                            readSingle(respHeaders, in);
                        }
                        
                        
                    }
                    
                    
                } catch (IOException e) {
                    client.error("Connection problem " + e);
                    disconnect(s);
                } catch (MessagingException e) {
                    if( e.toString().indexOf("SocketTimeoutException" ) == -1 ) { 
                        client.error("Exception reading headers, " + e.toString() );
                        disconnect(s);
                    }
                        
                }  catch (Exception e) {
                    client.error("Unknown exception: " + M3Utils.stackTrace(e).toString());
                    disconnect(s);
                }
               
            }
            
        }

        private void readSingle(InternetHeaders headers, InputStream in) throws IOException {
            int contentLen = -1;
            String[] contentLength = headers.getHeader("Content-Length");
            if(contentLength != null) {
                try {
                    contentLen = Integer.parseInt(contentLength[0]);
                }
                catch(NumberFormatException ignore){}
            }
            if( contentLen > 0 ) {
                //InputStreamReader reader = new InputStreamReader(in);
                byte[] data = new byte[contentLen];
                int bytesRead = 0;
                int position = 0;
                while( bytesRead < data.length) {
                    int currentBytes = in.read(data, position, data.length - bytesRead);
                    position += currentBytes;
                    bytesRead += currentBytes;
                }
                responseHandler.handleResponse(new String(data), unit, null);
            }
           
        }
    
    private void readMultipart(InternetHeaders headers, InputStream in) throws IOException, MessagingException {
        String boundary = null;
        boolean end = false;
        int contentCount = 0;
        byte [] xmpData = new byte[0];
        ArrayList<XmpAttachment> attachments = new ArrayList<XmpAttachment>();
        String[] contentType = headers.getHeader("Content-Type");
        if( contentType != null ) {
            String line = contentType[0];
            int bix = line.indexOf("boundary=");
            if( bix != -1 ) {
                boundary = line.substring(bix+9);
            }
        }
        if(boundary != null) {
            client.debug("Reading multipart answer with boundary " + boundary);
            // time limited later
           while( !end ) {
               InternetHeaders ih = new InternetHeaders(in);
               Enumeration<String> headerStrings = ih.getAllHeaderLines();
              
               while(headerStrings.hasMoreElements()) {
                   String line = headerStrings.nextElement();
                   if( line.indexOf(boundary + "--") != -1 ) {
                       end = true;
                       break;
                   }
               }
               int contentLen = -1;
               String[] contentLength = ih.getHeader("Content-Length");
               if(contentLength != null) {
                   try {
                       contentLen = Integer.parseInt(contentLength[0]);
                   }
                   catch(NumberFormatException ignore){}
               }
               if( contentLen > 0 ) {
                   String attachmentContentType = "unknown";
                   String[] attachmentContentTypeLines = ih.getHeader("Content-Type");
                   if(attachmentContentTypeLines != null && attachmentContentTypeLines.length > 0 ) {
                       attachmentContentType = attachmentContentTypeLines[0];
                   }
                   byte[] data = new byte[contentLen];
                   int bytesRead = 0;
                   int position = 0;
                   while( bytesRead < data.length) {
                       int currentBytes = in.read(data, position, data.length - bytesRead);
                       position += currentBytes;
                       bytesRead += currentBytes;
                   }
                   if( contentCount == 0 ) {
                       xmpData = data;
                   } else {
                       XmpAttachment att = new XmpAttachment(data, attachmentContentType);
                       attachments.add(att);
                       client.debug("Got attachment " + attachmentContentType);
                   }
                   contentCount++;
                }
               
           }
           responseHandler.handleResponse(new String(xmpData), unit, attachments);
        }
        
        
    }
       
    }
}
