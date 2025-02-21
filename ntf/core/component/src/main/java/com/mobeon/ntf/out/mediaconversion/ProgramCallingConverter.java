/*
* COPYRIGHT Abcxyz Communication Inc. Montreal 2010
* The copyright to the computer program(s) herein is the property
* of ABCXYZ Communication Inc. Canada. The program(s) may be used
* and/or copied only with the written permission from ABCXYZ
* Communication Inc. or in accordance with the terms and conditions
* stipulated in the agreement/contact under which the program(s)
* have been supplied.
*---------------------------------------------------------------------
* Created on 7-Apr-2010
*/
package com.mobeon.ntf.out.mediaconversion;

import java.io.InputStream;
import java.io.IOException;

import com.mobeon.ntf.util.Logger;

/**
 * FileToFileConverter that converts using an external program that converts from
 * one file to the other. Copied from MCC component.
 */
public class ProgramCallingConverter extends FileToFileConverter {
    
    static private Logger log = Logger.getLogger(ProgramCallingConverter.class);

    public static final long MAX_RUNTIME = 30000L;
    /* Special "exit" codes*/
    public static int CONVERSION_PROCESS_ABORTED = 111;
    public static int CONVERSION_PROCESS_EXEC_FAILED = 112;

    protected String program;

    /**
     * Constructor.
     */
    protected ProgramCallingConverter(String IN_FORMAT,
                                      String OUT_FORMAT,
                                      String OUT_MIME,
                                      String program) {
        super(IN_FORMAT, OUT_FORMAT, OUT_MIME);
        this.program = program;
    }

    public int convert(String inFileName, String outFileName, int wantedLength) {
        int result = 0;
        StreamSink stderrSink = null;
        StreamSink stdoutSink = null;
        String cmd = program + " " + inFileName + " " + outFileName + " " + wantedLength;

        log.logMessage("ProgramCallingConverter: Running command " + cmd, Logger.L_DEBUG);
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            ProcessKiller pc = new ProcessKiller(p);
            stderrSink = new StreamSink(p.getErrorStream(), true);
            stdoutSink = new StreamSink(p.getInputStream(), false);
            pc.start();
            stderrSink.start();
            stdoutSink.start();
            try { p.waitFor(); } catch (InterruptedException e) { ; }
	    p.destroy(); //Close all streams, since Process does not do that
			 //when the process terminates naturally.
            if (pc.didAbort()) {
                result = CONVERSION_PROCESS_ABORTED;
            } else {
                result =  p.exitValue();
            }
        } catch (IOException e) {
            log.logMessage("ProgramCallingConverter: Failed to execute " + cmd + ": " + e, Logger.L_ERROR);
            result = CONVERSION_PROCESS_EXEC_FAILED;
        }
        if (stderrSink != null) { stderrSink.stopIt(); }
        if (stdoutSink != null) { stdoutSink.stopIt(); }
        return result;
    }

    public void printit() {
        System.out.println("IN_FORMAT:" + IN_FORMAT);
        System.out.println("OUT_FORMAT:" + OUT_FORMAT);
        System.out.println("OUT_MIME:" + OUT_MIME);
    }

    private class ProcessKiller extends Thread {
        public ProcessKiller(Process p) {
            this.p = p;
        }

        public boolean didAbort() {
            return aborted;
        }

        public void run() {
            try { sleep(MAX_RUNTIME); } catch (InterruptedException e) { ; }
            try {
                p.exitValue();
            } catch (IllegalThreadStateException e) {
                log.logMessage("ProgramCallingConverter: MOV to 3GP conversion did not complete in time - aborting", Logger.L_ERROR);
                aborted = true;
                p.destroy();
            }
        }

        private Process p = null;
        private boolean aborted = false;
    }

    private class StreamSink extends Thread {
        public StreamSink(InputStream str, boolean isError) {
            this.str = str;
            this.isError = isError;
        }

        public void stopIt() {
            stop = true;
        }

        public void run() {
            long startTime = System.currentTimeMillis();
            byte[] buf = new byte[100];
            int got = 0;
            while (!stop && got >= 0 && System.currentTimeMillis() < startTime + MAX_RUNTIME) {
                try {
                    got = str.read(buf);
                    if (got > 0 && (isError)) {
                        String s = new String(buf, 0, got);
                        if (isError) {
                            log.logMessage(s, Logger.L_ERROR);
                        } else {
                            log.logMessage(s, Logger.L_DEBUG);
                        }
                    }
                } catch (IOException e) {
                    log.logMessage("ProgramCallingConverter: Stopping due to: " + e, Logger.L_DEBUG);
                    stop = true;
                }
                try { sleep(100); } catch (InterruptedException e) { ; }
            }
        }

        private InputStream str = null;
        private boolean isError = false;
        private boolean stop = false;
    }
}
