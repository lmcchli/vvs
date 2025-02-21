package com.mobeon.masp.stream;

import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataOutput;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-jun-29
 * Time: 10:36:49
 * To change this template use File | Settings | File Templates.
 */
public class RTPDumpWriter {
    private DataOutputStream out;

    public RTPDumpWriter(String dumpfile) throws IOException {
        FileOutputStream f = new FileOutputStream(dumpfile);

        String s = new String("#!rtpplay1.0 127.0.0.1/23000\n");
        f.write(s.getBytes());

        out = new DataOutputStream(f);
    }

    public void writeHeader(RTPDumpHeader header) throws IOException {
        out.writeInt(header.getSeconds());
        out.writeInt(header.getUsecs());
        out.writeInt((int) header.getSource());
        out.writeShort(header.getPort());
        out.writeShort(0); // padding
    }

    public void writePacket(RTPDumpPacket packet) throws IOException {
        out.writeShort(packet.getRtpDumpLength());
        out.writeShort(packet.getPlen());
        out.writeInt((int) packet.getOffset());
        out.write(packet.getData());
    }
}
