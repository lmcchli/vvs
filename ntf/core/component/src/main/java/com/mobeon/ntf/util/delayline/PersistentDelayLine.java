/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.delayline;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.time.NtfTime;
import java.io.*;
import java.util.*;

/**
 * This class extends the DelayLine by make it persistent. A journal file is
 * created on disk to keep track of changes of the delay line between
 * refresh. When the delay line is restarted it begin by check if any journal
 * files exists. If it does, it use that to reconstruct the delay line. This may
 * take a long time if the journal file is large. Estimated 2 minutes on a Sun
 * SPARC Ultra 5 with about 120.000 items and a 8 Mb large journal file.
 */
public class PersistentDelayLine extends DelayLine implements Runnable {
    /** All journal files in a file array*/
    protected File journalFiles[];
    /** The current file indicat which journal files that is currently written to.*/
    private int currentFile;
    protected ObjectOutputStream oStream;

    /** Used for indicate to the refresh thread that it shall terminate */
    private boolean close = false;

    /** The extension of the other journal file. Pretty ugly solution but
        simple. Better would be to use numbers instead and to let the
        administrator to configure how many journal files to keep */
    protected String dumpFilename = ".second";

    /** The add request to the journal file */
    protected static final String ADD_REQUEST = "ADD";
    /** The delete request to the journal file */
    protected static final String DELETE_REQUEST = "DEL";

    protected int refreshJournalDelay;
    protected Thread refreshThread;

    /**
     * Create a delay line that is persistent. The delay line store on disk
     * each operation from other clients. The operations is in and cancel.
     *@param grp - assign the refresh thread to a thread group if the group is not null
     *@param delayers - see DelayLine(delayers, timeoutDelayer)
     *@param timeoutDelayer - see DelayLine(delayers, timeoutDelayer)
     *@param contentLog - filename of log.
     *@param refreshJournalDelay - delay for refreshJournal in seconds
     *@throws IOException if the journal file could not be created
     *@throws FileNotFoundException if the journal file could not be located.
     *@see DelayLine
     */
    public PersistentDelayLine(ThreadGroup grp,
                               int delayers[],
                               int timeoutDelayer,
                               String contentLog,
                               int refreshJournalDelay) throws IOException,
                                                               FileNotFoundException {
        super(grp, delayers, timeoutDelayer);

        if (grp != null) {
            refreshThread = new Thread(grp, this, "Journal_refresh");
        } else {
            refreshThread = new Thread(this, "Journal_refresh");
        }

        this.refreshJournalDelay = refreshJournalDelay;

        dumpFilename = contentLog + dumpFilename;
        log.logMessage("Using journalfile: " + dumpFilename, log.L_DEBUG);
        journalFiles = new File[2];
        journalFiles[0] = new File(contentLog);
        journalFiles[1] = new File(dumpFilename);

        TreeSet sortedKeyList = new TreeSet();

        if (journalFiles[0].exists() && journalFiles[1].exists()) {
            // Ooops, we was taken down in the middle of a dump! Since the dump
            // may be incomplete, use the one that was NOT last modified.
            long modFile0 = journalFiles[0].lastModified();
            long modFile1 = journalFiles[1].lastModified();
            //System.out.println("Found both files");
            if (modFile0 < modFile1) {
                //initializeFromJournalFile(journalFile[0], sortedKeys);
                //journalFiles[1].delete();
                currentFile = 0;
            } else {
                //initializeFromJournalFile(journalFile[1], sortedKeys);
                //journalFiles[0].delete();
                currentFile = 1;
            }
            initializeFromJournalFile(currentFile, sortedKeyList);
        } else if (journalFiles[0].exists()) {
            // Found the first journal file.
            //System.out.println("Found 0");
            initializeFromJournalFile(0, sortedKeyList);
            currentFile = 0;
        } else if (journalFiles[1].exists()) {
            //System.out.println("Found 1");
            // Found the second journal file
            initializeFromJournalFile(1, sortedKeyList);
            currentFile = 1;
        } else {
            // The journal file was not found. Verify that the directory exists
            // bla bla
            for (int k = 0; k < journalFiles.length; k++) {
                File dir = journalFiles[k].getParentFile();
                //System.out.println(k+"Test for parent file " + dir);
                if (dir != null && !dir.exists()) {
                    //System.out.println("Create the directory ");
                    // Need to create the directory.
                    if (!dir.mkdirs()) {
                        // Could not create directories!
                        throw new java.io.FileNotFoundException("Could not create directory "
                                                                + dir.getAbsolutePath()
                                                                + " for journal file "
                                                                + contentLog);
                    }
                }
                // The directory has been created or was already created. Check that
                // we can create the journal file.
                try {
                    //System.out.println("Create file " + journalFiles[k]);
                    if (!journalFiles[k].createNewFile()) {
                        // The journal file did not exist and we could not create it
                        // ....
                        throw new java.io.FileNotFoundException("Could not create file "
                                                                + journalFiles[k]);
                    }
                } catch (java.io.IOException ioe) {
                    // Could not create the file.
                    if (ioe.getMessage() != null) {
                        throw new java.io.IOException("Could not create file "
                                                      + journalFiles[k]
                                                      + " Details: "
                                                      + ioe.getMessage());
                    } else {
                        throw new java.io.IOException("Could not create file "
                                                      + journalFiles[k] + " No further information");
                    }
                }
            }
            currentFile = 0;
            journalFiles[0].delete();
            journalFiles[1].delete();
        }

        initializeDelayers(sortedKeyList);
        // refresh the list
        refreshJournal();
        refreshThread.start();
    }

