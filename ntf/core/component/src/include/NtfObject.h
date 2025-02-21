/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#ifndef NTFOBJECT_H
#define NTFOBJECT_H

#include <string>

using namespace std;

struct NtfObject {
    NtfObject();

    string _name;
    string _version;
    int _operationalState;
    int _administrativeState;
    string _installDate;
    long _currentUpTime;
    long _accumulatedUpTime;
    int _notifInQueue;
    int _notifForRetry;
    int _internalQueues;
    int _loadConfig;
    int _logLevel;
    int _mailboxPollerStatus;
};

#endif
