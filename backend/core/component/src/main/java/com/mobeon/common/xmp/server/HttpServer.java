/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp.server;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.InetAddress;
import java.io.IOException;
import com.mobeon.common.util.M3Utils;
import com.mobeon.common.util.logging.ILogger;

/**
 * Listens for HTTP connections and spawns HTTP handlers to handle each new connection.
 */
public class HttpServer extends Thread implements ILogger {
    
    private static int readTimeout = 2000; // millisecs
    
    /** Socket to listen to */
    private ServerSocket serverSocket = null;
    
    /** General log target */
    private ILogger log;
    
    private int serverPort = 8080;
    private String host;
    
    /** Counts the total number of connections since the program was started */
    private Counter nConns = ComponentInfo.getCounter("HttpConnectionCount");
    
    private boolean keepRunning = true;
    
    /**
     * Constructor.
     */
    public HttpServer() {
        super("HttpServer");
        
    }
    
    /**
     * sets the port to listen ofr requests on. default port is 8080.
     *@param port the port to listen to
     */
    public void setPort(int port) {
        this.serverPort = port;
    }

    /**
     * sets the host to listen for requests on. default host is localhost.
     *@param host the host to listen to
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * Sets a logger to user.
     *@param log a ILogger logger.
     */
    public void setLogger(ILogger log) {
        this.log = log;
    }

    public ILogger getLog() {
        return log;
    }

    /**
     *Stops the server. 
     *All threads will stop whan possible
     */
    public void stopServer() {
        keepRunning = false;
        disconnect();
    }
    
    /**
     * Run method that listens for connections and spawns HTTP handlers.
     */
    public void run() {
        int i = 0;
        try {
            while (keepRunning) {
                try {
                    if(host == null)
                        serverSocket = new ServerSocket(serverPort);
                    else {
                        serverSocket = new ServerSocket(serverPort, 0,  InetAddress.getByName(host));
                        if(log.isDebugEnabled()) log.debug("Created server socket on "+host+":"+serverPort);
                    }
                   
                    while (true) {
                        debug("Waiting for connection to " + serverPort);
                        
                        
                        Socket s = serverSocket.accept();
                        debug("Connection to port " + serverPort);
                        
                        new HttpHandler(s, this).start();
                        
                    }
                } catch (IOException e) {
                    error("Could not establish a connection to host:port "
                          + host+":"+serverPort + e);
                    try { sleep(10000); } catch(Exception e2) {}
                } catch (Exception e) {
                    error("Unknown exception: " + M3Utils.stackTrace(e).toString());
                } catch (OutOfMemoryError e) {
                    System.exit(-1);
                }
                
                disconnect();
            }
            disconnect();
        } catch (OutOfMemoryError e) {
            System.exit(-1);
        }
    }
    
    /**
     * Method that shutdown connections.
     */
    private void disconnect() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            ; //ignore
        }
        serverSocket = null;
    }
    
    public void debug(Object message) {
        if( log != null ) {
            log.debug(message);
        }
    }
    
    public void debug(Object message, Throwable t) {
        if( log != null ) {
            log.debug(message,t);
        }
    }
    
    public void info(Object message) {
        if( log != null ) {
            log.info(message);
        }
    }
    
    public void info(Object message, Throwable t) {
        if( log != null ) {
            log.info(message,t);
        }
    }
    
    public void warn(Object message) {
        if( log != null ) {
            log.warn(message);
        }
    }
    
    public void warn(Object message, Throwable t) {
        if( log != null ) {
            log.warn(message,t);
        }
    }
    
    public void error(Object message) {
        if( log != null ) {
            log.error(message);
        }
    }
    
    public void error(Object message, Throwable t) {
        if( log != null ) {
            log.error(message,t);
        }
    }
    
    public void fatal(Object message) {
        if( log != null ) {
            log.fatal(message);
        }
    }
    
    public void fatal(Object message, Throwable t) {
        if( log != null ) {
            log.fatal(message,t);
        }
    }
    
    public void registerSessionInfo(String name, Object sessionInfo) {
    }
    
    public boolean isDebugEnabled() {
        if( log != null && log.isDebugEnabled() ) {
            return true;
        }
        return false;
    }
  
    boolean isKeepRunning() {
        return keepRunning;
    }
}
