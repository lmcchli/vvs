/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.abcxyz.services.moip.common.ss7.ISs7Manager;
import com.mobeon.common.configuration.*;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.operateandmaintainmanager.OMManager;
import com.mobeon.masp.operateandmaintainmanager.OperateMAS.AdminState;
import com.mobeon.masp.util.component.IComponentManager;
import com.mobeon.masp.util.component.SpringComponentManager;
import com.mobeon.masp.execution_engine.events.MASStarted;
import com.mobeon.masp.execution_engine.platformaccess.AlertScPhoneOnHandler;
import com.mobeon.masp.execution_engine.runtime.RuntimeConstants;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.util.logging.stdOutErrLogger;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.eventnotifier.MulticastDispatcher;
import org.springframework.beans.BeansException;
import com.abcxyz.messaging.common.hlr.HlrAccessManager;
import com.abcxyz.messaging.oe.common.perfmgt.PerformanceManagerExt;
import com.abcxyz.messaging.oe.common.util.KPIProfiler;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.cmnaccess.oam.MoipOamManager;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.io.File;


/**
 * The "main" method in the execution engine package, responsible for spawning the execution engine component and any component
 * required by the execution engine. In reality the main method of the whole MAS. Is responsible for putting the pieces together and
 * creating/starting of the necessary objects.
 * 
 * @author David Looberger
 */
public class Main {

    static private ILogger logger;

