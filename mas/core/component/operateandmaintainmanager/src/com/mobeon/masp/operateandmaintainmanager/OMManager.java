/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.operateandmaintainmanager;

/**
 * This is the main class for OMM.
 * After instansiate this class the setConfiguration must be done before running init.
 *
 * This class starts the operateandmaintain to be able to monitoring and interact with MAS..
 */


import com.abcxyz.messaging.common.oam.ConfigRefreshException;
import com.abcxyz.messaging.common.oam.ConfigRefreshListener;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.PerformanceManager;
import com.abcxyz.messaging.common.util.SystemPropertyHandler;
import com.abcxyz.messaging.oe.common.configuration.ConfigurationConstants;
import com.abcxyz.messaging.oe.common.subsystem.Manageable;
import com.abcxyz.messaging.oe.common.subsystem.ManageableExt;
import com.abcxyz.messaging.oe.common.topology.ComponentSubsystemInfo;
import com.abcxyz.messaging.oe.impl.configuration.ConfigurationDataManager;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.abcxyz.messaging.oe.lib.oamaccess.OeOamFacadeBuilder;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.PlatformAccessPluginLoader;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.LogAgentFactory;

public class OMManager implements Supervision, ManageableExt, ConfigRefreshListener {
    private static OperateMAS operateMAS;
    private ILogger log;
    private OMMConfiguration ommconfig;
    private EventHandler eventHandler;
    private IEventDispatcher eventDispatcher;
    private IConfigurationManager configManager;
    private OAMManager oamManager;
	private MemoryMonitor memoryMonitor;
    static{
        SystemPropertyHandler.setProperty("abcxyz.services.messaging.productPrefix", "moip");
    	SystemPropertyHandler.setProperty("management_config_backup_root", "");
        SystemPropertyHandler.setProperty("ObjectId", "19");
        SystemPropertyHandler.setProperty("BaseOid", ".1.3.6.1.4.1.193.91.4");
        SystemPropertyHandler.setProperty("RootOid", ".1.3.6.1.4.1.193.91.");
        SystemPropertyHandler.setProperty("mms_root", "/opt/moip");
    }
    /**
     * Constructor
     */
    public OMManager() {
        log = ILoggerFactory.getILogger(OMManager.class);
    }

    /**
     * Initiate omm and start RPC server.
     * @throws ConfigurationDataException
     */
    public void init() throws ConfigurationDataException {


        OEManager.setComponentType(ConfigurationConstants.MAS_COMPONENT_IDENTITY);
        OeOamFacadeBuilder facade  = new OeOamFacadeBuilder(ConfigurationConstants.MAS_COMPONENT_IDENTITY);
        try {
     	   oamManager = facade.getOamManager(ComponentSubsystemInfo.getComponentSubsystem(ConfigurationConstants.MAS_COMPONENT_IDENTITY)[0], ((Manageable) this));
        } catch (ConfigurationDataException e) {
     	   throw e;
        }

        oamManager.setLogAgent(LogAgentFactory.getLogAgent(OMManager.class));
        try {
     	   oamManager.getLogAgent().debug("Registering for refresh");
     	   ConfigurationDataManager cfgManager = ConfigurationDataManager.getInstance();

            // register for configuration changes
            cfgManager.addConfigRefreshListener(this);
        } catch (IllegalArgumentException e) {
     	   oamManager.getLogAgent().error("NtfMain error: " + e.getMessage());
        }

        // start loadRegulation
        LoadRegulation loadReg = new LoadRegulation();

        // This is only for self diagnostic to check OM Component.
        //SelfDiag diag = new SelfDiag(this);
        if (eventDispatcher == null) {
            log.error("No Event dispatcher was set");
            throw new NullPointerException("No Event dispatcher was set");
        }

        if (configManager == null) {
            log.error("No Configuration Manager was set");
            throw new NullPointerException("No Configuration Manager was set");
        }

        // if no configuration was set. create one with default values.
        if (ommconfig == null) {
            ommconfig = new OMMConfiguration();
        }

        // Create operate
        initMasPerformanceManager();
        operateMAS = new OperateMAS();
        operateMAS.setConfigManager(configManager);
        operateMAS.setLoadRegulation(loadReg);
        
        //create the memory Monitor
        memoryMonitor = new MemoryMonitor();
        memoryMonitor.init(operateMAS,configManager);

        

        //Create event handler and add as event receiver.
        eventHandler = new EventHandler(operateMAS);
        eventDispatcher.addEventReceiver(eventHandler);

        // Configuration is using a backup file
        // Set mib attribute to NOK. (Not ok, using backup)

        initMasFaultManager();
        log.debug("OMManager is intialized.");
    }

