/*
 * PersistentQueue.java
 *
 * Created on den 16 augusti 2005, 15:48
 */

package com.mobeon.ntf.util;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.util.time.NtfTime;
import java.util.*;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;


/**
 * Stores objects in a queue. All interactions on the queue is logged in a
 * transaction log. The transaction log is read during startup and the queue
 * is restored.
 *
 * The format on the logentry will be:
 * <ACTION> <TYPE> <KEY> <DATA>
 *
 * Where ACTION is:
 * A - Added entry
 * D - Deleted entry
 * U - Updated entry
 *
 * TYPE is the type of object, the type is fetched from the persistentObject
 * KEY is an counter value that are unique in every session.
 * DATA is a line of data fetched from persistentObject.
 *
 * @author  mnify
 */
public class PersistentQueue {
    private final static Logger log = Logger.getLogger(PersistentQueue.class);
    private static HashMap<String, PersistentQueue> queues;

    private static int keyGen = 1;

    // the actual queue with objects.
    private LinkedList<PersistentObject> queue;

    // list with objects that are to be retried.
    private LinkedList<DelayItem> timeOrderedItems;

    // a collection of objects that are getted but not deleted yet.
    private HashMap<String, PersistentObject> pendingObjects;
    private HashMap<String, Class<?>> classMap;

    /** The name of the queue */
    private String name;



    private Writer out;
    private File currentFile;
    private File lastFile;
    private File refreshFile;

    // 10 minutes delay
    private int maxSleepTime = 10 * 60 * 1000;

    static {
        queues = new HashMap<String, PersistentQueue>();
    }


    public static PersistentQueue getQueue(String name) {
        PersistentQueue queue = queues.get(name);
        if( queue == null ) {
            queue = new PersistentQueue(name);
            queues.put(name, queue);
        }
        return queue;
    }



    /** Creates a new instance of PersistentQueue */
    private PersistentQueue(String name) {

        this.name = name;
        queue = new LinkedList<PersistentObject>();
        classMap = new HashMap<String, Class<?>>();
        pendingObjects = new HashMap<String, PersistentObject>();
        timeOrderedItems = new LinkedList<DelayItem>();
        currentFile = new File(Config.getDataDirectory(), name + ".current");
        lastFile = new File(Config.getDataDirectory(), name + ".last");
        refreshFile = new File(Config.getDataDirectory(), name + ".refresh");

        restore();
        refresh();
        new Refresher();
        new DelayThread();
    }

