package com.mobeon.masp.masagent;

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
import java.lang.*;

/* --- Imports for common SNMP objects --- */
import com.snmp.common.*;

/* --- Imports for SNMP agent specific objects --- */
import com.snmp.agent.lib.*;


/**
 *  The masOID class defines the MIB objects supported
 *
 *  @author    SNMP Research, Inc.
 *  @version   1.0
 */
public class masOID implements OidList {

    /*
     *  Define an array to hold the list of SNMP MIB object information.
     *
     *  By default, each instantiaton of this class will construct a new
     *  list of SNMP MIB object information.  This means that multiple
     *  subagents running in the same JVM will access different instances
     *  of the method routines.
     *
     *  Add the "static" keyword to the array below so that all subagents
     *  running in the same JVM will access the _same_ instance of the
     *  method routines.
     */
    private ObjectInfo oiArray[];

    /*
     *  Define an array to hold the list of SNMP MIB object families.
     *  This is used to call the k_initialize() and k_terminate() methods
     *  of each family.
     *
     *  By default, each instantiaton of this class will construct a new
     *  list of SNMP MIB object information.  This means that multiple
     *  subagents running in the same JVM will access different instances
     *  of the k_initialize() and k_terminate() routines.
     *
     *  Add the "static" keyword to the array below so that all subagents
     *  running in the same JVM will access the _same_ instance of the
     *  k_initialize() and k_terminate() routines.
     */
    private MethodRoutine mrArray[];