    /**
     * Same as PersistentDelayLine(null, delayers, timeoutDelayer, contentLog, refreshJournalDelay)
     */
    public PersistentDelayLine(int delayers[],
                               int timeoutDelayer,
                               String contentLog,
                               int refreshJournalDelay) throws IOException,
                                                               FileNotFoundException,
                                                               NullPointerException {
        this(null, delayers, timeoutDelayer, contentLog, refreshJournalDelay);
    }

    /**
     * Add each DelayItem to a proper delayer.
     * @param sortedKeyList is a list of keys stored in the hashtable ordered by
     * arrival time of the DelayItem.
     */
    protected void initializeDelayers(TreeSet sortedKeyList) throws java.io.IOException {
        if (sortedKeyList.size() > 0) {
            // Calculate the accumulated delay time for each delayer
            int accumulatedDelayTime[] = new int[_delayers.length];
            accumulatedDelayTime[0] = _delayers[0].getDelayTime();
            for (int i = 1; i < accumulatedDelayTime.length; i++) {
                accumulatedDelayTime[i] = accumulatedDelayTime[i - 1] + _delayers[i].getDelayTime();
            }

            // Get the last item added before "crash" to establish downtime.
            PersistentDelayItem lastItem =  (PersistentDelayItem) sortedKeyList.last();

            int startupTime = NtfTime.now;
            int downtime = NtfTime.now - lastItem.getLastArrivalTime();
            log.logMessage("Last arrival time is " + lastItem.getLastArrivalTime(), log.L_DEBUG);
            log.logMessage("Persistent delay line estimated downtime is " + downtime + " seconds", log.L_DEBUG);

            // Iterate through the list and add all elements one by one.
            Iterator it = sortedKeyList.iterator();

            while (it.hasNext()) {
                PersistentDelayItem item = (PersistentDelayItem) it.next();
                boolean itemAdded = false;

                // Calculate the new arrival time for the item.
                int itemNewArrivalTime = item.getLastArrivalTime() + downtime;

                // We need to find a proper delayer to put the delayer in. This
                // is established as follows.
                // If the difference between startup time and lastArrivaltime is
                // larger than the accumulated delay time, we add it to that delayer.
                //System.out.println("Item new arrival time: " + item_new_arrival_time);
                for (int i = 0; i < _delayers.length; i++) {

                    if ((startupTime - itemNewArrivalTime) <  accumulatedDelayTime[i]) {
                        item.setQueueNumber((byte) i);
                        item.setLastArrivalTime(itemNewArrivalTime);
                        // The outtime for the item is established as follwos
                        // The difference between
                        // (startup_time-item_new_arrival_time) and the
                        // accumulatedDelayTime for the delayer must be the
                        // remaining time for this item in that queue.
                        item.setOutTime(startupTime + accumulatedDelayTime[i]
                                        - (startupTime - itemNewArrivalTime));
                        _delayers[i].add(item);
                        log.logMessage("Persistent delay line has recreated item " + item + " in queue " + i, log.L_DEBUG);
                        itemAdded = true;
                        break;
                    }
                }
                if (!itemAdded) {
                    // Could not find a proper queue to store it in. Can depend
                    // on that the queues has been less or that the timespan of
                    // the queues is less. Add the item to the first queue!
                    item.setQueueNumber((byte) 0);
                    item.setLastArrivalTime(itemNewArrivalTime);
                    item.setOutTime(-1);
                    _delayers[0].add(item);
                    log.logMessage("Persistent delay line has added the item " + item + " in the first delayer", log.L_DEBUG);
                }
            }
        }
    }

