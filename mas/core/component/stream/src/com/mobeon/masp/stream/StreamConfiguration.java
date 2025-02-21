/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import javax.naming.ConfigurationException;

import com.mobeon.common.configuration.ConfigurationLoadException;
import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.MissingConfigurationFileException;
import com.mobeon.common.configuration.ParameterTypeException;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.common.configuration.UnknownParameterException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Configuration variables for stream, which is mainly RTP-specific variables.
 * <p>
 * The configuration variables are kept in a Map instead of being read directly
 * from a configuration group. There are two main reason for this 
 * implementation:
 * <ul>
 * <li>When reading configuration values, there is no need to handle exceptions
 *     issued from the configuration component.</li>
 * <li>When writing testcases, it is useful to be able to modify some 
 *     configuration values at runtime. This is not possible if values
 *     are always read from configuration component classes</li>
 * </ul> 
 * <p>
 * The drawback it that for each additional configuration parameter, a line
 * that reads the parameter value and inserts it into the Map has to be added.
 * 
 * @author Jörgen Terner
 */
public class StreamConfiguration {
	private static final ILogger LOGGER = 
		ILoggerFactory.getILogger(StreamConfiguration.class);

	/** Group name in configuration file. */
	private static final String CONFIGURATION_GROUP_NAME = "stream.conf";

	/** 
	 * Initial capacity of the map containing all configuration variables.
	 * Should be #variables / 0.75 or greater.
	 */
	private static final int INITIAL_MAP_CAPACITY = 23;

	private static final String PORT_POOL_BASE_PARAM = "portPoolBase";
	private static final String PORT_POOL_SIZE_PARAM = "portPoolSize";
	private static final String THREAD_POOL_SIZE_PARAM = "threadpoolsize";    
	private static final String THREAD_POOL_MAX_WAIT_TIME_SEC_PARAM = 
		"threadpoolmaxwaittimesec";    
	private static final String SYNC_CALL_WAIT_TIME_SEC_PARAM = 
		"syncCallMaxWaitTimeSec";
	private static final String USE_POOL_FOR_SESSIONS_PARAM = 
		"usepoolforrtpsessions";
	private static final String PACKETPEND_TIMEOUT_MICROSEC_PARAM = 
		"packetpendtimeoutmicrosec";
	private static final String SEND_PACKETS_AHEAD_PARAM = 
		"sendPacketsAheadms";
	private static final String EXPIRE_TIMEOUT_MS_PARAM = 
		"expireTimeOutms";
	private static final String MTU_PARAM = "maximumTransmissionUnit";
	private static final String STREAM_ABANDONED_MS_PARAM = 
		"streamAbandonedms";
	private static final String SENDERS_CONTROL_FRACTION_PARAM = 
		"sendersControlFraction";
	private static final String AUDIO_SKIP_MS_PARAM = "audioSkipms";
	private static final String AUDIO_REPLACE_WITH_SILENCE_MS_PARAM = 
		"audioReplaceWithSilencems";
	private static final String LOCAL_HOST_NAME_PARAM = "localHostName";
	private static final String DISPATCH_DTMF_ON_KEY_DOWN_PARAM = 
		"dispatchDtmfOnKeyDown";
	private static final String SKEW_MS_PARAM = "skew";
	private static final String SKEW_METHOD_PARAM = "skewMethod";
	private static final String MAX_WAIT_FOR_IFRAME_PARAM = 
		"maxWaitForIframems";
	private static final String INBOUND_PTIME_PARAM = 
		"defaultInboundPtime";
	private static final String INBOUND_MAXPTIME_PARAM = 
		"defaultInboundMaxPtime";
	private static final String MOV_FILE_VERSION_PARAM = "movFileVersion";
	private static final String OUTPUT_PROCESSORS_PARAM = "outputProcessors";
	private static final String INPUT_PROCESSORS_PARAM = "inputProcessors";

	/** Subgroup name for supported content-types in configuration file. */
	private static final String CONFIGURATION_CONTENTTYPE_GROUP_NAME = 
		"SupportedContentType.List";    
	private static final String SUPPORTED_CONTENT_TYPE_NAME_PARAM = 
		"name";

	/** Subgroup name for RTP payload mappings in configuration file. */
	private static final String CONFIGURATION_RTPPAYLOADDEF_GROUP_NAME = 
		"RtpPayloadDef.Table";
	private static final String RTPPAYLOAD_PRIMARY_TYPE = "primaryType";
	private static final String RTPPAYLOAD_SUBTYPE = "subType";
	private static final String RTPPAYLOAD_RTPPAYLOAD = "rtpPayload";
	private static final String RTPPAYLOAD_ENCODING = "encoding";
	private static final String RTPPAYLOAD_CLOCKRATE = "clockRate";
	private static final String RTPPAYLOAD_BITRATE = "bitRate";
	private static final String RTPPAYLOAD_MEDIA_FORMAT_PARAMETERS = "mediaFormatParameters";
	private static final String RTPPAYLOAD_RS= "rs";
	private static final String RTPPAYLOAD_RR= "rr";
	private static final String RTPPAYLOAD_MINRS= "minrs";
	private static final String RTPPAYLOAD_MAXRS= "maxrs";
	private static final String RTPPAYLOAD_MINRR= "minrr";
	private static final String RTPPAYLOAD_MAXRR= "maxrr";

