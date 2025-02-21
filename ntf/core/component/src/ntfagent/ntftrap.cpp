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
#ifdef HAVE_MEMORY_H
#include <memory.h>
#endif /* HAVE_MEMORY_H */
#include "sr_snmp.h"
#include "diag.h"
#include "sr_trans.h"
#include "context.h"
#include "method.h"
#include "mibout.h"
#include "trap.h"
#include "getvar.h"

SR_FILENAME

#include "ntftrap.h"

static VarBind *ntf_GetTrapVar(
    OID *object,
    OID *inst,
    ContextInfo *contextInfo);

static VarBind *
ntf_GetTrapVar(
    OID *object,
    OID *inst,
    ContextInfo *contextInfo)
{
    OID *var;
    VarBind *vb = NULL;

    if ((object == NULL) || (inst == NULL)) {
        return NULL;
    }
    var = CatOID(object, inst);
    FreeOID(object);
    FreeOID(inst);
    if (var == NULL) {
        return NULL;
    }
    vb = i_GetVar(contextInfo, EXACT, var);
    if (vb == NULL) {
        FreeOID(var);
        return NULL;
    }
#ifdef SR_SNMPv2_PDU
    if ((vb->value.type == NO_SUCH_OBJECT_EXCEPTION) ||
        (vb->value.type == NO_SUCH_INSTANCE_EXCEPTION)) {
        FreeOID(var);
        FreeVarBindList(vb);
        return NULL;
    }
#endif /* SR_SNMPv2_PDU */
    FreeOID(var);
    return vb;
}

int
send_ntfStopped_trap(
    VarBind *add_vblist,
    ContextInfo *contextInfo)
{
    OID *ntfOperationalState;
    OID *ntfAdministrativeState;
    ntfOperationalState = MakeOIDFragFromDot("0");

    ntfAdministrativeState = MakeOIDFragFromDot("0");

    int retval = i_send_ntfStopped_trap(ntfOperationalState, ntfAdministrativeState, add_vblist, contextInfo);

    return retval;
}

int
i_send_ntfStopped_trap(
    OID             *ntfOperationalState,
    OID             *ntfAdministrativeState,
    VarBind         *add_vblist,
    ContextInfo     *contextInfo)
{
    OID             *enterprise = NULL;
    OID             *tmp_oid = NULL;
    VarBind         *vb = NULL;
    VarBind         *temp_vb = NULL;
    
    void            *admState_val;
    void            *opState_val;

    int             admState_tmp;
    int             opState_tmp;
    
    if ((ntfOperationalState == NULL) ||
        (ntfAdministrativeState == NULL)) {
        FreeOID(ntfOperationalState);
        FreeOID(ntfAdministrativeState);
        DPRINTF((APTRAP, "i_send_ntfStopped_trap: "));
        DPRINTF((APTRAP, "At least one incoming OID is NULL\n"));
        return -1;
    }

    /* 1.3.6.1.4.1.193.41.3.9.1.4 = ntfAdministrativeState 
    temp_vb = ntf_GetTrapVar(MakeOIDFromDot("1.3.6.1.4.1.193.41.3.9.1.4"),
    ntfAdministrativeState, contextInfo);*/

    admState_tmp = (int)1;
    admState_val = (void *)&admState_tmp;
    tmp_oid = MakeOIDFromDot("1.3.6.1.4.1.193.41.3.9.1.4");
    temp_vb = MakeVarBindWithValue(tmp_oid, (OID *) NULL, INTEGER_TYPE, admState_val);

    if (temp_vb == NULL) {
        DPRINTF((APTRAP, "i_send_ntfStopped_trap: "));
        DPRINTF((APTRAP, "ntf_GetTrapVar() failed\n"));
	FreeOID(ntfOperationalState);
        FreeOID(ntfAdministrativeState);
        return -1;
    }
    FreeOID(tmp_oid);
    
    temp_vb->next_var = vb;
    vb = temp_vb;

    /* 1.3.6.1.4.1.193.41.3.9.1.3 = ntfOperationalState
    temp_vb = ntf_GetTrapVar(MakeOIDFromDot("1.3.6.1.4.1.193.41.3.9.1.3"),
    ntfOperationalState, contextInfo);*/

    opState_tmp = (int)1;
    opState_val = (void *)&opState_tmp;
    tmp_oid = MakeOIDFromDot("1.3.6.1.4.1.193.41.3.9.1.3");
    temp_vb = MakeVarBindWithValue(tmp_oid, (OID *) NULL, INTEGER_TYPE, opState_val);
    if (temp_vb == NULL) {
        if (vb != NULL) {
            FreeVarBindList(vb);
        }
        DPRINTF((APTRAP, "i_send_ntfStopped_trap: "));
        DPRINTF((APTRAP, "ntf_GetTrapVar() failed\n"));
	FreeOID(ntfOperationalState);
	FreeOID(ntfAdministrativeState);
	FreeOID(tmp_oid);
        return -1;
    }
    FreeOID(tmp_oid);
    
    temp_vb->next_var = vb;
    vb = temp_vb;

    /* "1.3.6.1.4.1.193.41.3.9.2.2" = ntfEvents.2 */
    DPRINTF((APTRAP, "Sending ntfStopped Trap\n"));
    do_trap(6, 0, vb, enterprise, const_cast<char *>("1.3.6.1.4.1.193.41.3.9.2.2"));
    FreeOID(ntfOperationalState);
    FreeOID(ntfAdministrativeState);
    if (enterprise != NULL)
    FreeOID(enterprise);
    return 0;
}

