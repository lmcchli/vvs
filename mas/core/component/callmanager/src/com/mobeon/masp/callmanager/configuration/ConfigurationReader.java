/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.ParameterTypeException;
import com.mobeon.common.configuration.UnknownParameterException;
import com.mobeon.masp.execution_engine.ServiceEnablerException;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.releasecausemapping.ReleaseCauseConstants;
import com.mobeon.masp.callmanager.releasecausemapping.ReleaseCauseMapping;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import jakarta.activation.MimeTypeParseException;
import jakarta.activation.MimeType;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Configuration reader for Call Manager. This class is a singleton.
 * <p>
 * The read configuration variables are kept in a {@link CallManagerConfiguration}
 * instead of being read directly from a configuration group.
 * There are three reasons for this implementation:
 * <ul>
 * <li>When reading configuration values, there is no need to handle exceptions
 *     issued from the configuration component.</li>
 * <li>To be able to hold two separate views of the configuration when the
 *     configuration changed. Active calls keep the old while new calls get a
 *     new.</li>
 * <li>When writing testcases, it is useful to be able to modify some
 *     configuration values at runtime. This is not possible if values
 *     are always read from configuration component classes</li>
 * </ul>
 *
 * @author Malin Flodin
 */
public class ConfigurationReader {

    private static final ILogger log =
            ILoggerFactory.getILogger(ConfigurationReader.class);

    private static final ConfigurationReader INSTANCE = new ConfigurationReader();
    
    private static final String HOST_INSTANCE = System.getenv("HOST_INSTANCE");
    private static final String HOST_TYPE = System.getenv("HOST_TYPE");
    private static final String EXT_LB_VIP_IP = System.getenv("EXT_LB_VIP_IP");
    private static final String EXT_LB_VIP_HOSTNAME = "extlbvipip";
    private static final int SIP_BASE_PORT_MOIP = 5060;
    private static final int SIP_BASE_PORT_VMP = 5080;
    private static final int SIP_BASE_PORT = (("VMP".equals(HOST_TYPE))? SIP_BASE_PORT_VMP : SIP_BASE_PORT_MOIP);
    

    /** Current Configuration */
    private AtomicReference<CallManagerConfiguration> mCurrentConfig =
        new AtomicReference<CallManagerConfiguration>();

    /**
     * Initial {@link IConfiguration} instance. Is used to read the
     * configuration at startup and when the
     * {@link com.mobeon.masp.configuration.ConfigurationChanged} is received.
     */
    private IConfiguration initialConfiguration;


    /** Creates the single ConfigurationReader instance. */
    ConfigurationReader() {
    }

    /**
     * @return The single ConfigurationReader instance.
     */
    public static ConfigurationReader getInstance() {
        return INSTANCE;
    }

    public CallManagerConfiguration getConfig() {
        if (mCurrentConfig.get() == null) {
            throw new IllegalStateException(
                    "Methods setInitialConfiguration and update must be " +
                    "called first.");
        }
        return mCurrentConfig.get();
    }

