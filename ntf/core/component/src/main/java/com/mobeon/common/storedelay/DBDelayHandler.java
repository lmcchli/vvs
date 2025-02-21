/*
 * DBDelayHandler.java
 *
 * Created on den 12 augusti 2004, 12:14
 */
package com.mobeon.common.storedelay;

import java.util.Calendar;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Random;

import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.util.threads.NtfThread;
import com.mobeon.ntf.util.NtfUtil;


/**
 * Handles notifications that can wait for a time or for an event.
 * This class gives access to a default delayer but it is also possible
 * to create new delayer objects with their own configuratin and their
 * own threads.
 *
 * Each delayer accept new data to delay and allows registering of listeners
 * to be notified when the delay is over. The delayed data is persistent so
 * after a restart old reminders is still valid, the listernes must reregister
 * at a restart.
 * The DelayHandler thread must be started by the client to run, the starting
 * should be done after all listeners has registered themselves.
 */
public class DBDelayHandler extends NtfThread implements DelayHandler {
    /** The handler that normally all clients should use */
    private static DBDelayHandler defaultDelayer;

    /** The listeners we are notifying;  Map Long -> Collection(Listener)  */
    private Map<Short, Collection<DelayListener>> listeners  = new HashMap<Short, Collection<DelayListener>>();
    /** Registerinterest add to this map, later on we move to listeners */
    private Map<Short, Collection<DelayListener>> wantedListeners = new HashMap<Short, Collection<DelayListener>>();
    /** Unregisterinterest add to this map. Later on we remove them from listeners */
    private Map<Short, Collection<DelayListener>> wantedRemovedListeners = new HashMap<Short, Collection<DelayListener>>();

    /** DelayInfos to be cleaned */
    private List<DelayInfo> wantedCleanings  = new LinkedList<DelayInfo>();
    /** DelayInfos to be Scheduled  */
    private List<DelayInfo> wantedSchedulings = new LinkedList<DelayInfo>();
    /** DelayInfos to be Rescheduled */
    private List<DelayInfo> wantedReschedulings = new LinkedList<DelayInfo>();

    /** Notifications waiting to be sent. Array[DelayInfo, DelayEvent] */
    private List<Object[]> wantedEventNotifications = new LinkedList<Object[]>();

    // Temporary lists, used to save cleanings etc. when start
    // of event handling
    private List<DelayInfo> savedCleanings = null;
    private List<DelayInfo> savedSchedulings = null;
    private List<DelayInfo> savedReschedulings = null;
    private List<Object[]> savedEventNotifications = null;

    /** Soon to be sent DelayInfo, sorted on wantedtime  */
    private SortedSet<DelayInfo> activeSorted = new TreeSet<DelayInfo>(new DelayInfoComparator());

    /** Soon to be sent DelayInfo, organized for finding based on key+type
     * The map is DelayInfo->DelayInfo, note that you only need a DelayInfo
     * with key and type filled in to find the DelayInfo that is completely
     * filled in. <b>Note</b> All DelayInfo in this should also be in
     * activeSorted and the other way around!
     *
     */
    private Map<DelayInfo, DelayInfo>       activeMap = new HashMap<DelayInfo, DelayInfo>();

    /** Our connection to the database */
    private DelayInfoDAO diDAO;

    /** Know if we are first time in run loop or not */
    private boolean firstTime = true;
    /** Timestamp that we have read new notifications for */
    private long lastKnownNotification = 0;

    /** To get random small time for event notifications */
    private Random generator = new Random();

    /**
     * Get the default delayer, create if it does not already exists.
     */
    public static DBDelayHandler get()
        throws DelayException
    {
        if (defaultDelayer == null) {
            synchronized (DBDelayHandler.class) {
                // Inner if in case another tread managed to create
                if (defaultDelayer == null) {
                    defaultDelayer = new DBDelayHandler(System.getProperties());
                }
            }
        }
        return defaultDelayer;
    }

    public static String KEY_STORAGE_DIR = "delaystoragedir";
    public static String KEY_STORAGE_BASE = "delaystoragebase";

