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
 * -o ntf -c++ -agent -traps 
 */

#include "sr_conf.h"

#ifdef HAVE_STDIO_H
#include <stdio.h>
#endif /* HAVE_STDIO_H */

#ifdef HAVE_STDLIB_H
#include <stdlib.h>
#endif	/* HAVE_STDLIB_H */

#ifdef HAVE_MALLOC_H
#include <malloc.h>
#endif	/* HAVE_MALLOC_H */

#ifdef HAVE_STRING_H
#include <string.h>
#endif	/* HAVE_STRING_H */

#ifdef HAVE_MEMORY_H
#include <memory.h>
#endif	/* HAVE_MEMORY_H */

#ifdef HAVE_STDDEF_H
#include <stddef.h>
#endif /* HAVE_STDDEF_H */
#include "sr_snmp.h"
#include "sr_trans.h"
#include "context.h"
#include "method.h"
#include "makevb.h"
#include "inst_lib.h"
#include "oid_lib.h"
#include "lookup.h"
#include "v2table.h"
#include "diag.h"
SR_FILENAME
#include "min_v.h"
#include "mibout.h"

const SnmpType ntfObjectsTypeTable[] = {
    { OCTET_PRIM_TYPE, SR_READ_ONLY, offsetof(ntfObjects_t, ntfName), -1 },
    { OCTET_PRIM_TYPE, SR_READ_ONLY, offsetof(ntfObjects_t, ntfVersion), -1 },
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfObjects_t, ntfOperationalState), -1 },
    { INTEGER_TYPE, SR_READ_WRITE, offsetof(ntfObjects_t, ntfAdministrativeState), -1 },
    { OCTET_PRIM_TYPE, SR_READ_ONLY, offsetof(ntfObjects_t, ntfInstallDate), -1 },
    { TIME_TICKS_TYPE, SR_READ_ONLY, offsetof(ntfObjects_t, ntfCurrentUpTime), -1 },
    { TIME_TICKS_TYPE, SR_READ_ONLY, offsetof(ntfObjects_t, ntfAccumulatedUpTime), -1 },
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfObjects_t, ntfNotifInQueue), -1 },
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfObjects_t, ntfNotifForRetry), -1 },
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfObjects_t, ntfInternalQueues), -1 },
    { INTEGER_TYPE, SR_READ_WRITE, offsetof(ntfObjects_t, ntfLoadConfig), -1 },
    { INTEGER_TYPE, SR_READ_WRITE, offsetof(ntfObjects_t, ntfLogLevel), -1 },
    { INTEGER_TYPE, SR_READ_WRITE, offsetof(ntfObjects_t, ntfMailboxPollerStatus), -1 },
    { -1, -1, (unsigned short) -1, -1 }
};

const SrGetInfoEntry ntfObjectsGetInfo = {
    (SR_KGET_FPTR) new_k_ntfObjects_get,
    (SR_FREE_FPTR) NULL,
    (int) sizeof(ntfObjects_t),
    I_ntfObjects_max,
    (SnmpType *) ntfObjectsTypeTable,
    NULL,
    (short) offsetof(ntfObjects_t, valid)
};

/*---------------------------------------------------------------------
 * Retrieve data from the ntfObjects family.
 *---------------------------------------------------------------------*/
VarBind *
ntfObjects_get(OID *incoming, ObjectInfo *object, int searchType,
               ContextInfo *contextInfo, int serialNum)
{
    return (v_get(incoming, object, searchType, contextInfo, serialNum,
                  (SrGetInfoEntry *) &ntfObjectsGetInfo));
}

ntfObjects_t *
new_k_ntfObjects_get(int serialNum, ContextInfo *contextInfo,
                     int nominator, int searchType,
                     ntfObjects_t *data)
{
    return k_ntfObjects_get(serialNum, contextInfo, nominator);
}

#ifdef SETS 

#ifdef __cplusplus
extern "C" {
#endif
static int ntfObjects_cleanup(doList_t *trash);
#ifdef __cplusplus
}
#endif

/*
 * Syntax refinements for the ntfObjects family
 *
 * For each object in this family in which the syntax clause in the MIB
 * defines a refinement to the size, range, or enumerations, initialize
 * a data structure with these refinements.
 */
static RangeTest_t   ntfAdministrativeState_range[] = { { 1, 3 } };
static RangeTest_t   ntfLoadConfig_range[] = { { 1, 2 } };
static RangeTest_t   ntfLogLevel_range[] = { { 0, 3 } };
static RangeTest_t   ntfMailboxPollerStatus_range[] = { { 1, 2 } };

