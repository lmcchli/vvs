#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <pthread.h>
#include <log4cpp/Category.hh>
#include <log4cpp/PropertyConfigurator.hh>
#include <log4cpp/Priority.hh>
#include <log4cpp/Configurator.hh>
#include "ilogger.h"

using std::string;
using log4cpp::Category;
using log4cpp::Priority;
using log4cpp::PropertyConfigurator;

namespace {
    std::string cfgFilename;
    int watchDelay = 60;
    pthread_t tid = -1;
    pthread_mutex_t mutex= PTHREAD_MUTEX_INITIALIZER;
    struct stat st_old;
    struct stat st_new;
    bool continueMonitor = true;

    extern "C" {
	void *FileMonitorThread(void *arg)
	{
	    bool failed = false;
	    while(continueMonitor) {
		bool update = false;

		pthread_mutex_lock(&mutex);
		stat( cfgFilename.c_str(), &st_new );
		pthread_mutex_unlock(&mutex);

		if( st_old.st_ino != st_new.st_ino ) {
		    update = true;
		}

		if( st_old.st_size > st_new.st_size ) {
		    update = true;
		}

		if( st_old.st_ctime != st_new.st_ctime ) {
		    update = true;
		}

		if( st_old.st_size < st_new.st_size ) {
		    update = true;
		}

		if( st_old.st_mtime != st_new.st_mtime ) {
		    update = true;
		}

		if(update) {
		    pthread_mutex_lock(&mutex);
		    try {
			PropertyConfigurator::configure(cfgFilename);
			failed = false;
		    } catch(log4cpp::ConfigureFailure& e) {
			failed = true;
		    }

		    pthread_mutex_unlock(&mutex);
		}

		memcpy( &st_old, &st_new, sizeof( struct stat ));

		// The delay can be read and written to atomically, therefore
		// we skip the need for a mutex here.
		for(int i = 0; i < watchDelay && continueMonitor; ++i) {
		    usleep(999999);

		    // If the previous read failed, try again ASAP.
		    if(failed) {
			break;
		    }
		}
	    }

	    return 0;
	}
    }
}

ILogger::ILogger(const string& category) : cat(Category::getInstance(category)) {
}

void ILogger::debug(const char * stringFormat, ...) throw() {
    va_list va;
    va_start(va,stringFormat);
    cat.logva(Priority::DEBUG, stringFormat, va);
    va_end(va);
}

void ILogger::debug(const string& logMessage) throw() {
    cat.debug(logMessage);
}

void ILogger::info(const char * stringFormat, ...) throw() {
    va_list va;
    va_start(va,stringFormat);
    cat.logva(Priority::INFO, stringFormat, va);
    va_end(va);
}

void ILogger::info(const string& logMessage) throw() {
    cat.info(logMessage);
}

void ILogger::warn(const char * stringFormat, ...) throw() {
    va_list va;
    va_start(va,stringFormat);
    cat.logva(Priority::WARN, stringFormat, va);
    va_end(va);
}

void ILogger::warn(const string& logMessage) throw() {
    cat.warn(logMessage);
}

void ILogger::error(const char * stringFormat, ...) throw() {
    va_list va;
    va_start(va,stringFormat);
    cat.logva(Priority::ERROR, stringFormat, va);
    va_end(va);
}

void ILogger::error(const string& logMessage) throw() {
    cat.error(logMessage);
}

void ILogger::fatal(const char * stringFormat, ...) throw() {
    va_list va;
    va_start(va,stringFormat);
    cat.logva(Priority::FATAL, stringFormat, va);
    va_end(va);
}

void ILogger::fatal(const string& logMessage) throw() {
    cat.fatal(logMessage);
}

bool ILogger::isDebugEnabled() const throw() {
    return cat.isDebugEnabled();
}

bool ILogger::isInfoEnabled() const throw() { 
    return cat.isInfoEnabled();
}

bool ILogger::isWarningEnabled() const throw() {
    return cat.isWarnEnabled();
}

bool ILogger::isErrorEnabled() const throw() {
    return cat.isErrorEnabled();
}

bool ILogger::isFatalEnabled() const throw() {
    return cat.isFatalEnabled();
}

void ILogger::configureAndWatch(const string& filename, long delay) throw(ConfigureFailure) {
    pthread_mutex_lock(&mutex);

    cfgFilename = filename;
    watchDelay = delay;

    pthread_mutex_unlock(&mutex);

    try {
	PropertyConfigurator::configure(filename);
    } catch(log4cpp::ConfigureFailure& e) {
	throw ConfigureFailure(e.what());
    }

    stat( filename.c_str(), &st_old );

    if(tid != -1) {
	pthread_create(&tid, 0, FileMonitorThread, 0);
    }
}

void ILogger::configureAndWatch(const string& filename) throw(ConfigureFailure) {
    configureAndWatch(filename, 60);
}