    private static final String DEFAULT_STORAGE_DIR = "/var/ntf/delaydb";
    private static final String DEFAULT_STORAGE_BASE = "delay";

    /**
     * Creates an instance configured by the given properties.
     * The used properties are:
     * <ul>
     * <li>delaystoragedir - Directory where persistent data is stored,
     *                    default = /delaydb
     * <li>delaystoragebase - Identifies one of several data stores, needed
     *                    if several delayers are used with the same directory.
     *                    default = delay
     * </ul>
     */
    public DBDelayHandler(Properties props)
        throws DelayException
    {
        super("DBDelayHandler");
        String storageDir  = props.getProperty(KEY_STORAGE_DIR, DEFAULT_STORAGE_DIR);
        String storageBase = props.getProperty(KEY_STORAGE_BASE, DEFAULT_STORAGE_BASE);
        init(storageDir, storageBase);
    }

    /**
     * Creates an instance configured by the given parameters.
     *
     * @param storagedir Directory where persistent data is stored
     * @param base Name of datastore in directory
     */
    public DBDelayHandler(String storagedir, String base)
      throws DelayException
    {
        super("DBDelayHandler");
        init(storagedir, base);
    }

    private void init(String storagedir, String base)
        throws DelayException
    {
        SDLogger.log(SDLogger.DEBUG,
            "DBDelayHandler/NtfThread: Init with: " + storagedir + "/" + base);
        diDAO = new DelayInfoDAO(storagedir, base);
        // Remove old from DB when startup
        Calendar now = Calendar.getInstance();
        // TODO: Make definition of old info configurable
        now.add(Calendar.WEEK_OF_YEAR, -1);
        diDAO.removeOlderThan(now.getTime().getTime());
    }



    // -----------------------------------------------------------
    // Scheduling and notifying
    // -------------------------------------------------------------

    // Doc comment in interface
    public synchronized void schedule(Calendar when, DelayInfo info) {
        if (when != null) {
            info.setWantTime(when.getTime().getTime());
        } else {
            info.setWantTime(generator.nextInt(DelayInfoDAO.MAX_MS_FOR_NOTIFICATIONS));
        }
        wantedSchedulings.add(info);
        if (SDLogger.willLog(SDLogger.TRACE)) {
            SDLogger.log(SDLogger.TRACE, "Request scheduling DI: " + info);
        }
        this.notifyAll();
    }

    // Doc comment in interface
    public synchronized void reschedule(int waitSeconds, DelayInfo info) {
        Calendar wantTime = Calendar.getInstance();
        wantTime.add(Calendar.SECOND, waitSeconds);
        reschedule(wantTime, info);
    }

    // Doc comment in superclass
    public synchronized void reschedule(Calendar when, DelayInfo info) {
        if (when != null) {
            info.setWantTime(when.getTime().getTime());
        } else {
            info.setWantTime(0);
        }
        if (SDLogger.willLog(SDLogger.TRACE)) {
            SDLogger.log(SDLogger.TRACE, "Request rescheduling DI" + info);
        }
        wantedReschedulings.add(info);
        this.notifyAll();
    }

    // Doc comment in interface
    public synchronized void cleanInfo(String key, short type) {
        if (SDLogger.willLog(SDLogger.TRACE)) {
            SDLogger.log(SDLogger.TRACE, "Request clean of DI: " + key + "" +type);
        }
        wantedCleanings.add(new DelayInfo(key, type, null ,null));
        this.notifyAll();
    }

    public DelayInfo getInfo(String key, short type) {
        if (SDLogger.willLog(SDLogger.TRACE)) {
            SDLogger.log(SDLogger.TRACE, "Request find of DI: " + key + "" +type);
        }
        DelayInfo info = null;

        try {
            info = diDAO.find(key, type);
        } catch (DelayException e) {
            SDLogger.log(SDLogger.ERROR, "Exception finding: " + key + "" +type + e.toString());

        }

        return info;
    }


