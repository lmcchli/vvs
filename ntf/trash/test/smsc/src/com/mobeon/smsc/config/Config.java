/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.config;


import java.util.*;
import java.util.logging.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * The configuration is defined by default values and values in the SMSC
 * configuration file.
 */
public class Config {
    
    /** The name of the SMSC configuration file */
    private static final String CONFIG_FILE_NAME = "../cfg/smsc.cfg";

    /** Contains all configuration values */
    private static Properties props;
    
    /** Contains defaults for all configuration values */
    private static Properties def;
    
    /** Collects all errors generated before logging was initialized, so they
     can be logged later */
    private static Vector logErrors = new Vector();

    private static Level logLevel = Level.ALL;
    private static int smscServerPort;
    private static int logCount;
    private static int logSize;
    private static int httpServerPort;
    private static int sms0delay;
    private static int percentError;
    private static int responseDelay;
    private static int dropEvery;
    private static int connErrStart;
    private static int connErrOk;
    private static int connErrBad;
    private static int responseErrStart;
    private static int responseErrOk;
    private static int responseErrBad;

    /**
     * Update the configuration from file.
     */
    public static void updateCfg() {
        setDefaults();
        try {
            props = new Properties(def);
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(CONFIG_FILE_NAME));

            props.load(bis);
            bis.close();
        } catch (FileNotFoundException e) {
            err(
                    "Configuration file : \"" + CONFIG_FILE_NAME
                    + "\" not found. Using default values.");
        } catch (IOException e) {
            err(
                    "Could not read the configuration file. Error Text: "
                            + e.getMessage() + ". Using default values");
        }
        parseCfg();
    }

    /**
     * Sets the default values for all config parameters.
     */
    protected static void setDefaults() {
        def = new Properties();
        def.setProperty("SMSCServerPort", "5016");
        def.setProperty("LogCount", "3");
        def.setProperty("LogLevel", "SEVERE");
        def.setProperty("LogSize", "1000000");
        def.setProperty("HttpServerPort", "8080");
        def.setProperty("sms0delay", "30");
        def.setProperty("percenterror", "0");
        def.setProperty("dropevery", "0");
        def.setProperty("responsedelay", "0");
        def.setProperty("connerrstart", "0");
        def.setProperty("connerrok", "0");
        def.setProperty("connerrbad", "0");
        def.setProperty("responseerrstart", "0");
        def.setProperty("responseerrok", "0");
        def.setProperty("responseerrbad", "0");
    }

    /**
     * Parse all configuration parameters into their real type.
     */
    protected static void parseCfg() {
        logCount = getInt("LogCount");
        logLevel = parseLogLevel();
        logSize = getInt("LogSize");
        smscServerPort = getInt("SMSCServerPort");
        httpServerPort = getInt("HttpServerPort");
        responseDelay = getInt("responsedelay");
        sms0delay = getInt("sms0delay");
        percentError = getInt("percenterror");
        dropEvery = getInt("dropevery");
	connErrStart = getInt("connerrstart");
	connErrOk = getInt("connerrok");
	connErrBad = getInt("connerrbad");
	responseErrStart = getInt("responseerrstart");
	responseErrOk = getInt("responseerrok");
	responseErrBad = getInt("responseerrbad");
    }
    
    /**
     * Get a boolean parameter.
     *@param key the name of the boolean config parameter
     *@return the boolean value named key, or default
     */
    private static boolean getBoolean(String key) {
        String trueWords[] = { "on", "true", "yes", "1"};
        String falseWords[] = { "off", "false", "no", "0"};
        String tmp = props.getProperty(key);
        int i;

        if (tmp == null) {
            return false;
        }
        for (i = 0; i < trueWords.length; i++) {
            if (tmp.compareToIgnoreCase(trueWords[i]) == 0) {
                return true;
            }
        }
        for (i = 0; i < falseWords.length; i++) {
            if (tmp.compareToIgnoreCase(falseWords[i]) == 0) {
                return false;
            }
        }
        err(
                "Config: " + key
                + " should be on, true, yes, off, false or no, not \"" + tmp
                + "\"");
        return false;
    }

    /**
     * Get a byte array.
     *@param key the name of the integer array config parameter
     *@return the integer array named key, or default
     */
    private static byte[] getByteArray(String key) {
        String tmp = props.getProperty(key);
        int val;
        Vector v = new Vector();
        byte[] arr;

        StringTokenizer st = new StringTokenizer(tmp, " -,");

        while (st.hasMoreTokens()) {
            v.add(st.nextToken());
        }
        arr = new byte[v.size()];
        int i = 0;
        Iterator it = v.iterator();

        while (it.hasNext()) {
            try {
                val = Integer.parseInt((String) it.next());
                if (val < 0 || val > 255) {
                    throw new NumberFormatException("Value out of range");
                }
            } catch (Exception e) {
                err(
                        "Config: " + key
                        + " should have integer values 0-255, not " + tmp);
                return new byte[0];
            }
            arr[i++] = new Integer(val).byteValue();
        }
        return arr;
    }

    /**
     * Get an integer array.
     *@param key the name of the integer array config parameter
     *@param def the default value if the parameter is absent or not an integer array
     *@return the integer array named key, or default
     */
    private static int[] getIntArray(String key, int def[]) {
        String tmp = props.getProperty(key);
        int val;
        Vector v = new Vector();
        int[] arr;

        if (tmp == null) {
            return def;
        } else {
            StringTokenizer st = new StringTokenizer(tmp);

            while (st.hasMoreTokens()) {
                v.add(st.nextToken());
            }
            arr = new int[v.size()];
            int i = 0;
            Iterator it = v.iterator();

            while (it.hasNext()) {
                try {
                    val = Integer.parseInt((String) it.next());
                } catch (Exception e) {
                    System.err.println(
                            "Config: " + key
                            + " should have integer values, not " + tmp);
                    val = def[i];
                }
                arr[i++] = val;
            }
        }
        return arr;
    }

    /**
     * Get an integer parameter.
     *@param key the name of the integer config parameter
     *@return the integer value named key, or default
     */
    private static int getInt(String key) {
        String tmp = props.getProperty(key);
        int val;

        if (tmp == null) {
            return 0;
        }

        try {
            val = Integer.parseInt(tmp);
        } catch (Exception e) {
            err("Config: " + key + " should have an integer value, not " + tmp);
            val = Integer.parseInt(def.getProperty(key));
        }
        return val;
    } 

    /**
     * Special log function for the Config class, that saves messages until the
     * log is initialized.
     *@param msg the error message.
     */
    private static void err(String msg) {
        logErrors.add(msg);
    }

    /**
     * Get all saved error messages.
     *@return array of the saved error messages.
     */
    public static String[] getErrors() {
        Vector v = logErrors;

        logErrors = new Vector();
        return (String[]) (v.toArray(new String[0]));
    }

    /**
     * Parses a log level string and makes it into an integer log level.
     *@return the log level.
     */
    private static Level parseLogLevel() {
        String lev = props.getProperty("LogLevel");

        if (lev.equalsIgnoreCase("ALL")) {
            return Level.ALL;
        } else if (lev.equalsIgnoreCase("finest")
                || lev.equalsIgnoreCase("finer") || lev.equalsIgnoreCase("FINE")
                || lev.equalsIgnoreCase("info")
                || lev.equalsIgnoreCase("config")) {
            return Level.FINE;
        } else if (lev.equalsIgnoreCase("WARNING")) {
            return Level.WARNING;
        } else if (lev.equalsIgnoreCase("OFF")) {
            return Level.OFF;
        } else {
            if (!lev.equalsIgnoreCase("SEVERE")) {
                err(lev + " is not a valid LogLevel\n");
            }
            return Level.SEVERE;
        }
    }

    /**
     * Parses a log level string and makes it into an integer log level.
     *@return the log level.
     */
    private static Level parseLogLevel(String loglevel) {        

        if (loglevel.equalsIgnoreCase("ALL")) {
            return Level.ALL;
        } else if (loglevel.equalsIgnoreCase("finest")
                || loglevel.equalsIgnoreCase("finer")
                || loglevel.equalsIgnoreCase("FINE")
                || loglevel.equalsIgnoreCase("info")
                || loglevel.equalsIgnoreCase("config")) {
            return Level.FINE;
        } else if (loglevel.equalsIgnoreCase("WARNING")) {
            return Level.WARNING;
        } else if (loglevel.equalsIgnoreCase("OFF")) {
            return Level.OFF;
        } else {
            if (!loglevel.equalsIgnoreCase("SEVERE")) {
                err(loglevel + " is not a valid LogLevel\n");
            }
            return Level.SEVERE;
        }
    }

    public static Level getLogLevel() {
        return logLevel;
    }

    public static Level getLogLevel(String lev) {
        return parseLogLevel(lev);
    }

    public static int getSMSCServerPort() {
        return smscServerPort;
    }

    public static int getLogCount() {
        return logCount;
    }

    public static int getLogSize() {
        return logSize;
    }

    public static int getHttpServerPort() {
        return httpServerPort;
    }

    public static int getSms0delay() {
        return sms0delay;
    }

    public static int getPercentError() {
        return percentError;
    }

    public static int getResponseDelay() {
        return responseDelay;
    }

    public static int getDropEvery() {
        return dropEvery;
    }

    public static int getConnErrStart() {
        return connErrStart;
    }

    public static int getConnErrOk() {
        return connErrOk;
    }

    public static int getConnErrBad() {
        return connErrBad;
    }

    public static int getResponseErrStart() {
        return responseErrStart;
    }

    public static int getResponseErrOk() {
        return responseErrOk;
    }

    public static int getResponseErrBad() {
        return responseErrBad;
    }

    /**
     * The main program prints the configuration variables sorted
     * alphabetically. Parameters that have their default value are omitted.
     *@param args command line arguments. If the first argument is
     * --with_defaults, parameters with default values are included as comment
     * lines.
     */
    public static void main(String[] args) {
        boolean withDef = false;
        boolean withObs = false;

        String key;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--defaults")) {
                withDef = true;
            } else if (args[i].equals("--obsolete")) {
                withObs = true;
            } else if (args[i].startsWith("--") || i + 1 < args.length) {
                System.out.println(
                        "This program prints a clean version of the SMS-C configuration file.");
                System.out.println(
                        "If you omit the file name, the configuration file is selected in the");
                System.out.println("same way as SMS-C does when it it started.");
                System.out.println(
                        "Usage: java com.mobeon.smsc.Config [--with_defaults] [--with_obsolete] [--help] [<file>]");
                System.out.println(
                        "       --defaults, parameters that have the default value are also shown (as comments)");
                System.out.println(
                        "       --obsolete, parameters that are no longer valid are also shown (as comments)");
                System.out.println("       --help prints this information");
                return;
            }
        }
        updateCfg();

        for (Enumeration e = def.propertyNames(); e.hasMoreElements();) {
            key = (String) (e.nextElement());
            if (!props.containsKey(key)) {
                props.setProperty(key, def.getProperty(key));
            }
        }

        TreeMap tm = new TreeMap(props);
        Iterator it = tm.keySet().iterator();

        while (it.hasNext()) {
            key = (String) it.next();
            if (def.getProperty(key) == null) { // Obsolete parameter
                if (withObs) {
                    System.out.println(
                            "#OBSOLETE or ILLEGAL: " + key + "="
                            + props.getProperty(key));
                }
            } else if (props.getProperty(key).equals(def.getProperty(key))
                    && !props.getProperty(key).equals("NoDefault")) { // Has default value
                if (withDef) {
                    System.out.println(
                            "#DEFAULT VALUE      : " + key + "="
                            + props.getProperty(key));
                }
            } else {
                if (withDef || withObs) {
                    System.out.println(
                            "                      " + key + "="
                            + props.getProperty(key));
                } else {
                    System.out.println(key + "=" + props.getProperty(key));
                }
            }
        }
    }
}
