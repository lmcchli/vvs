package com.mobeon.smsc.util;

import java.util.logging.*;

/* This filter class is used to stop all outher active Loggers to log to the wrong log file**/
public class SMSCFilter implements Filter {
    
    /* The name of the ESME that should use this filter**/
    private String esme_name = null;
    
    /* @param esme_name the name of the ESME connected to this filter.**/
    public SMSCFilter(String esme_name) {                
        this.esme_name = esme_name;  
    }
    
    /* Should only log if the esme_name is equal to the LogRecords name**/
    public boolean isLoggable(LogRecord record){                
        return ( record.getLoggerName().equalsIgnoreCase(esme_name) )?true:false;
    }    
}
