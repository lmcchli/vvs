/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.abcxyz.services.moip.migration.profilemanager.moip.pool.DirContextPoolManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownParameterException;
import com.mobeon.masp.profilemanager.ProfileManagerException;

import java.util.*;

/**
 * Configuration class
 *
 * @author mande
 */
public class BaseConfig {
    private static final ILogger log = ILoggerFactory.getILogger(BaseConfig.class);
    private static final String PROFILEMANAGER_CONFIG_GROUP_NAME = "profilemanager";
    private static final String USER_REGISTER = "userregister";
    private static final String CONNECTION_POOL = "connectionpool";
    private static final String PROVISIONING = "provisioning";
    private static final String ATTRIBUTE_MAP = "attributemap";

    private static final String DEFAULT_SEARCHORDER = "community,cos,user,billing";
    private static final int DEFAULT_WRITE_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 5000;
    private static final String DEFAULT_ADMIN = "cn=Directory Manager";
    private static final String DEFAULT_PASSWORD = "emmanager";
    private static final String DEFAULT_SEARCHBASE = "o=mobeon.com";
    private static final int DEFAULT_TRY_LIMIT = 3;
    private static final int DEFAULT_TRY_TIME_LIMIT = 500;
    private static final String DEFAULT_LIMITSCOPE = "false";
    private static final int DEFAULT_COS_CACHE_TIMEOUT = 300000;
    private static final String DEFAULT_USER_ADMIN_PASSWORD = "secret";
    private static final int DEFAULT_POOL_MAX_SIZE = 25;
    private static final int DEFAULT_CONNECTION_LIFETIME = 300000;
    private static final String DEFAULT_BASIC_ATTRIBUTES = 
        "cosname," +
        "diskspaceremainingwarninglevel," +
        "emcnl," +
        "emexpirationrule," +
        "emftlfunctions," +
        "eminterfacename," +
        "eminterfacetype," +
        "emnoofmailquota," +
        "emnotretrievedvoicemsg," +
        "emodlpinskip," +
        "emremovemailboxtime," +
        "emretentiontime," +
        "emtuiaccess," +
        "emvuiaccess," +
        "fastloginavailable," +
        "fastloginenabled," +
        "inhoursdow," +
        "inhoursend," +
        "inhoursstart," +
        "mailquota," +
        "maxloginlockout," +
        "passwdlenmax," +
        "passwdlenmin," +
        "passwordmaxlength," +
        "passwordminlength," +
        "passwordskipavailable," +
        "passwordskipenabled," +
        "phonenumberexpansionsection," +
        "welcomegrt";
    private static final String DEFAULT_UER_PREFIX = "uniqueidentifier";
    private static final String DEFAULT_BILLING_PREFIX = "billingnumber";
        
