package com.mobeon.masp.stream;

import java.io.DataInput;
import java.io.IOException;
import java.io.DataOutput;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-jun-28
 * Time: 15:12:05
 * To change this template use File | Settings | File Templates.
 */
public class RTPDumpHeader {
    /*
    typedef struct {
      struct timeval start;   start of recording (GMT)
      u_int32 source;         network source (multicast address)
      u_int16 port;           UDP port
    } RD_hdr_t;
    */
    private int seconds;
    private int usecs;
    private long source;
    private int port;

    public RTPDumpHeader() {
    }

    public RTPDumpHeader(int seconds, int usecs, long source, int port) {
        this.seconds = seconds;
        this.usecs = usecs;
        this.source = source;
        this.port = port;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getUsecs() {
        return usecs;
    }

    public long getSource() {
        return source;
    }

    public int getPort() {
        return port;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public void setUsecs(int usecs) {
        this.usecs = usecs;
    }

    public void setSource(long source) {
        this.source = source;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String toString() {
        return "s=" + seconds + " us=" + usecs + " src=" + source + " port=" + port;
    }
}
