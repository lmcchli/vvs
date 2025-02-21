/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#include "NtfInterface.h"
#include "NtfMib.h"
#include "def.h"
#include "ntfdefs.h"
#include "Logger.h"
#include <IPMSGuard.h>

#define NANOSECONDS 1000000000LL

NtfMib* NtfMib::_theMib(0);

NtfMib::~NtfMib() {
}

NtfMib::NtfMib() :
    _mibMutex(),
    _obj(),
    _serv(),
    _servinst(),
    _notRespondingTime(0),
    _nextUpdateTime(0) {
}

NtfMib*
NtfMib::instance() {
    if (0 == _theMib) {
        _theMib = new NtfMib();
    }
    return _theMib;
}

NtfObject&
NtfMib::getNtfObject() {
    return _obj;
}

ConsumedService*
NtfMib::getConsumedService(int index) {
    list<ConsumedService>::iterator it = _serv.begin();
    while (it != _serv.end()) {
        if (it->_index >= index) {
            return &(*it);
        }
        it++;
    }
    return 0;
}

ConsumedService&
NtfMib::getOrMakeConsumedService(int index) {
    list<ConsumedService>::iterator it = _serv.begin();
    while (it != _serv.end() && it->_index <= index) {
        if (it->_index == index) {
            return *it;
        }
        it++;
    }
    ConsumedService cs(index);
    return *(_serv.insert(it, cs));
}

ConsumedServiceInstance*
NtfMib::getConsumedServiceInstance(int serviceIndex, int instanceIndex) {
    list<ConsumedServiceInstance>::iterator it = _servinst.begin();
    while (it != _servinst.end()) {
        if (it->_serviceIndex > serviceIndex ) {
           instanceIndex = 1;
        } 
        if (it->_serviceIndex >= serviceIndex
            && it->_instanceIndex >= instanceIndex
              && it->_status == D_ntfConsumedServiceInstancesStatus_down ) {
            return &(*it);
        }
        it++;
    }
    return 0;
}

ConsumedServiceInstance&
NtfMib::getOrMakeConsumedServiceInstance(int serviceIndex, int instanceIndex) {
    list<ConsumedServiceInstance>::iterator it = _servinst.begin();
    while (it != _servinst.end() && it->_serviceIndex <= serviceIndex && it->_instanceIndex <= instanceIndex) {
        if (it->_serviceIndex == serviceIndex && it->_instanceIndex == instanceIndex) {
            return *it;
        }
        it++;
    }
    ConsumedServiceInstance csi(serviceIndex, instanceIndex);
    return *(_servinst.insert(it, csi));
}

CommonAlarm*
NtfMib::getCommonAlarm(int index) {
    list<CommonAlarm>::iterator it = _commonAlarms.begin();
    while (it != _commonAlarms.end()) {
        if (it->_index >= index) {
            return &(*it);
        }
        it++;
    }
    return 0;
}

CommonAlarm&
NtfMib::getOrMakeCommonAlarm(int index) {
    list<CommonAlarm>::iterator it = _commonAlarms.begin();
    while (it != _commonAlarms.end() && it->_index <= index) {
        if (it->_index == index) {
            return *it;
        }
        it++;
    }
    CommonAlarm ca(index);
    return *(_commonAlarms.insert(it, ca));
}

IPMSMutex&
NtfMib::getMibMutex() {
    return _mibMutex;
}

void
NtfMib::downConsumedServiceInstance(int serviceIndex, int instanceIndex) {
    IPMSGuard g(_mibMutex);

    ConsumedServiceInstance& csi = getOrMakeConsumedServiceInstance(serviceIndex, instanceIndex);
    csi._status = D_ntfConsumedServiceInstancesStatus_down;
}

void
NtfMib::upConsumedServiceInstance(int serviceIndex, int instanceIndex) {
    IPMSGuard g(_mibMutex);

    ConsumedServiceInstance& csi = getOrMakeConsumedServiceInstance(serviceIndex, instanceIndex);
    csi._status = D_ntfConsumedServiceInstancesStatus_up;
}

