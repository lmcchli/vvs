package com.mobeon.smsc.management.http;

import java.net.Socket;
import java.net.ServerSocket;

import java.io.IOException;

import java.util.logging.*;

import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.smsc.config.Config;
import com.mobeon.smsc.interfaces.TrafficCentral;
import com.mobeon.smsc.util.SMSCFilter;
import com.mobeon.smsc.util.SMSCFormatter;

/**
 * Listens for HTTP connections and spawns HTTP handlers to handle each new connection.
 */
public class HttpServer extends Thread {

    /** Socket to listen to */
    private ServerSocket serverSocket = null;
    /** General log target */
    private Logger log = null;
    /** Interface to get SMS-C traffic from.*/
    private TrafficCentral trafficInfo = null;

    /**
     * Constructor.
     *@param trafficInfo is the interface to get SMS-C traffic from. 
     */
    public HttpServer(TrafficCentral trafficInfo) {        
        this.setName("HttpServer");
        this.trafficInfo = trafficInfo;
        setLogger();   
    }

     /* Initates the logger for this HttpServer.**/
    private void setLogger(){  
        String[] errors = Config.getErrors();
        String error = null;
        Handler h;         
                                
        Logger l = Logger.getLogger("HttpServer");                
        try {
            h = new FileHandler("../logs/HttpServer.%g.log",
                                Config.getLogSize(),
                                Config.getLogCount(),
                                true);
        } catch (IOException e) {
            h = new ConsoleHandler();
            error = "Failed to open log file " + e + "\n";
        }               
        h.setFormatter(new SMSCFormatter());
        h.setLevel(Level.ALL);
        h.setFilter(new SMSCFilter("HttpServer"));
        
        l.addHandler(h);
        l.setLevel(Config.getLogLevel());        
        
        log = Logger.getLogger("HttpServer");
        
        for (int i = 0; i < errors.length; i++) {
            log.severe(errors[i]);
        }
        if (error != null) {
            log.severe(error);
        }
    }
    
    /**
     * Run method that listens for connections and spawns HTTP handlers.
     */
    public void run() {
        setPriority(MAX_PRIORITY - 2); //High priority for swift interactive response
        int i = 0;
        try {
            while (true) {
                try {
                    serverSocket = new ServerSocket(Config.getHttpServerPort());
                    while (true) {
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Waiting for connection to " + Config.getHttpServerPort());
                        }
                        Socket s = serverSocket.accept();
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Connection to port " + Config.getHttpServerPort());
                        }
                        new HttpHandler(s, i++, log, trafficInfo).start();
                    }
                } catch (IOException e) {
                    log.severe("Could not establish a connection to port "
                               + Config.getHttpServerPort() + e);
                } catch (Exception e) {
                    log.severe("Unknown exception: " + e);
                } catch (OutOfMemoryError e) {
                    System.err.println("Out of memory: " + NtfUtil.stackTrace(e));
                    System.exit(-1);
                }
                try {
                    serverSocket.close();
                    sleep(5000);
                } catch (InterruptedException e) {
                    System.err.println("Unexpected exception: " + NtfUtil.stackTrace(e));
                } catch (IOException e) {
                    System.err.println("Unexpected exception: " + NtfUtil.stackTrace(e));
                }
            }
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory: " + NtfUtil.stackTrace(e));
            System.exit(-1);
        }
    }
}
