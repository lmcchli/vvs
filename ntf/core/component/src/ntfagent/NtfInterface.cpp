/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#include <sstream>

#include "NtfInterface.h"
#include "NtfMib.h"
#include "AgentThread.h"
#include "Config.h"
#include "Logger.h"

NtfInterface* NtfInterface::_pInstance = 0;

// Returns the IP-address if the variable port is defined as follow
// <IP address>:<port> 
static string getHost(const string strPort) {
    string ret;
    if (strPort.find(":") == string::npos)
	return "127.0.0.1";
    Tokenizer token(strPort, ':');
    return (token.get(ret) == SUCCESSFUL) ? ret : "127.0.0.1"; 	
}

// Returns the port number even if the variable port is defined as follow
// <IP address>:<port> 
static int getPort(const string strPort) {
    string ret;
    int port  = 18001;
    if (strPort.find(":") != string::npos) {
	Tokenizer token(strPort, ':');
	token.get(ret); 
	if (token.get(ret) == SUCCESSFUL)
	    port = string2int(ret);
	return port;
    }
    else
	return string2int(strPort);
}


NtfInterface *
NtfInterface::instance() {
    if (_pInstance == 0)
        _pInstance = new NtfInterface();
    return _pInstance;
}

NtfInterface::NtfInterface() {
    string ipAddrAndPort;
    Config::Instance()->getValue("snmpagentport", ipAddrAndPort);
    
    int port = getPort(ipAddrAndPort);
    string host = getHost(ipAddrAndPort);
    
    _socket = new UnixSocket(port, host, UnixSocket::server);
    start();
    
}

NtfInterface::~NtfInterface() {
}

int NtfInterface::sendGet() {
    
    string message;
    stringstream ss;
    int size = 0;
    
    
    ss << "Type=Get"<<endl;
    ss << endl;
    
    size=ss.str().size();
    message.assign(int2string(size) + "\n");
    message.append(ss.str());
    if (_socket->send(message) != SUCCESSFUL)
        return SYSTEM_ERROR;
    return SUCCESSFUL;
}


int NtfInterface::setLogLevel(int logLevel) {
    MIBAttribute attr("ntfLogLevel", int2string(logLevel));
    return sendSet(attr);
}

int NtfInterface::setAdministrativeState(int state) {
    MIBAttribute attr("ntfAdministrativeState", int2string(state));
    return sendSet(attr);
}

int NtfInterface::setLoadConfig(int value) {
    MIBAttribute attr("ntfLoadConfig", int2string(value));
    return sendSet(attr);
}

int NtfInterface::sendSet(const MIBAttribute &mibattr) {
    string name;
    string message;
    stringstream ss;
    int size = 0;
    
    // Get managed object name
    name = NtfMib::instance()->getNtfObject()._name;
    if ("" == name) {
        return DATA_ERROR;
    }
    
    ss << "Type=Set"<<endl;
    ss << "Index=0"<<endl;
    ss << "Name="<<name<<endl;
    ss << endl;
    ss << mibattr._name<<"="<<mibattr._value<<endl;
    
    size=ss.str().size();
    message.assign(int2string(size) + "\n");
    message.append(ss.str());
    
    if (_socket->send(message) != SUCCESSFUL)
        return SYSTEM_ERROR;
    return SUCCESSFUL;
}

int
NtfInterface::sendStart() {
    string message;
    stringstream ss;
    int size = 0;

    ss << "Type=Set"<<endl;
    ss << endl;
    ss << "start=true"<<endl;

    size=ss.str().size();
    message.assign(int2string(size) + "\n");
    message.append(ss.str());

    if (_socket->send(message) != SUCCESSFUL)
        return SYSTEM_ERROR;
    return SUCCESSFUL;
}

