/*
 * DeferredHandler.java
 *
 * Created on den 13 september 2004, 17:32
 */

package com.mobeon.ntf.deferred;

import com.mobeon.common.storedelay.DelayHandler;
import com.mobeon.common.storedelay.DelayInfo;
import com.mobeon.common.storedelay.SDLogger;

import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.NtfCompletedListener;

import java.util.Calendar;

/**
 *
 * @author  QMIER
 */
public class DeferredCmdHandler {

    private DelayHandler delayer;
    private DeferredListener voicemailCmdListener;
    private DeferredListener cfuCmdListener;
    private DeferredListener tempgreetCmdListener;


    /** Creates a new instance of DeferredHandler */
    public DeferredCmdHandler(DelayHandler delayerToUse)
    {
        // TODO: Add "real" listeners
        // TODO: Use Constants
        delayer = delayerToUse;
        voicemailCmdListener =
          new DeferredVoiceMailCmdListener("Deferred VoiceMail");
        cfuCmdListener       =
          new DeferredCFUCmdListener("Deferred CFU");
        tempgreetCmdListener =
          new DeferredTempGreetCmdListener("Deferred TempGreeting");


        voicemailCmdListener.setDaemon(true);
        cfuCmdListener.setDaemon(true);
        tempgreetCmdListener.setDaemon(true);

        voicemailCmdListener.start();
        cfuCmdListener.start();
        tempgreetCmdListener.start();

        delayer.registerInterest((short)5,
                                voicemailCmdListener);
        delayer.registerInterest((short)6,
                                 cfuCmdListener);
        delayer.registerInterest((short)7,
                                tempgreetCmdListener);

        delayer.registeringDone();
    }



    /**
     * Take care of an email
     */
    public void handleDeferredCommand(NotificationEmail email,
                                      NtfCompletedListener cleaner)
        throws DeferredException
    {
        // Note: To be even more sure that a command has been
        // saved in DB before cleaning from MailStore we coul
        // schedule it without a delay, then notify.
        // The handler whould then do the cleaning when it
        // gets a notify event and after that reschedule with the
        // wanted time (the time would have to come in the event).
        // This would need an extra DB access for every delayed message
        // so it is not currently done.
        //

        DeferredInfo deferredInfo = new DeferredInfo(email);
        SDLogger.log(SDLogger.DEBUG, "Got deferedinfo with action : " + deferredInfo.getAction());
        if (deferredInfo.getAction() == 0) {
            // No action given, this is a cancel of any pending commmands
            SDLogger.logObject(SDLogger.DEBUG,
                               "Cleaning info for" , deferredInfo);
            delayer.cleanInfo(deferredInfo.getReceiver(),
                              (short)deferredInfo.getMailType());
            cleaner.notifCompleted(email.getNtfEvent());
        } else {
            DelayInfo delayInfo = deferredInfo.getPersistentRepresentation();

            SDLogger.logObject(SDLogger.DEBUG,
                               "Scheduling for deferedinfo", deferredInfo);
            Calendar when = deferredInfo.getDeferredTime();
            delayer.schedule(when, delayInfo);
            cleaner.notifCompleted(email.getNtfEvent());
        }

    }


}
