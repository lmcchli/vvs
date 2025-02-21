/*
 * DeferredListener.java
 *
 * Created on den 13 september 2004, 17:38
 */

package com.mobeon.ntf.deferred;


import com.mobeon.common.storedelay.DelayListener;
import com.mobeon.common.storedelay.DelayHandler;
import com.mobeon.common.storedelay.DelayInfo;
import com.mobeon.common.storedelay.DelayEvent;
import com.mobeon.common.storedelay.SDLogger;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.SmsFilterInfo;

import com.mobeon.ntf.out.sms.SMSOut;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Listener for deferred commands.
 * This class handles basic management for listening to the
 * Retrieved commands, subclasses must implement specific handling
 * for specific deferred command, see {@link doWork}.
 */
public class DeferredListener extends Thread
    implements DelayListener
{

    private String id;
    private List<WorkItem> workQueue = new LinkedList<WorkItem>();
    private SMSOut smsOut = null;

    /**
     * Creates a new instance of DeferredListener */
    public DeferredListener(String id)
    {
        super(id);
        this.id = id;
    }


    /**
     * Get a string that identifies this handler.
     * @returns Identifying string.
     */
    public String getListenerId()
    {
        return id;
    }


    /**
     * Basic handling of errors is done here.
     *
     */
    public void handle(DelayHandler delayer, DelayInfo info,
                       int status, DelayEvent event)
    {
        SDLogger.logObject(SDLogger.DEBUG, "Callback for info", info);
        WorkItem item = new WorkItem(delayer, info, status, event);
        addToWorkQueue(item);
    }


    protected void addToWorkQueue(WorkItem item)
    {
        synchronized (this) {
            workQueue.add(item);
            this.notifyAll();
        }
    }

    public boolean ntfRun()
    {
        // Get next item from queue, if we wait and find
        // no item we was interrupted so return for another
        // run if we get one.
        WorkItem item = null;
        synchronized (this) {
           if (workQueue.size() == 0) {
              try {
                  this.wait();
              } catch (InterruptedException ie) {
                  // Let management do its thing
                  return false;
              }
           }
           if (workQueue.size() > 0) {
               item = workQueue.get(0);
               workQueue.remove(0);
           }
        }
        if (item == null) {
            return false;
        }

        DelayHandler delayer = item.getDelayer();
        DelayInfo    info    = item.getInfo();
        int status           = item.getStatus();
        DelayEvent event     = item.getEvent();

        SDLogger.logObject(SDLogger.DEBUG, "Listener.ntfRun; Delayinfo:", info);

        if (item.getStatus() >= 0) {
            // One of the ok statuses
            doWork(delayer, info, event);
        } else if (status == DelayListener.HANDLE_STATUS_ERR_SCHEDULE) {
            handleScheduleError(delayer,info);
        } else if (status == DelayListener.HANDLE_STATUS_ERR_RESCHEDULE) {
            handleRescheduleError(info);
        } else if (status == DelayListener.HANDLE_STATUS_ERR_NOTIFY) {
            handleNotifyError(info);
        } else {
            handleUnknownCode(info,status,event);
        }
        return false;
    }


    /**
     * Print final message after NtfThreads run.
     */
    public void run()
    {
        try {
            while (true) {
                ntfRun();
            }
        } catch (OutOfMemoryError e) {
            try {
                    ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                    SDLogger.log(SDLogger.ERROR,
                            "NTF out of memory, shutting down..." + NtfUtil.stackTrace(e));
                } catch (OutOfMemoryError e2) {;} //ignore
            return;
        }
    }




    /**
     * Do needed work for a data item whose time has come.
     * The default implementation just report that its time
     * to work via the logger.
     */
    protected void doWork(DelayHandler delayer, DelayInfo info, DelayEvent event)
    {
        SDLogger.log(SDLogger.ERROR, "DefaultListener " + id +
                                     " Time to do work for : " + info);
        if (event != null) {
            SDLogger.log(SDLogger.ERROR, " Event : " + event);
        }
        // This has been handled, in a way...
        delayer.cleanInfo(info.getKey(), info.getType());
    }

    /**
     * Handle when a schedule error is found.
     * Default action is to try to reschedule in case the problem
     * was due to dublicate keys.
     */
    protected void handleScheduleError(DelayHandler delayer, DelayInfo info)
    {
        // Scheduling went wrong, assume that it was since the
        // data already existed wo what we really should do was
        // a rescheduling
        Calendar when = Calendar.getInstance();
        when.setTime(new Date(info.getWantTime()));
        delayer.reschedule(when, info);
    }


    /**
     * Handle when a reschedule error is detected.
     * Default action is log the error.
     */
    protected void handleRescheduleError(DelayInfo info)
    {
       SDLogger.log(SDLogger.ERROR, "Could not (re)schedule " + info);
    }

    /**
     * Handle when a notify error is detected.
     * Default action is log the error.
     */
    protected void handleNotifyError(DelayInfo info)
    {
       SDLogger.log(SDLogger.ERROR, "Could not notify for " + info);
    }


    /**
     * Handle when an unkown error is detected.
     * Default action is log the error.
     */
    protected void handleUnknownCode(DelayInfo info, int status, DelayEvent event)
    {
       SDLogger.log(SDLogger.ERROR, "Unknown error code :  " + status  +
                    " for info : " + info + " Event : " + event);
    }

    /**
     * How long to wait until next attempt if temporary communication problem.
     */
    protected int getWaitTimeOnTempError()
    {
        return 300; // TODO: Configurable
    }

    /**
     * Send a sms.
     * The SMS is sent to the user defined by emailaddress using
     * the given template name.
     * @param email Email to user
     * @param template Template for the sms
     */
    protected void sendSMS(String email, String template)
    {
        try {
            SDLogger.log(SDLogger.INFO,
                         "*** Going to send SMS to " + email +
                         " with template " + template);
            if (smsOut == null) smsOut = SMSOut.get();
            Properties smsProps = new Properties();
            smsProps.setProperty("SMS", template);
            UserInfo userInfo = UserFactory.findUserByMail(email);
            if (userInfo != null) {
                String smsNumber = userInfo.getNotifNumber();
                String smsNumbers[] = new String[] {smsNumber};
                SmsFilterInfo filterInfo =
                    new SmsFilterInfo(smsProps, smsNumbers,null);
                smsOut.handleSMS(userInfo, filterInfo, null, null, null, Config.getSourceAddress(template, userInfo.getCosName()),
                		         userInfo.getValidity(template), 0);
                MerAgent mer = MerAgent.get();
                mer.systemReminderDelivered(userInfo.getMail(), Constants.NTF_SMS);
             }
        } catch (Throwable t) {
            SDLogger.log(SDLogger.ERROR,"SMS Sending problem",t);
        }
    }


   /**
    * Get the UserInfo for the actual user.
    * @return UserInfo, null if the user info could not be
    *     retreived
    */
   protected UserInfo getUserInfo(String email) {
       try {
         UserInfo userInfo = UserFactory.findUserByMail(email);
         return userInfo;
       } catch (Throwable t) {
           SDLogger.log(SDLogger.ERROR, "Could not get userinfo for " + email,t);
           return null;
       }
   }

}