/*
 * Initialize the sr_member_test array with one entry per object in the
 * ntfObjects family.
 */
static struct SrTestInfoEntry::sr_member_test ntfObjects_member_test[] =
{
    /* ntfName */
    { MINV_NOT_WRITABLE, 0, NULL, NULL },

    /* ntfVersion */
    { MINV_NOT_WRITABLE, 0, NULL, NULL },

    /* ntfOperationalState */
    { MINV_NOT_WRITABLE, 0, NULL, NULL },

    /* ntfAdministrativeState */
    { MINV_INTEGER_RANGE_TEST, 
      sizeof(ntfAdministrativeState_range)/sizeof(RangeTest_t), /* 3 */
      ntfAdministrativeState_range, NULL },

    /* ntfInstallDate */
    { MINV_NOT_WRITABLE, 0, NULL, NULL },

    /* ntfCurrentUpTime */
    { MINV_NOT_WRITABLE, 0, NULL, NULL },

    /* ntfAccumulatedUpTime */
    { MINV_NOT_WRITABLE, 0, NULL, NULL },

    /* ntfNotifInQueue */
    { MINV_NOT_WRITABLE, 0, NULL, NULL },

    /* ntfNotifForRetry */
    { MINV_NOT_WRITABLE, 0, NULL, NULL },

    /* ntfInternalQueues */
    { MINV_NOT_WRITABLE, 0, NULL, NULL },

    /* ntfLoadConfig */
    { MINV_INTEGER_RANGE_TEST, 
      sizeof(ntfLoadConfig_range)/sizeof(RangeTest_t), /* 2 */
      ntfLoadConfig_range, NULL },

    /* ntfLogLevel */
    { MINV_INTEGER_RANGE_TEST, 
      sizeof(ntfLogLevel_range)/sizeof(RangeTest_t), /* 4 */
      ntfLogLevel_range, NULL },

    /* ntfMailboxPollerStatus */
    { MINV_INTEGER_RANGE_TEST, 
      sizeof(ntfMailboxPollerStatus_range)/sizeof(RangeTest_t), /* 2 */
      ntfMailboxPollerStatus_range, NULL }
};

/*
 * Initialize SrTestInfoEntry for the ntfObjects family.
 */
const SrTestInfoEntry ntfObjectsTestInfo = {
    &ntfObjectsGetInfo,
    (SrTestInfoEntry::sr_member_test*const) ntfObjects_member_test,
    NULL,
    k_ntfObjects_test,
    k_ntfObjects_ready,
#ifdef SR_ntfObjects_UNDO
    ntfObjects_undo,
#else /* SR_ntfObjects_UNDO */
    NULL,
#endif /* SR_ntfObjects_UNDO */
    ntfObjects_ready,
    ntfObjects_set,
    ntfObjects_cleanup,
    (SR_COPY_FPTR) NULL
};

/*----------------------------------------------------------------------
 * cleanup after ntfObjects set/undo
 *---------------------------------------------------------------------*/
static int
ntfObjects_cleanup(doList_t *trash)
{
    return SrCleanup(trash, &ntfObjectsTestInfo);
}

#ifdef SR_ntfObjects_UNDO
/*----------------------------------------------------------------------
 * clone the ntfObjects family
 *---------------------------------------------------------------------*/
ntfObjects_t *
Clone_ntfObjects(ntfObjects_t *ntfObjects)
{
    /* Clone function is not used by auto-generated */
    /* code, but may be used by user code */
    return (ntfObjects_t *)SrCloneFamily(ntfObjects,
                         ntfObjectsGetInfo.family_size,
                         ntfObjectsGetInfo.type_table,
                         ntfObjectsGetInfo.highest_nominator,
                         ntfObjectsGetInfo.valid_offset,
                         ntfObjectsTestInfo.userpart_clone_func,
                         ntfObjectsGetInfo.userpart_free_func);
}

#endif /* defined(SR_ntfObjects_UNDO) */
/*---------------------------------------------------------------------
 * Determine if this SET request is valid. If so, add it to the do-list.
 *---------------------------------------------------------------------*/
int 
ntfObjects_test(OID *incoming, ObjectInfo *object, ObjectSyntax *value,
                doList_t *doHead, doList_t *doCur, ContextInfo *contextInfo)
{
    return v_test(incoming, object, value, doHead, doCur, contextInfo,
                  &ntfObjectsTestInfo);
}

/*---------------------------------------------------------------------
 * Determine if entries in this SET request are consistent
 *---------------------------------------------------------------------*/
