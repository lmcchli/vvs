/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "logger.h"

#include <log4cxx/logger.h>
#include <log4cxx/propertyconfigurator.h>
#include <log4cxx/mdc.h>
#include <base_include.h>
#include <iostream>
#include <string>

using std::cout;
using std::endl;

boost::thread_specific_ptr<base::String> Logger::mLoggingData;
base::String Logger::mDummySessionId;


LogData::LogData(base::String logData)
{
	Logger::setSessionLogData(logData);
}


LogData::~LogData(void)
{
	if (Logger::mLoggingData.get() != 0) {
		*(Logger::mLoggingData.get()) = "";
	}
}
		

void Logger::init(const base::String& configurationFile) {
    std::string dummy(configurationFile.c_str());
    cout << "Intializing log4cxx Log Manager [" << dummy << "]" << endl;
    log4cxx::PropertyConfigurator::configureAndWatch(dummy);
    cout << "Intialized Log Manager." << endl;
}

void Logger::setSessionLogData(const base::String& session)
{
	if (mLoggingData.get() == 0) {
        mLoggingData.reset(new base::String);
    }

    *(mLoggingData.get()) = session;
}

const base::String& Logger::getSessionId()
{
    if (mLoggingData.get() == 0) {
        return mDummySessionId;
    }
    return *(mLoggingData.get());
}

void Logger::cleanUp() {
}

Logger* Logger::getLogger(const char* name)
{
    return new Logger(log4cxx::Logger::getLogger(name));
}

Logger::Logger() :
    log4cxx(0)
{
}

Logger::Logger(log4cxx::Logger* logger) :
    log4cxx(logger)
{
}

