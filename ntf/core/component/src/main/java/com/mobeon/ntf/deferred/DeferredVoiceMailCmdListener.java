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

import com.mobeon.ntf.userinfo.esi.EsiOut;
import com.mobeon.ntf.userinfo.esi.ExternalSubscriberInformation;

/**
 * This class handles deferred commands for voice mail off.
 * The command is either to turn on voice mail or to send a
 * SMS reminder that voice mail is still off.
 */
public class DeferredVoiceMailCmdListener extends DeferredListener
{

    /**
     * Creates a new instance of DeferredVoiceMailCmdListener
     * @param id Identity of the listener
     */
    public DeferredVoiceMailCmdListener(String id)
    {
        super(id);
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
            SDLogger.logObject(SDLogger.DEBUG, "** Voicemail cmd for : ", deferredInfo);
            UserInfo userInfo = getUserInfo(deferredInfo.getReceiver());
            SDLogger.logObject(SDLogger.TRACE, "** UserInfo **", userInfo);
            if (userInfo == null) {
                // No such user
                SDLogger.logObject(SDLogger.WARNING,
                                   "VoiceMailCmd: User not found, no reminder or reset done",
                                   info);
                delayer.cleanInfo(info.getKey(), info.getType());
                return;
            }
            ExternalSubscriberInformation currentESI = userInfo.getExternalSubscriberInformation();
            if ((currentESI == null) || hasPermanentErrors(currentESI)) {
                // TODO: Report to MER ??
                SDLogger.logObject(SDLogger.ERROR,
                                   "Could not handle voicemail command, permanent ESI error",
                                   deferredInfo);
                delayer.cleanInfo(info.getKey(), info.getType());
                return;
            } else if (hasTemporaryErrors(currentESI)) {
                SDLogger.log(SDLogger.INFO, "Temporary error, rescheduling for later");
                delayer.reschedule(getWaitTimeOnTempError(), info);
                return;
            } else {
                // We got the information, only reset/sms if still voicemail off
                // on relevant forwardings
                SDLogger.logObject(SDLogger.TRACE,"** Current ESI OK**",
                                   currentESI);
                if (isVoiceMailOff(currentESI, deferredInfo)) {
                    if (deferredInfo.getAction() == DeferredInfo.ACTION_REMINDER) {
                        String smsTemplate = getReminderTemplate();
                        sendSMS(deferredInfo.getReceiver(), smsTemplate);
                    } else if (deferredInfo.getAction() ==
                               DeferredInfo.ACTION_AUTO_ON)  {
                        SDLogger.log(SDLogger.DEBUG, "VoiceMailListener: AutoOn");
                        ExternalSubscriberInformation  resetResult =
                            resetVoiceMail(userInfo, deferredInfo);
                        if ((resetResult == null) || hasPermanentErrors(resetResult)) {
                             // TODO: Report to MER ??
                             SDLogger.logObject(
                                   SDLogger.ERROR,
                                   "Could not update forwardings, permanent ESI error",
                                   deferredInfo);
                        } else if (hasTemporaryErrors(resetResult)) {
                             SDLogger.logObject(
                                   SDLogger.INFO,
                                   "Could not update forwardings, temporary error, try later",
                                   deferredInfo);
                            delayer.reschedule(getWaitTimeOnTempError(), info);
                            return;
                        } else {
                            // TODO: MER Event??
                            SDLogger.logObject(SDLogger.DEBUG,
                                               "Reset voicemail status",
                                               deferredInfo);
                        }

                    }  else {

                        SDLogger.logObject(SDLogger.ERROR,
                                           "Unknown voicemail command, cannot handle",
                                           deferredInfo);

                    }
                } else {
                    SDLogger.logObject(SDLogger.DEBUG,
                                       "No reset/reminder for voicemail off it is already on",
                                       deferredInfo);
                }

                delayer.cleanInfo(info.getKey(), info.getType());
            }
        } catch (DeferredException de) {
            SDLogger.log(SDLogger.ERROR,
                         "Could not convert to DeferredInfo",
                         de);
            // No use holding into the delayinfo
            delayer.cleanInfo(info.getKey(), info.getType());
        } catch (RuntimeException rte) {
            SDLogger.log(SDLogger.ERROR,
                         "Unexpected problem",
                         rte);
            // No use holding into the delayinfo
            delayer.cleanInfo(info.getKey(), info.getType());
            throw rte;
        }
    }


    protected boolean hasPermanentErrors(ExternalSubscriberInformation  externalSubInfo)
    {
        return externalSubInfo.getStatus() == 5;
    }

    protected boolean hasTemporaryErrors(ExternalSubscriberInformation  externalSubInfo)
    {
        return externalSubInfo.getStatus() == 4;
    }

    /**
     * Check if voicemail is off for turned off forwardings.
     */
    protected boolean isVoiceMailOff(ExternalSubscriberInformation currentESI,
                                     DeferredInfo deferredInfo)
    {
        if (deferredInfo.hasForwardNotReachable()) {
            String currNotReachable = currentESI.getCfNotReachable();
            if (isForward(currNotReachable)) return false;
        }
        if (deferredInfo.hasForwardBusy()) {
            String currBusy = currentESI.getCfBusy();
            if (isForward(currBusy)) return false;
        }
        if (deferredInfo.hasForwardNoReply()) {
            String currNoReply = currentESI.getCfNoReply();
            if (isForward(currNoReply)) return false;
        }
        if (deferredInfo.hasForwardUnconditional()) {
            String currUncond = currentESI.getCfUnconditional();
            if (isForward(currUncond)) return false;
        }
        return true;
    }

    /**
     * Return true if a forwarding is on
     */
    protected boolean isForward(String forward)
    {
        if ((forward == null) || (forward.length() == 0)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Reset voicemail for user
     */
    protected ExternalSubscriberInformation resetVoiceMail(UserInfo userInfo, DeferredInfo deferredInfo)
    {
        String cfNotReach = null;
        String cfBusy     = null;
        String cfNoReply  = null;
        String cfUncond   = null;
        String forward    = deferredInfo.getForwardingNumber();


        if (deferredInfo.hasForwardNotReachable()) {
            cfNotReach = forward;
        }
        if (deferredInfo.hasForwardBusy()) {
            cfBusy = forward;
        }
        if (deferredInfo.hasForwardNoReply()) {
            cfNoReply = forward;
        }
        if (deferredInfo.hasForwardUnconditional()) {
            cfUncond = forward;
        }

        EsiOut esiOut = EsiOut.get();
        ExternalSubscriberInformation esInfo =
                esiOut.modifyEsiData(userInfo.getNotifNumber(),
                                  cfNotReach, cfBusy, cfNoReply,
                                  cfUncond);
        return esInfo;
    }

    /**
     * Get name of SMS template to use for reminders.
     */
    protected String getReminderTemplate()
    {
        return "voicemailoffreminder";
    }

}