int 
ntfObjects_ready(doList_t *doHead, doList_t *doCur, ContextInfo *contextInfo)
{
    return v_ready(doHead, doCur, contextInfo,
                  &ntfObjectsTestInfo);
}

/*---------------------------------------------------------------------
 * Perform the kernel-specific set function for this group of
 * related objects.
 *---------------------------------------------------------------------*/
int 
ntfObjects_set(doList_t *doHead, doList_t *doCur, ContextInfo *contextInfo)
{
  return (k_ntfObjects_set((ntfObjects_t *) (doCur->data),
            contextInfo, doCur->state));
}

#endif /* SETS */


const SnmpType commonAlarmsEntryTypeTable[] = {
    { INTEGER_TYPE, SR_NOT_ACCESSIBLE, offsetof(commonAlarmsEntry_t, alarmIndex), 0 },
    { OCTET_PRIM_TYPE, SR_READ_ONLY, offsetof(commonAlarmsEntry_t, alarmId), -1 },
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(commonAlarmsEntry_t, alarmStatus), -1 },
    { -1, -1, (unsigned short) -1, -1 }
};

const SrIndexInfo commonAlarmsEntryIndexInfo[] = {
#ifdef I_alarmIndex
    { I_alarmIndex, T_uint, -1 },
#endif /* I_alarmIndex */
    { -1, -1, -1 }
};

const SrGetInfoEntry commonAlarmsEntryGetInfo = {
    (SR_KGET_FPTR) new_k_commonAlarmsEntry_get,
    (SR_FREE_FPTR) NULL,
    (int) sizeof(commonAlarmsEntry_t),
    I_commonAlarmsEntry_max,
    (SnmpType *) commonAlarmsEntryTypeTable,
    commonAlarmsEntryIndexInfo,
    (short) offsetof(commonAlarmsEntry_t, valid)
};

/*---------------------------------------------------------------------
 * Retrieve data from the commonAlarmsEntry family.
 *---------------------------------------------------------------------*/
VarBind *
commonAlarmsEntry_get(OID *incoming, ObjectInfo *object, int searchType,
                      ContextInfo *contextInfo, int serialNum)
{
#if !defined(I_alarmIndex)
    return NULL;
#else /* all indices are supported */
    return (v_get(incoming, object, searchType, contextInfo, serialNum,
                  (SrGetInfoEntry *) &commonAlarmsEntryGetInfo));
#endif /* all indices are supported */
}

commonAlarmsEntry_t *
new_k_commonAlarmsEntry_get(int serialNum, ContextInfo *contextInfo,
                            int nominator, int searchType,
                            commonAlarmsEntry_t *data)
{
    if (data == NULL) {
        return NULL;
    }
    return k_commonAlarmsEntry_get(serialNum, contextInfo, nominator,
                                   searchType, data->alarmIndex);
}

const SnmpType ntfConsumedServiceEntryTypeTable[] = {
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceEntry_t, ntfConsumedServiceIndex), 0 },
    { OCTET_PRIM_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceEntry_t, ntfConsumedServiceName), -1 },
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceEntry_t, ntfConsumedServiceStatus), -1 },
    { TIME_TICKS_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceEntry_t, ntfConsumedServiceTime), -1 },
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceEntry_t, ntfConsumedServiceNumSuccess), -1 },
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceEntry_t, ntfConsumedServiceNumFailures), -1 },
    { -1, -1, (unsigned short) -1, -1 }
};

const SrIndexInfo ntfConsumedServiceEntryIndexInfo[] = {
#ifdef I_ntfConsumedServiceIndex
    { I_ntfConsumedServiceIndex, T_uint, -1 },
#endif /* I_ntfConsumedServiceIndex */
    { -1, -1, -1 }
};

const SrGetInfoEntry ntfConsumedServiceEntryGetInfo = {
    (SR_KGET_FPTR) new_k_ntfConsumedServiceEntry_get,
    (SR_FREE_FPTR) NULL,
    (int) sizeof(ntfConsumedServiceEntry_t),
    I_ntfConsumedServiceEntry_max,
    (SnmpType *) ntfConsumedServiceEntryTypeTable,
    ntfConsumedServiceEntryIndexInfo,
    (short) offsetof(ntfConsumedServiceEntry_t, valid)
};

/*---------------------------------------------------------------------
 * Retrieve data from the ntfConsumedServiceEntry family.
 *---------------------------------------------------------------------*/
