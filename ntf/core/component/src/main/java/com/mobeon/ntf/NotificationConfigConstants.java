package com.mobeon.ntf;

public interface NotificationConfigConstants {

    /** Values for configuration parameters */
	public static final String NTF_HOME							= "ntfHome";
	public static final String CONFIG_FILE						= "configFile";
	public static final String USER_DIR							= "user.dir";
	public static final String LOGS_DIR							= "logs";

	//MoIP solaris config boolean values for backward compatibility.
	public static final String ON 								= "on";
	public static final String OFF 								= "off";
	public static final String YES 								= "yes";
	public static final String NO 								= "no";
	public static final String ONE 								= "1";
	public static final String TRUE                             = "true";
	public static final String FALSE                            = "true";
	public static final String ZERO 							= "0";
	

	public static final String GSM 								= "gsm";
	public static final String CDMA2000							= "cdma2000";
	public static final String PSTN 							= "pstn";

	public static final String SUBSCRIBER 						= "subscriber";
	public static final String CALLER							= "caller";

	public static final String NOTIFY_AND_WARN 					= "notifyandwarn";
	public static final String DISCARD							= "discard";
	public static final String NOTIFY							= "notify";
	public static final String WARN								= "warn";
	public static final String SMS 								= "sms";
	public static final String FLASH							= "flash";
	public static final String NONE								= "none";
	public static final String MWI								= "mwi";
	public static final String REPLACE							= "replace";
	public static final String MMS								= "mms";
	public static final String EMAIL							= "email";
	public static final String OUTDIAL							= "outdial";
	public static final String SIPMWI							= "sipmwi";

	public static final String AVAILABLE                        =  "available";
	public static final String UNAVAILABLE                      =  "unavailable";
	public static final String FORCED_UNAVAILABLE               =  "forcedUnavailable";

	//Types of MoipUserNTD (Disabling) to respect for a reminder - reminderUseNtdTypeType".
    public static final String REMINDER_NTD_FULL_ONLY           = "fullonly";
    public static final String REMINDER_NTD_ALL                 = "all";
    public static final String REMINDER_NTD_NONE                = "none";

	/** Configuration parameters */
	public static final String SHUTDOWN_TIME                    = "shutdownTime";
    public static final String COMPONENT_INSTANCE_NAME 			= "componentInstanceName";


    public static final String AUTO_FORWARDED_MESSAGES 			= "autoForwardedMessages";
    public static final String BEARING_NETWORK 					= "bearingNetwork";
    public static final String CALL_MWI_CALLER 					= "callMwiCaller";
    public static final String CHECK_QUOTA 						= "checkQuota";

    @Deprecated
    public static final String CANCEL_SMS_AT_RETRIEVAL 			= "cancelSmsAtRetrieval"; //use cancelSmsSentOnEvent
    public static final String CANCEL_SMS_ENABLED_FOR_EVENT     = "cancelSmsSentOnEvent";
    public static final String CANCEL_SMS_METHOD                = "cancelSmsMethod";
    public static final String CHECK_TERMINAL_CAPABILITY 		= "checkTerminalCapability";

    public static final String DELAYED_EVENT_RETRY_SCHEMA       = "delayedEventRetrySchema";
    public static final String DELAYED_EVENT_RETRY_EXPIRY       = "delayedEventRetryExpiry";

    public static final String DELAYED_EVENT_QUEUE_SIZE       = "delayedEventQueueSize";
    public static final String DELAYED_EVENT_WORKERS       = "delayedEventWorkers";

    public static final String DEFAULT_DATE_FORMAT 				= "defaultDateFormat";
    public static final String DEFAULT_LANGUAGE 				= "defaultLanguage";
    public static final String DEFAULT_NOTIFICATION_FILTER 		= "defaultNotificationFilter";
    public static final String DEFAULT_NOTIFICATION_FILTER_2 	= "defaultNotificationFilter2";

    public static final String DEFAULT_TERMINAL_CAPABILITY_FOR_MWI 		= "defaultTerminalCapabilityForMwi";
    public static final String DEFAULT_TERMINAL_CAPABILITY_FOR_FLASH 	= "defaultTerminalCapabilityForFlash";
    public static final String DEFAULT_TERMINAL_CAPABILITY_FOR_REPLACE	= "defaultTerminalCapabilityForReplace";

    public static final String DEFAULT_TIME_FORMAT 				= "defaultTimeFormat";