	/** Subgroup name for Silence Detection in configuration file. */
	private static final String CONFIGURATION_SILENCEDETECT_GROUP_NAME =
		"silenceDetection";    
	private static final String SILENCE_DETECTION_MODE = "silenceDetectionMode";
	private static final String SILENCE_THRESHOLD = "silenceThreshold";    
	private static final String INITIAL_SILENCE_FRAMES = "initialSilenceFrames";    
	private static final String SILENCE_DEADBAND = "silenceDeadband";
	private static final String SIGNAL_DEADBAND = "signalDeadband";    
	private static final String DETECTION_FRAMES = "detectionFrames"; 
	private static final String SILENCE_DETECTION_DEBUG_LEVEL = "silenceDetectionDebugLevel";                            

	/** Default number of RTP port pairs **/
	public static final int DEFAULT_PORT_POOL_SIZE = 100;

	/** Default start of RTP port range **/
	public static final int DEFAULT_PORT_POOL_BASE = 36000;

	/** Default number of threads in the threadpool. */
	public static final int DEFAULT_THREADPOOL_SIZE = 135;

	/** Default timeout in seconds for the threads while waiting for jobs. */
	public static final int DEFAULT_THREADPOOL_WAIT_TIME_SEC = 60;

	/** 
	 * Default timeout in seconds a synchronous call is made to wait for
	 * an operation to finish. 
	 */
	public static final int DEFAULT_SYNC_CALL_WAIT_TIME_SEC = 120;

	/** 
	 * The number of microseconds a session waits for data to arrive
	 * on a socket.
	 */
	public static final int DEFAULT_PACKETPEND_TIMEOUT_MICROSEC = 5000;

	/** 
	 * The number of milliseconds packets will be dispatched ahead of their 
	 * timestamp.
	 */
	public static final int DEFAULT_SEND_PACKETS_AHEAD_MS = 40;

	/** Default timeout to expire unsent packets in milliseconds. */
	public static final int DEFAULT_EXPIRE_TIMEOUT_MS = 10000000;

	/** 
	 * Default Maximum Transmission Unit (maximum payload segment size before
	 * fragmenting sends) in octets (one octet = 8 bits).
	 * <p>
	 * The IP layer can accept packets up to 65535 octets. The largest 
	 * packet an Ethernet can deliver is 1500 octets.
	 */
	public static final int DEFAULT_MTU = 2000;

	/** 
	 * Default maximum silent period in milliseconds before a stream
	 * is considered as abandoned (silence = no RTP-packets).
	 */
	public static final int DEFAULT_ABANDONED_STREAM_TIMEOUT_MS = 32000; 

	/** Default senders control fraction (0 - 1). */
	private static final float DEFAULT_SENDERS_CONTROL_FRACTION = 0.4f;

	/** Default amount of audio to skip while recording audio (milliseconds).*/
	private static final int DEFAULT_AUDIO_SKIP_MS = 10;

	/** 
	 * Default maximum wait time for an I-frame while recording video 
	 * (milliseconds).
	 */
	private static final int DEFAULT_MAX_WAIT_IFRAME_MS = 2000;

	/** 
	 * Default amount of audio to replace with silence while recording 
	 * video (milliseconds). 
	 */
	private static final int DEFAULT_AUDIO_REPLACE_WITH_SILENCE_MS = 10;

	/** Default address of local host. */
	private String mDefaultLocalHostAddress = "0.0.0.0";

	/**
	 * If <code>true</code> DTMF events are dispatched on key down, if 
	 * <code>false</code> DTMF events are dispatched on key up.
	 */
	private static final Boolean DEFAULT_DISPATCH_DTMF_ON_KEY_DOWN = 
		Boolean.TRUE;

	private static final int DEFAULT_INBOUND_PTIME = 40;
	private static final int DEFAULT_INBOUND_MAXPTIME = 40;

	private static final int DEFAULT_MOV_FILE_VERSION = 1;
	private static final int DEFAULT_OUTPUT_PROCESSORS = 1;
	private static final int DEFAULT_INPUT_PROCESSORS = 4;

	/**
	 * If <code>true</code>, all RTP sessions will be served by a pool of 
	 * threads, if <code>false</code>, each RTP session will be served by 
	 * its own thread.
	 */
	private static final Boolean DEFAULT_USE_POOL_FOR_SESSIONS = 
		Boolean.FALSE;

	/** Default skew (milliseconds).*/
	private static final int DEFAULT_SKEW_MS = 0;

	/** Default skew method. */
	private static final IMediaStream.SkewMethod DEFAULT_SKEW_METHOD = 
		IMediaStream.SkewMethod.LOCAL_AND_RTCP;


	private static final int DEFAULT_SILENCE_DETECTION_MODE = 0;   
	private static final int DEFAULT_SILENCE_THRESHOLD = 0;    
	private static final int DEFAULT_INITIAL_SILENCE_FRAMES = 40;    
	private static final int DEFAULT_SILENCE_DEADBAND = 150;
	private static final int DEFAULT_SIGNAL_DEADBAND = 10;    
	private static final int DEFAULT_DETECTION_FRAMES = 10;
	private static final int DEFAULT_SILENCE_DETECTION_DEBUG_LEVEL = 0;