VarBind *
ntfConsumedServiceEntry_get(OID *incoming, ObjectInfo *object, int searchType,
                            ContextInfo *contextInfo, int serialNum)
{
#if !defined(I_ntfConsumedServiceIndex)
    return NULL;
#else /* all indices are supported */
    return (v_get(incoming, object, searchType, contextInfo, serialNum,
                  (SrGetInfoEntry *) &ntfConsumedServiceEntryGetInfo));
#endif /* all indices are supported */
}

ntfConsumedServiceEntry_t *
new_k_ntfConsumedServiceEntry_get(int serialNum, ContextInfo *contextInfo,
                                  int nominator, int searchType,
                                  ntfConsumedServiceEntry_t *data)
{
    if (data == NULL) {
        return NULL;
    }
    return k_ntfConsumedServiceEntry_get(serialNum, contextInfo, nominator,
                                         searchType,
                                         data->ntfConsumedServiceIndex);
}

const SnmpType ntfConsumedServiceInstancesEntryTypeTable[] = {
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceInstancesEntry_t, ntfConsumedServiceInstancesIndex), 1 },
    { OCTET_PRIM_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceInstancesEntry_t, ntfConsumedServiceInstancesName), -1 },
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceInstancesEntry_t, ntfConsumedServiceInstancesStatus), -1 },
    { OCTET_PRIM_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceInstancesEntry_t, ntfConsumedServiceInstancesHostName), -1 },
    { INTEGER_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceInstancesEntry_t, ntfConsumedServiceInstancesPort), -1 },
    { OCTET_PRIM_TYPE, SR_READ_ONLY, offsetof(ntfConsumedServiceInstancesEntry_t, ntfConsumedServiceInstancesZone), -1 },
    { INTEGER_TYPE, SR_NOT_ACCESSIBLE, offsetof(ntfConsumedServiceInstancesEntry_t, ntfConsumedServiceIndex), 0 },
    { -1, -1, (unsigned short) -1, -1 }
};

const SrIndexInfo ntfConsumedServiceInstancesEntryIndexInfo[] = {
#ifdef I_ntfConsumedServiceInstancesEntryIndex_ntfConsumedServiceIndex
    { I_ntfConsumedServiceInstancesEntryIndex_ntfConsumedServiceIndex, T_uint, -1 },
#endif /* I_ntfConsumedServiceInstancesEntryIndex_ntfConsumedServiceIndex */
#ifdef I_ntfConsumedServiceInstancesIndex
    { I_ntfConsumedServiceInstancesIndex, T_uint, -1 },
#endif /* I_ntfConsumedServiceInstancesIndex */
    { -1, -1, -1 }
};

const SrGetInfoEntry ntfConsumedServiceInstancesEntryGetInfo = {
    (SR_KGET_FPTR) new_k_ntfConsumedServiceInstancesEntry_get,
    (SR_FREE_FPTR) NULL,
    (int) sizeof(ntfConsumedServiceInstancesEntry_t),
    I_ntfConsumedServiceInstancesEntry_max,
    (SnmpType *) ntfConsumedServiceInstancesEntryTypeTable,
    ntfConsumedServiceInstancesEntryIndexInfo,
    (short) offsetof(ntfConsumedServiceInstancesEntry_t, valid)
};

/*---------------------------------------------------------------------
 * Retrieve data from the ntfConsumedServiceInstancesEntry family.
 *---------------------------------------------------------------------*/
VarBind *
ntfConsumedServiceInstancesEntry_get(OID *incoming, ObjectInfo *object, int searchType,
                                     ContextInfo *contextInfo, int serialNum)
{
#if !defined(I_ntfConsumedServiceInstancesEntryIndex_ntfConsumedServiceIndex) || !defined(I_ntfConsumedServiceInstancesIndex)
    return NULL;
#else /* all indices are supported */
    return (v_get(incoming, object, searchType, contextInfo, serialNum,
                  (SrGetInfoEntry *) &ntfConsumedServiceInstancesEntryGetInfo));
#endif /* all indices are supported */
}

ntfConsumedServiceInstancesEntry_t *
new_k_ntfConsumedServiceInstancesEntry_get(int serialNum, ContextInfo *contextInfo,
                                           int nominator, int searchType,
                                           ntfConsumedServiceInstancesEntry_t *data)
{
    if (data == NULL) {
        return NULL;
    }
    return k_ntfConsumedServiceInstancesEntry_get(serialNum, contextInfo, nominator,
                                                  searchType,
                                                  data->ntfConsumedServiceIndex,
                                                  data->ntfConsumedServiceInstancesIndex);
}

