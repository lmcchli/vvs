package com.mobeon.masp.stream.jni;

public class Callback {
    // Command types ...
    public static final int PLAY_COMMAND = 1;
    public static final int RECORD_COMMAND = 2;
    public static final int JOIN_COMMAND = 3;
    public static final int UNJOIN_COMMAND = 4;
    public static final int CREATE_COMMAND = 5;
    public static final int DELETE_COMMAND = 6;
    public static final int DTMF_COMMAND = 7;
    // Status types
    // Success - 2xx
    public static final int OK = 200;
    public static final int OK_STOPPED = 201;
    public static final int OK_CANCELLED = 202;
    public static final int OK_JOINED = 203;
    public static final int OK_DELETED = 204;
    public static final int OK_MAX_DURATION = 205;
    public static final int OK_ABANDONED = 206;
    // Failed - 4xx
    public static final int FAILED = 400;
    public static final int FAILED_EXCEPTION = 401;
    public static final int FAILED_MIN_DURATION = 402;

    public int requestId = 0;
    public int command = 0;
    public int status = 0;
    public long data = 0;
    public boolean isOk = false;

    public Callback(long[] data) {
        requestId = (int)((data[0]>>16)&0x0000ffff);
        command =   (int)((data[0]>>12)&0x0000000f);
        status =    (int)((data[0]>>0) &0x00000fff);
        this.data = data[1];
        isOk = command != 0 && status != 0;
    }

    public Callback(int requestId, int command, int status) {
        this.requestId = requestId;
        this.command = command;
        this.status = status;
        isOk = command != 0 && status != 0;
    }

    public Callback(int requestId, int command, int status, long data) {
        this.requestId = requestId;
        this.command = command;
        this.status = status;
        this.data = data;
        isOk = command != 0 && status != 0;
    }
}