	private static final StreamConfiguration INSTANCE = 
		new StreamConfiguration();

	/** Current configuration. */
	private AtomicReference<Map<String, Object>> mConfiguration =
		new AtomicReference<Map<String, Object>>();

	/**
	 * Initial configuration instance. Is used to read an updated configuration
	 * when the "configuration has changed"-event is received.
	 */
	private IConfiguration mInitialConfiguration;


	//This is the new configManager from OEManager of MessageCore
	IGroup streamConfig;
	
	
	private static final String R_IPADDRESS = System.getenv("r_IPADDRESS");
	

	/**
	 * Creates the single StreamConfiguration instance.
	 */
	private StreamConfiguration() {
	}

	/**
	 * @return The single StreamConfiguration instance.
	 */
	public static StreamConfiguration getInstance() {
		return INSTANCE;
	}

	/**
	 * Sets the initial configuration instance. This method should only
	 * be called once when the stream component is initiated.
	 * 
	 * @param config The initial configuration isntance.
	 * 
	 * @throws IllegalArgumentException  If <code>config</code> is 
	 *         <code>null</code>.
	 */
	public void setInitialConfiguration(IConfiguration config) {
		if (config == null) {
			throw new IllegalArgumentException(
					"Parameter config may not be null");
		}
		mInitialConfiguration = config;
	}

