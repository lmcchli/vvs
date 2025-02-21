package com.mobeon.backend;

import com.mobeon.backend.exception.DataException;
import com.mobeon.backend.exception.SyntaxException;
import com.mobeon.backend.exception.SystemException;
import com.mobeon.backend.exception.TimeoutException;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Folder handles the messages that exist in a folder.
 */
public class Folder {

    String folder;
    int numberOfMsgs[][];
    public static final int arrayLen = (int)Math.pow(2,MessageFlags.NR_OF_FLAGS);

// 'Static' bitsets that are used to know which elements of numberOfMsgs
// that shall be counted in getNumberOf().
    BitSet All = new BitSet(arrayLen);
    BitSet SeenNotSet = new BitSet(arrayLen);
    BitSet SeenSet = new BitSet(arrayLen);
    BitSet SavedNotSet = new BitSet(arrayLen);
    BitSet SavedSet = new BitSet(arrayLen);
    BitSet UrgentNotSet = new BitSet(arrayLen);
    BitSet UrgentSet = new BitSet(arrayLen);

    /**
     * Creates a folder for a specific subscriber.
     * @param  subscriber  the phone number of the subscriber, e.g. 161077.
     * @param  folderName  the name of the folder, e.g. Inbox.
     */
    Folder(String subscriber, String folderName) {
        int i,j;
        folder = System.getProperty("StoragePath") + subscriber + "/" + folderName + "/";
        numberOfMsgs = new int[Message.NR_OF_TYPES][arrayLen];

// Initiate the 'static' bitsets, QUICK and DIRTY: only fixed for three bits
        for (i=0; i<arrayLen; i++) {
            All.set(i);
            if (i<arrayLen/2) {
                UrgentNotSet.set(i);
            } else {
                UrgentSet.set(i);
            }
            if (i<2) {
                SavedNotSet.set(i);
            } else if (i>=2 & i<4) {
                SavedSet.set(i);
            } else if (i>=4 & i<6) {
                SavedNotSet.set(i);
            } else {
                SavedSet.set(i);
            }
            if (i%2 == 0) {
                SeenNotSet.set(i);
            } else {
                SeenSet.set(i);
            }
        }
    }

    /**
     * Returns the number of messages that matches the defined criterias.
     * @param  type   the type of messages, e.g. voice or video
     * @param  flags  the flags for the messages, e.g. not seen, urgent
     */
    public int getNumberOf(BitSet type, MessageFlags flags) throws SystemException, DataException, SyntaxException, TimeoutException {

        int sum = 0;
        BitSet include = (BitSet) All.clone();
        Vector msgList = new Vector();

        getMessageList(msgList, type, flags, Message.FIFO);

        if (flags.get(MessageFlags.SEEN) == MessageFlags.NOT_SET)
            include.and(SeenNotSet);
        else if (flags.get(MessageFlags.SEEN) == MessageFlags.SET)
            include.and(SeenSet);
        if (flags.get(MessageFlags.SAVED) == MessageFlags.NOT_SET)
            include.and(SavedNotSet);
        else if (flags.get(MessageFlags.SAVED) == MessageFlags.SET)
            include.and(SavedSet);
        if (flags.get(MessageFlags.URGENT) == MessageFlags.NOT_SET)
            include.and(UrgentNotSet);
        else if (flags.get(MessageFlags.URGENT) == MessageFlags.SET)
            include.and(UrgentSet);

        for (int i=0; i<Message.NR_OF_TYPES; i++) {
            for (int j=0; j<arrayLen; j++) {
                if (type.get(i) & include.get(j))
                    if (numberOfMsgs[i][j] != -1) {
                        sum = sum + numberOfMsgs[i][j];
                    }
                    else
                        return -1;
            }
        }

        return sum;
    }

    /**
     * Retrieves a list of messages that matches the defines criterias.
     * @param  msgList   a Vector where the message objects are stored
     * @param  type      the type of messages, e.g. voice or video
     * @param  flags     the flags for the messages, e.g. not seen, urgent
     * @param  order     the order that the messages shall have in the list
     */
    public void getMessageList(Vector msgList, BitSet type, MessageFlags flags, int order) throws SystemException, DataException, SyntaxException, TimeoutException {

        BufferedReader reader;
        String file = "";
        String theLine;
        File directory = new File(folder);
        String f[] = directory.list();
        Vector files = new Vector();
        // Remove files starting in .
        for (int i = 0; i < f.length; i++) {
            if (! f[i].startsWith(".")) {
                files.add(f[i]);
            }
        }
        int i,j;
        int index;
        boolean cont = true;
        int typeIndex;
        int msgListIndex = 0;

        BitSet updated = (BitSet) All.clone();

        if (flags.get(MessageFlags.SEEN) == MessageFlags.NOT_SET)
            updated.and(SeenNotSet);
        else if (flags.get(MessageFlags.SEEN) == MessageFlags.SET)
            updated.and(SeenSet);
        if (flags.get(MessageFlags.SAVED) == MessageFlags.NOT_SET)
            updated.and(SavedNotSet);
        else if (flags.get(MessageFlags.SAVED) == MessageFlags.SET)
            updated.and(SavedSet);
        if (flags.get(MessageFlags.URGENT) == MessageFlags.NOT_SET)
            updated.and(UrgentNotSet);
        else if (flags.get(MessageFlags.URGENT) == MessageFlags.SET)
            updated.and(UrgentSet);

        for (i=0; i<Message.NR_OF_TYPES; i++) {
            for (j=0; j<arrayLen; j++) {
                if (type.get(i) & updated.get(j)) {
                    numberOfMsgs[i][j] = 0;
                }
                else
                    numberOfMsgs[i][j] = -1;
            }
        }

        i=0;
        while (cont) {
            try {
                typeIndex = -1;
                index = 0;
                if (files != null) {
                    file = folder + (String) files.get(i++);
                }
                reader = new BufferedReader(new FileReader(file));
                theLine = reader.readLine();
                if (theLine.indexOf("VOICE") != -1) {
                    if (type.get(Message.VOICE))
                        typeIndex = Message.VOICE;
                }
                else if (theLine.indexOf("VIDEO") != -1) {
                    if (type.get(Message.VIDEO))
                        typeIndex = Message.VIDEO;
                }
                else if (theLine.indexOf("FAX") != -1) {
                    if (type.get(Message.FAX))
                        typeIndex = Message.FAX;
                }
                else if (theLine.indexOf("EMAIL") != -1) {
                    if (type.get(Message.EMAIL))
                        typeIndex = Message.EMAIL;
                }
                if (typeIndex != -1) {
                    if (theLine.indexOf("SEEN") != -1) {
                        index = index + (int)Math.pow(2,MessageFlags.SEEN);
                    }
                    if (theLine.indexOf("SAVED") != -1) {
                        index = index + (int)Math.pow(2,MessageFlags.SAVED);
                    }
                    if (theLine.indexOf("URGENT") != -1) {
                        index = index + (int)Math.pow(2,MessageFlags.URGENT);
                    }
                    if (numberOfMsgs[typeIndex][index] != -1) {
                        numberOfMsgs[typeIndex][index]++;
                        Message msg=new Message(file);
                        msgList.insertElementAt(msg,msgListIndex++);
                    }
                }
            } catch(FileNotFoundException e) {
                cont=false;
                throw new DataException("Folder.getMessageList.FileNotFoundException:" + file);
            } catch(IOException e) {
                cont=false;
                throw new SystemException("Folder.getMessageList.IOException:" + file);
            } catch(ArrayIndexOutOfBoundsException e) {    // This is the normal end of the loop
                cont=false;
            }
        }
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}


