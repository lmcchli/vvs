/*
 *
 * Copyright (C) 1992-2009 by SNMP Research, Incorporated.
 *
 * This software is furnished under a license and may be used and copied
 * only in accordance with the terms of such license and with the
 * inclusion of the above copyright notice. This software or any other
 * copies thereof may not be provided or otherwise made available to any
 * other person. No title to and ownership of the software is hereby
 * transferred.
 *
 * The information in this software is subject to change without notice
 * and should not be construed as a commitment by SNMP Research, Incorporated.
 *
 * Restricted Rights Legend:
 *  Use, duplication, or disclosure by the Government is subject to
 *  restrictions as set forth in subparagraph (c)(1)(ii) of the Rights
 *  in Technical Data and Computer Software clause at DFARS 252.227-7013;
 *  subparagraphs (c)(4) and (d) of the Commercial Computer
 *  Software-Restricted Rights Clause, FAR 52.227-19; and in similar
 *  clauses in the NASA FAR Supplement and other corresponding
 *  governmental regulations.
 *
 */

/*
 *                PROPRIETARY NOTICE
 *
 * This software is an unpublished work subject to a confidentiality agreement
 * and is protected by copyright and trade secret law.  Unauthorized copying,
 * redistribution or other use of this work is prohibited.
 *
 * The above notice of copyright on this source code product does not indicate
 * any actual or intended publication of such source code.
 */


/*
 * Arguments used to create this file:
 * -o ntf -agent -traps 
 */

#include "sr_conf.h"

#ifdef HAVE_STDIO_H
#include <stdio.h>
#endif /* HAVE_STDIO_H */
#ifdef HAVE_STDLIB_H
#include <stdlib.h>
#endif /* HAVE_STDLIB_H */
#ifdef HAVE_STRING_H
#include <string.h>
#endif /* HAVE_STRING_H */
#ifdef HAVE_MEMORY_H
#include <memory.h>
#endif /* HAVE_MEMORY_H */
#ifdef HAVE_STDDEF_H
#include <stddef.h>
#endif /* HAVE_STDDEF_H */
#include "sr_snmp.h"
#include "sr_trans.h"
#include "context.h"
#include "method.h"
#include "makevb.h"
#include "lookup.h"
#include "v2table.h"
#include "min_v.h"
#include "mibout.h"

#include "MIBAttribute.h"
#include "IPMSGuard.h"
#include "IPMSThread.h"
#include "Config.h"
#include "NtfInterface.h"
#include "NtfMib.h"
#include "NtfObject.h"
#include "def.h"

int
k_initialize(void) {
    return 1;
}

int
k_terminate(void) {
    return 1;
}

ntfObjects_t *
k_ntfObjects_get(int serialNum, ContextInfo *contextInfo,
                 int nominator) {

    if (NtfMib::instance()->checkUpdate()) {
        IPMSThread::sleep(Config::Instance()->getTimeOut());
    }

    IPMSGuard g(NtfMib::instance()->getMibMutex());

    NtfObject& obj = NtfMib::instance()->getNtfObject();

    if ("" == obj._name) {
        return NULL;
    }

    static ntfObjects_t entry;

    FreeOctetString(entry.ntfName);
    entry.ntfName =  MakeOctetStringFromText(obj._name.c_str());
    FreeOctetString(entry.ntfVersion);
    entry.ntfVersion =  MakeOctetStringFromText(obj._version.c_str());
    if (NtfMib::instance()->ntfNotAnsweringOnRequest()) {
        entry.ntfOperationalState = D_ntfOperationalState_disabled;
    } else {
        entry.ntfOperationalState = obj._operationalState;
    }
    entry.ntfAdministrativeState = obj._administrativeState;
    FreeOctetString(entry.ntfInstallDate);
    entry.ntfInstallDate = MakeOctetStringFromHex(obj._installDate.c_str());
    entry.ntfCurrentUpTime = obj._currentUpTime * 100;
    entry.ntfAccumulatedUpTime = obj._accumulatedUpTime * 100;
    entry.ntfNotifInQueue = obj._notifInQueue;
    entry.ntfNotifForRetry = obj._notifForRetry;
    entry.ntfInternalQueues = obj._internalQueues;
    entry.ntfLoadConfig = obj._loadConfig;
    entry.ntfLogLevel = obj._logLevel;
    entry.ntfMailboxPollerStatus = obj._mailboxPollerStatus;

    SET_ALL_VALID(entry.valid);
    return(&entry);
}

#ifdef SETS
int
k_ntfObjects_test(ObjectInfo *object, ObjectSyntax *value,
                  doList_t *dp, ContextInfo *contextInfo)
{

    return NO_ERROR;
}

int
k_ntfObjects_ready(ObjectInfo *object, ObjectSyntax *value, 
                   doList_t *doHead, doList_t *dp)
{

    dp->state = SR_ADD_MODIFY;
    return NO_ERROR;
}