bool
NtfMib::checkUpdate() {
    IPMSGuard g(_mibMutex);

#ifdef linux
    time_t now = time(NULL);
#else
    hrtime_t now = gethrtime();
#endif

    if (now < _nextUpdateTime) { return false; }
    
#ifdef linux
    _nextUpdateTime = now + 10;
    if (0 == _notRespondingTime) {
        _notRespondingTime = now + 120;
    }
#else
    _nextUpdateTime = now + 10LL * NANOSECONDS;
    if (0 == _notRespondingTime) {
        _notRespondingTime = now + 120LL * NANOSECONDS;
    }
#endif
    NtfInterface::instance()->sendGet();
    return true;
}

static int
parseAttribute(string name, string value, int def) {
    if (""== value) {
        LOG(Logger::ERROR, "NtfMib", "Empty attribute value for " + name);
        return def;
    } else {
        return string2int(value);
    }
}

static string
parseAttribute(string name, string value, string def) {
	if ("" == value) {
        LOG(Logger::ERROR, "NtfMib", "Empty attribute value for " + name);
        return def;
    } else {
    	return value;
    }
}

static void
logEntry(const vector<MIBAttribute>& entry) {
    for (int i = 0; i < entry.size(); i++) {
        LOG(Logger::INFORMATION, "NtfMib", entry[i]._name + "=" + entry[i]._value);
    }
}

bool
NtfMib::parseNtfObject(const vector<MIBAttribute>& entry) {
    //    LOG(Logger::INFORMATION, "NtfMib", "Parsing NTF entry");
    //    logEntry(entry);
    IPMSGuard g(_mibMutex);
    for (int i = 0; i < entry.size(); i++) {
        string name = entry[i]._name;
        string value = entry[i]._value;
        if ("" == name) {
            LOG(Logger::ERROR, "NtfObjects with ", "empty attribute name");
        } else if ("ntfName" == name) {
            _obj._name = parseAttribute(name, value, "noname");
        } else if ("ntfVersion" == name) {
        	_obj._version = parseAttribute(name, value, "noversion");
        } else if ("ntfOperationalState" == name) {
            _obj._operationalState = parseAttribute(name, value, D_ntfOperationalState_enabled);
        } else if ("ntfAdministrativeState" == name) {
            _obj._administrativeState = parseAttribute(name, value, D_ntfAdministrativeState_unlocked);
        } else if ("ntfInstallDate" == name) {
            _obj._installDate = parseAttribute(name, value, "");
        } else if ("ntfCurrentUpTime" == name) {
            int newUptime = parseAttribute(name, value, 0);
            if (newUptime != _obj._currentUpTime) {
                _notRespondingTime = 0;
            }
            _obj._currentUpTime = newUptime;
        } else if ("ntfAccumulatedUpTime" == name) {
            _obj._accumulatedUpTime = parseAttribute(name, value, 0);
        } else if ("ntfNotifInQueue" == name) {
            _obj._notifInQueue = parseAttribute(name, value, 0);
        } else if ("ntfNotifForRetry" == name) {
            _obj._notifForRetry = parseAttribute(name, value, 0);
        } else if ("ntfInternalQueues" == name) {
            _obj._internalQueues = parseAttribute(name, value, 0);
        } else if ("ntfLoadConfig" == name) {
            _obj._loadConfig = parseAttribute(name, value, D_ntfLoadConfig_inactive);
        } else if ("ntfLogLevel" == name) {
            _obj._logLevel = parseAttribute(name, value, D_ntfLogLevel_error);
        } else if ("ntfMailboxPollerStatus" == name) {
            _obj._mailboxPollerStatus = parseAttribute(name, value, D_ntfMailboxPollerStatus_active);
        } else {
            LOG(Logger::INFORMATION, "NtfMib", "Unknown attribute name: " + name);
        }
    }
    return true;
}

