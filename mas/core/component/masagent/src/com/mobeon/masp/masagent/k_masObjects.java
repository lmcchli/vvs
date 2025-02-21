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

import com.mobeon.masp.rpcclient.MasMibAttributes;
import com.snmp.agent.lib.ContextInfo;
import com.snmp.agent.lib.DoList;
import com.snmp.agent.lib.ObjectInfo;
import com.snmp.common.*;

/* --- Imports for SNMP agent specific objects --- */
import com.snmp.agent.lib.*;


/**
 * The masObjects family
 */
public final class k_masObjects extends v_masObjects {
    private MasMibAttributes cache = null;
    private MasConnection masConnection = null;
    /*
     * Initialization/termination functions for instrumentation.
     */

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
     *         masObjects object should be retrieved
     *         from the application
     */
    public ObjectSyntax[] k_get(
	    int serialNum,
	    ContextInfo contextInfo,
	    int nominator,
	    int searchType,
	    ObjectSyntax[] index) {

	MasMibAttributes attrs = masConnection.getValues();

	if (attrs == null) {
	    return null;
	}

	if (attrs != cache) {
	    cache = attrs;
	}

	int state = 0;

	this.mibObjects[I_masName] = new OctetString(attrs.masName);
	this.mibObjects[I_masVersion] = new OctetString(attrs.masVersion);
	switch (attrs.masOperationalState) {
	    case ENABLED:
		state = D_masOperationalState_enabled;
		break;
	    case DISABLED:
		state = D_masOperationalState_disabled;
		break;
	    case UNKNOWN:
		state = D_masOperationalState_disabled;
		break;
	}
	this.mibObjects[I_masOperationalState] = new Integer32(state);
	if (attrs.masAdministrativeState.equalsIgnoreCase("unlocked")) {
	    state = D_masAdministrativeState_unlocked;
	} else if (attrs.masAdministrativeState.equalsIgnoreCase("locked")) {
	    state = D_masAdministrativeState_locked;
	} else if (attrs.masAdministrativeState.equalsIgnoreCase("shutdown")) {
	    state = D_masAdministrativeState_shutdown;
	}
	this.mibObjects[I_masAdministrativeState] = new Integer32(state);
	this.mibObjects[I_masInstallDate] = new DateAndTime(attrs.masInstallDate);
	this.mibObjects[I_masCurrentUpTime] = new TimeTicks(attrs.masCurrentUpTime / 10);
	this.mibObjects[I_masAccumulatedUpTime] = new TimeTicks(attrs.masAccumulatedUpTime / 10);
	if (attrs.masReloadConfiguration == 1) {
	    state = D_masConfigurationState_load;
	} else if (attrs.masReloadConfiguration == 2) {
	    state = D_masConfigurationState_ok;
	} else if (attrs.masReloadConfiguration == 3) {
	    state = D_masConfigurationState_nok;
	} else if (attrs.masReloadConfiguration == 4) {
	    state = D_masConfigurationState_failed;
	}
	
	this.mibObjects[I_masConfigurationState] = new Integer32(state);
	this.mibObjects[I_masLastConfigurationUpdateTime] = new DateAndTime(attrs.masReloadConfigurationTime);
	this.mibObjects[I_masLastConfigurationUpdateTicks] = new TimeTicks((System.currentTimeMillis() - attrs.masReloadConfigurationTime.getTime()) / 10);

	if (nominator != -1) {
	    this.setAllValid();
	} else {
	    valid[I_masConfigurationState] = false;
	    valid[I_masAdministrativeState] = false;
	}

	return this.mibObjects;
    }   /* k_get() */


    /**
     *  The k_test method is called multiple times, once per MIB object,
     *  to test each value in an SNMP set request individually.
     *
     *  @param object specifies the MIB object's dispatch table entry.
     *
     *  @param value specifies the MIB object's value to test.
     *
     *  @param dp specifies the MIB object's DoList entry.
     *
     *  @param contextInfo specifies the context information.
     *
     *  @return the integer status resulting from the testing.
     */
    public int k_test(
        ObjectInfo    object,
        ObjectSyntax  value,
        DoList        dp,
        ContextInfo   contextInfo) {

        return SRSNMP.NO_ERROR;
    }   /* k_test() */


    /**
     *  The k_ready method is called once per DoList entry to test
     *  the consistency of all values in the SNMP set request.
     *
     *  @param doHead specifies the array of DoList entries for the MIB
     *         objects in the SNMP set request.
     *
     *  @param dp specifies the current DoList entry.
     *
     *  @return the integer status resulting from the testing.
     */
    public int k_ready(
        DoList[]      doHead,
        DoList        dp) {

        dp.setState(DoList.SR_ADD_MODIFY);
        return SRSNMP.NO_ERROR;
    }   /* k_ready() */


    /**
     *  The k_set method is called to set the SNMP MIB objects.
     *
     *  @param data specifies the MIB object values to set.
     *
     *  @param contextInfo specifies the context information.
     *
     *  @param function can be ignored.
     *
     *  @return the integer status resulting from the set.
     */
    public int k_set(
	    ObjectSyntax[] data,
	    ContextInfo contextInfo,
	    int function) {

	if (data == null) {
	    return SRSNMP.COMMIT_FAILED_ERROR;
	}

	if (data[I_masAdministrativeState] != null && valid[I_masAdministrativeState]) {
	    switch (((Integer32) data[I_masAdministrativeState]).getValue()) {
		case D_masAdministrativeState_locked:
		    masConnection.lock();
		    break;
		case D_masAdministrativeState_unlocked:
		    masConnection.unlock();
		    break;
		case D_masAdministrativeState_shutdown:
		    masConnection.shutdown();
		    break;
	    }
	}

	if (data[I_masConfigurationState] != null && valid[I_masConfigurationState]) {
	    if (((Integer32) data[I_masConfigurationState]).getValue() == D_masConfigurationState_load) {
		masConnection.reloadConfiguration();
	    }
	}
	return SRSNMP.NO_ERROR;
    }   /* k_set() */


    /**
     *  The k_undo method is called to roll back a failed SNMP set
     *  and restore the original MIB object values.
     *
     *  @param doHead specifies the array of DoList entries for the
     *         the SNMP set.
     *
     *  @param dp specifies the current DoList entry.
     *
     *  @param contextInfo specifies the context information.
     *
     *  @return the integer status resulting from the undo.
     */
    public int k_undo(
        DoList[]      doHead,
        DoList        dp,
        ContextInfo   contextInfo) {

        return SRSNMP.UNDO_FAILED_ERROR;
    }   /* k_undo() */

}