    /** Add a listener to a map */
    private void addListener(Short wantType, DelayListener listener,
                             String info, Map<Short, Collection<DelayListener>> listenerMap)
    {
        SDLogger.log(SDLogger.DEBUG, info + "for " + wantType);
        synchronized (listenerMap) {
            Collection<DelayListener> oldListeners = listenerMap.get(wantType);
            if (oldListeners == null ) {
                oldListeners = new LinkedList<DelayListener>();
            }
            oldListeners.add(listener);
            listenerMap.put(wantType, oldListeners);
        }
    }

    // Javadoc in interface
    /* Insert the listener into waiting list, move inte "real" listeners
     * later. This is to avoid synchronization problem when notified
     * listeners wants to do register/unregister
     */
    public void registerInterest(short wantType, DelayListener listener)
    {
        addListener(new Short(wantType), listener, "Register", wantedListeners);
    }

    // Javadoc in interface
    /*
     * Insrert litener into waiting list, remove from "real" listeners later.
     * This is to avoid synchronization problem when notified
     * listeners wants to do register/unregister
     */
    public void unregisterInterest(short wantType,
                                   DelayListener listener)
    {
        addListener(new Short(wantType), listener,
                    "Unregister", wantedRemovedListeners);
    }

    // Javadoc in interface
    public synchronized void notifyEvent(String key, short type, DelayEvent event)
    {
        DelayInfo targetPlaceHolder = new DelayInfo(key, type, null, null);
        wantedEventNotifications.add(new Object[]{targetPlaceHolder, event});
        this.notifyAll();
    }


    /**
     * Stop executing the thread associated with this class
     */
    public void stopRun() {
        synchronized (this) {
            this.notifyAll();
        }
    }


