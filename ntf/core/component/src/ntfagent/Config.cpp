/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#include "Config.h"
#include "def.h"

Config::Config() {
    // Configuration variables used for the SNMP agent
    _variableList.insert(make_pair(string("loglevel"), string("1"))); // 1=Error
    _variableList.insert(make_pair(string("logsize"),string("100000"))); // byte
    _variableList.insert(make_pair(string("snmpagentport"),string("18001")));
    _variableList.insert(make_pair(string("snmpagenttimeout"), string("10"))); // milliseconds
}

Config::~Config() {}

Config * 
Config::Instance() {
    if (_pInstance == 0)
	_pInstance = new Config();
    return _pInstance;
}

void
Config::init(string cmd, string configFile) {
    if (updateValue("loglevel", cmd, configFile) != SUCCESSFUL ||
	updateValue("logsize", cmd, configFile) != SUCCESSFUL ||
	updateValue("snmpagentport", cmd, configFile) != SUCCESSFUL ||
	updateValue("snmpagenttimeout", cmd, configFile) != SUCCESSFUL)
	throw string("Failed to read configuration variables, using default values");
}

int
Config::getTimeOut() const {
    string t;
    getValue("snmpagenttimeout", t);
    return string2int(t);
}

int
Config::getValue(const string &var, string &value) const {
    map<string,string>::const_iterator ret;
    ret = _variableList.find(var);
    if ( ret != _variableList.end() ) {
	value = ret->second;
	return SUCCESSFUL;
    }
    else {
	value = "";
	return DATA_ERROR;
    }
}

int 
Config::updateValue(string variable, string cmd, string configFile) {

    string value;
    const int bufSize = 1000;
    char outputLine[bufSize];
    map<string,string>::iterator iter;
    pid_t childProcessPid = 0;

    cmd.append(variable);
    cmd.append(" ");
    cmd.append(configFile);

    FILE * cmdfd = popen(cmd.c_str(), "r");
    if (cmdfd == NULL) {
	pclose(cmdfd);
	return SYSTEM_ERROR;
    } 

    while(fgets(outputLine, bufSize, cmdfd) != NULL)
	value.assign(outputLine);

    if (value.size() > 0) {
	iter = _variableList.find(variable);
	if (iter != _variableList.end())
	    iter->second = value;
	else
	    _variableList.insert(make_pair(variable, value));
	pclose(cmdfd);
	return SUCCESSFUL;
    }
    else {
	pclose(cmdfd);
	return DATA_ERROR;
    }
}

Config * Config::_pInstance = 0;

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
