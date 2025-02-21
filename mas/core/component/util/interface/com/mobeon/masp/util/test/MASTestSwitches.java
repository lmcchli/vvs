package com.mobeon.masp.util.test;

import com.mobeon.common.logging.*;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

/**
 * This class contains switches that is used during testing
 * to alter behaviour of certain classes to aid in testing.
 *
 * @author Mikael Andersson
 */
public class MASTestSwitches {
    private static boolean unitTesting = false;
    private static boolean compilerTesting = false;
    private static ILogger log = ILoggerFactory.getILogger(MASTestSwitches.class);
    private static boolean configurationTesting = false;
    private static boolean watchdogTesting = false;
    private static boolean testCanTimeout = true;
    private static File cwd = new File("").getAbsoluteFile();
    private static boolean forcedLoggingEnabled = Boolean.getBoolean("com.mobeon.junit.runapp.force_debug");

    static {
        if("mas".equals(cwd.getName())) {
            String frob = "";
            if(File.separatorChar == '/')
                frob = File.separator;
            cwd = new File(frob+cwd.getAbsolutePath()+File.separator+"execution_engine");
        }
        if(forcedLoggingEnabled)
            enableForcedLogging();
    }


    public static File currentMasDir() {
        return cwd;
    }
    public static boolean isUnitTesting() {
        return unitTesting;
    }

    public static boolean isCompilerTesting() {
        return compilerTesting;
    }

    /**
     * Enable features that should only be active
     * during unit testing.
     */
    public static void enableUnitTesting() {
        unitTesting = true;
    }

    /**
     * Enable features that should only be active
     * while testing of compilers.
     */
    public static void initForCompilerTest() {
        compilerTesting = true;
    }


    public static long scale(long timeout) {
        String scalingFactorProperty = System.getProperty("com.mobeon.junit.runapp.scalingfactor");
        float scalingFactor = 1.0f;
        try {
            if (scalingFactorProperty != null)
                scalingFactor = new Float(scalingFactorProperty);
        } catch (NumberFormatException nfe) {
            if (log.isDebugEnabled())
                log.debug("Invalid scaling factor " + scalingFactorProperty + " specified. It must be a valid Java float");
        }
        if (scalingFactor > 0.01 && scalingFactor < 100) {
            if (timeout != 0) {
                int scaled = Math.abs(Math.round(timeout * scalingFactor));
                if (log.isDebugEnabled())
                    log.debug("Scaling " + timeout + " with scaling factor " + scalingFactor + " yielding " + scaled);
                timeout = scaled;
                //We add one ms because 0 here probably is a mistake, and it's better to fail than to hang.
            }
        }
        return timeout;
    }

    public static void enableConfigurationTesting() {
        configurationTesting = true;
    }

    public static boolean isConfigurationTesting() {
        return configurationTesting;
    }

    public static void enableWatchdogTesting() {
        watchdogTesting = true;
    }

    public static boolean isWatchdogTesting() {
        return watchdogTesting;
    }

    public static void disableTestTimeout() {
        testCanTimeout = false;
    }

    public static boolean canTestTimeout() {
        return testCanTimeout;
    }

    public static void disableWatchdogTesting() {
        watchdogTesting = false;
    }

public static void enableForcedLogging() {
 forcedLoggingEnabled = true;

}

//    public static void enableForcedLogging() {
//        forcedLoggingEnabled = true;
//        Enumeration e = RepositorySelectorImpl.getInstance().getCurrentLoggers();
//        while(e.hasMoreElements()) {
//            Logger l = (Logger)e.nextElement();
//            l.setLevel(Level.DEBUG);
//        }
//    } 

    public static void reset() {
        unitTesting = false;
        compilerTesting = false;
        configurationTesting = false;
        watchdogTesting = false;
        testCanTimeout = true;
        forcedLoggingEnabled = false;
    }

    public static boolean isForcedLoggingEnabled() {
        return forcedLoggingEnabled;
    }
}