    private int readTimeout;
    private int writeTimeout;
    private String admin;
    private String password;
    private String defaultSearchbase;
    private boolean limitScope;
    private int cosCacheTimeout;
    private Map<String, ProfileMetaData> applicationAttributeMap;
    private Map<String, Set<ProfileMetaData>> userRegisterAttributeMap;
    private List<ProfileLevel> defaultSearchOrder;
    private String [] basicAttributes;
    private Map<String, String> provisioningMap;
    private String userAdminPassword;
    private int tryLimit;
    private int tryTimeLimit;
    private int poolMaxSize;
    private int connectionLifeTime;
    private String billingPrefix;
    private String userPrefix;

    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * For testing purposes only. Sets the configured read timeout.
     * @param readTimeout The timeout to use for reading operations.
     */
    public void setReadTimeout(int readTimeout) {
        if (log.isDebugEnabled()) log.debug("setReadTimeout(readTimeout=" + readTimeout + ")");
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * For testing purposes only. Sets the configured write timeout.
     * @param writeTimeout The timeout to use for writing operations.
     */
    public void setWriteTimeout(int writeTimeout) {
        if (log.isDebugEnabled()) log.debug("setWriteTimeout(writeTimeout=" + writeTimeout + ")");
        this.writeTimeout = writeTimeout;
    }

    public String getAdmin() {
        return admin;
    }

    public String getPassword() {
        return password;
    }

    public String getDefaultSearchbase() {
        return defaultSearchbase;
    }

    public boolean getLimitScope() {
        return limitScope;
    }

    public int getCosCacheTimeout() {
        return cosCacheTimeout;
    }
    
    public String [] getBasicAttributes() {
    	return basicAttributes;
    }

    public List<ProfileLevel> getDefaultSearchOrder() {
        return defaultSearchOrder;
    }

    public Map<String,ProfileMetaData> getApplicationAttributeMap() {
        return applicationAttributeMap;
    }

    public Map<String,Set<ProfileMetaData>> getUserRegisterAttributeMap() {
        return userRegisterAttributeMap;
    }

    protected void init(IConfiguration configuration) throws ProfileManagerException {
        IGroup profileManagerGroup;
        try {
            profileManagerGroup = configuration.getGroup(PROFILEMANAGER_CONFIG_GROUP_NAME);
        } catch (ConfigurationException e) {
            if (log.isDebugEnabled()) log.debug(e.getMessage());
            throw new ProfileManagerException(e.getMessage());
        }
        createProfileManagerProperties(profileManagerGroup);
        createUserRegisterProperties(profileManagerGroup);
        createConnectionPoolProperties(profileManagerGroup);
        createProvisioningProperties(profileManagerGroup);
        createAttributeMaps(profileManagerGroup);
        setUpDirContextPoolManager();
    }

    private void setUpDirContextPoolManager() {
        // Todo: Retrieve this from com.sun.jndi.ldap.connect.timeout instead
        DirContextPoolManager.getInstance().setTimeoutLimit(getReadTimeout());
        DirContextPoolManager.getInstance().setMaxSize(getPoolMaxSize());
        DirContextPoolManager.getInstance().setForcedReleaseContextLimit(getConnectionLifeTime());
    }

    private void createProfileManagerProperties(IGroup profileManagerGroup) throws ProfileManagerException {
        try {
            limitScope = Boolean.parseBoolean(profileManagerGroup.getString("limitscope", DEFAULT_LIMITSCOPE));
            cosCacheTimeout = profileManagerGroup.getInteger("coscachetimeout", DEFAULT_COS_CACHE_TIMEOUT);
        } catch (ConfigurationException e) {
            log.fatal(e.getMessage());
            throw new ProfileManagerException(e.getMessage());
        }
    }

    private void createUserRegisterProperties(IGroup profileManagerGroup) throws ProfileManagerException {
        try {
            IGroup userRegisterGroup = profileManagerGroup.getGroup(USER_REGISTER);
            readTimeout = userRegisterGroup.getInteger("readtimeout", DEFAULT_READ_TIMEOUT);
            writeTimeout = userRegisterGroup.getInteger("writetimeout", DEFAULT_WRITE_TIMEOUT);
            admin = userRegisterGroup.getString("admin", DEFAULT_ADMIN);
            password = userRegisterGroup.getString("password", DEFAULT_PASSWORD);
            defaultSearchbase = userRegisterGroup.getString("defaultsearchbase", DEFAULT_SEARCHBASE);
            tryLimit = userRegisterGroup.getInteger("trylimit", DEFAULT_TRY_LIMIT);
            tryTimeLimit = userRegisterGroup.getInteger("trytimelimit", DEFAULT_TRY_TIME_LIMIT);
            userPrefix = userRegisterGroup.getString("userprefix", DEFAULT_UER_PREFIX);
            billingPrefix = userRegisterGroup.getString("billingprefix", DEFAULT_BILLING_PREFIX);
        } catch (ConfigurationException e) {
            log.fatal(e.getMessage());
            throw new ProfileManagerException(e.getMessage());
        }
    }

    private void createConnectionPoolProperties(IGroup profileManagerGroup) throws ProfileManagerException {
        try {
            IGroup connectionPoolGroup = profileManagerGroup.getGroup(CONNECTION_POOL);
            poolMaxSize = connectionPoolGroup.getInteger("maxsize", DEFAULT_POOL_MAX_SIZE);
            connectionLifeTime = connectionPoolGroup.getInteger("connectionlifetime", DEFAULT_CONNECTION_LIFETIME);
        } catch (ConfigurationException e) {
            log.fatal(e.getMessage());
            throw new ProfileManagerException(e.getMessage());
        }
    }

    private void createProvisioningProperties(IGroup profileManagerGroup) throws ProfileManagerException {
        try {
            IGroup userRegisterGroup = profileManagerGroup.getGroup(PROVISIONING);
            userAdminPassword = userRegisterGroup.getString("password", DEFAULT_USER_ADMIN_PASSWORD);
        } catch (ConfigurationException e) {
            log.fatal(e.getMessage());
            throw new ProfileManagerException(e.getMessage());
        }
    }

    private void createAttributeMaps(IGroup profileManagerGroup) throws ProfileManagerException {
        // Create the maps of application/userregister attributes and metadata
        applicationAttributeMap = new HashMap<String, ProfileMetaData>();
        userRegisterAttributeMap = new HashMap<String, Set<ProfileMetaData>>();
        provisioningMap = new HashMap<String, String>();
        try {
            IGroup attributeMapGroup = profileManagerGroup.getGroup(ATTRIBUTE_MAP);
            String defaultSearchOrderString = attributeMapGroup.getString("searchorder", DEFAULT_SEARCHORDER);
            defaultSearchOrder = ProfileMetaData.getSearchOrder(defaultSearchOrderString);
            basicAttributes = attributeMapGroup.getString("basicattributes", DEFAULT_BASIC_ATTRIBUTES).split(",");
            handleAttributes(attributeMapGroup);
        } catch (ConfigurationException e) {
            log.fatal(e.getMessage());
            throw new ProfileManagerException(e.getMessage());
        }
    }

    private void handleAttributes(IGroup attributeMapGroup) {
        List<String> attributes = attributeMapGroup.listGroups();
        for (String attribute : attributes) {
            try {
                IGroup attributeGroup = attributeMapGroup.getGroup(attribute);
                try {
                    attributeGroup.getString("userregistername");
                    createUserRegisterData(attributeGroup);
                } catch (UnknownParameterException e) {
                    // Not a userregister attribute, just skip
                    if (log.isDebugEnabled()) log.debug(attribute + " is not a user register attribute");
                }
                try {
                    attributeGroup.getString("provisioningname");
                    createProvisioningData(attributeGroup);
                } catch (UnknownParameterException e) {
                    // Not a provisioning attribute, just skip
                    if (log.isDebugEnabled()) log.debug(attribute + " is not a provisioning attribute");
                }
            } catch (ConfigurationException e) {
                log.warn("Couldn't get group " + attribute + ". " + e.getMessage());
            }
        }
    }

    private void createUserRegisterData(IGroup attributeGroup) {
        try {
            ProfileMetaData metaData =
                    new ProfileMetaData(attributeGroup, getDefaultSearchOrder());
            applicationAttributeMap.put(metaData.getApplicationName(), metaData);
            Set<ProfileMetaData> profileMetaDataSet;
            String userRegisterName = metaData.getUserRegisterName();
            if (userRegisterAttributeMap.containsKey(userRegisterName)) {
                profileMetaDataSet = userRegisterAttributeMap.get(userRegisterName);
            } else {
                profileMetaDataSet = new HashSet<ProfileMetaData>();
                userRegisterAttributeMap.put(userRegisterName, profileMetaDataSet);
            }
            profileMetaDataSet.add(metaData);
        } catch (MetaDataException e) {
            log.warn("Couldn't create metadata for attribute " + attributeGroup.getName() + ". " + e.getMessage());
        }
    }

    private void createProvisioningData(IGroup attributeGroup) {
        try {
            provisioningMap.put(attributeGroup.getName(), attributeGroup.getString("provisioningname"));
        } catch (UnknownParameterException e) {
            log.warn("Expected provisioningname attribute for " + attributeGroup.getName() + ". " + e.getMessage());
        }
    }


    public Map<String, String> getProvisioningMap() {
        return provisioningMap;
    }

    public String getUserAdminPassword() {
        return userAdminPassword;
    }

    public int getTryLimit() {
        return tryLimit;
    }

    public int getTryTimeLimit() {
        return tryTimeLimit;
    }

    /**
     * For testing purposes only. Sets the configured try time limit.
     * @param tryTimeLimit The time limit during which retries are made.
     */
    void setTryTimeLimit(int tryTimeLimit) {
        if (log.isDebugEnabled()) log.debug("setTryTimeLimit(tryTimeLimit=" + tryTimeLimit + ")");
        this.tryTimeLimit = tryTimeLimit;
    }

    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    public int getConnectionLifeTime() {
        return connectionLifeTime;
    }

    public String getBillingPrefix() {
        return billingPrefix;
    }

    public String getUserPrefix() {
        return userPrefix;
    }
}
