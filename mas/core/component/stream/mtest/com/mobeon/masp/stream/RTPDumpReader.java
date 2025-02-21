package com.mobeon.masp.stream;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-jun-27
 * Time: 15:18:01
 * To change this template use File | Settings | File Templates.
 */
public class RTPDumpReader {
    private DataInputStream in;

    RTPDumpReader(String dumpfile) throws FileNotFoundException, IOException {
        FileInputStream f = new FileInputStream(dumpfile);

        while (f.read() != '\n');

        in = new DataInputStream(f);
    }

    public RTPDumpHeader readHeader() throws IOException {
        int seconds = in.readInt();
        int usecs = in.readInt();
        long source = in.readUnsignedShort() << 16;
        source = source | in.readUnsignedShort();
        int port = in.readUnsignedShort();
        in.skipBytes(2); // header seems to be word aligned

        return new RTPDumpHeader(seconds, usecs, source, port);
    }

    public RTPDumpPacket readPacket() throws IOException {
        RTPDumpPacket res = null;
        if (in.available() > 0) {
            int length = in.readUnsignedShort();
            int plen = in.readUnsignedShort();
            long offset = in.readUnsignedShort() << 16;
            offset = offset | in.readUnsignedShort();

            byte data[] = new byte[length - 8];
            in.readFully(data);

            if (plen > 0)
                res = new RTPPacket(length, plen, offset, data);
            else
                res = new RTPDumpPacket(length, plen, offset, data);           
        }
        return res;
    }

    public boolean available() throws IOException {
        return in.available() > 0;        
    }
}