	/**
	 * Reads configuration parameters.
	 *
	 * @throws GroupCardinalityException 
	 * @throws UnknownGroupException             If the group was not found.
	 * @throws MissingConfigurationFileException
	 * @throws ConfigurationLoadException
	 * @throws ParameterTypeException
	 */
	/* package */ public void update()
	throws GroupCardinalityException, UnknownGroupException, 
	MissingConfigurationFileException, ConfigurationLoadException,
	ParameterTypeException, ConfigurationException {
		// TODO: what if the port pool configuration has changed? Must restart ...
		IConfiguration configuration = mInitialConfiguration.getConfiguration();
		Map<String, Object> newConfig = new HashMap<String, Object>(INITIAL_MAP_CAPACITY);
		streamConfig = configuration.getGroup(CONFIGURATION_GROUP_NAME);

		newConfig.put(PORT_POOL_BASE_PARAM,
				streamConfig.getInteger(PORT_POOL_BASE_PARAM, DEFAULT_PORT_POOL_BASE));
		newConfig.put(PORT_POOL_SIZE_PARAM,
				streamConfig.getInteger(PORT_POOL_SIZE_PARAM, DEFAULT_PORT_POOL_SIZE));
		newConfig.put(THREAD_POOL_SIZE_PARAM,
				streamConfig.getInteger(THREAD_POOL_SIZE_PARAM, DEFAULT_THREADPOOL_SIZE));
		newConfig.put(THREAD_POOL_MAX_WAIT_TIME_SEC_PARAM, 
				streamConfig.getInteger(THREAD_POOL_MAX_WAIT_TIME_SEC_PARAM, 
						DEFAULT_THREADPOOL_WAIT_TIME_SEC));
		newConfig.put(SYNC_CALL_WAIT_TIME_SEC_PARAM, 
				streamConfig.getInteger(SYNC_CALL_WAIT_TIME_SEC_PARAM, 
						DEFAULT_SYNC_CALL_WAIT_TIME_SEC));
		newConfig.put(PACKETPEND_TIMEOUT_MICROSEC_PARAM, 
				streamConfig.getInteger(PACKETPEND_TIMEOUT_MICROSEC_PARAM, 
						DEFAULT_PACKETPEND_TIMEOUT_MICROSEC));
		newConfig.put(SEND_PACKETS_AHEAD_PARAM, 
				streamConfig.getInteger(SEND_PACKETS_AHEAD_PARAM, 
						DEFAULT_SEND_PACKETS_AHEAD_MS));
		newConfig.put(EXPIRE_TIMEOUT_MS_PARAM, 
				streamConfig.getInteger(EXPIRE_TIMEOUT_MS_PARAM, 
						DEFAULT_EXPIRE_TIMEOUT_MS));
		newConfig.put(MTU_PARAM, 
				streamConfig.getInteger(MTU_PARAM, DEFAULT_MTU));
		newConfig.put(STREAM_ABANDONED_MS_PARAM, 
				streamConfig.getInteger(STREAM_ABANDONED_MS_PARAM, 
						DEFAULT_ABANDONED_STREAM_TIMEOUT_MS));
		newConfig.put(SENDERS_CONTROL_FRACTION_PARAM, 
				streamConfig.getFloat(SENDERS_CONTROL_FRACTION_PARAM, 
						DEFAULT_SENDERS_CONTROL_FRACTION));
		newConfig.put(AUDIO_SKIP_MS_PARAM, 
				streamConfig.getInteger(AUDIO_SKIP_MS_PARAM, 
						DEFAULT_AUDIO_SKIP_MS));
		newConfig.put(AUDIO_REPLACE_WITH_SILENCE_MS_PARAM, 
				streamConfig.getInteger(AUDIO_REPLACE_WITH_SILENCE_MS_PARAM, 
						DEFAULT_AUDIO_REPLACE_WITH_SILENCE_MS));
		newConfig.put(SKEW_MS_PARAM, 
				streamConfig.getInteger(SKEW_MS_PARAM, 
						DEFAULT_SKEW_MS));
		newConfig.put(MAX_WAIT_FOR_IFRAME_PARAM, 
				streamConfig.getInteger(MAX_WAIT_FOR_IFRAME_PARAM, 
						DEFAULT_MAX_WAIT_IFRAME_MS));
		newConfig.put(INBOUND_PTIME_PARAM, 
				streamConfig.getInteger(INBOUND_PTIME_PARAM, 
						DEFAULT_INBOUND_PTIME));
		newConfig.put(INBOUND_MAXPTIME_PARAM, 
				streamConfig.getInteger(INBOUND_MAXPTIME_PARAM, 
						DEFAULT_INBOUND_MAXPTIME));

		newConfig.put(MOV_FILE_VERSION_PARAM,
				streamConfig.getInteger(MOV_FILE_VERSION_PARAM,
						DEFAULT_MOV_FILE_VERSION));

		newConfig.put(OUTPUT_PROCESSORS_PARAM,
				streamConfig.getInteger(OUTPUT_PROCESSORS_PARAM,
						DEFAULT_OUTPUT_PROCESSORS));

		newConfig.put(INPUT_PROCESSORS_PARAM,
				streamConfig.getInteger(INPUT_PROCESSORS_PARAM,
						DEFAULT_INPUT_PROCESSORS));

		newConfig.put(SILENCE_DETECTION_MODE,
				streamConfig.getInteger(SILENCE_DETECTION_MODE,
						DEFAULT_SILENCE_DETECTION_MODE));

		newConfig.put(SILENCE_THRESHOLD,
				streamConfig.getInteger(SILENCE_THRESHOLD,
						DEFAULT_SILENCE_THRESHOLD));    

		newConfig.put(INITIAL_SILENCE_FRAMES,
				streamConfig.getInteger(INITIAL_SILENCE_FRAMES,
						DEFAULT_INITIAL_SILENCE_FRAMES));

		newConfig.put(SILENCE_DEADBAND,
				streamConfig.getInteger(SILENCE_DEADBAND,
						DEFAULT_SILENCE_DEADBAND));    

		newConfig.put(SIGNAL_DEADBAND,
				streamConfig.getInteger(SIGNAL_DEADBAND,
						DEFAULT_SIGNAL_DEADBAND));

		newConfig.put(DETECTION_FRAMES,
				streamConfig.getInteger(DETECTION_FRAMES,
						DEFAULT_DETECTION_FRAMES)); 

		newConfig.put(SILENCE_DETECTION_DEBUG_LEVEL,
				streamConfig.getInteger(SILENCE_DETECTION_DEBUG_LEVEL,
						DEFAULT_SILENCE_DETECTION_DEBUG_LEVEL));


		String method = streamConfig.getString(SKEW_METHOD_PARAM,
				DEFAULT_SKEW_METHOD.toString());
		newConfig.put(SKEW_METHOD_PARAM, toSkewMethod(method));

		
		// Always use .conf/.xsd value in priority.
		// Fallback to r_IPADDRESS if configured or else to hardcoded mDefaultLocalHostAddress
		String localHostName = streamConfig.getString(LOCAL_HOST_NAME_PARAM, null);
		// This above getString method uses the provided default value, only if the parameter
		// does not exist. The default value is not used if the parameter exists but has no
		// set value.
		if (localHostName == null || localHostName.isEmpty()) {
		    if (R_IPADDRESS != null && !R_IPADDRESS.isEmpty()) {
		        localHostName = R_IPADDRESS;
		    } else {
		        try {
		            mDefaultLocalHostAddress = InetAddress.getLocalHost().getHostAddress();
		        } catch (UnknownHostException e) {
		            LOGGER.debug("Could not retrieve the local hostname. Using hardcoded localHostName [" + 
		                    mDefaultLocalHostAddress + "]. Exception: " + e);
		        }
		        localHostName = mDefaultLocalHostAddress;
		    }
		}
		
		newConfig.put(LOCAL_HOST_NAME_PARAM, localHostName);
		

		String dispatchOnKeyDownStr = 
			streamConfig.getString(DISPATCH_DTMF_ON_KEY_DOWN_PARAM, 
					null);
		Boolean dispatchOnKeyDown = DEFAULT_DISPATCH_DTMF_ON_KEY_DOWN;
		if (dispatchOnKeyDownStr != null) {
			if ("false".equals(dispatchOnKeyDownStr)) {
				dispatchOnKeyDown = Boolean.FALSE;
			}
			else {
				dispatchOnKeyDown = Boolean.TRUE;
			}
		}
		newConfig.put(DISPATCH_DTMF_ON_KEY_DOWN_PARAM, dispatchOnKeyDown);

		String usePoolForSessionsStr = 
			streamConfig.getString(USE_POOL_FOR_SESSIONS_PARAM, 
					null);
		Boolean usePoolForSessions = DEFAULT_USE_POOL_FOR_SESSIONS;
		if (usePoolForSessionsStr != null) {
			if ("true".equals(usePoolForSessionsStr)) {
				usePoolForSessions = Boolean.TRUE;
			}
			else {
				usePoolForSessions = Boolean.FALSE;
			}
		}
		newConfig.put(USE_POOL_FOR_SESSIONS_PARAM, usePoolForSessions);

		// Read supported content-types
		ArrayList<String> contentTypeList = 
			streamConfig.getList(CONFIGURATION_CONTENTTYPE_GROUP_NAME);
		
		List<MimeType> supportedTypes = new ArrayList<MimeType>();
		try {

			for (int i=0; i < contentTypeList.size(); i++) {
				if (contentTypeList.get(i)!= null) {
					supportedTypes.add(new MimeType((String)contentTypeList.get(i)));
					LOGGER.debug("Supported contentType=" + contentTypeList.get(i));
				}
				else {
					LOGGER.debug("Could not read supported content-type for " +
					"an element in the configuration.");
				}
			}

		}
		catch (MimeTypeParseException e) {
			String msg = 
				"Could not parse content type specified in the configuration";
			LOGGER.info(msg, e);
			throw new ConfigurationException(msg);
		} 
		      
		newConfig.put(CONFIGURATION_CONTENTTYPE_GROUP_NAME, supportedTypes);

		// Read RTP payload mappings
		Map<String, Map<String, String>> rtppayloadmapping = 
			streamConfig.getTable(CONFIGURATION_RTPPAYLOADDEF_GROUP_NAME);
		List<RTPPayload> mapping = new ArrayList<RTPPayload>();
		try {
			boolean foundDTMF = false;
			boolean foundCN = false;

			Iterator<String> iter = rtppayloadmapping.keySet().iterator();
			while (iter.hasNext()) {
				Map<String, String> rtp = rtppayloadmapping.get(iter.next());
				MimeType mimeType = new MimeType(rtp.get(RTPPAYLOAD_PRIMARY_TYPE),rtp.get(RTPPAYLOAD_SUBTYPE)) ;
				if (mimeType.toString().equalsIgnoreCase(
						RTPPayload.AUDIO_DTMF.toString())) {
					foundDTMF = true;
				}
				if (mimeType.toString().equalsIgnoreCase(
						RTPPayload.AUDIO_CN.toString())) {
					foundCN = true;
				}
				RTPPayload p = createRTPPayload(rtp, mimeType);
				mapping.add(p);
			}
			if (!foundDTMF) {
				throw new IllegalStateException(
						"No RTP payload mapping exists for DTMF. " +
						"A default mapping should always exists in the " +
						"configuration.");
			}
			if (!foundCN) {
				throw new IllegalStateException(
						"No RTP payload mapping exists for " +
						"CN (Comfort noise). " +
						"A default mapping should always exists in the " +
				"configuration.");
			}
		}
		catch (MimeTypeParseException e) {
			String msg = 
				"Could not parse MimeType specified in the configuration";
			LOGGER.debug(msg,e);
			throw new ConfigurationException(msg);
		}
		catch (UnknownParameterException e) {
			// This should never happen. The configuration is validated
			// against a schema that should prevent this.
			String msg = 
				"Could not read parameter from configuration file";
			LOGGER.debug(msg,e);
			throw new ConfigurationException(msg + ": " + e);
		}
		newConfig.put(CONFIGURATION_RTPPAYLOADDEF_GROUP_NAME, mapping);

		
		LOGGER.info("StreamConfiguration:update() : \n\nContent of configuration Hash " + newConfig.toString() + "\n\n");
		
		
		Map<String, Object> oldConfig = mConfiguration.get();
		mConfiguration.set(newConfig);
		if (oldConfig != null) {
			oldConfig.clear();
		}

		// Update dependent instances
		RTPPayload.updateDefs(mapping);

		ConnectionProperties.updateDefaultPTimes(
				getDefaultPTime(), 
				getDefaultMaxPTime());

	}

