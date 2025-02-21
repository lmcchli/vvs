/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.releasecausemapping.ReleaseCauseMapping;

import java.util.HashSet;
import java.util.ArrayList;

/**
 * A container of constants used retrieving Call Manager configuration.
 * @author Malin Flodin
 */
public interface ConfigConstants {
    public static final String CONFIGURATION_GROUP_NAME = "callManager.conf";
    public static final String LICENSING_CONFIGURATION_GROUP_NAME = "licensing.conf";

    /** Names of the elements in configuration file. */
    public static final String CALL_NOT_ACCEPTED_TIMER              = "callNotAcceptedTimer";
    public static final String REGISTER_BACKOFF_TIMER               = "registerBackoffTimer";
    public static final String REGISTER_BEFORE_EXPIRATION_TIME      = "registerBeforeExpirationTime";
    public static final String BLACK_LIST_TIMER                     = "blackListTimer";
    public static final String INBOUND_AUDIO_MEDIA                  = "inboundAudioMedia";
    public static final String AUDIO_CLOCK_RATE                     = "audioClockRate";
    public static final String INBOUND_VIDEO_MEDIA                  = "inboundVideoMedia";
    public static final String VIDEO_CLOCK_RATE                     = "videoClockRate";
    public static final String PTIME                                = "ptime";
    public static final String REGISTERED_NAME                      = "registeredName";
    public static final String CALL_TYPE                            = "defaultCall";
    public static final String OUTBOUND_CALL_CONNECT_TIMEOUT        = "outboundCallConnectTimeout";
    public static final String OUTBOUND_CALL_CALLING_PARTY          = "outboundCallCallingParty";
    public static final String CONTACT_URI_OVERRIDE                 = "contactUriOverride";
    public static final String VIA_OVERRIDE                         = "viaOverride";
    public static final String DISCONNECT_ON_SIP_TIMEOUT            = "disconnectOnSipTimeout";
    public static final String RELIABLE_RESPONSE_USAGE              = "sendProvisionalResponsesReliable";
    public static final String SUPPORT_TEST_INPUT                   = "supportTestInput";
    public static final String OUTBOUND_CALL_SERVER_PORT            = "outboundCallServerPort";
    public static final String USER_AGENT_WITH_PHONE_IN_URI_BUT_NO_USER_PARAMETER_LIST = "UserAgentWithPhoneInSipUriButNoUserParameter.List";
    public static final String DISABLE_P_CHARGING_VECTOR_GENERATION    = "disablePChargingVectorGeneration";
    public static final String TERM_IOI                             = "termIOI";
    public static final String ALLOW_SPECIAL_CHARS_IN_P_CHARGING_VECTOR = "allowSpecialCharsInPChargingVector";
    public static final String P_CHARGING_VECTOR_IN_REGULAR_SESSION_PROGRESS_RETRANSMISSION = "pChargingVectorInRegularSessionProgressRetransmission";
    public static final String SIP_STACK_LEAKED_TRANSACTION_AUDITOR_TIMER = "sipStackLeakedTransactionAuditorTimer";
    public static final String P_EARLY_MEDIA_HEADER_IN_SIP_RESPONSE = "pEarlyMediaHeaderInSipResponse";
    public static final String SESSION_PROGRESS_RETRANSMISSION_TIMER= "sessionProgressRetransmissionTimer";
    
    public static final String SIP_TIMERS                           = "sipTimers";
    public static final String SIP_TIMER_T2                         = "sipTimerT2";
    public static final String SIP_TIMER_T4                         = "sipTimerT4";
    public static final String SIP_TIMER_B                          = "sipTimerB";
    public static final String SIP_TIMER_C                          = "sipTimerC";
    public static final String SIP_TIMER_D                          = "sipTimerD";
    public static final String SIP_TIMER_F                          = "sipTimerF";
    public static final String SIP_TIMER_H                          = "sipTimerH";
    public static final String SIP_TIMER_J                          = "sipTimerJ";

    public static final String ENCODING                             = "encoding";
    public static final String MEDIA_TYPE                           = "mediaType";

    public static final String REMOTE_PARTY                         = "remoteParty";
    public static final String REMOTE_PARTY_SIP_PROXY_HOST          = "remotePartySipProxyHost";
    public static final String REMOTE_PARTY_SIP_PROXY_PORT          = "remotePartySipProxyPort";

    public static final String RESTRICTED_OUTBOUND_HEADERS_LIST                 = "RestrictedOutboundHeadersList";
    public static final String RESTRICTED_OUTBOUND_HEADERS_REMOTE_PARTY_ID      = "restrictedOutboundHeaderRemotePartyId";   
    public static final String RESTRICTED_OUTBOUND_HEADERS_P_ASSERTED_IDENTITY  = "restrictedOutboundHeaderPAssertedIdentity";   
    
    public static final String REQUIRED_OUTBOUND_AUDIO_MEDIA_LIST   = "RequiredOutboundAudioMedia.List";
    public static final String REQUIRED_OUTBOUND_VIDEO_MEDIA_LIST   = "RequiredOutboundVideoMedia.List";

    public static final String LOAD_REGULATION_INITIAL_RAMP_HWM     = "loadRegulationInitialRampHWM";
    public static final String LOAD_REGULATION_CHANNELS_TO_INCREASE = "loadRegulationChannelsToIncrease";
    public static final String LOAD_REGULATION_NUMBER_OF_INCREMENTS = "loadRegulationNumberOfIncrements";
    public static final String RAMP_FACTOR                          = "rampFactor";

    public static final String SUPPORT_FOR_REDIRECTING_RTP_USER_AGENTS = "supportForRedirectingRtpUserAgents";   
    public static final String SUPPORT_FOR_REDIRECTING_RTP_TIMEOUT  = "supportForRedirectingRtpTimeout";

    public static final String APPLICATION_PROXY_MODE               = "applicationProxyMode";

    public static final String PRECONDITION_ENABLED                 = "preconditionEnabled";
    public static final String UNICAST_ENABLED                      = "unicastEnabled";
    public static final String SESSION_ESTABLISHMENT_TIMER          = "sessionEstablishmentTimer";
    
    public static final String ADD_HISTORY_INFO_TO_NEW_CALL_INVITE  = "addHistoryInfoToNewCallInvite";
    public static final String NEW_CALL_INVITE_MAX_FORWARDS         = "newCallInviteMaxForwards";
    

    public static final String SUBSCRIBE_MAX_EXPIRES = "subscribeMaxExpires";

    public static final String LICENSING_OPCO_MULTICAST_ADDRESS     = "opcoMulticastAddress";
    public static final String LICENSING_OPCO_MULTICAST_PORT        = "opcoMulticastPort";
    public static final String LICENSING_OPCO_MAX_VOICE             = "opcoMaxVoice";
    public static final String LICENSING_OPCO_MAX_VIDEO             = "opcoMaxVideo";

    
    /** values used in parameters  */

    public static final String USER_AGENT_EMPTY                     = "empty";
    public static final String USER_AGENT_ALL                       = "all";

    /** Default response code mappings. */
    public static final ReleaseCauseMapping DEFAULT_RELEASE_CAUSE_MAPPING =
            ReleaseCauseMapping.getDefaultReleaseCauseMappings();
    /** Default registered name. */
    public static final String DEFAULT_REGISTERED_NAME = "mas";


}
