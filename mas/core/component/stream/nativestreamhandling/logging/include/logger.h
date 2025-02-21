/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MOBEON_LOGGER_H_
#define MOBEON_LOGGER_H_

#ifdef WIN32
#include <WinSock2.h> // Ensuring that we do not include WinSock.h
#endif
#include <log4cxx/logger.h>
#include <boost/thread/tss.hpp>
#include <base_include.h>

class Logger;

#define LOGGER_FATAL(logger, message) \
    LOG4CXX_FATAL(logger->log4cxx, " [SID:" << Logger::getSessionId() << "] - " << message);

#define LOGGER_ERROR(logger, message) \
    LOG4CXX_ERROR(logger->log4cxx, " [SID:" << Logger::getSessionId() << "] - " << message);

#define LOGGER_WARN(logger, message) \
    LOG4CXX_WARN(logger->log4cxx, " [SID:" << Logger::getSessionId() << "] - " << message);

#define LOGGER_INFO(logger, message) \
    LOG4CXX_INFO(logger->log4cxx, " [SID:" << Logger::getSessionId() << "] - " << message);

#define LOGGER_DEBUG(logger, message) \
    LOG4CXX_DEBUG(logger->log4cxx, " [SID:" << Logger::getSessionId() << "] - " << message);

// Definition for exporting to DLL on windows 
#ifdef WIN32
#  pragma warning( disable : 4251 )
#  ifndef LOGGER_NO_DLL
#    define LOGGER_CLASS_EXPORT __declspec(dllexport)
#  else 
#    define LOGGER_CLASS_EXPORT
#  endif
#else
#  define LOGGER_CLASS_EXPORT
#endif


class LOGGER_CLASS_EXPORT  LogData {
public:
	/*LogData::*/LogData(base::String logData);
	~LogData(void);
};

class LOGGER_CLASS_EXPORT Logger {
 friend class LogData;
 public:
    /** 
     * Initiates the logger with log properties from configuration file. 
     * <p>
     * Should only be called once at system startup.
     */
    static void init(const base::String& configurationFile);
    
    static void setSessionLogData(const base::String& session);
    static const base::String& getSessionId();
    
    /** 
     * Clears cached configuration information.
     * <p>
     * Should only be called once at system shutdown.
     */
    static void cleanUp();
    
    /**
     * Creates a logger for a specific class.
     * <p>
     * Note that the caller owns the returned pointer!
     * 
     * @param className Name of calling class.
     * 
     * @return Pointer to logger-instance for the given class name.
     */
    static Logger* getLogger(const char* name);

    log4cxx::Logger* log4cxx;
private:
    Logger();
    Logger(log4cxx::Logger* logger);
    static boost::thread_specific_ptr<base::String> mLoggingData;
    static base::String mDummySessionId;
};


#endif
