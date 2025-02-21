package com.mobeon.common.cmnaccess;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.abcxyz.messaging.cdrgen.CDRRecord;
import com.abcxyz.messaging.cdrgen.config.CDRGenConfig;
import com.abcxyz.messaging.common.hlr.HlrAccessManager;
import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.Container3;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.Message;
import com.abcxyz.messaging.common.message.MessageIdentifier;
import com.abcxyz.messaging.common.message.MessageStreamingResult;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.message.MultiNameValuePairs;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.DefaultCounterEvent;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.PerformanceEvent;
import com.abcxyz.messaging.common.oam.PerformanceManager;
import com.abcxyz.messaging.common.util.crypto.PasswordFactory;
import com.abcxyz.messaging.identityformatter.IdentityFormatter;
import com.abcxyz.messaging.identityformatter.IdentityFormatterInvalidIdentityException;
import com.abcxyz.messaging.mfs.MFS;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mfs.MsgStoreServerFactory;
import com.abcxyz.messaging.mfs.data.MessageFileHandle;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.data.MfsBooleans;
import com.abcxyz.messaging.mfs.data.MfsFileHandle;
import com.abcxyz.messaging.mfs.data.Range;
import com.abcxyz.messaging.mfs.data.StateFileHandle;
import com.abcxyz.messaging.mfs.exception.CollisionException;
import com.abcxyz.messaging.mfs.exception.IdGenerationException;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.message.MessageServices;
import com.abcxyz.messaging.mfs.message.MfsStateFolderType;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.MessageContext;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.messaging.mrd.oamplugin.DispatcherConfigMgr;
import com.abcxyz.messaging.mrd.operation.DispatcherOperations;
import com.abcxyz.messaging.mrd.operation.InformEventReq;
import com.abcxyz.messaging.mrd.operation.InformEventResp;
import com.abcxyz.messaging.mrd.operation.TriggerSendMessageReq;
import com.abcxyz.messaging.mrd.operation.TriggerSendMessageResp;
import com.abcxyz.messaging.mrd.server.DispatcherServer;
import com.abcxyz.messaging.mrd.util.COSRetentionDaysChangedEvent;
import com.abcxyz.messaging.oe.common.geosystems.GeoSystems;
import com.abcxyz.messaging.oe.common.topology.ComponentInfo;
import com.abcxyz.messaging.oe.common.util.KPIProfiler;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.AppliEventOperations;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageIDGen;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryUpdater;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.ss7.ISs7Manager;
import com.abcxyz.services.moip.common.ss7.Ss7Exception;
import com.abcxyz.services.moip.common.ss7.Ss7Manager;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.abcxyz.services.moip.masevent.MessageCleaner;
import com.abcxyz.services.moip.masevent.SlamdownEventHandler;
import com.abcxyz.services.moip.masevent.messagedeposit.MessageDepositEventManager;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.abcxyz.services.moip.provisioning.businessrule.ProvisioningConstants;
import com.mobeon.common.cmnaccess.CosRetentionDaysChangedEventHandler.MessageExpirySetter;
import com.mobeon.common.cmnaccess.CosRetentionDaysChangedEventHandler.StateFileFetcher;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.common.configuration.UnknownParameterException;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.util.FaxPrintStatus;

import abcxyz.services.messaging.ssmg.util.SsmgConfigurationData;


/**
 * Class provides accessing to common messaging handling through MFS
 */

/**
 * @author ealebie
 */
public class CommonMessagingAccess implements ICommonMessagingAccess {

	private static final String PROFILER_CHECKPOINT_NAME_SEND_PREFIX = "MAS.BKD.CMA.Send.";
	private static final String PROFILER_CHECKPOINT_NAME_INFORM_PREFIX = "MAS.BKD.CMA.Inform.";    
	private static final String PROFILER_CHECKPOINT_NAME_OK_RESULT_SUFFIX = "(OK)";
	private static final String PROFILER_CHECKPOINT_NAME_FAILED_RESULT_SUFFIX = "(Failed)";

	private static final String CLASSNAME = "CommonMessagingAccess";

	private static final int INVALID_SUBSCRIBER_TYPE = 0;
	private static final int NUMBER_SUBSCRIBER_TYPE = 1;
	private static final int URL_SUBSCRIBER_TYPE = 2;
	private static final int STRING_SUBSCRIBER_TYPE = 3;

	static protected MsgStoreServer mfs = MsgStoreServerFactory.getMfsStoreServer();

	static protected DispatcherServer mrd;

	static protected AppliEventOperations eventHandler;

	static protected String schedulerInstanceId = "0";

	static private MessageDepositEventManager messageDepositEventManager = new MessageDepositEventManager();

	static private SlamdownEventHandler slamdownEventHandler = new SlamdownEventHandler();

	static private MessageCleaner messageCleaner = new MessageCleaner();

	static private CosRetentionDaysChangedEventHandler cosRetentionDaysChangedEventHandler = null;
	/**
	 * The object to access MCD
	 */
	static protected IDirectoryAccess directoryAccess = null;

	static protected ISs7Manager ss7Manager = null;

	static protected HlrAccessManager hlrAccessManager = null; // VF_NL MFD
	static private CommonMessagingAccess instance = new CommonMessagingAccess();

	/**
	 * The configuration object
	 */
	private IConfiguration configuration = null;

	/**
	 * application's service name
	 */
	private String serviceName;

	private boolean masProxy=false;

	public final static String MSG_CLASS = "moip";

	private boolean systemReady = false;

	private boolean storeMessageInRecipientsInbox = false;

	/**
	 * Common oam Manager Object
	 */
	protected CommonOamManager oamManager = CommonOamManager.getInstance();
	ConfigManager mrdConfig = null;

	/**
	 * The logger object
	 */
	private static final LogAgent logger = CommonOamManager.getInstance().getLogger();

	private VVSMonitor monitor = null;

	static {
		System.setProperty(CommonOamManager.EMS_ROOT, "/opt");
	}

	private IdentityFormatter normalizationFormatter = null;

	public static final String NORMALIZATION_CONFIG_PATH = "/opt/global/common/formattingrules.conf";