    /**
     */
    protected void initializeFromJournalFile(int journalFileIndex, TreeSet sortedKeyList)
        throws java.io.IOException, FileNotFoundException, NullPointerException  {
        // Read data from the journal file
        // elementList contain the keys in a sorted manner since the objects themselves
        // arrives in a sorted manner.

        if (journalFiles[journalFileIndex].length() <= 0) {
            log.logMessage("The journal file was empty", log.L_DEBUG);
            return;
        }

        try {
            int startupTime = NtfTime.now;
            ObjectInputStream istream =
                new ObjectInputStream(new FileInputStream(journalFiles[journalFileIndex].getAbsolutePath()));
            FileOutputStream fout = new FileOutputStream("../logs/Dump_OutdialFile.txt");
            while (true) {
                String action = (String) istream.readObject();
                Object value = (Object) istream.readObject();
                if (action.equals(ADD_REQUEST)) {
                    PersistentDelayItem pValue = null;
                    if (value instanceof PersistentDelayItem) {
                        pValue = (PersistentDelayItem) value;
                    } else {
                        throw new ClassCastException("Could not cast " + value
                                                     + " to PersistentDelayItem for in request");
                    }
                    int arrivalTime  = pValue.getArrivalTime();
                    int validityTime = 1;//Config.getVeryOldMessage();
                    if ((startupTime - arrivalTime) < (validityTime)) {
                        log.logMessage("Valid outdial, add to table.", log.L_DEBUG);
                        _objectTable.put(pValue.getItem().getKey(), pValue);
                    } else {
                        log.logMessage("Not a valid outdial, to old.", log.L_DEBUG);
                    }
                } else {
                    if (value instanceof Delayable) {
                        _objectTable.remove(((Delayable) value).getKey());
                    } else if (value instanceof PersistentDelayItem) {
                        //System.out.println("********** HUHHH? Got strange item");
                        _objectTable.remove(((PersistentDelayItem) value).getItem().getKey());
                    } else if (value instanceof String) {
                        // Got the key from file
                        _objectTable.remove((String) value);
                    } else {
                        throw new ClassCastException("Could not cast " + value
                                                     + " to PersistentDelayItem or Delayable for cancel request. Got "
                                                     + value.getClass().getName());
                    }
                }
            }
        } catch (EOFException e) {
            log.logMessage("The journal file has been read to end of file.", log.L_VERBOSE);
        } catch (NoSuchElementException noEl) {
            log.logMessage("Got exception while reading journal file. Journal file is perhaps not fully restored. Message: " + noEl, log.L_ERROR);

        } catch (ClassNotFoundException ee) {
            log.logMessage("Could not find class while reading journal file. Journal file is perhaps not fully restored. Message: " + ee.getMessage(), log.L_ERROR);
        } catch (java.io.InvalidClassException eee) {
            log.logMessage("Could not restore journal file. The journal file may have been corrupt. Message: " + eee.getMessage(), log.L_ERROR);
        } catch (ClassCastException cce) {
            log.logMessage("Error ocurred while restoring the journal file. The journal file may be of some other version. Message: " + cce.getMessage(), log.L_ERROR);
        } catch (java.io.StreamCorruptedException corruptExc) {
            String corruptFilename = renameFileAsCorrupt(journalFiles[journalFileIndex]);
            log.logMessage("The journal file was corrupt. The corrupted file has been saved as " + corruptFilename, log.L_ERROR);
        }

        log.logMessage("Start sort items in the journal file", log.L_VERBOSE);
        sortedKeyList.addAll(_objectTable.values());
        log.logMessage("Done sorting items in the journal file", log.L_VERBOSE);
    }