    public static final String DISABLE_SMSC_REPLACE				= "disableSmscReplace";
    public static final String DISCARD_SMS_WHEN_COUNT_IS_0		= "discardSmsWhenCountIs0";

    public static final String DO_OUTDIAL 						= "doOutdial";
    public static final String OUTDIAL_QUEUE_SIZE				= "outdialQueueSize";
    public static final String OUTDIAL_WORKERS					= "outdialWorkers";
    public static final String OUTDIAL_START_RETRY_SCHEMA       = "outdialStartRetrySchema";
    public static final String OUTDIAL_START_EXPIRE_TIME_IN_MIN = "outdialStartExpireTimeInMin";
    public static final String OUTDIAL_LOGIN_RETRY_SCHEMA       = "outdialLoginRetrySchema";
    public static final String OUTDIAL_LOGIN_EXPIRE_TIME_IN_MIN = "outdialLoginExpireTimeInMin";
    public static final String OUTDIAL_CALL_RETRY_SCHEMA        = "outdialCallRetrySchema";
    public static final String OUTDIAL_CALL_EXPIRE_TIME_IN_MIN  = "outdialCallExpireTimeInMin";
    public static final String OUTDIAL_EXPIRY_INTERVAL_IN_MIN   = "outdialExpiryIntInMin";
    public static final String OUTDIAL_EXPIRY_RETRIES           = "outdialExpiryRetries";
    public static final String OUTDIAL_REMINDER_ENABLED         = "outdialReminderEnabled";
    public static final String OUTDIAL_REMINDER_INTERVAL_IN_MIN = "outdialReminderIntervalInMin";
    public static final String OUTDIAL_REMINDER_EXPIRE_IN_MIN   = "outdialReminderExpireInMin";
    public static final String OUTDIAL_REMINDER_TYPE            = "outdialReminderType";
    public static final String OUTDIAL_REMINDER_TYPE_OUTDIAL    = "outdial";
    public static final String OUTDIAL_REMINDER_TYPE_FLSSMS     = "flssms";
    public static final String OUTDIAL_PHONE_ON_LOCK_FILE_VALIDITY_IN_SECONDS = "outdialPhoneOnLockValInSecs";
    public static final String OUTDIAL_STATUS_FILE_VALIDITY_IN_MIN = "outdialStatusValInMin";
    public static final String OUTDIAL_WHEN_IN_WAIT_STATE       = "outdialWhenInWaitState";
    public static final String OUTDIAL_WHEN_IN_WAIT_ON_STATE    = "outdialWhenInWaitOnState";

    public static final String SLAMDOWN_QUEUE_SIZE							       = "slamdownQueueSize";
    public static final String SLAMDOWN_WORKERS                                    = "slamdownWorkers";
    public static final String SLAMDOWN_MCN_SENDER_WORKERS                         = "slamdownMcnSenderWorkers";
    public static final String SLAMDOWN_MCN_SENDER_QUEUE_SIZE                      = "slamdownMcnSenderQueueSize";
    public static final String SLAMDOWN_MCN_SMS_UNIT_RETRY_SCHEMA 			       = "slamdownMcnSmsUnitRetrySchema";
    public static final String SLAMDOWN_MCN_SMS_UNIT_EXPIRE_TIME_IN_MIN		       = "slamdownMcnSmsUnitExpireTimeInMin";
    public static final String SLAMDOWN_MCN_SMS_TYPE_0_RETRY_SCHEMA			       = "slamdownMcnSmsType0RetrySchema";
    public static final String SLAMDOWN_MCN_SMS_TYPE_0_EXPIRE_TIME_IN_MIN	       = "slamdownMcnSmsType0ExpireTimeInMin";
    public static final String SLAMDOWN_MCN_SMS_INFO_RETRY_SCHEMA			       = "slamdownMcnSmsInfoRetrySchema";
    public static final String SLAMDOWN_MCN_SMS_INFO_EXPIRE_TIME_IN_MIN            = "slamdownMcnSmsInfoExpireTimeInMin";
    public static final String SLAMDOWN_MCN_EXPIRY_INTERVAL_IN_MIN                 = "slamdownMcnExpiryIntInMin";
    public static final String SLAMDOWN_MCN_EXPIRY_RETRIES                         = "slamdownMcnExpiryRetries";
    public static final String SLAMDOWN_MCN_PHONE_ON_LOCK_FILE_VALIDITY_IN_SECONDS = "slamdownMcnPhoneOnLockValInSecs";
    public static final String SLAMDOWN_MCN_STATUS_FILE_VALIDITY_IN_MIN            = "slamdownMcnStatusValInMin";