void
NtfInterface::dispatchEvent(string type, int index, int instanceIndex, AttributeVector &body) {
    

    if ("" == type) {
        LOG(Logger::WARNING, "NtfInterface", "Cannot find the event type, throwing event.");
        return;
    }
    if (-1 == index) {
        LOG(Logger::WARNING, "NtfInterface", "Missing Index in " + type + " event, throwing event.");
        return;
    }

    if (START == type) {
        if (index == INDEX_NTF_OBJECT) { //NTF object
            NtfMib::instance()->parseNtfObject(body);
            AgentThread::instance()->setStartTrapFlag();
        } else if (INDEX_NO_INSTANCE == instanceIndex) { //Consumed service
            NtfMib::instance()->parseConsumedService(index, body);
        } else { //Consumed service instance
            NtfMib::instance()->parseConsumedServiceInstance(index, instanceIndex, body);
            NtfMib::instance()->downConsumedServiceInstance(index, instanceIndex);
        }
    } else if (STOP == type) {
        if (index == INDEX_NTF_OBJECT) { //NTF object
            AgentThread::instance()->setStopTrapFlag();
        } else if (INDEX_NO_INSTANCE == instanceIndex) { //Consumed service
        } else { //Consumed service instance
            NtfMib::instance()->upConsumedServiceInstance(index, instanceIndex);
        }
    } else if (RESPONSE == type) {
        if (index == INDEX_NTF_OBJECT) { //NTF object
            NtfMib::instance()->parseNtfObject(body);
        } else if (index >= INDEX_COMMON_ALARM) { // Common alarms
            NtfMib::instance()->parseCommonAlarm(index, body); 
        } else if (INDEX_NO_INSTANCE == instanceIndex) { //Consumed service
            NtfMib::instance()->parseConsumedService(index, body);
        } else { //Consumed service instance
            NtfMib::instance()->parseConsumedServiceInstance(index, instanceIndex, body);
        }
    } else if (ALARM == type) { // Common alarms
        NtfMib::instance()->parseCommonAlarm(index, body);
    } else {
        LOG(Logger::WARNING, "NtfInterface", "Undefined event type: " + type + ", throwing event.");
    }
}

void
NtfInterface::handleData(string data) {
    AttributeVector attributes;
    Tokenizer dataReader(data, '\n');
    string row;
    Tokenizer rowReader;
    string column;
    string attr;
    string value;
    string evData;
    string tmpData;
    string type;
    int index = -1;
    int instanceIndex = -1;
    int eventSize = 0;

    while(dataReader.get(row) == SUCCESSFUL) {

        // Check if row contain only a new line character
        if (row.size() == 0)
            continue;
        // Read size of event
        eventSize = string2int(row.c_str());
        if (eventSize <= 0) {
            LOG(Logger::WARNING, "NtfInterface", "Event size is not specified, throwing event.");
            return;
        }

        // Copy data for event, +1 for the \n
        evData = data.substr(row.size()+1, eventSize);
        tmpData = data.substr(eventSize + row.size()+1);
        data.assign(tmpData);
        if (evData.size() == 0)
            continue;
        dataReader.init(evData, '\n');
        // Read the data and sort it into a map with the attribute name as key.
        while(dataReader.get(row) == SUCCESSFUL) {
            // Check if row contain only a new line character
            if (row.size() == 0)
                continue;
            attr.empty();
            value.empty();
            rowReader.init(row, '=');
            rowReader.get(attr);
            rowReader.get(value);

            // Put header data and body data in different maps
            if( "Type" == attr ) {
                type = value;
            } else if( "Index" == attr ) {
                index = string2int(value);
            } else if( "InstanceIndex" == attr ) {
                instanceIndex = string2int(value);
            } else if( "Name" == attr ) {

            } else {
                MIBAttribute attribute(attr, value);
                attributes.push_back(attribute);
            }


        }

        dispatchEvent(type, index, instanceIndex, attributes);
        attributes.clear();
        index = -1;
        instanceIndex = -1;



        if (data.size() > 0) {
            dataReader.init(data, '\n');
        }
    }
}

void
NtfInterface::run() {

    // Buffer to store data
    string dataBuff;

    LOG(Logger::INFORMATION, "NtfInterface", "Starting listener thread");

    while (!_stopRequested) {
        dataBuff.clear();
        if (_socket->receive(dataBuff) == SUCCESSFUL) {
            if (dataBuff.size() > 0) {
                LOG(Logger::INFORMATION, "NtfInterface", "Get message: \n" + dataBuff);
                handleData(dataBuff);
            }
        } else {
            LOG(Logger::WARNING, "NtfInterface", "Failed to receive message. " + _socket->getErrorMsg());
        }
    }
    
    LOG(Logger::INFORMATION, "NtfInterface", "Stopping listener thread.");
    return ;
}

void
NtfInterface::stop() {
    // Stop the run loop
    requestStop();
}
