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
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.impl.InMemoryConfigAgent;
import com.abcxyz.messaging.identityformatter.IdentityFormatter;
import com.abcxyz.messaging.identityformatter.IdentityFormatterInvalidIdentityException;
import com.abcxyz.messaging.mcd.proxy.MCDProxyService;
import com.abcxyz.messaging.mcd.proxy.ldap.DatabaseAccessFramework;
import com.abcxyz.messaging.mcd.proxy.ldap.McdProxyServiceImpl;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.cmnaccess.oam.MoipOamManager;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.util.check.Check;
import com.mobeon.masp.execution_engine.platformaccess.EventType;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessException;

public class GlobalDirectoryAccessUtil {
    
    private static final String VMP_SPECIFIC_CONF = "vmpSpecific.conf";
    private static final String BACK_END_CONF = "backend.conf";
    private static final String SUBSCRIBER_PROFILE_CLASS = "subscriber";
    private static final String MOIP_SERVICE_CLASS = "MOIP";
    
    private static final String MESSAGING_COMMON_DIRECTORY_TABLE = "MessagingCommonDirectory.Table";
    private static final String MESSAGING_COMMON_DIRECTORY_LDAPQUERYOPCONAME_ATTR = "ldapqueryopconame";
    private static final String MESSAGING_COMMON_DIRECTORY_PRIORITY_ATTR = "priority";
    private static final String MESSAGING_COMMON_DIRECTORY_CACHING_ATTR = "caching";
    private static final String MESSAGING_COMMON_DIRECTORY_HOSTNAME_ATTR = "hostname";
    private static final String MESSAGING_COMMON_DIRECTORY_PORT_ATTR = "port";
    private static final String MESSAGING_COMMON_DIRECTORY_USERNAME_ATTR = "username";
    private static final String MESSAGING_COMMON_DIRECTORY_PASSWORD_ATTR = "password";

    private LogAgent logAgent;

    private IdentityFormatter normalizationFormatter = null;
    private static Hashtable<String, MCDProxyService> mcdProxytable = new Hashtable<String, MCDProxyService>();

    public GlobalDirectoryAccessUtil() {
        this.logAgent = CommonOamManager.getInstance().getMcdOam().getLogAgent();
        initIdentityFormatter();
    }

    private String getMcdPoolSize() throws Exception{
        IGroup backEndGroup = CommonOamManager.getInstance().getConfiguration().getGroup(GlobalDirectoryAccessUtil.BACK_END_CONF);
        if(backEndGroup == null){
        	throw new ConfigurationDataException("GlobalDirectoryAccessUtil.getMcdPoolSize: Could not find " + BACK_END_CONF);
        }
        return Integer.toString(backEndGroup.getInteger(ConfigParam.MCD_POOL_SIZE));        
    }
    
    private Map<String, Map<String, String>> getMessagingCommonDirectoryTable() throws Exception{
        IGroup vmpSpecificGroup = CommonOamManager.getInstance().getConfiguration().getGroup(GlobalDirectoryAccessUtil.VMP_SPECIFIC_CONF);
        if(vmpSpecificGroup == null){
        	throw new ConfigurationDataException("GlobalDirectoryAccessUtil.getMessagingCommonDirectoryTable: Could not find " + VMP_SPECIFIC_CONF);
        }
        Map<String, Map<String, String>> messagingCommonDirectoryTable = vmpSpecificGroup.getTable(MESSAGING_COMMON_DIRECTORY_TABLE);
        if(messagingCommonDirectoryTable == null){
        	throw new ConfigurationDataException("GlobalDirectoryAccessUtil.getMessagingCommonDirectoryTable: Could not find " + MESSAGING_COMMON_DIRECTORY_TABLE + " in " + VMP_SPECIFIC_CONF);
        }     
        return messagingCommonDirectoryTable;
    }    
    
