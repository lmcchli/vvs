/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.releasecausemapping.ReleaseCauseMapping;
import com.mobeon.masp.callmanager.releasecausemapping.ReleaseCauseConstants;

import gov.nist.core.Host;
import gov.nist.core.HostPort;

import jakarta.activation.MimeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A container of configuration variables for Call Manager.
 * For each re-load of the configuration one new instance of this class is
 * created.
 * <p>
 * The configuration variables are kept in a Map.
 *
 * @author Malin Nyfeldt
 */
public class CallManagerConfiguration {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    /** Configuration data */
    private ConcurrentHashMap<String, Object> mConfiguration =
            new ConcurrentHashMap<String, Object>();

    public void addData(String name, Object value) {
        mConfiguration.put(name, value);
    }


    /**
     * @return The Call Not Accepted timer in milli seconds
     */
    public int getCallNotAcceptedTimer() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.CALL_NOT_ACCEPTED_TIMER);
        return i;
    }

    public int getSubscribeMaxExpires() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.SUBSCRIBE_MAX_EXPIRES);
        return i;
    }
    
    /**
     * @return The register backoff timer in milli seconds
     */
    public int getRegisterBackoffTimer() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.REGISTER_BACKOFF_TIMER);
        return i;
    }

    /**
     * @return The register befire expiration time in milli seconds
     */
    public int getRegisterBeforeExpirationTime() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.REGISTER_BEFORE_EXPIRATION_TIME);
        return i;
    }

    /**
     * No longer used as bad fix, forces only one clock rate for all codecs
     * even if configured otherwise
     * @returns audio clock rate, overriding the clock rate in the stream.conf
     * @deprecated
     */
    public int  getAudioClockRate() {
        Integer i = (Integer)mConfiguration.get(ConfigConstants.AUDIO_CLOCK_RATE);
        return i;
    }
    /**
     * No longer used as bad fix, forces only one clock rate for all codecs
     * even if configured otherwise
     * @returns Video clock rate, overriding the clock rate in the stream
     * @deprecated
     */
    public int  getVideoClockRate() {
        Integer i = (Integer)mConfiguration.get(ConfigConstants.VIDEO_CLOCK_RATE);
        return i;
    }
   
    /**
     * @return The black list timer in milli seconds
     */
    public int getBlackListTimer() {
        Integer i = (Integer)mConfiguration.get(ConfigConstants.BLACK_LIST_TIMER);
        return i;
    }

    /**
     * @return The ptime in milliseconds
     */
    public int getPTime() {
        Integer i = (Integer)mConfiguration.get(ConfigConstants.PTIME);
        return i;
    }

    /**
     * @return Name to use in registrations and similar.
     */
    public String getRegisteredName() {
        String s = (String)mConfiguration.get(ConfigConstants.REGISTERED_NAME);
        return (s != null) ? s : ConfigConstants.DEFAULT_REGISTERED_NAME;
    }

    /**
     * @return The mime type required for inbound audio streams.
     */
    public MimeType getInboundAudioMimeType() {
        return (MimeType)mConfiguration.get(ConfigConstants.INBOUND_AUDIO_MEDIA);
    }

    /**
     * @return The mime type required for inbound video streams.
     */
    public MimeType getInboundVideoMimeType() {
        return (MimeType)mConfiguration.get(ConfigConstants.INBOUND_VIDEO_MEDIA);
    }

    /**
     * @return a collection of the mime types required for outbound audio streams.
     */
    public Collection<MimeType> getOutboundAudioMimeTypes() {
        return (Collection<MimeType>)mConfiguration.get(
                ConfigConstants.REQUIRED_OUTBOUND_AUDIO_MEDIA_LIST);
    }

    /**
     * @return a collection of the mime types required for outbound video streams.
     */
    public Collection<MimeType> getOutboundVideoMimeTypes() {
        return (Collection<MimeType>)mConfiguration.get(
                ConfigConstants.REQUIRED_OUTBOUND_VIDEO_MEDIA_LIST);
    }

    /**
     * @return {@link ReleaseCauseMapping} containing the configured release
     * cause mapping.
     */
    public ReleaseCauseMapping getReleaseCauseMapping() {
        ReleaseCauseMapping mapping = (ReleaseCauseMapping)mConfiguration.get(
                ReleaseCauseConstants.CONF_RELEASE_CAUSE_MAPPINGS_TABLE);
        return (mapping != null) ? mapping :
                ConfigConstants.DEFAULT_RELEASE_CAUSE_MAPPING;
    }

    /**
     * @return {@link RemoteParty} containing the configured remote party
     * containing one sipproxy or a list of SSPs.
     */
    public RemoteParty getRemoteParty() {
        return (RemoteParty)mConfiguration.get(ConfigConstants.REMOTE_PARTY);
    }

    /**
     * @return {@link SipTimers} containing the configured sip timers,
     * or the default sip timers if no timers were configured.
     */
    public SipTimers getSipTimers() {
        SipTimers timers =
                (SipTimers)mConfiguration.get(ConfigConstants.SIP_TIMERS);
        return timers;
    }

    /**
     * @return {@link RestrictedOutboundHeaders} containing the configured
     * restricted outbound headers,
     * or the default set of restricted outbound headers if none were configured.
     */
    public RestrictedOutboundHeaders getRestrictedOutboundHeaders() {
        RestrictedOutboundHeaders headers =
                (RestrictedOutboundHeaders)mConfiguration.get(
                        ConfigConstants.RESTRICTED_OUTBOUND_HEADERS_LIST);
        return (headers != null) ? headers :
                RestrictedOutboundHeaders.getDefaultRestrictedOutboundHeaders();
    }

    /**
     * @return The default call type
     */
    public CallProperties.CallType getCallType() {
        CallProperties.CallType t = (CallProperties.CallType)mConfiguration.get(
                ConfigConstants.CALL_TYPE);
        return t;
    }

    /**
     * @return The Outbound Call Connect timer (in milliseconds) to use
     * when no timer is specified.
     */
    public int getOutboundCallConnectTimer() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.OUTBOUND_CALL_CONNECT_TIMEOUT);
        return i;
    }

    /**
     * @return The Outbound Call Calling party to use then no calling party
     * is specified.
     */
    public String getOutboundCallCallingParty() {
        return (String)mConfiguration.get(
                ConfigConstants.OUTBOUND_CALL_CALLING_PARTY);
    }

    /**
     * @return <code>true</code> if the call should be disconnected if a
     * SIP INFO request times out. <code>false</code> otherwise.
     */
    public boolean getDisconnectOnSipTimeout() {
        Boolean b = (Boolean)mConfiguration.get(
                ConfigConstants.DISCONNECT_ON_SIP_TIMEOUT);
        return b;
    }

    /**
     * @return <code>true</code> if it shall be possible to support test input
     * over SIP in a non standardized way, <code>false</code> otherwise.
     */
    public boolean getSupportTestInput() {
        Boolean b = (Boolean)mConfiguration.get(
                ConfigConstants.SUPPORT_TEST_INPUT);
        return b;
    }

    /**
     * @return the outbound call server port
     */
    public int getOutboundCallServerPort() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.OUTBOUND_CALL_SERVER_PORT);
        return i;
    }

    public int getInitialRampHWM() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.LOAD_REGULATION_INITIAL_RAMP_HWM);
        return i;
    }

    public double getRampFactor() {
        Double d = (Double)mConfiguration.get(
                ConfigConstants.RAMP_FACTOR);
        return (d != null) ? d : 1.0;
    }

    /**
     * Returns the configured value for usage of reliable provisional responses.
     * @return Returns {@link ReliableResponseUsage.YES} if all provisional
     * responses shall be sent reliably, {@link ReliableResponseUsage.NO} if
     * no provisional responses shall be sent reliably, or
     * {@link ReliableResponseUsage.SDPONLY} if only provisional responses
     * carrying an SDP shall be sent reliably.
     * <p>
     * If no value is configured, the default value is returned.
     */
    public ReliableResponseUsage getReliableResponseUsage() {
        ReliableResponseUsage r = (ReliableResponseUsage)mConfiguration.get(
                ConfigConstants.RELIABLE_RESPONSE_USAGE);
        return r;
    }

    /**
     * Return a list of useragents that will get special handling when the
     * uri is lacking user=phone
     * @return a list of useragents
     */
    public ArrayList<String> getUserAgentWithPhoneInUriButNoUserParameter() {
        return (ArrayList<String>) mConfiguration.get(
                ConfigConstants.USER_AGENT_WITH_PHONE_IN_URI_BUT_NO_USER_PARAMETER_LIST);
    }
    
    
    /**
     * @return <code>true</code> if a P-Charging-Vector should be generated when a
     * SIP INVITE request does not contain one. <code>false</code> otherwise.
     */
    public boolean getDisablePChargingVectorGeneration() {
        Boolean b = (Boolean)mConfiguration.get(
                ConfigConstants.DISABLE_P_CHARGING_VECTOR_GENERATION);
        return b;
    }
    
    /**
     * @return <code>true</code> if special characters are allowed in the parameters of
     * a P-Charging-Vector. <code>false</code> otherwise.
     */
    public boolean getAllowSpecialCharsInPChargingVector() {
        Boolean b = (Boolean)mConfiguration.get(
                ConfigConstants.ALLOW_SPECIAL_CHARS_IN_P_CHARGING_VECTOR);
        return b;
    }
    
    /**
     * @return The term-ioi for the generated P-Charging-Vector
     */
    public String getTermIOI() {
        return (String)mConfiguration.get(
                ConfigConstants.TERM_IOI);
    }
    
    /**
     * @return <code>true</code> if a P-Charging-Vector should be included in regular session progress retransmission.
     */
    public boolean getPChargingVectorInRegularSessionProgressRetransmission() {
        Boolean b = (Boolean)mConfiguration.get(
                ConfigConstants.P_CHARGING_VECTOR_IN_REGULAR_SESSION_PROGRESS_RETRANSMISSION);
        return b;
    }

    /**
     * @return session progress retransmission timer (seconds).
     */
    public int getSessionProgressRetransmissionTimer() {
        Integer i = (Integer)mConfiguration.get(ConfigConstants.SESSION_PROGRESS_RETRANSMISSION_TIMER);
        return i;
    }
    
    /**
     * 
     * @return The configured value the Sip Stack will used to determine if a SIP Transaction has leaked
     */
    public int getSipStackLeakedTransactionAuditorTimer() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.SIP_STACK_LEAKED_TRANSACTION_AUDITOR_TIMER);
        return i;
    }

    /**
     * @return The Contact used to override the default Uri which is based on IP Address
     * is specified.
     */
    public String getContactUriOverride() {
        return (String)mConfiguration.get(
                ConfigConstants.CONTACT_URI_OVERRIDE);
    } 

    /**
     * @return The VIA used to override the default VIA provided by SipStack.
     */
    public String getViaOverride() {
        return (String)mConfiguration.get(ConfigConstants.VIA_OVERRIDE);
    }

    public HostPort getViaOverrideHostPort()
    {
        String viaOverride = ConfigurationReader.getInstance().getConfig().getViaOverride();
        if (viaOverride != null && !viaOverride.isEmpty()) {
            HostPort viaOverrideHostPort= new HostPort();
            if(viaOverride.contains(":")){
                String viaHost = viaOverride.split(":")[0];
                String viaPortStr = viaOverride.split(":")[1];
                viaOverrideHostPort.setHost(new Host(viaHost));
                try{
                    int viaPort = Integer.parseInt(viaPortStr);
                    viaOverrideHostPort.setPort(viaPort);
                }
                catch(NumberFormatException e)
                {
                    log.error("Unable to parse via port from ViaOverRide config parameter: "+viaPortStr +" using local address instead");
                    return null;
                }
            } else {
                viaOverrideHostPort.setHost(new Host(viaOverride));
                viaOverrideHostPort.setPort(CMUtils.getInstance().getLocalPort());
            }
            log.debug("getViaOverrideHostPort via overide found "+viaOverrideHostPort);

            return viaOverrideHostPort;
        }
        else
        {
            return null;
        }
    }    
    /**
     * Return a list of useragents for Support For Redirecting RTP.
     */
    public HashSet<String> getSupportForRedirectingRtpUserAgents() {
        HashSet<String> s = (HashSet<String>)mConfiguration.get(ConfigConstants.SUPPORT_FOR_REDIRECTING_RTP_USER_AGENTS);
        return s;
    }
    
    /**
     * Return Support For Redirecting RTP timeout value
     */
    public int getSupportForRedirectingRtpTimeout() {
        Integer i = (Integer)mConfiguration.get(ConfigConstants.SUPPORT_FOR_REDIRECTING_RTP_TIMEOUT);
        return i;
    }

    /**
     * Return if the Support For Redirecting RTP parameters are configured.  
     * @return boolean
     */
    public boolean isSupportForRedirectingRTPConfigured() {
        
        boolean isSupported = false;
        
        if (this.getSupportForRedirectingRtpUserAgents() != null &&
            this.getSupportForRedirectingRtpTimeout() != 0)
        {
            isSupported = true;
        }
        
        return isSupported;
    }

    /**
     * @return <code>true</code> if the Application (MAS) is in PROXY mode. <code>false</code> otherwise.
     */
    public boolean getApplicationProxyMode() {
        Boolean b = (Boolean)mConfiguration.get(
                ConfigConstants.APPLICATION_PROXY_MODE);
        return b;
    }

    /**
     * @return <code>true</code> if session establishment (includes preconditions and unicast) enabled 
     */
    public boolean isSessionEstablishmentEnabled() {
        return isPreconditionEnabled() || isUnicastEnabled();
    }

    /**
     * @return <code>true</code> if the Application (MAS) MUST validate precondition attributes. <code>false</code> otherwise.
     */
    public boolean isPreconditionEnabled() {
        return (Boolean)mConfiguration.get(ConfigConstants.PRECONDITION_ENABLED);
    }

    /**
     * @return <code>true</code> if the Application (MAS) MUST validate unicast attribute. <code>false</code> otherwise.
     */
    public boolean isUnicastEnabled() {
        return (Boolean)mConfiguration.get(ConfigConstants.UNICAST_ENABLED);
    }

    /**
     * @return session establishment timer (seconds).
     */
    public int getSessionEstablishmentTimer() {
        Integer i = (Integer)mConfiguration.get(ConfigConstants.SESSION_ESTABLISHMENT_TIMER);
        return i;
    }
    
    /**
     * @return <code>true</code> if must add History-Info to new call SIP INVITE.
     */
    public boolean addHistoryInfoToNewCallInvite() {
    	return (Boolean)mConfiguration.get(ConfigConstants.ADD_HISTORY_INFO_TO_NEW_CALL_INVITE);
    }
    
    /**
     * @return MaxForwards value to be set in new call SIP INVITE.
     */
    public int getNewCallInviteMaxForwards() {
        Integer i = (Integer)mConfiguration.get(ConfigConstants.NEW_CALL_INVITE_MAX_FORWARDS);
        return i;
    }
    
    
    /**
     * @return P-Early-Media header must be present in SIP 18x provisional response using {@link EarlyMediaHeaderUsage}
     */
    public EarlyMediaHeaderUsage getPEarlyMediaHeaderInSipResponse() {
        return (EarlyMediaHeaderUsage)mConfiguration.get(ConfigConstants.P_EARLY_MEDIA_HEADER_IN_SIP_RESPONSE);
    }

    /**
     * @return The opco-specific Multicast Address to use for syncing Licenses using Size Throttlers.
     */
    public String getLicensingOpcoMulticastAddress() {
        return (String)mConfiguration.get(
                ConfigConstants.LICENSING_OPCO_MULTICAST_ADDRESS);
    }    

    /**
     * @return The opco-specific Multicast Port to use for syncing Licenses using Size Throttlers.
     */
    public int getLicensingOpcoMulticastPort() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.LICENSING_OPCO_MULTICAST_PORT);
        return i;
    }

    /**
     * @return The opco-specific maximum number of voice licenses to be used.
     */
    public int getOpcoMaxVoiceLicence() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.LICENSING_OPCO_MAX_VOICE);
        return i;
    }
    
    /**
     * @return The opco-specific maximum number of video licenses to be used.
     */
    public int getOpcoMaxVideoLicence() {
        Integer i = (Integer)mConfiguration.get(
                ConfigConstants.LICENSING_OPCO_MAX_VIDEO);
        return i;
    }

    /**
     * Sets the Call Not Accepted timer in milli seconds and overrides the
     * value of the configuration.
     * This method shall be used by basic tests only.
     * @param timer Value in milli seconds.
     */
    public void setCallNotAcceptedTimer(int timer) {
        mConfiguration.put(ConfigConstants.CALL_NOT_ACCEPTED_TIMER, timer);
    }

    /**
     * Sets whether or not to disconnect on SIP timeout for a SIP INFO request.
     * This method shall be used by basic tests only.
     * @param disconnectOnTimeout   Set to true if the a call shall be
     *                              disconnected on a SIP timeout for a SIP INFO
     *                              request. False otherwise.
     */
    public void setDisconnectOnSipTimeout(boolean disconnectOnTimeout) {
        mConfiguration.put(ConfigConstants.DISCONNECT_ON_SIP_TIMEOUT,
                disconnectOnTimeout);
    }

    /**
     * Sets whether or not to support test input over SIP in a non-standardized
     * manner.
     * This method shall be used by basic tests only.
     * @param supportTestInput     Set to true if test input shall be supported.
     */
    public void setSupportTestInput(boolean supportTestInput) {
        mConfiguration.put(ConfigConstants.SUPPORT_TEST_INPUT, supportTestInput);
    }

    /**
     * Sets the backoff timer in milli seconds and overrides the value of the
     * configuration.
     * This method shall be used by basic tests only.
     * @param backoffTimer Value in milli seconds.
     */
    public void setRegisterBackoffTimer(int backoffTimer) {
        mConfiguration.put(ConfigConstants.REGISTER_BACKOFF_TIMER, backoffTimer);
    }

    /**
     * Sets the remote party and overrides the value of the configuration.
     * This method shall be used by basic tests only.
     * @param remoteParty
     */
    public void setRemoteParty(RemoteParty remoteParty) {
        mConfiguration.put(ConfigConstants.REMOTE_PARTY, remoteParty);
    }

    /**
     * Clears the remote party configuration.
     * This method shall be used by basic tests only.
     */
    public void removeRemoteParty() {
        mConfiguration.remove(ConfigConstants.REMOTE_PARTY);
    }

    /**
     * Sets the initial HighWaterMark used during startup period.
     * Use only in basic test.
     * @param highWaterMark
     */
    public void setInitialRampHWM(int highWaterMark) {
        mConfiguration.put(ConfigConstants.LOAD_REGULATION_INITIAL_RAMP_HWM, highWaterMark);
    }

    /**
     * Sets the ramp factor. This is the number of channels that the
     * HighWaterMark should be increased each time the number of calls
     * is reached below LowWaterMark.
     * Use only in basic test.
     * @param rampFactor
     */
    public void setRampFactor(double rampFactor) {
        mConfiguration.put(ConfigConstants.RAMP_FACTOR, rampFactor);
    }

    /**
     * Sets the release cause mapping.
     * Use only in basic test.
     * @param mapping
     */
    public void setReleaseCauseMapping(ReleaseCauseMapping mapping) {
        mConfiguration.put(
                ReleaseCauseConstants.CONF_RELEASE_CAUSE_MAPPINGS_TABLE, mapping);
    }

    /**
     * Sets the outbound call calling party number.
     * Use only in basic test.
     * @param callingParty
     */
    public void setOutboundCallCallingParty(String callingParty) {
        mConfiguration.put(ConfigConstants.OUTBOUND_CALL_CALLING_PARTY, callingParty);
    }

    /**
     * Sets the usage of reliable responses.
     * Use only in basic test.
     * @param usage
     */
    public void setReliableResponseUsage(ReliableResponseUsage usage) {
        mConfiguration.put(ConfigConstants.RELIABLE_RESPONSE_USAGE, usage);
    }

    public void setUserAgentWithPhoneInUriButNoUserParameter(ArrayList<String> userAgents) {
        mConfiguration.put(ConfigConstants.USER_AGENT_WITH_PHONE_IN_URI_BUT_NO_USER_PARAMETER_LIST, userAgents);
    }

    /**
     * Sets the outbound call server port.
     * Use only in basic test.
     * @param port
     */
    public void setOutboundCallServerPort(int port) {
        mConfiguration.put(ConfigConstants.OUTBOUND_CALL_SERVER_PORT, port);
    }

    /**
     * Sets the applicationProxyMode.
     * Use only in basic test.
     * @param applicationProxyMode
     */
    public void setApplicationProxyMode(Boolean applicationProxyMode) {
        mConfiguration.put(ConfigConstants.APPLICATION_PROXY_MODE, applicationProxyMode);
    }
    
    /**
     * Sets whether or not to generate a P-Charging-Vector when not present on sip INVITE.
     * This method shall be used by basic tests only.
     * @param disablePChargingVectorGeneration Set to true if a P-Charging-Vector shall be
     *                                  generated when it isn't present on the SIP 
     *                                  INVITE request. False otherwise.
     */
    public void setDisablePChargingVectorGeneration(boolean disablePChargingVectorGeneration) {
        mConfiguration.put(ConfigConstants.DISABLE_P_CHARGING_VECTOR_GENERATION,
                disablePChargingVectorGeneration);
    }
    
    /**
     * Sets whether or not to allow special characters in the parameters of a P-Charging-Vector.
     * This method shall be used by basic tests only.
     * @param allowSpecialCharsInPChargingVector Set to true if special characters are 
     * allowed in the parameters of a P-Charging-Vector. False otherwise.
     */
    public void setAllowSpecialCharsInPChargingVector(boolean allowSpecialCharsInPChargingVector) {
        mConfiguration.put(ConfigConstants.ALLOW_SPECIAL_CHARS_IN_P_CHARGING_VECTOR,
                allowSpecialCharsInPChargingVector);
    }
    
    /**
     * Sets the term-ioi for the generated P-Charging-Vector.
     * Use only in basic test.
     * @param termIOI The terminating IOI param value
     */
    public void setTermIOI(String termIOI) {
        mConfiguration.put(ConfigConstants.TERM_IOI, termIOI);
    }
    
    /**
     * Sets whether or not to a P-Charging-Vector should be included in regular session progress retransmission.
     * This method shall be used by basic tests only.
     * @param pChargingVectorInRegularSessionProgressRetransmission Set to true if a P-Charging-Vector shall be
     *                                  included in regular session progress retransmission.
     */
    public void setPChargingVectorInRegularSessionProgressRetransmission(boolean pChargingVectorInRegularSessionProgressRetransmission) {
        mConfiguration.put(ConfigConstants.P_CHARGING_VECTOR_IN_REGULAR_SESSION_PROGRESS_RETRANSMISSION, pChargingVectorInRegularSessionProgressRetransmission);
    }

    
}
