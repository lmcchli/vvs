/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.logging.*;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.smsc.config.Config;
import com.mobeon.smsc.smpp.SMPPCom;
import com.mobeon.smsc.containers.ESMEHandler;

/**
 * This is the main SMS-C server thread wich listens for SMPP connections on
 * a configurable port number, default 5016. When a client connects to the server 
 * a SMPPCom thread is started. This thread handles reads and sends all SMPP traffic
 * related information back to the client.
 */
public class SMSCServer extends Thread {

    /** Socket to listen to */
    private ServerSocket serverSocket = null;
    private boolean listening = true;
    private int timeToAccept = 0; //How long to start accepting connections

    /** General log target. All logs events are written to the main logfile, smsc.log*/
    private Logger log = null;

    /**
     * Constructor.
     */
    public SMSCServer() {
        this.setName("SMSCServer");
        log = Logger.getLogger("SMSC");
    }

    /**
     * Simulate a process restart by closing all connections and not accepting
     * new connections for a specified time.
     *@param duration how long we shall refuse connections, in seconds
     */
    public void simulateRestart(int duration) {
	timeToAccept = duration/5; // sleep time in run is 5 s
	SMPPCom.killAllConnections();
    }
    
    /**
     * Run method that listens for connections and spawns SMPP handlers.
     */
    public void run() {
        int i = 0;
        try {
            while (listening) {
                try {
                    serverSocket = new ServerSocket(Config.getSMSCServerPort());
		    serverSocket.setSoTimeout(5000);
		    
		    log.fine("Waiting for a new connection on port " + Config.getSMSCServerPort());
		    while (timeToAccept <= 0) {
			try {
			    Socket s = serverSocket.accept();
			    if (log.isLoggable(Level.FINE)) {
				log.fine("New client connected on port " + Config.getSMSCServerPort());
			    }
			    new SMPPCom(s, i++, ESMEHandler.get());
			    log.fine("Waiting for a new connection on port " + Config.getSMSCServerPort());
			} catch (java.net.SocketTimeoutException e) {
			    ;
			}
		    }
		    try {
			serverSocket.close();
		    } catch (IOException e) {
			;
		    }
		    serverSocket = null;
		    while (timeToAccept-- > 0) {
			try { sleep(5000); } catch (InterruptedException e) { ; }
		    }
                } catch (IOException e) {
                    log.severe("Could not establish a connection to port "
                               + Config.getSMSCServerPort() + e);
                } catch (Exception e) {
                    System.err.println("Unexpected exception: " + NtfUtil.stackTrace(e));
                } catch (OutOfMemoryError e) {
                    System.err.println("Out of memory: " + NtfUtil.stackTrace(e));
                    System.exit(-1);
                }
                try {
                    if (serverSocket != null) {
			serverSocket.close();
		    }
                    sleep(5000);
                } catch (InterruptedException e) {
                    ;
                } catch (IOException e) {
                    ;
                }
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.severe("Could not close SMS-C correctly. " + e);
            }
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory: " + NtfUtil.stackTrace(e));
            System.exit(-1);
        }
    }
}

