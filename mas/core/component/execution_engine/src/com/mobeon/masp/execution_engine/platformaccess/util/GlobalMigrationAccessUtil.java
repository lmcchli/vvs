/**
 * COPYRIGHT (c) Abcxyz Canada Inc., 2010.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Abcxyz Canada Inc.  The program(s) may be used and/or copied only with the
 * written permission from Abcxyz Canada Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *
 */
package com.mobeon.masp.execution_engine.platformaccess.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.impl.InMemoryConfigAgent;
import com.abcxyz.messaging.identityformatter.IdentityFormatter;
import com.abcxyz.messaging.identityformatter.IdentityFormatterInvalidIdentityException;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.cmnaccess.oam.MoipOamManager;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.util.check.Check;
import com.mobeon.masp.execution_engine.platformaccess.EventType;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessException;

import abcxyz.services.messaging.migration.client.HttpMigrationService;
import abcxyz.services.messaging.migration.client.MigrationService;

public class GlobalMigrationAccessUtil {
    
    private static final String VMP_SPECIFIC_CONF = "vmpSpecific.conf";
    
    private static final String MIGRATION_TOOL_TABLE = "MigrationTool.Table";
    private static final String MIGRATION_TOOL_PRIORITY_ATTR = "priority";
    private static final String MIGRATION_TOOL_HOSTNAME_ATTR = "hostname";
    private static final String MIGRATION_TOOL_PORT_ATTR = "port";

    private LogAgent logAgent;

    private IdentityFormatter normalizationFormatter = null;
    private static Hashtable<String, MigrationService> migrationServiceTable = new Hashtable<String, MigrationService>();


    public GlobalMigrationAccessUtil() {
        this.logAgent = CommonOamManager.getInstance().getMcdOam().getLogAgent();
        initIdentityFormatter();
    }
    
    
    private Map<String, Map<String, String>> getMigrationTable() throws Exception{
        IGroup vmpSpecificGroup = CommonOamManager.getInstance().getConfiguration().getGroup(GlobalMigrationAccessUtil.VMP_SPECIFIC_CONF);
        if(vmpSpecificGroup == null){
            throw new ConfigurationDataException("GlobalMigrationAccessUtil.getMigrationTable: Could not find " + VMP_SPECIFIC_CONF);
        }
        Map<String, Map<String, String>> migrationTable = vmpSpecificGroup.getTable(MIGRATION_TOOL_TABLE);
        if(migrationTable == null){
            throw new ConfigurationDataException("GlobalMigrationAccessUtil.getMigrationTable: Could not find " + MIGRATION_TOOL_TABLE + " in " + VMP_SPECIFIC_CONF);
        }     
        return migrationTable;
    }    
    
    private Map<Integer, String> getOperatorPriority() throws Exception {
        try {
            /*
             * This is an ordered map of the operators name.
             * TreeMap<Priority[Integer], OperatorName[String]>
             * The order is not guaranteed for operators that have the same priority value.
             */
            Map<Integer, String> priorityOperatorMap = new TreeMap<Integer, String>();
            Map<String, Map<String, String>> migrationTable = getMigrationTable();
            
            Iterator<String> operatorsIterator = migrationTable.keySet().iterator();
            while( operatorsIterator.hasNext() ) {
                String operatorName = operatorsIterator.next();
                String priority = migrationTable.get(operatorName).get(MIGRATION_TOOL_PRIORITY_ATTR);
                priorityOperatorMap.put(Integer.valueOf(priority), operatorName);
            }
            return priorityOperatorMap;
        } catch (Exception e) {
            logAgent.error("GlobalMigrationAccessUtil.getOperatorPriority() Exception unable to get the operator priority", e);
            throw e;
        }
    }
    
    
    private MigrationService createMigrationService(String operatorName, Map<String, String> migrationValuesMap) throws Exception {    
        MigrationService migrationService = GlobalMigrationAccessUtil.migrationServiceTable.get(operatorName);
        if ( migrationService == null ) {
            migrationService = new HttpMigrationService(Integer.parseInt(migrationValuesMap.get(MIGRATION_TOOL_PORT_ATTR)));
            migrationService.addTargetHost("migrationServer", migrationValuesMap.get(MIGRATION_TOOL_HOSTNAME_ATTR));
            GlobalMigrationAccessUtil.migrationServiceTable.put(operatorName, migrationService);
        }
        return migrationService;
    }
    
    

    /**
     * This method returns the operator name of the specified subscriber or null
     * if the subscriber is not found in any operator.
     * The order in which the operators are queried is based on the priority
     * attribute found in the Topology file.  The Priority attribute is an optional
     * attribute in the Topology file.  Its default value is 0.  Operators having a
     * priority of 0 will be searched first.  The order is not guaranteed for operators
     * that have the same priority value.
     *
     * @param subscriberIdentity - The subscriber phone number.  It can be just the
     * telephone number, a Tel URI or a mail address in the form of <number>@domain
     * @return the name of the operator that the subscriber belongs to; null otherwise
     * @throws PlatformAccessException 
     */
    public Boolean isBeingMigrated(String subscriberIdentity) throws PlatformAccessException {
        Object perf = null;
        Map<Integer, String> priorityOperatorMap = null;
        
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("GlobalMAUtil.getOperatorName");
            }
                      
