/*
 * Copyright(c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging;

/**
 * An interface providing access to the logging functionality. 
 * It provides access to the underlaying logging framework used by the implementing class.
 *
 * @author David Looberger
 */
public interface ILogger {
    public enum Level{DEBUG,INFO,WARN,ERROR,FATAL}
    
    void debug(Object message);
    void debug(Object message, Throwable t);

    void info(Object message);
    void info(Object message, Throwable t);

    void warn(Object message);
    void warn(Object message, Throwable t);

    void error(Object message);
    void error(Object message, Throwable t);

    void fatal(Object message);
    void fatal(Object message, Throwable t);

    void clearSessionInfo();
    void registerSessionInfo(String name, Object sessionInfo);

    boolean isDebugEnabled();
    boolean isInfoEnabled();

}
