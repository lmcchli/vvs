/*
  File:		Logger.cpp
  Originated:	2004-05-10
  Author:	Joakim Nilsson
  Signature:	ermjnil


  Copyright (c) 2004 MOBEON AB

  The copyright to the computer program(s) herein is the property of 
  MOBEON AB, Sweden. The programs may be used and/or copied only 
  with the written permission from MOBEON AB or in accordance with 
  the terms and conditions stipulated in the agreement/contract under 
  which the programs have been supplied.
*/

#include <iostream>
#include <sstream>
#include <fstream>
#include <iomanip>
#include <sys/time.h>
#include <sys/stat.h>
#include <IPMSGuard.h>
#include "Logger.h"

Logger::Logger() {
    _fileName = "ntfagent.log";
    _pid = 0;
    _maxFileSize = 500000;
    _logtype = ERROR;
}

Logger::~Logger() {}

Logger *
Logger::Instance() {

    if ( _pInstance == 0 )
	_pInstance = new Logger();
    return _pInstance;
}

void
Logger::init(string fileName, pid_t pid, int maxSize, int loglevel) {
    _fileName = fileName;
    _pid = pid;
    _maxFileSize = maxSize;
    switch(loglevel) {
    case 0:
	_logtype = OFF;
	break;
    case 1:
	_logtype = ERROR;
	break;
    case 2:
	_logtype = INFORMATION;
	break;
    case 3:
	_logtype = INFORMATION;
	break;
    default:
	_logtype = ERROR;
	break;
    }
}

void
Logger::reinit(int maxSize, int loglevel) {
    _maxFileSize = maxSize;
    switch(loglevel) {
    case 0:
	_logtype = OFF;
	break;
    case 1:
	_logtype = ERROR;
	break;
    case 2:
	_logtype = INFORMATION;
	break;
    case 3:
	_logtype = INFORMATION;
	break;
    default:
	_logtype = ERROR;
	break;
    }
}

void
Logger::log(const logtype_e &type, const string &source, const string &message) {

    IPMSGuard guard(_logfilemutex);

    // log format
    // 2004-05-10 11:10:03.123|1049|Error|EventSender: This is the log message
    stringstream ss;
    ofstream file;

    ss << getDateString() << '|';
    ss << _pid << '|';
    switch(type) {
    case ERROR:
	ss << "Error";
	break;
    case WARNING:
	ss << "Warning";
	break;
    case INFORMATION:
	ss << "Information";
	break;
    default:
	ss << "Error";
	break;
    }
    ss << '|';
    if (source.empty())
	ss << "Unknown source: ";
    else
	ss << source << ": ";
    ss << message << endl;

    checkSize(ss.str().length());

    std::cout << "Logging to " << _fileName << ": " << ss.str() << std::endl;
    file.open(_fileName.c_str(), ios::app);
    if(file.is_open())
	{
	    file << ss.str();
	}
    file.close();    
}


string 
Logger::getDateString() {
    struct timeval tv;
    struct tm t;
    stringstream ss;
    
    if(gettimeofday(&tv, 0) == -1)
	return string("Could not get system time");
    
    tzset();
    localtime_r(&tv.tv_sec, &t);
    
    ss.fill('0');
    ss << 1900 + t.tm_year << '-' << 
	setw(2) << (t.tm_mon+1) << '-' <<
	setw(2) << t.tm_mday << ' ' <<
	setw(2) << t.tm_hour << ':' <<
	setw(2) << t.tm_min << ':' <<
	setw(2) <<  t.tm_sec << '.' <<
	setw(3) << tv.tv_usec / 1000 << ' ' <<
	((timezone > 0) ? '-' : '+') <<
	setw(4) << abs(timezone) / 36;
    return ss.str();
}

void
Logger::checkSize(const size_t messageLen) const {
    
    struct stat buf;
    size_t fileSize;
    stringstream ss;
    string suffix(".sav");
    string oldname = _fileName;
    oldname.append(suffix);
		   
    
    if(stat(_fileName.c_str(), &buf) < 0)
	fileSize = 0;
    else
	fileSize = buf.st_size;

    if((fileSize + messageLen) > _maxFileSize)
	{
	    if(stat(oldname.c_str(), &buf) >= 0) {
		
		if (S_ISREG(buf.st_mode))
		    unlink(oldname.c_str());
	    }   
	    rename(_fileName.c_str(), oldname.c_str());
	}
}

Logger * Logger::_pInstance = 0;

/*
 * These variables describe the formatting of this file.  If you don't like the 
 * template defaults, feel free to change them here (not in your .emacs file).
 *
 * Local Variables:
 * mode: C++
 * comment-column: 32
 * c-basic-offset: 4
 * fill-column: 79
 * End:
*/
