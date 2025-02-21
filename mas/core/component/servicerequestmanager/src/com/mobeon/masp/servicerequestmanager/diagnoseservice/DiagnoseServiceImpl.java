/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.diagnoseservice;

import com.mobeon.masp.operateandmaintainmanager.DiagnoseService;
import com.mobeon.masp.operateandmaintainmanager.Status;
import com.mobeon.masp.operateandmaintainmanager.ServiceInstance;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.masp.servicerequestmanager.xmp.XMPLogger;
import com.mobeon.common.xmp.XmpErrorHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.MissingResourceException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.*;

import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

/**
 * Implementation of the DiagnoseService interface for
 * {@link com.mobeon.masp.servicerequestmanager.ServiceRequestManager}
 * Test the status of a provided service by sending a service request.
 *
 * @author mmawi
 */
public class DiagnoseServiceImpl implements DiagnoseService {
    private IConfiguration configuration;

    private AtomicInteger transactionIdCounter = new AtomicInteger(0);

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public DiagnoseServiceImpl() {
    }

    /**
     * Inject configuration.
     *
     * @param configuration The configuration
     */
    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    public void init() {
        verifyResources();
        DiagnoseServiceConfiguration.getInstance().setInitialConfiguration(this.configuration);
        DiagnoseServiceConfiguration.getInstance().updateConfiguration();
    }

    /**
     * Check that the configuration is injected.
     *
     * @throws MissingResourceException If configuration is not found.
     */
    private void verifyResources() throws MissingResourceException {
        if (configuration == null) {
            throw new MissingResourceException("Configuration is not injected.",
                    "IConfiguration", "configuration");
        }
    }

    /**
     * Send a service request to a service to find out the status.
     *
     * @param serviceInstance   The service that should be tested
     * @return The status of the service
     * @throws IllegalArgumentException If some necessary attribute is missing
     * in serviceInstance
     */
    public Status serviceRequest(ServiceInstance serviceInstance) throws IllegalArgumentException {
        if (serviceInstance == null) {
            throw new IllegalArgumentException("serviceInstance cannot be null.");
        } else if (serviceInstance.getHostName() == null) {
            throw new IllegalArgumentException("serviceInstance does not contain host name.");
        } else if (serviceInstance.getPort() <= 0) {
            throw new IllegalArgumentException("serviceInstance does not contain port number.");
        }

        Status status = Status.UNKNOWN;

        Socket socket;
        OutputStreamWriter outputStreamWriter;
        try {
            socket = new Socket(serviceInstance.getHostName(), serviceInstance.getPort());
            OutputStream outputStream = socket.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);
        } catch (IOException e) {
            status = Status.DOWN;
            if (log.isInfoEnabled())
                log.info("Failed to create socket to " + serviceInstance.getHostName()
                        + ":" + serviceInstance.getPort() + ". Service status: down");
            return status;
        }

        ResponseListener responseListener = new ResponseListener(
                socket, DiagnoseServiceConfiguration.getInstance().getRequestTimeout());

        ExecutorService executorService =
                ExecutorServiceManager.getInstance().getExecutorService(getClass());
        executorService.execute(responseListener);

        transactionIdCounter.compareAndSet(10000, 0);
        int transactionId = transactionIdCounter.getAndIncrement();

        String message = createMessage(DiagnoseServiceConfiguration.getInstance().getClientId(),
                serviceInstance.getHostName(), transactionId);

        try {
            if (log.isInfoEnabled()) {
                log.info("Sending request: " + message);
            }
            outputStreamWriter.write(message);
            outputStreamWriter.flush();
        } catch (IOException e) {
            status = Status.DOWN;
            if (log.isInfoEnabled())
                log.info("Failed send request to " + serviceInstance.getHostName()
                        + ":" + serviceInstance.getPort() + ". Service status: down");
            return status;
        }

        status = responseListener.getStatus(
                DiagnoseServiceConfiguration.getInstance().getRequestTimeout());        

        try {
            socket.close();
        } catch (IOException e) {
            //ignore
        }

        return status;
    }

    private String createMessage(String clientId, String host, int transactionId) {
        String post = "POST /DiagnoseService HTTP/1.1\r\n";
        String header = "Host: " + host + "\r\n";
        header += "Connection: close\r\n";
        header += "Content-Type: text/xml; charset=utf-8\r\n";

        String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        body += "<xmp-message xmlns=\"http://www.abcxyz.se/xmp-1.0\">";
        body += "<xmp-service-request service-id=\"DiagnoseService\" " +
                "transaction-id=\"" + transactionId + "\" " +
                "client-id=\"" + clientId + "\">";
        body += "<validity>30</validity>";
        body += "<message-report>true</message-report>";
        body += "</xmp-service-request>";
        body += "</xmp-message>";

        header += "Content-Length: " + body.length() + "\r\n\r\n";

        return post + header + body;
    }

    private class ResponseListener implements Runnable {
        private Socket socket;
        private int requestTimeout;
        private AtomicBoolean isRunning = new AtomicBoolean(true);
        private AtomicReference<Status> status = new AtomicReference<Status>();

        public ResponseListener(Socket socket, int requestTimeout) {
            this.socket = socket;
            this.requestTimeout = requestTimeout;
        }

        public synchronized Status getStatus(int timeout) {
            if (status.compareAndSet(null, Status.DOWN)) {
                try {
                    wait(timeout);
                    isRunning.set(false);
                    socket.close();
                    return status.get();
                } catch (InterruptedException e) {
                    return Status.DOWN;
                } catch (IOException e) {
                    //ignore
                }
            }

            return status.get();
        }

        public void run() {

            //while (socket.isConnected()) {
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
                        long endTime = currentTime + requestTimeout;
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

                            NodeList nodes = doc.getElementsByTagName("xmp-service-response");
                            if (nodes.getLength() == 1) {

                                Element xsr = (Element) nodes.item(0);

                                String id = "";
                                String code = "";
                                String statusText = "";

                                if(xsr.getAttributes().getNamedItem("transaction-id") != null){
                                    id = xsr.getAttributes().getNamedItem("transaction-id").
                                            getNodeValue();
                                }

                                if(xsr.getElementsByTagName("status-code").getLength() != 0){
                                    code = xsr.getElementsByTagName("status-code").item(0).
                                            getFirstChild().getNodeValue();
                                }

                                if(xsr.getElementsByTagName("status-text").getLength() != 0){
                                    statusText = xsr.getElementsByTagName("status-text").item(0).
                                            getFirstChild().getNodeValue();
                                }

                                if (code.equals("200")) {
                                    //OK
                                    status.set(Status.UP);
                                } else {
                                    status.set(Status.DOWN);
                                }

                                if (log.isDebugEnabled())
                                    log.debug("Received response for transactionId "
                                            + id + " " + code + " \"" + statusText +
                                            "\". Status is " + status.get());
                                synchronized(this) {
                                    notify();
                                }
                            }

                        } catch (Exception e) {
                            //Fail
                            log.error("Parsing response failed: ", e);
                            status.set(Status.DOWN);
                        }
                    }
                } catch (IOException e) {

                    status.set(Status.DOWN);

                    if (!isRunning.get()) {
                        InetSocketAddress addr =
                                (InetSocketAddress)socket.getRemoteSocketAddress();
                        log.warn("The service request to " +
                                addr.getHostName() + ":" + addr.getPort() +
                                " has timed out.");                        
                    } else {
                        log.error("IOException: ", e);
                    }

                    synchronized(this) {
                        notify();
                    }

                }
            //}
        }
    }
}