    public static final String EMAIL_RETRY_SCHEMA            			= "emailRetrySchema";
    public static final String EMAIL_FORWARD_MAXIMUM_SIZE			    = "emailForwardMaximumSize";
    public static final String EMAIL_FORWARD_TRANSCODE_AUDIO			= "emailForwardTranscodeAudio";
    public static final String EMAIL_FORWARD_TRANSCODE_VIDEO			= "emailForwardTranscodeVideo";
    public static final String EMAIL_FORWARD_OUTPUT_AUDIO_MIME_TYPE     = "emailForwardOutputAudioMimeType";
    public static final String EMAIL_FORWARD_OUTPUT_VIDEO_MIME_TYPE     = "emailForwardOutputVideoMimeType";
    public static final String EMAIL_FROM_DEFAULT                       = "emailFromDefault";

    public static final String DO_SMS_TYPE_0_SLAMDOWN			= "doSmsType0Slamdown";
    public static final String DO_SMS_TYPE_0_MCN				= "doSmsType0Mcn";
    public static final String DO_SMS_TYPE_0_OUTDIAL			= "doSmsType0Outdial";
    public static final String DO_SIP_MWI						= "doSipMwi";
    public static final String SIP_MWI_QUEUE_SIZE				= "sipMwiQueueSize";
    public static final String SIP_MWI_WORKERS					= "sipMwiWorkers";
    public static final String HLR_ROAM_FAILURE_ACTION			= "hlrRoamFailureAction";
    public static final String IMAP_TIMEOUT						= "imapTimeout";
    public static final String APPLE_IMAP_SERVER_ADDRESS        = "appleImapServerAddress";
    public static final String APPLE_IMAP_SERVER_PORT           = "appleImapServerPort";
    public static final String INTERNAL_QUEUE_SIZE				= "internalQueueSize";
    public static final String KEEP_SMS_CONNECTIONS				= "keepSmscConnections";
    public static final String LOGICAL_ZONE						= "logicalZone";
    public static final String LOG_LEVEL						= "logLevel";
    public static final String LOG_SIZE							= "logSize";
    public static final String MAX_TIME_BEFORE_EXPUNGE			= "maxTimeBeforeExpunge";
    public static final String MAX_XMP_CONNECTIONS				= "maxXmpConnections";

    public static final String MMSC_PASSWORD					= "mmscPassword";
    public static final String MMS_VERSION						= "mmsVersion";
    public static final String MMS_SYSTEM_DOMAIN				= "mmsSystemDomain";
    public static final String MMSC_VAS_ID						= "mmscVasId";
    public static final String MMSC_VASP_ID						= "mmscVaspId";
    public static final String MMS_MAX_CONNECTION				= "mmsMaxConnection";
    public static final String MMS_MAX_VIDEO_LENGTH				= "mmsMaxVideoLength";
    public static final String MMS_POST_MASTER					= "mmsPostmaster";
    public static final String MMS_USER_NAME					= "mmsUsername";
    public static final String MMS_PREFERED_AUDIO_CODEC				= "mmsPreferedAudioCodec";
    public static final String MMS_MAX_MSG_SIZE					= "mmsMaxMsgSize";