bool
NtfMib::parseConsumedService(int index, const vector<MIBAttribute>& entry) {
    //    LOG(Logger::INFORMATION, "NtfMib", "Parsing consumed service entry " + int2string(index));
    //    logEntry(entry);
    IPMSGuard g(_mibMutex);

    ConsumedService& cs = getOrMakeConsumedService(index);

    unsigned int i;
    for (i = 0; i < entry.size(); i++) {
        string name = entry[i]._name;
        string value = entry[i]._value;
        if ("" == name) {
            LOG(Logger::ERROR, "NtfMib", "empty attribute name for consumed service");
        } else if ("ntfConsumedServiceName" == name) {
            cs._name = parseAttribute(name, value, "unknownname");
        } else if ("ntfConsumedServiceStatus" == name) {
            cs._status = parseAttribute(name, value, D_ntfConsumedServiceStatus_up);
        } else if ("ntfConsumedServiceTime" == name) {
            cs._time = parseAttribute(name, value, 0);
        } else if ("ntfConsumedServiceNumSuccess" == name) {
            cs._numSuccess = parseAttribute(name, value, 0);
        } else if ("ntfConsumedServiceNumFailures" == name) {
            cs._numFailures = parseAttribute(name, value, 0);
        } else if ("ntfConsumedServiceIndex" == name) {
            // do nothing with index.
        } else {
            LOG(Logger::INFORMATION, "NtfMib", "Unknown attribute name for consumed service: " + name);
        }
    }
    return true;
}

bool
NtfMib::parseConsumedServiceInstance(int serviceIndex, int instanceIndex, const vector<MIBAttribute>& entry) {
    //    LOG(Logger::INFORMATION, "NtfMib", "Parsing consumed service instance entry " + int2string(serviceIndex) + "," + int2string(instanceIndex));
    //    logEntry(entry);
    IPMSGuard g(_mibMutex);

    ConsumedServiceInstance& csi = getOrMakeConsumedServiceInstance(serviceIndex, instanceIndex);

    unsigned int i;
    for (i = 0; i < entry.size(); i++) {
        string name = entry[i]._name;
        string value = entry[i]._value;
        if ("" == name) {
            LOG(Logger::ERROR, "ConsumedServiceInstance", "empty attribute name");
        } else if ("ntfConsumedServiceInstanceName" == name) {
            csi._name = parseAttribute(name, value, "unknownname");
        } else if ("ntfConsumedServiceInstanceStatus" == name) {
            csi._status = parseAttribute(name, value, D_ntfConsumedServiceStatus_up);
        } else if ("ntfConsumedServiceInstanceHostName" == name) {
            csi._hostname = parseAttribute(name, value, "unknownhost");
        } else if ("ntfConsumedServiceInstancePort" == name) {
            csi._port = parseAttribute(name, value, 0);
        } else if ("ntfConsumedServiceInstanceZone" == name) {
            csi._zone = parseAttribute(name, value, "Unspecified");
        } else if ("ntfConsumedServiceInstanceIndex" == name) {
            
        } else if ("ntfConsumedServiceIndex" == name) {
            
        } else {
            LOG(Logger::ERROR, "NtfMib", "Unknown attribute name in consumed service instance: " + name);
        }
    }
    return true;
}

bool
NtfMib::parseCommonAlarm(int index, const vector<MIBAttribute>& entry) {
    //    LOG(Logger::INFORMATION, "NtfMib", "Parsing common alarm entry " + int2string(index));
    //    logEntry(entry);
    IPMSGuard g(_mibMutex);

    CommonAlarm& ca = getOrMakeCommonAlarm(index);

    unsigned int i;
    for (i = 0; i < entry.size(); i++) {
        string name = entry[i]._name;
        string value = entry[i]._value;
        if ("" == name) {
            LOG(Logger::ERROR, "NtfMib", "empty attribute name for common alarm");
        } else if ("alarmId" == name) {
            ca._id = parseAttribute(name, value, "unknownid");
        } else if ("alarmStatus" == name) {
            ca._status = parseAttribute(name, value, D_alarmStatus_notAvailable);
        } else if ("alarmIndex" == name) {
            // do nothing with index.
        } else {
            LOG(Logger::INFORMATION, "NtfMib", "Unknown attribute name for consumed service: " + name);
        }
    }
    return true;
}


bool
NtfMib::ntfNotAnsweringOnRequest() {
    if (_notRespondingTime == 0) { return false; }

#ifdef linux
    return (time(NULL) > _notRespondingTime);
#else
    return (gethrtime() > _notRespondingTime);
#endif
}
