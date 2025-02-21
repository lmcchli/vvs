/*
  File:		Logger.h
  Description:	
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

#ifndef LOGGER_H
#define LOGGER_H

#include <string>
#include <iostream>
#include <sstream>
#include <fstream>
#include <iomanip>
#include <sys/time.h>
#include <stdlib.h>
#include <sys/stat.h>
#include "IPMSMutex.h"

using std::string;
using std::ofstream;
using std::endl;
using std::setw;
using std::stringstream;
using std::ios;
using std::iostream;

#define LOG(type, source, message) { \
if (type > Logger::OFF && type <= Logger::Instance()->getLogType()) Logger::Instance()->log(type, source, message);}

class Logger {
public:
    enum logtype_e {OFF, ERROR, WARNING, INFORMATION};

    
    ~Logger();
    static Logger * Instance();
    void log(const logtype_e &type, const string &source, const string &message);
    void init(string fileName, pid_t pid, int maxSize, int loglevel);
    void reinit(int maxSize, int loglevel);
    logtype_e getLogType() const {return _logtype;};
private:
    string _fileName;
    pid_t _pid;
    int _maxFileSize;
    logtype_e _logtype;
    static Logger * _pInstance;
    IPMSMutex _logfilemutex; // Mutex to protect the logfile 
    string getDateString();    
    void checkSize(const size_t messageLen) const;

    Logger();
};

Logger::logtype_e string2loglevel(const string &str);

#endif
