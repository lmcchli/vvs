/*
 * DelayListener.java
 *
 * Created on den 12 augusti 2004, 15:57
 */

package com.mobeon.common.storedelay;


/**
 * Listen for messages whose time has come.
 * A client that are wants to know when the time has come for a DelayInfo
 * of a given type must define a class that implements DelayListener and
 * register an object of that class with the DelayHandler that handles
 * the objects of interest.
 */
public interface DelayListener {

   // ------------------------------------------------------------------
   // Status Codes
   // If new status codes are needed codes that defines a successful
   // (in some meaning) delivery should have values > 0 and codes
   // that defines notification of errors should have values < 0.
   // ------------------------------------------------------------------

   /**
     * Status for a info notification based on current time.
    */
   public static final int HANDLE_STATUS_OK = 0;
   /**
    * Old messages sent at startup.
    * Status for a info sent since its time is before current
    * when system is starting up. This means that the notification might have
    * been done once before but that it did not get acknowledged.
    * It is the listeners responsibility to check the wanttime in the
    * DelayInfo to see if the information is still relevant.
    * Messages sent with this code can be both timed events and items
    * waiting for notifications, if it is a notification the Listener
    * problably wants to set up waiting for the notificaton and report
    * it when it occurs.
    */
   public static final int HANDLE_STATUS_RESTART = 1;

   /**
    * And old message was scheduled.
    * Status for an information that was ready for delivery as soon as
    * it was scheduled, i.e, the want time was earlier than the current
    * time. Note that a delayinfo that is ready for scheduling at once
    * still is stored persistently so it will have to be removed by
    *
    * the listener juast as with STATUS_OK and STATUS_RESTART.
    */
   public static final int HANDLE_STATUS_OLD = 2;


   /**
    * A message was notified.
    * The message either has no wanted time or the notificatin was
    * done before the wanted time.
    */
   public static final int HANDLE_STATUS_NOTIFY = 3;



   /**
    * Status for a info that could not be scheduled.
    * The reason
    * might be that the information was already scheduled.
    * If this status is given handle is called soon after scheduling.
    */
   public static final int HANDLE_STATUS_ERR_SCHEDULE = -1;
   /**
    * Status for a info that could not be rescheduled.
    * The reason might be that it was not already scheduled.
    * If this status is given handle is called soon after rescheduling.
    */
   public static final int HANDLE_STATUS_ERR_RESCHEDULE = -2;

   /**
    * Problem with notifying.
    * E.g. Data to be notified about could not be found.
    * The DelayInfo sent to the listener will be a placeholder
    * consisting of the key and type given when notifying.
    */
   public static final int HANDLE_STATUS_ERR_NOTIFY = -3;




   /**
    * React on a timeout for info.
    * If the action taken might take a longer time (e.g. wait for
    * a response from another system/component) it should do
    * the action in a separate thread to keep the delayerthread
    * from hanging.
    * When all handling of the info is done delayer.cleanInfo
    * should be called, if the info shall be scheduled for later
    * handling delayer.reschedule should be called.
    * @param delayer The DelayHandler that notifies the listener
    * @param info The information the listener was waiting for
    * @param status Status for this notification, one of the
    *         HANDLE_STATUS_* constants defined in this class.
    * @param event Extra informaiton for some notifications,
    *        notifications sent for a timeout will not contain
    *        any event.
    */
   public void handle(DelayHandler delayer,
                      DelayInfo info,
                      int status,
                      DelayEvent event);


   /**
    * Identity for this listener.
    * All listeners must return an identity string the string
    * must be unique amongs all listeners of the same class.
    * This is used to manage listener sets.
    */
   public String getListenerId();


}
