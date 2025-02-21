/*
 * OdlInfo.java
 *
 * Created on den 19 september 2004, 18:29
 */

package com.mobeon.ntf.out.outdial;

import com.mobeon.common.commands.Command;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.storedelay.DelayEvent;
import com.mobeon.common.storedelay.DelayInfo;
import com.mobeon.ntf.mail.NotificationEmail;


/**
 * Holds information about one active out dial.
 */
public class OdlInfo
{

    /** Type used in delay to identify out dial notifications. */
    public static final short DELAY_TYPE_OUTDIAL = 0;


    /** Event to start an out dial. */
    public static final String EVENT_OUTDIAL_START = "Start";
    /** Event when a call is made. */
    public static final String EVENT_OUTDIAL_CALL_MADE = "Call";
    /** Event when a phone is turned on. */
    public static final String EVENT_OUTDIAL_PHONEON = "PhoneOn";


    //
    // XMP Codes
    //
    static public final int EVENT_CODE_COMPLETED       = 200;
    static public final int EVENT_CODE_INITIATED       = 202;

    // The outdial specific codes in the 400 range is now obsolete,
    // the codes in the 600 range should be used in config files
    // and should be the ones returned from MVAS.
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
    static public final int EVENT_CODE_PREPAID_FAILURE = 625;
    static public final int EVENT_CODE_SS7_ERROR = 627;




    /** Default code, used when we have not call response. */
    public static final int EVENT_CODE_DEFAULT = 900;
    /** Out call notification disabled. */
    public static final int EVENT_CODE_NOTIFDISABLED = 910;
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

    // Persistent info
    private byte version;
    private String dialNumber;
    private String userEmail;
    private long startTime;
    private String userDN;
    private Command currentCommand;

    // Volatile info, valid only for current memory instance
    /** Event that lead to notification. */
    private DelayEvent event;
    /** Status when notifying, only non error statuses will be given to worker. */
    private int status;

    private NotificationEmail userNotifEmail;

    public OdlInfo(String dialNumber, NotificationEmail userEmail, long startTime, String userDN) {
        this.version = 1;
        this.dialNumber = dialNumber;
        this.userNotifEmail = userEmail;
        this.startTime = startTime;
        this.userDN = userDN;
        currentCommand = null;
    }

    public NotificationEmail getNotificationEmail() {
    	return this.userNotifEmail;
    }

    /**
     * Create a new OdlInfo.
     * @param dialNumber Number to outdial to.
     * @param userEmail Email of user we are doing the outdial for
     * @param startTime Time the outdial started, milliseconds since 1970
     * @param userDN Identity for user in MUR.
     */
    public OdlInfo(String dialNumber, String userEmail,
                   long startTime, String userDN)
    {
        this.version = 1;
        this.dialNumber = dialNumber;
        this.userEmail = userEmail;
        this.startTime = startTime;
        this.userDN = userDN;
        currentCommand = null;
    }

    /**
     * Create OdlInfo from persistent storage.
     * @param di DelayInfo that has been stored.
     */
    public OdlInfo(DelayInfo di)
    {
        String key = di.getKey();
        int index = key.indexOf(',');
        if ((index <= 0)) {
            // Log bad format
            dialNumber = key;
            userEmail = "";
        } else {
            dialNumber = key.substring(0, index);
            if (index < key.length() - 1) {
                userEmail = key.substring(index + 1);
            } else {
                userEmail = "";
            }
        }
        userDN = di.getStrInfo();
        byte[] byteData = di.getByteInfo();
        version = byteData[0];
        startTime = DelayInfo.unpackLong(byteData, 1);
        if (byteData.length > 9) {
            currentCommand = new Command();
            currentCommand.restore(byteData, 9, CommandHandler.OP_NAMES);
        }
    }


    /**
     * Get string respresentation for logging/debugging.
     * @return Descriptive string
     */
    public String toString()
    {
        return dialNumber + "/" + userEmail + "/" + currentCommand +
            "/" + startTime + "/V" + version + " S= " + status +
            " Event=" + event;
    }

    /**
     * Get persistent storage for this info.
     * @return DelayInfo where info from this is packed.
     */
    public DelayInfo getPersistentRepresentation()
    {
        String key = dialNumber + "," + userEmail;
        int byteSize = 9; // For version + starttime
        if (currentCommand != null) {
            byteSize += currentCommand.getPackSize();
        }
        byte[] byteData = new byte[byteSize];
        byteData[0] = version;
        DelayInfo.packLong(byteData, 1, startTime);
        if (currentCommand != null) {
            currentCommand.pack(byteData, 9);
        }
        DelayInfo di = new DelayInfo(key, DELAY_TYPE_OUTDIAL, userDN, byteData);
        return di;
    }

    /**
     * Getter for property dialNumber.
     * @return Value of property dialNumber.
     */
    public java.lang.String getDialNumber()
    {
        return dialNumber;
    }

    /**
     * Getter for property userEmail.
     * @return Value of property userEmail.
     */
    public String getUserEmail()
    {
        return userEmail;
    }


    /**
     * Getter for property version.
     * @return Value of property version.
     */
    public byte getVersion()
    {
        return version;
    }


    /**
     * Getter for property userDN.
     * @return Value of property userDN.
     */
    public java.lang.String getUserDN()
    {
        return userDN;
    }


    /**
     * Getter for property startTime.
     * @return Value of property startTime.
     */
    public long getStartTime()
    {
        return startTime;
    }


    /**
     * Getter for property currentCommand.
     * @return Value of property currentCommand.
     */
    public Command getCurrentCommand()
    {
        return currentCommand;
    }

    /**
     * Set a new command.
     * @param newCommand Command to become current.
     */
    public void setCurrentCommand(Command newCommand)
    {
        currentCommand = newCommand;
    }

    /**
     * Getter for property event.
     * @return Value of property event.
     */
    public com.mobeon.common.storedelay.DelayEvent getEvent()
    {
        return event;
    }

    /**
     * Setter for property event.
     * @param event New value of property event.
     */
    public void setEvent(com.mobeon.common.storedelay.DelayEvent event)
    {
        this.event = event;
    }

    /**
     * Getter for property status.
     * @return Value of property status.
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * Setter for property status.
     * @param status New value of property status.
     */
    public void setStatus(int status)
    {
        this.status = status;
    }

}
