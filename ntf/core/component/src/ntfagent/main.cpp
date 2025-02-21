#include <iostream>
#include <sstream>
#include <fstream>
#include <string>
#include <stdlib.h>
#include <libgen.h>
#include <sys/types.h>
#include <unistd.h>
#include <signal.h>

#include "Config.h"
#include "Logger.h"
#include "def.h"
#include "AgentThread.h"
#include "NtfInterface.h"
#include "NtfMib.h"
#include "NtfObject.h"


using namespace std;

Logger::logtype_e string2loglevel(const string& str);
extern "C" void signalHandler(int signo);
extern "C" void init_signals();
extern void sendSignal(int signal);
void readConfigValues(Logger::logtype_e &logLevel, int &logfilesize, int &timeout);

bool stopFlag = false;
bool loadConfig = false;
bool startUp = true;
int sig = 0;

int main (int argc, char *argv[]) {

    int c;
    int asDaemon = 1;
    int logfilesize = 100000;
    int timeout = 10;
    Logger::logtype_e loglevel = Logger::ERROR;
    pid_t pid;
    string context = "public";
    string logfile;
    string configfile;
    string value;
    string confcmd;
    
    
    
    // Install dir
    char bin[256];
    strcpy(bin, argv[0]);
    char bindir[128];
    strcpy(bindir, dirname(bin));
    string installDir = dirname(bindir);
    string ntf_home = dirname(bindir);
    string cfg_home = "/opt/moip/config/ntf";

    // Parse the command line arguments. Format:
    // ntfgent [ -f ] [ -d ntf_home ] [ context ]
    while ((c = getopt(argc, argv, "fd:c:")) != EOF) {
	switch (c) {
	case 'f':
	    asDaemon = 0;
	    break;
	case 'd':
	    ntf_home = optarg;
                break;
	case 'c':
	    cfg_home = optarg;
        break;
	case '?':
	    break; /*Ignore bad options*/
	}
    }
    cout << "cfg_home: " << cfg_home << endl;
    
    
    // Ignore port number and IP-address. These values are now taken directly
    // from the configuration file. To maintain compatibility with existing
    // scripts and HA agent, the parameter is kept until the next release.
    if (optind < argc) {
	string dummy = argv[optind++];
    }

    // Check context
    if (optind < argc) {
	context.assign(argv[optind++]);
    }
    
    if (optind < argc) {
	cout<<"Error: arguments"<<endl;
	cout<<"Usage: ntfagent [ -f ] [ -d ntf_home ] [ -c cfg_home ] dummy [ context ]"<<endl;
	exit(1);
    }

    pid = getpid();
    
    if (asDaemon) {
	// Make the subagent run as deamon.
	if( (pid = fork() ) < 0) {
	    return(-1);
	}
	else if(pid != 0)
	    exit(0);
	setsid();
	close(0);
	close(1);
	close(2);
	pid = getpid();
    } 

    // Activate the signal catcher
    init_signals();

    // Log pid to file
    stringstream sspid;
    sspid << pid;
    ofstream file;
    string pidfile = ntf_home;
    pidfile.append("/logs/subagent.pid");
    file.open(pidfile.c_str());
    if(file.is_open())
	{
	    file << sspid.str();
	}
    file.close();  
    
    // Initiate the configuration reader

    confcmd = ntf_home;
    confcmd.append("/bin/getconfig ");
    configfile = cfg_home;
    configfile.append("/cfg/notification.cfg");
    try {
    cout << "confcmd: " << confcmd << endl;
	Config::Instance()->init(confcmd, configfile);
	// Initiate the logging
	logfile = ntf_home;
	logfile.append("/logs/ntfagent.log");

	readConfigValues(loglevel, logfilesize, timeout);
	Logger::Instance()->init(logfile, pid, logfilesize, loglevel);
    }
    catch (string message) {
	LOG(Logger::ERROR, "ntfagent", message);
    }

    // Start the SNMP agent
    stringstream ss;
    ss <<  "NTF SNMP agent is started as ";
    if(!asDaemon) 
	ss << "non-";
    ss << "daemon at location " << ntf_home << " with the following configuration values: ";
    LOG(Logger::INFORMATION, "ntfagent", ss.str());
    LOG(Logger::INFORMATION, "ntfagent", "loglevel: " + int2string(loglevel));
    LOG(Logger::INFORMATION, "ntfagent", "logsize: " + int2string(logfilesize));
    string s;
    Config::Instance()->getValue("snmpagentport", s);
    LOG(Logger::INFORMATION, "ntfagent", "snmpagentport: " + s);
    LOG(Logger::INFORMATION, "ntfagent", "snmpagenttimeout: " + int2string(timeout));
    
    NtfInterface* ni = NtfInterface::instance();
    AgentThread* agentThread = AgentThread::instance();
    agentThread->setContext(context);
    agentThread->start();
    
    
    while(!stopFlag && ! agentThread->getStopProcessFlag()) {

	switch(sig) {
	case SIGTERM:
	    LOG(Logger::INFORMATION, "ntfagent", "Caught signal SIGTERM.");
	    stopFlag = true;
	    agentThread->setStopTrapFlag();
	    sig = 0;
	    break;
	case SIGINT:
	    LOG(Logger::INFORMATION, "ntfagent", "Caught signal SIGINT.");
	    stopFlag = true;
	    agentThread->setStopTrapFlag();
	    sig = 0;
	    break;
	case SIGQUIT:
	    LOG(Logger::INFORMATION, "ntfagent", "Caught signal SIGQUIT.");
	    stopFlag = true;
	    agentThread->setStopTrapFlag();
	    sig = 0;
	    break;
	case SIGHUP:
	    LOG(Logger::INFORMATION, "ntfagent", "Caught signal SIGHUP.");
	    try {
		LOG(Logger::INFORMATION, "ntfagent", "Updating the configuration values.");
		Config::Instance()->update(confcmd, configfile);
		readConfigValues(loglevel, logfilesize, timeout);
		Logger::Instance()->reinit(logfilesize, loglevel);
		sig = 0;
	    }
	    catch (string message) {
		LOG(Logger::ERROR, "ntfagent", message);
		sig = 0;
	    }
	    break;
	default:
	    sig = 0;
	    break;
	}


	if (startUp && "" == NtfMib::instance()->getNtfObject()._name) {
	    NtfInterface::instance()->sendStart();
	    LOG(Logger::INFORMATION, "ntfagent", "No managed objects are registered. Sending a start request. " + NtfMib::instance()->getNtfObject()._name);
	}
	else
	    startUp = false;
	IPMSThread::sleep(10000);
    }
    
    // Stop the SNMP agent
    NtfInterface::instance()->stop();
    LOG(Logger::INFORMATION, "ntfagent", "Stopping the SNMP agent.");
    return 0;
}

