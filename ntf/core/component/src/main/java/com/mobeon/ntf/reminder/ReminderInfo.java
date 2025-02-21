package com.mobeon.ntf.reminder;

import com.mobeon.common.storedelay.DelayInfo;
import com.mobeon.ntf.mail.NotificationEmail;

/**
 * Holds information about one active outdial.
 */
public class ReminderInfo
{

    /** Type used in delay to identify outdial notifications. */
    public static final short DELAY_TYPE_REMINDER = 3;


    // Persistent info
    private byte version;
    private String userEmail;
    private long startTime;
    private String userDN;
    private short retryAttempts;


    private int status;

    private NotificationEmail userNotifEmail;

    public ReminderInfo(NotificationEmail userEmail, long startTime, String userDN)
    {
    	this.version = 1;
    	this.userNotifEmail = userEmail;
    	this.startTime = startTime;
    	this.userDN = userDN;
    	retryAttempts = 0;
    }

    public NotificationEmail getUserNotifEmail() {
    	return userNotifEmail;
    }
    /**
     * Create a new ReminderInfo.
     * @param userEmail Email of user we are doing the outdial for
     * @param startTime Time the outdial started, milliseconds since 1970
     * @param userDN Identity for user in MUR.
     */
    public ReminderInfo(String userEmail,
                        long startTime, String userDN)
    {
        this.version = 1;
        this.userEmail = userEmail;
        this.startTime = startTime;
        this.userDN = userDN;
        retryAttempts = 0;
    }

    /**
     * Create OdlInfo from persistent storage.
     * @param di DelayInfo that has been stored.
     */
    public ReminderInfo(DelayInfo di)
    {
        userEmail = di.getKey();

        userDN = di.getStrInfo();
        byte[] byteData = di.getByteInfo();
        version = byteData[0];
        startTime = DelayInfo.unpackLong(byteData, 1);
        setRetryAttempts(DelayInfo.unpackShort(byteData, 9));

    }


    /**
     * Get string respresentation for logging/debugging.
     * @return Descriptive string
     */
    public String toString()
    {
        return userEmail + "/"  + startTime + "/V"
                + version + " S= " + status +
            " RetryAttempts=" + getRetryAttempts();
    }

    /**
     * Get persistent storage for this info.
     * @return DelayInfo where info from this is packed.
     */
    public DelayInfo getPersistentRepresentation()
    {
        String key = userEmail;
        int byteSize = 11; // For version + starttime + retryattempts

        byte[] byteData = new byte[byteSize];
        byteData[0] = version;
        DelayInfo.packLong(byteData, 1, startTime);
        DelayInfo.packShort(byteData, 9, getRetryAttempts());

        DelayInfo di = new DelayInfo(key, ReminderInfo.DELAY_TYPE_REMINDER, userDN, byteData);
        return di;
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
    public String getUserDN()
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

    public short getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(short retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public void incRetryAttempts() {
        retryAttempts++;
    }
}
