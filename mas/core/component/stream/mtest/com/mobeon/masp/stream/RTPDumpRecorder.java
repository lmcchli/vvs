package com.mobeon.masp.stream;

import java.util.Date;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-jun-28
 * Time: 15:51:16
 * To change this template use File | Settings | File Templates.
 */
public class RTPDumpRecorder implements Runnable{
    private static final long SELECT_TIMEOUT = 20;
    private InetAddress host;
    private int rtpPort;
    private int rtcpPort;
    private long idleTimeout;
    private long startOffset;
    private RTPDump dump;

    private AtomicBoolean running;
    private Thread recordThread;
    private DatagramChannel rtpch;
    private DatagramChannel rtcpch;
    private Selector sel;

    public RTPDumpRecorder(String host, int rtpPort, int rtcpPort, long idleTimeout) throws IOException, UnknownHostException {
        running = new AtomicBoolean();
        this.host = InetAddress.getByName(host);
        this.rtpPort = rtpPort;
        this.rtcpPort = rtcpPort;
        this.idleTimeout = idleTimeout;

        sel = Selector.open();

        rtpch = DatagramChannel.open();
        rtpch.socket().setReuseAddress(true);
        rtpch.socket().bind(new InetSocketAddress(host, rtpPort));
        rtpch.configureBlocking(false);
        rtpch.register(sel, SelectionKey.OP_READ, rtpPort);

        rtcpch = DatagramChannel.open();
        rtcpch.socket().setReuseAddress(true);
        rtcpch.socket().bind(new InetSocketAddress(host, rtcpPort));
        rtcpch.configureBlocking(false);
        rtcpch.register(sel, SelectionKey.OP_READ, rtcpPort);
    }

    public void record() {
        recordThread = new Thread(this);
        recordThread.start();
    }

    public void stop() {
        if (recordThread != null) {
            running.set(false);
            boolean joined = false;
            while (!joined) {
                try {
                    recordThread.join();
                    joined = true;
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void waitUntilFinished() {
        boolean joined = false;
        while (!joined) {
            try {
                recordThread.join();
                joined = true;
            } catch (InterruptedException e) {
            }
        }
    }

    public void close() throws IOException {
        sel.close();
        rtpch.socket().close();
        rtcpch.socket().close();

    }

    public RTPDump getDump() {
        return dump;
    }

    public void run() {
        running.set(true);
        try {
            recordDump();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running.set(false);
    }

    public void recordRTPPacket(long offset, ByteBuffer pkt) {
        //System.out.println("Received RTP on port: " + rtpPort);
        byte data[] = new byte[pkt.remaining()];
        pkt.get(data);

        RTPPacket p = new RTPPacket(data.length + 8, data.length, offset, data);

        dump.addPacket(p);
    }

    public void recordRTCPPacket(long offset, ByteBuffer pkt) {
        byte data[] = new byte[pkt.remaining()];
        pkt.get(data);

        RTPDumpPacket p = new RTPDumpPacket(data.length + 8, 0, offset, data);

        dump.addPacket(p);
    }

    public void recordDump() throws IOException {
        dump = new RTPDump("Recording/"+ host.getHostName() + "/" + rtpPort);
        startOffset = System.currentTimeMillis();
        dump.setRecordStartSeconds((int) startOffset / 1000);
        dump.setRecordStartUsecs((int) startOffset % 1000);
        dump.setSource(0);
        dump.setPort(rtpPort);      

        ByteBuffer pkt = ByteBuffer.allocate(1500);

        long idleCount = 0;
        while (running.get() && idleCount < (idleTimeout / SELECT_TIMEOUT)) {
            ++idleCount;
            while (running.get() && sel.select(SELECT_TIMEOUT) > 0) {
                long offset = System.currentTimeMillis() - startOffset;
                Set<SelectionKey> keys = sel.selectedKeys();
                Iterator<SelectionKey> keyIter = keys.iterator();
                while (keyIter.hasNext()) {
                    SelectionKey key = (SelectionKey) keyIter.next();
                    idleCount = 0;
                    DatagramChannel ch = (DatagramChannel) key.channel();
                    pkt.clear();
                    ch.receive(pkt);
                    pkt.flip();
                    Integer port = (Integer) key.attachment();                    
                    if (port == rtpPort) {
                        recordRTPPacket(offset, pkt);
                    } else {
                        recordRTCPPacket(offset, pkt);
                    }

                    keyIter.remove();
                }
            }
        }
    }
}
