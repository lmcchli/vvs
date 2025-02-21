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

import java.util.Date;
import java.util.Calendar;

/**
 * The masServiceEnablerStatisticsConnectionsEntry family
 */
public final class k_masServiceEnablerStatisticsConnectionsEntry extends v_masServiceEnablerStatisticsConnectionsEntry {
    private V2Table serviceEnablerConnectionsTable = new V2Table();
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
		serviceEnablerConnectionsTable = new V2Table();
		for (int i = 0; i < attrs.serviceEnablers.size(); ++i) {
		    /* --- Construct a table entry --- */
		    MasMibServiceEnabler mmse = attrs.serviceEnablers.get(i);
		    for (int j = 0; j < mmse.connections.size(); ++j) {
			MasMibConnection mmc = mmse.connections.get(j);
			/* --- Construct a table entry --- */
			k_masServiceEnablerStatisticsConnectionsEntry mses = new k_masServiceEnablerStatisticsConnectionsEntry();

			/* --- Populate the new table entry --- */
			mses.mibObjects[I_masServiceEnablerStatisticsIndex] = new Integer32(i + 1);
			mses.mibObjects[I_masConnectionStatisticsIndex] = new Integer32(j + 1);
			mses.mibObjects[I_masConnectionStatisticsType] = new OctetString(mmc.connectionType.getInfo());
			mses.mibObjects[I_masConnectionStatisticsConnections] = new Integer32((int) mmc.currentConnections);
			mses.mibObjects[I_masConnectionStatisticsPeakConnections] = new Integer32((int) mmc.peakConnections);
			mses.mibObjects[I_masConnectionStatisticsPeakTime] = new DateAndTime(convertLongToDate(mmc.peakTime));
			mses.mibObjects[I_masConnectionStatisticsTotalConnections] = new Integer32((int) mmc.totalConnections);
			mses.mibObjects[I_masConnectionStatisticsAccumulatedConnections] = new Integer32((int) mmc.accumulatedConnections);

			/* --- Add the table entry to the table --- */
			serviceEnablerConnectionsTable.addRow(mses);
		    }
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
     *                    masServiceEnablerStatisticsConnectionsEntry object should be retrieved
     *                    from the application
     */
    public ObjectSyntax[] k_get(
	    int serialNum,
	    ContextInfo contextInfo,
	    int nominator,
	    int searchType,
	    ObjectSyntax[] index) {

	Integer32 masServiceEnablerStatisticsIndex = (Integer32) index[0];
	Integer32 masConnectionStatisticsIndex = (Integer32) index[1];

	k_masServiceEnablerStatisticsConnectionsEntry result;
	k_masServiceEnablerStatisticsConnectionsEntry mses = new k_masServiceEnablerStatisticsConnectionsEntry();

	/* --- Initialize if needed --- */
	k_initialize();
	if (cache == null) {
	    return null;
	}

	/* --- Populate the indices in the new table row --- */
	mses.mibObjects[I_masServiceEnablerStatisticsIndex] = masServiceEnablerStatisticsIndex;
	mses.mibObjects[I_masConnectionStatisticsIndex] = masConnectionStatisticsIndex;

	/* --- Extract the entry from the table --- */
	result = (k_masServiceEnablerStatisticsConnectionsEntry) serviceEnablerConnectionsTable.getRow(mses, searchType);

	/* --- Return the table row --- */
	if (result != null) {
	    result.setAllValid();
	    return result.mibObjects;
	}

	/* --- Otherwise nothing to return --- */
	return null;
    }   /* k_get() */

    private static Date convertLongToDate(Long milliSec) {
	Calendar cal = Calendar.getInstance();
	cal.setTimeInMillis(milliSec);
	return cal.getTime();
    }
}