    /**
     * This take the delay line and write all current items as ADD
     * operations. The delay line is blocked until the refresh is done. It is
     * recommended to do this when the journal size is small.
     */
    protected synchronized void refreshJournal() throws IOException {
        if (oStream != null) {
            try {
                oStream.close();
                oStream = null;
            } catch (Exception e) { ; }
        }
        int currentFileTmp = currentFile;
        int newFileNr = switchToNextJournalFile();
        log.logMessage("Persistent delay line switch to file " + journalFiles[newFileNr].getAbsolutePath(), log.L_DEBUG);
        oStream = new ObjectOutputStream(new FileOutputStream
                                         (journalFiles[newFileNr].getAbsolutePath(),
                                          false));
        if (_objectTable.size() <= 0) {
            log.logMessage("Persistent delay line has no objects to refresh.", log.L_VERBOSE);
            log.logMessage("Persistent delay line delete old file " + journalFiles[currentFileTmp].getAbsolutePath(), log.L_DEBUG);
            journalFiles[currentFileTmp].delete();
            return;
        }

        int refreshedObjects = 0;

        Enumeration keys = _objectTable.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = _objectTable.get(key);
            oStream.writeObject(ADD_REQUEST);
            if (value instanceof PersistentDelayItem) {
                oStream.writeObject(value);
            } else {
                PersistentDelayItem tr = new PersistentDelayItem((Delayable) value);
                tr.setQueueNumber((byte) 0);
                tr.setArrivalTime(NtfTime.now);
                tr.setLastArrivalTime(NtfTime.now);
                oStream.writeObject(tr);
                //System.out.println("Has dumped a strange thing ...");
            }
            oStream.flush();
            refreshedObjects++;
        }
        log.logMessage("Persistent delay line has refreshed " + refreshedObjects + " objects", log.L_VERBOSE);
        log.logMessage("Persistent delay line delete old file " + journalFiles[currentFileTmp].getAbsolutePath(), log.L_DEBUG);
        journalFiles[currentFileTmp].delete();
    }

    /**
     * This method takes as argument a file and renames it as the next possible
     * file with name journalfile.corrupt.<sequence>.
     *@param journalFile - the file to save as corrupt file. Ignores null input.
     *@return the filename of the corrupted file.
     */
    protected String renameFileAsCorrupt(File journalFile) {
        if (journalFile == null) {
            return null;
        }

        int fileCounter = 1;

        String journalFilename = journalFile.getAbsolutePath();

        // Count how many dots there is in the filename
        // First we need to know where the last slash is (since the path may
        // have dots
        int indexOfLastSlash = journalFilename.lastIndexOf("/");
        if (indexOfLastSlash >= 0) {
            journalFilename = journalFilename.substring(0, indexOfLastSlash + 1)
                + "journalfile.corrupt";
        } else {
            journalFilename =  "journalfile.corrupt";
        }

        // Find an available sequence number
        int sequenceNumber = 1;
        while (true) {
            String file = journalFilename + "." + (new Integer(sequenceNumber++)).toString();
            File f = new File(file);
            if (!f.exists()) {
                journalFile.renameTo(f);
                return file;
            }
        }
    }

    /**
     *  Select the next journal file to write to.
     * @return the new file index to write to.
     */
    protected int switchToNextJournalFile() {
        if (++currentFile >= journalFiles.length) {
            currentFile = 0;
        }
        return currentFile;
    }

    /**
     * Close the delay line. If the refresh thread is currently refreshing, it
     * will continue with that refresh. After a close has been called a new
     * refresh is done.
     */
    public synchronized void close() {
        close = true;
        if (refreshThread != null && refreshThread.isAlive()) {
            refreshThread.interrupt();
        }
    }

    /**
     * Add an item to the delay line, then store the event in a journal file.
     *@param key - the key of the item. Null values is discarded.
     *@param item - the value either a PersistentDelayItem or Delayable. Null
     * values is discarded.
     *@return the previously stored item in the delay line.
     *@throw IOException if write error occurs,
     */
    protected synchronized Delayable in(Object key, DelayItem item)
        throws java.io.IOException {
        if (key == null || item == null) { return null; }

        PersistentDelayItem i;
        if (item instanceof PersistentDelayItem) {
            i = (PersistentDelayItem) item;
        } else {
            i = new PersistentDelayItem(item);
        }

        i.setLastArrivalTime(NtfTime.now);

        // Add the element before writing it to disk to avoid that the element
        // is discarded if we're out of disk etc.
        Delayable o = super.in(key, i);

        oStream.writeObject(ADD_REQUEST);
        oStream.writeObject(i);

        oStream.flush();
        return  o;
    }

    /**
     * Cancel an object in the persistent delay line.
     * @param key - the key to remove. Null values is discarded
     * @see DelayLine.cancel
     */
    public synchronized Delayable cancel(Object key) throws IOException {

        Object theKey = key;
        if (key instanceof Delayable) {
            theKey = ((Delayable) key).getKey();
        }

        if (theKey == null) {
            return null;
        }

        // The object is first removed from the hash to prevent that is not
        // removed in the case that the disk is full.
        Delayable o = super.cancel(theKey);
        if (o != null) {
            oStream.writeObject(DELETE_REQUEST);
            oStream.writeObject(theKey);
            oStream.flush();
        }
        return o;
    }

    /**
     */
    public void run() {
        int nextRefreshJournal = NtfTime.now + refreshJournalDelay;

        java.text.SimpleDateFormat dateFormatter =
            new java.text.SimpleDateFormat("yyyy MMM dd HH:mm");
        dateFormatter.setTimeZone(Calendar.getInstance().getTimeZone());
        while (!close) {
            try {

                if (nextRefreshJournal > NtfTime.now) {
                    try {
                        refreshThread.sleep((nextRefreshJournal - NtfTime.now) * 1000);
                    } catch (Exception sleepExc) { ; }
                }
                nextRefreshJournal += refreshJournalDelay;
                log.logMessage("PersistentDelayLine try to refresh journal file", log.L_VERBOSE);
                refreshJournal();
                log.logMessage("PersistentDelayLine finished refreshing journal file. Next refresh will occur " + (dateFormatter.format((new java.util.Date((long) nextRefreshJournal * 1000)))) + ".", log.L_VERBOSE);
            } catch (Exception e) {
                log.logMessage("PersistentDelayLine finished refreshing journal file. Error occured. Message: " + e, log.L_ERROR);
            }
        }
        try {
            log.logMessage("PersistentDelayLine try to refresh journal file. Before close.", log.L_VERBOSE);
            refreshJournal();
            log.logMessage("PersistentDelayLine finished refreshing journal file. Closing.", log.L_VERBOSE);
        } catch (Exception e) { ; }
    }

    /**
     * For test purposes
     */
    public static void main(String args[]) {
        try {
            ObjectInputStream istream = new ObjectInputStream(new FileInputStream(args[0]));
            FileOutputStream fout = new FileOutputStream("../logs/Dump_OutdialFile.txt");
            while (true) {
                String action = (String) istream.readObject();
                Object value = (Object) istream.readObject();
                if (action.equals(ADD_REQUEST)) {
                    PersistentDelayItem pValue = null;
                    if (value instanceof PersistentDelayItem) {
                        pValue = (PersistentDelayItem) value;
                    } else {
                        throw new ClassCastException("Could not cast " + value
                                                     + " to PersistentDelayItem for in request");
                    }
                    String temp = pValue.toString();
                    Date arrival = new Date((long) pValue.getArrivalTime() * 1000);
                    Date lastArrival = new Date((long) pValue.getLastArrivalTime() * 1000);
                    temp = temp + " ArrivalTime: " + arrival + " LastArrivalTime: "
                        + lastArrival + "\n\n";
                    fout.write(temp.getBytes());
                    fout.flush();
                }
            }
        } catch (Exception e) {
            System.out.println("The journal file has been read to end of file.");
        }
    }
}