    public static final String MWI_STORE_MESSAGE_AFTER_UPDATING = "mwiStoreMessageAfterUpdating";
    public static final String MWI_OFF_CHECK_COUNT              = "mwiOffCheckCount";
    public static final String NETMASK							= "netmask";
    public static final String NOTIF_THREADS					= "notifThreads";
    public static final String NUMBER_OF_SMS					= "numberOfSms";
    public static final String NUMBER_TO_MESSAGING_SYSTEM		= "numberToMessagingSystem";
    public static final String NUMBER_TO_MESSAGING_SYSTEM_FOR_CALL_MWI	= "numberToMessagingSystemForCallMwi";
    public static final String JOURNAL_REFRESH					= "journalRefresh";
    public static final String PAGER_PAUSE_TIME					= "pagerPauseTime";
    public static final String PATH_TO_SNMP_SCRIPTS				= "pathToSnmpScripts";
    public static final String QUOTA_ACTION						= "quotaAction";
    public static final String QUOTA_TEMPLATE					= "quotaTemplate";
    public static final String QUOTA_PER_TYPE_TEMPLATE					= "quotaPerTypeTemplate";
    public static final String HIGH_QUOTA_TEMPLATE					= "highQuotaTemplate";
    public static final String HIGH_QUOTA_PER_TYPE_TEMPLATE					= "highQuotaPerTypeTemplate";
    public static final String VVM_SYSTEM_DEACTIVATED_TEMPLATE  = "vvmSystemDeactivatedTemplate";
    public static final String VVM_IMAP_FIRST_DETECTED_TEMPLATE = "vvmImapFirstDetectedTemplate";
    public static final String SEND_VVM_SYSTEM_DEACTIVATED_SMS  = "sendVvmSystemDeactivatedSMS";
    public static final String SEND_VVM_IMAP_FIRST_DETECTED_SMS = "sendVvmImapFirstDetectedSMS";
    public static final String SEND_UPDATE_AFTER_RETRIEVAL		= "sendUpdateAfterRetrieval";
    public static final String SEND_UPDATE_AFTER_TERMINAL_CHANGE	= "sendUpdateAfterTerminalChange";
    public static final String SET_REPLY_PATH					= "setReplyPath";

    public static final String SLAMDOWN_MAX_CALLERS				= "slamdownMaxCallers";
    public static final String SLAMDOWN_MAX_CALLS_PER_CALLER	= "slamdownMaxCallsPerCaller";
    public static final String SLAMDOWN_MAX_DIGITS_IN_NUMBER 	= "slamdownMaxDigitsInNumber";
    public static final String SLAMDOWN_OLDEST_FIRST			= "slamdownOldestFirst";
    public static final String SLAMDOWN_TIME_OF_LAST_CALL		= "slamdownTimeOfLastCall";
    public static final String SLAMDOWN_TRUNCATED_NUMBER_INDICATION				= "slamdownTruncatedNumberIndication";
    public static final String SLAMDOWN_NOTIFICATION_WHEN_PHONE_ON_FAILED		= "slamdownNotificationWhenPhoneOnFailed";
    public static final String SLAMDOWN_MCN_NOTIFICATION_WHEN_PHONE_ON_EXPIRY	= "slamdownMcnNotificationWhenPhoneOnExpiry";
    public static final String SLAMDOWN_RETENTION_DURATION                      = "slamdownRetentionDuration";
    public static final String SLAMDOWN_RETENTION_NB_OF_CALLERS                 = "slamdownRetentionNumberOfCallers";

    public static final String MCN_MAX_DIGITS_IN_NUMBER			= "mcnMaxDigitsInNumber";
    public static final String MCN_OLDEST_FIRST					= "mcnOldestFirst";
    public static final String MCN_TIME_OF_LAST_CALL			= "mcnTimeOfLastCall";
    public static final String MCN_TRUNCATED_NUMBER_INDICATION	= "mcnTruncatedNumberIndication";
    public static final String MCN_MAX_CALLERS					= "mcnMaxCallers";
    public static final String MCN_MAX_CALLS_PER_CALLER			= "mcnMaxCallsPerCaller";
    public static final String MCN_LANGUAGE						= "mcnLanguage";
    public static final String MCN_RETENTION_DURATION           = "mcnRetentionDuration";
    public static final String MCN_RETENTION_NB_OF_CALLERS      = "mcnRetentionNumberOfCallers";

    public static final String MCN_SUBSCRIBED_ENABLED                 = "mcnSubscribedEnabled";
    public static final String MCN_SUBSCRIBED_RETENTION_DURATION      = "mcnSubscribedRetentionDuration";
    public static final String MCN_SUBSCRIBED_RETENTION_NB_OF_CALLERS = "mcnSubscribedRetentionNumberOfCallers";

