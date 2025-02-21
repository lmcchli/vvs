
package com.mobeon.ntf.deferred;

import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.Constants;
import com.mobeon.common.storedelay.DelayInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.StringTokenizer;

/**
 * Information about a deferred command.
 * The command might be to send a reminder SMS or to turn/on off
 * some subscriber status. Objects of this class are initiated
 * from the original email that contained the command. The objects
 * have operations to store and restor in to DelayInfo objects
 * so that the command can be scheduled.
 * This class is mainly a data store, no command functionality
 * is performed by it.
 */
public class DeferredInfo {
    private static final String NOTIF_VOICEMAILOFF   = "voicemailoff";
    private static final String NOTIF_CFUON          = "cfuon";
    private static final String NOTIF_TEMPGREETON    = "temporarygreetingon";

    private static final String FORWARD_BUSY_STR      = "cf-busy";
    private static final String FORWARD_NOREPLY_STR   = "cf-no-reply";
    private static final String FORWARD_NOTREACH_STR  = "cf-not-reachable";
    private static final String FORWARD_UNCOND_STR    = "cf-unconditional";

    // Masks for bitset
    private static final int    FORWARD_BUSY         = 1;
    private static final int    FORWARD_NOREPLY      = 2;
    private static final int    FORWARD_NOTREACH     = 4;
    private static final int    FORWARD_UNCOND       = 8;

    // Possible Actions
    public static final int     ACTION_UNKNOWN       = -1;
    public static final int     ACTION_NONE          = 0;
    public static final int     ACTION_AUTO_ON       = 1;
    public static final int     ACTION_AUTO_OFF      = 2;
    public static final int     ACTION_REMINDER      = 3;



    private String receiver;
    private Calendar deferredTime;
    private byte version;
    private int mailType;
    private int action;
    private String forwardingNumber;
    private int applicableForwards; // Bitset

    /**
     * Create from an email.
     * The mail must be a deferred mail (I.e, have the
     * X-Ipms-Deferred-Delivery header.
     * @param email The mail to create from
     * @throws DeferredException if the mail was not a deferred mail
     *  or if there was problems with getting data from the mail.
     */
    public DeferredInfo(NotificationEmail email)
    throws DeferredException
    {
        version = (byte)1;
        String thisReceiver = email.getReceiver();
        if (!thisReceiver.startsWith("notification.off@")) {
        	receiver = thisReceiver;
        }
        if (receiver == null) {
            throw new DeferredException("Could not get receiver");
        }

        mailType = email.getEmailType();
        //Date deferredDate = email.getDeferredDeliveryDate();
        Date deferredDate = new Date();
        deferredTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        deferredTime.setTime(deferredDate);

        try {
            String msgText = email.getMessageText();
            updateFromMessageText(msgText);
       } catch (MsgStoreException mse) {
           throw new DeferredException("MsgStoreException " + mse);
       }


        // If CFU should cfu belongs in our forwards??
        if (mailType == Constants.NTF_DEFERRED_CFU) {
            applicableForwards |= FORWARD_UNCOND;
        }
    }



    /**
     * Create from a DelayInfo.
     * The info must have same format as one created by this class.
     * See {@link getPersistenyRepresentation}.
     * @param info The delayinfo to initiate from.
     * @throws DeferredException If all needed info could not be extracted
     *         from the DelayInfo.
     */
    public DeferredInfo(DelayInfo info)
    throws DeferredException
    {
        receiver = info.getKey();
        mailType = info.getType();
        forwardingNumber = info.getStrInfo();
        byte[] byteInfo = info.getByteInfo();
        int pos = 0;
        if (byteInfo.length == 8) {
            version = (byte)0;
        } else {
            version = byteInfo[0];
            pos++;
        }
        action = DelayInfo.unpackInt(byteInfo , pos);
        applicableForwards = DelayInfo.unpackInt(byteInfo, pos+4);
        deferredTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        deferredTime.setTime(new Date(info.getWantTime()));
    }


    /**
     * Make string representation, used for testing.
     */
    public String toString()
    {
        return receiver + "/" + mailType + "/" + forwardingNumber +
               "/" + action + "/" + applicableForwards +
               "V" + version + "/" + deferredTime.getTime().toString() + ";";
    }