    private Map<Integer, String> getOperatorPriority() throws Exception {
        try {
            /*
             * This is an ordered map of the operators name.
             * TreeMap<Priority[Integer], OperatorName[String]>
             * The order is not guaranteed for operators that have the same priority value.
             */
            Map<Integer, String> priorityOperatorMap = new TreeMap<Integer, String>();
            Map<String, Map<String, String>> messagingCommonDirectoryTable = getMessagingCommonDirectoryTable();
            
            Iterator<String> operatorsIterator = messagingCommonDirectoryTable.keySet().iterator();
            while( operatorsIterator.hasNext() ) {
                String operatorName = operatorsIterator.next();
                String priority = messagingCommonDirectoryTable.get(operatorName).get(MESSAGING_COMMON_DIRECTORY_PRIORITY_ATTR);
                priorityOperatorMap.put(Integer.valueOf(priority), operatorName);
            }
            return priorityOperatorMap;
        } catch (Exception e) {
            logAgent.error("GlobalDirectoryAccessUtil.getTopologyOperatorPriority() Exception unable to get the operator priority", e);
            throw e;
        }
    }
    
    private MCDProxyService createMcdProxyService(String operatorName, Map<String, String> messagingCommonDirectoryValuesMap, String mcdPoolSize) throws Exception {    
        MCDProxyService mcdProxyService = GlobalDirectoryAccessUtil.mcdProxytable.get(operatorName);
        if ( mcdProxyService == null ) {
            ConfigManager configMgr = new InMemoryConfigAgent();
            configMgr.setParameter(MCDConstants.CONFIG_OPCO, messagingCommonDirectoryValuesMap.get(MESSAGING_COMMON_DIRECTORY_LDAPQUERYOPCONAME_ATTR));
            configMgr.setParameter(MCDConstants.CONFIG_OPERATOR_NAME, operatorName);
            configMgr.setParameter(MCDConstants.CONFIG_HOST, messagingCommonDirectoryValuesMap.get(MESSAGING_COMMON_DIRECTORY_HOSTNAME_ATTR));
            configMgr.setParameter(MCDConstants.CONFIG_PORT, messagingCommonDirectoryValuesMap.get(MESSAGING_COMMON_DIRECTORY_PORT_ATTR));
            configMgr.setParameter(MCDConstants.CONFIG_BIND, messagingCommonDirectoryValuesMap.get(MESSAGING_COMMON_DIRECTORY_USERNAME_ATTR));
            configMgr.setParameter(MCDConstants.CONFIG_PASSWORD, messagingCommonDirectoryValuesMap.get(MESSAGING_COMMON_DIRECTORY_PASSWORD_ATTR));
            configMgr.setParameter(MCDConstants.MCD_SUBSCRIBER_PROFILE_CACHE, "false");
            configMgr.setParameter(MCDConstants.MCD_COS_PROFILE_CACHE, "false");
            configMgr.setParameter(MCDConstants.MCD_CACHING, "false");
            configMgr.setParameter(ConfigParam.MCD_POOL_SIZE, mcdPoolSize);

            OAMManager mcdOamOfCommonOamManager   = CommonOamManager.getInstance().getMcdOam();  
            OAMManager oamManager = new MoipOamManager();
            oamManager.setFaultManager(mcdOamOfCommonOamManager.getFaultManager());
            oamManager.setProfilerAgent(mcdOamOfCommonOamManager.getProfilerAgent());
            oamManager.setConfigManager(configMgr);
            
            McdProxyServiceImpl mcdProxyServiceImpl = new McdProxyServiceImpl();
            mcdProxyServiceImpl.initialize(oamManager);
            boolean doCaching = Boolean.parseBoolean(messagingCommonDirectoryValuesMap.get(MESSAGING_COMMON_DIRECTORY_CACHING_ATTR));
            if(doCaching){
                mcdProxyService = new DatabaseAccessFramework(oamManager, mcdProxyServiceImpl, doCaching, null);
            }
            else{
                mcdProxyService = mcdProxyServiceImpl;
            }
            GlobalDirectoryAccessUtil.mcdProxytable.put(operatorName, mcdProxyService);
        }
        return mcdProxyService;
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
    public String getOperatorName(String subscriberIdentity) throws PlatformAccessException {
        Object perf = null;
        Map<Integer, String> priorityOperatorMap = null;
        
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("GlobalDAUtil.getOperatorName");
            }
                      
            // Process Invalid arguments
            if (subscriberIdentity == null || subscriberIdentity.length() < 1) {
                logAgent.error("GlobalDirectoryAccessUtil.getOperatorName: Invalid argument received.  Subscriber identity is null or empty.");
                return null;
            }
            logAgent.debug("GlobalDirectoryAccessUtil.getOperatorName: searching for the operator of " + subscriberIdentity);

            String mcdPoolSize = getMcdPoolSize();            
            Map<String, Map<String, String>> messagingCommonDirectoryTable = getMessagingCommonDirectoryTable();
            
            // Get the list of MCD to query.
            // The set's iterator returns the entries in ascending key order.
            priorityOperatorMap   = getOperatorPriority();  
            Iterator<Entry<Integer, String>> entrySetIterator = priorityOperatorMap.entrySet().iterator();
            
            Exception lastThrownException = null;
            
            while( entrySetIterator.hasNext()) {
                Entry<Integer, String> operatorPriorityEntry = entrySetIterator.next();
                Integer priority = operatorPriorityEntry.getKey();
                String operatorName = operatorPriorityEntry.getValue();
                logAgent.debug("GlobalDirectoryAccessUtil.getOperatorName: querying " + operatorName + " having the priority " + priority);
                
                boolean profileExists = false;
                
                Object mcdProxyServicePerf = null;
                MCDProxyService mcdProxyService = null;

                try {
                    Map<String, String> messagingCommonDirectoryValuesMap = messagingCommonDirectoryTable.get(operatorName);
                    if(messagingCommonDirectoryValuesMap == null){
                    	throw new ConfigurationDataException("GlobalDirectoryAccessUtil.getOperatorName: Could not find " + operatorName + " in " + VMP_SPECIFIC_CONF + " " + MESSAGING_COMMON_DIRECTORY_TABLE);
                    }
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        mcdProxyServicePerf = CommonOamManager.profilerAgent.enterCheckpoint("GlobalDAUtil.getOperatorName."+operatorName);
                    }
                    mcdProxyService =  createMcdProxyService(operatorName, messagingCommonDirectoryValuesMap, mcdPoolSize);
                    profileExists = mcdProxyService.profileExists(SUBSCRIBER_PROFILE_CLASS, getSubscriberURI(subscriberIdentity));
                    logAgent.debug("GlobalDirectoryAccessUtil.getOperatorName: Profile exists in " + operatorName + ": " + profileExists);
                } catch (Exception e) {
                    logAgent.error("GlobalDirectoryAccessUtil.getOperatorName: Lookup in " + operatorName + ": "+ e.getMessage(), e);
                    lastThrownException = e;
                } finally {
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        CommonOamManager.profilerAgent.exitCheckpoint(mcdProxyServicePerf);
                    }
                    mcdProxyServicePerf = null;
                    mcdProxyService = null;
                }
                if (profileExists) {
                    return operatorName;
                }
            }
            if (lastThrownException == null) {
                return null;
            } else {
                throw new PlatformAccessException(EventType.SYSTEMERROR, lastThrownException.getMessage(), lastThrownException);
            }
        } catch ( PlatformAccessException e) {
            throw e;
        } catch ( Exception e) {
            logAgent.error("GlobalDirectoryAccessUtil.getOperatorName: "+ e.getMessage(), e);
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

    /**
     * This method returns the attribute value for the specified subscriber or null
     * if the attribute is not found.
     *
     * @param operatorName - The name of the operator to which the specified subscriber belongs.
     * @param subscriberIdentity - The subscriber phone number.  It can be just the
     * telephone number, a Tel URI or a mail address in the form of <number>@domain
     * @param attributeName - The name of the attribute for which the value is requested.
     * @return the value of the requested attribute; null otherwise
     * @throws PlatformAccessException 
     */
    public String[] getSubscriberStringAttribute(String operatorName, String subscriberIdentity, String attributeName) throws PlatformAccessException {
        Object perf = null;
        ProfileContainer subscriber;

        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("GlobalDAUtil.getSubscriberStringAttribute");
            }
                    
            String errorMessage = null;
            
            // Process Invalid arguments
            if (operatorName == null || operatorName.length() < 1) {
                errorMessage = "GlobalDirectoryAccessUtil.getSubscriberStringAttribute: Invalid argument received.  Operator name is null or empty."; 
                logAgent.error(errorMessage);
            }
            if (subscriberIdentity == null || subscriberIdentity.length() < 1) {
                errorMessage = "GlobalDirectoryAccessUtil.getSubscriberStringAttribute: Invalid argument received.  Subscriber identity is null or empty."; 
                logAgent.error(errorMessage);
            }
            if (attributeName == null || attributeName.length() < 1) {
                errorMessage = "GlobalDirectoryAccessUtil.getSubscriberStringAttribute: Invalid argument received.  Attribute name is null or empty."; 
                logAgent.error(errorMessage);
            }
            if (errorMessage != null) {
                return null;
            }
            logAgent.debug("GlobalDirectoryAccessUtil.getSubscriberStringAttribute: retrieving "+subscriberIdentity+":"+attributeName+" on "+operatorName);
            
            // Get the subscriber profile.
            try {
                Map<String, String> messagingCommonDirectoryValuesMap = getMessagingCommonDirectoryTable().get(operatorName);
                if(messagingCommonDirectoryValuesMap == null){
                	throw new ConfigurationDataException("GlobalDirectoryAccessUtil.getSubscriberStringAttribute: Could not find " + operatorName + " in " + VMP_SPECIFIC_CONF + " " + MESSAGING_COMMON_DIRECTORY_TABLE);
                }

                MCDProxyService mcdProxyService = createMcdProxyService(operatorName, messagingCommonDirectoryValuesMap, getMcdPoolSize());
                subscriber = (ProfileContainer)mcdProxyService.lookupProfile(SUBSCRIBER_PROFILE_CLASS, getSubscriberURI(subscriberIdentity), MOIP_SERVICE_CLASS);
            } catch(Exception e){
                logAgent.error("GlobalDirectoryAccessUtil.getSubscriberStringAttribute: "+ e.getMessage(), e);
                throw new PlatformAccessException(EventType.SYSTEMERROR, e.getMessage(), e);
            }
            if ( subscriber == null ) {
                // Subscriber not found.
                return null;
            }
            List<String> attributeValues = subscriber.getAttributeValues(attributeName);
            if ( attributeValues == null ) {
            	logAgent.debug("GlobalDirectoryAccessUtil.getSubscriberStringAttribute: " + attributeName + " not set for " + subscriberIdentity);
                return null;
            }
            String [] attributeValuesArray = attributeValues.toArray(new String[0]);
            
            if (attributeName.equalsIgnoreCase(DAConstants.ATTR_PIN)) {
                Check c = new Check();
                for( int i = 0; i < attributeValuesArray.length; i++ ) {
                    String pin = c.checkout(attributeValuesArray[i]);
                    if ( pin != null) {
                        attributeValuesArray[i] = pin;
                    }
                }
            }
            return attributeValuesArray;
        } finally {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
            perf = null;
            subscriber = null;
        }
    }

    private URI getSubscriberURI(String subscriberIdentity) {
        logAgent.debug("DirectoryAccess.getSubscriberURI: " + subscriberIdentity);
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
                    logAgent.debug("DirectoryAccess.getSubscriberURI: not able to normalize address: " + tmp);
                }
            }

            if(logAgent.isDebugEnabled()) {
                logAgent.debug("DirectoryAccess.getSubscriberURI: final result address: " + tmp + " is normalized to: " + formattedIdentity);
            }

            return new URI(formattedIdentity);
        } catch (URISyntaxException e) {
            return null;
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