    public static final String SME_SERVICE_TYPE					= "smeServiceType";
    public static final String VVM_SERVICE_TYPE                 = "vvmServiceType";
    public static final String SME_SERVICE_TYPE_FOR_MWI			= "smeServiceTypeForMwi";
    public static final String SME_SOURCE_ADDRESS				= "smeSourceAddress";
    public static final String SME_SOURCE_NPI					= "smeSourceNpi";
    public static final String SME_SOURCE_TON					= "smeSourceTon";
    public static final String SMPP_VERSION						= "smppVersion";
    public static final String SMCS_ERROR_ACTION				= "smscErrorAction";
    public static final String SMSC_POLL_INTERVAL				= "smscPollInterval";
    public static final String SMSC_TIMEOUT						= "smscTimeout";
    public static final String SMSC_TIMEOUT_SUBMIT_SM           = "smscTimeoutSubmitSm";
    public static final String SMSC_TIMEOUT_DATA_SM             = "smscTimeoutDataSm";
    public static final String SMSC_SHUTDOWN_PERIOD             = "smscShutdownPeriod";
    public static final String SMS_CLIENT_ASYNCHRONOUS          = "smsClientAsynchronous";
    public static final String SMPP_BINDING                     = "smppBinding";
    public static final String SMS_MIN_CONN                     = "smsMinConn";
    public static final String SMS_MAX_CONN						= "smsMaxConn";
    public static final String SMS_NUM_RECEIVER_CONN            = "smsNumReceiverConn";
    public static final String SMS_MIN_TIME_BETWEEN_CONN        = "smsMinTimeBetweenConn";
    public static final String SMS_MIN_TIME_BETWEEN_RECONN      = "smsMinTimeBetweenReConn";
    public static final String SMS_NUMBERING_PLAN_INDICATOR		= "smsNumberingPlanIndicator";
    public static final String SMS_STRING_LENGTH				= "smsStringLength";
    public static final String SMS_TYPE_OF_NUMBER				= "smsTypeOfNumber";
    public static final String SMS_PRIORITY						= "smsPriority";
    public static final String SMS_QUEUE_SIZE					= "smsQueueSize";
    public static final String SMS_HANDLER_LOAD_BALANCING		= "smsHandlerLoadBalancing";

    public static final String SMS_REMINDER_ENABLED             = "smsReminderEnabled";
    public static final String SMS_REMINDER_INTERVAL_IN_MIN     = "smsReminderIntervalInMin";
    public static final String SMS_REMINDER_EXPIRE_IN_MIN       = "smsReminderExpireInMin";
    public static final String SMS_REMINDER_CONTENT             = "smsReminderContent";
    public static final String SMS_REMINDER_ALLOWED_TYPE        = "smsReminderAllowedType";
    public static final String SMS_REMINDER_IGNORE_FILTERS      = "smsReminderIgnoreFilters";
    public static final String SMS_REMINDER_TRY_SMS_ON_FLS_DISABLED = "smsReminderTrySmsOnFlsDisabled";

    public static final String REMINDER_USE_NTD_TYPE            = "reminderUseNtdType";

    public static final String FLS_REMINDER_CONTENT             = "flsReminderContent";

    public static final String SNMP_AGENT_PORT					= "snmpAgentPort";
    public static final String SNMP_AGENT_TIMEOUT				= "snmpAgentTimeout";
    public static final String SNMP_AGENT_ADDRESS				= "snmpAgentAddress";
    public static final String SPLIT_MWI_AND_SMS				= "splitMwiAndSms";
    public static final String UNREAD_MESSAGE_REMINDER_INTERVAL	= "unreadMessageReminderInterval";
    public static final String UNREAD_MESSAGE_REMINDER_MAX_TIMES = "unreadMessageReminderMaxTimes";
    public static final String UNREAD_MESSAGE_REMINDER_TYPE		= "unreadMessageReminderType";
    public static final String USE_ALTERNATIVE_FLASH_DCS		= "useAlternativeFlashDcs";
    public static final String USE_MMS_POST_MASTER				= "useMmsPostmaster";
    public static final String USE_SMIL							= "useSmil";
    public static final String USE_CALLER_IN_EVENT_DESCRIPTION	= "useCallerInEventDescription";

    public static final String WAP_PUSH_PASSWD					= "wapPushPasswd";
    public static final String WAP_PUSH_RETIEVAL_HOST			= "wapPushRetrievalHost";
    public static final String WAP_PUSH_URL_SUFFIX				= "wapPushUrlSuffix";
    public static final String WAP_PUSH_USER_NAME				= "wapPushUserName";
    public static final String WATCHDOG_LOG_LEVEL				= "watchdogLogLevel";
    public static final String WATCHDOG_LOG_SIZE				= "watchdogLogDize";
    public static final String XMP_POLL_INTERVAL				= "xmpPollInterval";
    public static final String XMP_TIMEOUT						= "xmpTimeout";
    public static final String XMP_VALIDITY						= "xmpValidity";
    public static final String XMP_REFRESH_TIME					= "xmpRefreshTime";
    public static final String NTF_EVENTS_ROOT_PATH				= "ntfEventsRootPath";
    public static final String NOTIFY_RETRY_SCHEMA				= "notifRetrySchema";
    public static final String NOTIFY_EXPIRE_TIME_IN_MIN		= "notifExpireTimeInMin";

