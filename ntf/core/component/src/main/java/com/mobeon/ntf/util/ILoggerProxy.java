/*
 * ILoggerProxy.java
 *
 * Created on den 30 september 2005, 15:07
 */

package com.mobeon.ntf.util;

import com.mobeon.common.util.logging.ILogger;
import com.mobeon.ntf.util.Logger;
/**
 *
 * @author  MNIFY
 */
public class ILoggerProxy implements ILogger  {
    
    private Logger log = Logger.getLogger(ILoggerProxy.class); ;
    
    /** Creates a new instance of ILoggerProxy */
    public ILoggerProxy() {
    }
    
    public ILoggerProxy(String clazz) {
        log = Logger.getLogger(clazz);
    }
    
    public void debug(Object message) {
        log.logMessage((String)message,Logger.L_DEBUG);
    }
    
    public void debug(Object message, Throwable t) {
        log.logMessage((String)message + "\n" + NtfUtil.stackTrace(t), Logger.L_DEBUG);
    }
    
    public void error(Object message) {
        log.logMessage((String)message,Logger.L_ERROR);
    }
    
    public void error(Object message, Throwable t) {
        log.logMessage((String)message + "\n" + NtfUtil.stackTrace(t),Logger.L_ERROR);
        
    }
    
    public void fatal(Object message) {
        log.logMessage((String)message,Logger.L_ERROR);
    }
    
    public void fatal(Object message, Throwable t) {
        log.logMessage((String)message + "\n" + NtfUtil.stackTrace(t),Logger.L_ERROR);
    }
    
    public void info(Object message) {
        log.logMessage((String)message,Logger.L_VERBOSE);
    }
    
    public void info(Object message, Throwable t) {
        log.logMessage((String)message + "\n" + NtfUtil.stackTrace(t), Logger.L_VERBOSE);
    }
    
    public boolean isDebugEnabled() {
        return log.willLog(Logger.L_DEBUG);
    }
    
    public void registerSessionInfo(String name, Object sessionInfo) {
    }
    
    public void warn(Object message) {
        log.logMessage((String)message,Logger.L_VERBOSE);
    }
    
    public void warn(Object message, Throwable t) {
        log.logMessage((String)message,Logger.L_VERBOSE);
    }
    
}