	private RTPPayload createRTPPayload(Map<String, String> g, MimeType mimeType)
	throws UnknownParameterException, ParameterTypeException {
		RTPPayload p;
		//
		// Different RTPPayload depending on whether bandwidth modifiers max/min 
		// values are included or not.
		if( (g.get(RTPPAYLOAD_MINRS)== null )
				&& (g.get(RTPPAYLOAD_MAXRS)==null )
				&& (g.get(RTPPAYLOAD_MINRR)==null )
				&& (g.get(RTPPAYLOAD_MAXRR)==null)
				&& (g.get(RTPPAYLOAD_RS)==null )
				&& (g.get(RTPPAYLOAD_RR)==null )
		)
		{
			p = new RTPPayload(
					Integer.parseInt(g.get(RTPPAYLOAD_RTPPAYLOAD)),
					mimeType,
					g.get(RTPPAYLOAD_ENCODING),
					Integer.parseInt(g.get(RTPPAYLOAD_CLOCKRATE)),
					1, // Channels not supported yet
					Integer.parseInt(g.get(RTPPAYLOAD_BITRATE)),
					g.get(RTPPAYLOAD_MEDIA_FORMAT_PARAMETERS));

			LOGGER.debug("Added RTP payload def: " +
					"PayloadType=" + p.getPayloadType() + 
					", MimeType=" + p.getMimeType().toString() +
					", Encoding=" + p.getEncoding() +
					", ClockRate=" + p.getClockRate() +
					", BandWidth=" + p.getBitrate() + 
					", MediaFormatParameters=" + p.getMediaFormatParameters());
		} else {
			p = new RTPPayload(
					Integer.parseInt(g.get(RTPPAYLOAD_RTPPAYLOAD)),
					mimeType,
					g.get(RTPPAYLOAD_ENCODING),
					Integer.parseInt(g.get(RTPPAYLOAD_CLOCKRATE)),
					1, // Channels not supported yet
					Integer.parseInt(g.get(RTPPAYLOAD_BITRATE)),
					g.get(RTPPAYLOAD_MEDIA_FORMAT_PARAMETERS),	
					Integer.parseInt(g.get(RTPPAYLOAD_RS)),
					Integer.parseInt(g.get(RTPPAYLOAD_MINRS)),
					Integer.parseInt(g.get(RTPPAYLOAD_MAXRS)),
					Integer.parseInt(g.get(RTPPAYLOAD_RR)), 
					Integer.parseInt(g.get(RTPPAYLOAD_MINRR)), 
					Integer.parseInt(g.get(RTPPAYLOAD_MAXRR))
			);

			LOGGER.debug("Added RTP payload def: " +
					"PayloadType=" + p.getPayloadType() + 
					", MimeType=" + p.getMimeType().toString() +
					", Encoding=" + p.getEncoding() +
					", ClockRate=" + p.getClockRate() +
					", BandWidth=" + p.getBitrate() + 
					", MediaFormatParameters=" + p.getMediaFormatParameters() +
					", RS=" + p.getBwSender() +
					", minRS=" + p.getMinSender() +
					", maxRS=" + p.getMaxSender() +
					", RR=" + p.getBwReceiver() +
					", minRR=" + p.getMinReceiver() +
					", maxRR=" + p.getMaxReceiver());                    
		}
		return p;
	}

