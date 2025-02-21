/*
 * DeferredVoiceMailCmdListener.java
 *
 * Created on den 14 september 2004, 09:47
 */

package com.mobeon.ntf.deferred;

import com.mobeon.common.storedelay.DelayHandler;
import com.mobeon.common.storedelay.DelayInfo;
import com.mobeon.common.storedelay.SDLogger;

import com.mobeon.ntf.userinfo.UserInfo;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;

/**
 * This class handles deferred commands for voice mail off.
 * The command is either to turn on voice mail or to send a
 * SMS reminder that voice mail is still off.
 */
public class DeferredTempGreetCmdListener extends DeferredListener
{
    /** For use if user has no defined time zone */
    private SimpleDateFormat endTimeFormat;
    /** For use when user has a defined time zone */
    private SimpleDateFormat endTimeFormatTimeZone;

    /**
     * Creates a new instance of DeferredVoiceMailCmdListener
     * @param id Identity of the listener
     */
    public DeferredTempGreetCmdListener(String id)
    {
        super(id);
        String baseFormat = "yyyy-MM-dd HH:mm:ss";
        endTimeFormat = new SimpleDateFormat(baseFormat);
        endTimeFormatTimeZone = new SimpleDateFormat(baseFormat + " z");
    }


    /**
     * Handle the reminder / auto turn on off voice mail.
     * @param delayer We report here when done
     * @param info Information about the action
     * @status Status of the notification
     * @event In case of a notify, not used.
     */
    protected void doWork(DelayHandler delayer, DelayInfo info)
    {
        try {
            DeferredInfo deferredInfo = new DeferredInfo(info);
            SDLogger.logObject(SDLogger.DEBUG,
                              "Temporary Greeting cmd for : ", deferredInfo);
            UserInfo userInfo = getUserInfo(deferredInfo.getReceiver());
            String tempGreeting = userInfo.getTemporaryGreeting();
            String timeZone = userInfo.getTimeZone();
            if (isTempGreetingOn(tempGreeting, timeZone)) {
                if (deferredInfo.getAction() == DeferredInfo.ACTION_REMINDER) {
                    String smsTemplate = getReminderTemplate();
                    sendSMS(deferredInfo.getReceiver(), smsTemplate);
                }  else {

                    SDLogger.logObject(
                       SDLogger.ERROR,
                       "Unknown temporary greeting command, cannot handle",
                       deferredInfo);

                }
            } else {
                SDLogger.logObject(
                    SDLogger.DEBUG,
                    "No reminder since temporary greeting is off now",
                    deferredInfo
                );
            }

                delayer.cleanInfo(info.getKey(), info.getType());
        } catch (Exception de) {
            SDLogger.log(SDLogger.ERROR,
                         "Could not convert to DeferredInfo",
                         de);
            // No use holding into the delayinfo
            delayer.cleanInfo(info.getKey(), info.getType());
        }
    }


    /**
     * Check if voicemail is off for turned off forwardings.
     */
    protected boolean isTempGreetingOn(String tempGreetAttribute,
                                       String timeZone)
    {
        SDLogger.logObject(SDLogger.DEBUG,"Check temp greeting on",tempGreetAttribute);
        if (tempGreetAttribute == null) return false;
        tempGreetAttribute = tempGreetAttribute.trim();
        if (tempGreetAttribute.length() == 0) return false;
        int separatorIndex = tempGreetAttribute.indexOf(';');
        if (separatorIndex < 0) {
            // Not found, strange
            SDLogger.logObject(
                SDLogger.WARNING,
                "Separator (;) not found in temp greet attribute " +
                "assuming temp greeting is on. Attribute = ",
                tempGreetAttribute
            );
            // Assume temp greeting on
            return true;
        }
        if (separatorIndex == tempGreetAttribute.length() -1) {
            // Nothing after the separator, no time limit
            return true;
        }
        String timeStr = tempGreetAttribute.substring(separatorIndex+1);
        timeStr = timeStr.trim();
        try {
            SimpleDateFormat useFormat = endTimeFormat;
            if ((timeZone != null) && (timeZone.length() > 0)) {
                timeStr += " " + timeZone;
                useFormat = endTimeFormatTimeZone;
            }
            SDLogger.log(SDLogger.DEBUG, "Checking temp greeting with timestring : " + timeStr);
            Date endDate = useFormat.parse(timeStr, new ParsePosition(0));
            Calendar now = Calendar.getInstance();
            // Temp greeting is on if we have not passed enddate
            return now.getTime().getTime() < endDate.getTime();
        } catch (Exception e) {
            SDLogger.logObject(
                SDLogger.WARNING,
                "Could not parse time string, assuming tempgreet=on",
                timeStr
            );
            return true;
        }
    }



    /**
     * Get name of SMS template to use for reminders.
     */
    protected String getReminderTemplate()
    {
        // TODO: Look up in config
        return "temporarygreetingonreminder";
    }

}
