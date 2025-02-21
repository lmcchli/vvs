package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * This class loads configuration and stores to parameter values
 * All methods and variables is static in this class to be able to read values from any class
 * needed.
 */
public class OMMConfiguration {

    // Defines default values
    private static Integer	def_rpcPort=8081;
    private static Integer	def_rpcMaxNoOfRetries=10;
    private	static String	def_hostName="localhost";
    private	static Integer	def_providedServiceInitialThreshold=100;
    private	static Integer	def_serviceenablerhighwatermark=100;
    private	static Integer	def_serviceenablerlowwatermark=10;

    public static final String HOSTNAME = "ommHostname";
    public static final String RPC_MAX_NO_OF_RETRIES = "ommRpcMaxNoOfRetries";
    public static final String RPC_RETRY_DELAY_SEC = "ommRpcRetryDelaySec";
    public static final String COUNTER_SAVE_TIMEOUT = "ommCounterSaveTimeout";
    public static final String COUNTER_SAVE_FILENAME_SUFFIX = "ommCounterSaveFilenameSuffix";
    public static final String COUNTER_SAVE_FILENAME_PATH = "ommCounterSaveFilenamePath";
    public static final String MAS_NO_RESPONSE_TIMEOUT_LIMIT_UNTIL_DOWN = "ommMasNoResponseTimeoutLimitUntilDown";
    public static final String PORT = "ommPort";

    public static final String INIT_THRESHOLD = "initThreshold";
    public static final String LOW_WATERMARK = "lowWatermark";
    public static final String HIGH_WATERMARK = "highWatermark";


    // Define variables
    private static Integer	rpcPort;
    private static Integer	rpcMaxNoOfRetries;

    private	static String	hostName;

    private	static String	masInstallPath;
    private	static String	masVersion;
    private	static String	masHost;

    private static ILogger log;

    private static List<OMMServiceEnablerConfig> seList =
            new ArrayList<OMMServiceEnablerConfig>();

    private static IConfiguration config;

    public OMMConfiguration(IConfiguration config) {
        log = ILoggerFactory.getILogger(OMMConfiguration.class);
        OMMConfiguration.config = config;
        init(config);

    }

    public OMMConfiguration() {
       log = ILoggerFactory.getILogger(OMMConfiguration.class);
       rpcPort = def_rpcPort;
       rpcMaxNoOfRetries = def_rpcMaxNoOfRetries;
       hostName = def_hostName;

    }



    /**
     * Initiate load of configuration
     * @param config config
     */
    private static void init(IConfiguration config) {
        try {

            IGroup mainGroup = config.getGroup(CommonOamManager.MAS_SPECIFIC_CONF);
            readOmmValues(mainGroup);
            readServiceEnablerValues(mainGroup);
        } catch (UnknownGroupException e) {
            log.error("Unable to load configuration :"+e.getMessage());
          } catch (GroupCardinalityException e) {
            log.error("Unable to load configuration :"+e.getMessage());
        }
    }

    /**
     * Reloads configuration upon a configuration change.
     */
    public static void reloadConfig(){
            log.debug("Reload config");
            init(config.getConfiguration());

    }

    private static void readServiceEnablerValues(IGroup group){
    	try {

			Map<String, Map<String, String>> serviceEnablersTable = group.getTable("OmmServiceEnablers.Table");

			Set<String> serviceEnablersKeySet = serviceEnablersTable.keySet();
			Iterator<String> serviceEnablersIterator = serviceEnablersKeySet.iterator();

			while(serviceEnablersIterator.hasNext()){
				String protocol = serviceEnablersIterator.next();

				Map<String, String> serviceEnablerMap = serviceEnablersTable.get(protocol);
	            Integer threshold = new Integer(serviceEnablerMap.get(INIT_THRESHOLD));
	            Integer highWaterMark = new Integer(serviceEnablerMap.get(HIGH_WATERMARK));
	            Integer lowWaterMark = new Integer(serviceEnablerMap.get(LOW_WATERMARK));
	            addSe(protocol,threshold,highWaterMark,lowWaterMark);

	            log.debug("Values read for "+protocol);
	            log.debug("  serviceenablerhighwatermark="+highWaterMark);
	            log.debug("  serviceenablerlowwatermark="+lowWaterMark);
	            log.debug("  providedServiceInitialThreshold="+threshold);

			}

    	} catch (Exception e) {
    		if (log.isDebugEnabled())
    			log.error("Service enabler Threshold list could not be retrieved from configuration. ["+ e.getMessage()+"]" );
    	}

    }

    /**
     * Reads configuration values.
     * @param group config group
     */
    private static void readOmmValues(IGroup group){
        try {
            hostName=group.getString(HOSTNAME,def_hostName);
            rpcPort=group.getInteger(PORT,def_rpcPort);
            rpcMaxNoOfRetries=group.getInteger(RPC_MAX_NO_OF_RETRIES,def_rpcMaxNoOfRetries);
        } catch (ParameterTypeException e) {
            log.error("Unable to read config values :"+e.getMessage());
        }

    }

    /**
     * Retreives HostName from last succsessful configuration load.
     * @return hostName
     */
    public static String getHostName() {
        return hostName;
    }
    /**
     * Retreives port from last succsessful configuration load.
     * @return port
     */
    public static Integer getPort() {
        return rpcPort;
    }

    /**
     * Retreives rpcMaxNoOfRetries from last succsessful configuration load.
     * @return rpcMaxNoOfRetries
     */
    public static Integer getRpcMaxNoOfRetries() {
        return rpcMaxNoOfRetries;
    }




    public static Integer getProvidedServiceInitialThreshold(String protocol){
        return getServiceEnablerConfig(protocol).getThreshold();
    }

    public static Integer getserviceEnablerHighWaterMark(String protocol){
        return getServiceEnablerConfig(protocol).getHighWaterMark();
    }

    public static Integer getserviceEnablerLowWaterMark(String protocol){
        return getServiceEnablerConfig(protocol).getLowWaterMark();
    }


    public synchronized List<OMMServiceEnablerConfig> getSeList() {
        return seList;
    }


    private static void addSe(String protocol, Integer threshold, Integer highWaterMark, Integer lowWaterMark) {
        OMMServiceEnablerConfig se = new OMMServiceEnablerConfig(protocol,threshold,highWaterMark,lowWaterMark);
        if (!seList.contains(se))
            seList.add(se);
    }

    private static OMMServiceEnablerConfig getServiceEnablerConfig(String protocol){
        for (OMMServiceEnablerConfig serviceEnablerConfig : seList) {
            if (serviceEnablerConfig.getProtocol().equals(protocol)) {
                return serviceEnablerConfig;
            }
        }

        log.error("Unable to read service enabler threshold values for "+protocol+", Using default");
        OMMServiceEnablerConfig seDefault;
        seDefault = new OMMServiceEnablerConfig(protocol,def_providedServiceInitialThreshold,def_serviceenablerhighwatermark,def_serviceenablerlowwatermark);
        return seDefault;
    }


}
