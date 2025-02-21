package com.mobeon.smsc.management.http;

import com.mobeon.smsc.interfaces.TrafficCentral;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;


/**
 * Handles one connection and HTTP traffic on it.
 */
public class HttpHandler extends Thread implements HtmlConstants {
    /** HTTP headers fo use in responses */
    private static final String HTTP_HEADER = "HTTP/1.1 200 OK\r\ncontent-type: text/html\r\n";
    /** Socket for HTTP communication*/
    private Socket socket = null;
    /** Logger for log messages */
    private Logger log = null;
    /** Writes HTTP messages */
    private PrintWriter out = null;
    /**Used to create a HTTP responses when a change settings request arrives.*/
    private HttpSettingsHandler httpSettings = null;

    public static String HtmlVersionHeader;
    public static String version;

    private HttpMainPage mainPage = null;
    private HttpUpdateAccountPage updateAccountPage= null;
    private HttpEsmePage esmePage = null;
    private HttpAddAccountPage addAccountPage = null;
    private HttpAddEsmeAndAccountPage addEsmeAndAccountPage = null;
    private HttpUpdateEsmePage updateEsmePage = null;
    private HttpSMSListPage smsListPage = null;
    /**
     *Constructor.
     *@param s Socket for the HTTP connection.
     *@param i id of the connection.
     *@param l Logger for the connection
     *@param trafficInfo interface to get SMS-C traffic from.
     */      
    public HttpHandler(Socket s, int i, Logger l, TrafficCentral trafficInfo) {
        super("HttpHandler-" + i);
        version="";
        try {
            Properties p = new Properties();
            p.load(new FileInputStream("../VERSION"));
            version=p.getProperty("VERSION");
        } catch (Exception e) {
            ;
        }
        
        HtmlVersionHeader = HTML_HEADER.replaceAll("VERSION", version);
        socket = s;
        log = l;
        httpSettings = new HttpSettingsHandler(trafficInfo);
        mainPage = new HttpMainPage(trafficInfo);        
        updateAccountPage = new HttpUpdateAccountPage(trafficInfo);
        addAccountPage = new HttpAddAccountPage(trafficInfo);
        esmePage = new HttpEsmePage(trafficInfo);
        addEsmeAndAccountPage = new HttpAddEsmeAndAccountPage(trafficInfo);
        updateEsmePage = new HttpUpdateEsmePage(trafficInfo);
        smsListPage =  new HttpSMSListPage(trafficInfo);
    }
           
    /**
     * Returns the start of an HTML page, with version and esme name.
     *@param esme the name of the ESME of the page
     *@return the beginning of the HTTP page.
     */
    static String getHeader(String esme) {
        return HtmlVersionHeader.replaceAll("__ESME__", esme);
    }

    /**
     * Reads and parses an HTTP request line.
     *@param in - Reader to get the line from.
     *@param headers - is emptied, and then the values for "request-method" and
     * "request-path" are set.
     *@return true if a line could be read, and a request method could be
     * identified. Returns false if in was closed or the request line was
     * obviously wrong.
     *@throws IOException if a complete request could not be read.
     */
    private boolean getRequest(BufferedReader in, Properties headers) throws IOException {
        headers.clear();
        String line = in.readLine();
        if (log.isLoggable(Level.FINE)) {
            log.fine("   >>" + line + "<<");
        }
        if (line == null) { return false; }
        int ix = line.indexOf(" ");
        if (ix < 0) {
            log.severe("Invalid request line: \"" + line + "\"");
            return false;
        }
        headers.put("request-method", line.substring(0, ix).toLowerCase());
        line = line.substring(ix + 1).trim();
        ix = line.indexOf(" ");
        headers.put("request-path", line.substring(0, ix));
        return true;
    }