    public static final String SIP_MWI_NOTIFY_RETRY_SCHEMA		= "sipMwiNotifRetrySchema";
    public static final String SIP_MWI_EXPIRE_TIME_IN_MIN		= "sipMwiExpireTimeInMin";
    public static final String SIP_MWI_EXPIRY_INTERVAL_IN_MIN   = "sipMwiExpiryIntInMin";
    public static final String SIP_MWI_EXPIRY_RETRIES           = "sipMwiExpiryRetries";
    public static final String SIP_MWI_OK_XMP_CODE              = "sipMwiOkXmpCode";
    public static final String SIP_MWI_RETRY_XMP_CODE           = "sipMwiRetryXmpCode";
    public static final String SIP_MWI_NOT_SUBSCRIBED_XMP_CODE  = "sipMwiNotSubscribedXmpCode";
    public static final String SIP_MWI_REMINDER_INTERVAL_IN_MIN = "sipMwiReminderIntervalInMin";
    public static final String SIP_MWI_REMINDER_EXPIRE_IN_MIN  = "sipMwiReminderExpireInMin";
    public static final String SIP_MWI_REMINDER_ENABLED         = "sipMwiReminderEnabled";

    public static final String SERVICE_LISTENER_CORE_POOL_SIZE	= "serviceListenerCorePoolSize";
    public static final String SERVICE_LISTENER_MAX_POOL_SIZE	= "serviceListenerMaxPoolSize";
    public static final String LOGIN_FILE_VALIDITY_PERIOD_IN_MIN = "loginFileValidityPeriodInMin";

    public static final String FAX_PRINT_NOTIFY_RETRY_SCHEMA    = "faxPrintNotifRetrySchema";
    public static final String FAX_PRINT_EXPIRE_TIME_IN_MIN     = "faxPrintExpireTimeInMin";
    public static final String FAX_PRINT_MAX_CONN               = "faxPrintMaxConn";
    public static final String FAX_SUCCESS_NOTIFICATION         = "faxSuccessNotification";
    public static final String FAX_ENABLED                      = "faxEnabled";
    public static final String FAX_WORKERS                      = "faxWorkers";
    public static final String FAX_PRINT_QUEUE_SIZE             = "faxPrintQueueSize";
    public static final String FAX_WORKER_QUEUE_SIZE            = "faxWorkersQueueSize";

    // Character set encoding
    public static final String CHARSET_ENCODING                 = "charsetEncoding";

    // Read SMPP network error code when this error is thrown.
    public static final String LOOKUP_NETWORK_ERROR_CODE_WHEN_COMMAND_STATUS_IS = "lookupNetworkErrorCodeWhenCommandStatusIs";

    // Feature sim swap detection (VVM)
    public static final String SIM_SWAP_TIMEOUT                    = "simSwapTimeout";
    public static final String SIM_SWAP_TIMEOUT_EXPIRE_TIME_IN_MIN = "simSwapTimeoutExpireTimeInMin";
    /** @deprecated  */
    public static final String SIM_SWAP_PHONE_MODE                 = "simSwapPhoneOnMode";
    /** @deprecated  */
    public static       enum   PhoneOnDetectionKind            {HLR_SRI, SMSTYPE0};
    public static final String SIM_SWAP_SENDING_UNIT_PHONE_ON_RETRY_SCHEMA           = "simSwapSendingUnitPhoneOnRetrySchema";
    public static final String SIM_SWAP_SENDING_UNIT_PHONE_ON_EXPIRE_TIME_IN_MIN     = "simSwapSendingUnitPhoneOnExpireTimeInMin";
    public static final String SIM_SWAP_WAITING_PHONE_ON_RETRY_SCHEMA                = "simSwapWaitingPhoneOnRetrySchema";
    public static final String SIM_SWAP_WAITING_PHONE_ON_EXPIRE_TIME_IN_MIN          = "simSwapWaitingPhoneOnExpireTimeInMin";

    // lists
	public static final String ALLOWED_SMSC_LIST				= "AllowedSmsc.List";
    public static final String REPLACE_NOTIFICATIONS_LIST		= "ReplaceNotifications.List";
    public static final String SMPP_ERROR_CODES_IGNORED_LIST	= "SmppErrorCodesIgnored.List";

