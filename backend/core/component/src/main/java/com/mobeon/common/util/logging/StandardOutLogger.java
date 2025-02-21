/*
 * StandardOutLogger.java
 *
 * Created on den 30 september 2005, 15:07
 */

package com.mobeon.common.util.logging;

/**
 *
 * @author  MNIFY
 */
public class StandardOutLogger implements ILogger  {
    private int level;
    
    public static final int L_DEBUG = 5;
    public static final int L_INFO = 4;
    public static final int L_WARN = 3;
    public static final int L_ERROR = 2;
    public static final int L_FATAL = 1;
    
    /** Creates a new instance of ILoggerProxy */
    public StandardOutLogger(int level) {
        this.level = level;
    }
    
    public void debug(Object message) {
        if( level >= L_DEBUG ) {
            System.out.println(message.toString());
        }
    }
    
    public void debug(Object message, Throwable t) {
        if( level >= L_DEBUG ) {
            System.out.println(message.toString() + ": " + t.toString());
        }
    }
    
    public void error(Object message) {
        if( level >= L_ERROR ) {
            System.out.println(message.toString());
        }
    }
    
    public void error(Object message, Throwable t) {
        if( level >= L_ERROR ) {
            System.out.println(message.toString() + ": " + t.toString());
        }
        
    }
    
    public void fatal(Object message) {
        if( level >= L_FATAL ) {
            System.out.println(message.toString());
        }
    }
    
    public void fatal(Object message, Throwable t) {
        if( level >= L_FATAL ) {
            System.out.println(message.toString() + ": " + t.toString());
        }
    }
    
    public void info(Object message) {
        if( level >= L_INFO ) {
            System.out.println(message.toString());
        }
    }
    
    public void info(Object message, Throwable t) {
        if( level >= L_INFO ) {
            System.out.println(message.toString() + ": " + t.toString());
        }
    }
    
    public boolean isDebugEnabled() {
        return level >= L_DEBUG;
    }
    
    public void registerSessionInfo(String name, Object sessionInfo) {
    }
    
    public void warn(Object message) {
        if( level >= L_WARN ) {
            System.out.println(message.toString());
        }
    }
    
    public void warn(Object message, Throwable t) {
        if( level >= L_WARN ) {
            System.out.println(message.toString() + ": " + t.toString());
        }
    }
    
}
