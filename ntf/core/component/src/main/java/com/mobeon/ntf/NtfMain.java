/**
 * All Rights Reserved
 */
package com.mobeon.ntf;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import com.abcxyz.messaging.common.oam.ConfigRefreshException;
import com.abcxyz.messaging.common.oam.ConfigRefreshListener;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.util.SystemPropertyHandler;
import com.abcxyz.messaging.oe.common.configuration.ConfigurationConstants;
import com.abcxyz.messaging.oe.common.perfmgt.PerformanceManagerExt;
import com.abcxyz.messaging.oe.common.subsystem.ManageableExt;
import com.abcxyz.messaging.oe.common.topology.ComponentSubsystemInfo;
import com.abcxyz.messaging.oe.common.util.KPIProfiler;
import com.abcxyz.messaging.oe.impl.bpmanagement.utils.ComponentSAUtils;
import com.abcxyz.messaging.oe.impl.configuration.ConfigurationDataManager;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.abcxyz.messaging.oe.lib.oamaccess.OeOamFacadeBuilder;
import com.abcxyz.messaging.vvs.ntf.notifier.NotifierPluginHandler;
import com.abcxyz.services.moip.alarms.MoipAlarmEvent;
import com.abcxyz.services.moip.alarms.MoipAlarmFactory;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.NtfCmnManager;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackUtil;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.coremgmt.reminder.ReminderUtil;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpErrorCodesConfig;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.cancel.CancelSmsHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.delayedevent.DelayedEventHandler;
import com.mobeon.ntf.out.email.EmailOut;
import com.mobeon.ntf.out.fax.FaxPrintOut;
import com.mobeon.ntf.out.mms.MMSOut;
import com.mobeon.ntf.out.outdial.OdlEventReceiver;
import com.mobeon.ntf.out.outdial.OutdialNotificationOut;
import com.mobeon.ntf.out.pager.PagOut;
import com.mobeon.ntf.out.sip.SIPOut;
import com.mobeon.ntf.out.sms.SMSConfigWrapper;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.out.sms.ServiceTypePidLookupImpl;
import com.mobeon.ntf.out.ss7.AlertSCPhoneOnListener;
import com.mobeon.ntf.out.vvm.VvmHandler;
import com.mobeon.ntf.out.wap.WapOut;
import com.mobeon.ntf.out.wireline.CmwOut;
import com.mobeon.ntf.out.wireline.WmwOut;
import com.mobeon.ntf.slamdown.SlamdownListHandler;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.util.EmailQueue;
import com.mobeon.ntf.util.ILoggerProxy;

import com.mobeon.common.util.logging.stdOutErrLogger;

/**
 * NtfMain starts NTF up, by creating various interface objects and reading
 * configurations, and then creating and starting notification handler threads.
 */
public class NtfMain implements ManageableExt, ConfigRefreshListener {
	@SuppressWarnings("unused")
	private static NtfMain ntf;
	private static LogAgent log;
	private static NtfEventHandler eventHandler;
	private EmailQueue emailQueue;
	private UserFactory userFactory;
	private MerAgent mer;
	private SMSOut smsOut = null;
	private OutdialNotificationOut odlOut = null;
	private MMSOut mmsOut = null;
	private WapOut wapOut = null;
	private EmailOut emailOut = null;
	private WmwOut wmwOut = null;
	private CmwOut cmwOut = null;
	private PagOut pagOut = null;
	private SIPOut sipOut = null;
	private SlamdownListHandler slamdownHandler = null;
	private FaxPrintOut faxPrintOut =null;
	private VvmHandler vvmHandler = null;
	private DelayedEventHandler delayedeventHandler = null;
	private CancelSmsHandler cancelSmsHandler = null;

	private OAMManager oamManager;
	private boolean configAlarmRaised = false;
	static{
		SystemPropertyHandler.setProperty("abcxyz.services.messaging.productPrefix", "moip");
		SystemPropertyHandler.setProperty("management_config_backup_root", "");
		SystemPropertyHandler.setProperty("ObjectId", "20");
		SystemPropertyHandler.setProperty("BaseOid", ".1.3.6.1.4.1.193.91.4");
		SystemPropertyHandler.setProperty("RootOid", ".1.3.6.1.4.1.193.91.");
		SystemPropertyHandler.setProperty("mms_root", "/opt/moip");
	}

