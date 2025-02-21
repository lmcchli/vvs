/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.event.types;

/**
 * Fire in order to indicate an error. Carries the error code.
 */
public class MASError  extends MASEvent{
    public static final int BADFETCH = 1;
    public static final int NOAUTH = 2;
    public static final int SEMANTIC = 3;
    public static final int CONNECTION_BAD_DEST = 4;
    public static final int CONNECTION_NOAUTH = 5;
    public static final int CONNECTION_NORESOURCE = 6;
    public static final int NORESOURCE = 7;
    public static final int UNSUPPORTED_FORMAT = 8;
    public static final int UNSUPPORTED_ELEMENT = 9;

    private int error;

    public MASError(Object source) {
        super(source);
    }

    public MASError(Object source, int error) {
        super(source);
        this.error = error;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String toString() {
        switch (error) {
            case BADFETCH: return "BADFETCH";
            case NOAUTH: return "NOAUTH";
            case SEMANTIC:  return "SEMANTIC";
            case CONNECTION_BAD_DEST:   return "CONNECTION_BAD_DEST";
            case CONNECTION_NOAUTH:  return "CONNECTION_NOAUTH";
            case CONNECTION_NORESOURCE:  return "CONNECTION_NORESOURCE";
            case NORESOURCE:  return "NORESOURCE";
            case UNSUPPORTED_FORMAT:   return "UNSUPPORTED_FORMAT";
            case UNSUPPORTED_ELEMENT:  return "UNSUPPORTED_ELEMENT";
            default : return "UNKNOWN ERROR";
        }
    }
}
