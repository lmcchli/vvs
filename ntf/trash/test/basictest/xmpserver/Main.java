/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package xmpserver;

import xmpserver.HttpServer;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import xmpserver.config.Config;

/**
 * Initializes the component and starts the main threads.
 */
public class Main {

    static private Logger log = null;

    /**
     * Initialize logging by setting a handler that writes to an xmp server log file
     * and setting the log level to the configured value. Finally all errors
     * generated when updating the configuration, are logged.
     */
    private static void initLogging() {
        Config.updateCfg();
        String[] errors = Config.getErrors();
        String error = null;
        Handler h;
        Logger l = Logger.getLogger("xmpserver");
        Handler[] harr = l.getHandlers();
        for (int i = 0; i < harr.length; i++) {
            l.removeHandler(harr[i]);
        }
        try {
            h = new FileHandler("../logs/xmpserver.%g.log",
                                Config.getLogSize(),
                                3,
                                true);
        } catch (IOException e) {
            h = new ConsoleHandler();
            error = "Failed to open log file " + e + "\n";
        }
        h.setFormatter(new XmpServerFormatter());
        h.setLevel(Level.ALL);
        l.addHandler(h);
        l.setLevel(Config.getLogLevel());
        log = Logger.getLogger("xmpserver");
        //Log errors that occured before logging was initialized
        for (int i = 0; i < errors.length; i++) {
            log.severe(errors[i]);
        }
        if (error != null) {
            log.severe(error);
        }
    }

    /**
     * Main program for starting an XMP Server process.
     *@param args nothing
     */
    public static void main(String[] args) {
        initLogging();
        log.fine("================ XMP Server started ================================");
        int serverCount = Config.getServerCount();
        for( int i=0;i<serverCount;i++ ) {
            HttpServer httpserver = new HttpServer(i);
            httpserver.start();
        }
        
    }
}