    /**
     * Sets the initial {@link IConfiguration} instance.
     * This method should only be called once when the Call Manager component
     * is initiated.
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
        initialConfiguration = config;
    }

    /**
     * Reads configuration parameters.
     *
     * @throws ConfigurationException if configuration could not be read.
     * @throws ServiceEnablerException if there is an error in the configuration.
     * @throws MimeTypeParseException if there is an error with interpreting
     * mime types in the configuration.
     */
    public void update()
            throws ConfigurationException, ServiceEnablerException,
            MimeTypeParseException {

        IConfiguration configuration = initialConfiguration.getConfiguration();
        CallManagerConfiguration newConfig = new CallManagerConfiguration();

        IGroup configGroup =
                configuration.getGroup(ConfigConstants.CONFIGURATION_GROUP_NAME);

        IGroup licensingConfigGroup =
            configuration.getGroup(ConfigConstants.LICENSING_CONFIGURATION_GROUP_NAME);

        // Retrieve outbound call connect timer
        newConfig.addData(
                ConfigConstants.OUTBOUND_CALL_CONNECT_TIMEOUT,
                configGroup.getInteger(ConfigConstants.OUTBOUND_CALL_CONNECT_TIMEOUT));

        // Retrieve call not accepted timer
        newConfig.addData(
                ConfigConstants.CALL_NOT_ACCEPTED_TIMER,
                configGroup.getInteger(
                        ConfigConstants.CALL_NOT_ACCEPTED_TIMER));

        newConfig.addData(
                ConfigConstants.SUBSCRIBE_MAX_EXPIRES,
                configGroup.getInteger(
                        ConfigConstants.SUBSCRIBE_MAX_EXPIRES)); 

        // Retrieve register backoff timer
        newConfig.addData(
                ConfigConstants.REGISTER_BACKOFF_TIMER,
                configGroup.getInteger(
                        ConfigConstants.REGISTER_BACKOFF_TIMER));

        // Retrieve register video clock rate
        newConfig.addData(
                ConfigConstants.VIDEO_CLOCK_RATE,
                configGroup.getInteger(
                        ConfigConstants.VIDEO_CLOCK_RATE));        

        // Retrieve register audio clock rate
        newConfig.addData(
                ConfigConstants.AUDIO_CLOCK_RATE,
                configGroup.getInteger(
                        ConfigConstants.AUDIO_CLOCK_RATE));        

        
        // Retrieve register before expiration time
        newConfig.addData(
                ConfigConstants.REGISTER_BEFORE_EXPIRATION_TIME,
                configGroup.getInteger(
                        ConfigConstants.REGISTER_BEFORE_EXPIRATION_TIME));

        // Retrieve black list timer
        newConfig.addData(
                ConfigConstants.BLACK_LIST_TIMER,
                configGroup.getInteger(
                        ConfigConstants.BLACK_LIST_TIMER));


        // Retrieve inbound audio media
        newConfig.addData(
                ConfigConstants.INBOUND_AUDIO_MEDIA,
                new MimeType(
                        "audio",
                        configGroup.getString(ConfigConstants.INBOUND_AUDIO_MEDIA)));

        // Retrieve inbound video media
        newConfig.addData(
                ConfigConstants.INBOUND_VIDEO_MEDIA,
                new MimeType(
                        "video",
                        configGroup.getString(ConfigConstants.INBOUND_VIDEO_MEDIA)));

        // Retrive outbound audio media
        newConfig.addData(
                ConfigConstants.REQUIRED_OUTBOUND_AUDIO_MEDIA_LIST,
                parseMediaTypes("audio", configGroup));

        // Retrive outbound video media
        newConfig.addData(ConfigConstants.REQUIRED_OUTBOUND_VIDEO_MEDIA_LIST,
                parseMediaTypes("video", configGroup));

        // Retrieve ptime
        newConfig.addData(
                ConfigConstants.PTIME,
                configGroup.getInteger(ConfigConstants.PTIME));

        // Retrieve registered name
        newConfig.addData(
                ConfigConstants.REGISTERED_NAME,
                configGroup.getString(
                        ConfigConstants.REGISTERED_NAME));

        if (newConfig.getRegisteredName().equals("")) {
            log.warn("Registered name was set to the empty string in the " +
                    "configuration. The default \"" +
                    ConfigConstants.DEFAULT_REGISTERED_NAME +
                    "\" is used instead.");

            newConfig.addData(
                    ConfigConstants.REGISTERED_NAME,
                    ConfigConstants.DEFAULT_REGISTERED_NAME);
        }

        // Retrieve outbound call calling party
        newConfig.addData(
                ConfigConstants.OUTBOUND_CALL_CALLING_PARTY,
                configGroup.getString(ConfigConstants.OUTBOUND_CALL_CALLING_PARTY));

        // Retrieve release cause mappings
        try {
            newConfig.addData(
                    ReleaseCauseConstants.CONF_RELEASE_CAUSE_MAPPINGS_TABLE,
                    ReleaseCauseMapping.parseReleaseCauseMappings(configGroup));
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not retreive the response code mapping " +
                        "from configuration, using default values instead. " +
                        "Exception: " + e);
        }

        // Retrieve remote party
        newConfig.addData(
                ConfigConstants.REMOTE_PARTY,
                 RemoteParty.parseRemoteParty(
                        configGroup.getString(ConfigConstants.REMOTE_PARTY_SIP_PROXY_HOST),
                        configGroup.getInteger(ConfigConstants.REMOTE_PARTY_SIP_PROXY_PORT))) ;

        // Retrieve call type
        newConfig.addData(
                ConfigConstants.CALL_TYPE,
                getCallType(
                        configGroup.getString(
                                ConfigConstants.CALL_TYPE)));   