extern "C" {
    void signalHandler(int signo) {
	sig = signo;
	init_signals();
    }
}

extern "C" {
    void init_signals() {
	struct sigaction act, old_act;
	act.sa_handler = signalHandler;
	sigemptyset(&act.sa_mask);
	act.sa_flags = 0;

#ifdef SA_RESTART
	act.sa_flags |= SA_RESTART;
#endif	
	sigaction (SIGTERM, NULL, &old_act);
	if (old_act.sa_handler != SIG_IGN)
	    sigaction (SIGTERM, &act, NULL);
	sigaction (SIGINT, NULL, &old_act);
	if (old_act.sa_handler != SIG_IGN)
	    sigaction (SIGINT, &act, NULL);
	sigaction (SIGQUIT, NULL, &old_act);
	if (old_act.sa_handler != SIG_IGN)
	    sigaction (SIGQUIT, &act, NULL);
	sigaction (SIGHUP, NULL, &old_act);
	if (old_act.sa_handler != SIG_IGN)
	    sigaction (SIGHUP, &act, NULL);
    }
}

void readConfigValues(Logger::logtype_e &logLevel, int &logFileSize, int &timeOut) {
    string value;
    if (Config::Instance()->getValue("loglevel", value) == SUCCESSFUL)
	logLevel = string2loglevel(value);
    if (Config::Instance()->getValue("logsize", value) == SUCCESSFUL)
	logFileSize = string2int(value);
    if (Config::Instance()->getValue("snmpagenttimeout", value) == SUCCESSFUL)
	timeOut = string2int(value);
}
