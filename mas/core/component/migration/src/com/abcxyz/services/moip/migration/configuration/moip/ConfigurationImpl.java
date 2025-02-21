/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.configuration.moip;

import com.abcxyz.services.moip.migration.configuration.moip.GroupImpl;
import com.abcxyz.services.moip.migration.configuration.moip.Utilities;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.ConfigurationLoadException;
import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.MissingConfigurationFileException;
import com.mobeon.common.configuration.SchemaValidator;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Implements the IConfiguration interface.
 */
@SuppressWarnings({"EmptyCatchBlock"})
public final class ConfigurationImpl implements IConfiguration {
    private static final ILogger logger = ILoggerFactory.getILogger(IConfiguration.class);
    private GroupImpl rootGroup = null;
    private final IConfigurationManager cfgMgr;
    private boolean backupUsed = false;

    /**
     * .
     *
     * @param cfgMgr
     * @param configFiles
     * @param tryBackup
     * @throws MissingConfigurationFileException
     *
     */
    public ConfigurationImpl(IConfigurationManager cfgMgr, Collection<String> configFiles, boolean tryBackup)
            throws ConfigurationException {
        this.cfgMgr = cfgMgr;
        ConfigurationException e;

        // Loop through all configuration files.
        for (String configName : configFiles) {
            // Try to load a configuration file
            if ((e = loadFile(configName)) != null) {
                // The load failed, shall we use the backup?
                if (tryBackup) {
                    // Try the backup (usually used at initial load only).
                    String backupName = Utilities.createBackupName(configName);
                    logger.debug("Try to use backupfile [" + backupName + "]");

                    backupUsed = true;
                    File configFile = new File(backupName);
                    logger.warn("Configuration file is corrupt. Using backup file: " + configFile.getAbsolutePath());
                    if ((e = loadFile(backupName)) != null) {
                        // The backup file also failed. This is a serious error
                        // Throw runtime_error
                        if (e instanceof MissingConfigurationFileException) {
                            logger.warn("Configuration file is corrupt. No backup found.");
                        } else {
                            logger.warn("Backup configuration file is corrupt. Aborting configuration read.");
                        }
                        throw new RuntimeException(e);
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    private ConfigurationException loadFile(String configFile) {
        Document xmlDoc;

        logger.debug("Try loading file [" + configFile + "]");

        File in = new File(configFile);

        if (!in.exists()) {
            logger.debug("File not exsist [" + configFile + "]");
            return new MissingConfigurationFileException(configFile);
        }

        try {
            SAXReader reader = new SAXReader();
            xmlDoc = reader.read(in);

            String schemaName = SchemaValidator.getSchemaFromXML(xmlDoc);

            if (SchemaValidator.validateWithSchema(configFile, schemaName)) {
                // All ok.
                logger.debug("File validated OK [" + configFile + "]");

                if (rootGroup == null) {
                    this.rootGroup = new GroupImpl(xmlDoc);
                } else {
                    rootGroup.addConfiguration(xmlDoc);
                }
                return null;
            }
        } catch (DocumentException e) {
            // No special handling for this exception. If anything gets
            // past the inner block it's an error.
        }
        logger.debug("File validated not OK [" + configFile + "]");
        return new ConfigurationLoadException(configFile);
    }

    public IGroup getGroup(String name) throws GroupCardinalityException, UnknownGroupException {
        if (this.rootGroup == null) {
            throw new UnknownGroupException(name, null);
        }
        return rootGroup.getGroup(name);
    }

    public List<IGroup> getGroups(String name) throws UnknownGroupException {
        if (this.rootGroup == null) {
            throw new UnknownGroupException(name, null);
        }
        return rootGroup.getGroups(name);
    }

    public boolean hasGroup(String name) {
        try {
            List<IGroup> list = rootGroup.getGroups(name);
            return list.size() > 0;
        } catch (UnknownGroupException e) {
            return false;
        }
    }

    public IConfiguration getConfiguration() {
        return cfgMgr.getConfiguration();
    }

    public void setBackupUsed(boolean used) {
        backupUsed = used;
    }

    public boolean getBackupUsed() {
        return backupUsed;
    }
}
