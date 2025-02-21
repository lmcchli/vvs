/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#include "NtfObject.h"
#include "ntfdefs.h"

NtfObject::NtfObject():
    _name(""),
    _version(""),
    _operationalState(D_ntfOperationalState_enabled),
    _administrativeState(D_ntfAdministrativeState_unlocked),
    _installDate(""),
    _currentUpTime(0),
    _accumulatedUpTime(0),
    _notifInQueue(0),
    _notifForRetry(0),
    _internalQueues(0),
    _loadConfig(D_ntfLoadConfig_inactive),
    _logLevel(D_ntfLogLevel_error),
    _mailboxPollerStatus(D_ntfMailboxPollerStatus_active) {
};