int
send_ntfStarted_trap(
    VarBind *add_vblist,
    ContextInfo *contextInfo)
{
    OID *ntfOperationalState;
    OID *ntfAdministrativeState;
    ntfOperationalState = MakeOIDFragFromDot("0");

    ntfAdministrativeState = MakeOIDFragFromDot("0");

    int retval = i_send_ntfStarted_trap(ntfOperationalState, ntfAdministrativeState, add_vblist, contextInfo);
    
    return retval;
}

int
i_send_ntfStarted_trap(
    OID             *ntfOperationalState,
    OID             *ntfAdministrativeState,
    VarBind         *add_vblist,
    ContextInfo     *contextInfo)
{
    OID             *enterprise = NULL;
    OID             *tmp_oid = NULL;
    VarBind         *vb = NULL;
    VarBind         *temp_vb = NULL;
    
    void            *admState_val;
    void            *opState_val;
 
    int             admState_tmp;
    int             opState_tmp;
    
    if ((ntfOperationalState == NULL) ||
        (ntfAdministrativeState == NULL)) {
        FreeOID(ntfOperationalState);
        FreeOID(ntfAdministrativeState);
        DPRINTF((APTRAP, "i_send_ntfStarted_trap: "));
        DPRINTF((APTRAP, "At least one incoming OID is NULL\n"));
        return -1;
    }

    /* 1.3.6.1.4.1.193.41.3.9.1.4 = ntfAdministrativeState
    temp_vb = ntf_GetTrapVar(MakeOIDFromDot("1.3.6.1.4.1.193.41.3.9.1.4"),
    ntfAdministrativeState, contextInfo);*/

    admState_tmp = (int)1;
    admState_val = (void *)&admState_tmp;
    tmp_oid = MakeOIDFromDot("1.3.6.1.4.1.193.41.3.9.1.4");
    temp_vb = MakeVarBindWithValue(tmp_oid, (OID *) NULL, INTEGER_TYPE, admState_val);
    if (temp_vb == NULL) {
        DPRINTF((APTRAP, "i_send_ntfStarted_trap: "));
        DPRINTF((APTRAP, "ntf_GetTrapVar() failed\n"));
	FreeOID(ntfOperationalState);
        FreeOID(ntfAdministrativeState);
	FreeOID(tmp_oid);
        return -1;
    }
    
    FreeOID(tmp_oid);

    temp_vb->next_var = vb;
    vb = temp_vb;

    /* 1.3.6.1.4.1.193.41.3.9.1.3 = ntfOperationalState
    temp_vb = ntf_GetTrapVar(MakeOIDFromDot("1.3.6.1.4.1.193.41.3.9.1.3"),
    ntfOperationalState, contextInfo);*/

    opState_tmp = (int)1;
    opState_val = (void *)&opState_tmp;
    tmp_oid = MakeOIDFromDot("1.3.6.1.4.1.193.41.3.9.1.3");
    temp_vb = MakeVarBindWithValue(tmp_oid, (OID *) NULL, INTEGER_TYPE, opState_val);
    if (temp_vb == NULL) {
        if (vb != NULL) {
            FreeVarBindList(vb);
        }
        DPRINTF((APTRAP, "i_send_ntfStarted_trap: "));
        DPRINTF((APTRAP, "ntf_GetTrapVar() failed\n"));
	FreeOID(ntfOperationalState);
        FreeOID(ntfAdministrativeState);
	FreeOID(tmp_oid);
        return -1;
    }
    FreeOID(tmp_oid);
    
    temp_vb->next_var = vb;
    vb = temp_vb;

    /* "1.3.6.1.4.1.193.41.3.9.2.1" = ntfEvents.1 */
    DPRINTF((APTRAP, "Sending ntfStarted Trap\n"));
    do_trap(6, 0, vb, enterprise, const_cast<char *>("1.3.6.1.4.1.193.41.3.9.2.1"));
    FreeOID(ntfOperationalState);
    FreeOID(ntfAdministrativeState);
    if (enterprise != NULL)
    FreeOID(enterprise);
    return 0;
}