	/**
	 * creates objects that are used by more than one subsystem of NTF
	 * @throws ConfigurationDataException exception
	 */
	public NtfMain() throws ConfigurationDataException {
		
		// Initiate logger
		String logConfigurationFile=null;
		try {  		
			logConfigurationFile=System.getProperty("log4j.configurationFile");
		} catch (Exception e) {
			//ignore -we use default
		}
		if (logConfigurationFile == null) {
			logConfigurationFile =  Config.getNtfHome() + "/cfg/log4j2.xml";
		}
		File file = new File(logConfigurationFile);
		if ( !file.exists()) {
			System.err.println("com.mobeon.ntf.NtfMain.NtfMain()  WARNING: " + logConfigurationFile + " not found, logging not intitialized properly.");
		} else {
			try {
				LoggerContext context = Configurator.initialize(null,logConfigurationFile);
				if (context != null) {
					//only start the redirect it initialized logging ok.
					System.out.println("com.mobeon.ntf.NtfMain.NtfMain() " + logConfigurationFile );
					System.out.println("com.mobeon.ntf.NtfMain.NtfMain() Redirecting stdout and stderr to log4j...");
					stdOutErrLogger.startRedirectAll(); //redirect msgcore stdout/stderr to log4j.
				} else {
					System.err.println("com.mobeon.ntf.NtfMain.NtfMain() failed to load log configuration:" + logConfigurationFile );
				}
			} catch (Exception e) {
				System.err.println("com.mobeon.ntf.NtfMain.NtfMain() failed to load log configuration:" + logConfigurationFile );
			}
		} 

		try {     
			// Register shutdown hook
			ShutdownHook hook = new ShutdownHook();
			Runtime runtime = Runtime.getRuntime();
			runtime.addShutdownHook(hook);

			log = NtfCmnLogger.getLogAgent(NtfMain.class);

			OEManager.setComponentType(ConfigurationConstants.NTF_COMPONENT_IDENTITY);
			OeOamFacadeBuilder facade  = new OeOamFacadeBuilder(ConfigurationConstants.NTF_COMPONENT_IDENTITY);

			try {
				oamManager = facade.getOamManager(ComponentSubsystemInfo.getComponentSubsystem(ConfigurationConstants.NTF_COMPONENT_IDENTITY)[0], this);

				ManagementInfo.get().setOamManager(oamManager);

				if (oamManager.getStateManager().isLocked())
				{
					ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.LOCKED);
				}
			} catch (ConfigurationDataException e) {
				log.fatal("Configuration problem, exiting..",e);
				ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
			}

			try {
				oamManager.getLogAgent().debug("Registering for refresh");
				ConfigurationDataManager cfgManager = ConfigurationDataManager.getInstance();

				// register for configuration changes
				oamManager.getLogAgent().debug("Registering for refresh");
				cfgManager.addConfigRefreshListener(this);
			} catch (IllegalArgumentException e) {
				oamManager.getLogAgent().fatal("NtfMain: Registering for refresh, exception: ", e);
				ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
			}

			log.info("* **//NTF version " + Config.getVersion() + ", installed in " + Config.getInstallDir() + ", started with home " + Config.getNtfHome());

			try {
				//load NTF configuration from NTF home
				Config.loadCfg();

				//load xmpErrorCodes configuration file and print its content.
				XmpErrorCodesConfig.setXmpErrorCodesFilePath(Config.getNtfHome() + "/cfg/xmpErrorCodes.conf");
				XmpErrorCodesConfig.loadCfg();
				log.info(XmpErrorCodesConfig.getXmpErrorCodesConfigAsString());

				//load the smppErrorCodes configuration file and print its content
				smsOut = SMSOut.get();
				if (smsOut == null) {
					log.fatal("NTF will be shuting down because it couldn't load the ntf configuration files " +
							" becauce SMSOut (" + Config.getNtfHome() + "/smppErrorCodes.conf) didn't return properly.");
					ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
				}
				log.info(SMSConfigWrapper.getSmppErrorCodesAsString());

				MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(MoipAlarmFactory.MoipAlarm.CONFIG_LOADING_FAILED);
				oamManager.getFaultManager().clearAlarm(alarm);

			} catch (ConfigurationDataException cdex) {
				log.fatal("NTF will be shuting down because it couldn't load the ntf configuration files " +
						"(notification.conf, xmpErrorCodes.conf, smppErrorCodes.conf) under the directory " +
						Config.getNtfHome() + ". Error: " + cdex.getMessage());
				MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(MoipAlarmFactory.MoipAlarm.CONFIG_LOADING_FAILED);
				oamManager.getFaultManager().raiseAlarm(alarm);
				ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
			}

			emailQueue = new EmailQueue(Config.getInternalQueueSize());
			eventHandler = new NtfEventHandler(emailQueue);

			// Load back-end configuration
			if (loadBackendOam()) {
				initNtfEventMgmt();
			} else {
				log.fatal("NtfMain: Can not start NtfCmnManager: no common OAM manager available");
				ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
			}

			XmpClient xmpClient = XmpClient.get();
			xmpClient.setLogger(new ILoggerProxy(XmpClient.class.getCanonicalName()));
			xmpClient.setClientId(ComponentSAUtils.getInstance().getComponentName());

			xmpClient.setValidity(Config.getXmpValidity());
			xmpClient.setTimeout(Config.getXmpTimeout());
			xmpClient.setPollInterval(Config.getXmpPollInterval());
			xmpClient.setRefreshTime(Config.getXmpRefreshTime());
			xmpClient.setManagementHandler(ManagementInfo.get());
			xmpClient.startRefresher();

			userFactory = new UserFactory();
			String compName = Config.getInstanceComponentName();
			if ((compName != null) && (compName.length() !=0)) {
				mer = MerAgent.get(compName);
			} else {
				mer = MerAgent.get();
			}

			mmsOut = new MMSOut();
			wapOut = new WapOut();
			emailOut = EmailOut.get();
			pagOut = new PagOut();
			cmwOut = new CmwOut();
			slamdownHandler = new SlamdownListHandler();
			faxPrintOut = new FaxPrintOut();
			vvmHandler = new VvmHandler();
			odlOut = new OutdialNotificationOut();
			sipOut = new SIPOut();
			delayedeventHandler = DelayedEventHandler.get();
			cancelSmsHandler = CancelSmsHandler.get();

			// Connect to all SMSc
			if (SMSOut.get() != null) {
				SMSOut.get().connectSmsUnits();
			}

			// Start NotificationHandler threads
			for (int i = 0; i < Config.getNotifThreads(); i++) {
				new NotificationHandler(i,
						emailQueue,
						eventHandler,
						userFactory,
						mer,
						smsOut,
						mmsOut,
						wapOut,
						odlOut,
						wmwOut,
						pagOut,
						cmwOut,
						sipOut,
						emailOut,
						slamdownHandler,
						faxPrintOut,
						vvmHandler, 
						delayedeventHandler,
						cancelSmsHandler).start();
			}

			try {
				//if using HLR and not using a custom (plug-in) - initialise the ss7.
				if(CommonMessagingAccess.getInstance().getSs7Manager().useHlr() &&
						!CommonMessagingAccess.getInstance().getSs7Manager().getSubStatusHlrInterrogationMethod().equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_CUSTOM)) {
					int alertSCRegistrationNumOfRetry = Config.getAlertSCRegistrationNumOfRetry();
					int alertSCRegistrationTimeInSecBetweenRetry = Config.getAlertSCRegistrationTimeInSecBetweenRetry();
					if(!CommonMessagingAccess.getInstance().getSs7Manager().registerAlertScHandlerWithRetry(AlertSCPhoneOnListener.getInstance(),alertSCRegistrationNumOfRetry,alertSCRegistrationTimeInSecBetweenRetry)){
						log.fatal("NTF will shutdown Unable to register alert SC)");
						ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
					}
				}
			} catch (Throwable e) {
				log.fatal("NtfMain: Error while registering alertsc handler, exception: ", e);
				ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
			}