    /**
     *  Constructs a new OidList.
     */
    public masOID() {

        /*
         *  Return if the ObjectInfo and MethodRoutine arrays have been
         *  initialized.  This check allows the arrays to be either class
         *  or instance variables.
         */
        if ((oiArray != null) && (mrArray != null)) {
            return;
        }

        /* -- Construct an array to hold the SNMP MIB object information -- */
        oiArray = new ObjectInfo[37];

        /* -- Construct an array to hold the SNMP MIB object families -- */
        mrArray = new MethodRoutine[6];

        /* --- Construct one Java object for each MIB object family --- */
        k_masObjects masObjectsFamilyObject = 
            new k_masObjects();
        k_masServiceEnablerStatisticsEntry masServiceEnablerStatisticsEntryFamilyObject = 
            new k_masServiceEnablerStatisticsEntry();
        k_masServiceEnablerStatisticsConnectionsEntry masServiceEnablerStatisticsConnectionsEntryFamilyObject = 
            new k_masServiceEnablerStatisticsConnectionsEntry();
        k_commonAlarmsEntry commonAlarmsEntryFamilyObject = 
            new k_commonAlarmsEntry();
        k_masProvidedServiceEntry masProvidedServiceEntryFamilyObject = 
            new k_masProvidedServiceEntry();
        k_masConsumedServiceEntry masConsumedServiceEntryFamilyObject = 
            new k_masConsumedServiceEntry();

        /* --- Populate the MethodRoutine array --- */
        mrArray[0] = masObjectsFamilyObject;
        mrArray[1] = masServiceEnablerStatisticsEntryFamilyObject;
        mrArray[2] = masServiceEnablerStatisticsConnectionsEntryFamilyObject;
        mrArray[3] = commonAlarmsEntryFamilyObject;
        mrArray[4] = masProvidedServiceEntryFamilyObject;
        mrArray[5] = masConsumedServiceEntryFamilyObject;

        /* --- Populate the ObjectInfo array --- */
        oiArray[0] = new ObjectInfo(
            "masName",
            "1.3.6.1.4.1.24261.1.1.1.1",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masObjects.I_masName,
            32,
            masObjectsFamilyObject);

        oiArray[1] = new ObjectInfo(
            "masVersion",
            "1.3.6.1.4.1.24261.1.1.1.2",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masObjects.I_masVersion,
            32,
            masObjectsFamilyObject);

        oiArray[2] = new ObjectInfo(
            "masOperationalState",
            "1.3.6.1.4.1.24261.1.1.1.3",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masObjects.I_masOperationalState,
            32,
            masObjectsFamilyObject);

        oiArray[3] = new ObjectInfo(
            "masAdministrativeState",
            "1.3.6.1.4.1.24261.1.1.1.4",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_WRITE,
            v_masObjects.I_masAdministrativeState,
            32,
            masObjectsFamilyObject);

        oiArray[4] = new ObjectInfo(
            "masInstallDate",
            "1.3.6.1.4.1.24261.1.1.1.5",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masObjects.I_masInstallDate,
            32,
            masObjectsFamilyObject);

        oiArray[5] = new ObjectInfo(
            "masCurrentUpTime",
            "1.3.6.1.4.1.24261.1.1.1.6",
            SRSNMP.TIME_TICKS_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masObjects.I_masCurrentUpTime,
            32,
            masObjectsFamilyObject);

        oiArray[6] = new ObjectInfo(
            "masAccumulatedUpTime",
            "1.3.6.1.4.1.24261.1.1.1.7",
            SRSNMP.TIME_TICKS_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masObjects.I_masAccumulatedUpTime,
            32,
            masObjectsFamilyObject);

        oiArray[7] = new ObjectInfo(
            "masConfigurationState",
            "1.3.6.1.4.1.24261.1.1.1.20",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_WRITE,
            v_masObjects.I_masConfigurationState,
            32,
            masObjectsFamilyObject);

        oiArray[8] = new ObjectInfo(
            "masLastConfigurationUpdateTime",
            "1.3.6.1.4.1.24261.1.1.1.21",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masObjects.I_masLastConfigurationUpdateTime,
            32,
            masObjectsFamilyObject);

        oiArray[9] = new ObjectInfo(
            "masLastConfigurationUpdateTicks",
            "1.3.6.1.4.1.24261.1.1.1.22",
            SRSNMP.TIME_TICKS_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masObjects.I_masLastConfigurationUpdateTicks,
            32,
            masObjectsFamilyObject);

        oiArray[10] = new ObjectInfo(
            "masServiceEnablerStatisticsIndex",
            "1.3.6.1.4.1.24261.1.1.1.50.1.1.1",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masServiceEnablerStatisticsEntry.I_masServiceEnablerStatisticsIndex,
            32,
            masServiceEnablerStatisticsEntryFamilyObject);

        oiArray[11] = new ObjectInfo(
            "masServiceEnablerStatisticsProtocol",
            "1.3.6.1.4.1.24261.1.1.1.50.1.1.2",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masServiceEnablerStatisticsEntry.I_masServiceEnablerStatisticsProtocol,
            32,
            masServiceEnablerStatisticsEntryFamilyObject);

        oiArray[12] = new ObjectInfo(
            "masServiceEnablerStatisticsMaxConnections",
            "1.3.6.1.4.1.24261.1.1.1.50.1.1.3",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masServiceEnablerStatisticsEntry.I_masServiceEnablerStatisticsMaxConnections,
            32,
            masServiceEnablerStatisticsEntryFamilyObject);

        oiArray[13] = new ObjectInfo(
            "masConnectionStatisticsIndex",
            "1.3.6.1.4.1.24261.1.1.1.50.2.1.1",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masServiceEnablerStatisticsConnectionsEntry.I_masConnectionStatisticsIndex,
            32,
            masServiceEnablerStatisticsConnectionsEntryFamilyObject);

        oiArray[14] = new ObjectInfo(
            "masConnectionStatisticsType",
            "1.3.6.1.4.1.24261.1.1.1.50.2.1.2",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masServiceEnablerStatisticsConnectionsEntry.I_masConnectionStatisticsType,
            32,
            masServiceEnablerStatisticsConnectionsEntryFamilyObject);

        oiArray[15] = new ObjectInfo(
            "masConnectionStatisticsConnections",
            "1.3.6.1.4.1.24261.1.1.1.50.2.1.3",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masServiceEnablerStatisticsConnectionsEntry.I_masConnectionStatisticsConnections,
            32,
            masServiceEnablerStatisticsConnectionsEntryFamilyObject);

        oiArray[16] = new ObjectInfo(
            "masConnectionStatisticsPeakConnections",
            "1.3.6.1.4.1.24261.1.1.1.50.2.1.4",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masServiceEnablerStatisticsConnectionsEntry.I_masConnectionStatisticsPeakConnections,
            32,
            masServiceEnablerStatisticsConnectionsEntryFamilyObject);

        oiArray[17] = new ObjectInfo(
            "masConnectionStatisticsPeakTime",
            "1.3.6.1.4.1.24261.1.1.1.50.2.1.5",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masServiceEnablerStatisticsConnectionsEntry.I_masConnectionStatisticsPeakTime,
            32,
            masServiceEnablerStatisticsConnectionsEntryFamilyObject);

        oiArray[18] = new ObjectInfo(
            "masConnectionStatisticsTotalConnections",
            "1.3.6.1.4.1.24261.1.1.1.50.2.1.6",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masServiceEnablerStatisticsConnectionsEntry.I_masConnectionStatisticsTotalConnections,
            32,
            masServiceEnablerStatisticsConnectionsEntryFamilyObject);

        oiArray[19] = new ObjectInfo(
            "masConnectionStatisticsAccumulatedConnections",
            "1.3.6.1.4.1.24261.1.1.1.50.2.1.7",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masServiceEnablerStatisticsConnectionsEntry.I_masConnectionStatisticsAccumulatedConnections,
            32,
            masServiceEnablerStatisticsConnectionsEntryFamilyObject);

        oiArray[20] = new ObjectInfo(
            "alarmIndex",
            "1.3.6.1.4.1.24261.1.1.1.100.1.1",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_commonAlarmsEntry.I_alarmIndex,
            32,
            commonAlarmsEntryFamilyObject);

        oiArray[21] = new ObjectInfo(
            "alarmId",
            "1.3.6.1.4.1.24261.1.1.1.100.1.2",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_commonAlarmsEntry.I_alarmId,
            32,
            commonAlarmsEntryFamilyObject);

        oiArray[22] = new ObjectInfo(
            "alarmStatus",
            "1.3.6.1.4.1.24261.1.1.1.100.1.3",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_commonAlarmsEntry.I_alarmStatus,
            32,
            commonAlarmsEntryFamilyObject);

        oiArray[23] = new ObjectInfo(
            "masProvidedServiceIndex",
            "1.3.6.1.4.1.24261.1.1.1.200.1.1.1",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masProvidedServiceEntry.I_masProvidedServiceIndex,
            32,
            masProvidedServiceEntryFamilyObject);

        oiArray[24] = new ObjectInfo(
            "masProvidedServiceName",
            "1.3.6.1.4.1.24261.1.1.1.200.1.1.2",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masProvidedServiceEntry.I_masProvidedServiceName,
            32,
            masProvidedServiceEntryFamilyObject);

        oiArray[25] = new ObjectInfo(
            "masProvidedServiceStatus",
            "1.3.6.1.4.1.24261.1.1.1.200.1.1.3",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masProvidedServiceEntry.I_masProvidedServiceStatus,
            32,
            masProvidedServiceEntryFamilyObject);

        oiArray[26] = new ObjectInfo(
            "masProvidedServiceHostName",
            "1.3.6.1.4.1.24261.1.1.1.200.1.1.4",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masProvidedServiceEntry.I_masProvidedServiceHostName,
            32,
            masProvidedServiceEntryFamilyObject);

        oiArray[27] = new ObjectInfo(
            "masProvidedServicePort",
            "1.3.6.1.4.1.24261.1.1.1.200.1.1.5",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masProvidedServiceEntry.I_masProvidedServicePort,
            32,
            masProvidedServiceEntryFamilyObject);

        oiArray[28] = new ObjectInfo(
            "masProvidedServiceZone",
            "1.3.6.1.4.1.24261.1.1.1.200.1.1.6",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masProvidedServiceEntry.I_masProvidedServiceZone,
            32,
            masProvidedServiceEntryFamilyObject);

        oiArray[29] = new ObjectInfo(
            "masProvidedServiceApplicationName",
            "1.3.6.1.4.1.24261.1.1.1.200.1.1.20",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masProvidedServiceEntry.I_masProvidedServiceApplicationName,
            32,
            masProvidedServiceEntryFamilyObject);

        oiArray[30] = new ObjectInfo(
            "masProvidedServiceApplicationVersion",
            "1.3.6.1.4.1.24261.1.1.1.200.1.1.21",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masProvidedServiceEntry.I_masProvidedServiceApplicationVersion,
            32,
            masProvidedServiceEntryFamilyObject);

        oiArray[31] = new ObjectInfo(
            "masConsumedServiceIndex",
            "1.3.6.1.4.1.24261.1.1.1.200.2.1.1",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masConsumedServiceEntry.I_masConsumedServiceIndex,
            32,
            masConsumedServiceEntryFamilyObject);

        oiArray[32] = new ObjectInfo(
            "masConsumedServiceName",
            "1.3.6.1.4.1.24261.1.1.1.200.2.1.2",
            SRSNMP.OCTET_PRIM_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masConsumedServiceEntry.I_masConsumedServiceName,
            32,
            masConsumedServiceEntryFamilyObject);

        oiArray[33] = new ObjectInfo(
            "masConsumedServiceStatus",
            "1.3.6.1.4.1.24261.1.1.1.200.2.1.3",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masConsumedServiceEntry.I_masConsumedServiceStatus,
            32,
            masConsumedServiceEntryFamilyObject);

        oiArray[34] = new ObjectInfo(
            "masConsumedServiceTime",
            "1.3.6.1.4.1.24261.1.1.1.200.2.1.4",
            SRSNMP.TIME_TICKS_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masConsumedServiceEntry.I_masConsumedServiceTime,
            32,
            masConsumedServiceEntryFamilyObject);

        oiArray[35] = new ObjectInfo(
            "masConsumedServiceNumSuccess",
            "1.3.6.1.4.1.24261.1.1.1.200.2.1.5",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masConsumedServiceEntry.I_masConsumedServiceNumSuccess,
            32,
            masConsumedServiceEntryFamilyObject);

        oiArray[36] = new ObjectInfo(
            "masConsumedServiceNumFailures",
            "1.3.6.1.4.1.24261.1.1.1.200.2.1.6",
            SRSNMP.INTEGER_TYPE,
            SRSNMP.SR_READ_ONLY,
            v_masConsumedServiceEntry.I_masConsumedServiceNumFailures,
            32,
            masConsumedServiceEntryFamilyObject);

    }   /* masOID() */

    /**
     *  Returns the list of ObjectInfos to add/remove from the dispatch table.
     */
    public ObjectInfo[] getObjectInfoArray() {
        return oiArray;
    }   /* getObjectInfoArray() */

    /**
     *  Returns the list of MethodRoutines to initialize or terminate.
     */
    public MethodRoutine[] getMethodRoutineArray() {
        return mrArray;
    }   /* getMethodRoutineArray() */

}

