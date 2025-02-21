package com.mobeon.common.storedelay;

import java.util.Calendar;

// Copyright 2003 Swedish Connection Open Technology, All rights reserved

/**
 * Operations for Store and Delay Handling.
 * The public operations are defined in this interface and implemented
 * in a separate class.
 * <p />
 * Clients can schedule new information and reschedule or clean 
 * old information. If the information is time based the DelayHandler
 * will notify about the information when its time has come.
 * If the notification does not have a time the DelayHanlder will
 * notify about the information when the notify() method is called
 * with the informations keys.
 * <p />
 * The notifications are done to listeners. See {@link DelayListener}.
 * Each listener that
 * has been registred for an information type is notified when
 * the information is ready for notification. If not listener
 * is registered for some information the information is removed
 * at notification by the DelayHandler. If at least one listener
 * is notified then the information will <b>not</b> be removed
 * by the delayhandler, instead one of the listeners must remove
 * (or reschedule) the information by calling the appropriate 
 * methods.
 *
 */
public interface DelayHandler
{
   /**
    * Schedule a new delayinfo until a given time.
    * A delayinfo is regarded as new if a delay for the same user
    * and type is not already in progress. <br />
    * The delayer will do a best effort to deliver the information
    * to the appropriate listener as soon as possible after the wanted time.
    * Note that the scheduling might not be done immediately so the
    * result cannot be given to the caller. However, the listener will
    * be called to inform it that the scheduling went wrong.
    * @param when When the information should be sent to its listener.
    *        If the time has passed it is assumed that the informaton is
    *        wanted immediately.
    *        If when is null no time based event notification will be 
    *        done, to notify listeners about the info call the
    *        {@link #notify} method.
    * @param info The information to send
    */
   void schedule(Calendar when, DelayInfo info);

   /**
    * Rechedule an existing delayinfo until a given time. 
    * With delayinfo is regarded as existing if a delay for the same
    * user and type is already in progress. <br />
    * See also {@link #schedule(Calendar,DelayInfo)}.
    *        If the time has passed it is assumed that the informaton is
    *        wanted immediately.
    *        If when is null no time based event notification will be 
    *        done, to notify listeners about the info call the
    *        {@link #notify} method.
    * @param info The information to send
    */
   void reschedule(Calendar when, DelayInfo info);
   
   /**
    * Rescheduling an existing delayinfo for a number of seconds.
    * See also {@link #reschedule(Calendar, DelayInfo)}.
    * This method is only usable for time based notifications
    * @param waitSeconds Number of seconds to delay
    * @param info The information to send.
    */
   void reschedule(int waitSeconds, DelayInfo info);


   /**
    * Notify for the given key and type.
    * When this is called all listeners for the type will
    * be told to handle the DelayInfo with the given key and type.
    * They will be sent {@link DelayListener#HANDLE_STATUS_NOTIFY} in
    * the call to {@link DelayListener#handle}.
    * @param key String key for the DelayInfo to notify on
    * @param type Type of DelayInfo.
    * @param event Extra information about the notification, can be null.
    *    It is the individual listener that determines how the event 
    *    should be interpreted.
    */
   void notifyEvent(String key, short type, DelayEvent event);
   
   
   /**
    * Removes the delayinfo for a given user and type.
    * The info with the the given user id and type is removed.
    * No notification will be sent for that info.
    * @param key Identity of DelayInfo
    * @param type Type of information
    */
   void cleanInfo(String key, short type);


    /**
     * Retrieves a delayinfo for the speciefied key and type
      * @param key Identify of DelayInfo
     * @param type Type of Information
     * @return the found delayinfo or null if no exists
     */
   DelayInfo getInfo(String key, short type);

   /**
    * Register a listener for a given type.
    * Several listeners can listen for one type, all of them
    * will be called but the order is not defined.
    * It is the listeners responsibility to return control
    * in a "reasonable" time and move work to another thread if
    * it will be long running (i.e. doing an operation over a network).
    * @param wantType The type the listener is interested in
    * @param listener The listener to report to.
    */
   void registerInterest(short wantType, DelayListener listener);

   /**
    * Stop listening to events for a given type.
    * The listener will no longer be registred.
    * @param wantType The type the listener wants to stop listening to.
    * @param listener The listener to stop reporting to
    */
   void unregisterInterest(short wantType, DelayListener listener);

   /**
    * Call this when all initial listeners are registred.
    * When this is called the delayhandler will notify listeners
    * about found old events.
    */
   void registeringDone();

}
