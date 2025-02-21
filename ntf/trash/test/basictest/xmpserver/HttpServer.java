/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package xmpserver;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.logging.*;
import java.util.Random;
import xmpserver.config.Config;

/**
 * Listens for HTTP connections and spawns HTTP handlers to handle each new connection.
 */
public class HttpServer extends Thread {

    /** Socket to listen to */
    private ServerSocket serverSocket = null;
    /** */
    private boolean listening = true;
    /** General log target */
    private Logger log = null;
    
    private int serverNumber = 0;

    private Random random = null;
    /**
     * Constructor.
     */
    public HttpServer(int serverNumber) {
        this.setName("HttpServer");
        log = Logger.getLogger("xmpserver");
        this.serverNumber = serverNumber;
        random = new Random();
        log.fine("Starting XMP Server " + serverNumber + " at port " + Config.getHttpServerPort() + serverNumber);
    }

    /**
     * Run method that listens for connections and spawns HTTP handlers.
     */
    public void run() {
        int i = 0;
        try {
            while (listening) {
                int serverPort = Config.getHttpServerPort() + serverNumber;
                try {
                    
                    serverSocket = new ServerSocket(serverPort);
                    while (true) {
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Waiting for connection to " + serverPort);
                        }
                        if( random.nextInt(100) < Config.getAcceptFailuresPercent() ){
                            sleep(40000);
                        }
                        Socket s = serverSocket.accept();
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Connection to port " + serverPort);
                        }
                        new HttpHandler(s, i++, log).start();
                        int randInt = random.nextInt(100);
                        if( randInt < Config.getConnectionDropPercent() ) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    log.severe("Could not establish a connection to port "
                               + serverPort + e);
                } catch (Exception e) {
                    log.severe("Unknown exception: " + e);
                } catch (OutOfMemoryError e) {
                    System.exit(-1);
                }
                try {
                    serverSocket.close();
                    int sleepTime = random.nextInt(20);
                    sleep((1+sleepTime) * 1000);
                } catch (InterruptedException e) {
                    ;
                } catch (IOException e) {
                    ;
                }
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.severe("Could not close XmpServer correctly. " + e);
            }
        } catch (OutOfMemoryError e) {
            System.exit(-1);
        }
    }
}
