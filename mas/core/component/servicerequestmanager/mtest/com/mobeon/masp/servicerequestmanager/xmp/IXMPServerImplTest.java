/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.masp.mediaobject.MultiThreadedTeztCase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.ServiceHandlerStub;
import com.mobeon.common.xmp.server.ServiceHandler;
import com.mobeon.common.xmp.server.XmpHandler;
import org.apache.log4j.xml.DOMConfigurator;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.net.Socket;
import java.io.*;

/**
 * @author mmawi
 */
public class IXMPServerImplTest extends MultiThreadedTeztCase {
    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER = ILoggerFactory.getILogger(IXMPServerImplTest.class);

    private static int SERVERPORT = 8080;
    private static String HOST = "localhost";

    /**
     * The server that is being tested.
     */
    private IXMPServer xmpServer;

    private ServiceHandler serviceHandler;

    private Socket s;
    private static boolean done;

    public IXMPServerImplTest() {
        xmpServer = IXMPServerImpl.getInstance();
        xmpServer.start(HOST, SERVERPORT);
        serviceHandler = new ServiceHandlerStub();
        XmpHandler.setServiceHandler("TeztService", serviceHandler);
    }

    /**
     * Create the tested server
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        done = false;
        assertNotNull("Failed to initialize XMP Server.", xmpServer);
        s = new Socket(HOST, SERVERPORT);
    }

    protected void tearDown() throws Exception {
        s.close();
        super.tearDown();
    }

    public void testMultipleServiceRequests() throws Exception {
        String clientId = "MultiClient";
        int n = 3;
        final OutputStream outputStream;
        final OutputStreamWriter outputStreamWriter;
//        s = new Socket(HOST, SERVERPORT);
        outputStream = s.getOutputStream();
        outputStreamWriter = new OutputStreamWriter(outputStream);
        ResponseListener responseListener = new ResponseListener(s, n);
        responseListener.start();

        for (int i = 1; i <= n; ++i) {
            String message = createMessage(HOST, "TeztService", clientId, i, 10, true);
            LOGGER.debug("Sending request: " + message);
            outputStreamWriter.write(message);
            outputStreamWriter.flush();
        }

        responseListener.join();
        assertTrue("Invalid response.", responseListener.isSuccessful());
//        s.close();
    }

    public void testServiceRequest() throws Exception {
        String clientId = "SingleClient";
        OutputStream outputStream;
        OutputStreamWriter outputStreamWriter;
//        s = new Socket(HOST, SERVERPORT);
        outputStream = s.getOutputStream();
        outputStreamWriter = new OutputStreamWriter(outputStream);
        ResponseListener responseListener = new ResponseListener(s, 1);
        responseListener.start();

        String message = createMessage(HOST, "TeztService", clientId, 1, 2, true);
        LOGGER.debug("Sending request: " + message);
        outputStreamWriter.write(message);
        outputStreamWriter.flush();

        responseListener.join();
        assertTrue("Invalid response.", responseListener.isSuccessful());
//        s.close();
    }

    public void testEmptyRequest() throws Exception {
        String clientId = "EmptyClient";
        OutputStream outputStream;
        OutputStreamWriter outputStreamWriter;
//        s = new Socket(HOST, SERVERPORT);
        outputStream = s.getOutputStream();
        outputStreamWriter = new OutputStreamWriter(outputStream);

        //Send a request.
        String message = createMessage(HOST, "TeztService", clientId, 1, 10, true);
        LOGGER.debug("Sending request: " + message);
        outputStreamWriter.write(message);
        outputStreamWriter.flush();
        //close socket
        LOGGER.debug("Closing socket.");
        s.close();

        synchronized(this) {
            wait(8000);
        }

        s = new Socket(HOST, SERVERPORT);
        outputStream = s.getOutputStream();
        outputStreamWriter = new OutputStreamWriter(outputStream);

        ResponseListener responseListener = new ResponseListener(s, 1);
        responseListener.start();

        //Send Empty Request
        message = createMessage(HOST, "Empty", clientId, 0, 10, true);
        LOGGER.debug("Sending request: " + message);
        outputStreamWriter.write(message);
        outputStreamWriter.flush();

        responseListener.join();
        assertTrue("Invalid response.", responseListener.isSuccessful());
//        s.close();
    }

    public void testMultipleClients() throws Exception {
        XMPClient[] clients = new XMPClient[5];

        for (int i=0; i<clients.length; ++i) {
            clients[i] = new XMPClient("client" + i);
        }

        runTestCaseRunnables(clients);
        joinTestCaseRunnables(clients);
    }


    private static void setDone() {
        done = true;
    }

    private String createMessage(String host,
                                 String serviceId,
                                 String clientId,
                                 int transactionId,
                                 int validity,
                                 boolean keepAlive) {
        String post = "POST /" + serviceId + " HTTP/1.1\r\n";
        String header = "Host: " + host + "\r\n";
        if (!keepAlive) {
            header += "Connection: close\r\n";
        }
        header += "Content-Type: text/xml; charset=utf-8\r\n";

        String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        body += "<xmp-message xmlns=\"http://www.abcxyz.se/xmp-1.0\">";
        body += "<xmp-service-request service-id=\"" + serviceId + "\" transaction-id=\"" +
                transactionId + "\" client-id=\"" + clientId + "\">";
        body += "<validity>" + validity + "</validity>";
        body += "<message-report>false</message-report>";
        body += "<parameter name=\"mailbox-id\">9903</parameter>";
        body += "<parameter name=\"number\">0730205381</parameter>";
        body += "</xmp-service-request>";
        body += "</xmp-message>";

        header += "Content-Length: " + body.length() + "\r\n\r\n";

        return post + header + body;
    }

    private class XMPClient extends TestCaseRunnable {
        private ResponseListener responseListener;
        private String clientId;
        private Socket socket = null;
        private OutputStream outputStream;
        private OutputStreamWriter outputStreamWriter;

        public XMPClient(String clientId) {
            this.clientId = clientId;

        }

        public void runTestCase() throws Throwable {
            socket = new Socket(HOST, SERVERPORT);
            outputStream = socket.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);
            responseListener = new ResponseListener(socket, 5);
            responseListener.start();
            String message;

            for (int i=1; i<6; ++i) {
                message = createMessage(HOST, "TeztService", clientId, i, 20, true);
                LOGGER.debug("Sending request: " + message);
                outputStreamWriter.write(message);
                outputStreamWriter.flush();
            }

            responseListener.join();
            assertTrue("Response for client " + clientId + " failed.",
                    responseListener.isSuccessful());
            socket.close();
        }
    }

    private class ResponseListener extends Thread {
        private Socket socket;
        private boolean done = false;
        private BufferedReader bufferedReader;
        private int nrOfResponses;
        private boolean failed = false;

        public ResponseListener(Socket socket, int nrOfResponses) {
            this.socket = socket;
            this.nrOfResponses = nrOfResponses;
        }

        public void setDone() {
            LOGGER.debug("setDone");
            done = true;
        }

        public boolean isSuccessful() {
            return !failed;
        }

        public void run() {
            int k = 0;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String temp;
                LOGGER.debug("Starting response listener.");
                while ((temp = bufferedReader.readLine()) != null) {
                    //LOGGER.debug("Response received: " + temp);
                    if (temp.toLowerCase().indexOf("content-length") != -1) {
                        int length = Integer.parseInt(
                                temp.substring(temp.indexOf(":") + 1).trim());

                        char[] buffer = new char[length + 2];
                        int bytesRead = 0;
                        int position = 0;
                        long startTime = System.currentTimeMillis() + 2000;
                        long endTime = System.currentTimeMillis();
                        while( bytesRead < buffer.length && endTime <= startTime) {
                            int currentBytes =
                                    bufferedReader.read(buffer, position, buffer.length - bytesRead);
                            position += currentBytes;
                            bytesRead += currentBytes;
                            endTime = System.currentTimeMillis();
                        }

                        //LOGGER.debug("content: " + String.valueOf(buffer));

                        try {
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder builder = factory.newDocumentBuilder();
                            StringReader sr = new StringReader(new String(buffer,2,buffer.length-2));
                            InputSource iSrc = new InputSource(sr);
                            Document doc = builder.parse(iSrc);

                            for(int i = 0; i < doc.getElementsByTagName("xmp-service-response").getLength(); i++){
                                String id = "";
                                String code = "";
                                String statusText = "";
                                Element xsr = (Element)doc.getElementsByTagName("xmp-service-response").item(i);
                                if(xsr.getAttributes().getNamedItem("transaction-id") != null){
                                    id = xsr.getAttributes().getNamedItem("transaction-id").getNodeValue();
                                }

                                if(xsr.getElementsByTagName("status-code").getLength() != 0){
                                    code = xsr.getElementsByTagName("status-code").item(0).getFirstChild().getNodeValue();
                                }

                                if(xsr.getElementsByTagName("status-text").getLength() != 0){
                                    statusText = xsr.getElementsByTagName("status-text").item(0).getFirstChild().getNodeValue();
                                }

                                if (!code.equals("200")) {
                                    failed = true;
                                }
                            }
                        } catch (Exception e) {
                            failed = true;
                        }

                        ++k;
                    }
                    if (k >= nrOfResponses) {
                        break;
                    }
                }
                //LOGGER.debug("End of stream.");
            } catch (IOException e) {
                failed = true;
                e.printStackTrace();
            }
        }
    }
}