int
k_ntfObjects_set(ntfObjects_t *data,
                 ContextInfo *contextInfo, int function) {
    
    if (VALID(I_ntfAdministrativeState, data->valid)){
        if(NtfInterface::instance()->setAdministrativeState(data->ntfAdministrativeState) != SUCCESSFUL)
            return INCONSISTENT_VALUE_ERROR;
    } else if (VALID(I_ntfLoadConfig, data->valid)) {
        if(NtfInterface::instance()->setLoadConfig(data->ntfLoadConfig) != SUCCESSFUL)
            return INCONSISTENT_VALUE_ERROR;
    } else if (VALID(I_ntfLogLevel, data->valid)) {
        if(NtfInterface::instance()->setLogLevel(data->ntfLogLevel) != SUCCESSFUL)
            return INCONSISTENT_VALUE_ERROR;
    } else {
    	return COMMIT_FAILED_ERROR;
	}
    return NO_ERROR;
}

#ifdef SR_ntfObjects_UNDO
/*
 * Add #define SR_ntfObjects_UNDO in sitedefs.h to
 * include the undo routine for the ntfObjects family.
 */
int
ntfObjects_undo(doList_t *doHead, doList_t *doCur,
                ContextInfo *contextInfo)
{
   return UNDO_FAILED_ERROR;
}
#endif /* SR_ntfObjects_UNDO */

#endif /* SETS */

ntfConsumedServiceEntry_t *
k_ntfConsumedServiceEntry_get(int serialNum, ContextInfo *contextInfo,
                              int nominator,
                              int searchType,
                              SR_INT32 ntfConsumedServiceIndex) {
	if (NtfMib::instance()->checkUpdate()) {
        IPMSThread::sleep(Config::Instance()->getTimeOut());
    }
    
    IPMSGuard g(NtfMib::instance()->getMibMutex());
	
    ConsumedService* cs = NtfMib::instance()->getConsumedService(ntfConsumedServiceIndex);
	
    if (0 == cs
        || EXACT == searchType && cs->_index != ntfConsumedServiceIndex) {
        return NULL;
    }

    static ntfConsumedServiceEntry_t entry;

    entry.ntfConsumedServiceIndex = cs->_index;
    FreeOctetString(entry.ntfConsumedServiceName);
    entry.ntfConsumedServiceName = MakeOctetStringFromText(cs->_name.c_str());
    entry.ntfConsumedServiceStatus = cs->_status;
    entry.ntfConsumedServiceTime = cs->_time * 100;
    entry.ntfConsumedServiceNumSuccess = cs->_numSuccess;
    entry.ntfConsumedServiceNumFailures = cs->_numFailures;

    SET_ALL_VALID(entry.valid);
    return(&entry);
}

ntfConsumedServiceInstancesEntry_t *
k_ntfConsumedServiceInstancesEntry_get(int serialNum, ContextInfo *contextInfo,
                                       int nominator,
                                       int searchType,
                                       SR_INT32 ntfConsumedServiceIndex,
                                       SR_INT32 ntfConsumedServiceInstancesIndex)
{



    if (NtfMib::instance()->checkUpdate()) {
        IPMSThread::sleep(Config::Instance()->getTimeOut());
    }
    
    IPMSGuard g(NtfMib::instance()->getMibMutex());

    ConsumedServiceInstance* csi = NtfMib::instance()->
        getConsumedServiceInstance(ntfConsumedServiceIndex, ntfConsumedServiceInstancesIndex);

    if (0 == csi
        || EXACT == searchType
        && (csi->_serviceIndex != ntfConsumedServiceIndex
            || csi->_instanceIndex != ntfConsumedServiceInstancesIndex)) {
        return NULL;
	}

    static ntfConsumedServiceInstancesEntry_t entry;

    entry.ntfConsumedServiceInstancesIndex = csi->_instanceIndex;

    FreeOctetString(entry.ntfConsumedServiceInstancesName);
    entry.ntfConsumedServiceInstancesName = MakeOctetStringFromText(csi->_name.c_str());
    entry.ntfConsumedServiceInstancesStatus = csi->_status;
    FreeOctetString(entry.ntfConsumedServiceInstancesHostName);
    entry.ntfConsumedServiceInstancesHostName = MakeOctetStringFromText(csi->_hostname.c_str());
    entry.ntfConsumedServiceInstancesPort = csi->_port;
    FreeOctetString(entry.ntfConsumedServiceInstancesZone);
    entry.ntfConsumedServiceInstancesZone = MakeOctetStringFromText(csi->_zone.c_str());
    entry.ntfConsumedServiceIndex = csi->_serviceIndex;
    
    SET_ALL_VALID(entry.valid);
    return(&entry);
}

commonAlarmsEntry_t *
k_commonAlarmsEntry_get(int serialNum, ContextInfo *contextInfo,
                        int nominator,
                        int searchType,
                        SR_INT32 alarmIndex)
{
	if (NtfMib::instance()->checkUpdate()) {
        IPMSThread::sleep(Config::Instance()->getTimeOut());
    }
    
    IPMSGuard g(NtfMib::instance()->getMibMutex());
	
    CommonAlarm* ca = NtfMib::instance()->getCommonAlarm(alarmIndex);
	
    if (0 == ca
        || EXACT == searchType && ca->_index != alarmIndex) {
        return NULL;
    }

    static commonAlarmsEntry_t entry;

    entry.alarmIndex = ca->_index;
    FreeOctetString(entry.alarmId);
    entry.alarmId = MakeOctetStringFromText(ca->_id.c_str());
    entry.alarmStatus = ca->_status;

    SET_ALL_VALID(entry.valid);
    return(&entry);
}
