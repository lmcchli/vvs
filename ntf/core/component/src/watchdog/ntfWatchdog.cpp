/*
  File:		ntfWatchdog.cpp
  Description:	This class starts the NTF processes and re-starts them
                if they go down. Based on esiWatchdog made by Malin Flodin.
  Originated:	2004-10-13
  Author:	Niklas Fyhr
  Signature:	mnify


  Copyright (c) 2004 MOBEON AB

  The copyright to the computer program(s) herein is the property of 
  MOBEON AB, Sweden. The programs may be used and/or copied only 
  with the written permission from MOBEON AB or in accordance with 
  the terms and conditions stipulated in the agreement/contract under 
  which the programs have been supplied.
*/

#include <string>
#include <iostream>
#include <fstream>
#include <sys/stat.h>
#include <sys/wait.h>
#include <signal.h>

#include "Logger.h"
#include "def.h"

using std::string;

extern "C" void signalHandler(int signo);
extern "C" void init_signals();

bool loadConfig(false);

Logger::logtype_e loglevel(Logger::ERROR);
int logsize(10000000);
string instance_name("");
string ntf_basedir("");
string instance_home("");
string snmpport("");
bool debug = false;
string NTF_PIDFILE("");
string SUBAGENT_PIDFILE("");
string WATCHDOG_PIDFILE("");

pid_t agentPID = 0;
pid_t ntfPID = 0;
bool stopFlag = false;

enum ProcessType {NTF, SUBAGENT};

void set_pid(const string& pidFile, const pid_t& pid)
{
  std::fstream file(pidFile.c_str(), std::ios::out);
  if (file.is_open()) {
    file << pid << std::endl;
  }
}

void kill_process(const string& pidFile, 
                  pid_t pid)
{
  kill(pid, SIGKILL);
  unlink(pidFile.c_str());
}

pid_t start_process(const string& basedir,
                    const string& javabin, 
                    const string& pidFile, 
                    ProcessType type)
{
  pid_t pid;
  if ( (pid = fork()) == 0) {

    close(0);
    close(1);
    close(2);

    // Write started process pid to file
    set_pid(pidFile, getpid());

    switch (type) {

    case NTF: {
      string logPath = instance_home + "/logs/NotificationProcess.log";
      int fileDes = open(logPath.c_str(),O_RDWR|O_CREAT);
      LOG(Logger::INFORMATION, "ntfWatchdog",
      	"log file opened at " + int2string(fileDes) );

      if(fileDes != 2) {
	dup2(fileDes,2);
      }
      if(fileDes != 1) {
	dup2(fileDes,1);
      }

      string configFile = "-DconfigFile=" + instance_home + "/cfg/notification.cfg";
      string debug1 = "-Xdebug";
      string debug2 = "-Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n";

      if(debug) {
        execl(javabin.c_str(), javabin.c_str(), 
	"-server", "-Xmx256m", debug1.c_str(), debug2.c_str(), configFile.c_str(),
	    "com.mobeon.ntf.NtfMain", 0);
      } else {
        execl(javabin.c_str(), javabin.c_str(),
        "-server", "-Xmx256m", configFile.c_str(),
            "com.mobeon.ntf.NtfMain", 0);
      }

      break;
    }
    case SUBAGENT: {
      string process = basedir + "/bin/ntfagent";
      execl(process.c_str(), process.c_str(), 
	   "-f -d", basedir.c_str(), snmpport.c_str() , 0);

      break;
    }
    default:
      break;
    }

    unlink(pidFile.c_str());
    exit(0);
  } else {
    return pid;
  }
}

string getConfigParameter(const string& variable)
{
  string configBinary = ntf_basedir + string("/bin/getconfig");
  string configFile = instance_home + string("/cfg/notification.cfg");

  // usage: getconfig <variable> <configfile>
  string configCommand = configBinary + string(" ") + variable + 
    string(" ") + configFile;

  FILE* cmdfd = popen(configCommand.c_str(), "r");
  if (cmdfd == NULL) {
    pclose(cmdfd);
    return "";
  } 

  string value;
  const int bufSize(1000);
  char outputLine[bufSize];
  while(fgets(outputLine, bufSize, cmdfd) != NULL)
    value.assign(outputLine);

  // Get the substring before first new line character
  std::string::size_type index = value.find('\n');
  value = value.substr(0, index);
  pclose(cmdfd);
  return value;
}

