/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf;

import java.util.HashMap;


public interface Constants {

    //Result codes
    /**Everything was OK*/
    public static final int RESULT_OK= 0;
    /**Probably OK, but the complete result is not known until later,
       e.g. through a callback*/
    public static final int RESULT_MAYBE= 1;
    /**Not OK, but you can retry at a later time*/
    public static final int RESULT_LATER= 2;
    /**Not OK, and retries will also fail*/
    public static final int RESULT_NEVER= 3;
    /**Value that indicates an invalid message count*/
    public static final int MSG_COUNT_ERR= -1;

    // Message Notification Types
//                                              0      1      2      3      4          5          6        7      8      9        10       11          12      13      14               15               16     17           18          19               20         21
public static final String[] notifTypeStrings={"SMS", "MWI", "MMS", "WAP", "MWI off", "Outdial", "Pager", "TUI", "WMW", "Flash", "Email", "Slamdown", "Mcn",  "VVM",  "FaxPrintNotif", "Fax", "FaxReceiptTUI", "FlashSMS",  "Call MWI", "Delayed Event", "SIP MWI", "Unspecified notif type"};
//                                             0      1      2      3      4      5      6      7      8      9      10     11     12     13     14     15     16    17        18     19       20     21
public static final String[] notifTypeAbbrev={"SMS", "MWI", "MMS", "WAP", "OFF", "ODL", "PAG", "TUI", "WMW", "FLS", "EML", "SLM", "MCN", "VVM", "FAX", "FRS", "FRT","FLSSMS", "CMW", "DLEVT", "SMI", "ERR"};
    public static final String MOIPUserNTD_HOME_PREFIX = "H-";
    public static final String MOIPUserNTD_ROAM_PREFIX = "R-";

    public static final int NTF_SMS = 0;
    public static final int NTF_MWI = 1;
    public static final int NTF_MMS = 2;
    public static final int NTF_WAP = 3;
    public static final int NTF_MWIOff = 4;
    public static final int NTF_ODL = 5;
    public static final int NTF_PAG = 6;
    public static final int NTF_TUI = 7;
    public static final int NTF_WMW = 8;
    public static final int NTF_FLS = 9;
    public static final int NTF_EML = 10;
    public static final int NTF_SLAM = 11;
    public static final int NTF_MCNNOTIF = 12;
    public static final int NTF_VVM = 13;
    public static final int NTF_FAX_PRINT_NOTIF = 14;
    public static final int NTF_FAX_RECEIPT_SMS = 15;
    public static final int NTF_FAX_RECEIPT_TUI = 16;
    public static final int NTF_FLSSMS = 17; //special type for fall-back, means send both depending on filter
    public static final int NTF_CMW = 18; //moved due to numbering inconsistency, CMW not currently used anyway.
    public static final int NTF_DELAYED_NOTIFY = 19;
    public static final int NTF_SIPMWI = 20;
    //NTF_NO_NOTIF_TYPE must have the highest number of the notification type constants
    public static final int NTF_NO_NOTIF_TYPE = 21;

    public static final int FEEDBACK_STATUS_OK = 0;
    public static final int FEEDBACK_STATUS_RETRY = 1;
    public static final int FEEDBACK_STATUS_FAILED = 2;
    public static final int FEEDBACK_STATUS_EXPIRED = 3;
    public static final int FEEDBACK_STATUS_DISABLED = 4;
    public static final int FEEDBACK_STATUS_UNKNOWN = 999;



    public static final String[] STATUS_STRING = {
        "Ok",
        "Retry",
        "Failed",
        "Expired",
        "Unknown"
    };

    public static final int TRANSPORT_IGNORE = 0;
    public static final int TRANSPORT_MOBILE = 1;
    public static final int TRANSPORT_FIXED = 2;
    public static final int TRANSPORT_IP = 3;


    //Action for NTF to take if roaming check fails when roaming check enabled.
    public enum hlrFailAction {RETRY,FAIL,HOME,ROAM};

    //An enum to indicate true/false or error condition.
    public enum triState {TRUE,FALSE,ERROR};




