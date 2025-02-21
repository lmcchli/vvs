package com.mobeon.backend;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class Greeting {

    public static final int ALL_CALLS=0;
    public static final int CALLER_DEPENDENT=1;
    public static final int TEMPORARY=2;
    public static final int NO_ANSWER=3;
    public static final int BUSY=4;
    public static final int OUT_OF_HOURS=5;
    public static final int ABSENCE=6;
    public static final int SPOKEN_NAME=7;

    public static final int VOICE=0;
    public static final int VIDEO=1;

    String filename;

    /**
     * Creates a greeting object
     */
    public Greeting() {
    }

    /**
     * Returns a reference to the greeting file.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Stores a reference to the greeting file.
     * @param  file      path to the greeting file
     */
    public void setFilename(String file) {
        filename = file;
    }

}


