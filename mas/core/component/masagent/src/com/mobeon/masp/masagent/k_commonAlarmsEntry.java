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
 * -java -o mas -agent -traps 
 */



/* --- Imports for the Java language --- */
package com.mobeon.masp.masagent;

import java.lang.*;

/* --- Imports for common SNMP objects --- */
import com.mobeon.masp.rpcclient.MasMibAttributes;
import com.mobeon.masp.rpcclient.MasMibCommonAlarm;
import com.mobeon.masp.rpcclient.MasMibProvidedServices;
import com.snmp.common.*;

/* --- Imports for SNMP agent specific objects --- */
import com.snmp.agent.lib.*;


/**
 * The commonAlarmsEntry family
 */
public final class k_commonAlarmsEntry extends v_commonAlarmsEntry {

    /*
     * Initialization/termination functions for instrumentation.
     */
    private V2Table commonAlarmTable = new V2Table();
    private MasMibAttributes cache = null;
    private MasConnection masConnection = null;
	
	
    /**
     *  The k_initialize() method is called to initialize the
     *  instrumentation for the MIB object family.
     *
     *  @return 1 on success, -1 on failure.
     */
    public int k_initialize() {
    	if (masConnection == null) {
    		masConnection = MasConnection.getInstance();
    	}
    	MasMibAttributes attrs = masConnection.getValues();
    	if (attrs != null) {
    		if (attrs != cache) {
    			cache = attrs;
    			commonAlarmTable = new V2Table();
    			for (int i = 0; i < attrs.commonAlarms.size(); ++i) {
    				/* --- Construct a table entry --- */
    				MasMibCommonAlarm mca = attrs.commonAlarms.get(i);
    				k_commonAlarmsEntry mcae = new k_commonAlarmsEntry();

    				mcae.mibObjects[I_alarmIndex] = new Integer32(i + 1);
    				mcae.mibObjects[I_alarmId] = new OctetString(mca.id);
    				mcae.mibObjects[I_alarmStatus] = new Integer32(mca.status);

    				/* --- Add the table entry to the table --- */
    				commonAlarmTable.addRow(mcae);
    			}
    		}
    	} else {
    		cache = null;
    	}
    	return 1;
    }   /* k_initialize() */

    /**
     *  The k_terminate() method is called to terminate the
     *  instrumentation for the MIB object family.
     *
     *  @return 1 on success, -1 on failure.
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
     * @param serialNum specifies the serial number of the SNMP request
     *
     * @param contextInfo provides context information
     *
     * @param nominator specifies which instance variable in the
     *         commonAlarmsEntry object should be retrieved
     *         from the application
     */
    public ObjectSyntax[] k_get(
        int serialNum,
        ContextInfo contextInfo,
        int nominator,
        int searchType,
        ObjectSyntax[] index) {

    	k_commonAlarmsEntry result;
    	k_commonAlarmsEntry cae = new k_commonAlarmsEntry();

    	/* --- Initialize if needed --- */
    	k_initialize();
    	if (cache == null) {
    	    return null;
    	}

    	/* --- Populate the indices in the new table row --- */
    	cae.mibObjects[I_alarmIndex] = index[0];

    	/* --- Extract the entry from the table --- */
    	result = (k_commonAlarmsEntry) commonAlarmTable.getRow(cae, searchType);

    	/* --- Return the table row --- */
    	if (result != null) {
    	    result.setAllValid();
    	    return result.mibObjects;
    	}

    	/* --- Otherwise nothing to return --- */
    	return null;
     }   /* k_get() */

}
