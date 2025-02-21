package com.mobeon.ntf.util;

import com.mobeon.ntf.util.time.NtfTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ErrorLogLimiter supresses error messages. It works with code that reports an
 * error over and over when it detects it and suppresses transient errors and
 * limits the logging of persistent errors according to the following
 * rules:<UL>
 * <LI>When an error occurs, do not report it until it has been reported for 1
 * minute.
 * <LI>When an error persists, report it only once every 5 minutes.
 * <LI>If an error has not been reported in one minute, it is gone and the next
 * report will start the cycle anew.
 * </UL>
 * It is of course possible to set the values of "1" and "5" in the description
 * above.
 */
public class ErrorLogLimiter {

    private static final Logger log = Logger.getLogger(ErrorLogLimiter.class);
    ConcurrentHashMap<String, ErrorInfo> err;
    int first;
    int next;


    public ErrorLogLimiter(int timeToFirstLog,
                           int timeToNextLog) {
        first = timeToFirstLog;
        next = timeToNextLog;
        err = new ConcurrentHashMap<String, ErrorInfo>();
    }

    public synchronized int report(String msg, Exception e, boolean unexpected) {
        ErrorInfo info = err.get(msg);
        if (info == null) {
            info = new ErrorInfo();
            err.put(msg, info);
        } else {
            if (NtfTime.now > info.lastReportTime + first) {
                info.init();
            } else if (NtfTime.now >= info.nextLogTime) {
                log.logMessage(msg + " (" + info.count + " times)"
                               + (unexpected
                                  ?": unexpected " + NtfUtil.stackTrace(e)
                                  :": " + e), Logger.L_ERROR);
                info.logged();
            } 
        }
        info.report();
        return info.count;
    }


    /**
     * ErrorInfo holds information about errors that have been reported.
     */
    private class ErrorInfo {
        public int nextLogTime; //The earliest time it is allowed to log the error
        public int lastReportTime;
        public int count; //The number of times the error has occured since it
                          //was last logged
        
        public ErrorInfo() {
            init();
        }

        public void report() {
            ++count;
            lastReportTime = NtfTime.now;
        }
            
        public void init() {
            nextLogTime = NtfTime.now + first;
            lastReportTime = 0;
            count = 0;
        }

        public void logged() {
            nextLogTime = NtfTime.now + next;
            count = 0;
        }
    };
};
