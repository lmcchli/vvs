#ifndef _NTF_TRAP_H
#define _NTF_TRAP_H
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

#ifdef  __cplusplus
extern "C" {
#endif

int
send_ntfStopped_trap(
    VarBind *add_vblist,
    ContextInfo *contextInfo);

int
i_send_ntfStopped_trap(
    OID             *ntfOperationalState,
    OID             *ntfAdministrativeState,
    VarBind         *add_vblist,
    ContextInfo     *contextInfo);

int
send_ntfStarted_trap(
    VarBind *add_vblist,
    ContextInfo *contextInfo);

int
i_send_ntfStarted_trap(
    OID             *ntfOperationalState,
    OID             *ntfAdministrativeState,
    VarBind         *add_vblist,
    ContextInfo     *contextInfo);

#ifdef  __cplusplus
	   }
#endif

#endif /*_NTF_TRAP_H*/