    /**
     * Work through one step in run loop.
     * Checks for new schedulings/acknoledges and notify on time.
     */
    public boolean ntfRun() {
        try {
           Calendar now = Calendar.getInstance();
           handleListeners();
           if (firstTime) {
                handleOldEvents(now);
                firstTime = false;
                lastKnownNotification = now.getTime().getTime();
            }
           if (!diDAO.isBusy()) {
               saveAllEvents();
               handleSchedulesAndAcks();
               handleEventNotifications();
               handleTimeNotifications(now);
               loadTimeNotifications(now);
               diDAO.allowCleaning();
           } else {
               SDLogger.log(SDLogger.DEBUG, "DelayHandler - waiting since DB is Busy");
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException ie) {
                   SDLogger.log(SDLogger.DEBUG, "DB:Interrupted, keep on waiting)");
               }
           }
        } catch (DelayException de) {
            SDLogger.log(SDLogger.SEVERE,
                        "Exception in StoreDelay ntfRun", de);
        } catch (OutOfMemoryError e) {

            try {
                ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                SDLogger.log(SDLogger.ERROR,
                        "NTF out of memory, shutting down..." + NtfUtil.stackTrace(e));
            } catch (OutOfMemoryError e2) {;} //IGNORE
        }
        waitForSendTime();
        return false; // Wants to continue
    }

    /**
     * Allow one last run and then wait for cleaings
     */
    public boolean shutdown()
    {
        // Run one more time to be save
        ntfRun();
        SDLogger.log(SDLogger.INFO, "Shutdown done for DBDelayHandler");
        return true;
    }
    /**
     * The main thread loop
     */
    public void run() {
        super.run();
        // Clean up for final exit, close DB connection
        while (diDAO.isBusy()) {
            SDLogger.log(SDLogger.DEBUG, "DB:Waiting to close busy DAO");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                SDLogger.log(SDLogger.DEBUG, "DB:Interrupted, keep on waiting)");
            }
        };
        SDLogger.log(SDLogger.DEBUG, "DB: Going to close DAO");
        diDAO.close();
        SDLogger.log(SDLogger.DEBUG, "DB: Have closed DAO");
    }


    /**
     * Update our list of listeners according to requests.
     *
     */
    private void handleListeners()
    {
        handleWantedListeners();
        handleWantedRemovedListeners();
    }

    /** Take care of new listeners to add */
    private void handleWantedListeners()
    {
        Map<Short, Collection<DelayListener>> savedWantedListeners = null;
        synchronized (wantedListeners) {
            if (wantedListeners.size() == 0) return;
            savedWantedListeners = wantedListeners;
            wantedListeners = new HashMap<Short, Collection<DelayListener>>();
        }
        // Get all entrys from wanted, add their collection
        // to the corresponding collection in listeners.
        Set<Entry<Short, Collection<DelayListener>>> entrySet = savedWantedListeners.entrySet();
        for (Iterator<Entry<Short, Collection<DelayListener>>> it = entrySet.iterator(); it.hasNext() ; ) {
            Entry<Short, Collection<DelayListener>> wantedEntry = it.next();
            Short type = wantedEntry.getKey();
            Collection<DelayListener> newListeners = wantedEntry.getValue();
            SDLogger.logObject(SDLogger.DEBUG," Adding listeners ", newListeners);
            Collection<DelayListener> oldListeners = listeners.get(type);
            if (oldListeners == null) {
                listeners.put(type, newListeners);
            } else {
                oldListeners.addAll(newListeners);
            }
        }
    }

    /** Take care of listeners to remove */
    private void handleWantedRemovedListeners()
    {
        Map<Short, Collection<DelayListener>> savedWantedRemovedListeners = null;
        synchronized (wantedRemovedListeners) {
            if (wantedRemovedListeners.size() == 0) return;
            savedWantedRemovedListeners = wantedRemovedListeners;
            wantedRemovedListeners = new HashMap<Short, Collection<DelayListener>>();
        }
        // Get all entrys from wanted, remove info in their collection
        // from the corresponding collection in listeners.
        Set<Entry<Short, Collection<DelayListener>>> entrySet = savedWantedRemovedListeners.entrySet();
        for (Iterator<Entry<Short, Collection<DelayListener>>> it = entrySet.iterator(); it.hasNext() ; ) {
            Entry<Short, Collection<DelayListener>> wantedRemovedEntry = it.next();
            Short type = wantedRemovedEntry.getKey();
            Collection<DelayListener> listenersToRemove = wantedRemovedEntry.getValue();
            if (SDLogger.willLog(SDLogger.DEBUG)) {
                SDLogger.log(SDLogger.DEBUG, "Removing listeners " + listenersToRemove);
            }
            Collection<DelayListener> oldListeners = listeners.get(type);
            // If oldListeners == null we dont have any of the listeners to remove
            if (oldListeners != null) {
                oldListeners.removeAll(listenersToRemove);
            }
        }

    }


    /**
     * Notify about old events and notifications already existing.
     *
     */
    private void handleOldEvents(Calendar upTo)
      throws DelayException
    {
        long lastTimeForOld = upTo.getTime().getTime();
        SDLogger.log(SDLogger.DEBUG,
                     "Handle old events and notification up to : " +
                     lastTimeForOld);
        long startTime = 0;
        final int FIND_LIMIT = 2000;
        do {
            ArrayList<Object> result = new ArrayList<Object>();
            long foundUpTo = diDAO.findForTime(startTime, lastTimeForOld,
                                               FIND_LIMIT,  result);
            SDLogger.log(SDLogger.DEBUG,
                "Found up to = " + foundUpTo + "No of items " + result.size());
            for (Iterator<Object> it = result.iterator(); it.hasNext() ; ) {
                DelayInfo di = (DelayInfo)it.next();
                sendToListeners(di);
            }
            startTime = foundUpTo;

        } while (startTime < lastTimeForOld);
    }

    /**
     * Send an info to all listeners interested in it
     */
    private void sendToListeners(DelayInfo di)
    {
        Short wantTypeShort = new Short(di.getType());
        // Update lastSent event to reflect last time based event sent
        Collection<DelayListener> listenerColl = listeners.get(wantTypeShort);
        if ((listenerColl == null ) || (listenerColl.size() == 0) ) {
            SDLogger.log(SDLogger.WARNING, "No listener interested in " + di);
            SDLogger.log(SDLogger.WARNING, "Listeners == " + listeners);
            // Remove the info from DB
            try {
                diDAO.remove(di.getKey(), di.getType());
            } catch (DelayException de) {
                SDLogger.log(SDLogger.WARNING,
                             "Could not remove data without listeners", de);
            }
            return;
        }
        for (Iterator<DelayListener> it=listenerColl.iterator(); it.hasNext() ; ) {

            DelayListener listener = it.next();;
            if (SDLogger.willLog(SDLogger.DEBUG)) {
                SDLogger.log(SDLogger.DEBUG,
                            "Callback to listener " +
                            listener.getClass().getName() +
                            "-" + listener.getListenerId() + " for " + di.getKey());
            }
            // Insulate ourselves from runtime errors from listeners
            try {
                //listener.handle(this, di, notifCode, event);
            } catch (Throwable t) {
                SDLogger.log(SDLogger.ERROR, "Listener throwed exception ", t);
            }
        }


    }

    /**
     * Notify listeners about newly inserted notifications
     */
    private void handleEventNotifications()
    {
        SDLogger.log(SDLogger.TRACE,"DBDelay.handleEventNotifications");
        if (savedEventNotifications == null) return; // Nothing to do
        for (Iterator<Object[]> eventIt = savedEventNotifications.iterator();
             eventIt.hasNext() ; ) {
            Object[] infoEventPair = eventIt.next();
            try {
                doNotify(infoEventPair);
            } catch (DelayException de) {
                SDLogger.log(SDLogger.SEVERE,
                             "Could not notify for " + infoEventPair,
                             de);
            }
        }
        savedEventNotifications = null;
    }

    private void doNotify(Object[] infoEventPair)
        throws DelayException
    {
        DelayInfo placeHolder = (DelayInfo)infoEventPair[0];
        //DelayEvent event = (DelayEvent)infoEventPair[1];
        //Short wantTypeShort = new Short(placeHolder.getType());

        DelayInfo foundInfo = diDAO.find(placeHolder.getKey(),
                                         placeHolder.getType());
        if (foundInfo == null) {
            foundInfo = placeHolder;
        }
        // Send to all listeners
        sendToListeners(foundInfo);
    }


    /**
     * Send out time notifications whose time has passed.
     */
    private void handleTimeNotifications(Calendar now)
    {
        while (activeSorted.size() > 0) {
            DelayInfo di = activeSorted.first();
            if (di.getWantTime() > now.getTime().getTime()) {
                // Not time yet - we are done
                return;
            }
            sendToListeners(di);
            activeSorted.remove(di);
            activeMap.remove(di);
        }
    }

    /**
     * Time in advance that events are loaded, also determines
     * max time to sleep between work.
     */
    static private final int PRELOAD_TIME_MILLISECONDS = 15000;
    /**
     * Size of interval in which to preload events.
     */
    static private final int PRELOAD_INTERVAL_MILLISECONDS = 10000;

    /**
     * Load new notifications from the database if we are getting
     * near the end of those we already have.
     * Only call this when Dao is not busy.
     */
    private void loadTimeNotifications(Calendar now)
        throws DelayException
    {
        // Updates: lastKnownNotification, activeSorted, activeSet
        // reads from earlier lastKnownNotification and up
        long timeLeft = lastKnownNotification - now.getTime().getTime();
        if (timeLeft < PRELOAD_TIME_MILLISECONDS) {
            long nextTime = lastKnownNotification + PRELOAD_INTERVAL_MILLISECONDS;
            long startTime = lastKnownNotification;
            if (SDLogger.willLog(SDLogger.TRACE)) {
                SDLogger.log(SDLogger.TRACE,
                          "Load Time Reading events from " + lastKnownNotification +
                          " up to : " + nextTime);
            }
            final int FIND_LIMIT = 2000;
            do {
                ArrayList<DelayInfo> result = new ArrayList<DelayInfo>();
                long foundUpTo = diDAO.findForTime(startTime, nextTime,
                                                   FIND_LIMIT,  result);
                if (SDLogger.willLog(SDLogger.TRACE)) {
                    SDLogger.log(SDLogger.TRACE,
                                  "LoadNotif: Found up to = " + foundUpTo +
                                  " No of items " + result.size());
                }
                for (Iterator<DelayInfo> it = result.iterator(); it.hasNext() ; ) {
                    DelayInfo di = it.next();
                    activeMap.put(di,di);
                    activeSorted.add(di);
                }
                startTime = foundUpTo;

            } while (startTime < nextTime);
            lastKnownNotification = nextTime;

        }
    }


    private void waitForSendTime()
    {
        // TODO: waitForSendTime - Wait until a time notif, or some maxiumum
        // Note that even when waiting long time we will be woken
        // on a notifyall

        // For now, just wait a while to avoid spinning to much
        synchronized (this) {
            try {
                this.wait(1000);
            } catch (InterruptedException ignored) {}
        }
    }


    /**
     * Save all events that are to be handled
     */
    private synchronized void saveAllEvents()
    {
        if (wantedCleanings.size() > 0) {
            if (savedCleanings == null) {
                savedCleanings = wantedCleanings;
            } else {
                savedCleanings.addAll(wantedCleanings);
            }
            wantedCleanings = new LinkedList<DelayInfo>();
        }
        if (wantedSchedulings.size() > 0) {
            if (savedSchedulings == null) {
                savedSchedulings = wantedSchedulings;
            } else {
                savedSchedulings.addAll(wantedSchedulings);
            }
            wantedSchedulings = new LinkedList<DelayInfo>();
        }
        if (wantedReschedulings.size() > 0) {
            if (savedReschedulings == null) {
                savedReschedulings = wantedReschedulings;
            } else {
                savedReschedulings.addAll(wantedSchedulings);
            }
            wantedReschedulings = new LinkedList<DelayInfo>();
        }
        if (wantedEventNotifications.size() > 0) {
            if (savedEventNotifications == null) {
                savedEventNotifications = wantedEventNotifications;
            } else {
                savedEventNotifications.addAll(wantedEventNotifications);
            }
            wantedEventNotifications = new LinkedList<Object[]>();
        }
    }

    /**
     * Take care of the (re)scheduled and acknowledged objects.
     * The objects are taken from their waiting queues and
     * the wanted operation towards the storage is done.
     */
    private void handleSchedulesAndAcks() {
        // Clean Before Schedule to handle Clean + Schedule of same
        // Schedule Before Reschedule to handle  Schedule + Reschdule of same
        handleCleanings();
        handleSchedulings();
        handleReschedulings();
    }

    private void handleCleanings()
    {
        if (savedCleanings == null) return; // Nothing to do

        for (Iterator<DelayInfo> it = savedCleanings.iterator(); it.hasNext(); ) {
            DelayInfo placeHolder = it.next();
            try {
                diDAO.remove(placeHolder.getKey(), placeHolder.getType());
                // Check if we have loaded this but not sent
                DelayInfo existing = activeMap.get(placeHolder);
                if (existing != null) {
                    // Note: Important to use existing when removing
                    // in active sorted since it uses the timestamp as a
                    // key
                    activeMap.remove(existing);
                    activeSorted.remove(existing);
                }
            } catch (DelayException de) {
                SDLogger.log(SDLogger.WARNING,
                            "Could not remove info : " + placeHolder);
            }
        }
        savedCleanings = null; // Done with these
    }

    /**
     * Take care of all waiting schedulings. If their time is due
     * they will be sent at once, if time is not due they might
     * be stored in internal cache if early enough to send them.
     */
    private void handleSchedulings()
    {
        if (savedSchedulings == null) return; // Nothing to do

        for (Iterator<DelayInfo> it = savedSchedulings.iterator(); it.hasNext() ; ) {
            DelayInfo dInfo = it.next();
            try {
                SDLogger.logObject(SDLogger.DEBUG, "Scheduling object", dInfo);
                DelayInfo storedInfo = diDAO.find(dInfo.getKey(), dInfo.getType());
                if( storedInfo == null ) {
                    diDAO.create(dInfo);
                } else {
                    diDAO.update(dInfo);
                }

                if (dInfo.getWantTime() >
                    DelayInfoDAO.MAX_MS_FOR_NOTIFICATIONS) {
                    // This is a time based notification
                    Calendar now = Calendar.getInstance();
                    long nowMS = now.getTime().getTime();
                    // Send at once if time has passed AND the event will
                    // not be read from DB later, In higly loaded situations
                    // then lastKnownNotification might be before now.
                    if ((dInfo.getWantTime() < nowMS) &&
                        (dInfo.getWantTime() < lastKnownNotification))
                    {
                        SDLogger.logObject(SDLogger.DEBUG, "New info sent at once", dInfo.getKey());
                        sendToListeners(dInfo);
                    } else if (dInfo.getWantTime() < lastKnownNotification) {
                        // Put into soon to be sent list+map
                        SDLogger.logObject(SDLogger.DEBUG,
                                    "New info saved internally", dInfo.getKey());
                        activeSorted.add(dInfo);
                        activeMap.put(dInfo, dInfo);
                    } else {
                        SDLogger.log(SDLogger.DEBUG, "New info only in DB");
                    }
                }

            } catch (DelayException de) {
                SDLogger.log(SDLogger.WARNING,
                             "Could not schedule info : " + dInfo);
                SDLogger.log(SDLogger.TRACE, "Schedule Exception", de);
                // Notify listeners
                sendToListeners(dInfo);
            }

        }
        savedSchedulings = null;
    }


    /**
     * Take care of all newly added reschedulings
     */
    private void handleReschedulings()
    {
        if (savedReschedulings == null) return; // Nothing to do

        for (Iterator<DelayInfo> it = savedReschedulings.iterator(); it.hasNext() ; ) {
            DelayInfo dInfo = it.next();
            try {
                SDLogger.logObject(SDLogger.DEBUG, "Rescheduling object", dInfo);
                boolean updated = diDAO.update(dInfo);
                if (!updated) {
                    // The info was not already scheduled.
                    SDLogger.logObject(SDLogger.INFO,
                              "Can not rechedule info that is not scheduled",
                              dInfo);
                    sendToListeners(dInfo);
                    break; // Nothing more to do in this iteration
                }
                DelayInfo oldInfo = activeMap.get(dInfo);
                if (oldInfo != null) {
                    // Remove from cache
                    SDLogger.logObject(SDLogger.DEBUG,
                                        "Removing old object from cache",
                                        oldInfo);
                    activeMap.remove(oldInfo);
                    activeSorted.remove(oldInfo);
                }

                if (dInfo.getWantTime() >
                    DelayInfoDAO.MAX_MS_FOR_NOTIFICATIONS) {
                    // This is a time based notification
                    Calendar now = Calendar.getInstance();
                    long nowMS = now.getTime().getTime();
                    // Only do immediate delivery if we will not read the event
                    // from the DB later, this is a safeguard for
                    // highly loaded systems
                    if ((dInfo.getWantTime() < nowMS) &&
                        (dInfo.getWantTime() < lastKnownNotification)) {
                        SDLogger.logObject(SDLogger.DEBUG, "New info sent at once", dInfo.getKey());
                        sendToListeners(dInfo);
                    } else
                        // ----------- */
                    if (dInfo.getWantTime() < lastKnownNotification) {
                        // Put into soon to be sent list+map
                        SDLogger.logObject(SDLogger.DEBUG,
                                    "Recheduled info saved internally", dInfo.getKey());
                        activeSorted.add(dInfo);
                        activeMap.put(dInfo, dInfo);
                    } else {
                        SDLogger.logObject(SDLogger.DEBUG, "Rescheduled info only in DB", dInfo.getKey());
                    }
                }

            } catch (DelayException de) {
                SDLogger.log(SDLogger.WARNING,
                             "Could not reschedule info : " + dInfo,
                             de);
                sendToListeners(dInfo);
            }
        }
        savedReschedulings = null;
    }

    // Javadoc in interface
    public void registeringDone()
    {
        this.start();
    }
}
