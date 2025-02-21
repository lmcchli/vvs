/*
 *
 * Copyright (C) 1992-2006 by SNMP Research, Incorporated.
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
 * -o mas -agent -java -traps
 */

/* --- Imports for the Java language --- */

package com.mobeon.masp.masagent;

/* --- Imports for common SNMP objects --- */

import com.snmp.common.*;

/* --- Imports for SNMP agent specific objects --- */
import com.snmp.agent.lib.*;
import com.mobeon.masp.rpcclient.*;

/**
 * The masConsumedServiceEntry family
 */
public final class k_masConsumedServiceEntry extends v_masConsumedServiceEntry {
    private V2Table consumedServiceTable = new V2Table();
    private MasMibAttributes cache = null;
    private MasConnection masConnection = null;

    /*
     * Initialization/termination functions for instrumentation.
     */

    /**
     * The k_initialize() method is called to initialize the
     * instrumentation for the MIB object family.
     *
     * @return 1 on success, -1 on failure.
     */
    public int k_initialize() {
	if (masConnection == null) {
	    masConnection = MasConnection.getInstance();
	}
	MasMibAttributes attrs = masConnection.getValues();
	if (attrs != null) {
	    if (attrs != cache) {
		cache = attrs;
		consumedServiceTable = new V2Table();
		for (int i = 0; i < attrs.consumedServices.size(); ++i) {
		    /* --- Construct a table entry --- */
		    MasMibConsumedServices mmcs = attrs.consumedServices.get(i);
		    k_masConsumedServiceEntry mcse = new k_masConsumedServiceEntry();

		    mcse.mibObjects[I_masConsumedServiceIndex] = new Integer32(i + 1);
		    mcse.mibObjects[I_masConsumedServiceName] = new OctetString(mmcs.name);
		    switch (mmcs.status) {
			case UP:
			    mcse.mibObjects[I_masConsumedServiceStatus] = new Integer32(D_masConsumedServiceStatus_up);
			    break;
			case DOWN:
			    mcse.mibObjects[I_masConsumedServiceStatus] = new Integer32(D_masConsumedServiceStatus_down);
			    break;
			case IMPAIRED:
			    mcse.mibObjects[I_masConsumedServiceStatus] = new Integer32(D_masConsumedServiceStatus_impaired);
			    break;
		    }

		    mcse.mibObjects[I_masConsumedServiceTime] = new TimeTicks(mmcs.statusChangedTime / 10);
		    mcse.mibObjects[I_masConsumedServiceNumSuccess] = new Integer32((int) mmcs.successOperations);
		    mcse.mibObjects[I_masConsumedServiceNumFailures] = new Integer32((int) mmcs.failedOperations);

		    /* --- Add the table entry to the table --- */
		    consumedServiceTable.addRow(mcse);
		}
	    }
	} else {
	    cache = null;
	}
	return 1;
    }   /* k_initialize() */

    /**
     * The k_terminate() method is called to terminate the
     * instrumentation for the MIB object family.
     *
     * @return 1 on success, -1 on failure.
     */
    public int k_terminate() {
	return 1;
    }   /* k_terminate() */

    /*
		 * System dependent method routines
		 */

    /**
     * The k_get() method is called to retrieve
     * values from the Java application
     *
     * @param serialNum   specifies the serial number of the SNMP request
     * @param contextInfo provides context information
     * @param nominator   specifies which instance variable in the
     *                    masConsumedServiceEntry object should be retrieved
     *                    from the application
     */
    public ObjectSyntax[] k_get
	    (
		    int serialNum,
		    ContextInfo contextInfo,
		    int nominator,
		    int searchType,
		    ObjectSyntax[] index) {

	Integer32 masConsumedServiceIndex = (Integer32) index[0];
	k_masConsumedServiceEntry result;
	k_masConsumedServiceEntry mses = new k_masConsumedServiceEntry();

	/* --- Initialize if needed --- */
	k_initialize();
	if (cache == null) {
	    return null;
	}

	/* --- Populate the indices in the new table row --- */
	mses.mibObjects[I_masConsumedServiceIndex] = index[0];

	/* --- Extract the entry from the table --- */
	result = (k_masConsumedServiceEntry) consumedServiceTable.getRow(mses, searchType);

	/* --- Return the table row --- */
	if (result != null) {
	    result.setAllValid();
	    return result.mibObjects;
	}

	/* --- Otherwise nothing to return --- */
	return null;
    }   /* k_get() */

}