void getConfig()
{
  // Get loglevel
  string logLevelStr = getConfigParameter("WATCHDOGLOGLEVEL");
  if (!logLevelStr.empty())
    loglevel = string2loglevel(logLevelStr);

  // Get logsize
  logsize = 10000000;
  string ntfLogSize = getConfigParameter("WATCHDOGLOGSIZE");
  if (!ntfLogSize.empty())
    logsize = string2int(ntfLogSize);

  if(logsize == 0) {
     logsize = 10000000;
  }

    // Get instance name
  instance_name = getConfigParameter("MCR_INSTANCE_NAME");

  LOG(Logger::INFORMATION, "ntfWatchdog", 
      "Reading configuration parameters.");

  LOG(Logger::INFORMATION, "ntfWatchdog", 
      string("WATCHDOGLOGLEVEL: ") + int2string(loglevel));
  LOG(Logger::INFORMATION, "ntfWatchdog", 
      string("WATCHDOGLOGSIZE: ") + int2string(logsize));
  LOG(Logger::INFORMATION, "ntfWatchdog", 
      string("MCR_INSTANCE_NAME: ") + instance_name);

  Logger::Instance()->reinit(logsize, loglevel);
}

void setupLogging()
{
  pid_t pid = getpid();
  string logfile = instance_home + string("/logs/ntfWatchdog.log");
  string logLevelStr = getConfigParameter("WATCHDOGLOGLEVEL");
  if (!logLevelStr.empty())
    loglevel = string2loglevel(logLevelStr);

  Logger::Instance()->init(logfile, pid, logsize, loglevel);
}

void loadConfiguration()
{
  Logger::logtype_e loglevel(Logger::INFORMATION);
  int logsize(1000000);
  getConfig();
}

int main (int argc, char *argv[])
{
  // Parse the command line arguments. Format:
  // ntfWatchdog -d ntf_instance_home -b ntf_basedir [ -j java_binary ] -p port
  string java_binary("/usr/bin/java");
  int c(0);

  while ((c = getopt(argc, argv, "b:d:j:p:x")) != EOF) {
    switch (c) {
    case 'b':
      ntf_basedir = optarg;
      break;
    case 'd':
      instance_home = optarg;
      break;
    case 'j':
      java_binary = optarg;
      break;
    case 'p':
      snmpport = optarg;
      break;
    case 'x':
      debug = true;
      break;
    case '?':
      break; /*Ignore bad options*/
    }
  }
    
  if (optind < argc || ntf_basedir.empty() || snmpport.empty() ) {
    std::cout << "Error: arguments" << std::endl;
    std::cout 
      << "Usage: ntfWatchdog -d instancehome -b ntfbasedir [ -j java_binary ] -p snmpport " 
      << std::endl;
    exit(1);
  }


  NTF_PIDFILE = instance_home + "/logs/notification.pid";
  SUBAGENT_PIDFILE = instance_home + "/logs/subagent.pid";
  WATCHDOG_PIDFILE = instance_home + "/logs/watchdog.pid";

    // Make the process run as daemon.
  pid_t pid;
  if( (pid = fork() ) < 0) {
    return(-1);
  }
  else if(pid != 0) {
    exit(0);
  }

  setsid();
  close(0);
  close(1);
  close(2);

  init_signals();

    // Write watchdog pid to file
  set_pid(WATCHDOG_PIDFILE, getpgrp());

  // Setup logging
  setupLogging();

  // Retrieve necessary config parameters
  // Do this after logging has been setup so that the parameter values
  // can be logged
  getConfig();

  ntfPID = start_process(ntf_basedir, java_binary, 
			       NTF_PIDFILE, NTF);

  LOG(Logger::INFORMATION, "ntfWatchdog", 
      "Started NTF process, pid: " + int2string(ntfPID));

  agentPID = start_process(ntf_basedir, java_binary, 
				 SUBAGENT_PIDFILE, SUBAGENT);

  LOG(Logger::INFORMATION, "ntfWatchdog", 
      "Started ntfagent, pid: " + int2string(agentPID));


  while (1) {

    if (loadConfig) {
      LOG(Logger::INFORMATION, "ntfWatchdog", 
	  "Updating the configuration values.");
      loadConfiguration();
      loadConfig = false;
    }

    int status;
    pid_t stoppedPID = wait(&status);

    if( stopFlag ) {
	continue;
    }
     
    if (stoppedPID == ntfPID) {
      LOG(Logger::ERROR, "ntfWatchdog", 
	  "NTF process has terminated, pid: " + int2string(ntfPID));

      // First kill the ntfagent to resync the management interface
      kill_process(SUBAGENT_PIDFILE, agentPID);

      // Start NTF
      ntfPID = start_process(ntf_basedir, java_binary, 
			     NTF_PIDFILE, NTF);
      LOG(Logger::INFORMATION, "ntfWatchdog", 
	  "Started NTF process, pid: " + int2string(ntfPID));

      // Start ntfAgent

      agentPID = start_process(ntf_basedir, java_binary, 
			       SUBAGENT_PIDFILE, SUBAGENT);
      LOG(Logger::INFORMATION, "ntfWatchdog", 
	  "Started ntfagent, pid: " + int2string(agentPID));


    } else if (stoppedPID == agentPID) {
	LOG(Logger::ERROR, "ntfWatchdog", 
	    "ntfAgent process has terminated, pid: " + 
	    int2string(agentPID));
	agentPID = start_process(ntf_basedir, java_binary, 
				 SUBAGENT_PIDFILE, SUBAGENT);
	LOG(Logger::INFORMATION, "ntfWatchdog", 
	    "Started ntfagent, pid: " + int2string(agentPID));
    }
  }
}

