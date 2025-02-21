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
 * This class handles deferred commands for Call Forward Unconditional on.
 * The command is either to turn off CFU or to send a
 * SMS reminder that CFU is still on.
 */
public class DeferredCFUCmdListener extends DeferredListener
{

    /**
     * Creates a new instance of DeferredCFUCmdListener.
     * @param id Identity of the listener
     */
    public DeferredCFUCmdListener(String id)
    {
        super(id);
    }


    /**
     * Handle the reminder / auto turn off of CFY
     * @param delayer We report here when done
     * @param info Information about the action
     * @status Status of the notification
     * @event In case of a notify, not used.
     */
    protected void doWork(DelayHandler delayer, DelayInfo info)
    {
        try {
            DeferredInfo deferredInfo = new DeferredInfo(info);
            SDLogger.logObject(SDLogger.DEBUG, "CFU cmd for : ", deferredInfo);
            UserInfo userInfo = getUserInfo(deferredInfo.getReceiver());
            SDLogger.logObject(SDLogger.DEBUG, "** UserInfo **", userInfo);
            if (userInfo == null) {
                // No such user
                SDLogger.logObject(SDLogger.WARNING,
                                   "CFUCmd: User not found, no reminder or reset done",
                                   info);
                delayer.cleanInfo(info.getKey(), info.getType());
                return;
            }

            ExternalSubscriberInformation  currentESI =  userInfo.getExternalSubscriberInformation();
            if ((currentESI == null) || hasPermanentErrors(currentESI)) {
                // TODO: Report to MER ??
                SDLogger.logObject(SDLogger.ERROR,
                                   "Could not handle CFU command, permanent ESI error",
                                   deferredInfo);
                delayer.cleanInfo(info.getKey(), info.getType());
                return;
            } else if (hasTemporaryErrors(currentESI)) {
                SDLogger.logObject(SDLogger.INFO, "CFU: Temporary Error, resheduling for later",
                                   deferredInfo);
                delayer.reschedule(getWaitTimeOnTempError(), info);
                return;
            } else {
                // We got the information, only reset if CFU is still on
                // and directs to the system
                SDLogger.logObject(SDLogger.INFO,"** Current ESI OK**", currentESI);
                if (isCFUOn(currentESI, deferredInfo)) {
                    if (deferredInfo.getAction() == DeferredInfo.ACTION_REMINDER) {
                        String smsTemplate = getReminderTemplate();
                        sendSMS(deferredInfo.getReceiver(), smsTemplate);
                    } else if (deferredInfo.getAction() ==
                               DeferredInfo.ACTION_AUTO_OFF)  {
                        SDLogger.log(SDLogger.DEBUG, "CFU Listener: AutoOn");
                        ExternalSubscriberInformation  resetResult = resetCFU(userInfo);
                        if ((resetResult == null) || hasPermanentErrors(resetResult)) {
                             // TODO: Report to MER ??
                             SDLogger.logObject(
                                   SDLogger.ERROR,
                                   "Could not reset CFU, permanent ESI error",
                                   deferredInfo);
                        } else if (hasTemporaryErrors(resetResult)) {
                            SDLogger.logObject(SDLogger.INFO,
                                               "Could not remove CFU, temporary error, try later",
                                               deferredInfo);
                            delayer.reschedule(getWaitTimeOnTempError(), info);
                            return;
                        } else {
                            // TODO: MER Event??
                            SDLogger.logObject(SDLogger.DEBUG,
                                               "Removed CFU for user",
                                               deferredInfo);
                        }

                    }  else {

                        SDLogger.logObject(SDLogger.ERROR,
                                           "Unknown CFU command, cannot handle",
                                           deferredInfo);

                    }
                } else {
                    SDLogger.logObject(SDLogger.DEBUG,
                                       "Nothing done since CFU already off",
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


    protected boolean hasPermanentErrors(ExternalSubscriberInformation externalSubInfo)
    {
        return externalSubInfo.getStatus() == 5;
    }

    protected boolean hasTemporaryErrors(ExternalSubscriberInformation  externalSubInfo)
    {
        return externalSubInfo.getStatus() == 4;
    }

    /**
     * Check if Call Forward Unconditional is on and set to the system.
     */
    protected boolean isCFUOn(ExternalSubscriberInformation currentESI, DeferredInfo deferredInfo)
    {

        if (deferredInfo.hasForwardUnconditional()) {
            String currUncond = currentESI.getCfUnconditional();
            if (currUncond == null) return false;
            return currUncond.equals(deferredInfo.getForwardingNumber());
        } else {
            return false;
        }
    }

    /**
     * Reset CFU for user.
     * That is, delete call forward unconditional.
     */
    protected ExternalSubscriberInformation resetCFU(UserInfo userInfo)
    {

        EsiOut esiOut = EsiOut.get();
        ExternalSubscriberInformation esInfo =
                           esiOut.deleteEsiData(userInfo.getNotifNumber(),
                                                false, false,false,true);
        return esInfo;
    }

    /**
     * Get name of SMS template to use for reminders.
     */
    protected String getReminderTemplate()
    {
        // TODO: Look up in config
        return "cfuonreminder";
    }

}