    // tables
    public static final String SOURCE_ADDRESS_TABLE				= "SourceAddress.Table";
    public static final String SOURCE_ADDRESS_VALUE				= "sourceAddressValue";
    public static final String SOURCE_ADDRESS_DEBUT				= "sourceaddress_";

    public static final String VALIDITY_TABLE					= "Validity.Table";
    public static final String VALIDITY_VALUE					= "validityValue";
    public static final String VALIDITY_DEBUT					= "validity_";
    public static final int    DEFAULT_VALIDITY_VALUE			= -1;
    public static final int    DEFAULT_VALIDITY_SMS_TYPE_0_VALUE = 24;

    public static final String MWI_SERVERS_TABLE				= "MwiServers.Table";
    public static final String MWI_SERVERS						= "mwiServers";

    public static final String LANG_TO_MIME_TEXT_CHARSET_TABLE  = "LanguagetoMime-EmailTextCharSet.Table";
    public static final String LANG_TO_MIME_TEXT_CHARSET_VAL    = "charSetValue";

    public static final String SMSC_BACKUP_TABLE				= "SmscBackup.Table";
    public static final String SMSC_BACKUP_VALUE				= "smscBackupValue";

    public static final String NOTIFIER_PLUGIN_TABLE            = "NotifierPlugin.Table";
    public static final String NOTIFIER_PLUGIN                  = "notifierPlugIn";
    public static final String NOTIFIER_PLUGIN_CLASS            = "class";

    public static final String DEFAULT_MIME_TEXT_CHARSET        = "defaultEmailMimeTextCharset";

    public static final String SMS_TYPE_0						= "smstype0";
    public static final String MWI_ON							= "mwion";
    public static final String MWI_OFF							= "mwioff";
    public static final String MAIL_QUOTA_EXCEEDED				= "mailquotaexceeded";
    public static final String MAIL_QUOTA_HIGH_LEVEL_EXCEEDED   = "mailquotahighlevelexceeded";

    public static final String TEMPORARY_GREETING_ON_REMINDER	= "temporarygreetingonreminder";
    public static final String VOICEMAIL_OFF_REMINDER			= "voicemailoffreminder";
    public static final String CFU_ON_REMINDER					= "cfuonreminder";
    public static final String SLAMDOWN							= "slamdown";
    public static final String MCN                              = "mcn";

    public static final String VVM                              = "vvm";
    public static final String VVM_SYSTEM_DEACTIVATED           = "vvmsystemdeactivated";
    public static final String VVM_QUEUE_SIZE                   = "vvmQueueSize";
    public static final String VVM_WORKERS                      = "vvmWorkers";
    public static final String VVM_SOURCE_PORT                  = "vvmSourcePort";
    public static final String VVM_DESTINATION_PORT             = "vvmDestinationPort";
    public static final String VVM_SMS_UNIT_RETRY_SCHEMA        = "vvmSmsUnitRetrySchema";
    public static final String VVM_SMS_UNIT_EXPIRE_TIME_IN_MIN  = "vvmSmsUnitExpireTimeInMin";
    public static final String VVM_EXPIRY_INTERVAL_IN_MIN       = "vvmExpiryIntInMin";
    public static final String VVM_EXPIRY_RETRIES               = "vvmExpiryRetries";
    public static final String VVM_STATUS_FILE_VALIDITY_IN_MIN  = "vvmStatusValInMin";
    public static final String VVM_PHONE_ON_LOCK_FILE_VALIDITY_IN_SECONDS = "vvmPhoneOnLockValInSecs";

    public static final String AUTO_UNLOCK_PIN                              = "autounlockpin";

    public static final String SHORT_MESSAGE_TABLE              = "ShortMessage.Table";
    public static final String MAIL_TRANSFER_AGENT_TABLE        = "MailTransferAgent.Table";
    public static final String MULTIMEDIA_MESSAGE_TABLE         = "MultiMediaMessage.Table";
    public static final String FAX_SERVER_TABLE                 = "FaxServer.Table";

    public static final String COMPONENT_TYPE                   = "componentType";
    public static final String HOST_NAME                        = "hostName";
    public static final String PORT                             = "port";
    public static final String USER_NAME                        = "userName";
    public static final String PASSWORD                         = "password";
    public static final String SYSTEM_TYPE                      = "systemType";
    public static final String PROTOCOL                         = "protocol";
    public static final String URI                              = "uri";
    public static final String AVAILABILITY                     = "availability";
    public static final String INSTANCE                         = "instanceNo";