            // Process Invalid arguments
            if (subscriberIdentity == null || subscriberIdentity.length() < 1) {
                logAgent.error("GlobalMigrationAccessUtil.isBeingMigrated: Invalid argument received.  Subscriber identity is null or empty.");
                return false;
            }
            logAgent.debug("GlobalMigrationAccessUtil.isBeingMigrated: searching for " + subscriberIdentity);

            Map<String, Map<String, String>> migrationTable = getMigrationTable();
            
            // Get the list of MigrationTools to query.
            // The set's iterator returns the entries in ascending key order.
            priorityOperatorMap   = getOperatorPriority();  
            Iterator<Entry<Integer, String>> entrySetIterator = priorityOperatorMap.entrySet().iterator();
            
            Exception lastThrownException = null;
            
            while( entrySetIterator.hasNext()) {
                Entry<Integer, String> operatorPriorityEntry = entrySetIterator.next();
                Integer priority = operatorPriorityEntry.getKey();
                String operatorName = operatorPriorityEntry.getValue();
                logAgent.debug("GlobalMigrationAccessUtil.isBeingMigrated: querying " + operatorName + " having the priority " + priority);
                
                boolean isBeingMigrated = false;
                
                Object migrationServicePerf = null;
                MigrationService migrationService = null;

                try {
                    Map<String, String> migrationValuesMap = migrationTable.get(operatorName);
                    if(migrationValuesMap == null){
                        throw new ConfigurationDataException("GlobalMigrationAccessUtil.isBeingMigrated: Could not find " + operatorName + " in " + VMP_SPECIFIC_CONF + " " + MIGRATION_TOOL_TABLE);
                    }
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        migrationServicePerf = CommonOamManager.profilerAgent.enterCheckpoint("GlobalMAUtil.isBeingMigrated."+operatorName);
                    }
                    migrationService =  createMigrationService(operatorName, migrationValuesMap);
                    isBeingMigrated = migrationService.isBeingMigrated(normalizeSubscriberId(subscriberIdentity));
                    logAgent.debug("GlobalMigrationAccessUtil.isBeingMigrated: in " + operatorName + ", result=" + isBeingMigrated);
                } catch (Exception e) {
                    logAgent.error("GlobalMigrationAccessUtil.isBeingMigrated: in " + operatorName + ": "+ e.getMessage(), e);
                    lastThrownException = e;
                } finally {
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        CommonOamManager.profilerAgent.exitCheckpoint(migrationServicePerf);
                    }
                    migrationServicePerf = null;
                    migrationService = null;
                }
                if (isBeingMigrated) {
                    return isBeingMigrated;
                }
            }
            if (lastThrownException == null) {
                return false;
            } else {
                throw new PlatformAccessException(EventType.SYSTEMERROR, lastThrownException.getMessage(), lastThrownException);
            }
        } catch ( PlatformAccessException e) {
            throw e;
        } catch ( Exception e) {
            logAgent.error("GlobalMigrationAccessUtil.isBeingMigrated: "+ e.getMessage(), e);
            throw new PlatformAccessException(EventType.SYSTEMERROR, e.getMessage(), e);
        } finally {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
            if (priorityOperatorMap != null) {
                priorityOperatorMap.clear();
                priorityOperatorMap = null;
            }
            perf = null;
        }
    }

    

    private String normalizeSubscriberId(String subscriberIdentity) {
        logAgent.debug("GlobalMigrationAccessUtil.normalizeSubscriberId: " + subscriberIdentity);
        String tmp;
        String formattedIdentity;
        try {
            if(subscriberIdentity.contains(":")){
                tmp = subscriberIdentity;
            } else if(subscriberIdentity.contains("@")){
                tmp = MCDConstants.IDENTITY_SCHEME_MAILTO + ":" + subscriberIdentity;
            } else {
                tmp = MCDConstants.IDENTITY_SCHEME_TEL + ":" + subscriberIdentity;
            }

            try {
                formattedIdentity = normalizationFormatter.formatIdentity(tmp);
            } catch (IdentityFormatterInvalidIdentityException e) {
                formattedIdentity = null;
                logAgent.error("Unable to format subscriber identity", e);
            }

            if (formattedIdentity == null) {
                /**
                 * not able to format, put the original address
                 */
                formattedIdentity = tmp;
                if(logAgent.isDebugEnabled()) {
                    logAgent.debug("GlobalMigrationAccessUtil.normalizeSubscriberId: not able to normalize address: " + tmp);
                }
            }

            if(logAgent.isDebugEnabled()) {
                logAgent.debug("GlobalMigrationAccessUtil.normalizeSubscriberId: final result address: " + tmp + " is normalized to: " + formattedIdentity);
            }

            return formattedIdentity;
        } finally {
            tmp = null;
            formattedIdentity = null;
        }
    }
    
    private void initIdentityFormatter(){
        try {
            String cfgFile = System.getProperty("normalizationconfig", CommonMessagingAccess.NORMALIZATION_CONFIG_PATH);
            ConfigManager ruleFile = OEManager.getConfigManager(cfgFile, logAgent);
            normalizationFormatter = new IdentityFormatter(CommonOamManager.getInstance().getMfsOam(), ruleFile);
        }catch(Exception e){
            logAgent.error("DirectoryAccess.initIdentityFormatter() Exception unable to initialize identityformatter: " + e.getMessage(), e);
        }
    }
    
}
