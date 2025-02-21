/*
 * ILogger.java
 *
 * Created on den 20 september 2005, 14:55
 */

package com.mobeon.common.util.logging;


public interface ILogger {
    public void debug(Object message);
    public void debug(Object message, Throwable t);

    public void info(Object message);
    public void info(Object message, Throwable t);

    public void warn(Object message);
    public void warn(Object message, Throwable t);

    public void error(Object message);
    public void error(Object message, Throwable t);

    public void fatal(Object message);
    public void fatal(Object message, Throwable t);

    public void registerSessionInfo(String name, Object sessionInfo);

    public boolean isDebugEnabled();
};

