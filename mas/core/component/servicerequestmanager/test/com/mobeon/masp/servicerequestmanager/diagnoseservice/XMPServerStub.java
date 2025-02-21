package com.mobeon.masp.servicerequestmanager.diagnoseservice;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.xmp.XMPLogger;

import com.mobeon.common.xmp.XmpErrorHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

/**
 * Simulates an XMP server.
 *
 * @author mmawi
 */
public class XMPServerStub extends Thread {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private int port;
    private int responseCode = 200;

    private boolean invalidResponse = false;
    private boolean invalidXml = false;
    private boolean timeout = false;
    private AtomicBoolean isStopped = new AtomicBoolean(false);

    private AtomicReference<ServerSocket> ss = new AtomicReference<ServerSocket>();
    private AtomicBoolean isReady = new AtomicBoolean(false);

    public XMPServerStub(int port) {
        super("XMPServerStub");
        this.port = port;
    }

    public void run() {

        try {
            ss.set(new ServerSocket(port));
            isReady.set(true);

            while (! isStopped.get()) {
                log.debug("Start accepting");
                Socket socket = ss.get().accept();
                log.debug("Connection accepted on port " + port);
                handleRequest(socket);
                log.debug("Done handling request");

            }

        } catch (IOException e) {
            // why did we get an exception? if stopped, it was not an error
            if(isStopped.get()){
                log.debug("Server was stopped. IOExeption: ", e);
            } else {
                log.error("IOExeption: ", e);
            }
        } catch (Exception e) {
            log.error("Other exception: ", e);
        } finally {
            isReady.set(false);
        }
    }

    public boolean isReady(){
        return isReady.get();
    }

    public void stopMe() throws IOException {
        isStopped.set(true);
        ss.get().close();
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }

    public void setInvalidResponse(boolean invalidResponse) {
        this.invalidResponse = invalidResponse;
    }

    public void setInvalidXml(boolean invalidXml) {
        this.invalidXml = invalidXml;
    }

    private void handleRequest(Socket socket) {
        String id = "0";

        try {
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String temp;
            int length = 0;
            while ((temp = bufferedReader.readLine()) != null) {

                if (temp.toLowerCase().indexOf("content-length") != -1) {
                    length = Integer.parseInt(
                            temp.substring(temp.indexOf(":") + 1).trim());
                    break;
                }
            }

            if (length > 0) {
                char[] buffer = new char[length + 2];
                int bytesRead = 0;
                int position = 0;
                long currentTime = System.currentTimeMillis();
                long endTime = currentTime + 30000;
                while ((bytesRead < buffer.length) && (currentTime <= endTime)) {
                    int currentBytes = bufferedReader.read(
                            buffer, position, buffer.length - bytesRead);

                    position += currentBytes;
                    bytesRead += currentBytes;
                    currentTime = System.currentTimeMillis();
                }

                try {
                    DocumentBuilderFactory factory =
                            DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    // set a custom error handler to prevent error messages on System.out
                    builder.setErrorHandler(new XmpErrorHandler(new XMPLogger()));

                    StringReader sr = new StringReader(
                            new String(buffer, 2, buffer.length - 2));
                    InputSource iSrc = new InputSource(sr);
                    Document doc = builder.parse(iSrc);

                    NodeList nodes = doc.getElementsByTagName("xmp-service-request");
                    if (nodes.getLength() == 1) {

                        Element xsr = (Element) nodes.item(0);

                        if(xsr.getAttributes().getNamedItem("transaction-id") != null){
                            id = xsr.getAttributes().getNamedItem("transaction-id").
                                    getNodeValue();
                        }
                    }

                } catch (Exception e) {
                    //Fail
                    log.error("Parsing request failed: ", e);
                }
            }
        } catch (IOException e) {
            log.error("IOException: ", e);
        }

        sendResponse(socket, Integer.valueOf(id));
    }

    private void sendResponse(Socket socket, int transactionId) {
        if (timeout) {
            log.info("Not sending any response.");
            return;
        }

        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStream = socket.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);

            String message = createMessage(transactionId);
            log.info("Sending response: " + message);
            outputStreamWriter.write(message);
            outputStreamWriter.flush();

            socket.close();
        } catch (IOException e) {
            log.error("Failed to send response. ", e);
        }
    }

    private String createMessage(int transactionId) {
        String header = "HTTP/1.1 200 OK\r\n";
        header += "Content-Type: text/xml; charset=utf-8\r\n";

        String body = "<?xml version=\"1.0\"?>";
        if (invalidResponse) {
            body += "<invalid-response>value</invalid-response>";
        } else if (invalidXml) {
            body += "<invalidXml></noEndTag>";
        } else {
            body += "<xmp-message xmlns=\"http://www.mobeon.com/xmp-1.0\">";
            body += "<xmp-service-response transaction-id=\"" + transactionId + "\">";
            body += "<status-code>" + responseCode + "</status-code>";
            body += "<status-text>DiagnoseServiceResult</status-text>";
            body += "</xmp-service-response></xmp-message>";
        }

        header += "Content-Length: " + body.length() + "\r\n\r\n";

        return header + body;
    }
}