    /**
     * Reads all headers and puts the header name/value pairs int headers.
     *@param in - Reader to get lines from.
     *@param headers - headers are inserted here.
     *@throws IOException if the headers could not be read.
     */
    private void getHeaders(BufferedReader in, Properties headers) throws IOException {
        String line;
        int ix;
        do {
            line = in.readLine();
            if (log.isLoggable(Level.FINE)) {
                log.fine("   >>" + line + "<<");
            }
            if ("".equals(line)) { return; }

            ix = line.indexOf(":");
            if (ix < 0) {
                log.severe("Strange header line :\"" + line + "\"");
            } else {
                headers.put(line.substring(0, ix).trim().toLowerCase(),
                            line.substring(ix + 1).trim());
            }
        } while (true);
    }

    /**
     * Run method that reads HTTP requests and passes XMP requests on to the XMP
     * handler and forwards <I>get</I> and <I>head</I> requests to special
     * handler functions in this class.
     */
    public void run() {
        setPriority(MAX_PRIORITY - 2); //High priority for swift interactive response
        int length = 0;
        Properties headers = new Properties();
        try {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Handling new HTTP connection.");
            }
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (getRequest(in, headers)) {
                getHeaders(in, headers);
                if ("post".equals(headers.getProperty("request-method"))) {
                    handlePost(in, headers);
                } else if ("get".equals(headers.getProperty("request-method"))) {
                    handleGet(headers.getProperty("request-path"));
                } else if ("head".equals(headers.getProperty("request-method"))) {
                    handleHead(headers.getProperty("request-path"));
                } else {
                    log.severe("Request method " + headers.getProperty("request-method")
                               + " is not allowed.");
                    socket.close();
                    return;
                }
            }
            log.fine("Client disconnected");
        } catch (IOException e) {
            log.severe("Connection problem " + e);
            return;
        } catch (Exception e) {
            System.err.println("Unknown exception: " + stackTrace(e));
            return;
        }
    }

    /**
     * Handles HTTP <I>post</I> requests.
     *@param in - Reader to read body from.
     *@param headers - all request headers.
     *@throws IOException if the request body could not be read.
     */
    private void handlePost(BufferedReader in, Properties headers) throws IOException {
        int length = 0;
        if (headers.getProperty("content-length") != null) {
            try {
                length = Integer.parseInt(headers.getProperty("content-length"));
            } catch (NumberFormatException e) { ; }
        }
        if (length <= 0) {
            log.severe("Bad content-length: \"" + headers.getProperty("content-length") + "\"");
            return;
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("handling POST request, reading "
                     + headers.getProperty("content-length")
                     + "-byte body");
        }
        char[] b = new char[length];
        in.read(b, 0, length);
        if (log.isLoggable(Level.FINE)) {
            log.fine("    >>" + new String(b) + "<<");
        }
        httpRequestHandler(headers.getProperty("request-path"), b);
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
        } 
        else if (req.toLowerCase().startsWith("/get-add-account-page")) {                                
            int start = req.indexOf("esme=");            
            if( start != -1){
                String esmeName = req.substring(start+5);
                respond(HTTP_HEADER, addAccountPage.getAddAccountPage(esmeName));   
            }            
        }
        else if (req.toLowerCase().startsWith("/info")) {
            respond(HTTP_HEADER, "<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=3>\n" + HTML_FOOTER);
        } else {
            respond(HTTP_HEADER, mainPage.getMainPage());
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
        respond(HTTP_HEADER, HtmlVersionHeader
                + "<H3>Configuration management not implemented.</h3>" + HTML_FOOTER);
    }

    /**
     * Parses the parameters from the characters in b and returns key-value pairs
     * in a Properties object.
     */
    public static Properties parseParams(char[] b) {
        Properties result = new Properties();
        String p = new String(b);
        StringTokenizer st = new StringTokenizer(p, "&");
        while (st.hasMoreTokens()) {
            String param = st.nextToken().trim();
            int ix = param.indexOf("=");
            if (ix > 0) {
                result.put(param.substring(0, ix).trim(), param.substring(ix + 1).trim());
            }
        }
        return result;
    }

    /**
     * Handles incoming HTTP requests.
     *@param contentType type of the HTTP request.
     *@param b text of the HTTP request
     */
    private void httpRequestHandler(String requestPath, char[] b) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Handling HTTP request, type " + requestPath);            
        }
        
        Properties p = parseParams(b);
        if( requestPath.toLowerCase().indexOf("/update-esme-page") != -1 ){   
            respond(HTTP_HEADER, esmePage.getEsmePage(p, null));
        }  
        else if( requestPath.toLowerCase().indexOf("/home") != -1 ){    
            respond(HTTP_HEADER, mainPage.getMainPage());
        }
        else if( requestPath.toLowerCase().indexOf("/get-add-esme-and-account-page") != -1 ){
            respond(HTTP_HEADER, addEsmeAndAccountPage.getAddEsmeAndAccountPage());
        }             
        else if( requestPath.toLowerCase().indexOf("/add-esme") != -1 ){            
            httpSettings.addEsme(b);                       
            respond(HTTP_HEADER, mainPage.getMainPage());
        }          
        else if( requestPath.toLowerCase().indexOf("/remove-esme") != -1 ){    
            httpSettings.removeEsme(b);
            respond(HTTP_HEADER, mainPage.getMainPage());
        }                           
        else if( requestPath.toLowerCase().indexOf("/get-add-account-page/add-account") != -1 ){     
            httpSettings.addAccount(b);
            respond(HTTP_HEADER, addAccountPage.getAddAccountPage(b));
        }
        else if( requestPath.toLowerCase().indexOf("/get-add-account-page") != -1 ){   
            respond(HTTP_HEADER, addAccountPage.getAddAccountPage(b));
        }         
        else if( requestPath.toLowerCase().indexOf("/remove-account") != -1 ){   
            respond(HTTP_HEADER, esmePage.getEsmePage(p, httpSettings.removeAccount(b)));
        }   
        else if( requestPath.toLowerCase().indexOf("/config-esme") != -1 ){   
            respond(HTTP_HEADER, updateEsmePage.getpdateEsmePage(b));
        }              
        else if( requestPath.toLowerCase().indexOf("/update-emse") != -1 ){            
            respond(HTTP_HEADER, esmePage.getEsmePage(p, httpSettings.updateEsme(b)));
        }
        else if( requestPath.toLowerCase().indexOf("/config-account") != -1 ){   
            respond(HTTP_HEADER, updateAccountPage.getUpdateAccountPage(b));
        }    
        else if( requestPath.toLowerCase().indexOf("/update-account-bindings") != -1 ){                    
            respond(HTTP_HEADER, esmePage.getEsmePage(p, httpSettings.updateAccountBindings(b)));
        }         
        else if( requestPath.toLowerCase().indexOf("/update-account-uid-pwd") != -1 ){                        
            respond(HTTP_HEADER, esmePage.getEsmePage(p, httpSettings.updateAccountUidAndPwd(b)));
        }
        else if( requestPath.toLowerCase().indexOf("/reset-sms-cache") != -1 ){                                               
            respond(HTTP_HEADER, esmePage.getEsmePage(p, httpSettings.resetSMSList(b)));
        } 
        else if( requestPath.toLowerCase().indexOf("/reset-account-sms-list") != -1 ){                                               
            respond(HTTP_HEADER, esmePage.getEsmePage(p, httpSettings.resetAccount(b)) + HTML_FOOTER);
        }   
        else if( requestPath.toLowerCase().indexOf("/view-sms-list") != -1 ){                                               
            respond(HTTP_HEADER, smsListPage.getSMSListPage(p));
        }  
        else {
            log.severe("Unknown request-path: \"" + requestPath + "\", discarding request");
            respond(HTTP_HEADER,mainPage.getMainPage());
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
    public synchronized void httpResponseHandler(String response) {
        try {
            out.write(response);
            out.flush();
        } catch (Exception e) {
            log.severe("Unknown exception: " + stackTrace(e));
        }
    }    
       
    public static StringBuffer stackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.getBuffer();
    }
}
