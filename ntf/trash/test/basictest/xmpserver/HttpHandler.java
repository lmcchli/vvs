/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package xmpserver;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * Handles one connection and HTTP traffic on it.
 */
public class HttpHandler extends Thread {
    /** HTTP headers fo use in responses */
    private static final String HTTP_HEADER = "HTTP/1.1 200 OK\r\ncontent-type: text/html\r\n";
    /** Start of HTML pages */
    private static final String HTML_START = "<HTML><HEAD><TITLE>XMP Server</TITLE></HEAD><BODY><H1>XMP Server</H1>";
    /** End of HTML pages */
    private static final String HTML_END = "</BODY></HTML>";
    /** Socket for HTTP communication*/
    private Socket socket = null;
    /** Logger for log messages */
    private Logger log = null;
    /** Writes HTTP messages */
    private DataOutputStream out = null;
    /** XMP requests fo here */
    private XmpHandler xmphandler = null;

    /**
     *Constructor.
     *@param s Socket for the HTTP connection.
     *@param i id of the connection.
     *@param l Logger for the connection
     */
    public HttpHandler(Socket s, int i, Logger l) {
        super("HttpHandler-" + i);
        log = l;
        socket = s;
        
        xmphandler = new XmpHandler(this, log, i);
    }

    
    /**
     * Run method that reads HTTP requests and passes XMP requests on to the XMP
     * handler and forwards <I>get</I> and <I>head</I> requests to special
     * handler functions in this class.
     */
    public void run() {
        try {
            
            if (log.isLoggable(Level.FINE)) {
                log.fine("Handling new HTTP connection.");
            }
            out = new DataOutputStream(socket.getOutputStream());
            InputStream in = socket.getInputStream();
            boolean connected = true;
            
            
            while(connected) {
                
                InternetHeaders respHeaders = new InternetHeaders(in);
                boolean multipart = false;
                String get = null;
                String head = null;
                
                int headerCount = 0;
                Enumeration headerNames = respHeaders.getAllHeaders();
                while( headerNames.hasMoreElements() ) {
                    
                    headerCount++;
                    Header header = (Header)headerNames.nextElement();
                    if( headerCount == 1 ) {
                        if( header.getValue().startsWith("GET") ) {
                            get = header.getValue().substring(3).trim();
                        } else if( header.getValue().startsWith("HEAD") ) {
                            head = header.getValue().substring(4).trim();
                        }
                    }
                    if( header.getValue().indexOf( "Multipart/Related") != -1 ) {
                        multipart = true;
                    }
                }
                if( headerCount == 0 ) {
                    connected = false;
                } else if(get != null) {
                    handleGet(get);
                } else if(head != null) {
                    handleHead(head);
                } else if(multipart) {
                    readMultiple(respHeaders, in);
                } else {
                    readSingle(respHeaders, in);
                }
                
                
            }
            socket.close();
            log.fine("Client disconnected");
        } catch (IOException e) {
            log.severe("Connection problem " + e);
            try {socket.close(); } catch(Exception e2) {}
            return;
        } catch (MessagingException e) {
            log.severe("Connection dropped " + e);
            try {socket.close(); } catch(Exception e2) {}
            return;
        } catch (Exception e) {
            log.severe("Unknown exception: " + e);
            try {socket.close(); } catch(Exception e2) {}
            e.printStackTrace();
            return;
        }
    }

    
    private void readSingle(InternetHeaders headers, InputStream in) throws Exception {
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
            xmphandler.xmpRequestHandler(new String(data).toCharArray());
        }
        // do noting else
    }
    
    private void readMultiple(InternetHeaders headers, InputStream in) throws Exception {
        String boundary = null;
        boolean end = false;
        int contentCount = 0;
        char [] xmpData = new char[0];
        ArrayList attachments = new ArrayList();
        String[] contentType = headers.getHeader("Content-Type");
        if( contentType != null ) {
            String line = contentType[0];
            int bix = line.indexOf("boundary=");
            if( bix != -1 ) {
                boundary = line.substring(bix+9);
            }
        }
        log.fine("Reading multipart with boundary " + boundary );
        if(boundary != null) {
            // time limited later
           while( !end ) {
               InternetHeaders ih = new InternetHeaders(in);
               Enumeration headerStrings = ih.getAllHeaderLines();
              
               while(headerStrings.hasMoreElements()) {
                   String line = (String)headerStrings.nextElement();
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
                       xmpData = new String(data).toCharArray();
                   } else {
                       XmpAttachment att = new XmpAttachment(data, attachmentContentType);
                       attachments.add(att);
                       
                   }
                   contentCount++;
                }
               
           }
        }
        xmphandler.xmpRequestHandler(xmpData, attachments);
        
    }
    
    

    /**
     * Handles HTTP <I>head</I> requests.
     *@param req - the unprocessed part of the request string.
     */
    private void handleHead(String req) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("handling HEAD request");
        }
        httpResponseHandler(HTTP_HEADER);
    }

    /**
     * Handles HTTP <I>get</I> requests. Distributes known requests to special
     * handler functions and returns an HTML page with the component name and
     * version for unknown requests.
     *@param req - the unprocessed part of the request string.
     */
    private void handleGet(String req) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("handling GET request");
        }
        if (req.toLowerCase().startsWith("/config")) {
            handleConfigRequest(req.substring(7));
        } else if (req.toLowerCase().startsWith("/info")) {
            respond(HTTP_HEADER, HTML_START + "<H3>MoIP XMP server</H3>" + HTML_END);
        } else {
            respond(HTTP_HEADER, HTML_START + "<H3>MoIP XMP server</H3>" + HTML_END);
        }
    }

    /**
     * handles <I>config</I> requests.
     *@param req - the unprocessed part of the request string.
     */
    private void handleConfigRequest(String req) {
        int level = 0;
        if (log.isLoggable(Level.FINE)) {
            log.fine("handling GET config request");
        }
        respond(HTTP_HEADER, HTML_START
                + "<H3>Configuration management not implemented.</H3>" + HTML_END);
    }

    /**
     * Handles incoming HTTP requests.
     *@param contentType type of the HTTP request.
     *@param b text of the HTTP request
     */
    private void httpRequestHandler(String contentType, char[] b) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("    >> Handling HTTP request, type " + contentType + " <<");
        }
        if (contentType.toLowerCase().indexOf("text/xml") != -1) {
            xmphandler.xmpRequestHandler(b);
        } else if(contentType.toLowerCase().indexOf("multipart/related") != -1 ) {
	    respond(HTTP_HEADER, HTML_START
                    + "XMP content-type must be <I>text/xml</I>" + HTML_END);

	} else {
            log.severe("Unknown content-type: \"" + contentType + "\", discarding request");
            respond(HTTP_HEADER, HTML_START
                    + "XMP content-type must be <I>text/xml</I>" + HTML_END);
        }
    }

    /**
     * Responds to HTTP requests by adding the content-length to the body and
     * returning it to the client.
     *@param headers - the headers of the response, so far.
     *@param body - the body of the response
     */
    private void respond(String headers, String body) {
        httpResponseHandler(headers + "content-length: " + body.length() + "\r\n\r\n" + body);
    }
    

    /**
     * Returns an HTTP response to the client
     *@param httpResponse the response.
     */
    public synchronized void httpResponseHandler(String httpResponse) {
        try {
            log.fine("sending: " + httpResponse);
            out.writeBytes(httpResponse);
            out.flush();
        } catch (Exception e) {
            log.severe("Unknown exception: " + e);
        }
    }
    
    public synchronized void httpResponseHandler(String xmpResponse, ArrayList attachments) {
        try {
            
            StringBuffer buffer = new StringBuffer();
            buffer.append("--MIME_boundery\r\n"
                    + "Content-Type: text/xml; charset=utf-8\r\n"
                    + "Content-Length: " + xmpResponse.length() + "\r\n\r\n");
            buffer.append(xmpResponse);
            int attachmentSize = 0;
            StringBuffer [] attachmentsBuffer = new StringBuffer[attachments.size()];
            for( int i=0;i<attachments.size();i++ ) {
                attachmentsBuffer[i] = new StringBuffer();
                XmpAttachment attachment = (XmpAttachment) attachments.get(i);
                if( attachment.getSize() > -1) {
                    attachmentsBuffer[i].append("\r\n\r\n--MIME_boundery\r\n"
                        + "Content-Type: " + attachment.getContentType() + " ; charset=utf-8\r\n"
                        + "Content-Length: " + attachment.getSize() + "\r\n\r\n");
                    attachmentSize += attachment.getSize() + attachmentsBuffer[i].length();
                }
            }
            
            
            
            String endLine = "\r\n\r\n--MIME_boundery--\r\n\r\n";
            log.fine("sending multi: " + xmpResponse);
            out.writeBytes("HTTP/1.1 200 OK\r\n"
                + "Content-Type: Multipart/Related; boundary=MIME_boundery\r\n"
                + "Content-Length: " + (buffer.length() + attachmentSize + endLine.length()) + "\r\n\r\n" );
            
            out.writeBytes(buffer.toString());
            
            
            
            for( int i=0;i<attachments.size();i++ ) {
                XmpAttachment attachment = (XmpAttachment) attachments.get(i);
                InputStream reader = attachment.getInputStream();
                if( reader != null ) {
                    byte [] dataArray = new byte[4096];
                    int bytesRead = 0;
                    out.writeBytes(attachmentsBuffer[i].toString());
                    while( (bytesRead = reader.read(dataArray)) > 0 ) {
                        out.write(dataArray, 0, bytesRead);
                    }
                }
            }
            out.writeBytes(endLine);
            out.flush();
            
            
            
            
        } catch (Exception e) {
            log.severe("Unknown exception: " + e);
        }
    }
}
