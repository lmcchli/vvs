package com.mobeon.ntf.reminder;

import java.util.concurrent.TimeUnit;

import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserLogins;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.util.threads.NtfThread;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.common.storedelay.SDLogger;
import com.mobeon.common.storedelay.DelayHandler;
import com.mobeon.common.storedelay.DelayInfo;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-apr-02
 * Time: 15:09:36
 * To change this template use File | Settings | File Templates.
 */
public class ReminderWorker extends NtfThread implements Constants {
    private DelayHandler delayer;
    private ManagedArrayBlockingQueue<Object> queue;
    private MerAgent mer;
    private ReminderCaller caller;

    public ReminderWorker(DelayHandler delayer,
                          ManagedArrayBlockingQueue<Object> queue,
                          String threadName,
                          ReminderCaller caller)
    {
        super(threadName);
        this.delayer = delayer;
        this.queue = queue;
        this.mer = MerAgent.get();
        this.caller = caller;
    }

    @Override
    /**
     * The shutdown loop stops after the ntfRun method is finished.
     *
     * @return true always (i.e. this thread has not shutdown activity)
     */
    public boolean shutdown() {
        if (isInterrupted()) {
            return true;
        } //exit immediately if interrupted..

        if (queue.size() == 0)
        {
                //give a short time for new items to be queued in workers, to allow other threads to empty there queues.
                if (queue.isIdle(2,TimeUnit.SECONDS)) {
                    return true;
                }
                else
                {
                    if (queue.waitNotEmpty(2, TimeUnit.SECONDS)) {
                        return(ntfRun());
                    } else
                    {
                        return true;
                    }

                }
        } else {
            return(ntfRun());
        }
    }

    /**
     * Do one step of the work.
     * @return False if the work should continue, true if the worker wants to stop.
     */
    public boolean ntfRun()
    {
        ReminderInfo info = null;
        UserInfo userInfo = null;
        try {
            Object obj = queue.take();
            if (obj == null) return false;
            info = (ReminderInfo) obj;
            SDLogger.logObject(SDLogger.DEBUG, "ReminderWork", info);
            userInfo = getUserInfo(info);
            if (userInfo == null) {
                SDLogger.logObject(SDLogger.ERROR,
                        "Reminder - No Userinfo found for reminderInfo, stops : ",
                        info);
                doClean(info);
                return false;
            }
            if (isTooOld(info)) {
                SDLogger.logObject(SDLogger.INFO, "Reminder is too old, removing", info);
                doClean(info);
            }  else if( UserLogins.isUserLoggedIn(userInfo.getTelephoneNumber() ) ) {
                SDLogger.logObject(SDLogger.DEBUG, "User is logged in, delaying reminder for 60 seconds", info);
                reschedule(info, 60);

            } else {
                // Ok or Old, anyway its a time based event
                handleTimeBased(info, userInfo);
            }
        }  catch (Exception e) {
            SDLogger.log(SDLogger.ERROR,
                    "Unexpected exception in ReminderWorker " + NtfUtil.stackTrace(e), e);
            if (info != null) {
                doClean(info);
            }

        } catch (OutOfMemoryError e) {
            try {
                ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                SDLogger.log(SDLogger.ERROR,
                        "NTF out of memory, shutting down..." + NtfUtil.stackTrace(e));
            } catch (OutOfMemoryError e2) {;} //ignore
        return true; //exit
        }
        return false; //continue
    }

    private void handleTimeBased(ReminderInfo info, UserInfo user) {
    	NotificationEmail notifEmail = info.getUserNotifEmail();
        UserMailbox inbox = notifEmail.getUserMailbox();

        if( inbox.getNewTotalCount() == 0 ) {
            SDLogger.log(SDLogger.DEBUG, "Removing reminder SMS since the user has no new messages");
            doClean(info);
            return;
        }
        caller.send(info, user, inbox);
        mer.reminderSmsSent(user.getMail());
        info.incRetryAttempts();
        if( isTooOld(info)) {
            SDLogger.log(SDLogger.DEBUG, "Reminder sent max number of times, removing it from the database ");
            doClean(info);
        } else {
            reschedule(info, 0);
        }

    }

    private boolean isTooOld(ReminderInfo info) {
        if( info.getRetryAttempts() >= Config.getUnreadMessageReminderMaxTimes() ) {
            return true;
        }
        return false;

    }

    /**
     * Remove this info from storage.
     * @param info The info to remove.
     */
    private void doClean(ReminderInfo info)
    {
        delayer.cleanInfo(info.getUserEmail(),
                ReminderInfo.DELAY_TYPE_REMINDER);
    }


    /**
     * Get userinfo for a Outdial.
     * @param info defines the outdial.
     * @return Userinfo object of user we are doing outdial for.
     */
    protected UserInfo getUserInfo(ReminderInfo info)
    {
        return UserFactory.readByDN(info.getUserDN());
    }

    private void reschedule(ReminderInfo info, int time ) {
        DelayInfo dInfo = info.getPersistentRepresentation();
        if( time == 0 ) {
            time = Config.getUnreadMessageReminderInterval();
        }
        delayer.reschedule(time, dInfo);
    }


    public void setCaller(ReminderCaller caller) {
        this.caller = caller;
    }
}
