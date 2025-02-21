package com.mobeon.ntf.out.outdial;

public class OdlConstants {
    /** Event to start an out dial. */
    public static final String EVENT_OUTDIAL_START = "Start";
    /** Event when a call is made. */
    public static final String EVENT_OUTDIAL_CALL_MADE = "Call";
    /** Event when a phone is turned on. */
    public static final String EVENT_OUTDIAL_PHONEON = "PhoneOn";
    /** Event when a phone is deemed busy from HLR*/
    public static final String EVENT_OUTDIAL_BUSY = "Busy";
    /** Event when there is SS7 Error */
    public static final String EVENT_SS7_ERROR = "Ss7Error";

    //
    // XMP Codes
    //
    static public final int EVENT_CODE_COMPLETED       = 200;
    static public final int EVENT_CODE_INITIATED       = 202;

    // The outdial specific codes in the 400 range is now obsolete,
    // the codes in the 600 range should be used in config files
    // and should be the ones returned from MAS.
    // The general code in the 400 series (408, 421 is still
    // applicable.)
    static public final int EVENT_CODE_NUM_BLOCKED_OLD     = 401;
    static public final int EVENT_CODE_NUM_BUSY_OLD        = 402;
    static public final int EVENT_CODE_NOANSWER_OLD        = 404;
    static public final int EVENT_CODE_NOT_REACHABLE_OLD   = 405;
    static public final int EVENT_CODE_REQUEST_TIMEOUT     = 408;
    static public final int EVENT_CODE_NOT_AVAILABLE       = 421;

    static public final int EVENT_CODE_SYNTAX_ERROR    = 500;
    static public final int EVENT_CODE_UNRECOGNIZED    = 501;
    static public final int EVENT_CODE_LIMIT_EXCEEDED  = 502;
    static public final int EVENT_CODE_NUMBER_NOTEXIST = 511;
    static public final int EVENT_CODE_INVALIND_NUM    = 512;
    static public final int EVENT_CODE_UNKNOWN_ERR     = 513;
    static public final int EVENT_CODE_NOMAILBOX       = 514;

    // New event codes.
    // Codes between 601-634 are available for use, however
    // only the codes given below have predefined meanings.
    static public final int EVENT_CODE_BUSY               = 603;
    static public final int EVENT_CODE_CALL_NOT_ANSWERED  = 610;
    static public final int EVENT_CODE_DESTINATION_NOT_REACHABLE = 613;
    static public final int EVENT_CODE_DO_NOT_DISTURB     = 614;
    static public final int EVENT_CODE_NETWORK_CONGESTION = 620;
    static public final int EVENT_CODE_PREPAID_FAILURE = 625; //failure due to insufficient funds
    static public final int EVENT_CODE_HLR_ERROR = 627;


    /** Default code, used when we have not call response. */
    public static final int EVENT_CODE_DEFAULT = 900;
    /** Out call notification disabled. */
    public static final int EVENT_CODE_NOTIFDISABLED = 910;
    /** out call was disabled due to roaming disabled. */
    public static final int EVENT_CODE_ROAMINGDISABLED = 911;
    /** Unable to decide roaming-location. */
    public static final int EVENT_CODE_LOCATION_FAILURE = 915;
    /** Phone has Call Forward Unconditional, could not call. */
    public static final int EVENT_CODE_CFU_ON = 920;
    /** Unable to decide CFU */
    public static final int EVENT_CODE_CFU_FAILURE = 925;
    /** Out call was interrupted, internal problem. */
    public static final int EVENT_CODE_INTERRUPTED = 930;
    /** The phone was turned on. */
    public static final int EVENT_CODE_PHONEON = 940;

}