	public static final ThreadLocal<DateFormat> DateFormatter = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
		}
	};

	protected CommonMessagingAccess() {
	}

	public static CommonMessagingAccess getInstance() {
		return instance;
	}

	public void initConfig() {
		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::initConfig() called.");
		}
		oamManager.setConfiguration(configuration);
		oamManager.setProfilerAgent();
		// setCDRGenRepositoryPerComponent();
		setSs7InstanceId();
		setSs7SliceId();
	}

	private void initializeMonitor(){
		if (monitor != null) {
			if (logger != null)
				logger.info("initializeMonitor() : local host monitor already started");
			return;
		}
		String portAsString = null;
		try {
			//oamManager.getConfigManager().getParameter("MonitorPort");

			//if ((portAsString == null) || (portAsString.length()== 0)){
			if (serviceName.equalsIgnoreCase(MoipMessageEntities.MESSAGE_SERVICE_MAS)) {
				portAsString = "1234";
			}else if(serviceName.equalsIgnoreCase(MoipMessageEntities.MESSAGE_SERVICE_NTF)) {
				portAsString = "1235";
			}

			//                if (logger != null){
			//                    logger.info("MinitializeMonitor() : No value found for parameter MonitorPort" +
			//                                  " so starting on default " + portAsString);
			//                }
			//}

			int port = Integer.parseInt(portAsString);

			String base64MD5EncrypedPassword = PasswordFactory.getPassword("md5").encodeAsBase64("1234");//oamManager.getConfigManager().getParameter("MonitorPassword");
			monitor = new VVSMonitor(serviceName.toUpperCase(), port, base64MD5EncrypedPassword, oamManager.getMcdOam());

			if (logger != null){
				logger.info("initializeMonitor() : monitor started on port:" + port);
			}
		}
		catch (Exception ioe) {
			if (logger != null) {
				logger.error("IOException initializing Monitor on port " + portAsString + "!");
			}
		}
	}

	public void stop() {
		if(mrd != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("CommonMessagingAccess::stop() - mrd will be stopped.");
			}
			mrd.stop();
		}
	}

	/**
	 * queries if system can take traffic
	 */
	public boolean isSystemReady() {
		return systemReady;
	}

	void setSystemReady() {
		systemReady = true;
	}


	protected void initMcd() {
		if(directoryAccess == null) {
			if(logger.isDebugEnabled()) {
				logger.debug("CommonMessagingAccess::initMcd() - mcd will be initialized.");
			}
			directoryAccess = DirectoryUpdater.getInstance();
		}
	}

	static public void setMcd(final IDirectoryAccess myDirAccess) {
		directoryAccess = myDirAccess;
	}

	public IDirectoryAccess getMcd() {
		return directoryAccess;
	}

	protected void initSs7Manager() {
		if(ss7Manager == null) {
			try {
				ss7Manager = Ss7Manager.getInstance(CommonOamManager.getInstance().getSs7Oam() );
			} catch (Ss7Exception e) {
				logger.error("CommonMessagingAccess::initSs7Manager exception: "  + e.getMessage(),e);
				e.printStackTrace();
			}
		}
	}

	static public void setSs7Manager(final ISs7Manager ss7Mgr) {
		ss7Manager = ss7Mgr;
	}

	public ISs7Manager getSs7Manager(){
		return ss7Manager;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////// VF_NL MFD
	protected void initHlrAccessManager() {
		if(hlrAccessManager == null) {
			try {
				OAMManager hlrAccessOam = CommonOamManager.getInstance().getHlrAccessOam();
				ConfigManager conMgr = hlrAccessOam.getConfigManager(); 
				if (conMgr == null) { // in case HLR access method != custom, no Config Mgr was created in CommonOamManager.getHlrAccessConfiguration()
					return; 
				}
				String classPath = conMgr.getParameter(ConfigParam.SUBSCRIBER_STATUS_HLR_METHOD_CUSTOM_CLASS_PATH);
				String configFile  = conMgr.getParameter(ConfigParam.SUBSCRIBER_STATUS_HLR_METHOD_CUSTOM_CONFIG_FILE);
				if (classPath == null || configFile == null || classPath.isEmpty() || configFile.isEmpty()) {
					logger.error("CommonMessagingAccess::initHlrAccessManager: classPath == null || configFile == null: " + classPath + " " + configFile);
					return;
				}
				hlrAccessManager = HlrAccessManager.getInstance(classPath, configFile, hlrAccessOam);
			} catch (Exception e) {
				logger.error("CommonMessagingAccess::initHlrAccessManager exception: "  + e.getMessage(), e);
				e.printStackTrace();
			}
		}
	}

	static public void setHlrAccessManager(final HlrAccessManager hlrAccessMgr) {
		hlrAccessManager = hlrAccessMgr;
	}

	public HlrAccessManager getHlrAccessManager() {
		if (hlrAccessManager == null) initHlrAccessManager();
		return hlrAccessManager;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////// VF_NL MFD

	/**
	 * return MUID from a subscriber's ID
	 */
	public String getMuid(final String subscriber) {
		String muid = null;

		IDirectoryAccessSubscriber sub = directoryAccess.lookupSubscriber(subscriber);

		if (sub != null) {
			muid = sub.getSubscriberIdentity(MCDConstants.IDENTITY_SCHEME_MUID);
		}

		return muid;
	}

	/**
	 * return TEL from a subscriber's ID
	 */

	public String getTel(final String subscriber) {
		String tel = null;

		IDirectoryAccessSubscriber sub = directoryAccess.lookupSubscriber(subscriber);

		if (sub != null) {
			tel = sub.getSubscriberIdentity(MCDConstants.IDENTITY_SCHEME_TEL);
		}

		return tel;
	}

	/**
	 * return MSA from a subscriber's ID
	 */
	public MSA getMsid(final String subscriber) {
		return getMsid(subscriber,null);
	}

	/**
	 * return MSA from a subscriber's ID
	 *
	 * @param recipientMSID - recipients MSID to be used as template for external depositer
	 */
	public MSA getMsid(final String subscriber,final MSA recipientMSID) {
		MSA msa = null;

		IDirectoryAccessSubscriber sub = directoryAccess.lookupSubscriber(subscriber);

		if(sub != null) {
			msa = new MSA(sub.getSubscriberIdentity(MCDConstants.IDENTITY_SCHEME_MSID));
		} else {
			//GEO-Distribution, If SUB is external use recipient MSID as template
			//get recipient from message
			try {

				int subscriberValueType = INVALID_SUBSCRIBER_TYPE;

				// check if numbers - common case - most of the time
				if(subscriber.matches("\\+?\\d+")){
					subscriberValueType = NUMBER_SUBSCRIBER_TYPE;
				} else {
					// check if there is a scheme - rarely used.
					// we want to check url genereric case for tel:, sip: etc.
					try {
						URI uri = new URI (subscriber);
						if (uri.getScheme() != null) {
							subscriberValueType = URL_SUBSCRIBER_TYPE;
						} else {
							subscriberValueType = STRING_SUBSCRIBER_TYPE;
						}
					}  catch (URISyntaxException e) {
						// can be any string such as widheld number.
						subscriberValueType = STRING_SUBSCRIBER_TYPE;
					}

				}

				// check if numbers - common case - most of the time
				if( subscriberValueType == NUMBER_SUBSCRIBER_TYPE || subscriberValueType == URL_SUBSCRIBER_TYPE ){
					if ( recipientMSID != null ){
						msa = MFSFactory.getGen2MSA(subscriber, false,recipientMSID);
					} else {
						msa = MFSFactory.getGen2MSA(subscriber, false);
					}
				} else if (subscriberValueType == STRING_SUBSCRIBER_TYPE ) {
					if ( recipientMSID != null ){
						String eid = MFSFactory.getGen2Eid(recipientMSID.getId());
						msa = new MSA (eid,false);
					} else {
						String msg = "CommonMessagingAccess::getMsid() should not be called with recipientMSID equal to null";
						logger.error (msg);
						throw new NullPointerException(msg);
					}
				} else {
					logger.error("CommonMessagingAccess::getMsid invalid subscriber string : "+subscriber );
				}

			} catch(final IdGenerationException e) {
				logger.error("CommonMessagingAccess::getMsid exception: "  + e.getMessage(),e);
			}
		}


		return msa;
	}
	static public MFS getMfs() {
		return mfs;
	}

	/**
	 * Initialises MFS
	 */
	protected void initMfs() {
		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::initMfs() - mfs will be initialized.");
		}
		final OAMManager mfsOam = oamManager.getMfsOam();
		MFSFactory.setOamManager(mfsOam);
		MsgStoreServer.resetInstance();
		//scheduler = SchedulerFactory.getSchedulerManager();

	}

	/**
	 * Reinitialise MFS configuration
	 *
	 * @param _configMgr new configuration
	 */
	public void reInitializeMfs(final ConfigManager _configMgr) {
		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::reInitializeMfs() called.");
		}
		final OAMManager mfsOam = oamManager.getMfsOam();
		mfsOam.setConfigManager(_configMgr);
		MFSFactory.setOamManager(mfsOam);
		MsgStoreServer.resetInstance();
	}

	/**
	 * Reinitialise MFS configuration
	 *
	 * @param _configMgr new configuration
	 */
	public void reInitializeMrd(final ConfigManager _configMgr) {
		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::reInitializeMrd() called.");
		}
		final OAMManager mrdOam = oamManager.getMrdOam();
		mrdOam.setConfigManager(_configMgr);
	}

	static public DispatcherOperations getDispacher() {
		if (mrd == null) {
			return null;
		}
		return mrd.getFacade();
	}

	/**
	 * Initialises MRD
	 */
	protected void initMrd() {
		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::initMrd() called.");
		}

		mrdConfig = oamManager.getMrdOam().getConfigManager();
		final OAMManager mrdOam = oamManager.getMrdOam();

		try {
			mrdOam.getConfigManager().setParameter(DispatcherConfigMgr.MrdServerPrimaryRemotePort, "0");
			String param = oamManager.getLocalConfig().getParameter(MoipMessageEntities.MasEventsRootPath);
			File path = new File(param);
			if (!path.exists()) { //TODO this can be just temporary for easy setup
				path.mkdirs();
			}
			mrdOam.getConfigManager().setParameter(DispatcherConfigMgr.EventsRootPath, param);
			if(logger.isDebugEnabled()) {
				logger.debug("CommonMessagingAccess::initMrd() " + DispatcherConfigMgr.EventsRootPath + " is set to " + param);
			}
		} catch (ConfigurationDataException e) {
			logger.error("CommonMessagingAccess::initMrd() " + e.getMessage(),e);
		}

		DispatcherServer.setOamManager(mrdOam);

		mrd = DispatcherServer.getInstance();

		if(!mrd.start()) {
			logger.fatal("Starting MRD failed, check logs ...");
			return;
		}

		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::initMrd() - mrd is started");
		}
	}

	/**
	 * this method can be called only after MRD is started
	 */
	protected void initMasEventHandlers() {
		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::initMasEventHandlers() called.");
		}

		messageDepositEventManager.startRetryEventHandler(MoipMessageEntities.SERVICE_NAME_MSG_DEPOSIT_NOTIF);
		slamdownEventHandler.start(serviceName);
		eventHandler = slamdownEventHandler.getEventOperator();
		messageCleaner.start(MoipMessageEntities.SERVICE_TYPE_MESSAGE_DELETE);
	}

	public AppliEventOperations getMasEventOpeartor() {
		return slamdownEventHandler.getEventOperator();
	}

	public MessageCleaner getMessageCleaner(){
		return messageCleaner;
	}

	protected static MFS getMFS() {
		return MsgStoreServerFactory.getMfsStoreServer();
	}

	public CommonOamManager getOamManager() {
		return oamManager;
	}

	public void setOamManager(CommonOamManager oamManager) {
		this.oamManager = oamManager;
	}

	public IConfiguration getConfiguration() {
		return configuration;
	}


	public void setServiceName(String serviceName)
	{
		this.serviceName=serviceName;
	}


	public void setMasProxy(boolean masProxy)
	{
		this.masProxy=masProxy;
	}

	public boolean isMasProxy()
	{
		return this.masProxy;
	}

	/**
	 * entry point of the application from named service startup
	 */
	public void setConfiguration(IConfiguration configuration) {
		this.configuration = configuration;
	}

	public void init() {
		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::init() called for serviceName = " + serviceName + ", configuration = " + configuration.toString());
		}

		instance.initConfig();
		instance.initMfs();

		if (serviceName.equalsIgnoreCase(MoipMessageEntities.MESSAGE_SERVICE_MAS)) {

			// check if masProxy. 
			// don't start the mrd in this case.

			if (!isMasProxy ()) {
				instance.initMrd();
				instance.initMasEventHandlers();
			}    	   
			SystemHealthCheckHandler handler = new SystemHealthCheckHandler();
			// check McdProxy only, so pass MCD OAM
			handler.startComponentChecking(instance.getOamManager().getMcdOam());

			storeMessageInRecipientsInbox = oamManager.getLocalConfig().getBooleanValue(ConfigParam.MAS_STORE_MSG_BODY_IN_RECIPIENTS_INBOX);
		}



		//HK89551: This needs to be done after setMcdExtraParameters() in MAS, for MCD to get the proper config
		instance.initMcd();

		instance.initSs7Manager();
		instance.initHlrAccessManager(); // VFE_NL MFD

		initializeMonitor();

		try {
			String cfgFile = System.getProperty("normalizationconfig", NORMALIZATION_CONFIG_PATH);
			ConfigManager ruleFile = OEManager.getConfigManager(cfgFile, logger);
			normalizationFormatter = new IdentityFormatter(oamManager.getMfsOam(), ruleFile);
		}
		catch (Exception e) {
			logger.error("CommonMessagingAccess.init() unable to initialize the IdentityFormatter, normalization will not be performed."+e.getMessage(),e);
		}

		initCDR();
	}


	private void setSs7InstanceId() {
		ConfigManager ss7ConfigManager = instance.getOamManager().getSs7Oam().getConfigManager();

		String instanceid = "1";
		if (serviceName.equalsIgnoreCase(MoipMessageEntities.MESSAGE_SERVICE_MAS)) {
			instanceid = instance.getOamManager().getLocalConfig().getParameter(ConfigParam.MAS_SS7_INSTANCE_ID);
		}else if(serviceName.equalsIgnoreCase(MoipMessageEntities.MESSAGE_SERVICE_NTF)) {
			instanceid = instance.getOamManager().getLocalConfig().getParameter(ConfigParam.NTF_SS7_INSTANCE_ID);
		}

		try {
			ss7ConfigManager.setParameter(SsmgConfigurationData.EIN_HD_USER_INSTANCE, instanceid);
			if(logger.isDebugEnabled()) {
				logger.debug("Cm.EinJcpUserInstanceId in the ss7Config manager has been set to " + ss7ConfigManager.getIntValue(SsmgConfigurationData.EIN_HD_USER_INSTANCE));
			}
		} catch (ConfigurationDataException e) {
			logger.error("CommonMessagingAccess.setSs7InstanceId: Exception while setting instance id : " + e.getMessage(),e);
		}
	}

	private void setSs7SliceId() {
		ConfigManager ss7ConfigManager = instance.getOamManager().getSs7Oam().getConfigManager();

		String sliceid = "0";
		if (serviceName.equalsIgnoreCase(MoipMessageEntities.MESSAGE_SERVICE_MAS)) {
			sliceid = instance.getOamManager().getLocalConfig().getParameter(ConfigParam.MAS_SS7_DIALOG_SLICE_ID);
		}else if(serviceName.equalsIgnoreCase(MoipMessageEntities.MESSAGE_SERVICE_NTF)) {
			sliceid = instance.getOamManager().getLocalConfig().getParameter(ConfigParam.NTF_SS7_DIALOG_SLICE_ID);
		}

		try {
			ss7ConfigManager.setParameter(SsmgConfigurationData.EINSS7_DID_SLICING_ID, sliceid);

			if(logger.isDebugEnabled()) {
				logger.debug("Cm.EinSs7DidSlicingId in the ss7Config manager has been set to " + ss7ConfigManager.getIntValue(SsmgConfigurationData.EINSS7_DID_SLICING_ID));               
			}
		} catch (ConfigurationDataException e) {
			logger.error("CommonMessagingAccess.setSs7InstanceId: Exception while setting instance id : " + e.getMessage(),e);
		}
	}

	private void initCDR(){
		setCDRGenRepositoryPerComponent(serviceName);

		/*
		 * Calling CDRRecord at least once so that the CDR file rotation even 
		 * when the file are of size 0, that they are created from the start
		 * and not only when we generate the first CDR. 
		 */
		new CDRRecord(getOamManager().getCdrGenOam());
	}

	/**
	 * entry point of the application from named service startup
	 */
	public void setCDRGenRepositoryPerComponent(String compType) {
		String rootdirectory = System.getProperty("rootdirectory", "/opt/moip/cdr/");
		String rootConfigDirectory = System.getProperty("rootconfigdirectory", "/opt/moip/common/cdrgen/");
		String asciirepository = System.getProperty("asciirepository", "/opt/moip/cdr/rep/");
		String asn1berrepository = System.getProperty("asn1berrepository", "/opt/moip/cdr/asn1berRep/");
		String tagname = System.getProperty("tagname", "tagname.cnf");
		String tagdescription = System.getProperty("tagdescription", "tagdescription.cnf");
		String tagorder = System.getProperty("tagorder", "tagorder.cnf");
		String tagtype = System.getProperty("tagtype", "tagtype.cnf");

		ConfigManager cdrConfigmanager = instance.getOamManager().getCdrGenOam().getConfigManager();

		String compname = "unknown";
		String host = "unknownHost";
		try {
			ComponentInfo ci = OEManager.getSystemTopologyInfo().getLocalComponentInfoOfType(compType);
			if(ci != null){
				compname = ci.getName();
				host = ci.getHost();
			}else{
				logger.fatal("setCDRGenRepositoryPerComponent: Error in topology, should have a component of type " + compType + " on this node.");
			}

		} catch (ConfigurationDataException e1) {
			logger.error("setCDRGenRepositoryPerComponent: unable to get host" + e1.getMessage(),e1);
		}

		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess.setCDRGenRepositoryPerComponent: component is " + compname);
			logger.debug("CommonMessagingAccess.setCDRGenRepositoryPerComponent: host is " + host);
		}

		//Set the node id for geo redundancy
		String nodeId = "";


		OAMManager oam = instance.getOamManager().getCdrGenOam();
		GeoSystems geo = OEManager.getGeoSystems(oam );


		if (geo.isGeoSolutionEnabled()) {
			nodeId = geo.getSystemHomeID() + "_";
		}

		nodeId = nodeId+host;

		/**
		 * Let take the first part (before the @ sign) as the componentId to append to the path of the CDR file
		 * MAS: asciiRepository="/opt/cdr/rep/" becomes asciiRepository="/opt/cdr/rep/mas1/"
		 * NTF: asciiRepository="/opt/cdr/rep/" becomes asciiRepository="/opt/cdr/rep/ntf1/"
		 */





		asciirepository = asciirepository + compname + "/";
		asn1berrepository = asn1berrepository + compname + "/";


		try {
			cdrConfigmanager.setParameter(CDRGenConfig.PROP_NAME_ROOTDIRECTORY, rootdirectory);
			cdrConfigmanager.setParameter(CDRGenConfig.PROP_NAME_ROOTCONFIGDIRECTORY, rootConfigDirectory);
			cdrConfigmanager.setParameter(CDRGenConfig.PROP_NAME_ASCII_REPOSITORY, asciirepository);
			cdrConfigmanager.setParameter(CDRGenConfig.PROP_NAME_ASN1_REPOSITORY, asn1berrepository);
			cdrConfigmanager.setParameter(CDRGenConfig.PROP_NAME_TAGNAMEFILE, tagname);
			cdrConfigmanager.setParameter(CDRGenConfig.PROP_NAME_TAGDESCRIPTIONFILE, tagdescription);
			cdrConfigmanager.setParameter(CDRGenConfig.PROP_NAME_TAGORDERFILE, tagorder);
			cdrConfigmanager.setParameter(CDRGenConfig.PROP_NAME_TAGTYPEFILE, tagtype);
			cdrConfigmanager.setParameter(CDRGenConfig.PROP_NAME_CURRENT_NODE_ID, nodeId);
			logger.info("setCDRGenRepositoryPerComponent(): asciiCdrPath and asn1CdrPath for component <" + compname + ">: " + asciirepository + ", " + asn1berrepository);
		}
		catch (Exception e) {
			logger.error("CommonMessagingAccess.setCDRGenRepositoryPerComponent " + e.getMessage(),e);
		}
	}

	public String extractDigitsFromTelephoneNumber(String telephoneNumber) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<telephoneNumber.length(); i++) {           
			if (Character.isDigit(telephoneNumber.charAt(i)))         
				sb.append(telephoneNumber.charAt(i));
		}
		return new String(sb);
	}

	public String denormalizeNumber(String number)
	{
		String denormalizednumber="";
		if(number==null || number.isEmpty()) return denormalizednumber;
		denormalizednumber=number;

		try {
			denormalizednumber = normalizeAddressField(number);
		} catch (IdentityFormatterInvalidIdentityException iie) {
			logger.info("Unable to normalize " + number + ", " + iie.getMessage(), iie);
			return denormalizednumber;
		}
		//remove < >
		denormalizednumber = extractPhoneNumber(denormalizednumber);
		//remove the URI
		int start = denormalizednumber.indexOf(":") + 1;
		if(start>0)
		{
			denormalizednumber=denormalizednumber.substring(start);
		}

		//remove the (+) sign
		if(denormalizednumber.startsWith("+"))
		{
			denormalizednumber=denormalizednumber.substring(1);
		}


		int end=denormalizednumber.indexOf(">");

		if (end != -1) {
			//found >, remove it
			denormalizednumber=denormalizednumber.substring(0, end);
		}

		denormalizednumber=denormalizednumber.trim();

		if(logger.isDebugEnabled()) {
			logger.debug("denormalizeNumber from "+number + " to "+denormalizednumber);
		}
		return denormalizednumber;
	}

	/**
	 * Create a new message in MFS spool directory. MFS leaves the MfsFileHandle
	 * (@see com.abcxyz.messaging.mfs.data.MfsFileHandle) opened, be sure to
	 * release it after usage.
	 *
	 * @return message handle, includes mfs file handle, be sure to call message
	 *         terminated for releasing the file handle
	 */
	static protected MessageHandle handleMessage(final Message message,
			final MessageHandle handle) throws MsgAccessingException {
		if(handle == null) {
			logger.error("CmnMessagingFacade handleMessage handle null, can nt continue.");
			throw new MsgAccessingException("handleMessage handle null");
		}

		// store message with c1 and c2, context is for routing lookup
		final MSA omsa = message.getOmsa();
		final String omsgid = message.getOMsgid();

		try {
			if(omsa.isInternal() == false) {
				// create path
				mfs.createPath(omsa);
			}
			// Option for storing msg body in receiver's inbox for 1-to-1 msgs
			if (message.getRmsa() != null)
				handle.origFileHandle = mfs.createOriginatorMsgFileInReceiverInbox(message);
			else 
				handle.origFileHandle = mfs.createOriginatorMsgFile(omsa, omsgid, message);

		}
		catch(final MsgStoreException e) {
			throw new MsgAccessingException(
					"CommonMessagingAccess handleMessage msgstore exception:"
							+ e.getMessage(), e);
		}

		return handle;
	}

	/**
	 * This method appends a new Container3 part to the main message.
	 *
	 * @return message handle, includes mfs file handle, be sure to call message
	 *         terminated for releasing the file handle
	 */
	static protected MessageHandle handleMsgPart(final MessageHandle handle,
			final MsgBodyPart[] parts) throws MsgAccessingException {
		if (handle == null) {
			logger.error("CommonMessagingAccess handleMsgPart with not handle, can not continue.");
			return null;
		}

		try {
			parts[parts.length - 1].resetLastPart();
			handle.origFileHandle =mfs.appendOriginatorMsgFile((MessageFileHandle) handle.origFileHandle, parts);
		} catch(final MsgStoreException e) {
			logger.error("CommonMessagingAccess handleMsgPart "+e.getMessage(),e);
			throw new MsgAccessingException(
					"MessagingAccess handleMsgPart exception:" + e.getMessage(),
					e);
		}
		return handle;
	}

	/**
	 * First adding of an external body file. The originator container file may
	 * or may not be created yet. keeps the external body file open and return
	 * the handle.
	 *
	 * @param handle not null message handle
	 */
	// Note: this method is not called; so no need to make change for the sender-outbox-2-receiver-inbox
	static protected MessageHandle handleAttachement(
			final String attachmentName, final MessageHandle handle,
			final MsgBodyPart externalPart) throws MsgAccessingException {
		if(handle == null) {
			logger.warn("MessagingAccess handleAttachement message handle not set, can not continue.");
			throw new MsgAccessingException(
					"MessagingAccess handleAttachement handle null");
		}
		try {
			externalPart.resetLastPart();
			if(handle.attachFileHandles == null) {
				handle.attachFileHandles = new HashMap<String, MfsFileHandle>();
			}

			final MfsFileHandle oneHandle =
					mfs.addMsgBodyFile(handle.omsa, handle.omsgid, externalPart, handle.origFileHandle);
			handle.attachFileHandles.put(attachmentName, oneHandle);
		}
		catch(final Exception e) {
			logger.warn("MessagingAccess handleAttachement "+e.getMessage(),e);
			throw new MsgAccessingException(
					"MessagingAccess handleMsgPart exception:" + e.getMessage(),
					e);
		}
		return handle;
	}

	/**
	 * Following adding of an external body file. keeps the external body file
	 * open and return the handle. This method is intended to be called if the
	 * external message file is particularly large and all of its data cannot be
	 * contained within one byte array. This method can be called an unlimited
	 * number of times.
	 * @param handle external body file's handle
	 */
	static protected MessageHandle handleAttachementContent(
			final String filename, final MessageHandle handle,
			final byte[] content) throws MsgAccessingException {
		if(handle == null) {
			logger
			.warn("MessagingAccess handleAttachement message handle not set, can not continue.");
			throw new MsgAccessingException(
					"MessagingAccess handleAttachementContent handle null");
		}

		try {
			MfsFileHandle oneHandle = handle.attachFileHandles.get(filename);
			if(oneHandle == null) {
				logger
				.warn("MessagingAccess handleAttachement message was not created, can not continue");
				throw new MsgAccessingException(
						"MessagingAccess handleAttachementContent external file not initialized");
			}

			oneHandle = mfs.appendMsgBodyFile(handle.omsa, handle.omsgid, oneHandle, filename, content);
			handle.attachFileHandles.put(filename, oneHandle);
		}
		catch(final MsgStoreException e) {
			logger.error("MessagingAccess::handleAttachementContent : " + e.getMessage(),e);
			throw new MsgAccessingException(
					"MessagingAccess handleMsgPart exception:" + e.getMessage(),
					e);
		}
		return handle;
	}


	protected int handleMessageCompleted(final MessageHandle handle,
			final StateAttributes attributes, final String nonNormalizedTo, boolean lastRecipient,
			boolean storeMsgInRecipientInbox) throws MsgAccessingException {
		return handleMessageCompleted(handle, attributes, nonNormalizedTo, lastRecipient, storeMsgInRecipientInbox, true);
	}    

	/**
	 * Create the message from in its final holding area to outbox (or inbox), signaling that the
	 * message is available for further processing.
	 * 
	 * Note : the message body file is not transferred from the spool anymore (for performance reasons).
	 */
	protected int handleMessageCompleted(final MessageHandle handle,
			final StateAttributes attributes, final String nonNormalizedTo, boolean lastRecipient,
			boolean storeMsgInRecipientInbox, boolean appendFinalBoundaryPart) throws MsgAccessingException {


		/*
		 * if (handle.routeFound == null) {logger.warn(
		 * "MessagingAccess::messageCompleted routing info, can not continue.");
		 * return MessageStreamingResult.noRouteFound; }
		 */

		if(handle.c1 == null || handle.omsa == null || handle.omsgid == null
				|| handle.rmsa == null) {
			logger.warn("MessagingAccess::messageCompleted missing mandatory info, can not continue.");
			return MessageStreamingResult.messageRejected;
		}

		try {
			// Append last boundary part 
			if (appendFinalBoundaryPart) {
				try {
					if (handle.origFileHandle != null) {
						MsgBodyPart part = new MsgBodyPart();
						part.setBoundaryPart();
						MsgBodyPart parts[] = {part};
						// No need to call another method for the sender-outbox-2-receiver-inbox change here; the adjustment will be
						// done inside MessageServices.appendOriginatorMsgFile()
						handle.origFileHandle = mfs.appendOriginatorMsgFile((MessageFileHandle)handle.origFileHandle, parts);
					}
				} catch (MsgStoreException e) {
					logger.error("MessagingAccess::handleMessageCompleted append last part exception: " + e.getMessage(),e);
					throw new MsgAccessingException("MessagingAccess handleMsgPart exception:" + e.getMessage(), e);
				}
			}
		}  finally {
			// Release the handle no matter what. We are done with the SPOOL.
			handle.release();
		}

		//If a retry-able error occurs, this entire block needs to be redone.  Maximum number of retries is 3.
		//This while-loop will be exited if:
		// - isMsgStored == true meaning all steps in the block was completed successfully.
		// - A non-retry-able error occurred which means an exception is thrown.
		// - The number of retries have been exhausted.
		int tried = 0;
		boolean isMsgStored = false;
		Exception lastUnthrownException = null;
		MessageInfo msginfo = null;
		while (!isMsgStored && tried++ < 3) {
			//Always get a new rmsgid:
			//- If it is the first try, this is where the rmsgid is intialised.
			//- If it is not the first try, currently, the only retry-able error is if there is a collision when storing the body file. So, on retry, always get a new rmsgid.
			//  In the future, if there are other retry-able errors, we can add a check to see if a new rmsgid is needed on retry.
			handle.rmsgid = MsgStoreServer.getAnyMsgId();
			if (logger.isDebugEnabled()) {
				logger.debug("MessagingAccess::handleMessageCompleted starting try=" + tried + ":" + handle.omsa + " " + handle.rmsa + " " + handle.omsgid + " " +handle.rmsgid);
			}

			//store message body file in MFS
			msginfo = new MessageInfo(handle.omsa, handle.rmsa, handle.omsgid, handle.rmsgid);
			// Option for storing msg body in receiver's inbox for 1-to-1 msgs
			msginfo.setStoreMsgInRecipientInbox(storeMsgInRecipientInbox);
			try {
				if(lastRecipient){
					mfs.createFinalOriginatorMsgfile(msginfo, handle.msg, MfsBooleans.LAST_RECEIPIENT_true);
				}else {
					mfs.createFinalOriginatorMsgfile(msginfo, handle.msg, MfsBooleans.LAST_RECEIPIENT_false);
				}
			} catch (CollisionException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("MessagingAccess::createFinalOriginatorMsgfile CollisionExcepton with:" + handle.omsa + " " + handle.rmsa + " " + handle.omsgid + " " +handle.rmsgid + ".  Failed try=" + tried);
				}
				lastUnthrownException = e;
				continue;
			} catch (MsgStoreException e) {
				logger.warn("MessagingAccess::createFinalOriginatorMsgfile MsgStoreException " + handle.omsa + " " + handle.rmsa + " " + handle.omsgid + " " +handle.rmsgid);
				throw new MsgAccessingException("createFinalOriginatorMsgfile MsgStoreException "+e.getMessage(), e);
			}

			//create the state file for the recipient
			StateFile sfile = new StateFile(handle.omsa, handle.rmsa, handle.omsgid, handle.rmsgid);
			sfile.prepareCreation(handle.omsa);

			String msgType = handle.c1.getMsgClass();
			StateFileHandle stateHandle = null;
			try {

				if (attributes != null) {
					sfile.setAll(attributes);
				}
				sfile.setDestMsgClass(msgType);
				// Option for storing msg body in receiver's inbox for 1-to-1 msgs
				if (storeMsgInRecipientInbox) sfile.setAttribute(StateAttributes.STORE_MSG_IN_REC_INBOX, "true");

				setNewMessageExpiry(nonNormalizedTo, msgType, sfile);

				/*
                sfile.setOrigMsgClass(prepareReq.destMsgClass.value);
				 */
				if (handle.rmsa.isInternal() == false) {
					/*
					 * JIRA TR MIOTWO-416 (http://jira.lmc.abcxyz.se:8080/browse/MIOTWO-416)
					 * In this version of VM we don't have any feature that supports
					 * retrieving external messages, so
					 */
					throw new MsgAccessingException("External subscribers are not supported in this release. Message store failed for recipient " + nonNormalizedTo);
				}
				stateHandle = mfs.createState(sfile, handle.c1, true);

			} catch (CollisionException e) {
				throw new MsgAccessingException("MessagingAccess::MessageCreateState CollisionException:" + e.getMessage(), e);
			} catch (MsgStoreException e) {
				throw new MsgAccessingException("MessagingAccess::MessageCreateState MsgStoreException:" + e.getMessage(), e);
			} finally {
				if (stateHandle != null) {
					stateHandle.release();
				}
			}

			isMsgStored = true;
		}

		if(isMsgStored) {
			messageDepositEventManager.informNtf(msginfo, nonNormalizedTo);
		} else {
			if(lastUnthrownException != null) {
				throw new MsgAccessingException("MessagingAccess::handleMessageCompleted failed :" + lastUnthrownException.getMessage(), lastUnthrownException);
			} else {
				throw new MsgAccessingException("MessagingAccess::handleMessageCompleted failed.");
			}
		}

		return MessageStreamingResult.streamingOK;
	}

	private void setNewMessageExpiry(String nonNormalizedTo, String messageType, StateFile statefile) throws MsgAccessingException{
		try {
			String[] retentionNew = null;

			IDirectoryAccessSubscriber das = directoryAccess.lookupSubscriber(nonNormalizedTo);

			if (das != null) {
				if(ServiceName.VOICE.equalsIgnoreCase(messageType)) {
					retentionNew = das.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE);
				} else if(ServiceName.VIDEO.equalsIgnoreCase(messageType)) {
					retentionNew = das.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_NEW_VIDEO);
				} else if(ServiceName.FAX.equalsIgnoreCase(messageType)) {
					retentionNew = das.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_NEW_FAX);
				}
			}

			if(retentionNew != null && retentionNew.length > 0) {
				String retention = retentionNew[0];
				if(retention.length() != 0) {
					setMessageExpiry(new Integer(retention).intValue(), statefile);
				}else {
					logger.warn("CommonMessagingAccess.handleMessageCompleted: Did not find the retention time for new " + messageType + " messages in profile " + nonNormalizedTo);
				}
			}else {
				logger.warn("CommonMessagingAccess.handleMessageCompleted: Did not find the retention time for new " + messageType + " messages in profile " + nonNormalizedTo);
			}
		} catch (MsgStoreException e) {
			throw new MsgAccessingException("CommonMessagingAccess::handleMessageCompleted MsgStoreException:" + e.getMessage(), e);
		} catch (NumberFormatException nme) {
			logger.warn("CommonMessagingAccess.handleMessageCompleted: Unable to parse the retention time for new " + messageType + " messages in profile " + nonNormalizedTo);
			throw new MsgAccessingException("CommonMessagingAccess::handleMessageCompleted NumberFormatException:" + nme.getMessage(), nme);
		}
	}


	/**
	 * Sets the expiry date of the current message and schedules an expiry event.
	 *
	 * @param stateFile State file of the message.
	 * @throws MsgStoreException Thrown on error.
	 */
	public void setMessageExpiry(int expiryInDays, StateFile stateFile) throws MsgStoreException {
		if(expiryInDays < 0){
			// a negative value means to ignore this expiry
			return;
		} else if (expiryInDays == 0) {
			// 0 means no expiry - just cancel any existing event
			String previousExpiryEventId = stateFile.getAttribute(MoipMessageEntities.EXPIRY_EVENT_ID);
			cancelExpiry(previousExpiryEventId);
		} else {
			// Normal case - cancel any existing event and schedule a new one
			Calendar expiryDate = Calendar.getInstance();
			expiryDate.add(Calendar.DATE, expiryInDays);
			setMessageExpiry(expiryDate.getTime(), stateFile);
		}

	}


	/**
	 * Sets the expiry date of the current message and schedules an expiry event.
	 *
	 * @param expiryDate Expiry date.
	 * @param stateFile State file of the message.
	 * @throws MsgStoreException Thrown on error.
	 */
	public void setMessageExpiry(Date expiryDate, StateFile stateFile) throws MsgStoreException {

		String previousExpiryEventId = stateFile.getAttribute(MoipMessageEntities.EXPIRY_EVENT_ID);
		stateFile.setC1Attribute(Container1.Time_of_expiry, CommonMessagingAccess.DateFormatter.get().format(expiryDate));

		// Since the scheduler may fire an event up to 30s early, we add 31s to the expiry date. This is set after
		// updating the C1 attribute in order for the C1 to reflect the earliest possible expiry time.
		expiryDate.setTime(expiryDate.getTime() + 31000);

		Properties properties = new Properties();
		properties.setProperty(MoipMessageEntities.OMSA, stateFile.omsa.toString());
		properties.setProperty(MoipMessageEntities.RMSA, stateFile.rmsa.toString());
		properties.setProperty(MoipMessageEntities.OMSGID, stateFile.omsgid);
		properties.setProperty(MoipMessageEntities.RMSGID, stateFile.rmsgid);
		if(MfsStateFolderType.TRASH.equals(stateFile.folderType)){
			properties.setProperty(MoipMessageEntities.FOLDER, stateFile.folderType.toString().toLowerCase());
		}

		AppliEventInfo eventInfo = getMessageCleaner().scheduleEvent(expiryDate.getTime(),
				CommonMessagingAccess.getUniqueId(),
				EventTypes.INTERNAL_TIMER.getName(),
				properties);
		stateFile.setAttribute(MoipMessageEntities.EXPIRY_EVENT_ID, eventInfo.getEventId());

		// Cancel the previous scheduled event if it exists.
		cancelExpiry(previousExpiryEventId);
	}

	private void cancelExpiry(String expiryEventId) {
		if (expiryEventId != null && !expiryEventId.isEmpty()) {
			AppliEventInfo previousExpiryEventInfo = new AppliEventInfo();
			previousExpiryEventInfo.setEventId(expiryEventId);
			getMessageCleaner().cancelEvent(previousExpiryEventInfo);
		}
	}

	/**
	 * Cancels the schedule expiry event associated to a message.
	 * @param stateFile State file of the message.
	 */
	public void cancelScheduledEvent(StateFile stateFile) {
		String eventId = stateFile.getAttribute(MoipMessageEntities.EXPIRY_EVENT_ID);
		if (eventId != null && !eventId.isEmpty()) {
			AppliEventInfo eventInfo = new AppliEventInfo();
			eventInfo.setEventId(eventId);
			getMessageCleaner().cancelEvent(eventInfo);
			stateFile.setC1Attribute(Container1.Time_of_expiry, "");
			stateFile.setAttribute(MoipMessageEntities.EXPIRY_EVENT_ID, "");
		}
	}

	static public String getUniqueId() {
		String uniqueId = UUID.randomUUID().toString();
		uniqueId = uniqueId.replace("-", "");
		return uniqueId;
	}

	public static void handleVvmSync(EventTypes eventType, String phone, MessageInfo messageInfo){
		handleVvmSync(eventType, phone, messageInfo, null);
	}
	/**
	 * Utility method to verify if user has vvm enabled and to send sync message to ntf
	 * @param eventType The event type
	 * @param phone The phone number
	 * @param messageInfo The Message Info
	 */
	public static void handleVvmSync(EventTypes eventType, String phone, MessageInfo messageInfo, Properties properties){

		IDirectoryAccessSubscriber profile = DirectoryAccess.getInstance().lookupSubscriber(phone);
		if(profile == null) {
			return;
		}
		String[] services = profile.getStringAttributes(DAConstants.ATTR_SERVICES);
		boolean hasVvmService = false;
		for(String service: services){
			if(service.equalsIgnoreCase(ProvisioningConstants.SERVICES_VVM)){
				if (logger.isDebugEnabled()) {
					logger.debug("CommonMessagingAccess.handleVvmSync(): " + phone + " has vvm service enabled");
				}
				hasVvmService = true;
				break;
			}
		}

		boolean hasVvmActivated = false;
		String[] vvmActivated = profile.getStringAttributes(DAConstants.ATTR_VVM_ACTIVATED);
		if(vvmActivated != null && vvmActivated.length > 0){
			if(vvmActivated[0].equalsIgnoreCase(ProvisioningConstants.YES)){
				if (logger.isDebugEnabled()) {
					logger.debug("CommonMessagingAccess.handleVvmSync(): " + phone + " has vvm service activated");
				}
				hasVvmActivated = true;
			}
		}

		if(hasVvmService && hasVvmActivated) {
			if(messageInfo == null) {
				IDirectoryAccessSubscriber subscriber = DirectoryAccess.getInstance().lookupSubscriber(phone);
				String msid = subscriber.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MSID);
				messageInfo = new MessageInfo();
				messageInfo.rmsa = new MSA(msid);
			}
			CommonMessagingAccess.getInstance().notifyNtf(eventType, messageInfo, phone, properties);
		}


	}


	/**
	 * Utility method to extract phone number from the "FROM" header
	 * The From header SHOULD be assumed to have the following format:
	 * From: "{Common Name} {(phone number)}" <{e-mail address}>
	 * @return the phone number if found, empty string if not found
	 */
	public static String getPhoneNumber(String inputStr) {
		String result = "";
		String patternStr = "^\"?\\s*(?:[A-Za-z][A-Za-z0-9\\s\\.\\@_\\-~#]*)?\\(?(\\+?[0-9]*)\\)?\"?\\s*(?:<.*>)?$";

		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(inputStr);

		if (matcher.find()) {
			result = matcher.group(1);
		}
		return result;
	}

	/**
	 * schedule one event for a given time and type
	 *
	 * @param eventType eventType names are those listed in MasEventTypes
	 * @param props the properties key names are those listed in MasEventProperties.
	 * @return scheduled event ID to be kept in state file
	 */
	public String scheduleEvent(long when, MessageInfo msgInfo, EventTypes eventType, Properties props) {

		String id = MoipMessageIDGen.getShortSenderMessageID(msgInfo, schedulerInstanceId);
		AppliEventInfo event = eventHandler.scheduleEvent(when, id, eventType.getName(), props);

		return event.getEventId();
	}

	/**
	 * schedule the first retry event, the time will be retrieved from retry schema
	 */
	public String scheduleEvent(MessageInfo msgInfo, EventTypes eventType, Properties props) {

		String id = MoipMessageIDGen.getShortSenderMessageID(msgInfo, schedulerInstanceId);
		AppliEventInfo event = eventHandler.scheduleEvent(id, eventType.getName(), props);

		return event.getEventId();
	}


	/**
	 * cancel one scheduled event ID
	 * @param eventId previous scheduled event id
	 */
	public void cancelEvent(String eventId) {
		AppliEventInfo event = new AppliEventInfo();
		event.setEventId(eventId);

		eventHandler.cancelEvent(event);
	}


	/**
	 * trigger a direct send message to NTF from MAS
	 */
	public void notifyNtf(EventTypes eventType, MessageInfo msgInfo, String recipientId, Properties properties) {
		if (logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::notifyNtf <" + eventType + "> for:" + recipientId);
		}

		Object perf = null;

		try{
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("CMA.notifyNtf");
			}

			TriggerSendMessageReq req = new TriggerSendMessageReq();
			req.destMsgClass.value = MoipMessageEntities.MESSAGE_SERVICE_NTF;
			req.destRcptID.value = recipientId;
			req.eventType = eventType.getName();

			if (msgInfo.omsa != null) {
				req.oMsa.value = msgInfo.omsa.getId();
			} else {
				req.oMsa.value = msgInfo.rmsa.getId();
			}

			//Use the omsgId if exist
			req.rMsa.value = msgInfo.rmsa.getId();
			if(msgInfo.omsgid == null || msgInfo.omsgid.equals("")){
				req.oMsgID.value = MsgStoreServer.getAnyMsgId();
			}else{
				req.oMsgID.value = msgInfo.omsgid;
			}

			//Use the rmsgId if exist
			if(msgInfo.rmsgid == null || msgInfo.rmsgid.equals("")){
				req.rMsgID.value = MsgStoreServer.getAnyMsgId();
			}else{
				req.rMsgID.value = msgInfo.rmsgid;
			}

			//set all properties to be sent through send message
			if (properties != null && properties.size() > 0) {
				req.msgctx.extraValue = new HashMap<String, String>();
				String key;
				for (Enumeration<?> e = properties.keys(); e.hasMoreElements();) {

					key = (String)e.nextElement();
					req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + key,properties.getProperty(key) );
				}
			}

			if (properties == null) {
				properties = new Properties();
			}

			//properties to be kept in event, but not sending to NTF
			properties.setProperty(SlamdownEventHandler.RECIPIENT_ID, recipientId);
			properties.setProperty(SlamdownEventHandler.OMSA, req.oMsa.value);
			properties.setProperty(SlamdownEventHandler.RMSA, req.rMsa.value);
			properties.setProperty(SlamdownEventHandler.OMSGID, req.oMsgID.value);
			properties.setProperty(SlamdownEventHandler.RMSGID, req.rMsgID.value);
			properties.setProperty(SlamdownEventHandler.EVENTTYPE, eventType.getName());

			String schedulerID = CommonOamManager.getInstance().getMrdOam().getConfigManager().getParameter(DispatcherConfigMgr.SchedulerID);

			/**
			 * MsgInfo might be partly empty since the notification is performed for a non-subscriber (MCN case).
			 * Therefore, the messageId generation would be compromised without all its msgInfo values.
			 */
			msgInfo.omsa = new MSA(msgInfo.rmsa.toString());
			msgInfo.omsgid = req.oMsgID.value;
			msgInfo.rmsgid = req.rMsgID.value;

			AppliEventInfo event = slamdownEventHandler.scheduleEvent(MoipMessageIDGen.getRecipientMessageID(msgInfo, schedulerID), req.eventType, properties);

			try {
				//Pass the event to MRD. The latter will handle the cancellation of the event on successful response from NTF
				req.retryEventId = new EventID(event.getEventId());
			} catch (InvalidEventIDException e) {
				logger.warn("CommonMessagingAccess::notifyNtf Exception: "+e.getMessage(), e);
			}

			if (mrd != null) {
				TriggerSendMessageResp resp = mrd.getFacade().triggerSendMessage(req);

				if(eventType == EventTypes.FAX_PRINT) {
					try{
						if(!resp.result.isOk())
						{
							String rmsa = properties.getProperty("rmsa");
							String rmsgid =properties.getProperty("rmsgid");
							String omsa = properties.getProperty("omsa");
							String omsgid =properties.getProperty("omsgid");
							if(rmsa!=null && rmsgid!=null &&omsa!=null &&omsgid!=null)
							{
								MessageInfo messageInfo = new MessageInfo(new MSA(omsa), new MSA(rmsa), omsgid, rmsgid);

								StateFile mfsStateFile = CommonMessagingAccess.getInstance().getStateFile(messageInfo);
								FaxPrintStatus.changeStatus(mfsStateFile, FaxPrintStatus.done);
							}

						}
					}
					catch(MsgStoreException e)
					{
						logger.error("CommonMessagingAccess::notifyNtf Unable to set fax as done exception "+e.getMessage(),e);
					}
				}

				if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
					StringBuilder checkPoint = new StringBuilder(PROFILER_CHECKPOINT_NAME_SEND_PREFIX);
					checkPoint.append(eventType.getPerformanceProfilingName());
					checkPoint.append(resp.result.isOk() ? PROFILER_CHECKPOINT_NAME_OK_RESULT_SUFFIX : PROFILER_CHECKPOINT_NAME_FAILED_RESULT_SUFFIX);
					profilerAgentCheckPoint(checkPoint.toString());
				}

				if (logger.isDebugEnabled()) {
					logger.debug("CommonMessagingAccess::notifyNtf <" + eventType + "> for :" + recipientId + " response: " + resp.result);
				}
			}
		}finally{
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
	}


	/**
	 * Send inform event to NTF from MAS
	 */
	public void informNtf(EventTypes eventType, MessageInfo msgInfo, String recipientId, Properties properties) {
		if (logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::informNtf <" + eventType + "> for:" + recipientId);
		}

		if (mrd != null) {
			InformEventReq req = constructInformEventReq(eventType, recipientId, msgInfo, properties);
			InformEventResp resp = mrd.getFacade().informEvent(req);

			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				StringBuilder checkPoint = new StringBuilder(PROFILER_CHECKPOINT_NAME_INFORM_PREFIX);
				checkPoint.append(eventType.getPerformanceProfilingName());
				checkPoint.append(resp.informEventResult.isOk() ? PROFILER_CHECKPOINT_NAME_OK_RESULT_SUFFIX : PROFILER_CHECKPOINT_NAME_FAILED_RESULT_SUFFIX);
				profilerAgentCheckPoint(checkPoint.toString());
			}

			if (logger.isDebugEnabled()) {
				logger.debug("CommonMessagingAccess::informNtf <" + eventType + "> for: " + recipientId + " response: " + resp.informEventResult);
			}
		} else {
			logger.error("Dispatcher is null; failed informNtf <" + eventType + "> for: " + recipientId);

		}
	}


	/**
	 * Creates an inform event request to send to NTF from MAS
	 */
	public static InformEventReq constructInformEventReq(EventTypes eventType, String recipientId, MessageInfo msgInfo, Properties properties) {
		InformEventReq req = new InformEventReq();
		req.transID.value = "0";
		req.origMsgClass.value = MoipMessageEntities.MESSAGE_SERVICE_MAS;
		req.destMsgClass.value = MoipMessageEntities.MESSAGE_SERVICE_NTF;
		req.destRcptID.value = recipientId;
		req.informEventType.value = eventType.getName();
		//For the MSA use the to string method, it return also the external mfs information
		if (msgInfo.omsa != null) {
			req.oMsa.value = msgInfo.omsa.toString();
		} else {
			req.oMsa.value = msgInfo.rmsa.toString();
		}

		//Use the omsgId if exist
		req.rMsa.value = msgInfo.rmsa.getId();
		if(msgInfo.omsgid == null || msgInfo.omsgid.equals("")){
			req.oMsgID.value = MsgStoreServer.getAnyMsgId();
		}else{
			req.oMsgID.value = msgInfo.omsgid;
		}

		//Use the rmsgId if exist
		if(msgInfo.rmsgid == null || msgInfo.rmsgid.equals("")){
			req.rMsgID.value = MsgStoreServer.getAnyMsgId();
		}else{
			req.rMsgID.value = msgInfo.rmsgid;
		}

		//set all properties to be sent through send message
		if (properties != null && properties.size() > 0) {
			req.extraValue = new HashMap<String, String>();
			String key;
			for (Enumeration<?> e = properties.keys(); e.hasMoreElements();) {
				key = (String)e.nextElement();
				req.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + key,properties.getProperty(key) );
			}
		}    
		return req;
	}


	public MessageInfo storeMessageTest(Container1 c1, Container2 c2, MsgBodyPart[] c3Parts, StateAttributes attributes) throws Exception
	{

		if (logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::Do_StoreMessage for: " + c1.getMsgClass());
		}

		doNormalization(c1, c2);

		MSA omsa =  MFSFactory.getGen2MSA(c1.getFrom(), true); // TODO: ealebie - use the actual MCDProxy call this is temporary
		String omsgid = MsgStoreServer.getAnyMsgId();

		Container3 c3 =  new Container3(omsa,omsgid);
		MessageHandle msgHandle = new MessageHandle();

		//create message in mfs spool
		Message message = new Message(omsa, omsgid, c1, c2, c3);

		msgHandle.c1 = c1;
		msgHandle.omsa = omsa;
		msgHandle.omsgid = omsgid;

		msgHandle = handleMessage(message, msgHandle);

		if(logger.isDebugEnabled()) {
			logger.debug("storeMessage new message done: " + c1.getMsgClass()
			+ " from: " + c1.getUri(Container1.From) + " to:"
			+ c1.getUri(Container1.To));
		}

		// add parts in the file

		if(logger.isDebugEnabled()) {
			logger.debug("storeMessage handleContentPart for omsa " + omsa
					+ ", omsgid " + omsgid);
		}

		msgHandle = handleMsgPart(msgHandle, c3Parts);

		if (c3Parts[0].isExternal()) {
			MfsFileHandle handle = mfs.appendMsgBodyFile(omsa, omsgid, null, "2549millisecondgoodfile.wav", c3Parts[0].getContent());
			//MfsFileHandle handle = mfs.addMsgBodyFile(omsa, omsgid, c3Parts[0], null);
			handle.release();
		}

		if(logger.isDebugEnabled()) {
			logger.debug("storeMessage handleMessageCompleted for omsa " + omsa
					+ ", omsgid " + omsgid);
		}

		msgHandle.rmsa = getMsid(c1.getTo());

		msgHandle.rmsgid = MsgStoreServer.getAnyMsgId();

		// completed the storage
		handleMessageCompleted(msgHandle, attributes, c1.getTo(), true, true);

		return new MessageInfo(msgHandle.omsa, msgHandle.rmsa,
				msgHandle.omsgid, msgHandle.rmsgid);

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.mobeon.common.cmnaccess.ICommonMessagingAccess#storeMessage(com.abcxyz
	 * .messaging.common.message.Container1,
	 * com.abcxyz.messaging.common.message.Container2,
	 * com.abcxyz.messaging.common.message.MsgBodyPart[],
	 * com.abcxyz.messaging.mfs.statefile.StateAttributes)
	 */
	public int storeMessageOld(final Container1 c1, final Container2 c2,
			final MsgBodyPart[] c3Parts, final StateAttributes attributes)
					throws Exception {

		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::storeMessage: "+ c1.getMsgClass());
		}

		String nonNormalizedTo = c1.getTo();
		doNormalization(c1, c2);
		String from = c1.getFrom();

		//GEO-Distribution optimization. If the originator is external, store the message
		//using the recipients MSID
		String[] toFields = nonNormalizedTo.split(";|,");
		MSA rmsa = null; //Recipient MSISD
		if ( toFields != null && toFields.length > 0){
			String to = toFields[0];
			rmsa = getMsid(to);
		}
		String fromIdentity = extractIdentityFromFrom(from);
		MSA omsa = getMsid(fromIdentity,rmsa);

		final String omsgid = MsgStoreServer.getAnyMsgId();

		final Container3 c3 = new Container3(omsa, omsgid);
		MessageHandle msgHandle = new MessageHandle();

		Object kpiPerf = null; // KPI for msg store latency

		try {

			if (KPIProfiler.isStatsEnabled()) {
				kpiPerf = KPIProfiler.enterCheckpoint(KPIProfiler.KPI_NAME_MSG_STORE_LATENCY, KPIProfiler.KPI_DISPLAY_MSG_STORE_LATENCY);
			}

			// create message in mfs spool
			final Message message = new Message(omsa, omsgid, c1, c2, c3);

			msgHandle.c1 = c1;
			msgHandle.omsa = omsa;
			msgHandle.omsgid = omsgid;

			// Msg encrytion mfd
			// Must call MessageServices.isEncryptionEnabled() once and only once at the beginning, 
			// then, pass the same value for subsequent msg handling steps, because config auto refresh is 
			// supported by MessageServices.isEncryptionEnabled() so we must avoid inconsistent value for isEnabled
			// in the middle of handling the same msg
			boolean toEncrypt = MessageServices.isEncryptionEnabled();
			message.toEncrypt = toEncrypt;

			// Option for storing msg body in receiver's inbox for 1-to-1 msgs
			boolean storeMsgInRecipientInbox = false; 
			if (toFields.length == 1 && storeMessageInRecipientsInbox) {
				message.setRmsa(rmsa);
				storeMsgInRecipientInbox = true;
			}

			msgHandle = handleMessage(message, msgHandle);

			if(logger.isDebugEnabled()) {
				logger.debug("storeMessage new message done: "
						+ c1.getMsgClass() + " from: "
						+ c1.getUri(Container1.From) + " to:"
						+ c1.getUri(Container1.To));
				logger.debug("storeMessage handleContentPart for omsa " + omsa + ", omsgid " + omsgid);
			}

			// add parts in the file
			msgHandle = handleMsgPart(msgHandle, c3Parts);

			if(logger.isDebugEnabled()) {
				logger.debug("storeMessage handleMessageCompleted for omsa " + omsa + ", omsgid " + omsgid);
			}

			int status = MessageStreamingResult.streamingOK;

			List<String> failedRecipients = new ArrayList<String>();

			if (toFields != null) {

				List<String> alreadyProcessedRecipients = new ArrayList<String>();

				for(int i=0; i<toFields.length; i++){
					String normalizedField = normalizeAddressField(toFields[i]);
					if (! alreadyProcessedRecipients.contains(normalizedField)) {
						alreadyProcessedRecipients.add(normalizedField);
						try {
							if (! doesSubHaveVMService(toFields[i]))
							{
								throw new MsgAccessingException("storeMessage - Message not stored.  Recipient " + toFields[i] + " does not have Voice Mail service.");
							}

							// Fix TR: HP72702
							MSA fomsa;
							if (i == 0) {
								fomsa = omsa;
								msgHandle.rmsa = rmsa;
							} else {
								msgHandle.rmsa = getMsid(toFields[i]);
								// pass the recipient to avoid null exception if originator is number withheld
								fomsa = getMsid(fromIdentity, msgHandle.rmsa);
							}

							if(!isStorageOperationsAvailable(fomsa, msgHandle.rmsa)) {
								throw new MsgAccessingException("storeMessage - Message not stored.  Recipient " + toFields[i] + " storage is not available.");
							}


							if(logger.isDebugEnabled()) {
								logger.debug("storeMessage - Sending message to " + toFields[i]);
							}

							//Have to copy stateAttribute contents to another object because in the MFS implementation
							//once an object is written, flags are updated so that they are not written to disk again
							//Therefore, if we use the same object, the second and subsequent times we try to write the
							//state file, we will not have information such as the MsgState on disk.
							StateAttributes strAttr = null;
							if (attributes != null ){
								strAttr =  attributes.clone();

							}
							//end of stateAttributes copying

							boolean lastRecipient = (i==toFields.length-1);
							// Option for storing msg body in receiver's inbox for 1-to-1 msgs
							status = handleMessageCompleted(msgHandle, strAttr, toFields[i], lastRecipient, storeMsgInRecipientInbox);

							if(logger.isDebugEnabled()) {
								logger.debug("storeMessage - Message successfully stored for recipient " + toFields[i]);
							}

							if(status != MessageStreamingResult.streamingOK){
								throw new MsgAccessingException("Failed to send message to " + toFields[i]);
							}
						} catch (MsgAccessingException e) {
							logger.info("storeMessage - Message storing failed for recipient " + toFields[i]+" Detail: "+e.getMessage(),e);
							failedRecipients.add(toFields[i]);
						}
					}
				}
			}

			if (! failedRecipients.isEmpty()) {
				MsgAccessingException e = new MsgAccessingException("Failed to send message to the following recipients: " + failedRecipients);

				for (String failedRecipient : failedRecipients) {
					e.addFailedRecipient(failedRecipient);
				}

				throw e;
			}

			return status;
		} catch(final MsgAccessingException e) {
			logger.error("storeMessage:handleMessageCompleted exception: omsa "
					+ omsa + ", omsgid " + omsgid + " return rejected "+e.getMessage(), e);
			throw e;
		} finally {
			if (kpiPerf != null) KPIProfiler.exitCheckpoint(kpiPerf);
		}
	}



	/**
	 * Optimized version of the storeMessage method.
	 * 
	 * 
	 * @see com.mobeon.common.cmnaccess.ICommonMessagingAccess#storeMessage(com.abcxyz.messaging.common.message.Container1, com.abcxyz.messaging.common.message.Container2, com.abcxyz.messaging.common.message.MsgBodyPart[],com.abcxyz.messaging.mfs.statefile.StateAttributes)
	 */
	public int storeMessage(final Container1 c1, final Container2 c2, final MsgBodyPart[] c3Parts, final StateAttributes attributes) throws Exception {

		String METHOD_NAME = "CommonMessagingAccess.storeMessage() : ";

		if(logger.isDebugEnabled()) {
			logger.debug(METHOD_NAME + c1.getMsgClass());
		}

		String nonNormalizedTo = c1.getTo();
		doNormalization(c1, c2);
		String from = c1.getFrom();

		// GEO-Distribution optimization. If the originator is external, store the message
		// using the recipients MSID
		String[] toFields = nonNormalizedTo.split(";|,");
		MSA rmsa = null;
		if ( toFields != null && toFields.length > 0){
			String to = toFields[0];
			rmsa = getMsid(to);
		}
		String fromIdentity = extractIdentityFromFrom(from);
		MSA omsa = getMsid(fromIdentity,rmsa);

		final String omsgid = MsgStoreServer.getAnyMsgId();

		final Container3 c3 = new Container3(omsa, omsgid);

		for (MsgBodyPart c3Part : c3Parts) {
			c3.addPart(c3Part);
		}
		c3.setDataCompletion();

		MessageHandle msgHandle = new MessageHandle();

		Object kpiPerf = null; // KPI for Message Store Latency

		try {

			if (KPIProfiler.isStatsEnabled()) {
				kpiPerf = KPIProfiler.enterCheckpoint(KPIProfiler.KPI_NAME_MSG_STORE_LATENCY, KPIProfiler.KPI_DISPLAY_MSG_STORE_LATENCY);
			}

			// Create message in MFS spool
			final Message message = new Message(omsa, omsgid, c1, c2, c3);

			msgHandle.c1 = c1;
			msgHandle.omsa = omsa;
			msgHandle.omsgid = omsgid;

			/**
			 * Message encryption MFD
			 * Must call MessageServices.isEncryptionEnabled() once and only once at the beginning,
			 * then, pass the same value for subsequent msg handling steps, because config auto refresh is
			 * supported by MessageServices.isEncryptionEnabled() so we must avoid inconsistent value for isEnabled
			 * in the middle of handling the same msg
			 */
			boolean toEncrypt = MessageServices.isEncryptionEnabled();
			message.toEncrypt = toEncrypt;

			// Option for storing msg body in receiver's inbox for 1-to-1 msgs
			boolean storeMsgInRecipientInbox = false; 
			if (toFields.length == 1 && storeMessageInRecipientsInbox) {
				message.setRmsa(rmsa);
				storeMsgInRecipientInbox = true;
			}

			// Do not call below method anymore, as it was only used to store the message body in the spool.
			// This is not done anymore.
			//msgHandle = handleMessage(message, msgHandle);
			msgHandle.msg = message; 

			if(logger.isDebugEnabled()) {
				logger.debug(METHOD_NAME + "New message done: "
						+ c1.getMsgClass() + " from: "
						+ c1.getUri(Container1.From) + " to:"
						+ c1.getUri(Container1.To));
				logger.debug(METHOD_NAME + "handleMessage for omsa " + omsa + ", omsgid " + omsgid);
			}

			//            msgHandle = handleMsgPart(msgHandle, c3Parts);
			//
			//            if(logger.isDebugEnabled()) {
			//                logger.debug(METHOD_NAME + "handleMsgPart for omsa " + omsa + ", omsgid " + omsgid);
			//            }

			int status = MessageStreamingResult.streamingOK;

			List<String> failedRecipients = new ArrayList<String>();

			if (toFields != null) {

				List<String> alreadyProcessedRecipients = new ArrayList<String>();

				for(int i=0; i<toFields.length; i++){
					String normalizedField = normalizeAddressField(toFields[i]);
					if (! alreadyProcessedRecipients.contains(normalizedField)) {
						alreadyProcessedRecipients.add(normalizedField);
						try {
							if (! doesSubHaveVMService(toFields[i]))
							{
								throw new MsgAccessingException(METHOD_NAME + "Message not stored.  Recipient " + toFields[i] + " does not have Voice Mail service.");
							}

							// Fix TR: HP72702
							MSA fomsa;
							if (i == 0) {
								fomsa = omsa;
								msgHandle.rmsa = rmsa;
							} else {
								msgHandle.rmsa = getMsid(toFields[i]);
								// pass the recipient to avoid null exception if originator is number withheld
								fomsa = getMsid(fromIdentity, msgHandle.rmsa);
							}

							if(!isStorageOperationsAvailable(fomsa, msgHandle.rmsa)) {
								throw new MsgAccessingException(METHOD_NAME + "Message not stored.  Recipient " + toFields[i] + " storage is not available.");
							}


							if(logger.isDebugEnabled()) {
								logger.debug(METHOD_NAME + "Sending message to " + toFields[i]);
							}

							//Have to copy stateAttribute contents to another object because in the MFS implementation
							//once an object is written, flags are updated so that they are not written to disk again
							//Therefore, if we use the same object, the second and subsequent times we try to write the
							//state file, we will not have information such as the MsgState on disk.
							StateAttributes strAttr = null;
							if (attributes != null ){
								strAttr =  attributes.clone();

							}
							//end of stateAttributes copying

							boolean lastRecipient = (i==toFields.length-1);
							// Option for storing msg body in receiver's inbox for 1-to-1 msgs
							status = handleMessageCompleted(msgHandle, strAttr, toFields[i], lastRecipient, storeMsgInRecipientInbox, false);

							if(logger.isDebugEnabled()) {
								logger.debug(METHOD_NAME + "Message successfully stored for recipient " + toFields[i]);
							}

							if(status != MessageStreamingResult.streamingOK){
								throw new MsgAccessingException(METHOD_NAME + "Failed to send message to " + toFields[i]);
							}
						} catch (MsgAccessingException e) {
							logger.info(METHOD_NAME + "Message storing failed for recipient " + toFields[i]+" Detail: "+e.getMessage(),e);
							failedRecipients.add(toFields[i]);
						}
					}
				}
			}

			if (! failedRecipients.isEmpty()) {
				MsgAccessingException e = new MsgAccessingException(METHOD_NAME + "Failed to send message to the following recipients: " + failedRecipients);

				for (String failedRecipient : failedRecipients) {
					e.addFailedRecipient(failedRecipient);
				}

				throw e;
			}

			return status;
		} catch(final MsgAccessingException e) {
			logger.error(METHOD_NAME + "Exception: omsa " + omsa + ", omsgid " + omsgid + " return rejected "+e.getMessage(), e);
			throw e;
		} finally {
			if (kpiPerf != null) KPIProfiler.exitCheckpoint(kpiPerf);
		}
	}


	/**
	 * Checks if MFS storage is available. The storage is unavailable on a replicated site during Geo-Redundancy failover.
	 * @param originator The A number
	 * @param recipient The B number
	 * @return true if storage is possible in the Geo Redundant system
	 */
	public boolean isStorageOperationsAvailable(String originator, String recipient) {
		MSA omsa = getMsid(originator);
		MSA rmsa = getMsid(recipient);
		return isStorageOperationsAvailable(omsa, rmsa);
	}

	/**
	 * Checks if MFS storage is available. The storage is unavailable on a replicated site during Geo-Redundancy failover.
	 * @param recipient recipient
	 * @return true if storage is possible in the Geo Redundant system
	 */
	public boolean isStorageOperationsAvailable(String recipient) {
		MSA rmsa = getMsid(recipient);
		return isStorageOperationsAvailable(new MSA(""), rmsa);
	}

	/**
	 * Checks if MFS storage is available. The storage is unavailable on a replicated site during Geo-Redundancy failover.
	 * @param omsa The A number msa
	 * @param rmsa The B number msa
	 * @return true if storage is possible in the Geo Redundant system
	 */
	public boolean isStorageOperationsAvailable(MSA omsa, MSA rmsa) {
		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess.isStorageOperationsAvailable: omsa: " + omsa + " rmsa:" + rmsa);
		}
		boolean result = MsgStoreServerFactory.getMfsStoreServer().isStorageOperationsAvailable(omsa, rmsa);
		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess.isStorageOperationsAvailable: result = " + result);
		}
		return result;
	}

	private boolean doesSubHaveVMService(String subscriberIdentity) {
		boolean hasVMService = false;

		if (subscriberIdentity != null) {
			IDirectoryAccessSubscriber sub = directoryAccess.lookupSubscriber(subscriberIdentity);

			if (sub != null) {
				hasVMService = sub.hasVoiceMailService();
			}
		}

		return hasVMService;
	}

	/**
	 * Extract identity from from field
	 * @param from in format John Smith <tel:+1729700731620>
	 * @return identity in format tel:+1729700731620
	 */
	private static String extractIdentityFromFrom(String from) {
		String identity = from;
		int first = from.indexOf('<');
		if(first < 0) {
			return from;
		}

		int second = from.indexOf('>', first + 1);
		try {
			identity = from.substring(first+1, second);
		}catch(Exception e) {
			if(logger.isDebugEnabled()) {
				logger.debug("CommonMessagingAccess.extractIdentityFromFrom could not extract identity" + e.getMessage(),e);
			}
		}
		return identity;
	}

	/**
	 * Do normalization of addresses in FROM, TO, CC, BCC, and REPLY-TO fields for the storage in MFS
	 * @param c1 Container1 object
	 */
	private void doNormalization(Container1 c1, Container2 c2) {
		if (normalizationFormatter == null) {
			return;
		}

		String output;
		String input = null;
		try {
			input = c1.getFrom();
			if (input != null) {
				output = normalizeAddressField(input);
				c1.setFrom(output);
			}
		}catch (Exception e) {
			logger.info("CommonMessagingAccess::storeMessage: Exception caught when trying to normalize addresses in the FROM field, it is normal if the number is withheld: From Field [" + input + "]");
		}
		try {
			input = c1.getTo();
			if (input != null) {
				output = normalizeAddressField(input);
				c1.resetHeader(Container1.To);
				c1.setTo(output);
			}
		}catch (Exception e) {
			logger.error("CommonMessagingAccess::storeMessage: Exception caught when trying to normalize addresses in the TO field [" + input + "]", e);
		}
		try {
			input = c1.getCc();
			if (input != null) {
				output = normalizeAddressField(input);
				c1.resetHeader(Container1.Cc);
				c1.setCc(output);
			}
		}catch (Exception e) {
			logger.info("CommonMessagingAccess::storeMessage: Exception caught when trying to normalize addresses in the CC field [" + input + "]");
		}
		try {
			input = c1.getBcc();
			if (input != null) {
				output = normalizeAddressField(input);
				c1.resetHeader(Container1.Bcc);
				c1.setBcc(output);
			}
		}catch (Exception e) {
			logger.info("CommonMessagingAccess::storeMessage: Exception caught when trying to normalize addresses in the BCC field [" + input + "]");
		}
		try {
			input = c2.getMsgHeader(MoipMessageEntities.REPLY_TO_HEADER);
			if (input != null) {
				output = normalizeAddressField(input);

				MultiNameValuePairs msgHeaders = c2.getMsgHeaderMultiNvps();
				msgHeaders.remove(MoipMessageEntities.REPLY_TO_HEADER);
				msgHeaders.addValue(MoipMessageEntities.REPLY_TO_HEADER, output);
				c2.setMsgHeaders(msgHeaders);
			}
		}catch (Exception e) {
			logger.info("CommonMessagingAccess::storeMessage: Exception caught when trying to normalize addresses in the REPLY-TO field [" + input + "]");
		}
	}

	/**
	 * Normalize an address
	 * @param addresses address or list of addresses to be formatted according to normalization rules
	 * @return newly formatted address if formatting is successfull, otherwise the original address
	 */
	public String normalizeAddressField(String addresses) throws IdentityFormatterInvalidIdentityException {

		if (normalizationFormatter == null || addresses == null || addresses.isEmpty()) {
			return addresses;
		}

		String normalizedMultipAddr = "";
		String normalizedSingleAddr;
		String number;
		if (addresses.contains(Container1.SEMI_COLON) || addresses.contains(Container1.COMMA)) {
			for(final String buffer : addresses.split(Container1.DELIMITERS)) {
				if (buffer != null && buffer.length() > 0) {
					number = extractPhoneNumber(buffer);
					if (!number.startsWith(DAConstants.IDENTITY_PREFIX_TEL) && !number.startsWith(DAConstants.IDENTITY_PREFIX_SIP)&& !number.startsWith(DAConstants.IDENTITY_PREFIX_FAX)) {
						number = DAConstants.IDENTITY_PREFIX_TEL + number;
					}
					normalizedSingleAddr = normalizationFormatter.formatIdentity(number);
					if (normalizedMultipAddr.length() > 0) {
						normalizedMultipAddr += Container1.SEMI_COLON;
					}

					if (normalizedSingleAddr != null) {
						normalizedSingleAddr = constructDisplayName(buffer, normalizedSingleAddr);
						normalizedMultipAddr += normalizedSingleAddr;
						if(logger.isDebugEnabled()) {
							logger.debug("CommonMessagingAccess::normalizeAddressField: address: " + buffer + " is normalized to: " + normalizedSingleAddr);
						}
					}
					else {
						/**
						 * not able to format, put the original address
						 */
						normalizedMultipAddr += buffer;
						if(logger.isDebugEnabled()) {
							logger.debug("CommonMessagingAccess::normalizeAddressField: not able to normalize address: " + buffer);
						}
					}
				}
			}
		}
		else {
			number = extractPhoneNumber(addresses);
			if (!number.startsWith(DAConstants.IDENTITY_PREFIX_TEL) && !number.startsWith(DAConstants.IDENTITY_PREFIX_SIP)&& !number.startsWith(DAConstants.IDENTITY_PREFIX_FAX)) {
				number = DAConstants.IDENTITY_PREFIX_TEL + number;
			}

			normalizedSingleAddr = normalizationFormatter.formatIdentity(number);

			if (normalizedSingleAddr != null) {
				normalizedMultipAddr = constructDisplayName(addresses, normalizedSingleAddr);;
			}
			else {
				/**
				 * not able to format, put the original address
				 */
				normalizedMultipAddr = addresses;
				if(logger.isDebugEnabled()) {
					logger.debug("CommonMessagingAccess::normalizeAddressField: not able to normalize address: " + addresses);
				}
			}
		}

		if(logger.isDebugEnabled()) {
			logger.debug("CommonMessagingAccess::normalizeAddressField: final result addresses: " + addresses + " are normalized to: " + normalizedMultipAddr);
		}

		return normalizedMultipAddr;
	}

	/**
	 * 
	 * @param number
	 *            The number that is to be normalised.
	 * @param context
	 *            The context to use when determining the rules (regular expressions) to apply to the number in order to normalise it.
	 * @param useDefaultContext
	 *            Boolean which specifies whether a default context is to be used in the event that the given context does not exist in the
	 *            configuration file.
	 * @return The normalized number. If normalization fails, the original number is returned.
	 */
	public String normalize(String number, String context, boolean useDefaultContext) {
		if (normalizationFormatter != null) {
			return normalizationFormatter.normalize(number, context, useDefaultContext);
		}
		return number;
	}

	/**
	 * Extract the phone number from the address with the following form
	 * "John Smith <5143786397>"
	 * @return phone number inside the display address
	 */
	private String extractPhoneNumber(String displayAddress) {

		String result = displayAddress;
		int beginIndex = displayAddress.indexOf('<');
		int endIndex = displayAddress.indexOf('>');

		if ((beginIndex != -1) && (endIndex != -1) && (endIndex > beginIndex)) {
			result = displayAddress.substring(beginIndex+1, endIndex);
		}

		return result.trim();
	}


	/**
	 * Construct the display address from the following form
	 * "John Smith <5143786397>" to "John Smith <tel:+15143786397>"
	 * assuming that "tel:+15143786397" is the newly formatted number
	 * @param displayAddress display address with non formatted number
	 * @param formattedNumber newly formatted number
	 * @return dispay address with newly formatted number
	 */
	private String constructDisplayName(String displayAddress, String formattedNumber) {

		String result = formattedNumber;
		int beginIndex = displayAddress.indexOf('<');
		int endIndex = displayAddress.indexOf('>');

		if ((beginIndex != -1) && (endIndex != -1) && (endIndex > beginIndex)) {
			result = displayAddress.substring(0, beginIndex+1) + formattedNumber + displayAddress.substring(endIndex);
		}

		return result;
	}

	/** 2) MFS Retrieve part **/
	/**
	 *
	 */

	/*
	 * (non-Javadoc)
	 * @see
	 * com.mobeon.common.cmnaccess.ICommonMessagingAccess#readMessage(com.abcxyz
	 * .messaging.mfs.data.MessageInfo)
	 */
	public Message readMessage(final MessageInfo msgInfo)
			throws MsgStoreException {
		return readMessage(msgInfo, false);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.mobeon.common.cmnaccess.ICommonMessagingAccess#readMessage(com.abcxyz
	 * .messaging.mfs.data.MessageInfo)
	 */
	public Message readMessage(final MessageInfo msgInfo, boolean extendMultiparts)
			throws MsgStoreException {

		MessageFileHandle messageFileHandle;

		// progressFile = mfs.createProgressFile(msgInfo);
		if(logger.isDebugEnabled()) {
			logger.debug(" readMessage delivery progress file created for:"
					+ msgInfo.toString());
		}

		try {
			if(logger.isDebugEnabled()) {
				logger.debug(" readMessage of:" + msgInfo.toString());
			}
			//Option for storing msg body in receiver's inbox for 1-to-1 msgs
			StateFile sf = getStateFile(msgInfo); 
			if (Boolean.parseBoolean(sf.getAttribute(StateAttributes.STORE_MSG_IN_REC_INBOX)))
				msgInfo.setStoreMsgInRecipientInbox(true);		
			messageFileHandle = mfs.readMessage(msgInfo, new Range(), null, extendMultiparts);

			if(messageFileHandle == null || messageFileHandle.message == null) {
				throw new MsgStoreException("Message not found");
			}

			// release file handle
			messageFileHandle.release();
			return messageFileHandle.message;

		}  catch(final MsgStoreException e) {
			logger.error("readMessage exception for:" + msgInfo + " "
					+ e.getMessage(), e);
			throw new MsgStoreException(e.getMessage(), e);
		}
	}



	/**
	 * *******be sure to call releaseFileHandle for releasing file handle*******
	 */
	public MessageFileHandle getMessageFileHandle(final MessageInfo msgInfo)
			throws MsgStoreException {
		MessageFileHandle messageFileHandle;
		// Option for storing msg body in receiver's inbox for 1-to-1 msgs
		StateFile sf = getStateFile(msgInfo); 
		if (Boolean.parseBoolean(sf.getAttribute(StateAttributes.STORE_MSG_IN_REC_INBOX)))
			msgInfo.setStoreMsgInRecipientInbox(true);		
		messageFileHandle = mfs.readMessage(msgInfo, new Range(), null);
		return messageFileHandle;
	}

	public void releaseFileHandle(final MessageFileHandle handle) {
		handle.release();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.mobeon.common.cmnaccess.ICommonMessagingAccess#searchMessages(com
	 * .abcxyz.messaging.common.message.MSA,
	 * com.abcxyz.messaging.mfs.statefile.StateAttributesFilter)
	 */
	public Message[] searchMessages(final MSA msa,
			final StateAttributesFilter filter) throws MsgStoreException {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("CommonMessagingAccess.searchMessages");
			}
			final StateFile[] stateFiles = mfs.getStateFiles(msa, filter);
			final Message[] messages = new Message[stateFiles.length];

			for(int i = 0; i < stateFiles.length; i++) {
				final MessageInfo m =
						new MessageInfo(stateFiles[i].omsa, stateFiles[i].rmsa,
								stateFiles[i].omsgid, stateFiles[i].rmsgid);
				messages[i] = readMessage(m);
			}

			return messages;
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
	}

	public MessageInfo[] searchMessageInfos(final MSA msa,
			final StateAttributesFilter filter) throws MsgStoreException {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("CommonMessagingAccess.searchMessageInfos");
			}
			final StateFile[] stateFiles = mfs.getStateFiles(msa, filter);
			final MessageInfo[] messages = new MessageInfo[stateFiles.length];

			for(int i = 0; i < stateFiles.length; i++) {
				final MessageInfo m =
						new MessageInfo(stateFiles[i].omsa, stateFiles[i].rmsa,
								stateFiles[i].omsgid, stateFiles[i].rmsgid);
				messages[i] = m;
			}
			return messages;
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}

	}

	public StateFile[] searchStateFiles(final MSA msa, final StateAttributesFilter filter) throws MsgStoreException {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("CommonMessagingAccess.searchStateFiles");
			}
			final StateFile[] stateFiles = mfs.getStateFiles(msa, filter);
			return stateFiles;
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
	}

	/** 5) Utility functions **/

	/*
	 * (non-Javadoc)
	 * @see
	 * com.mobeon.common.cmnaccess.ICommonMessagingAccess#countMessages(com.
	 * abcxyz.messaging.common.message.MSA,
	 * com.abcxyz.messaging.mfs.statefile.StateAttributesFilter)
	 */

	public int countMessages(final MSA msa, final StateAttributesFilter filter)
			throws MsgStoreException {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("CommonMessagingAccess.searchMessages");
			}
			return mfs.countStateFiles(msa, filter);
		} finally {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.mobeon.common.cmnaccess.ICommonMessagingAccess#updateState(com.abcxyz
	 * .messaging.mfs.statefile.StateFile)
	 */
	public void updateState(final StateFile state) throws MsgStoreException {
		mfs.updateState(state);
	}

	public void updateState(final StateFile state, boolean create) throws MsgStoreException {
		mfs.updateState(state, create);
	}


	public StateFile getStateFile(final MessageInfo msgInfo)
			throws MsgStoreException {
		return mfs.readState(msgInfo);
	}

	public StateFile getStateFile(final MessageInfo msgInfo, final String folder)
			throws MsgStoreException {
		if("trash".equalsIgnoreCase(folder)){
			return mfs.readStateInTrash(msgInfo);
		}

		return getStateFile(msgInfo);
	}


	/**
	 * TR HY15820: This method does NOT filter out fake-deleted messages
	 * Callers of this method do expect fake-deleted messages be included
	 * If you expect a list that filters out fake-detelted messages, you should call other methods in MFS/MsgStoreServer, such as:
	 * public StateFile[] getStateFiles(MSA msa, StateAttributesFilter[] filters) throws MsgStoreException 
	 */
	public MessageInfo[] listStateFile(final MSA msa, final String folder)
			throws MsgStoreException {
		if("trash".equalsIgnoreCase(folder)){
			return mfs.listStateFilesInTrash(msa);
		}

		return mfs.listStateFiles(msa);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.mobeon.common.cmnaccess.ICommonMessagingAccess#deleteMessage(com.
	 * abcxyz.messaging.mfs.statefile.StateFile)
	 */
	public void deleteMessage(final StateFile state) throws MsgStoreException {
		mfs.deleteState(state);
	}

	/*
	 * public MessageFileHandle readMessage(MessageInfo msgInfo, Range range,
	 * MessageFileHandle fileHandle) throws MsgStoreException; public Message
	 * readMessage(MessageInfo msgInfo, boolean extendMultipart) throws
	 * MsgStoreException;
	 */

	/** 4) Greetings **/

	public void createMoipPrivateFolder(String msid) throws MsgStoreException
	{
		mfs.createMsgClassPath(MSG_CLASS, msid);
	}

	public String getMoipPrivateFolder(String msid, boolean internal)
	{
		return mfs.getMsgClassPath(MSG_CLASS, msid);
	}

	public void deleteMoipPrivateFolder(String msid) throws MsgStoreException
	{
		mfs.deleteMsgClassPath(MSG_CLASS, msid);
	}

	/**
	 * Profiling method.
	 * This method does not check if profiling is enabled; the caller is responsible for checking.
	 * This was done to prevent unnecessary String object creation for check point names when profiling is not enabled (normal live traffic case).
	 * Only if profiling is enabled, caller should create check point name and call this method.
	 * @param checkPoint - name of profiling check point
	 */
	static private void profilerAgentCheckPoint(String checkPoint) {
		Object perf = null;
		try {
			perf = CommonOamManager.profilerAgent.enterCheckpoint(checkPoint);
		} finally {
			CommonOamManager.profilerAgent.exitCheckpoint(perf);
		}
	}

	public void incrementPerformanceCounter(String counterName) {

		PerformanceEvent event = new DefaultCounterEvent(counterName);
		PerformanceManager perfManager = CommonOamManager.getInstance().getPerformanceManager();
		if (perfManager != null) {
			if (logger.isDebugEnabled()) logger.debug(CLASSNAME + ".incrementPerformanceCounter : incrementing " + counterName);
			perfManager.markEvent(event);
		} else
			logger.warn(CLASSNAME + "Performance Manager not initialized.  We're probably in a design environment.  Counters won't be incremented.");
	}

	public void incrementPerformanceCounter(String counterName, long increment) {

		PerformanceEvent event = new DefaultCounterEvent(counterName);
		PerformanceManager perfManager = CommonOamManager.getInstance().getPerformanceManager();
		if (perfManager != null) {
			if (logger.isDebugEnabled()) logger.debug(CLASSNAME + ".incrementPerformanceCounter : incrementing " + counterName + " by " + increment);
			perfManager.markEvent(event, increment);
		} else
			logger.warn(CLASSNAME + "Performance Manager not initialized.  We're probably in a design environment.  Counters won't be incremented.");
	}

	private static void usage() {
		String out = "\rUsage: \r" +
				"java -cp <classpath> <config file> <config group> <parameter> [log4j config]" +
				"classpath: The java classpath to backend.jar.\r" +
				"config file: The full path and file to the config file to read.\r" +
				"config group: The config group, usually the same at the config file name.\r" +
				"log4j config: The log4j config file for debug configuration, optional, default is to stdout.\r" +
				"/r" +
				"Example: java -cp <classpath> \"/opt/moip/config/mas/masSpecific.conf\" \"masSpecific.conf\" \"shutdownGracePeriod\" \"/opt/moip/config/mas/logmanager.xml\"";
		System.out.println(out);
	}


	/**
	 * Main function, used to get a configuration parameter from a configuration file.
	 * @param args
	 *   config file: The full path and file to the config file to read.
	 *   config group: The config group, usually the same at the config file name.
	 *   log4j config: The log4j config file for debug configuration, optional, default is to stdout
	 *
	 *   Prints to stdout the value of parameter if found.
	 *
	 *
	 */
	public static void main(String [] args)
	{
		String cfgFile;
		String cfgGrp;
		String param;
		if (args.length < 3 || args.length > 4) {
			System.out.println("Error: Incorrect number of parameters!");
			usage();
			System.exit(1);
		}

		cfgFile = args[0].trim();
		cfgGrp = args[1].trim();
		param = args[2].trim();

		if (args.length == 4)
		{
			try {
				ILoggerFactory.configureAndWatch(args[3].trim());
			} catch (Exception e)
			{
				System.out.println("Warning: Exception while configuring log4j!");
				e.printStackTrace();
			}
		}

		CommonMessagingAccess commonMessagingAccess = CommonMessagingAccess.getInstance();
		Collection<String> configFilenames = new LinkedList<String>();

		configFilenames.add(cfgFile);
		ConfigurationImpl configuration = null;
		try {
			configuration = new ConfigurationImpl(null,configFilenames,false);
			CommonMessagingAccess.getInstance().setConfiguration(configuration);
		} catch (ConfigurationException e) {
			System.out.println("Error: File not found, or not valid!");
			usage();
			System.exit(2);
		}

		String result = "";

		try {
			IGroup grp = commonMessagingAccess.getConfiguration().getGroup(cfgGrp);
			if (grp == null)
			{
				System.out.println("Error: Unknown Group!");
				usage();
				System.exit(4);
			} else {
				result =  grp.getString(param);
			}
		} catch (UnknownParameterException e) {
			System.out.println("Error: Parameter not found!");
			usage();
			System.exit(2);
		} catch (GroupCardinalityException e) {
			System.out.println("Error: More than one group with same name found!");
			e.printStackTrace();
			usage();
			System.exit(3);
		} catch (UnknownGroupException e) {
			System.out.println("Error: Unknown Group!");
			usage();
			System.exit(4);
		}

		if (result == null)
		{
			System.out.println("Error: Parameter not found!");
			usage();
			System.exit(2);
		} else {
			if (result.length()!=0)
			{
				System.out.println(param + "= " + result);
			}
			else
			{
				System.out.println(param + "= " + "No Value");
			}
		}
	}

	/**
	 * 
	 * @param firedEventInfo AppliEventInfo received from LocalEventHandler
	 * @param storedEvent Event stored persistently by the client
	 * @return boolean True if both eventIds are the same
	 */
	public boolean compareEventIds(AppliEventInfo firedEventInfo, String storedEvent) {

		if ((firedEventInfo == null || firedEventInfo.getEventKey() == null || firedEventInfo.getEventKey().isEmpty()) ||
				(storedEvent == null || storedEvent.isEmpty())) {
			if(logger.isDebugEnabled()) {
				logger.debug("compareEventIds() : firedEvent or storedEvent is null or empty, considered not equal");
			}
			return false;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//HS80034: Please see comments in com.abcxyz.messaging.vvs.ntf.notifier.isEventIdsMatching(String eventId)
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////     

		String fireEventId = firedEventInfo.getEventId();
		return EventID.compareAsUTC(fireEventId, storedEvent);
	}

	/**
	 * For message undelete
	 * @param stateFile
	 * @param maxNumberMsgUndelete
	 * @param daysToExpire
	 * @throws Exception
	 */
	public void saveChangesForRecycle(StateFile stateFile, int maxNumberMsgUndelete, int daysToExpire) throws Exception {
		//checkFaxMessageBeforeDeletion();        
		try {
			String prioryState = stateFile.getMsgState();
			if (prioryState.contains("recycled")) { // should never happen here, but ...
				logger.warn("CommonMessagingAccess.saveChangesForRecycle(): state = " + prioryState + "; this shall not happen here!");
				cancelScheduledEvent(stateFile);
				deleteMessage(stateFile);
				return;
			}
			String timeStamp = "" + System.currentTimeMillis();
			stateFile.setMsgState(timeStamp + "|recycled|" + prioryState);
			// If there is any VVM/IMAP flag, reset it to recent -- doing this when actually undelete a msg
			//String imapFlag = stateFile.getAttribute("imap.statusflag");
			//if (imapFlag != null && !imapFlag.isEmpty()) stateFile.setAttribute("imap.statusflag", "recent");
			cancelScheduledEvent(stateFile);
			setMessageExpiry(daysToExpire, stateFile);
			updateState(stateFile, false);
		} catch (MsgStoreException e) {
			logger.error("CommonMessagingAccess.saveChangesForRecycle() error updating " + stateFile + ": " + e, e);
			throw new Exception(e.toString());
		}
		// Check total number of messages in recycle bin; if exceeding max, delete the oldest ones
		MsgStoreServer mfs = MsgStoreServerFactory.getMfsStoreServer();
		try {
			ArrayList<StateFile> sfs = mfs.getsortedListOfRecycledMessages(stateFile.rmsa.getId());
			if (sfs != null && sfs.size() > maxNumberMsgUndelete) {
				int numberOfOldestToDelete = sfs.size() - maxNumberMsgUndelete;
				logger.debug("CommonMessagingAccess.saveChangesForRecycle(): total number of msg in recycle bin exceeds the limit " + 
						maxNumberMsgUndelete + " by " + numberOfOldestToDelete + "; to permanently delete some oldest msg in recycle bin");
				for (int i = 0; i < numberOfOldestToDelete; i++) {
					StateFile sf = sfs.get(i);
					cancelScheduledEvent(sf);
					deleteMessage(sf);
				}
			}
		} catch (MsgStoreException e) {
			logger.error("CommonMessagingAccess.saveChangesForRecycle() error updating recycled msg list for " + stateFile.rmsa.getId() + ": " + e, e);
			// Do not throw exception as the recylcing op itself succeeded
		} catch (Exception e) {
			logger.error("CommonMessagingAccess.saveChangesForRecycle() error updating recycled msg list for " + stateFile.rmsa.getId() + ": " + e, e);
			// Do not throw exception as the recylcing op itself succeeded
		}
	}

	public static boolean checkIfStateFileExist(MessageIdentifier mfsId, MfsStateFolderType folderType){
		StateFile statefile = new StateFile(mfsId.omsa, mfsId.rmsa, mfsId.omsgid, mfsId.rmsgid, folderType);
		File state = new File(statefile.getPath());
		return state.exists();
	}


	/**
	 * 
	 * @param firedEventInfo AppliEventInfo received from LocalEventHandler
	 * @param storedEvent Event stored persistently by the client
	 * @return boolean True if both eventIds are the same
	 */
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//HS80034: Please see comments in com.abcxyz.messaging.vvs.ntf.notifier.isEventIdsMatching(String eventId)
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////     
	public boolean compareEventKeys(AppliEventInfo firedEventInfo, String storedEvent) {
		EventID storedEventId = null; 

		if ((firedEventInfo == null || firedEventInfo.getEventKey() == null || firedEventInfo.getEventKey().isEmpty()) ||
				(storedEvent == null || storedEvent.isEmpty())) {
			if(logger.isDebugEnabled()) {
				logger.debug("firedEvent or storedEvent is null or empty, considered not equal");
			}
			return false;
		}

		try {
			storedEventId = new EventID(storedEvent);
			if (storedEventId.getEventKey().contains(firedEventInfo.getEventKey())) {
				if(logger.isDebugEnabled()) {
					logger.debug("both firedEvent and storedEvent keys are the same");
				}
				return true;
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("firedEventKey " + storedEventId.getEventKey() + " differs from storedEventKey " + firedEventInfo.getEventKey());
				}
				return false;
			}
		} catch (InvalidEventIDException e) {
			logger.error("Invalid EventId for stored event " + storedEvent);
			return false;
		}
	}


	private class CosRetentionMessageExpirySetter implements MessageExpirySetter {
		@Override
		public void setMessageExpiry(Date expiryTime, StateFile stateFile) throws MsgStoreException {
			CommonMessagingAccess.this.setMessageExpiry(expiryTime, stateFile);
			CommonMessagingAccess.this.updateState(stateFile);
		}

		@Override
		public void cancelMessageExpiry(StateFile stateFile) throws MsgStoreException {
			CommonMessagingAccess.this.cancelScheduledEvent(stateFile);
			CommonMessagingAccess.this.updateState(stateFile);
		}
	}

	private class CosRetentionStateFileFetcher implements StateFileFetcher {
		@Override
		public StateFile[] getAllSubscriberMessages(String subscriberMsid) throws MsgStoreException {
			MSA msa = new MSA(subscriberMsid);
			StateFile[] stateFileList = searchStateFiles(msa, new StateAttributesFilter());
			return stateFileList;
		}
	}

	public void handleCosRetentionDaysChangedEvent(COSRetentionDaysChangedEvent event) throws MsgStoreException {
		if (cosRetentionDaysChangedEventHandler == null) {
			cosRetentionDaysChangedEventHandler = new CosRetentionDaysChangedEventHandler(logger, new CosRetentionMessageExpirySetter(), new CosRetentionStateFileFetcher());
		}

		cosRetentionDaysChangedEventHandler.handleCosRetentionDaysChangedEvent(event);
	}
} 