    public static final String PHONE_ON_METHOD					= "phoneOnMethod";
    public static final String PHONE_ON_SMS_TYPE_0				= "SMSType0";
    public static final String PHONE_ON_ALERT_SC				= "AlertServiceCentre";
 //TR VDF
    public static final String SEND_DETACH_ON_ASSUMED_UNAVAILABLE = "sendDetachOnAssumedUnavailable";
    public static final String CHECK_ROAMING					= "checkRoaming";
    public static final String CHECK_BUSY						= "checkBusyBeforeOutdialNotification";
    public static final String GET_SUBSCRIBER_CHARGING_MODEL	= "getSubscriberChargingModel";  //prepaid or postpaid
    public static final String ROAMING_TEMPLATE_POSITION		= "roamingTemplatePosition";
    public static final String ROAMING_TEMPLATE_POSITION_BEGIN	= "begin";
    public static final String ROAMING_TEMPLATE_POSITION_END 	= "end";
    public static final String ROAMING_TEMPLATE_POSITION_NONE   = "none";


    public static final String FALLBACK_SMS                    = "fallbackSms";
    public static final String FALLBACK_MMS                    = "fallbackMms";
    public static final String FALLBACK_SIPMWI                 = "fallbackSipMwi";
    public static final String FALLBACK_OUTDIAL                = "fallbackOutdial";
    public static final String FALLBACK_VVM                    = "fallbackVvm";
    public static final String FALLBACK_QUEUE_SIZE             = "fallbackQueueSize";
    public static final String FALLBACK_WORKERS                = "fallbackWorkers";
    public static final String FALLBACK_WHEN_ROAMING           = "fallbackWhenRoaming";
    public static final String FALLBACK_USE_MOIP_USER_NTD      = "fallbackUseMoipUserNTD";
    public static final String FALLBACK_FLS_URGENT_ONLY        = "fallbackFlsUrgentOnly";
    public static final String FALLBACK_SMS_ON_URGENT_IF_FLS_SENT = "fallbackSmsOnUrgentifFlsSent";
    public static final String FALLBACK_SMS_WHEN_MWI_SMS_ONLY  = "fallbackSmsWhenMwiSmsOnly";
    public static final String FALLBACK_RETRY_SCHEMA           = "fallbackRetrySchema";
    public static final String FALLBACK_EXPIRE_TIME_IN_MIN     = "fallbackExpireTimeInMin";
    public static final String FALLBACK_OUTDIAL_TO_SMS_CONTENT = "fallbackOutdialToSmsContent";
    public static final String FALLBACK_OUTDIAL_TO_FLS_CONTENT = "fallbackOutdialToFlsContent";
    public static final String FALLBACK_SIPMWI_TO_SMS_CONTENT  = "fallbackSipMwiToSmsContent";
    public static final String FALLBACK_VVM_TO_SMS_CONTENT     = "fallbackVvmToSmsContent";

    public static final String ALERT_SC_REGISTRATION_NUMBER_OF_RETRY = "alertSCRegistrationNumOfRetry";
    public static final String ALERT_SC_REGISTRATION_SEC_BETWEEN_RETRY  = "alertSCRegistrationTimeInSecBetweenRetry";
    public static final int ALERT_SC_REGISTRATION_NUMBER_OF_RETRY_DEFAULT_VALUE = 30;
    public static final int ALERT_SC_REGISTRATION_SEC_BETWEEN_RETRY_DEFAULT_VALUE  = 2;
    
    public static final String SMS_TYPE_0_MESSAGE_STATE        = "smsType0MessageState";
    public static final String FS_TIMEOUT                      = "fsTimeout";

    public static final String FROM_TAG_DENORMALIZE_NUMBER     = "fromTagDenormalizeNumber";

    //Special debugging
    
    public static final String WRITE_MMS_DEBUG_TO_TEMP_FOLDER = "writeMmsDebugToTempFolder"; //write the contents of an MMS notification to TEMP folder.
    public static final String CANCEL_SMS_AT_DEBUG             = "cancelSmsDebug"; //add very verbose logging to debug cancel SMS requests.
    public static final String TEMP_DIRECTORY                  = "tempDirectory"; //used for writing temporary data or debug output for special debug.

}