    private void initMasFaultManager() {

    	MasFaultManager masFaultManager = new MasFaultManager();
    	masFaultManager.setFaultManager(oamManager.getFaultManager());
    	operateMAS.setMasFaultManager(masFaultManager);
    	CommonOamManager.getInstance().setFaultManager(masFaultManager);

	}

    private void initMasPerformanceManager() {
        PerformanceManager performanceManager = oamManager.getPerformanceManager();
        CommonOamManager.getInstance().setPerformanceManager(performanceManager);
    }


	/**
     * Sets the configuration object for omm to read.
     *
     * @param config
     */
    public void setConfiguration(IConfiguration config) {
        // setConfiguration must be run before init.
        if (config == null) {
            ommconfig = null;
        } else {
            ommconfig = new OMMConfiguration(config);
        }
    }

    /**
     * Sets the configuration manager to be able to reload configuration
     *
     * @param configManager
     */
    public void setConfigurationManager(IConfigurationManager configManager) {
        this.configManager = configManager;
        if (configManager == null) {
            log.error("The config manager cant be null. Bailing out.");
            throw new NullPointerException("config manager cant be null");
        }

    }

    /**
     * Sets the event dispatcher that should be used to receive events
     * from other components.
     *
     * @param eventDispatcher The event dispatcher. May not be
     *                        <code>null</code>.
     * @throws IllegalArgumentException If <code>eventDispatcher</code>
     *                                  is <code>null</code>.
     */
    public void setEventDispatcher(IEventDispatcher eventDispatcher) {

        if (eventDispatcher == null) {
            log.error("The event dispatcher cant be null. Bailing out.");
            throw new NullPointerException("event dispatcher cant be null");
        }

        this.eventDispatcher = eventDispatcher;

    }

    /**
     * Register a provided service to OMM.
     *
     * @param serviceName           : Name of provided service to be registered.
     * @param host                  : Host where service exsists
     * @param port                  : Port where the service talk.
     * @param serviceEnablerOperate : ServiceEnablerOperate reference to the serviceenabler where the service is connected.
     * @return ProvidedServiceEntry : ProvidedService
     */
    public ProvidedService createProvidedServiceEntry(String serviceName, String host, Integer port, ServiceEnablerOperate serviceEnablerOperate) throws IlegalServiceParametersException {
        ProvidedService ps = null;
        if (serviceName != null && host != null && port != null && serviceEnablerOperate != null) {
            ps = operateMAS.createProvidedServiceEntry(serviceName, host, port, serviceEnablerOperate);
        } else {
            throw new IlegalServiceParametersException("Parameter can not be null:");
        }
        return ps;
    }


    /**
     * Register a consumed service to OMM.
     *
     * @param serviceName : Name of consumed service to be registered.
     * @param host        : Host where service exsists
     * @param port        : Port where the service talk.
     * @return ConsumedServiceEntry : ConsumedService
     */

    public ConsumedService createConsumedServiceEntry(String serviceName, String host, int port) {
        log.debug("Consumed service registered");
        ConsumedService cs;
        cs = operateMAS.createConsumedServiceEntry(serviceName, host, port);
        return cs;
    }


    /**
     * returns the a session factory.
     *
     * @return SessionInfoFactory
     */
    public SessionInfoFactory getSessionInfoFactory() {
        return operateMAS.getConnectionMonitorFactory();
    }


    public ServiceEnablerInfo getServiceEnablerStatistics(ServiceEnablerOperate serviceEnablerRef) throws Exception {
        if (serviceEnablerRef == null)
            throw new Exception("Service Enabler Operate must not be null");
        return operateMAS.getServiceEnablerStatistics(serviceEnablerRef);
    }



    /**
     * @return the operateMAS instance
     */
    public static OperateMAS getOperateMAS() {
        return operateMAS;
    }




	@Override
	public String getSubsysName() {
		return null;
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public boolean lock() {
		return (operateMAS.setAdminState(OperateMAS.AdminState.LOCK));
	}

	@Override
	public boolean refreshTopology() {
		//TODO
		return true;
	}

	@Override
	public boolean unlock() {
        return(operateMAS.setAdminState(OperateMAS.AdminState.UNLOCK));
	}

	@Override
	public void refreshConfig(long timestamp) throws ConfigRefreshException {
	    operateMAS.sendReloadConfiguration();

	    if (PlatformAccessPluginLoader.get().getPlugin() != null) {
	        PlatformAccessPluginLoader.get().getPlugin().refreshConfig();
	    }

	    log.debug("OMManager.refreshConfig: config has been refreshed");
	}

	public OAMManager getOamManager()
	{
	    return oamManager;
	}

    @Override
    public boolean shutdown() {
        log.warn("OMManager.shutdown: shutdown command received");
        operateMAS.shutdown();
        return true;
    }

}






