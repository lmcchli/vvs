package com.mobeon.backend;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class MessageFlags {

    public static final int NR_OF_FLAGS=3;	// Number of flags below
    public static final int SEEN=0;		// MVAS: Set=Read, NotSet=New
    public static final int SAVED=1;		// MVAS: Called Stored
    public static final int URGENT=2;

    public static final int NOT_DEFINED=-1;
    public static final int NOT_SET=0;
    public static final int SET=1;

    int flags[];

    /**
     * Creates a MessageFlags object where all flags are set to "not defined".
     */
    public MessageFlags() {
        flags = new int[NR_OF_FLAGS];
        for (int i=0; i < NR_OF_FLAGS; i++)
            flags[i] = NOT_DEFINED;
    }

    /**
     * Creates a MessageFlags object where all flags are set to the same value as in the parameter.
     * @param  msgFlags      the MessageFlags object that shall be copied
     */
    MessageFlags(MessageFlags msgFlags) {
        flags = new int[NR_OF_FLAGS];
        for (int i=0; i < NR_OF_FLAGS; i++)
            flags[i] = msgFlags.get(i);
    }

     /**
     * Set the specied flag to the specified value.
     * @param  flag      the flag that shall be set
     * @param  value     the value that shall be set on the flag
     */
     public void set(int flag, int value) {
         if (flag < NR_OF_FLAGS)
             flags[flag] = value;
     }

     /**
     * Returns the value of the specified flag.
     * @param  flag      the flag that shall be fetched
     */
    public int get(int flag) {
        return flags[flag];
    }

     /**
     * Removes the value of the specified flag. The value is "not defined".
     * @param  flag      the flag that shall be removed
     */
    public void remove(int flag) {
        flags[flag] = NOT_DEFINED;
    }

     /**
     * Removes the value for all flags. The value is "not defined".
     */
    public void removeAll() {
        for (int i=0; i < NR_OF_FLAGS; i++)
            flags[i] = NOT_DEFINED;
    }

     /**
     * Clears all flags. The value is "not set".
     */
    public void clearAll() {
        for (int i=0; i < NR_OF_FLAGS; i++)
            flags[i] = NOT_SET;
    }
}