    //These are legacy deposit/email/ types, as they are currently exposed as an API for
    //notification plug-in types they have to be kept but are mapped when needed to the
    //depositType enum below.
    public static final int NTF_EMAIL = 0;
    public static final int NTF_VOICE = 1;
    public static final int NTF_FAX = 2;
    public static final int NTF_SLAMDOWN = 3;
    public static final int NTF_VIDEO = 4;
    /** Voice mail reminder or auto turn on(or cancel) */
    public static final int NTF_DEFERRED_VOICEMAIL = 5;                             //NO LONGER USED
    /** CFU reminder or autoturn off (or cancel pending) */
    public static final int NTF_DEFERRED_CFU = 6;                                   //NO LONGER USED
    /** Temporary greeting reminder or (cancel pending) */
    public static final int NTF_DEFERRED_TEMPGREET = 7;                             //NO LONGER USED
    public static final int NTF_FAX_PRINT= 8;
    public static final int NTF_DEPOSIT_TYPE_MAX= 9;                                //This is supposed to be the end of the list DepositTypeStrings - but its' broken now
    public static final int NTF_MCN = 10;
    public static final int NTF_VVM_DEPOSIT = 11;
    public static final int NTF_VVM_GREETING = 12;
    public static final int NTF_VVM_EXPIRY = 13;
    public static final int NTF_VVM_LOGOUT = 14;
    public static final int NTF_FAX_RECEPT_MAIL_TYPE = 15;
    public static final int NTF_VVA_SMS = 16;
    public static final int NTF_AUTO_UNLOCK_PIN = 17;
    public static final int NTF_FLASH = 18;                                         //This is the last value for deposit type from EMAIL to FLASH
    public static final int NTF_VVM_TYPE = 19;
    public static final int NTF_SMS_TYPE_0 = 20;
    public static final int NTF_MWI_OFF = 21;
    public static final int NTF_MWI_ON = 22;
    public static final int NTF_DELAYED_SMS_REMINDER = 23;
    public static final int NTF_DEPOSIT_UNKNOWN = 999;
 /*This is the depositType referenced from the legacy deposit_types above
  *For now it maps the deposit type (email type) to the numbers above.
  *The dead types just return DEPOSIT_TYPE_UNKNOWN
  *Currently this is used to map the sourceAddress types from the
  *notification.xsd amongst other things.
  */
 public enum depositType {
        EMAIL(NTF_EMAIL,"email","e"),
        VOICE(NTF_VOICE,"voice","v"),
        FAX(NTF_FAX,"fax","f"),
        SLAMDOWN(NTF_SLAMDOWN,"slamdown","s"),
        VIDEO(NTF_VIDEO,"video","m"),
        FAX_PRINT(NTF_FAX_PRINT,"faxprint","p"),
        MCN(NTF_MCN,"mcn","#"),
        VVM_DEPOSIT(NTF_VVM_DEPOSIT,"vvm","#"),
        VVM_GREETING(NTF_VVM_GREETING,"vvm","#"),
        VVM_EXPIRY(NTF_VVM_EXPIRY,"vvm","#"),
        VVM_LOGOUT(NTF_VVM_LOGOUT,"vvm","#"),
        FAX_RECEPT_MAIL_TYPE(NTF_FAX_RECEPT_MAIL_TYPE,"fax","#"),
        VVA_SMS(NTF_VVA_SMS,"vvasms","#"),
        AUTO_UNLOCK_PIN(NTF_AUTO_UNLOCK_PIN,"autounlockpin","#"),
        FLASH(NTF_FLASH,"flash","#"),
        SMS_TYPE_0(NTF_SMS_TYPE_0,"smstype0","#"),
        VVM(NTF_VVM_TYPE,"vvm","#"),
        MWI_OFF(NTF_MWI_ON,"mwioff","#"),
        MWI_ON(NTF_MWI_ON,"mwion","#"),
        DELAYED_SMS_REMINDER(NTF_DELAYED_SMS_REMINDER,"delayedsmsreminder","#"),
        DEPOSIT_TYPE_UNKNOWN(NTF_DEPOSIT_UNKNOWN,"smeSourceAddress","#");

        private String sourceString;
        private int value;
        private String oneCharAbreviation;

        private static HashMap<Integer, depositType> map = new HashMap<Integer, depositType>();

        static {
            for (depositType depositTypeEnum : depositType.values()) {
                map.put(depositTypeEnum.value, depositTypeEnum);
            }
        }

        depositType(int value,String sourceString,String oneCharAbreviation) {
            this.value=value;
            this.sourceString = sourceString;
            this.oneCharAbreviation=oneCharAbreviation;
        }

        public String source() {
            return sourceString;
        }

        /*
         * returns the one character filter abbreviation if any for this
         * type i.e e for email type.
         */
        public String filtAbrev() {
            return oneCharAbreviation;
        }

        public int value() {
            return value;
        }

        public static depositType getDepositType(int val)
        {
            depositType t = map.get(val);
            if (t == null) {
                return (DEPOSIT_TYPE_UNKNOWN);
            }
            return t;
        }
    }