    /**
     * The main routine of the execution engine, and therefore also the whole MAS. Insatiate
     * 
     * @param args
     * 
     * @logs.error "Failed to create ComponentManager, bailing out" - Failed to instanciate the ComponentManager. Most likely due to
     *             a bad configuration (ComponentConfig.xml)
     * 
     * @logs.error "Failed to create ExecutorServiceManager, bailing out" - Failed to create the ExecutorServiceManager probably due
     *             to a misconfiguration in the Spring configuration file (ComponentManager.xml)
     * 
     * @logs.fatal "Could not create CallManager." - Failed to start the CallManager. Could be caused by the target port being used
     *             by a running callmanagar. No incomming calls can be accepted, and hence the MAS is shut down.
     */
    public static void main(String[] args) {
    	
    	// Initiate logger
    	String logConfigurationFile=null;	
    	try {  		
    		logConfigurationFile=System.getProperty("log4j.configurationFile");
    	} catch (Exception e) {
    		//ignore -we use default
    	}
    	if (logConfigurationFile == null) {
    		logConfigurationFile="/opt/moip/config/mas/log4j2.xml";
    	}
    	File file = new File(logConfigurationFile);
    	if ( !file.exists()) {
    		System.err.println("com.mobeon.masp.execution_engine.Main  WARNING: " + logConfigurationFile + " not found, logging not intitialized properly.");
    	} else {
    		try {
    			LoggerContext context = Configurator.initialize(null,logConfigurationFile);
    			if (context != null) {
    				//only start the redirect it initialized logging ok.
    				System.out.println("com.mobeon.masp.execution_engine.Main " + logConfigurationFile );
    				System.out.println("com.mobeon.masp.execution_engine.Main Redirecting stdout and stderr to log4j...");
    				stdOutErrLogger.startRedirectAll(); //redirect msgcore stdout/stderr to log4j.
    			} else {
    				System.err.println("com.mobeon.masp.execution_engine.Main failed to load log configuration:" + logConfigurationFile );
    			}
    		} catch (Exception e) {
    			System.err.println("com.mobeon.masp.execution_engine.Main failed to load log configuration:" + logConfigurationFile );
    		}
    	} 
                                       
        logger = ILoggerFactory.getILogger(Main.class);
        logger.info("MAS starting up...");
        
        printAllEnvVaribles();

        // Create an ComponentManager

        IComponentManager compManager = null;
        try {
            compManager = SpringComponentManager.getInstance();
        } catch (Exception e) {
            logger.error("Failed to create ComponentManager, bailing out",e);
            System.exit(0);
        }
        if(compManager==null)
        {
            logger.error("ComponentManager is null, bailing out");
            System.exit(0);
        }

        OMManager masOmManager=null;

        try {
            masOmManager = (OMManager)compManager.create("OmManager", OMManager.class);
        } catch (Exception e) {
            logger.error("Failed to create OMManager, bailing out", e);
            System.exit(0);
        }
        if(masOmManager==null)
        {
            logger.error("Failed to create OMManager, is null");
            System.exit(0);

        }
        
        ISs7Manager ss7Manager = CommonMessagingAccess.getInstance().getSs7Manager();
        try { // HZ23356, HZ97095
            if (ss7Manager.useHlr() && !ss7Manager.getSubStatusHlrInterrogationMethod().equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_CUSTOM)) {
                // HZ57598 use other way than masOmManager.getOamManager().getConfigManager() to get config
                IConfigurationManager configurationManager = new ConfigurationManagerImpl();
                configurationManager.setConfigFile("/opt/moip/config/mas/masSpecific.conf");
                IConfiguration iConf = configurationManager.getConfiguration();
                IGroup iGrp = iConf.getGroup(RuntimeConstants.CONFIG.GROUP_NAME);
                //int alertSCRegistrationNumOfRetry = masOmManager.getOamManager().getConfigManager().getIntValue(RuntimeConstants.CONFIG.ALERT_SC_REGISTRATION_NUMBER_OF_RETRY);
                int alertSCRegistrationNumOfRetry = iGrp.getInteger(RuntimeConstants.CONFIG.ALERT_SC_REGISTRATION_NUMBER_OF_RETRY);
                if (alertSCRegistrationNumOfRetry == -1)
                    alertSCRegistrationNumOfRetry = RuntimeConstants.CONFIG.ALERT_SC_REGISTRATION_NUMBER_OF_RETRY_DEFAULT_VALUE;
                //int alertSCRegistrationTimeInSecBetweenRetry = masOmManager.getOamManager().getConfigManager().getIntValue(RuntimeConstants.CONFIG.ALERT_SC_REGISTRATION_SEC_BETWEEN_RETRY);
                int alertSCRegistrationTimeInSecBetweenRetry = iGrp.getInteger(RuntimeConstants.CONFIG.ALERT_SC_REGISTRATION_SEC_BETWEEN_RETRY);
                if (alertSCRegistrationTimeInSecBetweenRetry == -1)
                    alertSCRegistrationTimeInSecBetweenRetry = RuntimeConstants.CONFIG.ALERT_SC_REGISTRATION_SEC_BETWEEN_RETRY_DEFAULT_VALUE;
                if (!CommonMessagingAccess.getInstance().getSs7Manager().registerAlertScHandlerWithRetry(AlertScPhoneOnHandler.getInstance(), alertSCRegistrationNumOfRetry, alertSCRegistrationTimeInSecBetweenRetry)) {
                    logger.error("Unable to register alertSC MAS will shutdown");
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
                
        logger.info("MAS Main.main(): calling InitHlrAccessManager()"); 
        InitHlrAccessManager(); // VFE_NL MFD
        logger.info("MAS Main.main(): calling initKpi()");
        initKpi();
        
        // First instantiate the ExecutorServiceManager in order to configure it
        try {
            compManager.create("ExecutorServiceManager", ExecutorServiceManager.class);
        } catch (Exception e) {
            logger.error("Failed to create ExecutorServiceManager, bailing out", e);
            System.exit(0);
        }

        // Instantiate a CallManager
        try {
        	compManager.create(
                    "CallManager", CallManager.class);
        } catch (BeansException e) {
            logger.error("Could not create CallManager.", e);
            System.exit(0);
        }

        // fire a global event so dependent components may see that startup is complete
        MulticastDispatcher eventDispatcher = new MulticastDispatcher();
        eventDispatcher.fireGlobalEvent(new MASStarted());
        
        if (logger.isInfoEnabled()) logger.info("MAS started!");
                                

        boolean testEnv =  Boolean.getBoolean("TEST");
        if (testEnv){
        	OMManager.getOperateMAS().setAdminState(AdminState.UNLOCK);
        }

    }
    
    private static void printAllEnvVaribles() {
        logger.info("\n====================================\n   Current system environment variables\n====================================\n\n");
        Map<String, String> envs = System.getenv();

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : envs.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        sb.append("\n");
        logger.info(sb.toString());
    }
    
    
    ///////////////////////////////////////////////////////////////// VFE_NL MFD
    
    private static final String BACK_END_CONF = "backend.conf";
    
    private static void InitHlrAccessManager() {
        IGroup backEndGroup = null;
        boolean tryLoadBackendConf = false;
        try {
            backEndGroup = CommonOamManager.getInstance().getConfiguration().getGroup(BACK_END_CONF);
        } catch (Exception e) {
            logger.warn("Main.main().InitHlrAccessManager(): exception calling CommonOamManager.getInstance().getConfiguration().getGroup(GlobalDirectoryAccessUtil.BACK_END_CONF); " +
                    "will try to load : " + BACK_END_CONF + ": " + e);
            tryLoadBackendConf = true;
        }
        if (backEndGroup == null || tryLoadBackendConf) { // load backend.conf
            backEndGroup = loadBackendConfig();
            if(backEndGroup == null){
                logger.error("Main.main().InitHlrAccessManager(): failed to load " + BACK_END_CONF);
                return;
            }
        }

        try {
            if (!(backEndGroup.getBoolean(ConfigParam.ENABLE_HLR_ACCESS))) {
                System.out.println("Main.main().InitHlrAccessManager(): Cm.enableHlrAccess in backend.conf is set to false; will not load HlrAccessManager");
                return;
            }
        } catch (Exception e) {
            logger.error("Main.main().InitHlrAccessManager(): failed to get value for " + ConfigParam.ENABLE_HLR_ACCESS + ": " + e);
            e.printStackTrace();
            return;
        }

        String hlrMethod = "";
        try {
            hlrMethod = backEndGroup.getString(ConfigParam.SUBSCRIBER_STATUS_HLR_METHOD);
        } catch (Exception e) {
            logger.error("Main.main().InitHlrAccessManager(): failed to get value for " + ConfigParam.SUBSCRIBER_STATUS_HLR_METHOD + ": " + e);
            e.printStackTrace();
            return;
        }

        logger.info("Main.main().InitHlrAccessManager(): @@@HLR Access method = " + hlrMethod);
        
        if (!(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_CUSTOM.equalsIgnoreCase(hlrMethod))) {
            logger.info("Main.main().InitHlrAccessManager(): @@@HLR Access method != " + ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_CUSTOM + "; will not load HlrAccessManager");
            return;
        }

        logger.info("Main.main().InitHlrAccessManager(): @@@trying to load HlrAccessManager");
        HlrAccessManager ham = null;
        try {
            ham = CommonMessagingAccess.getInstance().getHlrAccessManager();
        } catch (Exception e) {
            ham = null;
        }
        if (ham != null) {
            logger.info("Main.main().InitHlrAccessManager(): call to CommonMessagingAccess.getInstance().getHlrAccessManager() succeeded");
            return;
        }
        
        logger.info("Main.main().InitHlrAccessManager(): call to CommonMessagingAccess.getInstance().getHlrAccessManager() returned null; try get HlrAccessManager directly");
        try {
            ham = HlrAccessManager.getInstance(backEndGroup.getString(ConfigParam.SUBSCRIBER_STATUS_HLR_METHOD_CUSTOM_CLASS_PATH),
                    backEndGroup.getString(ConfigParam.SUBSCRIBER_STATUS_HLR_METHOD_CUSTOM_CONFIG_FILE), new MoipOamManager());
        } catch (Exception e) {
            logger.error("Main.main().InitHlrAccessManager(): exception getting config or calling HlrAccessManager.getInstance(): " + e);
            e.printStackTrace();
            return;          
        }
        if (ham == null) {
            logger.error("Main.main().InitHlrAccessManager(): failed to get HlrAccessMaanger by calling HlrAccessManager.getInstance()");
            return;
        }
        CommonMessagingAccess.setHlrAccessManager(ham); // will be there for next times we call CommonMessagingAccess.getInstance().getHlrAccessManager() 
        
    }
    
    ////////////////////////////////////////////////// KIP
    private static void initKpi() {
        //
        // Initiate KPI client if enabled
        //
    	logger.info("MAS Main(): logger = " + logger.getClass());
    	logger.info("MAS Main(): Will initialize KPIProfiler");
    	KPIProfiler.setStatsEnabled((PerformanceManagerExt) CommonMessagingAccess.getInstance().getOamManager().getPerformanceManager(),
			CommonMessagingAccess.getInstance().getOamManager().getLogger(), "MAS"); // HX56652
    	/******************************** params reading moved to KPIProfiler
        ConfigManager cm = CommonMessagingAccess.getInstance().getOamManager().getLocalConfig();
        String kpiEnabled = cm.getParameter(ConfigParam.ENABLE_KPI_DATA);        
    	logger.info("MAS Main(): kpiEnabled = " + kpiEnabled);
        if (Boolean.parseBoolean(kpiEnabled)) {        	
        	String url = cm.getParameter(ConfigParam.KPI_SERVER_URL);
        	if (url == null || url.isEmpty()) url = KPIProfiler.DEFAULT_KPI_SERVER_URL;
        	String intervalStr = cm.getParameter(ConfigParam.KPI_SEND_INTERVAL);
        	int interval = ConfigParam.DEFAULT_KPI_SEND_INTERVAL;
        	try {
        		interval = Integer.parseInt(intervalStr);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	String cn =cm.getParameter(ConfigParam.KPI_COUNTER_NAMES);
        	if (cn == null) cn = "";
        	logger.info("MAS Main(): Will initialize KPIProfiler with url = " + url + " interval = " + 
        	            interval + " counter names = " + cn);        	
        	KPIProfiler.setStatsEnabled(true, 
        			(PerformanceManagerExt) CommonMessagingAccess.getInstance().getOamManager().getPerformanceManager(),
        			CommonMessagingAccess.getInstance().getOamManager().getLogger(),
        			url, interval, cn.split("\\|"));
        } else {
        	logger.info("MAS Main(): KPI disabled");
        }
        ******************************************************/

    }
    
    
    private static IGroup loadBackendConfig() {
        IGroup bg = null;
        Collection<String> configFilenames = new LinkedList<String>();
        String configFilename = "/opt/moip/config/backend/backend.conf";
        File backendXml = new File(configFilename);
        if (!backendXml.exists()) {
            logger.error("PlatformAccessImpl.loadBackendConfig(): cannot find " + configFilename);
            return bg;
        }

        configFilenames.add(configFilename);
        IConfiguration configuration;
        try {
            configuration = new ConfigurationImpl(null,configFilenames,false);
            bg = configuration.getGroup(BACK_END_CONF);
        } catch (Exception e) {
            logger.error("PlatformAccessImpl.loadBackendConfig(): error trying to init COmmonMessagingAccess with backend.conf: " + e);
            e.printStackTrace();
        } finally {
            return bg;
        }

    }

    

}