	public int getPortPoolBase() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}

		Integer i = (Integer)mConfiguration.get().get(PORT_POOL_BASE_PARAM);
		return (i != null) ? i.intValue() : DEFAULT_PORT_POOL_BASE;
	}

	public int getPortPoolSize() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}

		Integer i = (Integer)mConfiguration.get().get(PORT_POOL_SIZE_PARAM);
		return (i != null) ? i.intValue() : DEFAULT_PORT_POOL_SIZE;
	}

	/**
	 * Limits the number of concurrently executed requests.
	 * <p>
	 * This is a static configuration variable, it will only be read at system
	 * startup.
	 * 
	 * @return Number of threads in the thread pool.
	 */

	
    public int getThreadPoolSize() {
        if (mConfiguration.get() == null) {
            throw new IllegalStateException("Methods setInitialConfiguration" +
                    " and update must be called first.");
        }

        Integer i = (Integer)mConfiguration.get().get(THREAD_POOL_SIZE_PARAM);
        return (i != null) ? i.intValue() : DEFAULT_THREADPOOL_SIZE;
    }
	
	// TODO: remove this
	/**
	 * This is a static configuration variable, it will only be read at system
	 * startup.
	 * 
	 * @return Maximum wait time in seconds for the threads in the threadpool.
	 */
	public int getThreadPoolMaxWaitTime() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(
				THREAD_POOL_MAX_WAIT_TIME_SEC_PARAM);
		return (i != null) ? i.intValue() : DEFAULT_THREADPOOL_WAIT_TIME_SEC;
	}

	// TODO: remove this
	/**
	 * This configuration variable is static within the lifetime of a stream
	 * (that is, it is read once when a stream is created).
	 * 
	 * @return Maximum time in seconds a caller is made to wait for an 
	 *         operation to finish in a sync call.
	 */
	public int getSyncCallMaxWaitTime() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(
				SYNC_CALL_WAIT_TIME_SEC_PARAM);
		return (i != null) ? i.intValue() : DEFAULT_SYNC_CALL_WAIT_TIME_SEC;
	}

	/**
	 * @return The number of microseconds a session waits for data to arrive
	 *         on a socket.
	 */
	public int getPacketPendTimeout() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(
				PACKETPEND_TIMEOUT_MICROSEC_PARAM);
		return (i != null) ? i.intValue() : DEFAULT_PACKETPEND_TIMEOUT_MICROSEC;
	}

	/**
	 * @return The number of milliseconds packets will be dispatched ahead
	 *         of their timestamp.
	 */
	public int getSendPacketsAhead() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(
				SEND_PACKETS_AHEAD_PARAM);
		return (i != null) ? i.intValue() : DEFAULT_SEND_PACKETS_AHEAD_MS;
	}

	/**
	 * "Expired" timer in milliseconds for expiring packets pending in the
	 * send queue which have gone unsent and are already "too late" to be 
	 * sent now.
	 * <p>
	 * This configuration variable is static within the lifetime of a stream
	 * (that is, it is read once when a stream is created).
	 * 
	 * @return Timeout to expire unsent packets in milliseconds.
	 */
	public int getExpireTimeout() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(
				EXPIRE_TIMEOUT_MS_PARAM);
		return (i != null) ? i.intValue() : DEFAULT_EXPIRE_TIMEOUT_MS;
	}

	/**
	 * XXX Not used yet!
	 * 
	 * @return Maximum payload segment size before fragmenting sends.
	 */
	public int getMaximumTransmissionUnit() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(MTU_PARAM);
		return (i != null) ? i.intValue() : DEFAULT_MTU;
	}

	/**
	 * @return Maximum silent period in milliseconds before a stream
	 *         is considered as abandoned (silence = no RTP-packets).
	 */
	public int getAbandonedStreamDetectedTimeout() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = 
			(Integer)mConfiguration.get().get(STREAM_ABANDONED_MS_PARAM);
		return (i != null)? i.intValue() : DEFAULT_ABANDONED_STREAM_TIMEOUT_MS;
	}

	/**
	 * @return Maximum time in milliseconds a record will wait for the
	 *         first I-frame before starting to record.
	 */
	public int getMaxWaitForIFrameTimeout() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = 
			(Integer)mConfiguration.get().get(MAX_WAIT_FOR_IFRAME_PARAM);
		return (i != null)? i.intValue() : DEFAULT_MAX_WAIT_IFRAME_MS;
	}

	/**
	 * Gets the fraction of the total control bandwith to be dedicated 
	 * to senders reports.
	 * <p>
	 * Of course, 1 - fraction will be dedicated to receiver reports.
	 * 
	 * @return fraction Fraction of bandwidth, between 0 an 1.
	 */    
	public float getSendersControlFraction() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Double d = (Double)mConfiguration.get().get(SENDERS_CONTROL_FRACTION_PARAM);
		return (d != null)? d.floatValue() : DEFAULT_SENDERS_CONTROL_FRACTION;
	}

	/**
	 * Gets all supported content-types.
	 * 
	 * @return A list with all supported content-types, can never be
	 *         <code>null</code>.
	 */    
	@SuppressWarnings("unchecked")
	public List<MimeType> getSupportedContentTypes() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		return (List<MimeType>)mConfiguration.get().get(CONFIGURATION_CONTENTTYPE_GROUP_NAME);
	}

	/**
	 * Gets all RTP payload definitions.
	 * 
	 * @return A list with all RTP payload definitions, can never be
	 *         <code>null</code>.
	 */    
	@SuppressWarnings("unchecked")
	public List<RTPPayload> getRTPPayloadDefs() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		return (List<RTPPayload>)mConfiguration.get().get(
				CONFIGURATION_RTPPAYLOADDEF_GROUP_NAME);
	}

	/**
	 * Gets the amount of audio to skip while recording. 
	 * This is used to avoid hearing the "beep" first in the recording.
	 * 
	 * @return Amount of audio to skip in milliseconds.
	 */
	public int getAudioSkip() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(AUDIO_SKIP_MS_PARAM);
		return (i != null)? i.intValue() : DEFAULT_AUDIO_SKIP_MS;
	}

	/**
	 * Gets the amount of milliseconds the audio should be sent ahead of the 
	 * video. If negative, the number of milliseconds the video should be sent
	 * ahead of the audio.
	 * 
	 * @return Number of milliseconds the audio should be sent ahead of the 
	 *         video. 
	 */
	public int getSkew() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(SKEW_MS_PARAM);
		return (i != null)? i.intValue() : DEFAULT_SKEW_MS;
	}

	/**
	 * @return The integer representation of the skew method.
	 *  
	 * @see IMediaStream.SkewMethod
	 */
	public int getSkewMethodIntRep() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		IMediaStream.SkewMethod m = (IMediaStream.SkewMethod)
		mConfiguration.get().get(SKEW_METHOD_PARAM);
		return (m != null)? m.decimalDef() : DEFAULT_SKEW_METHOD.decimalDef();
	}

	/**
	 * Gets the amount of audio data that is replaced by silence
	 * in a video stream during record. This is used to avoid hearing 
	 * the "beep" first in the recording.
	 * 
	 * @return Amount of audio to replace in milliseconds.
	 */
	public int getAudioReplaceWithSilence() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(
				AUDIO_REPLACE_WITH_SILENCE_MS_PARAM);
		return (i != null)? i.intValue() : 
			DEFAULT_AUDIO_REPLACE_WITH_SILENCE_MS;
	}

	/**
	 * Gets the default pTime for audio.
	 * 
	 * @return pTime in milliseconds.
	 */
	public int getDefaultPTime() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(INBOUND_PTIME_PARAM);
		return (i != null)? i.intValue() : DEFAULT_INBOUND_PTIME;
	}

	/**
	 * Gets the default maxPTime for audio.
	 * 
	 * @return maxPTime in milliseconds.
	 */
	public int getDefaultMaxPTime() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(INBOUND_MAXPTIME_PARAM);
		return (i != null)? i.intValue() : DEFAULT_INBOUND_MAXPTIME;
	}

	public int getMovFileVersion() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}

		Integer i = (Integer)mConfiguration.get().get(MOV_FILE_VERSION_PARAM);
		return (i != null) ? i.intValue() : DEFAULT_MOV_FILE_VERSION;
	}

	public int getOutputProcessors() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(OUTPUT_PROCESSORS_PARAM);
		return (i != null)? i : DEFAULT_OUTPUT_PROCESSORS;
	}

	public int getInputProcessors() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(INPUT_PROCESSORS_PARAM);
		return (i != null)? i : DEFAULT_INPUT_PROCESSORS;
	}

	public int getSilenceDetectionMode() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(SILENCE_DETECTION_MODE);
		return (i != null)? i : DEFAULT_SILENCE_DETECTION_MODE;

	}


	public int getSilenceThreshold() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(SILENCE_THRESHOLD);
		return (i != null)? i : DEFAULT_SILENCE_THRESHOLD;

	}


	public int getInitialSilenceFrames() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(INITIAL_SILENCE_FRAMES);
		return (i != null)? i : DEFAULT_INITIAL_SILENCE_FRAMES;

	}


	public int getSilenceDeadband() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(SILENCE_DEADBAND);
		return (i != null)? i : DEFAULT_SILENCE_DEADBAND;

	}


	public int getSignalDeadband() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(SIGNAL_DEADBAND);
		return (i != null)? i : DEFAULT_SIGNAL_DEADBAND;

	}


	public int getDetectionFrames() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(DETECTION_FRAMES);
		return (i != null)? i : DEFAULT_DETECTION_FRAMES;

	}


	public int getSilenceDetectionDebugLevel() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Integer i = (Integer)mConfiguration.get().get(SILENCE_DETECTION_DEBUG_LEVEL);
		return (i != null)? i : DEFAULT_SILENCE_DETECTION_DEBUG_LEVEL;

	}



	/**
	 * @return Name of local host, for example "0.0.0.0" or "127.0.0.1".
	 */
	public String getLocalHostName() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		String s = (String)mConfiguration.get().get(LOCAL_HOST_NAME_PARAM);
		return (s != null) ? s : mDefaultLocalHostAddress;
	}

	/**
	 * @return <code>true</code> if DTMF events are dispatched on key down.
	 */
	public boolean isDispatchDTMFOnKeyDown() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Boolean b = (Boolean)mConfiguration.get().get(
				DISPATCH_DTMF_ON_KEY_DOWN_PARAM);
		return (b != null) ? b == Boolean.TRUE : 
			DEFAULT_DISPATCH_DTMF_ON_KEY_DOWN;
	}

	/**
	 * @return <code>true</code> if all RTP session is served by pooled threads,
	 *         <code>false</code> if all RTP session creates its own thread.
	 */
	public boolean isUsePoolForRTPSessions() {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		Boolean b = (Boolean)mConfiguration.get().get(
				USE_POOL_FOR_SESSIONS_PARAM);
		return (b != null) ? b == Boolean.TRUE : 
			DEFAULT_USE_POOL_FOR_SESSIONS;
	}

	/**
	 * Sets the abandoned stream detected timeout.
	 * 
	 * @param timeout Timout in milliseconds.
	 */
	/* package */ void setAbandonedStreamDetectedTimeout(int timeout) {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		mConfiguration.get().put(STREAM_ABANDONED_MS_PARAM, timeout);
	}

	/**
	 * Sets amount of milliseconds of audio to skip.
	 * 
	 * @param skip Length in milliseconds.
	 */
	/* package */ void setAudioSkip(int skip) {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		mConfiguration.get().put(AUDIO_SKIP_MS_PARAM, skip);
	}

	/**
	 * @param val <code>true</code> if DTMF events should be dispatched on key 
	 *        down.
	 */
	/*package*/ void setDispatchDTMFOnKeyDown(boolean val) {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		mConfiguration.get().put(DISPATCH_DTMF_ON_KEY_DOWN_PARAM, 
				val ? Boolean.TRUE : Boolean.FALSE);
	}

	/**
	 * Sets the version number of the MOV files to be created.
	 * 
	 * @param version an integer where 0 is MDAT atom first and 1 is MOOV atom first.
	 */
	/*package*/ void setMovFileVersion(int version) {
		if (mConfiguration.get() == null) {
			throw new IllegalStateException("Methods setInitialConfiguration" +
			" and update must be called first.");
		}
		mConfiguration.get().put(MOV_FILE_VERSION_PARAM, version);
	}

	/**
	 * Converts a String to a SkewMethod value.
	 */
	private IMediaStream.SkewMethod toSkewMethod(String method) {
		if (IMediaStream.SkewMethod.LOCAL.toString().equals(method)) {
			return IMediaStream.SkewMethod.LOCAL;
		}
		else if (IMediaStream.SkewMethod.LOCAL_AND_RTCP.toString().equals(method)) {
			return IMediaStream.SkewMethod.LOCAL_AND_RTCP;
		}
		else if (IMediaStream.SkewMethod.RTCP.toString().equals(method)) {
			return IMediaStream.SkewMethod.RTCP;
		}
		return DEFAULT_SKEW_METHOD; // Should never reach this line.
	}

}
