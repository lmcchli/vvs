package com.mobeon.common.util.logging;

import java.io.PrintStream;

import org.apache.log4j.Level;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.mobeon.common.logging.LogAgentFactory;

/**
 * Logging class used to redirect stdout and stderr to log4j via
 * loggingOutputStream
 *
 * @author lmcmajo
 */
public class stdOutErrLogger {
      
    private class STDERR {} //Fake class for logger
    private class STDOUT {} //Fake class for logger
    
    //save original streams...
    static PrintStream stdOutStream = System.out;
    static PrintStream stdErrstream = System.err;
  
    static LogAgent stdLog = LogAgentFactory.getLogAgent(STDOUT.class);
    static LogAgent stdErr = LogAgentFactory.getLogAgent(STDERR.class);
    
    static public void startRedirectStdOut() {       
        System.setOut(new PrintStream(new loggingOutputStream(stdLog,Level.DEBUG, stdOutStream),true));
    }
    
    static public void startRedirectStdErr() {
        System.setErr(new PrintStream(new loggingOutputStream(stdErr,Level.ERROR, stdErrstream),true));
    }
    static public void startRedirectAll() {
        startRedirectStdOut();
        startRedirectStdErr();
    }
    static public void stopRedirectStdOut() {
        System.setOut(stdOutStream);
    }
    static public void stopRedirectStdErr() {
        System.setErr(stdErrstream);
    }
    static public void stopRedirectAll() {
        stopRedirectStdOut();
        stopRedirectStdErr();
    }
}