        // Retrieve SIP timers
        try {
            newConfig.addData(ConfigConstants.SIP_TIMERS,
                    SipTimers.parseSipTimers(configGroup));
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not retreive the sip timer configuration, " +
                        "using default values instead. " +
                        "Exception: " + e);
        }

        // Retrieve restricted outbound headers
        try {
            newConfig.addData(ConfigConstants.RESTRICTED_OUTBOUND_HEADERS_LIST,
                    RestrictedOutboundHeaders.parseRestrictedOutboundHeaders(configGroup));
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not retreive the configuration of " +
                        "restricted outbound headers, using default values " +
                        "instead. Exception: " + e);
        }

        // Retrieve disconnect on sip timeout
        newConfig.addData(
                ConfigConstants.DISCONNECT_ON_SIP_TIMEOUT,
                Boolean.valueOf(
                        configGroup.getString(
                                ConfigConstants.DISCONNECT_ON_SIP_TIMEOUT)));

        // Retrieve support test input
        newConfig.addData(
                ConfigConstants.SUPPORT_TEST_INPUT,
                Boolean.valueOf(
                        configGroup.getString(
                                ConfigConstants.SUPPORT_TEST_INPUT)));

        // Retrieve the configuration for loadregulation
        try {
            getLoadRegulationConfig(newConfig, configGroup);
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not retreive the load regulation configuration, " +
                        "using default values instead. " +
                        "Exception: " + e);
        }

        // Retrieve reliable response usage
        newConfig.addData(
                ConfigConstants.RELIABLE_RESPONSE_USAGE,
                ReliableResponseUsage.parseReliableResponseUsage(
                        configGroup.getString(
                        ConfigConstants.RELIABLE_RESPONSE_USAGE, null))); 

        //Retrieve useragent information
        try {
            newConfig.addData(
                ConfigConstants.USER_AGENT_WITH_PHONE_IN_URI_BUT_NO_USER_PARAMETER_LIST,
                configGroup.getList(ConfigConstants.USER_AGENT_WITH_PHONE_IN_URI_BUT_NO_USER_PARAMETER_LIST));
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not retreive the user agent uri configuration, " +
                        "using default values instead. " +
                        "Exception: " + e);
        }

        // Retrieve generated P-Charging-Vector
        newConfig.addData(
                ConfigConstants.DISABLE_P_CHARGING_VECTOR_GENERATION,
                Boolean.valueOf(
                        configGroup.getString(
                                ConfigConstants.DISABLE_P_CHARGING_VECTOR_GENERATION)));

        // Retrieve allow special chars in P-Charging-Vector        
        System.setProperty(
                ConfigConstants.ALLOW_SPECIAL_CHARS_IN_P_CHARGING_VECTOR,
                Boolean.valueOf(configGroup.getString(ConfigConstants.ALLOW_SPECIAL_CHARS_IN_P_CHARGING_VECTOR)).toString());

        // Retrieve TermIOI
        try {
        newConfig.addData(
                ConfigConstants.TERM_IOI,
                configGroup.getString(ConfigConstants.TERM_IOI));
        } catch (Exception e){
            if (log.isDebugEnabled())
                log.debug("Could not retreive the term ioi configuration, " +
                        "using default value instead. " +
                        "Exception: " + e);            
        }
        
        newConfig.addData(
                ConfigConstants.P_CHARGING_VECTOR_IN_REGULAR_SESSION_PROGRESS_RETRANSMISSION,
                Boolean.valueOf(
                        configGroup.getString(ConfigConstants.P_CHARGING_VECTOR_IN_REGULAR_SESSION_PROGRESS_RETRANSMISSION, "true")));
        
        newConfig.addData(ConfigConstants.SESSION_PROGRESS_RETRANSMISSION_TIMER,
                configGroup.getInteger(ConfigConstants.SESSION_PROGRESS_RETRANSMISSION_TIMER));

        // Retrieve SIP Stack leaked transaction auditor timer (default to 120000)
        newConfig.addData(
                ConfigConstants.SIP_STACK_LEAKED_TRANSACTION_AUDITOR_TIMER,
                configGroup.getInteger(
                        ConfigConstants.SIP_STACK_LEAKED_TRANSACTION_AUDITOR_TIMER, 120000));

        newConfig.addData(
                ConfigConstants.OUTBOUND_CALL_SERVER_PORT,
                configGroup.getInteger(
                        ConfigConstants.OUTBOUND_CALL_SERVER_PORT));


        String extlbvipip = EXT_LB_VIP_IP;
        if (extlbvipip == null || extlbvipip.isEmpty()) {
            // We could not get the external LB VIP from the environment variable,
            // let's try to resolve it from the DNS.
            extlbvipip = resolveHostname(EXT_LB_VIP_HOSTNAME);
        }

        // Retrieve Contact Uri Override
        String contactUriOverride = null;
        try {
            contactUriOverride = configGroup.getString(ConfigConstants.CONTACT_URI_OVERRIDE);
            // If the configuration value is not set, no exception is thrown. The return string is simply empty.
        } catch (Exception e) {
            log.info("Could not retrieve " + ConfigConstants.CONTACT_URI_OVERRIDE + " parameter from configuration, " +
                    "Will build value from environment variables. " +
                    "Exception : " + e);
        }
        
        if (contactUriOverride == null || contactUriOverride.isEmpty()) {
            if (HOST_INSTANCE != null && !HOST_INSTANCE.isEmpty() && isInteger(HOST_INSTANCE) &&
                    extlbvipip != null & !extlbvipip.isEmpty()) {

                    // moip-1 : sip:mas1@10.121.133.228:5061
                    // moip-2 : sip:mas2@10.121.133.228:5062
                    // Where the IP corresponds to the External LB VIP (must be an IP, not a hostname).

                    StringBuffer sb = new StringBuffer();
                    sb.append("sip:mas");
                    sb.append(HOST_INSTANCE);
                    sb.append("@");
                    sb.append(extlbvipip);
                    sb.append(":");
                    String sipPort = String.valueOf(SIP_BASE_PORT + Integer.parseInt(HOST_INSTANCE));
                    sb.append(sipPort);
                    contactUriOverride = sb.toString();
                    log.info("ConfigurationReader:update() : " + ConfigConstants.CONTACT_URI_OVERRIDE + "=" + contactUriOverride + ". Built with Env. vars. HOST_INSTANCE, SIP_BASE_PORT and extlbvipip.");
            } else {
                log.info("ConfigurationReader:update() : " + ConfigConstants.CONTACT_URI_OVERRIDE + ". No configuration, no Env. vars.");
            }
        } else {
            log.info("ConfigurationReader:update() : " + ConfigConstants.CONTACT_URI_OVERRIDE + "=" + contactUriOverride + ". Read from configuration.");
        }
        
        newConfig.addData(ConfigConstants.CONTACT_URI_OVERRIDE, contactUriOverride);
        
        
        // Retrieve via override information
        String viaOverride = null;
        try {
            viaOverride = configGroup.getString(ConfigConstants.VIA_OVERRIDE);
            // If the configuration value is not set, no exception is thrown. The return string is simply empty.
        } catch (Exception e) {
            log.info("Could not retrieve " + ConfigConstants.VIA_OVERRIDE + " parameter from configuration, " +
                    "Will build value from environment variables. " +
                    "Exception : " + e);
        }
        
        if (viaOverride == null || viaOverride.isEmpty()) {
            if (HOST_INSTANCE != null && !HOST_INSTANCE.isEmpty() && isInteger(HOST_INSTANCE) &&
                    extlbvipip != null & !extlbvipip.isEmpty()) {

                // moip-1 : 10.121.133.228:5061
                // moip-2 : 10.121.133.228:5062
                // Where the IP corresponds to the External LB VIP (must be an IP, not a hostname).
                
                String sipPort = String.valueOf(SIP_BASE_PORT + Integer.parseInt(HOST_INSTANCE));
                StringBuffer sb = new StringBuffer();
                sb.append(extlbvipip).append(":").append(sipPort);
                viaOverride = sb.toString();
                log.info("ConfigurationReader:update() : " + ConfigConstants.VIA_OVERRIDE + "=" + viaOverride + ". Built with Env. vars. HOST_INSTANCE, SIP_BASE_PORT and extlbvipip.");
            } else {
                log.info("ConfigurationReader:update() : " + ConfigConstants.VIA_OVERRIDE + ". No configuration, no Env. vars.");
            }
        } else {
            log.info("ConfigurationReader:update() : " + ConfigConstants.VIA_OVERRIDE + "=" + viaOverride + ". Read from configuration.");
        }
        
        newConfig.addData(ConfigConstants.VIA_OVERRIDE, viaOverride);

        
        // Retrieve Support For Redirecting RTP
        try {
            parseSupportForRedirectingRtpConfig(newConfig, configGroup);
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not retreive the support for redirected rtp configuration, " +
                        "using default values instead. " +
                        "Exception: " + e);
        }

        // Retrieve Proxy data
        Boolean proxyModeEnabled = Boolean.valueOf(configGroup.getString(ConfigConstants.APPLICATION_PROXY_MODE)); 
        newConfig.addData(ConfigConstants.APPLICATION_PROXY_MODE, proxyModeEnabled);

        if ( (!proxyModeEnabled) && CMUtils.getInstance().getCallManagerLicensing().isLicensingEnabled() ) {
            // Retrieve opco-specific Multicast Address to use for syncing Licenses using Size Throttlers.
            newConfig.addData(
                    ConfigConstants.LICENSING_OPCO_MULTICAST_ADDRESS,
                    licensingConfigGroup.getString(ConfigConstants.LICENSING_OPCO_MULTICAST_ADDRESS));

            // Retrieve opco-specific Multicast Port to use for syncing Licenses using Size Throttlers.
            newConfig.addData(
                    ConfigConstants.LICENSING_OPCO_MULTICAST_PORT,
                    licensingConfigGroup.getInteger(ConfigConstants.LICENSING_OPCO_MULTICAST_PORT));

            // Retrieve opco-specific maximum number of voice licenses to be used.
            newConfig.addData(
                    ConfigConstants.LICENSING_OPCO_MAX_VOICE,
                    licensingConfigGroup.getInteger(ConfigConstants.LICENSING_OPCO_MAX_VOICE));

            // Retrieve opco-specific maximum number of video licenses to be used.
            newConfig.addData(
                    ConfigConstants.LICENSING_OPCO_MAX_VIDEO,
                    licensingConfigGroup.getInteger(ConfigConstants.LICENSING_OPCO_MAX_VIDEO));
        } else {
            newConfig.addData(
                    ConfigConstants.LICENSING_OPCO_MULTICAST_ADDRESS, "");

            newConfig.addData(
                    ConfigConstants.LICENSING_OPCO_MULTICAST_PORT, 0);

            newConfig.addData(
                    ConfigConstants.LICENSING_OPCO_MAX_VOICE, 0);

            newConfig.addData(
                    ConfigConstants.LICENSING_OPCO_MAX_VIDEO, 0);
        }

        // Retrieve session establishment values
        Boolean preconditionEnabled = Boolean.valueOf(configGroup.getString(ConfigConstants.PRECONDITION_ENABLED)); 
        newConfig.addData(ConfigConstants.PRECONDITION_ENABLED, preconditionEnabled);

        Boolean unicastEnabled = Boolean.valueOf(configGroup.getString(ConfigConstants.UNICAST_ENABLED)); 
        newConfig.addData(ConfigConstants.UNICAST_ENABLED, unicastEnabled);

        newConfig.addData(ConfigConstants.SESSION_ESTABLISHMENT_TIMER, configGroup.getInteger(ConfigConstants.SESSION_ESTABLISHMENT_TIMER));

        newConfig.addData(ConfigConstants.P_EARLY_MEDIA_HEADER_IN_SIP_RESPONSE,
                EarlyMediaHeaderUsage.parseEarlyMediaHeaderUsage(configGroup.getString(ConfigConstants.P_EARLY_MEDIA_HEADER_IN_SIP_RESPONSE, null)));
        
        Boolean insertHistoryInfo = Boolean.valueOf(configGroup.getString(ConfigConstants.ADD_HISTORY_INFO_TO_NEW_CALL_INVITE)); 
        newConfig.addData(ConfigConstants.ADD_HISTORY_INFO_TO_NEW_CALL_INVITE, insertHistoryInfo);
        
        newConfig.addData(ConfigConstants.NEW_CALL_INVITE_MAX_FORWARDS, configGroup.getInteger(ConfigConstants.NEW_CALL_INVITE_MAX_FORWARDS));
        

        mCurrentConfig.set(newConfig);
    }


    //===================== Private methods ============================

    private static boolean isInteger(String s) {
        try { 
            Integer.parseInt(s); 
        } catch(NumberFormatException e) { 
            return false; 
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private String resolveHostname(String hostname) {
        String ip = null;

        InetAddress inetIP = null;
        try {
            inetIP = InetAddress.getByName(hostname);
            ip = inetIP.getHostAddress();
        } catch (UnknownHostException e) {
        } 
        return ip;
    }
    
    /**
     * @param media Should be either "audio" or "video".
     * @param group
     * @return a collection of mime types. The sub type is found from the
     * media type encodings in the configuration group. The primary type is
     * given in the media parameter.
     * @throws MimeTypeParseException if a MimeType could not be created.
     * @throws ConfigurationException if the configuration could not be parsed.
     */
    private static Collection<MimeType> parseMediaTypes(
            String media, IGroup group)
            throws MimeTypeParseException, ConfigurationException {
        List<String> mediaTypes = null;
        if (media.equals("audio")) {
            mediaTypes = group.getList(ConfigConstants.REQUIRED_OUTBOUND_AUDIO_MEDIA_LIST);
        } else {        
            mediaTypes = group.getList(ConfigConstants.REQUIRED_OUTBOUND_VIDEO_MEDIA_LIST);
        }
        List<MimeType> mimeTypes = new ArrayList<MimeType>();
        for (String encoding : mediaTypes) {
        	String raw=media+"/"+encoding;
            mimeTypes.add(new MimeType(raw));
        }

        return mimeTypes;
    }

    private CallProperties.CallType getCallType(String callTypeStr) {
        CallProperties.CallType callType = CallProperties.CallType.VIDEO;
        if (callTypeStr.equals("voice"))
            callType = CallProperties.CallType.VOICE;

        return callType;
    }

    /**
     * Reads the config group ConfigConstants.LOAD_REGULATION
     *
     * @param newConfig The configuration to be updated
     * @param configGroup The loadregulation group
     * @throws ParameterTypeException if the values cannot be parsed as integers
     */
    private void getLoadRegulationConfig(CallManagerConfiguration newConfig, IGroup configGroup)
            throws ParameterTypeException, UnknownParameterException {

        if (configGroup != null) {
            // Retrieve Ramp Factor
            int n = configGroup.getInteger(ConfigConstants.LOAD_REGULATION_CHANNELS_TO_INCREASE);
            int m = configGroup.getInteger(ConfigConstants.LOAD_REGULATION_NUMBER_OF_INCREMENTS);

            newConfig.addData(
                    ConfigConstants.RAMP_FACTOR,
                    (double) n / m);

            // Retrieve Initial Ramp HWM
            newConfig.addData(
                    ConfigConstants.LOAD_REGULATION_INITIAL_RAMP_HWM,
                    configGroup.getInteger(ConfigConstants.LOAD_REGULATION_INITIAL_RAMP_HWM));
        }
    }


    /**
     * Reads the config group ConfigConstants.SUPPORT_FOR_REDIRECTING_RTP
     *
     * @param newConfig The configuration to be updated
     * @param configGroup The supportforredirectingrtp group
     * @throws ParameterTypeException if integer values cannot be parsed
     * @throws UnknownParameterException if string values cannot be parsed
     */
    private void parseSupportForRedirectingRtpConfig(CallManagerConfiguration newConfig, IGroup configGroup)
            throws ParameterTypeException, UnknownParameterException {

        if (configGroup != null) {
            newConfig.addData(
                    ConfigConstants.SUPPORT_FOR_REDIRECTING_RTP_USER_AGENTS,
                    parseUserAgent(
                            configGroup.getString(
                            ConfigConstants.SUPPORT_FOR_REDIRECTING_RTP_USER_AGENTS)));

            newConfig.addData(
                    ConfigConstants.SUPPORT_FOR_REDIRECTING_RTP_TIMEOUT,
                    configGroup.getInteger(
                            ConfigConstants.SUPPORT_FOR_REDIRECTING_RTP_TIMEOUT));
        }
    }


    /**
     * Reads the config group ConfigConstants.SUPPORT_FOR_REDIRECTING_RTP_USER_AGENTS
     * 
     * @param list Useragents
     * @return HashSet
     */
    private static HashSet<String> parseUserAgent(String list) {
        if( list == null ) {
            return null;
        }

        HashSet<String> result = new HashSet<String>();
        String[] values = list.split(",");

        for (String value : values) {
            if (value != null) {
                result.add(value.toLowerCase());
            }
        }
        return result;
    }

}
