package com.mobeon.masp.stream;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-jul-30
 * Time: 16:15:37
 * To change this template use File | Settings | File Templates.
 */
public class RTPPacket extends RTPDumpPacket {
    private byte version;
    private boolean padding;
    private boolean extension;
    private byte contributingSources;
    private boolean marker;
    private byte payloadType;
    private long extendedSequenceNumber;
    private long timestamp;
    private long ssrc;

    public RTPPacket() {
        super();
    }

    public RTPPacket(int length, int plen, long offset, byte[] data) {
        super(length, plen, offset, data);        

        try {
            version = (byte) ((int) (data[0] & 0xff) >> 6);
            padding = (((int) (data[0] & 0xff) >> 5) & 0x1) > 0;
            extension = (((int) (data[0] & 0xff) >> 4) & 0x1) > 0;
            contributingSources = (byte) (((int) (data[0] & 0xff) >> 3) & 0xf);
            marker = (data[1] & 0x80) != 0;
            payloadType = (byte) (data[1] & 0x7f);

            extendedSequenceNumber = (int) (data[2] & 0xff) << 8;
            extendedSequenceNumber |= (int) (data[3] & 0xff);

            timestamp = (int) (data[4] & 0xff) << 24;
            timestamp |= (int) (data[5] & 0xff) << 16;
            timestamp |= (int) (data[6] & 0xff) << 8;
            timestamp |= (int) (data[7] & 0xff);

            ssrc = (int) (data[8] & 0xff) << 24;
            ssrc |= (int) (data[9] & 0xff) << 16;
            ssrc |= (int) (data[10] & 0xff) << 8;
            ssrc |= (int) (data[11] & 0xff);
        } catch (RuntimeException e) {
            e.printStackTrace(System.out);

            for (int i = 0; i < data.length; ++i)
                System.out.println(Integer.toBinaryString((int) (data[i] & 0xff)));
            throw e;
        }
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public boolean getPadding() {
        return padding;
    }

    public void setPadding(boolean padding) {
        this.padding = padding;
    }

    public boolean getExtension() {
        return extension;
    }

    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    public byte getContributingSources() {
        return contributingSources;
    }

    public void setContributingSorces(byte contributingSources) {
        this.contributingSources = contributingSources;
    }

    public boolean getMarker() {
        return marker;
    }

    public void setMarker(boolean marker) {
        this.marker = marker;
    }

    public byte getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(byte payloadType) {
        this.payloadType = payloadType;
    }

    public long getExtendedSequenceNumber() {
        return extendedSequenceNumber;
    }

    public void setExtendedSequenceNumber(long extendedSequenceNumber) {
        this.extendedSequenceNumber = extendedSequenceNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getSSRC() {
        return ssrc;
    }

    public void setSSRC(long ssrc) {
        this.ssrc = ssrc;
    }

    public boolean isDTMF() {
        return getPayloadType() == 101;
    }

    public int getDTMFToken() {
        int dataOffset = 12 + 4 * contributingSources;

        return ((int)(getData()[dataOffset]) & 0xFF);
    }

    public int getDTMFDuration() {
        int dataOffset = 12 + 4 * contributingSources;

        return (int)(getData()[dataOffset + 1]) << 8;
    }

    public int getDTMFVolume() {
        int dataOffset = 12 + 4 * contributingSources;

        return ((int)(getData()[dataOffset + 1]) & 0x3F);
    }

    private boolean equalsPayloadData(RTPPacket p) {
        if (getData().length == p.getData().length) {
            for (int i = 12 + contributingSources * 4; i < getData().length; ++i) {
                if (getData()[i] != p.getData()[i]) {
                    return false;
                }
            }
            return true;
        } else {            
            return false;
        }
    }

    public boolean equals(Object o) {
        if (o != null && o instanceof RTPPacket) {
            RTPPacket p = (RTPPacket) o;

            return version == p.getVersion() &&
                   padding == p.getPadding() &&
                   extension == p.getExtension() &&
                   contributingSources == p.getContributingSources() &&
                   //marker == p.getMarker() &&
                   payloadType == p.getPayloadType() &&
                   equalsPayloadData(p);
        } else {
            return false;
        }
    }

    public String toString() {
        return "RTP: " +
               "V: " + version + " " +
               "P: " + padding + " " +
               "x:" + extension + " " +
               "CC:" + contributingSources + " " +
               "M:" + marker + " " +
               "PT:" + payloadType + " " +
               "SN:" + (extendedSequenceNumber >> 16) + ":" + (extendedSequenceNumber & 0xffff) + " " + 
               "TS:" + timestamp;
    }
}