    /**
     * Save into a DelayInfo.
     * The save format is; The receiver is key and mailType used
     * as type. The forwarding number is the stringData and action
     * and applicableForwards are packed into the byte data.
     * The deferred time is stored into the wanted time.
     */
    public DelayInfo getPersistentRepresentation()
    {
        int size = 1 + 2 * DelayInfo.intPackSize();
        byte[] byteInfo = new byte[size];
        byteInfo[0] = version;
        int pos = 1;
        pos = pos + DelayInfo.packInt(byteInfo, pos, action);
        DelayInfo.packInt(byteInfo, pos, applicableForwards);

        DelayInfo di = new DelayInfo(receiver, (short)mailType,
                                     forwardingNumber,byteInfo);
        di.setWantTime(deferredTime.getTime().getTime());
        return di;
    }

    /**
     * Get scheduled time, the time is taken from original email.
     */
    public Calendar getDeferredTime()
    {
        return deferredTime;
    }


    /**
     * Get receiver of the mail, ie. the subscriber that requested the command.
     */
    public String getReceiver()
    {
        return receiver;
    }

    /**
     * Get version number for this info.
     */
    public byte getVersion()
    {
        return version;
    }


    /**
     * Return type of mail as defined in Constants.
     * The type is the same as the type of the original email.
     */
    public int getMailType() {
        return mailType;
    }

    /**
     * Get the wanted action.
     */
    public int getAction()
    {
        return action;
    }

    /**
     * Get forwarding number to use.
     * The number is either used to set in the HLR or to compare current
     * settings with.
     * @return The forwardingnumber
     */
    public String getForwardingNumber()
    {
        return forwardingNumber;
    }

    /**
     * Tell if busy is among the affected forwards
     */
    public boolean hasForwardBusy()
    {
       return (applicableForwards & FORWARD_BUSY) == FORWARD_BUSY;
    }

    public boolean hasForwardNotReachable()
    {
        return (applicableForwards & FORWARD_NOTREACH) == FORWARD_NOTREACH;
    }

    public boolean hasForwardNoReply()
    {
        return (applicableForwards & FORWARD_NOREPLY) == FORWARD_NOREPLY;
    }

    public boolean hasForwardUnconditional()
    {
        return (applicableForwards & FORWARD_UNCOND) == FORWARD_UNCOND;
    }

   /**
     * Get data from mails message text.
     */
    private void updateFromMessageText(String message)
    {
        StringTokenizer st = new StringTokenizer(message, "\r\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            int equalPos = line.indexOf('=');
            if (equalPos < 0) continue; // No data on this line
            String key = line.substring(0,equalPos).trim();
            String info = "";
            if (equalPos < line.length()) {
                info = line.substring(equalPos+1).trim();
            }

            if (key.equals("action")) {
                action = findActionValue(info);
            } else if (key.equals("forwardingnumber")) {
                forwardingNumber = info;
            } else if (key.equals("unsetforwards")) {
                applicableForwards = findForwards(info);
            }
        }
    }

    /**
     * Convert a string with action to its numerical value.
     */
    private int findActionValue(String strAction)
    {
        if (strAction.equals("autoon")) {
            return ACTION_AUTO_ON;
        } else if (strAction.equals("autooff")) {
            return ACTION_AUTO_OFF;
        } else if (strAction.equals("reminder")) {
            return ACTION_REMINDER;
        } else if (strAction.equals("none") || strAction.equals("")) {
            return ACTION_NONE;
        } else {
            return ACTION_UNKNOWN;
        }
    }

    /**
     * Convert string with forwards to bitmap.
     */
    private int findForwards(String forwardsStr)
    {
        int forwards = 0;
        StringTokenizer st = new StringTokenizer(forwardsStr, ",");
        while (st.hasMoreTokens()) {
            String forward = st.nextToken();
            if (forward.equals(FORWARD_BUSY_STR)) {
                forwards |= FORWARD_BUSY;
            } else if (forward.equals(FORWARD_NOREPLY_STR)) {
                forwards |= FORWARD_NOREPLY;
            } else if (forward.equals(FORWARD_NOTREACH_STR)) {
                forwards |= FORWARD_NOTREACH;
            } else if (forward.equals(FORWARD_UNCOND_STR)) {
                forwards |= FORWARD_UNCOND;
            }
        }
        return forwards;
    }



}