   //Flash and SMS types to cancel if cancel enabled.
   depositType[] flsSmsCancelDepositTypes = { depositType.EMAIL, depositType.VOICE, depositType.FAX, depositType.VIDEO };;

    // sms types
    public static final String UPDATESMSNAME = "updatesms";
    public static final String UPDATEAFTERRETRIEVALNAME = "updateafterretrieval";
    public static final String UPDATESMSTERMINALCHANGENAME = "updateafterterminalchange";
    public static final String REMINDERSMSNAME = "unreadmessagereminder";

    //Message Priority
    public static final int HIGHEST_PRIORITY = 1;
    public static final int HIGH_PRIORITY    = 2;
    public static final int NORMAL_PRIORITY  = 3;
    public static final int LOW_PRIORITY     = 4;
    public static final int LOWEST_PRIORITY  = 5;

    // System Property Names
    static final String MUR_HOST = "LDAPHost";
    static final String MUR_PORT = "LDAPPort";
    static final String MUR_DISTINGUISHED_NAME = "DN";
    static final String MUR_PASSWORD = "LDAPPasswd";
    static final String MUR_SEARCHBASE = "LDAPSearchBase";
    static final int LDAP_VERSION = 3;
    static final String CONF_NUMBER_OF_SMS="NumberOfSMS";
    static final String SMS_STRING="SMSString";

    static final String SMSC_NAME="smppcenterid"; // "SMSCenterID";
//    static final String SMSC_HOST="SMSCHost";
    static final String SMPP_PORT="smpport"; // "SMPPPort";
    static final String SME_SYSTEM_ID="SMESystemID";
    static final String SME_SYSTEM_PASSWD = "SMEPasswd";
    static final String SME_SYSTEM_TYPE = "SMESystemType";

    //default values
    static final String DEFAULT_CONFIG_FILE_NAME = "./notification.cfg";

    //Bearing Networks
    static final int BEARING_NETWORK_GSM= 1;
    static final int BEARING_NETWORK_CDMA2000= 2;

   // values for building Usage Event Message
    static final byte IPMS_SUBSYS_MERAGENT= 0x03;
    static final int  EVENT_TYPE =   1;  //login,deposit,retrieve
    static final int  EVENT_TIME =   5;
    static final int  DEPOSIT_TYPE = 7;  //login,deposit,retrieve

    static final int  EVENT_DESCRIPTION = 90;
    static final int  COS              = 92;  //community of interest
    static final int  MER_MESSAGE_TYPE = 93;  // text, video,voice
    static final int  MAILBOX_ID       = 100;

    // Values for type of caller in call-MWI CLI information
    public static final int CALLMWI_CALLER_SYSTEM = 0;
    public static final int CALLMWI_CALLER_CALLER = 1;
    public static final int CALLMWI_CALLER_SUBSCRIBER = 2;

    // String values for ESI parameters
    public static final String MSISDN = "msisdn";
    public static final String CFU = "cf-unconditional";
    public static final String CFB = "cf-busy";
    public static final String CFNRC = "cf-not-reachable";
    public static final String CFNRY = "cf-no-reply";
    public static final String LOCATION = "roaming-status";
    public static final String SEP = "prepaid-service-node-number";
    public static final String HASMWI = "terminal-has-mwi";
    public static final String HASFLASH = "terminal-has-flash";
    public static final String HASREPLACE = "terminal-has-replace";

 // the phone number for the MWI OFF notification
    public final static String DEST_RECIPIENT_ID = "RecipientId";

    // Dummy class of service for Missed Call Notification
    public final static String DUMMY_MCN_COS = "mcn_cos";

    public final static String FAX_DELEVERY_RECEIPT_CONTENT_TYPE_SUCCESS_DEFAULT = "faxprintsuccessdefault";
    public final static String FAX_DELEVERY_RECEIPT_CONTENT_TYPE_SUCCESS = "faxprintsuccess";
    public final static String FAX_DELEVERY_RECEIPT_CONTENT_TYPE_FAIL_DEFAULT = "faxprintfaildefault";
    public final static String FAX_DELEVERY_RECEIPT_CONTENT_TYPE_FAIL = "faxprintfail";


    //type of event that initiates a cancel SMS if no unread messages.
    public enum CancelSmsEnabledForEvent {
        none,
        mwioff,
        mailboxupdate
    }

    // Type of cancel to send if cancel SMS.
    public enum CancelSmsMethod {
        serviceTypeContent,
        sourceAddress,
        both
    }



    public static final int DEFAULT_DELAY = 1;
}