void waitForProcessesToDie(int maxSeconds) {
   int count = 0;
   while( count <= maxSeconds ) {
	if(ntfPID != 0) {
	    if( kill(ntfPID,0) == 1 ) {
		ntfPID = 0;
	    }
	}
	if(agentPID != 0) {
            if( kill(agentPID,0) == 1 ) {
                agentPID = 0;
            }   
        }
	sleep(1); 
	count++;
	if( ntfPID == agentPID && ntfPID == 0 ) {
	    break;
	}
   }
}

void shutdown() {
   LOG(Logger::INFORMATION, "ntfWatchdog",
            "Ntfwatchdog shutting down");

   // set stopFlag so no restart of processes.
   stopFlag = true;
   //shutdown ntf
   if (instance_name.size() == 0)
       getConfig();
   string command = ntf_basedir + "/bin/stopntf " + snmpport + " " + instance_name;
   
   system(command.c_str());   

   //shutdown agent 
    LOG(Logger::INFORMATION, "ntfWatchdog",
            "sending kill to " + int2string(agentPID));
   kill(agentPID, SIGQUIT);

   waitForProcessesToDie(15);

   if( ntfPID != 0 && kill(ntfPID,0) == 0 ) {
      kill(ntfPID,9);
   }

   if( agentPID != 0 && kill(agentPID,0) == 0 ) {
      kill(agentPID,9);
   }

   remove(NTF_PIDFILE.c_str());
   remove(SUBAGENT_PIDFILE.c_str());
   remove(WATCHDOG_PIDFILE.c_str());
   
   exit(0);
   
}

extern "C" { 
  void init_signals() {
    struct sigaction act;
    act.sa_handler = signalHandler;
    sigemptyset(&act.sa_mask);
    act.sa_flags = 0;

    sigaction(SIGHUP, &act, 0);
    sigaction(SIGQUIT, &act, 0);
  }
}

extern "C" {
  void signalHandler(int signo) 
    {
      switch(signo) {
      case SIGHUP:
	loadConfig = true;
	break;
      case SIGQUIT:  
	shutdown();
	break;
      default:
	break;
      }
      init_signals();
    }
}

