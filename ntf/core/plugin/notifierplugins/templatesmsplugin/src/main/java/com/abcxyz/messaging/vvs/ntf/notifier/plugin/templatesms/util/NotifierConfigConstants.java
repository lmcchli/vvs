/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util;

public interface NotifierConfigConstants {

    public static final String TEMPLATE_PLUGIN_CONFIG_FILE_NAME        = "templateSmsPlugin.conf";

    // NTF configuration parameters
    public static final String CHARSET_ENCODING                        = "charsetEncoding";

    public static final String VALIDITY_TABLE                          = "Validity.Table";
    public static final String VALIDITY_VALUE                          = "validityValue";
    public static final String VALIDITY_PREFIX                         = "validity_";
    public static final int    DEFAULT_VALIDITY_VALUE                  = -1;

    public static final String SMS_NUMBERING_PLAN_INDICATOR            = "smsNumberingPlanIndicator";
    public static final String SMS_TYPE_OF_NUMBER                      = "smsTypeOfNumber";
    public static final String SOURCE_ADDRESS_TABLE                    = "SourceAddress.Table";
    public static final String SOURCE_ADDRESS_VALUE                    = "sourceAddressValue";
    public static final String SOURCE_ADDRESS_PREFIX                   = "sourceaddress_";
    public static final String SME_SOURCE_ADDRESS                      = "smeSourceAddress";
    public static final String SME_SOURCE_NPI                          = "smeSourceNpi";
    public static final String SME_SOURCE_TON                          = "smeSourceTon";
    
    public static final String CANCEL_SMS_ENABLED_FOR_EVENT     	   = "cancelSmsSentOnEvent";
    public static final String DISABLE_SMSC_REPLACE					   = "disableSmscReplace";

    // Notifier common attributes
    public static final String NOTIFIER_QUEUE_SIZE                     = "templateSmsSenderSmsQueueSize";
    public static final String NOTIFIER_WORKERS                        = "templateSmsWorkers";
    public static final String NOTIFIER_SENDER_SMS_QUEUE_SIZE          = "templateSmsSenderSmsQueueSize";
    public static final String NOTIFIER_SENDER_SMS_WORKERS             = "templateSmsSenderSmsWorkers";

    // Notifier Table attributes
    public static final String TEMPLATE_SMS_TABLE                      = "templateSms.Table";
    public static final String CPHR_NOTIF_TYPE  	                   = "cphrType";
    public static final String PDU_TYPE                                = "pduType";
    public static final String STATUS_FILE_VALIDITY_IN_MIN             = "statusFileValidityInMin";
    public static final String SERVICE_TYPE                            = "serviceType";
    //This value means that the serviceType should be taken from NTF Replace Table or smeServiceType.
    //When set for SERVICE_TYPE otherwise it is overridden by the plug-in config.
    public static final String USE_NTF_SERVICE_TYPE					   = "smeServiceType";
    public static final String MDR_PORT_TYPE                           = "mdrPortType";
    public static final String MDR_NAME                                = "mdrName";
 
    public static final String INTITIAL_RETRY_SCHEMA                   = "initialRetrySchema";
    public static final String INTITIAL_EXPIRE_TIME_IN_MIN             = "initialExpireTimeInMin";

    public static final String SENDING_RETRY_SCHEMA     		       = "sendingRetrySchema";
    public static final String SENDING_EXPIRE_TIME_IN_MIN              = "sendingExpireTimeInMin";
    public static final String ENABLED_COS                             = "enabledCos";
    public static final String EXPIRY_INTERVAL_IN_MIN                  = "expiryIntervalInMin";
    public static final String EXPIRY_RETRIES                          = "expiryRetries";
    public static final String NOTIFICATION_NUMBER_TYPE                = "notificationNumberType";
    public static final String REPLACE_SMS_ENABLED                     = "replaceSMSEnabled";
    public static final String CANCEL_SMS_ON_NTF_CANCEL		           = "cancelSmsOnNtfCancel";
    
    //type of event that initiates a cancel SMS if no unread messages.
    public enum CancelSmsEnabledForEvent {
        none,
        mwioff,
        mailboxupdate,        
    }
}
