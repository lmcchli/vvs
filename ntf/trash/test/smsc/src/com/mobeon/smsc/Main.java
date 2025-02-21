/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc;

import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.smsc.SMSCServer;
import com.mobeon.smsc.config.LabHandler;
import com.mobeon.smsc.config.Config;
import com.mobeon.smsc.util.SMSCFormatter;
import com.mobeon.smsc.containers.ESMEHandler;
import com.mobeon.smsc.interfaces.TrafficCentral;
import com.mobeon.smsc.util.SMSCFilter;
import com.mobeon.smsc.management.http.HttpServer;
import com.mobeon.smsc.smpp.SMPPCom;

import java.io.IOException;
import java.util.*;
import java.util.logging.*;

/**
 * Initializes the component and starts the SMS-C and the HTTP server threads.
 */
public class Main {

    static private Logger log = null;

    /**
     * Initialize logging by setting a handler that writes to an SMS-C log file
     * (smsc.log) and setting the log level to the configured value. Finally all errors
     * generated when updating the configuration, are logged.
     */
    private static void initLogging() {
        Config.updateCfg();
        String[] errors = Config.getErrors();
        String error = null;
        Handler h;
        Logger l = Logger.getLogger("");
        Handler[] harr = l.getHandlers();
        for (int i = 0; i < harr.length; i++) {
            l.removeHandler(harr[i]);
        }
        try {
            h = new FileHandler("../logs/smpp.%g.log",
                                Config.getLogSize(),
                                Config.getLogCount(),
                                true);
        } catch (IOException e) {
            h = new ConsoleHandler();
            error = "Failed to open log file " + e + "\n";
        }
        h.setFormatter(new SMSCFormatter());
        h.setLevel(Level.ALL);
        h.setFilter(new SMSCFilter("SMSC"));
        
        l.addHandler(h);
        l.setLevel(Config.getLogLevel());
        
        log = Logger.getLogger("SMSC");
        
        for (int i = 0; i < errors.length; i++) {
            log.severe(errors[i]);
        }
        if (error != null) {
            log.severe(error);
        }
    }

    /**
     * Main program for starting the SMS-C server and the HTTP server.
     *@param args nothing
     */
    public static void main(String[] args) {         
        try {
            initLogging();         
            if (log.isLoggable(Level.FINE)) {        
                log.fine("================ SMS-C started ================================");
            }
            LabHandler e = new LabHandler();
            SMSCServer smscserver = new SMSCServer();
            smscserver.start();
            HttpServer http = new HttpServer(ESMEHandler.get());
            http.start();
	    if (Config.getConnErrBad() > 0) {
		log.severe("Simulating bad connections: "
			   + Config.getConnErrStart() + "-"
			   + Config.getConnErrBad() + "-"
			   + Config.getConnErrOk());
		ConnErrScheduler ces = new ConnErrScheduler(smscserver);
	    }
	    if (Config.getResponseErrBad() > 0) {
		log.severe("Simulating lost responses: "
			   + Config.getResponseErrStart() + "-"
			   + Config.getResponseErrBad() + "-"
			   + Config.getResponseErrOk());
		ResponseErrScheduler res = new ResponseErrScheduler();
	    }
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory: " + NtfUtil.stackTrace(e));
        }
    }

    private static class ConnErrScheduler extends Thread {
	private SMSCServer ss = null;
	
	public ConnErrScheduler(SMSCServer ss) {
	    this.ss = ss;
	    start();
	}

	public void run() {
	    try { sleep(Config.getConnErrStart() * 1000); } catch (InterruptedException e) { ; }
	    while (true) {
		log.severe("RefuseConnect mode " + Config.getConnErrBad() + " seconds");
		ss.simulateRestart(Config.getConnErrBad());
		try { sleep((Config.getConnErrOk() + Config.getConnErrBad()) * 1000); } catch (InterruptedException e) { ; }
	    }
	}
    }

    private static class ResponseErrScheduler extends Thread {
	public ResponseErrScheduler() {
	    start();
	}

	public void run() {
	    try { sleep(Config.getResponseErrStart() * 1000); } catch (InterruptedException e) { ; }
	    while (true) {
		log.severe("NoResponse mode " + Config.getResponseErrBad() + " seconds");
		SMPPCom.dontRespond(Config.getResponseErrBad());
		try { sleep((Config.getResponseErrOk() + Config.getResponseErrBad()) * 1000); } catch (InterruptedException e) { ; }
	    }
	}
    }
}
