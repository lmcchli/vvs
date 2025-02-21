/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.mobeon.common.logging.LogAgentFactory;
import com.mobeon.common.storedelay.SDLogger;

import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * Converts use of SDLogger to logg with standardlogger.
 */
public class DelayLoggerProxy extends SDLogger
{
    private final static Logger ntfLog = Logger.getLogger(DelayLoggerProxy.class); 
    
    //added to force the existing logger to use the backend logger when writing to the actual file
	private static final LogAgent logAgent = LogAgentFactory.getLogAgent(DelayLoggerProxy.class);

    public DelayLoggerProxy()
    {
        
    }        
    
    //replace the code to use the backend logger when writing to a file
    public void doLog(int level, String message)
    {
    	//only if level is fatal write to the error.log file using the backend
        if ( level == FATAL ) {
          	 logAgent.fatal(message);
        }
    	ntfLog.logMessage(message, convertLevel(level));
    }

    public void doLog(int level, String message, Throwable t)
    {
        if( level == FATAL) {
        	logAgent.fatal(message, t); 
        }
        int ntfLevel = convertLevel(level);
        ntfLog.logMessage(message + ": Exception : " + t,
                          ntfLevel);
    }

    protected void doLogObject(int level, String message, Object obj)
    {
	ntfLog.logMessage(message + ":" + obj, convertLevel(level));
    }

    /** MAP[SDLevel] == NTFLevel */
    private static final int[] LEVEL_MAP =
	new int[] {
	    Logger.L_DEBUG, Logger.L_DEBUG,          // TRACE, DEBUG
		Logger.L_DEBUG, Logger.L_VERBOSE,  // INFO,WARNING
		Logger.L_ERROR, Logger.L_ERROR,      // ERROR, SEVERE
		Logger.L_ERROR, Logger.L_ERROR,      // FATAL, EXTRA
		Logger.L_ERROR, Logger.L_ERROR};     // EXTRA, EXTRA
    /**
     * Convert a SDLogger level to Ntf level
     */
    private int convertLevel(int sdLevel)
    {
	try {
	    return LEVEL_MAP[sdLevel];
	} catch (IndexOutOfBoundsException paranoid) {
	    return Logger.L_ERROR;
	}
    }


     public String getCurDate() {
        Calendar cal = Calendar.getInstance();
        Date curdate = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss:SSS");
        df.setTimeZone(cal.getTimeZone());
        return df.format(curdate);
    }


}
