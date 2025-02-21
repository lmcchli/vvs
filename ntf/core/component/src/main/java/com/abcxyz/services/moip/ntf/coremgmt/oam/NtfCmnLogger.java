package com.abcxyz.services.moip.ntf.coremgmt.oam;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.LogEvent;
import com.abcxyz.messaging.common.oam.LogLevel;
import com.mobeon.common.logging.LogAgentFactory;

/**
 * NTF logger to be used by common core classes
 *
 * @author lmchuzh
 *
 */
public class NtfCmnLogger implements LogAgent{

    private static LogAgent logger = LogAgentFactory.getLogAgent("com.abcxyz.services.moip.ntf.coremgmt");

    public static LogAgent getLogAgent(String clazz) {
        return LogAgentFactory.getLogAgent(clazz);
    }

    @SuppressWarnings("unchecked")
    public static LogAgent getLogAgent(Class clazz) {
        return LogAgentFactory.getLogAgent(clazz);
    }
    
    public void trace(String arg0)
    {
        logger.trace(arg0);

    }

    public void trace(String arg0, Throwable arg1)
    {
        logger.trace(arg0, arg1);
    }

    public void debug(String arg0)
    {
        logger.debug(arg0);

    }

    public void debug(String arg0, Throwable arg1)
    {
        logger.debug(arg0, arg1);
    }

    public void error(String arg0)
    {
        logger.error(arg0);
    }

    public void error(LogEvent arg0)
    {
        logger.error(arg0);
    }

    public void error(String arg0, Throwable arg1)
    {
        logger.error(arg0, arg1);
    }

    public void fatal(String arg0)
    {
        logger.fatal(arg0);
    }

    public void fatal(LogEvent arg0)
    {
        logger.fatal(arg0);
    }

    public void fatal(String arg0, Throwable arg1)
    {
        logger.fatal(arg0, arg1);
    }

    public void info(String arg0)
    {
        logger.info(arg0);
    }

    public void info(LogEvent arg0)
    {
        logger.info(arg0);
    }

    public void info(String arg0, Throwable arg1)
    {
        logger.info(arg0, arg1);
    }

    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }

    public void setEventLogLevel(int arg0)
    {
        // not needed
    }

    public void setEventLogLevel(String arg0)
    {
        // not needed
    }

    public void warn(String arg0)
    {
        logger.warn(arg0);
    }

    public void warn(LogEvent arg0)
    {
        logger.warn(arg0);
    }

    public void warn(String arg0, Throwable arg1)
    {
        logger.warn(arg0, arg1);
    }


    @Override
    public void setLogLevel(String level)
    {
    	logger.setLogLevel(level);
    }
    
	@Override
	public void trace(LogEvent arg0) {
		logger.trace(arg0);
	}

	@Override
	public void debug(LogEvent arg0) {
		 logger.debug(arg0);
	}

	@Override
	public LogLevel getLogLevel() {
		return logger.getLogLevel();
	}

	@Override
	public boolean isEnabledFor(LogLevel arg0) {
		return logger.isEnabledFor(arg0);
	}

	@Override
	public boolean isEnabledFor(String arg0) {
		return logger.isEnabledFor(arg0);
	}

	@Override
	public void setLogLevel(LogLevel arg0) {
		logger.setLogLevel(arg0);
	}

}
