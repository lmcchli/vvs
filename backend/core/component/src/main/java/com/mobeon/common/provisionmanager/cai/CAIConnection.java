/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.provisionmanager.cai;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Contains a connection (socket) to a CAI server. Has methods to send CAI commands and read the response from the
 * server.
 *
 * @author ermmaha
 */
public class CAIConnection {
    private static ILogger log = ILoggerFactory.getILogger(CAIConnection.class);

    /**
     * Socket timeout value in milliseconds
     */
    private int socketTimeout = 10 * 1000; //10 seconds default
    /**
     * the socket
     */
    private Socket socket;
    /**
     * true if connected, false if not
     */
    private boolean connected;
    /**
     * Last time this connection was used
     */
    private long lastTouchedTime = 0;
    private String host;
    private int port;
    private String uid;
    private String pwd;

    public CAIConnection(String uid, String pwd) {
        this.uid = uid;
        this.pwd = pwd;
    }

    /**
     * Sends a CAI command to a CAI server
     *
     * @param caiCommand
     * @throws CAIException
     */
    public CAIResponse sendCommand(CAICommand caiCommand) throws IOException, CAIException {
        if (log.isDebugEnabled()) log.debug("sendCommand(caiCommand=" + caiCommand + ")");
        try {
            CAIResponse response = send(caiCommand);
            // Empty response only expected for LOGOUT
            if (response == null) {
                throw new CAIException("Server sent empty response.");
            }
            if (log.isDebugEnabled()) log.debug("sendCommand(CAICommand) returns " + response);
            return response;
        } catch (SocketException e) {
            if (log.isDebugEnabled()) log.debug("send(CAICommand) throws " + e + ", will try reconnect.");
            // Try reconnect
            try {
                close();
                connect(host, port);
                CAIResponse response = send(caiCommand);
                if (log.isDebugEnabled()) log.debug("sendCommand(CAICommand) returns " + response);
                return response;
            } catch (IOException e1) {
                throw e;
            }
        }
    }

    private CAIResponse send(CAICommand caiCommand) throws IOException, CAIException {
        touch();
        write(caiCommand);
        String response = read();
        if (response != null) {
            CAIResponse caiResponse = new CAIResponse(response);
            if (caiResponse.getCode() != 0) {
                throw new CAIException("Server sent message: " + caiResponse.getMessage(), caiResponse.getCode());
            }
            return caiResponse;
        }
        return null;
    }

    /**
     * Writes the CAI command on the socket.
     *
     * @param caiCommand
     */
    private void write(CAICommand caiCommand) throws IOException {
        OutputStream out = socket.getOutputStream();
        String toSend = caiCommand.toCommandString() + "\n";
        out.write(toSend.getBytes());
        out.flush();
        if (log.isDebugEnabled()) log.debug("> " + toSend);
    }

    /**
     * Reads the CAI response from the server. The data is returned as a string
     *
     * @return String response data, null if nothing was read
     */
    private String read() throws IOException {
        InputStream in = socket.getInputStream();

        byte[] buf = new byte[256];

        StringBuffer response = new StringBuffer();
        int bytesRead;
        while ((bytesRead = in.read(buf)) != -1) {
            String tmp = new String(buf, 0, bytesRead);
            response.append(tmp);
            if (log.isDebugEnabled()) log.debug("< " + tmp);
            if (tmp.indexOf("CAI>") > -1 || tmp.indexOf("command:") > -1) break;
        }
        if(bytesRead == -1)
            throw new SocketException("End of stream reached on CAI Connection !");
        return response.length() == 0 ? null : response.toString();
    }

    /**
     * Creates and connects a socket to the server specified in parameters
     *
     * @param host
     * @param port
     * @throws IOException or CAIException If failing to connect.
     */
    public void connect(String host, int port) throws IOException, CAIException {
        if (log.isDebugEnabled()) log.debug("connect(host=" + host + ", port=" + port + ")");
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        socket.setSoTimeout(socketTimeout);
        socket.setSoLinger(true, 5);
        // Default receivebuffer on the socket is 8192 bytes
        read();
        // Uses send(CAICommand) which will not try reconnect
        send(new LoginCommand(uid, pwd));
        connected = true;
        if (log.isDebugEnabled()) log.debug("connect(String, int) returns void");
    }

    /**
     * Disconnects the connection by closing the socket.
     *
     * @logs.warning "Could not logout CAI user &lt;user&gt; from host &lt;host:port&gt;" - The CAI logout command
     * failed, the socket will be closed anyway. The exception's error message is added to the log message.
     */
    public void disconnect() {
        if (log.isDebugEnabled()) log.debug("disconnect()");
        if (socket != null) {
            try {
                send(new LogoutCommand());
            } catch (Exception e) {
                // Not much to do if logout fails at disconnect
                StringBuilder errmsg = new StringBuilder("Could not logout CAI user <");
                errmsg.append(uid).append("> from host <").append(host).append(":").append(port).append("> ");
                errmsg.append(e);
                log.warn(errmsg);
            }
            close();
        }
        if (log.isDebugEnabled()) log.debug("disconnect() returns void");
    }

    /**
     * Closes the socket.
     *
     * @logs.warning "Could not close socket to host &lt;host:port&gt;" - The socket close method failed for some reason,
     * this is just logged as this warning and ignored in the execution. The exception's error message is added to the
     * log message, indicating the actual error.
     */
    private void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // Not much to do if close fails at disconnect
                StringBuilder errmsg = new StringBuilder("Could not close socket to host <");
                errmsg.append(host).append(":").append(port).append("> ").append(e.getMessage());
                log.warn(errmsg);
            }
            socket = null;
            connected = false;
        }
    }

    /**
     * @return true if connected, false if not
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Set socket timeout value in milliseconds
     *
     * @param socketTimeout in milliseconds
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    long getSinceTouched() {
        return System.currentTimeMillis() - lastTouchedTime;
    }

    private void touch() {
        lastTouchedTime = System.currentTimeMillis();
    }
}