    /**
     *Reads the old file and creates the queue of items left in the file.
     */
    public void restore() {

        File journalDir = new File(Config.getDataDirectory());
        if (!journalDir.exists()) {
            if (!journalDir.mkdirs()) {
                log.logMessage("Unable to create datadirectory \""
                                + someName(journalDir) + "\"", Logger.L_ERROR);
                ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
            }
        }
        if (!currentFile.exists() && lastFile.exists()) {
            lastFile.renameTo(currentFile);
        }

        if (currentFile.exists()) {
            log.logMessage("Restoring queue data from file "
                            + someName(currentFile), Logger.L_VERBOSE);
            
            BufferedReader in = null;
            try {
                in = new BufferedReader
                    (new InputStreamReader(new FileInputStream(currentFile), "UTF-8"));
                String line = in.readLine();
                HashMap<String, PersistentObject> map = new HashMap<String, PersistentObject>();
                while( line != null ) {
                    parseLine(line, map);
                    line = in.readLine();
                }
                Iterator<PersistentObject> iterator = map.values().iterator();
                while(iterator.hasNext()) {
                    PersistentObject obj = iterator.next();
                    obj.setKey("" + keyGen++);
                    queue.addLast(obj);
                }
            } catch(IOException ioe) {
                log.logMessage(NtfUtil.stackTrace(ioe).toString(), Logger.L_ERROR);
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        // ignore
                    }
            }
        }

    }

    private PersistentObject parseLine(String line, HashMap<String, PersistentObject> map) {
        String items [] = line.split(" ");
        if( items.length < 3 ) {
            log.logMessage("Line contains no useful data" + line, Logger.L_VERBOSE );
            return null;
        }

        String action = items[0];
        if( action.equals("C")) {
            String type = items[1];
            String className = items[2];
            try {
                log.logMessage("Adding class " + type + ": " + className, Logger.L_VERBOSE);
                Class<?> c = Class.forName(className);
                classMap.put(type, c);
            } catch(Exception e) {
               log.logMessage(NtfUtil.stackTrace(e).toString(), Logger.L_ERROR);
            }
        } else {
            try {
                String type = items[1];
                String key = items[2];
                String data = "";
                for( int i=3;i<items.length;i++ ) {
                    data += items[i] + " ";
                }
                data = data.trim();
                String mapKey = type + "-" + key;
                Class<?> c = classMap.get(type);
                if( c != null && action.equals("A")) {
                    PersistentObject obj = (PersistentObject) c.newInstance();
                    obj.setKey(key);
                    obj.parseStringData(data);

                    map.put(mapKey, obj);
                } else if( action.equals("R")) {
                    map.remove(mapKey);
                }
            } catch(Exception e) {
                log.logMessage(e.toString(), Logger.L_ERROR);
            }

        }

        return null;
    }

    /**
     * Replaces the current file with a new one and writes all existing info
     * in the queue to the new file.
     */
    public synchronized void refresh() {
        int t = NtfTime.now;
        Writer w = null;

        log.logMessage("Refreshing persistent queue " + name, Logger.L_DEBUG);
        try {
            if (out != null) {
                out.close();
            }
            
            w = new BufferedWriter
                (new OutputStreamWriter(new FileOutputStream(refreshFile), "UTF-8"));
            
            Iterator<?> iterator  = classMap.keySet().iterator();
            while( iterator.hasNext() ) {
                String type = (String) iterator.next();
                Class<?> c = classMap.get(type);
                try {
                    w.write("C " + type + " " + c.getName() + "\n" );
                    w.flush();
                } catch(IOException ioe) {
                    log.logMessage("Failed to write queue data " + ioe.toString(), Logger.L_ERROR );
                }

            }
            
            iterator = queue.iterator();
            while( iterator.hasNext() ) {
                PersistentObject obj = (PersistentObject) iterator.next();
                writeLine(w, "A", obj);
            }

            iterator  = pendingObjects.values().iterator();
            while( iterator.hasNext() ) {
                PersistentObject obj = (PersistentObject) iterator.next();
                writeLine(w, "A", obj);
            }


            try {
                w.flush();
                currentFile.renameTo(lastFile);
                refreshFile.renameTo(currentFile);
                out = w;
            } catch (IOException e) { ; }
            log.logMessage("Refreshed persistent queue " + name + " in " + (NtfTime.now - t) + " seconds", Logger.L_DEBUG );
            
        } catch (IOException e) {
            log.logMessage("Failed to open slamdown refresh file", Logger.L_ERROR);
        }
    }

    private synchronized void addToQueue( PersistentObject obj ) {
        queue.addLast(obj);
        notify();
    }

    /**
     *Add an object to the queue
     */
    public synchronized void add( PersistentObject obj ) {
        checkClass(obj);
        obj.setKey( "" + keyGen++ );
        writeLine(out, "A", obj);
        queue.addLast(obj);
        notify();
    }

    /**
     *Removes an object from the queue
     */
    public synchronized void remove( PersistentObject obj ) {
        writeLine(out, "R", obj);
        pendingObjects.remove(obj.getKey());
    }

    /**
     *Will put the object to the queue again after 10 minutes.
     *@param obj The object to be retried.
     */
    public synchronized void retry( PersistentObject obj ) {
        DelayItem delayItem = new DelayItem();
        delayItem.key = obj.getKey();
        delayItem.wakeUpTime = NtfTime.now + maxSleepTime/1000;
        log.logMessage("Retry of " + delayItem.key + " will happen in " + maxSleepTime/1000 + " seconds", Logger.L_DEBUG );
        timeOrderedItems.addLast(delayItem);
    }

    /**
     *Gets an object. If no object exists the method will wait until an
     *object exists.
     */
    public synchronized PersistentObject get() {
        while( queue.size() == 0 ) {
            try {
                wait();
            } catch( InterruptedException e) {
                log.logMessage(NtfUtil.stackTrace(e).toString(), Logger.L_ERROR);
            }
        }

        PersistentObject obj = queue.getFirst();
        pendingObjects.put(obj.getKey(), obj );
        queue.removeFirst();
        return obj;
    }

    /**
     *@return the total count of messages in the queue and objects that awaits
     *deletion.
     */
    public int getTotalCount() {
        return queue.size() + pendingObjects.size();
    }

    /**
     *@return number of objects in the queue.
     */
    public int getQueueSize() {
        return queue.size();
    }

    private synchronized void checkClass(PersistentObject obj) {
        String type = obj.getType();
        if( !classMap.containsKey(type) ) {
            Class<?> c = obj.getClass();
            classMap.put(type, c);
            try {
                out.write("C " + type + " " + c.getName() + "\n" );
                out.flush();
            } catch(IOException ioe) {
                log.logMessage("Failed to write queue data " + ioe.toString(), Logger.L_ERROR );
            }
        }
    }

    private void writeLine(Writer w, String action, PersistentObject obj) {
        try {
            w.write(action + " " + obj.getType() + " " + obj.getKey() + " " + obj.getStringData() + "\n" );
            w.flush();
        } catch(IOException ioe) {
            log.logMessage("Failed to write queue data " + ioe.toString(), Logger.L_ERROR );
        }

    }

    private String someName(File f) {
        try {
            return  f.getCanonicalPath();
        } catch (IOException e) {
            return f.toString();
        }
    }


    private class Refresher
        extends Thread {

        public Refresher() {
            super("PersistentQueue:Refresher");
            setDaemon(true);
            start();
        }

        public void run() {
            // Start by waiting a third of the interval to avoid collision with
            // outdial refresh and slamdown refresh
            int nextRefresh = NtfTime.now + Config.getJournalRefreshInterval() / 3;

            while (true) {
                try {
                    log.logMessage("Next refresh in "
                                    + (nextRefresh - NtfTime.now) + " seconds", Logger.L_DEBUG);
                    NtfTime.sleepUntil(nextRefresh);
                    refresh();
                    nextRefresh = NtfTime.now + Config.getJournalRefreshInterval();
                } catch (Exception e) { //Catch all exceptions that have gone this far, and log where they came from
                    log.logMessage("SlamdownStore got unexpected exception: "
                                    + NtfUtil.stackTrace(e), Logger.L_ERROR);
                }  catch (OutOfMemoryError e) {
                    try {
                        ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                        log.logMessage("Ntf out of memory! Will Shutdown",  Logger.L_ERROR);
                    } catch (OutOfMemoryError e2) {;} //ignore
                    return;
                }
            }
        }
    }

    private class DelayItem {
        public int wakeUpTime;
        public String key;

    }


    /**
     *checks for objects that are to be retried.
     */
    private class DelayThread extends Thread {
        public DelayThread() {
            super("PersistentQueue:delayThread");
            setDaemon(true);
            start();
        }

        public void run() {
            while( true ) {
                int now = NtfTime.now;
                if( timeOrderedItems.size() == 0 ) {
                    try {
                        sleep( maxSleepTime );
                    } catch (InterruptedException e ) {};
                } else {
                    DelayItem item = timeOrderedItems.getFirst();
                    if( item.wakeUpTime < now ) {
                        PersistentObject obj = pendingObjects.remove(item.key);
                        timeOrderedItems.removeFirst();
                        if( obj != null ) {
                            addToQueue(obj);
                        }
                    } else {
                        int sleepTime = (item.wakeUpTime - now) * 1000;
                        sleepTime = Math.max( sleepTime, 500 );
                        try {
                            sleep(sleepTime);
                        } catch (InterruptedException e) {}
                    }
                }
            }
        }
    }

}