			// Load and initialise the notifier plug-in.
			NotifierPluginHandler.get(); //first call loads plug-in(s)

			// Start MRD 
			NtfCmnManager.getInstance().startMrdListener();
		} catch (Throwable t) {
			if (log != null)
				log.fatal("NtfMain: Can not start due to uncaught exception ", t);
			else {
				System.err.print("FATAL: NtfMain: Can not start due to uncaught exception: ");
				t.printStackTrace(System.err);        
			}
			ManagementInfo.get().shutdownNtf();
		}

		//
		// Initiate KPI client if enabled
		//
		log.info("NtfMain(): logger = " + log.getClass());
		log.info("NtfMain(): Will initialize KPIProfiler in 1 minute");
		try { // wait for 1 minute to avoid initial counter value of 0
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		KPIProfiler.setStatsEnabled((PerformanceManagerExt) CommonMessagingAccess.getInstance().getOamManager().getPerformanceManager(),
				CommonMessagingAccess.getInstance().getOamManager().getLogger(), "NTF"); // HX56652
	}

    /**
     * Get the eventHandler singleton
     * @return NtfEventHandler
     */
    public static NtfEventHandler getEventHandler() {
        return eventHandler;
    }

    private boolean loadBackendOam() {
        Collection<String> configFilenames = new LinkedList<String>();

        String configFilename = "/opt/moip/config/backend/backend.conf";
        File backendXml = new File(configFilename);
        if (!backendXml.exists()) {
            configFilename = Config.getNtfHome() + "/cfg/backend.conf";
            backendXml = new File(configFilename);

            if (backendXml.exists() == false) {
                System.setProperty("backendConfigDirectory", Config.getNtfHome() + "/../ipms_sys2/backend/cfg");
                configFilename = Config.getNtfHome() + "/../ipms_sys2/backend/cfg/backend.conf";
                backendXml = new File(configFilename);
                if (backendXml.exists() == false) {
                    log.error("NtfMain failed to find backend.conf file. verify \"ntfHome\" is defined correctly.");
                    return false;
                }
            }
        }

        configFilenames.add(configFilename);
        IConfiguration configuration;
        try {
            configuration = new ConfigurationImpl(null,configFilenames,false);
            CommonMessagingAccess.getInstance().setConfiguration(configuration);
            NtfCmnManager.getInstance().setFaultManager(oamManager.getFaultManager());
            NtfCmnManager.getInstance().setPerformanceManager(oamManager.getPerformanceManager());
            CommonOamManager.getInstance().setFaultManager(NtfCmnManager.getInstance().getFaultManager());
            CommonOamManager.getInstance().setPerformanceManager(oamManager.getPerformanceManager());
            CommonMessagingAccess.getInstance().setServiceName(MoipMessageEntities.MESSAGE_SERVICE_NTF);
            CommonMessagingAccess.getInstance().init();
            return true;
        } catch (ConfigurationException e) {
            log.fatal("NtfMain: loadBackendOam, exception: ", e);
            ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
        }

        return false;
    }

    private void initNtfEventMgmt() {

        try {
            // Set NTF default event receiver
            NtfEventHandlerRegistry.registerDefaultEventReceiver(eventHandler);

            // Event handlers are registered in NtfCmnManager
            NtfEventHandlerRegistry.registerEventReceiver(NtfEventTypes.OUTDIAL.getName(), new OdlEventReceiver());
            NtfEventHandlerRegistry.registerEventReceiver(NtfEventTypes.SIPMWI.getName(), new OdlEventReceiver());
            NtfEventHandlerRegistry.registerEventReceiver(NtfEventTypes.SLAMDOWN_L3.getName(), new OdlEventReceiver());

            NtfCmnManager.getInstance().start();

        } catch (Exception e) {
            log.error("NtfMain: initNtfEventMgmt, exception: ", e);
            ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
        }
    }

    /**
     * Main function for NTF.
     *@param args not used.
     */
    public static void main(String[] args) throws Exception {
        try {
            if (args.length > 0) {
                System.out.println("Usage: java [-DntfHome=<directory>] [-DconfigFile=<file>] com.mobeon.ntf.NtfMain");
                System.out.println("       ntfHome sets the home of the NTF instance, where configuration shall be taken from and logs shall be written");
                System.out.println("       configFile sets the configuration file.");
                return;
            }

            ntf = new NtfMain();

            log.info("NTF started successfully.");
        } catch (Exception e) {
        	if (log != null) {
                log.error("NtfMain: Unexpected exception: ", e);
        	} else {
        		e.printStackTrace();
        	}
        	ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
        }
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
    	log.debug("NtfMain.Lock: ntf is locked");
    	ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.LOCKED);
    	return true;
    }

    @Override
    public boolean refreshTopology() {
    	// TODO Auto-generated method stub
    	return true;
    }

    @Override
    public boolean unlock() {
    	log.debug("NtfMain.Unlock: ntf is unlocked");
    	ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.UNLOCKED);
    	return true;
    }

    @Override
    public boolean shutdown() {
        ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
        return true;
    }

    @Override
    public void refreshConfig(long arg0) throws ConfigRefreshException {
    	log.debug("NtfMain.refreshConfig");
    	try {
    		Config.reloadConfig();
            ServiceTypePidLookupImpl.get().refreshConfig(); //this should be one of the first as it's referenced by other classes.
    		SMSConfigWrapper.refreshConfig();
    		SMSOut.get().connectSmsUnits();
    		FallbackUtil.refreshConfig();
    		ReminderUtil.refreshConfig();
            CancelSmsHandler.refreshConfig();
    		NotifierPluginHandler.get().refreshConfig(); //make sure to refresh plug-in last.    		
    	} catch (ConfigurationDataException e) {
    		log.error("NtfMain: refreshConfig: exception while refreshing: " + e.getMessage());
    		if(!configAlarmRaised) {
    			MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(MoipAlarmFactory.MoipAlarm.CONFIG_LOADING_FAILED);
    			oamManager.getFaultManager().raiseAlarm(alarm);
    			configAlarmRaised = true;
    		}
    	}

		if(configAlarmRaised) {
			MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(MoipAlarmFactory.MoipAlarm.CONFIG_LOADING_FAILED);
			oamManager.getFaultManager().clearAlarm(alarm);
			configAlarmRaised = false;
        }
    }

                  
}

