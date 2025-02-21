package com.mobeon.masp.stream;

import java.io.DataInput;
import java.io.IOException;
import java.io.DataOutput;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-jun-28
 * Time: 15:09:32
 * To change this template use File | Settings | File Templates.
 */
public class RTPDumpPacket {
        /*
    typedef struct {
      u_int16 length;     length of packet, including this header (may
                            be smaller than plen if not whole packet recorded)
      u_int16 plen;       actual header+payload length for RTP, 0 for RTCP
      u_int32 offset;     milliseconds since the start of recording
    } RD_packet_t;
    */
    private int rtpDumpLength;
    private int plen;
    private long offset;
    private byte[] data;

    public RTPDumpPacket() {        
    }

    public RTPDumpPacket(int rtpDumpLength, int plen, long offset, byte[] data) {
        this.rtpDumpLength = rtpDumpLength;
        this.plen = plen;
        this.offset = offset;
        this.data = data;
    }

    public int getRtpDumpLength() {
        return rtpDumpLength;
    }

    public int getPlen() {
        return plen;
    }

    public long getOffset() {
        return offset;
    }

    public byte[] getData() {
        return data;
    }

    public void setRtpDumpLength(int rtpDumpLength) {
        this.rtpDumpLength = rtpDumpLength;
    }

    public void setPlen(int plen) {
        this.plen = plen;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isRTPPacket() {
        return getPlen() > 0;
    }

    public boolean isRTCPPacket() {
        return getPlen() == 0;
    }

    public String toString() {
        return "len=" + rtpDumpLength + " plen=" + plen + " offset=" + offset;
    }
}
